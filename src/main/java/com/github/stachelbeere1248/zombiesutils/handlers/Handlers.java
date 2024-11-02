package com.github.stachelbeere1248.zombiesutils.handlers;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import dev.faiths.Faiths;

public class Handlers {
    private final RenderGameOverlayHandler renderer;

    public Handlers() {
        renderer = new RenderGameOverlayHandler();
    }

    public void registerAll() {
        ZombiesUtils.getInstance().getConfig();
        Faiths.INSTANCE.getEventManager().registerEvent(renderer);
        Faiths.INSTANCE.getEventManager().registerEvent(new TickHandler());
        Faiths.INSTANCE.getEventManager().registerEvent(new ChatHandler());
        Faiths.INSTANCE.getEventManager().registerEvent(new KeyInputHandler());
    }

    public RenderGameOverlayHandler getRenderer() {
        return renderer;
    }
}
