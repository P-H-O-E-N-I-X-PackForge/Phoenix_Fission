package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.FissionModeratorBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFissionModeratorType;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true, fluent = true)
public class FissionModeratorBlockBuilder extends FissionTintedBlockBuilder {

    @Setter
    public transient int euBoost = 1;
    @Setter
    public transient int fuelDiscount = 1;

    public FissionModeratorBlockBuilder(ResourceLocation i) {
        super(i);
        noValidSpawns(true);
    }

    @Override
    protected String kind() {
        return "moderator";
    }

    @Override
    protected boolean hasActiveState() {
        return false;
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
            return isTextureManual() ? manualTexture(false) : sharedBase();
        }

        @Override
        public ResourceLocation getActiveTexture() {
            return isTextureManual() ? manualTexture(true) : sharedBase();
        }

        @Override
        public boolean isTextureManual() {
            return FissionModeratorBlockBuilder.this.isTextureManual();
        }

        @Override
        public int getTintColor() {
            return tintArgb();
        }

        @Override
        public @NotNull String getSerializedName() {
            return getName();
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
