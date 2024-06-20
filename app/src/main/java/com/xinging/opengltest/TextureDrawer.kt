package com.xinging.opengltest

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

class TextureDrawer : IDrawer{

    companion object{
        val vertexShaderSource =
            """
            #version 300 es
            layout(location = 0) in vec3 aPos;
            layout(location = 1) in vec2 aTexCoord;
            out vec2 texCoord;
            void main()
            {
                gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
                texCoord = aTexCoord;
            }
            """.trimIndent()

        val fragmentShaderSource =
            """
            #version 300 es
            in vec2 texCoord;
            uniform sampler2D texture1;
            out vec4 FragColor;
            void main()
            {
                FragColor = texture(texture1, texCoord);
            }
            """.trimIndent()
    }

    private val vertices = floatArrayOf(
        // positions       // texture coords
        0.5f, 0.5f, 0.0f,  1.0f, 1.0f,   // top right
        0.5f, -0.5f, 0.0f, 1.0f, 0.0f,  // bottom right
        -0.5f, -0.5f, 0.0f,0.0f, 0.0f, // bottom left
        -0.5f, 0.5f, 0.0f, 0.0f, 1.0f   // top left
    )

    private val indices = intArrayOf(
        0, 1, 3, // first triangle
        1, 2, 3  // second triangle
    )

    val vaos = IntBuffer.allocate(1)
    val vbos = IntBuffer.allocate(1)
    val texIds = IntBuffer.allocate(1)

    private val sharer = Shader(
        vertexShaderSource,
        fragmentShaderSource
    )

    fun checkGlError(op: String) {
        var error: Int
        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            Log.e(
                "GLES30Error",
                "$op: glError $error"
            )
            throw java.lang.RuntimeException("$op: glError $error")
        }
    }

    override fun prepare(context: Context){
        // compile shader
        sharer.prepareShaders()
        checkGlError("compile shader")

        // prepare vbo data
        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).order(
            ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        // generate vao and vbo
        GLES30.glGenVertexArrays(1, vaos)
        GLES30.glGenBuffers(1, vbos)
        checkGlError("gen vertex array and buffer")

        // configure vao and vbo....
        GLES30.glBindVertexArray(vaos[0])
        // set vbo data
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0])
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,
            Float.SIZE_BYTES * vertices.size,
            vertexBuffer,
            GLES30.GL_STATIC_DRAW
        )
        checkGlError("glBufferData")

        // set vao attribute
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 6 * Float.SIZE_BYTES, 3 * Float.SIZE_BYTES)
        GLES30.glEnableVertexAttribArray(1)

        // unbind
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindVertexArray(0)

        // prepare texture
        // generate texture id
        GLES30.glGenTextures(texIds.capacity(), texIds)
        checkGlError("glGenTextures")
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texIds[0])
        checkGlError("glBindTexture")

        // set filtering
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        checkGlError("glTexParameteri")

        // set texture image data
        val options = BitmapFactory.Options()
        options.inScaled = false   // No pre-scaling
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.lye, options)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        checkGlError("texImage2D")
        bitmap.recycle()

        // unbind texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)


        // use share program and set texture location
        sharer.use()
        sharer.setInt("texture1", 0)
    }

    override fun draw(){
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glBindVertexArray(vaos[0])
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texIds[0])

        val indicesBuffer = ByteBuffer.allocateDirect(indices.size * Int.SIZE_BYTES).order(
            ByteOrder.nativeOrder()).asIntBuffer()
        indicesBuffer.put(indices)
        indicesBuffer.position(0)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, indicesBuffer)
    }
}