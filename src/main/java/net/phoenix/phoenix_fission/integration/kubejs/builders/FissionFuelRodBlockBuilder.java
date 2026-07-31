package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.FissionFuelRodBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true, fluent = true)
public class FissionFuelRodBlockBuilder extends BlockBuilder {

    @Setter
    public transient int baseHeatProduction = 50;
    @Setter
    public transient int tier = 1;
    @Setter
    public transient int durationTicks = 20;
    @Setter
    public transient int amountPerCycle = 1;
    @Setter
    public transient int neutronBias = 0;
    @Setter
    @NotNull
    public transient String fuelKey = "";
    @Setter
    @NotNull
    public transient String outputKey = "";
    @Setter
    public transient String texture = "phoenix_fission:block/fission/fuel_rod_base";

    public FissionFuelRodBlockBuilder(ResourceLocation i) {
        super(i);
        noValidSpawns(true);
    }

    public class KjsFuelRodType implements IFissionFuelRodType {

        @Override
        public @NotNull String getName() {
            return id.getPath();
        }

        @Override
        public int getBaseHeatProduction() {
            return Math.max(0, baseHeatProduction);
        }

        @Override
        public int getDurationTicks() {
            return Math.max(1, durationTicks);
        }

        @Override
        public int getAmountPerCycle() {
            return Math.max(1, amountPerCycle);
        }

        @Override
        public int getNeutronBias() {
            return neutronBias;
        }

        @Override
        public @NotNull String getFuelKey() {
            return fuelKey.isEmpty() ? "minecraft:air" : fuelKey;
        }

        @Override
        public @NotNull String getOutputKey() {
            return outputKey.isEmpty() ? "minecraft:air" : outputKey;
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            ResourceLocation rl = ResourceLocation.tryParse(texture);
            return rl != null ? rl :
                    ResourceLocation.fromNamespaceAndPath("phoenix_fission", "block/fission/fuel_rod_base");
        }

        @Override
        public @NotNull String getSerializedName() {
            return getName();
        }
    }

    @Override
    public Block createObject() {
        KjsFuelRodType type = new KjsFuelRodType();
        FissionFuelRodBlock result = new FissionFuelRodBlock(this.createProperties(), type);
        PhoenixAPI.FISSION_FUEL_RODS.put(type, () -> result);
        return result;
    }
}
