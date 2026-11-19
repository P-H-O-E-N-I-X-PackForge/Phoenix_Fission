package net.phoenix.phoenix_fission.gtm8.api.block;

import net.minecraft.world.entity.MobCategory;
import net.phoenix.phoenix_fission.gtm8.client.NukePrimedRenderer;
import net.phoenix.phoenix_fission.gtm8.common.data.block.entity.NukePrimedEntity;

import com.tterrag.registrate.util.entry.EntityEntry;

import static net.phoenix.phoenix_fission.gtm8.PhoenixFission.PHOENIX_REGISTRATE;

public class PhoenixFissionEntities {

    public static void init() {}

    public static final EntityEntry<NukePrimedEntity> NUKE_PRIMED = PHOENIX_REGISTRATE
            .entity("nuke_primed", NukePrimedEntity::new, MobCategory.MISC)
            .properties(b -> b.sized(0.98f, 0.98f).clientTrackingRange(10).updateInterval(10))
            .renderer(() -> NukePrimedRenderer::new)
            .register();
}
