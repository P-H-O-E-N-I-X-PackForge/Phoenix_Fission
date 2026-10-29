package net.phoenix.phoenix_fission.api.machine.multiblock.managers;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FissionThermalManagerTest {

    private static IFissionCoolerType cooler(int coolerTemperature, int coolantUsagePerTick, double conductivity) {
        return new IFissionCoolerType() {

            @Override
            public @NotNull String getName() {
                return "test_cooler";
            }

            @Override
            public int getCoolerTemperature() {
                return coolerTemperature;
            }

            @Override
            public @NotNull String getRequiredCoolantMaterialId() {
                return "minecraft:water";
            }

            @Override
            public int getCoolantUsagePerTick() {
                return coolantUsagePerTick;
            }

            @Override
            public double getConductivity() {
                return conductivity;
            }

            @Override
            public ResourceLocation getTexture() {
                return null;
            }
        };
    }

    private static PhoenixFissionConfigs.HeatModelConfigs heatModel() {
        return new PhoenixFissionConfigs.HeatModelConfigs();
    }

    @Test
    void resolveConductivity_usesCoolerOverride_whenNonNegative() {
        var hm = heatModel();
        hm.activeCoolingConductivity = 0.25;
        IFissionCoolerType c = cooler(1000, 10, 0.9);

        assertEquals(0.9, FissionThermalManager.resolveConductivity(c, hm));
    }

    @Test
    void resolveConductivity_fallsBackToGlobal_whenNegative() {
        var hm = heatModel();
        hm.activeCoolingConductivity = 0.25;
        IFissionCoolerType c = cooler(1000, 10, -1.0);

        assertEquals(0.25, FissionThermalManager.resolveConductivity(c, hm));
    }

    @Test
    void scaledCoolantAmount_isProportionalToHeatRemoved() {
        var hm = heatModel();
        hm.coolantReferenceHeatRemovalHUt = 100.0;

        assertEquals(10, FissionThermalManager.scaledCoolantAmount(10, 1, 100.0, hm));

        assertEquals(20, FissionThermalManager.scaledCoolantAmount(10, 1, 200.0, hm));

        assertEquals(1, FissionThermalManager.scaledCoolantAmount(10, 1, 10.0, hm));
    }

    @Test
    void scaledCoolantAmount_scalesWithBlockCount() {
        var hm = heatModel();
        hm.coolantReferenceHeatRemovalHUt = 100.0;

        assertEquals(30, FissionThermalManager.scaledCoolantAmount(10, 3, 100.0, hm));
    }

    @Test
    void scaledCoolantAmount_isZero_whenNoHeatRemoved() {
        var hm = heatModel();
        assertEquals(0, FissionThermalManager.scaledCoolantAmount(10, 1, 0.0, hm));
        assertEquals(0, FissionThermalManager.scaledCoolantAmount(10, 1, -5.0, hm));
    }

    @Test
    void scaledCoolantAmount_isZero_whenBaseAmountOrCountNonPositive() {
        var hm = heatModel();
        assertEquals(0, FissionThermalManager.scaledCoolantAmount(0, 1, 100.0, hm));
        assertEquals(0, FissionThermalManager.scaledCoolantAmount(-10, 1, 100.0, hm));
        assertEquals(0, FissionThermalManager.scaledCoolantAmount(10, 0, 100.0, hm));
    }

    @Test
    void scaledItemAmount_isFractionalAndProportionalToHeatRemoved() {
        var hm = heatModel();
        hm.coolantReferenceHeatRemovalHUt = 100.0;
        assertEquals(0.05, FissionThermalManager.scaledItemAmount(0.05, 1, 100.0, hm), 1e-9);
        assertEquals(0.10, FissionThermalManager.scaledItemAmount(0.05, 1, 200.0, hm), 1e-9);
        assertEquals(0.15, FissionThermalManager.scaledItemAmount(0.05, 3, 100.0, hm), 1e-9);
    }

    @Test
    void scaledItemAmount_isZero_whenNothingToConsume() {
        var hm = heatModel();
        assertEquals(0.0, FissionThermalManager.scaledItemAmount(0.0, 1, 100.0, hm));
        assertEquals(0.0, FissionThermalManager.scaledItemAmount(0.05, 0, 100.0, hm));
        assertEquals(0.0, FissionThermalManager.scaledItemAmount(0.05, 1, 0.0, hm));
    }

    @Test
    void ratedFraction_isZeroAtAmbient_oneAtSafeLimit_andClamped() {
        assertEquals(0.0, FissionThermalManager.ratedFraction(293.0, 293.0, 1000.0), 1e-9);
        assertEquals(0.0, FissionThermalManager.ratedFraction(100.0, 293.0, 1000.0), 1e-9);
        assertEquals(1.0, FissionThermalManager.ratedFraction(1000.0, 293.0, 1000.0), 1e-9);
        assertEquals(1.0, FissionThermalManager.ratedFraction(5000.0, 293.0, 1000.0), 1e-9);
        assertEquals(0.5, FissionThermalManager.ratedFraction(646.5, 293.0, 1000.0), 1e-9);
    }

    @Test
    void ratedCoolingDelivered_scalesRatingByFraction() {
        assertEquals(500.0, FissionThermalManager.ratedCoolingDelivered(1000.0, 646.5, 293.0, 1000.0), 1e-9);
        assertEquals(0.0, FissionThermalManager.ratedCoolingDelivered(1000.0, 200.0, 293.0, 1000.0), 1e-9);
        assertEquals(0.0, FissionThermalManager.ratedCoolingDelivered(-5.0, 900.0, 293.0, 1000.0), 1e-9);
    }

    @Test
    void ratedModel_stableHeatFractionIsProductionOverRatedCooling() {
        // Production 600 HU/t against 1000 HU/t rated: cooling matches production at 60% of the span.
        double ambient = 293.0, safe = 1000.0, rated = 1000.0, production = 600.0;
        double stable = ambient + (production / rated) * (safe - ambient);
        assertEquals(production, FissionThermalManager.ratedCoolingDelivered(rated, stable, ambient, safe), 1e-6);
    }

    @Test
    void scaledCoolantAmount_neverGoesNegative_evenWithATinyReference() {
        var hm = heatModel();
        hm.coolantReferenceHeatRemovalHUt = 0.0;

        int result = FissionThermalManager.scaledCoolantAmount(10, 1, 50.0, hm);
        org.junit.jupiter.api.Assertions.assertTrue(result >= 0);
    }
}
