package com.rkapp.giftmanagement.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.rkapp.giftmanagement.usecase.CameraUseCase
import com.rkapp.giftmanagement.usecase.ImageUseCase
import com.rkapp.giftmanagement.view.MainActivity

class MainViewModel : ViewModel() {
    private val cameraUseCase = CameraUseCase.INSTANCE
    private val imageUseCase = ImageUseCase.INSTANCE

    fun checkPermissions(
        context: Context
    ): Boolean {
        for (permission in MainActivity.REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    /**
     * Camera
     */
    fun getCameraInfo(
        context: Context,
        myImageAnalyzer: MainActivity.ImageAnalyze,
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
    ): CameraInfo {
        return cameraUseCase.getCameraInfo(context, myImageAnalyzer, cameraProvider, preview)
    }

    /**
     * Image
      */
    fun imageFromProxy(
        proxy: ImageProxy
    ): InputImage {
        return imageUseCase.imageFromProxy(proxy)
    }
    fun parseResultText(
        result: Text
    ): Array<String?> {
        return imageUseCase.parseResultText(result)
    }
}