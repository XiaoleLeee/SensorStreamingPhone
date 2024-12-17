package com.SensorStreamer.Component.Listen;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoListen extends Listen {

    // 视频回调接口
    public interface VideoCallback extends ListenCallback {
        void dealVideoData(byte[] data);  // 处理视频数据
    }

    private static final String LOG_TAG = "VideoListen";
    private final int intNull;

    private ExecutorService cameraExecutor;      // 相机执行器
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private ImageAnalysis imageAnalysis;
    private int width=640,height=480 ;
    private VideoCallback callback;              // 回调接口
    private Thread readThread;                   // 持续监听线程
    private final Object launchLock;

    // 构造函数
    public VideoListen(Activity activity) {
        super(activity);
        this.intNull = 0;
        this.launchLock = new Object();
        this.cameraExecutor = Executors.newSingleThreadExecutor();
    }

    // 初始化相机组件
    @Override
    public synchronized boolean launch(String[] params, ListenCallback callback) {
        synchronized (this.launchLock) {
            if (!this.canLaunch())
                return false;

            if (params == null || params.length == 0)
                return false;

            try {
                this.callback = (VideoCallback) callback;
                this.cameraExecutor = Executors.newSingleThreadExecutor();
                AtomicBoolean futureFlag = new AtomicBoolean(false);
                ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(activity);
                future.addListener(() -> {
                    try {
                        this.cameraProvider = future.get();

                        this.cameraSelector = new CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build();

                        this.imageAnalysis = new ImageAnalysis.Builder()
                                .setTargetAspectRatio(AspectRatio.RATIO_4_3)  // 设置目标分辨率
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();
                        this.launchFlag = true;
                    } catch (Exception e) {
                        Log.e("VideoListen", "启动失败", e);
                        this.off();
                    } finally {
                        synchronized (this.launchLock) {
                            this.launchLock.notifyAll();
                            futureFlag.set(true);
                        }
                    }

                }, ContextCompat.getMainExecutor(activity));

                if (!futureFlag.get())
                    this.launchLock.wait(5000);
                futureFlag.set(false);
                return true;
            } catch (Exception e) {
                Log.e("VideoListen", "启动失败", e);
                this.off();
                return false;
            }
        }
    }

    // 注销组件
    @Override
    public synchronized boolean off() {
        if (!this.canOff())
            return false;

        new Handler(Looper.getMainLooper()).post(() -> {
            if (this.cameraProvider != null) {
                this.cameraProvider.unbindAll();
                this.cameraProvider = null;
            }

            if (this.cameraExecutor != null && !this.cameraExecutor.isShutdown()) {
                this.cameraExecutor.shutdown();
                this.cameraExecutor = null;
            }
        });

        this.imageAnalysis = null;
        this.callback = null;
        this.launchFlag = false;
        return true;
    }

    private void compressImage(ImageProxy image, int quality) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        // 将 NV21 数据转换为 Bitmap
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);

        byte[] yuvBytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(yuvBytes, 0, yuvBytes.length);

        // 压缩图像
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] compressedBytes = baos.toByteArray();

        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.length);
        Log.i("wechat", "压缩后图片的大小: " + (compressedBitmap.getByteCount() / 1024 / 1024)
                + "M 宽度: " + compressedBitmap.getWidth()
                + " 高度: " + compressedBitmap.getHeight()
                + " bytes.length= " + (compressedBytes.length / 1024) + "KB"
                + " quality= " + quality);

        if (this.callback == null)
            return;
        this.callback.dealVideoData(compressedBytes);
    }

    // 启动读取线程
    @Override
    public synchronized void startRead() {
        if (!this.canStartRead()) {
            return;
        }


        if (cameraProvider == null || imageAnalysis == null) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                this.imageAnalysis.setAnalyzer(this.cameraExecutor, image -> {
                    compressImage(image, 10);
                    image.close();
                });

                this.cameraProvider.bindToLifecycle(
                        (LifecycleOwner) activity, this.cameraSelector, this.imageAnalysis
                );
                this.startFlag = true;
            } catch (Exception e) {
                Log.e("VideoListen", "相机操作失败", e);
                stopRead();
            }
        });

    }

    // 停止读取线程
    @Override
    public synchronized void stopRead() {
        if (!this.canStopRead()) return;

        if (this.cameraProvider != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                this.cameraProvider.unbindAll();
            });
        }
        this.startFlag = false;
    }
}