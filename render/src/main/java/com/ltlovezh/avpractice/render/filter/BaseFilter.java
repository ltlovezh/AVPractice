package com.ltlovezh.avpractice.render.filter;

import android.content.res.Resources;
import android.opengl.GLES20;


/**
 * Description:
 */
public class BaseFilter extends AFilter {

    public BaseFilter(Resources res) {
        super(res);
    }

    @Override
    protected void onCreate() {
        // 普通纹理
        createProgramByAssetsFile("shader/base_vertex.glsl", "shader/base_fragment.glsl");
    }

    /**
     * 背景默认为黑色
     */
    @Override
    protected void onClear() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void onSizeChanged(int width, int height) {
//        GLES20.glViewport(0, 0, width, height);
    }
}
