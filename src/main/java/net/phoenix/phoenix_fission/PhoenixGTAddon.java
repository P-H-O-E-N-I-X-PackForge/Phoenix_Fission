package net.phoenix.phoenix_fission;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.RecipeOutput;

@SuppressWarnings("unused")
@GTAddon(PhoenixFission.MOD_ID)
public class PhoenixGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return PhoenixFission.PHOENIX_REGISTRATE;
    }

    @Override
    public void addRecipes(RecipeOutput provider) {
        if (!net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs.INSTANCE.fission.fusion.defaultRecipesEnabled)
            return;
        net.phoenix.phoenix_fission.common.data.TokamakRecipes.init(provider);
    }
}
