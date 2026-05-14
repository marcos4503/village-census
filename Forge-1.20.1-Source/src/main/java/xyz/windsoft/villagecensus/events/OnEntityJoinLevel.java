package xyz.windsoft.villagecensus.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.windsoft.villagecensus.utils.BroadcastMessage;

/*
 * This class do actions when a Entity join a level.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnEntityJoinLevel {

    //Private final variables
    private final int BROADCAST_MESSAGE_RADIUS = 128; //<- In Blocks

    //Public events

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event){
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;



        //Get the joined Entity data
        Entity joinedEntity = event.getEntity();
        String joinedEntityId = ForgeRegistries.ENTITY_TYPES.getKey(joinedEntity.getType()).toString();
        ServerLevel serverLevel = ((ServerLevel) event.getLevel());
        Vec3 eventPosition = new Vec3(joinedEntity.getX(), joinedEntity.getY(), joinedEntity.getZ());

        //If is a Regular Villager...
        if (joinedEntity instanceof Villager){
            //Read the data about Spawn...
            CompoundTag data = joinedEntity.getPersistentData();
            String spawnType = GetSpawnType(data);
            //If is a supported Spawn Type and was not announced yet, announce this Spawn to near Players and store the info that was already announced...
            if (spawnType.equals("UNKNOWN") == false && spawnType.equals("NATURAL") == false && spawnType.equals("STRUCTURE") == false)
                if (isAnnounced(data) == false){
                    BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, joinedEntity, Component.translatable(EntityType.VILLAGER.getDescriptionId()).getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.spawn_villager");
                    data.putBoolean("villagecensus_announced", true);
                }
        }
        //If is a Guard Villager...
        if (joinedEntityId.equals("guardvillagers:guard") == true){
            //... Guard Villagers don't Spawn, they are converted from Villager to Guard
        }
        //If is a Iron Golem...
        if (joinedEntity instanceof IronGolem){
            //Read the data about Spawn...
            CompoundTag data = joinedEntity.getPersistentData();
            String spawnType = GetSpawnType(data);
            //If is a supported Spawn Type and was not announced yet, announce this Spawn to near Players and store the info that was already announced...
            if (spawnType.equals("UNKNOWN") == false && spawnType.equals("NATURAL") == false && spawnType.equals("STRUCTURE") == false)
                if (isAnnounced(data) == false){
                    BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, joinedEntity, Component.translatable(EntityType.IRON_GOLEM.getDescriptionId()).getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.spawn_iron_golem");
                    data.putBoolean("villagecensus_announced", true);
                }
        }
        //If is a Snow Golem...
        if (joinedEntity instanceof SnowGolem) {
            //Read the data about Spawn...
            CompoundTag data = joinedEntity.getPersistentData();
            String spawnType = GetSpawnType(data);
            //If is a supported Spawn Type and was not announced yet, announce this Spawn to near Players and store the info that was already announced...
            if (spawnType.equals("NATURAL") == false && spawnType.equals("STRUCTURE") == false)
                if (isAnnounced(data) == false){
                    BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, joinedEntity, Component.translatable(EntityType.SNOW_GOLEM.getDescriptionId()).getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.spawn_snow_golem");
                    data.putBoolean("villagecensus_announced", true);
                }
        }
        //If is a Turret...
        if (joinedEntityId.contains("v_turrets:") == true){
            //Read the data about Spawn...
            CompoundTag data = joinedEntity.getPersistentData();
            String spawnType = GetSpawnType(data);
            //If is a supported Spawn Type and was not announced yet, announce this Spawn to near Players and store the info that was already announced...
            if (spawnType.equals("UNKNOWN") == false && spawnType.equals("NATURAL") == false && spawnType.equals("STRUCTURE") == false)
                if (isAnnounced(data) == false){
                    BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, joinedEntity, Component.translatable("chat.villagecensus.generic_turret").getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.spawn_turret");
                    data.putBoolean("villagecensus_announced", true);
                }
        }
    }

    //Private methods

    private String GetSpawnType(CompoundTag data){
        //Prepare the value to return
        String toReturn = "UNKNOWN";

        //If have info, get it
        if (data.contains("villagecensus_spawntype") == true)
            toReturn = data.getString("villagecensus_spawntype");

        //Return the value
        return toReturn;
    }

    private boolean isAnnounced(CompoundTag data){
        //Prepare the value to return
        boolean toReturn = false;

        //If have info, get it
        if (data.contains("villagecensus_announced") == true)
            toReturn = data.getBoolean("villagecensus_announced");

        //Return the value
        return toReturn;
    }
}