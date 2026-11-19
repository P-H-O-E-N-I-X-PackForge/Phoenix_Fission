package net.phoenix.phoenix_fission.gtm8;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Starts Phoenix's Fission on GregTech 8. */
public final class Gtm8Bootstrap {

    public static final Logger LOGGER = LogManager.getLogger("phoenix_fission");

    @SuppressWarnings("unused")
    private final PhoenixFission mod;

    public Gtm8Bootstrap() {
        LOGGER.info("Phoenix's Fission is running on GregTech 8");
        mod = new PhoenixFission();
    }
}
