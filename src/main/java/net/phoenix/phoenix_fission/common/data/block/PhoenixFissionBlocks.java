package net.phoenix.phoenix_fission.common.data.block;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.MSRCoreLinerBlock;
import net.phoenix.phoenix_fission.api.block.iface.IMSRCoreLinerType;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static net.phoenix.phoenix_fission.PhoenixFission.PHOENIX_REGISTRATE;

@SuppressWarnings("unused")
public class PhoenixFissionBlocks {

    private static boolean isEnabled(boolean flag) {
        return flag || GTCEu.isDataGen();
    }

    private static boolean fissionEnabled() {
        return PhoenixFissionConfigs.INSTANCE != null &&
                isEnabled(PhoenixFissionConfigs.INSTANCE.fission.fissionReactorEnabled);
    }

    private static boolean msrEnabled() {
        return PhoenixFissionConfigs.INSTANCE != null && isEnabled(PhoenixFissionConfigs.INSTANCE.fission.msrEnabled);
    }

    private static boolean breederEnabled() {
        return PhoenixFissionConfigs.INSTANCE != null &&
                isEnabled(PhoenixFissionConfigs.INSTANCE.fission.breederReactorEnabled);
    }

    public static final Map<IMSRCoreLinerType, BlockEntry<MSRCoreLinerBlock>> MSR_LINERS = new HashMap<>();

    public static final BlockEntry<NukeBlock> NUKE_BLOCK = PHOENIX_REGISTRATE
            .block("nuke_block", NukeBlock::new)
            .initialProperties(() -> Blocks.TNT)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().cubeBottomTop(
                            ctx.getName(),
                            PhoenixFission.id("block/nuke_side"),
                            PhoenixFission.id("block/nuke_bottom"),
                            PhoenixFission.id("block/nuke_top"))))
            .item(BlockItem::new)
            .build()
            .register();

    public static BlockEntry<Block> FISSILE_HEAT_SAFE_CASING = null;
    public static BlockEntry<Block> FISSILE_REACTION_SAFE_CASING = null;
    public static BlockEntry<Block> FISSILE_SAFE_GEARBOX_CASING = null;

    public static BlockEntry<Block> EMPTY_REACTOR_COMPONENT = PHOENIX_REGISTRATE
            .block("empty_reactor_component", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .blockstate((ctx, prov) -> {
                var model = prov.models().cubeAll(
                        ctx.getName(),
                        PhoenixFission.id("block/fission/cooler_base"));
                prov.simpleBlock(ctx.getEntry(), model);
            })
            .lang("Empty Reactor Component")
            .item((block, props) -> new TooltipBlockItem(block, props, "tooltip.phoenix.empty_component"))
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName())))
            .build()
            .register();

    public static void init() {
        FISSILE_HEAT_SAFE_CASING = registerSimpleBlock("§bFissile Heat Safe Casing", "fissile_heat_safe_casing",
                "fissile_heat_safe_casing", BlockItem::new);
        FISSILE_REACTION_SAFE_CASING = registerSimpleBlock("§bFissile Reaction Safe Casing",
                "fissile_reaction_safe_casing", "fissile_reaction_safe_casing", BlockItem::new);
        FISSILE_SAFE_GEARBOX_CASING = registerSimpleBlock("§bFissile Safe Gearbox", "fissile_safe_gearbox_casing",
                "fissile_safe_gearbox", BlockItem::new);
    }

    private static @NotNull BlockEntry<Block> registerSimpleBlock(String name, String id, String texture,
                                                                  NonNullBiFunction<Block, Item.Properties, ? extends BlockItem> func) {
        return PHOENIX_REGISTRATE
                .block(id, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(ctx.getName(), PhoenixFission.id("block/" + texture))))
                .lang(name)
                .item(func)
                .build()
                .register();
    }
}
