package net.phoenix.phoenix_fission.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Immediate-mode drawing surface used by the fission screens. Everything is drawn in local coordinates with the
 * origin at the widget's top-left corner (the pose stack is already translated), so the old absolute-coordinate
 * drawing code ports over unchanged.
 */
public interface CanvasPainter {

    void render(GuiGraphics g, int w, int h, int mouseX, int mouseY);

    default boolean click(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean scroll(double mouseX, double mouseY, double amount) {
        return false;
    }
}
