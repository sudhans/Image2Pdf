package com.msd.image2pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPageEventHelper
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min

// Constants for page layout
private const val HEADER_SPACE = 10f  // Points reserved for header
private const val FOOTER_SPACE = 20f  // Points reserved for footer
private const val PAGE_MARGINS = 20f  // Left/right margins

/**
 * Custom page event handler to position footer at the absolute bottom of each page
 */
private class FooterPageEventHelper(
    private val pageNumberSettings: PageNumberSettings
) : PdfPageEventHelper() {
    private var pageNum = 0

    override fun onStartPage(writer: PdfWriter?, document: Document?) {
        pageNum++
    }

    override fun onEndPage(writer: PdfWriter?, document: Document?) {
        val cb = writer?.directContent ?: return
        cb.saveState()

        // Get page width for positioning
        val pageWidth = document?.pageSize?.width ?: PageSize.A4.width

        // Position footer at the bottom of the page
        val footerYPosition = FOOTER_SPACE / 2  // Y position from bottom

        // Create footer text
        val displayPageNumber = pageNum + pageNumberSettings.startPageNumber - 1
        val pageText = "${pageNumberSettings.prefixText} $displayPageNumber".trim()

        // Set font for footer
        val bf = com.itextpdf.text.pdf.BaseFont.createFont(
            com.itextpdf.text.pdf.BaseFont.HELVETICA,
            com.itextpdf.text.pdf.BaseFont.WINANSI,
            false
        )
        cb.setFontAndSize(bf, 10f)
        cb.setRGBColorFill(0, 0, 0)  // Set black color for text

        // Calculate X position based on alignment
        val textWidth = bf.getWidthPoint(pageText, 10f)
        val xPosition = when (pageNumberSettings.horizontalAlignment) {
            HorizontalPageNumberAlignment.START -> PAGE_MARGINS
            HorizontalPageNumberAlignment.CENTER -> (pageWidth - textWidth) / 2
            HorizontalPageNumberAlignment.END -> pageWidth - PAGE_MARGINS - textWidth
        }

        // Draw text at bottom of page
        cb.beginText()
        cb.setTextMatrix(xPosition, footerYPosition)
        cb.showText(pageText)
        cb.endText()

        cb.restoreState()
    }
}

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
    val maxImageHeight = if (LocalWindowInfo.current.containerSize.width > 600) 200.dp else 120.dp
    val listState = rememberLazyListState()

    val addImagesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        viewModel.addImages(it)
    }

    LaunchedEffect(viewModel.imageUris.size) {
        if (viewModel.imageUris.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.imageUris.size - 1)
        }
    }

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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { addImagesLauncher.launch("image/*") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Images")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                state = listState,
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
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .sizeIn(maxHeight = maxImageHeight)
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

private fun getBitmapAsImage(uri: Uri, context: Context, jpegQuality: Int): Image {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
        decoder.isMutableRequired = true
    }

    // Scale bitmap based on quality setting
    val qualityScale = jpegQuality / 100f
    val scaledBitmap = Bitmap.createScaledBitmap(
        bitmap,
        (bitmap.width * qualityScale).toInt(),
        (bitmap.height * qualityScale).toInt(),
        true
    )
    bitmap.recycle()

    // Convert to ByteArray for iText
    val stream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    val imageBytes = stream.toByteArray()
    scaledBitmap.recycle()

    return Image.getInstance(imageBytes)
}


private suspend fun createPdf(context: Context, imageUris: List<Uri>, fileName: String): Pair<Boolean, String?> {
    return withContext(Dispatchers.IO) {
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

            resolver.openOutputStream(pdfUri)?.use { outputStream ->
                val pageSize = AppSettings.getPageSize(context)
                val jpegQuality = AppSettings.getJpegQuality(context).value
                val pageNumberSettings = AppSettings.getPageNumberSettings(context)

                // Use A4 page size for all modes
                val document = Document(PageSize.A4)

                // Set margins based on page size
                if (pageSize == com.msd.image2pdf.PageSize.A4_GRID) {
                    document.setMargins(15f, 15f, 15f, 40f)
                } else {
                    document.setMargins(20f, 20f, 20f, 40f)
                }

                val pdfWriter = PdfWriter.getInstance(document, outputStream)

                // Register footer page event handler
                if (pageNumberSettings.showPageNumbers) {
                    pdfWriter.pageEvent = FooterPageEventHelper(pageNumberSettings)
                }

                document.open()

                when (pageSize) {
                    com.msd.image2pdf.PageSize.A4_GRID -> {
                        addImagesInGridWithIText(
                            document,
                            imageUris,
                            context,
                            jpegQuality,
                            pageNumberSettings
                        )
                    }

                    else -> {
                        imageUris.forEachIndexed { index, uri ->
                            addPageWithSingleImageIText(
                                document,
                                uri,
                                pageSize,
                                context,
                                jpegQuality,
                                pageNumberSettings
                            )
                            if (index < imageUris.size - 1) {
                                document.newPage()
                            }
                        }
                    }
                }

                document.close()
            }
            Pair(true, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, e.message)
        }
    }
}


