package dev.neardrop.feature.settings

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
fun SettingsScreen(onBack: () -> Unit, state: ProximityState = ProximityState(), onEnablePublic: () -> Unit = {}, onDisablePublic: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().displayCutoutPadding().navigationBarsPadding().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp), contentPadding = PaddingValues(horizontal = 0.dp)) { Text("‹  Início", color = MaterialTheme.colorScheme.primary) }
        Text("Configurações", style = MaterialTheme.typography.headlineMedium)
        Text("Preferências e privacidade", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Este dispositivo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Nome\nNearDrop Android", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Text("Visibilidade\n${if (state.advertising) "Ativa por até 10 minutos" else "Desligada"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Button(onClick = if (state.advertising) onDisablePublic else onEnablePublic, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.advertising) "Desativar visibilidade" else "Ficar visível por 10 minutos")
            }
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Privacidade", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Recebimentos automáticos desativados. Cada transferência futura exigirá sua aprovação.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Transportes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Nearby Connections  ${state.status}\nAWDL  Não implementado\nWi‑Fi Direct  Não implementado\nmDNS / Bonjour  Não implementados", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.weight(1f))
        Text("NearDrop 0.1.0 · AirDrop não implementado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
    }
}
