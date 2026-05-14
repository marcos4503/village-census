package xyz.windsoft.villagecensus;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import xyz.windsoft.villagecensus.block.ModBlockEntities;
import xyz.windsoft.villagecensus.block.ModBlocks;
import xyz.windsoft.villagecensus.events.*;
import xyz.windsoft.villagecensus.inventory.ModCreativeTab;
import xyz.windsoft.villagecensus.item.ModItems;

/*
 * This class is the Entry Point for this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main
{
    //Public classes
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        //Can use "@Mod.EventBusSubscriber" to automatically register all static methods in the class annotated with @SubscribeEvent...

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("Village Census mod starting on client... >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    //Public static variables
    public static final String MODID = "villagecensus";
    private static final Logger LOGGER = LogUtils.getLogger();

    //Public methods

    public Main() {
        //Get the mod event bus
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        //Register the mod needed events, in forge mod event bus
        MinecraftForge.EVENT_BUS.register(new OnEntitySpawn());
        MinecraftForge.EVENT_BUS.register(new OnEntityJoinLevel());
        MinecraftForge.EVENT_BUS.register(new OnEntityConversion());
        MinecraftForge.EVENT_BUS.register(new OnEntityDeath());

        //Start the register of the mod custom creative tab
        ModCreativeTab.Register(modEventBus);
        //Start the register of the needed mod items
        ModItems.Register(modEventBus);
        //Start the register of the needed mod blocks
        ModBlocks.Register(modEventBus);
        //Start the register of the needed mod block entities
        ModBlockEntities.Register(modEventBus);

        //Register the "CommonSetup" method for modloading
        modEventBus.addListener(this::CommonSetup);

        //Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //Register the mod ForgeConfigSpec, for Forge can create and load the config file
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onServerRegisterCommands(RegisterCommandsEvent event) {
        //Register the mod needed commands
        new OnCmdHighlightEntity().Register(event);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        //Can use "@SubscribeEvent" and let the Event Bus discover methods to call...

        //Do something when the server starts
        LOGGER.info("Village Census mod starting on server...");
    }

    //Private methods

    private void CommonSetup(final FMLCommonSetupEvent event) {
        //Some common setup code
        LOGGER.info("Village Census mod starting!");
        //LOGGER.info("Configs loaded...");
        //LOGGER.info("raidCreationCooldown: " + Config.raidCreationCooldown);
    }
}