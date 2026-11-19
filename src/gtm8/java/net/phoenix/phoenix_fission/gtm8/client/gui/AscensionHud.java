package net.phoenix.phoenix_fission.gtm8.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.AscensionReactorMachine;
import net.phoenix.phoenix_fission.gtm8.client.FissionText;

import java.util.Locale;

/**
 * The Ascension Reactor's main page: the phase ladder, three gauges with their bands and a forecast of the next twenty
 * seconds of disturbance on each, the field stability, and four sliders and a SCRAM button.
 */
@OnlyIn(Dist.CLIENT)
public class AscensionHud implements CanvasPainter {

    public interface Controls {

        void set(int which, int value);
    }

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
    private static final int[] GAUGE_COLOR = { 0xFF_44AAFF, 0xFF_FF8833, 0xFF_C98CFF };
    private static final String[] PHASES = { "IGNITION", "COUPLING", "SUSTAIN", "ASCEND" };
    private static final int FORECAST = 20;

    private static final int SLIDER_X = 56;
    private static final int SLIDER_W = 150;
    private static final String[] SLIDER_NAMES = { "RODS", "COILS", "INJECT" };
    private static final int SLIDERS = 3;
    private final int[] sliderY = new int[SLIDERS];

    private final AscensionReactorMachine m;
    private final AscensionHistory history;
    private final Controls controls;
    private int dragging = -1;
    private int lastSent = -1;
    private int scramX;
    private int scramY;
    private int scramW;

    public AscensionHud(AscensionReactorMachine machine, AscensionHistory history, Controls controls) {
        this.m = machine;
        this.history = history;
        this.controls = controls;
    }

    @Override
    public void render(GuiGraphics g, int w, int h, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        history.sampleIfDue(m);
        GuiDraw.drawGradientRect(g, 0, 0, w, h, BG_TOP, BG_BOT, false);

        g.fill(0, 0, w, 16, BG_TOP);
        g.fill(3, 3, 6, 13, C_TEAL);
        g.drawString(font, FissionText.str("phoenix_fission.ui.ascension_hud.ascension_reactor"), 10, 4, C_CYAN, false);
        g.fill(0, 15, w, 16, 0x44_00E5CC);

        String badge;
        int badgeColor;
        if (!m.isFormed()) {
            badge = FissionText.str("phoenix_fission.ui.ascension_hud.offline");
            badgeColor = C_DIM;
        } else if (m.cooldown() > 0) {
            badge = FissionText.str("phoenix_fission.ui.ascension_hud.scrammed_s", m.cooldown());
            badgeColor = C_RED;
        } else if (m.phase() == AscensionReactorMachine.OFF) {
            badge = FissionText.str("phoenix_fission.ui.ascension_hud.standby");
            badgeColor = C_GOLD;
        } else if (m.phase() == AscensionReactorMachine.ASCENDED) {
            badge = FissionText.str("phoenix_fission.ui.ascension_hud.ascended");
            badgeColor = 0xFF_FFFFFF;
        } else {
            badge = PHASES[m.phase() - 1];
            badgeColor = C_GREEN;
        }
        int bw = font.width(badge) + 8;
        int bx = w - bw - 8;
        g.fill(bx, 3, bx + bw, 13, 0x44_000000);
        GuiDraw.drawBorder(g, bx, 3, bw, 10, (0x88 << 24) | (badgeColor & 0xFFFFFF), 1);
        g.drawString(font, badge, bx + 4, 4, badgeColor, false);

        if (!m.isFormed()) {
            g.drawString(font, FissionText.str("phoenix_fission.ui.ascension_hud.structure_not_complete"), 8, 24, C_DIM,
                    false);
            GuiDraw.drawBorder(g, 0, 0, w, h, C_TEAL, 1);
            return;
        }

        ladder(g, font, w);
        int y = 46;
        for (int k = 0; k < 3; k++) y = gauge(g, font, w, y, k);
        y = stability(g, font, w, y);
        y = banner(g, font, w, y);
        controlsBlock(g, font, w, y, mouseX, mouseY);
        GuiDraw.drawBorder(g, 0, 0, w, h, C_TEAL, 1);
    }

    private void ladder(GuiGraphics g, Font font, int w) {
        int phase = m.phase();
        int cw = (w - 16) / 4;
        for (int i = 0; i < 4; i++) {
            int x = 8 + i * cw;
            boolean done = phase > i + 1;
            boolean now = phase == i + 1;
            int col = done || (phase == AscensionReactorMachine.ASCENDED && i == 3) ? C_GREEN : now ? C_CYAN : C_DIM;
            g.fill(x, 21, x + cw - 3, 33, 0x55_000000);
            GuiDraw.drawBorder(g, x, 21, cw - 3, 12, col, 1);
            g.drawString(font, PHASES[i], x + (cw - 3 - font.width(PHASES[i])) / 2, 23, col, false);
            if (now && i < 3) {
                float f = Mth.clamp(m.phaseTimer() / (float) AscensionReactorMachine.PHASE_SECONDS[phase], 0f, 1f);
                g.fill(x + 1, 31, x + 1 + (int) ((cw - 5) * f), 33, C_GREEN);
            } else if (done) {
                g.fill(x + 1, 31, x + cw - 4, 33, C_GREEN);
            }
        }
    }

