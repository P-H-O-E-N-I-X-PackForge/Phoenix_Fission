package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.ThermoelectricGeneratorMachine;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import javax.annotation.Nonnull;

public class ThermoelectricGeneratorFancyUIWidget extends FancyMachineUIWidget {

    private final ThermoelectricGeneratorMachine machine;

    private static final int BG_TOP = 0xFF04090F;
    private static final int BG_BOT = 0xFF010508;
    private static final int GRID_COL = 0x07_00E5CC;
    private static final int MIST_HUE = 0x0080B8D0;

    private static final int C_TEAL = 0xFF_00AAA0;
    private static final int C_DIM = 0xFF_3A5E6A;
    private static final int C_MID = 0xFF_6A9BAA;
    private static final int C_WHITE = 0xFF_DCF0F4;
    private static final int C_GOLD = 0xFF_FFC44D;
    private static final int C_ORANGE = 0xFF_FF8833;
    private static final int C_RED = 0xFF_FF3333;
    private static final int C_GREEN = 0xFF_33FF88;
    private static final int C_BLUE = 0xFF_44AAFF;
    private static final int C_ICE = 0xFF_AADDFF;

    private static final int C_CRYO = 0xFF_55DDFF;

    private static final int COOLDOWN_MAX = 600;

    public ThermoelectricGeneratorFancyUIWidget(IFancyUIProvider provider, int width, int height) {
        super(provider, width, height);
        this.machine = (ThermoelectricGeneratorMachine) provider;
        setBackground((IGuiTexture) null);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        FissionReactorFancyUIWidget.applyTheme(sideTabsWidget);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics g, int mouseX, int mouseY, float dt) {
        int x = getPosition().x, y = getPosition().y;
        int w = getSize().width, h = getSize().height;

        DrawerHelper.drawGradientRect(g, x, y, w, h, BG_TOP, BG_BOT, false);
        drawGrid(g, x, y, w, h);
        drawScanlines(g, x, y, w, h);
        drawMist(g, x, y, w, h);

        super.drawInBackground(g, mouseX, mouseY, dt);

        drawHudIfHome(g);

        DrawerHelper.drawBorder(g, x, y, w, h, borderColor(), 1);
        DrawerHelper.drawBorder(g, x + 2, y + 2, w - 4, h - 4, 0x22_00E5CC, 1);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawHudIfHome(GuiGraphics g) {

        if (currentPage != mainPage) return;
        if (pageContainer == null || pageContainer.widgets.isEmpty()) return;

        var page = pageContainer.widgets.get(0);
        int px = page.getPosition().x, py = page.getPosition().y;
        int pw = page.getSize().width, ph = page.getSize().height;

        g.enableScissor(px - 2, py - 2, px + pw + 2, py + ph + 2);
        drawHUD(g, px + 6, py + 5, pw - 12);
        g.disableScissor();
    }

    @OnlyIn(Dist.CLIENT)
    private void drawHUD(GuiGraphics g, int x, int y, int W) {
        Font font = Minecraft.getInstance().font;
        boolean formed = machine.isFormed();
        boolean working = formed && machine.getRecipeLogic().isWorking();
        boolean burnt = formed && machine.getCooldownTicks() > 0;
        boolean helium = formed && machine.isHeliumActive();

        y = drawHeader(g, font, x, y, W, formed, working, burnt, helium);
        if (!formed) {
            g.drawString(font, "Structure not complete", x + 4, y + 6, C_DIM, false);
            return;
        }

        y = drawHeatBar(g, font, x, y, W, burnt);

        if (burnt) {
            y = drawCooldownBar(g, font, x, y, W);
        }

        y = drawExchangeStats(g, font, x, y, W, helium);

        drawCoolantStatus(g, font, x, y, working, helium);
    }

    @OnlyIn(Dist.CLIENT)
    private int drawHeader(GuiGraphics g, Font font, int x, int y, int W,
                           boolean formed, boolean working, boolean burnt, boolean helium) {
        String badge;
        int badgeColor;
        if (!formed) {
            badge = "OFFLINE";
            badgeColor = C_DIM;
        } else if (burnt) {
            badge = "BURNT OUT";
            badgeColor = C_RED;
        } else if (helium) {
            badge = "CRYO-BOOST";
            badgeColor = C_CRYO;
        } else if (working) {
            badge = "ACTIVE";
            badgeColor = C_GREEN;
        } else {
            badge = "STANDBY";
            badgeColor = C_GOLD;
        }

        int bw = font.width(badge) + 8, bh = 10;
        int bx = x + W - bw, by = y - 1;
        g.fill(bx, by, bx + bw, by + bh, 0x44_000000);
        DrawerHelper.drawBorder(g, bx, by, bw, bh, (0x88 << 24) | (badgeColor & 0xFFFFFF), 1);
        if (helium && !burnt) {
            g.fill(bx, by + bh - 2, bx + bw, by + bh, 0x66_AADDFF);
        }
        g.drawString(font, badge, bx + 4, by + 1, badgeColor, false);

        int dotColor = !formed ? C_DIM : burnt ? pulsingColor(C_RED, 0xFF_FF8844, 0.012) : helium ?
                pulsingColor(C_CRYO, C_ICE, 0.006) : working ? pulsingColor(C_GREEN, 0xFF_AAFFCC, 0.006) : C_GOLD;
        g.fill(x, y + 2, x + 5, y + 7, dotColor);

        y += 12;
        g.fill(x, y, x + W, y + 1, C_TEAL);
        g.fill(x, y + 1, x + W, y + 2, 0x33_00E5CC);
        return y + 5;
    }

    @OnlyIn(Dist.CLIENT)
    private int drawHeatBar(GuiGraphics g, Font font, int x, int y, int W, boolean burnt) {
        int heat = machine.getHeat();
        double pct = heat / 100.0;

        String label = burnt ? "CORE HEAT - BURNT OUT" : "CORE HEAT";
        g.drawString(font, label, x, y, burnt ? pulsingColor(C_RED, 0xFF_FF8844, 0.010) : C_MID, false);

        String pctStr = heat + "%";
        g.drawString(font, pctStr, x + W - font.width(pctStr), y,
                heat >= 75 ? C_RED : heat >= 40 ? C_ORANGE : C_GOLD, false);
        y += 10;

        int barH = 7, half = W / 2;
        int fillW = (int) (pct * W);
        g.fill(x, y, x + W, y + barH, 0x22_FFFFFF);
        g.fill(x, y, x + 1, y + barH, 0x44_FFFFFF);
        if (fillW > 0) {
            DrawerHelper.drawGradientRect(g, x, y, Math.min(fillW, half), barH, C_GREEN, C_GOLD, true);
            if (fillW > half)
                DrawerHelper.drawGradientRect(g, x + half, y, fillW - half, barH, C_GOLD, C_RED, true);
        }
        if (pct > 0.5 && fillW > 0) {
            long t = System.currentTimeMillis();
            int sw = 14;
            int sOff = (int) ((t / 18) % (fillW + sw));
            int sx1 = Math.max(x + sOff - sw, x), sx2 = Math.min(x + sOff, x + fillW);
            if (sx2 > sx1) {
                int mid = (sx1 + sx2) / 2;
                DrawerHelper.drawGradientRect(g, sx1, y, mid - sx1, barH, 0x00_FFFFFF, 0x33_FFFFFF, true);
                DrawerHelper.drawGradientRect(g, mid, y, sx2 - mid, barH, 0x33_FFFFFF, 0x00_FFFFFF, true);
            }
        }
        int dangerX = x + (int) (0.75 * W);
        g.fill(dangerX, y - 1, dangerX + 1, y + barH + 1, 0xAA_FFFFFF);
        y += barH + 2;
        g.drawString(font, "danger", dangerX - font.width("danger") / 2, y, 0x44_FFFFFF, false);
        y += 10;

        rule(g, x, W, y);
        return y + 4;
    }

    @OnlyIn(Dist.CLIENT)
    private int drawCooldownBar(GuiGraphics g, Font font, int x, int y, int W) {
        int ticks = machine.getCooldownTicks();
        double frac = Math.min(1.0, ticks / (double) COOLDOWN_MAX);
        int fillW = (int) (frac * W);
        int secsLeft = (int) Math.ceil(ticks / 20.0);

        String leftStr = "COOLDOWN";
        String rightStr = secsLeft + "s remaining";
        g.drawString(font, leftStr, x, y, pulsingColor(C_RED, 0xFF_FF8844, 0.010), false);
        g.drawString(font, rightStr, x + W - font.width(rightStr), y, C_ORANGE, false);
        y += 10;

        int barH = 6;
        g.fill(x, y, x + W, y + barH, 0x44_000000);
        if (fillW > 0) {
            DrawerHelper.drawGradientRect(g, x, y, fillW, barH,
                    pulsingColor(C_RED, 0xFF_FF6600, 0.012), 0xFF_440000, true);
        }
        DrawerHelper.drawBorder(g, x, y, W, barH, 0x88_FF3300, 1);
        y += barH + 3;

        rule(g, x, W, y);
        return y + 4;
    }

    @OnlyIn(Dist.CLIENT)
    private int drawExchangeStats(GuiGraphics g, Font font, int x, int y, int W, boolean helium) {
        int len = machine.getLength();

        double baseMulti = 1.5 + Math.max(0, len - 1) * 0.25;
        double fullMulti = helium ? baseMulti * 2.0 : baseMulti;
        double euMulti = fullMulti * fullMulti;
        int baseCoolant = Math.max(1,
                PhoenixFissionConfigs.INSTANCE.fission.thermoelectricGenerator.baseCoolantAmount);
        int coolantMb = (int) (baseCoolant * (1.0 + Math.max(0, len - 1) * 0.2));
        int heliumMb = (int) (25 * (1.0 + Math.max(0, len - 1) * 0.2));

        g.drawString(font, "EXCHANGE STATS", x, y, C_MID, false);
        y += 11;

        y = statRow(g, font, x, y, W, "Columns", len + " layers",
                len > 3 ? C_GREEN : len > 1 ? C_GOLD : C_WHITE);

        String euStr = String.format(java.util.Locale.ROOT, "x%.2f", euMulti) + (helium ? "  (cryo)" : "");
        y = statRow(g, font, x, y, W, "EU multiplier", euStr,
                helium ? C_CRYO : euMulti > 2.0 ? C_GREEN : C_MID);

        String throughputStr = helium ? heliumMb + " mB/s helium  +  " + coolantMb + " mB/s coolant" :
                coolantMb + " mB/s coolant";
        y = statRow(g, font, x, y, W, "Throughput", throughputStr, C_BLUE);

        long maxOut = machine.getMaxHatchOutput();
        String outStr = maxOut > 0 ? GTValues.VNF[machine.getDynamoTier()] + "  (" + formatEU(maxOut) + ")" :
                "No dynamo hatches";
        y = statRow(g, font, x, y, W, "Dynamo output", outStr, maxOut > 0 ? C_ICE : C_DIM);

        rule(g, x, W, y);
        return y + 4;
    }

    @OnlyIn(Dist.CLIENT)
    private void drawCoolantStatus(GuiGraphics g, Font font, int x, int y,
                                   boolean working, boolean helium) {
        if (!working) {
            g.fill(x, y + 2, x + 5, y + 7, C_DIM);
            g.drawString(font, "  Idle - awaiting recipe", x, y, C_DIM, false);
            return;
        }
        if (helium) {
            g.fill(x, y + 2, x + 5, y + 7, pulsingColor(C_CRYO, C_ICE, 0.006));
            g.drawString(font, "  CRYO-BOOST ACTIVE - helium cooling", x, y, C_CRYO, false);
        } else {
            g.fill(x, y + 2, x + 5, y + 7, C_BLUE);
            g.drawString(font, "  Standard loop - configured coolant", x, y, C_ICE, false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static int statRow(GuiGraphics g, Font font, int x, int y, int W,
                               String label, String value, int valueColor) {
        g.drawString(font, label, x, y, C_MID, false);
        g.drawString(font, value, x + W - font.width(value), y, valueColor, false);
        return y + 10;
    }

    private static String formatEU(long eu) {
        if (eu >= 1_000_000) return String.format(java.util.Locale.ROOT, "%.1fM EU/t", eu / 1_000_000.0);
        if (eu >= 1_000) return String.format(java.util.Locale.ROOT, "%.1fk EU/t", eu / 1_000.0);
        return eu + " EU/t";
    }

    private static void rule(GuiGraphics g, int x, int W, int y) {
        g.fill(x, y, x + W, y + 1, 0x33_00E5CC);
    }

    @OnlyIn(Dist.CLIENT)
    private int borderColor() {
        if (!machine.isFormed()) return 0x55_203040;
        int cd = machine.getCooldownTicks();
        if (cd > 0) return pulsingColor(0xBB_FF2222, 0xBB_FF8844, 0.012);
        if (machine.isHeliumActive()) return pulsingColor(0xAA_55DDFF, 0xAA_AADDFF, 0.005);
        double heatPct = machine.getHeat() / 100.0;
        return heatPct > 0.75 ? 0xAA_FF6622 : 0xAA_00E5CC;
    }

    @OnlyIn(Dist.CLIENT)
    private static int pulsingColor(int a, int b, double freq) {
        return lerpRGB(a, b, (Math.sin(System.currentTimeMillis() * freq) + 1.0) * 0.5);
    }

    private static int lerpRGB(int a, int b, double f) {
        int aa = (a >>> 24), ar = (a >> 16 & 0xFF), ag = (a >> 8 & 0xFF), ab = (a & 0xFF);
        int ba = (b >>> 24), br = (b >> 16 & 0xFF), bg = (b >> 8 & 0xFF), bb = (b & 0xFF);
        return ((aa + (int) ((ba - aa) * f)) << 24) | ((ar + (int) ((br - ar) * f)) << 16) |
                ((ag + (int) ((bg - ag) * f)) << 8) | (ab + (int) ((bb - ab) * f));
    }

    @OnlyIn(Dist.CLIENT)
    private void drawGrid(GuiGraphics g, int x, int y, int w, int h) {
        for (int gx = x + 18; gx < x + w; gx += 18) g.fill(gx, y, gx + 1, y + h, GRID_COL);
        for (int gy = y + 18; gy < y + h; gy += 18) g.fill(x, gy, x + w, gy + 1, GRID_COL);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawScanlines(GuiGraphics g, int x, int y, int w, int h) {
        DrawerHelper.drawGradientRect(g, x, y, w, (float) h / 2, 0x10_000000, 0x04_000000, false);
        DrawerHelper.drawGradientRect(g, x, y + (float) h / 2, w, (float) h / 2, 0x04_000000, 0x10_000000, false);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawMist(GuiGraphics g, int x, int y, int w, int h) {
        long t = System.currentTimeMillis();
        int blobH = h / 3;
        int hue = machine.isHeliumActive() ? 0x0055AAFF : (MIST_HUE & 0xFFFFFF);
        for (int i = 0; i < 6; i++) {
            double phase = t * 0.00035 + i * (Math.PI / 3.0);
            int alpha = (int) ((Math.sin(phase) + 1.0) * 7);
            int col = (alpha << 24) | hue;
            DrawerHelper.drawGradientRect(g, x, y + ((float) (i * h) / 6), w, blobH, col, col & 0x00FFFFFF, false);
        }
    }
}
