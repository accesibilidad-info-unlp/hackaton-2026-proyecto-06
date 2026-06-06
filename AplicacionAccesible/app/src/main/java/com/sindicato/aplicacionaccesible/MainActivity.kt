package com.sindicato.aplicacionaccesible

import android.os.Build
import android.os.Bundle
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sindicato.aplicacionaccesible.data.AppDatabase
import com.sindicato.aplicacionaccesible.data.DatabaseSeeder
import com.sindicato.aplicacionaccesible.ui.components.Soundboard
import com.sindicato.aplicacionaccesible.ui.comunicacion.ComunicacionScreen
import com.sindicato.aplicacionaccesible.ui.comunicacion.ComunicacionViewModel
import com.sindicato.aplicacionaccesible.viewmodel.SoundboardViewModel
import com.sindicato.aplicacionaccesible.ui.signlanguage.SignLanguageGrid
import com.sindicato.aplicacionaccesible.ui.sound.SoundEffectManager
import com.sindicato.aplicacionaccesible.ui.theme.AppTheme
import com.sindicato.aplicacionaccesible.ui.theme.AplicacionAccesibleTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SoundEffectManager.init(applicationContext)

        // Seed the database
        val dao = AppDatabase.getDatabase(applicationContext).signLanguageDao()
        lifecycleScope.launch {
            DatabaseSeeder.seedDatabase(dao)
        }

        setContent {
            val soundboardViewModel: SoundboardViewModel = viewModel()
            val comunicacionViewModel: ComunicacionViewModel = viewModel()
            val uiState by comunicacionViewModel.uiState.collectAsStateWithLifecycle()

            AplicacionAccesibleTheme(appTheme = uiState.theme) {
                MainScreen(
                    comunicacionViewModel = comunicacionViewModel,
                    soundboardViewModel = soundboardViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundEffectManager.release()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    comunicacionViewModel: ComunicacionViewModel,
    soundboardViewModel: SoundboardViewModel
) {
    val uiState by comunicacionViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var isConfigScreenOpen by rememberSaveable { mutableStateOf(false) }

    val helpContent = when (selectedTab) {
        0 -> "Sonidos" to "Esta pantalla te permite reproducir sonidos comunes. Toca cualquier botón para escuchar el sonido correspondiente. Puedes cambiar el número de columnas desde el menú superior (icono de tres líneas)."
        1 -> "Comunicación" to "Aquí tienes dos modos: 'Texto a Voz' para escribir lo que quieres que la app diga, y 'Voz a Texto' para que la app transcriba lo que hablas. Cambia entre ellos usando el selector superior."
        else -> "Lenguaje de Señas" to "Explora palabras en lenguaje de señas. Toca una palabra en la lista para ver su representación visual. Puedes volver a la lista usando el botón de retroceso en el detalle."
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Entendido")
                }
            },
            title = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Help, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ayuda: ${helpContent.first}")
                }
            },
            text = { Text(helpContent.second) }
        )
    }

    if (isConfigScreenOpen) {
        BackHandler {
            isConfigScreenOpen = false
        }
        ConfigurationScreen(
            onBack = { isConfigScreenOpen = false },
            comunicacionViewModel = comunicacionViewModel
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            when(selectedTab) {
                                0 -> "Sonidos"
                                1 -> "Comunicación"
                                else -> "Señas"
                            }
                        ) 
                    },
                    actions = {
                        IconButton(onClick = { isConfigScreenOpen = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración")
                        }

                        // Botón para cambiar el número de columnas en la grilla
                        if (selectedTab == 0) {
                            IconButton(onClick = {
                                val columnCount = soundboardViewModel.columnCount
                                val nextCols = if (columnCount >= 4) 2 else columnCount + 1
                                soundboardViewModel.columnCount = nextCols
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Columnas: ${soundboardViewModel.columnCount}")
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        label = { Text("Sonidos") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Email, contentDescription = null) },
                        label = { Text("Comunicación") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                        label = { Text("Señas") }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showHelpDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = "Botón de Ayuda"
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                when (selectedTab) {
                    0 -> Soundboard(soundboardViewModel, uiState.theme == AppTheme.COLORBLIND)
                    1 -> ComunicacionScreen(comunicacionViewModel)
                    2 -> SignLanguageGrid()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    onBack: () -> Unit,
    comunicacionViewModel: ComunicacionViewModel
) {
    val uiState by comunicacionViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Tema de la aplicación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            AppTheme.entries.forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { comunicacionViewModel.setTheme(theme) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.theme == theme,
                        onClick = { comunicacionViewModel.setTheme(theme) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when(theme) {
                            AppTheme.LIGHT -> "Modo Claro"
                            AppTheme.DARK -> "Modo Oscuro"
                            AppTheme.COLORBLIND -> "Modo Accesible (Colores de alto contraste)"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Idioma de Voz", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            val languages = listOf("es" to "Español", "en" to "English")
            languages.forEach { (code, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { comunicacionViewModel.setLanguage(code) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.selectedLanguage == code,
                        onClick = { comunicacionViewModel.setLanguage(code) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Voz y Tono", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Seleccionar voz disponible", style = MaterialTheme.typography.titleMedium)
            Text(
                "Diferentes voces pueden sonar más masculinas o femeninas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.availableVoices.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "No hay voces disponibles para el idioma seleccionado en este dispositivo.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.availableVoices) { voice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { comunicacionViewModel.selectVoice(voice) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.selectedVoice?.name == voice.name,
                                    onClick = { comunicacionViewModel.selectVoice(voice) }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = voice.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Calidad: ${if (voice.quality == Voice.QUALITY_VERY_HIGH) "Muy alta" else if (voice.quality == Voice.QUALITY_HIGH) "Alta" else "Normal"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Personalizar tono (Pitch): ${"%.1f".format(uiState.pitch)}", style = MaterialTheme.typography.titleMedium)
            Text(
                "Desliza a la izquierda para un tono más grave/profundo, o a la derecha para uno más agudo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = uiState.pitch,
                onValueChange = { comunicacionViewModel.setPitch(it) },
                valueRange = 0.5f..2.0f,
                steps = 15,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Velocidad de habla: ${"%.1f".format(uiState.speechRate)}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = uiState.speechRate,
                onValueChange = { comunicacionViewModel.setSpeechRate(it) },
                valueRange = 0.5f..2.0f,
                steps = 15,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { comunicacionViewModel.speak(if (uiState.selectedLanguage == "es") "Hola, esta es una prueba de la configuración de voz elegida." else "Hello, this is a test of the chosen voice configuration.") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Probar configuración de voz")
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val soundboardViewModel: SoundboardViewModel = viewModel()
    val comunicacionViewModel: ComunicacionViewModel = viewModel()
    MainScreen(
        comunicacionViewModel = comunicacionViewModel,
        soundboardViewModel = soundboardViewModel
    )
}
