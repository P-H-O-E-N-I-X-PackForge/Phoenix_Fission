package net.phoenix.phoenix_fission.gametest;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.FissionCoolerBlock;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.common.PhoenixFissionMachines;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionGameTestFixtures;

/**
 * Places a real Pressurized Fission Reactor controller in a GameTest world, uses GTCEu's own
 * {@code BlockPattern#autoBuild} (the same "auto-construct structure" logic backing the in-game
 * cheat/creative build feature) to fill in the rest of the multiblock, forms it, and asserts on the
 * live reactor: that it actually forms, that the component manager picks up installed fuel
 * rod/cooler/moderator counts (the exact class of bug fixed in FissionReactorFancyUIWidget - this
 * test exists so that regression can't silently reappear), and that heat production/cooling
 * actually tick.
 *
 * <p>Deliberately does not hand-place the structure block-by-block: GTCEu's pattern matcher uses a
 * relative-direction coordinate system (see {@code RelativeDirection}) distinct from the raw x/y/z
 * grid used elsewhere (e.g. {@code MultiblockShapeInfo}'s preview renderer), so autoBuild is the only
 * reliable way to construct an arbitrary pattern's structure without duplicating that internal logic.
 *
 * <p>Uses its own Java-registered {@link PhoenixFissionGameTestFixtures} components rather than any
 * KubeJS script, so the test is fully self-contained and doesn't depend on a pack/dev-environment
 * startup script existing.
 */
@PrefixGameTestTemplate(false)
public class FissionReactorGameTest {

    private static final BlockPos CONTROLLER_POS = new BlockPos(7, 7, 7);

    @GameTest(templateNamespace = PhoenixFission.MOD_ID, template = "pressurized_reactor", timeoutTicks = 400)
    public static void pressurizedReactorFormsAndDetectsComponents(GameTestHelper helper) {
        BlockState controllerState = PhoenixFissionMachines.PRESSURIZED_FISSION_REACTOR.defaultBlockState();
        helper.setBlock(CONTROLLER_POS, controllerState);

        helper.runAtTickTime(5, () -> {
            var machine = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(CONTROLLER_POS));
            if (!(machine instanceof FissionWorkableElectricMultiblockMachine reactor)) {
                helper.fail("Controller position did not resolve to a FissionWorkableElectricMultiblockMachine");
                return;
            }

            reactor.setFrontFacing(Direction.NORTH);

            var pattern = PhoenixFissionMachines.PRESSURIZED_FISSION_REACTOR.getPatternFactory().get();
            var fakePlayer = helper.makeMockPlayer();
            pattern.autoBuild(fakePlayer, reactor.getMultiblockState());

            // Drive the pattern check synchronously ourselves instead of relying on
            // MultiblockControllerMachine#asyncCheckPattern: that method throttles the actual
            // check to only run when (blockEntityOffset + offsetTick) % 4 == 0, so a single
            // manual call is not reliable in a GameTest. checkPatternAt/onStructureFormed are
            // the same public methods the throttled path calls internally.
            if (pattern.checkPatternAt(reactor.getMultiblockState(), false)) {
                reactor.onStructureFormed();
            }

            // The reactor's fuel-rod/cooler slots are a single shared predicate
            // (fissionCoolers().or(fissionFuelRods())): autoBuild fills every one of those cells
            // with whichever single candidate it picks first. The dev-environment KubeJS example
            // script is also loaded here, registering its own cooler/fuel rod types into the same
            // PhoenixAPI maps our predicates draw candidates from, so autoBuild may pick one of
            // those instead of our GameTest fixture block - match by the shared FissionCoolerBlock
            // Java class (common to both Java- and KubeJS-registered coolers) rather than our
            // specific fixture block, and swap it to our fixture fuel rod so the reactor has fuel
            // to run on, then re-check/re-form so the component manager picks up the change.
            var fuelRodState = PhoenixFissionGameTestFixtures.GAMETEST_FUEL_ROD.get().defaultBlockState();
            BlockPos controllerAbs = helper.absolutePos(CONTROLLER_POS);
            var level = helper.getLevel();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            boolean swapped = false;
            // The pattern is at most 5x5x5 and the controller need not sit at its center, so a
            // radius of 6 guarantees full coverage regardless of where in the structure it sits.
            outer: for (int dx = -6; dx <= 6; dx++) {
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
                helper.fail("autoBuild produced no FissionCoolerBlock within radius 6 of the controller to swap to a fuel rod");
                return;
            }

            if (pattern.checkPatternAt(reactor.getMultiblockState(), false)) {
                reactor.onStructureFormed();
            }
        });

