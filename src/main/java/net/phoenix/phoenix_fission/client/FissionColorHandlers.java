package net.phoenix.phoenix_fission.client;

import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.bus.api.IEventBus;
import net.phoenix.phoenix_fission.api.PhoenixAPI;

import java.util.function.Supplier;

/**
 * Tints every fission component block and its item with its type's {@code getTintColor()}. Applies to both
 * Java-registered and KubeJS-registered components, since both land in the {@link PhoenixAPI} maps. Types that use
 * a manual texture simply have no tinted faces in their model, so the tint has no visible effect on them.
 */
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
