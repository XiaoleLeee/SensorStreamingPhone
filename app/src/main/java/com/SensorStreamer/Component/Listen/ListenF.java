package com.SensorStreamer.Component.Listen;

import android.app.Activity;

/**
 * Listen 抽象工厂
 * @author chen
 * @version 2.0
 * */

public abstract class ListenF {
    /**
     * @param activity 主活动
     * */
    public abstract Listen create(Activity activity);
}
