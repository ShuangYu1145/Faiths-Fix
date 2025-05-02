package dev.faiths.module.client;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.MouseInputHandler;
import dev.faiths.utils.render.GlowUtils;
import dev.faiths.utils.render.RoundedUtil;
import dev.faiths.utils.render.shader.ShaderElement;
import dev.faiths.value.ValueInt;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import java.awt.*;

import static dev.faiths.utils.IMinecraft.mc;

public class ModuleInventoryHUD extends CheatModule {

    public ModuleInventoryHUD() {
        super("InventoryHUD", Category.CLIENT,"物品栏显示");
    }

    //位置，下面有详细的代码
    private final ValueInt xValue = new ValueInt("X", 50, 0, 4000);
    private final ValueInt yValue = new ValueInt("Y", 50, 0, 4000);
    private int prevX = 0, prevY = 0;
    //填你需要的长宽
    float hudwidth = 162;
    float hudheight = 55;

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
            return;
        }
        prevX = 0;
        prevY = 0;
        //在游戏界面绘制
        draw();
    };

    //绘制
    public void draw() {
        ItemStack[] inventory = mc.thePlayer.inventory.mainInventory;
        boolean hasItems = false;


            RoundedUtil.drawRound(xValue.getValue() - 1, yValue.getValue() - 1, 162, 55, 4, new Color(0, 0, 0, ModuleHUD.globalalpha.getValue()));
            ShaderElement.addBlurTask(() -> RoundedUtil.drawRound(xValue.getValue() - 1, yValue.getValue() - 1, 162, 55, 4, new Color(0, 0, 0)));
            ShaderElement.addBloomTask(() -> RoundedUtil.drawRound(xValue.getValue() - 1, yValue.getValue() - 1, 162, 55, 4, new Color(0, 0, 0)));
            for (int i = 9; i < inventory.length; i++) {
                ItemStack stack = inventory[i];
                if (stack != null) {
                    hasItems = true;
                    int itemX = (int) xValue.getValue() + ((i - 9) % 9) * 18;
                    int itemY = (int) yValue.getValue() + ((i - 9) / 9) * 18;
                    drawItemStack(stack, itemX, itemY);
                }
            }

        if (!hasItems) {
            String emptyText = "Empty";
            // 计算文本宽度
            int textWidth = FontManager.bold20.getStringWidth(emptyText);
            // 计算居中位置 (xValue + HUD宽度/2 - 文本宽度/2)
            float centerX = xValue.getValue() + (hudwidth / 2f) - (textWidth / 2f);
            // 计算垂直居中 (yValue + HUD高度/2 - 字体高度/2)
            float centerY = yValue.getValue() + (hudheight / 2f) - (FontManager.bold20.getHeight() / 2f);

            FontManager.bold20.drawStringWithShadow(emptyText, centerX, centerY, new Color(255, 255, 255, 200).getRGB());
        }
    }


    private void drawItemStack(ItemStack stack, int x, int y) {
        RenderItem itemRender = mc.getRenderItem();

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableLighting();
        itemRender.zLevel = 200.0F;

        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRendererObj, stack, x, y, null);

        itemRender.zLevel = 0.0F;
        GlStateManager.popMatrix();
        GlStateManager.disableLighting();
    }
}
