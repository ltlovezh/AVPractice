package com.ltlovezh.avpractice.activity;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import com.ltlovezh.avpractice.R;
import com.ltlovezh.avpractice.render.render.TextureRender;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY;

public class GLSurfaceViewActivity extends Activity implements GLSurfaceView.Renderer, SurfaceHolder.Callback {
    private static final String TAG = "GLSurfaceViewActivity";

    private GLSurfaceView mGLSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private int count;

    private TextureRender textureRender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gl_surfaceview_layout);
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mSurfaceHolder = mGLSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.RGBA_8888);
        mSurfaceHolder.addCallback(this);

        textureRender = new TextureRender(this);
//        textureRender.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.iv_lyrics_quote);

        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(RENDERMODE_CONTINUOUSLY);
        // todo 这里设置的背景居然覆盖了OpenGL绘制的内容
//        mGLSurfaceView.setBackgroundColor(Color.YELLOW);
    }


    /**
     * GLSurfaceView.Renderer相关接口
     */

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "GLSurfaceView.Renderer onSurfaceCreated");
        textureRender.surfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "GLSurfaceView.Renderer onSurfaceChanged, width : " + width + ", height : " + height);
        textureRender.surfaceSizeChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        count++;
        if (count > 300) {
            count = 0;
            Log.i(TAG, "GLSurfaceView.Renderer onDrawFrame");
        }
        textureRender.drawFrame();
    }


    /**
     * SurfaceHolder.Callback相关接口
     */


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "SurfaceHolder.Callback surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "SurfaceHolder.Callback surfaceChanged, width : " + width + ", height : " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "SurfaceHolder.Callback surfaceDestroyed");
    }
}
