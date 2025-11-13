package com.example.edgeviewer.nativebridge

object NativeBridge {
    init {
        System.loadLibrary("edgeproc")
    }

    external fun initNative()
    external fun processFrame(
        yPlane: ByteArray,
        width: Int,
        height: Int,
        outRgba: ByteArray
    )
}
