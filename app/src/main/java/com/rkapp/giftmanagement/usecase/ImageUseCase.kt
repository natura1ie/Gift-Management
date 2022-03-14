package com.rkapp.giftmanagement.usecase

import android.graphics.Bitmap
import android.view.Surface
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

private var useCase: ImageUseCase? = null

class ImageUseCase {

    companion object {
        /**
         * インスタンス取得.
         */
        val INSTANCE
            get() = useCase?: ImageUseCase().also {
                useCase = it
            }
    }

    fun getMatFromImage(
        image: ImageProxy
    ): Mat {
        /* Mat画像を取得する */
        /* https://stackoverflow.com/questions/30510928/convert-android-camera2-api-yuv-420-888-to-rgb */
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]
        val yuv = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
        yuv.put(0, 0, nv21)
        val matDst = Mat()
        Imgproc.cvtColor(yuv, matDst, Imgproc.COLOR_YUV2RGB_NV21, 3)
        return matDst
    }

    fun fixMatRotation(
        matOrg: Mat,
        rotation: Int
    ): Mat {
        /* 回転による補正をかける */
        val matDst: Mat
        when (rotation) {
            Surface.ROTATION_0 -> {
                matDst = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                Core.transpose(matOrg, matDst)
                Core.flip(matDst, matDst, 1)
            }
            Surface.ROTATION_90 -> matDst = matOrg
            Surface.ROTATION_270 -> {
                matDst = matOrg
                Core.flip(matDst, matDst, -1)
            }
            else -> {
                matDst = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                Core.transpose(matOrg, matDst)
                Core.flip(matDst, matDst, 1)
            }
        }
        return matDst
    }

    fun bitmapFromMat(
        matSrc: Mat
    ): Bitmap {
        /* 描画したMat画像をBitmapに変換する */
        val bitmap =
            Bitmap.createBitmap(matSrc.cols(), matSrc.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matSrc, bitmap)
        return bitmap
    }

    fun makeMatImages(
        matSrc: Mat,
        matPreviousSrc: Mat?,
    ): Pair<Mat, Mat> {
        /* OpenCV関連の実装はここで行う */
        var matPrevious: Mat? = matPreviousSrc
        val matDst = Mat(matSrc.rows(), matSrc.cols(), matSrc.type())

        if (matPrevious == null) matPrevious = matSrc

        /* 差分 */
        Core.absdiff(matSrc, matPrevious, matDst)
        matPrevious = matSrc

        return matDst to matPrevious
    }

    fun drawSomething(
        mat: Mat
    ) {
        /* 作成されたMat画像の上に描画する */
    }
}