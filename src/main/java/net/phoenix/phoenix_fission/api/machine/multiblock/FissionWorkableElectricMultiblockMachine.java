package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionComponentManager;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionFuelManager;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionThermalManager;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.AdvancedFissionScramHatchPart;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.FissionScramHatchPart;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.SensorHatchPartMachine;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@MethodsReturnNonnullByDefault
public class FissionWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private final FissionComponentManager componentManager;
    @Getter
    private final FissionThermalManager thermalManager;
    @Getter
    private final FissionFuelManager fuelManager;

    @SaveField
    @SyncToClient
    public int meltdownTimerTicks = -1;
    @SaveField
    @SyncToClient
    public int meltdownTimerMax = 0;
    @SaveField
    public long continuousBurnTicks = 0;
    @Setter
    @SaveField
    @SyncToClient
    @Getter
    protected double heat = 0.0;
    @Setter
    @SaveField
    @Getter
    protected double reactivityFactor = 0.0;

    @SaveField
    @SyncToClient
        @Getter
    protected List<String> persistedCoolerIDs = new ArrayList<>();
    @SaveField
    @SyncToClient
        @Getter
    protected List<String> persistedModeratorIDs = new ArrayList<>();
    @SaveField
    @SyncToClient
        @Getter
    protected List<String> persistedFuelRodIDs = new ArrayList<>();
    @SaveField
    @SyncToClient
        @Getter
    protected List<String> persistedBlanketIDs = new ArrayList<>();
    @SaveField
    @SyncToClient
        @Getter
    protected List<String> persistedStabilityIDs = new ArrayList<>();
    @SaveField
    @SyncToClient
        @Getter
    protected List<String> persistedSensorIDs = new ArrayList<>();
    @SaveField
    @SyncToClient
    public int lastRequiredCooling = 0;
    @SaveField
    @SyncToClient
    public int lastProvidedCooling = 0;
    @SaveField
    @SyncToClient
    public boolean lastHasCoolant = true;
    @SaveField
    @SyncToClient
    public int lastParallels = 1;
    @SaveField
    @SyncToClient
    public long lastGeneratedEUt = 0;
    @SaveField
    @SyncToClient
    public double lastHeatGainedPerTick = 0.0;
    @SaveField
    @SyncToClient
    public double lastHeatRemovedPerTick = 0.0;
    @SaveField
    @SyncToClient
    public boolean isOverCooled = false;
    @SaveField
    @SyncToClient
    private boolean isScrammedClientCache = false;
    @Getter
    @SaveField
    @SyncToClient
    private boolean runningForHud = false;
    @SaveField
    @SyncToClient
    public int multiBlockCount = 0;
    @SaveField
    private boolean ambientHeatInitialized = false;

    protected final ConditionalSubscriptionHandler reactorTickHandler;
    public final Map<String, Double> fuelRemainderPerType = new HashMap<>();

    public FissionWorkableElectricMultiblockMachine(BlockEntityCreationInfo holder, Object... args) {
        super(holder);
        this.componentManager = createComponentManager();
        this.thermalManager = createThermalManager();
        this.fuelManager = createFuelManager();
        this.reactorTickHandler = new ConditionalSubscriptionHandler(this, this::reactorTick, this::isFormed);
    }

    protected FissionComponentManager createComponentManager() {
        return new FissionComponentManager(this);
    }

    protected FissionThermalManager createThermalManager() {
        return new FissionThermalManager(this);
    }

    protected FissionFuelManager createFuelManager() {
        return new FissionFuelManager(this);
    }

    public double getHeatCapacity() {
        var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
        return Math.max(1.0, multiBlockCount * hm.heatCapacityPerBlock);
    }

    public double getMaxSafeHeatHU() {
        return PhoenixFissionConfigs.INSTANCE.fission.heatModel.maxSafeTempK * getHeatCapacity();
    }

    public double getMaxHeatClampHU() {
        return PhoenixFissionConfigs.INSTANCE.fission.heatModel.maxHeatClampTempK * getHeatCapacity();
    }

    public double getHeatKelvin() {
        return heat / getHeatCapacity();
    }

    public void ensureAmbientHeatInitialized() {
        if (!ambientHeatInitialized) {
            heat = PhoenixFissionConfigs.INSTANCE.fission.heatModel.ambientTemperatureHU * getHeatCapacity();
            ambientHeatInitialized = true;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        componentManager.resolvePersistedComponents();
        thermalManager.limitHeat();
        reactorTickHandler.initialize(getLevel());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        reactorTickHandler.unsubscribe();
    }

    // The component manager's active lists are resolved from these persisted ID lists, but that only otherwise
    // runs once at onLoad(). formStructure/invalidateStructure keep the SERVER's copy live; the client only sees
    // the synced ID lists change, so re-resolve whenever one of them does.
    @ClientFieldChangeListener(fieldName = "persistedCoolerIDs")
    private void onCoolerIDsChanged() {
        componentManager.resolvePersistedComponents();
    }

    @ClientFieldChangeListener(fieldName = "persistedModeratorIDs")
    private void onModeratorIDsChanged() {
        componentManager.resolvePersistedComponents();
    }

    @ClientFieldChangeListener(fieldName = "persistedFuelRodIDs")
    private void onFuelRodIDsChanged() {
        componentManager.resolvePersistedComponents();
    }

    @ClientFieldChangeListener(fieldName = "persistedBlanketIDs")
    private void onBlanketIDsChanged() {
        componentManager.resolvePersistedComponents();
    }

    @ClientFieldChangeListener(fieldName = "persistedStabilityIDs")
    private void onStabilityIDsChanged() {
        componentManager.resolvePersistedComponents();
    }

    @ClientFieldChangeListener(fieldName = "persistedSensorIDs")
    private void onSensorIDsChanged() {
        componentManager.resolvePersistedComponents();
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName)) return;

        multiBlockCount = getDefaultPatternState().getCache().size();
        componentManager.onStructureFormed();
        thermalManager.onStructureFormed();
        fuelManager.onStructureFormed();

        reactorTickHandler.updateSubscription();
        setChanged();
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName)) return;

        componentManager.onStructureInvalid();
        thermalManager.onStructureInvalid();
        fuelManager.onStructureInvalid();
        if (this.runningForHud) this.runningForHud = false;
        reactorTickHandler.updateSubscription();
        setChanged();
    }

    protected void reactorTick() {
        if (getLevel() == null || getLevel().isClientSide || !isFormed()) return;

        boolean running = shouldRunReactor();

        if (this.runningForHud != running) {
            this.runningForHud = running;
            setChanged();
        }

        boolean scramFound = checkHardwareScram();
        if (this.isScrammedClientCache != scramFound) {
            this.isScrammedClientCache = scramFound;
            setChanged();
        }

        fuelManager.recalcRuntimeParallels();
        thermalManager.tickThermalLogic(running);

        tickPeripheralHatches();
        setChanged();
    }

    protected boolean shouldRunReactor() {
        if (!isFormed() || !getRecipeLogic().isWorkingEnabled() || !getRecipeLogic().isWorking() ||
                componentManager.getActiveFuelRods().isEmpty() || isScramActive())
            return false;

        if (PhoenixFissionConfigs.INSTANCE.fission.coolingRequiresCoolant &&
                !componentManager.getActiveCoolers().isEmpty()) {
            if (!thermalManager.canConsumeCoolantForThisTick()) return false;
        }

        return fuelManager.hasFuelAvailableForNextTick();
    }

    public boolean isScramActive() {
        return this.isScrammedClientCache;
    }

    private boolean checkHardwareScram() {
        for (var part : getParts()) {
            if (part instanceof FissionScramHatchPart basic && basic.isScrammed()) return true;
            if (part instanceof AdvancedFissionScramHatchPart adv && adv.isScrammed()) return true;
        }
        return false;
    }

    private void tickPeripheralHatches() {
        for (var part : getParts()) {
            if (part instanceof SensorHatchPartMachine sensor) sensor.updateSignal();
            if (part instanceof AdvancedFissionScramHatchPart advanced) advanced.tick();
        }
    }

    public boolean executeFluidIO(FluidStack stack, IO type, boolean simulate) {
        if (stack.isEmpty()) return false;

        GTRecipeBuilder builder = GTRecipeBuilder.ofRaw();

        if (type == IO.IN) {
            builder.input(FluidRecipeCapability.CAP,
                    RecipeHelper.makeSizedFluidIngredient(stack));
        } else {
            builder.output(FluidRecipeCapability.CAP,
                    RecipeHelper.makeSizedFluidIngredient(stack));
        }

        GTRecipe dummy = builder.buildRawRecipe();

        if (type == IO.IN) {
            boolean matches = RecipeHelper.matchRecipe(this, dummy).isSuccess();
            if (simulate || !matches) return matches;
        }

        if (simulate) return true;

        return RecipeHelper.handleRecipeIO(this, dummy, type, getRecipeLogic().getChanceCaches()).isSuccess();
    }

    public void executeItemIO(ItemStack stack, IO type) {
        executeItemIO(stack, type, false);
    }

    public boolean executeItemIO(ItemStack stack, IO type, boolean simulate) {
        if (stack.isEmpty()) return false;

        GTRecipeBuilder builder = GTRecipeBuilder.ofRaw();

        if (type == IO.IN) {
            builder.input(ItemRecipeCapability.CAP,
                    RecipeHelper.makeSizedIngredient(stack));
        } else {
            builder.output(ItemRecipeCapability.CAP,
                    RecipeHelper.makeSizedIngredient(stack));
        }

        GTRecipe dummy = builder.buildRawRecipe();

        if (type == IO.IN) {
            boolean matches = RecipeHelper.matchRecipe(this, dummy).isSuccess();
            if (simulate || !matches) return matches;
        }

        if (simulate) return true;

        return RecipeHelper.handleRecipeIO(this, dummy, type, getRecipeLogic().getChanceCaches()).isSuccess();
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof FissionWorkableElectricMultiblockMachine m) || !m.isFormed())
            return ModifierFunction.IDENTITY;
        return FissionFuelManager.buildRecipeModifier(m);
    }
}
