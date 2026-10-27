package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.MoltenSaltReactorMultiblockMachine;

import java.util.Locale;

import javax.annotation.Nonnull;

public class MSRFancyUIWidget extends FancyMachineUIWidget {

    private final MoltenSaltReactorMultiblockMachine msr;
    private static final int BG_TOP = 0xFF_040A0F;
    private static final int BG_BOT = 0xFF_020508;
    private static final int GRID_COL = 0x07_FFC844;

    private static final int C_SALT = 0xFF_FFC844;
    private static final int C_AMBER = 0xFF_CC8800;
    private static final int C_AMBER_D = 0xFF_664400;
    private static final int C_XENON = 0xFF_BB44FF;
    private static final int C_CYAN = 0xFF_00E5CC;
    private static final int C_DIM = 0xFF_3A5E6A;
    private static final int C_MID = 0xFF_6A9BAA;
    private static final int C_GREEN = 0xFF_33FF88;
    private static final int C_RED = 0xFF_FF3333;
    private static final int C_ORANGE = 0xFF_FF8833;
    private static final int C_BLUE = 0xFF_44AAFF;

    public MSRFancyUIWidget(IFancyUIProvider provider, int width, int height) {
        super(provider, width, height);
        this.msr = (MoltenSaltReactorMultiblockMachine) provider;
        setBackground((IGuiTexture) null);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        FissionReactorFancyUIWidget.applyTheme(sideTabsWidget);
        FissionReactorFancyUIWidget.applySlotTheme(this);
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

        if (currentPage == mainPage) {
            drawTitleBar(g, x, y, w);
        }
        drawHudIfHome(g);

        DrawerHelper.drawBorder(g, x, y, w, h, borderColor(), 1);
        DrawerHelper.drawBorder(g, x + 2, y + 2, w - 4, h - 4, 0x22_FFC844, 1);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawTitleBar(GuiGraphics g, int x, int y, int w) {
        if (pageContainer == null) return;
        int contentY = pageContainer.getPosition().y;
        int titleH = contentY - y;
        if (titleH <= 4) return;

        g.fill(x, y, x + w, contentY, 0xFF_04090F);
        g.fill(x + 3, y + 3, x + 6, contentY - 3, C_AMBER);

        Font font = Minecraft.getInstance().font;
        String title = "MOLTEN SALT REACTOR";
        int titleY = y + (titleH - font.lineHeight) / 2;
        g.drawString(font, title, x + 10, titleY, C_SALT, false);
        g.fill(x, contentY - 1, x + w, contentY, 0x44_FFC844);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawHudIfHome(GuiGraphics g) {

        if (currentPage != mainPage) return;
        if (pageContainer == null || pageContainer.widgets.isEmpty()) return;

        var page = pageContainer.widgets.get(0);
        int px = page.getPosition().x, py = page.getPosition().y;
        int pw = page.getSize().width, ph = page.getSize().height;

        g.enableScissor(px - 2, py - 2, px + pw + 2, py + ph + 2);
        if (msr.meltdownTimerTicks > 0 || (msr.meltdownTimerMax > 0 && msr.isScramActive())) {
            FissionReactorFancyUIWidget.drawMeltdownScreen(
                    g, Minecraft.getInstance().font, px, py, pw, ph, msr);
        } else {
            drawHUD(g, px + 6, py + 5, pw - 12);
        }
        g.disableScissor();
    }

    @OnlyIn(Dist.CLIENT)
    private void drawHUD(GuiGraphics g, int x, int y, int W) {
        Font font = Minecraft.getInstance().font;
        boolean formed = msr.isFormed();
        boolean working = formed && msr.getRecipeLogic().isWorking();
        boolean scram = formed && msr.isScramActive();

        y = drawStatusHeader(g, font, x, y, W, formed, working, scram);
        if (!formed) {
            g.drawString(font, "Structure not complete", x + 4, y + 6, C_DIM, false);
            return;
        }

        y = drawCoreHeat(g, font, x, y, W);
        y = drawXenonBar(g, font, x, y, W);
        y = drawSaltLoop(g, font, x, y, W);
        drawChemistryStatus(g, font, x, y);
    }

    @OnlyIn(Dist.CLIENT)
    private int drawStatusHeader(GuiGraphics g, Font font, int x, int y, int W,
                                 boolean formed, boolean working, boolean scram) {
        String badge;
        int badgeColor;
        if (!formed) {
            badge = "OFFLINE";
            badgeColor = C_DIM;
        } else if (scram) {
            badge = "SCRAM";
            badgeColor = C_RED;
        } else if (working) {
            badge = "CRITICAL";
            badgeColor = C_SALT;
        } else {
            badge = "STANDBY";
            badgeColor = C_AMBER;
        }

        int bw = font.width(badge) + 8, bh = 10;
        int bx = x + W - bw, by = y - 1;
        g.fill(bx, by, bx + bw, by + bh, 0x44_000000);
        DrawerHelper.drawBorder(g, bx, by, bw, bh, (0x88 << 24) | (badgeColor & 0xFFFFFF), 1);
        g.drawString(font, badge, bx + 4, by + 1, badgeColor, false);

        int dotColor = !formed ? C_DIM :
                scram ? pulse(C_RED, 0xFF_FF8844, 0.012) : working ? pulse(C_SALT, 0xFF_FF8833, 0.006) : C_AMBER;
        g.fill(x, y + 2, x + 5, y + 7, dotColor);

        y += 12;
        g.fill(x, y, x + W, y + 1, C_AMBER);
        g.fill(x, y + 1, x + W, y + 2, 0x33_FFC844);
        return y + 5;
    }

    @OnlyIn(Dist.CLIENT)
    private int drawCoreHeat(GuiGraphics g, Font font, int x, int y, int W) {
        double maxSafe = msr.getMaxSafeHeatHU();
        double heat = msr.getHeat();
        double heatPct = maxSafe > 0 ? heat / maxSafe : 0.0;
        double eff = 0.5 + Math.pow(heatPct, 2.0) * 2.0;

        String effStr = String.format(Locale.ROOT, "Eff: %.0f%%", eff * 100.0);
        int effColor = eff >= 2.0 ? C_GREEN : eff >= 1.0 ? C_SALT : C_AMBER;

        g.drawString(font, "SALT CORE", x, y, C_MID, false);
        g.drawString(font, effStr, x + W - font.width(effStr), y, effColor, false);
        y += 10;

        int barH = 7, fillW = (int) (heatPct * W);
        g.fill(x, y, x + W, y + barH, 0x22_FFFFFF);
        if (fillW > 0) {
            int half = W / 2;
            DrawerHelper.drawGradientRect(g, x, y, Math.min(fillW, half), barH,
                    C_AMBER_D, C_AMBER, true);
            if (fillW > half)
                DrawerHelper.drawGradientRect(g, x + half, y, fillW - half, barH,
                        C_AMBER, C_RED, true);
        }
        if (heatPct > 0.4 && fillW > 0) {
            long t = System.currentTimeMillis();
            int sw = 14;
            int sOff = (int) ((t / 20) % (fillW + sw));
            int sx1 = Math.max(x + sOff - sw, x), sx2 = Math.min(x + sOff, x + fillW);
            if (sx2 > sx1) {
                int mid = (sx1 + sx2) / 2;
                DrawerHelper.drawGradientRect(g, sx1, y, mid - sx1, barH, 0x00_FFFFFF, 0x33_FFFFFF, true);
                DrawerHelper.drawGradientRect(g, mid, y, sx2 - mid, barH, 0x33_FFFFFF, 0x00_FFFFFF, true);
            }
        }
        y += barH + 2;

        String heatStr = String.format(Locale.ROOT, "%.0f / %.0f HU", heat, maxSafe);
        g.drawString(font, heatStr, x, y, C_DIM, false);
        y += 10;

        rule(g, x, W, y);
        return y + 4;
    }

    @OnlyIn(Dist.CLIENT)
    private int drawXenonBar(GuiGraphics g, Font font, int x, int y, int W) {
        double xenon = msr.xenonPoisonLevel;
        double xenonPct = xenon * 100.0;
        double barFrac = xenon / 0.50;
        int fillW = (int) (barFrac * W);

        int xenonColor = xenon < 0.05 ? C_GREEN : xenon < 0.20 ? C_SALT : xenon < 0.35 ? C_ORANGE : C_XENON;

        String xenonStr = String.format(Locale.ROOT, "%.1f%%", xenonPct);
        g.drawString(font, "XENON POISON", x, y, C_MID, false);
        g.drawString(font, xenonStr, x + W - font.width(xenonStr), y, xenonColor, false);
        y += 10;

        int barH = 6;
        g.fill(x, y, x + W, y + barH, 0x22_FFFFFF);
        if (fillW > 0) {
            int seg1 = W / 10;
            int seg2 = W * 7 / 10;
            int f1 = Math.min(fillW, seg1);
            if (f1 > 0) DrawerHelper.drawGradientRect(g, x, y, f1, barH, C_GREEN, C_SALT, true);
            if (fillW > seg1) {
                int f2 = Math.min(fillW - seg1, seg2);
                DrawerHelper.drawGradientRect(g, x + seg1, y, f2, barH, C_SALT, C_ORANGE, true);
            }
            if (fillW > seg1 + seg2) {
                int f3 = fillW - seg1 - seg2;
                DrawerHelper.drawGradientRect(g, x + seg1 + seg2, y, f3, barH, C_ORANGE, C_XENON, true);
            }
        }
        int dangerX = x + (int) (0.70 * W);
        g.fill(dangerX, y - 1, dangerX + 1, y + barH + 1, 0x88_FFFFFF);
        y += barH + 2;

        boolean purgeOk = msr.lastXenonPurgeSucceeded;
        boolean cleanCore = xenon < 0.05 && msr.lastGeneratedEUt > 0;

        int pillY = y;
        pillY = drawPill(g, font, x, pillY,
                purgeOk ? "PURGE: OK" : "PURGE: BLOCKED",
                purgeOk ? C_GREEN : C_RED);
        if (cleanCore) drawPill(g, font, x + W / 2, pillY - 10, "CLEAN CORE +50%", C_GREEN);
        y = pillY + 2;

        rule(g, x, W, y);
        return y + 4;
    }

    @OnlyIn(Dist.CLIENT)
    private int drawSaltLoop(GuiGraphics g, Font font, int x, int y, int W) {
        g.drawString(font, "SALT LOOP", x, y, C_MID, false);
        y += 11;

        boolean hasLiner = msr.linerName != null && !msr.linerName.isEmpty();
        String linerLabel = hasLiner ? msr.linerName.toUpperCase() : "No liner";

        y = row(g, font, x, y, W, "Liner", linerLabel,
                hasLiner ? C_CYAN : C_DIM);

        int flowTotal = Math.max(1, msr.structuralLinerCount) * msr.linerFlowRate;
        y = row(g, font, x, y, W, "Flow",
                msr.linerFlowRate > 0 ? flowTotal + " mb/t" : "---",
                C_BLUE);
        y = row(g, font, x, y, W, "Parallels",
                String.valueOf(msr.lastParallels), C_SALT);

        long eu = msr.lastGeneratedEUt;
        String euStr = eu > 0 ? formatEU(eu) : "offline";
        y = row(g, font, x, y, W, "EU output", euStr,
                eu > 0 ? C_GREEN : C_DIM);

        rule(g, x, W, y);
        return y + 4;
    }

    @OnlyIn(Dist.CLIENT)
    private void drawChemistryStatus(GuiGraphics g, Font font, int x, int y) {
        g.drawString(font, "CHEMISTRY", x, y, C_MID, false);
        y += 11;

        if (msr.lastCatalystActive) {
            drawPill(g, font, x, y, "FLUORINE CATALYST  x3", C_XENON);
        } else {
            g.fill(x, y + 3, x + 5, y + 7, C_DIM);
            g.drawString(font, "  No catalyst", x, y, C_DIM, false);
        }
        y += 12;

        if (msr.isScramActive()) {
            g.fill(x, y + 2, x + 5, y + 7, C_RED);
            g.drawString(font, "  SCRAM ENGAGED", x, y, C_RED, false);
        }
    }

    private static int drawPill(GuiGraphics g, Font font, int x, int y, String label, int color) {
        int pw = font.width(label) + 8, ph = 10;
        g.fill(x, y, x + pw, y + ph, 0x44_000000);
        DrawerHelper.drawBorder(g, x, y, pw, ph, (0x88 << 24) | (color & 0xFFFFFF), 1);
        g.drawString(font, label, x + 4, y + 1, color, false);
        return y + ph + 2;
    }

    private static int row(GuiGraphics g, Font font, int x, int y, int W,
                           String label, String value, int valueColor) {
        g.drawString(font, label, x, y, C_MID, false);
        g.drawString(font, value, x + W - font.width(value), y, valueColor, false);
        return y + 10;
    }

    private static void rule(GuiGraphics g, int x, int W, int y) {
        g.fill(x, y, x + W, y + 1, 0x33_FFC844);
    }

    private static String formatEU(long eu) {
        if (eu >= 1_000_000) return String.format(Locale.ROOT, "%.2fM EU/t", eu / 1_000_000.0);
        if (eu >= 1_000) return String.format(Locale.ROOT, "%.1fk EU/t", eu / 1_000.0);
        return eu + " EU/t";
    }

    @OnlyIn(Dist.CLIENT)
    private static int pulse(int a, int b, double freq) {
        return lerpRGB(a, b, (Math.sin(System.currentTimeMillis() * freq) + 1.0) * 0.5);
    }

    private static int lerpRGB(int a, int b, double f) {
        int aa = (a >>> 24), ar = (a >> 16 & 0xFF), ag = (a >> 8 & 0xFF), ab = (a & 0xFF);
        int ba = (b >>> 24), br = (b >> 16 & 0xFF), bg = (b >> 8 & 0xFF), bb = (b & 0xFF);
        return ((aa + (int) ((ba - aa) * f)) << 24) | ((ar + (int) ((br - ar) * f)) << 16) |
                ((ag + (int) ((bg - ag) * f)) << 8) | (ab + (int) ((bb - ab) * f));
    }

    @OnlyIn(Dist.CLIENT)
    private int borderColor() {
        if (!msr.isFormed()) return 0x55_203040;
        if (msr.isScramActive()) return pulse(0xBB_FF2222, 0xBB_FF8844, 0.012);
        if (msr.meltdownTimerTicks > 0) return pulse(0xBB_FF2222, 0xBB_FF8844, 0.020);
        double xenon = msr.xenonPoisonLevel;
        if (xenon > 0.35) return pulse(0xAA_BB44FF, 0xAA_660088, 0.008);
        if (msr.getRecipeLogic().isWorking()) return 0xAA_FFC844;
        return 0x55_FFC844;
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
        double xenon = msr.isFormed() ? msr.xenonPoisonLevel : 0.0;
        int hue = xenon > 0.30 ? lerpRGB(0x00CC8800, 0x00660088, Math.min(1.0, (xenon - 0.30) / 0.20)) & 0xFFFFFF :
                0xCC8800;
        for (int i = 0; i < 6; i++) {
            double phase = t * 0.00030 + i * (Math.PI / 3.0);
            int alpha = (int) ((Math.sin(phase) + 1.0) * 6);
            int col = (alpha << 24) | hue;
            DrawerHelper.drawGradientRect(g, x, y + ((float) (i * h) / 6), w, blobH, col, col & 0x00FFFFFF, false);
        }
    }
}
