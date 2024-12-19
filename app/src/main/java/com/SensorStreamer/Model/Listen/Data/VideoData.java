package com.SensorStreamer.Model.Listen.Data;

public class VideoData extends TypeData {
    public static final String TYPE = "VIDEO";
    public int width;
    public int height;
    public byte[] values;

    /**
     * @param unixTimestamp 时间戳
     * @param width 图像宽度
     * @param height 图像高度
     * @param values 视频数据
     * */
    public VideoData(long unixTimestamp, int width, int height, byte[] values) {
        super(VideoData.TYPE, unixTimestamp);
        this.width = width;
        this.height = height;
        this.values = values;
    }
}