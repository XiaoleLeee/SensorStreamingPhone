package com.SensorStreamer.Component.Switch;

import com.SensorStreamer.Component.Net.Net;
import com.google.gson.Gson;

/**
 * Switch 抽象类
 * @author chen
 * @version 1.0
 * */

public abstract class Switch {
    /**
     * 通用回调函数接口
     * */
    public interface SwitchCallback {}

    protected final Gson gson;
    protected boolean launchFlag, startFlag;

    /**
     * 设置启动和开始标志
     * 0 0 launch
     * 1 0 off, start
     * 1 1 stop
     * 0 1 error
     * */
    public Switch() {
        gson = new Gson();
        this.launchFlag = this.startFlag = false;
    }

    /**
     * 注册组件
     * @param link 连接类
     * @return 是否注册成功
     * */
    public abstract boolean launch(Net link, SwitchCallback callback);

    /**
     * 注销组件
     * @return 是否注销成功
     * */
    public abstract boolean off();

    /**
     * 监听并使用回调函数
     * @param bufSize 缓存大小
     * */
    public abstract void startListen(int bufSize);

    /**
     * 停止监听
     * */
    public abstract void stopListen();

    /**
     * 是否能注册
     * */
    public boolean canLaunch() {
        return !(this.launchFlag || this.startFlag);
    }

    /**
     * 是否能注销
     * */
    public boolean canOff() {
        return !(!this.launchFlag || this.startFlag);
    }

    /**
     * 是否能开始
     * */
    public boolean canStartListen() {
        return this.canOff();
    }

    /**
     * 是否能结束
     * */
    public boolean canStopListen() {
        return !(!this.launchFlag || !this.startFlag);
    }
}
