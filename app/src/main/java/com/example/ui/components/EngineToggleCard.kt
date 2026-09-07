package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TtsEngineType {
    AI_STUDIO,
    DEVICE_TTS
}

@Composable
fun EngineToggleCard(
    engineType: TtsEngineType,
    onEngineChanged: (TtsEngineType) -> Unit,
    pitch: Float,
    onPitchChanged: (Float) -> Unit,
    speechRate: Float,
    onSpeechRateChanged: (Float) -> Unit,
    isIndonesianSupportedOnDevice: Boolean,
    isApiKeyConfigured: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Segmented Tab Toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Option 1: AI Studio
                    val isAi = engineType == TtsEngineType.AI_STUDIO
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isAi) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable { onEngineChanged(TtsEngineType.AI_STUDIO) }
                            .padding(vertical = 10.dp)
                            .testTag("engine_toggle_ai"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Studio",
                                tint = if (isAi) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AI Studio Gemini",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isAi) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAi) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Option 2: Device Offline TTS
                    val isDevice = engineType == TtsEngineType.DEVICE_TTS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDevice) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable { onEngineChanged(TtsEngineType.DEVICE_TTS) }
                            .padding(vertical = 10.dp)
                            .testTag("engine_toggle_device"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "Perangkat",
                                tint = if (isDevice) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Perangkat (Offline)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isDevice) FontWeight.Bold else FontWeight.Medium,
                                color = if (isDevice) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle / Status for selected engine
            if (engineType == TtsEngineType.AI_STUDIO) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Suara AI ekspresif dengan intonasi emosional mendalam",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = if (isApiKeyConfigured) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isApiKeyConfigured) "AI Aktif" else "Siap (Fallback Aktif)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isApiKeyConfigured) Color(0xFF2E7D32) else Color(0xFFE65100),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                // Device TTS Pitch and Rate sliders
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tinggi Nada (Pitch): ${(pitch * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                        if (isIndonesianSupportedOnDevice) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Bahasa ID Siap",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2E7D32),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Slider(
                        value = pitch,
                        onValueChange = onPitchChanged,
                        valueRange = 0.5f..1.5f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("pitch_slider")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Kecepatan Bicara (Rate): ${(speechRate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )

                    Slider(
                        value = speechRate,
                        onValueChange = onSpeechRateChanged,
                        valueRange = 0.5f..1.5f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .testTag("rate_slider")
                    )
                }
            }
        }
    }
}
