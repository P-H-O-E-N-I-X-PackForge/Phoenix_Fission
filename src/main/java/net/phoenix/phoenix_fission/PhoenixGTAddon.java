package net.phoenix.phoenix_fission;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;
import net.phoenix.phoenix_fission.common.data.TokamakRecipes;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import java.util.function.Consumer;

@SuppressWarnings("unused")
@GTAddon
public class PhoenixGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        // on GregTech 8 this class is still scanned, but must not touch the GregTech 7 half of the mod
        return net.phoenix.phoenix_fission.PhoenixFissionMod.isGregTech8() ? GTRegistrate.create(PhoenixFission.MOD_ID) :
                PhoenixFission.PHOENIX_REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return PhoenixFission.MOD_ID;
    }

    @Override
    public void registerElements() {
        IGTAddon.super.registerElements();
    }

    /** The default Tokamak recipes are GT's fusion recipes, copied (see TokamakRecipes). */
    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        if (net.phoenix.phoenix_fission.PhoenixFissionMod.isGregTech8()) return;
        if (!PhoenixFissionConfigs.INSTANCE.fission.fusion.defaultRecipesEnabled) return;
        TokamakRecipes.init(provider);
    }
}
