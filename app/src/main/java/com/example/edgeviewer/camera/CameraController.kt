package com.example.edgeviewer.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface

private const val TAG = "CameraController"

class CameraController(
    private val context: Context,
    private val onFrame: (yBytes: ByteArray, width: Int, height: Int) -> Unit
) {

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    @SuppressLint("MissingPermission")
    fun start() {
        handlerThread = HandlerThread("CameraThread").also { it.start() }
        handler = Handler(handlerThread!!.looper)

        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.first { id ->
            val chars = manager.getCameraCharacteristics(id)
            val facing = chars.get(CameraCharacteristics.LENS_FACING)
            facing == CameraCharacteristics.LENS_FACING_BACK
        }

        val streamConfig = manager.getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val previewSize = streamConfig!!
            .getOutputSizes(ImageFormat.YUV_420_888)
            .first { it.width >= 640 }

        imageReader = ImageReader.newInstance(
            previewSize.width,
            previewSize.height,
            ImageFormat.YUV_420_888,
            2
        ).apply {
            setOnImageAvailableListener({ reader ->
                val img = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                val yPlane = img.planes[0]
                val buffer = yPlane.buffer
                val yBytes = ByteArray(buffer.remaining())
                buffer.get(yBytes)
                img.close()

                onFrame(yBytes, previewSize.width, previewSize.height)
            }, handler)
        }

        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Log.e(TAG, "Camera error: $error")
                camera.close()
            }
        }, handler)
    }

    private fun createSession() {
        val camera = cameraDevice ?: return
        val readerSurface = imageReader!!.surface

        camera.createCaptureSession(
            listOf(readerSurface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    val req = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                        addTarget(readerSurface)
                        set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    }
                    session.setRepeatingRequest(req.build(), null, handler)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Camera session configure failed")
                }
            },
            handler
        )
    }

    fun stop() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        handlerThread?.quitSafely()
        handlerThread = null
    }
}
