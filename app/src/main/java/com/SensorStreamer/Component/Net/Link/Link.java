package com.SensorStreamer.Component.Net.Link;

import com.SensorStreamer.Component.Net.Net;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.nio.charset.Charset;

/**
 * Link 抽象类
 * @author chen
 * @version 1.2
 * */

public abstract class Link implements Net {
    public final static int INTNULL = 0;

    protected final static Gson gson = new Gson();
//    缓存最值
    protected final static int MAX_BUF_SIZE = 65536, MIN_BUF_SIZE = 128;

    protected boolean launchFlag;
    protected InetAddress address;
    protected int port;
//    自适应缓存大小
    protected int bufSize;
//    编码集
    protected Charset charset;

    /**
     * 设置启动标志
     * 0 launch
     * 1 off
     * */
    public Link() {
        this.launchFlag = false;
        this.bufSize = 1024;

    }

    /**
     * 注册组件
     * @param address 目的地址
     * @param port 目的端口
     * @param timeout 连接超时
     * @param charset 编码集
     * @return 是否注册成功
     * */
    public abstract boolean launch(InetAddress address, int port, int timeout, Charset charset) throws Exception;

    /**
     * 重新启动
     * @return 重启成功
     * @param timeLimit 重启时间限制
     * */
    public abstract boolean reLaunch(int timeLimit);

    /**
     * 注销组件
     * @return 是否注销成功
     * */
    public abstract boolean off();

    /**
     * 发信
     * @param msg 数据
     * */
    public abstract void send(String msg) throws Exception;

    /**
     * 发信 Hooks
     * @param msg 信息
     * @param param 特殊参数
     * */
    @Override
    public void structSend(String msg, String... param) throws Exception {
        this.send(msg);
    }

    /**
     * 收信
     * @param bufSize 缓存大小
     * @param timeLimit 接收时间限制，毫秒为单位
     * @return 数据
     * */
    public abstract String rece(int bufSize, int timeLimit) throws Exception;

    /**
     * 收信 Hooks
     * @param bufSize 缓存大小
     * @param timeLimit 接收时间限制，毫秒为单位
     * @param param 特殊参数
     * @return 数据
     * */
    @Override
    public String structRece(int bufSize, int timeLimit, String... param) throws Exception {
        return this.rece(bufSize, timeLimit);
    }

    /**
     * 自适应缓冲大小设置
     * @param packetSize 当前包大小
     * */
    protected abstract void adaptiveBufSize(int packetSize);

    /**
     * 能否注册
     * */
    @Override
    public boolean canLaunch() {
        return !this.launchFlag;
    }

    /**
     * 能否注销
     * */
    @Override
    public boolean canOff() {
        return this.launchFlag;
    }

    /**
     * 能否发送
     * */
    @Override
    public boolean canSend() {
        return this.canOff();
    }

    /**
     * 能否接收
     * */
    @Override
    public boolean canRece() {
        return this.canOff();
    }
}
