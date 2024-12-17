package com.SensorStreamer.Component.Net.Link.RTCPLink.RTCPLinkExpand;

import android.util.Log;

import com.SensorStreamer.Component.Net.Link.Link;
import com.SensorStreamer.Component.Net.Link.RTCPLink.RTCPLink;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RTCP 心跳拓展类
 * @author chen
 * @version 1.0
 * @deprecated 弃用，使用 RLinkExpand 中的 HeartBeat
 */

public class HeartBeatForRTCP {
    /**
     * HeartBeat 回调函数类接口
     * */
    public interface HeartBeatCallback {
        /**
         * 用于处理连接失效时的回调函数
         */
        void onLinkLose();
    }

    public final static String LOG_TAG = "HeartBeatForRTCP";
    private final static String HEART_BEAT = "heartbeat";
//    同步锁
    private final Object RTTLock;
//    回调函数
    private final HeartBeatCallback callback;
//    连接
    private final RTCPLink link;
//    往返时间，毫秒单位
    private double RTT;
//    心跳服务
    private ScheduledExecutorService heartbeatService;

    public HeartBeatForRTCP(RTCPLink link, HeartBeatCallback callback) {
        this.RTTLock = new Object();

        this.link = link;
        this.callback = callback;
        this.RTT = RTCPLink.INTNULL;
    }

    /**
     * 心跳
     * @param timeLimit 心跳最长响应时间，毫秒单位
     * @param numLimit 连续超时的最大次数
     * @param interTime 心跳间隔，毫秒单位
     */
    public synchronized void startHeartbeat(int timeLimit, int numLimit, int interTime) {
//        1
        if (!link.canRece() || this.heartbeatService != null)
            return;

        if (timeLimit <= RTCPLink.INTNULL || numLimit < RTCPLink.INTNULL || interTime <= RTCPLink.INTNULL)
            return;

//        连续超时次数
        AtomicInteger timeOutNum = new AtomicInteger();

        this.heartbeatService = Executors.newSingleThreadScheduledExecutor();
        this.heartbeatService.scheduleWithFixedDelay(
                () -> {
                    try {
//                   发送心跳信息
                        link.structSend(HeartBeatForRTCP.HEART_BEAT, HeartBeatForRTCP.LOG_TAG);
                        long startTime = System.currentTimeMillis();
                        String msg = link.structRece(1024, timeLimit, HeartBeatForRTCP.LOG_TAG);
                        long stopTime = System.currentTimeMillis();

//                    心跳超时
                        if (!HeartBeatForRTCP.HEART_BEAT.equals(msg)) {
//                            没有超出次数
                            if (timeOutNum.addAndGet(1) <= numLimit) {
                                Log.e(HeartBeatForRTCP.LOG_TAG, "startHeartbeat: Timeout");
                                return;
                            }
                            Log.i(HeartBeatForRTCP.LOG_TAG, "startHeartbeat: " + numLimit + " consecutive timeouts, relaunch now");
                            boolean result = link.reLaunch(timeLimit);
                            if (result) {
                                timeOutNum.set(0);
                                return;
                            }
//                            重启失败
                            Log.e(HeartBeatForRTCP.LOG_TAG, "startHeartbeat: Relaunch fail");
                            this.stopHeartbeat();
//                                执行回调函数
                            new Thread(this.callback::onLinkLose).start();
                            return;
                        }
//                    心跳正常
                        timeOutNum.set(0);
                        synchronized (this.RTTLock) {
                            if (this.RTT == RTCPLink.INTNULL)
                                this.RTT = stopTime - startTime;
                            else this.RTT = this.RTT * 0.5 + (stopTime - startTime) * 0.5;
                        }
                        Log.i(HeartBeatForRTCP.LOG_TAG, "Heartbeat: RTT = " + this.RTT);
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

        this.RTT = RTCPLink.INTNULL;
    }

    /**
     * 获取 RTT
     * */
    public double getRTT() {
        if (this.heartbeatService == null)
            return Link.INTNULL;

        synchronized (this.RTTLock) {
            return this.RTT;
        }
    }
}
