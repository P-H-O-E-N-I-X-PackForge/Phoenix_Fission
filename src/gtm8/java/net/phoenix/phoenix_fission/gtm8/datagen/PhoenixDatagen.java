package net.phoenix.phoenix_fission.gtm8.datagen;

import net.phoenix.phoenix_fission.gtm8.datagen.lang.PhoenixMachineLangHandler;
import net.phoenix.phoenix_fission.gtm8.datagen.lang.PhoenixUiLangHandler;

import com.tterrag.registrate.providers.ProviderType;

import static net.phoenix.phoenix_fission.gtm8.PhoenixFission.PHOENIX_REGISTRATE;

public class PhoenixDatagen {

    public static void init() {
        PHOENIX_REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixMachineLangHandler::init);
        PHOENIX_REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixUiLangHandler::init);
    }
}
