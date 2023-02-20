package com.xinging.opengltest

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Triangle {
    private val vertices = floatArrayOf(
        -0.5f, -0.5f, 0.0f, // left
        0.5f, -0.5f, 0.0f, // right
        0.0f, 0.5f, 0.0f  // top
    )

    val vaos: IntArray = intArrayOf(0)
    val vbos: IntArray = intArrayOf(0)

    fun prepareData(){
        // prepare vbo data
        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        // generate vao and vbo
        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(1, vbos, 0)

        GLES30.glBindVertexArray(vaos[0])

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0])
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            Float.SIZE_BYTES * vertices.size,
            vertexBuffer,
            GLES30.GL_STATIC_DRAW
        )

        GLES30.glVertexAttribPointer(
            0, 3, GLES30.GL_FLOAT, false,
            3 * Float.SIZE_BYTES, 0
        )
        GLES30.glEnableVertexAttribArray(0)

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)
    }
}