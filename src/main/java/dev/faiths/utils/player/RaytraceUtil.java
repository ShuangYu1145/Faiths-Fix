package dev.faiths.utils.player;

import com.google.common.base.Predicates;
import dev.faiths.Faiths;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.optifine.reflect.Reflector;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class RaytraceUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static MovingObjectPosition rayCast(final Vector2f rotation, final double range, final float expand, final boolean throughWall) {
        return rayCast(rotation, range, expand, mc.thePlayer, throughWall);
    }
    public static MovingObjectPosition rayCast(final Vector2f rotation, final double range, final float expand, Entity entity, boolean throughWall) {
        final float partialTicks = mc.timer.renderPartialTicks;
        MovingObjectPosition objectMouseOver;

        if (entity != null && mc.theWorld != null) {
            objectMouseOver = entity.rayTraceCustom(range, rotation.x, rotation.y);
            double d1 = range;
            final Vec3 vec3 = entity.getPositionEyes(2F);

            if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && !throughWall) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
                // RayCastUtil.rayCast(new Rotation(mc.player.rotationYaw, mc.player.rotationPitch), 3, 0F, false)
            }

            final Vec3 vec31 = mc.thePlayer.getVectorForRotation(rotation.y, rotation.x);
            final Vec3 vec32 = vec3.addVector(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            final float f = 1.0F;
            final List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = d1;

            for (final Entity entity1 : list) {
                final float f1 = entity1.getCollisionBorderSize() + expand;
                AxisAlignedBB original = entity1.getEntityBoundingBox();
                // predict
                original = original.offset(entity.posX - entity.prevPosX, entity.posY - entity.prevPosY, entity.posZ - entity.prevPosZ);
                final AxisAlignedBB axisalignedbb = f1 >= 0 ? original.expand(f1, f1, f1).expand(-f1, -f1, -f1) : original.contract(f1, f1, f1).contract(-f1, -f1, -f1);
                final MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    final double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                    }
                }
            }

            if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
                objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
            }

            return objectMouseOver;
        }

        return null;
    }

}
