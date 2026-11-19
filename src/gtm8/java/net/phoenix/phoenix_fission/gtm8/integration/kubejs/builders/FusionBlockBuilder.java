package net.phoenix.phoenix_fission.gtm8.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Plain cube blocks for the fusion reactor's coils, with an optional manual texture. */
@Accessors(chain = true, fluent = true)
public abstract class FusionBlockBuilder extends BlockBuilder {

    /** Texture id like "gtceu:block/casings/solid/uv". Empty uses this kind's default fusion texture. */
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
    protected void generateBlockModelJsons(AssetJsonGenerator gen) {
        gen.blockModel(id, m -> {
            m.parent("minecraft:block/cube_all");
            m.texture("all", resolvedTexture().toString());
            m.texture("particle", resolvedTexture().toString());
        });
    }

    @Override
    protected void generateBlockStateJson(VariantBlockStateGenerator gen) {
        gen.simpleVariant("", id.getNamespace() + ":block/" + id.getPath());
    }

    @Override
    protected void generateItemModelJson(ModelGenerator m) {
        m.parent(id.getNamespace() + ":block/" + id.getPath());
    }
}
