package com.ltlovezh.avpractice

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ltlovezh.avpractice.render.NativeLayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sample_text.text = NativeLayer.stringFromNative()
    }


}
