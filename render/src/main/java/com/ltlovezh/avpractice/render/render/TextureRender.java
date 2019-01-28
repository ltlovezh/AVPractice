package com.ltlovezh.avpractice.render.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Environment;
import android.text.TextPaint;
import android.util.Log;
import com.ltlovezh.avpractice.render.filter.BaseFilter;
import com.ltlovezh.avpractice.render.filter.CoordinateFilter;
import com.ltlovezh.avpractice.render.utils.Buffer;
import com.ltlovezh.avpractice.render.utils.EasyGlUtils;
import com.ltlovezh.avpractice.render.utils.GlUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class TextureRender {
    private static final String TAG = "TextureRender";

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aColor;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "varying vec4 vColor;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = aTextureCoord;\n" +
                    "    vColor = aColor;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "varying vec4 vColor;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";


    private float VERTEX_COORDINATE_TRIANGLE[] = {
            0.5f, 0.5f,
            -0.5f, 0.5f,
            0.0f, -0.5f,
    };

    private float COLOR[] = {
            1.0f, 0.0f, 0.0f, 1.0f, // 红色
            0.0f, 1.0f, 0.0f, 1.0f, // 绿色
            0.0f, 0.0f, 1.0f, 1.0f, // 蓝色
            0.0f, 0.0f, 0.0f, 1.0f, // 黑色
    };


//    private float GL_TRIANGLES_POSITION[] = {
//            -0.75f, 0.5f, // 左上角
//            -0.75f, 0f,   // 左下角
//            -0.25f, 0f,   // 右下角
//
//            0.25f, 0.5f, // 左上角
//            0.75f, 0f,   // 右下角
//            0.75f, 0.5f  // 右上角
//    };
//    private float GL_TRIANGLES_COLOR[] = {
//            1.0f, 0.0f, 0.0f, 1.0f, // 红色
//            0.0f, 1.0f, 0.0f, 1.0f, // 绿色
//            0.0f, 0.0f, 1.0f, 1.0f, // 蓝色
//
//            1.0f, 0.0f, 0.0f, 1.0f, // 红色
//            0.0f, 1.0f, 0.0f, 1.0f, // 绿色
//            0.0f, 0.0f, 1.0f, 1.0f, // 蓝色
//    };


    private float GL_TRIANGLES_POSITION[] = {
            0.0f, 0.866f, // 顶部
            -0.5f, 0f,   // 左下角
            0.5f, 0f,   // 右下角
    };
    private float GL_TRIANGLES_COLOR[] = {
            1.0f, 0.0f, 0.0f, 1.0f, // 红色
            0.0f, 1.0f, 0.0f, 1.0f, // 绿色
            0.0f, 0.0f, 1.0f, 1.0f, // 蓝色
    };


    // 坐标轴的两条线
    private float INE_POSITION[] = {
            -1f, 0f,
            1f, 0f,

            0f, -1f,
            0f, 1f,
    };
    private float LINE_COLOR[] = {
            1.0f, 0.0f, 0.0f, 1.0f, // 红色
            0.0f, 1.0f, 0.0f, 1.0f, // 绿色

            1.0f, 0.0f, 0.0f, 1.0f, // 红色
            0.0f, 1.0f, 0.0f, 1.0f, // 绿色
    };


//private float VERTEX_COORDINATE_TRIANGLE[] = {
//        0.5f, 0.5f,
//        -0.5f, 0.5f,
//        0.0f, -0.5f,
//};


    //顶点坐标
    private float VERTEX_COORDINATE[] = {
            -0.5f, -0.5f,
            -0.5f, 0.5f,
            0.5f, -0.5f,
            0.5f, 0.5f,
    };

    // 纹理坐标，该纹理坐标是绘制到FBO使用的，若是绘制到Android屏幕，则需要使用FinalFilter中的纹理坐标
    // 原因是Android屏幕的左上角是坐标原点，而绘制到FBO时，左下角是坐标原点，Y坐标正好是反过来的。

    // 纹理坐标原点在左上角
//    private float TEXTURE_COORDINATE[] = {
//            0.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//    };

    // 纹理坐标原点在左下角
    private float TEXTURE_COORDINATE[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };


    /**
     * FBO
     */
    private int[] fbo = new int[1];
    private int[] fboTexture = new int[1];

    /**
     * 顶点坐标Buffer
     */
    protected FloatBuffer mVerBuffer;

    protected FloatBuffer mColorBuffer;

    /**
     * 纹理坐标Buffer
     */
    protected FloatBuffer mTexBuffer;


    private FloatBuffer coordinatePositionBuffer;
    private FloatBuffer coordinateColorBuffer;

    private int mProgramHandle;


    // attribute
    private int attributePosition;
    private int attributeColor;
    private int attributeTextureCoord;

    // uniform
    private int uniformMVPMatrix;
    private int uniformTexture;

    private float[] mMVPMatrix = new float[16];

    private int mTextureId;

    public Bitmap bitmap;

    private Context mContext;

    private Paint paint;

    private int viewWidth;
    private int viewHeight;

    private BaseFilter finalFilter;
    private CoordinateFilter coordinateFilter;

    public TextureRender(Context context) {
        mContext = context;
        initCoordinateBuffer();
        initCoordinate();

        Matrix.setIdentityM(mMVPMatrix, 0);

        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(dip2px(20));
        paint.setColor(Color.WHITE);
        createBitmap();

        finalFilter = new BaseFilter(context.getResources());
        coordinateFilter = new CoordinateFilter((context.getResources()));

        Log.d(TAG, "0.75 : " + Math.sqrt(0.75));
    }

    private void initCoordinateBuffer() {
        ByteBuffer a = ByteBuffer.allocateDirect(INE_POSITION.length * 4);
        a.order(ByteOrder.nativeOrder());
        coordinatePositionBuffer = a.asFloatBuffer();
        coordinatePositionBuffer.put(INE_POSITION);
        coordinatePositionBuffer.position(0);

        ByteBuffer b = ByteBuffer.allocateDirect(LINE_COLOR.length * 4);
        b.order(ByteOrder.nativeOrder());
        coordinateColorBuffer = b.asFloatBuffer();
        coordinateColorBuffer.put(LINE_COLOR);
        coordinateColorBuffer.position(0);
    }

    private void initCoordinate() {
        ByteBuffer a = ByteBuffer.allocateDirect(VERTEX_COORDINATE.length * 4);
        a.order(ByteOrder.nativeOrder());
        mVerBuffer = a.asFloatBuffer();
        mVerBuffer.put(VERTEX_COORDINATE);
        mVerBuffer.position(0);

        ByteBuffer c = ByteBuffer.allocateDirect(GL_TRIANGLES_COLOR.length * 4);
        c.order(ByteOrder.nativeOrder());
        mColorBuffer = c.asFloatBuffer();
        mColorBuffer.put(GL_TRIANGLES_COLOR);
        mColorBuffer.position(0);

        ByteBuffer b = ByteBuffer.allocateDirect(TEXTURE_COORDINATE.length * 4);
        b.order(ByteOrder.nativeOrder());
        mTexBuffer = b.asFloatBuffer();
        mTexBuffer.put(TEXTURE_COORDINATE);
        mTexBuffer.position(0);
    }

    private void createBitmap() {
        bitmap = Bitmap.createBitmap(dip2px(200), dip2px(200), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.RED);
        canvas.drawLine(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
        canvas.drawText("中国", bitmap.getWidth() / 2, bitmap.getHeight() / 2, paint);
    }

    public void surfaceCreated() {
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D);
        // attribute
        attributePosition = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        GlUtil.checkLocation(attributePosition, "aPosition");

//        attributeColor = GLES20.glGetAttribLocation(mProgramHandle, "aColor");
//        GlUtil.checkLocation(attributeColor, "aColor");

        // todo 如果aTextureCoord没有使用，这居然找不到！！！难道被优化掉了？？？
        attributeTextureCoord = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        GlUtil.checkLocation(attributeTextureCoord, "aTextureCoord");

        uniformMVPMatrix = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        GlUtil.checkLocation(uniformMVPMatrix, "uMVPMatrix");

        uniformTexture = GLES20.glGetUniformLocation(mProgramHandle, "sTexture");
        GlUtil.checkLocation(uniformTexture, "sTexture");

        mTextureId = createTexture();
        if (bitmap != null && !bitmap.isRecycled()) {
            // 上传纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            GlUtil.checkGlError("GLUtils.texImage2D");
        }

        // 创建FBO
        GLES20.glGenFramebuffers(1, fbo, 0);

        finalFilter.create();
        coordinateFilter.create();
    }

    // M V P 矩阵是对顶点坐标的变换
    public void surfaceSizeChanged(int width, int height) {
        viewWidth = width;
        viewHeight = height;

//        MatrixUtils.getShowMatrix(mMVPMatrix, bitmap.getWidth(), bitmap.getHeight(), viewWidth, viewHeight);

        int length = Math.min(width, height);
        float[] view = new float[16];
        Matrix.setLookAtM(view, 0, 0, 0, 3, 0, 0, 0, 0, 1, 0);
        float[] projection = new float[16];
//        Matrix.orthoM(projection, 0, -width * 1.0f / height, width * 1.0f / height, -1, 1, 0, 4);
//        Matrix.orthoM(projection, 0, -0.5f, 0.5f, 0, 0.866f, 0, 4);
        Matrix.orthoM(projection, 0, -1, 1, -height * 1.0f / width, height * 1.0f / width, 0, 4);

//        Matrix.scaleM(mMVPMatrix, 0, 1.0f, width * 1.0f / height, 1.0f);

        float[] mvp = new float[16];
//        Matrix.multiplyMM(mMVPMatrix, 0, projection, 0, view, 0);


        // 创建FBO纹理
        GLES20.glDeleteTextures(1, fboTexture, 0);
        EasyGlUtils.genTexturesWithParameter(1, fboTexture, 0, GLES20.GL_RGBA, viewWidth, viewHeight);

        GLES20.glViewport(0, 0, width, height);
    }

    boolean read = false;

    public void drawFrame() {
        EasyGlUtils.bindFrameTexture(fbo[0], fboTexture[0]);
//
        GLES20.glViewport(0, 0, viewWidth, viewHeight);

                onClear();
//        coordinateFilter.draw();



        onUseProgram();

        onSetExpandData();

        onBindTexture();

        onDraw();

        if (!read) {
            Buffer.INSTANCE.saveFrame(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "fbo.jpg", viewWidth, viewHeight);
        }


        EasyGlUtils.unBindFrameBuffer();


        GLES20.glViewport(0, 0, viewWidth, viewHeight);
        finalFilter.setTextureId(fboTexture[0]);
        finalFilter.draw();

        if (!read) {
            read = true;
            Buffer.INSTANCE.saveFrame(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "surface.jpg", viewWidth, viewHeight);
        }

    }

    /**
     * 清除画布
     */
    protected void onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * 设置其他扩展数据
     */
    protected void onSetExpandData() {
        GLES20.glUniformMatrix4fv(uniformMVPMatrix, 1, false, mMVPMatrix, 0);
    }

    protected void onUseProgram() {
        GLES20.glUseProgram(mProgramHandle);
    }

    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        // 绑定纹理单元
//        GLES20.glUniform1i(mHTexture, 0);
//        GlUtil.checkGlError("GLES20.glUniform1i");
    }

    protected void onDraw() {
//        drawCoordinate();

        // 顶点
        GLES20.glEnableVertexAttribArray(attributePosition);
        GLES20.glVertexAttribPointer(attributePosition, 2, GLES20.GL_FLOAT, false, 0, mVerBuffer);

        // 颜色
//        GLES20.glEnableVertexAttribArray(attributeColor);
//        GLES20.glVertexAttribPointer(attributeColor, 4, GLES20.GL_FLOAT, false, 0, mColorBuffer);

        GLES20.glEnableVertexAttribArray(attributeTextureCoord);
        GLES20.glVertexAttribPointer(attributeTextureCoord, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(attributePosition);
//        GLES20.glDisableVertexAttribArray(attributeColor);
        GLES20.glDisableVertexAttribArray(attributeTextureCoord);
        GlUtil.checkGlError("onDraw");
    }


    /**
     * 绘制坐标轴
     */
    private void drawCoordinate() {
        GLES20.glEnableVertexAttribArray(attributePosition);
        GLES20.glVertexAttribPointer(attributePosition, 2, GLES20.GL_FLOAT, false, 0, coordinatePositionBuffer);
        GLES20.glEnableVertexAttribArray(attributeColor);
        GLES20.glVertexAttribPointer(attributeColor, 4, GLES20.GL_FLOAT, false, 0, coordinateColorBuffer);
        GLES20.glLineWidth(10);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 4);
        GLES20.glDisableVertexAttribArray(attributePosition);
        GLES20.glDisableVertexAttribArray(attributeColor);
    }

    private int createTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GlUtil.checkGlError("glGenTextures");

        int texId = textures[0];
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GlUtil.checkGlError("glBindTexture " + texId);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GlUtil.checkGlError("glTexParameter");

        return texId;
    }

    private int dip2px(float dipValue) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
