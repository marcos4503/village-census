package xyz.windsoft.villagecensus.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class BroadcastMessage {

    //Public static methods

    public static void BroadcastMessageToNearPlayers(ServerLevel serverLevel, Vec3 eventPosition, Entity aboutEntity, String defaultName, int blocksRadius, String translateKey){
        //Get the final String to send to Players
        String messageToSend = " ";
        if (aboutEntity.hasCustomName() == false)
            messageToSend = Component.translatable(translateKey, defaultName).getString();
        if (aboutEntity.hasCustomName() == true)
            messageToSend = Component.translatable(translateKey, (defaultName + " " + aboutEntity.getCustomName().getString())).getString();

        //Iterate through all Online Players in the same level of the target Entity...
        for (ServerPlayer serverPlayer : serverLevel.getServer().getPlayerList().getPlayers())
            if (serverPlayer.level() == aboutEntity.level())
                if (isBlocksDistanceBetweenEntitiesEqualOrLessThan(blocksRadius, serverPlayer, eventPosition) == true)
                    serverPlayer.sendSystemMessage(Component.literal(messageToSend));
    }

    //Private static methods

    private static boolean isBlocksDistanceBetweenEntitiesEqualOrLessThan(int blocksDistance, Entity a, Vec3 b){
        //Prepare the value to return
        boolean toReturn = false;

        //Calculate the distance without square root
        double distSq = a.distanceToSqr(b);
        //If the distance is equal or less than...
        if (distSq <= (blocksDistance * blocksDistance))
            toReturn = true;

        //Return the value
        return toReturn;
    }
}