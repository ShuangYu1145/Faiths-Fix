package dev.faiths.event.impl;

import dev.faiths.event.Event;
import dev.faiths.utils.Servers;

public class HypixelServerSwitchEvent extends Event {
    public final Servers lastServer;
    public final Servers server;

    public HypixelServerSwitchEvent(Servers lastServer, Servers server) {
        this.lastServer = lastServer;
        this.server = server;
    }
}
