package dev.faiths.module.client;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.render.ColorUtil;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.utils.render.shader.ShaderElement;
import dev.faiths.value.ValueBoolean;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleModuleList extends CheatModule {

    public ValueBoolean background = new ValueBoolean("Background", true);
    public ValueBoolean sortByLength = new ValueBoolean("按长度排序", true);

    public ModuleModuleList() {
        super("ModuleList", Category.CLIENT, "模块列表");
    }

    private final Handler<Render2DEvent> renderHandler = event -> {
        int count = 0;
        int x = 5;
        int y = 30;
        List<CheatModule> enabledModules = Faiths.moduleManager.getModules().stream()
                .filter(CheatModule::getState)
                .sorted(Comparator.comparing(module -> {
                    // 使用实际渲染宽度排序（包含模块名+后缀）
                    String name = module.getCNName();
                    String suffix = module.suffixIsNotEmpty() ? " " + module.getSuffix() : "";
                    int totalWidth = FontManager.bold15.getStringWidth(name + suffix);
                    return sortByLength.getValue() ? -totalWidth : totalWidth;
                }))
                .collect(Collectors.toList());

        int padding = 2;
        int textHeight = FontManager.bold15.getHeight();
        int moduleHeight = textHeight + padding * 2;

        for (CheatModule module : enabledModules) {
            String moduleName = module.getCNName();
            String suffix = module.suffixIsNotEmpty() ? " " + module.getSuffix() : "";

            // 计算实际渲染总宽度（包含空格）
            int totalWidth = FontManager.bold15.getStringWidth(moduleName + suffix);
            int moduleWidth = totalWidth + padding * 2;

            // 绘制背景（修正了参数顺序）
            if (background.getValue()) {
                RenderUtils.drawRect(x, y, moduleWidth, moduleHeight + 1,
                        new Color(0, 0, 0, ModuleHUD.globalalpha.getValue()));
                int finalY = y;
                ShaderElement.addBlurTask(() -> RenderUtils.drawRect(x, finalY, moduleWidth, moduleHeight + 1,
                        new Color(0, 0, 0, 255)));
                ShaderElement.addBloomTask(() -> RenderUtils.drawRect(x, finalY, moduleWidth, moduleHeight + 1,
                        new Color(0, 0, 0, 255)));
            }

            // 绘制模块名
            FontManager.bold15.drawString(
                    moduleName,
                    x + padding,
                    y + padding,
                    ColorUtil.applyOpacity(ModuleHUD.color(count).getRGB(), 255)
            );

            // 绘制后缀（包含前置空格）
            if (module.suffixIsNotEmpty()) {
                FontManager.bold15.drawString(
                        suffix,
                        x + padding + FontManager.bold15.getStringWidth(moduleName),
                        y + padding,
                        new Color(150, 150, 150).getRGB()
                );
            }

            y += moduleHeight + 1;
            count++;
        }
    };
}