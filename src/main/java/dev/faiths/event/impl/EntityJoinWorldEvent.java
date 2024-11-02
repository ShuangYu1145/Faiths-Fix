package dev.faiths.event.impl;

import dev.faiths.event.Event;
import net.minecraft.entity.Entity;

public class EntityJoinWorldEvent extends Event {
    public final Entity entity;

    public EntityJoinWorldEvent(Entity entity) {
        this.entity = entity;
    }
}
