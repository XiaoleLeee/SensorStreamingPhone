package com.SensorStreamer.Component.Listen;

import android.app.Activity;

/**
 * SensorListen 工厂
 * @author chen
 * @version 1.0
 * */

public class SensorListListenF extends ListenF {
    @Override
    public Listen create(Activity activity) {
        return new SensorListListen(activity);
    }
}
