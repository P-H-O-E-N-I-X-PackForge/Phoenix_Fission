package net.phoenix.phoenix_fission.api.examples.managers;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.phoenix.phoenix_fission.api.block.FissionFuelRodBlock;
import net.phoenix.phoenix_fission.api.block.FissionModeratorBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionThermalManager;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionBlocks;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

public class LegacyFlatCoolingThermalManager extends FissionThermalManager {

    private final double idleHeatLoss;

    public LegacyFlatCoolingThermalManager(FissionWorkableElectricMultiblockMachine machine,
                                           double idleHeatLoss) {
        super(machine);
        this.idleHeatLoss = Math.max(0.0, idleHeatLoss);
    }

    public LegacyFlatCoolingThermalManager(FissionWorkableElectricMultiblockMachine machine) {
        this(machine, 2.0);
    }

    @Override
    public void tickThermalLogic(boolean running) {
        machine.lastHeatGainedPerTick = 0.0;
        machine.lastHeatRemovedPerTick = 0.0;
        machine.lastGeneratedEUt = 0;

        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        var cfg = PhoenixFissionConfigs.INSTANCE.fission;
        var comp = machine.getComponentManager();

        if (running && !comp.getActiveFuelRods().isEmpty() && !machine.isScramActive()) {
            machine.setReactivityFactor(Math.min(1.0,
                    machine.getReactivityFactor() + hm.reactivityRampRatePerTick));
            machine.continuousBurnTicks++;
        } else {
            machine.setReactivityFactor(Math.max(0.0,
                    machine.getReactivityFactor() - hm.reactivityRampRatePerTick));
            machine.continuousBurnTicks = 0;
        }

        if (running && !comp.getActiveFuelRods().isEmpty()) {
            double heatProduced = machine.getFuelManager().calculateTickHeat(machine.lastParallels);
            machine.setHeat(machine.getHeat() + heatProduced);
            machine.lastHeatGainedPerTick = heatProduced;

            machine.getFuelManager().consumeFuelTick(machine.lastParallels);
        }

        int totalCooling = comp.getActiveCoolers().stream()
                .mapToInt(IFissionCoolerType::getCoolerTemperature)
                .sum();
        machine.lastProvidedCooling = totalCooling;

        double removed = 0.0;

        if (running) {
            machine.lastHasCoolant = true;
            if (cfg.coolingRequiresCoolant && !comp.getActiveCoolers().isEmpty()) {
                machine.lastHasCoolant = legacyCanConsumeCoolant() && legacyConsumeCoolant();
            }

            if (!cfg.coolingRequiresCoolant || machine.lastHasCoolant) {
                double aboveMin = Math.max(0.0, machine.getHeat() - hm.minHeat);
                removed = Math.min(aboveMin, totalCooling);
                machine.setHeat(machine.getHeat() - removed);
            }
        } else {
            machine.lastHasCoolant = true;

            double aboveMin = Math.max(0.0, machine.getHeat() - hm.minHeat);
            double idleCooling = Math.min(aboveMin, totalCooling * 0.25);
            machine.setHeat(machine.getHeat() - idleCooling);
            removed += idleCooling;

            double variableDrain = machine.getHeat() * 0.005;
            double passiveDrain = Math.min(
                    Math.max(0.0, machine.getHeat() - hm.minHeat),
                    Math.max(idleHeatLoss, variableDrain));
            machine.setHeat(machine.getHeat() - passiveDrain);
            removed += passiveDrain;
        }

        machine.lastHeatRemovedPerTick = removed;

        limitHeat();
        machine.getFuelManager().processPowerGeneration(running);
        processMeltdownTimer();
    }

    private boolean legacyCanConsumeCoolant() {
        return processCoolantAction(false);
    }

    private boolean legacyConsumeCoolant() {
        return processCoolantAction(true);
    }

