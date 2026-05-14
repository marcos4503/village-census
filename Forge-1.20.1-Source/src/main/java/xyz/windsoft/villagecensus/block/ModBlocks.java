package xyz.windsoft.villagecensus.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.villagecensus.Main;
import xyz.windsoft.villagecensus.block.custom.CensusLecternBlock;
import xyz.windsoft.villagecensus.item.ModItems;

import java.util.function.Supplier;

/*
 * This class is responsible by the registering of blocks of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModBlocks {

    //Public static final variables
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);

    //Public static variables
    public static RegistryObject<Block> CENSUS_LECTERN_BLOCK = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for block of this mod in the event bus of Forge
        BLOCKS.register(eventBus);

        //Register the "Census Lectern" block...
        CENSUS_LECTERN_BLOCK = RegisterBlock("census_lectern_block", () -> {
            //Set up the block properties and return it for register
            BlockBehaviour.Properties props = BlockBehaviour.Properties.copy(Blocks.LECTERN)
                    .noOcclusion()
                    .sound(SoundType.WOOD);
            return new CensusLecternBlock(props);
        });
    }

    //Private static auxiliar methods

    private static <T extends Block>RegistryObject<T> RegisterBlock(String name, Supplier<T> block){
        //Register the block and prepare to return it
        RegistryObject<T> toReturn = BLOCKS.register(name, block);

        //Register a item for the block too
        RegisterBlockItem(name, toReturn);

        //Return the block registered, with a block item registered too
        return toReturn;
    }

    private static <T extends Block>RegistryObject<Item> RegisterBlockItem(String name, RegistryObject<T> block){
        //Return a registered item for the block, based on a already registered block
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}