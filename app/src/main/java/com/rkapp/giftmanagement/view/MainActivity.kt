package com.rkapp.giftmanagement.view

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.rkapp.giftmanagement.databinding.ActivityMainBinding
import com.rkapp.giftmanagement.viewmodel.MainViewModel
import com.rkapp.giftmanagement.R
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.opencv.core.*


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_FOR_PERMISSIONS = 1234
    private val mainViewModel: MainViewModel by viewModels()

    /*** Views  */
    private lateinit var binding: ActivityMainBinding

    /*** For CameraX  */
    private var cameraInfo: CameraInfo? = null
    private var preview: Preview? = null

    companion object {
        /*** Fixed values  */
        const val TAG = "MyApp"

        init {
            System.loadLibrary("opencv_java4")
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        // Data Binding 準備(ないなら直下ので良い)
//        binding = ActivityMainBinding.inflate(layoutInflater)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val view = binding.root
        setContentView(view)
        binding.viewModel = mainViewModel

        if (binding.viewModel!!.checkPermissions(this)) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                binding.viewModel!!.REQUIRED_PERMISSIONS,
                REQUEST_CODE_FOR_PERMISSIONS
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            lifecycleScope.launch {
                runCatching {
                    val cameraProvider = cameraProviderFuture.get()
                    preview = Preview.Builder().build()
                    cameraInfo = binding.viewModel!!.getCameraInfo(
                        this@MainActivity,
                        MyImageAnalyzer(),
                        cameraProvider,
                        preview!!
                    )
                }.fold(
                    onSuccess = {
                        preview!!.setSurfaceProvider(binding.previewView.createSurfaceProvider(cameraInfo))
                    },
                    onFailure = {
                        Log.e(TAG, "[startCamera] Use case binding failed", it)
                    }
                )
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_FOR_PERMISSIONS) {
            if (binding.viewModel!!.checkPermissions(this)) {
                startCamera()
            } else {
                Log.i(TAG, "[onRequestPermissionsResult] Failed to get permissions")
                finish()
            }
        }
    }

    // 画像処理を行うインナークラス
    inner class MyImageAnalyzer : ImageAnalysis.Analyzer {
        private var matPrevious: Mat? = null
        private fun bitmapFromImage(image: ImageProxy): Flow<Bitmap> =
            flow {
                /* Create cv::mat(RGB888) from image(NV21) */
                val matOrg = binding.viewModel!!.getMatFromImage(image)

                /* Fix image rotation (it looks image in PreviewView is automatically fixed by CameraX???) */
                val mat = binding.viewModel!!.fixMatRotation(matOrg, binding.previewView.display.rotation)
                Log.i(
                    TAG,
                    "[analyze] width = " + image.width + ", height = " + image.height + ", Rotation = " + binding.previewView.display.rotation
                )
                Log.i(TAG, "[analyze] mat width = " + matOrg.cols() + ", mat height = " + matOrg.rows())

                /* Do some image processing */
                val matOutput = binding.viewModel!!.makeMatImages(mat, matPrevious)
                matPrevious = matOutput.second

                /* Draw something for test */
                binding.viewModel!!.drawSomething(matOutput.first)

                /* Convert cv::mat to bitmap for drawing */
                emit(binding.viewModel!!.bitmapFromMat(matOutput.first))
            }.catch {
                /* ライフサイクル再スタート */
                /* 回転させるとサイズが変わるためここで落ちる */
            }

        override fun analyze(image: ImageProxy) {
            lifecycleScope.launch {
                bitmapFromImage(image).collect {
                    binding.imageView.setImageBitmap(it)
                }
                /* Close the image otherwise, this function is not called next time */
                image.close()
            }
        }
    }
}