package dev.neardrop.feature.home

import android.animation.ValueAnimator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val IosBlue = Color(0xFF0A84FF)
private val PrimaryLabel = Color(0xFFF5F5F7)
private val SecondaryLabel = Color(0xFF98989F)
private val Separator = Color(0xFF38383D)

/** AirDrop-style recipient sheet. No peer is shown until a real discovery source supplies it. */
@Composable
fun HomeScreen(onSend: () -> Unit, onReceive: () -> Unit, onDevices: () -> Unit, onSettings: () -> Unit, publicState: String = "Recebimento desligado") {
    var sheetVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { sheetVisible = true }
    val motionEnabled = ValueAnimator.areAnimatorsEnabled()
    Box(Modifier.fillMaxSize().background(Color(0xFF05070B))) {
        Canvas(Modifier.fillMaxSize().blur(38.dp)) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF344456), Color(0xFF111922), Color(0xFF090D12))))
            drawCircle(Color(0x885C7590), radius = size.width * .42f, center = Offset(size.width * .86f, size.height * .18f))
            drawCircle(Color(0x66485369), radius = size.width * .5f, center = Offset(size.width * .08f, size.height * .55f))
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .42f)))

        Column(Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding()) {
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(
                visible = sheetVisible,
                enter = if (motionEnabled) {
                    slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)) { it / 3 } + fadeIn(tween(180))
                } else EnterTransition.None
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 570.dp),
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                    color = Color(0xF01C1C1E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .10f))
                ) {
                    Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp)) {
                    Spacer(Modifier.height(15.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IdentityAvatar()
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Compartilhamento como", color = SecondaryLabel, style = MaterialTheme.typography.labelMedium)
                            Text("NearDrop", color = PrimaryLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = onSettings, modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF303034))) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Configurações", tint = PrimaryLabel, modifier = Modifier.size(21.dp))
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    Hairline()
                    Spacer(Modifier.height(19.dp))

                    Text("Meus dispositivos", color = SecondaryLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth().heightIn(min = 116.dp), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Top) {
                        EmptyDevice()
                    }

                    Spacer(Modifier.height(12.dp))
                    Hairline()
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Quick Share", color = SecondaryLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        Text("Via Android", color = IosBlue, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(11.dp))
                    Text(
                        "Envie diretamente para aparelhos com NearDrop. Quick Share continua disponível pelo menu nativo do Android.\n$publicState",
                        color = SecondaryLabel, style = MaterialTheme.typography.bodyMedium, lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(9.dp))
                    TextButton(onClick = onDevices, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
                        Text("Ver compatibilidade do dispositivo", color = IosBlue)
                    }

                    Spacer(Modifier.weight(1f))
                    Hairline()
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onSend,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IosBlue, contentColor = Color.White)
                    ) { Text("Selecionar arquivo", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
                    Spacer(Modifier.height(7.dp))
                    TextButton(onClick = onReceive, modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)) {
                        Text("Receber arquivos", color = IosBlue, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun IdentityAvatar() {
    Box(Modifier.size(48.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF8AB8FF), Color(0xFF334E78)))), contentAlignment = Alignment.Center) {
        Text("N", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyDevice() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(92.dp)) {
        DeviceGlyph()
        Spacer(Modifier.height(7.dp))
        Text("Nenhum dispositivo", color = SecondaryLabel, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DeviceGlyph() {
    Canvas(Modifier.size(70.dp)) {
        drawCircle(Color(0xFF2C2C2E), radius = size.minDimension * .49f)
        drawRoundRect(Color(0xFF8E8E93), topLeft = Offset(size.width * .36f, size.height * .21f), size = androidx.compose.ui.geometry.Size(size.width * .28f, size.height * .56f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()), style = Stroke(1.7.dp.toPx()))
        drawLine(IosBlue, Offset(size.width * .43f, size.height * .34f), Offset(size.width * .57f, size.height * .34f), 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(IosBlue, radius = size.minDimension * .026f, center = Offset(size.width * .5f, size.height * .68f))
    }
}

@Composable
private fun Hairline() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Separator))
}
