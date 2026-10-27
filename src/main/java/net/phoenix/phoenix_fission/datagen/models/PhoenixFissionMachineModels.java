package net.phoenix.phoenix_fission.datagen.models;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.iface.IFissionBlanketType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionCoolerType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionFuelRodType;
import net.phoenix.phoenix_fission.api.block.iface.IFissionModeratorType;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

public class PhoenixFissionMachineModels {

    private static ResourceLocation tinted2LayerParent() {
        return PhoenixFission.id("block/cube_2_layer_all_tinted");
    }

    private static ResourceLocation base(String kind) {
        return PhoenixFission.id("block/fission/" + kind + "_base");
    }

    /**
     * Tinted by default: the shared kind base underneath and the (grayscale) mask on top, which the
     * block/item colour handlers tint with the type's colour. A type that supplies its own finished
     * texture ({@code isTextureManual}) gets a plain cube with that texture instead.
     */
    private static void buildModels(RegistrateBlockstateProvider prov, Block block, String name, boolean manual,
                                    ResourceLocation texture, ResourceLocation activeTexture, ResourceLocation base,
                                    ResourceLocation mask, ResourceLocation maskOn) {
        var inactive = manual ?
                prov.models().cubeAll(name, texture) :
                prov.models().withExistingParent(name, tinted2LayerParent())
                        .texture("bot_all", base).texture("top_all", mask);
        var active = manual ?
                prov.models().cubeAll(name + "_active", activeTexture) :
                prov.models().withExistingParent(name + "_active", tinted2LayerParent())
                        .texture("bot_all", base).texture("top_all", maskOn);

        prov.getVariantBuilder(block)
                .partialState().with(GTBlockStateProperties.ACTIVE, false)
                .modelForState().modelFile(inactive).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, true)
                .modelForState().modelFile(active).addModel();
    }

    private static ResourceLocation coolerMask() {
        return PhoenixFission.id("block/fission/masks/cooler_mask");
    }

    private static ResourceLocation coolerMaskOn() {
        return PhoenixFission.id("block/fission/masks/cooler_mask_active");
    }

    private static ResourceLocation rodMask() {
        return PhoenixFission.id("block/fission/masks/fuel_rod_mask");
    }

    private static ResourceLocation rodMaskOn() {
        return PhoenixFission.id("block/fission/masks/fuel_rod_mask_active");
    }

    private static ResourceLocation blanketMask() {
        return PhoenixFission.id("block/fission/masks/blanket_mask");
    }

    private static ResourceLocation blanketMaskOn() {
        return PhoenixFission.id("block/fission/masks/blanket_mask_active");
    }

    private static ResourceLocation modMask() {
        return PhoenixFission.id("block/fission/masks/moderator_mask");
    }

    public static <
            T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> createActiveCoolerModel(IFissionCoolerType type) {
        return (ctx, prov) -> buildModels(prov, ctx.getEntry(), ctx.getName(), type.isTextureManual(),
                type.getTexture(), type.getActiveTexture(), base("cooler"), coolerMask(), coolerMaskOn());
    }

    public static <
            T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> createBlanketRodModel(IFissionBlanketType type) {
        return (ctx, prov) -> buildModels(prov, ctx.getEntry(), ctx.getName(), type.isTextureManual(),
                type.getTexture(), type.getActiveTexture(), base("blanket"), blanketMask(), blanketMaskOn());
    }

    public static <
            T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> createFuelRodModel(IFissionFuelRodType type) {
        return (ctx, prov) -> buildModels(prov, ctx.getEntry(), ctx.getName(), type.isTextureManual(),
                type.getTexture(), type.getActiveTexture(), base("fuel_rod"), rodMask(), rodMaskOn());
    }

    public static <
            T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> createFissionModeratorModel(IFissionModeratorType type) {
        return (ctx, prov) -> {
            var model = type.isTextureManual() ?
                    prov.models().cubeAll(ctx.getName(), type.getTexture()) :
                    prov.models().withExistingParent(ctx.getName(), tinted2LayerParent())
                            .texture("bot_all", base("moderator")).texture("top_all", modMask());
            prov.simpleBlock(ctx.getEntry(), model);
        };
    }
}
