package net.phoenix.phoenix_fission.api.block.iface;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public interface IMSRCoreLinerType extends StringRepresentable {

    String getName();

    ResourceLocation getTexture();

    int getFluidFlowRate();

    double getHeatPerMb();

    String getInputFluidId();

    String getOutputFluidId();
}
