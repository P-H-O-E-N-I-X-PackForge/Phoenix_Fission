package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
public abstract class FusionBlockBuilder extends BlockBuilder {

    @Setter
    public transient String texture = "";

    protected FusionBlockBuilder(ResourceLocation id) {
        super(id);
    }

    protected abstract String defaultTexture();

    protected ResourceLocation resolvedTexture() {
        ResourceLocation rl = texture.isEmpty() ? null : ResourceLocation.tryParse(texture);
        return rl != null ? rl : ResourceLocation.fromNamespaceAndPath("phoenix_fission", defaultTexture());
    }

    @Override
    protected void generateBlockModels(KubeAssetGenerator gen) {
        gen.blockModel(id, m -> {
            m.parent(ResourceLocation.withDefaultNamespace("block/cube_all"));
            m.texture("all", resolvedTexture().toString());
            m.texture("particle", resolvedTexture().toString());
        });
    }

    @Override
    protected void generateBlockState(VariantBlockStateGenerator gen) {
        gen.simpleVariant("", ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()));
    }

    @Override
    protected void generateItemModel(ModelGenerator m) {
        m.parent(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()));
    }
}
