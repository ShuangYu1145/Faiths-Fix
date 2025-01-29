package dev.faiths.module;

import dev.faiths.Faiths;
import dev.faiths.module.client.ModuleCNname;

public enum Category {
    CLIENT("Client","客户端"),
    COMBAT("Combat","暴力"),
    PLAYER("Player","玩家"),
    MOVEMENT("Movement","移动"),
    RENDER("Render","渲染"),
    WORLD("World","世界"),
    Misc("Misc","杂项");

    private final String displayName;
    private final String displayCNName;

    Category(String displayName,String displayCNName) {
        this.displayName = displayName;
        this.displayCNName = displayCNName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayCNName() {
        String displayCNName1 = displayName;
        if (Faiths.moduleManager.getModule(ModuleCNname.class).getState())
        {
            displayCNName1 = displayCNName;
        }
        return displayCNName1;
    }

}
