package net.phoenix.phoenix_fission.gtm8;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
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

    private static boolean dynamicRendersRegistered;

    /**
     * The machine models name their dynamic renders, and are read before the client setup event, so the renders have
     * to be known by then. Datagen needs them too: whichever comes first registers them, as GregTech throws on a
     * second registration. Only call this on the client or in datagen.
     */
    public static synchronized void registerDynamicRenders() {
        if (dynamicRendersRegistered) return;
        dynamicRendersRegistered = true;
        com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager.register(id("ascension_reactor"),
                net.phoenix.phoenix_fission.gtm8.client.render.AscensionReactorRender.TYPE);
        com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager.register(id("tokamak"),
                net.phoenix.phoenix_fission.gtm8.client.render.TokamakRender.TYPE);
    }

    public PhoenixFission() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        PhoenixFissionConfigs.init();

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::clientSetup);
            modEventBus.addListener(net.phoenix.phoenix_fission.gtm8.client.render.TokamakVolume::onRegisterShaders);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                    net.phoenix.phoenix_fission.gtm8.client.TokamakCommand::register);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
                    net.phoenix.phoenix_fission.gtm8.client.TokamakStageRenderer::onStage);
            registerDynamicRenders();
            net.phoenix.phoenix_fission.gtm8.client.FissionColorHandlers.register(modEventBus);
        }

        PhoenixFissionEntities.init();
        PhoenixFissionBlocks.init();
        net.phoenix.phoenix_fission.gtm8.common.data.item.PhoenixFissionItems.init();
        net.phoenix.phoenix_fission.gtm8.common.data.block.PhoenixFusionBlocks.init();
        PhoenixFissionDefaultComponents.init();
        PhoenixDatagen.init();
        modEventBus.addListener(this::addGtm8Pack);
        modEventBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
    }

    /** The generated models, blockstates and lang for GregTech 8, in a built-in pack on top of the shared assets. */
    private void addGtm8Pack(net.minecraftforge.event.AddPackFindersEvent event) {
        var type = event.getPackType();
        var file = net.minecraftforge.fml.ModList.get().getModFileById(MOD_ID);
        if (file == null) return;
        java.nio.file.Path root = file.getFile().findResource("gtm8_pack");
        var pack = net.minecraft.server.packs.repository.Pack.readMetaAndCreate("phoenix_fission_gtm8",
                net.minecraft.network.chat.Component.literal("Phoenix's Fission (GregTech 8)"), true,
                id -> new net.minecraftforge.resource.PathPackResources(id, true, root),
                type, net.minecraft.server.packs.repository.Pack.Position.TOP,
                net.minecraft.server.packs.repository.PackSource.BUILT_IN);
        if (pack != null) event.addRepositorySource(consumer -> consumer.accept(pack));
    }

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        PhoenixRecipeTypes.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
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
