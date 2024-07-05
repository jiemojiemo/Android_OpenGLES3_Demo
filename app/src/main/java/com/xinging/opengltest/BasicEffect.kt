package com.xinging.opengltest

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

enum class BasicEffectType {
    // 动态网格
    DYNAMIC_MESH,

    // 动态圆
    DYNAMIC_CIRCLE,

    // 四分屏
    QUAD_SPLIT,

    // 百叶窗
    BLINDS,

    // 溶解渐入
    DISSOLVE,

    // 劈裂
    SPLITTING,

    // 轮子
    WHEEL,

    // 马赛克
    MOSAIC
}

class BasicEffect(private val effectType: BasicEffectType) : AbstractDrawer() {

    companion object {
        val vertexShaderSource =
            """
            #version 300 es

            layout(location = 0) in vec4 a_position;
            layout(location = 1) in vec2 a_texcoord;
            
            out vec2 v_texcoord;
            
            void main()
            {
                gl_Position = a_position;
                v_texcoord = a_texcoord;
            }
            """.trimIndent()

        private val DYNAMIC_MESH_SOURCE = """
            #version 300 es
            precision mediump float;

            uniform vec2 resolution;
            uniform float offset;

            uniform sampler2D texture0;

            in vec4 v_position;
            in vec2 v_texcoord;

            out vec4 fragColor;

            void main()
            {
                vec2 imgTextCoord = v_texcoord * resolution;
                float sideLength = resolution.y / 6.0;
                float maxOffset = 0.15 * sideLength;
                float x = mod(imgTextCoord.x, floor(sideLength));
                float y = mod(imgTextCoord.y, floor(sideLength));
                
                float offsetLength = offset * maxOffset;
                if(offsetLength <= x && x <= sideLength-offsetLength
                && offsetLength <= y && y <= sideLength-offsetLength)
                {
                    fragColor = texture(texture0, v_texcoord);
                }else{
                    fragColor = vec4(1.0, 1.0, 1.0, 1.0);
                }
            }

            """.trimIndent()

        val fragmentShaderSources: Map<BasicEffectType, String> = mapOf(
            BasicEffectType.DYNAMIC_MESH to DYNAMIC_MESH_SOURCE
        )
    }

    private val vertices = floatArrayOf(
        // positions       // texture coords
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f,   // top right
        1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  // bottom right
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, // bottom left
        -1.0f, 1.0f, 0.0f, 0.0f, 1.0f   // top left
    )

    private val indices = intArrayOf(
        0, 1, 3, // first triangle
        1, 2, 3  // second triangle
    )

    val vaos = IntBuffer.allocate(1)
    val vbos = IntBuffer.allocate(1)
    val ebo = IntBuffer.allocate(1)
    val texIds = IntBuffer.allocate(1)
    var offset = 0.0f
    var offsetStep = 0.01f

    private lateinit var shader: Shader
    override fun prepare(context: Context) {
        shader = Shader(vertexShaderSource, fragmentShaderSources[effectType]!!)
        shader.prepareShaders()
        MyGLUtils.checkGlError("compile shader")

        // prepare vbo data
        val vertexBuffer = ByteBuffer
            .allocateDirect(vertices.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }

        // generate vao, vbo and ebo
        GLES30.glGenVertexArrays(1, vaos)
        GLES30.glGenBuffers(1, vbos)
        GLES30.glGenBuffers(1, ebo)
        MyGLUtils.checkGlError("gen vertex array and buffer")

        // bind and set vao
        GLES30.glBindVertexArray(vaos[0])

        // set vbo data
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbos[0])
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            Float.SIZE_BYTES * vertices.size,
            vertexBuffer,
            GLES30.GL_STATIC_DRAW
        )
        MyGLUtils.checkGlError("glBufferData")

        // set ebo data
        val indexBuffer = ByteBuffer
            .allocateDirect(indices.size * Int.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .apply {
                put(indices)
                position(0)
            }
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        GLES30.glBufferData(
            GLES30.GL_ELEMENT_ARRAY_BUFFER,
            Int.SIZE_BYTES * indices.size,
            indexBuffer,
            GLES30.GL_STATIC_DRAW
        )
        MyGLUtils.checkGlError("glBufferData for indices")

        // set vao attribute
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 5 * Float.SIZE_BYTES, 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(
            1,
            2,
            GLES30.GL_FLOAT,
            false,
            5 * Float.SIZE_BYTES,
            3 * Float.SIZE_BYTES
        )
        GLES30.glEnableVertexAttribArray(1)

        // unbind vao
        GLES30.glBindVertexArray(0)

        // prepare texture
        // generate texture id
        GLES30.glGenTextures(texIds.capacity(), texIds)
        MyGLUtils.checkGlError("glGenTextures")
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texIds[0])
        MyGLUtils.checkGlError("glBindTexture")

        // set filtering
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_NEAREST
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        MyGLUtils.checkGlError("glTexParameteri")

        // set texture image data
        val options = BitmapFactory.Options()
        options.inScaled = false   // No pre-scaling
        var bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.lye, options)

        // Flip the bitmap vertically
        val matrix = android.graphics.Matrix()
        matrix.preScale(1.0f, -1.0f)
        bitmap = android.graphics.Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            false
        )

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        MyGLUtils.checkGlError("texImage2D")
        bitmap.recycle()

        // unbind texture
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        // use share program
        shader.use()
        // set uniform values
        shader.setInt("texture0", 0)

    }

    override fun draw() {
        // update offset
        offset += offsetStep
        if(offset >= 1.0f){
            offset -= 1.0f
        }
        shader.setFloat("offset", offset)

        val resolution = floatArrayOf(screenWidth.toFloat(), screenHeight.toFloat())
        shader.setVec2("resolution", resolution)

        GLES30.glViewport(0, 0, screenWidth, screenHeight)

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glBindVertexArray(vaos[0])
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texIds[0])

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.size, GLES30.GL_UNSIGNED_INT, 0)

        // unbind vao
        GLES30.glBindVertexArray(0)
    }
}