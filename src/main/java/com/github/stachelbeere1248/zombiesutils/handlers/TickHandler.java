package com.github.stachelbeere1248.zombiesutils.handlers;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.game.waves.WaveTiming;
import com.github.stachelbeere1248.zombiesutils.utils.Scoreboard;
import dev.faiths.event.Handler;
import dev.faiths.event.Listener;
import dev.faiths.event.impl.PostUpdateEvent;

public class TickHandler implements Listener {
    public Handler<PostUpdateEvent> postUpdateEventHandler = this::onTick;

    public void onTick(PostUpdateEvent event) {
        Scoreboard.refresh();
        WaveTiming.onTick();
        ZombiesUtils.getInstance().getHandlers().getRenderer().tick();
    }

    @Override
    public boolean isAccessible() {
        return true;
    }
}
