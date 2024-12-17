package com.SensorStreamer.Model.Listen.Control;

/**
 * 通用控制数据结构
 * @author chen
 * @version 1.0
 * */

public class TypeControl {
    public String type;
    public int sampling;

    /**
     * @param type 控制类型
     * @param sampling 采样率
     * */
    public TypeControl(String type, int sampling) {
        this.type = type;
        this.sampling = sampling;
    }
}
