package com.msd.image2pdf

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
    val versionName = packageInfo?.versionName ?: "N/A"
    val installTime = packageInfo?.firstInstallTime ?: 0
    val installDate = if (installTime > 0) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(installTime))
    } else {
        "N/A"
    }

    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        append("Source: ")
        pushStringAnnotation(tag = "URL", annotation = "https://github.com/sudhans/Image2Pdf")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            append("Github")
        }
        pop()
    }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        },
        text = {
            Column {
                Text("Version: $versionName")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Installation Date: $installDate")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Author: Madhusudhan Sarvodhaya", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = annotatedString,
                    onTextLayout = { result -> textLayoutResult = result },
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures { offset ->
                            textLayoutResult?.let { layoutResult ->
                                val position = layoutResult.getOffsetForPosition(offset)
                                annotatedString.getStringAnnotations(tag = "URL", start = position, end = position)
                                    .firstOrNull()?.let { annotation ->
                                        uriHandler.openUri(annotation.item)
                                    }
                            }
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
