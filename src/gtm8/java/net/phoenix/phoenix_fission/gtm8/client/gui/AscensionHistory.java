package net.phoenix.phoenix_fission.gtm8.client.gui;

import net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.AscensionReactorMachine;

/** The last minute of the Ascension Reactor's three gauges, sampled while its screen is open. */
public class AscensionHistory {

    public static final int LEN = 120;
    private static final long INTERVAL_MS = 500;

    public final double[] flux = new double[LEN];
    public final double[] heat = new double[LEN];
    public final double[] plasma = new double[LEN];
    public final double[] stability = new double[LEN];
    public int head = 0;
    public int filled = 0;
    private long lastSampleMs = 0;

    public void sampleIfDue(AscensionReactorMachine m) {
        long now = System.currentTimeMillis();
        if (now - lastSampleMs < INTERVAL_MS) return;
        lastSampleMs = now;
        flux[head] = m.isFormed() ? m.flux() : 0;
        heat[head] = m.isFormed() ? m.heatPercent() : 0;
        plasma[head] = m.isFormed() ? m.plasma() : 0;
        stability[head] = m.isFormed() ? m.stability() : 0;
        head = (head + 1) % LEN;
        filled = Math.min(LEN, filled + 1);
    }

    public int index(int i) {
        return (head - filled + i + LEN * 2) % LEN;
    }
}
