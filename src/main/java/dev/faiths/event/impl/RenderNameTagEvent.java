package dev.faiths.event.impl;

import dev.faiths.event.CancelableEvent;
import net.minecraft.entity.EntityLivingBase;

public class RenderNameTagEvent extends CancelableEvent {

    private final EntityLivingBase entityLivingBase;

    public RenderNameTagEvent(EntityLivingBase entityLivingBase) {
        this.entityLivingBase = entityLivingBase;
    }

    public EntityLivingBase getEntityLivingBase() {
        return entityLivingBase;
    }

}
