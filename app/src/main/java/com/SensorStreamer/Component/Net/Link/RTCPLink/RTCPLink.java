package com.SensorStreamer.Component.Net.Link.RTCPLink;

import android.util.Log;

import com.SensorStreamer.Component.Net.Link.Link;
import com.SensorStreamer.Component.Net.RLink.RLink;
import com.SensorStreamer.Component.Net.Link.TCPLink.TCPLink;
import com.SensorStreamer.Utils.TypeTranDeter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 可复用 TCP 的 Link
 * @author chen
 * @version 1.0
 */

public class RTCPLink extends TCPLink {
    public static class RTCP_PDU extends RLink.RLink_PDU {
        /**
         * @param reuseName 复用名称
         * @param data 数据
         * */
        public RTCP_PDU(String reuseName, String data) {
            super(reuseName, data);
        }
    }

    private final static String LOG_TAG = "RTCPLink";
//    队列映射
    private final Map<String, LinkedBlockingQueue<RTCP_PDU>> queueMap;
//    默认阻塞队列
    protected final LinkedBlockingQueue<String> defaultQueue;
//    信号
    protected int receNum;
//    socket 收信线程
    protected Thread socketThread;
//    同步锁
    protected final Object socketLock, receNumLock;

    public RTCPLink() {
        super();

        this.defaultQueue = new LinkedBlockingQueue<>();
        this.queueMap = new HashMap<>();

        this.socketLock = new Object();
        this.receNumLock = new Object();

        this.receNum = Link.INTNULL;
    }

    /**
     * 注册所有可变成员变量，设置目的地址
     * */
    @Override
    public synchronized boolean launch(InetAddress address, int port, int timeout, Charset charset) throws Exception {
//        0
        if (!this.canLaunch())
            return false;

        try {
            this.address = address;
            this.port = port;
            this.charset = charset;
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(address, port), timeout);

            this.socketSend = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), this.charset), true);
            this.socketRece = new BufferedReader(new InputStreamReader(socket.getInputStream(), this.charset));
//            允许接收
            this.startSocketRece();
        } catch (Exception e) {
            Log.e(RTCPLink.LOG_TAG, "launch:Exception", e);
            this.launchFlag = true;
            this.off();
            throw e;
        }