private fun addPageWithSingleImageIText(
    document: Document,
    uri: Uri,
    pageSize: com.msd.image2pdf.PageSize,
    context: Context,
    jpegQuality: Int,
    pageNumberSettings: PageNumberSettings
) {
    try {
        // Get Image from bitmap with quality scaling applied
        val image = getBitmapAsImage(uri, context, jpegQuality)

        // Calculate available space for image (account for header and footer space)
        val availableHeight = if (pageNumberSettings.showPageNumbers) {
            PageSize.A4.height - HEADER_SPACE - FOOTER_SPACE
        } else {
            PageSize.A4.height - 40  // Original bottom margin only
        }

        // Scale image based on page size settings
        when (pageSize) {
            com.msd.image2pdf.PageSize.A4 -> {
                val maxWidth = PageSize.A4.width - 2 * PAGE_MARGINS
                val maxHeight = availableHeight
                val scale = min(maxWidth / image.width, maxHeight / image.height)
                image.scaleAbsolute(image.width * scale, image.height * scale)
            }

            com.msd.image2pdf.PageSize.A4_SCALE_DOWN -> {
                val maxWidth = PageSize.A4.width - 2 * PAGE_MARGINS
                val maxHeight = availableHeight
                if (image.width > maxWidth || image.height > maxHeight) {
                    val scale = min(maxWidth / image.width, maxHeight / image.height)
                    image.scaleAbsolute(image.width * scale, image.height * scale)
                }
            }

            else -> {
                // For A4_GRID and other modes, scale to fit A4
                val maxWidth = PageSize.A4.width - 2 * PAGE_MARGINS
                val maxHeight = availableHeight
                val scale = min(maxWidth / image.width, maxHeight / image.height)
                image.scaleAbsolute(image.width * scale, image.height * scale)
            }
        }

        // Center align the image
        image.alignment = Element.ALIGN_CENTER

        // Add header space only if footer is shown
        if (pageNumberSettings.showPageNumbers) {
            val headerSpacer = Paragraph(" ")
            headerSpacer.spacingBefore = HEADER_SPACE
            document.add(headerSpacer)
        }

        document.add(image)

        // Footer is now handled by page event handler, no need to add it here
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun addImagesInGridWithIText(
    document: Document,
    imageUris: List<Uri>,
    context: Context,
    jpegQuality: Int,
    pageNumberSettings: PageNumberSettings
) {
    val pageWidth = PageSize.A4.width
    val pageHeight = PageSize.A4.height
    val marginLeft = 15f
    val marginRight = 15f
    val marginTop = 15f
    val marginBottom = 40f  // Extra space for footer
    val spacingBetweenImages = 16f  // Minimum space between images (approx 16dp in points)

    // Calculate available space
    val availableWidth = pageWidth - marginLeft - marginRight
    val availableHeight = pageHeight - marginTop - marginBottom

    // Target 2 columns per row
    val numColumns = 2
    val maxImageWidth = (availableWidth - spacingBetweenImages * (numColumns - 1)) / numColumns
    val maxImageHeight =
        availableHeight / 3f  // Allow up to 3 rows per page (will be adapted based on image heights)

    // Rows for current page. Each row is a list of Triples (Image, scaledWidth, scaledHeight)
    var currentPageRows = mutableListOf<List<Triple<Image, Float, Float>>>()
    var currentRow = mutableListOf<Triple<Image, Float, Float>>()
    var totalHeightOnPage = 0f

    fun flushCurrentPage() {
        if (currentPageRows.isNotEmpty()) {
            addGridTable(
                document,
                currentPageRows,
                numColumns,
                spacingBetweenImages,
                pageNumberSettings
            )
            currentPageRows = mutableListOf()
            totalHeightOnPage = 0f
        }
    }

    imageUris.forEach { uri ->
        val image = getBitmapAsImage(uri, context, jpegQuality)

        // Maintain aspect ratio
        val aspectRatio = image.height.toFloat() / image.width.toFloat()
        var scaledWidth = maxImageWidth
        var scaledHeight = scaledWidth * aspectRatio

        if (scaledHeight > maxImageHeight) {
            scaledHeight = maxImageHeight
            scaledWidth = scaledHeight / aspectRatio
        }

        image.scaleAbsolute(scaledWidth, scaledHeight)

        currentRow.add(Triple(image, scaledWidth, scaledHeight))

        // When row is full (numColumns) or it's the last image, decide about adding the row
        if (currentRow.size == numColumns) {
            val rowHeight = currentRow.maxOf { it.third }
            // If this row fits on current page, add it, otherwise flush page and start new
            if (totalHeightOnPage + rowHeight + spacingBetweenImages <= availableHeight || currentPageRows.isEmpty()) {
                currentPageRows.add(currentRow.toList())
                totalHeightOnPage += rowHeight + spacingBetweenImages
            } else {
                // Flush current page and start a new one
                flushCurrentPage()
                document.newPage()
                currentPageRows.add(currentRow.toList())
                totalHeightOnPage += rowHeight + spacingBetweenImages
            }
            currentRow = mutableListOf()
        }
    }

    // Handle leftover images in the last incomplete row
    if (currentRow.isNotEmpty()) {
        val rowHeight = currentRow.maxOf { it.third }
        if (totalHeightOnPage + rowHeight + spacingBetweenImages <= availableHeight || currentPageRows.isEmpty()) {
            currentPageRows.add(currentRow.toList())
            totalHeightOnPage += rowHeight + spacingBetweenImages
        } else {
            flushCurrentPage()
            document.newPage()
            currentPageRows.add(currentRow.toList())
            totalHeightOnPage += rowHeight + spacingBetweenImages
        }
    }

    // Flush remaining rows to document
    if (currentPageRows.isNotEmpty()) {
        addGridTable(
            document,
            currentPageRows,
            numColumns,
            spacingBetweenImages,
            pageNumberSettings
        )
    }
}

private fun addGridTable(
    document: Document,
    rows: List<List<Triple<Image, Float, Float>>>,
    numColumns: Int,
    spacing: Float,
    pageNumberSettings: PageNumberSettings
) {
    // Use PdfPTable to place images side-by-side
    val table = com.itextpdf.text.pdf.PdfPTable(numColumns)
    table.widthPercentage = 100f
    table.spacingBefore = spacing
    table.spacingAfter = spacing
    table.defaultCell.border = com.itextpdf.text.pdf.PdfPCell.NO_BORDER
    table.defaultCell.horizontalAlignment = Element.ALIGN_CENTER

    // Add header spacer if footer/page numbers are shown
    if (pageNumberSettings.showPageNumbers) {
        val headerSpacer = Paragraph(" ")
        headerSpacer.spacingBefore = HEADER_SPACE
        document.add(headerSpacer)
    }

    rows.forEach { row ->
        val rowHeight = row.maxOf { it.third }
        // For each column in the row
        for (i in 0 until numColumns) {
            if (i < row.size) {
                val (image, _, _) = row[i]
                val cell = com.itextpdf.text.pdf.PdfPCell()
                cell.border = com.itextpdf.text.pdf.PdfPCell.NO_BORDER
                cell.setPadding(spacing)
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.verticalAlignment = Element.ALIGN_MIDDLE
                cell.minimumHeight = rowHeight
                // Add the image element to the cell
                cell.addElement(image)
                table.addCell(cell)
            } else {
                // empty cell for missing columns in last row
                val emptyCell = com.itextpdf.text.pdf.PdfPCell()
                emptyCell.border = com.itextpdf.text.pdf.PdfPCell.NO_BORDER
                emptyCell.minimumHeight = rowHeight
                table.addCell(emptyCell)
            }
        }
    }

    document.add(table)
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
