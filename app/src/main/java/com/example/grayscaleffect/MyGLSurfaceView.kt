package com.example.grayscaleffect

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView


/**
 * @author Md Jahirul Islam Hridoy
 * Created on 17,March,2022
 */
class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer
    private var photo: Bitmap? = null
    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        renderer = MyGLRenderer()
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        photo = BitmapFactory.decodeResource(context.resources, R.drawable.forest)
        renderer.photo = photo
        //renderer.photoHeight = photo!!.width
       // renderer.photoWidth =  photo!!.height

        // Render the view only when there is a change in the drawing data.
        // To allow the triangle to rotate automatically, this line is commented out:
        renderMode = RENDERMODE_WHEN_DIRTY

        requestRender()

    }

}