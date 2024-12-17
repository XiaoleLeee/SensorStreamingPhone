package com.SensorStreamer.Component.Net.Link.RTCPLink;

import com.SensorStreamer.Component.Net.Link.Link;
import com.SensorStreamer.Component.Net.Link.LinkF;

/**
 * RTCPLink 工厂
 * @author chen
 * @version 1.0
 * */

public class RTCPLinkF extends LinkF {
    @Override
    public Link create() {
        return new RTCPLink();
    }
}
