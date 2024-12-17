package com.SensorStreamer.Component.Net.RLink.NRLink;

import android.util.Log;

import com.SensorStreamer.Component.Net.Link.Link;
import com.SensorStreamer.Component.Net.RLink.RLink;
import com.SensorStreamer.Utils.TypeTranDeter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * NRLink 适用任何 Link 的复用套件
 * @author chen
 * @version 1.0
 */

public class NRLink extends RLink {
    private final static String LOG_TAG = "NRLink";
    //    默认队列
    protected final LinkedBlockingQueue<RLink.RLink_PDU> defaultQueue;
    //    复用队列映射
    protected final Map<String, LinkedBlockingQueue<RLink.RLink_PDU>> queueMap;
    //    同步锁
    protected final Object receNumLock, mainLinkLock;
    //    信号
    protected int receNum;
    //    接收线程
    protected Thread mainLinkThread;

    public NRLink() {
        super();

        this.defaultQueue = new LinkedBlockingQueue<>();
        this.queueMap = new HashMap<>();
        this.receNumLock = new Object();
        this.mainLinkLock = new Object();

        this.receNum = RLink.INT_NULL;
        this.mainLinkThread = null;
    }

    /**
     * 注册变量并检查有效性
     * */
    @Override
    public synchronized boolean launch(Link link) throws Exception {
        if (!this.canLaunch())
            return false;

        if (!link.canSend() || !link.canRece())
            return false;

        try {
            this.link = link;
        } catch (Exception e) {
            Log.e(NRLink.LOG_TAG, "launch:Exception", e);
            this.launchFlag = true;
            this.off();
            throw e;
        }

        this.launchFlag = true;
        this.startMainLink();
        return true;
    }

    /**
     * 将传入的 Link 重启
     * */
    @Override
    public synchronized boolean reLaunchLink(int timeLimit) {
        if (!canOff())
            return false;

        try {
//            停止线程
            if (this.mainLinkThread != null)
                this.mainLinkThread.interrupt();
            this.mainLinkThread = null;
            this.receNum = RLink.INT_NULL;

//            link 重启
            if (this.link.reLaunch(timeLimit)) {
                this.launchFlag = false;
            }
            return this.launch(this.link);
        } catch (Exception e) {
            Log.e(NRLink.LOG_TAG, "reLaunchLink:Exception", e);
            return false;
        }
    }

    /**
     * 注销所有变量
     * */
    public synchronized boolean off() {
        if (!this.canOff())
            return false;

        try {
//            停止线程
            this.endMainLink();
//            清空队列信息
            this.defaultQueue.clear();
//            清空队列映射队列信息
            for (LinkedBlockingQueue<RLink_PDU> queue : this.queueMap.values())
                queue.clear();
//            清空映射信息
            this.queueMap.clear();
        } catch (Exception e) {
            Log.e(NRLink.LOG_TAG, "off:Exception", e);
            return false;
        }

        this.launchFlag = false;
        return true;
    }

    /**
     * 添加复用名称，并生成相关队列
     * @param reuseName 复用名称
     * @return 是否添加成功
     * */
    public boolean addReuseName(String reuseName) {
        if (!this.canRece())
            return false;

        LinkedBlockingQueue<RLink_PDU> queue = new LinkedBlockingQueue<>();
        this.queueMap.put(reuseName, queue);
        return true;
    }

