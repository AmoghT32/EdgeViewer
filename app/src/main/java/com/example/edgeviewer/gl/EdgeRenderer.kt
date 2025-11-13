package com.example.edgeviewer.gl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class EdgeRenderer : GLSurfaceView.Renderer {

    private var program = 0
    private var textureId = 0
    private var frameBuffer: ByteBuffer? = null
    private var frameWidth = 0
    private var frameHeight = 0
    @Volatile private var frameDirty = false

    private val vertexData: FloatBuffer =
        ByteBuffer.allocateDirect(4 * 4 * java.lang.Float.BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(floatArrayOf(
                    -1f, -1f,
                    1f, -1f,
                    -1f,  1f,
                    1f,  1f
                ))
                position(0)
            }

    private val texCoordData: FloatBuffer =
        ByteBuffer.allocateDirect(4 * 2 * java.lang.Float.BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(floatArrayOf(
                    0f, 1f,
                    1f, 1f,
                    0f, 0f,
                    1f, 0f
                ))
                position(0)
            }

    private val mvp = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        textureId = createTexture()
        Matrix.setIdentityM(mvp, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (frameDirty && frameBuffer != null && frameWidth > 0 && frameHeight > 0) {
            synchronized(this) {
                frameBuffer?.position(0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    frameWidth,
                    frameHeight,
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    frameBuffer
                )
                frameDirty = false
            }
        }

        GLES20.glUseProgram(program)

        val aPos = GLES20.glGetAttribLocation(program, "aPosition")
        val aTex = GLES20.glGetAttribLocation(program, "aTexCoord")
        val uMvp = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val uTex = GLES20.glGetUniformLocation(program, "uTexture")

        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 0, vertexData)

        GLES20.glEnableVertexAttribArray(aTex)
        GLES20.glVertexAttribPointer(aTex, 2, GLES20.GL_FLOAT, false, 0, texCoordData)

        GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(uTex, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aTex)
    }

    fun updateFrame(rgbaBytes: ByteArray, width: Int, height: Int) {
        synchronized(this) {
            if (frameBuffer == null || frameWidth != width || frameHeight != height) {
                frameWidth = width
                frameHeight = height
                frameBuffer = ByteBuffer.allocateDirect(rgbaBytes.size)
                    .order(ByteOrder.nativeOrder())
            }
            frameBuffer?.position(0)
            frameBuffer?.put(rgbaBytes)
            frameDirty = true
        }
    }

    private fun createTexture(): Int {
        val tex = IntArray(1)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        return tex[0]
    }

    private fun createProgram(vs: String, fs: String): Int {
        fun loadShader(type: Int, src: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, src)
            GLES20.glCompileShader(shader)
            return shader
        }
        val v = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val f = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, v)
        GLES20.glAttachShader(prog, f)
        GLES20.glLinkProgram(prog)
        return prog
    }

    companion object {
        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            uniform mat4 uMVPMatrix;
            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vTexCoord = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """
    }
}
