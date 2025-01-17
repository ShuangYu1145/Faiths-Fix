package dev.faiths.ui.clickgui;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.List;

import dev.faiths.module.render.ModuleHUD;
import dev.faiths.utils.render.animation.normal.Animation;
import dev.faiths.utils.render.animation.normal.Direction;
import dev.faiths.utils.render.animation.normal.easing.EaseBackIn;
import dev.faiths.utils.render.animation.simple.SimpleAnimation;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import dev.faiths.Faiths;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.HSBData;
import dev.faiths.utils.Pair;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.value.AbstractValue;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueColor;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueInt;
import dev.faiths.value.ValueMode;
import dev.faiths.value.ValueMultiBoolean;
import net.minecraft.util.ResourceLocation;

public class Window {
    private final Category category;
    private float x, y;
    private int prevMouseX, prevMouseY, moveX, moveY;
    private boolean leftMouseClicked = false, rightMouseClicked = false, expand = false;
    private boolean dragging = false;
    private Runnable runnable = null;
    private DecimalFormat FLOAT_POINT_FORMAT = new DecimalFormat("0.00");

    private Animation animation;

    public Window(Category category, float x, float y) {
        this.category = category;
        this.x = x;
        this.y = y;
    }

    private boolean mouseHovered(final float x, final float y, final float width, final float height, final int mouseX,
            final int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    protected void renderWindow(final int mouseX, final int mouseY) {
        moveX = mouseX - this.prevMouseX;
        moveY = mouseY - this.prevMouseY;
        this.prevMouseX = mouseX;
        this.prevMouseY = mouseY;
        if (mouseHovered(x, y, 100F, 13F, mouseX, mouseY) && Mouse.isButtonDown(0)) {
            if ((moveX != 0 || moveY != 0) && !dragging) {
                runnable = () -> {
                    this.x += moveX;
                    this.y += moveY;
                };
                dragging = true;


            }

        }


        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        final List<CheatModule> modules = Faiths.moduleManager.getModulesByCategory(category);
        float height = 15F;
        if (expand) {
            for (final CheatModule module : modules) {
                height += 10F;
                if (module.isExpanded()) {
                    for (final AbstractValue<?> value : module.getValues()) {
                        if (!value.isVisible())
                            continue;
                        if (value instanceof ValueMultiBoolean) {
                            final ValueMultiBoolean multiBooleanValue = (ValueMultiBoolean) value;
                            height += (multiBooleanValue.isExpanded() ? multiBooleanValue.getValue().length + 1 : 1)
                                    * 10F;
                        } else {
                            if (value instanceof ValueFloat || value instanceof ValueInt)
                                height += 10F;
                            else
                                height += 10F;
                        }
                    }
                }
            }
        }
        // if (expand) height += 3F;
     //   RenderUtils.drawOutLineRect(0F, 0F, 100F, height, 1F, new Color(25, 25, 25), ModuleHUD.color(ModuleHUD.colortick.getValue()));
        RenderUtils.drawRoundedRect(0F, 0F, 100F, height, 5F, new Color(25, 25, 25,100).getRGB());

        if (!expand) {
            RenderUtils.drawImage(new ResourceLocation("client/icon/eye_close.png"), 80F, 3F, 8F, 8F,
                    new Color(60, 60, 60));
        } else {
            RenderUtils.drawImage(new ResourceLocation("client/icon/eye_open.png"), 80F, 3F, 8F, 8F,
                    new Color(164, 53, 144));
        }
        final ResourceLocation categoryResourceLocation;
        switch (category) {
            case COMBAT:
                categoryResourceLocation = new ResourceLocation("client/icon/combat_good.png");
                break;
            case MOVEMENT:
                categoryResourceLocation = new ResourceLocation("client/icon/movement_good.png");

                break;
            case RENDER:
                categoryResourceLocation = new ResourceLocation("client/icon/visual_good.png");
                break;

            case PLAYER:
                categoryResourceLocation = new ResourceLocation("client/icon/player_good.png");
                break;
            default:
                categoryResourceLocation = new ResourceLocation("client/icon/world_good.png");
                break;

        }

        RenderUtils.drawImage(categoryResourceLocation, 90F, 3F, 8F, 8F, ModuleHUD.maincolor.getValue());
        float moduleHeight = 10F;
        for (final CheatModule module : modules) {
            if (expand) {
                if (!module.isExpanded()) {
            //        RenderUtils.drawRect(3F, moduleHeight, 95F, 11F, new Color(36, 36, 36));
                    if (module.getState()) {
               //         RenderUtils.drawRect(3F, moduleHeight, 95F, 11F, ModuleHUD.color(ModuleHUD.colortick.getValue()));
                        RenderUtils.drawRoundedRect(3F, moduleHeight + 2, 95F, 10F, 5,new Color(25, 25, 25,120).getRGB());
                    }
                }

                if (mouseHovered(x, y + moduleHeight + 2, 100, 10F, mouseX, mouseY)) {
                    if (!module.isExpanded() && !module.getState()) {
                  //      RenderUtils.drawRect(3F, moduleHeight, 95F, 11F, new Color(255, 255, 255, 50));
                        RenderUtils.drawRoundedRect(3F, moduleHeight + 2, 95F, 10F, 5 ,new Color(255, 255, 255, 50).getRGB());
                    }
                    if (Mouse.isButtonDown(1)) {
                        if (!rightMouseClicked) {
                            if (!module.getValues().isEmpty())
                                module.setExpanded(!module.isExpanded());
                            rightMouseClicked = true;
                        }
                    } else {
                        rightMouseClicked = false;
                    }

                    if (Mouse.isButtonDown(0)) {
                        if (!leftMouseClicked) {
                            module.toggle();
                            leftMouseClicked = true;
                        }
                    } else {
                        leftMouseClicked = false;
                    }
                }
                FontManager.bold15.drawString(module.getName(),
                        100F - FontManager.bold15.getStringWidth(module.getName()) - 3F,
                        moduleHeight + 5F,
                        module.getState() && module.isExpanded() ? ModuleHUD.maincolor.getValue().getRGB()
                                : new Color(160, 160, 160).getRGB());

                if (module.isExpanded()) {
                    for (final AbstractValue<?> value : module.getValues()) {
                        if (!value.isVisible())
                            continue;
                        if (value instanceof ValueBoolean) {
                            moduleHeight += 10F;
                            final ValueBoolean booleanValue = (ValueBoolean) value;
                            if (mouseHovered(x, y + moduleHeight + 2, 100F, 10F, mouseX, mouseY)) {
                                if (Mouse.isButtonDown(0)) {
                                    if (!leftMouseClicked) {
                                        leftMouseClicked = true;
                                        booleanValue.setValue(!booleanValue.getValue());
                                    }
                                } else {
                                    leftMouseClicked = false;
                                }
                            }
                            if (booleanValue.getValue()) {
                        //        RenderUtils.drawRect(3F, moduleHeight, 95F, 11F, ModuleHUD.color(ModuleHUD.colortick.getValue()));
                                RenderUtils.drawRoundedRect(3F, moduleHeight + 2, 95F, 10F, 5,ModuleHUD.maincolor.getValue().getRGB());
                            }
                            FontManager.bold13.drawString(booleanValue.getName(), 5F, moduleHeight + 5F, -1);
                        }

                        if (value instanceof ValueMode) {
                            moduleHeight += 10F;
                            final ValueMode modeValue = (ValueMode) value;
                            if (mouseHovered(x, y + moduleHeight + 2, 100F, 10F, mouseX, mouseY)) {
                                if (Mouse.isButtonDown(0)) {
                                    if (!leftMouseClicked) {
                                        leftMouseClicked = true;
                                        boolean neverBreak = true;
                                        for (int index = 0; index < modeValue.getModes().length; index++) {
                                            if (modeValue.getValue() == modeValue.getModes()[index]) {
                                                if (index == modeValue.getModes().length - 1) {
                                                    modeValue.setValue(modeValue.getModes()[0]);
                                                    neverBreak = false;
                                                    break;
                                                } else {
                                                    modeValue.setValue(modeValue.getModes()[index + 1]);
                                                    neverBreak = false;
                                                    break;
                                                }
                                            }
                                        }

                                        if (neverBreak) {
                                            modeValue.setValue(modeValue.getModes()[0]);
                                        }
                                    }
                                } else {
                                    leftMouseClicked = false;
                                }
                            }

                            FontManager.bold13.drawString(modeValue.getName(), 5F, moduleHeight + 5F, -1);
                            FontManager.bold13.drawString(modeValue.getValue(),
                                    95F - FontManager.bold13.getStringWidth(modeValue.getValue()), moduleHeight + 5F,
                                    -1);
                        }

                        if (value instanceof ValueFloat) {
                            moduleHeight += 10F;
                            final ValueFloat floatValue = (ValueFloat) value;
                            RenderUtils.drawRect(3F, moduleHeight,
                                    95F * (floatValue.getValue() / floatValue.getMaximum()), 10F,
                                    ModuleHUD.maincolor.getValue());

                            FontManager.bold13.drawString(floatValue.getName(), 5F, moduleHeight + 5F, -1);
                            FontManager.bold13.drawCenteredString(FLOAT_POINT_FORMAT.format(floatValue.getValue()), 88F,
                                    moduleHeight + 5F, -1);

                            if (mouseHovered(x, y + moduleHeight + 2, 95F, 10F, mouseX, mouseY) && Mouse.isButtonDown(0))
                                floatValue.setValue(
                                        Float.valueOf(Math.max(floatValue.getMinimum(),
                                                Float.valueOf(
                                                        FLOAT_POINT_FORMAT.format(((((Math.max(0F, ((mouseX - x) / 95F))
                                                                * (floatValue.getMaximum()))))))))));

                        }

                        if (value instanceof ValueColor) {
                            moduleHeight += 10F;
                            final ValueColor colorValue = (ValueColor) value;
                            HSBData data = new HSBData(colorValue.getValue());
                            final float[] hsba = {
                                    data.getHue(),
                                    data.getSaturation(),
                                    data.getBrightness(),
                                    colorValue.getValue().getAlpha(),
                            };
                            RenderUtils.drawRoundedRect(88, moduleHeight + 1F, 9F, 9F, 3f,
                                    colorValue.getValue().getRGB());
                            FontManager.bold13.drawString(colorValue.getName(), 5f,
                                    5.5f + moduleHeight, 0xffffffff, false);

                            if (mouseHovered(x, y + moduleHeight + 2, 100F, 10F, mouseX, mouseY)) {
                                if (Mouse.isButtonDown(0)) {
                                    if (!leftMouseClicked) {
                                        colorValue.setExpanded(!colorValue.isExpanded());
                                        leftMouseClicked = true;
                                    }
                                } else {
                                    leftMouseClicked = false;
                                }
                            }

                            if (colorValue.isExpanded()) {

                                RenderUtils.drawRect(98 + 3, moduleHeight, 61, 61,
                                        new Color(0, 0, 0));
                                RenderUtils.drawRect(98 + 3.5F, 0.5F + moduleHeight, 60, 60,
                                        getColor(Color.getHSBColor(hsba[0], 1, 1)));
                                RenderUtils.drawHorizontalGradientSideways(98F + 3.5F,
                                        0.5F + moduleHeight, 60, 60, getColor(Color.getHSBColor(hsba[0], 0, 1)).getRGB(),
                                        0x00F);
                                RenderUtils.drawVerticalGradientSideways(98 + 3.5f,
                                0.5F + moduleHeight, 60, 60, 0x00F,
                                        getColor(Color.getHSBColor(hsba[0], 1, 0)).getRGB());

                                RenderUtils.drawRect(98 + 3.5f + hsba[1] * 60 - .5f,
                                0.5F + ((1 - hsba[2]) * 60) - .5f + moduleHeight, 1.5f, 1.5f,
                                        new Color(0, 0, 0));
                                RenderUtils.drawRect(98 + 3.5F + hsba[1] * 60,
                                0.5F + ((1 - hsba[2]) * 60) + moduleHeight, .5f, .5f, getColor(colorValue.getValue()));

                                final boolean onSB = RenderUtils.isHovering(x + 98 + 3, y + 0.5F + moduleHeight, 61, 61,
                                        mouseX, mouseY);

                                if (onSB && Mouse.isButtonDown(0)) {
                                    data.setSaturation(Math.min(Math.max((mouseX - (x + 98) - 3) / 60F, 0), 1));
                                    data.setBrightness(
                                            1 - Math.min(Math.max((mouseY - y - moduleHeight) / 60F, 0), 1));
                                    colorValue.setValue(data.getAsColor());

                                }

                                RenderUtils.drawRect(98 + 67, moduleHeight, 10, 61,
                                        new Color(0, 0, 0));

                                for (float f = 0F; f < 5F; f += 1F) {
                                    final Color lasCol = Color.getHSBColor(f / 5F, 1F, 1F);
                                    final Color tarCol = Color.getHSBColor(Math.min(f + 1F, 5F) / 5F, 1F, 1F);
                                    RenderUtils.drawVerticalGradientSideways(98 + 67.5F,
                                    0.5F + f * 12 + moduleHeight, 9, 12, getColor(lasCol).getRGB(),
                                            getColor(tarCol).getRGB());
                                }

                                RenderUtils.drawRect(98 + 67.5F, -1 + hsba[0] * 60 + moduleHeight, 9,
                                        2, new Color(0, 0, 0));
                                RenderUtils.drawRect(98 + 67.5F, -0.5f + hsba[0] * 60 + moduleHeight,
                                        9, 1, new Color(204, 198, 255));

                                final boolean onHue = RenderUtils.isHovering(x + 98 + 67,
                                        y + moduleHeight + 2, 10, 61, mouseX, mouseY);

                                if (onHue && Mouse.isButtonDown(0)) {
                                    data.setHue(Math.min(Math.max((mouseY - y - moduleHeight) / 60F, 0), 1));
                                    colorValue.setValue(data.getAsColor());
                                }
                            }
                        }

                        if (value instanceof ValueInt) {
                            moduleHeight += 10F;
                            final ValueInt intValue = (ValueInt) value;
                            RenderUtils.drawRoundedRect(3F, moduleHeight + 2,
                                    95F * ((float) intValue.getValue() / (float) intValue.getMaximum()), 10F,5,
                                    ModuleHUD.maincolor.getValue().getRGB());



                            FontManager.bold13.drawString(intValue.getName(), 5F, moduleHeight + 5F, -1);
                            FontManager.bold13.drawCenteredString(String.valueOf(intValue.getValue()), 90F,
                                    moduleHeight + 5F, -1);

                            if (mouseHovered(x, y + moduleHeight + 2, 95F, 10F, mouseX, mouseY) && Mouse.isButtonDown(0))
                                intValue.setValue(
                                        Integer.valueOf(Math.round(Math.max(intValue.getMinimum(),
                                                ((((Math.max(0, ((mouseX - x) / 95F)) * (intValue.getMaximum())))))))));

                        }

                        if (value instanceof ValueMultiBoolean) {
                            moduleHeight += 10F;
                            final ValueMultiBoolean multiBooleanValue = (ValueMultiBoolean) value;
                            if (multiBooleanValue.isExpanded()) {
                                RenderUtils.drawRect(3F, moduleHeight, 95F, 10F, new Color(17, 17, 17));
                            }
                            FontManager.bold13.drawCenteredString(multiBooleanValue.getName() + "...", 50F,
                                    moduleHeight + 5F, -1);
                            if (mouseHovered(x, y + moduleHeight + 2, 100, 10F, mouseX, mouseY)) {
                                if (Mouse.isButtonDown(0)) {
                                    if (!leftMouseClicked) {
                                        multiBooleanValue.setExpanded(!multiBooleanValue.isExpanded());
                                        leftMouseClicked = true;
                                    }
                                } else {
                                    leftMouseClicked = false;
                                }
                            }
                            if (multiBooleanValue.isExpanded()) {
                                RenderUtils.drawRect(3F, moduleHeight + 10F, 95F,
                                        multiBooleanValue.getValue().length * 10F, new Color(17, 17, 17));
                                for (final Pair<String, Boolean> pair : multiBooleanValue.getValue()) {
                                    moduleHeight += 10F;
                                    if (mouseHovered(x, y + moduleHeight + 2, 100F, 10F, mouseX, mouseY)) {
                                        if (Mouse.isButtonDown(0)) {
                                            if (!leftMouseClicked) {
                                                leftMouseClicked = true;
                                                multiBooleanValue.changeValue(pair.getKey(), !pair.getValue());
                                            }
                                        } else {
                                            leftMouseClicked = false;
                                        }
                                    }
                                    if (pair.getValue()) {
                                        RenderUtils.drawRect(3F, moduleHeight, 95F, 10F,
                                                ModuleHUD.maincolor.getValue());
                                    }
                                    FontManager.bold13.drawString(pair.getKey(), 5F, moduleHeight + 5F, -1);
                                }
                            }

                        }

                    }

                }
            }
            moduleHeight += 10F;
        }

        if (mouseHovered(x, y, 100F, 20F, mouseX, mouseY)) {
            if (Mouse.isButtonDown(1)) {
                if (!rightMouseClicked) {
                    rightMouseClicked = true;
                    expand = !expand;
                }
            } else {
                rightMouseClicked = false;
            }
        }
        FontManager.bold15.drawString(category.getDisplayName(), 5F, 5F, -1);

        GL11.glPopMatrix();
        if (runnable != null) {
            runnable.run();
        }
    }

    public Category getCategory() {
        return category;
    }

    private Color getColor(Color color) {
        return reAlpha(color, (int) (1f * color.getAlpha()));
    }

    private Color reAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        runnable = null;
    }
}
