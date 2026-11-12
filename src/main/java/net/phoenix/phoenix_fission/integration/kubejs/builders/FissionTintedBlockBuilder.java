package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
public abstract class FissionTintedBlockBuilder extends BlockBuilder {

    @Setter
    public transient int tintColor = 0xFFFFFF;

    @Setter
    public transient String texture = "";
    @Setter
    public transient String activeTexture = "";

    protected FissionTintedBlockBuilder(ResourceLocation id) {
        super(id);
    }

    protected abstract String kind();

    protected boolean hasActiveState() {
        return true;
    }

    public boolean isTextureManual() {
        return !texture.isEmpty();
    }

    protected ResourceLocation manualTexture(boolean active) {
        String t = active && !activeTexture.isEmpty() ? activeTexture : texture;
        ResourceLocation rl = ResourceLocation.tryParse(t);
        return rl != null ? rl :
                ResourceLocation.fromNamespaceAndPath("phoenix_fission", "block/fission/" + kind() + "_base");
    }

    protected ResourceLocation sharedBase() {
        return ResourceLocation.fromNamespaceAndPath("phoenix_fission", "block/fission/" + kind() + "_base");
    }

    protected int tintArgb() {
        return tintColor | 0xFF000000;
    }

    private String maskName(boolean active) {
        return "phoenix_fission:block/fission/masks/" + kind() + "_mask" + (active ? "_active" : "");
    }

    private void model(ModelGenerator m, boolean active) {
        if (isTextureManual()) {
            m.parent(ResourceLocation.withDefaultNamespace("block/cube_all"));
            m.texture("all", manualTexture(active).toString());
            m.texture("particle", manualTexture(active).toString());
        } else {
            m.parent(ResourceLocation.fromNamespaceAndPath("phoenix_fission", "block/cube_2_layer_all_tinted"));
            m.texture("bot_all", sharedBase().toString());

            m.texture("top_all", maskName(active && hasActiveState()));
        }
    }

    @Override
    protected void generateBlockModels(KubeAssetGenerator gen) {
        gen.blockModel(id, m -> model(m, false));
        if (hasActiveState()) {
            gen.blockModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_active"),
                    m -> model(m, true));
        }
    }

    @Override
    protected void generateBlockState(VariantBlockStateGenerator gen) {
        ResourceLocation model = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());
        if (hasActiveState()) {
            gen.simpleVariant("active=false", model);
            gen.simpleVariant("active=true", model.withSuffix("_active"));
        } else {
            gen.simpleVariant("", model);
        }
    }

    @Override
    protected void generateItemModel(ModelGenerator m) {
        m.parent(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()));
    }
}
