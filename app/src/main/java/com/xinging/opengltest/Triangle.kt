package com.xinging.opengltest

import android.opengl.GLES30
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL

class Triangle {
    private val TAG = "Triangle"

    private val vertices = floatArrayOf(
        -0.5f, -0.5f, 0.0f, // left
        0.5f, -0.5f, 0.0f, // right
        0.0f, 0.5f, 0.0f  // top
    )

    private val vertexShaderSource = """
    #version 300 es
    layout (location = 0) in vec3 aPos;
    void main()
    {
        gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
    }
    """.trimIndent()

    private val fragmentShaderSource = """
    #version 300 es
    out vec4 FragColor;
    void main()
    {
        FragColor = vec4(0.0f, 0.5f, 0.2f, 1.0f);
    }
    """.trimIndent()

    val vaos: IntArray = intArrayOf(0)
    val vbos: IntArray = intArrayOf(0)
    var shaderProgram: Int = 0

    fun prepareOpenGL(){
        var success: IntArray = intArrayOf(0)

        // compile vertex shader
        val vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        GLES30.glShaderSource(vertexShader, vertexShaderSource)
        GLES30.glCompileShader(vertexShader)
        GLES30.glGetShaderiv(vertexShader, GLES30.GL_COMPILE_STATUS, success, 0)
        if (success[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(vertexShader)
            Log.e(TAG, "compile vertex source failed.$log")
        }

        // compile fragment shader
        val fragmentShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(fragmentShader, fragmentShaderSource)
        GLES30.glCompileShader(fragmentShader)
        GLES30.glGetShaderiv(fragmentShader, GLES30.GL_COMPILE_STATUS, success, 0)
        if (success[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(fragmentShader)
            Log.e(TAG, "compile fragment source failed. $log")
        }

        // link shaders
        shaderProgram = GLES30.glCreateProgram()
        GLES30.glAttachShader(shaderProgram, vertexShader)
        GLES30.glAttachShader(shaderProgram, fragmentShader)
        GLES30.glLinkProgram(shaderProgram)

        GLES30.glGetProgramiv(shaderProgram, GLES30.GL_LINK_STATUS, success, 0)
        if (success[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(shaderProgram)
            Log.e(TAG, "link shader program failed. $log")
        }

        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

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