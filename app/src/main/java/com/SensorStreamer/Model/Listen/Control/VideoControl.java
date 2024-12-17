package com.SensorStreamer.Model.Listen.Control;

/**
 * 视频控制数据结构
 * @author chen
 * @version 1.0
 * */

public class VideoControl extends TypeControl {
    public static final String TYPE = "VIDEO";

    /**
     * @param sampling 采样率
     */
    public VideoControl(int sampling) {
        super(VideoControl.TYPE, sampling);
    }
}
