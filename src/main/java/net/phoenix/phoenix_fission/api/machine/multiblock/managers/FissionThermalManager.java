package net.phoenix.phoenix_fission.api.machine.multiblock.managers;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.phoenix.phoenix_fission.api.block.FissionFuelRodBlock;
import net.phoenix.phoenix_fission.api.block.FissionModeratorBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionBlocks;
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
                                                    long count, double conductivity, double heatCapacity) {
        double coolerTempHU = cooler.getCoolerTemperature() * heatCapacity;
        double delta = (coolerTempHU - heat) * count * conductivity;
        return delta < 0 ? -delta : 0.0;
    }

    protected static double resolveConductivity(IFissionCoolerType cooler, PhoenixFissionConfigs.HeatModelConfigs hm) {
        double c = cooler.getConductivity();
        return c >= 0 ? c : hm.activeCoolingConductivity;
    }

    public static boolean isRated(PhoenixFissionConfigs.HeatModelConfigs hm) {
        return hm.thermalModel == PhoenixFissionConfigs.ThermalModel.RATED;
    }

    /** 0 at/below ambient, 1 at/above the safe-heat limit, linear in between. */
    public static double ratedFraction(double heat, double ambientHU, double maxSafeHU) {
        double span = Math.max(1e-6, maxSafeHU - ambientHU);
        return Math.max(0.0, Math.min(1.0, (heat - ambientHU) / span));
    }

    /** HU/t one cooler block removes right now under the RATED model. */
    public static double ratedCoolingDelivered(double ratedHUt, double heat, double ambientHU, double maxSafeHU) {
        return Math.max(0.0, ratedHUt) * ratedFraction(heat, ambientHU, maxSafeHU);
    }

    /** Full rated HU/t of one cooler block - explicit if set, else what the CONDUCTIVE model gave at the limit. */
    public static double resolveRatedCooling(IFissionCoolerType cooler, PhoenixFissionConfigs.HeatModelConfigs hm,
                                             double heatCapacity) {
        double r = cooler.getRatedCoolingHUt();
        if (r >= 0) return r;
        return Math.max(0.0,
                (hm.maxSafeTempK - cooler.getCoolerTemperature()) * resolveConductivity(cooler, hm) * heatCapacity);
    }

    protected CoolerLoad computeCoolerLoad() {
        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        var comp = machine.getComponentManager();
        double heatCapacity = machine.getHeatCapacity();
        Map<IFissionCoolerType, Long> counts = comp.getActiveCoolers().stream()
                .filter(c -> !c.isPassive())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        boolean rated = isRated(hm);
        double ambientHU = hm.ambientTemperatureHU * heatCapacity;
        double maxSafeHU = machine.getMaxSafeHeatHU();
        double refHU = Math.max(0.0001, hm.coolantReferenceHeatRemovalHUt);

        Map<IFissionCoolerType, Double> removal = new HashMap<>();
        Map<IFissionCoolerType, Double> usage = new HashMap<>();
        for (var entry : counts.entrySet()) {
            IFissionCoolerType cooler = entry.getKey();
            long count = entry.getValue();
            if (rated) {
                double frac = ratedFraction(machine.getHeat(), ambientHU, maxSafeHU);
                removal.put(cooler, ratedCoolingDelivered(resolveRatedCooling(cooler, hm, heatCapacity),
                        machine.getHeat(), ambientHU, maxSafeHU) * count);
                // Coolant use tracks how much of the cooler's rating is in use.
                usage.put(cooler, frac * refHU);
            } else {
                double total = computeActiveCoolerHeatRemoval(cooler, machine.getHeat(), count,
                        resolveConductivity(cooler, hm), heatCapacity);
                removal.put(cooler, total);
                usage.put(cooler, total / Math.max(1, count));
            }
        }
        return new CoolerLoad(counts, removal, usage);
    }

    /**
     * counts: active cooler blocks per type. removal: total HU/t removed per type. usage: per-block heat-removal
     * equivalent that coolant/item consumption is scaled from (see scaledCoolantAmount).
     */
    protected record CoolerLoad(Map<IFissionCoolerType, Long> counts, Map<IFissionCoolerType, Double> removal,
                                Map<IFissionCoolerType, Double> usage) {}

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

    protected double computeCoolingFloorHU(double ambientTemperatureK, double heatCapacity) {
        return ambientTemperatureK * heatCapacity;
    }

    public void tickThermalLogic(boolean running) {
        machine.ensureAmbientHeatInitialized();

        machine.lastHeatGainedPerTick = 0.0;
        machine.lastHeatRemovedPerTick = 0.0;
        machine.lastGeneratedEUt = 0;

        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        var comp = machine.getComponentManager();
        boolean hasActiveRods = !comp.getActiveFuelRods().isEmpty();

        if (running && hasActiveRods && !machine.isScramActive()) {
            machine.setReactivityFactor(Math.min(1.0, machine.getReactivityFactor() + hm.reactivityRampRatePerTick));
            machine.continuousBurnTicks++;
        } else {
            machine.setReactivityFactor(Math.max(0.0, machine.getReactivityFactor() - hm.reactivityRampRatePerTick));
            machine.continuousBurnTicks = 0;
        }

        if (running && hasActiveRods) {
            double heatProduced = machine.getFuelManager().calculateTickHeat(machine.lastParallels);
            machine.setHeat(machine.getHeat() + heatProduced);
            machine.lastHeatGainedPerTick += heatProduced;

            machine.getFuelManager().consumeFuelTick(machine.lastParallels);
        }

        double coolingFloorHU = computeCoolingFloorHU(hm.ambientTemperatureHU, machine.getHeatCapacity());
        double passiveDelta = computePassiveCoolingDelta(machine.getHeat(), coolingFloorHU,
                hm.passiveCoolingConductivity);

        machine.setHeat(machine.getHeat() + passiveDelta);
        if (passiveDelta < 0) {
            machine.lastHeatRemovedPerTick += -passiveDelta;
        } else {
            machine.lastHeatGainedPerTick += passiveDelta;
        }

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

        overcooledThisTick = running && hasActiveRods && machine.getHeat() <= coolingFloorHU;
        machine.isOverCooled = overcooledThisTick;

        machine.lastHasCoolant = true;

        double heatCapacityForCoolers = machine.getHeatCapacity();
        boolean ratedModel = isRated(hm);
        boolean requiresActiveCooling = comp.getActiveCoolers().stream()
                .anyMatch(c -> !c.isPassive() && (ratedModel ? machine.getHeat() > coolingFloorHU :
                        c.getCoolerTemperature() * heatCapacityForCoolers < machine.getHeat()));

        CoolerLoad load = computeCoolerLoad();

        if (requiresActiveCooling && PhoenixFissionConfigs.INSTANCE.fission.coolingRequiresCoolant) {
            // Simulate fluid and item coolant first so neither is consumed if the other is missing.
            machine.lastHasCoolant = processCoolantAction(false, load) && processItemCoolantAction(false, load) &&
                    processCoolantAction(true, load) && processItemCoolantAction(true, load);
        }

        if (requiresActiveCooling &&
                (!PhoenixFissionConfigs.INSTANCE.fission.coolingRequiresCoolant || machine.lastHasCoolant)) {
            for (var entry : load.counts().entrySet()) {
                double removed = load.removal().getOrDefault(entry.getKey(), 0.0);
                if (removed > 0) {
                    machine.setHeat(machine.getHeat() - removed);
                    activeCoolingRemoved += removed;
                }
            }
        }

        machine.lastHeatGainedPerTick += activeHeatingAdded;
        machine.lastHeatRemovedPerTick += activeCoolingRemoved;
        machine.lastProvidedCooling = (int) Math.round(calculateMaxCooling(machine.getMaxSafeHeatHU()));

        limitHeat();

        if (machine.getHeat() > 0.0) {
            machine.getFuelManager().processPowerGeneration(running);
        } else {
            machine.lastGeneratedEUt = 0;
        }

        processMeltdownTimer();
    }

    public boolean canConsumeCoolantForThisTick() {
        CoolerLoad load = computeCoolerLoad();
        return processCoolantAction(false, load) && processItemCoolantAction(false, load);
    }

    static int scaledCoolantAmount(int baseAmountPerBlock, long count, double removalForType,
                                   PhoenixFissionConfigs.HeatModelConfigs hm) {
        if (baseAmountPerBlock <= 0 || count <= 0 || removalForType <= 0) return 0;
        double refHU = Math.max(0.0001, hm.coolantReferenceHeatRemovalHUt);
        double scaled = (baseAmountPerBlock * (double) count) * (removalForType / refHU);
        return (int) Math.round(Math.max(0.0, scaled));
    }

    // Items are discrete but consumption is fractional per tick, so carry the remainder like fuel does.
    private final Map<String, Double> itemCoolantRemainder = new HashMap<>();

    // Package-private for tests: fractional items consumed this tick for one cooler type.
    static double scaledItemAmount(double itemsPerTickPerBlock, long count, double removalForType,
                                   PhoenixFissionConfigs.HeatModelConfigs hm) {
        if (itemsPerTickPerBlock <= 0 || count <= 0 || removalForType <= 0) return 0.0;
        double refHU = Math.max(0.0001, hm.coolantReferenceHeatRemovalHUt);
        return itemsPerTickPerBlock * count * (removalForType / refHU);
    }

    private boolean processItemCoolantAction(boolean execute, CoolerLoad load) {
        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;

        Map<String, Double> pendingPerItem = new HashMap<>();
        Map<String, String> outputIdForItem = new HashMap<>();
        for (var entry : load.counts().entrySet()) {
            IFissionCoolerType c = entry.getKey();
            if (!c.hasItemCoolant()) continue;
            double amount = scaledItemAmount(c.getItemCoolantPerTick(), entry.getValue(),
                    load.usage().getOrDefault(c, 0.0), hm);
            if (amount <= 0) continue;
            pendingPerItem.merge(c.getItemCoolantId(), amount, Double::sum);
            String outId = c.getItemCoolantOutputId();
            if (!outId.isEmpty() && !"none".equalsIgnoreCase(outId) && !outId.equalsIgnoreCase(c.getItemCoolantId())) {
                outputIdForItem.put(c.getItemCoolantId(), outId);
            }
        }

        for (var entry : pendingPerItem.entrySet()) {
            String itemId = entry.getKey();
            double total = itemCoolantRemainder.getOrDefault(itemId, 0.0) + entry.getValue();
            int whole = (int) Math.floor(total);

            ResourceLocation rl = ResourceLocation.tryParse(itemId);
            if (rl == null || !BuiltInRegistries.ITEM.containsKey(rl)) return false;
            var item = Objects.requireNonNull(BuiltInRegistries.ITEM.getOptional(rl).orElse(null));

            if (!execute) {
                // Don't stall the reactor on a zero-item tick, only when a whole item is actually due.
                if (whole > 0 && !machine.executeItemIO(new ItemStack(item, whole), IO.IN, true)) return false;
                continue;
            }

            if (whole > 0 && !machine.executeItemIO(new ItemStack(item, whole), IO.IN, false)) return false;
            itemCoolantRemainder.put(itemId, total - whole);

            String outId = outputIdForItem.get(itemId);
            if (whole > 0 && outId != null) {
                ResourceLocation outRl = ResourceLocation.tryParse(outId);
                if (outRl != null && BuiltInRegistries.ITEM.containsKey(outRl)) {
                    machine.executeItemIO(new ItemStack(Objects.requireNonNull(BuiltInRegistries.ITEM.getOptional(outRl).orElse(null)),
                            whole), IO.OUT);
                }
            }
        }
        return true;
    }

    private boolean processCoolantAction(boolean execute, CoolerLoad load) {
        var cfg = PhoenixFissionConfigs.INSTANCE.fission;
        var hm = cfg.heatModel;
        var comp = machine.getComponentManager();
        if (comp.getActiveCoolers().isEmpty()) return true;

        if (!cfg.coolantUsageAdditive) {
            IFissionCoolerType primary = comp.getPrimaryCoolerType();
            if (primary == null || !primary.hasFluidCoolant()) return true;
            double removalPerBlock = load.usage().getOrDefault(primary, 0.0);
            int scaledIn = scaledCoolantAmount(primary.getCoolantPerTick(), 1, removalPerBlock, hm);
            int scaledOut = scaledCoolantAmount(primary.getOutputCoolantPerTick(), 1, removalPerBlock, hm);
            return handleFluidConversion(primary.getInputCoolantFluidId(), primary.getOutputCoolantFluidId(),
                    scaledIn, scaledOut, execute);
        }

        record ConversionGroup(String inId, String outId) {}

        Map<String, Integer> totalPerInput = new HashMap<>();
        Map<ConversionGroup, Integer> groupInput = new HashMap<>();
        Map<ConversionGroup, Integer> groupOutput = new HashMap<>();

        for (var entry : load.counts().entrySet()) {
            IFissionCoolerType c = entry.getKey();
            if (!c.hasFluidCoolant()) continue;
            double usagePerBlock = load.usage().getOrDefault(c, 0.0);
            int scaledIn = scaledCoolantAmount(c.getCoolantPerTick(), entry.getValue(), usagePerBlock, hm);
            int scaledOut = scaledCoolantAmount(c.getOutputCoolantPerTick(), entry.getValue(), usagePerBlock, hm);
            if (scaledIn <= 0) continue;

            totalPerInput.merge(c.getInputCoolantFluidId(), scaledIn, Integer::sum);
            ConversionGroup key = new ConversionGroup(c.getInputCoolantFluidId(), c.getOutputCoolantFluidId());
            groupInput.merge(key, scaledIn, Integer::sum);
            groupOutput.merge(key, scaledOut, Integer::sum);
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
        if (inAmount <= 0) return true;

        ResourceLocation inRl = ResourceLocation.tryParse(inId);
        if (inRl == null) return false;

        Fluid inFluid = BuiltInRegistries.FLUID.getOptional(inRl).orElse(null);
        if (inFluid == null) return false;

        FluidStack inStack = new FluidStack(inFluid, inAmount);
        if (!machine.executeFluidIO(inStack, IO.IN, !execute)) return false;

        if (execute) {
            if (overcooledThisTick) {
                handleOvercooledRefund(inFluid, inAmount);
            } else {
                handleFluidOutput(inId, outId, outAmount);
            }
        }

        return true;
    }

    private void handleOvercooledRefund(Fluid inFluid, int inAmount) {
        int refund = (int) Math.floor(inAmount * 0.9);
        if (refund > 0) {
            machine.executeFluidIO(new FluidStack(inFluid, refund), IO.OUT, false);
        }
    }

    private void handleFluidOutput(String inId, String outId, int outAmount) {
        if (outAmount <= 0 || outId.equalsIgnoreCase(inId)) return;

        ResourceLocation outRl = ResourceLocation.tryParse(outId);
        if (outRl == null) return;

        Fluid outFluid = BuiltInRegistries.FLUID.getOptional(outRl).orElse(null);
        if (outFluid != null) {
            machine.executeFluidIO(new FluidStack(outFluid, outAmount), IO.OUT, false);
        }
    }

    private double calculateMaxCooling(double maxSafe) {
        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        double heatCapacity = machine.getHeatCapacity();
        double ambientHU = hm.ambientTemperatureHU * heatCapacity;
        double maxCooling = Math.max(0.0, (maxSafe - ambientHU) * hm.passiveCoolingConductivity);
        boolean rated = isRated(hm);
        for (IFissionCoolerType c : machine.getComponentManager().getActiveCoolers()) {
            if (c.isPassive()) {
                maxCooling += c.getFlatCoolingHUt();
            } else if (rated) {
                maxCooling += resolveRatedCooling(c, hm, heatCapacity);
            } else {
                double coolerTempHU = c.getCoolerTemperature() * heatCapacity;
                maxCooling += Math.max(0.0, (maxSafe - coolerTempHU) * resolveConductivity(c, hm));
            }
        }
        return maxCooling;
    }

    public void limitHeat() {
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

        textList.add(Component
                .translatable("phoenix_fission.current_heat_display",
                        String.format("%.1f", machine.getHeat()),
                        String.format("%.1f", machine.getMaxSafeHeatHU()))
                .withStyle(s -> s.withColor(machine.getHeat() > machine.getMaxSafeHeatHU() ? 0xFF3333 : 0x33FF33)));

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
