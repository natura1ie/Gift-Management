package com.rkapp.giftmanagement.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.rkapp.giftmanagement.usecase.CameraUseCase
import com.rkapp.giftmanagement.usecase.ImageUseCase
import com.rkapp.giftmanagement.view.MainActivity
import org.opencv.core.Mat

class MainViewModel : ViewModel() {
    private val cameraUseCase = CameraUseCase.INSTANCE
    private val imageUseCase = ImageUseCase.INSTANCE
    val REQUIRED_PERMISSIONS =
        arrayOf("android.permission.CAMERA")

    fun checkPermissions(
        context: Context
    ): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
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

    /*** Camera  */
    fun getCameraInfo(
        context: Context,
        myImageAnalyzer: MainActivity.MyImageAnalyzer,
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
    ): CameraInfo {
        return cameraUseCase.getCameraInfo(context, myImageAnalyzer, cameraProvider, preview)
    }

    /*** Image  */
    fun getMatFromImage(image: ImageProxy): Mat {
        return imageUseCase.getMatFromImage(image)
    }

    fun fixMatRotation(matOrg: Mat, rotation: Int): Mat {
        return imageUseCase.fixMatRotation(matOrg, rotation)
    }

    fun bitmapFromMat(mat: Mat): Bitmap {
        return imageUseCase.bitmapFromMat(mat)
    }

    fun makeMatImages(
        matSrc: Mat,
        matPreviousSrc: Mat?,
    ): Pair<Mat, Mat> {
        return imageUseCase.makeMatImages(matSrc, matPreviousSrc)
    }

    fun drawSomething(mat: Mat) {
        return imageUseCase.drawSomething(mat)
    }
}