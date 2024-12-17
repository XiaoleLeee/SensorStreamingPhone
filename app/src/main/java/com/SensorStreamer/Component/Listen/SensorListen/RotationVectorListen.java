package com.SensorStreamer.Component.Listen.SensorListen;

import android.app.Activity;
import android.hardware.Sensor;

/**
 * RotationVectorListen 允许自定义数据处理
 * @author chen
 * @version 1.0
 * */

public class RotationVectorListen extends SensorListen {
    public RotationVectorListen(Activity activity) {
        super(activity);
    }

    @Override
    protected Sensor getSensor() {
        return this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }
}
