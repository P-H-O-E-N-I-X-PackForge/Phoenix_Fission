package net.phoenix.phoenix_fission.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.FusionReactorMachine;
import net.phoenix.phoenix_fission.client.FissionText;

@OnlyIn(Dist.CLIENT)
public class FusionGraphsPainter implements CanvasPainter {

    private static final int BG_TOP = 0xFF_02101A;
    private static final int BG_BOT = 0xFF_010508;
    private static final int C_TEAL = 0xFF_00B8A5;
    private static final int C_CYAN = 0xFF_00E5CC;
    private static final int C_MID = 0xFF_7FA3AE;
    private static final int C_DIM = 0xFF_3A5E6A;
    private static final int C_WHITE = 0xFF_DCF0F4;
    private static final int C_GREEN = 0xFF_33FF88;
    private static final int C_GOLD = 0xFF_FFC040;

    private final FusionReactorMachine m;
    private final FusionHistory h;

    public FusionGraphsPainter(FusionReactorMachine machine, FusionHistory history) {
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
        g.drawString(font, FissionText.str("phoenix_fission.ui.fusion_graphs_page.fusion_graphs"), 10, 4, C_CYAN,
                false);
        g.fill(0, 15, w, 16, 0x44_00E5CC);

        int x = 8, cw = w - 16;
        int plotH = (hgt - 16 - 8 - 3 * 14) / 3;
        int y = 22;
        double peak = 1;
        for (int i = 0; i < h.filled; i++) peak = Math.max(peak, h.output[h.index(i)]);
        plot(g, font, x, y, cw, plotH, h.output, peak * 1.1, C_GOLD,
                FissionText.str("phoenix_fission.ui.fusion_graphs_page.output_eu_t"), m.getGeneratedEUt() + " EU/t");
        y += plotH + 14;
        plot(g, font, x, y, cw, plotH, h.integrity, 1.0, C_GREEN,
                FissionText.str("phoenix_fission.ui.fusion_graphs_page.field_integrity"),
                Math.round((1.0 - m.getDrift() / 100.0) * 100) + "%");
        y += plotH + 14;
        plot(g, font, x, y, cw, plotH, h.pressure, 100.0, C_CYAN,
                FissionText.str("phoenix_fission.ui.fusion_graphs_page.pressure"),
                Math.round(m.getPressure()) + " / " + Math.round(m.getRequiredPressure()));
        GuiDraw.drawBorder(g, 0, 0, w, hgt, C_TEAL, 1);
    }

    private void plot(GuiGraphics g, Font font, int x, int y, int w, int hh, double[] data, double max, int color,
                      String label, String value) {
        g.drawString(font, label, x, y, C_MID, false);
        g.drawString(font, value, x + w - font.width(value), y, C_WHITE, false);
        int top = y + 10;
        g.fill(x, top, x + w, top + hh, 0x33_000000);
        GuiDraw.drawBorder(g, x, top, w, hh, 0x66_00B8A5, 1);
        for (int q = 1; q < 4; q++) g.fill(x + 1, top + hh * q / 4, x + w - 1, top + hh * q / 4 + 1, 0x14_FFFFFF);
        if (h.filled < 2) {
            g.drawString(font, FissionText.str("phoenix_fission.ui.ascension_graphs_page.collecting"), x + 4,
                    top + hh / 2 - 4, C_DIM, false);
            return;
        }
        int inner = w - 2, innerH = hh - 2;
        int prev = -1;
        for (int col = 0; col < inner; col++) {
            int i = (int) ((long) col * (h.filled - 1) / Math.max(1, inner - 1));
            double v = Math.max(0, Math.min(1, data[h.index(i)] / max));
            int bar = (int) Math.round(v * (innerH - 1));
            int px = x + 1 + col, base = top + hh - 1;
            g.fill(px, base - bar, px + 1, base, (0x33 << 24) | (color & 0xFFFFFF));
            int lo = prev < 0 ? bar : Math.min(prev, bar), hi = prev < 0 ? bar : Math.max(prev, bar);
            g.fill(px, base - hi - 1, px + 1, base - lo, color);
            prev = bar;
        }
    }
}
