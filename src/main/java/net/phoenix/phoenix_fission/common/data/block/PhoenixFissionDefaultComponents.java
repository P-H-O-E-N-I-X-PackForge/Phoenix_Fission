package net.phoenix.phoenix_fission.common.data.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.FissionCoolerBlock;
import net.phoenix.phoenix_fission.api.block.FissionFuelRodBlock;
import net.phoenix.phoenix_fission.api.block.FissionModeratorBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionModeratorType;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;
import net.phoenix.phoenix_fission.datagen.models.PhoenixFissionMachineModels;

import com.tterrag.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.phoenix.phoenix_fission.PhoenixFission.PHOENIX_REGISTRATE;

/**
 * The built-in example progression: five fuel rod tiers, six active coolers and four moderators, tuned
 * against the RATED heat model so a tier-matched build settles at a comfortable fraction of the safe limit.
 * Turn it off with {@code fission.defaultComponentsEnabled = false} to define everything yourself (KubeJS).
 *
 * <p>Reference loadouts for the Pressurized Fission Reactor (48 rod/cooler slots, 14 moderator slots),
 * stable heat = production / total rated cooling, under 100% is safe:
 * <ul>
 * <li>8 rods + 40 same-tier coolers, no moderators: roughly 50%</li>
 * <li>16 rods + 32 coolers one tier up: runs hot but stable; with the same tier of cooler it overheats</li>
 * </ul>
 */
public class PhoenixFissionDefaultComponents {

    public record FuelTier(String name, String display, int heatPerRod, String fuelItem, String outputItem,
                           int durationTicks, int tint) {}

    public record CoolerTier(String name, String display, int ratedHUt, String coolantIn, String coolantOut,
                             int coolantPerTick, int coolerTemperature, int tint) {}

    public record ModeratorTier(String name, String display, int euBoost, int fuelDiscount, int tint) {}

    public static final List<FuelTier> FUEL_TIERS = List.of(
            new FuelTier("t1_fuel_rod", "Tier 1 Fuel Rod", 10, "gtceu:uranium_dust", "gtceu:lead_dust", 1200,
                    0xFF8FBF5A),
            new FuelTier("t2_fuel_rod", "Tier 2 Fuel Rod", 25, "gtceu:uranium_235_dust", "gtceu:uranium_dust", 1000,
                    0xFF5ABF8F),
            new FuelTier("t3_fuel_rod", "Tier 3 Fuel Rod", 60, "gtceu:plutonium_dust", "gtceu:uranium_dust", 900,
                    0xFF5A9FBF),
            new FuelTier("t4_fuel_rod", "Tier 4 Fuel Rod", 150, "gtceu:plutonium_241_dust", "gtceu:plutonium_dust",
                    800, 0xFF8F5ABF),
            new FuelTier("t5_fuel_rod", "Tier 5 Fuel Rod", 380, "gtceu:americium_dust", "gtceu:plutonium_241_dust",
                    700, 0xFFBF5A5A));

    public static final List<CoolerTier> COOLER_TIERS = List.of(
            new CoolerTier("cooler_basic", "Basic Cooler", 20, "minecraft:water", "gtceu:steam", 4, 373, 0xFFBBBBBB),
            new CoolerTier("cooler_mv", "MV Cooler", 50, "gtceu:distilled_water", "gtceu:steam", 4, 373,
                    0xFFFFAA55),
            new CoolerTier("cooler_hv", "HV Cooler", 120, "gtceu:liquid_air", "gtceu:air", 3, 80, 0xFFFFDD55),
            new CoolerTier("cooler_ev", "EV Cooler", 300, "gtceu:liquid_oxygen", "gtceu:oxygen", 3, 90,
                    0xFFAA66FF),
            new CoolerTier("cooler_iv", "IV Cooler", 750, "gtceu:liquid_helium", "gtceu:helium", 2, 4, 0xFF66AACC),
            new CoolerTier("cooler_luv", "LuV Cooler", 1900, "gtceu:helium_3", "gtceu:helium", 2, 3,
                    0xFFFF66AA));

    public static final List<ModeratorTier> MODERATOR_TIERS = List.of(
            new ModeratorTier("graphite_moderator", "Graphite Moderator", 10, 5, 0xFF555555),
            new ModeratorTier("beryllium_moderator", "Beryllium Moderator", 20, 10, 0xFF99BB88),
            new ModeratorTier("heavy_water_moderator", "Heavy Water Moderator", 35, 15, 0xFF5599DD),
            new ModeratorTier("niobium_sic_moderator", "Niobium-SiC Moderator", 50, 25, 0xFF8866CC));

