package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.FusionCoilBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFusionCoilType;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true, fluent = true)
public class FusionCoilBlockBuilder extends FusionBlockBuilder {

    @Setter
    public transient boolean inner = false;
    @Setter
    public transient int plasmaHeat = 100;
    @Setter
    public transient int confinement = 120;
    @Setter
    public transient long upkeepEut = 256;
    @Setter
    public transient String coolant = "";
    @Setter
    public transient String hotCoolant = "";
    @Setter
    public transient int coolantPerSecond = 0;

    public FusionCoilBlockBuilder(ResourceLocation id) {
        super(id);
        noValidSpawns(true);
    }

    @Override
    protected String defaultTexture() {
        return inner ? "block/fusion/fusion_inner_coil_t1" : "block/fusion/fusion_outer_coil_t1";
    }

    public class KjsCoilType implements IFusionCoilType {

        @Override
        public @NotNull String getName() {
            return id.getPath();
        }

        @Override
        public int getTintColor() {
            return 0xFFFFFFFF;
        }

        @Override
        public boolean isInner() {
            return inner;
        }

        @Override
        public int getPlasmaHeat() {
            return inner ? Math.max(0, plasmaHeat) : 0;
        }

        @Override
        public int getConfinement() {
            return inner ? 0 : Math.max(0, confinement);
        }

        @Override
        public long getUpkeepEut() {
            return inner ? 0 : Math.max(0, upkeepEut);
        }

        @Override
        public @NotNull String getCoolantFluidId() {
            return coolant;
        }

        @Override
        public @NotNull String getHotCoolantFluidId() {
            return hotCoolant;
        }

        @Override
        public int getCoolantPerSecond() {
            return Math.max(0, coolantPerSecond);
        }
    }

    @Override
    public Block createObject() {
        IFusionCoilType type = new KjsCoilType();
        if (inner) {
            var result = new FusionCoilBlock.Inner(this.createProperties(), type);
            PhoenixAPI.FUSION_INNER_COILS.put(type, () -> result);
            return result;
        }
        var result = new FusionCoilBlock.Outer(this.createProperties(), type);
        PhoenixAPI.FUSION_OUTER_COILS.put(type, () -> result);
        return result;
    }
}
