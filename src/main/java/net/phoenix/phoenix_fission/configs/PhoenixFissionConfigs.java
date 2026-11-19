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
        @Configurable.Comment({
                "Register the built-in example progression: 5 fuel rod tiers, 6 active coolers and 4 moderators,",
                "tuned for the RATED heat model so the mod is playable out of the box.",
                "Pack devs defining their own components (KubeJS) should set this to false.",
                "Changing it needs a restart; components already placed in a world vanish when turned off."
        })
        public boolean defaultComponentsEnabled = true;

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
        public RtgConfigs rtg = new RtgConfigs();

        @Configurable
        public FusionConfigs fusion = new FusionConfigs();

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

    public enum ThermalModel {
        RATED,
        CONDUCTIVE
    }

    public static class HeatModelConfigs {

        @Configurable
        @Configurable.Comment({
                "Which heat model reactors use.",
                "RATED (default): every active cooler has a rated cooling in HU/t (ratedCoolingHUt) that it",
                "  delivers in full at the safe-heat limit, scaling down linearly to nothing at ambient.",
                "  Heat production does NOT depend on temperature and fuel burn is steady, so a reactor's",
                "  stable heat is simply: production / total rated cooling = fraction of the way from",
                "  ambient to the safe limit. Under 100% is safe, over 100% eventually melts down.",
                "CONDUCTIVE: the older model - coolers pull heat toward their own temperature through",
                "  conductivity constants and production grows with current heat (see exponentialHeatGrowth).",
                "  Harder to predict, but production feeds back on temperature."
        })
        public ThermalModel thermalModel = ThermalModel.RATED;

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
                "[CONDUCTIVE model; also the fallback for RATED coolers with no explicit ratedCoolingHUt]",
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
                "How heat production scales with current heat.",
                "false (default): production is normalized against maxSafeTempK, so it's bounded to a modest",
                "  multiple of baseline output across the whole safe range - reactors settle into a stable idle",
                "  equilibrium well below maxSafeTempK and effectively can't melt down from thermal effects alone,",
                "  no matter how long they're left running unattended.",
                "true: production grows exponentially with current heat instead of the normalized/bounded curve",
                "  above, so once cooling falls behind, the reactor genuinely spirals toward meltdown rather than",
                "  asymptoting to a safe plateau. Exponential growth is much more sensitive to heatGenerationExponent",
                "  than the default curve is - tune that down first if reactors melt down too easily."
        })
        public boolean exponentialHeatGrowth = false;

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

    public static class RtgConfigs {

        @Configurable
        @Configurable.Comment({
                "Registers the four default RTG cells. Turn off to supply your own (KubeJS 'rtg_cell' items).",
                "Needs a restart."
        })
        public boolean defaultCellsEnabled = true;

        @Configurable
        @Configurable.Comment("Multiplies the EU/t every RTG cell produces.")
        public double euMultiplier = 1.0;

        @Configurable
        @Configurable.Comment({
                "Multiplies how long RTG cells last. Applied when items register, so changing it needs a restart.",
                "Set very high for effectively indefinite cells."
        })
        public double lifespanMultiplier = 1.0;
    }

    public static class FusionConfigs {

        @Configurable
        @Configurable.Comment({
                "Registers the default fusion coils and compute modules. Turn off to supply your own",
                "(KubeJS 'fusion_coil' / 'fusion_compute' blocks or the Java API). Needs a restart."
        })
        public boolean defaultComponentsEnabled = true;

        @Configurable
        @Configurable.Comment({
                "GregTech 7 has no substructures, so there is no compute room here: the Tokamak's compute is this flat",
                "number. It sets parallels, threads and the recipe tier exactly like built compute would."
        })
        public int flatCompute = 100;

        @Configurable
        @Configurable.Comment("Adds the default fusion recipes (a copy of GT's). Turn off to write your own.")
        public boolean defaultRecipesEnabled = true;

        @Configurable
        @Configurable.Comment("EU/t of output per point of plasma power at full throttle and a perfect field.")
        public double euPerHeat = 8.0;

        @Configurable
        @Configurable.Comment("Field strength the outer coils must supply per point of plasma power in use.")
        public double confinementPerHeat = 1.0;

        @Configurable
        @Configurable.Comment("Field drift points gained per second while the field is under-supplied (0-100 quenches).")
        public double driftPerSecond = 2.0;

        @Configurable
        @Configurable.Comment("How long the reactor stays dead after a quench, in ticks.")
        public int quenchCooldownTicks = 1200;

        @Configurable
        @Configurable.Comment("mB of each fuel fluid burned per second at full throttle.")
        public int fuelPerSecondMb = 20;

        @Configurable
        @Configurable.Comment("First fuel fluid.")
        public String fuelAFluidId = "gtceu:deuterium";

        @Configurable
        @Configurable.Comment("Second fuel fluid.")
        public String fuelBFluidId = "gtceu:tritium";

        @Configurable
        @Configurable.Comment("Waste points produced per second per point of plasma power in use.")
        public double wasteRatePerHeat = 0.02;

        @Configurable
        @Configurable.Comment("Waste points that make one Activated Fusion Waste item.")
        public int wastePointsPerItem = 1000;

        @Configurable
        @Configurable.Comment("Contamination gained per second while a waste item has nowhere to go.")
        public double contaminationPerBlockedSecond = 1.5;

        @Configurable
        @Configurable.Comment("Fluid used to purge contamination (an air intake feeding a fluid input hatch).")
        public String purgeFluidId = "gtceu:air";

        @Configurable
        @Configurable.Comment("mB of purge fluid used per second while there is contamination to clear.")
        public int purgePerSecondMb = 40;

        @Configurable
        @Configurable.Comment("Contamination removed per second while purging.")
        public double purgeRate = 2.0;

        @Configurable
        @Configurable.Comment("Compute at which field drift is halved. Drift is divided by (1 + compute / this).")
        public double computeDriftHalf = 100.0;

        @Configurable
        @Configurable.Comment("Compute needed per extra parallel lane (each lane multiplies output, fuel, waste and coolant).")
        public int computePerParallel = 30;

        @Configurable
        @Configurable.Comment("Hard cap on parallel lanes.")
        public int maxParallels = 8;

        @Configurable
        @Configurable.Comment("Compute needed per extra thread (used by recipe batching).")
        public int computePerThread = 80;

        @Configurable
        @Configurable.Comment("Hard cap on threads.")
        public int maxThreads = 4;

        @Configurable
        @Configurable.Comment("Compute needed to run tier 2 fusion recipes.")
        public int tier2Compute = 40;

        @Configurable
        @Configurable.Comment("Compute needed to run tier 3 fusion recipes.")
        public int tier3Compute = 160;

        @Configurable
        @Configurable.Comment("Energy input hatches needed for the first overclock (halves duration, doubles EUt).")
        public int ocHatchesLevel1 = 2;

        @Configurable
        @Configurable.Comment("Energy input hatches needed for the second overclock.")
        public int ocHatchesLevel2 = 8;

        @Configurable
        @Configurable.Comment("Gas held in the chamber to keep it at the pressure setpoint.")
        public String pressureGasFluidId = "gtceu:nitrogen";

        @Configurable
        @Configurable.Comment("mB of pressure gas used per second at a 100% pressure setpoint (scales with the setpoint).")
        public int pressureGasPerSecondMb = 30;

        @Configurable
        @Configurable.Comment("Chamber pressure (0-100) the plasma wants, per percent of throttle.")
        public double pressurePerThrottle = 0.8;

        @Configurable
        @Configurable.Comment("Pressure error that is tolerated before the plasma starts to drift faster.")
        public double pressureTolerance = 15.0;

        @Configurable
        @Configurable.Comment({
                "Item fed in as a neutron source (radiation input). One is used per interval while burning.",
                "The default is the reactor's own waste, so fusion can feed itself."
        })
        public String radiationItemId = "phoenix_fission:fusion_waste";

        @Configurable
        @Configurable.Comment({
                "Outer confinement coils are GT heating coil blocks (cupronickel is the base).",
                "Field strength one cupronickel coil gives."
        })
        public int outerCoilBaseConfinement = 60;

        @Configurable
        @Configurable.Comment("Extra field per coil tier above cupronickel, as a fraction (0.25 = +25% per tier).")
        public double outerCoilTierBoost = 0.25;

        @Configurable
        @Configurable.Comment("EU/t each outer coil draws to hold its field.")
        public long outerCoilUpkeepEut = 100;

        @Configurable
        @Configurable.Comment("mB/s of coolant gas each outer coil uses.")
        public int outerCoilCoolantPerSecond = 1;

        @Configurable
        @Configurable.Comment("Coil tiers below this use the low coolant, this tier and above use the high one.")
        public int outerCoilCoolantTierSplit = 2;

        @Configurable
        @Configurable.Comment("Coolant gas for the lower tiers. Supplied as a gas; the reactor liquefies it internally.")
        public String coolantLowFluidId = "gtceu:nitrogen";

        @Configurable
        @Configurable.Comment("Coolant gas for the higher tiers.")
        public String coolantHighFluidId = "gtceu:helium";

        @Configurable
        @Configurable.Comment("Fluid made when air is used to purge contamination.")
        public String radonFluidId = "gtceu:radon";

        @Configurable
        @Configurable.Comment("mB of radon made per second of purging. Needs a fluid output hatch.")
        public int radonPerPurgeMb = 20;

        @Configurable
        @Configurable.Comment("Seconds a single radiation item lasts.")
        public int radiationItemSeconds = 30;

        @Configurable
        @Configurable.Comment("Output multiplier while the plasma is being irradiated.")
        public double radiationOutputBoost = 1.25;

        @Configurable
        @Configurable.Comment("Waste multiplier while the plasma is being irradiated.")
        public double radiationWasteBoost = 1.5;
    }
}
