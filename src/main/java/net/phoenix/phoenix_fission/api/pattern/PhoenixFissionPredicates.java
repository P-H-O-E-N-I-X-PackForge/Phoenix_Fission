package net.phoenix.phoenix_fission.api.pattern;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.*;
import net.phoenix.phoenix_fission.api.block.iface.*;
import net.phoenix.phoenix_fission.common.data.block.*;

import java.util.*;
import java.util.function.Supplier;

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

    public static TraceabilityPredicate fissionCoolers() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            var emptyBlock = safeGet(PhoenixFissionBlocks.EMPTY_REACTOR_COMPONENT);
            if (PhoenixAPI.FISSION_COOLERS.isEmpty() && emptyBlock != null && blockState.is(emptyBlock)) {
                return true;
            }

            for (Map.Entry<IFissionCoolerType, Supplier<FissionCoolerBlock>> entry : PhoenixAPI.FISSION_COOLERS
                    .entrySet()) {
                var block = safeGet(entry.getValue());
                if (block == null) continue;
                if (blockState.is(block)) {
                    List<IFissionCoolerType> componentList = blockWorldState.getMatchContext().getOrPut("CoolerTypes",
                            new ArrayList<>());
                    componentList.add(entry.getKey());
                    return true;
                }
            }
            return false;
        }, () -> {
            var infos = PhoenixAPI.FISSION_COOLERS.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getName()))
                    .map(e -> safeGet(e.getValue()))
                    .filter(Objects::nonNull)
                    .map(b -> BlockInfo.fromBlockState(b.defaultBlockState()))
                    .toArray(BlockInfo[]::new);
            return infos.length > 0 ? infos : new BlockInfo[] { emptyComponentFallback() };
        }).addTooltips(Component.translatable("phoenix.multiblock.pattern.info.multiple_coolers"));
    }

    public static TraceabilityPredicate fissionBlankets() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            var emptyBlock = safeGet(PhoenixFissionBlocks.EMPTY_REACTOR_COMPONENT);
            if (PhoenixAPI.FISSION_BLANKETS.isEmpty() && emptyBlock != null && blockState.is(emptyBlock)) {
                return true;
            }

            for (Map.Entry<IFissionBlanketType, Supplier<FissionBlanketBlock>> entry : PhoenixAPI.FISSION_BLANKETS
                    .entrySet()) {
                var block = safeGet(entry.getValue());
                if (block == null) continue;
                if (blockState.is(block)) {
                    List<IFissionBlanketType> componentList = blockWorldState.getMatchContext().getOrPut("BlanketTypes",
                            new ArrayList<>());
                    componentList.add(entry.getKey());
                    return true;
                }
            }
            return false;
        }, () -> {
            var infos = PhoenixAPI.FISSION_BLANKETS.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getName()))
                    .map(e -> safeGet(e.getValue()))
                    .filter(Objects::nonNull)
                    .map(b -> BlockInfo.fromBlockState(b.defaultBlockState()))
                    .toArray(BlockInfo[]::new);
            return infos.length > 0 ? infos : new BlockInfo[] { emptyComponentFallback() };
        }).addTooltips(Component.translatable("phoenix.multiblock.pattern.info.multiple_blankets"));
    }

    public static TraceabilityPredicate fissionFuelRods() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            var emptyBlock = safeGet(PhoenixFissionBlocks.EMPTY_REACTOR_COMPONENT);
            if (PhoenixAPI.FISSION_FUEL_RODS.isEmpty() && emptyBlock != null && blockState.is(emptyBlock)) {
                return true;
            }

            for (Map.Entry<IFissionFuelRodType, Supplier<FissionFuelRodBlock>> entry : PhoenixAPI.FISSION_FUEL_RODS
                    .entrySet()) {
                var block = safeGet(entry.getValue());
                if (block == null) continue;
                if (blockState.is(block)) {
                    List<IFissionFuelRodType> componentList = blockWorldState.getMatchContext().getOrPut("FuelRodTypes",
                            new ArrayList<>());
                    componentList.add(entry.getKey());
                    return true;
                }
            }
            return false;
        }, () -> {
            var infos = PhoenixAPI.FISSION_FUEL_RODS.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getName()))
                    .map(e -> safeGet(e.getValue()))
                    .filter(Objects::nonNull)
                    .map(b -> BlockInfo.fromBlockState(b.defaultBlockState()))
                    .toArray(BlockInfo[]::new);
            return infos.length > 0 ? infos : new BlockInfo[] { emptyComponentFallback() };
        }).addTooltips(Component.translatable("phoenix.multiblock.pattern.info.multiple_fuel_rods"));
    }

    public static TraceabilityPredicate fissionModerators() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            var emptyBlock = safeGet(PhoenixFissionBlocks.EMPTY_REACTOR_COMPONENT);
            if (PhoenixAPI.FISSION_MODERATORS.isEmpty() && emptyBlock != null && blockState.is(emptyBlock)) {
                return true;
            }

            for (Map.Entry<IFissionModeratorType, Supplier<FissionModeratorBlock>> entry : PhoenixAPI.FISSION_MODERATORS
                    .entrySet()) {
                var block = safeGet(entry.getValue());
                if (block == null) continue;
                if (blockState.is(block)) {
                    List<IFissionModeratorType> componentList = blockWorldState.getMatchContext()
                            .getOrPut("ModeratorTypes", new ArrayList<>());
                    componentList.add(entry.getKey());
                    return true;
                }
            }
            return false;
        }, () -> {
            var infos = PhoenixAPI.FISSION_MODERATORS.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getName()))
                    .map(e -> safeGet(e.getValue()))
                    .filter(Objects::nonNull)
                    .map(b -> BlockInfo.fromBlockState(b.defaultBlockState()))
                    .toArray(BlockInfo[]::new);
            return infos.length > 0 ? infos : new BlockInfo[] { emptyComponentFallback() };
        }).addTooltips(Component.translatable("phoenix.multiblock.pattern.info.multiple_moderators"));
    }

    public static TraceabilityPredicate msrCoreLiner() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();

            var emptyBlock = safeGet(PhoenixFissionBlocks.EMPTY_REACTOR_COMPONENT);
            if (PhoenixAPI.MSR_LINERS.isEmpty() && emptyBlock != null && blockState.is(emptyBlock)) {
                return true;
            }

            for (Map.Entry<IMSRCoreLinerType, Supplier<MSRCoreLinerBlock>> entry : PhoenixAPI.MSR_LINERS.entrySet()) {
                var block = safeGet(entry.getValue());
                if (block == null) continue;
                if (blockState.is(block)) {
                    var type = entry.getKey();
                    blockWorldState.getMatchContext().increment("MSRLinerCount", 1);
                    List<IMSRCoreLinerType> componentList = blockWorldState.getMatchContext().getOrPut("LinerTypes",
                            new ArrayList<>());
                    componentList.add(type);
                    return true;
                }
            }
            return false;
        }, () -> {
            var infos = PhoenixAPI.MSR_LINERS.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().getName()))
                    .map(e -> safeGet(e.getValue()))
                    .filter(Objects::nonNull)
                    .map(b -> BlockInfo.fromBlockState(b.defaultBlockState()))
                    .toArray(BlockInfo[]::new);
            return infos.length > 0 ? infos : new BlockInfo[] { emptyComponentFallback() };
        }).addTooltips(Component.translatable("phoenix.multiblock.pattern.info.multiple_liners"));
    }
}
