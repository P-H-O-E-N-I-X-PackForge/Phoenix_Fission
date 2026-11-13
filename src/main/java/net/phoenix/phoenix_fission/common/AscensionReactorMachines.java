package net.phoenix.phoenix_fission.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.machine.PhoenixPartAbility;
import net.phoenix.phoenix_fission.api.machine.multiblock.AscensionReactorMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.pattern.PhoenixFissionPredicates;
import net.phoenix.phoenix_fission.common.data.PhoenixRecipeTypes;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionBlocks;

import static net.phoenix.phoenix_fission.PhoenixFission.PHOENIX_REGISTRATE;

/**
 * The Ascension Reactor, the last of the Phoenix fission line: a real fission core (fuel rods, coolers, moderators and
 * blankets, as in the Pressurized Fission Reactor) standing inside a ring of fusion coils. Nine by nine across and five
 * high.
 */
public final class AscensionReactorMachines {

    private AscensionReactorMachines() {}

    /** One slice of the build, as five rows from the floor up. */
    private static String[] slice(int z) {
        StringBuilder floor = new StringBuilder();
        StringBuilder[] mid = { new StringBuilder(), new StringBuilder(), new StringBuilder() };
        StringBuilder roof = new StringBuilder();
        for (int x = -4; x <= 4; x++) {
            int m = Math.max(Math.abs(x), Math.abs(z));
            floor.append(m == 4 ? 'F' : 'C');
            char c;
            if (m == 4) c = Math.abs(x) == 4 && Math.abs(z) == 4 ? 'C' : 'W';
            else if (m == 3) c = ((x + z) & 1) == 0 ? 'O' : 'W';
            else if (m == 2) c = x == 0 || z == 0 ? 'N' : ' ';
            else c = x == 0 && z == 0 ? 'M' : 'R';
            for (StringBuilder row : mid) row.append(c);
            roof.append('C');
        }
        if (z == 4) floor.setCharAt(4, 'S');
        return new String[] { floor.toString(), mid[0].toString(), mid[1].toString(), mid[2].toString(),
                roof.toString() };
    }

    public static final MachineEntry<MultiblockMachineDefinition> ASCENSION_REACTOR = PHOENIX_REGISTRATE
            .multiblock("ascension_reactor", AscensionReactorMachine::new)
            .langValue("§bAscension Reactor")
            .recipeType(PhoenixRecipeTypes.PRESSURIZED_FISSION_REACTOR_RECIPES)
            .generator(true)
            .regressWhenWaiting(false)
            .recipeModifiers(FissionWorkableElectricMultiblockMachine::recipeModifier,
                    GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING)
            .tooltips(
                    FissionLang.tr("phoenix_fission.ascension_reactor_machines.the_real_fission_reactor_inside"),
                    FissionLang.tr("phoenix_fission.ascension_reactor_machines.the_core_is_a_full"),
                    FissionLang.tr("phoenix_fission.ascension_reactor_machines.with_the_usual_heat_model"),
                    FissionLang.tr("phoenix_fission.ascension_reactor_machines.flux_heat_and_plasma_are"),
                    FissionLang.tr("phoenix_fission.ascension_reactor_machines.disturbances_you_can_see_coming"),
                    FissionLang.tr("phoenix_fission.ascension_reactor_machines.the_coils_and_the_gas"),
                    FissionLang.tr("phoenix_fission.ascension_reactor_machines.hold_all_three_in_their"),
                    FissionLang.tr("phoenix_fission.ascension_reactor_machines.let_the_field_fail_and"),
                    FissionLang.tr("phoenix_fission.ascension_reactor_machines.the_core_s_output_the"))
            .pattern(definition -> {
                var casing = Predicates.blocks(PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING.get());
                var glass = Predicates.blocks(GTBlocks.FUSION_GLASS.get());
                var shell = casing
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                        .or(Predicates.abilities(PhoenixPartAbility.FISSION_SCRAM).setMaxGlobalLimited(1))
                        .or(Predicates.abilities(PhoenixPartAbility.FISSION_SENSOR).setMaxGlobalLimited(2))
                        .or(Predicates.autoAbilities(definition.getRecipeTypes()));
                var builder = MultiblockPatternBuilder
                        .start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.RIGHT);
                for (int z = -4; z <= 4; z++) {
                    String[] s = slice(z);
                    builder.slice(s[0], s[1], s[2], s[3], s[4]);
                }
                return builder
                        .where('S', Predicates.controller(definition))
                        .where('C', casing)
                        .where('F', shell)
                        .where('W', glass)
                        .where('O', PhoenixFissionPredicates.fusionOuterCoils())
                        .where('N', PhoenixFissionPredicates.fusionInnerCoils())
                        .where('M', PhoenixFissionPredicates.fissionModerators()
                                .or(PhoenixFissionPredicates.fissionBlankets()))
                        .where('R', PhoenixFissionPredicates.fissionCoolers()
                                .or(PhoenixFissionPredicates.fissionFuelRods()))
                        .where(' ', Predicates.air())
                        .build();
            })
            .model(com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel(
                    PhoenixFission.id("block/fission/fissile_reaction_safe_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor")).andThen(
                            b -> b.addDynamicRenderer(
                                    () -> net.phoenix.phoenix_fission.client.render.AscensionReactorRender.INSTANCE)))
            .hasBER(true)
            .register();

    static {
        if (com.gregtechceu.gtceu.GTCEu.isDataGen()) {
            // the model data names its dynamic render, so the render has to be known before it is written
            com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager.register(
                    PhoenixFission.id("ascension_reactor"),
                    net.phoenix.phoenix_fission.client.render.AscensionReactorRender.TYPE);
        }
        PhoenixFissionMachines.registerFissionReactor(ASCENSION_REACTOR, AscensionReactorMachine.class);
    }

    public static void init() {}
}
