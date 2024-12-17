package com.SensorStreamer.Component.Net.Link.UDPLink;

import android.util.Log;

import com.SensorStreamer.Component.Net.Link.Link;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;

/**
 * 基于 UDP 的 Link
 * @author chen
 * @version 1.1
 * */

public class UDPLink extends Link {
    private final static String LOG_TAG = "UDPLink";
//    发送和接收数据的 socket
    protected DatagramSocket sendSocket, receSocket;

    public UDPLink () {
        super();
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
//            发送用初始化
            this.sendSocket = new DatagramSocket();
//            接收用初始化 固定接收对应地址端口的信息
            this.receSocket = new DatagramSocket(this.port);
        } catch (Exception e) {
            Log.d(UDPLink.LOG_TAG, "launch:Exception", e);
            this.launchFlag = true;
            this.off();
            throw e;
        }

//        1
        this.launchFlag = true;
        return true;
    }

    /**
     * UDP 不需要重启，非面向连接的协议
     * */
    @Override
    public boolean reLaunch(int timeLimit) {
        return true;
    }

    /**
     * 注销所有可变成员变量
     * */
    @Override
    public synchronized boolean off() {
//        1
        if (!this.canOff())
            return false;

        try {
            if (this.sendSocket != null && !this.sendSocket.isClosed())
                this.sendSocket.close();
            this.sendSocket = null;

            if (this.receSocket != null && !this.receSocket.isClosed())
                this.receSocket.close();
            this.receSocket = null;
        } catch (Exception e) {
            Log.d(UDPLink.LOG_TAG, "off:Exception", e);
            return false;
        }

//        0
        this.launchFlag = false;
        return true;
    }

    /**
     * 发送 buf 数据
     * */
    @Override
    public void send(String msg) throws Exception {
//        1
        if (!this.canSend())
            return;

        try {
            byte[] buf = msg.getBytes(this.charset);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
            this.sendSocket.send(packet);
            System.out.println();
        } catch (Exception e) {
            Log.e(UDPLink.LOG_TAG, "send:Exception", e);
            this.off();
            throw e;
        }
    }

    /**
     * 接收并将数据存储在 buf
     * */
    @Override
    public String rece(int bufSize, int timeLimit) throws Exception {
//        1
        if (!this.canRece())
            return null;

        if (bufSize < Link.MIN_BUF_SIZE)
            bufSize = this.bufSize;

        try {
            byte[] buf = new byte[bufSize];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

//            设置时间限制
            if (timeLimit != Link.INTNULL)
                this.receSocket.setSoTimeout(timeLimit);
            this.receSocket.receive(packet);
//            开始自适应
            synchronized (this) {
                this.adaptiveBufSize(packet.getLength());
            }

            return new String(packet.getData(), packet.getOffset(), packet.getLength(), this.charset);
        } catch (Exception e) {
            Log.e(UDPLink.LOG_TAG, "rece:Exception", e);
            this.off();
            throw e;
        }
    }

    /**
     * 自适应缓冲大小
     * */
    @Override
    protected synchronized void adaptiveBufSize(int packetSize) {
        if (packetSize > this.bufSize) {
            this.bufSize = Math.min(Link.MAX_BUF_SIZE, (this.bufSize << 1));
            return;
        }
        if (packetSize < (this.bufSize >> 2))
            this.bufSize = Math.max(Link.MIN_BUF_SIZE, (this.bufSize >> 1));
    }
}
