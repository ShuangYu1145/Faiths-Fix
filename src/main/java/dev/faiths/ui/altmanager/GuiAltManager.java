package dev.faiths.ui.altmanager;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import dev.faiths.Faiths;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.elixir.account.MicrosoftAccount;
import dev.faiths.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class GuiAltManager extends GuiScreen implements GuiYesNoCallback
{
    private static final Logger logger = LogManager.getLogger();
    private GuiScreen parentScreen;
    private GuiList altsList;
    private boolean initialized;
    private CustomGuiTextField searchField;
    protected AltManagerButton loginButton;

    public GuiAltManager(GuiScreen parentScreen)
    {
        this.parentScreen = parentScreen;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        if (!this.initialized)
        {
            this.initialized = true;
            searchField = new CustomGuiTextField(0, mc.fontRendererObj, this.width - 130, 10, 100, 13, "Search...");
            altsList = new GuiList(this, searchField);
            altsList.setDimensions(this.width, this.height, 32, this.height - 64);
        }
        else
        {
            searchField.xPosition = this.width - 130;
            altsList.setDimensions(this.width, this.height, 32, this.height - 64);
        }

        this.createButtons();
    }

    /**
     * Handles mouse input.
     */
    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        this.altsList.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.searchField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        this.searchField.updateCursorCounter();
    }

    public void createButtons()
    {
        this.buttonList.add(loginButton = new AltManagerButton(1, this.width / 2 - 125, this.height - 52, 120, 20, "Login"));
        this.buttonList.add(new AltManagerButton(2, this.width / 2 + 5, this.height - 52, 120, 20, "Add"));
        this.buttonList.add(new AltManagerButton(999, this.width / 2 + 135, this.height - 52, 120, 20, "Token"));
        this.buttonList.add(new AltManagerButton(998, this.width / 2 + 135, this.height - 28, 120, 20, "Change Skin"));
        this.buttonList.add(new AltManagerButton(997, this.width / 2 - 255, this.height - 28, 120, 20, "Change Name"));
        this.buttonList.add(new AltManagerButton(996, this.width / 2 - 255, this.height - 52, 120, 20, "Buy Alt (CN)"));
        this.buttonList.add(new AltManagerButton(3, this.width / 2 - 125, this.height - 28, 80, 20, "Edit"));
        this.buttonList.add(new AltManagerButton(4, this.width / 2 - 40, this.height - 28, 80, 20, "Delete"));
        this.buttonList.add(new AltManagerButton(0, this.width / 2 + 45, this.height - 28, 80, 20, "Done"));
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(parentScreen);
                break;
            case 1:
                loginButton.enabled = false;
                altsList.login();
                break;
            case 2:
                mc.displayGuiScreen(new GuiAddAlt(this));
                break;
            case 3:
                mc.displayGuiScreen(new GuiEdit(this, altsList.getSelectedAccount()));
                break;
            case 4:
                Faiths.configManager.accountsConfig.removeAccount(altsList.getSelectedAccount());
                break;
            case 999:
                try {
                    JFrame jf = new JFrame();
                    jf.setAlwaysOnTop(true);
                    String token = JOptionPane.showInputDialog(jf, "Refresh Token?");
                    if (token == null) {
                        break;
                    }
                    jf.dispose();
                    Faiths.configManager.accountsConfig.addAccount(MicrosoftAccount.buildFromRefreshToken(token, MicrosoftAccount.AuthMethod.TOKENLOGIN));
                    Faiths.configManager.accountsConfig.save();
                    Faiths.notificationManager.pop("Alt Manager", "Done", NotificationType.SUCCESS);
                } catch (Exception e) {
                    Faiths.notificationManager.pop("Alt Manager", "Failed login with token", NotificationType.ERROR);
                    e.printStackTrace();
                }
                break;
            case 998:
                try {
                    JFileChooser jFileChooser = new JFileChooser() {
                        @Override
                        protected JDialog createDialog(Component parent) throws HeadlessException {
                            // intercept the dialog created by JFileChooser
                            JDialog dialog = super.createDialog(parent);
                            dialog.setModal(true);
                            dialog.setAlwaysOnTop(true);
                            return dialog;
                        }
                    };
                    int returnVal = jFileChooser.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File skinFile = jFileChooser.getSelectedFile();
                        String url = "https://api.minecraftservices.com/minecraft/profile/skins";
                        Map<String, String> headers = new HashMap<>();

                        if (!skinFile.getName().endsWith(".png")) {
                            Faiths.notificationManager.pop("Its seems that the file isn't a skin..", NotificationType.ERROR);
                            break;
                        }

                        // Skin file
                        int result = JOptionPane.showConfirmDialog((Component) null, "Is this a slim skin?", "alert", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (result == JOptionPane.CANCEL_OPTION) break;
                        String skinType;
                        if (result == JOptionPane.YES_OPTION) {
                            skinType = "slim";
                        } else {
                            skinType = "classic";
                        }

                        headers.put("Accept", "*/*");
                        headers.put("Authorization", "Bearer " + mc.getSession().getToken());
                        headers.put("User-Agent", "MojangSharp/0.1");

                        HttpResponse response = HttpRequest.post(url)
                                .headerMap(headers, true)
                                .form("variant", skinType)
                                .form("file", skinFile)
                                .execute();
                        if (response.getStatus() == 200 || response.getStatus() == 204) {
                            Faiths.notificationManager.pop("Skin changed!", NotificationType.SUCCESS);
                        } else {
                            Faiths.notificationManager.pop("Failed to change skin.", NotificationType.ERROR);
                            logger.error(response);
                        }
                    }
                } catch (Exception e) {
                    Faiths.notificationManager.pop("Failed to change skin.", NotificationType.ERROR);
                    e.printStackTrace();
                }
                break;
            case 997:
                try {
                    JFrame jf = new JFrame();
                    jf.setAlwaysOnTop(true);
                    String name = JOptionPane.showInputDialog(jf, "Name?");
                    if (name == null) {
                        break;
                    }
                    jf.dispose();
                    String url = "https://api.minecraftservices.com/minecraft/profile/name/" + name;
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Accept", "*/*");
                    headers.put("Authorization", "Bearer " + mc.getSession().getToken());
                    headers.put("User-Agent", "MojangSharp/0.1");
                    headers.put("Content-Type", "application/json");

                    HttpResponse response = HttpRequest.put(url)
                            .headerMap(headers, true)
                            .execute();
                    if (response.getStatus() == 200 || response.getStatus() == 204) {
                        Faiths.notificationManager.pop("Name changed!", NotificationType.SUCCESS);
                        mc.setSession(new Session(name, mc.getSession().getPlayerID(), mc.getSession().getToken(), mc.getSession().getSessionType().name()));
                    } else {
                        String cause = "Unknown";
                        switch (response.getStatus()) {
                            case 400:
                                cause = "Name is invaild";
                                break;
                            case 403:
                                cause = "Name is unlivable";
                                break;
                            case 401:
                                cause = "Unauthorized";
                                break;
                            case 429:
                                cause = "Too many requests";
                                break;
                            case 500:
                                cause = "Mojang API lags";
                                break;
                        }
                        Faiths.notificationManager.pop("Failed to change name due to " + cause, NotificationType.ERROR);
                        logger.error(response);
                    }
                } catch (Exception e) {
                    Faiths.notificationManager.pop("Failed to change name.", NotificationType.ERROR);
                    e.printStackTrace();
                }
                break;
            case 996:
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(URI.create("https://shop.lishangmc.com/"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
//        this.drawDefaultBackground();
        this.altsList.drawScreen(mouseX, mouseY, partialTicks);
        this.searchField.drawTextBox();
        RenderUtils.drawImage(new ResourceLocation("client/icon/altening.png"), this.width - 20, 5, 16, 16);
        this.drawCenteredString(this.fontRendererObj, "Alt Manager", this.width / 2, 10, 16777215);
        this.drawString(this.fontRendererObj, String.format("§7Username: §3%s", mc.getSession().getUsername()), 2, 5, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
