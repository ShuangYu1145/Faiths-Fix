package dev.faiths.module.render;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render3DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.world.ModuleContainerAura;
import dev.faiths.utils.render.RenderUtils;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.tileentity.*;

import java.awt.*;

import static dev.faiths.utils.IMinecraft.mc;
import static org.lwjgl.opengl.GL11.*;

@SuppressWarnings("unused")
public class ModuleContainerESP extends CheatModule {
    public ModuleContainerESP() {
        super("ContainerESP", Category.RENDER);
    }

    private Handler<Render3DEvent> render3DEventHandler = event -> {
        try {
            float gamma = mc.gameSettings.gammaSetting;
            mc.gameSettings.gammaSetting = 100000.0F;

            for (final TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
                if (tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityBrewingStand || tileEntity instanceof TileEntityFurnace) {
                    Color color;

                    color = new Color(255, 43, 28);
                    if (ModuleContainerAura.openedContainer.contains(tileEntity.getPos())) {
                        color = new Color(37, 247, 240);
                    }

                    RenderUtils.drawBlockBox(tileEntity.getPos(), color, false);
                }
            }

            RenderUtils.glColor(new Color(255, 255, 255, 255));
            mc.gameSettings.gammaSetting = gamma;
        } catch (Exception ignored) {
        }
    };
}
