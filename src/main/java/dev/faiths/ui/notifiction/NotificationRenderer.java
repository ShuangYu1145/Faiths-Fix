package dev.faiths.ui.notifiction;

import dev.faiths.Faiths;
import dev.faiths.module.client.ModuleHUD;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.utils.render.shader.ShaderElement;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.awt.*;

public final class NotificationRenderer {

    private static final int RED = new Color(255, 80, 80).getRGB();
    private static final int GREEN = new Color(100,198,119).getRGB();
    private static final int ORANGE = new Color(255, 215, 100).getRGB();
    private static final int WHITE = new Color(255, 255, 255).getRGB();
    private static final NotificationManager notificationManager = Faiths.notificationManager;

    private static int displayHeight = 0, displayWidth = 0;

    public static void draw(ScaledResolution resolution) {
        if (!notificationManager.getNotifications().isEmpty()) {
            notificationManager.update();
        }

        for (Notification notification : notificationManager.getNotifications()) {

            double x = notification.getX();
            double y = resolution.getScaledHeight() - notification.getY();

            // region notification-rendering
            String callReason = notification.getCallReason() == null ? StringUtils.capitalize(notification.getType().toString()) :
                    notification.getCallReason();
            String message = notification.getMessage();

            Gui.drawRect3(
                    resolution.getScaledWidth() - x - (notification.getNotificationType() == NotificationType.WARNING || notification.getNotificationType() == NotificationType.SUCCESS || notification.getNotificationType() == NotificationType.INFO ? 2 : 0),
                    y,
                    resolution.getScaledWidth(),
                    24,
                    new Color(0, 0, 0, ModuleHUD.globalalpha.getValue()).getRGB());


            ShaderElement.addBlurTask(() -> Gui.drawRect3(
                    resolution.getScaledWidth() - x - (notification.getNotificationType() == NotificationType.WARNING || notification.getNotificationType() == NotificationType.SUCCESS || notification.getNotificationType() == NotificationType.INFO ? 2 : 0),
                    y,
                    resolution.getScaledWidth(),
                    24,
                    new Color(0, 0, 0, 255).getRGB()));

            ShaderElement.addBloomTask(() -> Gui.drawRect3(
                    resolution.getScaledWidth() - x - (notification.getNotificationType() == NotificationType.WARNING || notification.getNotificationType() == NotificationType.SUCCESS || notification.getNotificationType() == NotificationType.INFO ? 2 : 0),
                    y,
                    resolution.getScaledWidth(),
                    24,
                    new Color(0, 0, 0,255).getRGB()));

//            FontManager.sf19.drawString(callReason,resolution.getScaledWidth() - (float)x + 25,(float) y + 4F,Color.WHITE.getRGB(),false);
//            FontManager.sf18.drawString(message + " ",resolution.getScaledWidth() - (float)x + 25,(float)y + 12.5F,Color.GRAY.getRGB(),false);

            FontManager.bold19.drawStringWithShadow(callReason,resolution.getScaledWidth() - (float)x + 25,(float) y + 4F,Color.WHITE.getRGB());
            FontManager.sf18.drawStringWithShadow(message + " ",resolution.getScaledWidth() - (float)x + 25,(float)y + 12.5F,Color.GRAY.getRGB());
            //endregion

            //region icon-rendering
            switch (notification.getType()) {
                case ERROR:
                    RenderUtils.drawImage(new ResourceLocation("client/notifications/DANGER.png"), (float) (resolution.getScaledWidth() - x + 3.5), (float) y + 3, 16, 16, new Color(255, 80, 80));
                    break;
                case WARNING:
                    RenderUtils.drawImage(new ResourceLocation("client/notifications/WARNING.png"), (float) (resolution.getScaledWidth() - x + 3.5), (float) y + 3, 16, 16, new Color(255, 215, 100));
                    break;
                case SUCCESS:
                    RenderUtils.drawImage(new ResourceLocation("client/notifications/SUCCESS.png"), (float) (resolution.getScaledWidth() - x + 3.5), (float) y + 3, 16, 16, new Color(100,198,119));
                    break;
                case INFO:
                    RenderUtils.drawImage(new ResourceLocation("client/notifications/INFO.png"), (float) (resolution.getScaledWidth() - x + 3.5), (float) y + 3, 16, 16, new Color(255, 255, 255));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + notification.getType());
            }
            //endregion

            //region timebar-rendering
            double width = notification.getX();
            double w1 = width/100;
            double a = (double) notification.getDelay() /100;
            double perc = (float) (notification.getCount()/a);

            Gui.drawRect(
                    resolution.getScaledWidth() - x -
                            (notification.getNotificationType() == NotificationType.WARNING || notification.getNotificationType() == NotificationType.SUCCESS || notification.getNotificationType() == NotificationType.INFO ? 2 : 0),
                    y + 22,
                    resolution.getScaledWidth() - x + (perc * w1),
                    y + 24,
                    getColorForType(notification.getType()));
            //endregion
        }

    }


    private static int getColorForType(NotificationType type) {
        switch (type) { // @off
            case SUCCESS:
                return GREEN;
            case ERROR:
                return RED;
            case WARNING:
                return ORANGE;
            case INFO:
                return WHITE;
        } // @on

        return 0;
    }
}