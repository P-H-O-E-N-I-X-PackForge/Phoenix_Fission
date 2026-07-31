package net.phoenix.phoenix_fission.api.block.iface;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

public interface IFissionFuelRodType extends StringRepresentable {

    @NotNull
    String getName();

    default int getTintColor() {
        return 0xFFFFFFFF;
    }

    default int getNeutronBias() {
        return 0;
    }

    int getBaseHeatProduction();

    @NotNull
    String getFuelKey();

    @NotNull
    String getOutputKey();

    int getDurationTicks();

    int getAmountPerCycle();

    ResourceLocation getTexture();

    @Override
    default @NotNull String getSerializedName() {
        return getName();
    }
}
