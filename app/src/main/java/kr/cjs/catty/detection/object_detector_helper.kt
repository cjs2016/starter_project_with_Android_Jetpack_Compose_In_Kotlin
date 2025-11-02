package kr.cjs.catty.detection

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

class ObjectDetectorHelper(
    val threshold: Float = 0.5f,
    val maxResults: Int = 3,
    val currentDelegate: Int = DELEGATE_CPU,
    val runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    val objectDetectorListener: DetectorListener? = null
) {

    private var objectDetector: ObjectDetector? = null

    init {
        setupObjectDetector()
    }

    fun clearObjectDetector() {
        objectDetector?.close()
        objectDetector = null
    }

    private fun setupObjectDetector() {
        try {
            val baseOptionsBuilder = BaseOptions.builder()

            when (currentDelegate) {
                DELEGATE_CPU -> {
                    baseOptionsBuilder.setDelegate(Delegate.CPU)
                }
                DELEGATE_GPU -> {
                    baseOptionsBuilder.setDelegate(Delegate.GPU)
                }
            }

            baseOptionsBuilder.setModelAssetPath(MODEL_NAME)

            val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setScoreThreshold(threshold)
                .setMaxResults(maxResults)
                .setRunningMode(runningMode)

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder.setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()
            objectDetector = ObjectDetector.createFromOptions(context, options)
        } catch (e: Exception) {
            objectDetectorListener?.onError("Object Detector failed to initialize: ${e.message}")
            Log.e(TAG, "ObjectDetector failed to load: ${e.message}")
        }
    }

    fun detect(image: Bitmap) {
        if (runningMode != RunningMode.IMAGE) {
            throw IllegalArgumentException("Attempting to call detect in non-IMAGE mode")
        }

        if (objectDetector == null) {
            setupObjectDetector()
        }

        val startTime = SystemClock.uptimeMillis()

        val mpImage = BitmapImageBuilder(image).build()

        objectDetector?.detect(mpImage)?.also { result ->
            val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
            objectDetectorListener?.onResults(
                ResultBundle(
                    listOf(result),
                    inferenceTimeMs,
                    image.height,
                    image.width
                )
            )
        }
    }

    fun detectAsync(image: Bitmap, frameTime: Long) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw IllegalArgumentException("Attempting to call detectAsync in non-LIVE_STREAM mode")
        }

        if (objectDetector == null) {
            setupObjectDetector()
        }

        val mpImage = BitmapImageBuilder(image).build()
        objectDetector?.detectAsync(mpImage, frameTime)
    }

    private fun returnLivestreamResult(
        result: ObjectDetectorResult,
        input: com.google.mediapipe.framework.image.MPImage
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        objectDetectorListener?.onResults(
            ResultBundle(
                listOf(result),
                inferenceTime,
                input.height,
                input.width
            )
        )
    }

    private fun returnLivestreamError(error: RuntimeException) {
        objectDetectorListener?.onError(error.message ?: "An unknown error has occurred")
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(resultBundle: ResultBundle)
    }

    data class ResultBundle(
        val results: List<ObjectDetectorResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int
    )

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val MODEL_NAME = "efficientdet_lite0.tflite"
        private const val TAG = "ObjectDetectorHelper"
    }
}