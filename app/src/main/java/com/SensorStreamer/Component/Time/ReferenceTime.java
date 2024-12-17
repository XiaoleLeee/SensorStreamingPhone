package com.SensorStreamer.Component.Time;

/**
 * ReferenceTime 获取基于基准时间的当前时间
 * @author chen
 * @version 1.0
 */
public class ReferenceTime extends Time {

    public ReferenceTime() {
        super();
    }

    /**
     * 设置基准时间，本地开始时间和偏移量
     * */
    @Override
    public boolean setBase(long baseTime, long localTime, long offsetTime) {
        if (baseTime < 0 || localTime < 0)
            return false;
        this.baseTime = baseTime;
        this.localTime = localTime;
        this.offsetTime = offsetTime;
        return true;
    }

    /**
     * 获取基于基准时间的当前时间
     * */
    @Override
    public long getTime() {
        return this.baseTime + (System.currentTimeMillis() - this.localTime) + this.offsetTime;
    }
}
