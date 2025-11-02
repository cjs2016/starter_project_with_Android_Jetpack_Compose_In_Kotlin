package kr.cjs.catty.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.cjs.catty.detection.ObjectDetectorHelper

@Composable
fun ObjectDetectionScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedImage by remember { mutableStateOf<Bitmap?>(null) }
    var detectionResults by remember { mutableStateOf<ObjectDetectorHelper.ResultBundle?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    isProcessing = true
                    errorMessage = null
                    
                    val bitmap = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(it)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                    
                    bitmap?.let { bmp ->
                        selectedImage = bmp
                        
                        withContext(Dispatchers.Default) {
                            val detector = ObjectDetectorHelper(
                                context = context,
                                threshold = 0.5f,
                                maxResults = 5,
                                objectDetectorListener = object : ObjectDetectorHelper.DetectorListener {
                                    override fun onError(error: String) {
                                        errorMessage = error
                                    }

                                    override fun onResults(resultBundle: ObjectDetectorHelper.ResultBundle) {
                                        detectionResults = resultBundle
                                    }
                                }
                            )
                            
                            detector.detect(bmp)
                            detector.clearObjectDetector()
                        }
                    }
                } catch (e: Exception) {
                    errorMessage = "Error loading image: ${e.message}"
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Object Detection",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Select Image")
        }

        if (!hasCameraPermission) {
            Button(
                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Request Camera Permission")
            }
        }

        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (isProcessing) {
            CircularProgressIndicator()
        }

        selectedImage?.let { bitmap ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize()
                )

                detectionResults?.let { results ->
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val scaleX = size.width / results.inputImageWidth
                        val scaleY = size.height / results.inputImageHeight

                        results.results.forEach { result ->
                            result.detections().forEach { detection ->
                                val boundingBox = detection.boundingBox()
                                
                                drawRect(
                                    color = Color.Green,
                                    topLeft = Offset(
                                        boundingBox.left * scaleX,
                                        boundingBox.top * scaleY
                                    ),
                                    size = Size(
                                        boundingBox.width() * scaleX,
                                        boundingBox.height() * scaleY
                                    ),
                                    style = Stroke(width = 4f)
                                )
                            }
                        }
                    }
                }
            }

            detectionResults?.let { results ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Results",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "Inference time: ${results.inferenceTime}ms",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        results.results.forEach { result ->
                            result.detections().forEach { detection ->
                                val category = detection.categories().firstOrNull()
                                category?.let {
                                    Text(
                                        text = "${it.categoryName()}: ${(it.score() * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}