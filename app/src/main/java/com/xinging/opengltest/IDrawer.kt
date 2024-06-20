package com.xinging.opengltest

import android.content.Context

interface IDrawer {
    fun prepare(context: Context)
    fun draw()
}