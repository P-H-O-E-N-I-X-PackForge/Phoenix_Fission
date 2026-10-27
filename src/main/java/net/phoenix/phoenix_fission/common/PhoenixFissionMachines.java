package net.phoenix.phoenix_fission.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.network.chat.Component;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.machine.PhoenixPartAbility;
import net.phoenix.phoenix_fission.api.machine.multiblock.BreederWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.DynamicFissionReactorMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.MoltenSaltReactorMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.ThermoelectricGeneratorMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.AdvancedFissionScramHatchPart;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.AdvancedFissionStabilitySensorPart;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.FissionScramHatchPart;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.FissionStabilitySensorPart;
import net.phoenix.phoenix_fission.api.pattern.PhoenixFissionPredicates;
import net.phoenix.phoenix_fission.common.data.PhoenixRecipeTypes;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionBlocks;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.VNF;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Steel;
import static net.phoenix.phoenix_fission.PhoenixFission.PHOENIX_REGISTRATE;

@SuppressWarnings("all")
public class PhoenixFissionMachines {

    static {
        PHOENIX_REGISTRATE.creativeModeTab(() -> PhoenixFission.PHOENIX_CREATIVE_TAB);
    }

    public static final String OVERLAY_PLASMA_HATCH_TEX = "overlay_plasma_hatch_input";
    public static final String OVERLAY_PLASMA_HATCH_HALF_PX_TEX = "overlay_plasma_hatch_half_px_out";

    public static final MachineEntry<MachineDefinition>[] FISSION_ADVANCED_STABILITY_SENSOR = registerAdvancedStabilitySensors(
            "fission_advanced_stability_sensor", "Advanced Fission Stability Sensor", GTValues.EV, GTValues.UV);
    public static final MachineEntry<MachineDefinition>[] FISSION_SCRAM_HATCH = registerScramHatches("fission_scram_hatch",
            "Fission SCRAM Hatch", GTValues.HV, GTValues.UV);
    public static final MachineEntry<MachineDefinition>[] FISSION_ADVANCED_SCRAM_HATCH = registerAdvancedScramHatches(
            "fission_advanced_scram_hatch", "Advanced Fission SCRAM Hatch", GTValues.EV, GTValues.UV);
    public static final MachineEntry<MachineDefinition>[] FISSION_STABILITY_SENSOR = registerStabilitySensors(
            "fission_stability_sensor", "Fission Stability Sensor", GTValues.HV, GTValues.UV);

