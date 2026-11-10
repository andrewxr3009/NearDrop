package dev.neardrop.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import dev.neardrop.core.DiscoveryProvenance
import dev.neardrop.core.NearbyDevice
import dev.neardrop.core.SharedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class ReceiveOffer(val endpointId: String, val sender: String, val names: List<String>, val sizes: List<Long>, val totalBytes: Long)
data class ProximityState(
    val advertising: Boolean = false,
    val discovering: Boolean = false,
    val status: String = "Pronto",
    val devices: List<NearbyDevice> = emptyList(),
    val pendingConnectionId: String? = null,
    val pendingConnectionName: String? = null,
    val pendingConnectionIncoming: Boolean? = null,
    val verificationCode: String? = null,
    val offer: ReceiveOffer? = null,
    val progress: Float? = null,
    val transferMessage: String? = null
)

/** Real Android-to-Android NearDrop transport over Google Play services Nearby Connections. */
class NearbyTransferManager(context: Context) {
    private val app = context.applicationContext
    private val client = Nearby.getConnectionsClient(app)
    private val serviceId = app.packageName + ".neardrop"
    private val io = Executors.newSingleThreadExecutor()
    private val _state = MutableStateFlow(ProximityState())
    val state: StateFlow<ProximityState> = _state.asStateFlow()
    private val endpointNames = ConcurrentHashMap<String, String>()
    private val pendingByEndpoint = ConcurrentHashMap<String, ReceiveOffer>()
    private val outgoing = ConcurrentHashMap<String, List<SharedItem>>()
    private val outgoingIndexes = ConcurrentHashMap<String, Int>()
    private val transferIds = ConcurrentHashMap<String, String>()
    private val payloads = ConcurrentHashMap<Long, Pair<String, Int>>()
    private val incomingIndexes = ConcurrentHashMap<String, Int>()
    private val receivedBytesByEndpoint = ConcurrentHashMap<String, Long>()
    private val receiverPayloads = ConcurrentHashMap<Long, Pair<String, Int>>()
    private val receivedFiles = ConcurrentHashMap<Long, Payload>()
    private val deviceName by lazy { android.os.Build.MODEL?.take(32)?.ifBlank { "Android" } ?: "Android" }
    private var visibilityTimeout: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private val lifecycle = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            endpointNames[endpointId] = info.endpointName.take(64).ifBlank { "Dispositivo Android" }
            _state.value = _state.value.copy(
                pendingConnectionId = endpointId,
                pendingConnectionName = endpointNames[endpointId],
                pendingConnectionIncoming = info.isIncomingConnection,
                verificationCode = info.authenticationToken,
                status = "Compare o código de segurança com ${endpointNames[endpointId]}"
            )
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                _state.value = _state.value.copy(status = "Conectado a ${endpointNames[endpointId] ?: "dispositivo"}")
                if (outgoing.containsKey(endpointId)) sendOffer(endpointId)
            } else {
                _state.value = _state.value.copy(status = "Não foi possível conectar (${result.status.statusCode})", transferMessage = "Conexão recusada ou interrompida")
                outgoing.remove(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String) {
            endpointNames.remove(endpointId)
            outgoing.remove(endpointId)
            pendingByEndpoint.remove(endpointId)
            _state.value = _state.value.copy(
                devices = _state.value.devices.filterNot { it.id == endpointId },
                pendingConnectionId = null, pendingConnectionName = null, pendingConnectionIncoming = null, verificationCode = null,
                status = "Conexão encerrada"
            )
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) runCatching { payload.asBytes()?.let { handleMessage(endpointId, it) } }
            else if (payload.type == Payload.Type.FILE) {
                val pending = pendingByEndpoint[endpointId] ?: return
                val index = incomingIndexes[endpointId] ?: 0
                receiverPayloads[payload.id] = endpointId to index
                receivedFiles[payload.id] = payload
                _state.value = _state.value.copy(status = "Recebendo ${pending.names.getOrNull(index) ?: "arquivo"}")
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.totalBytes > 0) _state.value = _state.value.copy(progress = update.bytesTransferred.toFloat() / update.totalBytes.toFloat())
            if (update.status != PayloadTransferUpdate.Status.SUCCESS) {
                if (update.status == PayloadTransferUpdate.Status.FAILURE) {
                    _state.value = _state.value.copy(status = "Falha na transferência", transferMessage = "O arquivo não foi recebido completamente")
                }
                return
            }
            val sent = payloads.remove(update.payloadId)
            if (sent != null) {
                val (_, index) = sent
                val total = outgoing[endpointId]?.size ?: 1
                _state.value = _state.value.copy(progress = ((index + 1).toFloat() / total).coerceAtMost(0.99f), status = "Arquivo ${index + 1} transferido; confirmando gravação")
                return
            }
            val received = receiverPayloads.remove(update.payloadId) ?: return
            val (peer, index) = received
            persistReceivedFile(endpointId, update.payloadId, peer, index)
        }
    }

    fun startAdvertisingForTenMinutes() {
        client.stopAdvertising()
        visibilityTimeout?.let(handler::removeCallbacks)
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        client.startAdvertising(deviceName, serviceId, lifecycle, options)
            .addOnSuccessListener {
                _state.value = _state.value.copy(advertising = true, status = "Visível por até 10 minutos")
                val stop = Runnable { stopAdvertising() }
                visibilityTimeout = stop
                handler.postDelayed(stop, TimeUnit.MINUTES.toMillis(10))
            }
            .addOnFailureListener { error -> _state.value = _state.value.copy(advertising = false, status = "Não foi possível ativar a visibilidade: ${error.localizedMessage ?: "erro Nearby"}") }
    }

    fun stopAdvertising() {
        visibilityTimeout?.let(handler::removeCallbacks)
        visibilityTimeout = null
        client.stopAdvertising()
        _state.value = _state.value.copy(advertising = false, status = "Visibilidade desativada")
    }

    fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        client.startDiscovery(serviceId, object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                val name = info.endpointName.take(64).ifBlank { "Dispositivo Android" }
                endpointNames[endpointId] = name
                val device = NearbyDevice(endpointId, name, "Nearby Connections", DiscoveryProvenance.REAL)
                _state.value = _state.value.copy(devices = (_state.value.devices.filterNot { it.id == endpointId } + device), status = "Encontrado: $name")
            }
            override fun onEndpointLost(endpointId: String) {
                _state.value = _state.value.copy(devices = _state.value.devices.filterNot { it.id == endpointId })
            }
        }, options).addOnSuccessListener { _state.value = _state.value.copy(discovering = true, status = "Procurando dispositivos NearDrop…") }
            .addOnFailureListener { error -> _state.value = _state.value.copy(discovering = false, status = "Falha ao iniciar descoberta: ${error.localizedMessage ?: "Nearby indisponível"}") }
    }

    fun stopDiscovery() {
        client.stopDiscovery()
        _state.value = _state.value.copy(discovering = false)
    }

    fun connectAndSend(device: NearbyDevice, items: List<SharedItem>) {
        if (items.isEmpty()) return
        if (items.size > 50) {
            _state.value = _state.value.copy(transferMessage = "Selecione no máximo 50 arquivos por envio")
            return
        }
        if (items.any { (it.sizeBytes ?: 0L) > 2L * 1024 * 1024 * 1024 }) {
            _state.value = _state.value.copy(transferMessage = "Cada arquivo deve ter até 2 GB")
            return
        }
        if (items.sumOf { (it.sizeBytes ?: 0L).coerceAtLeast(0L) } > 5L * 1024 * 1024 * 1024) {
            _state.value = _state.value.copy(transferMessage = "O envio total deve ter até 5 GiB")
            return
        }
        outgoing[device.id] = items
        outgoingIndexes[device.id] = 0
        _state.value = _state.value.copy(status = "Conectando a ${device.displayName}…", transferMessage = null, progress = null)
        client.requestConnection(deviceName, device.id, lifecycle)
            .addOnFailureListener { error -> outgoing.remove(device.id); _state.value = _state.value.copy(status = "Falha ao solicitar conexão", transferMessage = error.localizedMessage) }
    }

    fun acceptIncomingConnection() {
        val id = _state.value.pendingConnectionId ?: return
        client.acceptConnection(id, payloadCallback)
        _state.value = _state.value.copy(pendingConnectionId = null, pendingConnectionName = null, pendingConnectionIncoming = null, verificationCode = null, status = "Conectando com segurança…")
    }

    fun rejectIncomingConnection() {
        val id = _state.value.pendingConnectionId ?: return
        client.rejectConnection(id)
        _state.value = _state.value.copy(pendingConnectionId = null, pendingConnectionName = null, pendingConnectionIncoming = null, verificationCode = null, status = "Conexão recusada")
    }

    fun acceptOffer() {
        val offer = _state.value.offer ?: return
        pendingByEndpoint[offer.endpointId] = offer
        incomingIndexes[offer.endpointId] = 0
        receivedBytesByEndpoint[offer.endpointId] = 0L
        sendJson(offer.endpointId, JSONObject().put("type", "accept").put("transfer", transferIds[offer.endpointId]))
        _state.value = _state.value.copy(offer = null, progress = 0f, status = "Recebimento autorizado")
    }

    fun rejectOffer() {
        val offer = _state.value.offer ?: return
        sendJson(offer.endpointId, JSONObject().put("type", "reject").put("transfer", transferIds[offer.endpointId]))
        _state.value = _state.value.copy(offer = null, status = "Solicitação recusada")
    }

    private fun sendOffer(endpointId: String) {
        val items = outgoing[endpointId] ?: return
        val id = UUID.randomUUID().toString()
        transferIds[endpointId] = id
        val names = JSONArray()
        val sizes = JSONArray()
        items.forEach { names.put(safeDisplayName(it.displayName)); sizes.put(it.sizeBytes ?: -1L) }
        sendJson(endpointId, JSONObject().put("type", "offer").put("transfer", id).put("names", names).put("sizes", sizes))
        _state.value = _state.value.copy(status = "Solicitação enviada; aguardando autorização", progress = 0f)
    }

    private fun handleMessage(endpointId: String, bytes: ByteArray) {
        if (bytes.size > 16_384) return
        val msg = JSONObject(String(bytes, Charsets.UTF_8))
        when (msg.optString("type")) {
            "offer" -> {
                val transfer = msg.optString("transfer")
                if (transfer.length !in 1..64) return
                val rawNames = msg.optJSONArray("names") ?: return
                val rawSizes = msg.optJSONArray("sizes") ?: return
                if (rawNames.length() !in 1..50 || rawNames.length() != rawSizes.length()) return
                val names = (0 until rawNames.length()).map { safeDisplayName(rawNames.optString(it)) }
                val sizes = (0 until rawSizes.length()).map { rawSizes.optLong(it, -1L).coerceAtMost(2L * 1024 * 1024 * 1024) }
                if (sizes.any { it < -1L }) return
                if (sizes.filter { it > 0L }.sum() > 5L * 1024 * 1024 * 1024) return
                transferIds[endpointId] = transfer
                val offer = ReceiveOffer(endpointId, endpointNames[endpointId] ?: "Dispositivo próximo", names, sizes, sizes.filter { it > 0 }.sum())
                _state.value = _state.value.copy(offer = offer, status = "Solicitação de ${offer.sender}")
            }
            "accept" -> if (msg.optString("transfer") == transferIds[endpointId]) sendNextFile(endpointId)
            "reject" -> if (msg.optString("transfer") == transferIds[endpointId]) {
                outgoing.remove(endpointId)
                _state.value = _state.value.copy(status = "O destinatário recusou", transferMessage = "Nenhum arquivo foi enviado")
            }
            "file_sent" -> {
                if (msg.optString("transfer") != transferIds[endpointId]) return
                val index = msg.optInt("index", -1)
                val current = outgoingIndexes[endpointId] ?: return
                if (index != current) return
                outgoingIndexes[endpointId] = current + 1
                sendNextFile(endpointId)
            }
        }
    }

    private fun sendNextFile(endpointId: String) {
        val items = outgoing[endpointId] ?: return
        val transfer = transferIds[endpointId] ?: return
        val sent = outgoingIndexes[endpointId] ?: 0
        if (sent >= items.size) {
            outgoing.remove(endpointId)
            outgoingIndexes.remove(endpointId)
            _state.value = _state.value.copy(progress = 1f, status = "Envio concluído", transferMessage = "Todos os arquivos foram entregues")
            return
        }
        val item = items[sent]
        io.execute {
            try {
                val pfd = app.contentResolver.openFileDescriptor(item.uri, "r") ?: error("Não foi possível abrir ${item.displayName}")
                val payload = Payload.fromFile(pfd)
                payloads[payload.id] = transfer to sent
                client.sendPayload(endpointId, payload)
                _state.value = _state.value.copy(status = "Enviando ${safeDisplayName(item.displayName)}", progress = sent.toFloat() / items.size)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(status = "Falha ao abrir arquivo", transferMessage = t.localizedMessage ?: "Verifique o acesso ao arquivo")
            }
        }
    }

    private fun persistReceivedFile(endpointId: String, payloadId: Long, peer: String, index: Int) {
        val offer = pendingByEndpoint[peer] ?: return
        val name = offer.names.getOrNull(index) ?: "arquivo-$index"
        val payload = receivedFiles.remove(payloadId) ?: return
        val sourceUri = payload.asFile()?.asUri()
        if (sourceUri == null) {
            _state.value = _state.value.copy(status = "Falha ao salvar arquivo", transferMessage = "O transporte não forneceu um arquivo acessível")
            return
        }
        io.execute {
            var destination: Uri? = null
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    if (android.os.Build.VERSION.SDK_INT >= 29) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/NearDrop")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }
                destination = if (android.os.Build.VERSION.SDK_INT >= 29) {
                    app.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                } else {
                    val folder = File(app.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "NearDrop").apply { mkdirs() }
                    Uri.fromFile(File(folder, name))
                } ?: error("Não foi possível criar o arquivo de destino")
                val input = app.contentResolver.openInputStream(sourceUri) ?: error("Arquivo recebido indisponível")
                val output = if (android.os.Build.VERSION.SDK_INT >= 29) app.contentResolver.openOutputStream(destination, "w") else java.io.FileOutputStream(File(destination.path!!))
                var copied = 0L
                input.use { source -> output?.use { target ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        copied += count
                        if (copied > 2L * 1024 * 1024 * 1024) error("Arquivo excede o limite de 2 GB")
                        if (copied + (receivedBytesByEndpoint[peer] ?: 0L) > 5L * 1024 * 1024 * 1024) error("Transferência excede o limite de 5 GiB")
                        target.write(buffer, 0, count)
                    }
                    val expected = offer.sizes.getOrNull(index) ?: -1L
                    if (expected >= 0 && expected != copied) error("Tamanho do arquivo não corresponde aos metadados")
                } ?: error("Não foi possível gravar o arquivo") }
                if (android.os.Build.VERSION.SDK_INT >= 29) {
                    app.contentResolver.update(destination, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
                }
                val transfer = transferIds[peer] ?: return@execute
                receivedBytesByEndpoint[peer] = (receivedBytesByEndpoint[peer] ?: 0L) + copied
                sendJson(peer, JSONObject().put("type", "file_sent").put("transfer", transfer).put("index", index))
                val next = index + 1
                incomingIndexes[peer] = next
                val finished = next >= offer.names.size
                _state.value = _state.value.copy(
                    progress = next.toFloat() / offer.names.size,
                    status = if (finished) "Recebimento concluído" else "Arquivo $next de ${offer.names.size} recebido",
                    transferMessage = if (finished) "Arquivos salvos em Downloads/NearDrop" else null
                )
                if (finished) {
                    pendingByEndpoint.remove(peer)
                    receivedBytesByEndpoint.remove(peer)
                }
            } catch (t: Throwable) {
                destination?.let { runCatching { app.contentResolver.delete(it, null, null) } }
                _state.value = _state.value.copy(status = "Falha ao salvar arquivo", transferMessage = t.localizedMessage ?: "Armazenamento indisponível")
            }
        }
    }

    private fun sendJson(endpointId: String, message: JSONObject) {
        runCatching { client.sendPayload(endpointId, Payload.fromBytes(message.toString().toByteArray(Charsets.UTF_8))) }
    }

    private fun safeDisplayName(raw: String): String {
        val leaf = raw.substringAfterLast('/').substringAfterLast('\\').replace(Regex("[\\p{Cntrl}]"), "_").trim().take(120)
        return leaf.takeIf { it.isNotBlank() && it != "." && it != ".." } ?: "arquivo"
    }
}