    private int gauge(GuiGraphics g, Font font, int w, int y, int kind) {
        int x = 8;
        int cw = w - 16;
        boolean live = kind < 2 || m.phase() >= AscensionReactorMachine.COUPLING;
        double v = m.gauge(kind);
        double lo = AscensionReactorMachine.BANDS[kind][0];
        double hi = AscensionReactorMachine.BANDS[kind][1];
        boolean in = v >= lo && v <= hi;
        int color = GAUGE_COLOR[kind];
        g.drawString(font, AscensionReactorMachine.GAUGES[kind], x, y, live ? C_MID : C_DIM, false);
        String value = String.format(Locale.ROOT, "%.0f", v) +
                (live ? "  (" + (int) lo + "-" + (int) hi + ")" : "  idle");
        g.drawString(font, value, x + cw - font.width(value), y, !live ? C_DIM : in ? C_WHITE : C_RED, false);
        int by = y + 10;
        g.fill(x, by, x + cw, by + 8, 0x66_000000);
        g.fill(x + (int) (cw * lo / 100.0), by, x + (int) (cw * hi / 100.0), by + 8, 0x4433FF88);
        g.fill(x, by + 1, x + (int) (cw * Mth.clamp((float) (v / 100.0), 0f, 1f)), by + 7,
                live ? (in ? color : C_RED) : C_DIM);
        GuiDraw.drawBorder(g, x, by, cw, 8, 0x66_00B8A5, 1);
        // the forecast: the next twenty seconds of disturbance on this gauge, drawn up or down from a midline
        int fy = by + 16;
        double amp = AscensionReactorMachine.amplitudeOf(m.phase());
        g.fill(x, fy, x + cw, fy + 1, 0x33_FFFFFF);
        int step = cw / FORECAST;
        for (int j = 0; j < FORECAST; j++) {
            double d = AscensionReactorMachine.disturbance(m.runSeed(), kind, m.runSeconds() + 1 + j, amp);
            int hgt = (int) Mth.clamp(Math.abs(d) * 0.55, 0, 6);
            int fx = x + j * step;
            int fc = d > 0 ? C_ORANGE : 0xFF_44AAFF;
            int a = 0xFF - j * 8;
            if (d > 0) g.fill(fx, fy - hgt, fx + step - 1, fy, (a << 24) | (fc & 0xFFFFFF));
            else g.fill(fx, fy + 1, fx + step - 1, fy + 1 + hgt, (a << 24) | (fc & 0xFFFFFF));
        }
        return y + 36;
    }

    private int stability(GuiGraphics g, Font font, int w, int y) {
        int x = 8;
        int cw = w - 16;
        double s = m.stability();
        g.drawString(font, FissionText.str("phoenix_fission.ui.ascension_graphs_page.field_stability"), x, y, C_MID,
                false);
        String v = Math.round(s) + "%";
        g.drawString(font, v, x + cw - font.width(v), y, s < 30 ? C_RED : C_WHITE, false);
        g.fill(x, y + 10, x + cw, y + 17, 0x66_000000);
        g.fill(x, y + 10, x + (int) (cw * s / 100.0), y + 17, s < 30 ? C_RED : s < 60 ? C_ORANGE : C_GREEN);
        g.fill(x + cw / 2, y + 8, x + cw / 2 + 1, y + 19, 0x88_FFFFFF);
        GuiDraw.drawBorder(g, x, y + 10, cw, 7, 0x66_00B8A5, 1);
        return y + 24;
    }

