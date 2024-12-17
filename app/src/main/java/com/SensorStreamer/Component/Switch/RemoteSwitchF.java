package com.SensorStreamer.Component.Switch;

/**
 * RemoteSwitch 工厂
 * @author chen
 * @version 1.0
 * */

public class RemoteSwitchF extends SwitchF {
    @Override
    public Switch create() {
        return new RemoteSwitch();
    }
}
