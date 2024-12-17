package com.SensorStreamer.Component.Listen.SensorListen;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.SensorStreamer.Component.Listen.Listen;

import java.util.HashMap;

/**
 * 抽象 SensorListen 基于回调函数处理
 * @author chen
 * @version 1.0
 * */

public abstract class SensorListen extends Listen implements SensorEventListener {
    /**
     * Sensor 回调函数类接口
     * */
    public interface SensorCallback extends ListenCallback {
        /**
         * 回调函数 用于处理数据
         * @param sensorType Sensor 类型
         * @param data 传入 Sensor 数据
         * @param sensorTimestamp 与硬件绑定的时间戳
         * */
        void dealSensorData(String sensorType, float[] data, long sensorTimestamp);
    }

    private final static String LOG_TAG = "SensorListen";
    private final static HashMap<Integer, String> sensorDir = new HashMap<Integer, String>(){{
        put(Sensor.TYPE_ACCELEROMETER, "ACCELEROMETER");
        put(Sensor.TYPE_GYROSCOPE, "GYROSCOPE");
        put(Sensor.TYPE_ROTATION_VECTOR, "ROTATION_VECTOR");
        put(Sensor.TYPE_MAGNETIC_FIELD, "MAGNETIC_FIELD");
        put(Sensor.TYPE_LIGHT, "LIGHT");
        put(Sensor.TYPE_PRESSURE, "PRESSURE");
        put(Sensor.TYPE_GRAVITY, "GRAVITY");
        put(Sensor.TYPE_LINEAR_ACCELERATION, "LINEAR_ACCELERATION");
        put(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "MAGNETIC_FIELD_UNCALIBRATED");
        put(Sensor.TYPE_GAME_ROTATION_VECTOR, "GAME_ROTATION_VECTOR");
        put(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "GYROSCOPE_UNCALIBRATED");
        put(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT, "LOW_LATENCY_OFFBODY_DETECT");

        put(Sensor.TYPE_PROXIMITY, "PROXIMITY");
        put(Sensor.TYPE_RELATIVE_HUMIDITY, "RELATIVE_HUMIDITY");
        put(Sensor.TYPE_AMBIENT_TEMPERATURE, "AMBIENT_TEMPERATURE");
        put(Sensor.TYPE_POSE_6DOF, "POSE_6DOF");
        put(Sensor.TYPE_STATIONARY_DETECT, "STATIONARY_DETECT");
        put(Sensor.TYPE_MOTION_DETECT, "MOTION_DETECT");
        put(Sensor.TYPE_HEART_BEAT, "HEART_BEAT");
        put(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, "ACCELEROMETER_UNCALIBRATED");
        put(69682, "HEART_BEAT");
    }};
    protected final SensorManager sensorManager;

//    回调函数
    private SensorCallback callback;
//    需要监听的 sensor
    private Sensor sensor;

    /**
     * 常量初始化
     * */
    public SensorListen(Activity activity) {
        super(activity);
        this.sensorManager = (SensorManager) this.activity.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * 获取 sensor
     * @return sensor
     * */
    protected abstract Sensor getSensor();

    /**
     * 选择 Sensor，并设置成员变量，优先使用重载适配器
     * */
    @Override
    public synchronized boolean launch(String[] params, ListenCallback callback) {
//        0 0
        if (!this.canLaunch())
            return false;

        this.sensor = this.getSensor();
//        传感器无效
        if (this.sensor == null)
            return false;
        this.callback = (SensorCallback) callback;

//        1 0
        return this.launchFlag = true;
    }

    /**
     * 注销 Sensor 监听组件
     * */
    @Override
    public synchronized boolean off() {
//        1 0
        if (!this.canOff()) return false;

        this.sensor = null;
        this.callback = null;

//        0 0
        this.launchFlag = false;
        return true;
    }

    /**
     * 读取并使用回调函数处理 Sensor 数据
     * 仅当采样率为零时启动 onSensorChanged
     * */
    @Override
    public synchronized void startRead() {
//        1 0
        if (!this.canStartRead()) return;

//        注册监听
        this.sensorManager.registerListener(this, this.sensor, SensorManager.SENSOR_DELAY_FASTEST);

//        1 1
        this.startFlag = true;
    }

    /**
     * 重置数据处理相关的参数
     * */
    protected void resetProc() {}

    /**
     * 停止处理 Sensor 数据
     * */
    @Override
    public synchronized void stopRead() {
//        1 1
        if (!this.canStopRead()) return;

//        注销监听
        this.sensorManager.unregisterListener(this, sensor);
        this.resetProc();

//        1 0
        this.startFlag = false;
    }

    /**
     * 数据处理
     * @param values 原始数据
     * @return 处理数据
     */
    protected float[] valuesProc(float[] values) {
        return values;
    }

    /**
     * 当 Sensor 数据变化时执行回调函数
     * */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        1 1
        if (!this.canStopRead())
            return;

        Thread readChangedThread = new Thread(() -> {
            try {
                if (this.callback == null)
                    return;
//                使用字典将 type 转换为 String
                this.callback.dealSensorData(SensorListen.sensorDir.get(sensorEvent.sensor.getType()), this.valuesProc(sensorEvent.values), sensorEvent.timestamp);
            } catch (Exception e) {
                Log.e(SensorListen.LOG_TAG, "onSensorChanged:Exception", e);
            }
        });
        readChangedThread.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
