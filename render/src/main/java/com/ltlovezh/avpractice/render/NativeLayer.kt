package com.ltlovezh.avpractice.render


class NativeLayer {

    companion object {
        init {
            System.loadLibrary("avpractice")
        }

        const val TAG = "NativeLayer"

        @JvmStatic
        external fun stringFromNative(): String
    }


}