package com.SensorStreamer.Component.Time;

/**
 * ReferenceTime 工厂
 * @author chen
 * @version 1.0
 */
public class ReferenceTimeF extends TimeF {
    @Override
    public Time create() {
        return new ReferenceTime();
    }
}
