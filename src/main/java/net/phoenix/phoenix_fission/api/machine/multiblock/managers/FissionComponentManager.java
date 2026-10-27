package net.phoenix.phoenix_fission.api.machine.multiblock.managers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.*;
import net.phoenix.phoenix_fission.api.block.iface.*;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FissionComponentManager {

    private final FissionWorkableElectricMultiblockMachine machine;

    @Getter
    protected final List<IFissionCoolerType> activeCoolers = new ArrayList<>();
    @Getter
    protected final List<IFissionModeratorType> activeModerators = new ArrayList<>();
    @Getter
    protected final List<IFissionFuelRodType> activeFuelRods = new ArrayList<>();
    @Getter
    protected final List<IFissionBlanketType> activeBlankets = new ArrayList<>();
    @Getter
    protected final List<IFissionStabilityHatchType> activeStabilityHatches = new ArrayList<>();
    @Getter
    protected final List<IFissionSensorHatchType> activeSensorHatches = new ArrayList<>();

    @Getter
    protected final List<IMSRCoreLinerType> activeLiners = new ArrayList<>();

    @Nullable
    @Getter
    protected IFissionCoolerType primaryCoolerType = null;
    @Nullable
    @Getter
    protected IFissionModeratorType primaryModeratorType = null;
    @Nullable
    @Getter
    protected IFissionFuelRodType primaryFuelRodType = null;

    public FissionComponentManager(FissionWorkableElectricMultiblockMachine machine) {
        this.machine = machine;
    }

    public void onStructureFormed() {
        activeCoolers.clear();
        activeModerators.clear();
        activeFuelRods.clear();
        activeBlankets.clear();
        activeLiners.clear();
        classifyFormedBlocks();

        machine.getPersistedCoolerIDs().clear();
        machine.getPersistedCoolerIDs().addAll(this.activeCoolers.stream().map(IFissionCoolerType::getName).toList());
        machine.getPersistedModeratorIDs().clear();
        machine.getPersistedModeratorIDs()
                .addAll(this.activeModerators.stream().map(IFissionModeratorType::getName).toList());
        machine.getPersistedFuelRodIDs().clear();
        machine.getPersistedFuelRodIDs()
                .addAll(this.activeFuelRods.stream().map(IFissionFuelRodType::getName).toList());
        machine.getPersistedBlanketIDs().clear();
        machine.getPersistedBlanketIDs()
                .addAll(this.activeBlankets.stream().map(IFissionBlanketType::getName).toList());
        machine.getPersistedStabilityIDs().clear();
        machine.getPersistedStabilityIDs()
                .addAll(this.activeStabilityHatches.stream().map(IFissionStabilityHatchType::getName).toList());
        machine.getPersistedSensorIDs().clear();
        machine.getPersistedSensorIDs()
                .addAll(this.activeSensorHatches.stream().map(IFissionSensorHatchType::getName).toList());

        recalcPrimaries();
    }

    public void onStructureInvalid() {
        activeCoolers.clear();
        activeModerators.clear();
        activeFuelRods.clear();
        activeBlankets.clear();
        activeStabilityHatches.clear();
        activeSensorHatches.clear();
        activeLiners.clear();

        machine.getPersistedCoolerIDs().clear();
        machine.getPersistedModeratorIDs().clear();
        machine.getPersistedFuelRodIDs().clear();
        machine.getPersistedBlanketIDs().clear();
        machine.getPersistedStabilityIDs().clear();
        machine.getPersistedSensorIDs().clear();

        primaryCoolerType = null;
        primaryModeratorType = null;
        primaryFuelRodType = null;
    }

    public void resolvePersistedComponents() {

        activeCoolers.clear();
        activeModerators.clear();
        activeFuelRods.clear();
        activeBlankets.clear();
        activeStabilityHatches.clear();
        activeSensorHatches.clear();
        primaryCoolerType = null;
        primaryModeratorType = null;
        primaryFuelRodType = null;

        for (String id : machine.getPersistedCoolerIDs()) {
            PhoenixAPI.FISSION_COOLERS.keySet().stream().filter(t -> t.getName().equals(id)).findFirst()
                    .ifPresent(activeCoolers::add);
        }
        for (String id : machine.getPersistedModeratorIDs()) {
            PhoenixAPI.FISSION_MODERATORS.keySet().stream().filter(t -> t.getName().equals(id)).findFirst()
                    .ifPresent(activeModerators::add);
        }
        for (String id : machine.getPersistedFuelRodIDs()) {
            PhoenixAPI.FISSION_FUEL_RODS.keySet().stream().filter(t -> t.getName().equals(id)).findFirst()
                    .ifPresent(activeFuelRods::add);
        }
        for (String id : machine.getPersistedBlanketIDs()) {
            PhoenixAPI.FISSION_BLANKETS.keySet().stream().filter(t -> t.getName().equals(id)).findFirst()
                    .ifPresent(activeBlankets::add);
        }
        for (String id : machine.getPersistedStabilityIDs()) {
            PhoenixAPI.FISSION_STABILITY_HATCHES.keySet().stream().filter(t -> t.getName().equals(id)).findFirst()
                    .ifPresent(activeStabilityHatches::add);
        }
        for (String id : machine.getPersistedSensorIDs()) {
            PhoenixAPI.FISSION_SENSOR_HATCHES.keySet().stream().filter(t -> t.getName().equals(id)).findFirst()
                    .ifPresent(activeSensorHatches::add);
        }
        recalcPrimaries();
    }

    private void recalcPrimaries() {
        this.primaryCoolerType = calcPrimary(activeCoolers);
        this.primaryModeratorType = calcPrimary(activeModerators);
        this.primaryFuelRodType = calcPrimary(activeFuelRods);
    }

    private <T extends StringRepresentable> @Nullable T calcPrimary(List<T> list) {
        if (list.isEmpty()) return null;
        return list.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.<Map.Entry<T, Long>>comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Sorts every block of the formed structure into the component lists. Works on the block classes, so it covers
     * components from Java, the default set and KubeJS alike.
     */
    private void classifyFormedBlocks() {
        for (var info : machine.getDefaultPatternState().getCache().values()) {
            Block block = info.getBlockState().getBlock();
            if (block instanceof FissionCoolerBlock b) activeCoolers.add(b.getCoolerType());
            else if (block instanceof FissionModeratorBlock b) activeModerators.add(b.getModeratorType());
            else if (block instanceof FissionFuelRodBlock b) activeFuelRods.add(b.getFuelRodType());
            else if (block instanceof FissionBlanketBlock b) activeBlankets.add(b.getBlanketType());
            else if (block instanceof MSRCoreLinerBlock b) activeLiners.add(b.getLinerType());
        }
    }

    public void addDisplayText(List<Component> textList) {
        addBreakdown(textList, activeFuelRods, "Fuel Rods");
        addBreakdown(textList, activeModerators, "Moderators");
        addBreakdown(textList, activeCoolers, "Coolers");
    }

    private <T extends StringRepresentable> void addBreakdown(List<Component> textList, List<T> components,
                                                              String header) {
        textList.add(Component.literal(header + " (" + components.size() + "):").withStyle(ChatFormatting.AQUA));
        if (components.isEmpty()) {
            textList.add(Component.literal("  None").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        components.stream()
                .collect(Collectors.groupingBy(T::getSerializedName, LinkedHashMap::new, Collectors.counting()))
                .forEach((name, count) -> textList.add(Component.literal("  ")
                        .append(Component.translatable("phoenix_fission." + name).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" x" + count).withStyle(ChatFormatting.GRAY))));
    }
}
