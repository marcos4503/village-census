package xyz.windsoft.villagecensus.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.villagecensus.Main;

/*
 * This class is responsible by the registering of items of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModItems {

    //Public static final variables
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

    //Public static variables
    public static RegistryObject<Item> MOD_BADGE = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for items of this mod in the event bus of Forge
        ITEMS.register(eventBus);

        //Register the "Mod Badge" item...
        MOD_BADGE = ITEMS.register("mod_badge", () -> {
            //Set up the item properties and return it for register
            Item.Properties props = new Item.Properties()
                    .stacksTo(16);
            return new Item(props);
        });
    }
}