package com.msd.image2pdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.print.PrintAttributes
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import kotlin.math.max
import kotlin.math.min

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
    var isCreatingPdf by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(id = R.string.app_name))
                        Text(
                            text = "Edit and Preview",
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
                    OverflowMenu(
                        showMenu = showMenu,
                        onDismiss = { showMenu = false },
                        navController = navController,
                        onAboutClick = { showAboutDialog = true }
                    )
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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                items(viewModel.imageUris, key = { it.hashCode() }) { uri ->
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { showDialog = uri }) {
                                Icon(Icons.Default.Info, contentDescription = "Info")
                            }
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                            )
                            IconButton(onClick = { showDeleteConfirmDialog = uri }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
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

    if (isCreatingPdf) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissal */ },
            title = { Text("Creating PDF File") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Please wait...")
                }
            },
            confirmButton = {}
        )
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
                            isCreatingPdf = true
                            val (success, errorMessage) = createPdf(context, viewModel.imageUris, pdfFileName)
                            withContext(Dispatchers.Main) {
                                isCreatingPdf = false
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
                ?: return@withContext Pair(false, "Could not create PDF file.")

            val pageSize = AppSettings.getPageSize(context)

            if (pageSize == PageSize.A4_GRID) {
                addImagesInGrid(pdfDocument, imageUris, context)
            } else {
                imageUris.forEachIndexed { index, uri ->
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                    addPageWithSingleImage(pdfDocument, index + 1, bitmap, pageSize)
                    bitmap.recycle()
                }
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

private fun addPageWithSingleImage(pdfDocument: PdfDocument, pageNumber: Int, bitmap: Bitmap, pageSize: PageSize) {
    val a4Width = (PrintAttributes.MediaSize.ISO_A4.widthMils / 1000f * 72f).toInt()
    val a4Height = (PrintAttributes.MediaSize.ISO_A4.heightMils / 1000f * 72f).toInt()

    val (pageWidth, pageHeight) = when (pageSize) {
        PageSize.A4, PageSize.A4_SCALE_DOWN, PageSize.A4_NO_SCALING -> a4Width to a4Height
        PageSize.IMAGE_SIZE -> bitmap.width to bitmap.height
        else -> a4Width to a4Height // Should not happen in this function
    }

    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val scale = when (pageSize) {
        PageSize.A4 -> min(pageWidth.toFloat() / bitmap.width, pageHeight.toFloat() / bitmap.height)
        PageSize.A4_SCALE_DOWN -> if (bitmap.width > pageWidth || bitmap.height > pageHeight) {
            min(pageWidth.toFloat() / bitmap.width, pageHeight.toFloat() / bitmap.height)
        } else {
            1.0f
        }
        else -> 1.0f
    }

    val scaledWidth = bitmap.width * scale
    val scaledHeight = bitmap.height * scale

    val left = (pageWidth - scaledWidth) / 2f
    val top = (pageHeight - scaledHeight) / 2f

    val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
    canvas.drawBitmap(bitmap, null, destRect, null)
    pdfDocument.finishPage(page)
}

private fun addImagesInGrid(pdfDocument: PdfDocument, imageUris: List<Uri>, context: Context) {
    val a4Width = (PrintAttributes.MediaSize.ISO_A4.widthMils / 1000f * 72f).toInt()
    val a4Height = (PrintAttributes.MediaSize.ISO_A4.heightMils / 1000f * 72f).toInt()
    val spacing = 10f
    var currentX = spacing
    var currentY = spacing
    var maxRowHeight = 0f
    var page: PdfDocument.Page? = null

    imageUris.forEach { uri ->
        val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ -> decoder.isMutableRequired = true }

        val scale = if (bitmap.width > a4Width || bitmap.height > a4Height) {
            min((a4Width - 2 * spacing) / bitmap.width, (a4Height - 2 * spacing) / bitmap.height)
        } else {
            1.0f
        }
        val scaledWidth = bitmap.width * scale
        val scaledHeight = bitmap.height * scale

        if (page != null && currentX + scaledWidth + spacing > a4Width) {
            currentX = spacing
            currentY += maxRowHeight + spacing
            maxRowHeight = 0f
        }

        if (page == null || currentY + scaledHeight + spacing > a4Height) {
            page?.let { pdfDocument.finishPage(it) }
            val newPageNumber = pdfDocument.pages.size + 1
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(a4Width, a4Height, newPageNumber).create())
            currentX = spacing
            currentY = spacing
            maxRowHeight = 0f
        }

        val canvas = page!!.canvas
        val destRect = RectF(currentX, currentY, currentX + scaledWidth, currentY + scaledHeight)
        canvas.drawBitmap(bitmap, null, destRect, null)

        currentX += scaledWidth + spacing
        maxRowHeight = max(maxRowHeight, scaledHeight)

        bitmap.recycle()
    }
    page?.let { pdfDocument.finishPage(it) }
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
