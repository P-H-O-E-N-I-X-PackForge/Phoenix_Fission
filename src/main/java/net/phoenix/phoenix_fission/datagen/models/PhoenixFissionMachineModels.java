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
        return (ctx, prov) -> {
            String name = ctx.getName();
            Block block = ctx.getEntry();

            var inactive = prov.models()
                    .withExistingParent(name, tinted2LayerParent())
                    .texture("bot_all", PhoenixFission.id("block/fission/coolers/" + type.getName()))
                    .texture("top_all", coolerMask());

            var active = prov.models()
                    .withExistingParent(name + "_active", tinted2LayerParent())
                    .texture("bot_all", PhoenixFission.id("block/fission/coolers/" + type.getName()))
                    .texture("top_all", coolerMaskOn());

            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false)
                    .modelForState().modelFile(inactive).addModel()
                    .partialState().with(GTBlockStateProperties.ACTIVE, true)
                    .modelForState().modelFile(active).addModel();
        };
    }

    public static <
            T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> createBlanketRodModel(IFissionBlanketType type) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            Block block = ctx.getEntry();

            var inactive = prov.models()
                    .withExistingParent(name, tinted2LayerParent())
                    .texture("bot_all", PhoenixFission.id("block/fission/blankets/" + type.getName()))
                    .texture("top_all", blanketMask());

            var active = prov.models()
                    .withExistingParent(name + "_active", tinted2LayerParent())
                    .texture("bot_all", PhoenixFission.id("block/fission/blankets/" + type.getName()))
                    .texture("top_all", blanketMaskOn());

            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false)
                    .modelForState().modelFile(inactive).addModel()
                    .partialState().with(GTBlockStateProperties.ACTIVE, true)
                    .modelForState().modelFile(active).addModel();
        };
    }

    public static <
            T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> createFuelRodModel(IFissionFuelRodType type) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            Block block = ctx.getEntry();

            var inactive = prov.models()
                    .withExistingParent(name, tinted2LayerParent())
                    .texture("bot_all", PhoenixFission.id("block/fission/fuel_rods/" + type.getName()))
                    .texture("top_all", rodMask());

            var active = prov.models()
                    .withExistingParent(name + "_active", tinted2LayerParent())
                    .texture("bot_all", PhoenixFission.id("block/fission/fuel_rods/" + type.getName()))
                    .texture("top_all", rodMaskOn());

            prov.getVariantBuilder(block)
                    .partialState().with(GTBlockStateProperties.ACTIVE, false)
                    .modelForState().modelFile(inactive).addModel()
                    .partialState().with(GTBlockStateProperties.ACTIVE, true)
                    .modelForState().modelFile(active).addModel();
        };
    }

    public static <
            T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> createFissionModeratorModel(IFissionModeratorType type) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            Block block = ctx.getEntry();

            var model = prov.models()
                    .withExistingParent(name, tinted2LayerParent())
                    .texture("bot_all", PhoenixFission.id("block/fission/moderators/" + type.getName()))
                    .texture("top_all", modMask());

            prov.simpleBlock(block, model);
        };
    }
}
