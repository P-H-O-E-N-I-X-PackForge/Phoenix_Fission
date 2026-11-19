package net.phoenix.phoenix_fission;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * The mod entry point. GregTech 7 and GregTech 8 have different APIs, so the mod ships both implementations in one
 * jar and starts the one that matches the installed GregTech. {@link PhoenixFission} is the GregTech 7 one; the
 * GregTech 8 one lives under {@code net.phoenix.phoenix_fission.gtm8}. Nothing here touches either set until one has
 * been chosen, so the other is never loaded.
 */
@Mod(PhoenixFissionMod.MOD_ID)
public class PhoenixFissionMod {

    public static final String MOD_ID = "phoenix_fission";

    private static final String GTM8_BOOTSTRAP = "net.phoenix.phoenix_fission.gtm8.Gtm8Bootstrap";

    public PhoenixFissionMod() {
        if (isGregTech8()) {
            try {
                Class.forName(GTM8_BOOTSTRAP).getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not start the GregTech 8 implementation of Phoenix's Fission",
                        e);
            }
        } else {
            new PhoenixFission();
        }
    }

    /** GregTech 8 and later use the new machine, UI and sync APIs. */
    public static boolean isGregTech8() {
        return ModList.get().getModContainerById("gtceu")
                .map(c -> c.getModInfo().getVersion().getMajorVersion() >= 8).orElse(false);
    }
}
