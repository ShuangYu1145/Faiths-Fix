package dev.faiths.module.client;

import dev.faiths.module.CheatModule;
import dev.faiths.module.Category;

public class ModuleClientCrash extends CheatModule {
    public ModuleClientCrash() {
        super("ClientCrash", Category.CLIENT, "客户端崩溃");
    }
    @Override
    public void onEnable() {
        System.exit(0);
    }
}
