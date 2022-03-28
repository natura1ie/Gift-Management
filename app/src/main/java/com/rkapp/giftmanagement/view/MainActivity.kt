package com.rkapp.giftmanagement.view

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.rkapp.giftmanagement.R
import com.rkapp.giftmanagement.databinding.ActivityMainBinding
import com.rkapp.giftmanagement.viewmodel.MainViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

typealias ODetection = (odt: Array<String?>) -> Unit

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1234
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    /*** View関連  */
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    /*** CameraX関連  */
    private var preview: Preview? = null
    private var cameraInfo: CameraInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // DataBindingをしないなら直下ので良い
//        binding = ActivityMainBinding.inflate(layoutInflater)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val view = binding.root
        setContentView(view)
        binding.viewModel = mainViewModel

        if (binding.viewModel!!.checkPermissions(baseContext)) {
            startCamera()
        } else {
            // カメラパーミッションをリクエスト
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    // カメラ起動
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val frameLayout = FrameLayout(this)
        cameraProviderFuture.addListener({
            lifecycleScope.launch {
                runCatching {
                    val cameraProvider = cameraProviderFuture.get()
                    preview = Preview.Builder().build()
                    // カメラ情報を取得する
                    cameraInfo = binding.viewModel!!.getCameraInfo(
                        this@MainActivity,
                        ImageAnalyze { txtArr ->
                            var showTxt = ""
                            frameLayout.removeAllViews()
                            for (txt in txtArr){
                                txt?.let{
                                    showTxt += " $txt"
                                }
                            }
                            binding.bottomSheetText.text = showTxt
                        },
                        cameraProvider,
                        preview!!
                    )
                }.fold(
                    onSuccess = {
                        preview!!.setSurfaceProvider(binding.previewView.createSurfaceProvider(cameraInfo))
                    },
                    onFailure = {
                        Toast.makeText(this@MainActivity,
                            "カメラ起動失敗",
                            Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // 1フレーム単位で画像を分析する
    inner class ImageAnalyze (private val listener: ODetection): ImageAnalysis.Analyzer {
        private val labelOptions = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()

        // 英語のみの場合は直下ので良い
//        private val textOptions = TextRecognizerOptions.DEFAULT_OPTIONS
        private val jTextOptions = JapaneseTextRecognizerOptions.Builder().build()

        private val labeler = ImageLabeling.getClient(labelOptions)

        private val textRecognizer = TextRecognition.getClient(jTextOptions)

        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
        override fun analyze(proxy: ImageProxy) {
            lifecycleScope.launch {
                // 画像分類
                imageFromProxy(proxy).collect {
                    labeler.process(it)
                        .addOnSuccessListener { labels ->
                            for (label in labels) {
                                val text = label.text
                                Log.d("MyApp", "text: $text")
                                if (text == "Paper") {
                                    // 紙の文字だけ認識する　
                                    runBlocking {
                                        doTextRecognition(it)
                                    }
                                } else {
                                    // TODO:あとで考える
                                }
                            }
                        }
                        .addOnCompleteListener {
                            proxy.close()
                        }
                }
            }
        }

        @SuppressLint("UnsafeOptInUsageError")
        private fun imageFromProxy(proxy: ImageProxy): Flow<InputImage> =
            flow {
                emit(binding.viewModel!!.imageFromProxy(proxy))
            }.catch {
                /**
                 *  ライフサイクル再スタート
                 *  回転させるとサイズが変わるためここで落ちる
                 */
            }

        private fun doTextRecognition(image: InputImage) {
            textRecognizer.process(image)
                .addOnSuccessListener { firebaseVisionText ->
                    // 認識した文字列を結果に反映する
                    listener(binding.viewModel!!.parseResultText(firebaseVisionText))
                }
        }
    }
}