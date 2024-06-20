package com.xinging.opengltest

import android.opengl.GLES30
import android.util.Log

class Shader(private val vertexShaderSource: String, private val fragmentShaderSource: String) {
    private val TAG = "Shader"
    var id : Int = 0

    fun prepareShaders(): Int{

        // compile vertex shader
        val vertexShader = createAndCompileShader(GLES30.GL_VERTEX_SHADER, vertexShaderSource)
        if(vertexShader == -1){
            return -1
        }

        // compile fragment shader
        val fragmentShader = createAndCompileShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderSource)
        if(fragmentShader == -1){
            return -1
        }

        id = createShaderProgram(vertexShader, fragmentShader)
        if(id == -1){
            return -1
        }

        return 0
    }

    fun use(){
        GLES30.glUseProgram(id)
    }

    fun setInt(name: String, value: Int){
        val location = GLES30.glGetUniformLocation(id, name)
        GLES30.glUniform1i(location, value)
    }

    private fun createAndCompileShader(type: Int, source: String): Int{
        val success: IntArray = intArrayOf(0)

        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, success, 0)
        if (success[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            Log.e(TAG, "compile vertex source failed.$log")
            GLES30.glDeleteShader(shader)
            throw RuntimeException("compile shader failed")
        }
        return shader
    }

    private fun createShaderProgram(vertexShader: Int, fragmentShader: Int): Int{
        val success: IntArray = intArrayOf(0)

        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)

        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, success, 0)
        if (success[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(program)
            Log.e(TAG, "link shader program failed. $log")
            GLES30.glDeleteProgram(program)
            throw RuntimeException("compile shader failed")
        }

        GLES30.glDeleteShader(vertexShader)
        GLES30.glDeleteShader(fragmentShader)

        return program
    }
}