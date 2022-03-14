package com.rkapp.giftmanagement.usecase

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import com.rkapp.giftmanagement.view.MainActivity
import java.util.concurrent.Executors

private var useCase: CameraUseCase? = null

class CameraUseCase {

    /*** For CameraX  */
    private var camera: Camera? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    companion object {
        /**
         * インスタンス取得.
         */
        val INSTANCE
            get() = useCase?: CameraUseCase().also {
                useCase = it
            }
    }

    fun getCameraInfo(
        context: Context,
        myImageAnalyzer: MainActivity.MyImageAnalyzer,
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
    ): CameraInfo {
        /* カメラ情報を取得する */
        imageAnalysis = ImageAnalysis.Builder().build()
        imageAnalysis!!.setAnalyzer(cameraExecutor, myImageAnalyzer)
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
        cameraProvider.unbindAll()
        camera = cameraProvider.bindToLifecycle(
            (context as LifecycleOwner),
            cameraSelector,
            preview,
            imageAnalysis
        )
        return camera!!.cameraInfo
    }
}