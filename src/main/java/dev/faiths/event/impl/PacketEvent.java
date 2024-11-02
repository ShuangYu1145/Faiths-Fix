package dev.faiths.event.impl;

import dev.faiths.event.CancelableEvent;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public class PacketEvent extends CancelableEvent {
    private final Type type;
    private final Packet<?> packet;
    private final INetHandler netHandler;

    public PacketEvent(final Type type, final Packet<?> packet,INetHandler netHandler) {
        this.type = type;
        this.packet = packet;
        this.netHandler = netHandler;
    }

    public Type getType() {
        return type;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public enum Type {
        SEND, RECEIVE
    }
}
