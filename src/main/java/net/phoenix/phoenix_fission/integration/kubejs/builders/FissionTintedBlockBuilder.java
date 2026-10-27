package net.phoenix.phoenix_fission.integration.kubejs.builders;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Shared look for fission component blocks. By default the block uses the shared texture for its kind and is
 * tinted with {@link #tintColor}; calling {@code texture(...)} switches to a fully manual texture with no
 * tinting ({@code activeTexture(...)} optionally gives the active state its own).
 */
@Accessors(chain = true, fluent = true)
public abstract class FissionTintedBlockBuilder extends BlockBuilder {

    /** RGB, e.g. 0x44AAFF. */
    @Setter
    public transient int tintColor = 0xFFFFFF;
    /** Empty = use the shared tinted look. Otherwise a texture id like "gtceu:block/casings/solid/uv". */
    @Setter
    public transient String texture = "";
    @Setter
    public transient String activeTexture = "";

    protected FissionTintedBlockBuilder(ResourceLocation id) {
        super(id);
    }

    /** "cooler", "fuel_rod", "moderator" or "blanket" - picks the shared base and masks. */
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
        return rl != null ? rl : ResourceLocation.fromNamespaceAndPath("phoenix_fission", "block/fission/" + kind() + "_base");
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
            m.parent("minecraft:block/cube_all");
            m.texture("all", manualTexture(active).toString());
            m.texture("particle", manualTexture(active).toString());
        } else {
            m.parent("phoenix_fission:block/cube_2_layer_all_tinted");
            m.texture("bot_all", sharedBase().toString());
            // Moderators only have one mask; every other kind has a separate active mask.
            m.texture("top_all", maskName(active && hasActiveState()));
        }
    }

    @Override
    protected void generateBlockModelJsons(AssetJsonGenerator gen) {
        gen.blockModel(id, m -> model(m, false));
        if (hasActiveState()) {
            gen.blockModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_active"),
                    m -> model(m, true));
        }
    }

    @Override
    protected void generateBlockStateJson(VariantBlockStateGenerator gen) {
        String model = id.getNamespace() + ":block/" + id.getPath();
        if (hasActiveState()) {
            gen.simpleVariant("active=false", model);
            gen.simpleVariant("active=true", model + "_active");
        } else {
            gen.simpleVariant("", model);
        }
    }

    @Override
    protected void generateItemModelJson(ModelGenerator m) {
        m.parent(id.getNamespace() + ":block/" + id.getPath());
    }
}
