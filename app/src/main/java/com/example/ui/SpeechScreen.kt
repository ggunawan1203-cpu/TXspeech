package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.AudioPlayerCard
import com.example.ui.components.EngineToggleCard
import com.example.ui.components.ExpressiveStyleChips
import com.example.ui.components.SamplePresetsDialog
import com.example.ui.components.SpeechHistoryDialog
import com.example.ui.components.TtsEngineType
import com.example.ui.components.VoicePersonaSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechScreen(
    viewModel: SpeechViewModel,
    modifier: Modifier = Modifier
) {
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val selectedVoice by viewModel.selectedVoice.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
    val engineType by viewModel.engineType.collectAsStateWithLifecycle()
    val pitch by viewModel.pitch.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isEnhancing by viewModel.isEnhancing.collectAsStateWithLifecycle()

    val isPlayerPlaying by viewModel.audioPlayer.isPlaying.collectAsStateWithLifecycle()
    val isDeviceSpeaking by viewModel.deviceTtsManager.isSpeaking.collectAsStateWithLifecycle()
    val isPlaying = isPlayerPlaying || isDeviceSpeaking

    val currentPositionMs by viewModel.audioPlayer.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.audioPlayer.durationMs.collectAsStateWithLifecycle()
    val lastAudioBytes by viewModel.lastGeneratedBytes.collectAsStateWithLifecycle()
    val currentSavedEntity by viewModel.currentSavedEntity.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()

    val isIndonesianSupportedOnDevice by viewModel.deviceTtsManager.isIndonesianSupported.collectAsStateWithLifecycle()
    val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPresetsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Suara AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Text to Speech Bahasa Indonesia",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // API Key Settings Button
                    IconButton(
                        onClick = { showApiKeyDialog = true },
                        modifier = Modifier.testTag("button_open_api_key")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Kunci API Gemini",
                            tint = if (isApiKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Presets Button
                    IconButton(
                        onClick = { showPresetsDialog = true },
                        modifier = Modifier.testTag("button_open_presets")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Contoh Skrip Teks",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // History Button with Badge
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("button_open_history")
                    ) {
                        BadgedBox(
                            badge = {
                                if (historyList.isNotEmpty()) {
                                    Badge {
                                        Text(text = historyList.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Riwayat Suara",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Main Speak / Generate Button Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isPlaying) {
                                viewModel.audioPlayer.stop()
                                viewModel.deviceTtsManager.stop()
                            } else {
                                viewModel.speakCurrentText()
                            }
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("button_speak_main"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Menghasilkan Suara AI...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else if (isPlaying) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hentikan Suara",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (engineType == TtsEngineType.AI_STUDIO) "Ucapkan dengan Suara ${selectedVoice.name}" else "Ucapkan Teks (Perangkat)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Teks Bahasa Indonesia",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.updateInputText("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus Teks",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.updateInputText(it) },
                        placeholder = {
                            Text(
                                text = "Ketik atau tempel teks Bahasa Indonesia di sini...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("input_text_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Action Row: Character count & AI Enhance button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${inputText.length} karakter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilledTonalButton(
                            onClick = { viewModel.enhanceText() },
                            enabled = !isEnhancing && inputText.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("button_enhance_text")
                        ) {
                            if (isEnhancing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Memoles Teks...", fontSize = 12.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Perindah Teks (AI)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Audio Player Card (Visible when generated or playing)
            AnimatedVisibility(
                visible = (lastAudioBytes != null || isPlaying),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                AudioPlayerCard(
                    voicePersona = selectedVoice,
                    expressiveStyle = selectedStyle,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    engineName = if (engineType == TtsEngineType.AI_STUDIO) "Gemini AI" else "Perangkat",
                    isSaved = currentSavedEntity?.isFavorite == true,
                    onPlayPauseToggle = {
                        if (isPlayerPlaying) {
                            viewModel.audioPlayer.pause()
                        } else if (lastAudioBytes != null) {
                            viewModel.audioPlayer.resume()
                        } else {
                            viewModel.speakCurrentText()
                        }
                    },
                    onStop = {
                        viewModel.audioPlayer.stop()
                        viewModel.deviceTtsManager.stop()
                    },
                    onReplay = {
                        lastAudioBytes?.let {
                            viewModel.audioPlayer.playBytes(it)
                        } ?: viewModel.speakCurrentText()
                    },
                    onSeek = { targetMs -> viewModel.audioPlayer.seekTo(targetMs) },
                    onToggleSave = { viewModel.toggleSaveCurrentAudio() }
                )
            }

            // Voice Persona Selector
            VoicePersonaSelector(
                selectedPersona = selectedVoice,
                onPersonaSelected = { persona -> viewModel.selectVoice(persona) },
                onPreviewPersona = { persona -> viewModel.previewVoice(persona) }
            )

            // Expressive Style Selector
            ExpressiveStyleChips(
                selectedStyle = selectedStyle,
                onStyleSelected = { style -> viewModel.selectStyle(style) }
            )

            // TTS Engine Toggle & Fine Pitch/Rate Controls
            EngineToggleCard(
                engineType = engineType,
                onEngineChanged = { newType -> viewModel.setEngineType(newType) },
                pitch = pitch,
                onPitchChanged = { viewModel.updatePitch(it) },
                speechRate = speechRate,
                onSpeechRateChanged = { viewModel.updateSpeechRate(it) },
                isIndonesianSupportedOnDevice = isIndonesianSupportedOnDevice,
                isApiKeyConfigured = isApiKeyConfigured,
                onOpenApiKeyDialog = { showApiKeyDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentApiKey = viewModel.getCustomApiKey(),
            onDismiss = { showApiKeyDialog = false },
            onSaveKey = { newKey -> viewModel.saveCustomApiKey(newKey) },
            onClearKey = { viewModel.clearCustomApiKey() }
        )
    }

    if (showPresetsDialog) {
        SamplePresetsDialog(
            onDismiss = { showPresetsDialog = false },
            onPresetSelected = { preset -> viewModel.loadPreset(preset) }
        )
    }

    if (showHistoryDialog) {
        SpeechHistoryDialog(
            historyList = historyList,
            onDismiss = { showHistoryDialog = false },
            onPlayItem = { item -> viewModel.playHistoryItem(item) },
            onToggleFavorite = { item -> viewModel.toggleFavorite(item) },
            onDeleteItem = { item -> viewModel.deleteHistoryItem(item) },
            onClearAll = { viewModel.clearAllHistory() }
        )
    }
}
