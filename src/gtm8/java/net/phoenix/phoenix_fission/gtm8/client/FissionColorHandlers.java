package net.phoenix.phoenix_fission.gtm8.client;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.phoenix.phoenix_fission.gtm8.api.PhoenixAPI;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class FissionColorHandlers {

    private FissionColorHandlers() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(FissionColorHandlers::onBlockColors);
        modBus.addListener(FissionColorHandlers::onItemColors);
    }

    private static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        PhoenixAPI.FISSION_FUEL_RODS.forEach((type, block) -> registerBlock(event, block, type::getTintColor));
        PhoenixAPI.FISSION_COOLERS.forEach((type, block) -> registerBlock(event, block, type::getTintColor));
        PhoenixAPI.FISSION_MODERATORS.forEach((type, block) -> registerBlock(event, block, type::getTintColor));
        PhoenixAPI.FISSION_BLANKETS.forEach((type, block) -> registerBlock(event, block, type::getTintColor));
    }

    private static void onItemColors(RegisterColorHandlersEvent.Item event) {
        PhoenixAPI.FISSION_FUEL_RODS.forEach((type, block) -> registerItem(event, block, type::getTintColor));
        PhoenixAPI.FISSION_COOLERS.forEach((type, block) -> registerItem(event, block, type::getTintColor));
        PhoenixAPI.FISSION_MODERATORS.forEach((type, block) -> registerItem(event, block, type::getTintColor));
        PhoenixAPI.FISSION_BLANKETS.forEach((type, block) -> registerItem(event, block, type::getTintColor));
    }

    private static void registerBlock(RegisterColorHandlersEvent.Block event, Supplier<? extends Block> block,
                                      java.util.function.IntSupplier tint) {
        Block b = block.get();
        if (b != null) event.register((state, level, pos, index) -> tint.getAsInt() & 0xFFFFFF, b);
    }

    private static void registerItem(RegisterColorHandlersEvent.Item event, Supplier<? extends Block> block,
                                     java.util.function.IntSupplier tint) {
        Block b = block.get();
        if (b != null) event.register((stack, index) -> tint.getAsInt() & 0xFFFFFF, b);
    }
}
