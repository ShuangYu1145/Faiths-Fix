package dev.faiths.ui.altmanager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import dev.faiths.Faiths;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.ClientUtils;
import dev.faiths.utils.MiscUtils;
import dev.faiths.utils.elixir.account.MicrosoftAccount;
import dev.faiths.utils.elixir.account.MicrosoftAccount.AuthMethod;
import dev.faiths.utils.elixir.compat.OAuthHandler;
import dev.faiths.utils.elixir.compat.OAuthServer;
import dev.faiths.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class GuiAddAlt extends GuiScreen {
    private GuiAltManager prevScreen;
    private CustomGuiTextField nameField;
    private AltManagerButton addButton;
    private AltManagerButton microsoftButton;
    private AltManagerButton cancelButton;
    private OAuthServer oAuthServer;
    

    public GuiAddAlt(final GuiAltManager prevScreen) {
        this.prevScreen = prevScreen;
    }

    @Override
    public void initGui() {
        // final ScaledResolution scaledResolution = new ScaledResolution(mc);
        // final int width = scaledResolution.getScaledWidth();
        // final int height = scaledResolution.getScaledHeight();
        this.addButton = new AltManagerButton(0, width / 2 - 125, height / 2 + 25, 120, 20, "Add");
        this.microsoftButton = new AltManagerButton(1, width / 2 + 5, height / 2 + 25, 120, 20, "Microsoft");
        this.cancelButton = new AltManagerButton(2, width / 2 - 125, height / 2 + 50, 250, 20, "Cancel");
        this.nameField = new CustomGuiTextField(0, mc.fontRendererObj, width / 2 - 125, height / 2 - 20, 250, 20,
                "Name");
        this.buttonList.add(this.addButton);
        this.buttonList.add(this.microsoftButton);
        this.buttonList.add(this.cancelButton);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                Faiths.configManager.accountsConfig.addCrackedAccount(this.nameField.getText());
                this.mc.displayGuiScreen(this.prevScreen);
                break;
            case 1:
                try {
                    oAuthServer = MicrosoftAccount.buildFromOpenBrowser(new OAuthHandler() {

                        @Override
                        public void openUrl(String url) {
                            Toolkit toolkit = Toolkit.getDefaultToolkit();
                            Clipboard clipboard = toolkit.getSystemClipboard();
                            StringSelection strSel = new StringSelection(url);
                            clipboard.setContents(strSel, null);
                            try {
                                Desktop.getDesktop().browse(new URI(url));
                            } catch (URISyntaxException | IOException e) {
                                throw new RuntimeException(e);
                            }

                            Faiths.notificationManager.pop("Microsoft",
                                    "啾啾啾",NotificationType.SUCCESS);

                        }


                        @Override
                        public void authResult(MicrosoftAccount account) {
                            if (!Faiths.configManager.accountsConfig.accountExists(account)) {
                                Faiths.configManager.accountsConfig.addAccount(account);
                                Faiths.configManager
                                        .saveConfig(Faiths.configManager.accountsConfig);
                                mc.displayGuiScreen(prevScreen);
                                Faiths.notificationManager.pop("Microsoft",
                                        "Successfully added Microsoft account", NotificationType.SUCCESS);
                            } else {
                                Faiths.notificationManager.pop("Microsoft", "Account already exists",
                                        NotificationType.ERROR);
                            }
                            oAuthServer.stop(false);
                        }

                        @Override
                        public void authError(String error) {
                            Faiths.notificationManager.pop("Microsoft",
                                    "Error adding Microsoft account: " + error, NotificationType.ERROR);
                            oAuthServer.stop(false);
                        }
                    }, AuthMethod.AZURE_APP);
                } catch (Exception e) {
                    ClientUtils.LOGGER.error(e);
                }
                break;
            case 2:
                this.mc.displayGuiScreen(this.prevScreen);
                break;
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.nameField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRect(0F, 0F, mc.displayWidth, mc.displayHeight, new Color(24, 22, 20));
        this.nameField.drawTextBox();
        drawCenteredString(mc.fontRendererObj, "Add Alt", this.width / 2, 25, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
