package com.xinging.opengltest

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyOpenGLSurfaceRender : GLSurfaceView.Renderer {
    private val tri = Triangle()

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        //设置背景颜色
        GLES30.glClearColor(1.0f, 0.5f, 0.5f, 0.5f)
        tri.prepareOpenGL()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        GLES30.glUseProgram(tri.shaderProgram)
        GLES30.glBindVertexArray(tri.vaos[0])

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

        //7. 解绑VAO
        GLES30.glBindVertexArray(0)
    }
}