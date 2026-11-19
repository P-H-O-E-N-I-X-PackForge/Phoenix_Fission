package net.phoenix.phoenix_fission.client.gui;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import net.minecraft.client.gui.GuiGraphics;

/** Small drawing helpers shared by the canvas painters. */
public final class GuiDraw {

    private GuiDraw() {}

    public static void drawGradientRect(GuiGraphics g, int x, int y, int w, int h, int topColor, int bottomColor,
                                        boolean horizontal) {
        DrawerHelper.drawGradientRect(g, x, y, w, h, topColor, bottomColor, horizontal);
    }

    public static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color, int thickness) {
        DrawerHelper.drawBorder(g, x, y, w, h, color, thickness);
    }

    /** Scissors in absolute GUI coordinates, so the current translation is applied to the rectangle itself. */
    public static void enableScissor(GuiGraphics g, int x1, int y1, int x2, int y2) {
        var m = g.pose().last().pose();
        int ax = Math.round(m.m00() * x1 + m.m30()), ay = Math.round(m.m11() * y1 + m.m31());
        int bx = Math.round(m.m00() * x2 + m.m30()), by = Math.round(m.m11() * y2 + m.m31());
        g.enableScissor(ax, ay, bx, by);
    }
}
