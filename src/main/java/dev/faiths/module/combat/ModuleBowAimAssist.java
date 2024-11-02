package dev.faiths.module.combat;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.world.ModuleTeams;
import dev.faiths.utils.player.Rotation;
import dev.faiths.utils.player.RotationUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueMode;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleBowAimAssist extends CheatModule {
    private EntityLivingBase target;

    private final ValueFloat fov = new ValueFloat("Fov", 180F, 10F, 360F);
    private final ValueFloat range = new ValueFloat("Range", 100F, 1F, 200F);
    private final ValueBoolean movefix = new ValueBoolean("MoveFix",true);
    private final ValueMode priority = new ValueMode("Priority", new String[] {"Angle", "Range"}, "Range");
    private final ValueBoolean autoRelease = new ValueBoolean("AutoRelease", true);
    private final ValueBoolean silent = new ValueBoolean("Silent", true);
    public ModuleBowAimAssist() {
        super("BowAimAssist", Category.COMBAT);
    }

    private final Handler<MotionEvent> motionHandler = event -> {
        // check bow || check is using bow
        if (mc.thePlayer.inventory.getCurrentItem() == null || mc.thePlayer.inventory.getCurrentItem().getItem() != Items.bow || !mc.thePlayer.isUsingItem() || event.isPre()) {
            target = null;
            return;
        }

        target = this.getTarget();

        if (target == null) {
            return;
        }

        final float[] rotation = RotationUtils.getPlayerRotations(target);

        if (silent.getValue()) {
            Faiths.INSTANCE.getRotationManager().setRotation(new Rotation(rotation[0], rotation[1]), 180F, movefix.getValue());
        } else {
            new Rotation(rotation[0], rotation[1]).toPlayer(mc.thePlayer);
        }
        if (autoRelease.getValue() && mc.thePlayer.getItemInUseDuration() >= 20) {
            mc.playerController.onStoppedUsingItem(mc.thePlayer);
        }
    };


    @Override
    public void onDisable() {
        target = null;
    }

    public boolean isValid(EntityPlayer entity, double range) {
        if (mc.thePlayer.getClosestDistanceToEntity(entity) > range)
            return false;
        if (!entity.isEntityAlive())
            return false;
        if (entity == Minecraft.getMinecraft().thePlayer || entity.isDead
                || Minecraft.getMinecraft().thePlayer.getHealth() == 0F)
            return false;
        if (entity.getEntityId() == -8 || entity.getEntityId() == -1337) {
            return false;
        }
        if (ModuleTeams.isSameTeam(entity))
            return false;

        return !Faiths.moduleManager.getModule(ModuleAntiBot.class).isBot(entity);
    }

    private EntityLivingBase getTarget() {
        Stream<EntityPlayer> stream = mc.theWorld.playerEntities.stream()
                .filter(e -> isValid(e, range.getValue()))
                .filter(mc.thePlayer::canEntityBeSeen)
                .filter(e -> RotationUtils.inFoV(e, fov.getValue()));

        if (priority.is("Range")) {
            stream = stream.sorted(Comparator.comparingDouble(e -> e.getDistanceToEntity(mc.thePlayer)));
        } else if (priority.is("Angle")) {
            stream = stream.sorted(Comparator.comparingDouble(RotationUtils::getYawToEntity));
        }
        List<EntityPlayer> list = stream.collect(Collectors.toList());
        if (list.isEmpty())
            return null;

        return list.get(0);
    }
}
