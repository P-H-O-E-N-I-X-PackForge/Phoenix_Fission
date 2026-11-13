package net.phoenix.phoenix_fission.client;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;

public final class FissionText {

    private FissionText() {}

    public static String str(String key, Object... args) {
        return I18n.get(key, args);
    }

    public static String label(String key) {
        return Language.getInstance().getOrDefault(key);
    }
}
