package net.phoenix.phoenix_fission.common.data.block;

import com.gregtechceu.gtceu.GTCEu;

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

import com.tterrag.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.NotNull;

import static net.phoenix.phoenix_fission.PhoenixFission.PHOENIX_REGISTRATE;

public class PhoenixFissionGameTestFixtures {

    public static final String FUEL_ROD_ID = "gametest_fuel_rod";
    public static final String COOLER_ID = "gametest_cooler";
    public static final String MODERATOR_ID = "gametest_moderator";

    public static BlockEntry<FissionFuelRodBlock> GAMETEST_FUEL_ROD = null;
    public static BlockEntry<FissionCoolerBlock> GAMETEST_COOLER = null;
    public static BlockEntry<FissionModeratorBlock> GAMETEST_MODERATOR = null;

    private static boolean enabled() {
        String namespaces = System.getProperty("forge.enabledGameTestNamespaces", "");
        return GTCEu.isDataGen() || namespaces.contains(PhoenixFission.MOD_ID);
    }

    public static void init() {
        if (!enabled()) return;

        IFissionFuelRodType fuelRodType = new GameTestFuelRodType();
        GAMETEST_FUEL_ROD = PHOENIX_REGISTRATE
                .block(FUEL_ROD_ID, props -> new FissionFuelRodBlock(props, fuelRodType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(ctx.getName(), PhoenixFission.id("block/fission/cooler_base"))))
                .lang("GameTest Fuel Rod")
                .item(BlockItem::new)
                .build()
                .register();
        PhoenixAPI.FISSION_FUEL_RODS.put(fuelRodType, GAMETEST_FUEL_ROD::get);

        IFissionCoolerType coolerType = new GameTestCoolerType();
        GAMETEST_COOLER = PHOENIX_REGISTRATE
                .block(COOLER_ID, props -> new FissionCoolerBlock(props, coolerType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(ctx.getName(), PhoenixFission.id("block/fission/cooler_base"))))
                .lang("GameTest Cooler")
                .item(BlockItem::new)
                .build()
                .register();
        PhoenixAPI.FISSION_COOLERS.put(coolerType, GAMETEST_COOLER::get);

        IFissionModeratorType moderatorType = new GameTestModeratorType();
        GAMETEST_MODERATOR = PHOENIX_REGISTRATE
                .block(MODERATOR_ID, props -> new FissionModeratorBlock(props, moderatorType))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(ctx.getName(), PhoenixFission.id("block/fission/moderator_base"))))
                .lang("GameTest Moderator")
                .item(BlockItem::new)
                .build()
                .register();
        PhoenixAPI.FISSION_MODERATORS.put(moderatorType, GAMETEST_MODERATOR::get);
    }

    private static final class GameTestFuelRodType implements IFissionFuelRodType {

        @Override
        public @NotNull String getName() {
            return FUEL_ROD_ID;
        }

        @Override
        public int getBaseHeatProduction() {
            return 100;
        }

        @Override
        public @NotNull String getFuelKey() {
            return "minecraft:coal";
        }

        @Override
        public @NotNull String getOutputKey() {
            return "minecraft:charcoal";
        }

        @Override
        public int getDurationTicks() {
            return 200;
        }

        @Override
        public int getAmountPerCycle() {
            return 1;
        }

        @Override
        public ResourceLocation getTexture() {
            return PhoenixFission.id("block/fission/cooler_base");
        }
    }

    private static final class GameTestCoolerType implements IFissionCoolerType {

        @Override
        public @NotNull String getName() {
            return COOLER_ID;
        }

        @Override
        public int getCoolerTemperature() {
            return 0;
        }

        @Override
        public @NotNull String getRequiredCoolantMaterialId() {
            return "none";
        }

        @Override
        public int getCoolantUsagePerTick() {
            return 0;
        }

        @Override
        public double getFlatCoolingHUt() {
            return 50.0;
        }

        @Override
        public boolean isPassive() {
            return true;
        }

        @Override
        public ResourceLocation getTexture() {
            return PhoenixFission.id("block/fission/cooler_base");
        }
    }

    private static final class GameTestModeratorType implements IFissionModeratorType {

        @Override
        public @NotNull String getName() {
            return MODERATOR_ID;
        }

        @Override
        public int getEUBoost() {
            return 10;
        }

        @Override
        public int getFuelDiscount() {
            return 5;
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
            return PhoenixFission.id("block/fission/moderator_base");
        }
    }
}
