package kr.cjs.catty.view

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import kr.cjs.catty.detection.ObjectDetectorHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraDetectionScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var detectionResults by remember { mutableStateOf<ObjectDetectorHelper.ResultBundle?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var inferenceTime by remember { mutableStateOf(0L) }
    
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    val objectDetectorHelper = remember {
        ObjectDetectorHelper(
            context = context,
            threshold = 0.5f,
            maxResults = 5,
            currentDelegate = ObjectDetectorHelper.DELEGATE_CPU,
            runningMode = RunningMode.LIVE_STREAM,
            objectDetectorListener = object : ObjectDetectorHelper.DetectorListener {
                override fun onError(error: String) {
                    errorMessage = error
                }

                override fun onResults(resultBundle: ObjectDetectorHelper.ResultBundle) {
                    detectionResults = resultBundle
                    inferenceTime = resultBundle.inferenceTime
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            objectDetectorHelper.clearObjectDetector()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                val bitmap = imageProxy.toBitmap()
                                val rotatedBitmap = bitmap.rotate(imageProxy.imageInfo.rotationDegrees)
                                
                                objectDetectorHelper.detectAsync(
                                    rotatedBitmap,
                                    imageProxy.imageInfo.timestamp
                                )
                                
                                imageProxy.close()
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        errorMessage = "Camera binding failed: ${e.message}"
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 검출 결과 오버레이
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

        // 정보 표시
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Inference: ${inferenceTime}ms",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    detectionResults?.results?.forEach { result ->
                        result.detections().forEach { detection ->
                            val category = detection.categories().firstOrNull()
                            category?.let {
                                Text(
                                    text = "${it.categoryName()}: ${(it.score() * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun Bitmap.rotate(degrees: Int): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}