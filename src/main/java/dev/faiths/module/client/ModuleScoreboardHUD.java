package dev.faiths.module.client;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.MouseInputHandler;
import dev.faiths.utils.render.ColorUtil;
import dev.faiths.utils.render.GLUtil;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.value.ValueInt;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static dev.faiths.utils.IMinecraft.mc;
import static net.minecraft.client.gui.Gui.drawRect;

public class ModuleScoreboardHUD extends CheatModule {
    public ModuleScoreboardHUD() {
        super("ScoreboardHUD", Category.CLIENT, "记分版");
    }

    //位置，下面有详细的代码
    private final ValueInt xValue = new ValueInt("X", 50, 0, 4000);
    private final ValueInt yValue = new ValueInt("Y", 50, 0, 4000);
    private int prevX = 0, prevY = 0;
    //填你需要的长宽
    float hudwidth = 100;
    float hudheight = 100;

    private ScoreObjective objective;
    private boolean isFirstLoad;

    ScaledResolution scaledresolution = new ScaledResolution(mc);

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
                if (mouseX >= xValue.getValue() && mouseX <= xValue.getValue() + width + hudwidth && mouseY >= yValue.getValue() && mouseY <= yValue.getValue() + hudheight && Mouse.isButtonDown(0)) {
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
        }
        prevX = 0;
        prevY = 0;
        //在游戏界面绘制
        draw();
    };

    public void draw() {

        if (isFirstLoad) {
            isFirstLoad = false;
        }

//        if (mc.isSingleplayer()) {
//            objective = null;
//        }

        if (objective != null) {

            Scoreboard scoreboard = objective.getScoreboard();
            Collection<Score> scores = scoreboard.getSortedScores(objective);
            List<Score> filteredScores = Lists.newArrayList(Iterables.filter(scores, p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")));
            Collections.reverse(filteredScores);


            if (filteredScores.size() > 15) {
                scores = Lists.newArrayList(Iterables.skip(filteredScores, scores.size() - 15));
            } else {
                scores = filteredScores;
            }

            int maxWidth = mc.fontRendererObj.getStringWidth(objective.getDisplayName());

            for (Score score : scores) {

                ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());

//            if(numberSetting.isToggled()) {
//                s +=  ": " + EnumChatFormatting.RED + score.getScorePoints();
//            }

                maxWidth = Math.max(maxWidth, mc.fontRendererObj.getStringWidth(s));
            }

            int index = 0;

            GLUtil.startScale(xValue.getValue(), yValue.getValue(), 1.0F);

            for (Score score : scores) {

                index++;

                ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                String playerName = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
                String scorePoints = EnumChatFormatting.RED + "" + score.getScorePoints();

                RenderUtils.drawRect(xValue.getValue(), yValue.getValue() + (index * mc.fontRendererObj.FONT_HEIGHT) + 1, maxWidth + 4, mc.fontRendererObj.FONT_HEIGHT, Color.GRAY);

                mc.fontRendererObj.drawString(playerName, xValue.getValue() + 2, yValue.getValue() + (index * mc.fontRendererObj.FONT_HEIGHT) + 1, 553648127);

//                if (numberSetting.isToggled()) {
//                    mc.fontRendererObj.drawString(scorePoints, (xValue.getValue() + 2 + maxWidth + 2) - mc.fontRendererObj.getStringWidth(scorePoints), yValue.getValue() + (index * mc.fontRendererObj.FONT_HEIGHT) + 1, 553648127);
//                }

                if (index == scores.size()) {

                    String displayName = objective.getDisplayName();

                    RenderUtils.drawRect(xValue.getValue(), yValue.getValue(), 2 + maxWidth + 2, mc.fontRendererObj.FONT_HEIGHT, Color.GRAY);
                    RenderUtils.drawRect(xValue.getValue(), yValue.getValue() + mc.fontRendererObj.FONT_HEIGHT, 2 + maxWidth + 2, 1,Color.GRAY);

                    mc.fontRendererObj.drawString(displayName, xValue.getValue() + 2 + maxWidth / 2 - mc.fontRendererObj.getStringWidth(displayName) / 2, yValue.getValue() + 1, 553648127);
                }
            }

            GLUtil.stopScale();

            int lastMaxWidth = maxWidth + 4;
            int lastMaxHeight = (index * mc.fontRendererObj.FONT_HEIGHT) + 10;

        }
    }
}
