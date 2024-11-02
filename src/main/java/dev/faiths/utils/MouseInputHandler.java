package dev.faiths.utils;

import java.util.ArrayList;
import java.util.List;

public class MouseInputHandler {
    public interface MouseCallback {
        void handleMouseInput(int mouseX, int mouseY);
    }
    private static final List<MouseCallback> mouseCallbacks = new ArrayList<>();
    
    public static void handleMouseInput(final int mouseX, final int mouseY) {
        mouseCallbacks.forEach(mouseCallback -> mouseCallback.handleMouseInput(mouseX, mouseY));
        mouseCallbacks.clear();
    }

    public static void addMouseCallback(final MouseCallback mouseCallback) {
        mouseCallbacks.add(mouseCallback);
    }
}
