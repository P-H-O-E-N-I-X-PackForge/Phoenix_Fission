package net.phoenix.phoenix_fission.api.machine.multiblock.managers;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
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

    /**
     * cooler.getCoolerTemperature() is a Kelvin value (a coolant's real-world temperature, e.g. ~77K for
     * liquid nitrogen), not a raw HU quantity - it needs the same K-to-HU conversion maxSafeTempK/
     * maxHeatClampTempK already get (temp * heatCapacity) before it can be meaningfully compared against
     * or subtracted from machine.getHeat(). Without it, a cooler's configured temperature is negligible
     * next to raw heat for any multiblock of nontrivial size, so only its conductivity ever mattered in
     * practice - different coolant types barely differed from one another regardless of how cold they
     * were actually supposed to be.
     */
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

    protected CoolerLoad computeCoolerLoad() {
        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        var comp = machine.getComponentManager();
        double heatCapacity = machine.getHeatCapacity();
        Map<IFissionCoolerType, Long> counts = comp.getActiveCoolers().stream()
                .filter(c -> !c.isPassive())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<IFissionCoolerType, Double> removal = new HashMap<>();
        for (var entry : counts.entrySet()) {
            double conductivity = resolveConductivity(entry.getKey(), hm);
            removal.put(entry.getKey(), computeActiveCoolerHeatRemoval(entry.getKey(), machine.getHeat(),
                    entry.getValue(), conductivity, heatCapacity));
        }
        return new CoolerLoad(counts, removal);
    }

    protected record CoolerLoad(Map<IFissionCoolerType, Long> counts, Map<IFissionCoolerType, Double> removal) {}

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

    /**
     * The passive/ambient equilibration floor is pure room temperature - period. It intentionally does
     * NOT fold in any active cooler's getCoolerTemperature(), even though that looks tempting since
     * coolers are right there in scope. An active cooler's own threshold is already handled correctly
     * and separately by computeActiveCoolerHeatRemoval, which compares its coolerTempHU against current
     * heat to decide how much it removes. Folding that same threshold into this SHARED floor as well
     * (via Math.max(ambient, coolerTemp), as this method previously did) meant that any normal, non-heater
     * cooler configured above ambient - which is most of them; genuinely sub-ambient "cryo" coolants are
     * the exception, not the rule - would silently turn passive equilibration into a heat source, pulling
     * an idle, fuel-less reactor UP toward that cooler's own operating temperature instead of down toward
     * real room temperature. A cooler's whole job is to pull heat down when it's hot; it should never be
     * able to pull heat up when it's cold, no matter what its own operating temperature stat says.
     *
     * <p>ambientTemperatureK is a Kelvin value (293K room temp) - converted to actual HU via heatCapacity
     * here, the same way maxSafeTempK/maxHeatClampTempK already are, so this represents real room
     * temperature instead of a few hundred raw HU (near absolute zero once divided back out by
     * heatCapacity for any multiblock bigger than 1 block).
     */
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
        // c.getCoolerTemperature() is Kelvin - convert to HU before comparing against raw heat, same
        // fix as everywhere else in this class.
        double heatCapacityForCoolers = machine.getHeatCapacity();
        boolean requiresActiveCooling = comp.getActiveCoolers().stream()
                .anyMatch(c -> !c.isPassive() && c.getCoolerTemperature() * heatCapacityForCoolers < machine.getHeat());

        CoolerLoad load = computeCoolerLoad();

        if (requiresActiveCooling && PhoenixFissionConfigs.INSTANCE.fission.coolingRequiresCoolant) {
            machine.lastHasCoolant = processCoolantAction(false, load) && processCoolantAction(true, load);
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
        return processCoolantAction(false, computeCoolerLoad());
    }

    // Package-private (not private) so FissionThermalManagerTest can exercise it directly.
    static int scaledCoolantAmount(int baseAmountPerBlock, long count, double removalForType,
                                   PhoenixFissionConfigs.HeatModelConfigs hm) {
        if (baseAmountPerBlock <= 0 || count <= 0 || removalForType <= 0) return 0;
        double refHU = Math.max(0.0001, hm.coolantReferenceHeatRemovalHUt);
        double scaled = (baseAmountPerBlock * (double) count) * (removalForType / refHU);
        return (int) Math.round(Math.max(0.0, scaled));
    }

    private boolean processCoolantAction(boolean execute, CoolerLoad load) {
        var cfg = PhoenixFissionConfigs.INSTANCE.fission;
        var hm = cfg.heatModel;
        var comp = machine.getComponentManager();
        if (comp.getActiveCoolers().isEmpty()) return true;

        if (!cfg.coolantUsageAdditive) {
            IFissionCoolerType primary = comp.getPrimaryCoolerType();
            if (primary == null) return true;
            long count = Math.max(1, load.counts().getOrDefault(primary, 1L));
            double removalPerBlock = load.removal().getOrDefault(primary, 0.0) / count;
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
            if (c.getCoolantPerTick() <= 0 || c.getInputCoolantFluidId().isEmpty() ||
                    "none".equalsIgnoreCase(c.getInputCoolantFluidId()))
                continue;
            double removalForType = load.removal().getOrDefault(c, 0.0);
            int scaledIn = scaledCoolantAmount(c.getCoolantPerTick(), entry.getValue(), removalForType, hm);
            int scaledOut = scaledCoolantAmount(c.getOutputCoolantPerTick(), entry.getValue(), removalForType, hm);
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

        Fluid inFluid = ForgeRegistries.FLUIDS.getValue(inRl);
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

        Fluid outFluid = ForgeRegistries.FLUIDS.getValue(outRl);
        if (outFluid != null) {
            machine.executeFluidIO(new FluidStack(outFluid, outAmount), IO.OUT, false);
        }
    }

    private double calculateMaxCooling(double maxSafe) {
        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        double heatCapacity = machine.getHeatCapacity();
        double ambientHU = hm.ambientTemperatureHU * heatCapacity;
        double maxCooling = Math.max(0.0, (maxSafe - ambientHU) * hm.passiveCoolingConductivity);
        for (IFissionCoolerType c : machine.getComponentManager().getActiveCoolers()) {
            double coolerTempHU = c.getCoolerTemperature() * heatCapacity;
            maxCooling += c.isPassive() ? c.getFlatCoolingHUt() :
                    Math.max(0.0, (maxSafe - coolerTempHU) * resolveConductivity(c, hm));
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
        // Raw HU, matching the "HU" label in the translation string below and
        // MoltenSaltReactorMultiblockMachine's own addDisplayText override.
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