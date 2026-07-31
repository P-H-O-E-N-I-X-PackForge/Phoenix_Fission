package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.FissionBlanketBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFissionBlanketType;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true, fluent = true)
public class FissionBlanketRodBlockBuilder extends BlockBuilder {

    @Setter
    public transient int durationTicks = 1200;
    @Setter
    public transient int amountPerCycle = 1;
    @Setter
    @NotNull
    public transient String inputKey = "";

    public transient List<IFissionBlanketType.BlanketOutput> outputs = new ArrayList<>();

    @Setter
    public transient String texture = "phoenix_fission:block/fission/blanket_base";

    public FissionBlanketRodBlockBuilder(ResourceLocation i) {
        super(i);
        noValidSpawns(true);
    }

    public class KjsBlanketType implements IFissionBlanketType {

        @Override
        public int getDurationTicks() {
            return Math.max(1, durationTicks);
        }

        @Override
        public int getAmountPerCycle() {
            return Math.max(1, amountPerCycle);
        }

        @Override
        public @NotNull String getInputKey() {
            return inputKey.isEmpty() ? "minecraft:air" : inputKey;
        }

        @Override
        public @NotNull String getRequiredFuelKey() {
            return "";
        }

        @Override
        public List<BlanketOutput> getOutputs() {
            return outputs;
        }

        @Override
        public @NotNull ResourceLocation getTexture() {
            ResourceLocation rl = ResourceLocation.tryParse(texture);
            return rl != null ? rl :
                    ResourceLocation.fromNamespaceAndPath("phoenix_fission", "block/fission/blanket_base");
        }

        @Override
        public @NotNull String getSerializedName() {
            return getName();
        }

        @Override
        public @NotNull String getName() {
            return id.getPath();
        }
    }

    @Override
    public Block createObject() {
        IFissionBlanketType type = new KjsBlanketType();
        FissionBlanketBlock result = new FissionBlanketBlock(this.createProperties(), type);
        PhoenixAPI.FISSION_BLANKETS.put(type, () -> result);
        return result;
    }
}
