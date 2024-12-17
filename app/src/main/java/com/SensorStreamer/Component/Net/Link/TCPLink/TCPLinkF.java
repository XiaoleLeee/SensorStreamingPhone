package com.SensorStreamer.Component.Net.Link.TCPLink;

import com.SensorStreamer.Component.Net.Link.Link;
import com.SensorStreamer.Component.Net.Link.LinkF;

/**
 * TCPLink 工厂
 * @author chen
 * @version 1.0
 * */

public class TCPLinkF extends LinkF {
    @Override
    public Link create() {
        return new TCPLink();
    }
}