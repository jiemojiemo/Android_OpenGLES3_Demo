package com.xinging.opengltest

class VertexShaderSource {
    companion object {
        val vertexShaderSource =
            """
            #version 300 es
            layout (location = 0) in vec3 aPos;
            void main()
            {
                gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
            }
            """.trimIndent()
    }
}