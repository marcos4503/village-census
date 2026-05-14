package xyz.windsoft.villagecensus.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.windsoft.villagecensus.utils.BroadcastMessage;

/*
 * This class do actions when a Entity is converted to another Entity.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnEntityConversion {

    //Private final variables
    private final int BROADCAST_MESSAGE_RADIUS = 128; //<- In Blocks

    //Public events

    @SubscribeEvent
    public void onEntityConversion(LivingConversionEvent.Post event){
        //If the entity is null, stop here
        if (event.getOutcome() == null)
            return;

        //If not is the logical server, stop here
        if (event.getOutcome().level().isClientSide() == true)
            return;



        //Get the converted Entity data
        Entity outcomeEntity = event.getOutcome();
        ServerLevel serverLevel = ((ServerLevel) outcomeEntity.level());
        Vec3 eventPosition = new Vec3(outcomeEntity.getX(), outcomeEntity.getY(), outcomeEntity.getZ());

        //If is being converted to Zombie Villager
        if (outcomeEntity instanceof ZombieVillager)
            BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, outcomeEntity, Component.translatable(EntityType.VILLAGER.getDescriptionId()).getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.conversion_zombievillager");
        //If is being converted to Villager
        if (outcomeEntity instanceof Villager)
            BroadcastMessage.BroadcastMessageToNearPlayers(serverLevel, eventPosition, outcomeEntity, Component.translatable(EntityType.VILLAGER.getDescriptionId()).getString(), BROADCAST_MESSAGE_RADIUS, "chat.villagecensus.conversion_villager");
    }
}