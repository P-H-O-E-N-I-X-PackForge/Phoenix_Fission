package net.phoenix.phoenix_fission.common.data.item;

import net.phoenix.phoenix_fission.api.item.RtgCellItem;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.util.ArrayList;
import java.util.List;

import static net.phoenix.phoenix_fission.PhoenixFission.PHOENIX_REGISTRATE;

public class PhoenixFissionItems {

    public record RtgTier(String id, String name, long euPerTick, int lifespanSeconds) {}

    /** Default cells, weakest to strongest. They are meant to come out of fission by-products. */
    public static final List<RtgTier> RTG_TIERS = List.of(
            new RtgTier("rtg_cell_sr90", "Strontium-90 RTG Cell", 8, 6 * 3600),
            new RtgTier("rtg_cell_am241", "Americium-241 RTG Cell", 32, 12 * 3600),
            new RtgTier("rtg_cell_pu238", "Plutonium-238 RTG Cell", 128, 24 * 3600),
            new RtgTier("rtg_cell_cm244", "Curium-244 RTG Cell", 512, 48 * 3600));

    public static final List<ItemEntry<RtgCellItem>> RTG_CELLS = new ArrayList<>();

    public static void init() {
        double lifespanMul = Math.max(0.01, PhoenixFissionConfigs.INSTANCE.fission.rtg.lifespanMultiplier);
        for (RtgTier tier : RTG_TIERS) {
            int lifespan = (int) Math.min(Integer.MAX_VALUE - 1, Math.max(1, tier.lifespanSeconds() * lifespanMul));
            RTG_CELLS.add(PHOENIX_REGISTRATE
                    .item(tier.id(), props -> new RtgCellItem(props, tier.euPerTick(), lifespan))
                    .lang(tier.name())
                    // The model json and texture are plain resources (item/generated parent).
                    .model(NonNullBiConsumer.noop())
                    .register());
        }
    }
}
