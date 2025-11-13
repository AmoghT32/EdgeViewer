package com.example.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.edgeviewer.camera.CameraController
import com.example.edgeviewer.databinding.ActivityMainBinding
import com.example.edgeviewer.nativebridge.NativeBridge

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private var cameraController: CameraController? = null
    private var rgbaBuffer: ByteArray? = null

    private val reqPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NativeBridge.initNative()
        askPermission()
    }

    private fun askPermission() {
        if (hasPermission()) startCamera()
        else reqPermission.launch(Manifest.permission.CAMERA)
    }

    private fun hasPermission() =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        if (cameraController != null) return

        cameraController = CameraController(this) { yBytes, w, h ->
            val outSize = w * h * 4
            if (rgbaBuffer == null || rgbaBuffer!!.size != outSize)
                rgbaBuffer = ByteArray(outSize)

            NativeBridge.processFrame(yBytes, w, h, rgbaBuffer!!)

            binding.glView.queueEvent {
                binding.glView.updateFrame(rgbaBuffer!!, w, h)
            }
        }
        cameraController?.start()
    }

    override fun onPause() {
        super.onPause()
        cameraController?.stop()
    }
}
