package dev.neardrop.feature.send

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.neardrop.core.SharedItem
import dev.neardrop.core.NearbyDevice
import dev.neardrop.data.ProximityState

private val IosBlue = Color(0xFF0A84FF)
private val SecondaryText = Color(0xFF98989F)

@Composable
fun SendScreen(items: List<SharedItem>, onPick: () -> Unit, onBack: () -> Unit, onShare: () -> Unit, sharedFromAnotherApp: Boolean = false, devices: List<NearbyDevice> = emptyList(), discoveryStatus: String = "", proximityState: ProximityState = ProximityState(), onSendNearby: (NearbyDevice) -> Unit = {}, onConfirmConnection: () -> Unit = {}, onRejectConnection: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().displayCutoutPadding().navigationBarsPadding()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(start = 12.dp, top = 8.dp)) { Text("‹   Início", color = IosBlue) }
        Text(if (sharedFromAnotherApp) "Compartilhado\ncom NearDrop" else "Preparar envio", modifier = Modifier.padding(horizontal = 24.dp), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(if (sharedFromAnotherApp) "O item veio do menu Compartilhar de outro aplicativo." else "Selecione arquivos para preparar o compartilhamento.", modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp), color = SecondaryText, style = MaterialTheme.typography.bodyMedium)

        OutlinedButton(onClick = onPick, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp).height(52.dp).animateContentSize(), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = IosBlue)) {
            Icon(Icons.Rounded.Share, contentDescription = null)
            Spacer(Modifier.width(9.dp))
            Text("Adicionar arquivos", fontWeight = FontWeight.SemiBold)
        }

        Text("ITENS SELECIONADOS  ·  ${items.size}", modifier = Modifier.padding(start = 26.dp, top = 14.dp, bottom = 10.dp), color = IosBlue, style = MaterialTheme.typography.labelMedium)
        if (items.isEmpty()) {
            EmptySelection(Modifier.weight(1f).fillMaxWidth())
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.uri.toString() }) { item ->
                    Surface(Modifier.animateItem(), shape = RoundedCornerShape(16.dp), color = Color(0xFF1C1C1E), border = BorderStroke(1.dp, Color.White.copy(alpha = .08f))) {
                        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(44.dp), shape = RoundedCornerShape(13.dp), color = Color(0xFF25252A)) {
                                Box(contentAlignment = Alignment.Center) { FileGlyph() }
                            }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.displayName, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                                Text(item.sizeBytes?.let(::formatBytes) ?: "Tamanho indisponível", color = SecondaryText, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        Text("DISPOSITIVOS NEARDROP", modifier = Modifier.padding(start = 26.dp, top = 12.dp, bottom = 8.dp), color = IosBlue, style = MaterialTheme.typography.labelMedium)
        if (devices.isEmpty()) {
            Text(discoveryStatus.ifBlank { "Procurando dispositivos próximos…" }, modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 8.dp), color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        } else {
            LazyColumn(Modifier.heightIn(max = 176.dp), contentPadding = PaddingValues(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(devices, key = { it.id }) { device ->
                    Surface(shape = RoundedCornerShape(15.dp), color = Color(0xFF1C1C1E), border = BorderStroke(1.dp, Color.White.copy(alpha = .08f))) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(34.dp), shape = CircleShape, color = Color(0xFF26394F)) { Box(contentAlignment = Alignment.Center) { Text("N", color = IosBlue, fontWeight = FontWeight.SemiBold) } }
                            Column(Modifier.weight(1f).padding(start = 11.dp)) {
                                Text(device.displayName, color = Color.White, style = MaterialTheme.typography.titleSmall)
                                Text("Perto · ${device.transport}", color = SecondaryText, style = MaterialTheme.typography.labelSmall)
                            }
                            Button(onClick = { onSendNearby(device) }, enabled = items.isNotEmpty(), contentPadding = PaddingValues(horizontal = 15.dp, vertical = 7.dp)) { Text("Enviar") }
                        }
                    }
                }
            }
        }

        proximityState.pendingConnectionName?.takeIf { proximityState.pendingConnectionIncoming == false }?.let { peerName ->
            Surface(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF1C1C1E), border = BorderStroke(1.dp, Color.White.copy(alpha = .10f))) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Confirme o dispositivo", color = Color.White, style = MaterialTheme.typography.titleSmall)
                    Text("Compare este código com $peerName. Aprove nos dois aparelhos somente se forem iguais.", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                    Text(proximityState.verificationCode.orEmpty(), modifier = Modifier.align(Alignment.CenterHorizontally), color = IosBlue, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onRejectConnection, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        Button(onClick = onConfirmConnection, modifier = Modifier.weight(1f)) { Text("Código igual") }
                    }
                }
            }
        }

        Surface(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp), shape = RoundedCornerShape(18.dp), color = Color(0xFF1C1C1E), border = BorderStroke(1.dp, Color.White.copy(alpha = .08f))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Quick Share", color = Color.White, style = MaterialTheme.typography.titleSmall)
                Text("O Android abrirá o menu de compartilhamento. Quick Share aparecerá como destino se estiver disponível neste aparelho.", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(5.dp))
                Button(
                    onClick = onShare,
                    enabled = items.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(48.dp).animateContentSize(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosBlue, contentColor = Color.White)
                ) { Text("Abrir compartilhamento do Android", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun EmptySelection(modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(Modifier.size(74.dp), shape = CircleShape, color = Color(0xFF1C1C1E)) {
            Box(contentAlignment = Alignment.Center) { FileGlyph(Modifier.size(30.dp)) }
        }
        Spacer(Modifier.height(16.dp))
        Text("Nenhum arquivo ainda", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(5.dp))
        Text("Escolha um arquivo ou use o botão Adicionar arquivos.", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FileGlyph(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier) {
        val stroke = 1.6.dp.toPx()
        drawRoundRect(IosBlue, topLeft = androidx.compose.ui.geometry.Offset(size.width * .23f, size.height * .08f), size = androidx.compose.ui.geometry.Size(size.width * .54f, size.height * .84f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
        drawLine(IosBlue, androidx.compose.ui.geometry.Offset(size.width * .36f, size.height * .38f), androidx.compose.ui.geometry.Offset(size.width * .64f, size.height * .38f), stroke)
        drawLine(IosBlue, androidx.compose.ui.geometry.Offset(size.width * .36f, size.height * .55f), androidx.compose.ui.geometry.Offset(size.width * .64f, size.height * .55f), stroke)
        drawLine(IosBlue, androidx.compose.ui.geometry.Offset(size.width * .36f, size.height * .72f), androidx.compose.ui.geometry.Offset(size.width * .56f, size.height * .72f), stroke)
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024f)
    bytes < 1024L * 1024L * 1024L -> "%.1f MB".format(bytes / (1024f * 1024f))
    else -> "%.2f GB".format(bytes / (1024f * 1024f * 1024f))
}
