package dev.faiths.event.impl;

import dev.faiths.event.Event;
import net.minecraft.entity.EntityLivingBase;

public class EntityHealthUpdateEvent extends Event {

    private final EntityLivingBase entity;
    private final double damage;

    public EntityHealthUpdateEvent(EntityLivingBase entity, double damage) {
        this.entity = entity;
        this.damage = damage;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public double getDamage() {
        return damage;
    }

}
