package com.SensorStreamer.Command.SensorCommand;

import android.app.Activity;
import android.hardware.Sensor;

import com.SensorStreamer.Component.Listen.SensorListen.AccelerometerListen;
import com.SensorStreamer.Component.Listen.SensorListen.GyroscopeListen;
import com.SensorStreamer.Component.Listen.SensorListen.MagneticFieldListen;
import com.SensorStreamer.Component.Listen.SensorListen.RotationVectorListen;
import com.SensorStreamer.Component.Listen.SensorListen.SensorListen;

import java.util.HashMap;

/**
 * Sensor 命令管理器
 * */
public class SensorCommandManger {
//    参数与对应的命令
    private final HashMap<Integer, SensorCommand> sensorCommandDict = new HashMap<>();

    public SensorCommandManger(Activity activity) {
//        设置字典
        sensorCommandDict.put(Sensor.TYPE_ACCELEROMETER, new SensorCommand(new AccelerometerListen(activity)));
        sensorCommandDict.put(Sensor.TYPE_GYROSCOPE, new SensorCommand(new GyroscopeListen(activity)));
        sensorCommandDict.put(Sensor.TYPE_MAGNETIC_FIELD, new SensorCommand(new MagneticFieldListen(activity)));
        sensorCommandDict.put(Sensor.TYPE_ROTATION_VECTOR, new SensorCommand(new RotationVectorListen(activity)));
    }

    /**
     * 根据参数启动 Launch 命令
     * @param sensorType 设备类型参数
     * @param callback 回调函数
     * */
    public void executeLaunchCommand(int sensorType, SensorListen.SensorCallback callback) {
//        根据参数获取命令
        SensorCommand sensorCommand = this.sensorCommandDict.get(sensorType);
        if (sensorCommand == null)
            return;
        sensorCommand.launch(callback);
    }

    /**
     * 根据参数启动 Off 命令
     * @param sensorType 设备类型参数
     * */
    public void executeOffCommand(int sensorType) {
//        根据参数获取命令
        SensorCommand sensorCommand = this.sensorCommandDict.get(sensorType);
        if (sensorCommand == null)
            return;
        sensorCommand.off();
    }

    /**
     * 启动所有 Launch 命令
     * @param callback 回调函数
     * */
    public void executeAllLaunchCommand(SensorListen.SensorCallback callback) {
        for (SensorCommand sensorCommand : this.sensorCommandDict.values())
            sensorCommand.launch(callback);
    }

    /**
     * 启动所有 Off 命令
     * */
    public void executeAllOffCommand() {
        for (SensorCommand sensorCommand : this.sensorCommandDict.values())
            sensorCommand.off();
    }
}
