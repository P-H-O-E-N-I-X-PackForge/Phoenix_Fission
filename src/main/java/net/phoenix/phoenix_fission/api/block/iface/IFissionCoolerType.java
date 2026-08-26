package net.phoenix.phoenix_fission.api.block.iface;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

public interface IFissionCoolerType extends StringRepresentable {

    @NotNull
    String getName();

    int getCoolerTemperature();

    @NotNull
    String getRequiredCoolantMaterialId();

    @NotNull
    default String getOutputCoolantFluidId() {
        return getRequiredCoolantMaterialId();
    }

    @NotNull
    default String getInputCoolantFluidId() {
        return getRequiredCoolantMaterialId();
    }

    int getCoolantUsagePerTick();

    default int getCoolantPerTick() {
        return getCoolantUsagePerTick();
    }

    default int getOutputCoolantPerTick() {
        return getCoolantPerTick();
    }

    default double getFlatCoolingHUt() {
        return 0.0;
    }

    default double getConductivity() {
        return -1.0;
    }

    default boolean isPassive() {
        return getFlatCoolingHUt() != 0.0;
    }

    default int getTintColor() {
        return 0xFFFFFFFF;
    }

    Material getMaterial();

    ResourceLocation getTexture();

    @Override
    default @NotNull String getSerializedName() {
        return getName();
    }
}
