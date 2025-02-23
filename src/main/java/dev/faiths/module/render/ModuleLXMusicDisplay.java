package dev.faiths.module.render;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.CustomFont;
import dev.faiths.ui.font.FontManager;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.ClientUtils;
import dev.faiths.utils.MouseInputHandler;
import dev.faiths.utils.render.GlowUtils;
import dev.faiths.utils.render.ImageUtils;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.utils.render.RoundedUtil;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueInt;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static dev.faiths.module.client.ModuleHUD.globalalpha;
import static dev.faiths.module.client.ModuleHUD.glow;
import static dev.faiths.utils.IMinecraft.mc;


/**
 * 自写+ChatGPT+Kimi
 * 随便用没意见，但是不要删除此注释
 * By ShuangYu
 */
public class ModuleLXMusicDisplay extends CheatModule {
    public ModuleLXMusicDisplay() {
        super("LXMusicDisplay", Category.RENDER, "落雪音乐显示");
    }

    //位置，下面有详细的代码
    private final ValueInt xValue = new ValueInt("X", 50, 0, 4000);
    private final ValueInt yValue = new ValueInt("Y", 50, 0, 4000);
    public ValueBoolean image = new ValueBoolean("Image", false);
    private int prevX = 0, prevY = 0;

    public static int imagewidth = 0;

    //设置
    String name = null;
    String singer = null;
    String lyric = null;
    String progress = null;
    String duration = null;
    String picurl = null;


    //开启的时候提示，可以删除
    @Override
    public void onEnable() {
        ClientUtils.displayChatMessage("请确保落雪音乐设置中“开启API”这一项开启");
        ClientUtils.displayChatMessage("服务端口必须为“23330”");
        ClientUtils.displayChatMessage("如果都不设置你老母死了");
        super.onEnable();
    }

    //实时更新(Kimi)
    private final Handler<UpdateEvent> updateEventHandler = event -> {
        //端口
        String port = "23330";
        //API地址
        String apiURL = "http://127.0.0.1:" + port + "/status?filter=name,singer,lyricLineText,duration,progress,playbackRate,picUrl"; // 确保这是正确的API地址和端口
        //尝试获取API
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);



            lyric = jsonResponse.get("lyricLineText").getAsString();
            name = jsonResponse.get("name").getAsString();
            singer = jsonResponse.get("singer").getAsString();
            progress = jsonResponse.get("progress").getAsString();
            duration = jsonResponse.get("duration").getAsString();
            picurl = jsonResponse.get("picUrl").getAsString();


            /*  输出，测试时使用
            System.out.println("歌曲名: " + name);
            System.out.println("歌手名: " + singer);
            System.out.println("歌曲播放进度: " + progress);
            System.out.println("歌曲总时长: " + duration);
            System.out.println("歌曲封面图片链接: " + picurl);
             */

        } catch (Exception e) {
            //     e.printStackTrace();
            //判定，如果你一个设置不对就关闭并且提示
            Faiths.moduleManager.getModule(ModuleLXMusicDisplay.class).setState(false);
            Faiths.notificationManager.pop("六百六十六", "请确保你落雪音乐设置完毕", 10000, NotificationType.WARNING);
        }


    };

    private final Handler<Render2DEvent> renderHandler = event -> {
        //这里是信仰本来的判定，我抄上去了（在聊天界面移动位置）
        ScaledResolution sr = new ScaledResolution(mc);
        if (mc.currentScreen instanceof GuiChat) {
            MouseInputHandler.addMouseCallback((mouseX, mouseY) -> {
                if (prevX == 0 && prevY == 0) {
                    prevX = mouseX;
                    prevY = mouseY;
                }
                int prevMouseX = prevX;
                int prevMouseY = prevY;
                prevX = mouseX;
                prevY = mouseY;
                float width = Math.max(75, mc.fontRendererObj.getStringWidth(mc.thePlayer.getName()) + 20);
                if (mouseX >= xValue.getValue() && mouseX <= xValue.getValue() + width + 50 && mouseY >= yValue.getValue() && mouseY <= yValue.getValue() + 47 && Mouse.isButtonDown(0)) {
                    int moveX = mouseX - prevMouseX;
                    int moveY = mouseY - prevMouseY;

                    if (moveX != 0 || moveY != 0) {
                        xValue.setValue(xValue.getValue() + moveX);
                        yValue.setValue(yValue.getValue() + moveY);
                    }
                }
            });
            //打开聊天界面绘制
            draw();
            return;
        }
        prevX = 0;
        prevY = 0;
        //在游戏界面绘制
        draw();
    };

    // 长度判定
    public static String truncateString(String input) {
        // 判断字符串长度是否超过 10
        if (input != null && input.length() > 15) {
            // 若超过 10 个字符，截取前 10 个字符并拼接 ...
            return input.substring(0, 15) + "...";
        }
        // 若未超过 10 个字符，直接返回原字符串
        return input;
    }

    //绘制
    public void draw() {
        //防止空指针
        if (picurl != null || name != null || lyric != null || progress != null || duration != null) {

            //背景
            RoundedUtil.drawRound(xValue.getValue() - 5, yValue.getValue() - 5, 210, 60 + 20, 4, new Color(0, 0, 0, globalalpha.getValue()));
            if (glow.getValue()) {
                GlowUtils.drawGlow(xValue.getValue() - 5, yValue.getValue() - 5 , 210, 60 + 20, 4, new Color(0, 0, 0, globalalpha.getValue()));
            }

            //封面
            if(image.getValue()) {
                ImageUtils.drawImageFromUrl(picurl, xValue.getValue(), yValue.getValue(), 60, 60);
            }

            if (image.getValue()) {
                imagewidth = 65;
            } else {
                imagewidth = 0;
            }

            //歌名
            FontManager.sf24.drawString(name, xValue.getValue() + imagewidth, yValue.getValue(), -1);
            //歌手
            String singershort = truncateString(singer);
            FontManager.sf20.drawString(singershort, xValue.getValue() + imagewidth, yValue.getValue() + FontManager.sf24.getHeight() + 5, -1);
            //歌词
            FontManager.sf20.drawString(lyric, xValue.getValue() + imagewidth, yValue.getValue() + FontManager.sf24.getHeight() + 5 + FontManager.sf20.getHeight() + 5, -1);

            //一些进度条的变量和绘制(ChatGPT)
            //最大进度条宽度为 200 像素
            float maxWidth = 200.0f;
            //获取 duration 和 progress 的值
            float durationValue = Float.parseFloat(duration);  // 获取总时长
            float progressValue = Float.parseFloat(progress);  // 获取当前进度
            //确保 duration 和 progress 的比例适配
            float progressWidth = Math.min(progressValue / durationValue, 1.0f) * maxWidth;
            //限制背景条的最大宽度为 maxWidth
            float durationWidth = Math.min(durationValue, 1.0f) * maxWidth;  // 确保背景条宽度不会超过 maxWidth

            //绘制背景条（duration），灰色
            RoundedUtil.drawRound(xValue.getValue(), yValue.getValue() + 60 + 5, durationWidth, 4, 1.5F, Color.GRAY);
            //绘制进度条（progress），白色
            RoundedUtil.drawRound(xValue.getValue(), yValue.getValue() + 60 + 5, progressWidth, 4, 1.5F, Color.WHITE);

        }
    }
}
