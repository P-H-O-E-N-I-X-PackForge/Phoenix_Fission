package net.phoenix.phoenix_fission.client.gui;

import net.minecraft.client.gui.GuiGraphics;

/** Small drawing helpers the old LDLib DrawerHelper used to provide. */
public final class GuiDraw {

    private GuiDraw() {}

    public static void drawGradientRect(GuiGraphics g, int x, int y, int w, int h, int topColor, int bottomColor,
                                        boolean horizontal) {
        if (!horizontal) {
            g.fillGradient(x, y, x + w, y + h, topColor, bottomColor);
            return;
        }
        for (int i = 0; i < w; i++) {
            float t = w <= 1 ? 0 : i / (float) (w - 1);
            g.fill(x + i, y, x + i + 1, y + h, lerpArgb(topColor, bottomColor, t));
        }
    }

    public static void drawGradientRect(GuiGraphics g, float x, float y, float w, float h, int topColor,
                                        int bottomColor, boolean horizontal) {
        drawGradientRect(g, Math.round(x), Math.round(y), Math.round(w), Math.round(h), topColor, bottomColor,
                horizontal);
    }

    public static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color, int thickness) {
        g.fill(x, y, x + w, y + thickness, color);
        g.fill(x, y + h - thickness, x + w, y + h, color);
        g.fill(x, y + thickness, x + thickness, y + h - thickness, color);
        g.fill(x + w - thickness, y + thickness, x + w, y + h - thickness, color);
    }

    /**
     * Vanilla 1.21.1 scissors in absolute GUI coordinates and ignores the pose stack, so local drawing code has to
     * push the current translation/scale into the rectangle itself.
     */
    public static void enableScissor(GuiGraphics g, int x1, int y1, int x2, int y2) {
        var m = g.pose().last().pose();
        int ax = Math.round(m.m00() * x1 + m.m30()), ay = Math.round(m.m11() * y1 + m.m31());
        int bx = Math.round(m.m00() * x2 + m.m30()), by = Math.round(m.m11() * y2 + m.m31());
        g.enableScissor(ax, ay, bx, by);
    }

    private static int lerpArgb(int a, int b, float t) {
        int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int ra = (int) (aa + (ba - aa) * t), rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t), rb = (int) (ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }
}
