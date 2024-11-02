package dev.faiths.ui.altmanager;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

import dev.faiths.Faiths;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.ClientUtils;
import dev.faiths.utils.MiscUtils;
import dev.faiths.utils.elixir.account.CrackedAccount;
import dev.faiths.utils.elixir.account.MicrosoftAccount;
import dev.faiths.utils.elixir.account.MicrosoftAccount.AuthMethod;
import dev.faiths.utils.elixir.account.MinecraftAccount;
import dev.faiths.utils.elixir.compat.OAuthHandler;
import dev.faiths.utils.elixir.compat.OAuthServer;
import dev.faiths.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiEdit extends GuiScreen {
    private final GuiAltManager prevScreen;
    private final MinecraftAccount selectedAccount;
    private CustomGuiTextField nameField;
    private AltManagerButton okButton;
    private AltManagerButton cancelButton;
    private OAuthServer oAuthServer;

    public GuiEdit(final GuiAltManager prevScreen, final MinecraftAccount selectedAccount) {
        this.prevScreen = prevScreen;
        this.selectedAccount = selectedAccount;
    }

    @Override
    public void initGui() {
        this.okButton = new AltManagerButton(0, width / 2 - 125, height / 2 + 25, 250, 20, "OK");
        this.cancelButton = new AltManagerButton(1, width / 2 - 125, height / 2 + 50, 250, 20, "Cancel");
        this.nameField = new CustomGuiTextField(0, mc.fontRendererObj, width / 2 - 125, height / 2 - 20, 250, 20,
                "Name");
        this.nameField.setText(selectedAccount.getName());
        this.buttonList.add(this.okButton);
        this.buttonList.add(this.cancelButton);
    }

    @Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.nameField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
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
                if (selectedAccount instanceof CrackedAccount) {
                    selectedAccount.setName(this.nameField.getText());
                    Faiths.notificationManager.pop("Cracked",
                    "Successfully edited Cracked account", NotificationType.SUCCESS);
                } else {
                    try {
                        oAuthServer = MicrosoftAccount.buildFromOpenBrowser(new OAuthHandler() {
                            @Override
                            public void openUrl(String url) {
                                Toolkit toolkit = Toolkit.getDefaultToolkit();
                                Clipboard clipboard = toolkit.getSystemClipboard();
                                StringSelection strSel = new StringSelection(url);
                                clipboard.setContents(strSel, null);
                                Faiths.notificationManager.pop("Microsoft",
                                        "Login url copied. Open your browser and paste it.", NotificationType.SUCCESS);
                            }

                            @Override
                            public void authResult(MicrosoftAccount account) {
                                if (!Faiths.configManager.accountsConfig.accountExists(account)) {
                                    Faiths.configManager.accountsConfig.removeAccount(selectedAccount);
                                    Faiths.configManager.accountsConfig.addAccount(account);
                                    Faiths.configManager
                                            .saveConfig(Faiths.configManager.accountsConfig);
                                    mc.displayGuiScreen(prevScreen);
                                    Faiths.notificationManager.pop("Microsoft",
                                            "Successfully edited Microsoft account", NotificationType.SUCCESS);
                                } else {
                                    Faiths.notificationManager.pop("Microsoft", "Account already exists",
                                            NotificationType.ERROR);
                                }
                                oAuthServer.stop(false);
                            }

                            @Override
                            public void authError(String error) {
                                Faiths.notificationManager.pop("Microsoft",
                                        "Error editing Microsoft account: " + error, NotificationType.ERROR);
                                oAuthServer.stop(false);
                            }
                        }, AuthMethod.AZURE_APP);
                    } catch (Exception e) {
                        ClientUtils.LOGGER.error(e);
                    }
                }
                break;
            case 1:
                this.mc.displayGuiScreen(this.prevScreen);
                break;
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRect(0F, 0F, mc.displayWidth, mc.displayHeight, new Color(24, 22, 20));
        this.nameField.drawTextBox();
        drawCenteredString(mc.fontRendererObj, "Edit Alt", this.width / 2, 25, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
