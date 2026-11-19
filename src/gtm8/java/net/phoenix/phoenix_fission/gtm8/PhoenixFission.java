package net.phoenix.phoenix_fission.gtm8;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;
import net.phoenix.phoenix_fission.gtm8.api.block.PhoenixFissionEntities;
import net.phoenix.phoenix_fission.gtm8.common.PhoenixFissionMachines;
import net.phoenix.phoenix_fission.gtm8.common.data.PhoenixRecipeTypes;
import net.phoenix.phoenix_fission.gtm8.common.data.block.PhoenixFissionBlocks;
import net.phoenix.phoenix_fission.gtm8.common.data.block.PhoenixFissionDefaultComponents;
import net.phoenix.phoenix_fission.gtm8.datagen.PhoenixDatagen;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Phoenix's Fission on GregTech 8. Started by {@code PhoenixFissionMod} when GregTech 8 is installed. */
@SuppressWarnings("all")
public class PhoenixFission {

    public static final String MOD_ID = "phoenix_fission";
    public static final Logger LOGGER = LogManager.getLogger();
    public static GTRegistrate PHOENIX_REGISTRATE = GTRegistrate.create(MOD_ID);
    public static RegistryEntry<CreativeModeTab> PHOENIX_CREATIVE_TAB = PHOENIX_REGISTRATE
            .defaultCreativeTab(PHOENIX_REGISTRATE, MOD_ID,
                    builder -> builder
                            .title(PHOENIX_REGISTRATE.addLang("itemGroup", PhoenixFission.id("creative_tab"),
                                    "Phoenix's Fission"))
                            .icon(() -> PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING.asStack()))
            .register();

    public PhoenixFission() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        PhoenixFissionConfigs.init();

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::clientSetup);
            net.phoenix.phoenix_fission.gtm8.client.FissionColorHandlers.register(modEventBus);
        }

        PhoenixFissionEntities.init();
        PhoenixRecipeTypes.init();
        PhoenixFissionBlocks.init();
        net.phoenix.phoenix_fission.gtm8.common.data.item.PhoenixFissionItems.init();
        net.phoenix.phoenix_fission.gtm8.common.data.block.PhoenixFusionBlocks.init();
        PhoenixFissionDefaultComponents.init();
        PhoenixDatagen.init();
        PhoenixFissionMachines.init();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Phoenix's Fission (GregTech 8) on Minecraft version {}!",
                Minecraft.getInstance().getLaunchedVersion());
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