    private static MachineEntry<MachineDefinition>[] registerScramHatches(String name, String displayName, int minTier, int maxTier) {
        final String ioOverlay = "overlay_pipe_in_emissive";
        final String emissiveOverlay = OVERLAY_PLASMA_HATCH_TEX;

        return registerTieredMachines(name,
                (holder, tier) -> new FissionScramHatchPart(holder, tier),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + ' ' + displayName)
                        .rotationState(RotationState.ALL)
                        .abilities(PhoenixPartAbility.FISSION_SCRAM)
                        .colorOverlayTieredHullModel(ioOverlay, null, emissiveOverlay)
                        .tooltips(
                                Component.translatable("phoenix_fission.machine.fission_scram_hatch.tooltip"),
                                Component.translatable("phoenix_fission.machine.fission_scram_hatch.tooltip2"))

                        .register(),
                GTValues.HV);
    }

    public static MachineEntry<MachineDefinition>[] registerTieredMachines(String name,
                                                                           MachineInstanceFactory.Tiered<? extends MetaMachine> factory,
                                                                           BiFunction<Integer, MachineBuilder<MachineDefinition, MetaMachine, ?>, MachineEntry<MachineDefinition>> builder,
                                                                           int... tiers) {
        return GTMachineUtils.registerTieredMachines(PHOENIX_REGISTRATE, name,
                (MachineInstanceFactory.Tiered<MetaMachine>) factory::buildMachine, builder, tiers);
    }

    private static MachineEntry<MachineDefinition>[] registerAdvancedStabilitySensors(String name, String displayName, int minTier,
                                                                        int maxTier) {
        final String ioOverlay = "overlay_pipe_out_emissive";
        final String emissiveOverlay = OVERLAY_PLASMA_HATCH_TEX;

        return registerTieredMachines(name,
                (holder, tier) -> new AdvancedFissionStabilitySensorPart(holder, tier),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + ' ' + displayName)
                        .rotationState(RotationState.ALL)
                        .abilities(PhoenixPartAbility.FISSION_SENSOR)
                        .colorOverlayTieredHullModel(ioOverlay, null, emissiveOverlay)
                        .tooltips(
                                Component.translatable(
                                        "phoenix_fission.machine.fission_advanced_stability_sensor.tooltip"),
                                Component
                                        .translatable(
                                                "phoenix_fission.machine.fission_advanced_stability_sensor.tooltip2"))
                        .register(),
                minTier);
    }

    private static MachineEntry<MachineDefinition>[] registerStabilitySensors(String name, String displayName, int minTier,
                                                                int maxTier) {
        final String ioOverlay = "overlay_pipe_out_emissive";
        final String emissiveOverlay = OVERLAY_PLASMA_HATCH_TEX;

        return registerTieredMachines(name,
                (holder, tier) -> new FissionStabilitySensorPart(holder, tier),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + ' ' + displayName)
                        .abilities(PhoenixPartAbility.FISSION_SENSOR)
                        .rotationState(RotationState.ALL)
                        .colorOverlayTieredHullModel(ioOverlay, null, emissiveOverlay)
                        .tooltips(Component.translatable("phoenix_fission.machine.fission_stability_sensor.tooltip"))
                        .register(),
                GTValues.HV);
    }

    private static MachineEntry<MachineDefinition>[] registerAdvancedScramHatches(String name, String displayName, int minTier,
                                                                    int maxTier) {
        final String ioOverlay = "overlay_pipe_in_emissive";
        final String emissiveOverlay = OVERLAY_PLASMA_HATCH_TEX;

        return registerTieredMachines(name,
                (holder, tier) -> new AdvancedFissionScramHatchPart(holder, tier),
                (tier, builder) -> builder
                        .langValue(VNF[tier] + ' ' + displayName)
                        .rotationState(RotationState.ALL)
                        .abilities(PhoenixPartAbility.FISSION_SCRAM)
                        .colorOverlayTieredHullModel(ioOverlay, null, emissiveOverlay)
                        .tooltips(
                                Component.translatable("phoenix_fission.machine.fission_advanced_scram_hatch.tooltip"),
                                Component.translatable("phoenix_fission.machine.fission_advanced_scram_hatch.tooltip2"))
                        .register(),
                minTier);
    }

    public static MachineEntry<MultiblockMachineDefinition> HIGH_PERFORMANCE_BREEDER_REACTOR = null;
    public static MachineEntry<MultiblockMachineDefinition> MOLTEN_SALT_REACTOR = null;
    public static MachineEntry<MultiblockMachineDefinition> PRESSURIZED_FISSION_REACTOR = null;

    public record FissionReactorEntry(MachineEntry<MultiblockMachineDefinition> definition,
                                      Class<? extends FissionWorkableElectricMultiblockMachine> machineClass) {}

    public static final List<FissionReactorEntry> ALL_FISSION_REACTORS = new ArrayList<>();

    public static void registerFissionReactor(MachineEntry<MultiblockMachineDefinition> definition,
                                              Class<? extends FissionWorkableElectricMultiblockMachine> machineClass) {
        ALL_FISSION_REACTORS.add(new FissionReactorEntry(definition, machineClass));
    }

    static {
        if ((PhoenixFissionConfigs.INSTANCE != null && PhoenixFissionConfigs.INSTANCE.fission.breederReactorEnabled) ||
                GTCEu.isDataGen()) {
            HIGH_PERFORMANCE_BREEDER_REACTOR = PHOENIX_REGISTRATE
                    .multiblock("high_performance_breeder_reactor", BreederWorkableElectricMultiblockMachine::new)
                    .langValue("§bHigh Performance Breeder Reactor")
                    .recipeType(PhoenixRecipeTypes.HIGH_PERFORMANCE_BREEDER_REACTOR_RECIPES)
                    .generator(true)
                    .regressWhenWaiting(false)
                    .recipeModifiers(BreederWorkableElectricMultiblockMachine::recipeModifier,
                            GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
                    .appearanceBlock(PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING)
                    .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                            .slice("BBCCCCCBB", "BBDEEEDBB", "BBDEEEDBB", "BBDEEEDBB", "BBDFFFDBB", "BBDFFFDBB",
                                    "BBCFFFCBB",
                                    "BBCFFFCBB", "BBBBBBBBB")
                            .slice("BCCCCCCCB", "BGAAAAHGB", "BGAAAAHGB", "BGAAAAHGB", "BGAAAAHGB", "BGAAAAHGB",
                                    "BCAAAAHCB",
                                    "BCAAAAHCB", "BBCCCCCBB")
                            .slice("CCCCCCCCC", "DHIAAAIHD", "DHIAAAIHD", "DHIAAAIHD", "DHIAAAIHD", "DHIJJJIHD",
                                    "CHIKKKIHC",
                                    "CHIAAAIHC", "BCCCCCCCB")
                            .slice("CCCCCCCCC", "EAALILAAE", "EAALILAAE", "EAALILAAE", "FAALILAAF", "FAJJIJJAF",
                                    "FAKKIKKAF",
                                    "FAAAIAAAF", "BCCGGGCCB")
                            .slice("CCCCCCCCC", "EAAIJIAAE", "EAAIJIAAE", "EAAIJIAAE", "FAAIJIAAF", "FAJIJIJAF",
                                    "FAKIDIKAF",
                                    "FAAIDIAAF", "BCCGJGCCB")
                            .slice("CCCCCCCCC", "EAALILAAE", "EAALILAAE", "EAALILAAE", "FAALILAAF", "FAJJIJJAF",
                                    "FAKKIKKAF",
                                    "FAAAIAAAF", "BCCGGGCCB")
                            .slice("CCCCCCCCC", "DHIAAAIHD", "DHIAAAIHD", "DHIAAAIHD", "DHIAAAIHD", "DHIJJJIHD",
                                    "CHIKKKIHC",
                                    "CHIAAAIHC", "BCCCCCCCB")
                            .slice("BCCCCCCCB", "BGHAAAHGB", "BGHAAAHGB", "BGHAAAHGB", "BGHAAAHGB", "BGHAAAHGB",
                                    "BCHAAAHCB",
                                    "BCHAAAHCB", "BBCCCCCBB")
                            .slice("BBCCMCCBB", "BBDEEEDBB", "BBDEEEDBB", "BBDEEEDBB", "BBDFFFDBB", "BBDFFFDBB",
                                    "BBCFFFCBB",
                                    "BBCFFFCBB", "BBBBBBBBB")
                            .where('A', Predicates.air())
                            .where('B', Predicates.any())
                            .where('C', blocks(PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING.get())
                                    .setMinGlobalLimited(10)
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(Predicates.abilities(PartAbility.SUBSTATION_OUTPUT_ENERGY)
                                            .setMaxGlobalLimited(2))
                                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                                    .or(Predicates.abilities(PhoenixPartAbility.FISSION_SCRAM).setMaxGlobalLimited(1))
                                    .or(Predicates.abilities(PhoenixPartAbility.FISSION_SENSOR).setMaxGlobalLimited(2)))
                            .where('D', blocks(PhoenixFissionBlocks.FISSILE_HEAT_SAFE_CASING.get()))
                            .where('E', blocks(Blocks.TINTED_GLASS))
                            .where('F', Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                            .where('G', Predicates.blocks(GCYMBlocks.HEAT_VENT.get()))
                            .where('H', Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                            .where('I',
                                    PhoenixFissionPredicates.fissionModerators()
                                            .or(PhoenixFissionPredicates.fissionBlankets()))
                            .where('J', Predicates.blocks(COIL_HSSG.get()))
                            .where('K',
                                    PhoenixFissionPredicates.fissionCoolers()
                                            .or(PhoenixFissionPredicates.fissionFuelRods()))
                            .where('L', Predicates.blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                            .where('M', Predicates.controller(definition))
                            .build())
                    .workableCasingModel(PhoenixFission.id("block/fission/fissile_reaction_safe_casing"),
                                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .register();
            registerFissionReactor(HIGH_PERFORMANCE_BREEDER_REACTOR, BreederWorkableElectricMultiblockMachine.class);
        }

        if ((PhoenixFissionConfigs.INSTANCE != null && PhoenixFissionConfigs.INSTANCE.fission.fissionReactorEnabled) ||
                GTCEu.isDataGen()) {
            PRESSURIZED_FISSION_REACTOR = PHOENIX_REGISTRATE
                    .multiblock("pressurized_fission_reactor", DynamicFissionReactorMachine::new)
                    .langValue("§bPressurized Fission Reactor")
                    .recipeType(PhoenixRecipeTypes.PRESSURIZED_FISSION_REACTOR_RECIPES)
                    .generator(true)
                    .regressWhenWaiting(false)
                    .recipeModifiers(DynamicFissionReactorMachine::recipeModifier,
                            GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
                    .appearanceBlock(PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING)
                    .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                            .slice("BCCCB", "CDDDC", "CDDDC", "CDDDC", "BCCCB")
                            .slice("CEFEC", "DGGGD", "DGGGD", "DGGGD", "CDHDC")
                            .slice("CFEFC", "DGFGD", "DGFGD", "DGFGD", "CHEHC")
                            .slice("CEFEC", "DGGGD", "DGGGD", "DGGGD", "CDHDC")
                            .slice("BCICB", "CDDDC", "CDDDC", "CDDDC", "BCCCB")
                            .where('A', air())
                            .where('B', any())
                            .where('C', blocks(PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING.get())
                                    .setMinGlobalLimited(12)
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(Predicates.abilities(PhoenixPartAbility.FISSION_SCRAM).setMaxGlobalLimited(1))
                                    .or(Predicates.abilities(PhoenixPartAbility.FISSION_SENSOR).setMaxGlobalLimited(2))
                                    .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                            .where('D', blocks(Blocks.TINTED_GLASS))
                            .where('E', blocks(COIL_KANTHAL.get()))
                            .where('F',
                                    PhoenixFissionPredicates.fissionModerators()
                                            .or(PhoenixFissionPredicates.fissionBlankets()))
                            .where('G',
                                    PhoenixFissionPredicates.fissionCoolers()
                                            .or(PhoenixFissionPredicates.fissionFuelRods()))
                            .where('H', blocks(PhoenixFissionBlocks.FISSILE_HEAT_SAFE_CASING.get()))
                            .where('I', Predicates.controller(definition))
                            .build())
                    .workableCasingModel(PhoenixFission.id("block/fission/fissile_reaction_safe_casing"),
                                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .register();
            registerFissionReactor(PRESSURIZED_FISSION_REACTOR, DynamicFissionReactorMachine.class);
        }
        if ((PhoenixFissionConfigs.INSTANCE != null && PhoenixFissionConfigs.INSTANCE.fission.msrEnabled) ||
                GTCEu.isDataGen()) {

            MOLTEN_SALT_REACTOR = PHOENIX_REGISTRATE
                    .multiblock("molten_salt_reactor", MoltenSaltReactorMultiblockMachine::new)
                    .langValue("Molten Salt Reactor")
                    .recipeType(PhoenixRecipeTypes.PRESSURIZED_FISSION_REACTOR_RECIPES)
                    .generator(true)
                    .regressWhenWaiting(false)
                    .recipeModifiers(MoltenSaltReactorMultiblockMachine::recipeModifier,
                            GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK))
                    .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
                    .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.LEFT)
                            .slice("ABACCCABA", "ADACCCADA", "ADACCCADA", "ADACCCADA", "ADACCCADA", "ADACCCADA",
                                    "ABACCCABA")
                            .slice("BEEBEBEEB", "DGHHDHHGD", "DGHHDHHGD", "DGHHDHHGD", "DGHHDHHGD", "DGHHDHHGD",
                                    "BHHBHBHHB")
                            .slice("AEAEEEAEA", "AHIJCJIHA", "AHIJCJIHA", "AHIJCJIHA", "AHIJCJIHA", "AHIJCJIHA",
                                    "AHHHHHHHA")
                            .slice("CBEEEEEBC", "CHJJKJJHC", "CHJJKJJHC", "CHJJKJJHC", "CHJJKJJHC", "CHJJKJJHC",
                                    "CBHHLHHBC")
                            .slice("CEEEEEEEC", "CDCKJKCDC", "CDCKMKCDC", "CDCKMKCDC", "CDCKMKCDC", "CDCKMKCDC",
                                    "CHHLLLHHC")
                            .slice("CBEEEEEBC", "CHJJKJJHC", "CHJJKJJHC", "CHJJKJJHC", "CHJJKJJHC", "CHJJKJJHC",
                                    "CBHHLHHBC")
                            .slice("AEAEEEAEA", "AHIJCJIHA", "AHIJCJIHA", "AHIJCJIHA", "AHIJCJIHA", "AHIJCJIHA",
                                    "AHHHHHHHA")
                            .slice("BEEBFBEEB", "DGHHDHHGD", "DGHHDHHGD", "DGHHDHHGD", "DGHHDHHGD", "DGHHDHHGD",
                                    "BHHBHBHHB")
                            .slice("ABACCCABA", "ADACCCADA", "ADACCCADA", "ADACCCADA", "ADACCCADA", "ADACCCADA",
                                    "ABACCCABA")
                            .where('A', blocks(PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING.get()))
                            .where('B', Predicates.blocks(GCYMBlocks.HEAT_VENT.get()))
                            .where('C', Predicates.any())
                            .where('D', Predicates.blocks(CASING_LAMINATED_GLASS.get()))
                            .where('E', Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                            .where('F', Predicates.controller(definition))
                            .where('G', Predicates.blocks(HIGH_POWER_CASING.get()))
                            .where('H', Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get())
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.SUBSTATION_OUTPUT_ENERGY).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(Predicates.abilities(PhoenixPartAbility.FISSION_SCRAM).setMaxGlobalLimited(1))
                                    .or(Predicates.abilities(PhoenixPartAbility.FISSION_SENSOR).setMaxGlobalLimited(2)))
                            .where('J', Predicates.blocks(CASING_TUNGSTENSTEEL_ROBUST.get()))
                            .where('I', Predicates.blocks(CASING_TUNGSTENSTEEL_PIPE.get()))
                            .where('K', PhoenixFissionPredicates.fissionCoolers())
                            .where('M', PhoenixFissionPredicates.msrCoreLiner())
                            .where('L', Predicates.blocks(PhoenixFissionBlocks.FISSILE_HEAT_SAFE_CASING.get()))
                            .build())
                    .workableCasingModel(GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .register();
            registerFissionReactor(MOLTEN_SALT_REACTOR, MoltenSaltReactorMultiblockMachine.class);
        }

    }

    public static final MachineEntry<MultiblockMachineDefinition> THERMOELECTRIC_GENERATOR = PHOENIX_REGISTRATE
            .multiblock("thermoelectric_generator", ThermoelectricGeneratorMachine::new)
            .langValue("§bThermoelectric Generator")
            .rotationState(RotationState.ALL)
            .allowExtendedFacing(true)
            .recipeType(PhoenixRecipeTypes.THERMOELECTRIC_GENERATOR_RECIPES)
            .recipeModifiers(ThermoelectricGeneratorMachine::recipeModifier)
            .appearanceBlock(PhoenixFissionBlocks.FISSILE_HEAT_SAFE_CASING)
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.LEFT)
                    .slice("ABBBBBA", "BBBCBBB", "BBCCCBB", "BCCGCCB", "BBCCCBB", "BBBCBBB", "ABBBBBA")
                    .slice("ABCCCBA", "BBAAABB", "CAAEAAC", "CAESEAC", "CAAEAAC", "BBAAABB", "ABCCCBA")
                    .slice("ABCCCBA", "BBAAABB", "CAAEAAC", "CAESEAC", "CAAEAAC", "BBAAABB", "ABCCCBA")
                    .slice("ABCCCBA", "BBAAABB", "CAAEAAC", "CAESEAC", "CAAEAAC", "BBAAABB", "ABCCCBA")
                    .sliceRepeatable(1, 20, "ABCCCBA", "BBAAABB", "CAAEAAC", "CAESEAC", "CAAEAAC", "BBAAABB", "ABCCCBA")
                    .slice("ABBBBBA", "BBBCBBB", "BBCCCBB", "BCCCCCB", "BBCCCBB", "BBBCBBB", "ABBBBBA")

                    .where('A', Predicates.any())
                    .where('B', Predicates.blocks(PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING.get()))
                    .where('C',
                            Predicates.blocks(PhoenixFissionBlocks.FISSILE_HEAT_SAFE_CASING.get())
                                    .setMinGlobalLimited(6)
                                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS))
                                    .or(Predicates.abilities(PartAbility.EXPORT_ITEMS))
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS))
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS))
                                    .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY)))
                    .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, Steel)))
                    .where('S', Predicates.blocks(PhoenixFissionBlocks.FISSILE_SAFE_GEARBOX_CASING.get()))
                    .where('G', Predicates.controller(definition))
                    .build())
            .workableCasingModel(
                    PhoenixFission.id("block/fission/fissile_heat_safe_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
            .register();

    public static void init() {}
}
