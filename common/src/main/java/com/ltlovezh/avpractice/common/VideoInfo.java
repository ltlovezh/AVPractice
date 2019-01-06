package com.ltlovezh.avpractice.common;

import java.io.Serializable;

/**
 * Created by leon on 2019/1/6 0029.
 * 视频的信息
 */
public class VideoInfo implements Serializable {
    public String path;//路径
    public int rotation;//旋转角度
    public int width;//宽
    public int height;//高
    public int bitRate;//比特率
    public int frameRate;//帧率
    public int frameInterval;//关键帧间隔
    public int duration;//时长

    public int expWidth;//期望宽度
    public int expHeight;//期望高度
    public int cutPoint;//剪切的开始点
    public int cutDuration;//剪切的时长


    public int getShowWidth() {
        if (rotation == 0 || rotation == 180) {
            return width;
        } else {
            return height;
        }
    }

    public int getShowHeight() {
        if (rotation == 0 || rotation == 180) {
            return height;
        } else {
            return width;
        }
    }

    public boolean checkParams() {
        return width > 0 && height > 0 && duration > 0;
    }
}
