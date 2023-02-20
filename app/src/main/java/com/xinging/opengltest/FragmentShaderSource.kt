package com.xinging.opengltest

class FragmentShaderSource {
    companion object {
        val fragmentShaderSource = """
            #version 300 es
            precision mediump float;
            out vec4 FragColor;
            void main()
            {
                FragColor = vec4(0.0f, 0.5f, 0.2f, 1.0f);
            }
            """.trimIndent()
    }
}