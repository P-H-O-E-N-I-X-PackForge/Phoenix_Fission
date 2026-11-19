package net.phoenix.phoenix_fission.gtm8.client.gui;

import net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.RtgArrayMachine;

public class RtgHistory {

    public static final int LEN = 120;
    private static final long INTERVAL_MS = 500;

    public final double[] output = new double[LEN];
    public final double[] life = new double[LEN];
    public int head = 0;
    public int filled = 0;
    private long lastSampleMs = 0;

    public void sampleIfDue(RtgArrayMachine machine) {
        long now = System.currentTimeMillis();
        if (now - lastSampleMs < INTERVAL_MS) return;
        lastSampleMs = now;

        double lifeSum = 0;
        int n = 0;
        for (String entry : machine.getCellInfo()) {
            String[] p = entry.split("\\|");
            if (p.length < 3) continue;
            try {
                long max = Long.parseLong(p[2]);
                if (max > 0) {
                    lifeSum += Long.parseLong(p[1]) / (double) max;
                    n++;
                }
            } catch (NumberFormatException ignored) {}
        }

        output[head] = machine.isFormed() ? machine.getGeneratedEUt() : 0;
        life[head] = n == 0 ? 0 : lifeSum / n;
        head = (head + 1) % LEN;
        filled = Math.min(LEN, filled + 1);
    }

    public int index(int i) {
        return (head - filled + i + LEN * 2) % LEN;
    }
}
