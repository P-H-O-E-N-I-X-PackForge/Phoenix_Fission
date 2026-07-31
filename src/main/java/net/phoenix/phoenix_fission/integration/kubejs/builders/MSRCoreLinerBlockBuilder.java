package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.MSRCoreLinerBlock;
import net.phoenix.phoenix_fission.api.block.iface.IMSRCoreLinerType;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true, fluent = true)
public class MSRCoreLinerBlockBuilder extends BlockBuilder {

    @Setter
    public transient int tier = 1;
    @Setter
    public transient int fluidFlowRate = 20;
    @Setter
    public transient double heatPerMb = 10.0;
    @Setter
    @NotNull
    public transient String inputFluidId = "minecraft:empty";
    @Setter
    @NotNull
    public transient String outputFluidId = "minecraft:empty";
    @Setter
    public transient String texture = "phoenix_fission:block/fission/liner_base";

    public MSRCoreLinerBlockBuilder(ResourceLocation i) {
        super(i);
        noValidSpawns(true);
    }

    public class KjsLinerType implements IMSRCoreLinerType {

        @Override
        public @NotNull String getName() {
            return id.getPath();
        }

        @Override
        public int getFluidFlowRate() {
            return Math.max(0, fluidFlowRate);
        }

        @Override
        public double getHeatPerMb() {
            return Math.max(0.0, heatPerMb);
        }

        @Override
        public @NotNull String getInputFluidId() {
            return inputFluidId.isEmpty() ? "minecraft:empty" : inputFluidId;
        }

        @Override
        public @NotNull String getOutputFluidId() {
            return outputFluidId.isEmpty() ? "minecraft:empty" : outputFluidId;
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            ResourceLocation rl = ResourceLocation.tryParse(texture);
            return rl != null ? rl :
                    ResourceLocation.fromNamespaceAndPath("phoenix_fission", "block/fission/liner_base");
        }

        @Override
        public @NotNull String getSerializedName() {
            return getName();
        }
    }

    @Override
    public Block createObject() {
        IMSRCoreLinerType type = new KjsLinerType();
        MSRCoreLinerBlock result = new MSRCoreLinerBlock(this.createProperties(), type);
        PhoenixAPI.MSR_LINERS.put(type, () -> result);
        return result;
    }
}
