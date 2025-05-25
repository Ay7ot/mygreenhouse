package com.example.mygreenhouse.ui.composables

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mygreenhouse.R // Assuming you have a placeholder drawable
import com.example.mygreenhouse.ui.theme.DarkSurface
import com.example.mygreenhouse.ui.theme.PrimaryGreen
import com.example.mygreenhouse.ui.theme.TextWhite
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

// Helper function to create a file for camera photos
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "PLANT_${timeStamp}_"
    val imageDir = File(context.filesDir, "plant_images")
    if (!imageDir.exists()) {
        imageDir.mkdirs()
    }
    return File.createTempFile(
        imageFileName,
        ".jpg",
        imageDir
    )
}

@Composable
fun ImagePicker(
    modifier: Modifier = Modifier,
    imageUri: String?,
    onImageSelected: (String?) -> Unit,
    label: String = "Plant Image"
) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var cameraPermissionGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == 
            PackageManager.PERMISSION_GRANTED
        ) 
    }
    
    // State for ContentScale toggle
    var useScaleCrop by remember { mutableStateOf(true) }
    val contentScale = if (useScaleCrop) ContentScale.Crop else ContentScale.Fit

    // For gallery selection
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (e: SecurityException) {
                    Log.w("ImagePicker", "Failed to take persistable URI permission for $uri: ${'$'}{e.message}")
                }
                
                val filePath = copyUriToAppStorage(context, uri)
                onImageSelected(filePath)
            }
        }
    )

    // For camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && photoFile != null) {
                onImageSelected(photoFile?.absolutePath)
            }
        }
    )

    // For permission request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            cameraPermissionGranted = isGranted
            if (isGranted) {
                // Launch camera immediately after permission is granted
                launchCamera(context, cameraLauncher) { file ->
                    photoFile = file
                }
            }
        }
    )

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            
            // Only show the toggle if an image is selected
            if (imageUri != null) {
                IconButton(
                    onClick = { useScaleCrop = !useScaleCrop },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (useScaleCrop) Icons.Default.CropFree else Icons.Default.CropSquare,
                        contentDescription = if (useScaleCrop) "Switch to fit mode" else "Switch to crop mode",
                        tint = PrimaryGreen
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(DarkSurface.copy(alpha = 0.3f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium
                )
                .clickable { 
                    if (imageUri == null) {
                        showImageSourceDialog = true
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
                    contentScale = contentScale, // Using the toggled content scale
                    modifier = Modifier.fillMaxSize()
                )
                
                // Clear button
                IconButton(
                    onClick = { onImageSelected(null) },
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
                
                // Change image button
                Button(
                    onClick = { showImageSourceDialog = true },
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = "Add plant image",
                        modifier = Modifier.size(48.dp),
                        tint = TextWhite.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap to add image",
                        color = TextWhite.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Select Image Source") },
            text = { Text("Choose where to get the image from") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        if (cameraPermissionGranted) {
                            launchCamera(context, cameraLauncher) { file ->
                                photoFile = file
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Text("Camera")
                }
            }
        )
    }
}

private fun launchCamera(
    context: Context, 
    cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>, 
    onFileCreated: (File) -> Unit
) {
    try {
        val photoFile = createImageFile(context)
        onFileCreated(photoFile)
        val photoURI = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(photoURI)
    } catch (ex: IOException) {
        Log.e("ImagePicker", "Error creating image file", ex)
    }
} 