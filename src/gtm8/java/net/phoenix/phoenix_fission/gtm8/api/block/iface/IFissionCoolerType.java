package net.phoenix.phoenix_fission.gtm8.api.block.iface;

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

    @NotNull
    default String getItemCoolantId() {
        return "";
    }

    @NotNull
    default String getItemCoolantOutputId() {
        return "";
    }

    default double getItemCoolantSeconds() {
        return 0.0;
    }

    default double getItemCoolantPerTick() {
        double seconds = getItemCoolantSeconds();
        return seconds > 0.0 ? 1.0 / (seconds * 20.0) : 0.0;
    }

    default boolean hasItemCoolant() {
        return !getItemCoolantId().isEmpty() && !"none".equalsIgnoreCase(getItemCoolantId()) &&
                getItemCoolantPerTick() > 0.0;
    }

    default boolean hasFluidCoolant() {
        return !getInputCoolantFluidId().isEmpty() && !"none".equalsIgnoreCase(getInputCoolantFluidId()) &&
                getCoolantPerTick() > 0;
    }

    default double getFlatCoolingHUt() {
        return 0.0;
    }

    default double getRatedCoolingHUt() {
        return -1.0;
    }

    default double getConductivity() {
        return -1.0;
    }

    default boolean isPassive() {
        return getFlatCoolingHUt() != 0.0;
    }

    default boolean isTextureManual() {
        return false;
    }

    default ResourceLocation getActiveTexture() {
        return getTexture();
    }

    default int getTintColor() {
        return 0xFFFFFFFF;
    }

    ResourceLocation getTexture();

    @Override
    default @NotNull String getSerializedName() {
        return getName();
    }
}
