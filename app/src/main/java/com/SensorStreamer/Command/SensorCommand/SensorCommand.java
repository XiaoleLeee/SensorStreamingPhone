package com.SensorStreamer.Command.SensorCommand;

import com.SensorStreamer.Component.Listen.SensorListen.SensorListen;

/**
 * Sensor 命令
 * */
public class SensorCommand {
    private final SensorListen sensorListen;

    SensorCommand(SensorListen sensorListen) {
        this.sensorListen = sensorListen;
    }

    /**
     * 控制对应的 sensor 启动
     * */
    public void launch(SensorListen.SensorCallback callback) {
        this.sensorListen.launch(null, callback);
        this.sensorListen.startRead();
    }

    /**
     * 控制对应的 sensor 关闭
     * */
    public void off() {
        this.sensorListen.stopRead();
        this.sensorListen.off();
    }
}
