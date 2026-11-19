package net.phoenix.phoenix_fission.common.data.block;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.PhoenixAPI;
import net.phoenix.phoenix_fission.api.block.FusionCoilBlock;
import net.phoenix.phoenix_fission.api.block.iface.IFusionCoilType;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import com.tterrag.registrate.util.entry.BlockEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.phoenix.phoenix_fission.PhoenixFission.PHOENIX_REGISTRATE;

/**
 * The default fusion coils. The inner plasma coils are the only ones the Tokamak needs; the outer confinement coils
 * are GT heating coils, so the custom outer blocks here are optional extras. Textures live under
 * textures/block/fusion.
 */
public class PhoenixFusionBlocks {

    public record CoilTier(String name, String display, boolean inner, int heat, int confinement, long upkeep,
                           int tint, String coolant, String hot, int coolantPerSecond) {}

    public static final List<CoilTier> COIL_TIERS = List.of(
            new CoilTier("fusion_inner_coil_t1", "Tier 1 Inner Plasma Coil", true, 1000, 0, 0, 0xFFE0703A,
                    "gtceu:nitrogen", "gtceu:nitrogen", 4),
            new CoilTier("fusion_inner_coil_t2", "Tier 2 Inner Plasma Coil", true, 3000, 0, 0, 0xFFF0A030,
                    "gtceu:nitrogen", "gtceu:nitrogen", 3),
            new CoilTier("fusion_inner_coil_t3", "Tier 3 Inner Plasma Coil", true, 9000, 0, 0, 0xFFFFE066,
                    "gtceu:helium", "gtceu:helium", 2),
            new CoilTier("fusion_outer_coil_t1", "Tier 1 Outer Confinement Coil", false, 0, 60, 100, 0xFF4A90D9,
                    "gtceu:nitrogen", "gtceu:nitrogen", 1),
            new CoilTier("fusion_outer_coil_t2", "Tier 2 Outer Confinement Coil", false, 0, 180, 400, 0xFF50C8E8,
                    "gtceu:nitrogen", "gtceu:nitrogen", 1),
            new CoilTier("fusion_outer_coil_t3", "Tier 3 Outer Confinement Coil", false, 0, 540, 1600, 0xFFA070F0,
                    "gtceu:helium", "gtceu:helium", 1));

    public static final List<BlockEntry<FusionCoilBlock>> COILS = new ArrayList<>();

    public static boolean enabled() {
        var cfg = PhoenixFissionConfigs.INSTANCE;
        return GTCEu.isDataGen() || (cfg != null && cfg.fission.fusion.defaultComponentsEnabled);
    }

    public static void init() {
        if (!enabled()) return;
        for (CoilTier t : COIL_TIERS) {
            IFusionCoilType type = new DefaultCoil(t);
            BlockEntry<FusionCoilBlock> entry = PHOENIX_REGISTRATE
                    .block(t.name(), p -> (FusionCoilBlock) (t.inner() ? new FusionCoilBlock.Inner(p, type) :
                            new FusionCoilBlock.Outer(p, type)))
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false))
                    .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                            prov.models().cubeAll(ctx.getName(), PhoenixFission.id("block/fusion/" + t.name()))))
                    .lang(t.display())
                    .item(BlockItem::new)
                    .build()
                    .register();
            if (t.inner()) {
                PhoenixAPI.FUSION_INNER_COILS.put(type, () -> (FusionCoilBlock.Inner) entry.get());
            } else {
                PhoenixAPI.FUSION_OUTER_COILS.put(type, () -> (FusionCoilBlock.Outer) entry.get());
            }
            COILS.add(entry);
        }
    }

    private record DefaultCoil(CoilTier t) implements IFusionCoilType {

        @Override
        public @NotNull String getName() {
            return t.name();
        }

        @Override
        public int getTintColor() {
            return t.tint();
        }

        @Override
        public boolean isInner() {
            return t.inner();
        }

        @Override
        public int getPlasmaHeat() {
            return t.heat();
        }

        @Override
        public int getConfinement() {
            return t.confinement();
        }

        @Override
        public long getUpkeepEut() {
            return t.upkeep();
        }

        @Override
        public @NotNull String getCoolantFluidId() {
            return t.coolant();
        }

        @Override
        public @NotNull String getHotCoolantFluidId() {
            return t.hot();
        }

        @Override
        public int getCoolantPerSecond() {
            return t.coolantPerSecond();
        }
    }
}
