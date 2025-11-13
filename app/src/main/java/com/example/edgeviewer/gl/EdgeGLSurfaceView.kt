package com.example.edgeviewer.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class EdgeGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer = EdgeRenderer()

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateFrame(rgba: ByteArray, w: Int, h: Int) {
        renderer.updateFrame(rgba, w, h)
    }
}
