package com.SensorStreamer.Component.Listen;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.SensorStreamer.Utils.TypeTranDeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 读取 Sensor 数据，并基于回调函数处理
 * @author chen
 * @version 1.0
 * */

public class SensorListListen extends Listen implements SensorEventListener {
    /**
     * SensorList 回调函数类接口
     * */
    public interface SensorListCallback extends ListenCallback {
        /**
         * 回调函数 用于处理数据
         * @param sensorType Sensor 类型
         * @param data 传入 Sensor 数据
         * @param sensorTimestamp 与硬件绑定的时间戳
         * */
        void dealSensorData(String sensorType, float[] data, long sensorTimestamp);
    }

    private final static String LOG_TAG = "SensorListen";
    private final HashMap<Integer, String> sensorDir;
    private final SensorManager sensorManager;
    private final int intNull;
//    回调函数
    private SensorListCallback callback;
//    当采样率为 0 时 启动变化时传输数据
    private int samplingRate;
//    需要监听的 sensor
    private List<Sensor> sensors;

    /**
     * 常量初始化
     * */
    public SensorListListen(Activity activity) {
        super(activity);

        this.sensorManager = (SensorManager) this.activity.getSystemService(Context.SENSOR_SERVICE);
        this.sensorDir = new HashMap<>();
        this.sensorDir.put(Sensor.TYPE_ACCELEROMETER, "ACCELEROMETER");
        this.sensorDir.put(Sensor.TYPE_GYROSCOPE, "GYROSCOPE");
        this.sensorDir.put(Sensor.TYPE_ROTATION_VECTOR, "ROTATION_VECTOR");
        this.sensorDir.put(Sensor.TYPE_MAGNETIC_FIELD, "MAGNETIC_FIELD");
        this.sensorDir.put(Sensor.TYPE_LIGHT, "LIGHT");
        this.sensorDir.put(Sensor.TYPE_PRESSURE, "PRESSURE");
        this.sensorDir.put(Sensor.TYPE_GRAVITY, "GRAVITY");
        this.sensorDir.put(Sensor.TYPE_LINEAR_ACCELERATION, "LINEAR_ACCELERATION");
        this.sensorDir.put(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "MAGNETIC_FIELD_UNCALIBRATED");
        this.sensorDir.put(Sensor.TYPE_GAME_ROTATION_VECTOR, "GAME_ROTATION_VECTOR");
        this.sensorDir.put(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "GYROSCOPE_UNCALIBRATED");
        this.sensorDir.put(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT, "LOW_LATENCY_OFFBODY_DETECT");

        this.sensorDir.put(Sensor.TYPE_PROXIMITY, "PROXIMITY");
        this.sensorDir.put(Sensor.TYPE_RELATIVE_HUMIDITY, "RELATIVE_HUMIDITY");
        this.sensorDir.put(Sensor.TYPE_AMBIENT_TEMPERATURE, "AMBIENT_TEMPERATURE");
        this.sensorDir.put(Sensor.TYPE_POSE_6DOF, "POSE_6DOF");
        this.sensorDir.put(Sensor.TYPE_STATIONARY_DETECT, "STATIONARY_DETECT");
        this.sensorDir.put(Sensor.TYPE_MOTION_DETECT, "MOTION_DETECT");
        this.sensorDir.put(Sensor.TYPE_HEART_BEAT, "HEART_BEAT");
        this.sensorDir.put(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, "ACCELEROMETER_UNCALIBRATED");
        this.sensorDir.put(69682, "HEART_BEAT");
    
        this.samplingRate = this.intNull = -1;

        for (Sensor sensor : this.sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            Log.i(SensorListListen.LOG_TAG ,"Support " + sensor.getName() + ", Type = " + sensor.getType());
        }
    }

    /**
     * 启动组件并设置回调函数，适配器
     * @param sensors Sensor 类型
     * @param samplingRate 采样率
     * @param callback 数据处理回调函数
     * */
    public synchronized boolean launch(int[] sensors, int samplingRate, SensorListCallback callback) {
//        0 0
        if (!this.canLaunch()) return false;

        if (sensors == null)
            return false;
        
        String[] params = new String[sensors.length + 1];
//        将类型转换为字符串参数
        for (int i = 0; i < sensors.length; i++) {
            params[i] = Integer.toString(sensors[i]);
        }
        params[sensors.length] = Integer.toString(samplingRate);

        return this.launch(params, callback);
    }

    /**
     * 选择 Sensor，并设置成员变量，优先使用重载适配器
     * */
    @Override
    public synchronized boolean launch(String[] params, ListenCallback callback) {
//        0 0
        if (!this.canLaunch()) return false;

        if (params == null || params.length < 2 || !TypeTranDeter.canStr2Num(params[params.length - 1]) || Integer.parseInt(params[params.length - 1]) < 0)
            return false;

        this.samplingRate = Integer.parseInt(params[params.length - 1]);

        this.sensors = new ArrayList<>();
//        获取当前选择的 Sensor
        for (int i = 0; i < params.length - 1; i++) {
//            是否有效 是否在字典中
            if (!TypeTranDeter.canStr2Num(params[i]) || !this.sensorDir.containsKey(Integer.parseInt(params[i])))
                continue;
            int type = Integer.parseInt(params[i]);
            Sensor sensor = this.sensorManager.getDefaultSensor(type);
//            是否有效设备或重复
            if (sensor == null || this.sensors.contains(sensor)) {
                Log.i(SensorListListen.LOG_TAG, "launch: The sensors are invalid or duplicate. SenorType = " + this.sensorDir.get(type));
                continue;
            }
            this.sensors.add(sensor);
        }

        this.callback = (SensorListCallback) callback;

//        1 0
        return this.launchFlag = true;
    }

    /**
     * 注销 Sensor 监听组件
     * @implNote 如果在调用 off 前有使用 startRead，必须先使用 stopRead
     * */
    @Override
    public synchronized boolean off() {
//        1 0
        if (!this.canOff()) return false;

        if (this.sensors != null)
            this.sensors.clear();
        this.sensors = null;

        this.samplingRate = this.intNull;

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

//        注册对应的监听
        for (Sensor sensor : this.sensors) {
            this.sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

//        1 1
        this.startFlag = true;
    }

    /**
     * 停止处理 Sensor 数据
     * */
    @Override
    public synchronized void stopRead() {
//        1 1
        if (!this.canStopRead()) return;

//        注销对应的监听
        for (Sensor sensor : this.sensors) {
            this.sensorManager.unregisterListener(this, sensor);
        }

//        1 0
        this.startFlag = false;
    }

    /**
     * 当 Sensor 数据变化时执行回调函数，仅当采样率为零时生效
     * */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        1 1
        if (this.samplingRate > 0 || !this.startFlag)
            return;

        Thread readChangedThread = new Thread(() -> {
            try {
                if (this.callback == null)
                    return;
//                使用字典将 type 转换为 String
                this.callback.dealSensorData(this.sensorDir.get(sensorEvent.sensor.getType()), sensorEvent.values, sensorEvent.timestamp);
            } catch (Exception e) {
                Log.e(SensorListListen.LOG_TAG, "onSensorChanged:Exception", e);
            }
        });
        readChangedThread.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
