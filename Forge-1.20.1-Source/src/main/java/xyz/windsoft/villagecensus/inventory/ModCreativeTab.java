package xyz.windsoft.villagecensus.inventory;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.villagecensus.Main;
import xyz.windsoft.villagecensus.block.ModBlocks;
import xyz.windsoft.villagecensus.item.ModItems;

/*
 * This class is responsible by the dedicated creative tab of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModCreativeTab {

    //Public static final variables
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MODID);

    //Public static variables
    public static RegistryObject<CreativeModeTab> VILLAGE_CENSUS_TAB = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the creative mode tabs of this mod in the event bus of Forge
        CREATIVE_MODE_TABS.register(eventBus);

        //Create the "Village Census" custom creative tab
        VILLAGE_CENSUS_TAB = CREATIVE_MODE_TABS.register("village_census_tab", () ->
        {
            //Prepare and set up the tab
            CreativeModeTab creativeModeTab = CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> { return ModItems.MOD_BADGE.get().getDefaultInstance(); })  //<- Use the "Mod Badge" as tab icon
                    .title(Component.literal("Village Census"))
                    .displayItems((params, output) -> {
                        //Add items to display in this custom creative tab
                        output.accept(ModItems.MOD_BADGE.get());
                        output.accept(ModBlocks.CENSUS_LECTERN_BLOCK.get());
                    })
                    .build();
            return creativeModeTab;
        });
    }
}