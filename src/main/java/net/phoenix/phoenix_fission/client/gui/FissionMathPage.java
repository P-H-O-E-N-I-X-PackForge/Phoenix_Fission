package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionThermalManager;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import java.util.List;
import java.util.Locale;

public class FissionMathPage implements CanvasPainter {

    private static final int CONTENT_HEIGHT = 420;

    private final MathWidget view;
    private int scroll = 0;

    public FissionMathPage(FissionWorkableElectricMultiblockMachine reactor) {
        this.view = new MathWidget(reactor);
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

        private final FissionWorkableElectricMultiblockMachine reactor;

        private static final int BG = 0xEE_010810;
        private static final int C_CYAN = 0xFF_00E5CC;
        private static final int C_DIM = 0xFF_3A5E6A;
        private static final int C_MID = 0xFF_6A9BAA;
        private static final int C_WHITE = 0xFF_DCF0F4;
        private static final int C_GOLD = 0xFF_FFC44D;
        private static final int C_ORANGE = 0xFF_FF8833;
        private static final int C_RED = 0xFF_FF3333;
        private static final int C_GREEN = 0xFF_33FF88;
        private static final int A_FUEL = 0xFF_FF7744;
        private static final int A_COOL = 0xFF_44AAFF;

        MathWidget(FissionWorkableElectricMultiblockMachine reactor) {
            this.reactor = reactor;
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
            var fcfg = PhoenixFissionConfigs.INSTANCE.fission;

            y = section(g, font, x, y, W, "HEAT GAIN / TICK", A_FUEL);

            y = formula(g, font, x, y, W,
                    "heat = sum(rodBase) * modMult * parallels",
                    "modMult = sum(heatMult - 1) + 1");
            y = note(g, font, x, y, W, "Live: " + fmt1(reactor.lastHeatGainedPerTick) + " HU/t");
            y += 3;

            y = section(g, font, x, y, W, "COOLING / TICK", A_COOL);

            if (FissionThermalManager.isRated(fcfg.heatModel)) {
                y = formula(g, font, x, y, W,
                        "passive: sum(flatHU/t)",
                        "active: sum(rated * (heat - ambient) / (safe - ambient))");
                y = note(g, font, x, y, W, "Production ignores temperature: stable heat = production / rated");
            } else {
                double cond = fcfg.heatModel.activeCoolingConductivity;
                y = formula(g, font, x, y, W,
                        "passive: sum(flatHU/t)",
                        "active: sum((heat - coolerTemp) * conductivity)");
                y = note(g, font, x, y, W,
                        "Default conductivity: " + fmt2(cond) + " (per-cooler types may override)");
            }
            y = note(g, font, x, y, W, "Live: " + fmt1(reactor.lastHeatRemovedPerTick) + " HU/t removed");
            y += 3;

            y = section(g, font, x, y, W, "HEAT BOUNDS", C_CYAN);

            double maxSafe = reactor.getMaxSafeHeatHU();
            double heatRate = fcfg.heatModel.passiveCoolingConductivity;
            y = kv(g, font, x, y, W, "Meltdown Threshold", fmt0(maxSafe) + " HU");
            y = kv(g, font, x, y, W, "Passive Conductivity", fmt2(heatRate), C_CYAN);

            double heatPct = reactor.getHeat() / Math.max(1, maxSafe);
            String hpStr = String.format(Locale.ROOT, "%.1f%%", heatPct * 100);
            int hpCol = heatPct > 0.85 ? C_RED : heatPct > 0.5 ? C_ORANGE : C_GREEN;
            y = kv(g, font, x, y, W, "Current", hpStr + " of threshold", hpCol);
            y += 3;

            y = section(g, font, x, y, W, "MELTDOWN GRACE", C_RED);

            double base = fcfg.meltdown.baseGraceSeconds;
            double min = fcfg.meltdown.minGraceSeconds;
            double sev = fcfg.meltdown.excessHeatSeverity;
            y = formula(g, font, x, y, W,
                    "grace = base - (base - min) * min(1, excess% * sev)",
                    String.format(Locale.ROOT,
                            " = %.0f - (%.0f - %.0f) * min(1, x * %.1f)", base, base, min, sev));
            y = kv(g, font, x, y, W, "base", fmt1(base) + "s", C_MID);
            y = kv(g, font, x, y, W, "min", fmt1(min) + "s", C_MID);
            y = kv(g, font, x, y, W, "severity", fmt2(sev), C_MID);

            int timerTicks = reactor.meltdownTimerTicks;
            int timerMax = reactor.meltdownTimerMax;
            if (timerMax > 0 && timerTicks >= 0) {
                String cd = String.format(Locale.ROOT, "%.1fs / %.1fs",
                        timerTicks / 20.0, timerMax / 20.0);
                y = note(g, font, x, y, W, "Countdown: " + cd +
                        (reactor.isScramActive() ? " (FROZEN)" : ""));
            } else {
                y = note(g, font, x, y, W, "No active countdown");
            }
            y += 3;

            if (fcfg.enableDirectEUOutput) {
                y = section(g, font, x, y, W, "EU OUTPUT", C_GOLD);
                y = formula(g, font, x, y, W,
                        "EU/t = heatHU * euConversion * (1 + boost%)",
                        "fuel cost * (1 - discount%)");

                String formattedEU = FormattingUtil.formatNumbers(reactor.lastGeneratedEUt);
                y = note(g, font, x, y, W, "Live: " + formattedEU + " EU/t");
                y += 3;
            }

            y = section(g, font, x, y, W, "PARALLELS", C_GOLD);
            y = formula(g, font, x, y, W,
                    "parallels = base + sum(modParBonus)",
                    "heat/tick scales * parallels");
            note(g, font, x, y, W, "Live: x" + reactor.lastParallels);
        }

        private static int section(GuiGraphics g, Font font, int x, int y, int W,
                                   String title, int accent) {
            g.fill(x, y + 2, x + 4, y + 7, accent);
            g.drawString(font, title, x + 7, y, accent, false);
            y += 11;
            g.fill(x, y, x + W, y + 1, (0x66 << 24) | (accent & 0xFFFFFF));
            return y + 3;
        }

        private static int formula(GuiGraphics g, Font font, int x, int y, int W,
                                   String... lines) {
            var splitter = font.getSplitter();
            for (String line : lines) {
                List<FormattedText> wrappedLines = new java.util.ArrayList<>();
                splitter.splitLines(Component.literal(line), W - 4, Style.EMPTY,
                        (formattedText, bo) -> wrappedLines.add(formattedText));

                for (FormattedText subLine : wrappedLines) {
                    g.drawString(font, subLine.getString(), x + 2, y, C_WHITE, false);
                    y += 9;
                }
            }
            return y + 1;
        }

        private static int note(GuiGraphics g, Font font, int x, int y, int W,
                                String text) {
            var splitter = font.getSplitter();
            List<FormattedText> wrappedLines = new java.util.ArrayList<>();
            splitter.splitLines(Component.literal(text), W - 4, Style.EMPTY,
                    (formattedText, bo) -> wrappedLines.add(formattedText));

            for (FormattedText subLine : wrappedLines) {
                g.drawString(font, subLine.getString(), x + 2, y, C_DIM, false);
                y += 9;
            }
            return y;
        }

        private static int kv(GuiGraphics g, Font font, int x, int y, int W,
                              String key, String value) {
            return kv(g, font, x, y, W, key, value, C_CYAN);
        }

        private static int kv(GuiGraphics g, Font font, int x, int y, int W,
                              String key, String value, int valColor) {
            g.drawString(font, key, x + 2, y, C_MID, false);
            g.drawString(font, value, x + W - font.width(value), y, valColor, false);
            return y + 9;
        }

        private static String fmt0(double v) {
            return String.format(Locale.ROOT, "%.0f", v);
        }

        private static String fmt1(double v) {
            return String.format(Locale.ROOT, "%.1f", v);
        }

        private static String fmt2(double v) {
            return String.format(Locale.ROOT, "%.2f", v);
        }
    }
}
