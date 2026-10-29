package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.ThermoelectricGeneratorMachine;

import java.util.Locale;

public class ThermoelectricGeneratorMathPage implements CanvasPainter {

    private static final int CONTENT_HEIGHT = 300;
    private static final int MAX_LENGTH = 20;

    private final MathWidget view;
    private int scroll = 0;

    public ThermoelectricGeneratorMathPage(ThermoelectricGeneratorMachine machine) {
        this.view = new MathWidget(machine);
    }

    @Override
    public void render(GuiGraphics g, int w, int h, int mouseX, int mouseY) {
        int maxScroll = Math.max(0, CONTENT_HEIGHT - h);
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        GuiDraw.enableScissor(g, 0, 0, w, h);
        g.pose().pushPose();
        g.pose().translate(0, -scroll, 0);
        view.paint(g, w - 10, CONTENT_HEIGHT);
        g.pose().popPose();
        if (maxScroll > 0) {
            int trackH = h;
            int thumbH = Math.max(12, (int) (trackH * (h / (double) CONTENT_HEIGHT)));
            int thumbY = (int) ((trackH - thumbH) * (scroll / (double) maxScroll));
            g.fill(w - 4, 0, w, trackH, 0x33_00E5CC);
            g.fill(w - 4, thumbY, w, thumbY + thumbH, 0xAA_00E5CC);
        }
        g.disableScissor();
    }

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        scroll -= (int) (amount * 12);
        return true;
    }

    private static final class MathWidget {

        private static final int MAX_LENGTH = ThermoelectricGeneratorMathPage.MAX_LENGTH;

        private final ThermoelectricGeneratorMachine machine;

        private static final int BG = 0xEE_04090F;
        private static final int C_TEAL = 0xFF_00AAA0;
        private static final int C_CYAN = 0xFF_00E5CC;
        private static final int C_DIM = 0xFF_3A5E6A;
        private static final int C_MID = 0xFF_6A9BAA;
        private static final int C_WHITE = 0xFF_DCF0F4;
        private static final int C_GOLD = 0xFF_FFC44D;
        private static final int C_ORANGE = 0xFF_FF8833;
        private static final int C_GREEN = 0xFF_33FF88;
        private static final int C_BLUE = 0xFF_44AAFF;
        private static final int C_CRYO = 0xFF_55DDFF;

        MathWidget(ThermoelectricGeneratorMachine machine) {
            this.machine = machine;
        }

        @OnlyIn(Dist.CLIENT)
        void paint(GuiGraphics g, int w, int h) {
            int x0 = 0, y0 = 0;
            g.fill(x0, y0, x0 + w, y0 + h, BG);
            GuiDraw.drawBorder(g, x0, y0, w, h, 0x33_00E5CC, 1);

            draw(g, x0 + 4, y0 + 4, w - 8);
        }

        @OnlyIn(Dist.CLIENT)
        private void draw(GuiGraphics g, int x, int y, int W) {
            Font font = Minecraft.getInstance().font;
            boolean formed = machine.isFormed();
            int len = Math.max(1, machine.getLength());
            boolean helium = formed && machine.isHeliumActive();

            y = section(g, font, x, y, W, "EU MULTIPLIER", C_GOLD);
            y = formula(g, font, x, y, W,
                    "linear = 1.5 + (len - 1) * 0.25",
                    "linear *= 2  if helium active",
                    "EUt multiplier = linear^2");
            y = kv(g, font, x, y, W, "Columns (len)", len + "", formed ? C_WHITE : C_DIM);
            double linear = linearMultiplier(len, helium);
            y = kv(g, font, x, y, W, "Linear multiplier", fmt2(linear) + (helium ? "  (cryo)" : ""),
                    helium ? C_CRYO : C_MID);
            y = kv(g, font, x, y, W, "Live EUt multiplier", "x" + fmt2(linear * linear),
                    linear * linear > 4.0 ? C_GREEN : C_MID);
            y += 3;

            y = section(g, font, x, y, W, "COOLANT THROUGHPUT", C_BLUE);
            int baseCoolant = Math.max(1,
                    net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs.INSTANCE.fission.thermoelectricGenerator.baseCoolantAmount);
            y = formula(g, font, x, y, W,
                    "coolant = " + baseCoolant + " * (1 + (len - 1) * 0.2)",
                    "helium = 25 * (1 + (len - 1) * 0.2)");
            int coolantMb = throughput(baseCoolant, len);
            int heliumMb = throughput(25, len);
            y = kv(g, font, x, y, W, "Coolant", coolantMb + " mB/s", C_MID);
            y = kv(g, font, x, y, W, "Helium", heliumMb + " mB/s", C_CRYO);
            y += 3;

            y = section(g, font, x, y, W, "DYNAMO OUTPUT", C_TEAL);
            long maxOut = machine.getMaxHatchOutput();
            String tierStr = maxOut > 0 ? GTValues.VNF[machine.getDynamoTier()] : "none";
            y = kv(g, font, x, y, W, "Detected tier", tierStr, maxOut > 0 ? C_GREEN : C_DIM);
            y = kv(g, font, x, y, W, "Max hatch output", maxOut > 0 ? formatEU(maxOut) : "no dynamo hatches",
                    maxOut > 0 ? C_ORANGE : C_DIM);
            y += 5;

            y = section(g, font, x, y, W, "EUT MULTIPLIER vs. COLUMNS", C_CYAN);
            drawMultiplierCurve(g, font, x, y, W, len, helium);
        }

        @OnlyIn(Dist.CLIENT)
        private void drawMultiplierCurve(GuiGraphics g, Font font, int x, int y, int W, int currentLen,
                                         boolean helium) {
            g.fill(x, y + 3, x + 8, y + 5, C_MID);
            g.drawString(font, "base", x + 10, y, C_MID, false);
            int lx2 = x + 10 + font.width("base") + 8;
            g.fill(lx2, y + 3, lx2 + 8, y + 5, C_CRYO);
            g.drawString(font, "helium", lx2 + 10, y, C_CRYO, false);
            y += 10;

            int plotH = 60;
            double[] baseY = new double[MAX_LENGTH];
            double[] heliumY = new double[MAX_LENGTH];
            double yMax = 1.0;
            for (int i = 0; i < MAX_LENGTH; i++) {
                int len = i + 1;
                double base = linearMultiplier(len, false);
                double heliumMult = linearMultiplier(len, true);
                baseY[i] = base * base;
                heliumY[i] = heliumMult * heliumMult;
                yMax = Math.max(yMax, heliumY[i]);
            }
            yMax *= 1.1;

            drawPlot(g, x, y, W, plotH);
            drawCurve(g, x, y, W, plotH, baseY, yMax, C_MID);
            drawCurve(g, x, y, W, plotH, heliumY, yMax, C_CRYO);

            int markX = x + (int) ((currentLen - 1) / (double) (MAX_LENGTH - 1) * W);
            double markVal = (helium ? heliumY : baseY)[Math.min(MAX_LENGTH, Math.max(1, currentLen)) - 1];
            double markNorm = Math.min(1.0, markVal / yMax);
            int markY = y + plotH - 2 - (int) (markNorm * (plotH - 4));
            g.fill(markX - 2, markY - 2, markX + 3, markY + 3, 0xFF_FFFFFF);
            g.fill(markX - 1, markY - 1, markX + 2, markY + 2, helium ? C_CRYO : C_GOLD);

            int labelY = y + plotH + 2;
            g.drawString(font, "1", x, labelY, C_DIM, false);
            String maxLabel = MAX_LENGTH + "";
            g.drawString(font, maxLabel, x + W - font.width(maxLabel), labelY, C_DIM, false);
        }

        @OnlyIn(Dist.CLIENT)
        private static void drawPlot(GuiGraphics g, int x, int y, int W, int H) {
            g.fill(x, y, x + W, y + H, 0x18_FFFFFF);
            GuiDraw.drawBorder(g, x, y, W, H, 0x44_00E5CC, 1);
            for (int gi = 1; gi <= 3; gi++) {
                int gy = y + H - (int) (gi * 0.25 * H);
                g.fill(x, gy, x + W, gy + 1, 0x18_FFFFFF);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static void drawCurve(GuiGraphics g, int plotX, int plotY, int plotW, int plotH,
                                      double[] values, double yMax, int color) {
            Integer prevX = null, prevY = null;
            for (int i = 0; i < values.length; i++) {
                int px = plotX + (int) ((i / (double) (values.length - 1)) * plotW);
                double norm = Math.min(1.0, values[i] / yMax);
                int py = plotY + plotH - 2 - (int) (norm * (plotH - 4));
                py = Math.max(plotY + 1, Math.min(plotY + plotH - 2, py));
                if (prevX != null) drawLine(g, prevX, prevY, px, py, color);
                prevX = px;
                prevY = py;
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
            int dx = x2 - x1, dy = y2 - y1;
            int steps = Math.max(Math.abs(dx), Math.abs(dy));
            if (steps == 0) {
                g.fill(x1, y1, x1 + 1, y1 + 1, color);
                return;
            }
            for (int s = 0; s <= steps; s++) {
                int px = x1 + Math.round(dx * (s / (float) steps));
                int py = y1 + Math.round(dy * (s / (float) steps));
                g.fill(px, py, px + 1, py + 1, color);
            }
        }

        private static double linearMultiplier(int len, boolean helium) {
            double linear = 1.5 + Math.max(0, len - 1) * 0.25;
            return helium ? linear * 2.0 : linear;
        }

        private static int throughput(int base, int len) {
            return (int) (base * (1.0 + Math.max(0, len - 1) * 0.2));
        }

        private static int section(GuiGraphics g, Font font, int x, int y, int W, String title, int accent) {
            g.fill(x, y + 2, x + 4, y + 7, accent);
            g.drawString(font, title, x + 7, y, accent, false);
            y += 11;
            g.fill(x, y, x + W, y + 1, (0x66 << 24) | (accent & 0xFFFFFF));
            return y + 3;
        }

        private static int formula(GuiGraphics g, Font font, int x, int y, int W, String... lines) {
            for (String line : lines) {
                g.drawString(font, line, x + 2, y, C_WHITE, false);
                y += 9;
            }
            return y + 1;
        }

        private static int kv(GuiGraphics g, Font font, int x, int y, int W, String key, String value,
                              int valColor) {
            g.drawString(font, key, x + 2, y, C_MID, false);
            g.drawString(font, value, x + W - font.width(value), y, valColor, false);
            return y + 9;
        }

        private static String formatEU(long eu) {
            if (eu >= 1_000_000) return String.format(Locale.ROOT, "%.1fM EU/t", eu / 1_000_000.0);
            if (eu >= 1_000) return String.format(Locale.ROOT, "%.1fk EU/t", eu / 1_000.0);
            return eu + " EU/t";
        }

        private static String fmt2(double v) {
            return String.format(Locale.ROOT, "%.2f", v);
        }
    }
}
