package com.example.mygreenhouse.ui.composables

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mygreenhouse.R // Assuming you have a placeholder drawable
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Context
import android.util.Log

// Helper function to copy URI to app's internal storage
private fun copyUriToAppStorage(context: Context, contentUri: Uri): String? {
    try {
        val inputStream = context.contentResolver.openInputStream(contentUri) ?: run {
            Log.e("ImagePicker", "Failed to open input stream for URI: $contentUri")
            return null
        }

        val imageDir = File(context.filesDir, "plant_images")
        if (!imageDir.exists()) {
            if (!imageDir.mkdirs()) {
                Log.e("ImagePicker", "Failed to create directory: ${'$'}imageDir")
                return null
            }
        }

        // Use a more robust way to get file extension if needed, or stick to .jpg for simplicity
        val extension = context.contentResolver.getType(contentUri)?.substringAfterLast('/') ?: "jpg"
        val fileName = "plant_${System.currentTimeMillis()}.${'$'}extension"
        val outputFile = File(imageDir, fileName)

        FileOutputStream(outputFile).use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }
        Log.d("ImagePicker", "Image copied to: ${'$'}{outputFile.absolutePath}")
        return outputFile.absolutePath
    } catch (e: IOException) {
        Log.e("ImagePicker", "Error copying image URI: $contentUri", e)
        return null
    } catch (e: SecurityException) {
        Log.e("ImagePicker", "Security exception for URI: $contentUri", e)
        return null
    } catch (e: Exception) {
        Log.e("ImagePicker", "Generic exception for URI: $contentUri", e)
        return null
    }
}

@Composable
fun ImagePicker(
    modifier: Modifier = Modifier,
    imageUri: String?,
    onImageSelected: (String?) -> Unit,
    label: String = "Plant Image"
) {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // Persist permission to access the URI - this might not be strictly necessary
                // if we are immediately copying it to internal storage.
                // However, it doesn't hurt for the brief moment we're accessing it.
                try {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (e: SecurityException) {
                    Log.w("ImagePicker", "Failed to take persistable URI permission for $uri: ${'$'}{e.message}")
                    // Continue anyway, copying might still work if permission was already granted or not needed for copy.
                }
                
                val filePath = copyUriToAppStorage(context, uri)
                onImageSelected(filePath) // Pass the new file path (or null if copy failed)
            } else {
                // Optionally handle if no image was selected, perhaps call onImageSelected(null)
                 onImageSelected(null)
            }
        }
    )

    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(DarkSurface.copy(alpha = 0.3f))
                .clickable { // Click the box to pick image if no image is present
                    if (imageUri == null) {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(imageUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Selected plant image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Clear button
                IconButton(
                    onClick = { 
                        // When clearing, we also need to potentially delete the backing file
                        // For now, just set path to null. Deletion can be handled by PhotoManagementService.
                        onImageSelected(null) 
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            DarkSurface.copy(alpha = 0.7f),
                            CircleShape
                        )
                ) {
                    Icon(Icons.Filled.Clear, "Clear image", tint = TextWhite.copy(alpha = 0.8f))
                }
                // Change image button (optional, could also click image to change)
                 Button(
                    onClick = { 
                         photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen.copy(alpha = 0.8f),
                        contentColor = TextWhite
                    ),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text("Change")
                }

            } else {
                Icon(
                    imageVector = Icons.Filled.AddAPhoto,
                    contentDescription = "Add plant image",
                    modifier = Modifier.size(48.dp),
                    tint = TextWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
} 