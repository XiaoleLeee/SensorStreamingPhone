package com.SensorStreamer.Component.Net.Link.UDPLink;

import com.SensorStreamer.Component.Net.Link.Link;
import com.SensorStreamer.Component.Net.Link.LinkF;

/**
 * UDPLink 工厂
 * @author chen
 * @version 1.0
 * */

public class UDPLinkF extends LinkF {
    @Override
    public Link create() {
        return new UDPLink();
    }
}
