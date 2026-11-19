package net.phoenix.phoenix_fission.gtm8.common.data.item;

import net.minecraft.world.item.Item;
import net.phoenix.phoenix_fission.gtm8.api.item.RtgCellItem;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import java.util.ArrayList;
import java.util.List;

import static net.phoenix.phoenix_fission.gtm8.PhoenixFission.PHOENIX_REGISTRATE;

public class PhoenixFissionItems {

    public record RtgTier(String id, String name, long euPerTick, int lifespanSeconds) {}

    public static final List<RtgTier> RTG_TIERS = List.of(
            new RtgTier("rtg_cell_sr90", "Strontium-90 RTG Cell", 8, 6 * 3600),
            new RtgTier("rtg_cell_am241", "Americium-241 RTG Cell", 32, 12 * 3600),
            new RtgTier("rtg_cell_pu238", "Plutonium-238 RTG Cell", 128, 24 * 3600),
            new RtgTier("rtg_cell_cm244", "Curium-244 RTG Cell", 512, 48 * 3600));

    public static final List<ItemEntry<RtgCellItem>> RTG_CELLS = new ArrayList<>();

    public static ItemEntry<Item> FUSION_WASTE;

    public static void init() {
        FUSION_WASTE = PHOENIX_REGISTRATE.item("fusion_waste", Item::new)
                .lang("Activated Fusion Waste")
                .model(NonNullBiConsumer.noop())
                .register();

        var rtgCfg = PhoenixFissionConfigs.INSTANCE.fission.rtg;
        if (!rtgCfg.defaultCellsEnabled && !com.gregtechceu.gtceu.GTCEu.isDataGen()) return;
        double lifespanMul = Math.max(0.01, PhoenixFissionConfigs.INSTANCE.fission.rtg.lifespanMultiplier);
        for (RtgTier tier : RTG_TIERS) {
            int lifespan = (int) Math.min(Integer.MAX_VALUE - 1, Math.max(1, tier.lifespanSeconds() * lifespanMul));
            RTG_CELLS.add(PHOENIX_REGISTRATE
                    .item(tier.id(), props -> new RtgCellItem(props, tier.euPerTick(), lifespan))
                    .lang(tier.name())

                    .model(NonNullBiConsumer.noop())
                    .register());
        }
    }
}
