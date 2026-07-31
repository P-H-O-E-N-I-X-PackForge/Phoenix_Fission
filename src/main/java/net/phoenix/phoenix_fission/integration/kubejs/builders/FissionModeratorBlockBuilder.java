package net.phoenix.phoenix_fission.integration.kubejs.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.FissionModeratorBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFissionModeratorType;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true, fluent = true)
public class FissionModeratorBlockBuilder extends BlockBuilder {

    @Setter
    public transient int euBoost = 1;
    @Setter
    public transient int fuelDiscount = 1;
    @Setter
    public transient String texture = "phoenix_fission:block/fission/moderator_base";

    public FissionModeratorBlockBuilder(ResourceLocation i) {
        super(i);
        noValidSpawns(true);
    }

    public class KjsModeratorType implements IFissionModeratorType {

        @Override
        public @NotNull String getName() {
            return id.getPath();
        }

        @Override
        public int getEUBoost() {
            return Math.max(0, euBoost);
        }

        @Override
        public int getFuelDiscount() {
            return Math.max(0, fuelDiscount);
        }

        @Override
        public double getHeatMultiplier() {
            return 0;
        }

        @Override
        public int getParallelBonus() {
            return 0;
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            ResourceLocation rl = ResourceLocation.tryParse(texture);
            return rl != null ? rl :
                    ResourceLocation.fromNamespaceAndPath("phoenix_fission", "block/fission/moderator_base");
        }

        @Override
        public @NotNull String getSerializedName() {
            return getName();
        }

        @Override
        public Material getMaterial() {
            return GTMaterials.NULL;
        }
    }

    @Override
    public Block createObject() {
        IFissionModeratorType type = new KjsModeratorType();
        FissionModeratorBlock result = new FissionModeratorBlock(this.createProperties(), type);
        PhoenixAPI.FISSION_MODERATORS.put(type, () -> result);
        return result;
    }
}
