package net.phoenix.phoenix_fission.client.gui;

import net.minecraft.client.gui.GuiGraphics;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.SlotGroupWidget;

/** Shared panel chrome for the fission screens: dark HUD background and the tab column outside the left edge. */
public final class FissionPanelUi {

    private FissionPanelUi() {}

    private static final int BG_TOP = 0xFF_02101A;
    private static final int BG_BOT = 0xFF_010508;
    private static final int C_TEAL = 0xFF_00B8A5;

    /** Draws the whole panel (screen area and inventory) in the HUD style. */
    public static final IDrawable BACKGROUND = (ctx, x, y, w, h, theme) -> {
        GuiGraphics g = ctx.getGraphics();
        GuiDraw.drawGradientRect(g, x, y, w, h, BG_TOP, BG_BOT, false);
        for (int gx = x + 4; gx < x + w; gx += 8) g.fill(gx, y, gx + 1, y + h, 0x0C_00E5CC);
        for (int gy = y + 4; gy < y + h; gy += 8) g.fill(x, gy, x + w, gy + 1, 0x0C_00E5CC);
        GuiDraw.drawBorder(g, x, y, w, h, C_TEAL, 1);
        GuiDraw.drawBorder(g, x + 2, y + 2, w - 4, h - 4, 0x22_00E5CC, 1);
    };

    public static void apply(ModularPanel<?> panel, FissionTabbedScreen screen) {
        panel.background(BACKGROUND);
        var strip = new CanvasWidget(screen.stripPainter());
        strip.size(FissionTabbedScreen.TAB_W, screen.stripHeight()).leftRel(1f).top(8);
        strip.excludeAreaInRecipeViewer();
        panel.child(strip);
    }

    private static final IDrawable SLOT = (ctx, x, y, w, h, theme) -> {
        GuiGraphics g = ctx.getGraphics();
        g.fill(x, y, x + w, y + h, 0xAA_010A12);
        GuiDraw.drawBorder(g, x, y, w, h, 0x55_00E5CC, 1);
    };

    private static final IDrawable SECTION = (ctx, x, y, w, h, theme) -> {
        GuiGraphics g = ctx.getGraphics();
        g.fill(x, y, x + w, y + h, 0xCC_010A12);
        GuiDraw.drawBorder(g, x, y, w, h, 0x88_00B8A5, 1);
        g.fill(x, y, x + 3, y + 8, 0xFF_00B8A5);
    };

    /**
     * A separate inventory section below the screen area (which ends at {@code top}), with dark HUD-style cells.
     * Add on both sides so the slots exist on the server.
     */
    public static void addInventory(ParentWidget<?> main, int width, int top) {
        main.child(new Widget<>().pos(0, top + 3).size(width, 92).background(SECTION));
        main.child(SlotGroupWidget.playerInventory((index, slot) -> {
            slot.background(SLOT);
            return slot;
        }).top(top + 11).leftRel(0.5f));
    }
}