//        1
        this.launchFlag = true;
        return true;
    }

    /**
     * 重新启动连接
     * @param timeLimit 重启时间限制
     */
    public synchronized boolean reLaunch(int timeLimit) {
//        非启动状态不考虑重启
        if (!this.canRece())
            return false;

        try {
//            部分关闭，不清空已有的队列映射及内容
            if (this.socket != null && !this.socket.isClosed())
                this.socket.close();
            this.socket = null;

            if (this.socketThread != null)
                this.socketThread.interrupt();
            this.socketThread = null;

            this.receNum = Link.INTNULL;

//            0
            this.launchFlag = false;
            return this.launch(this.address, this.port, timeLimit, this.charset);
        } catch (Exception e) {
            Log.e(RTCPLink.LOG_TAG, "reLaunch:Exception", e);
            return false;
        }
    }

    /**
     * 注销所有可变成员变量
     */
    @Override
    public synchronized boolean off() {
//        1
        if (!this.canOff())
            return false;

        try {
//            清空队列信息
            this.defaultQueue.clear();
//            清空队列映射队列
            for (LinkedBlockingQueue<RTCP_PDU> queue : queueMap.values()) {
                queue.clear();
            }
//            清空队列映射
            queueMap.clear();

            if (this.socketThread != null)
                this.socketThread.interrupt();
            this.socketThread = null;

            if (this.socket != null && !this.socket.isClosed())
                this.socket.close();
            this.socket = null;

            this.receNum = Link.INTNULL;
        } catch (Exception e) {
            Log.e(RTCPLink.LOG_TAG, "off:Exception", e);
            return false;
        }

//        0
        this.launchFlag = false;
        return true;
    }

    /**
     * 添加复用及其对应队列
     * @param reuseName 复用名称
     * @return 是否添加成功
     * */
    public boolean addReuseName(String reuseName) {
        if (!this.canRece())
            return false;

        if (this.queueMap.containsKey(reuseName))
            return false;

        LinkedBlockingQueue<RTCP_PDU> queue = new LinkedBlockingQueue<>();
        this.queueMap.put(reuseName, queue);
        return true;
    }

    /**
     * 使用 socket 收信，并将信息存入对应队列
     * 若队列已满，则删除最先入队的元素
     */
    protected void startSocketRece() {
        this.socketThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (this.socketLock) {
                        while (true) {
//                            获取数字锁
                            synchronized (this.receNumLock) {
                                Log.i(RTCPLink.LOG_TAG, "receNum = " + this.receNum);
                                if (this.receNum > Link.INTNULL)
                                    break;
                            }
                            Log.i(RTCPLink.LOG_TAG, "socketLock = lock on");
                            this.socketLock.wait();
                            Log.i(RTCPLink.LOG_TAG, "socketLock = lock off");
                        }
                    }
//                    执行任务
                    String msg = this.socketRece.readLine();
                    Log.i(RTCPLink.LOG_TAG, "socketRece = " + msg);

//                    如果无法结构化，则放入默认队列,判断复用名称
                    if (!TypeTranDeter.canStr2JsonData(msg, RTCP_PDU.class) || !this.queueMap.containsKey(Link.gson.fromJson(msg, RTCP_PDU.class).reuseName)) {
//                        如果满队，则删除第一个
                        if (this.defaultQueue.remainingCapacity() == 0)
                            this.defaultQueue.take();
                        this.defaultQueue.put(msg);
                        continue;
                    }

                    RTCP_PDU structMsg = Link.gson.fromJson(msg, RTCP_PDU.class);
//                    获取队列
                    LinkedBlockingQueue<RTCP_PDU> queue = this.queueMap.get(structMsg.reuseName);
                    assert queue != null;
                    if (queue.remainingCapacity() == 0)
                        queue.take();
                    queue.put(structMsg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e(RTCPLink.LOG_TAG, "startSocketRece:InterruptedException", e);
                    break;
                } catch (Exception e) {
                    Log.e(RTCPLink.LOG_TAG, "startSocketRece:Exception", e);
                    this.off();
                    break;
                }
            }
        });

        this.socketThread.start();
    }

    /**
     * 接收默认队列的数据
     */
    @Override
    public String rece(int bufSize, int timeLimit) throws Exception {
        Log.i(RTCPLink.LOG_TAG, "Using rece");
//        1
        if (!this.canRece())
            return null;

        try {
            String msg;
            synchronized (this.receNumLock) {
                this.receNum++;
            }
            if (!this.defaultQueue.isEmpty())
                return this.defaultQueue.take();

            synchronized (this.socketLock) {
                this.socketLock.notify();
            }
            if (timeLimit == Link.INTNULL)
                msg = this.defaultQueue.take();
            else msg = this.defaultQueue.poll(timeLimit, TimeUnit.MILLISECONDS);

            return msg;
        } catch (Exception e) {
            Log.e(RTCPLink.LOG_TAG, "rece:Exception", e);
            throw e;
        } finally {
            synchronized (this.receNumLock) {
                if (this.receNum > Link.INTNULL)
                    this.receNum--;
            }
        }
    }

    /**
     * 接收复用名称队列的数据
     */
    @Override
    public String structRece(int bufSize, int timeLimit, String... reuseName) throws Exception {
        Log.i(RTCPLink.LOG_TAG, "Using structRece");
//        1
        if (!this.canRece())
            return null;

//        名称判断
        if (reuseName.length < 1 || !this.queueMap.containsKey(reuseName[0]))
            return null;

        try {
//            获取队列
            LinkedBlockingQueue<RTCP_PDU> queue = this.queueMap.get(reuseName[0]);
            assert queue != null;

            RTCP_PDU structMsg;
            synchronized (this.receNumLock) {
                this.receNum++;
            }
            if (!queue.isEmpty())
                return queue.take().data;

            synchronized (this.socketLock) {
                this.socketLock.notify();
            }
            if (timeLimit == Link.INTNULL)
                structMsg = queue.take();
            else structMsg = queue.poll(timeLimit, TimeUnit.MILLISECONDS);

//            如果超时返回 null
            if (structMsg == null)
                return null;
            return structMsg.data;
        } catch (Exception e) {
            Log.e(RTCPLink.LOG_TAG, "structRece:Exception", e);
            throw e;
        } finally {
            synchronized (this.receNumLock) {
                if (this.receNum > Link.INTNULL)
                    this.receNum--;
            }
        }
    }

    /**
     * 结构化发送信息
     */
    @Override
    public void structSend(String msg, String... reuseName) throws Exception {
//        1
        if (!this.canSend())
            return;

//        名称判断
        if (reuseName.length < 1 || !this.queueMap.containsKey(reuseName[0]))
            return;

        try {
            RTCP_PDU structMsg = new RTCP_PDU(reuseName[0], msg);
            this.send(Link.gson.toJson(structMsg));
        } catch (Exception e) {
            Log.e(RTCPLink.LOG_TAG, "structSend:Exception", e);
            throw e;
        }
    }

    @Override
    public boolean canRece() {
        return (this.launchFlag && this.socketThread != null);
    }
}
