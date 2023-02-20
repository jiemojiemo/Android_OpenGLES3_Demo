package com.xinging.opengltest

import android.content.Context
import android.opengl.GLSurfaceView

class MyOpenGLSurfaceView(context: Context?) : GLSurfaceView(context) {
    var render = MyOpenGLSurfaceRender()

    init {
        setEGLContextClientVersion(3)
        setRenderer(render)
    }
}