package net.phoenix.phoenix_fission.common.data.multiblock.fission.managers;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.phoenix_fission.api.block.IFissionCoolerType;
import net.phoenix.phoenix_fission.api.block.IFissionFuelRodType;
import net.phoenix.phoenix_fission.common.data.block.FissionFuelRodBlock;
import net.phoenix.phoenix_fission.common.data.block.FissionModeratorBlock;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionBlocks;
import net.phoenix.phoenix_fission.common.data.multiblock.fission.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FissionThermalManager {

    protected final FissionWorkableElectricMultiblockMachine machine;
    private boolean meltdownInProgress = false;

    public FissionThermalManager(FissionWorkableElectricMultiblockMachine machine) {
        this.machine = machine;
    }

    public void onStructureFormed() {
        machine.meltdownTimerTicks = -1;
        machine.meltdownTimerMax = 0;
    }

    public void onStructureInvalid() {
        meltdownInProgress = false;
        machine.meltdownTimerTicks = -1;
        machine.meltdownTimerMax = 0;
        machine.lastRequiredCooling = 0;
        machine.lastProvidedCooling = 0;
        machine.lastHasCoolant = true;
        machine.lastHeatGainedPerTick = 0.0;
        machine.lastHeatRemovedPerTick = 0.0;
        machine.isOverCooled = false;
        overcooledThisTick = false;
    }

    protected double computePassiveCoolingDelta(double heat, double ambientTemp, double conductivity) {
        return (ambientTemp - heat) * conductivity;
    }

    protected double computeActiveCoolerHeatRemoval(IFissionCoolerType cooler, double heat,
                                                    long count, double conductivity) {
        double delta = (cooler.getCoolerTemperature() - heat) * count * conductivity;
        return delta < 0 ? -delta : 0.0;
    }

    protected int computeMeltdownGracePeriodTicks(double heat, double maxSafeHeat,
                                                  double minGrace, double baseGrace, double severity) {
        double scale = ((heat - maxSafeHeat) / Math.max(1.0, maxSafeHeat)) * Math.max(0.0001, severity);
        double grace = Math.max(minGrace, baseGrace - (baseGrace - minGrace) * Math.min(1.0, scale));
        return (int) Math.max(1, Math.floor(grace * 20.0));
    }

    protected float computeExplosionPower(double base, double powerPerRod, double powerPerHeat) {
        var comp = machine.getComponentManager();
        double avgRodHeat = comp.getActiveFuelRods().stream()
                .mapToDouble(IFissionFuelRodType::getBaseHeatProduction).average().orElse(0.0);
        return (float) (base + (comp.getActiveFuelRods().size() * powerPerRod) + (avgRodHeat * powerPerHeat));
    }

    private boolean overcooledThisTick = false;

    protected double computeCoolingFloorHU(Collection<IFissionCoolerType> activeCoolers, double ambientTemperatureHU) {
        OptionalDouble minCoolerTemp = activeCoolers.stream()
                .mapToDouble(IFissionCoolerType::getCoolerTemperature).min();
        return minCoolerTemp.isPresent() ? Math.max(ambientTemperatureHU, minCoolerTemp.getAsDouble()) :
                ambientTemperatureHU;
    }

    public void tickThermalLogic(boolean running) {
        machine.lastHeatGainedPerTick = 0.0;
        machine.lastHeatRemovedPerTick = 0.0;
        machine.lastGeneratedEUt = 0;

        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        var comp = machine.getComponentManager();

        if (running && !comp.getActiveFuelRods().isEmpty() && !machine.isScramActive()) {
            machine.setReactivityFactor(Math.min(1.0, machine.getReactivityFactor() + hm.reactivityRampRatePerTick));
            machine.continuousBurnTicks++;
        } else {
            machine.setReactivityFactor(Math.max(0.0, machine.getReactivityFactor() - hm.reactivityRampRatePerTick));
            machine.continuousBurnTicks = 0;
        }

        if (running && !comp.getActiveFuelRods().isEmpty()) {
            double heatProduced = machine.getFuelManager().calculateTickHeat(machine.lastParallels);
            machine.setHeat(machine.getHeat() + heatProduced);
            machine.lastHeatGainedPerTick = heatProduced;

            machine.getFuelManager().consumeFuelTick(machine.lastParallels);
        }

        double coolingFloorHU = computeCoolingFloorHU(comp.getActiveCoolers(), hm.ambientTemperatureHU);

        double passiveDelta = computePassiveCoolingDelta(machine.getHeat(), coolingFloorHU,
                hm.passiveCoolingConductivity);
        machine.setHeat(machine.getHeat() + passiveDelta);

        double activeCoolingRemoved = 0.0;
        double activeHeatingAdded = 0.0;

        if (!comp.getActiveCoolers().isEmpty()) {
            for (IFissionCoolerType cooler : comp.getActiveCoolers()) {
                if (!cooler.isPassive()) continue;
                double flat = cooler.getFlatCoolingHUt();
                if (flat > 0.0) {

                    if (machine.getHeat() <= coolingFloorHU) continue;
                    double applied = Math.min(flat, machine.getHeat() - coolingFloorHU);
                    machine.setHeat(machine.getHeat() - applied);
                    activeCoolingRemoved += applied;
                } else if (flat < 0.0) {

                    double added = -flat;
                    machine.setHeat(machine.getHeat() + added);
                    activeHeatingAdded += added;
                }
            }
        }

        overcooledThisTick = running && !comp.getActiveFuelRods().isEmpty() &&
                machine.getHeat() <= coolingFloorHU;
        machine.isOverCooled = overcooledThisTick;

        machine.lastHasCoolant = true;
        boolean requiresActiveCooling = comp.getActiveCoolers().stream()
                .anyMatch(c -> !c.isPassive() && c.getCoolerTemperature() < machine.getHeat());

        if (requiresActiveCooling && PhoenixFissionConfigs.INSTANCE.fission.coolingRequiresCoolant) {
            machine.lastHasCoolant = canConsumeCoolantForThisTick() && processCoolantConsumption();
        }

        if (requiresActiveCooling &&
                (!PhoenixFissionConfigs.INSTANCE.fission.coolingRequiresCoolant || machine.lastHasCoolant)) {
            Map<IFissionCoolerType, Long> activeMap = comp.getActiveCoolers().stream()
                    .filter(c -> !c.isPassive())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            for (var entry : activeMap.entrySet()) {
                double removed = computeActiveCoolerHeatRemoval(
                        entry.getKey(), machine.getHeat(), entry.getValue(), hm.activeCoolingConductivity);
                if (removed > 0) {
                    machine.setHeat(machine.getHeat() - removed);
                    activeCoolingRemoved += removed;
                }
            }
        }

        machine.lastHeatGainedPerTick += activeHeatingAdded;
        machine.lastHeatRemovedPerTick = activeCoolingRemoved + (passiveDelta < 0 ? -passiveDelta : 0.0);
        machine.lastProvidedCooling = (int) Math.round(calculateTheoreticalMaxCooling(machine.getMaxSafeHeatHU()));

        clampHeat();
        machine.getFuelManager().processPowerGeneration(running);
        processMeltdownTimer();
    }

    public boolean canConsumeCoolantForThisTick() {
        return processCoolantAction(false);
    }

    private boolean processCoolantConsumption() {
        return processCoolantAction(true);
    }

    private boolean processCoolantAction(boolean execute) {
        var cfg = PhoenixFissionConfigs.INSTANCE.fission;
        var comp = machine.getComponentManager();
        if (comp.getActiveCoolers().isEmpty()) return true;

        if (!cfg.coolantUsageAdditive) {
            IFissionCoolerType primary = comp.getPrimaryCoolerType();
            if (primary == null) return true;
            return handleFluidConversion(primary.getInputCoolantFluidId(), primary.getOutputCoolantFluidId(),
                    primary.getCoolantPerTick(), primary.getOutputCoolantPerTick(), execute);
        }

        record ConversionGroup(String inId, String outId) {}

        Map<String, Integer> totalPerInput = new HashMap<>();
        Map<ConversionGroup, Integer> groupInput = new HashMap<>();
        Map<ConversionGroup, Integer> groupOutput = new HashMap<>();

        for (IFissionCoolerType c : comp.getActiveCoolers()) {
            if (c.getCoolantPerTick() <= 0 || c.getInputCoolantFluidId().isEmpty() ||
                    "none".equalsIgnoreCase(c.getInputCoolantFluidId()))
                continue;
            totalPerInput.merge(c.getInputCoolantFluidId(), c.getCoolantPerTick(), Integer::sum);
            ConversionGroup key = new ConversionGroup(c.getInputCoolantFluidId(), c.getOutputCoolantFluidId());
            groupInput.merge(key, c.getCoolantPerTick(), Integer::sum);
            groupOutput.merge(key, c.getOutputCoolantPerTick(), Integer::sum);
        }

        if (!execute) {
            for (var entry : totalPerInput.entrySet()) {
                if (!handleFluidConversion(entry.getKey(), "", entry.getValue(), 0, false)) return false;
            }
            return true;
        }

        for (var entry : groupInput.entrySet()) {
            ConversionGroup key = entry.getKey();
            if (!handleFluidConversion(key.inId(), key.outId(), entry.getValue(),
                    groupOutput.getOrDefault(key, entry.getValue()), true))
                return false;
        }
        return true;
    }

    private boolean handleFluidConversion(String inId, String outId, int inAmount, int outAmount, boolean execute) {
        if (inAmount <= 0 || inId.isEmpty() || "none".equalsIgnoreCase(inId)) return true;
        ResourceLocation inRl = ResourceLocation.tryParse(inId);
        if (inRl == null || !ForgeRegistries.FLUIDS.containsKey(inRl)) return false;

        FluidStack inStack = new FluidStack(Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(inRl)), inAmount);
        if (!machine.executeFluidIO(inStack, IO.IN, !execute)) return false;

        if (execute) {
            if (overcooledThisTick) {

                int refund = (int) Math.floor(inAmount * 0.9);
                if (refund > 0) {
                    machine.executeFluidIO(
                            new FluidStack(Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(inRl)), refund),
                            IO.OUT,
                            false);
                }
            } else if (outAmount > 0 && !outId.isEmpty() && !"none".equalsIgnoreCase(outId) &&
                    !outId.equalsIgnoreCase(inId)) {
                        ResourceLocation outRl = ResourceLocation.tryParse(outId);
                        if (outRl != null && ForgeRegistries.FLUIDS.containsKey(outRl)) {
                            machine.executeFluidIO(
                                    new FluidStack(Objects.requireNonNull(ForgeRegistries.FLUIDS.getValue(outRl)),
                                            outAmount),
                                    IO.OUT,
                                    false);
                        }
                    }
        }
        return true;
    }

    private double calculateTheoreticalMaxCooling(double maxSafe) {
        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        double maxCooling = Math.max(0.0, (maxSafe - hm.ambientTemperatureHU) * hm.passiveCoolingConductivity);
        for (IFissionCoolerType c : machine.getComponentManager().getActiveCoolers()) {
            maxCooling += c.isPassive() ? c.getFlatCoolingHUt() :
                    Math.max(0.0, (maxSafe - c.getCoolerTemperature()) * hm.activeCoolingConductivity);
        }
        return maxCooling;
    }

    public void clampHeat() {
        if (machine.getHeat() < PhoenixFissionConfigs.INSTANCE.fission.heatModel.minHeat) {
            machine.setHeat(PhoenixFissionConfigs.INSTANCE.fission.heatModel.minHeat);
        }
    }

    private void processMeltdownTimer() {
        var md = PhoenixFissionConfigs.INSTANCE.fission.meltdown;
        double maxSafeHU = machine.getMaxSafeHeatHU();
        double maxClampHU = machine.getMaxHeatClampHU();

        if (machine.getHeat() >= maxClampHU) {
            machine.setHeat(maxClampHU);
            machine.meltdownTimerMax = (int) Math.max(1, Math.floor(md.minGraceSeconds * 20.0));
            if (machine.meltdownTimerTicks < 0) machine.meltdownTimerTicks = machine.meltdownTimerMax;
        } else if (machine.meltdownTimerTicks > 0) {
            if (!machine.isScramActive()) machine.meltdownTimerTicks--;
            if (machine.meltdownTimerTicks == 0) triggerThermalDetonation();
            return;
        }

        if (machine.getHeat() <= maxSafeHU) {
            if (md.clearTimerWhenSafe) {
                machine.meltdownTimerTicks = -1;
                machine.meltdownTimerMax = 0;
            }
            return;
        }

        machine.meltdownTimerMax = computeMeltdownGracePeriodTicks(
                machine.getHeat(), maxSafeHU, md.minGraceSeconds, md.baseGraceSeconds, md.excessHeatSeverity);
        if (machine.meltdownTimerTicks < 0) {
            machine.meltdownTimerTicks = machine.meltdownTimerMax;
        } else if (machine.meltdownTimerTicks > 0) {
            machine.meltdownTimerTicks--;
        }

        if (machine.meltdownTimerTicks == 0) triggerThermalDetonation();
    }

    private void triggerThermalDetonation() {
        if (meltdownInProgress) return;
        meltdownInProgress = true;

        if (machine.getLevel() instanceof ServerLevel world) {
            BlockPos pos = machine.getPos();
            List<BlockPos> cachedBlocks = new ArrayList<>(machine.getMultiblockState().getCache() != null ?
                    machine.getMultiblockState().getCache() : List.of());

            machine.onStructureInvalid();

            if (PhoenixFissionConfigs.INSTANCE.fission.explosion.destructiveExplosion) {
                for (BlockPos targetPos : cachedBlocks) {
                    if (targetPos.equals(pos)) continue;
                    var state = world.getBlockState(targetPos);
                    boolean vaporize = state.is(PhoenixFissionBlocks.FISSILE_HEAT_SAFE_CASING.get()) ||
                            state.is(PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING.get()) ||
                            state.is(PhoenixFissionBlocks.FISSILE_SAFE_GEARBOX_CASING.get()) ||
                            state.is(Blocks.TINTED_GLASS) ||
                            state.getBlock() instanceof FissionFuelRodBlock ||
                            state.getBlock() instanceof FissionModeratorBlock;

                    if (vaporize) {
                        world.removeBlock(targetPos, false);
                        world.sendParticles(ParticleTypes.LARGE_SMOKE, targetPos.getX() + 0.5, targetPos.getY() + 0.5,
                                targetPos.getZ() + 0.5, 3, 0.1, 0.1, 0.1, 0.05);
                    }
                }
            }

            var exp = PhoenixFissionConfigs.INSTANCE.fission.explosion;
            float finalPower = computeExplosionPower(exp.baseExplosionPower, exp.explosionPowerPerFuelRod,
                    exp.explosionPowerPerHeatUnit);

            world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, finalPower, false,
                    Level.ExplosionInteraction.NONE);
            world.removeBlock(pos, false);
        }

        machine.meltdownTimerTicks = -1;
        machine.setHeat(PhoenixFissionConfigs.INSTANCE.fission.heatModel.minHeat);
        meltdownInProgress = false;
    }

    public void addDisplayText(List<Component> textList) {
        double maxSafeHU = machine.getMaxSafeHeatHU();
        textList.add(Component
                .translatable("phoenix_fission.current_heat_display", String.format("%.1f", machine.getHeat()),
                        String.format("%.1f", maxSafeHU))
                .withStyle(s -> s.withColor(machine.getHeat() > maxSafeHU ? 0xFF3333 : 0x33FF33)));
        textList.add(Component.translatable("phoenix_fission.cooling_power", machine.lastProvidedCooling)
                .withStyle(s -> s.withColor(0x55FFFF)));
        textList.add(Component.translatable(
                machine.lastHasCoolant ? "phoenix_fission.coolant_status.ok" : "phoenix_fission.coolant_status.empty"));
        if (machine.meltdownTimerTicks > 0) {
            textList.add(
                    Component
                            .translatable("phoenix_fission.status.danger_timer",
                                    (int) Math.ceil(machine.meltdownTimerTicks / 20.0))
                            .withStyle(s -> s.withColor(0xFF4444)));
        }
    }
}
