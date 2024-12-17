package com.SensorStreamer.Component.Listen;

import android.app.Activity;

/**
 * AudioListen 工厂
 * @author chen
 * @version 1.0
 * */

public class AudioListenF extends ListenF {
    @Override
    public Listen create(Activity activity) {
        return new AudioListen(activity);
    }
}