    private boolean processCoolantAction(boolean execute) {
        var cfg = PhoenixFissionConfigs.INSTANCE.fission;
        var comp = machine.getComponentManager();
        if (comp.getActiveCoolers().isEmpty()) return true;

        if (!cfg.coolantUsageAdditive) {
            IFissionCoolerType primary = comp.getPrimaryCoolerType();
            if (primary == null) return true;
            return handleFluidConversion(
                    primary.getInputCoolantFluidId(), primary.getOutputCoolantFluidId(),
                    primary.getCoolantPerTick(), execute);
        }

        java.util.Map<String, Integer> requirements = new java.util.HashMap<>();
        java.util.Map<String, String> outputs = new java.util.HashMap<>();

        for (IFissionCoolerType c : comp.getActiveCoolers()) {
            if (c.getCoolantPerTick() <= 0 || c.getInputCoolantFluidId().isEmpty() ||
                    "none".equalsIgnoreCase(c.getInputCoolantFluidId()))
                continue;
            requirements.merge(c.getInputCoolantFluidId(), c.getCoolantPerTick(), Integer::sum);
            outputs.put(c.getInputCoolantFluidId(), c.getOutputCoolantFluidId());
        }

        for (var entry : requirements.entrySet()) {
            if (!handleFluidConversion(entry.getKey(), outputs.get(entry.getKey()),
                    entry.getValue(), execute))
                return false;
        }
        return true;
    }

    private boolean handleFluidConversion(String inId, String outId, int amount, boolean execute) {
        if (amount <= 0 || inId.isEmpty() || "none".equalsIgnoreCase(inId)) return true;

        ResourceLocation inRl = ResourceLocation.tryParse(inId);
        if (inRl == null || !BuiltInRegistries.FLUID.containsKey(inRl))
            return false;

        FluidStack inStack = new FluidStack(
                BuiltInRegistries.FLUID.getOptional(inRl).orElse(null), amount);

        if (!machine.executeFluidIO(inStack, IO.IN, !execute))
            return false;

        if (execute && !outId.isEmpty() && !"none".equalsIgnoreCase(outId) && !outId.equalsIgnoreCase(inId)) {
            ResourceLocation outRl = ResourceLocation.tryParse(outId);
            if (outRl != null && BuiltInRegistries.FLUID.containsKey(outRl)) {
                machine.executeFluidIO(
                        new FluidStack(
                                BuiltInRegistries.FLUID.getOptional(outRl).orElse(null), amount),
                        IO.OUT, false);
            }
        }
        return true;
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
            if (machine.meltdownTimerTicks == 0) triggerDetonation();
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
                machine.getHeat(), maxSafeHU,
                md.minGraceSeconds, md.baseGraceSeconds, md.excessHeatSeverity);

        if (machine.meltdownTimerTicks < 0) {
            machine.meltdownTimerTicks = machine.meltdownTimerMax;
        } else if (machine.meltdownTimerTicks > 0) {
            machine.meltdownTimerTicks--;
        }

        if (machine.meltdownTimerTicks == 0) triggerDetonation();
    }

    private void triggerDetonation() {
        if (!(machine.getLevel() instanceof ServerLevel world)) return;

        var exp = PhoenixFissionConfigs.INSTANCE.fission.explosion;
        float power = computeExplosionPower(
                exp.baseExplosionPower, exp.explosionPowerPerFuelRod, exp.explosionPowerPerHeatUnit);

        var pos = machine.getPos();

        java.util.List<BlockPos> cached = new java.util.ArrayList<>(
                machine.getMultiblockState().getCache() != null ? machine.getMultiblockState().getCache() :
                        java.util.List.of());

        machine.onStructureInvalid();

        if (exp.destructiveExplosion) {
            for (var targetPos : cached) {
                if (targetPos.equals(pos)) continue;
                var state = world.getBlockState(targetPos);
                boolean vaporize = state
                        .is(PhoenixFissionBlocks.FISSILE_HEAT_SAFE_CASING
                                .get()) ||
                        state.is(
                                PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING
                                        .get()) ||
                        state.is(
                                PhoenixFissionBlocks.FISSILE_SAFE_GEARBOX_CASING
                                        .get()) ||
                        state.is(Blocks.TINTED_GLASS) ||
                        state.getBlock() instanceof FissionFuelRodBlock ||
                        state.getBlock() instanceof FissionModeratorBlock;

                if (vaporize) {
                    world.removeBlock(targetPos, false);
                    world.sendParticles(ParticleTypes.LARGE_SMOKE,
                            targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                            3, 0.1, 0.1, 0.1, 0.05);
                }
            }
        }

        world.explode(null,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                power, false, Level.ExplosionInteraction.NONE);
        world.removeBlock(pos, false);

        machine.meltdownTimerTicks = -1;
        machine.setHeat(PhoenixFissionConfigs.INSTANCE.fission.heatModel.minHeat);
    }
}
