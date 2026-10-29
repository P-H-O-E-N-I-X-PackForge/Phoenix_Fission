package net.phoenix.phoenix_fission.api.block.iface;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

public interface IFissionModeratorType extends StringRepresentable {

    @NotNull
    String getName();

    /**
     * False (default): the block uses the shared moderator texture and is tinted with {@link #getTintColor()}.
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

    int getEUBoost();

    int getFuelDiscount();

    double getHeatMultiplier();

    int getParallelBonus();

    ResourceLocation getTexture();

    @Override
    default @NotNull String getSerializedName() {
        return getName();
    }
}
