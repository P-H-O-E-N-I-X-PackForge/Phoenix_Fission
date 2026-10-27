package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionBlocks;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ThermoelectricGeneratorMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {


    @Getter
    @SyncToClient
    @SaveField
    private int length = 0;

    @Getter
    @SaveField
    @SyncToClient
    private int dynamoTier = GTValues.HV;
    @Getter
    @SaveField
    @SyncToClient
    private int heat = 0;
    @Getter
    @SaveField
    @SyncToClient
    private int cooldownTicks = 0;
    @Getter
    @SaveField
    @SyncToClient
    private boolean heliumActive = false;
    @Getter
    @SaveField
    @SyncToClient
    private long maxHatchOutput = 0;

    protected final ConditionalSubscriptionHandler updateHandler;

    public ThermoelectricGeneratorMachine(BlockEntityCreationInfo holder, Object... args) {
        super(holder);
        this.updateHandler = new ConditionalSubscriptionHandler(this, this::updateLogic, this::isFormed);
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName)) return;
        calculateExchangerLength();
        detectDynamoTier();

        this.updateHandler.updateSubscription();

        getRecipeLogic().updateTickSubscription();

        if (!Objects.requireNonNull(getLevel()).isClientSide) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private void calculateExchangerLength() {
        if (getLevel() == null) return;

        int validLayers = 0;
        Direction back = getFrontFacing().getOpposite();

        for (int depth = 1; depth <= 30; depth++) {
            BlockPos scanPos = getBlockPos().relative(back, depth);
            Block blockAtCenter = getLevel().getBlockState(scanPos).getBlock();

            if (isExchangerLayerBlock(blockAtCenter)) {
                validLayers++;
            } else if (depth > 5) {
                break;
            }
        }
        this.length = Math.max(1, validLayers);
    }

    protected boolean isExchangerLayerBlock(Block block) {
        return block == PhoenixFissionBlocks.FISSILE_SAFE_GEARBOX_CASING.get();
    }

    private void detectDynamoTier() {
        int detectedTier = GTValues.ULV;
        long totalPower = 0;

        var parts = getParts();
        for (MultiblockPartMachine part : parts) {
            if (!(part instanceof EnergyHatchPartMachine hatch)) continue;

            IEnergyContainer container = hatch.energyContainer;
            if (container == null) continue;

            long voltage = container.getOutputVoltage();
            long amperage = container.getOutputAmperage();

            if (voltage <= 0 || amperage <= 0) continue;

            detectedTier = Math.max(detectedTier, GTUtil.getFloorTierByVoltage(voltage));
            totalPower += (voltage * amperage);
        }

        if (this.dynamoTier != detectedTier || this.maxHatchOutput != totalPower) {
            this.dynamoTier = detectedTier;
            this.maxHatchOutput = totalPower;

            if (this.isFormed() && getLevel() != null && !getLevel().isClientSide) {
                getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public int getTier() {
        return isFormed() ? this.dynamoTier : GTValues.ULV;
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof ThermoelectricGeneratorMachine exchanger))
            return ModifierFunction.IDENTITY;

        double multiplier = 1.5 + (Math.max(0, exchanger.getLength() - 1) * 0.25);

        if (exchanger.heliumActive) multiplier *= 2.0;

        return ModifierFunction.builder()
                .eutMultiplier(multiplier * multiplier)
                .build();
    }

    protected void updateLogic() {
        if (getLevel() == null || getLevel().isClientSide) return;

        if (getOffsetTimer() % 20 == 0) {
            detectDynamoTier();
        }

        if (cooldownTicks > 0) {
            cooldownTicks--;
            if (getRecipeLogic().isWorking()) {
                getRecipeLogic().interruptRecipe();
            }
            return;
        }

        if (getRecipeLogic().isWorking()) {
            if (getOffsetTimer() % 20 == 0) {
                if (consumeColdCoolant()) {
                    heat = Math.max(0, heat - 5);
                } else {
                    heat += 10;

                    if (heat >= 100) {
                        heat = 100;
                        cooldownTicks = 600;
                        getRecipeLogic().interruptRecipe();
                    }
                }
            }
        } else {
            if (getOffsetTimer() % 20 == 0 && heat > 0) {
                heat--;
            }
        }
    }

    private boolean consumeColdCoolant() {
        int effectiveLength = Math.max(1, length);
        var cfg = PhoenixFissionConfigs.INSTANCE.fission.thermoelectricGenerator;
        int coolantAmount = (int) (Math.max(1, cfg.baseCoolantAmount) * (1.0 + (effectiveLength - 1) * 0.2));
        int heliumAmount = (int) (25 * (1.0 + (effectiveLength - 1) * 0.2));

        var heliumInput = GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, heliumAmount);
        var heliumOutput = GTMaterials.Helium.getFluid(heliumAmount);

        var heliumRecipe = GTRecipeBuilder.ofRaw()
                .inputFluids(heliumInput)
                .outputFluids(heliumOutput)
                .buildRawRecipe();

        if (RecipeHelper.matchRecipe(this, heliumRecipe).isSuccess()) {
            if (RecipeHelper.handleRecipeIO(this, heliumRecipe, IO.IN, getRecipeLogic().getChanceCaches())
                    .isSuccess()) {
                this.heliumActive = true;
                return RecipeHelper.handleRecipeIO(this, heliumRecipe, IO.OUT, getRecipeLogic().getChanceCaches())
                        .isSuccess();
            }
        }

        this.heliumActive = false;

        FluidStack coldIn = resolveConfiguredFluid(cfg.coldCoolantFluidId, coolantAmount);
        FluidStack hotOut = resolveConfiguredFluid(cfg.hotCoolantFluidId, coolantAmount);
        if (coldIn == null || hotOut == null) return false;

        var coolantRecipe = GTRecipeBuilder.ofRaw()
                .inputFluids(coldIn)
                .outputFluids(hotOut)
                .buildRawRecipe();

        if (RecipeHelper.matchRecipe(this, coolantRecipe).isSuccess()) {
            if (RecipeHelper.handleRecipeIO(this, coolantRecipe, IO.IN, getRecipeLogic().getChanceCaches())
                    .isSuccess()) {
                return RecipeHelper.handleRecipeIO(this, coolantRecipe, IO.OUT, getRecipeLogic().getChanceCaches())
                        .isSuccess();
            }
        }

        return false;
    }

    @Nullable
    private static FluidStack resolveConfiguredFluid(String id, int amount) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(rl).orElse(null);
        if (fluid == null || fluid == Fluids.EMPTY) return null;
        return new FluidStack(fluid, Math.max(1, amount));
    }

}
