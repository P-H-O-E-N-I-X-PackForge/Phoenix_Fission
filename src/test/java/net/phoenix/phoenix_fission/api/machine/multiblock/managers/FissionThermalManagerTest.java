package net.phoenix.phoenix_fission.api.machine.multiblock.managers;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression coverage for the proportional coolant-consumption formula and per-cooler conductivity
 * fallback introduced alongside {@code coolantReferenceHeatRemovalHUt}. Both are pure functions of
 * their arguments, so they're testable without spinning up a Minecraft/GTCEu runtime.
 */
class FissionThermalManagerTest {

    /** Minimal {@link IFissionCoolerType} stub — only the fields these two formulas actually read are meaningful. */
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
            public Material getMaterial() {
                return null;
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

    // --- resolveConductivity ---

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

    // --- scaledCoolantAmount ---

    @Test
    void scaledCoolantAmount_isProportionalToHeatRemoved() {
        var hm = heatModel();
        hm.coolantReferenceHeatRemovalHUt = 100.0;

        // At exactly the reference removal, consumption equals the configured base rate.
        assertEquals(10, FissionThermalManager.scaledCoolantAmount(10, 1, 100.0, hm));
        // At double the reference removal, consumption doubles too.
        assertEquals(20, FissionThermalManager.scaledCoolantAmount(10, 1, 200.0, hm));
        // At a tenth of the reference removal, consumption scales down proportionally.
        assertEquals(1, FissionThermalManager.scaledCoolantAmount(10, 1, 10.0, hm));
    }

    @Test
    void scaledCoolantAmount_scalesWithBlockCount() {
        var hm = heatModel();
        hm.coolantReferenceHeatRemovalHUt = 100.0;

        // 3 blocks of a 10 mB/t cooler, removing the reference amount in aggregate.
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
    void scaledCoolantAmount_neverGoesNegative_evenWithATinyReference() {
        var hm = heatModel();
        hm.coolantReferenceHeatRemovalHUt = 0.0;

        int result = FissionThermalManager.scaledCoolantAmount(10, 1, 50.0, hm);
        org.junit.jupiter.api.Assertions.assertTrue(result >= 0);
    }
}
