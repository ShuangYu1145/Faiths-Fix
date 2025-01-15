package dev.faiths.ui.notifiction;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import dev.faiths.ui.font.CustomFont;
import dev.faiths.ui.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.MathHelper;

import static dev.faiths.utils.IMinecraft.mc;

public final class NotificationManager {

    private final int DEFAULT_DELAY = 2_000;
    private final CustomFont fontRendererObj = FontManager.bold18;
    /* fields */
    private final List<Notification> NOTIFICATIONS = new CopyOnWriteArrayList<>();

    /* methods */
    public void update() {
        for (Notification notification : NOTIFICATIONS) {
            int i = NOTIFICATIONS.indexOf(notification) * 37;

            if (notification.getY() < 50 + i) notification.setY(MathHelper.clamp_double(notification.getY() + 0.5 * ((double) 2000 / Minecraft.getDebugFPS()),0,50 + i));
            if (notification.getY() > 50 + i) notification.setY(MathHelper.clamp_double(notification.getY() - 0.25 * ((double) 2000 / Minecraft.getDebugFPS()),50 + i,99999));

            String seconds = notification.getDelay() / 1000 + "";
            String s = " (" + seconds.substring(0, seconds.indexOf(".") + 2) + "s) ";
            if (notification.isExtending() && notification.getX() < Math.max(fontRendererObj.getStringWidth(notification.getMessage() + s), fontRendererObj.getStringWidth(notification.getCallReason())) + 16) {
                notification.setX(MathHelper.clamp_double(notification.getX() + 0.25 * ((double) 2000 / Minecraft.getDebugFPS()),0,Math.max(fontRendererObj.getStringWidth(notification.getMessage() + s), fontRendererObj.getStringWidth(notification.getCallReason())) + 16));
                notification.getTimer().reset();
            } else {
                notification.setExtending(false);
            }

            if (!notification.isExtending() && notification.getTimer().delay(notification.getDelay() + 150) && notification.getX() > 0) {
                notification.setX(notification.getX() - 0.5 * ((double) 2000 / Minecraft.getDebugFPS()));
            }

            if (notification.getX() <= 0) remove(notification);
        }
    }

    public void pop(String message, int delay, NotificationType type) {
        Notification notification = new Notification(message, delay, type != null ? type : NotificationType.SUCCESS);

        for (Notification prevNotification : NOTIFICATIONS) {
            if (notification.getMessage().equalsIgnoreCase(prevNotification.getMessage())) {
                prevNotification.getTimer().reset();
                return;
            }
        }

        notification.setExtending(true);
        notification.getTimer().reset();

        add(notification);
    }

    public void pop(String callReason, String message, int delay, NotificationType type) {
        Notification notification = new Notification(callReason, message, delay, type != null ? type : NotificationType.SUCCESS);

        for (Notification prevNotification : NOTIFICATIONS) {
            if (notification.getMessage().equalsIgnoreCase(prevNotification.getMessage())) {
                prevNotification.getTimer().reset();
                return;
            }
        }

        notification.setExtending(true);
        notification.getTimer().reset();

        add(notification);
    }

    public void pop(String message, NotificationType type) {
        pop(message, DEFAULT_DELAY, type);
    }

    public void pop(String callReason, String message, NotificationType type) {
        pop(callReason, message, DEFAULT_DELAY, type);
    }

    public void add(Notification notification) {
        notification.setExtending(true);
        notification.getTimer().reset();

        NOTIFICATIONS.add(notification);
    }

    public void remove(Notification notification) {
        NOTIFICATIONS.remove(notification);
    }

    public List<Notification> getNotifications() {
        return NOTIFICATIONS;
    }

}