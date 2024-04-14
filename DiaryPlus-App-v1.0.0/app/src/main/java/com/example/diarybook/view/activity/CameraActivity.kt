package com.example.diarybook.view.activity

import android.Manifest
import com.example.diarybook.viewmodel.CameraViewModel
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.diarybook.R
import com.example.diarybook.databinding.ActivityCameraBinding
import com.example.diarybook.constant.Constant.CAMERA_PHOTO

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewModel: CameraViewModel
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest.Builder
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var imageReader: ImageReader
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this@CameraActivity)[CameraViewModel::class.java]

        handlerInit()
        setupCameraScreen()
        observeLiveData()

    }

    private fun setupCameraScreen() = with(binding){

        val captureButton = findViewById<ImageView>(R.id.cameraCaptureButton)
        val switchButton = findViewById<ImageView>(R.id.cameraReverseButton)

        cameraTexture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                openCamera(binding)
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {

                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {}

        }

        imageReaderInit(this@CameraActivity) { imageUri ->
            closeCameraActivityWithUri(imageUri)
        }

        captureButton.setOnClickListener {
            captureRequestOperations()
        }

        switchButton.setOnClickListener {
            //switchCamera()
        }
    }

    private fun switchCamera(){
        cameraDevice.close()
        val cameraIdList = cameraManager.cameraIdList
        for (cameraId in cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraOperation(camera, binding)
                    }

                    override fun onDisconnected(camera: CameraDevice) {

                    }

                    override fun onError(camera: CameraDevice, error: Int) {

                    }
                }, handler)
                break
            }
        }
    }

    private fun handlerInit() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        handlerThread = HandlerThread("cameraThread")
        handlerThread.start()
        handler = Handler((handlerThread).looper)
    }

    private fun imageReaderInit(activity: Activity, onImageUriReady: (Uri) -> Unit) {
        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader?.acquireLatestImage()
            if (image != null) {
                viewModel.saveImageToFile(activity, image, onImageUriReady)
            }
        }, handler)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(binding: ActivityCameraBinding) {
        cameraManager.openCamera(
            cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {
                override fun onOpened(p0: CameraDevice) {
                    cameraOperation(p0, binding)
                }

                override fun onDisconnected(p0: CameraDevice) {

                }

                override fun onError(p0: CameraDevice, p1: Int) {

                }

            }, handler
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice.close()
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
    }

    private fun observeLiveData() = with(binding){

        viewModel.cameraToastMessage.observe(this@CameraActivity, Observer { toastMessage ->
            Toast.makeText(this@CameraActivity,toastMessage,Toast.LENGTH_SHORT).show()
        })

    }

    private fun cameraOperation(p0: CameraDevice, binding: ActivityCameraBinding) {
        cameraDevice = p0
        captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        val surface = Surface(binding.cameraTexture.surfaceTexture)
        captureRequest.addTarget(surface)
        cameraDevice.createCaptureSession(
            listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(p0: CameraCaptureSession) {
                    cameraCaptureSession = p0
                    cameraCaptureSession.setRepeatingRequest(captureRequest.build(), null, null)
                }

                override fun onConfigureFailed(p0: CameraCaptureSession) {

                }
            }, handler
        )
    }

    private fun captureRequestOperations() {
        captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequest.addTarget(imageReader.surface)
        cameraCaptureSession.capture(captureRequest.build(), null, null)
    }

    private fun closeCameraActivityWithUri(imageUri: Uri) {
        viewModel.savePhotoData(CAMERA_PHOTO,imageUri.toString())
        this@CameraActivity.finish()
    }
}






