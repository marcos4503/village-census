package xyz.windsoft.villagecensus.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.windsoft.villagecensus.utils.BroadcastMessage;

/*
 * This class do actions when a Entity spawns, only once.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnEntitySpawn {

    //Public events

    @SubscribeEvent
    public void onEntitySpawn(MobSpawnEvent.FinalizeSpawn event){
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;



        //Get the spawned Entity data
        Entity spawnedEntity = event.getEntity();
        String spawnedEntityId = ForgeRegistries.ENTITY_TYPES.getKey(spawnedEntity.getType()).toString();
        MobSpawnType spawnType = event.getSpawnType();

        //If is a Regular Villager...
        if (spawnedEntity instanceof Villager)
            spawnedEntity.getPersistentData().putString("villagecensus_spawntype", String.valueOf(spawnType).toUpperCase());
        //If is a Guard Villager...
        if (spawnedEntityId.equals("guardvillagers:guard") == true)
            spawnedEntity.getPersistentData().putString("villagecensus_spawntype", String.valueOf(spawnType).toUpperCase());
        //If is a Iron Golem...
        if (spawnedEntity instanceof IronGolem)
            spawnedEntity.getPersistentData().putString("villagecensus_spawntype", String.valueOf(spawnType).toUpperCase());
        //If is a Snow Golem...
        if (spawnedEntity instanceof SnowGolem)
            spawnedEntity.getPersistentData().putString("villagecensus_spawntype", String.valueOf(spawnType).toUpperCase());
        //If is a Turret...
        if (spawnedEntityId.contains("v_turrets:") == true)
            spawnedEntity.getPersistentData().putString("villagecensus_spawntype", String.valueOf(spawnType).toUpperCase());
    }
}