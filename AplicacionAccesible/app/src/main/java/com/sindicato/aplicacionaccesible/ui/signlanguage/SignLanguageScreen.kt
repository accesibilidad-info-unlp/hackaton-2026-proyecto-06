package com.sindicato.aplicacionaccesible.ui.signlanguage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sindicato.aplicacionaccesible.R
import com.sindicato.aplicacionaccesible.data.SignLanguageEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignLanguageGrid(viewModel: SignLanguageViewModel = viewModel()) {
    val signItems by viewModel.signItems.collectAsState()
    var selectedItem by rememberSaveable { mutableStateOf<SignLanguageEntity?>(null) }
    
    if (selectedItem == null) {
        // Vista de Grilla con Texto (Actuando como iconos)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(signItems) { item ->
                Card(
                    onClick = { selectedItem = item },
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.word,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Vista de Detalle con la imagen de la seña desde la base de datos
        SignLanguageDetail(item = selectedItem!!, onBack = { selectedItem = null })
    }
}

@Composable
fun SignLanguageDetail(item: SignLanguageEntity, onBack: () -> Unit) {
    val context = LocalContext.current
    val imageRes = remember(item.imageName) {
        val resId = context.resources.getIdentifier(item.imageName, "drawable", context.packageName)
        if (resId != 0) resId else R.drawable.ic_launcher_foreground
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = item.word, style = MaterialTheme.typography.headlineMedium)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Seña de ${item.word}",
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Representación en lengua de señas para '${item.word}'.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Espacio para futura funcionalidad de audio (mencionada por el usuario)
        if (item.audioName != null) {
            Button(onClick = { /* Implementar reproducción de audio */ }) {
                Text("Escuchar pronunciación")
            }
        }
    }
}
