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

    /**
     * Optional item/block coolant, consumed in addition to (or instead of) the fluid coolant.
     * Empty string disables it. Any registered item id works, including block items (e.g. "minecraft:ice").
     */
    @NotNull
    default String getItemCoolantId() {
        return "";
    }

    /** Optional item produced from the consumed item coolant (e.g. ice -> water bucket). Empty = no output. */
    @NotNull
    default String getItemCoolantOutputId() {
        return "";
    }

    /**
     * Seconds one cooler block takes to use up one item at the reference heat removal
     * ({@code coolantReferenceHeatRemovalHUt}); scales with actual heat removed like fluid coolant does.
     * Fractions are fine (0.5 = two items a second). 0 or less disables item coolant.
     */
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

    /**
     * RATED heat model: HU/t this cooler removes at the reactor's safe-heat limit (scaling down linearly to
     * nothing at ambient). Negative = unset, falling back to what the CONDUCTIVE model would have delivered
     * at the safe limit: (maxSafeTempK - coolerTemperature) * conductivity * heatCapacity.
     */
    default double getRatedCoolingHUt() {
        return -1.0;
    }

    default double getConductivity() {
        return -1.0;
    }

    default boolean isPassive() {
        return getFlatCoolingHUt() != 0.0;
    }

    /**
     * False (default): the block uses the shared cooler texture and is tinted with {@link #getTintColor()}.
     * True: {@link #getTexture()} (and {@link #getActiveTexture()}) are used as-is, with no tinting.
     */
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