        helper.runAtTickTime(20, () -> {
            var machine = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(CONTROLLER_POS));
            if (!(machine instanceof FissionWorkableElectricMultiblockMachine reactor)) {
                helper.fail("Controller position did not resolve to a FissionWorkableElectricMultiblockMachine");
                return;
            }

            var pattern = PhoenixFissionMachines.PRESSURIZED_FISSION_REACTOR.getPatternFactory().get();
            if (!reactor.isFormed() && pattern.checkPatternAt(reactor.getMultiblockState(), false)) {
                reactor.onStructureFormed();
            }

            if (!reactor.isFormed()) {
                var state = reactor.getMultiblockState();
                String detail = "no error info available";
                if (state != null && state.hasError() && state.error != null) {
                    BlockPos origin = helper.absolutePos(BlockPos.ZERO);
                    BlockPos relative = state.error.getPos().subtract(origin);
                    detail = "at world " + state.error.getPos() + " (local " + relative + "): " +
                            state.error.getErrorInfo().getString();
                }
                helper.fail("Reactor did not form after autoBuild (" + detail + ")");
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

            double heatBefore = reactor.getHeat();
            reactor.getRecipeLogic().setWorkingEnabled(true);

            helper.runAtTickTime(80, () -> {
                double heatAfter = reactor.getHeat();
                if (!(heatAfter > heatBefore)) {
                    helper.fail("Expected reactor heat to rise once running with fuel rods installed " +
                            "(before=" + heatBefore + ", after=" + heatAfter + ")");
                    return;
                }
                helper.succeed();
            });
        });
    }

    /**
     * Regression test for the fuel-withdrawal-failure heat spike: {@code FissionFuelManager#consumeFuelTick}
     * used to force {@code heat = maxSafeHeatHU + 1.0} whenever a rod group's item withdrawal came up short,
     * regardless of whether fuel/coolant were actually present that tick - and since a failed withdrawal
     * kept re-triggering (higher heat -> higher consumption thermal scalar -> guaranteed failure again), it
     * produced a runaway no cooling setup could catch. See FissionFuelManager for the fix.
     *
     * <p>Doesn't rely on knowing how much fuel {@code pressurized_reactor.nbt}'s built-in supply actually
     * holds: instead it seeds {@code fuelRemainderPerType} directly with a value large enough that the
     * resulting per-tick withdrawal ({@code directConsume}) can never be satisfied by any reasonable stock,
     * which is exactly the same "we thought we had enough, but the real per-tick draw came up short"
     * mismatch that triggers the bug in practice (see hasFuelAvailableForNextTick only confirming 1 unit
     * exists, not the full scaled amount a tick may need).
     */
    @GameTest(templateNamespace = PhoenixFission.MOD_ID, template = "pressurized_reactor", timeoutTicks = 400)
    public static void noOverheatSpikeWhenFuelWithdrawalFails(GameTestHelper helper) {
        BlockState controllerState = PhoenixFissionMachines.PRESSURIZED_FISSION_REACTOR.defaultBlockState();
        helper.setBlock(CONTROLLER_POS, controllerState);

        helper.runAtTickTime(5, () -> {
            var machine = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(CONTROLLER_POS));
            if (!(machine instanceof FissionWorkableElectricMultiblockMachine reactor)) {
                helper.fail("Controller position did not resolve to a FissionWorkableElectricMultiblockMachine");
                return;
            }

            reactor.setFrontFacing(Direction.NORTH);

            var pattern = PhoenixFissionMachines.PRESSURIZED_FISSION_REACTOR.getPatternFactory().get();
            var fakePlayer = helper.makeMockPlayer();
            pattern.autoBuild(fakePlayer, reactor.getMultiblockState());

            if (pattern.checkPatternAt(reactor.getMultiblockState(), false)) {
                reactor.onStructureFormed();
            }

            var fuelRodState = PhoenixFissionGameTestFixtures.GAMETEST_FUEL_ROD.get().defaultBlockState();
            BlockPos controllerAbs = helper.absolutePos(CONTROLLER_POS);
            var level = helper.getLevel();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            boolean swapped = false;
            outer: for (int dx = -6; dx <= 6; dx++) {
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

            if (pattern.checkPatternAt(reactor.getMultiblockState(), false)) {
                reactor.onStructureFormed();
            }
        });

        helper.runAtTickTime(20, () -> {
            var machine = MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(CONTROLLER_POS));
            if (!(machine instanceof FissionWorkableElectricMultiblockMachine reactor)) {
                helper.fail("Controller position did not resolve to a FissionWorkableElectricMultiblockMachine");
                return;
            }

            var pattern = PhoenixFissionMachines.PRESSURIZED_FISSION_REACTOR.getPatternFactory().get();
            if (!reactor.isFormed() && pattern.checkPatternAt(reactor.getMultiblockState(), false)) {
                reactor.onStructureFormed();
            }

            var comp = reactor.getComponentManager();
            if (!reactor.isFormed() || comp.getActiveFuelRods().isEmpty()) {
                helper.fail("Reactor did not form with at least one fuel rod installed");
                return;
            }

            // Bypass the normal ramp-up and any real availability check entirely: force reactivity
            // to full and seed an impossible-to-satisfy remainder for the installed fuel rod type, so
            // consumeFuelTick's withdrawal is guaranteed to fail no matter what the structure's own
            // built-in fuel supply happens to contain.
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
}