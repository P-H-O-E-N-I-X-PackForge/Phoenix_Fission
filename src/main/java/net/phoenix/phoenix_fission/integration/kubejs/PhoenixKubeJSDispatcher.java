package net.phoenix.phoenix_fission.integration.kubejs;

import net.phoenix.phoenix_fission.PhoenixFissionMod;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;

/**
 * The only KubeJS plugin the mod lists. KubeJS loads every listed plugin, and the GregTech 7 and GregTech 8 plugins
 * reference classes the other GregTech does not have, so this hands over to whichever one matches.
 */
public class PhoenixKubeJSDispatcher extends KubeJSPlugin {

    private static final String GT7 = "net.phoenix.phoenix_fission.integration.kubejs.PhoenixKubeJSPlugin";
    private static final String GT8 = "net.phoenix.phoenix_fission.gtm8.integration.kubejs.PhoenixKubeJSPlugin";

    private final KubeJSPlugin delegate;

    public PhoenixKubeJSDispatcher() {
        try {
            delegate = (KubeJSPlugin) Class.forName(PhoenixFissionMod.isGregTech8() ? GT8 : GT7)
                    .getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not load the Phoenix's Fission KubeJS plugin", e);
        }
    }

    @Override
    public void init() {
        delegate.init();
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        delegate.registerClasses(type, filter);
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        delegate.registerRecipeSchemas(event);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        delegate.registerBindings(event);
    }
}
