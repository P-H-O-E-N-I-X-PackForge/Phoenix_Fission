package net.phoenix.phoenix_fission.gtm8.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.FusionReactorMachine;
import net.phoenix.phoenix_fission.gtm8.client.FissionText;

@OnlyIn(Dist.CLIENT)
public class FusionHud implements CanvasPainter {

    private static final int BG_TOP = 0xFF_02101A;
    private static final int BG_BOT = 0xFF_010508;
    private static final int C_TEAL = 0xFF_00B8A5;
    private static final int C_CYAN = 0xFF_00E5CC;
    private static final int C_DIM = 0xFF_3A5E6A;
    private static final int C_MID = 0xFF_7FA3AE;
    private static final int C_WHITE = 0xFF_DCF0F4;
    private static final int C_GREEN = 0xFF_33FF88;
    private static final int C_GOLD = 0xFF_FFC040;
    private static final int C_RED = 0xFF_FF4444;
    private static final int C_ORANGE = 0xFF_FF8833;

    public static final int CONTROLS_H = 26;

    private final FusionReactorMachine m;
    private final FusionHistory history;

    public FusionHud(FusionReactorMachine machine, FusionHistory history) {
        this.m = machine;
        this.history = history;
    }

    @Override
    public void render(GuiGraphics g, int w, int h, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        history.sampleIfDue(m);
        GuiDraw.drawGradientRect(g, 0, 0, w, h, BG_TOP, BG_BOT, false);

        g.fill(0, 0, w, 16, BG_TOP);
        g.fill(3, 3, 6, 13, C_TEAL);
        g.drawString(font, FissionText.str("phoenix_fission.ui.fusion_hud.tokamak_fusion_reactor"), 10, 4, C_CYAN,
                false);
        g.fill(0, 15, w, 16, 0x44_00E5CC);

        boolean formed = m.isFormed();
        String badge;
        int badgeColor;
        if (!formed) {
            badge = FissionText.str("phoenix_fission.ui.ascension_hud.offline");
            badgeColor = C_DIM;
        } else if (m.isScrammed()) {
            badge = FissionText.str("jade.phoenix_fission.fusion_scrammed");
            badgeColor = C_RED;
        } else if (m.getQuenchTicks() > 0) {
            badge = FissionText.str("phoenix_fission.ui.fusion_hud.quenched");
            badgeColor = C_RED;
        } else if (m.isPlasmaOn()) {
            badge = FissionText.str("phoenix_fission.ui.fusion_hud.burning");
            badgeColor = C_GREEN;
        } else {
            badge = FissionText.str("phoenix_fission.ui.ascension_hud.standby");
            badgeColor = C_GOLD;
        }
        int bw = font.width(badge) + 8, bx = w - bw - 8;
        g.fill(bx, 3, bx + bw, 13, 0x44_000000);
        GuiDraw.drawBorder(g, bx, 3, bw, 10, (0x88 << 24) | (badgeColor & 0xFFFFFF), 1);
        g.drawString(font, badge, bx + 4, 4, badgeColor, false);

        int x = 8, cw = w - 16, y = 24;
        if (!formed) {
            g.drawString(font, FissionText.str("phoenix_fission.ui.ascension_hud.structure_not_complete"), x, y, C_DIM,
                    false);
            GuiDraw.drawBorder(g, 0, 0, w, h, C_TEAL, 1);
            return;
        }

        double integrity = 1.0 - m.getDrift() / 100.0;
        g.drawString(font, FissionText.str("phoenix_fission.ui.fusion_graphs_page.field_integrity"), x, y, C_MID,
                false);
        String pct = Math.round(integrity * 100) + "%";
        g.drawString(font, pct, x + cw - font.width(pct), y, integrity < 0.3 ? C_RED : C_WHITE, false);
        bar(g, x, y + 11, cw, 8, integrity, integrity < 0.3 ? C_RED : integrity < 0.6 ? C_ORANGE : C_GREEN);
        y += 26;

        double ratio = m.getFieldRatio();
        g.drawString(font, FissionText.str("phoenix_fission.ui.fusion_hud.confinement"), x, y, C_MID, false);
        String rs = String.format("%.2fx", ratio);
        g.drawString(font, rs, x + cw - font.width(rs), y, ratio < 1.0 ? C_RED : ratio < 1.25 ? C_GOLD : C_GREEN,
                false);
        bar(g, x, y + 11, cw, 8, Math.min(1.0, ratio / 2.0), ratio < 1.0 ? C_RED : C_CYAN);
        g.fill(x + cw / 2, y + 9, x + cw / 2 + 1, y + 21, C_WHITE);
        g.fill(x + (int) (cw * 0.625), y + 9, x + (int) (cw * 0.625) + 1, y + 21, 0x88_FFFFFF);
        y += 28;

        double req = m.getRequiredPressure(), cur = m.getPressure();
        double dev = Math.abs(cur - req);
        g.drawString(font, FissionText.str("phoenix_fission.ui.fusion_graphs_page.pressure"), x, y, C_MID, false);
        String ps = Math.round(cur) + " / " + Math.round(req);
        g.drawString(font, ps, x + cw - font.width(ps), y, dev > 15 ? C_ORANGE : C_WHITE, false);
        bar(g, x, y + 11, cw, 6, Math.min(1.0, cur / 100.0), dev > 15 ? C_ORANGE : C_CYAN);
        int reqX = x + (int) (cw * Math.min(1.0, req / 100.0));
        g.fill(reqX, y + 9, reqX + 1, y + 19, C_WHITE);
        y += 26;

        int rowH = 12, rows = 5, boxH = rows * rowH + 8;
        g.fill(x, y, x + cw, y + boxH, 0x33_00E5CC);
        GuiDraw.drawBorder(g, x, y, cw, boxH, 0x88_00B8A5, 1);
        int ry = y + 5, lx = x + 6, rx = x + cw - 6;
        row(g, font, lx, rx, ry, m.getGeneratedEUt() + " EU/t", C_WHITE,
                m.getInnerCoils() + " inner / " + m.getOuterCoils() + " outer", C_MID);
        ry += rowH;
        row(g, font, lx, rx, ry, "upkeep " + m.getUpkeepEut() + " EU/t", m.isPowered() ? C_MID : C_RED,
                "power " + m.getPlasmaPower(), C_MID);
        ry += rowH;
        row(g, font, lx, rx, ry, m.getRackLayers() == 0 ? "basic compute" : "compute upgrade L" + m.getRackLayers(),
                C_MID,
                "compute " + m.getCompute(), C_MID);
        ry += rowH;
        row(g, font, lx, rx, ry, "x" + m.getParallels() + " parallel", C_CYAN,
                m.getRunningThreads() + "/" + m.getThreads() + " threads", C_CYAN);
        ry += rowH;
        row(g, font, lx, rx, ry, "recipe tier " + m.getRecipeTier(), C_MID,
                "OC x" + m.getOcLevels() + " (" + m.getEnergyHatches() + " hatches)", C_CYAN);
        y += boxH + 8;

        double contam = m.getContamination() / 100.0;
        g.drawString(font, FissionText.str("phoenix_fission.ui.fusion_hud.contamination"), x, y, C_MID, false);
        String cs = Math.round(contam * 100) + "%";
        g.drawString(font, cs, x + cw - font.width(cs), y, contam > 0.6 ? C_RED : contam > 0.25 ? C_ORANGE : C_WHITE,
                false);
        bar(g, x, y + 10, cw, 5, contam, contam > 0.6 ? C_RED : contam > 0.25 ? C_ORANGE : C_GREEN);
        y += 21;

        int ix = x;
        ix = indicator(g, font, ix, y, "COOLANT", m.isCoolantOk());
        ix = indicator(g, font, ix, y, "WASTE", !m.isWasteBlocked());
        ix = indicator(g, font, ix, y, "GAS", m.isGasOk());
        if (m.isRadiationActive()) ix = indicator(g, font, ix, y, "IRRADIATED", true);
        if (m.getContamination() > 0) indicator(g, font, ix, y, "PURGE", m.isPurgeOk());
        y += 14;

        String status;
        int statusColor;
        if (m.isScrammed()) {
            status = FissionText.str("phoenix_fission.ui.fusion_hud.scrammed_clear_the_scram_hatch");
            statusColor = C_RED;
        } else if (m.getQuenchTicks() > 0) {
            status = FissionText.str("phoenix_fission.ui.fusion_hud.quenched_restarting_in_s",
                    (m.getQuenchTicks() / 20 + 1));
            statusColor = C_RED;
        } else if (!m.isPowered()) {
            status = FissionText.str("phoenix_fission.ui.fusion_hud.field_unpowered_supply_energy_to");
            statusColor = C_RED;
        } else if (!m.isFueled()) {
            status = FissionText.str("phoenix_fission.ui.fusion_hud.no_fuel_deuterium_and_tritium");
            statusColor = C_GOLD;
        } else if (!m.isGasOk()) {
            status = FissionText.str("phoenix_fission.ui.fusion_hud.no_pressure_gas_the_chamber");
            statusColor = C_ORANGE;
        } else if (!m.isCoolantOk()) {
            status = FissionText.str("phoenix_fission.ui.fusion_hud.coils_overheating_supply_coolant");
            statusColor = C_ORANGE;
        } else if (m.isWasteBlocked()) {
            status = FissionText.str("phoenix_fission.ui.fusion_hud.waste_backing_up_empty_the");
            statusColor = C_ORANGE;
        } else if (ratio < 1.0) {
            status = FissionText.str("phoenix_fission.ui.fusion_hud.field_too_weak_lower_the");
            statusColor = C_ORANGE;
        } else {
            status = FissionText.str("phoenix_fission.ui.fusion_hud.plasma_stable");
            statusColor = C_GREEN;
        }
        int sy = y;
        for (var line : font.split(Component.literal(status), cw)) {
            if (sy + 9 > h - CONTROLS_H) break;
            g.drawString(font, line, x, sy, statusColor, false);
            sy += 10;
        }

        g.fill(x, h - CONTROLS_H, x + cw, h - CONTROLS_H + 1, 0x44_00E5CC);
        int fieldY = h - 20;
        g.drawString(font, FissionText.label("phoenix_fission.ui.fusion_hud.throttle"), x, fieldY + 3, C_MID, false);
        g.drawString(font, FissionText.label("phoenix_fission.ui.fusion_hud.pressure"), x + cw / 2 + 4, fieldY + 3,
                C_MID, false);
        GuiDraw.drawBorder(g, 0, 0, w, h, C_TEAL, 1);
    }

    private static void row(GuiGraphics g, Font font, int lx, int rx, int y, String left, int lc, String right,
                            int rc) {
        g.drawString(font, left, lx, y, lc, false);
        g.drawString(font, right, rx - font.width(right), y, rc, false);
    }

    private static int indicator(GuiGraphics g, Font font, int x, int y, String label, boolean ok) {
        g.fill(x, y + 1, x + 5, y + 6, ok ? C_GREEN : C_RED);
        g.drawString(font, label, x + 8, y, ok ? C_MID : C_RED, false);
        return x + 8 + font.width(label) + 8;
    }

    private static void bar(GuiGraphics g, int x, int y, int w, int h, double frac, int color) {
        g.fill(x, y, x + w, y + h, 0x33_FFFFFF);
        int fill = (int) Math.round(w * Math.max(0, Math.min(1, frac)));
        if (fill > 0) GuiDraw.drawGradientRect(g, x, y, fill, h, color, color, true);
    }
}
