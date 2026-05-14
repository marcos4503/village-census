package xyz.windsoft.villagecensus.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.windsoft.villagecensus.utils.BroadcastMessage;

/*
 * This class do actions when a Entity dead.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnEntityDeath {

    //Private final variables
    private final int BROADCAST_MESSAGE_RADIUS = 128; //<- In Blocks

    //Public events

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event){
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;



        //Get the dead Entity data
        Entity deadEntity = event.getEntity();
        String deadEntityId = ForgeRegistries.ENTITY_TYPES.getKey(deadEntity.getType()).toString();
        ServerLevel serverLevel = ((ServerLevel) deadEntity.level());
        Vec3 eventPosition = new Vec3(deadEntity.getX(), deadEntity.getY(), deadEntity.getZ());

        //If is a Regular Villager...
        if (deadEntity instanceof Villager)
            BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, deadEntity, Component.translatable(EntityType.VILLAGER.getDescriptionId()).getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.dead_villager");
        //If is a Guard Villager...
        if (deadEntityId.equals("guardvillagers:guard") == true)
            BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, deadEntity, Component.translatable("chat.villagecensus.generic_guard").getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.dead_guard");
        //If is a Iron Golem...
        if (deadEntity instanceof IronGolem)
            BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, deadEntity, Component.translatable(EntityType.IRON_GOLEM.getDescriptionId()).getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.dead_irongolem");
        //If is a Snow Golem...
        if (deadEntity instanceof SnowGolem)
            BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, deadEntity, Component.translatable(EntityType.SNOW_GOLEM.getDescriptionId()).getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.dead_snowgolem");
        //If is a Turret...
        if (deadEntityId.contains("v_turrets:") == true)
            BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, deadEntity, Component.translatable("chat.villagecensus.generic_turret").getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.dead_turret");
    }
}