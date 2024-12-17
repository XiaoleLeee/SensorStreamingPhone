package com.SensorStreamer.Model.Listen.Data;

public class VideoData extends TypeData {
    public static final String TYPE = "VIDEO";
    public byte[] values;

    /**
     * @param unixTimestamp 时间戳
     * @param values 视频数据
     * */
    public VideoData(long unixTimestamp, byte[] values) {
        super(VideoData.TYPE, unixTimestamp);
        this.values = values;
    }
}