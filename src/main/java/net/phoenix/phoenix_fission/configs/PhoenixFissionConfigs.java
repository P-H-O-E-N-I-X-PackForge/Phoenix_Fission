package net.phoenix.phoenix_fission.configs;

import net.phoenix.phoenix_fission.PhoenixFission;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = PhoenixFission.MOD_ID)
public class PhoenixFissionConfigs {

    public static PhoenixFissionConfigs INSTANCE;
    public static ConfigHolder<PhoenixFissionConfigs> CONFIG_HOLDER;

    public static void init() {
        CONFIG_HOLDER = Configuration.registerConfig(PhoenixFissionConfigs.class, ConfigFormats.yaml());
        INSTANCE = CONFIG_HOLDER.getConfigInstance();
    }

    @Configurable
    public FissionConfigs fission = new FissionConfigs();

    public static class FissionConfigs {

        @Configurable
        @Configurable.Comment("Enable the base Fission Reactor multiblock.")
        public boolean fissionReactorEnabled = true;

        @Configurable
        @Configurable.Comment("Enable the High-Performance Breeder Reactor multiblock.")
        public boolean breederReactorEnabled = true;

        @Configurable
        @Configurable.Comment("Enable the Molten Salt Reactor multiblock.")
        public boolean msrEnabled = true;

        @Configurable
        @Configurable.Comment("The Fluid that poisons the Molten Salt Reactor's efficiency")
        public String msrReactorPoisoningOutputFluid = "minecraft:lava";

        @Configurable
        @Configurable.Comment("Enable the Nuke block.")
        public boolean nukeEnabled = true;

        @Configurable
        @Configurable.Comment({
                "If false, the reactor produces no EU/t at all.",
                "Heat, fuel consumption, and cooling still function normally.",
                "Use this for heat-only or steam-only pack configs."
        })
        public boolean enableDirectEUOutput = true;

        @Configurable
        @Configurable.Comment("If false, coolant fluid is never consumed or required (cooling still reduces heat via temperature differential).")
        public boolean coolingRequiresCoolant = true;

        @Configurable
        @Configurable.Comment("If true, every installed cooler type contributes its cooling each tick. If false, only one coolant loop is processed.")
        public boolean coolantUsageAdditive = true;

        @Configurable
        @Configurable.Comment("If true, all installed blanket types are processed each breeding cycle. If false, only the highest-tier blanket is used.")
        public boolean blanketUsageAdditive = true;

        @Configurable
        public HeatModelConfigs heatModel = new HeatModelConfigs();

        @Configurable
        @Configurable.Comment("Base GT recipe parallels contributed by each installed fuel rod.")
        public int parallelsPerFuelRod = 1;

        @Configurable
        @Configurable.Comment("Hard ceiling on total GT recipe parallels.")
        public int maxParallels = 256;

        @Configurable
        public EUOutputConfigs euOutput = new EUOutputConfigs();

        @Configurable
        @Configurable.Comment("Maximum bonus percent applied to heat production and EU output after long continuous run. Example: 30 = up to +30%.")
        public double burnBonusMaxPercent = 30.0;

        @Configurable
        @Configurable.Comment("Seconds of continuous running required to reach the full burn bonus.")
        public double burnBonusRampSeconds = 1200.0;

        @Configurable
        @Configurable.Comment("Maximum total fuel discount percent that moderators can contribute.")
        public int maxFuelDiscountPercent = 90;

        @Configurable
        @Configurable.Comment("Maximum total EU/heat boost percent that moderators can contribute.")
        public int maxEUBoostPercent = 100;

        @Configurable
        public MeltdownConfigs meltdown = new MeltdownConfigs();

        @Configurable
        public ExplosionConfigs explosion = new ExplosionConfigs();

        @Configurable
        public ThermoelectricGeneratorConfigs thermoelectricGenerator = new ThermoelectricGeneratorConfigs();

        @Configurable
        @Configurable.Comment("Cube radius in blocks. Total affected volume is (2r+1)^3.")
        public int nukeCubeRadius = 16;

