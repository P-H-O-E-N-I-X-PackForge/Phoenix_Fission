package net.phoenix.phoenix_fission;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.phoenix.phoenix_fission.api.block.PhoenixFissionEntities;
import net.phoenix.phoenix_fission.common.PhoenixFissionMachines;
import net.phoenix.phoenix_fission.common.data.PhoenixRecipeTypes;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionBlocks;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionDefaultComponents;
import net.phoenix.phoenix_fission.common.data.block.PhoenixFissionGameTestFixtures;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;
import net.phoenix.phoenix_fission.datagen.PhoenixDatagen;
import net.phoenix.phoenix_fission.gametest.FissionReactorGameTest;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("all")
@Mod(PhoenixFission.MOD_ID)
public class PhoenixFission {

    public static final String MOD_ID = "phoenix_fission";
    public static final Logger LOGGER = LogManager.getLogger();
    public static GTRegistrate PHOENIX_REGISTRATE = GTRegistrate.create(MOD_ID);
    public static RegistryEntry<CreativeModeTab, CreativeModeTab> PHOENIX_CREATIVE_TAB = PHOENIX_REGISTRATE
            .defaultCreativeTab(PhoenixFission.MOD_ID,
                    builder -> builder
                            .title(PHOENIX_REGISTRATE.addLang("itemGroup", PhoenixFission.id("creative_tab"),
                                    "Phoenix's Fission"))
                            .icon(() -> PhoenixFissionBlocks.FISSILE_REACTION_SAFE_CASING.asStack())
                            .build())
            .register();

    public PhoenixFission(IEventBus modEventBus, ModContainer container) {
        PhoenixFissionConfigs.init();
        PHOENIX_REGISTRATE.registerEventListeners(modEventBus);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::clientSetup);
            net.phoenix.phoenix_fission.client.FissionColorHandlers.register(modEventBus);
        }
        modEventBus.addListener(this::registerGameTests);

        PhoenixFissionEntities.init();
        PhoenixRecipeTypes.init();
        PhoenixFissionBlocks.init();
        PhoenixFissionDefaultComponents.init();
        PhoenixFissionGameTestFixtures.init();
        PhoenixDatagen.init();
        PhoenixFissionMachines.init();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Hey, we're on Minecraft version {}!", Minecraft.getInstance().getLaunchedVersion());
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(FissionReactorGameTest.class);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
