package net.phoenix.phoenix_fission.common.data;

import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.data.recipes.RecipeOutput;

import static com.gregtechceu.gtceu.api.GTValues.*;

public final class TokamakRecipes {

    private TokamakRecipes() {}

    public static void init(RecipeOutput provider) {
        var FUSION = PhoenixRecipeTypes.FUSION_REACTOR_RECIPES;

        FUSION.recipeBuilder("tokamak_deuterium_and_tritium_to_helium_plasma")
                .inputFluids(GTMaterials.Deuterium.getFluid(125))
                .inputFluids(GTMaterials.Tritium.getFluid(125))
                .outputFluids(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA, 125))
                .duration(16)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        FUSION.recipeBuilder("tokamak_carbon_and_helium_3_to_oxygen_plasma")
                .inputFluids(GTMaterials.Carbon.getFluid(16))
                .inputFluids(GTMaterials.Helium3.getFluid(125))
                .outputFluids(GTMaterials.Oxygen.getFluid(FluidStorageKeys.PLASMA, 125))
                .duration(32)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        FUSION.recipeBuilder("tokamak_beryllium_and_deuterium_to_nitrogen_plasma")
                .inputFluids(GTMaterials.Beryllium.getFluid(16))
                .inputFluids(GTMaterials.Deuterium.getFluid(375))
                .outputFluids(GTMaterials.Nitrogen.getFluid(FluidStorageKeys.PLASMA, 125))
                .duration(16)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        FUSION.recipeBuilder("tokamak_silicon_and_magnesium_to_iron_plasma")
                .inputFluids(GTMaterials.Silicon.getFluid(16))
                .inputFluids(GTMaterials.Magnesium.getFluid(16))
                .outputFluids(GTMaterials.Iron.getFluid(FluidStorageKeys.PLASMA, 16))
                .duration(32)
                .EUt(VA[UV], 16)
                .addData("fusion_tier", 3)
                .save(provider);

        FUSION.recipeBuilder("tokamak_potassium_and_fluorine_to_nickel_plasma")
                .inputFluids(GTMaterials.Potassium.getFluid(16))
                .inputFluids(GTMaterials.Fluorine.getFluid(125))
                .outputFluids(GTMaterials.Nickel.getFluid(FluidStorageKeys.PLASMA, 16))
                .duration(16)
                .EUt(VA[UV], 16)
                .addData("fusion_tier", 3)
                .save(provider);

        FUSION.recipeBuilder("tokamak_carbon_and_magnesium_to_argon_plasma")
                .inputFluids(GTMaterials.Carbon.getFluid(16))
                .inputFluids(GTMaterials.Magnesium.getFluid(16))
                .outputFluids(GTMaterials.Argon.getFluid(FluidStorageKeys.PLASMA, 125))
                .duration(32)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        FUSION.recipeBuilder("tokamak_neodymium_and_hydrogen_to_europium_plasma")
                .inputFluids(GTMaterials.Neodymium.getFluid(16))
                .inputFluids(GTMaterials.Hydrogen.getFluid(375))
                .outputFluids(GTMaterials.Europium.getFluid(16))
                .duration(64)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        FUSION.recipeBuilder("tokamak_lutenium_and_chromium_to_americium_plasma")
                .inputFluids(GTMaterials.Lutetium.getFluid(16))
                .inputFluids(GTMaterials.Chromium.getFluid(16))
                .outputFluids(GTMaterials.Americium.getFluid(16))
                .duration(64)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        FUSION.recipeBuilder("tokamak_americium_and_naquadria_to_neutronium_plasma")
                .inputFluids(GTMaterials.Americium.getFluid(128))
                .inputFluids(GTMaterials.Naquadria.getFluid(128))
                .outputFluids(GTMaterials.Neutronium.getFluid(32))
                .duration(200)
                .EUt(VA[UV], 16)
                .addData("fusion_tier", 3)
                .save(provider);

