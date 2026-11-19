package net.phoenix.phoenix_fission.gtm8.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.phoenix.phoenix_fission.gtm8.PhoenixFission;
import net.phoenix.phoenix_fission.gtm8.api.gui.PhoenixGuiTextures;

import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.widgets.ProgressWidget;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MULTIBLOCK;

public class PhoenixRecipeTypes {

    public static GTRecipeType HIGH_PERFORMANCE_BREEDER_REACTOR_RECIPES;
    public static GTRecipeType ADVANCED_PRESSURIZED_FISSION_REACTOR_RECIPES;
    public static GTRecipeType PRESSURIZED_FISSION_REACTOR_RECIPES;
    public static GTRecipeType THERMOELECTRIC_GENERATOR_RECIPES;

    public static GTRecipeType RTG_ARRAY_RECIPES;

    public static GTRecipeType FUSION_REACTOR_RECIPES;

    public static void init() {
        HIGH_PERFORMANCE_BREEDER_REACTOR_RECIPES = GTRecipeTypes.register(PhoenixFission.id("high_performance_breeder_reactor"), MULTIBLOCK)
                .setMaxIOSize(2, 2, 2, 2)
                .setEUIO(IO.OUT)
                .UI(builder -> builder.setProgressBarSupplier((layout, value, machine) -> new ProgressWidget()
                        .value(value)
                        .name("progressBar")
                        .texture(PhoenixGuiTextures.PROGRESS_BAR_FISSION, ProgressDrawable.Direction.RIGHT)
                        .size(20)))
                .setSound(GTSoundEntries.CHEMICAL);

        PRESSURIZED_FISSION_REACTOR_RECIPES = GTRecipeTypes.register(PhoenixFission.id("pressurized_fission_reactor"), MULTIBLOCK)
                .setMaxIOSize(1, 1, 0, 0)
                .setEUIO(IO.OUT)
                .UI(builder -> builder.setProgressBarSupplier((layout, value, machine) -> new ProgressWidget()
                        .value(value)
                        .name("progressBar")
                        .texture(PhoenixGuiTextures.PROGRESS_BAR_FISSION, ProgressDrawable.Direction.RIGHT)
                        .size(20)))
                .setSound(GTSoundEntries.CHEMICAL);

        ADVANCED_PRESSURIZED_FISSION_REACTOR_RECIPES = GTRecipeTypes.register(PhoenixFission.id("advanced_pressurized_fission_reactor"), MULTIBLOCK)
                .setMaxIOSize(1, 1, 1, 1)
                .UI(builder -> builder.setProgressBarSupplier((layout, value, machine) -> new ProgressWidget()
                        .value(value)
                        .name("progressBar")
                        .texture(PhoenixGuiTextures.PROGRESS_BAR_FISSION, ProgressDrawable.Direction.RIGHT)
                        .size(20)))
                .setSound(GTSoundEntries.CHEMICAL);

        THERMOELECTRIC_GENERATOR_RECIPES = GTRecipeTypes.register(PhoenixFission.id("thermoelectric_generating"), MULTIBLOCK)
                .setMaxIOSize(1, 0, 1, 1)
                .setEUIO(IO.OUT)
                .UI(builder -> builder.setProgressBarSupplier((layout, value, machine) -> new ProgressWidget()
                        .value(value)
                        .name("progressBar")
                        .texture(PhoenixGuiTextures.PROGRESS_BAR_FISSION, ProgressDrawable.Direction.RIGHT)
                        .size(20)))
                .setSound(GTSoundEntries.MIXER);

        RTG_ARRAY_RECIPES = GTRecipeTypes.register(PhoenixFission.id("rtg_array"), MULTIBLOCK)
                .setMaxIOSize(0, 0, 0, 0)
                .setEUIO(IO.OUT);

        FUSION_REACTOR_RECIPES = GTRecipeTypes.register(PhoenixFission.id("fusion_reactor"), MULTIBLOCK)
                .setMaxIOSize(0, 0, 3, 3)
                .setEUIO(IO.IN);
    }
}
