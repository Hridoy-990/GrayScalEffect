package com.example.grayscaleffect.texture

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * @author Md Jahirul Islam Hridoy
 * Created on 17,March,2022
 */
class TextureRenderer {
    private var vertices = floatArrayOf(
        // x, y, z, u, v
        -1.0f, -1.0f, 0.0f, 0f, 0f,
        -1.0f, 1.0f, 0.0f, 0f, 1f,
        1.0f, 1.0f, 0.0f, 1f, 1f,
        1.0f, -1.0f, 0.0f, 1f, 0f
    )

    private var indices = intArrayOf(
        0, 1, 2, 0, 2, 3

    )

    private var program: Int
    private var vertexHandle: Int = 0
    private var bufferHandles = IntArray(2)
    private var uvsHandle: Int = 0
    private var mvpHandle: Int = 0
    private var samplerHandle: Int = 0


    private val vertexShaderCode =

        "precision highp float;" +
        "attribute vec3 vertexPosition;" +
                "attribute vec2 uvs;" +
                "varying vec2 varUvs;" +
                "uniform mat4 mvp;" +
                "void main() {" +
                "  gl_Position = mvp * vec4(vertexPosition, 1.0);" +
                "  varUvs = uvs;" +
                "}"

    private val fragmentShaderCode =

        "precision mediump float;" +
                "uniform sampler2D texSampler;" +
                "varying vec2 varUvs;" +
                "void main() {" +
                "  gl_FragColor = texture2D(texSampler, varUvs);" +
                "}"

    var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
        order(ByteOrder.nativeOrder())
        asFloatBuffer().apply {
            put(vertices)
            position(0)
        }
    }

    var indexBuffer: IntBuffer = ByteBuffer.allocateDirect(indices.size * 4).run {
        order(ByteOrder.nativeOrder())
        asIntBuffer().apply {
            put(indices)
            position(0)
        }
    }

    /**
    glBindBuffer — bind a named buffer object
    glBufferData — create and initialize a buffer object's data store
    glBlendFunc — specify pixel arithmetic and  defines the operation of blending when it is enabled
    glActiveTexture — selects which texture unit subsequent texture state calls will affect
    glBindTexture — bind a named texture to a texturing target
    glPixelStorei — sets pixel storage modes that affect the operation of subsequent
    glTexImage2D — specify a two-dimensional texture image and map the Bitmap to the texture
     */

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    init {
        // Create program
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {

            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)

            vertexHandle = GLES20.glGetAttribLocation(it, "vertexPosition")
            uvsHandle = GLES20.glGetAttribLocation(it, "uvs")
            mvpHandle = GLES20.glGetUniformLocation(it, "mvp")
            samplerHandle = GLES20.glGetUniformLocation(it, "texSampler")
        }

        // Initialize buffers
        GLES20.glGenBuffers(2, bufferHandles, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.size * 4, vertexBuffer, GLES20.GL_DYNAMIC_DRAW)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1])
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.size * 4, indexBuffer, GLES20.GL_DYNAMIC_DRAW)


        // Ensure I can draw transparent stuff that overlaps properly
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    fun draw( textureHandle : Int , bitmap: Bitmap, mvpMatrix: FloatArray) {

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glUseProgram(program)
        GLES20.glDisable(GLES20.GL_BLEND)

        // Pass transformations to shader
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        GLES20.glUniform1i(samplerHandle, 0)


        // Prepare buffers with vertices and indices & draw
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandles[0])
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, bufferHandles[1])

        GLES20.glEnableVertexAttribArray(vertexHandle)
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 4 * 5, 0)

        GLES20.glEnableVertexAttribArray(uvsHandle)
        GLES20.glVertexAttribPointer(uvsHandle, 2, GLES20.GL_FLOAT, false, 4 * 5, 3 * 4)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0)
    }

}