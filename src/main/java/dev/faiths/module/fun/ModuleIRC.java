package dev.faiths.module.fun;

import dev.faiths.Faiths;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.tasks.FutureTask;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ScreenShotHelper;


import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.Socket;

import static dev.faiths.utils.IMinecraft.mc;

public class ModuleIRC extends CheatModule {

    private static Socket socket;
    private static PrintWriter writer;
    private static boolean isConnected = false;
    private static final String SERVERIP = "141.11.125.53";//你服务器公网ip
    private static final String SERVERPORT = "42491";//你服务器开放端口 我JS用的80端口 服务端开放80端口
    private static final int RECONNECT_INTERVAL = 5;
    private static boolean A = true;
    private String Verifyname = "";
    private String name = "";

    public ModuleIRC() {
        super("IRC", Category.FUN);
    }

    @Override
    public void onEnable() {
        startIRCThread();
    }

    @SneakyThrows
    @Override
    public void onDisable() {
//            if (isConnected){
//                try {
//                socket.close();
//                } catch (Exception ignored) {
//            }
//
//            }
//            A = false;
//            isConnected = false;
        Faiths.moduleManager.getModule(ModuleIRC.class).setState(true);
    }


        /*
        对于代码的讲解
        writer.printin(这里写你要写入的信息)这是将消息发送到IRC服务器
        showIRCMessage(这里写你要写入的信息)这是将消息显示在聊天框
         */
        public static void startIRCThread() {
            Thread ircThread = new Thread(() -> {
                try {
                    socket = new Socket(SERVERIP, Integer.parseInt(SERVERPORT));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                    //writer.println(".serverusernames" + "§b[" + Verify.Username + "("  +  mc.thePlayer.getName() + ") ]§r" +"<" + mc.thePlayer.getName() + ">");
                //    TypeMap.Type();
                    writer.println(".serverusernames"  +  "§f[§e"  + /*TypeMap.Type*/ Faiths.NAME +"§f] §f" + mc.thePlayer.getName() + "§7(§b§l" + mc.thePlayer.getName()  + "§7)§f" +  "§7(§b§l" + Faiths.NAME + " TEST§7)§f");//自己选择Type添加的位置 TypeMap.Type
                    startIRCChatThread();
                    showIRCMessage("§3[§e§lIRC INFO§3]§r " +"§c§b聊天服务器链接成功！ 需要和同客户端聊天在聊天前加! 例如:! 双鱼是傻子");
                    isConnected = true;
                } catch (IOException e) {
                    showIRCMessage("§3[§e§lIRC §cERROR§3]§r " +"§c§b聊天服务器连接失败！ 请联系管理或者群主解决！");
                }
            });
            ircThread.start();
     //       reconnect();
        }

        public static void sendToServer(String content) {
            if (isConnected) {
                writer.println(content);
                startIRCChatThread();
            } else {
                showIRCMessage("§3[§e§lIRC §cERROR§3]§r 未连接到聊天服务器");
                reconnect();
          //      startIRCThread();
            }
        }
        public static void startIRCChatThread() {
            Thread messageThread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    while (!socket.isClosed()) {
                        String message = reader.readLine();
                        if (message != null) {
                            String finalMessage = "§3[§e§lIRC§3]§r " + message;
                            showIRCMessage(finalMessage);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageThread.start();
        }

        public static void showIRCMessage(String message) {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft.thePlayer != null && minecraft.ingameGUI != null) {
                IChatComponent chatComponent = new ChatComponentText(message);
                minecraft.ingameGUI.getChatGUI().printChatMessage(chatComponent);
            }
        }

    public static void reconnect() {
        isConnected = false;
        showIRCMessage("未连接到聊天服务器,正在尝试链接.....");
        try {
            TimeUnit.SECONDS.sleep(RECONNECT_INTERVAL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        startIRCThread();
    }
}


