package dev.neardrop.feature.receive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.neardrop.data.ProximityState
import dev.neardrop.data.ReceiveOffer
import dev.neardrop.design.GlassCard

@Composable
fun ReceiveScreen(
    state: ProximityState,
    onBack: () -> Unit,
    onEnablePublic: () -> Unit,
    onDisablePublic: () -> Unit,
    onAcceptConnection: () -> Unit,
    onRejectConnection: () -> Unit,
    onAcceptOffer: () -> Unit,
    onRejectOffer: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().displayCutoutPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(start = 0.dp, top = 8.dp), contentPadding = PaddingValues(horizontal = 0.dp)) { Text("‹  Início", color = MaterialTheme.colorScheme.primary) }
        Text("Receber arquivos", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text("A visibilidade fica ativa por até 10 minutos. Você aprova cada conexão e cada envio.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(22.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(10.dp), shape = CircleShape, color = if (state.advertising) Color(0xFF30D158) else MaterialTheme.colorScheme.onSurfaceVariant) {}
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (state.advertising) "Visível para dispositivos próximos" else "Visibilidade desativada", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(state.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(15.dp))
            Button(onClick = if (state.advertising) onDisablePublic else onEnablePublic, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)) {
                Text(if (state.advertising) "Parar de aparecer" else "Ficar visível por 10 minutos", fontWeight = FontWeight.SemiBold)
            }
        }
        state.pendingConnectionName?.takeIf { state.pendingConnectionIncoming == true }?.let { sender ->
            Spacer(Modifier.height(16.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Text("Pedido de conexão", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("$sender quer se conectar. Compare o código nos dois dispositivos antes de aprovar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.verificationCode?.let { Text(it, modifier = Modifier.padding(vertical = 12.dp).align(Alignment.CenterHorizontally), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onRejectConnection, modifier = Modifier.weight(1f)) { Text("Recusar") }
                    Button(onClick = onAcceptConnection, modifier = Modifier.weight(1f)) { Text("Aprovar") }
                }
            }
        }
        state.offer?.let { offer ->
            Spacer(Modifier.height(16.dp))
            ReceiveOfferCard(offer, onAcceptOffer, onRejectOffer)
        }
        state.progress?.let { progress ->
            Spacer(Modifier.height(16.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Text(state.status, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                state.transferMessage?.let { Text(it, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        if ((state.pendingConnectionName == null || state.pendingConnectionIncoming != true) && state.offer == null && state.progress == null) {
            Spacer(Modifier.height(16.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Text("Nenhuma solicitação pendente", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(5.dp))
                Text("Quando outro NearDrop pedir conexão ou enviar uma oferta, você verá o nome do aparelho e os arquivos antes de aceitar.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReceiveOfferCard(offer: ReceiveOffer, onAccept: () -> Unit, onReject: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Text("Receber de ${offer.sender}?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("${offer.names.size} arquivo(s) · ${formatBytes(offer.totalBytes)}", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        offer.names.forEachIndexed { index, name ->
            Text("$name${offer.sizes.getOrNull(index)?.takeIf { it >= 0 }?.let { " · ${formatBytes(it)}" } ?: ""}", modifier = Modifier.padding(vertical = 3.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("Recusar") }
            Button(onClick = onAccept, modifier = Modifier.weight(1f)) { Text("Aceitar arquivos") }
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024L * 1024 -> "%.1f KB".format(bytes / 1024f)
    bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / (1024f * 1024))
    else -> "%.2f GB".format(bytes / (1024f * 1024 * 1024))
}
