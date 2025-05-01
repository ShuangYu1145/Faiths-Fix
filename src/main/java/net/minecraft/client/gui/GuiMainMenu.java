package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import dev.faiths.Faiths;
import dev.faiths.module.client.ModuleHUD;
import dev.faiths.ui.altmanager.GuiAltManager;
import dev.faiths.ui.font.FontManager;
import dev.faiths.ui.menu.AstolfoMenuButton;

import dev.faiths.ui.menu.GuiAbout;
import dev.faiths.utils.render.BlurUtil;
import dev.faiths.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;
import tech.skidonion.obfuscator.inline.Wrapper;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
    protected List<AstolfoMenuButton> buttons = Lists.<AstolfoMenuButton>newArrayList();
    private static final Random RANDOM = new Random();

    public static float progress;
    public static long startTime;

    /**
     * Counts the number of screen updates.
     */
    private float updateCounter;
    private AstolfoMenuButton altManagerButton;
    /**
     * Timer used to rotate the panorama, increases every tick.
     */
    private int panoramaTimer;

    /**
     * Texture allocated for the current viewport of the main menu's panorama
     * background.
     */
    private DynamicTexture viewportTexture;


    /**
     * An array of all the paths to the panorama pictures.
     */
    public static boolean initialized = false;

    public GuiMainMenu() {
        this.updateCounter = RANDOM.nextFloat();
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @NativeObfuscation
    public void updateScreen() {
        ++this.panoramaTimer;
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in
     * single-player
     */
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the
     * equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key),
     * keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when
     * the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        this.buttons.clear();
        this.viewportTexture = new DynamicTexture(256, 256);
        int j = this.height / 4 + 48;
        this.addSingleplayerMultiplayerButtons(j, 24);
    }

    /**
     * Adds Singleplayer and Multiplayer buttons on Main Menu for players who have
     * bought the game.
     */
    private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_) {
        this.buttons.add(new AstolfoMenuButton(1, this.width / 2 - 50, p_73969_1_,
                "SinglePlayer"));
        this.buttons.add(new AstolfoMenuButton(2, this.width / 2 - 50, p_73969_1_ + p_73969_2_ * 1,
                "Multiplayer"));
        this.buttons.add(this.altManagerButton = new AstolfoMenuButton(14, this.width / 2 - 50,
                p_73969_1_ + p_73969_2_ * 2, "AltManager"));
        this.buttons.add(new AstolfoMenuButton(3, this.width / 2 - 50, p_73969_1_ + p_73969_2_ * 3,
                "Options"));
        this.buttons.add(new AstolfoMenuButton(5, this.width / 2 - 50, p_73969_1_ + p_73969_2_ * 4,
                "About"));
        this.buttons.add(new AstolfoMenuButton(4, this.width / 2 - 50, p_73969_1_ + p_73969_2_ * 5,
                "Quit"));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for
     * buttons)
     */
    protected void actionPerformed(AstolfoMenuButton button) throws IOException {

        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 3) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }

        if (button.id == 4)
        {
            this.mc.shutdown();
        }

        if (button.id == 5)
        {
            this.mc.displayGuiScreen(new GuiAbout());
        }

        if (button.id == 14) {
            this.mc.displayGuiScreen(new GuiAltManager(this));
        }
    }


    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY,
     * renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRect(0, 0, Display.getWidth(), Display.getHeight(),new Color(0,0,0));
        if (progress < 1) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            float totalTimeInSeconds = 1f;
            progress = Math.min(1.0f, elapsedTime / (totalTimeInSeconds * 1000f));
            double trueAnim = 1 - Math.pow(1 - progress, 8);
            GlStateManager.translate((1 - trueAnim) * (this.width / 2D), (1 - trueAnim) * (this.height / 2D), 0D);
            GlStateManager.scale(trueAnim, trueAnim, trueAnim);
            RenderUtils.drawImage(
                    new ResourceLocation("client/furry.png"),
                    0,
                    0,
                    this.width,
                    this.height);
            BlurUtil.blurArea(0, 0, Display.getWidth(), Display.getHeight(), 10f);
        }

        if (!(progress < 1)) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            float totalTimeInSeconds = 1f;
            progress = Math.min(1.0f, elapsedTime / (totalTimeInSeconds * 1000f));
            double trueAnim = 1 - Math.pow(1 - progress, 8);
            GlStateManager.translate((1 - trueAnim) * (this.width / 2D), (1 - trueAnim) * (this.height / 2D), 0D);
            GlStateManager.scale(trueAnim, trueAnim, trueAnim);
            RenderUtils.drawImage(
                    new ResourceLocation("client/furry.png"),
                    0,
                    0,
                    this.width,
                    this.height);
            BlurUtil.blurArea(0, 0, Display.getWidth(), Display.getHeight(), 10f);
        }

        startProgress();

        String s = "Faiths Client #" + Faiths.VERSION;
        fontRendererObj.drawString(s, 5, this.height - 10,ModuleHUD.color(ModuleHUD.colortick.getValue()).getRGB());

        Optional<String> username = Wrapper.getUsername();
        if (username.isPresent())
        {
            String s2 = "Welcome, " + "Furry User" + "!";
            fontRendererObj.drawString(s2, this.width - fontRendererObj.getStringWidth(s2) - 2, this.height - 10, ModuleHUD.color(ModuleHUD.colortick.getValue()).getRGB());
        }


        for (int i2 = 0; i2 < this.buttons.size(); ++i2) {
            (this.buttons.get(i2)).drawButton(this.mc, mouseX, mouseY);
        }

        FontManager.p100.drawStringDynamicWithShadow("Faiths",this.width / 2F - 110F,this.height / 4f - 40F,1,50);


//        RenderUtils.drawImage(
//                new ResourceLocation("client/icon/logo.png"),
//                this.width / 2F - 30F,
//                this.height / 4f - 40F,
//                64F,
//                64F,
//                ModuleHUD.color.getValue().getRGB());


        super.drawScreen(mouseX, mouseY, partialTicks);

    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        if (mouseButton == 0) {
            for (int i = 0; i < this.buttons.size(); ++i) {
                AstolfoMenuButton guibutton = this.buttons.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    this.actionPerformed(guibutton);
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
        this.buttons.clear();

    }

    //界面动画
    public void startProgress() {
        if (progress < 1) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            float totalTimeInSeconds = 5000f;
            progress = Math.min(1.0f, elapsedTime / (totalTimeInSeconds * 0.2f));
            double trueAnim = 1 - Math.pow(1 - progress, 4);
            GlStateManager.translate((1 - trueAnim) * ((double) width), (1 - trueAnim) * ((double) height / 1000),  trueAnim);
        }
    }

    public static void restProgress() {
        progress = 0;
        startTime = System.currentTimeMillis();
    }
}
