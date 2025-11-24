package com.msd.image2pdf

import android.content.Intent
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class PdfFileDetails(
    val name: String,
    val size: Long,
    val dateAdded: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(viewModel: MainViewModel, navController: NavHostController) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.findPdfFiles(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(id = R.string.app_name))
                        Text(
                            text = "View PDF Files",
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
        val sortedPdfDetails = remember(viewModel.pdfFiles) {
            viewModel.pdfFiles
                .mapNotNull { uri -> getPdfFileDetails(context, uri)?.let { details -> uri to details } }
                .sortedByDescending { it.second.dateAdded }
        }

        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) { 
            items(sortedPdfDetails) { (uri, details) ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = details.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val sizeInMb = details.size / (1024.0 * 1024.0)
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f MB", sizeInMb),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(details.dateAdded * 1000))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog { showAboutDialog = false }
    }
}

private fun getPdfFileDetails(context: android.content.Context, uri: android.net.Uri): PdfFileDetails? {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        val dateAddedIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)

        if (cursor.moveToFirst()) {
            val name = if (nameIndex != -1) cursor.getString(nameIndex) else "Unknown"
            val size = if (sizeIndex != -1) cursor.getLong(sizeIndex) else 0L
            val dateAdded = if (dateAddedIndex != -1) cursor.getLong(dateAddedIndex) else 0L
            return PdfFileDetails(name, size, dateAdded)
        }
    }
    return null
}
