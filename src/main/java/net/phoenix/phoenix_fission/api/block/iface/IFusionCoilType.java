package net.phoenix.phoenix_fission.api.block.iface;

import org.jetbrains.annotations.NotNull;

public interface IFusionCoilType {

    @NotNull
    String getName();

    int getTintColor();

    boolean isInner();

    int getPlasmaHeat();

    int getConfinement();

    long getUpkeepEut();

    default @NotNull String getCoolantFluidId() {
        return "";
    }

    default @NotNull String getHotCoolantFluidId() {
        return "";
    }

    default int getCoolantPerSecond() {
        return 0;
    }
}
