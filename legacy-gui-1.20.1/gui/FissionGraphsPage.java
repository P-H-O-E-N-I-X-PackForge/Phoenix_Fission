package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionModeratorType;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.MoltenSaltReactorMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionThermalManager;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class FissionGraphsPage implements IFancyUIProvider {

    private final FissionWorkableElectricMultiblockMachine reactor;

    public FissionGraphsPage(FissionWorkableElectricMultiblockMachine reactor) {
        this.reactor = reactor;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new GuiTextureGroup(
                new ColorRectTexture(0xFF_010810),
                new TextTexture("~", 0xFF_00E5CC));
    }

    @Override
    public Component getTitle() {
        return Component.literal("Heat Curves");
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        int w = widget.getPageContainer().getSize().width;
        int h = widget.getPageContainer().getSize().height;
        return new GraphWidget(reactor, w, h);
    }

    static final class GraphWidget extends WidgetGroup {

        private final FissionWorkableElectricMultiblockMachine reactor;

        private int activeGraph = 0;

        private final int[] tabRect0 = new int[4];
        private final int[] tabRect1 = new int[4];

        private static final int BG = 0xEE_010810;
        private static final int C_DIM = 0xFF_3A5E6A;
        private static final int C_WHITE = 0xFF_DCF0F4;
        private static final int C_TEAL = 0xFF_00AAA0;
        private static final int C_CYAN = 0xFF_00E5CC;
        private static final int C_GREEN = 0xFF_33FF88;

        private static final int C_HEAT = 0xFF_FF5533;
        private static final int C_COOL = 0xFF_44AAFF;
        private static final int C_POS = 0xFF_FF6644;
        private static final int C_NEG = 0xFF_44AAFF;
        private static final int C_RED = 0xFF_FF3333;

        GraphWidget(FissionWorkableElectricMultiblockMachine reactor, int w, int h) {
            super(0, 0, w, h);
            this.reactor = reactor;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mx, double my, int btn) {
            if (btn == 0) {
                if (hit(tabRect0, mx, my)) {
                    activeGraph = 0;
                    return true;
                }
                if (hit(tabRect1, mx, my)) {
                    activeGraph = 1;
                    return true;
                }
            }
            return super.mouseClicked(mx, my, btn);
        }

        private static boolean hit(int[] r, double mx, double my) {
            return r[2] > 0 && mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3];
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@Nonnull GuiGraphics g, int mouseX, int mouseY, float dt) {
            int x0 = getPosition().x, y0 = getPosition().y;
            int w = getSize().width, h = getSize().height;
            g.fill(x0, y0, x0 + w, y0 + h, BG);
            DrawerHelper.drawBorder(g, x0, y0, w, h, 0x33_00E5CC, 1);
            super.drawInBackground(g, mouseX, mouseY, dt);
            drawContent(g, x0 + 4, y0 + 4, w - 8, mouseX, mouseY);
        }

        @OnlyIn(Dist.CLIENT)
        private void drawContent(GuiGraphics g, int x, int y, int W,
                                 int mouseX, int mouseY) {
            Font font = Minecraft.getInstance().font;

            if (!reactor.isFormed()) {
                g.drawString(font, "Reactor not formed.", x, y + 4, C_DIM, false);
                return;
            }

            var comp = reactor.getComponentManager();
            List<IFissionFuelRodType> rods = comp.getActiveFuelRods();
            List<IFissionModeratorType> mods = comp.getActiveModerators();
            List<IFissionCoolerType> coolers = comp.getActiveCoolers();

            var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
            double modBonus = 1.0 + (computeModBonus(mods) / 100.0);

            y = drawGraphTabs(g, font, x, y, W, mouseX, mouseY);

            int plotH = 62;
            if (activeGraph == 0) {
                y = drawEquilibriumGraph(g, font, x, y, W, plotH, rods, coolers, modBonus, hm);
            } else {
                y = drawNetRateGraph(g, font, x, y, W, plotH, rods, coolers, modBonus, hm);
            }

            g.fill(x, y, x + W, y + 1, 0x33_00E5CC);
            y += 5;

            boolean hasMSRLiners = reactor instanceof MoltenSaltReactorMultiblockMachine msr2 &&
                    msr2.linerFlowRate > 0 && msr2.linerHeatPerMb > 0;
            if (rods.isEmpty() && !hasMSRLiners) {
                g.drawString(font, reactor instanceof MoltenSaltReactorMultiblockMachine ? "No liner installed." :
                        "No fuel rods installed.", x, y, C_DIM, false);
            } else {
                double stableT = findStableTemperature(rods, coolers, modBonus,
                        reactor.getMaxHeatClampHU());
                drawAnnotations(g, font, x, y, W, stableT, reactor.getMaxSafeHeatHU(), reactor.getMaxHeatClampHU());
            }
        }

        @OnlyIn(Dist.CLIENT)
        private int drawGraphTabs(GuiGraphics g, Font font, int x, int y, int W,
                                  int mouseX, int mouseY) {
            int btnH = 13;
            int gap = 3;
            int btnW = (W - gap) / 2;

            String[] labels = { "EQUIL", "NET RATE" };
            int[][] rects = { tabRect0, tabRect1 };

            for (int i = 0; i < 2; i++) {
                int bx = x + i * (btnW + gap);
                boolean active = activeGraph == i;
                boolean hovered = mouseX >= bx && mouseX < bx + btnW && mouseY >= y && mouseY < y + btnH;

                int fill = active ? 0xFF_002830 : hovered ? 0xFF_061825 : 0xFF_010810;
                int border = active ? 0xFF_00AAA0 : hovered ? 0xCC_00E5CC : 0x66_00E5CC;
                int txt = active ? C_CYAN : hovered ? C_WHITE : C_DIM;

                g.fill(bx, y, bx + btnW, y + btnH, fill);
                DrawerHelper.drawBorder(g, bx, y, btnW, btnH, border, 1);
                int tw = font.width(labels[i]);
                g.drawString(font, labels[i], bx + (btnW - tw) / 2, y + 3, txt, false);

                rects[i][0] = bx;
                rects[i][1] = y;
                rects[i][2] = btnW;
                rects[i][3] = btnH;
            }
            return y + btnH + 5;
        }

        @OnlyIn(Dist.CLIENT)
        private int drawEquilibriumGraph(GuiGraphics g, Font font,
                                         int x, int y, int SAMPLES, int plotH,
                                         List<IFissionFuelRodType> rods, List<IFissionCoolerType> coolers,
                                         double modBonus, PhoenixFissionConfigs.HeatModelConfigs hm) {
            g.fill(x, y + 3, x + 8, y + 5, C_HEAT);
            g.drawString(font, "production", x + 10, y, C_HEAT, false);
            int lx2 = x + 10 + font.width("production") + 8;
            g.fill(lx2, y + 3, lx2 + 8, y + 5, C_COOL);
            g.drawString(font, "cooling", lx2 + 10, y, C_COOL, false);
            y += 10;

            double maxSafeHU = reactor.getMaxSafeHeatHU();
            double maxClampHU = reactor.getMaxHeatClampHU();
            double stableT = findStableTemperature(rods, coolers, modBonus, maxClampHU);
            double xMax = maxClampHU * 1.15;

            double[] prodY = new double[SAMPLES];
            double[] coolY = new double[SAMPLES];
            double yMax = 1.0;
            for (int i = 0; i < SAMPLES; i++) {
                double T = xMax * i / (SAMPLES - 1);
                prodY[i] = computeHeatProduction(T, rods, modBonus, hm);
                coolY[i] = computeCooling(T, coolers, hm);
                yMax = Math.max(yMax, Math.max(prodY[i], coolY[i]));
            }
            yMax *= 1.1;

            drawPlot(g, x, y, SAMPLES, plotH);
            drawCurve(g, x, y, SAMPLES, plotH, prodY, yMax, SAMPLES, C_HEAT);
            drawCurve(g, x, y, SAMPLES, plotH, coolY, yMax, SAMPLES, C_COOL);

            if (stableT > 0 && stableT <= xMax) {
                double sFrac = stableT / xMax;
                int sX = x + (int) (sFrac * SAMPLES);
                double sNorm = Math.min(1.0, computeHeatProduction(stableT, rods, modBonus, hm) / yMax);
                int sY = y + plotH - 2 - (int) (sNorm * (plotH - 4));
                g.fill(sX - 2, sY - 2, sX + 3, sY + 3, 0xFF_FFFFFF);
                g.fill(sX - 1, sY - 1, sX + 2, sY + 2, C_GREEN);
            }

            return drawXAxisLabels(g, font, x, y, SAMPLES, plotH, xMax, maxSafeHU, maxClampHU);
        }

        @OnlyIn(Dist.CLIENT)
        private int drawNetRateGraph(GuiGraphics g, Font font,
                                     int x, int y, int SAMPLES, int plotH,
                                     List<IFissionFuelRodType> rods, List<IFissionCoolerType> coolers,
                                     double modBonus, PhoenixFissionConfigs.HeatModelConfigs hm) {
            g.fill(x, y + 3, x + 8, y + 5, C_POS);
            g.drawString(font, "warming", x + 10, y, C_POS, false);
            int lx2 = x + 10 + font.width("warming") + 8;
            g.fill(lx2, y + 3, lx2 + 8, y + 5, C_NEG);
            g.drawString(font, "cooling", lx2 + 10, y, C_NEG, false);
            y += 10;

            double maxSafeHU = reactor.getMaxSafeHeatHU();
            double maxClampHU = reactor.getMaxHeatClampHU();
            double stableT = findStableTemperature(rods, coolers, modBonus, maxClampHU);
            double xMax = maxClampHU * 1.15;

            double[] netY = new double[SAMPLES];
            double peakAbs = 1.0;
            for (int i = 0; i < SAMPLES; i++) {
                double T = xMax * i / (SAMPLES - 1);
                netY[i] = computeHeatProduction(T, rods, modBonus, hm) - computeCooling(T, coolers, hm);
                peakAbs = Math.max(peakAbs, Math.abs(netY[i]));
            }
            double scale = (plotH / 2.0 - 2) / (peakAbs * 1.1);
            int zeroY = y + plotH / 2;

            drawPlot(g, x, y, SAMPLES, plotH);
            g.fill(x, zeroY, x + SAMPLES, zeroY + 1, 0x55_FFFFFF);

            for (int i = 1; i < SAMPLES; i++) {
                double n0 = netY[i - 1], n1 = netY[i];
                int px0 = x + i - 1, px1 = x + i;
                int py0 = zeroY - (int) (n0 * scale);
                int py1 = zeroY - (int) (n1 * scale);
                py0 = Math.max(y + 1, Math.min(y + plotH - 2, py0));
                py1 = Math.max(y + 1, Math.min(y + plotH - 2, py1));
                drawLine(g, px0, py0, px1, py1, (n0 + n1) >= 0 ? C_POS : C_NEG);
            }

            if (stableT > 0 && stableT <= xMax) {
                int sX = x + (int) (stableT / xMax * SAMPLES);
                g.fill(sX - 2, zeroY - 2, sX + 3, zeroY + 3, 0xFF_FFFFFF);
                g.fill(sX - 1, zeroY - 1, sX + 2, zeroY + 2, C_GREEN);
            }

            return drawXAxisLabels(g, font, x, y, SAMPLES, plotH, xMax, maxSafeHU, maxClampHU);
        }

        @OnlyIn(Dist.CLIENT)
        private void drawAnnotations(GuiGraphics g, Font font, int x, int y, int W,
                                     double stableT, double maxSafe, double maxClamp) {
            double heatCap = reactor.getHeatCapacity();
            if (stableT < 0) {
                long t = System.currentTimeMillis();
                int pulse = (int) (128 + 127 * Math.sin(t * 0.006));
                int warnColor = (0xFF << 24) | (pulse << 16) | 0x2222;
                g.drawString(font, "!! NO STABLE TEMPERATURE", x, y, warnColor, false);
                y += 10;
                g.drawString(font, "Reactor will always trend to meltdown.", x, y, C_DIM, false);
                y += 13;
            } else if (stableT == 0.0) {
                g.fill(x, y + 2, x + 5, y + 7, C_GREEN);
                g.drawString(font, "  Stable at minimum heat (0 K)", x, y, C_GREEN, false);
                g.drawString(font, "100% margin", x + W - font.width("100% margin"), y, C_TEAL, false);
                y += 13;
            } else if (stableT >= maxSafe) {
                g.fill(x, y + 2, x + 5, y + 7, C_RED);
                g.drawString(font, "  STABLE ABOVE MELTDOWN THRESHOLD", x, y, C_RED, false);
                y += 10;
                g.drawString(font, "  Equilibrium: " + formatK(stableT / heatCap), x, y, 0xFF_FF8833, false);
                y += 10;
                g.drawString(font, "  Reactor melts before stabilising.", x, y, C_DIM, false);
                y += 13;
            } else {
                g.fill(x, y + 2, x + 5, y + 7, C_GREEN);
                g.drawString(font, "  Stable temperature: " + formatK(stableT / heatCap), x, y, C_GREEN, false);
                double margin = (maxSafe - stableT) / maxSafe * 100.0;
                String marginStr = String.format("%.0f%% margin", margin);
                g.drawString(font, marginStr, x + W - font.width(marginStr), y, C_TEAL, false);
                y += 13;
            }

            g.fill(x, y + 2, x + 5, y + 7, 0xCC_FF8833);
            g.drawString(font, "  Threshold:  " + formatK(maxSafe / heatCap), x, y, 0xFF_FF8833, false);
            y += 10;
            g.fill(x, y + 2, x + 5, y + 7, C_RED);
            g.drawString(font, "  Meltdown: " + formatK(maxClamp / heatCap), x, y, 0xCC_FF3333, false);
            y += 12;
            var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
            if (FissionThermalManager.isRated(hm)) {
                var comp = reactor.getComponentManager();
                double modBonus = 1.0 + (computeModBonus(comp.getActiveModerators()) / 100.0);
                double production = computeHeatProduction(0, comp.getActiveFuelRods(), modBonus, hm);
                double rated = 0.0;
                for (IFissionCoolerType c : comp.getActiveCoolers()) {
                    if (c.isPassive()) production -= Math.max(0.0, c.getFlatCoolingHUt());
                    else rated += FissionThermalManager.resolveRatedCooling(c, hm, heatCap);
                }
                production = Math.max(0.0, production);
                double pct = rated > 0 ? production / rated * 100.0 : Double.POSITIVE_INFINITY;
                String load = rated > 0 ?
                        String.format("Load: %.0f HU/t vs %.0f rated = %.0f%%", production, rated, pct) :
                        String.format("Load: %.0f HU/t, no rated cooling", production);
                int col = pct < 100.0 ? C_GREEN : C_RED;
                g.drawString(font, load, x, y, col, false);
            } else {
                g.drawString(font, "Assumes: reactivity=1.0, coolant present", x, y, C_DIM, false);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static void drawPlot(GuiGraphics g, int x, int y, int W, int H) {
            g.fill(x, y, x + W, y + H, 0x18_FFFFFF);
            DrawerHelper.drawBorder(g, x, y, W, H, 0x44_00E5CC, 1);
            for (int gi = 1; gi <= 3; gi++) {
                int gy = y + H - (int) (gi * 0.25 * H);
                g.fill(x, gy, x + W, gy + 1, 0x18_FFFFFF);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private int drawXAxisLabels(GuiGraphics g, Font font, int x, int y, int W, int plotH,
                                    double xMax, double maxSafe, double maxClamp) {
            int labelY = y + plotH + 2;
            double heatCap = reactor.getHeatCapacity();
            String zeroStr = "0";
            g.drawString(font, zeroStr, x, labelY, C_DIM, false);
            String xMaxStr = formatK(xMax / heatCap);
            int endLabelX = x + W - font.width(xMaxStr);
            g.drawString(font, xMaxStr, endLabelX, labelY, C_DIM, false);

            int zeroLabelRight = x + font.width(zeroStr);
            int pad = 4;

            double criticalFrac = maxSafe / xMax;
            int sx = x + (int) (criticalFrac * W);
            String sl = "threshold";
            int sLeft = sx - font.width(sl) / 2;
            if (sLeft > zeroLabelRight + pad && sLeft + font.width(sl) < endLabelX - pad) {
                g.drawString(font, sl, sLeft, labelY, 0xCC_FF8833, false);
            }

            double meltdownFrac = maxClamp / xMax;
            int mx = x + (int) (meltdownFrac * W);
            String ml = "meltdown";
            int mLeft = mx - font.width(ml) / 2;
            if (mLeft > zeroLabelRight + pad && mLeft + font.width(ml) < endLabelX - pad) {
                g.drawString(font, ml, mLeft, labelY, 0xCC_FF3333, false);
            }
            return labelY + font.lineHeight + 3;
        }

        @OnlyIn(Dist.CLIENT)
        private static void drawCurve(GuiGraphics g, int plotX, int plotY,
                                      int plotW, int plotH, double[] values, double yMax, int samples, int color) {
            Integer prevX = null, prevY = null;
            for (int i = 0; i < samples; i++) {
                int px = plotX + (int) ((i / (double) (samples - 1)) * plotW);
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

        private double computeHeatProduction(double T,
                                             List<IFissionFuelRodType> rods, double modBonus,
                                             PhoenixFissionConfigs.HeatModelConfigs hm) {
            if (reactor instanceof MoltenSaltReactorMultiblockMachine msr) {
                if (msr.linerFlowRate <= 0 || msr.linerHeatPerMb <= 0) return 0.0;
                int flow = Math.max(1, msr.structuralLinerCount) * msr.linerFlowRate;
                return flow * msr.linerHeatPerMb * (1.0 - msr.xenonPoisonLevel);
            }
            if (rods.isEmpty()) return 0.0;
            double rodInteraction = (rods.size() + 1.0) / 2.0;
            double ratio = T / Math.max(1.0, reactor.getMaxSafeHeatHU());
            double thermalScalar = FissionThermalManager.isRated(hm) ? 1.0 :
                    hm.exponentialHeatGrowth ? Math.exp(ratio * hm.heatGenerationExponent) :
                            Math.pow(1.0 + ratio, hm.heatGenerationExponent);
            Map<IFissionFuelRodType, Long> groups = rods.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            double total = 0.0;
            for (var e : groups.entrySet()) {
                total += e.getKey().getBaseHeatProduction() * rodInteraction * thermalScalar * e.getValue() * modBonus *
                        hm.fuelConductivity;
            }
            return total;
        }

        private double computeCooling(double T,
                                      List<IFissionCoolerType> coolers,
                                      PhoenixFissionConfigs.HeatModelConfigs hm) {
            double cooling = 0.0;

            double heatCapacity = reactor.getHeatCapacity();
            double floor = hm.ambientTemperatureHU * heatCapacity;

            double amb = (T - floor) * hm.passiveCoolingConductivity;
            if (amb > 0) cooling += amb;
            Map<IFissionCoolerType, Long> groups = coolers.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            for (var e : groups.entrySet()) {
                IFissionCoolerType c = e.getKey();
                if (c.isPassive()) {
                    if (T > floor) cooling += c.getFlatCoolingHUt() * e.getValue();
                } else if (FissionThermalManager.isRated(hm)) {
                    cooling += FissionThermalManager.ratedCoolingDelivered(
                            FissionThermalManager.resolveRatedCooling(c, hm, heatCapacity), T, floor,
                            reactor.getMaxSafeHeatHU()) * e.getValue();
                } else {
                    double conductivity = c.getConductivity() >= 0 ? c.getConductivity() : hm.activeCoolingConductivity;
                    double coolerTempHU = c.getCoolerTemperature() * heatCapacity;
                    double delta = (T - coolerTempHU) * e.getValue() * conductivity;
                    if (delta > 0) cooling += delta;
                }
            }
            return cooling;
        }

        private double findStableTemperature(List<IFissionFuelRodType> rods,
                                             List<IFissionCoolerType> coolers, double modBonus, double maxT) {
            var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
            double prevNet = computeHeatProduction(0, rods, modBonus, hm) - computeCooling(0, coolers, hm);

            if (prevNet <= 0) return 0.0;
            for (int i = 1; i <= 600; i++) {
                double T = maxT * i / 600;
                double net = computeHeatProduction(T, rods, modBonus, hm) - computeCooling(T, coolers, hm);
                if (prevNet > 0 && net <= 0) {
                    double step = maxT / 600;
                    return (T - step) + step * (prevNet / (prevNet - net));
                }
                prevNet = net;
            }
            return -1.0;
        }

        private static int computeModBonus(List<IFissionModeratorType> mods) {
            int raw = mods.stream().mapToInt(IFissionModeratorType::getEUBoost).sum();
            return Math.min(raw, PhoenixFissionConfigs.INSTANCE.fission.maxEUBoostPercent);
        }

        private static String formatK(double v) {
            if (v >= 1_000_000) return String.format("%.2fM K", v / 1_000_000.0);
            if (v >= 1_000) return String.format("%.1fk K", v / 1_000.0);
            return String.format("%.0f K", v);
        }
    }
}