    /**
     * 使用 link 收信，并将信息存入对应队列
     * 若队列已满，则删除最先入队的元素
     * */
    public synchronized boolean startMainLink() {
        if (!this.canSend() || this.mainLinkThread != null)
            return false;

        this.mainLinkThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (this.mainLinkLock) {
                        while (true) {
//                            获取数字锁
                            synchronized (this.receNumLock) {
                                Log.i(NRLink.LOG_TAG, "receNum = " + this.receNum);
                                if (this.receNum > RLink.INT_NULL)
                                    break;
                            }
                            Log.i(NRLink.LOG_TAG, "mainLinkLock = lock on");
                            this.mainLinkLock.wait();
                            Log.i(NRLink.LOG_TAG, "mainLinkLock = lock off");
                        }
                    }
//                    获取信息
                    String msg = this.link.rece(this.bufSize, RLink.INT_NULL);
                    Log.i(NRLink.LOG_TAG, "link.rece = " + msg);

                    RLink_PDU structMsg = null;
//                    无法结构化
                    if (!TypeTranDeter.canStr2JsonData(msg, RLink_PDU.class))
                        structMsg = new RLink_PDU(null, msg);
//                    无对应复用名称
                    else if (!this.queueMap.containsKey(this.gson.fromJson(msg, RLink_PDU.class).reuseName))
                        structMsg = this.gson.fromJson(msg, RLink_PDU.class);

//                    放入默认队列
                    if (structMsg != null) {
                        if (this.defaultQueue.remainingCapacity() == 0)
                            this.defaultQueue.take();
                        this.defaultQueue.put(structMsg);
                        continue;
                    }

                    structMsg = this.gson.fromJson(msg, RLink_PDU.class);
//                    获取队列
                    LinkedBlockingQueue<RLink_PDU> queue = this.queueMap.get(structMsg.reuseName);

                    if (queue == null) {
                        Log.e(NRLink.LOG_TAG, "startMainLink:Has a reuseName but the queue is null");
                        continue;
                    }
                    if (queue.remainingCapacity() == 0)
                        queue.take();
                    queue.put(structMsg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e(NRLink.LOG_TAG, "startMainLink:InterruptedException", e);
                    break;
                } catch (Exception e) {
                    Log.e(NRLink.LOG_TAG, "startMainLink:Exception", e);
                    this.off();
                    break;
                }
            }
        });
        this.mainLinkThread.start();

        return true;
    }

    /**
     * 终止当前心跳线程
     * */
    public synchronized boolean endMainLink() {
        if (!this.canSend() || this.mainLinkThread == null)
            return false;

        this.mainLinkThread.interrupt();
        this.mainLinkThread = null;
        this.receNum = RLink.INT_NULL;
        return true;
    }

    /**
     * 结构化发信
     * */
    @Override
    public void structSend(String msg, String... reuseName) throws Exception {
        Log.i(NRLink.LOG_TAG, "Using structSend");
        if (!this.canSend())
            return;

        try {
//            如果没有名称
            if (reuseName == null || !this.queueMap.containsKey(reuseName[0])) {
                this.link.send(msg);
                return;
            }

            RLink_PDU structMsg = new RLink_PDU(reuseName[0], msg);
            this.link.send(this.gson.toJson(structMsg));
        } catch (Exception e) {
            Log.e(NRLink.LOG_TAG, "structSend:Exception", e);
            throw e;
        }
    }

    /**
     * 接收复用名称队列的数据
     */
    @Override
    public String structRece(int bufSize, int timeLimit, String... reuseName) throws Exception {
        Log.i(NRLink.LOG_TAG, "Using structRece");
        if (!this.canRece())
            return null;

        try {
            LinkedBlockingQueue<RLink_PDU> queue;
//            获取队列
            if (reuseName == null || !this.queueMap.containsKey(reuseName[0]))
                queue = this.defaultQueue;
            else queue = this.queueMap.get(reuseName[0]);
            assert queue != null;

            RLink_PDU structMsg;
            synchronized (this.receNumLock) {
                this.receNum++;
            }
            if (!queue.isEmpty())
                return queue.take().data;

            synchronized (this.mainLinkLock) {
                this.bufSize = bufSize;
                this.mainLinkLock.notify();
            }
            if (timeLimit == Link.INTNULL)
                structMsg = queue.take();
            else structMsg = queue.poll(timeLimit, TimeUnit.MILLISECONDS);
//            如果超时返回 null
            if (structMsg == null)
                return null;
            return structMsg.data;
        } catch (Exception e) {
            Log.e(NRLink.LOG_TAG, "structRece:Exception", e);
            throw e;
        } finally {
            synchronized (this.receNumLock) {
                if (this.receNum > Link.INTNULL)
                    this.receNum--;
            }
        }
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
        return (launchFlag && this.link != null && this.mainLinkThread != null);
    }

}
