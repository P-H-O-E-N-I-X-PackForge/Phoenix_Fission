package net.phoenix.phoenix_fission.gtm8;

import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Starts Phoenix's Fission on GregTech 8. */
public final class Gtm8Bootstrap {

    public static final Logger LOGGER = LogManager.getLogger("phoenix_fission");

    public static GTRegistrate REGISTRATE;

    public Gtm8Bootstrap() {
        REGISTRATE = GTRegistrate.create("phoenix_fission");
        LOGGER.info("Phoenix's Fission is running on GregTech 8");
    }

    @SuppressWarnings("unused")
    private @interface Probe {}

    @SuppressWarnings("unused")
    private static class Smoke {

        @SaveField
        int x;
    }
}
