package com.ltlovezh.avpractice.render.filter;

import android.content.res.Resources;

/**
 * 负责直接渲染到屏幕上
 */
public class FinalFilter extends BaseFilter {

    public FinalFilter(Resources res) {
        super(res);
    }

    @Override
    protected void initBuffer() {
        super.initBuffer();
        float[] coord = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };
        getMTexBuffer().clear();
        getMTexBuffer().put(coord);
        getMTexBuffer().position(0);
    }
}
