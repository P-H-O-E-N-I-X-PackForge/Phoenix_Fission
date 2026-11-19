package net.phoenix.phoenix_fission.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/** Immediate-mode drawing surface in local coordinates (origin at the widget's top-left corner). */
public interface CanvasPainter {

    void render(GuiGraphics g, int w, int h, int mouseX, int mouseY);

    default boolean click(double mouseX, double mouseY, int button) {
        return false;
    }

    default void drag(double mouseX, double mouseY, int button) {}

    default void release(int button) {}

    default boolean scroll(double mouseX, double mouseY, double amount) {
        return false;
    }
}
