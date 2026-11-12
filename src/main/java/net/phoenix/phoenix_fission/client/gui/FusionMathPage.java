package net.phoenix.phoenix_fission.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.FusionReactorMachine;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class FusionMathPage implements CanvasPainter {

    private static final int BG_TOP = 0xFF_02101A;
    private static final int BG_BOT = 0xFF_010508;
    private static final int C_TEAL = 0xFF_00B8A5;
    private static final int C_CYAN = 0xFF_00E5CC;
    private static final int C_MID = 0xFF_7FA3AE;
    private static final int C_DIM = 0xFF_3A5E6A;
    private static final int C_WHITE = 0xFF_DCF0F4;
    private static final int C_GOLD = 0xFF_FFC040;
    private static final int C_GREEN = 0xFF_33FF88;
    private static final int C_RED = 0xFF_FF4444;

    private static final int CONTENT_HEIGHT = 520;

    private final FusionReactorMachine m;
    private int scroll = 0;
    private int y;

    public FusionMathPage(FusionReactorMachine machine) {
        this.m = machine;
    }

    @Override
    public void render(GuiGraphics g, int w, int h, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        GuiDraw.drawGradientRect(g, 0, 0, w, h, BG_TOP, BG_BOT, false);
        g.fill(0, 0, w, 16, BG_TOP);
        g.fill(3, 3, 6, 13, C_TEAL);
        g.drawString(font, "FUSION MATH", 10, 4, C_CYAN, false);
        g.fill(0, 15, w, 16, 0x44_00E5CC);

        int viewH = h - 18;
        int maxScroll = Math.max(0, CONTENT_HEIGHT - viewH);
        scroll = Math.max(0, Math.min(maxScroll, scroll));
        GuiDraw.enableScissor(g, 0, 18, w, h - 2);
        g.pose().pushPose();
        g.pose().translate(0, 18 - scroll, 0);
        paint(g, font, w - 12);
        g.pose().popPose();
        g.disableScissor();

        if (maxScroll > 0) {
            int thumbH = Math.max(12, (int) (viewH * (viewH / (double) CONTENT_HEIGHT)));
            int thumbY = 18 + (int) ((viewH - thumbH) * (scroll / (double) maxScroll));
            g.fill(w - 4, 18, w - 1, h - 2, 0x33_00E5CC);
            g.fill(w - 4, thumbY, w - 1, thumbY + thumbH, 0xAA_00E5CC);
        }
        GuiDraw.drawBorder(g, 0, 0, w, h, C_TEAL, 1);
    }

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        scroll -= (int) (amount * 14);
        return true;
    }

    private void paint(GuiGraphics g, Font font, int w) {
        var cfg = PhoenixFissionConfigs.INSTANCE.fission.fusion;
        int x = 8;
        y = 4;

        double load = m.getThrottle() / 100.0;
        double heat = m.getPlasmaPower() * load;
        double required = heat * cfg.confinementPerHeat;
        double dev = Math.abs(m.getPressure() - m.getRequiredPressure());
        double pFactor = dev > cfg.pressureTolerance ? 1.0 + (dev - cfg.pressureTolerance) / 25.0 : 1.0;
        double computeDiv = 1.0 + m.getCompute() / Math.max(1.0, cfg.computeDriftHalf);
        double driftPerSec = cfg.driftPerSecond * (1.0 + m.getContamination() / 50.0) *
                (m.isCoolantOk() ? 1.0 : 1.5) * pFactor / computeDiv;

        head(g, font, x, w, "THE FIELD");
        text(g, font, x,
                "Inner coils make plasma power; outer coils hold the field. The field has to stay at least as strong as the plasma needs.");
        line(g, font, x, w, "plasma power (all inner coils)", String.valueOf(m.getPlasmaPower()), C_WHITE);
        line(g, font, x, w, "x throttle", Math.round(load * 100) + "%", C_WHITE);
        line(g, font, x, w, "= power in use", fmt(heat), C_GOLD);
        line(g, font, x, w, "field needed = power x " + fmt(cfg.confinementPerHeat), fmt(required), C_WHITE);
        line(g, font, x, w, "field supplied", fmt(m.getConfinement()) + (m.isPowered() ? "" : " (x0.2 unpowered)") +
                (m.isCoolantOk() ? "" : " (x0.6 hot)"), C_WHITE);
        line(g, font, x, w, "ratio supplied / needed", String.format(Locale.ROOT, "%.2fx", m.getFieldRatio()),
                m.getFieldRatio() < 1.0 ? C_RED : m.getFieldRatio() < 1.25 ? C_GOLD : C_GREEN);
        text(g, font, x, "Below 1.0x the plasma drifts. 1.0x to 1.25x creeps slowly. 1.25x or more recovers drift.");
        gap(6);

        head(g, font, x, w, "DRIFT AND QUENCH");
        line(g, font, x, w, "base drift per second", fmt(cfg.driftPerSecond), C_WHITE);
        line(g, font, x, w, "x (1 + contamination / 50)", fmt(1.0 + m.getContamination() / 50.0), C_WHITE);
        line(g, font, x, w, "x 1.5 if coils are overheating", m.isCoolantOk() ? "no" : "yes",
                m.isCoolantOk() ? C_WHITE : C_RED);
        line(g, font, x, w, "x pressure factor", fmt(pFactor), pFactor > 1 ? C_GOLD : C_WHITE);
        line(g, font, x, w, "/ (1 + compute / " + fmt(cfg.computeDriftHalf) + ")", fmt(computeDiv), C_CYAN);
        line(g, font, x, w, "= drift per second when under-supplied", fmt(driftPerSec), C_GOLD);
        line(g, font, x, w, "current drift", fmt(m.getDrift()) + " / 100", m.getDrift() > 70 ? C_RED : C_WHITE);
        text(g, font, x, "At 100 the reactor quenches and stays dead for " + (cfg.quenchCooldownTicks / 20) + "s.");
        gap(6);

        head(g, font, x, w, "PRESSURE");
        line(g, font, x, w, "wanted = throttle x " + fmt(cfg.pressurePerThrottle), fmt(m.getRequiredPressure()),
                C_WHITE);
        line(g, font, x, w, "chamber (follows your setpoint)", fmt(m.getPressure()), C_WHITE);
        line(g, font, x, w, "error", fmt(dev), dev > cfg.pressureTolerance ? C_GOLD : C_GREEN);
        text(g, font, x, "Past " + fmt(cfg.pressureTolerance) +
                " of error drift speeds up. Output also drops by error / 200. Holding the setpoint costs nitrogen.");
        gap(6);

        head(g, font, x, w, "OUTPUT");
        text(g, font, x, "EU/t = power in use x parallels x " + fmt(cfg.euPerHeat) +
                " x irradiation x (1 - drift/200) x (1 - pressure error/200)");
        line(g, font, x, w, "power in use", fmt(heat), C_WHITE);
        line(g, font, x, w, "x parallels", "x" + m.getParallels(), C_CYAN);
        line(g, font, x, w, "x irradiation", m.isRadiationActive() ? "x" + fmt(cfg.radiationOutputBoost) : "x1",
                m.isRadiationActive() ? C_GREEN : C_WHITE);
        line(g, font, x, w, "generated", m.getGeneratedEUt() + " EU/t", C_GOLD);
        line(g, font, x, w, "upkeep (outer coils)", m.getUpkeepEut() + " EU/t", C_WHITE);
        gap(6);

        head(g, font, x, w, "FUEL, WASTE, PURGE");
        line(g, font, x, w, "fuel per second (each)", fmt(cfg.fuelPerSecondMb * load * m.getParallels()) + " mB",
                C_WHITE);
        line(g, font, x, w, "waste points per second", fmt(heat * m.getParallels() * cfg.wasteRatePerHeat), C_WHITE);
        line(g, font, x, w, "points per waste item", String.valueOf(cfg.wastePointsPerItem), C_WHITE);
        line(g, font, x, w, "contamination", fmt(m.getContamination()) + "%",
                m.getContamination() > 25 ? C_GOLD : C_WHITE);
        text(g, font, x, "A full output hatch makes contamination rise " + fmt(cfg.contaminationPerBlockedSecond) +
                "/s. Air purges " + fmt(cfg.purgeRate) + "/s for " + cfg.purgePerSecondMb + " mB/s.");
        gap(6);

        head(g, font, x, w, "COMPUTE, RECIPES, THREADS");
        line(g, font, x, w, "compute (modules in the computer room)", String.valueOf(m.getCompute()), C_CYAN);
        line(g, font, x, w, "parallel lanes = 1 + compute / " + cfg.computePerParallel,
                "x" + m.getParallels() + " (max " + cfg.maxParallels + ")", C_WHITE);
        line(g, font, x, w, "threads = 1 + compute / " + cfg.computePerThread,
                m.getThreads() + " (max " + cfg.maxThreads + ")", C_WHITE);
        line(g, font, x, w, "recipe tier (2 at " + cfg.tier2Compute + ", 3 at " + cfg.tier3Compute + ")",
                String.valueOf(m.getRecipeTier()), C_GOLD);
        line(g, font, x, w, "energy hatches / overclocks", m.getEnergyHatches() + " / x" + m.getOcLevels(), C_CYAN);
        text(g, font, x, "Each overclock halves recipe time and doubles EUt. " + cfg.ocHatchesLevel1 +
                " hatches give x1, " + cfg.ocHatchesLevel2 + " give x2. Extra threads run more recipes at once.");
    }

    private void head(GuiGraphics g, Font font, int x, int w, String title) {
        g.drawString(font, title, x, y, C_CYAN, false);
        g.fill(x, y + 10, x + w - 8, y + 11, 0x66_00B8A5);
        y += 15;
    }

    private void line(GuiGraphics g, Font font, int x, int w, String label, String value, int color) {
        g.drawString(font, font.plainSubstrByWidth(label, w - 8 - font.width(value) - 8), x + 2, y, C_MID, false);
        g.drawString(font, value, x + w - 8 - font.width(value), y, color, false);
        y += 11;
    }

    private void text(GuiGraphics g, Font font, int x, String s) {
        for (var l : font.split(net.minecraft.network.chat.Component.literal(s), 232)) {
            g.drawString(font, l, x + 2, y, C_DIM, false);
            y += 10;
        }
    }

    private void gap(int px) {
        y += px;
    }

    private static String fmt(double v) {
        return Math.abs(v - Math.rint(v)) < 0.005 ? String.valueOf((long) Math.rint(v)) :
                String.format(Locale.ROOT, "%.2f", v);
    }
}
