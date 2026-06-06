package com.sindicato.aplicacionaccesible.ui.comunicacion

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sindicato.aplicacionaccesible.data.PhraseEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicacionScreen(
    viewModel: ComunicacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModeSelector(
            selectedMode = uiState.mode,
            onModeSelected = { viewModel.changeMode(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = uiState.mode,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "modeTransition"
        ) { mode ->
            when (mode) {
                ComunicacionMode.TEXT_TO_SPEECH -> {
                    TtsSection(
                        text = uiState.ttsText,
                        onTextChanged = { viewModel.onTtsTextChanged(it) },
                        status = uiState.ttsStatus,
                        onSpeak = { viewModel.speak() },
                        onSavePhrase = { viewModel.saveCurrentTtsAsPhrase() },
                        savedPhrases = uiState.savedPhrases,
                        onDeletePhrase = { viewModel.deletePhrase(it) }
                    )
                }
                ComunicacionMode.SPEECH_TO_TEXT -> {
                    SttSection(
                        text = uiState.sttText,
                        status = uiState.sttStatus,
                        onMicClick = {
                            val permissionCheckResult = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            )
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                if (uiState.sttStatus == SttStatus.ESCUCHANDO) {
                                    viewModel.stopListening()
                                } else {
                                    viewModel.startListening()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onClearText = { viewModel.onSttTextChanged("") },
                        onUseText = { 
                            viewModel.onTtsTextChanged(it)
                            viewModel.changeMode(ComunicacionMode.TEXT_TO_SPEECH)
                        }
                    )
                }
            }
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ModeSelector(
    selectedMode: ComunicacionMode,
    onModeSelected: (ComunicacionMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = selectedMode == ComunicacionMode.TEXT_TO_SPEECH,
            onClick = { onModeSelected(ComunicacionMode.TEXT_TO_SPEECH) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = { Icon(Icons.Default.Edit, contentDescription = null) }
        ) {
            Text("Texto a Voz")
        }
        SegmentedButton(
            selected = selectedMode == ComunicacionMode.SPEECH_TO_TEXT,
            onClick = { onModeSelected(ComunicacionMode.SPEECH_TO_TEXT) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = { Icon(Icons.Default.Mic, contentDescription = null) }
        ) {
            Text("Voz a Texto")
        }
    }
}

@Composable
fun TtsSection(
    text: String,
    onTextChanged: (String) -> Unit,
    status: TtsStatus,
    onSpeak: () -> Unit,
    onSavePhrase: () -> Unit,
    savedPhrases: List<PhraseEntity>,
    onDeletePhrase: (PhraseEntity) -> Unit
) {
    var localError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("Escribe el texto que quieres que la app diga...") },
            label = { Text("Mensaje") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSpeak,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = text.isNotBlank() && status != TtsStatus.CARGANDO,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (status == TtsStatus.REPRODUCIENDO) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hablando...")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escuchar")
                }
            }
            
            OutlinedButton(
                onClick = { 
                    if (text.isNotBlank()) {
                        onSavePhrase()
                        localError = null
                    } else {
                        localError = "Escribe un mensaje antes de guardar"
                    }
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Guardar")
            }
        }

        if (localError != null) {
            Text(
                text = localError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Frases rápidas",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (savedPhrases.isEmpty()) {
            Text(
                "No tienes frases guardadas aún.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            // Usamos FlowRow o una grilla manual ya que LazyVerticalGrid dentro de Column con scroll es problemático
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                savedPhrases.chunked(2).forEach { rowPhrases ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPhrases.forEach { phrase ->
                            QuickPhraseCard(
                                phrase = phrase,
                                onClick = { onTextChanged(phrase.text) },
                                onDelete = { onDeletePhrase(phrase) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPhrases.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        InfoBox(
            title = "Consejo",
            description = "Escribe y presiona el icono '+' para guardar frases que uses frecuentemente."
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPhraseCard(
    phrase: PhraseEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = phrase.text,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SttSection(
    text: String,
    status: SttStatus,
    onMicClick: () -> Unit,
    onClearText: () -> Unit,
    onUseText: (String) -> Unit
) {
    var showFullTextDialog by remember { mutableStateOf(false) }

    if (showFullTextDialog && text.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showFullTextDialog = false },
            confirmButton = {
                TextButton(onClick = { showFullTextDialog = false }) {
                    Text("Cerrar")
                }
            },
            title = { Text("Texto reconocido") },
            text = {
                Text(
                    text = text,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            onClick = { if (text.isNotEmpty()) showFullTextDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (text.isEmpty() && status == SttStatus.IDLE) {
                    Text(
                        "Presiona el micrófono y empieza a hablar",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 5,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ExtendedFloatingActionButton(
            onClick = onMicClick,
            containerColor = if (status == SttStatus.ESCUCHANDO) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
            contentColor = if (status == SttStatus.ESCUCHANDO) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary,
            expanded = status == SttStatus.ESCUCHANDO,
            icon = {
                Icon(
                    imageVector = if (status == SttStatus.ESCUCHANDO) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null
                )
            },
            text = {
                Text(if (status == SttStatus.ESCUCHANDO) "Detener" else "Empezar a hablar")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (text.isNotEmpty() && status == SttStatus.IDLE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearText,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar")
                }
                Button(
                    onClick = { onUseText(text) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Usar texto")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = when (status) {
                SttStatus.ESCUCHANDO -> "Escuchando..."
                SttStatus.PROCESANDO -> "Procesando..."
                else -> "Toca para hablar"
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoBox(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
