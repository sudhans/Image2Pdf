package com.msd.image2pdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.msd.image2pdf.ui.theme.Image2PdfTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val navController = rememberNavController()
            var showMenu by remember { mutableStateOf(false) }
            var showAboutDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current

            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
                viewModel.onImagesSelected(it)
                if (it.isNotEmpty()) {
                    navController.navigate("preview")
                }
            }

            Image2PdfTheme {
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {

                        LaunchedEffect(Unit) {
                            viewModel.findPdfFiles(context)
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = { Text(text = stringResource(id = R.string.app_name)) },
                                    navigationIcon = {
                                        IconButton(onClick = { /*TODO*/ }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                                contentDescription = "App Logo"
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
                                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Greeting(
                                    text = stringResource(id = R.string.app_msg),
                                    modifier = Modifier.padding(16.dp)
                                )
                                Button(modifier = Modifier.padding(16.dp), onClick = { launcher.launch("image/*") }) {
                                    Text("Select Images")
                                }
                                if (viewModel.pdfFiles.isNotEmpty()) {
                                    Button(onClick = { navController.navigate("pdf_viewer") }) {
                                        Text("View PDF Files")
                                    }
                                }
                            }
                        }
                    }
                    composable("preview") {
                        PreviewScreen(viewModel, navController)
                    }
                    composable("pdf_viewer") {
                        PdfViewerScreen(viewModel, navController)
                    }
                    composable("settings") {
                        SettingsScreen(navController)
                    }
                }
                if (showAboutDialog) {
                    AboutDialog { showAboutDialog = false }
                }
            }
        }
    }
}

@Composable
fun Greeting(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Image2PdfTheme {
        Greeting("Android")
    }
}