    private int banner(GuiGraphics g, Font font, int w, int y) {
        String text;
        int color;
        if (m.cooldown() > 0) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.scrammed_the_core_is_held");
            color = C_RED;
        } else if (m.meltdownTimerTicks >= 0) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.meltdown_in_s_cool_the",
                    (m.meltdownTimerTicks / 20 + 1));
            color = C_RED;
        } else if (m.isScramActive()) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.scram_hatch_signal_clear_it");
            color = C_RED;
        } else if (m.event() != AscensionReactorMachine.EVENT_NONE) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.for_s",
                    AscensionReactorMachine.EVENT_NAMES[m.event()], m.eventLeft());
            color = C_RED;
        } else if (m.warnKind() != AscensionReactorMachine.EVENT_NONE) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.warning_in_s",
                    AscensionReactorMachine.EVENT_NAMES[m.warnKind()], m.nextEventIn());
            color = C_GOLD;
        } else if (!m.lastHasCoolant) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.no_coolant_reaching_the_coolers");
            color = C_GOLD;
        } else if (m.phase() == AscensionReactorMachine.OFF) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.waiting_for_fuel_and_active");
            color = C_MID;
        } else if (m.phase() >= AscensionReactorMachine.COUPLING && !m.isGasOk()) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.no_injection_gas_in_the");
            color = C_GOLD;
        } else if (m.phase() >= AscensionReactorMachine.COUPLING && !m.isRingPowered()) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.not_enough_power_for_the");
            color = C_RED;
        } else if (m.phase() == AscensionReactorMachine.ASCENDED) {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.ascended_the_ring_adds_to");
            color = 0xFF_FFFFFF;
        } else {
            text = FissionText.str("phoenix_fission.ui.ascension_hud.hold_all_gauges_in_their");
            color = C_MID;
        }
        g.drawString(font, font.plainSubstrByWidth(text, w - 16), 8, y, color, false);
        return y + 14;
    }

    private void controlsBlock(GuiGraphics g, Font font, int w, int y, int mouseX, int mouseY) {
        for (int i = 0; i < SLIDERS; i++) {
            int sy = y + i * 15;
            sliderY[i] = sy;
            int v = m.control(i);
            g.drawString(font, SLIDER_NAMES[i], 8, sy + 2, C_MID, false);
            g.fill(SLIDER_X, sy + 4, SLIDER_X + SLIDER_W, sy + 9, 0x66_000000);
            g.fill(SLIDER_X, sy + 4, SLIDER_X + (int) (SLIDER_W * v / 100.0), sy + 9, 0x8800B8A5);
            GuiDraw.drawBorder(g, SLIDER_X, sy + 4, SLIDER_W, 5, 0x66_00B8A5, 1);
            int hx = SLIDER_X + (int) (SLIDER_W * v / 100.0);
            g.fill(hx - 2, sy + 1, hx + 2, sy + 12, C_CYAN);
            g.drawString(font, v + "%", SLIDER_X + SLIDER_W + 6, sy + 2, C_WHITE, false);
        }
        // the real reactor's own numbers
        int ry = y + SLIDERS * 15 + 1;
        String cooling = m.lastHasCoolant ? FissionText.str("phoenix_fission.ui.ascension_hud.cooling",
                m.lastProvidedCooling, m.lastRequiredCooling) :
                FissionText.str("phoenix_fission.ui.ascension_hud.no_coolant");
        g.drawString(font, cooling, 8, ry, m.lastHasCoolant ? C_MID : C_RED, false);
        String rods = FissionText.str("phoenix_fission.ui.ascension_hud.x_rods_ring_coils", m.lastParallels,
                m.ringCoils());
        g.drawString(font, rods, w - 8 - font.width(rods), ry, C_MID, false);

        int by = ry + 12;
        scramX = 8;
        scramY = by;
        scramW = 60;
        boolean over = mouseX >= scramX && mouseX < scramX + scramW && mouseY >= scramY && mouseY < scramY + 14;
        g.fill(scramX, scramY, scramX + scramW, scramY + 14, over ? 0x66_FF4444 : 0x44_FF4444);
        GuiDraw.drawBorder(g, scramX, scramY, scramW, 14, over ? C_RED : 0x99_FF4444, 1);
        g.drawString(font, FissionText.str("phoenix_fission.ui.ascension_hud.scram"),
                scramX + (scramW - font.width(FissionText.str("phoenix_fission.ui.ascension_hud.scram"))) / 2,
                scramY + 3, C_RED, false);

        String out = m.lastGeneratedEUt + " + " + m.fusionEut() + " EU/t";
        g.drawString(font, out, 76, by + 1, m.totalEut() > 0 ? C_GOLD : C_DIM, false);
        String stats = FissionText.str("phoenix_fission.ui.ascension_hud.runs_lost_ascended_x", m.scrams(),
                m.ascensions());
        g.drawString(font, stats, 76, by + 10, C_DIM, false);
    }

    private int sliderAt(double mx, double my) {
        for (int i = 0; i < SLIDERS; i++) {
            if (my >= sliderY[i] && my < sliderY[i] + 14 && mx >= SLIDER_X - 4 && mx <= SLIDER_X + SLIDER_W + 4) {
                return i;
            }
        }
        return -1;
    }

    private void setFromMouse(int which, double mx) {
        int value = (int) Math.round(Mth.clamp((mx - SLIDER_X) / SLIDER_W, 0.0, 1.0) * 100.0);
        int key = which * 1000 + value;
        if (key == lastSent) return;
        lastSent = key;
        controls.set(which, value);
    }

    @Override
    public boolean click(double mx, double my, int button) {
        if (button != 0) return false;
        if (mx >= scramX && mx < scramX + scramW && my >= scramY && my < scramY + 14 && scramW > 0) {
            controls.set(AscensionReactorMachine.C_SCRAM, 0);
            return true;
        }
        int which = sliderAt(mx, my);
        if (which >= 0) {
            dragging = which;
            lastSent = -1;
            setFromMouse(which, mx);
            return true;
        }
        return false;
    }

    @Override
    public void drag(double mx, double my, int button) {
        if (dragging >= 0) setFromMouse(dragging, mx);
    }

    @Override
    public void release(int button) {
        dragging = -1;
    }

    @Override
    public boolean scroll(double mx, double my, double amount) {
        int which = sliderAt(mx, my);
        if (which < 0) return false;
        controls.set(which, (int) Mth.clamp(m.control(which) + Math.signum(amount) * 5, 0, 100));
        return true;
    }
}
