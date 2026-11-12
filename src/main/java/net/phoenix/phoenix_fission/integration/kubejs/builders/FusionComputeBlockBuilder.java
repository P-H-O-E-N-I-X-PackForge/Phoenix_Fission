package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.FusionComputeBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFusionComputeType;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true, fluent = true)
public class FusionComputeBlockBuilder extends FusionBlockBuilder {

    @Setter
    public transient int compute = 10;

    public FusionComputeBlockBuilder(ResourceLocation id) {
        super(id);
        noValidSpawns(true);
    }

    @Override
    protected String defaultTexture() {
        return "block/fusion/fusion_compute_t1";
    }

    public class KjsComputeType implements IFusionComputeType {

        @Override
        public @NotNull String getName() {
            return id.getPath();
        }

        @Override
        public int getTintColor() {
            return 0xFFFFFFFF;
        }

        @Override
        public int getCompute() {
            return Math.max(0, compute);
        }
    }

    @Override
    public Block createObject() {
        IFusionComputeType type = new KjsComputeType();
        var result = new FusionComputeBlock(this.createProperties(), type);
        PhoenixAPI.FUSION_COMPUTE_MODULES.put(type, () -> result);
        return result;
    }
}
