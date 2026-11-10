package dev.neardrop.app

import android.animation.ValueAnimator
import android.content.ClipData
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.neardrop.core.SharedItem
import dev.neardrop.data.NearbyTransferManager
import dev.neardrop.design.NearDropTheme
import dev.neardrop.feature.devices.DevicesScreen
import dev.neardrop.feature.home.HomeScreen
import dev.neardrop.feature.receive.ReceiveScreen
import dev.neardrop.feature.send.SendScreen
import dev.neardrop.feature.settings.SettingsScreen

private enum class Route { HOME, SEND, RECEIVE, DEVICES, SETTINGS }

class MainActivity : ComponentActivity() {
    private val nearbyManager by lazy { NearbyTransferManager(applicationContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureSystemBars()
        showApp(intent)
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); setIntent(intent); showApp(intent) }

    private fun showApp(incoming: Intent?) {
        val initialItems = readSharedItems(incoming)
        setContent {
            NearDropTheme {
                val manager = nearbyManager
                val proximity by manager.state.collectAsState()
                var route by remember { mutableStateOf(if (initialItems.isNotEmpty()) Route.SEND else Route.HOME) }
                var items by remember { mutableStateOf(initialItems) }
                var permissionAction by remember { mutableStateOf<String?>(null) }
                val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                    val action = permissionAction
                    permissionAction = null
                    if (result.values.all { it }) {
                        when (action) {
                            "advertise" -> manager.startAdvertisingForTenMinutes()
                            "discover" -> manager.startDiscovery()
                        }
                    } else {
                        // Keep discovery and availability off when the user declines a required radio permission.
                    }
                }
                fun requestNearby(action: String) {
                    val required = buildList {
                        if (Build.VERSION.SDK_INT >= 31) {
                            add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
                            add(android.Manifest.permission.BLUETOOTH_CONNECT)
                            add(android.Manifest.permission.BLUETOOTH_SCAN)
                        } else {
                            add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (Build.VERSION.SDK_INT >= 29) add(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                        if (Build.VERSION.SDK_INT >= 33) add(android.Manifest.permission.NEARBY_WIFI_DEVICES)
                    }.distinct()
                    val missing = required.filter { checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
                    if (missing.isEmpty()) {
                        if (action == "advertise") manager.startAdvertisingForTenMinutes() else manager.startDiscovery()
                    } else {
                        permissionAction = action
                        permissionLauncher.launch(missing.toTypedArray())
                    }
                }
                LaunchedEffect(route) {
                    if (route == Route.SEND && !proximity.discovering) requestNearby("discover")
                    else if (route != Route.SEND) manager.stopDiscovery()
                }
                val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                    items = uris.map(::toSharedItem)
                }
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val motionEnabled = ValueAnimator.areAnimatorsEnabled()
                    AnimatedContent(
                        targetState = route,
                        label = "screen-transition",
                        transitionSpec = {
                            if (motionEnabled) {
                                (fadeIn(tween(170)) + slideInVertically(tween(220)) { it / 18 }) togetherWith
                                    (fadeOut(tween(100)) + slideOutVertically(tween(180)) { -it / 24 })
                            } else EnterTransition.None togetherWith ExitTransition.None
                        }
                    ) { destination ->
                        when (destination) {
                            Route.HOME -> HomeScreen(
                                onSend = { route = Route.SEND },
                                onReceive = { route = Route.RECEIVE },
                                onDevices = { route = Route.DEVICES },
                                onSettings = { route = Route.SETTINGS },
                                publicState = if (proximity.advertising) "Visível para receber · ${proximity.status}" else "Recebimento desligado"
                            )
                            Route.SEND -> SendScreen(
                                items = items,
                                onPick = { picker.launch(arrayOf("*/*")) },
                                onBack = { manager.stopDiscovery(); route = Route.HOME },
                                onShare = { shareWithAndroid(items) },
                                sharedFromAnotherApp = incoming?.action == Intent.ACTION_SEND || incoming?.action == Intent.ACTION_SEND_MULTIPLE,
                                devices = proximity.devices,
                                discoveryStatus = proximity.status,
                                proximityState = proximity,
                                onSendNearby = { manager.connectAndSend(it, items) },
                                onConfirmConnection = manager::acceptIncomingConnection,
                                onRejectConnection = manager::rejectIncomingConnection
                            )
                            Route.RECEIVE -> ReceiveScreen(
                                state = proximity,
                                onBack = { route = Route.HOME },
                                onEnablePublic = { requestNearby("advertise") },
                                onDisablePublic = manager::stopAdvertising,
                                onAcceptConnection = manager::acceptIncomingConnection,
                                onRejectConnection = manager::rejectIncomingConnection,
                                onAcceptOffer = manager::acceptOffer,
                                onRejectOffer = manager::rejectOffer
                            )
                            Route.DEVICES -> DevicesScreen(onBack = { route = Route.HOME }, state = proximity)
                            Route.SETTINGS -> SettingsScreen(
                                onBack = { route = Route.HOME },
                                state = proximity,
                                onEnablePublic = { requestNearby("advertise") },
                                onDisablePublic = manager::stopAdvertising
                            )
                        }
                    }
                }
            }
        }
    }

