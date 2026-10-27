package net.phoenix.phoenix_fission.common.data.block;

import org.junit.jupiter.api.Test;

import static net.phoenix.phoenix_fission.common.data.block.PhoenixFissionDefaultComponents.COOLER_TIERS;
import static net.phoenix.phoenix_fission.common.data.block.PhoenixFissionDefaultComponents.FUEL_TIERS;
import static net.phoenix.phoenix_fission.common.data.block.PhoenixFissionDefaultComponents.MODERATOR_TIERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the built-in progression to the balance it was tuned for, using the RATED model's math directly:
 * production = rods * baseHeat * (rods+1)/2, stable heat fraction = production / total rated cooling.
 */
class PhoenixFissionDefaultComponentsTest {

    private static double stableFraction(int rods, int rodTier, int coolers, int coolerTier, double boostPct) {
        double production = rods * FUEL_TIERS.get(rodTier).heatPerRod() * ((rods + 1) / 2.0) * (1.0 + boostPct / 100.0);
        double rated = coolers * COOLER_TIERS.get(coolerTier).ratedHUt();
        return production / rated;
    }

    @Test
    void everyTierIsStrictlyStrongerThanTheLast() {
        for (int i = 1; i < FUEL_TIERS.size(); i++) {
            assertTrue(FUEL_TIERS.get(i).heatPerRod() > FUEL_TIERS.get(i - 1).heatPerRod());
        }
        for (int i = 1; i < COOLER_TIERS.size(); i++) {
            assertTrue(COOLER_TIERS.get(i).ratedHUt() > COOLER_TIERS.get(i - 1).ratedHUt());
        }
        for (int i = 1; i < MODERATOR_TIERS.size(); i++) {
            assertTrue(MODERATOR_TIERS.get(i).euBoost() > MODERATOR_TIERS.get(i - 1).euBoost());
        }
    }

    @Test
    void comfortableBuild_settlesAroundHalfTheSafeLimit_atEveryTier() {
        // 8 rods + 40 same-tier coolers, no moderators.
        for (int tier = 0; tier < FUEL_TIERS.size(); tier++) {
            double f = stableFraction(8, tier, 40, tier, 0);
            assertTrue(f > 0.40 && f < 0.50, "tier " + (tier + 1) + " comfortable build = " + f);
        }
    }

    @Test
    void pushedBuild_isStableOneCoolerTierUp_butOverheatsOnTheSameTier() {
        // 16 rods + 32 coolers: big reactors carry real risk, but a better cooler tier keeps them stable.
        for (int tier = 0; tier < FUEL_TIERS.size(); tier++) {
            double sameTier = stableFraction(16, tier, 32, tier, 0);
            double oneUp = stableFraction(16, tier, 32, tier + 1, 0);
            assertTrue(sameTier > 1.0, "tier " + (tier + 1) + " same-tier pushed = " + sameTier);
            assertTrue(oneUp > 0.80 && oneUp < 0.95, "tier " + (tier + 1) + " one-up pushed = " + oneUp);
        }
    }

    @Test
    void moderators_makeAComfortableBuildHotterButNotUnsafe_atLowTiers() {
        // Six graphite moderators (+60% heat) on a comfortable build.
        double f = stableFraction(8, 0, 40, 0, 6 * MODERATOR_TIERS.get(0).euBoost());
        assertEquals(0.72, f, 0.02);
    }
}
