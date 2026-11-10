package dev.neardrop.feature.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.neardrop.design.GlassCard
import dev.neardrop.data.ProximityState

@Composable
fun DevicesScreen(onBack: () -> Unit, state: ProximityState = ProximityState()) {
    Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().displayCutoutPadding().navigationBarsPadding().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp), contentPadding = PaddingValues(horizontal = 0.dp)) { Text("‹  Início", color = MaterialTheme.colorScheme.primary) }
        Text("Dispositivos e compatibilidade", style = MaterialTheme.typography.headlineMedium)
        Text("Estado dos rádios e dos transportes deste build.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Descoberta", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Nearby Connections  ${state.status}\nBluetooth / Wi‑Fi gerenciados pelo Google Play services\nmDNS / Bonjour  Não implementado\nAWDL  Não disponível", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = MaterialTheme.typography.bodyMedium.lineHeight)
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Text("AirDrop", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(7.dp))
            Text("Compatibilidade não verificada. Um APK Android convencional não recebe acesso AWDL pelas APIs públicas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Quick Share", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(7.dp))
            Text("O app entrega os arquivos ao Sharesheet do Android. O sistema oferece Quick Share quando disponível e controla descoberta, confirmação e transferência.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Pares NearDrop", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(7.dp))
            Text("${state.devices.size} aparelho(s) NearDrop encontrados. Dispositivos simulados nunca são exibidos como encontrados.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
