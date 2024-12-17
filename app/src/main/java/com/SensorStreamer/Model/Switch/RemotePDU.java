package com.SensorStreamer.Model.Switch;

/**
 * 通信数据单元与变量字典
 * @author chen
 * @version 1.0
 * */


public class RemotePDU {
    /**
     * 控制报文：
     * time 发送方时间戳，
     * control 控制信息，
     * data 子控制信息（如控制要使用的传感器类型）
     * */
    public final static String TYPE_CONTROL = "type_control";
    /**
     * 同步报文：
     * time 发送方时间戳，
     * control null，
     * data null
     * */
    public final static String TYPE_SYN = "type_syn";
    public final static String TYPE_MSG = "type_msg";

    public final static String CONTROL_SWITCHON = "control_switchOn";
    public final static String CONTROL_SWITCHOFF = "control_switchOff";

//    信息类型
    public String type;
//    时间
    public long time;
//    命令
    public String control;
//    数据
    public String[] data;

    public RemotePDU(String type, long time, String control, String[] data) {
        this.type = type;
        this.time = time;
        this.control = control;
        this.data = data;
    }
}
