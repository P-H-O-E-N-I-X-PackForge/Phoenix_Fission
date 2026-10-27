package net.phoenix.phoenix_fission.api.block.iface;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IFissionBlanketType extends StringRepresentable {

    @NotNull
    String getName();

    int getDurationTicks();

    int getAmountPerCycle();

    @NotNull
    String getInputKey();

    @NotNull
    String getRequiredFuelKey();

    /**
     * False (default): the block uses the shared blanket texture and is tinted with {@link #getTintColor()}.
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

    record BlanketOutput(String key, int weight, int instability) {}

    List<BlanketOutput> getOutputs();

    @NotNull
    ResourceLocation getTexture();

    @Override
    default @NotNull String getSerializedName() {
        return getName();
    }
}
