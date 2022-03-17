package com.example.grayscaleffect

import android.graphics.Bitmap
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.media.effect.EffectFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.os.SystemClock
import com.example.grayscaleffect.texture.TextureRenderer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * @author Md Jahirul Islam Hridoy
 * Created on 17,March,2022
 */
class MyGLRenderer : GLSurfaceView.Renderer {

    private lateinit var mTexture : TextureRenderer

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)
    var textureHandle = IntArray(2)

    private var effectContext: EffectContext? = null
    private var effect: Effect? = null

     var photo : Bitmap? = null
     var photoWidth = 0
     var photoHeight = 0

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mTexture = TextureRenderer()
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()

        photoWidth  = width
        photoHeight = height
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)

        generateSquare()

    }

    private fun grayScaleEffect() {
        val factory = effectContext!!.factory
        effect = factory.createEffect(EffectFactory.EFFECT_DOCUMENTARY)
        effect!!.apply(textureHandle[0], photoWidth, photoHeight, textureHandle[1])
    }

    private fun generateSquare() {
        GLES20.glGenTextures(2, textureHandle, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
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
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, photo, 0)
    }

    override fun onDrawFrame(unused: GL10?) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)


        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        // Calculate the projection and view transformation
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        val time = SystemClock.uptimeMillis() % 4000L
        val angle = 0.090f * time.toInt()
        val scratch = FloatArray(16)

        Matrix.setRotateM(rotationMatrix, 0, 180f, 0f, 0f, -1.0f)
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        //Media Effect
        if(effectContext==null)  effectContext = EffectContext.createWithCurrentGlContext()

        if(effect!=null) effect!!.release()

        grayScaleEffect()

        mTexture.draw(textureHandle[1] , photo!! , scratch)


    }
}