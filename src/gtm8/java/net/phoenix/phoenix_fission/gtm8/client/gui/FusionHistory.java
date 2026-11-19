package net.phoenix.phoenix_fission.gtm8.client.gui;

import net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.FusionReactorMachine;

public class FusionHistory {

    public static final int LEN = 120;
    private static final long INTERVAL_MS = 500;

    public final double[] output = new double[LEN];
    public final double[] integrity = new double[LEN];
    public final double[] pressure = new double[LEN];
    public final double[] required = new double[LEN];
    public int head = 0;
    public int filled = 0;
    private long lastSampleMs = 0;

    public void sampleIfDue(FusionReactorMachine m) {
        long now = System.currentTimeMillis();
        if (now - lastSampleMs < INTERVAL_MS) return;
        lastSampleMs = now;
        output[head] = m.isFormed() ? m.getGeneratedEUt() : 0;
        integrity[head] = m.isFormed() ? 1.0 - m.getDrift() / 100.0 : 0;
        pressure[head] = m.getPressure();
        required[head] = m.getRequiredPressure();
        head = (head + 1) % LEN;
        filled = Math.min(LEN, filled + 1);
    }

    public int index(int i) {
        return (head - filled + i + LEN * 2) % LEN;
    }
}
