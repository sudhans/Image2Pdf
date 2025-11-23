package com.msd.image2pdf

import android.content.Intent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun OverflowMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    onAboutClick: () -> Unit
) {
    val context = LocalContext.current
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                navController.navigate("settings")
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Share this App") },
            onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "This app helps you convert images to pdf files offline. Check this out : https://play.google.com/store/apps/details?id=${context.packageName}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("About") },
            onClick = {
                onAboutClick()
                onDismiss()
            }
        )
    }
}
