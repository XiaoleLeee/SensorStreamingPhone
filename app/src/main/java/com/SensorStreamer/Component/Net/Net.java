package com.SensorStreamer.Component.Net;

public interface Net {
    /**
     * 结构化发信
     * */
    void structSend(String msg, String... reuseName) throws Exception;

    /**
     * 结构化收信
     * */
    String structRece(int bufSize, int timeLimit, String... reuseName) throws Exception;

    /**
     * 能否注册
     * */
    boolean canLaunch();

    /**
     * 能否注销
     * */
    public boolean canOff();

    /**
     * 能否发送
     * */
    public boolean canSend();

    /**
     * 能否接收
     * */
    public boolean canRece();
}
