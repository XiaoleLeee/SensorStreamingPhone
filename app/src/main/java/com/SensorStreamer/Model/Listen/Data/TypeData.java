package com.SensorStreamer.Model.Listen.Data;

/**
 * 通用数据结构
 * @author chen
 * @version 1.0
 * */

public class TypeData {
    public String type;
    public long unixTimestamp;

    /**
     * @param type 数据类型
     * @param unixTimestamp 时间戳
     * */
    public TypeData(String type, long unixTimestamp) {
        this.type = type;
        this.unixTimestamp = unixTimestamp;
    }
}
