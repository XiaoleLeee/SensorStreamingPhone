package com.SensorStreamer.Model.Listen.Control;

/**
 * 传感器控制数据结构
 * @author chen
 * @version 1.0
 * */

public class SensorControl extends TypeControl {
    public static final String TYPE = "SensorControl";
    public int[] sensors;

    /**
     * @param sampling 采样率
     * @param sensors 传感器
     * */
    public SensorControl(int sampling, int[] sensors) {
        super(SensorControl.TYPE, sampling);
        this.sensors = sensors;
    }
}
