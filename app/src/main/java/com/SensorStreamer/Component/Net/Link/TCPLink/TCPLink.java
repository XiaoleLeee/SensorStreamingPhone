package com.SensorStreamer.Component.Net.Link.TCPLink;

import android.util.Log;

import com.SensorStreamer.Component.Net.Link.Link;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * 基于 TCP 的 Link
 * @author chen
 * @version 1.0
 * */

public class TCPLink extends Link {
    private final static String LOG_TAG = "TCPLink";
//    发送和接收数据的 socket
    protected Socket socket;
//    发送
    protected PrintWriter socketSend;
//    接收
    protected BufferedReader socketRece;

    public TCPLink () {
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
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(address, port), timeout);

            this.socketSend = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), this.charset), true);
            this.socketRece = new BufferedReader(new InputStreamReader(socket.getInputStream(), this.charset));
        } catch (Exception e) {
            Log.d(TCPLink.LOG_TAG, "launch:Exception", e);
            this.launchFlag = true;
            this.off();
            throw e;
        }

//        1
        this.launchFlag = true;
        return true;
    }

    /**
     * 注销部分变量后重新注册
     * */
    @Override
    public synchronized boolean reLaunch(int timeLimit) {
        if (!this.canOff())
            return false;

        try {
            this.off();
            return this.launch(this.address, this.port, timeLimit, this.charset);
        } catch (Exception e) {
            Log.d(TCPLink.LOG_TAG, "reLaunch:Exception", e);
            return false;
        }
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
            if (this.socket != null && !this.socket.isClosed())
                this.socket.close();
            this.socket = null;
        } catch (Exception e) {
            Log.d(TCPLink.LOG_TAG, "off:Exception", e);
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
            this.socketSend.println(msg);
        } catch (Exception e) {
            Log.e(TCPLink.LOG_TAG, "send:Exception", e);
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

        try {
            return this.socketRece.readLine();
        } catch (Exception e) {
            Log.e(TCPLink.LOG_TAG, "rece:Exception", e);
            this.off();
            throw e;
        }
    }

    @Override
    protected synchronized void adaptiveBufSize(int packetSize) {
    }
}
