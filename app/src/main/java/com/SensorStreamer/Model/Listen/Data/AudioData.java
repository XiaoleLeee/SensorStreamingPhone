package com.SensorStreamer.Model.Listen.Data;

/**
 * 音频数据结构
 * @author chen
 * @version 1.0
 * */

public class AudioData extends TypeData {
    public static final String TYPE = "AUDIO";
    public byte[] values;

    /**
     * @param unixTimestamp 时间戳
     * @param values 音频数据
     * */
    public AudioData(long unixTimestamp, byte[] values) {
        super(AudioData.TYPE, unixTimestamp);
        this.values = values;
    }
}