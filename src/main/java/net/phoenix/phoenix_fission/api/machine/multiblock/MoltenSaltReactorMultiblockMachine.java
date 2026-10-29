package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.phoenix.phoenix_fission.api.block.iface.IMSRCoreLinerType;
import net.phoenix.phoenix_fission.client.gui.CanvasPainter;
import net.phoenix.phoenix_fission.client.gui.MsrHud;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public class MoltenSaltReactorMultiblockMachine extends FissionWorkableElectricMultiblockMachine {

    @SaveField
    @SyncToClient
    public double xenonPoisonLevel = 0.0;

    @SaveField
    @SyncToClient
    public int structuralLinerCount = 0;

    protected @Nullable IMSRCoreLinerType coreLinerSpec = null;

    @SaveField
    @SyncToClient
    public String linerName = "";
    @SaveField
    @SyncToClient
    public int linerFlowRate = 0;
    @SaveField
    @SyncToClient
    public double linerHeatPerMb = 0.0;

    @SaveField
    @SyncToClient
    public boolean lastCatalystActive = false;
    @SaveField
    @SyncToClient
    public boolean lastXenonPurgeSucceeded = false;

    public MoltenSaltReactorMultiblockMachine(BlockEntityCreationInfo holder, Object... args) {
        super(holder);
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName)) return;

        List<IMSRCoreLinerType> liners = getComponentManager().getActiveLiners();
        this.structuralLinerCount = liners.size();

        this.coreLinerSpec = liners.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.<IMSRCoreLinerType, Long>comparingByValue()
                        .thenComparing(e -> e.getKey().getName()))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (this.coreLinerSpec != null) {
            this.linerName = this.coreLinerSpec.getName();
            this.linerFlowRate = this.coreLinerSpec.getFluidFlowRate();
            this.linerHeatPerMb = this.coreLinerSpec.getHeatPerMb();
        } else {
            this.linerName = "";
            this.linerFlowRate = 0;
            this.linerHeatPerMb = 0.0;
        }

        this.lastHasCoolant = true;
        this.lastParallels = Math.max(1, this.structuralLinerCount);

        this.getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName)) return;
        this.structuralLinerCount = 0;
        this.coreLinerSpec = null;
        this.linerName = "";
        this.linerFlowRate = 0;
        this.linerHeatPerMb = 0.0;
        this.lastCatalystActive = false;
        this.lastXenonPurgeSucceeded = false;
    }

    @Override
    protected boolean shouldRunReactor() {
        if (!isFormed() || coreLinerSpec == null) return false;
        if (isScramActive()) return false;

        int totalRequiredMb = Math.max(1, this.structuralLinerCount) * coreLinerSpec.getFluidFlowRate();

        FluidStack targetSalt = resolveFluidStack(coreLinerSpec.getInputFluidId(), totalRequiredMb);
        assert targetSalt != null;
        return executeFluidIO(targetSalt, IO.IN, true);
    }

    @Override
    public boolean onWorking() {
        boolean wasWorking = super.onWorking();

        ensureAmbientHeatInitialized();

        boolean running = shouldRunReactor();

        lastHeatGainedPerTick = 0.0;
        lastHeatRemovedPerTick = 0.0;
        lastGeneratedEUt = 0;

        lastCatalystActive = false;
        lastXenonPurgeSucceeded = false;

        if (coreLinerSpec == null) return wasWorking;

        int parallels = Math.max(1, this.structuralLinerCount);
        int saltToProcess = parallels * coreLinerSpec.getFluidFlowRate();
        String inputSalt = coreLinerSpec.getInputFluidId();
        String outputSalt = coreLinerSpec.getOutputFluidId();

        if (running) {
            continuousBurnTicks++;
            xenonPoisonLevel = Math.min(0.50, xenonPoisonLevel + 0.00004);
        } else {
            continuousBurnTicks = 0;
            xenonPoisonLevel = Math.max(0.0, xenonPoisonLevel - 0.0002);
        }

        this.lastParallels = parallels;
        double cleanCoreBonus = 1.0;

        if (running) {
            if (tryConvertFuelSalt(inputSalt, outputSalt, saltToProcess)) {
                double heatProduced = (saltToProcess * coreLinerSpec.getHeatPerMb()) * (1.0 - xenonPoisonLevel);
                this.setHeat(this.getHeat() + heatProduced);
                this.lastHeatGainedPerTick = heatProduced;

                int xenonPerTick = Math.max(1, (int) Math.ceil(saltToProcess * 0.1 / 20.0));
                lastXenonPurgeSucceeded = tryOutputPoisonFluidBoolean(xenonPerTick);
                if (lastXenonPurgeSucceeded) {
                    xenonPoisonLevel = Math.max(0.0, xenonPoisonLevel - 0.002);
                    if (xenonPoisonLevel < 0.05) cleanCoreBonus = 1.5;
                }
            } else {
                running = false;
            }
        } else {
            var hm = PhoenixFissionConfigs.INSTANCE.fission.heatModel;
            double structureFactor = 1.0 / Math.max(1, this.structuralLinerCount);

            double ambientHU = hm.ambientTemperatureHU * this.getHeatCapacity();
            double passiveDelta = (ambientHU - this.getHeat()) * hm.passiveCoolingConductivity * 2.0 *
                    structureFactor;
            if (passiveDelta < 0) this.setHeat(this.getHeat() + passiveDelta);
        }

        if (running) {
            double maxSafeHeat = Math.max(1.0, this.getMaxSafeHeatHU());
            double heatRatio = this.getHeat() / maxSafeHeat;
            double thermalEfficiency = 0.5 + (Math.pow(heatRatio, 2) * 2.0);

            double catalystMultiplier = 1.0;
            FluidStack catalystIn = resolveFluidStack("gtceu:liquid_fluorine", 10);
            assert catalystIn != null;
            if (executeFluidIO(catalystIn, IO.IN, true) && executeFluidIO(catalystIn, IO.IN, false)) {
                catalystMultiplier = 3.0;
                lastCatalystActive = true;
                xenonPoisonLevel = Math.min(0.50, xenonPoisonLevel + 0.0004);
            }

            double euPerHeatUnit = PhoenixFissionConfigs.INSTANCE.fission.euOutput.euPerHeatUnit;
            long baseEU = (long) (this.lastHeatGainedPerTick * euPerHeatUnit);

            double structuralDamping = 1.0 - xenonPoisonLevel;

            this.lastGeneratedEUt = (long) (baseEU * thermalEfficiency * structuralDamping * cleanCoreBonus *
                    catalystMultiplier);

            if (this.lastGeneratedEUt > 0) {
                tryAddEnergy(this.lastGeneratedEUt);
            }
        } else {
            this.lastGeneratedEUt = 0;
        }

        return running || wasWorking;
    }

    protected boolean tryConvertFuelSalt(@NotNull String inFluid, @NotNull String outFluid, int mb) {
        if (mb <= 0) return true;

        FluidStack inStack = resolveFluidStack(inFluid, mb);
        FluidStack outStack = resolveFluidStack(outFluid, mb);

        assert inStack != null;
        if (!executeFluidIO(inStack, IO.IN, true)) return false;
        if (!executeFluidIO(inStack, IO.IN, false)) return false;

        assert outStack != null;
        executeFluidIO(outStack, IO.OUT, false);
        return true;
    }

    protected boolean tryOutputPoisonFluidBoolean(int mb) {
        if (mb <= 0) return true;

        FluidStack fs = resolveFluidStack(PhoenixFissionConfigs.INSTANCE.fission.msrReactorPoisoningOutputFluid, mb);
        assert fs != null;
        if (fs.isEmpty()) return false;

        return executeFluidIO(fs, IO.OUT, false);
    }

    @Nullable
    private FluidStack resolveFluidStack(String fluidId, int amount) {
        ResourceLocation rl = ResourceLocation.tryParse(fluidId);
        if (rl == null) return null;
        var fluid = BuiltInRegistries.FLUID.getOptional(rl).orElse(null);
        if (fluid == null || fluid == Fluids.EMPTY) return null;
        return new FluidStack(fluid, amount);
    }

    private void tryAddEnergy(long joules) {
        getEnergyContainer().addEnergy(joules);
    }

    private Component getVoltageFormattedOutputMSR(long euOut) {
        int tier = 0;
        for (int i = 0; i < GTValues.V.length; i++) {
            if (euOut >= GTValues.V[i]) tier = i;
            else break;
        }
        return Component.translatable("phoenix_fission.eu_generation", euOut)
                .append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(GTValues.VNF[tier]))
                .append(Component.literal(")").withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected CanvasPainter createHudPainter() {
        return new MsrHud(this);
    }
}