    public static final List<BlockEntry<FissionFuelRodBlock>> FUEL_RODS = new ArrayList<>();
    public static final List<BlockEntry<FissionCoolerBlock>> COOLERS = new ArrayList<>();
    public static final List<BlockEntry<FissionModeratorBlock>> MODERATORS = new ArrayList<>();

    public static boolean enabled() {
        var cfg = PhoenixFissionConfigs.INSTANCE;
        return GTCEu.isDataGen() || (cfg != null && cfg.fission.defaultComponentsEnabled);
    }

    public static void init() {
        if (!enabled()) return;

        for (FuelTier t : FUEL_TIERS) {
            IFissionFuelRodType type = new DefaultFuelRod(t);
            var entry = PHOENIX_REGISTRATE
                    .block(t.name(), p -> new FissionFuelRodBlock(p, type))
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                    .blockstate(PhoenixFissionMachineModels.createFuelRodModel(type))
                    .lang(t.display())
                    .item(BlockItem::new)
                    .build()
                    .register();
            PhoenixAPI.FISSION_FUEL_RODS.put(type, entry::get);
            FUEL_RODS.add(entry);
        }

        for (CoolerTier t : COOLER_TIERS) {
            IFissionCoolerType type = new DefaultCooler(t);
            var entry = PHOENIX_REGISTRATE
                    .block(t.name(), p -> new FissionCoolerBlock(p, type))
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                    .blockstate(PhoenixFissionMachineModels.createActiveCoolerModel(type))
                    .lang(t.display())
                    .item(BlockItem::new)
                    .build()
                    .register();
            PhoenixAPI.FISSION_COOLERS.put(type, entry::get);
            COOLERS.add(entry);
        }

        for (ModeratorTier t : MODERATOR_TIERS) {
            IFissionModeratorType type = new DefaultModerator(t);
            var entry = PHOENIX_REGISTRATE
                    .block(t.name(), p -> new FissionModeratorBlock(p, type))
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                    .blockstate(PhoenixFissionMachineModels.createFissionModeratorModel(type))
                    .lang(t.display())
                    .item(BlockItem::new)
                    .build()
                    .register();
            PhoenixAPI.FISSION_MODERATORS.put(type, entry::get);
            MODERATORS.add(entry);
        }
    }

    private record DefaultFuelRod(FuelTier t) implements IFissionFuelRodType {

        @Override
        public @NotNull String getName() {
            return t.name();
        }

        @Override
        public int getTintColor() {
            return t.tint();
        }

        @Override
        public int getBaseHeatProduction() {
            return t.heatPerRod();
        }

        @Override
        public @NotNull String getFuelKey() {
            return t.fuelItem();
        }

        @Override
        public @NotNull String getOutputKey() {
            return t.outputItem();
        }

        @Override
        public int getDurationTicks() {
            return t.durationTicks();
        }

        @Override
        public int getAmountPerCycle() {
            return 1;
        }

        @Override
        public ResourceLocation getTexture() {
            return PhoenixFission.id("block/fission/fuel_rods/" + t.name());
        }
    }

    private record DefaultCooler(CoolerTier t) implements IFissionCoolerType {

        @Override
        public @NotNull String getName() {
            return t.name();
        }

        @Override
        public int getTintColor() {
            return t.tint();
        }

        @Override
        public int getCoolerTemperature() {
            return t.coolerTemperature();
        }

        @Override
        public @NotNull String getRequiredCoolantMaterialId() {
            return t.coolantIn();
        }

        @Override
        public @NotNull String getOutputCoolantFluidId() {
            return t.coolantOut();
        }

        @Override
        public int getCoolantUsagePerTick() {
            return t.coolantPerTick();
        }

        @Override
        public double getRatedCoolingHUt() {
            return t.ratedHUt();
        }


        @Override
        public ResourceLocation getTexture() {
            return PhoenixFission.id("block/fission/coolers/" + t.name());
        }
    }

    private record DefaultModerator(ModeratorTier t) implements IFissionModeratorType {

        @Override
        public @NotNull String getName() {
            return t.name();
        }

        @Override
        public int getTintColor() {
            return t.tint();
        }

        @Override
        public int getEUBoost() {
            return t.euBoost();
        }

        @Override
        public int getFuelDiscount() {
            return t.fuelDiscount();
        }

        @Override
        public double getHeatMultiplier() {
            return 1.0;
        }

        @Override
        public int getParallelBonus() {
            return 0;
        }


        @Override
        public ResourceLocation getTexture() {
            return PhoenixFission.id("block/fission/moderators/" + t.name());
        }
    }
}
