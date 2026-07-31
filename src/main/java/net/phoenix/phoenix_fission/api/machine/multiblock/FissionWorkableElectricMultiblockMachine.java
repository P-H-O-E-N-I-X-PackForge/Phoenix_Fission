package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionComponentManager;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionFuelManager;
import net.phoenix.phoenix_fission.api.machine.multiblock.managers.FissionThermalManager;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.AdvancedFissionScramHatchPart;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.FissionScramHatchPart;
import net.phoenix.phoenix_fission.api.machine.multiblock.part.SensorHatchPartMachine;
import net.phoenix.phoenix_fission.client.gui.FissionGraphsPage;
import net.phoenix.phoenix_fission.client.gui.FissionMathPage;
import net.phoenix.phoenix_fission.client.gui.FissionReactorFancyUIWidget;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@MethodsReturnNonnullByDefault
public class FissionWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine
                                                      implements IExplosionMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FissionWorkableElectricMultiblockMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Getter
    private final FissionComponentManager componentManager;
    @Getter
    private final FissionThermalManager thermalManager;
    @Getter
    private final FissionFuelManager fuelManager;

    @Persisted
    @DescSynced
    public int meltdownTimerTicks = -1;
    @Persisted
    @DescSynced
    public int meltdownTimerMax = 0;
    @Persisted
    public long continuousBurnTicks = 0;
    @Setter
    @Persisted
    @DescSynced
    @Getter
    protected double heat = 0.0;
    @Setter
    @Persisted
    @Getter
    protected double reactivityFactor = 0.0;
    @Persisted
    @DescSynced
    @Getter
    protected List<String> persistedCoolerIDs = new ArrayList<>();
    @Persisted
    @DescSynced
    @Getter
    protected List<String> persistedModeratorIDs = new ArrayList<>();
    @Persisted
    @DescSynced
    @Getter
    protected List<String> persistedFuelRodIDs = new ArrayList<>();
    @Persisted
    @DescSynced
    @Getter
    protected List<String> persistedBlanketIDs = new ArrayList<>();
    @Persisted
    @DescSynced
    @Getter
    protected List<String> persistedStabilityIDs = new ArrayList<>();
    @Persisted
    @DescSynced
    @Getter
    protected List<String> persistedSensorIDs = new ArrayList<>();
    @Persisted
    @DescSynced
    public int lastRequiredCooling = 0;
    @Persisted
    @DescSynced
    public int lastProvidedCooling = 0;
    @Persisted
    @DescSynced
    public boolean lastHasCoolant = true;
    @Persisted
    @DescSynced
    public int lastParallels = 1;
    @Persisted
    @DescSynced
    public long lastGeneratedEUt = 0;
    @Persisted
    @DescSynced
    public double lastHeatGainedPerTick = 0.0;
    @Persisted
    @DescSynced
    public double lastHeatRemovedPerTick = 0.0;
    @Persisted
    @DescSynced
    public boolean isOverCooled = false;
    @Persisted
    @DescSynced
    private boolean isScrammedClientCache = false;
    @Getter
    @Persisted
    @DescSynced
    private boolean runningForHud = false;
    @Persisted
    @DescSynced
    public int multiBlockCount = 0;
    @Persisted
    private boolean ambientHeatInitialized = false;

    protected final ConditionalSubscriptionHandler reactorTickHandler;
    public final Map<String, Double> fuelRemainderPerType = new HashMap<>();

    public FissionWorkableElectricMultiblockMachine(IMachineBlockEntity holder) {
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

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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

    @Override
    public void onLoad() {
        super.onLoad();
        componentManager.resolvePersistedComponents();
        thermalManager.limitHeat();
        reactorTickHandler.updateSubscription();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        multiBlockCount = getMultiblockState().getCache() != null ? getMultiblockState().getCache().size() : 0;
        componentManager.onStructureFormed();
        thermalManager.onStructureFormed();
        fuelManager.onStructureFormed();

        if (!ambientHeatInitialized) {
            heat = 0.0;
            ambientHeatInitialized = true;
        }

        reactorTickHandler.updateSubscription();
        markDirty();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        componentManager.onStructureInvalid();
        thermalManager.onStructureInvalid();
        fuelManager.onStructureInvalid();
        if (this.runningForHud) this.runningForHud = false;
        reactorTickHandler.updateSubscription();
        markDirty();
    }

    protected void reactorTick() {
        if (getLevel() == null || getLevel().isClientSide || !isFormed()) return;

        boolean running = shouldRunReactor();

        if (this.runningForHud != running) {
            this.runningForHud = running;
            this.markDirty();
            this.getHolder().self().setChanged();
        }

        boolean scramFound = checkHardwareScram();
        if (this.isScrammedClientCache != scramFound) {
            this.isScrammedClientCache = scramFound;
            this.markDirty();
            this.getHolder().self().setChanged();
        }

        fuelManager.recalcRuntimeParallels();
        thermalManager.tickThermalLogic(running);

        tickPeripheralHatches();
        markDirty();
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
                    FluidIngredient.of(stack));
        } else {
            builder.output(FluidRecipeCapability.CAP,
                    FluidIngredient.of(stack));
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
                    SizedIngredient.create(stack));
        } else {
            builder.output(ItemRecipeCapability.CAP,
                    SizedIngredient.create(stack));
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void attachSideTabs(TabsWidget tabs) {
        super.attachSideTabs(tabs);
        tabs.attachSubTab(new FissionMathPage(this));
        tabs.attachSubTab(new FissionGraphsPage(this));
    }

    @Override
    public ModularUI createUI(@NotNull Player player) {
        if (getLevel() != null && !getLevel().isClientSide) {
            getLevel().sendBlockUpdated(getPos(), getBlockState(), getBlockState(), 3);
        }
        return new ModularUI(264, 256, this, player)
                .widget(new FissionReactorFancyUIWidget(this, 264, 256));
    }

    @Override
    public Widget createUIWidget() {
        return new WidgetGroup(0, 0, 224, 196);
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) {
            textList.add(Component.translatable("phoenix_fission.not_formed").withStyle(s -> s.withColor(0xFF4444)));
            return;
        }
        thermalManager.addDisplayText(textList);
        fuelManager.addDisplayText(textList);
        componentManager.addDisplayText(textList);
    }
}
