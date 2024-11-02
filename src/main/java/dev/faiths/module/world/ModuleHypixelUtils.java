package dev.faiths.module.world;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.HypixelServerSwitchEvent;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.ServerUtils;
import dev.faiths.utils.Servers;
import dev.faiths.utils.tasks.FutureTask;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ScreenShotHelper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleHypixelUtils extends CheatModule {
    public ModuleHypixelUtils() {
        super("HypixelUtils", Category.WORLD);
    }

    private static Servers currentServer = Servers.NONE;
    private boolean setLanguage, wasPre, mwPort, hubbed, notify;
    private long gled = System.currentTimeMillis();

    public ValueBoolean bantracker = new ValueBoolean("BanTracker", true);
    private ValueBoolean reconnect = new ValueBoolean("Reconnect", true);
    private ValueBoolean auto_play = new ValueBoolean("AutoPlay", true);
    private ValueInt delay = new ValueInt("Delay", 3, 1, 10).visible(auto_play::getValue);
    private ValueBoolean auto_gg = new ValueBoolean("AutoGG", true);
    private ValueBoolean auto_gl = new ValueBoolean("AutoGL", false);
    private ValueBoolean auto_who = new ValueBoolean("AutoWho", true);
    private ValueBoolean auto_screenshot = new ValueBoolean("AutoScreenShot", true);
    private ValueInt auto_screenshot_delay = new ValueInt("ScreenShotDelay", 700, 50, 3000).visible(auto_screenshot::getValue);

    @Override
    public void onEnable() {
    }

    public Map<String, String> playerTag = new HashMap<>();

    private final Handler<HypixelServerSwitchEvent> updateEventHandler = event -> {
        if (event.lastServer == Servers.PRE) {
            if (event.server == Servers.BW) {
                ModuleBedBreaker.setWhiteListed(null);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        for (int x = -20; x < 21; x++) {
                            for (int z = -20; z < 21; z++) {
                                for (int y = -10; y < 12; y++) {
                                    BlockPos pos = new BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY + y, mc.thePlayer.posZ - z);
                                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                                    if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.bed && mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                                        ModuleBedBreaker.setWhiteListed(pos);
                                        Faiths.notificationManager.pop("Whitelisted your own bed!", "Whitelisted bed at " + pos, 3000, NotificationType.INFO);
                                    }
                                }
                            }
                        }
                    }
                }, 1000);
            }

            if (event.server != Servers.NONE) {
                if (auto_gl.getValue()) mc.thePlayer.sendChatMessage("/ac glhf");
                if (auto_who.getValue()) mc.thePlayer.sendChatMessage("/who");
                Faiths.notificationManager.pop("HypixelUtils", "Game Started! Mode: " + event.server.name(), NotificationType.INFO);
            }
        }
        if (event.server == Servers.PRE) {
            if (auto_who.getValue()) mc.thePlayer.sendChatMessage("/who");
        }
    };

    private void autoRegl(PacketEvent event) {
        if (event.getPacket() instanceof S02PacketChat && Math.abs(System.currentTimeMillis() - gled) <= 3000) {
            S02PacketChat packet = (S02PacketChat) event.getPacket();
            String message = packet.getChatComponent().getUnformattedText();
            if (message.contains("You can't shout if you're not in a team game!")) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mc.thePlayer.sendChatMessage("/ac glhf");
                    }
                }, 4000);
            }
        }
    }

    private void reconnect(PacketEvent event) {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) event.getPacket();
            String message = packet.getChatComponent().getUnformattedText();

            if (message.contains("Flying or related")) {
                mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage("/back"));
            }
        }
    }

    private void autoplay(PacketEvent packetEventReceive) {
        if (packetEventReceive.getPacket() instanceof S02PacketChat) {
            try {
                final S02PacketChat packet = (S02PacketChat) packetEventReceive.getPacket();
                final String command = packet.getChatComponent().toString().split("action=RUN_COMMAND, value='")[1];

                if (command.startsWith("/play ")) {
                    final String split = command.split("'}")[0];
                    Faiths.notificationManager.pop("Sending you to the next game in ", delay.getValue() * 1000, NotificationType.INFO);

                    Faiths.INSTANCE.getTaskManager().queue(new FutureTask(this.delay.getValue() * 1_000) {

                        @Override
                        public void execute() {
                            mc.getNetHandler().addToSendQueue(new C01PacketChatMessage(split));
                        }

                        @Override
                        public void run() {
                        }
                    });
                }
            } catch (Exception ignored) {}
        }
    }

    private void autogg(PacketEvent e) {
        if (e.getPacket() instanceof S45PacketTitle) {
            S45PacketTitle packet = (S45PacketTitle) e.getPacket();

            if (packet.getMessage().getUnformattedText().contains("VICTORY!")) {

                mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage("gg"));

            }
        }
    }

    private void autoscreenshot(PacketEvent e) {
        if (e.getPacket() instanceof S45PacketTitle) {
            S45PacketTitle packet = (S45PacketTitle) e.getPacket();

            if (packet.getMessage().getUnformattedText().contains("VICTORY!")) {
                Faiths.INSTANCE.getTaskManager().queue(new FutureTask(auto_screenshot_delay.getValue()) {
                    @Override
                    public void execute() {
                        ScreenShotHelper.safeSaveScreenshot();
                    }

                    @Override
                    public void run() {
                    }
                });
            }
        }
    }

    private void quickmath(PacketEvent event) {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) event.getPacket();
            String text = packetChat.getChatComponent().getUnformattedText();

            if (text.contains("QUICK MATHS! Solve:")) {
                String[] eArray = text.split("Solve: ");
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");

                try {
                    mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage(engine.eval(eArray[1].replace("x", "*")).toString()));
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final Handler<PacketEvent> packetEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            if (reconnect.getValue()) {
                reconnect(event);
            }

            if (auto_play.getValue()) {
                autoplay(event);
            }

            if (auto_gg.getValue()) {
                autogg(event);
            }

            if (auto_screenshot.getValue()) {
                autoscreenshot(event);
            }

            if (ServerUtils.isHypixel() && event.getPacket() instanceof S3DPacketDisplayScoreboard) {
                S3DPacketDisplayScoreboard packet = (S3DPacketDisplayScoreboard) event.getPacket();
                String serverName = packet.func_149370_d();
                Servers _currentServer = Servers.NONE;

                if (serverName.equalsIgnoreCase("Mw")) {
                    _currentServer = Servers.MW;
                } else if (serverName.equalsIgnoreCase("\u00a7e\u00a7lHYPIXEL")) {
                    _currentServer = Servers.UHC;
                } else if (serverName.equalsIgnoreCase("SForeboard")) {
                    _currentServer = Servers.SW;
                } else if (serverName.equalsIgnoreCase("BForeboard")) {
                    _currentServer = Servers.BW;
                } else if (serverName.equalsIgnoreCase("PreScoreboard")) {
                    _currentServer = Servers.PRE;
                } else if (serverName.equalsIgnoreCase("Duels")) {
                    _currentServer = Servers.DUELS;
                } else if (serverName.equalsIgnoreCase("Pit")) {
                    _currentServer = Servers.PIT;
                } else if (serverName.equalsIgnoreCase("Blitz SG")) {
                    _currentServer = Servers.SG;
                } else if (serverName.equalsIgnoreCase("MurderMystery")) {
                    _currentServer = Servers.MM;
                } else if (!serverName.contains("health") && !serverName.contains("\u272B")) {
                    _currentServer = Servers.NONE;
                }

                if (_currentServer != currentServer) {
                    Faiths.INSTANCE.getEventManager().callEvent(new HypixelServerSwitchEvent(currentServer, _currentServer));
                    currentServer = _currentServer;
                }
            }
        }
    };

    private void onPaste(String str){
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            StringSelection strse1 = new StringSelection(str);
            c.setContents(strse1, strse1);
        } catch (Exception e){
            e.printStackTrace();
        }//try
    }//onPaste


    public static Servers getCurrentServer() {
        return currentServer;
    }

    public static void setCurrentServer(Servers server) {
        currentServer = server;
    }
}