        @Configurable
        @Configurable.Comment("Hard cap on nuke cube radius to prevent insane values.")
        public int nukeCubeRadiusCap = 48;

        @Configurable
        @Configurable.Comment("Fuse time in ticks before the nuke detonates (20 ticks = 1 second).")
        public int nukeFuseTicks = 120;

        @Configurable
        @Configurable.Comment("Blocks processed per tick during the nuke wipe pass.")
        public int nukeBatchPerTick = 4000;

        @Configurable
        @Configurable.Comment("Skip blocks containing a BlockEntity (machines, chests, etc.) during the nuke wipe.")
        public boolean nukeSkipBlockEntities = true;

        @Configurable
        @Configurable.Comment("Skip blocks in unloaded chunks (prevents forced chunk loading).")
        public boolean nukeSkipUnloadedChunks = true;

        @Configurable
        @Configurable.Comment("Replace destroyed blocks with fire instead of air.")
        public boolean nukeReplaceWithFire = false;
    }

    public static class HeatModelConfigs {

        @Configurable
        @Configurable.Comment({
                "Safe operating temperature threshold in Kelvin.",
                "Meltdown sequence begins when the reactor temperature exceeds this value.",
                "Actual HU threshold = maxSafeTempK * heatCapacity (scales with multi size).",
                "Default 1000 K matches old 100 000 HU behaviour for a 100-block reactor."
        })
        public double maxSafeTempK = 1000.0;

        @Configurable
        @Configurable.Comment("Heat floor - reactor cannot cool below ambient temperature.")
        public double minHeat = 0.0;

        @Configurable
        @Configurable.Comment({
                "Absolute temperature ceiling in Kelvin to prevent numeric overflow.",
                "Actual HU ceiling = maxHeatClampTempK * heatCapacity (scales with multi size).",
                "Default 2500 K matches old 250 000 HU behaviour for a 100-block reactor."
        })
        public double maxHeatClampTempK = 2500.0;

        @Configurable
        @Configurable.Comment({
                "Heat capacity per structural block (HU per Kelvin per block).",
                "Temperature (K) = storedHeat (HU) / heatCapacity.",
                "heatCapacity = blockCount * heatCapacityPerBlock.",
                "Larger reactors therefore have a higher HU buffer at the same K threshold,",
                "giving bigger structures a greater safety margin without changing the K thresholds."
        })
        public double heatCapacityPerBlock = 1.0;

        @Configurable
        @Configurable.Comment({
                "Ambient temperature in Kelvin.",
                "Initialized as room temperature (293K). Passive cooling pulls toward this value."
        })
        public double ambientTemperatureHU = 293.0;

        @Configurable
        @Configurable.Comment({
                "Fuel heat conductivity constant (C_fuel).",
                "Scales how effectively your fuel rods emit thermal energy.",
                "Formula: baseHeatRate * ((totalRods+1)/2) * (1 + temp/maxSafeTemp)^2 * parallels *" +
                        " moderatorBonus * reactivity * C_fuel"
        })
        public double fuelConductivity = 1.0;

        @Configurable
        @Configurable.Comment("Exponent scaling for the fuel consumption rate curve. Xefyr recommends: 4.0")
        public double fuelConsumptionExponent = 4.0;

        @Configurable
        @Configurable.Comment("Exponent scaling for the fuel heat generation curve. Xefyr recommends: 2.0")
        public double heatGenerationExponent = 2.0;

        @Configurable
        @Configurable.Comment({
                "Active cooling conductivity constant (C_active).",
                "Scales how effectively coolant cells transfer heat away.",
                "Formula: (coolantTemp - reactorTemp) * parallels * C_active"
        })
        public double activeCoolingConductivity = 0.25;

        @Configurable
        @Configurable.Comment({
                "Passive cooling conductivity constant (C_passive).",
                "Scales the thermal leak rate to ambient air.",
                "Formula: (ambientTemp - reactorTemp) * C_passive"
        })
        public double passiveCoolingConductivity = 0.005;

