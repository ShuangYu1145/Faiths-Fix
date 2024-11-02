package com.github.stachelbeere1248.zombiesutils.handlers;

import com.github.stachelbeere1248.zombiesutils.timer.Timer;
import com.github.stachelbeere1248.zombiesutils.utils.Scoreboard;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.EntityJoinWorldEvent;
import net.minecraft.entity.monster.EntityZombie;

import java.util.Optional;

public class Round1Correction {

    private final Timer timer;
    private final String serverNumber;

    public Round1Correction(Timer timer, String serverNumber) {
        this.timer = timer;
        this.serverNumber = serverNumber;
    }

    public Handler<EntityJoinWorldEvent> entityJoinWorldEventHandler = this::eventHandler;

    public void eventHandler(EntityJoinWorldEvent event) {
        if (!(event.entity instanceof EntityZombie)) return;
        if (Scoreboard.isNotZombies()) return;
        final Optional<String> s = Scoreboard.getServerNumber();
        if (!s.isPresent()) return;
        if (!s.get().equals(serverNumber)) {
            return;
        }
        this.timer.correctStartTick();
    }
}
