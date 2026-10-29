package net.phoenix.phoenix_fission.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.RtgArrayMachine;

/** Output and average cell life over the last minute. */
@OnlyIn(Dist.CLIENT)
public class RtgGraphsPage implements CanvasPainter {

    private static final int BG_TOP = 0xFF_02101A;
    private static final int BG_BOT = 0xFF_010508;
    private static final int C_TEAL = 0xFF_00B8A5;
    private static final int C_CYAN = 0xFF_00E5CC;
    private static final int C_MID = 0xFF_7FA3AE;
    private static final int C_DIM = 0xFF_3A5E6A;
    private static final int C_WHITE = 0xFF_DCF0F4;
    private static final int C_GREEN = 0xFF_33FF88;
    private static final int C_GOLD = 0xFF_FFC040;

    private final RtgArrayMachine machine;
    private final RtgHistory history;

    public RtgGraphsPage(RtgArrayMachine machine, RtgHistory history) {
        this.machine = machine;
        this.history = history;
    }

    @Override
    public void render(GuiGraphics g, int w, int h, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        history.sampleIfDue(machine);

        GuiDraw.drawGradientRect(g, 0, 0, w, h, BG_TOP, BG_BOT, false);
        g.fill(0, 0, w, 16, BG_TOP);
        g.fill(3, 3, 6, 13, C_TEAL);
        g.drawString(font, "RTG GRAPHS", 10, 4, C_CYAN, false);
        g.fill(0, 15, w, 16, 0x44_00E5CC);

        int x = 8, cw = w - 16;
        int plotH = (h - 16 - 30 - 20) / 2;
        int y = 24;
        double peak = 1;
        for (int i = 0; i < history.filled; i++) peak = Math.max(peak, history.output[history.index(i)]);
        plot(g, font, x, y, cw, plotH, history.output, peak * 1.1, C_GOLD, "OUTPUT EU/t",
                (long) machine.getGeneratedEUt() + " EU/t");
        y += plotH + 22;
        plot(g, font, x, y, cw, plotH, history.life, 1.0, C_GREEN, "AVERAGE CELL LIFE",
                (int) Math.round(latest(history.life) * 100) + "%");

        GuiDraw.drawBorder(g, 0, 0, w, h, C_TEAL, 1);
    }

    private double latest(double[] data) {
        return history.filled == 0 ? 0 : data[history.index(history.filled - 1)];
    }

    private void plot(GuiGraphics g, Font font, int x, int y, int w, int h, double[] data, double max, int color,
                      String label, String value) {
        g.drawString(font, label, x, y, C_MID, false);
        g.drawString(font, value, x + w - font.width(value), y, C_WHITE, false);
        int top = y + 11;
        g.fill(x, top, x + w, top + h, 0x33_000000);
        GuiDraw.drawBorder(g, x, top, w, h, 0x66_00B8A5, 1);
        for (int q = 1; q < 4; q++) g.fill(x + 1, top + h * q / 4, x + w - 1, top + h * q / 4 + 1, 0x14_FFFFFF);

        if (history.filled < 2) {
            g.drawString(font, "collecting...", x + 4, top + h / 2 - 4, C_DIM, false);
            return;
        }
        int inner = w - 2, innerH = h - 2;
        int prev = -1;
        for (int col = 0; col < inner; col++) {
            int i = (int) ((long) col * (history.filled - 1) / Math.max(1, inner - 1));
            double v = Math.max(0, Math.min(1, data[history.index(i)] / max));
            int bar = (int) Math.round(v * (innerH - 1));
            int px = x + 1 + col;
            int base = top + h - 1;
            g.fill(px, base - bar, px + 1, base, (0x33 << 24) | (color & 0xFFFFFF));
            int lo = prev < 0 ? bar : Math.min(prev, bar), hi = prev < 0 ? bar : Math.max(prev, bar);
            g.fill(px, base - hi - 1, px + 1, base - lo, color);
            prev = bar;
        }
    }
}
