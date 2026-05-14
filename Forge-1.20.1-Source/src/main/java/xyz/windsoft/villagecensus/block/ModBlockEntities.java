package xyz.windsoft.villagecensus.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.villagecensus.Main;
import xyz.windsoft.villagecensus.block.entity.CensusLecternBlockEntity;

/*
 * This class is responsible by the registering of blocks entities of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModBlockEntities {

    //Public static final variables
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MODID);

    //Public static variables
    public static RegistryObject<BlockEntityType<CensusLecternBlockEntity>> CENSUS_LECTERN_BLOCK_ENTITY = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for block entities of this mod in the event bus of Forge
        BLOCK_ENTITIES.register(eventBus);

        //Register the "Census Lectern" block entity...
        CENSUS_LECTERN_BLOCK_ENTITY = BLOCK_ENTITIES.register("census_lectern_block_entity", () -> {
            //Set up the block entity type and return it for register
            BlockEntityType<CensusLecternBlockEntity> blockEntityType = BlockEntityType.Builder.of(CensusLecternBlockEntity::new, ModBlocks.CENSUS_LECTERN_BLOCK.get())
                    .build(null);
            return blockEntityType;
        });
    }
}