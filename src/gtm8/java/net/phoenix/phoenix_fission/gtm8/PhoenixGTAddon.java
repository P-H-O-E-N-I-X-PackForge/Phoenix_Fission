package net.phoenix.phoenix_fission.gtm8;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

@SuppressWarnings("unused")
@GTAddon
public class PhoenixGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        // on GregTech 7 this class is still scanned, but must not touch the GregTech 8 half of the mod
        return GTRegistrate.create(PhoenixFission.MOD_ID);
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return PhoenixFission.MOD_ID;
    }

    @Override
    public void addRecipes(java.util.function.Consumer<net.minecraft.data.recipes.FinishedRecipe> provider) {
        if (!net.phoenix.phoenix_fission.PhoenixFissionMod.isGregTech8()) return;
        if (!net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs.INSTANCE.fission.fusion.defaultRecipesEnabled)
            return;
        net.phoenix.phoenix_fission.gtm8.common.data.TokamakRecipes.init(provider);
    }
}
