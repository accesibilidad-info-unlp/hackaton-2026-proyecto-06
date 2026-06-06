package com.sindicato.aplicacionaccesible.ui.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sindicato.aplicacionaccesible.ui.screens.soundgrid.SoundButton
import com.sindicato.aplicacionaccesible.ui.screens.soundgrid.SoundEffect
import com.sindicato.aplicacionaccesible.viewmodel.SoundboardViewModel
import com.sindicato.aplicacionaccesible.ui.screens.soundgrid.Template
import com.sindicato.aplicacionaccesible.ui.theme.AppTheme
import com.sindicato.aplicacionaccesible.ui.theme.SafeColors
import com.sindicato.aplicacionaccesible.ui.theme.getContrastColor


@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview()
fun SoundboardPreview(){
    val viewModel: SoundboardViewModel = viewModel()
    Soundboard(viewModel, false)
}


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Soundboard(viewModel: SoundboardViewModel, isColorblindMode: Boolean){
    Scaffold(
        topBar = { SoundboardTopBar(viewModel) },
        content = { padding -> 
            Box(modifier = Modifier.padding(padding)) {
                SoundGrid(viewModel, isColorblindMode)
            }
        },
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SoundGrid(viewModel: SoundboardViewModel, isColorblindMode: Boolean) {
    val context = LocalContext.current
    val currentTemplate = viewModel.templates.getOrNull(viewModel.currentTemplateIndex)
    val totalCells = 20

    var showDialogAtPosition by remember { mutableStateOf<Int?>(null) }



    showDialogAtPosition?.let { position ->
        AddButtonDialog(
            onDismiss = { showDialogAtPosition = null },
            onConfirm = { name, effect, tts, selectedColor, selectedIcon ->
                viewModel.addButtonToCurrentTemplate(name, effect, tts, selectedColor, selectedIcon, position)
                showDialogAtPosition = null
            },
            soundboardViewModel = viewModel
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(viewModel.columnCount),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(totalCells) { index ->
            val buttonAtPosition = currentTemplate?.buttons?.find { it.gridPosition == index }

            if (buttonAtPosition != null) {
                SoundButtonItem(
                    button = buttonAtPosition,
                    isColorblindMode = isColorblindMode,
                    onClick = {
                        if (!viewModel.isEditMode) {
                            viewModel.playSound(context, buttonAtPosition)
                        } else {
                            viewModel.deleteButtonAtPosition(buttonAtPosition.gridPosition)
                        }
                    },
                    viewModel
                )
            } else if (viewModel.isEditMode) {
                EmptyCellPlaceholder(onClick = { showDialogAtPosition = index })
            }
        }
    }
}


@Composable
fun SoundButtonItem(
    button: SoundButton,
    isColorblindMode: Boolean,
    onClick: () -> Unit,
    viewModel: SoundboardViewModel
) {
    val backgroundColor = Color(button.color)
    val contentColor = getContrastColor(backgroundColor)

    // Aplicar transparencia en modo edición
    val alpha = if (viewModel.isEditMode) 0.5f else 1f

    ElevatedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = backgroundColor.copy(alpha = alpha),
            contentColor = contentColor.copy(alpha = alpha)
        ),
        border = if (isColorblindMode) BorderStroke(2.dp, contentColor.copy(alpha = alpha)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (viewModel.isEditMode) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Icono de eliminación en modo edición
                    Icon(
                        imageVector = Icons.Default.RemoveCircle,
                        contentDescription = "Eliminar",
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = button.name,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = availableIcons.getOrNull(button.iconRes) ?: Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = button.name,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}



@Preview(name = "Add Button Dialog", showBackground = true)
@Composable
fun AddButtonDialogPreview() {
    AddButtonDialog(
        onDismiss = { /* Do nothing */ },
        onConfirm = { name, effect, tts, color, iconRes -> println("Preview: $name with $effect or $tts")},
        soundboardViewModel = viewModel()
    )
}



val buttonColors = listOf(
    Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFF42A5F5),
    Color(0xFFFFCA28), Color(0xFFAB47BC), Color(0xFF26A69A)
)

// Define a list of selectable icons
val availableIcons = listOf(
    Icons.Default.MusicNote, Icons.Default.Notifications,
    Icons.Default.Favorite, Icons.Default.Star,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddButtonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, SoundEffect?, String?, Long, Int) -> Unit,
    soundboardViewModel: SoundboardViewModel
) {
    var name by remember { mutableStateOf("") }
    var selectedEffect by remember { mutableStateOf<SoundEffect?>(SoundEffect.CLAPPING) }
    var ttsText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Botón", "Voz")

    var iconExpanded by remember { mutableStateOf(false) } // State for icon dropdown
    var selectedIconIndex by remember { mutableIntStateOf(0) }

    var selectedColor by remember { mutableStateOf(buttonColors[0]) }

    val context = LocalContext.current


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {

            Column {
                Text("Añadir Botón")
                // TabRow for switching between Button and Speech
                androidx.compose.material3.TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        androidx.compose.material3.Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Botón") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if(selectedTabIndex == 0) {
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedEffect?.displayName ?: "Seleccionar Sonido")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SoundEffect.entries.forEach { effect ->
                                DropdownMenuItem(
                                    text = {
                                        // Layout the name and the play button side-by-side
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(effect.displayName, modifier = Modifier.weight(1f))

                                            // The Preview Play Button
                                            IconButton(
                                                onClick = {
                                                    // This plays the sound without closing the menu
                                                    soundboardViewModel.previewSound(context, effect)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Reproducir sonido",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedEffect = effect
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    TextField(
                        value = ttsText,
                        onValueChange = { ttsText = it },
                        label = { Text("Texto a decir") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Seleccionar Icono", style = MaterialTheme.typography.labelMedium)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { iconExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = availableIcons[selectedIconIndex],
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Elegir Icono")
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = iconExpanded,
                        onDismissRequest = { iconExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.8f) // Adjust width as needed
                            .padding(8.dp)
                    ) {
                        // Using FlowRow inside the Dropdown to create the Grid effect
                        // This is scalable: as you add more icons, they wrap automatically
                        FlowRow(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 4 // Control how many columns
                        ) {
                            availableIcons.forEachIndexed { index, icon ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = if (selectedIconIndex == index)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedIconIndex = index
                                            iconExpanded = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (selectedIconIndex == index)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Text("Elegir Color", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SafeColors.size) { index ->
                        val color = SafeColors[index]
                        Surface(
                            modifier = Modifier
                                .size(35.dp)
                                .clickable { selectedColor = color },
                            shape = CircleShape,
                            color = color,
                            border = if (selectedColor == color)
                                BorderStroke(3.dp, Color.Black) else null
                        ) {}
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val colorLong = selectedColor.toArgb().toLong()

                        if (selectedTabIndex == 0) {
                            onConfirm(name, selectedEffect, null, colorLong, selectedIconIndex)
                        } else {
                            if (ttsText.isNotBlank()) {
                                onConfirm(name, null, ttsText, colorLong, selectedIconIndex)
                            }
                        }
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundboardTopBar(viewModel: SoundboardViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var newTemplateName by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDialog = false
                newTemplateName = ""
            },
            title = { Text("Nueva Plantilla") },
            text = {
                TextField(
                    value = newTemplateName,
                    onValueChange = { newTemplateName = it },
                    label = { Text("Nombre de la Plantilla") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTemplateName.isNotBlank()) {
                            viewModel.addTemplate(newTemplateName)
                            showDialog = false
                            newTemplateName = ""
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDialog = false
                    newTemplateName = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Borrar Plantilla") },
            text = { Text("¿Estás seguro de que quieres borrar esta plantilla?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCurrentTemplate()
                        viewModel.toggleEditMode()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Plantilla")
            }
        },
        title = {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                if (viewModel.isEditMode) {
                    val template = viewModel.templates.getOrNull(viewModel.currentTemplateIndex)
                    if (template != null) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { },
                                label = { Text(template.name) }
                            )
                        }
                    }
                } else {
                    itemsIndexed(viewModel.templates) { index, template ->
                        FilterChip(
                            selected = viewModel.currentTemplateIndex == index,
                            onClick = { viewModel.currentTemplateIndex = index },
                            label = { Text(template.name) }
                        )
                    }
                }
            }
        },
        actions = {
            if (viewModel.isEditMode) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar Plantilla", tint = Color.Red)
                }
                Button(
                    onClick = { viewModel.toggleEditMode() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC8E6C9), // Light Green
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Aceptar")
                }
            } else {
                if(viewModel.templates.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.toggleEditMode() }
                    ) {
                        Text("Editar")
                    }
                }
            }
        }
    )
}





@Composable
fun EmptyCellPlaceholder(onClick: () -> Unit) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = Color(0xFFEEEEEE) // Light Gray
        ),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Añadir Botón")
    }
}
