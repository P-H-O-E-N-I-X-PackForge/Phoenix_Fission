package net.phoenix.phoenix_fission.api.pattern;

import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.error.PlaceholderError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.*;
import net.phoenix.phoenix_fission.common.data.block.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Pattern predicates that accept any fission component block. They match by block class, so components registered
 * from Java, the default set or KubeJS all work without needing to be in a fixed list; the formed structure is then
 * classified by the machine itself (see FissionComponentManager).
 */
public class PhoenixFissionPredicates {

    private static BlockInfo emptyComponentFallback() {
        var reg = PhoenixFissionBlocks.EMPTY_REACTOR_COMPONENT;
        if (reg == null) return BlockInfo.fromBlockState(Blocks.IRON_BLOCK.defaultBlockState());
        var block = reg.get();
        if (block == null) return BlockInfo.fromBlockState(Blocks.IRON_BLOCK.defaultBlockState());
        return BlockInfo.fromBlockState(block.defaultBlockState());
    }

    private static <B> B safeGet(Supplier<B> supplier) {
        if (supplier == null) return null;
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isEmptyPlaceholder(BlockState state, boolean registryEmpty) {
        if (!registryEmpty) return false;
        var empty = safeGet(PhoenixFissionBlocks.EMPTY_REACTOR_COMPONENT);
        return empty != null && state.is(empty);
    }

    private static <T extends Block> MultiPredicate componentPredicate(String name, Class<T> blockClass,
                                                                       Collection<? extends Supplier<? extends Block>> registered,
                                                                       String tooltipKey) {
        List<BlockInfo> candidates = registered.stream()
                .map(PhoenixFissionPredicates::safeGet)
                .filter(Objects::nonNull)
                .map(b -> BlockInfo.fromBlockState(b.defaultBlockState()))
                .toList();
        if (candidates.isEmpty()) candidates = List.of(emptyComponentFallback());

        Predicate<com.gregtechceu.gtceu.api.multiblock.PredicateContext> test = ctx -> {
            BlockState state = ctx.state();
            return blockClass.isInstance(state.getBlock()) || isEmptyPlaceholder(state, registered.isEmpty());
        };

        return Predicates.builder(name)
                .candidates(candidates)
                .predicate(test)
                .errorFunction(ctx -> PlaceholderError.instance())
                .toMultiPredicate()
                .addTooltips(Component.translatable(tooltipKey));
    }

    public static MultiPredicate fissionCoolers() {
        return componentPredicate("fission_cooler", FissionCoolerBlock.class, PhoenixAPI.FISSION_COOLERS.values(),
                "phoenix.multiblock.pattern.info.multiple_coolers");
    }

    public static MultiPredicate fissionBlankets() {
        return componentPredicate("fission_blanket", FissionBlanketBlock.class, PhoenixAPI.FISSION_BLANKETS.values(),
                "phoenix.multiblock.pattern.info.multiple_blankets");
    }

    public static MultiPredicate fissionFuelRods() {
        return componentPredicate("fission_fuel_rod", FissionFuelRodBlock.class, PhoenixAPI.FISSION_FUEL_RODS.values(),
                "phoenix.multiblock.pattern.info.multiple_fuel_rods");
    }

    public static MultiPredicate fissionModerators() {
        return componentPredicate("fission_moderator", FissionModeratorBlock.class,
                PhoenixAPI.FISSION_MODERATORS.values(), "phoenix.multiblock.pattern.info.multiple_moderators");
    }

    public static MultiPredicate msrCoreLiner() {
        return componentPredicate("msr_core_liner", MSRCoreLinerBlock.class, PhoenixAPI.MSR_LINERS.values(),
                "phoenix.multiblock.pattern.info.multiple_liners");
    }
}
