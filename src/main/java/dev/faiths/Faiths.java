package dev.faiths;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.google.common.reflect.ClassPath;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import dev.faiths.command.CommandManager;
import dev.faiths.command.impl.ModuleCommand;
import dev.faiths.component.SmoothCameraComponent;
import dev.faiths.config.ConfigManager;
import dev.faiths.event.EventManager;
import dev.faiths.hackerdetector.HackerDetector;
import dev.faiths.module.CheatModule;
import dev.faiths.module.ModuleManager;
import dev.faiths.ui.font.FontManager;
import dev.faiths.ui.notifiction.NotificationManager;
import dev.faiths.utils.SlotSpoofManager;
import dev.faiths.utils.player.RotationManager;
import dev.faiths.utils.tasks.TaskManager;
import ltd.guimc.silencefix.SFIRCListener;
import ltd.guimc.silencefix.SilenceFixIRC;
import net.vialoadingbase.ViaLoadingBase;
import net.viamcp.ViaMCP;
import org.apache.commons.lang3.RandomUtils;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;

import java.util.ArrayList;
import java.util.Comparator;

public class Faiths {
    public static Faiths INSTANCE;
    public static final String NAME = "Faiths";
    public static String VERSION = "250118";
    public static boolean IS_BETA = true;
    @NativeObfuscation.Inline
    public static boolean verified = false; // this is a temporary boolean
    private final EventManager eventManager;
    @NativeObfuscation.Inline
    public static HackerDetector hackerDetector;
    private final RotationManager rotationManager;
    public final SlotSpoofManager slotSpoofManager;
    @NativeObfuscation.Inline
    public static ModuleManager moduleManager;
    @NativeObfuscation.Inline
    public static ConfigManager configManager;
    @NativeObfuscation.Inline
    public static CommandManager commandManager;
    @NativeObfuscation.Inline
    public static NotificationManager notificationManager;
    public TaskManager taskManager;
    public static boolean isInitializing = true;
    public static int delta;
    public static long lastFrame;
    public final int astolfo;


    public Faiths() {
        INSTANCE = this;
        this.eventManager = new EventManager();
        commandManager = new CommandManager();
        commandManager.registerCommands();
        hackerDetector = new HackerDetector();
        FontManager.init();
        rotationManager = new RotationManager();
        notificationManager = new NotificationManager();
        slotSpoofManager = new SlotSpoofManager();
        taskManager = new TaskManager();
        astolfo = RandomUtils.nextInt(0, 4);
    }

    public static void onLoaded() {
        try {
            ViaMCP.create();
            ViaMCP.INSTANCE.initAsyncSlider();
            ViaLoadingBase.getInstance().reload(ProtocolVersion.v1_12_2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Faiths.moduleManager = new ModuleManager();
        Faiths.configManager = new ConfigManager();

        try {
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getPackageName().startsWith("dev.faiths") || info.getPackageName().startsWith("dev.jnic")) {
                    final Class<?> clazzs = info.load();
                    if (CheatModule.class.isAssignableFrom(clazzs) && clazzs != CheatModule.class) {
                        try {
                            Faiths.moduleManager.modules.add((CheatModule) clazzs.newInstance());
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Faiths.moduleManager.modules.sort(Comparator.comparing(CheatModule::getName));
        Faiths.moduleManager.modules.forEach(Faiths.INSTANCE.getEventManager()::registerEvent);
        Faiths.moduleManager.modules.forEach(module -> {
            if (!module.getValues().isEmpty())
                Faiths.commandManager.registerCommand(new ModuleCommand(module, module.getValues()));
        });
        Faiths.moduleManager.copiedModules = new ArrayList<>(Faiths.moduleManager.modules);
        Faiths.configManager.loadConfigs();
        Runtime.getRuntime().addShutdownHook(new Thread(Faiths.configManager::saveConfigs));
        Faiths.INSTANCE.getEventManager().registerEvent(Faiths.INSTANCE.getRotationManager());
        Faiths.INSTANCE.getEventManager().registerEvent(new SmoothCameraComponent());
        new ZombiesUtils().init();

                /*if (!IRC.isRunning()) {
                    try {
                        IRC.run(SHA256(String.valueOf(Wrapper.getUsername())));
                    } catch (NoSuchAlgorithmException e) {
                        Faiths.notificationManager.pop("IRC", "An error occurred.", NotificationType.WARNING);
                    }
                }*/

        try {
            SilenceFixIRC.init();
            SilenceFixIRC.Instance.connect();
            Faiths.INSTANCE.getEventManager().registerEvent(new SFIRCListener());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Faiths.isInitializing = false;
    }

    public RotationManager getRotationManager() {
        return rotationManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public boolean isInitializing() {
        return isInitializing;
    }

    public static boolean getIsBeta() { return true; }

    public static boolean isDev() {
        try {
            Class.forName("dev.faiths.Fai" + new StringBuilder("ht").reverse() + "s").getClass();
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
