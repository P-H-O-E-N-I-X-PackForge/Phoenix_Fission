package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.phoenix_fission.api.block.FusionCoilBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFusionCoilType;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.AdvancedFissionScramHatchPart;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.FissionScramHatchPart;
import net.phoenix.phoenix_fission.client.gui.FusionFancyUIWidget;
import net.phoenix.phoenix_fission.client.gui.FusionGraphsPage;
import net.phoenix.phoenix_fission.client.gui.FusionHistory;
import net.phoenix.phoenix_fission.client.gui.FusionMathPage;
import net.phoenix.phoenix_fission.common.data.item.PhoenixFissionItems;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FusionReactorMachine extends WorkableElectricMultiblockMachine implements IStabilityMonitored {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FusionReactorMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final int UI_WIDTH = 264;
    public static final int UI_HEIGHT = 256;

    @Getter
    @Persisted
    @DescSynced
    private int throttle = 50;
    @Getter
    @Persisted
    @DescSynced
    private double drift = 0;
    @Getter
    @Persisted
    @DescSynced
    private int quenchTicks = 0;

    @Getter
    @DescSynced
    private int innerCoils = 0;
    @Getter
    @DescSynced
    private int outerCoils = 0;
    @Getter
    @DescSynced
    private long plasmaPower = 0;
    @Getter
    @DescSynced
    private long confinement = 0;
    @Getter
    @DescSynced
    private long upkeepEut = 0;
    @Getter
    @DescSynced
    private double fieldRatio = 0;
    @Getter
    @DescSynced
    private long generatedEUt = 0;
    @Getter
    @DescSynced
    private boolean plasmaOn = false;
    @Getter
    @DescSynced
    private boolean fueled = false;
    @Getter
    @DescSynced
    private boolean powered = false;
    @Getter
    @DescSynced
    private boolean scrammed = false;

    @Getter
    @DescSynced
    private int compute = 0;
    @Getter
    @DescSynced
    private int rackLayers = 0;
    @Getter
    @DescSynced
    private int parallels = 1;
    @Getter
    @DescSynced
    private int threads = 1;
    @Getter
    @DescSynced
    private int recipeTier = 1;
    @Getter
    @DescSynced
    private int energyHatches = 0;
    @Getter
    @DescSynced
    private int ocLevels = 0;
    @Getter
    @Persisted
    @DescSynced
    private double contamination = 0;
    @Persisted
    private double wasteBuffer = 0;
    @Getter
    @DescSynced
    private boolean coolantOk = true;
    @Getter
    @DescSynced
    private boolean purgeOk = true;
    @Getter
    @DescSynced
    private boolean wasteBlocked = false;

    @Getter
    @Persisted
    @DescSynced
    private int pressureTarget = 50;
    @Getter
    @DescSynced
    private double pressure = 0;
    @Getter
    @DescSynced
    private double requiredPressure = 0;
    @Getter
    @DescSynced
    private boolean gasOk = true;
    @Getter
    @DescSynced
    private boolean radiationActive = false;
    @Persisted
    private int radiationSeconds = 0;
    @Getter
    @DescSynced
    private int runningThreads = 0;

    private final Map<IFusionCoilType, Integer> coilCounts = new HashMap<>();

    private static final Map<ICoilType, IFusionCoilType> GT_COILS = new HashMap<>();

    private record GtOuterCoil(ICoilType coil) implements IFusionCoilType {

        private static PhoenixFissionConfigs.FusionConfigs cfg() {
            return PhoenixFissionConfigs.INSTANCE.fission.fusion;
        }

        @Override
        public @org.jetbrains.annotations.NotNull String getName() {
            return "gt_coil_" + coil.getName();
        }

        @Override
        public int getTintColor() {
            return 0xFFFFFFFF;
        }

        @Override
        public boolean isInner() {
            return false;
        }

        @Override
        public int getPlasmaHeat() {
            return 0;
        }

        @Override
        public int getConfinement() {
            return (int) Math.round(cfg().outerCoilBaseConfinement * (1.0 + cfg().outerCoilTierBoost * coil.getTier()));
        }

        @Override
        public long getUpkeepEut() {
            return cfg().outerCoilUpkeepEut;
        }

        @Override
        public @org.jetbrains.annotations.NotNull String getCoolantFluidId() {
            return coil.getTier() < cfg().outerCoilCoolantTierSplit ? cfg().coolantLowFluidId :
                    cfg().coolantHighFluidId;
        }

        @Override
        public @org.jetbrains.annotations.NotNull String getHotCoolantFluidId() {
            return getCoolantFluidId();
        }

        @Override
        public int getCoolantPerSecond() {
            return cfg().outerCoilCoolantPerSecond;
        }
    }

    public static class RecipeThread {

        @Nullable
        GTRecipe recipe;
        long progress;
        long duration;
        final Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches = new HashMap<>();

        boolean active() {
            return recipe != null;
        }

        void clear() {
            recipe = null;
            progress = duration = 0;
            chanceCaches.clear();
        }
    }

    private static final int THREAD_SLOTS = 16;
    private final RecipeThread[] extraThreads = new RecipeThread[THREAD_SLOTS];

    {
        for (int i = 0; i < THREAD_SLOTS; i++) extraThreads[i] = new RecipeThread();
    }

    private final ConditionalSubscriptionHandler tickHandler;

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public FusionReactorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
        this.tickHandler = new ConditionalSubscriptionHandler(this, this::tickReactor, this::isFormed);
    }

    public void setPressureTarget(int value) {
        this.pressureTarget = Mth.clamp(value, 0, 100);
    }

    public void setThrottle(int value) {
        this.throttle = Mth.clamp(value, 10, 100);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        classifyCoils();
        updateCompute();
        tickHandler.updateSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        innerCoils = outerCoils = compute = rackLayers = 0;
        parallels = threads = recipeTier = 1;
        energyHatches = ocLevels = 0;
        plasmaPower = confinement = upkeepEut = generatedEUt = 0;
        plasmaOn = fueled = powered = scrammed = false;
        coolantOk = purgeOk = gasOk = true;
        wasteBlocked = false;
        radiationActive = false;
        pressure = requiredPressure = 0;
        runningThreads = 0;
        for (var t : extraThreads) t.clear();
        coilCounts.clear();
        fieldRatio = 0;
        tickHandler.updateSubscription();
    }

    /**
     * GregTech 7 has no substructures, so there is no compute room to build: compute is the flat number from the
     * config. It drives parallels, threads and the recipe tier the same way built compute does elsewhere.
     */
    private void updateCompute() {
        var cfg = PhoenixFissionConfigs.INSTANCE.fission.fusion;
        rackLayers = 0;
        compute = isFormed() ? Math.max(0, cfg.flatCompute) : 0;
        parallels = Mth.clamp(1 + compute / Math.max(1, cfg.computePerParallel), 1, Math.max(1, cfg.maxParallels));
        threads = Mth.clamp(1 + compute / Math.max(1, cfg.computePerThread), 1, Math.max(1, cfg.maxThreads));
        recipeTier = 1 + (compute >= cfg.tier2Compute ? 1 : 0) + (compute >= cfg.tier3Compute ? 1 : 0);
    }

    @Override
    public double getStabilityPercent() {
        return drift;
    }

    private boolean checkHardwareScram() {
        for (var part : getParts()) {
            if (part instanceof FissionScramHatchPart basic && basic.isScrammed()) return true;
            if (part instanceof AdvancedFissionScramHatchPart advanced && advanced.isScrammed()) return true;
        }
        return false;
    }

    private void classifyCoils() {
        if (getLevel() == null || getLevel().isClientSide) return;
        int inner = 0, outer = 0;
        long power = 0, field = 0, upkeep = 0;
        coilCounts.clear();
        var cache = getMultiblockState().getCache();
        if (cache == null) return;
        for (BlockPos pos : cache) {
            var block = getLevel().getBlockState(pos).getBlock();
            IFusionCoilType type;
            if (block instanceof FusionCoilBlock coil) type = coil.getCoilType();
            else if (block instanceof CoilBlock gt) type = GT_COILS.computeIfAbsent(gt.coilType, GtOuterCoil::new);
            else continue;
            coilCounts.merge(type, 1, Integer::sum);
            if (type.isInner()) {
                inner++;
                power += type.getPlasmaHeat();
            } else {
                outer++;
                field += type.getConfinement();
                upkeep += type.getUpkeepEut();
            }
        }
        innerCoils = inner;
        outerCoils = outer;
        plasmaPower = power;
        confinement = field;
        upkeepEut = upkeep;
    }

    private void tickReactor() {
        if (getLevel() == null || getLevel().isClientSide || !isFormed()) return;
        var cfg = PhoenixFissionConfigs.INSTANCE.fission.fusion;

        if (quenchTicks > 0) {
            quenchTicks--;
            plasmaOn = false;
            generatedEUt = 0;
            return;
        }

        for (var part : getParts()) {
            if (part instanceof AdvancedFissionScramHatchPart advanced) advanced.tick();
        }
        boolean scram = checkHardwareScram();
        scrammed = scram;
        boolean enabled = getRecipeLogic().isWorkingEnabled() && !scram;
        double load = throttle / 100.0;
        double heat = plasmaPower * load;
        double required = heat * cfg.confinementPerHeat;

        long upkeep = enabled ? upkeepEut : 0;
        powered = upkeep == 0 || getEnergyContainer().getEnergyStored() >= upkeep;
        if (powered && upkeep > 0) getEnergyContainer().changeEnergy(-upkeep);

        if (getOffsetTimer() % 20 == 0) {
            int amount = Math.max(1, (int) Math.round(cfg.fuelPerSecondMb * load * parallels));
            FluidStack a = fluid(cfg.fuelAFluidId, amount), b = fluid(cfg.fuelBFluidId, amount);
            fueled = enabled && powered && a != null && b != null && HatchIO.fluid(this, a, IO.IN, true) &&
                    HatchIO.fluid(this, b, IO.IN, true) && HatchIO.fluid(this, a, IO.IN, false) &&
                    HatchIO.fluid(this, b, IO.IN, false);
        }

        if (getOffsetTimer() % 20 == 0) {
            updateOverclock(cfg);
            coolantOk = !enabled || !powered || runCoolant(load * parallels);
            updatePressure(enabled && powered, cfg, load);
            updateRadiation(enabled && powered && fueled, cfg);
            if (enabled && powered && fueled)
                handleWaste(cfg, heat * parallels * (radiationActive ? cfg.radiationWasteBoost : 1.0));
            handlePurge(cfg);
        }

        plasmaOn = enabled && powered && fueled;
        double field = (powered ? confinement : confinement * 0.2) * (coolantOk ? 1.0 : 0.6);
        fieldRatio = required <= 0 ? 2.0 : field / required;

        double perTick = cfg.driftPerSecond / 20.0 * (1.0 + contamination / 50.0) * (coolantOk ? 1.0 : 1.5) *
                pressureDriftFactor(cfg) /
                (1.0 + compute / Math.max(1.0, cfg.computeDriftHalf));
        if (plasmaOn) {
            if (fieldRatio < 1.0) drift += perTick * (1.0 + (1.0 - fieldRatio) * 4.0);
            else if (fieldRatio >= 1.25) drift -= perTick * Math.min(2.0, 0.25 + (fieldRatio - 1.25));
            else drift += perTick * 0.25;
        } else {
            drift -= 0.5 / 20.0;
        }
        drift = Mth.clamp(drift, 0, 100);

        if (drift >= 100) {
            quenchTicks = Math.max(1, cfg.quenchCooldownTicks);
            drift = 0;
            plasmaOn = false;
            generatedEUt = 0;
            return;
        }

        generatedEUt = plasmaOn ? (long) Math
                .floor(heat * parallels * cfg.euPerHeat * (radiationActive ? cfg.radiationOutputBoost : 1.0) *
                        (1.0 - drift / 200.0) * (1.0 - Math.min(1.0, Math.abs(pressure - requiredPressure) / 200.0)) *
                        (1.0 - contamination / 200.0)) :
                0;
        if (generatedEUt > 0) getEnergyContainer().addEnergy(generatedEUt);
        tickThreads();
    }

    private double pressureDriftFactor(PhoenixFissionConfigs.FusionConfigs cfg) {
        double dev = Math.abs(pressure - requiredPressure);
        return dev > cfg.pressureTolerance ? 1.0 + (dev - cfg.pressureTolerance) / 25.0 : 1.0;
    }

    private void updatePressure(boolean active, PhoenixFissionConfigs.FusionConfigs cfg, double load) {
        requiredPressure = Math.min(100.0, throttle * cfg.pressurePerThrottle);
        if (!active) {
            pressure = Math.max(0, pressure - 10);
            gasOk = true;
            return;
        }
        int amount = Math.max(1, (int) Math.round(cfg.pressureGasPerSecondMb * pressureTarget / 100.0));
        FluidStack gas = fluid(cfg.pressureGasFluidId, amount);
        gasOk = gas != null && HatchIO.fluid(this, gas, IO.IN, true) && HatchIO.fluid(this, gas, IO.IN, false);
        if (gasOk) {
            pressure = pressure < pressureTarget ? Math.min(pressureTarget, pressure + 10) :
                    Math.max(pressureTarget, pressure - 5);
        } else {
            pressure = Math.max(0, pressure - 10);
        }
    }

    private void updateRadiation(boolean active, PhoenixFissionConfigs.FusionConfigs cfg) {
        if (!active) {
            radiationActive = false;
            radiationSeconds = 0;
            return;
        }
        if (radiationSeconds > 0) {
            radiationSeconds--;
            radiationActive = true;
            return;
        }
        ResourceLocation rl = ResourceLocation.tryParse(cfg.radiationItemId);
        var item = rl == null ? null : ForgeRegistries.ITEMS.getValue(rl);
        if (item != null && item != net.minecraft.world.item.Items.AIR && HatchIO.item(this, new ItemStack(item), IO.IN, true) &&
                HatchIO.item(this, new ItemStack(item), IO.IN, false)) {
            radiationSeconds = Math.max(1, cfg.radiationItemSeconds);
            radiationActive = true;
        } else {
            radiationActive = false;
        }
    }

    private void tickThreads() {
        if (getLevel() == null || getLevel().isClientSide) return;
        int allowed = Math.max(0, threads - 1);
        boolean mainRunning = getRecipeLogic().isWorking();

        int running = mainRunning ? 1 : 0;
        for (var thread : extraThreads) {
            if (!thread.active()) continue;
            running++;
            if (plasmaOn) tickThread(thread);
        }
        runningThreads = running;

        if (mainRunning && plasmaOn && getOffsetTimer() % 5 == 0) {
            for (int i = 0; i < allowed && i < THREAD_SLOTS; i++) {
                if (!extraThreads[i].active()) {
                    tryStartThread(extraThreads[i]);
                    break;
                }
            }
        }
    }



    private void tryStartThread(RecipeThread thread) {
        var iterator = getRecipeLogic().searchRecipe();
        if (iterator == null) return;
        while (iterator.hasNext()) {
            GTRecipe candidate = iterator.next();
            if (candidate == null) continue;
            ModifierFunction modifier = recipeModifier(this, candidate);
            if (modifier == ModifierFunction.NULL) continue;
            GTRecipe modified = modifier.apply(candidate.copy());
            if (modified == null) continue;
            if (!RecipeHelper.matchRecipe(this, modified).isSuccess()) continue;

            var caches = new HashMap<RecipeCapability<?>, Object2IntMap<?>>();
            if (!RecipeHelper.handleRecipeIO(this, modified, IO.IN, caches).isSuccess()) continue;

            thread.recipe = modified;
            thread.progress = 0;
            thread.duration = modified.duration;
            thread.chanceCaches.clear();
            thread.chanceCaches.putAll(caches);
            return;
        }
    }

    private void tickThread(RecipeThread thread) {
        GTRecipe recipe = thread.recipe;
        if (recipe == null) return;
        if (!RecipeHelper.handleTickRecipeIO(this, recipe, IO.IN, thread.chanceCaches).isSuccess()) return;
        thread.progress++;
        if (thread.progress >= thread.duration) {
            RecipeHelper.handleRecipeIO(this, recipe, IO.OUT, thread.chanceCaches);
            thread.clear();
        }
    }

    private void updateOverclock(PhoenixFissionConfigs.FusionConfigs cfg) {
        int hatches = 0;
        for (var part : getParts()) {
            if (part instanceof EnergyHatchPartMachine hatch && hatch.energyContainer != null &&
                    hatch.energyContainer.getInputVoltage() > 0)
                hatches++;
        }
        energyHatches = hatches;
        ocLevels = hatches >= cfg.ocHatchesLevel2 ? 2 : hatches >= cfg.ocHatchesLevel1 ? 1 : 0;
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof FusionReactorMachine reactor) || !reactor.isFormed()) return ModifierFunction.IDENTITY;
        int needed = Math.max(1, recipe.data == null ? 1 : recipe.data.getInt("fusion_tier"));
        if (needed > reactor.recipeTier) {
            return ModifierFunction.cancel(
                    Component.literal("Needs fusion tier " + needed + " (more compute)"));
        }
        int oc = reactor.ocLevels;
        return ModifierFunction.builder()
                .eutMultiplier(Math.pow(2, oc))
                .durationMultiplier(1.0 / Math.pow(2, oc))
                .parallels(reactor.parallels)
                .build();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        return plasmaOn && super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        return plasmaOn && super.onWorking();
    }

    private boolean runCoolant(double load) {
        Map<String, Integer> in = new HashMap<>();
        Map<String, Integer> out = new HashMap<>();
        for (var entry : coilCounts.entrySet()) {
            IFusionCoilType type = entry.getKey();
            if (type.getCoolantFluidId().isEmpty() || type.getCoolantPerSecond() <= 0) continue;
            double scale = type.isInner() ? load : 1.0;
            int amount = (int) Math.ceil(type.getCoolantPerSecond() * entry.getValue() * scale);
            if (amount <= 0) continue;
            in.merge(type.getCoolantFluidId(), amount, Integer::sum);
            if (!type.getHotCoolantFluidId().isEmpty()) out.merge(type.getHotCoolantFluidId(), amount, Integer::sum);
        }
        for (var e : in.entrySet()) {
            FluidStack stack = fluid(e.getKey(), e.getValue());
            if (stack == null || !HatchIO.fluid(this, stack, IO.IN, true)) return false;
        }
        for (var e : out.entrySet()) {
            FluidStack stack = fluid(e.getKey(), e.getValue());
            if (stack != null && !HatchIO.fluid(this, stack, IO.OUT, true)) return false;
        }
        for (var e : in.entrySet()) HatchIO.fluid(this, fluid(e.getKey(), e.getValue()), IO.IN, false);
        for (var e : out.entrySet()) {
            FluidStack stack = fluid(e.getKey(), e.getValue());
            if (stack != null) HatchIO.fluid(this, stack, IO.OUT, false);
        }
        return true;
    }

    private void handleWaste(PhoenixFissionConfigs.FusionConfigs cfg, double heat) {
        wasteBuffer += heat * cfg.wasteRatePerHeat;
        wasteBlocked = false;
        int perItem = Math.max(1, cfg.wastePointsPerItem);
        while (wasteBuffer >= perItem) {
            if (HatchIO.item(this, new ItemStack(PhoenixFissionItems.FUSION_WASTE.get()), IO.OUT, false)) {
                wasteBuffer -= perItem;
            } else {
                wasteBlocked = true;
                contamination = Math.min(100, contamination + cfg.contaminationPerBlockedSecond);
                break;
            }
        }
    }

    private void handlePurge(PhoenixFissionConfigs.FusionConfigs cfg) {
        if (contamination <= 0) {
            purgeOk = true;
            return;
        }
        FluidStack air = fluid(cfg.purgeFluidId, Math.max(1, cfg.purgePerSecondMb));
        FluidStack radon = cfg.radonPerPurgeMb > 0 ? fluid(cfg.radonFluidId, cfg.radonPerPurgeMb) : null;
        purgeOk = air != null && HatchIO.fluid(this, air, IO.IN, true) &&
                (radon == null || HatchIO.fluid(this, radon, IO.OUT, true)) && HatchIO.fluid(this, air, IO.IN, false);
        if (purgeOk) {
            if (radon != null) HatchIO.fluid(this, radon, IO.OUT, false);
            contamination = Math.max(0, contamination - cfg.purgeRate);
        }
    }

    @Nullable
    private static FluidStack fluid(String id, int amount) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
        if (fluid == null || fluid == Fluids.EMPTY) return null;
        return new FluidStack(fluid, amount);
    }

    private FusionHistory history;

    /** Client-side rolling samples for the screen pages; created on first use. */
    public FusionHistory getHistory() {
        if (history == null) history = new FusionHistory();
        return history;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void attachSideTabs(TabsWidget tabs) {
        super.attachSideTabs(tabs);
        tabs.attachSubTab(new FusionMathPage(this));
        tabs.attachSubTab(new FusionGraphsPage(this));
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player entityPlayer) {
        return new ModularUI(UI_WIDTH, UI_HEIGHT, this, entityPlayer)
                .widget(new FusionFancyUIWidget(this, UI_WIDTH, UI_HEIGHT));
    }
}