        FUSION.recipeBuilder("tokamak_silver_and_copper_to_osmium_plasma")
                .inputFluids(GTMaterials.Silver.getFluid(16))
                .inputFluids(GTMaterials.Copper.getFluid(16))
                .outputFluids(GTMaterials.Osmium.getFluid(16))
                .duration(64)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        FUSION.recipeBuilder("tokamak_mercury_and_magnesium_to_uranium_235_plasma")
                .inputFluids(GTMaterials.Mercury.getFluid(125))
                .inputFluids(GTMaterials.Magnesium.getFluid(16))
                .outputFluids(GTMaterials.Uranium235.getFluid(16))
                .duration(128)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        FUSION.recipeBuilder("tokamak_gold_and_aluminium_to_uranium_238_plasma")
                .inputFluids(GTMaterials.Gold.getFluid(16))
                .inputFluids(GTMaterials.Aluminium.getFluid(16))
                .outputFluids(GTMaterials.Uranium238.getFluid(16))
                .duration(128)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        FUSION.recipeBuilder("tokamak_xenon_and_zinc_to_plutonium_239_plasma")
                .inputFluids(GTMaterials.Xenon.getFluid(125))
                .inputFluids(GTMaterials.Zinc.getFluid(16))
                .outputFluids(GTMaterials.Plutonium239.getFluid(16))
                .duration(128)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        FUSION.recipeBuilder("tokamak_krypton_and_cerium_to_plutonium_241_plasma")
                .inputFluids(GTMaterials.Krypton.getFluid(125))
                .inputFluids(GTMaterials.Cerium.getFluid(16))
                .outputFluids(GTMaterials.Plutonium241.getFluid(16))
                .duration(128)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        FUSION.recipeBuilder("tokamak_hydrogen_and_vanadium_to_chromium_plasma")
                .inputFluids(GTMaterials.Hydrogen.getFluid(125))
                .inputFluids(GTMaterials.Vanadium.getFluid(16))
                .outputFluids(GTMaterials.Chromium.getFluid(16))
                .duration(64)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        FUSION.recipeBuilder("tokamak_gallium_and_radon_to_duranium_plasma")
                .inputFluids(GTMaterials.Gallium.getFluid(16))
                .inputFluids(GTMaterials.Radon.getFluid(125))
                .outputFluids(GTMaterials.Duranium.getFluid(16))
                .duration(32)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        FUSION.recipeBuilder("tokamak_titanium_and_duranium_to_tritanium_plasma")
                .inputFluids(GTMaterials.Titanium.getFluid(48))
                .inputFluids(GTMaterials.Duranium.getFluid(32))
                .outputFluids(GTMaterials.Tritanium.getFluid(16))
                .duration(16)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        FUSION.recipeBuilder("tokamak_gold_and_mercury_to_radon_plasma")
                .inputFluids(GTMaterials.Gold.getFluid(16))
                .inputFluids(GTMaterials.Mercury.getFluid(16))
                .outputFluids(GTMaterials.Radon.getFluid(125))
                .duration(64)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        FUSION.recipeBuilder("tokamak_silver_and_lithium_to_indium_plasma")
                .inputFluids(GTMaterials.Silver.getFluid(L))
                .inputFluids(GTMaterials.Lithium.getFluid(L))
                .outputFluids(GTMaterials.Indium.getFluid(L))
                .duration(16)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        FUSION.recipeBuilder("tokamak_enriched_naquadah_and_radon_to_naquadria_plasma")
                .inputFluids(GTMaterials.NaquadahEnriched.getFluid(16))
                .inputFluids(GTMaterials.Radon.getFluid(125))
                .outputFluids(GTMaterials.Naquadria.getFluid(4))
                .duration(64)
                .EUt(VA[UV], 16)
                .addData("fusion_tier", 3)
                .save(provider);

        FUSION.recipeBuilder("tokamak_lantanum_and_silicon_to_lutetium_plasma")
                .inputFluids(GTMaterials.Lanthanum.getFluid(16))
                .inputFluids(GTMaterials.Silicon.getFluid(16))
                .outputFluids(GTMaterials.Lutetium.getFluid(16))
                .duration(16)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        FUSION.recipeBuilder("tokamak_arsenic_and_ruthenium_to_darmstadtium_plasma")
                .inputFluids(GTMaterials.Arsenic.getFluid(32))
                .inputFluids(GTMaterials.Ruthenium.getFluid(16))
                .outputFluids(GTMaterials.Darmstadtium.getFluid(16))
                .duration(32)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        FUSION.recipeBuilder("tokamak_plutonium_241_and_hydrogen_gas_to_americium_plasma")
                .inputFluids(GTMaterials.Plutonium241.getFluid(144))
                .inputFluids(GTMaterials.Hydrogen.getFluid(FluidStorageKeys.GAS, 2000))
                .outputFluids(GTMaterials.Americium.getFluid(FluidStorageKeys.PLASMA, 144))
                .duration(64)
                .EUt(VA[UV], 16)
                .addData("fusion_tier", 3)
                .save(provider);

        FUSION.recipeBuilder("tokamak_silver_and_helium_3_to_tin_plasma")
                .inputFluids(GTMaterials.Silver.getFluid(144))
                .inputFluids(GTMaterials.Helium3.getFluid(375))
                .outputFluids(GTMaterials.Tin.getFluid(FluidStorageKeys.PLASMA, 144))
                .duration(16)
                .EUt(VA[ZPM], 16)
                .addData("fusion_tier", 2)
                .save(provider);

        prettyRecipes(provider);
    }

    /**
     * Fusion recipes that do nothing useful and only make the plasma pretty. They burn liquid dye and give back a
     * drop of water, so there is nothing to gain from them; the Tokamak draws its plasma in their colours.
     */
    private static void prettyRecipes(RecipeOutput provider) {
        var FUSION = PhoenixRecipeTypes.FUSION_REACTOR_RECIPES;

        // Miku: teal and white, with a little pink
        FUSION.recipeBuilder("tokamak_pretty_miku")
                .inputFluids(GTMaterials.DyeCyan.getFluid(250))
                .inputFluids(GTMaterials.DyeWhite.getFluid(125))
                .inputFluids(GTMaterials.DyePink.getFluid(25))
                .outputFluids(GTMaterials.DistilledWater.getFluid(1))
                .duration(200)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        // Teto: red, pink and a drop of black
        FUSION.recipeBuilder("tokamak_pretty_teto")
                .inputFluids(GTMaterials.DyeRed.getFluid(250))
                .inputFluids(GTMaterials.DyePink.getFluid(125))
                .inputFluids(GTMaterials.DyeBlack.getFluid(25))
                .outputFluids(GTMaterials.DistilledWater.getFluid(1))
                .duration(200)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);

        // the Phoenix Forge Technologies logo: purple into magenta, with a little white
        FUSION.recipeBuilder("tokamak_pretty_pft")
                .inputFluids(GTMaterials.DyePurple.getFluid(250))
                .inputFluids(GTMaterials.DyeMagenta.getFluid(125))
                .inputFluids(GTMaterials.DyeWhite.getFluid(25))
                .outputFluids(GTMaterials.DistilledWater.getFluid(1))
                .duration(200)
                .EUt(VA[LuV], 16)
                .addData("fusion_tier", 1)
                .save(provider);
    }
}
