package dev.faiths.module.combat;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.EntityHealthUpdateEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.event.impl.WorldLoadEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.Pair;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.value.ValueMultiBoolean;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.concurrent.ConcurrentLinkedDeque;

@SuppressWarnings("unused")
public class ModuleAntiBot extends CheatModule {

    private final CheckPlayer botCheck;

    public ModuleAntiBot() {
        super("AntiBot", Category.COMBAT);
        this.botCheck = PlayerUtils::hasInvalidNetInfo;
    }

    private final ValueMultiBoolean mode = new ValueMultiBoolean("Mode",
            new Pair("Watchdog", true),
            new Pair("SleepingEntity", false),
            new Pair("NoArmor", false));
    private final ConcurrentLinkedDeque<Entity> validEntities = new ConcurrentLinkedDeque<>();

    private final Handler<WorldLoadEvent> worldLoadEventHandler = event -> {
        if (mode.isEnabled("Watchdog")) {
            this.validEntities.clear();
        }
    };

    private final Handler<EntityHealthUpdateEvent> entityHealthUpdateEventHandler = event -> {
        if (mode.isEnabled("Watchdog")) {
            if (event.getEntity() instanceof EntityOtherPlayerMP)
                this.validEntities.add(event.getEntity());
        }
    };

    public boolean isBot(final Entity entity) {
        if (!(entity instanceof EntityPlayer))
            return false;
        final EntityPlayer player = (EntityPlayer) entity;
        if (mode.isEnabled("Watchdog")) {
            if (!this.validEntities.contains(player) && botCheck.check(player))
                return false;
        }
        if (mode.isEnabled("SleepingEntity")) {
            if (player.isPlayerSleeping())
                return true;
        }
        if (mode.isEnabled("NoArmor")) {
            return player.inventory.armorInventory[0] == null
                    && (player.inventory.armorInventory[1] == null
                            && (player.inventory.armorInventory[2] == null
                                    && (player.inventory.armorInventory[3] == null)));
        }

        return false;
    }

    @FunctionalInterface
    public interface CheckPlayer {
        boolean check(EntityPlayer player);
    }
}
