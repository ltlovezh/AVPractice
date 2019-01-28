package com.ltlovezh.avpractice.common

import android.util.Log

object Logger {
    var level = Log.VERBOSE


    fun v(tag: String, msg: String?) {
        if (msg == null) {
            return
        }
        if (level <= Log.VERBOSE)
            Log.v(tag, msg)
    }

    fun d(tag: String, msg: String?) {
        if (msg == null) {
            return
        }
        if (level <= Log.DEBUG)
            Log.d(tag, msg)
    }

    fun i(tag: String, msg: String?) {
        if (msg == null) {
            return
        }
        if (level <= Log.INFO)
            Log.i(tag, msg)
    }

    fun w(tag: String, msg: String?) {
        if (msg == null) {
            return
        }
        if (level <= Log.WARN)
            Log.w(tag, msg)
    }

    fun e(tag: String, msg: String?) {
        if (msg == null) {
            return
        }
        if (level <= Log.ERROR)
            Log.e(tag, msg)
    }
}