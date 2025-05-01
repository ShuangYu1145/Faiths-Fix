package dev.faiths.module.client;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.render.RoundedUtil;
import dev.faiths.utils.render.shader.ShaderElement;

import java.awt.*;

import static dev.faiths.module.client.ModuleHUD.globalalpha;
import static dev.faiths.utils.IMinecraft.mc;

public class ModuleWatermark extends CheatModule {
    public ModuleWatermark() {
        super("Watermark",Category.CLIENT,"客户端名字");
    }


    private final Handler<Render2DEvent> renderHandler = event -> {
        final String name = "Faiths" + " | " + mc.thePlayer.getName() + " | " + "FPS:" + mc.getDebugFPS();
        int width = FontManager.bold20.getStringWidth(name) + 3;
        int height = FontManager.bold20.getHeight() + 2 ;
        Color bgColor = new Color(0, 0, 0, globalalpha.getValue());

        RoundedUtil.drawRound(5.0f, 10f, width, height, 2, bgColor);
        ShaderElement.addBlurTask(() -> RoundedUtil.drawRound(5, 10, width, height, 2, new Color(0,0,0)));
        ShaderElement.addBloomTask(() -> RoundedUtil.drawRound(5, 10, width, height, 2, new Color(0,0,0)));
            FontManager.bold20.drawStringDynamicWithShadow(name, 7, 11, 1, 50);
    };
}
