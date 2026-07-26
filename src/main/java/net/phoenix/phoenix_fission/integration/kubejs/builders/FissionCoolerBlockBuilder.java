package net.phoenix.phoenix_fission.integration.kubejs.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.IFissionCoolerType;
import net.phoenix.phoenix_fission.common.data.block.FissionCoolerBlock;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true, fluent = true)
public class FissionCoolerBlockBuilder extends BlockBuilder {

    @Setter
    public transient int coolerTemperature = 1000;
    @Setter
    public transient int tier = 1;
    @Setter
    public transient int coolantUsagePerTick = 10;
    @Setter
    public transient int outputCoolantAmountPerTick = -1;
    @Setter
    public transient double flatCoolingHUt = 0.0;
    @Setter
    public transient boolean isPassive = false;

    @Setter
    @NotNull
    public transient String requiredCoolantMaterialId = "gtceu:distilled_water";
    @Setter
    @NotNull
    public transient String outputCoolantFluidId = "gtceu:hot_distilled_water";
    @Setter
    public transient int tintColor = -1;
    @Setter
    public transient String texture = "phoenix_fission:block/fission/cooler_base";

    public FissionCoolerBlockBuilder(ResourceLocation i) {
        super(i);
        noValidSpawns(true);
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
        public double getFlatCoolingHUt() {
            return flatCoolingHUt;
        }

        @Override
        public boolean isPassive() {
            return isPassive || "none".equalsIgnoreCase(requiredCoolantMaterialId);
        }

        @Override
        public int getTier() {
            return Math.max(0, tier);
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            ResourceLocation rl = ResourceLocation.tryParse(texture);
            return rl != null ? rl :
                    ResourceLocation.fromNamespaceAndPath("phoenix_fission", "block/fission/cooler_base");
        }

        @Override
        public int getTintColor() {
            if (tintColor != -1) return tintColor;
            return switch (getTier()) {
                case 1 -> 0xFF7DE7FF;
                case 2 -> 0xFFB07CFF;
                case 3 -> 0xFFFFD27D;
                case 4 -> 0xFFFF7DAA;
                default -> 0xFFFFFFFF;
            };
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
