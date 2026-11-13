package net.phoenix.phoenix_fission.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.AscensionReactorMachine;

import java.util.Locale;

/** The last minute of the three gauges, one strip each, with their bands shaded in. */
@OnlyIn(Dist.CLIENT)
public class AscensionGraphsPage implements CanvasPainter {

    private static final int BG_TOP = 0xFF_02101A;
    private static final int BG_BOT = 0xFF_010508;
    private static final int C_TEAL = 0xFF_00B8A5;
    private static final int C_CYAN = 0xFF_00E5CC;
    private static final int C_MID = 0xFF_7FA3AE;
    private static final int C_DIM = 0xFF_3A5E6A;
    private static final int C_WHITE = 0xFF_DCF0F4;
    private static final int[] COLORS = { 0xFF_44AAFF, 0xFF_FF8833, 0xFF_C98CFF };

    private final AscensionReactorMachine m;
    private final AscensionHistory h;

    public AscensionGraphsPage(AscensionReactorMachine machine, AscensionHistory history) {
        this.m = machine;
        this.h = history;
    }

    @Override
    public void render(GuiGraphics g, int w, int hgt, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        h.sampleIfDue(m);
        GuiDraw.drawGradientRect(g, 0, 0, w, hgt, BG_TOP, BG_BOT, false);
        g.fill(0, 0, w, 16, BG_TOP);
        g.fill(3, 3, 6, 13, C_TEAL);
        g.drawString(font, "ASCENSION GRAPHS", 10, 4, C_CYAN, false);
        g.fill(0, 15, w, 16, 0x44_00E5CC);

        int x = 8;
        int cw = w - 16;
        int plotH = (hgt - 22 - 8 - 4 * 14) / 4;
        int y = 22;
        plot(g, font, x, y, cw, plotH, h.flux, 0, COLORS[0], "FLUX", String.format(Locale.ROOT, "%.0f", m.flux()));
        y += plotH + 14;
        plot(g, font, x, y, cw, plotH, h.heat, 1, COLORS[1], "HEAT",
                String.format(Locale.ROOT, "%.0f", m.heatPercent()));
        y += plotH + 14;
        plot(g, font, x, y, cw, plotH, h.plasma, 2, COLORS[2], "PLASMA",
                String.format(Locale.ROOT, "%.0f", m.plasma()));
        y += plotH + 14;
        plot(g, font, x, y, cw, plotH, h.stability, -1, 0xFF_33FF88, "FIELD STABILITY",
                Math.round(m.stability()) + "%");
        GuiDraw.drawBorder(g, 0, 0, w, hgt, C_TEAL, 1);
    }

    private void plot(GuiGraphics g, Font font, int x, int y, int w, int hh, double[] data, int band, int color,
                      String label, String value) {
        g.drawString(font, label, x, y, C_MID, false);
        g.drawString(font, value, x + w - font.width(value), y, C_WHITE, false);
        int top = y + 10;
        g.fill(x, top, x + w, top + hh, 0x33_000000);
        if (band >= 0) {
            double lo = AscensionReactorMachine.BANDS[band][0];
            double hi = AscensionReactorMachine.BANDS[band][1];
            int by0 = top + hh - 1 - (int) ((hh - 2) * hi / 100.0);
            int by1 = top + hh - 1 - (int) ((hh - 2) * lo / 100.0);
            g.fill(x + 1, by0, x + w - 1, by1, 0x2233FF88);
        }
        GuiDraw.drawBorder(g, x, top, w, hh, 0x66_00B8A5, 1);
        if (h.filled < 2) {
            g.drawString(font, "collecting...", x + 4, top + hh / 2 - 4, C_DIM, false);
            return;
        }
        int inner = w - 2;
        int innerH = hh - 2;
        int prev = -1;
        for (int col = 0; col < inner; col++) {
            int i = (int) ((long) col * (h.filled - 1) / Math.max(1, inner - 1));
            double v = Math.max(0, Math.min(1, data[h.index(i)] / 100.0));
            int bar = (int) Math.round(v * (innerH - 1));
            int px = x + 1 + col;
            int base = top + hh - 1;
            int lo = prev < 0 ? bar : Math.min(prev, bar);
            int hi = prev < 0 ? bar : Math.max(prev, bar);
            g.fill(px, base - hi - 1, px + 1, base - lo, color);
            prev = bar;
        }
    }
}