    private fun configureSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= 29) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    /** Opens Android's native Sharesheet; Quick Share is offered by the OS when available. */
    private fun shareWithAndroid(items: List<SharedItem>) {
        val uris = items.map(SharedItem::uri).distinct()
        if (uris.isEmpty()) return
        val mimeTypes = items.distinctBy { it.uri }.map { item ->
            contentResolver.getType(item.uri)
                ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.displayName.substringAfterLast('.', "").lowercase())
        }
        val mimeType = commonMimeType(mimeTypes)
        val sendIntent = Intent(if (uris.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_TITLE, if (uris.size == 1) items.first().displayName else "${uris.size} arquivos")
            if (uris.size == 1) putExtra(Intent.EXTRA_STREAM, uris.first())
            else putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            clipData = ClipData.newUri(contentResolver, items.first().displayName, uris.first()).also { clip ->
                uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
            }
        }
        val chooser = Intent.createChooser(sendIntent, "Compartilhar arquivo").apply {
            putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(ComponentName(this@MainActivity, MainActivity::class.java)))
        }
        startActivity(chooser)
    }

    private fun commonMimeType(types: List<String?>): String {
        if (types.any { it == null }) return "*/*"
        val available = types.filterNotNull().distinct()
        if (available.isEmpty()) return "*/*"
        if (available.size == 1) return available.single()
        val majorTypes = available.map { it.substringBefore('/') }.distinct()
        return if (majorTypes.size == 1 && majorTypes.single() != "application") "${majorTypes.single()}/*" else "*/*"
    }

    private fun readSharedItems(intent: Intent?): List<SharedItem> {
        if (intent == null || (intent.action != Intent.ACTION_SEND && intent.action != Intent.ACTION_SEND_MULTIPLE)) return emptyList()
        val uris = buildList {
            sharedUri(intent)?.let(::add)
            addAll(sharedUris(intent))
            intent.clipData?.let { clip ->
                for (index in 0 until clip.itemCount) clip.getItemAt(index).uri?.let(::add)
            }
        }.distinct()
        // Keep provider access for the lifetime of the app task when the sender grants it.
        if (intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) {
            uris.forEach { uri ->
                runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            }
        }
        return uris.map(::toSharedItem)
    }

    @Suppress("DEPRECATION")
    private fun sharedUri(intent: Intent): Uri? = if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java) else intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri

    @Suppress("DEPRECATION")
    private fun sharedUris(intent: Intent): List<Uri> = if (Build.VERSION.SDK_INT >= 33)
        intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
    else intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).orEmpty()

    private fun toSharedItem(uri: Uri): SharedItem {
        val name = runCatching { contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME, android.provider.OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) null else {
                val nameColumn = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                (if (nameColumn >= 0) cursor.getString(nameColumn) else null) to if (sizeColumn < 0 || cursor.isNull(sizeColumn)) null else cursor.getLong(sizeColumn)
            }
        } }.getOrNull()
        return SharedItem(uri, name?.first ?: uri.lastPathSegment ?: "Arquivo", name?.second)
    }
}
