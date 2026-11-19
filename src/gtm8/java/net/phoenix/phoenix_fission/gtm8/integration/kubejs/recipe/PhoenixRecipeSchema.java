package net.phoenix.phoenix_fission.gtm8.integration.kubejs.recipe;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import lombok.experimental.Accessors;

import static com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema.*;

public interface PhoenixRecipeSchema {

    @SuppressWarnings({ "unused", "UnusedReturnValue" })
    @Accessors(chain = true, fluent = true)
    class PhoenixRecipeJS extends GTRecipeSchema.GTRecipeJS {

    }

    RecipeSchema SCHEMA = new RecipeSchema(PhoenixRecipeJS.class, PhoenixRecipeJS::new,
            DURATION, DATA, CONDITIONS,
            ALL_INPUTS, ALL_TICK_INPUTS, ALL_OUTPUTS, ALL_TICK_OUTPUTS,
            INPUT_CHANCE_LOGICS, OUTPUT_CHANCE_LOGICS, TICK_INPUT_CHANCE_LOGICS, TICK_OUTPUT_CHANCE_LOGICS, CATEGORY)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(from.getValue(recipe, ID)), ID)
            .constructor(DURATION, CONDITIONS, ALL_INPUTS, ALL_OUTPUTS, ALL_TICK_INPUTS, ALL_TICK_OUTPUTS);
}
