package com.SensorStreamer.Component.Net.RLink;

import com.SensorStreamer.Component.Net.Link.Link;
import com.SensorStreamer.Component.Net.Net;
import com.google.gson.Gson;

/**
 * RLink 适用任何 Link 的抽象复用套件
 * @author chen
 * @version 1.0
 */

public abstract class RLink implements Net {
    /**
     * RLink 协议数据单元
     * 使用 RLink 必须基于 PDU 构造数据
     * */
    public static class RLink_PDU {
        public String reuseName;
        public String data;

        /**
         * @param reuseName 复用名称
         * @param data 数据
         * */
        public RLink_PDU(String reuseName, String data) {
            this.reuseName = reuseName;
            this.data = data;
        }
    }

    public final static int INT_NULL = 0;
//    格式转换
    protected final Gson gson;
//    Link
    protected Link link;
//    状态标志
    protected boolean launchFlag;
//    缓冲大小
    protected int bufSize;

    public RLink() {
        this.gson = new Gson();

        this.link = null;
        this.launchFlag = false;
        this.bufSize = RLink.INT_NULL;
    }

    /**
     * 注册变量并检查有效性
     * @param link 任意类型 Link
     * @return 是否注册成功
     * @throws Exception 注册失败原因
     * */
    public abstract boolean launch(Link link) throws Exception;

    /**
     * 将传入的 Link 重启
     * @return 是否重启成功
     * */
    public abstract boolean reLaunchLink(int timeLimit);

    /**
     * 注销所有变量
     * @return 注销是否成功
     * */
    public abstract boolean off();

    /**
     * 结构化发信
     * @throws Exception 发信错误
     * */
    @Override
    public abstract void structSend(String msg, String... reuseName) throws Exception;

    /**
     * 接收复用名称队列的数据
     * @param bufSize 缓冲大小
     * @param timeLimit 时间限制
     * @param reuseName 复用名称
     * @return 数据
     * @throws Exception 接收异常
     */
    @Override
    public abstract String structRece(int bufSize, int timeLimit, String... reuseName) throws Exception;

    /**
     * @return 是否可以启动
     * */
    @Override
    public synchronized boolean canLaunch() {
        return !launchFlag;
    }

    /**
     * @return 是否可以关闭
     * */
    @Override
    public synchronized boolean canOff() {
        return launchFlag;
    }

    /**
     * @return 是否可以发送
     * */
    @Override
    public synchronized boolean canSend() {
        return (launchFlag && this.link != null);
    }

    /**
     * @return 是否可以接收
     * */
    @Override
    public synchronized boolean canRece() {
        return (launchFlag && this.link != null);
    }
}
