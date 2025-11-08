package com.msd.image2pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(viewModel: MainViewModel, navController: NavHostController) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    var showFileNameDialog by remember { mutableStateOf(false) }
    var pdfFileName by remember { mutableStateOf("images.pdf") }
    var showDeleteConfirmDialog by remember { mutableStateOf<Uri?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val state = rememberReorderableLazyListState(onMove = { from, to -> viewModel.reorderImages(from.index, to.index) })

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(id = R.string.app_name))
                        Text(
                            text = "Sort and Preview",
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
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("About") },
                            onClick = {
                                showAboutDialog = true
                                showMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .weight(1f)
                    .reorderable(state)
            ) {
                itemsIndexed(viewModel.imageUris, { _, item -> item.hashCode() }) { index, uri ->
                    ReorderableItem(state, key = uri.hashCode()) {
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                                .detectReorder(state)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                )
                                Row {
                                    IconButton(onClick = { showDeleteConfirmDialog = uri }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                    IconButton(onClick = { showDialog = uri }) {
                                        Icon(Icons.Default.Info, contentDescription = "Info")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = {
                    if (viewModel.imageUris.isNotEmpty()) {
                        showFileNameDialog = true
                    } else {
                        Toast.makeText(context, "No images selected", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Convert To PDF")
            }
        }
    }

    if (showFileNameDialog) {
        AlertDialog(
            onDismissRequest = { showFileNameDialog = false },
            title = { Text("Enter PDF name") },
            text = { TextField(value = pdfFileName, onValueChange = { pdfFileName = it }) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFileNameDialog = false
                        scope.launch {
                            val (success, errorMessage) = createPdf(context, viewModel.imageUris, pdfFileName)
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    Toast.makeText(context, "PDF created successfully", Toast.LENGTH_SHORT).show()
                                    navController.navigate("main") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    Toast.makeText(context, "Error creating PDF: $errorMessage", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFileNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    showDeleteConfirmDialog?.let { uriToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to remove this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeImage(uriToDelete)
                        if (viewModel.imageUris.isEmpty()) {
                            navController.popBackStack()
                        }
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("No")
                }
            }
        )
    }

    showDialog?.let { uri ->
        val (name, size) = getFileDetails(context, uri)
        AlertDialog(
            onDismissRequest = { showDialog = null },
            title = { Text(text = "File Properties") },
            text = { Text(text = "Name: $name\nSize: $size bytes\nLocation: $uri") },
            confirmButton = {
                TextButton(onClick = { showDialog = null }) {
                    Text("OK")
                }
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog { showAboutDialog = false }
    }
}

private suspend fun createPdf(context: Context, imageUris: List<Uri>, fileName: String): Pair<Boolean, String?> {
    return withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        try {
            if (imageUris.isEmpty()) {
                return@withContext Pair(false, "No images selected")
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Image2Pdf")
            }

            val resolver = context.contentResolver
            val pdfUri = resolver.insert(MediaStore.Files.getContentUri("external"), values)

            if (pdfUri == null) {
                return@withContext Pair(false, "Could not create PDF file.")
            }

            imageUris.forEachIndexed { index, uri ->
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }

                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)
                bitmap.recycle()
            }
            resolver.openOutputStream(pdfUri)?.use {
                pdfDocument.writeTo(it)
            }
            Pair(true, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, e.message)
        } finally {
            pdfDocument.close()
        }
    }
}

private fun getFileDetails(context: Context, uri: Uri): Pair<String?, Long?> {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        val name = if (nameIndex != -1) cursor.getString(nameIndex) else null
        val size = if (sizeIndex != -1) cursor.getLong(sizeIndex) else null
        return Pair(name, size)
    }
    return Pair(null, null)
}
