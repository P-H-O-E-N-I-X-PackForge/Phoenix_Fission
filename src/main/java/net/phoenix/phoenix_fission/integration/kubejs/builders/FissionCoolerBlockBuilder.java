package net.phoenix.phoenix_fission.integration.kubejs.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.FissionCoolerBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true, fluent = true)
public class FissionCoolerBlockBuilder extends FissionTintedBlockBuilder {

    @Setter
    public transient int coolerTemperature = 1000;
    @Setter
    public transient int coolantUsagePerTick = 10;
    @Setter
    public transient int outputCoolantAmountPerTick = -1;
    @Setter
    public transient double flatCoolingHUt = 0.0;
    @Setter
    public transient boolean isPassive = false;
    @Setter
    public transient double conductivity = -1.0;
    @Setter
    public transient double ratedCoolingHUt = -1.0;

    @Setter
    @NotNull
    public transient String requiredCoolantMaterialId = "gtceu:distilled_water";
    @Setter
    @NotNull
    public transient String outputCoolantFluidId = "gtceu:steam";
    @Setter
    @NotNull
    public transient String itemCoolantId = "";
    @Setter
    @NotNull
    public transient String itemCoolantOutputId = "";
    @Setter
    public transient double itemCoolantSeconds = 0.0;

    public FissionCoolerBlockBuilder(ResourceLocation i) {
        super(i);
        noValidSpawns(true);
    }

    @Override
    protected String kind() {
        return "cooler";
    }

    public class KjsCoolerType implements IFissionCoolerType {

        @Override
        public @NotNull String getName() {
            return id.getPath();
        }

        @Override
        public int getCoolerTemperature() {
            return Math.max(0, coolerTemperature);
        }

        @Override
        public int getCoolantUsagePerTick() {
            return Math.max(0, coolantUsagePerTick);
        }

        @Override
        public int getCoolantPerTick() {
            return getCoolantUsagePerTick();
        }

        @Override
        public int getOutputCoolantPerTick() {
            return outputCoolantAmountPerTick >= 0 ? outputCoolantAmountPerTick : getCoolantUsagePerTick();
        }

        @Override
        public @NotNull String getRequiredCoolantMaterialId() {
            return isPassive() ? "none" : requiredCoolantMaterialId;
        }

        @Override
        public @NotNull String getOutputCoolantFluidId() {
            return isPassive() ? "none" : outputCoolantFluidId;
        }

        @Override
        public @NotNull String getItemCoolantId() {
            return isPassive() ? "" : itemCoolantId;
        }

        @Override
        public @NotNull String getItemCoolantOutputId() {
            return isPassive() ? "" : itemCoolantOutputId;
        }

        @Override
        public double getItemCoolantSeconds() {
            return isPassive() ? 0.0 : Math.max(0.0, itemCoolantSeconds);
        }

        @Override
        public double getFlatCoolingHUt() {
            return flatCoolingHUt;
        }

        @Override
        public double getRatedCoolingHUt() {
            return ratedCoolingHUt;
        }

        @Override
        public double getConductivity() {
            return conductivity;
        }

        @Override
        public boolean isPassive() {
            // "none" fluid only means passive if there's no item coolant either (item-only coolers are active).
            boolean noFluid = "none".equalsIgnoreCase(requiredCoolantMaterialId);
            boolean hasItem = !itemCoolantId.isEmpty() && !"none".equalsIgnoreCase(itemCoolantId) &&
                    itemCoolantSeconds > 0.0;
            return isPassive || (noFluid && !hasItem);
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            return isTextureManual() ? manualTexture(false) : sharedBase();
        }

        @Override
        public ResourceLocation getActiveTexture() {
            return isTextureManual() ? manualTexture(true) : sharedBase();
        }

        @Override
        public boolean isTextureManual() {
            return FissionCoolerBlockBuilder.this.isTextureManual();
        }

        @Override
        public int getTintColor() {
            return tintArgb();
        }

        @Override
        public @NotNull String getSerializedName() {
            return getName();
        }

        @Override
        public @NotNull Material getMaterial() {
            return GTMaterials.NULL;
        }
    }

    @Override
    public Block createObject() {
        IFissionCoolerType type = new KjsCoolerType();
        FissionCoolerBlock result = new FissionCoolerBlock(this.createProperties(), type);
        PhoenixAPI.FISSION_COOLERS.put(type, () -> result);
        return result;
    }
}
