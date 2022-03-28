package com.rkapp.giftmanagement.usecase

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import com.rkapp.giftmanagement.view.MainActivity
import java.util.concurrent.Executors

private var useCase: CameraUseCase? = null

class CameraUseCase {

    /*** CameraX  */
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    companion object {
        // インスタンス取得
        val INSTANCE
            get() = useCase?: CameraUseCase().also {
                useCase = it
            }
    }

    // カメラ情報を取得する
    fun getCameraInfo(
        context: Context,
        myImageAnalyzer: MainActivity.ImageAnalyze,
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
    ): CameraInfo {
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, myImageAnalyzer)
            }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        cameraProvider.unbindAll()

        camera = cameraProvider.bindToLifecycle(
            (context as LifecycleOwner),
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )

        return camera!!.cameraInfo
    }
}