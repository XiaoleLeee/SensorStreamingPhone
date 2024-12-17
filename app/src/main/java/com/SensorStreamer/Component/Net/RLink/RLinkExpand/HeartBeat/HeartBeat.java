package com.SensorStreamer.Component.Net.RLink.RLinkExpand.HeartBeat;

import android.util.Log;

import com.SensorStreamer.Component.Net.RLink.RLink;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RTCP 心跳拓展类
 * @author chen
 * @version 1.0
 */

public class HeartBeat {
    /**
     * HeartBeat 回调函数类接口
     * */
    public interface HeartBeatCallback {
        /**
         * 用于处理连接失效时的回调函数
         */
        void onLinkLose();
    }

    public final static String LOG_TAG = "HeartBeat";
    private final static String HEART_BEAT = "heartbeat";
    //    同步锁
    private final Object RTTLock;
    //    回调函数
    private final HeartBeatCallback callback;
    //    连接
    private final RLink rLink;
    //    往返时间，毫秒单位
    private double RTT;
    //    心跳服务
    private ScheduledExecutorService heartbeatService;

    /**
     * @param rLink 变量
     * @param callback 回调函数
     * */
    public HeartBeat(RLink rLink, HeartBeatCallback callback) {
        this.RTTLock = new Object();

        this.rLink = rLink;
        this.callback = callback;
        this.RTT = RLink.INT_NULL;
    }

    /**
     * 心跳
     * @param timeLimit 心跳最长响应时间，毫秒单位
     * @param numLimit 连续超时的最大次数
     * @param interTime 心跳间隔，毫秒单位
     */
    public synchronized void startHeartbeat(int timeLimit, int numLimit, int interTime) {
//        1
        if (!rLink.canRece() || this.heartbeatService != null)
            return;

        if (timeLimit <= RLink.INT_NULL || numLimit < RLink.INT_NULL || interTime <= RLink.INT_NULL)
            return;

//        连续超时次数
        AtomicInteger timeOutNum = new AtomicInteger();

        this.heartbeatService = Executors.newSingleThreadScheduledExecutor();
        this.heartbeatService.scheduleWithFixedDelay(
                () -> {
                    try {
//                   发送心跳信息
                        rLink.structSend(HeartBeat.HEART_BEAT, HeartBeat.LOG_TAG);
                        long startTime = System.currentTimeMillis();
                        String msg = rLink.structRece(1024, timeLimit, HeartBeat.LOG_TAG);
                        long stopTime = System.currentTimeMillis();

//                    心跳超时
                        if (!HeartBeat.HEART_BEAT.equals(msg)) {
//                            没有超出次数
                            if (timeOutNum.addAndGet(1) <= numLimit) {
                                Log.e(HeartBeat.LOG_TAG, "startHeartbeat: Timeout");
                                return;
                            }
                            Log.i(HeartBeat.LOG_TAG, "startHeartbeat: " + numLimit + " consecutive timeouts, relaunch now");
                            boolean result = rLink.reLaunchLink(timeLimit);
                            if (result) {
                                timeOutNum.set(0);
                                return;
                            }
//                            重启失败
                            Log.e(HeartBeat.LOG_TAG, "startHeartbeat: Relaunch fail");
                            this.stopHeartbeat();
//                                执行回调函数
                            new Thread(this.callback::onLinkLose).start();
                            return;
                        }
//                    心跳正常
                        timeOutNum.set(0);
                        synchronized (this.RTTLock) {
                            if (this.RTT == RLink.INT_NULL)
                                this.RTT = stopTime - startTime;
                            else this.RTT = this.RTT * 0.5 + (stopTime - startTime) * 0.5;
                        }
                        Log.i(HeartBeat.LOG_TAG, "Heartbeat: RTT = " + this.RTT);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "heartbeatService:Exception", e);
                    }
                }, 0, interTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止心跳
     */
    public synchronized void stopHeartbeat() {
        if (this.heartbeatService == null)
            return;

//        关闭心跳线程
        this.heartbeatService.shutdown();
        this.heartbeatService = null;

        this.RTT = RLink.INT_NULL;
    }

    /**
     * 获取 RTT
     * */
    public double getRTT() {
        if (this.heartbeatService == null)
            return RLink.INT_NULL;

        synchronized (this.RTTLock) {
            return this.RTT;
        }
    }
}

