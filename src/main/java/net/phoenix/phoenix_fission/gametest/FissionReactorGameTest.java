package net.phoenix.phoenix_fission.gametest;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.FissionCoolerBlock;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.common.PhoenixFissionMachines;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionDefaultComponents;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionGameTestFixtures;

@PrefixGameTestTemplate(false)
public class FissionReactorGameTest {

    private static final BlockPos CONTROLLER_POS = new BlockPos(7, 7, 7);

    @GameTest(templateNamespace = PhoenixFission.MOD_ID, template = "pressurized_reactor", timeoutTicks = 400)
    public static void pressurizedReactorFormsAndDetectsComponents(GameTestHelper helper) {
        BlockState controllerState = PhoenixFissionMachines.PRESSURIZED_FISSION_REACTOR.get().getBlock()
                .defaultBlockState();
        helper.setBlock(CONTROLLER_POS, controllerState);

        helper.runAtTickTime(5, () -> {
            var machine = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(CONTROLLER_POS));
            if (!(machine instanceof FissionWorkableElectricMultiblockMachine reactor)) {
                helper.fail("Controller position did not resolve to a FissionWorkableElectricMultiblockMachine");
                return;
            }

            reactor.setFrontFacing(Direction.NORTH);

            buildStructure(helper, reactor);
            reactor.checkAndFormStructure();

            var fuelRodState = PhoenixFissionGameTestFixtures.GAMETEST_FUEL_ROD.get().defaultBlockState();
            BlockPos controllerAbs = helper.absolutePos(CONTROLLER_POS);
            var level = helper.getLevel();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            boolean swapped = false;

            outer:
            for (int dx = -6; dx <= 6; dx++) {
                for (int dy = -6; dy <= 6; dy++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        cursor.set(controllerAbs.getX() + dx, controllerAbs.getY() + dy, controllerAbs.getZ() + dz);
                        if (level.getBlockState(cursor).getBlock() instanceof FissionCoolerBlock) {
                            level.setBlock(cursor, fuelRodState, 3);
                            swapped = true;
                            break outer;
                        }
                    }
                }
            }
            if (!swapped) {
                helper.fail(
                        "autoBuild produced no FissionCoolerBlock within radius 6 of the controller to swap to a fuel rod");
                return;
            }

            reactor.checkAndFormStructure();
        });

        helper.runAtTickTime(20, () -> {
            var machine = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(CONTROLLER_POS));
            if (!(machine instanceof FissionWorkableElectricMultiblockMachine reactor)) {
                helper.fail("Controller position did not resolve to a FissionWorkableElectricMultiblockMachine");
                return;
            }

            if (!reactor.isFormed()) reactor.checkAndFormStructure();

            if (!reactor.isFormed()) {
                helper.fail("Reactor did not form after building the structure (" +
                        reactor.getDefaultPatternState().getErrors() + ")");
                return;
            }

            var comp = reactor.getComponentManager();
            if (comp.getActiveFuelRods().isEmpty() || comp.getActiveCoolers().isEmpty() ||
                    comp.getActiveModerators().isEmpty()) {
                helper.fail("Reactor formed but component manager saw fuelRods=" +
                        comp.getActiveFuelRods().size() + " coolers=" + comp.getActiveCoolers().size() +
                        " moderators=" + comp.getActiveModerators().size());
                return;
            }

            // No fuel hatches or running recipe exist in this bare structure, so the reactor's own tick loop
            // never considers it "running". Drive one thermal tick directly with running=true instead: it
            // exercises the real production formula against the detected rods/moderators/coolers.
            reactor.getThermalManager().tickThermalLogic(true);
            double produced = reactor.lastHeatGainedPerTick;
            if (!(produced > 0.0)) {
                helper.fail("Expected a running tick to produce heat with fuel rods installed " +
                        "(lastHeatGainedPerTick=" + produced + ", heat=" + reactor.getHeat() + ")");
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = PhoenixFission.MOD_ID, template = "pressurized_reactor", timeoutTicks = 400)
    public static void noOverheatSpikeWhenFuelWithdrawalFails(GameTestHelper helper) {
        BlockState controllerState = PhoenixFissionMachines.PRESSURIZED_FISSION_REACTOR.get().getBlock()
                .defaultBlockState();
        helper.setBlock(CONTROLLER_POS, controllerState);

        helper.runAtTickTime(5, () -> {
            var machine = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(CONTROLLER_POS));
            if (!(machine instanceof FissionWorkableElectricMultiblockMachine reactor)) {
                helper.fail("Controller position did not resolve to a FissionWorkableElectricMultiblockMachine");
                return;
            }

            reactor.setFrontFacing(Direction.NORTH);

            buildStructure(helper, reactor);
            reactor.checkAndFormStructure();

            var fuelRodState = PhoenixFissionGameTestFixtures.GAMETEST_FUEL_ROD.get().defaultBlockState();
            BlockPos controllerAbs = helper.absolutePos(CONTROLLER_POS);
            var level = helper.getLevel();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            boolean swapped = false;
            outer:
            for (int dx = -6; dx <= 6; dx++) {
                for (int dy = -6; dy <= 6; dy++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        cursor.set(controllerAbs.getX() + dx, controllerAbs.getY() + dy, controllerAbs.getZ() + dz);
                        if (level.getBlockState(cursor).getBlock() instanceof FissionCoolerBlock) {
                            level.setBlock(cursor, fuelRodState, 3);
                            swapped = true;
                            break outer;
                        }
                    }
                }
            }
            if (!swapped) {
                helper.fail(
                        "autoBuild produced no FissionCoolerBlock within radius 6 of the controller to swap to a fuel rod");
                return;
            }

            reactor.checkAndFormStructure();
        });

        helper.runAtTickTime(20, () -> {
            var machine = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(CONTROLLER_POS));
            if (!(machine instanceof FissionWorkableElectricMultiblockMachine reactor)) {
                helper.fail("Controller position did not resolve to a FissionWorkableElectricMultiblockMachine");
                return;
            }

            if (!reactor.isFormed()) reactor.checkAndFormStructure();

            var comp = reactor.getComponentManager();
            if (!reactor.isFormed() || comp.getActiveFuelRods().isEmpty()) {
                helper.fail("Reactor did not form with at least one fuel rod installed");
                return;
            }

            reactor.setReactivityFactor(1.0);
            String fuelKey = comp.getActiveFuelRods().get(0).getName();
            reactor.fuelRemainderPerType.put(fuelKey, 1.0e9);

            double maxSafeHeat = reactor.getMaxSafeHeatHU();
            reactor.getThermalManager().tickThermalLogic(true);

            if (reactor.getHeat() > maxSafeHeat) {
                helper.fail("A failed fuel withdrawal forced heat (" + reactor.getHeat() +
                        ") above maxSafeHeatHU (" + maxSafeHeat +
                        ") - the withdrawal-failure heat spike regressed");
                return;
            }

            helper.succeed();
        });
    }

    /**
     * Every fluid and item id the built-in progression refers to must actually exist - a typo or a material
     * that's missing from this GTCEu version would otherwise silently leave a tier unable to run or cool.
     */
    @GameTest(templateNamespace = PhoenixFission.MOD_ID, template = "pressurized_reactor", timeoutTicks = 20)
    public static void defaultComponentsReferenceRealFluidsAndItems(GameTestHelper helper) {
        var problems = new java.util.ArrayList<String>();
        for (var t : PhoenixFissionDefaultComponents.FUEL_TIERS) {
            requireItem(problems, t.name() + " fuel", t.fuelItem());
            requireItem(problems, t.name() + " output", t.outputItem());
        }
        for (var t : PhoenixFissionDefaultComponents.COOLER_TIERS) {
            requireFluid(problems, t.name() + " coolant", t.coolantIn());
            requireFluid(problems, t.name() + " hot coolant", t.coolantOut());
        }
        if (!problems.isEmpty()) {
            helper.fail("Default components reference ids that don't exist: " + problems);
            return;
        }
        helper.succeed();
    }

    private static void requireItem(java.util.List<String> problems, String what, String id) {
        var rl = net.minecraft.resources.ResourceLocation.tryParse(id);
        if (rl == null || !net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(rl)) {
            problems.add(what + "=" + id);
        }
    }

    private static void requireFluid(java.util.List<String> problems, String what, String id) {
        var rl = net.minecraft.resources.ResourceLocation.tryParse(id);
        if (rl == null || !net.minecraft.core.registries.BuiltInRegistries.FLUID.containsKey(rl)) {
            problems.add(what + "=" + id);
        }
    }

    /** Places every block of the reactor's default pattern around the controller using GTM's own structure helper. */
    private static void buildStructure(GameTestHelper helper, FissionWorkableElectricMultiblockMachine reactor) {
        var blocks = new java.util.HashMap<BlockPos, com.gregtechceu.gtceu.api.multiblock.util.BlockInfo>();
        com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper
                .blockPattern(new it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap())
                .populate(blocks, reactor.getDefaultStructurePattern(),
                        new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<>(), reactor.getFrontFacing(),
                        reactor.getUpwardsFacing(), false);
        BlockPos controllerAbs = helper.absolutePos(CONTROLLER_POS);
        blocks.forEach((rel, info) -> {
            if (rel.equals(BlockPos.ZERO) || info.isAir()) return;
            helper.getLevel().setBlockAndUpdate(controllerAbs.offset(rel), info.getBlockState());
        });
    }
}
