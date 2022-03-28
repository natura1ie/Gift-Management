package com.rkapp.giftmanagement.usecase

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text

private var useCase: ImageUseCase? = null

class ImageUseCase {

    companion object {
        // インスタンス取得
        val INSTANCE
            get() = useCase?: ImageUseCase().also {
                useCase = it
            }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun imageFromProxy(
        proxy: ImageProxy
    ): InputImage {
        return InputImage.fromMediaImage(proxy.image!!, proxy.imageInfo.rotationDegrees)
    }

    // 認識された文字列をパースする
    fun parseResultText(
        result: Text
    ): Array<String?> {
        var resultTxtList:Array<String?> = arrayOf(null)
        for (block in result.textBlocks) {
            val blockText = block.text
            resultTxtList += blockText
        }
        return resultTxtList
    }
}