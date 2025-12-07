package com.msd.image2pdf

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(id = R.string.app_name))
                        Text(
                            text = "Settings",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Page Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp)
            ) {
                val pageRadioOptions = listOf(
                    Triple(PageSize.A4, "A4 - Print Friendly", "Images will be fit to A4."),
                    Triple(
                        PageSize.A4_SCALE_DOWN,
                        "A4 - Scale Down",
                        "Bigger images will fit to A4, Smaller ones stay as is"
                    ),
                    Triple(PageSize.A4_NO_SCALING, "A4 - No Scaling", "Images will be used as is"),
                    Triple(
                        PageSize.IMAGE_SIZE,
                        "Image Size",
                        "Page size changes as per image size."
                    ),
                    Triple(
                        PageSize.A4_GRID,
                        "A4 - Grid",
                        "Smart Scaling - Multiple Images per page"
                    )
                )
                var selectedPageSize by remember { mutableStateOf(AppSettings.getPageSize(context)) }

                pageRadioOptions.forEach { (size, title, description) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (selectedPageSize == size),
                                onClick = {
                                    selectedPageSize = size
                                    AppSettings.setPageSize(context, size)
                                }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedPageSize == size),
                            onClick = {
                                selectedPageSize = size
                                AppSettings.setPageSize(context, size)
                            }
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(text = title)
                            Text(text = description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Page Numbers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp)
            ) {
                var pageNumberSettings by remember {
                    mutableStateOf(
                        AppSettings.getPageNumberSettings(
                            context
                        )
                    )
                }
                var showPageNumbersExpanded by remember { mutableStateOf(false) }
                var horizontalAlignmentExpanded by remember { mutableStateOf(false) }
                var verticalAlignmentExpanded by remember { mutableStateOf(false) }

                // Show page numbers dropdown
                ExposedDropdownMenuBox(
                    expanded = showPageNumbersExpanded,
                    onExpandedChange = { showPageNumbersExpanded = !showPageNumbersExpanded }) {
                    TextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = if (pageNumberSettings.showPageNumbers) "Yes" else "No",
                        onValueChange = {},
                        label = { Text("Show page numbers") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPageNumbersExpanded) },
                    )
                    ExposedDropdownMenu(expanded = showPageNumbersExpanded, onDismissRequest = { showPageNumbersExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Yes") },
                            onClick = {
                                pageNumberSettings = pageNumberSettings.copy(showPageNumbers = true)
                                AppSettings.savePageNumberSettings(context, pageNumberSettings)
                                showPageNumbersExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("No") },
                            onClick = {
                                pageNumberSettings =
                                    pageNumberSettings.copy(showPageNumbers = false)
                                AppSettings.savePageNumberSettings(context, pageNumberSettings)
                                showPageNumbersExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Start page number from: ${pageNumberSettings.startPageNumber}")
                Slider(
                    value = pageNumberSettings.startPageNumber.toFloat(),
                    onValueChange = {
                        pageNumberSettings = pageNumberSettings.copy(startPageNumber = it.toInt())
                        AppSettings.savePageNumberSettings(context, pageNumberSettings)
                    },
                    valueRange = 1f..999f,
                    enabled = pageNumberSettings.showPageNumbers
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Vertical Alignment Dropdown
                ExposedDropdownMenuBox(
                    expanded = verticalAlignmentExpanded,
                    onExpandedChange = { verticalAlignmentExpanded = !verticalAlignmentExpanded }) {
                    TextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = pageNumberSettings.verticalAlignment.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        label = { Text("Vertical Alignment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = verticalAlignmentExpanded) },
                        enabled = pageNumberSettings.showPageNumbers
                    )
                    ExposedDropdownMenu(expanded = verticalAlignmentExpanded, onDismissRequest = { verticalAlignmentExpanded = false }) {
                        VerticalPageNumberAlignment.values().forEach { alignment ->
                            DropdownMenuItem(
                                text = { Text(alignment.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    pageNumberSettings =
                                        pageNumberSettings.copy(verticalAlignment = alignment)
                                    AppSettings.savePageNumberSettings(context, pageNumberSettings)
                                    verticalAlignmentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Horizontal Alignment Dropdown
                ExposedDropdownMenuBox(
                    expanded = horizontalAlignmentExpanded,
                    onExpandedChange = {
                        horizontalAlignmentExpanded = !horizontalAlignmentExpanded
                    }) {
                    TextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = pageNumberSettings.horizontalAlignment.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        label = { Text("Horizontal Alignment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = horizontalAlignmentExpanded) },
                        enabled = pageNumberSettings.showPageNumbers
                    )
                    ExposedDropdownMenu(expanded = horizontalAlignmentExpanded, onDismissRequest = { horizontalAlignmentExpanded = false }) {
                        HorizontalPageNumberAlignment.values().forEach { alignment ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        alignment.name.lowercase()
                                            .replaceFirstChar { it.uppercase() })
                                },
                                onClick = {
                                    pageNumberSettings =
                                        pageNumberSettings.copy(horizontalAlignment = alignment)
                                    AppSettings.savePageNumberSettings(context, pageNumberSettings)
                                    horizontalAlignmentExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = pageNumberSettings.prefixText,
                    onValueChange = {
                        if (it.length <= 10) {
                            pageNumberSettings = pageNumberSettings.copy(prefixText = it)
                            AppSettings.savePageNumberSettings(context, pageNumberSettings)
                        }
                    },
                    placeholder = { Text("Page")},
                    label = { Text("Prefix Text") },
                    enabled = pageNumberSettings.showPageNumbers,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
