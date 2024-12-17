package com.SensorStreamer.Component.Net.RLink.NRLink;

import com.SensorStreamer.Component.Net.RLink.RLink;
import com.SensorStreamer.Component.Net.RLink.RLinkF;

/**
 * NRLink 工厂
 * @author chen
 * @version 1.0
 * */

public class NRLinkF extends RLinkF {
    @Override
    public RLink create() {
        return new NRLink();
    }
}
