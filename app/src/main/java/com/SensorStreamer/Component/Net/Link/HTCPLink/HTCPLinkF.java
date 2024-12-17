package com.SensorStreamer.Component.Net.Link.HTCPLink;

import com.SensorStreamer.Component.Net.Link.Link;
import com.SensorStreamer.Component.Net.Link.LinkF;

/**
 * HTCPLink 工厂
 * @author chen
 * @version 1.0
 * */

public class HTCPLinkF extends LinkF {
    @Override
    public Link create() {
        return new HTCPLink();
    }
}
