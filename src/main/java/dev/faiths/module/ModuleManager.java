package dev.faiths.module;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.Listener;
import dev.faiths.event.impl.KeyEvent;
import dev.faiths.module.client.*;
import dev.faiths.module.combat.*;
import dev.faiths.module.misc.*;
import dev.faiths.module.movement.*;
import dev.faiths.module.player.*;
import dev.faiths.module.render.*;
import dev.faiths.module.client.*;
import dev.faiths.module.world.*;

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
        // 注册模块
        modules.add(new ModuleWatermark());
        modules.add(new ModuleModuleList());
        modules.add(new ModulePotionHUD());
        modules.add(new ModuleCNname());
        modules.add(new ModuleGetYourPhone());
        modules.add(new ModuleLXMusicDisplay());
        modules.add(new ModuleHyt());
        modules.add(new ModuleClickGui());
        modules.add(new ModuleHUD());
        modules.add(new ModuleInventoryHUD());
        modules.add(new ModuleCape());
        modules.add(new ModuleAntiBot());
        modules.add(new ModuleAutoSoup());
        modules.add(new ModuleBackTrack());
        modules.add(new ModuleBowAimAssist());
        modules.add(new ModuleCriticals());
        modules.add(new ModuleGapple());
        modules.add(new ModuleGhostHand());
        modules.add(new ModuleKeepSprint());
        modules.add(new ModuleKillAura());
        modules.add(new ModuleSnowballAura());
        modules.add(new ModuleVelocity());
        modules.add(new ModuleBHop());
        modules.add(new ModuleGuiMove());
        modules.add(new ModuleLongJump());
        modules.add(new ModuleTimer());
        modules.add(new ModuleNoFluid());
        modules.add(new ModuleNoJumpDelay());
        modules.add(new ModuleNoSlow());
        modules.add(new ModuleNoWeb());
        modules.add(new ModuleSpeed());
        modules.add(new ModuleSprint());
        modules.add(new ModuleTargetStrafe());
        modules.add(new ModuleAntiVoid());
        modules.add(new ModuleAutoPearl());
        modules.add(new ModuleBlink());
        modules.add(new ModuleChatBypass());
        modules.add(new ModuleContainerStealer());
        modules.add(new ModuleFastPlace());
        modules.add(new ModuleInsult());
        modules.add(new ModuleInvManager());
        modules.add(new ModuleNoFall());
        modules.add(new ModuleNoRotateSet());
        modules.add(new ModuleNotify());
        modules.add(new ModuleRatProtection());
        modules.add(new ModuleSpeedMine());
        modules.add(new ModuleStuck());
        modules.add(new ModuleAnimation());
        modules.add(new ModuleCamera());
        modules.add(new ModuleContainerESP());
        modules.add(new ModuleESP2D());
        modules.add(new ModuleFarmHunterESP());
        modules.add(new ModuleItemESP());
        modules.add(new ModuleNameTags());
        modules.add(new ModuleProjectile());
        modules.add(new ModuleTargetHUD());
        modules.add(new ModuleXRay());
        modules.add(new ModuleItemPhysics());
        modules.add(new ModuleMotionBlur());
        modules.add(new ModuleAutoTool());
        modules.add(new ModuleBedBreaker());
        modules.add(new ModuleContainerAura());
        modules.add(new ModuleDisabler());
        modules.add(new ModuleHackerDetector());
        modules.add(new ModuleHypixelUtils());
        modules.add(new ModuleProtocol());
        modules.add(new ModuleScaffold());
        modules.add(new ModuleTeams());
        modules.add(new ModuleAutoKit());

    }


    public CheatModule getModule(final String name) {
        if (modules == null || modules.isEmpty()) {
            return null;
        }
        return modules.stream()
                .filter(cheatModule -> cheatModule.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public <T> T getModule(final Class<T> clazz) {
        if (modules == null || modules.isEmpty()) {
            return null;
        }
        for (final CheatModule cheatModule : modules) {
            if (clazz.isInstance(cheatModule)) {
                return clazz.cast(cheatModule);
            }
        }
        return null;
    }

    public List<CheatModule> getModulesByCategory(final Category category) {
        if (modules == null || modules.isEmpty()) {
            return new ArrayList<>();
        }
        return modules.stream()
                .filter(cheatModule -> cheatModule.getCategory() == category)
                .collect(Collectors.toList());
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