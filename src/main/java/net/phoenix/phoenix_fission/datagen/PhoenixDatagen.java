package net.phoenix.phoenix_fission.datagen;

import net.phoenix.phoenix_fission.datagen.lang.PhoenixMachineLangHandler;
import net.phoenix.phoenix_fission.datagen.lang.PhoenixUiLangHandler;

import com.tterrag.registrate.providers.ProviderType;

import static net.phoenix.phoenix_fission.PhoenixFission.PHOENIX_REGISTRATE;

public class PhoenixDatagen {

    public static void init() {
        PHOENIX_REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixMachineLangHandler::init);
        PHOENIX_REGISTRATE.addDataGenerator(ProviderType.LANG, PhoenixUiLangHandler::init);
        PHOENIX_REGISTRATE.addDataGenerator(ProviderType.LANG, net.phoenix.phoenix_fission.datagen.lang.PhoenixFusionLangHandler::init);
    }
}
