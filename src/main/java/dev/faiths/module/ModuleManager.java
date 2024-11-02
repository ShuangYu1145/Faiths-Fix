package dev.faiths.module;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.Listener;
import dev.faiths.event.impl.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager implements Listener {
    public final List<CheatModule> modules = new ArrayList<>();
    public List<CheatModule> copiedModules;
    private final Handler<KeyEvent> keyHandler = event -> {
        modules.stream().filter(cheatModule -> cheatModule.getKeyBind() == event.getKey()).forEach(CheatModule::toggle);
    };

    public ModuleManager() {
        Faiths.INSTANCE.getEventManager().registerEvent(this);
    }

    public CheatModule getModule(final String name) {
        return modules.stream().filter(cheatModule -> cheatModule.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public <T> T getModule(final Class<T> clazz) {
        for (final CheatModule cheatModule : modules) {
            if (cheatModule.getClass() == clazz) {
                return (T) cheatModule;
            }
        }
        return null;
    }
    
    public List<CheatModule> getModulesByCategory(final Category category) {
        return modules.stream().filter(cheatModule -> cheatModule.getCategory() == category).collect(Collectors.toList());
    }

    public List<CheatModule> getModules() {
        return modules;
    }

    public List<CheatModule> getCopiedModules() {
        return copiedModules;
    }

    public void resetCopiedModules() {
        this.copiedModules = new ArrayList<>(this.modules);
    }

    @Override
    public boolean isAccessible() {
        return true;
    }
}
