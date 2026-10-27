package net.phoenix.phoenix_fission.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import net.phoenix.phoenix_fission.api.gui.PhoenixGuiTextures;

import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.widgets.ProgressWidget;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MULTIBLOCK;
import static net.phoenix.phoenix_fission.PhoenixFission.PHOENIX_REGISTRATE;

public class PhoenixRecipeTypes {

    public static GTRecipeTypeEntry HIGH_PERFORMANCE_BREEDER_REACTOR_RECIPES;
    public static GTRecipeTypeEntry ADVANCED_PRESSURIZED_FISSION_REACTOR_RECIPES;
    public static GTRecipeTypeEntry PRESSURIZED_FISSION_REACTOR_RECIPES;
    public static GTRecipeTypeEntry THERMOELECTRIC_GENERATOR_RECIPES;

    public static void init() {
        HIGH_PERFORMANCE_BREEDER_REACTOR_RECIPES = PHOENIX_REGISTRATE
                .recipeType("high_performance_breeder_reactor", MULTIBLOCK)
                .setMaxIOSize(2, 2, 2, 2)
                .setEUIO(IO.OUT)
                .UI(builder -> builder.setProgressBarSupplier((layout, value, machine) -> new ProgressWidget()
                        .value(value)
                        .name("progressBar")
                        .texture(PhoenixGuiTextures.PROGRESS_BAR_FISSION, ProgressDrawable.Direction.RIGHT)
                        .size(20)))
                .setSound(GTSoundEntries.CHEMICAL)
                .register();

        PRESSURIZED_FISSION_REACTOR_RECIPES = PHOENIX_REGISTRATE
                .recipeType("pressurized_fission_reactor", MULTIBLOCK)
                .setMaxIOSize(1, 1, 0, 0)
                .setEUIO(IO.OUT)
                .UI(builder -> builder.setProgressBarSupplier((layout, value, machine) -> new ProgressWidget()
                        .value(value)
                        .name("progressBar")
                        .texture(PhoenixGuiTextures.PROGRESS_BAR_FISSION, ProgressDrawable.Direction.RIGHT)
                        .size(20)))
                .setSound(GTSoundEntries.CHEMICAL)
                .register();

        ADVANCED_PRESSURIZED_FISSION_REACTOR_RECIPES = PHOENIX_REGISTRATE
                .recipeType("advanced_pressurized_fission_reactor", MULTIBLOCK)
                .setMaxIOSize(1, 1, 1, 1)
                .UI(builder -> builder.setProgressBarSupplier((layout, value, machine) -> new ProgressWidget()
                        .value(value)
                        .name("progressBar")
                        .texture(PhoenixGuiTextures.PROGRESS_BAR_FISSION, ProgressDrawable.Direction.RIGHT)
                        .size(20)))
                .setSound(GTSoundEntries.CHEMICAL)
                .register();

        THERMOELECTRIC_GENERATOR_RECIPES = PHOENIX_REGISTRATE
                .recipeType("thermoelectric_generating", MULTIBLOCK)
                .setMaxIOSize(1, 0, 1, 1)
                .setEUIO(IO.OUT)
                .UI(builder -> builder.setProgressBarSupplier((layout, value, machine) -> new ProgressWidget()
                        .value(value)
                        .name("progressBar")
                        .texture(PhoenixGuiTextures.PROGRESS_BAR_FISSION, ProgressDrawable.Direction.RIGHT)
                        .size(20)))
                .setSound(GTSoundEntries.MIXER)
                .register();
    }
}