        @Configurable
        @Configurable.Comment({
                "How quickly the reactor ramps reactivity up or down when control state changes, " +
                        "as a fraction per tick.",
                "1.0 = instant transition, 0.02 = ~1-second ramp, 0.005 = ~4-second ramp."
        })
        public double reactivityRampRatePerTick = 1.0;

        @Configurable
        @Configurable.Comment({
                "Reference active-cooling heat removal (HU/t) that corresponds to 100% of an active cooler's",
                "configured coolant usage per tick (both input coolant consumed and hot coolant produced).",
                "Coolant usage scales linearly with how much heat a cooler is actually removing this tick:",
                "coolantConsumed = coolantUsagePerTick * (actualHeatRemoved / coolantReferenceHeatRemovalHUt).",
                "Lower this to make coolers burn through coolant faster as load increases; " +
                        "raise it to soften the scaling."
        })
        public double coolantReferenceHeatRemovalHUt = 100.0;
    }

    public static class EUOutputConfigs {

        @Configurable
        @Configurable.Comment("EU generated per unit of heat activity per tick.")
        public double euPerHeatUnit = 1.0;

        @Configurable
        @Configurable.Comment("Optional EU/t ceiling. Set <= 0 for no cap.")
        public long maxGeneratedEUt = 0;

        @Configurable
        @Configurable.Comment("Minimum EU/t while the reactor is active. Set 0 to allow zero output.")
        public long minGeneratedEUt = 1024;

        @Configurable
        @Configurable.Comment("Exponent for the heat-to-power curve. 1 = linear, >1 rewards high heat.")
        public double powerCurveExponent = 2.0;

        @Configurable
        @Configurable.Comment("Heat fraction of maxSafeHeat at which EU generation begins. 0.0 = always generating.")
        public double powerStartFraction = 0.0;
    }

    public static class MeltdownConfigs {

        @Configurable
        @Configurable.Comment("Grace period in seconds when heat barely exceeds maxSafeHeat.")
        public double baseGraceSeconds = 60.0;

        @Configurable
        @Configurable.Comment("Minimum grace period in seconds when heat is far above maxSafeHeat.")
        public double minGraceSeconds = 15.0;

        @Configurable
        @Configurable.Comment("Severity multiplier - higher values shorten the grace period faster as heat rises.")
        public double excessHeatSeverity = 1.0;

        @Configurable
        @Configurable.Comment("If true, dropping back below maxSafeHeat cancels the meltdown timer.")
        public boolean clearTimerWhenSafe = true;
    }

    public static class ExplosionConfigs {

        @Configurable
        @Configurable.Comment("If true, replaces blocks with air/fire. " +
                "If false, uses standard GT explosion (drops items).")
        public boolean destructiveExplosion = false;

        @Configurable
        @Configurable.Comment("Base explosion power added before fuel scaling.")
        public float baseExplosionPower = 2.0f;

        @Configurable
        @Configurable.Comment({
                "Additional explosion power per installed fuel rod.",
                "Power = baseExplosionPower + (rodCount * perRod) + (avgFuelHeat * perHeatUnit)."
        })
        public double explosionPowerPerFuelRod = 1.5;

        @Configurable
        @Configurable.Comment("Additional explosion power per unit of average fuel rod base heat production.")
        public double explosionPowerPerHeatUnit = 0.001;
    }

    public static class ThermoelectricGeneratorConfigs {

        @Configurable
        @Configurable.Comment("Cold coolant fluid the Thermoelectric Generator consumes " +
                "(when not running on the helium cryo-boost loop).")
        public String coldCoolantFluidId = "gtceu:distilled_water";

        @Configurable
        @Configurable.Comment("Hot coolant fluid produced from the cold coolant.")
        public String hotCoolantFluidId = "gtceu:steam";

        @Configurable
        @Configurable.Comment({
                "Base mB/s of cold coolant consumed (and hot coolant produced) at 1 exchange column.",
                "Actual usage scales with column count the same way it always has: " +
                        "base * (1 + (columns - 1) * 0.2)."
        })
        public int baseCoolantAmount = 100;
    }
}
