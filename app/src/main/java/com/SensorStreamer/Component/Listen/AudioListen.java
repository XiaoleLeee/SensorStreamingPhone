package com.SensorStreamer.Component.Listen;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.SensorStreamer.Utils.TypeTranDeter;

/**
 * 读取音频数据，并基于回调函数处理
 * @author chen
 * @version 1.0
 * */

public class AudioListen extends Listen {
    /**
     * Audio 回调函数类接口
     * */
    public interface AudioCallback extends ListenCallback {
        /**
         * 回调函数 用于处理数据
         * @param data 传入音频数据
         * */
        void dealAudioData(byte[] data);
    }

    private final static String LOG_TAG = "AudioListen";
    private final int intNull;
//    回调函数
    private AudioCallback callback;
//    音频记录
    private AudioRecord audioRecord;
//    最小缓存大小
    private int minBufSize;
//    开始线程
    private Thread readThread;

    /**
     * 常量初始化
     * */
    public AudioListen(Activity activity) {
        super(activity);

        this.intNull = 0;
    }

    /**
     * 启动组件并设置回调函数 适配器
     * @param samplingRate 采样率
     * @param callback 数据处理回调函数
     * */
    public synchronized boolean launch(int samplingRate, AudioCallback callback) {
//        0 0
        if (!this.canLaunch()) return false;

        String[] params = new String[1];
        params[0] = Integer.toString(samplingRate);

        return this.launch(params, callback);
    }

    /**
     * 注册监听音频所需要的成员变量 优先使用重载适配器
     * */
    @Override
    public synchronized boolean launch(String[] params, ListenCallback callback) {
//        0 0
        if (!this.canLaunch()) return false;

        if (params == null || params.length < 1 || !TypeTranDeter.canStr2Num(params[0]) || Integer.parseInt(params[0]) <= 0)
            return false;

        int samplingRate = Integer.parseInt(params[0]);
        this.minBufSize = AudioRecord.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufSize * 10);

        this.callback = (AudioCallback)callback;

//        1 0
        return this.launchFlag = true;
    }

    /**
     * 注销监听音频所需要的成员变量
     * */
    @Override
    public synchronized boolean off() {
//        1 0
        if (!this.canOff())
            return false;

        this.audioRecord.release();
        this.audioRecord = null;

        this.callback = null;
        this.minBufSize = this.intNull;

//        0 0
        this.launchFlag = false;
        return true;
    }

    /**
     * 启动线程 处理音频数据
     * */
    @Override
    public synchronized void startRead() {
//        1 0
        if (!this.canStartRead()) return;

//        创建一个持续监听音频的类
        this.readThread = new Thread(() -> {
            try {
                this.audioRecord.startRecording();
                byte[] buf = new byte[this.minBufSize];
                while (!Thread.currentThread().isInterrupted()) {
                    int nRead = this.audioRecord.read(buf, 0, buf.length);
                    if (nRead <= 0 || this.callback == null) {
                        continue;
                    }
                    this.callback.dealAudioData(buf);
                }
            } catch (Exception e) {
                Log.e(AudioListen.LOG_TAG, "startRead.readThread:Exception", e);
                this.stopRead();
            }
        });

//        1 1
        this.startFlag = true;
        this.readThread.start();
    }

    /**
     * 结束线程 停止处理音频数据
     * */
    @Override
    public synchronized void stopRead() {
//        1 1
        if (!this.canStopRead()) return;

        if (this.readThread != null)
            this.readThread.interrupt();
        if (this.audioRecord != null)
            this.audioRecord.stop();

//        1 0
        this.startFlag = false;
    }
}
