package com.SensorStreamer.Component.Time;

/**
 * Time 抽象类
 * @author chen
 * @version 1.0
 */
public abstract class Time {
    protected long baseTime;
    protected long localTime;
    protected long offsetTime;

    public Time() {
        this.baseTime = 0;
        this.localTime = 0;
        this.offsetTime = 0;
    }

    /**
     * 初始化
     * @param baseTime 基准时间
     * @param localTime 当前本地时间
     * @param offsetTime 补偿量
     * */
    public abstract boolean setBase(long baseTime, long localTime, long offsetTime);

    /**
     * 获取时间
     * */
    public abstract long getTime();
}
