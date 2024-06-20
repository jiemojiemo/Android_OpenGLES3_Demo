package com.xinging.opengltest

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MyOpenGLSurfaceView(context: Context?, attrs :AttributeSet?) : GLSurfaceView(context, attrs) {
    constructor(context: Context?) : this(context, null)

    var render = MyOpenGLSurfaceRender(TriangleDrawer(), context!!)

    init {
        setEGLContextClientVersion(3)
        setRenderer(render)
    }
}