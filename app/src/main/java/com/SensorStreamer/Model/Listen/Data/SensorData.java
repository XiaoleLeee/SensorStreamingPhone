package com.SensorStreamer.Model.Listen.Data;

/**
 * Sensor 数据结构
 * @author chen
 * @version 1.0
 * */

public class SensorData extends TypeData {
    public static final String TYPE = "SENSOR";
    public long sensorTimestamp;
    public float[] values;

    /**
     * @param sensorType 传感器类型
     * @param unixTimestamp 系统时间戳
     * @param sensorTimestamp 传感器时间戳
     * @param values 传感器数据
     * */
    public SensorData(String sensorType, long unixTimestamp, long sensorTimestamp, float[] values) {
        super(sensorType, unixTimestamp);
        this.sensorTimestamp = sensorTimestamp;
        this.values = values;
    }
}
