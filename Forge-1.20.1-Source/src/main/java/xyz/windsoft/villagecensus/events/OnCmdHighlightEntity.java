package xyz.windsoft.villagecensus.events;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.UUID;

/*
 * This class do actions when a Command is runned.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [X] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnCmdHighlightEntity {

    //Public events

    public OnCmdHighlightEntity OnCmdHighlightEntity(){
        //Return this class after build it
        return this;
    }

    public void Register(RegisterCommandsEvent event){
        //Register this Command
        event.getDispatcher().register(Commands.literal("villagecensus").then(Commands.literal("highlight").requires(source -> source.hasPermission(0)).then(Commands.argument("target_uuid", UuidArgument.uuid()).executes(context -> {
            //Run the logic of this Command...
            return Execute(context.getSource().getPlayer(), context.getSource().getLevel(), context.getSource(), UuidArgument.getUuid(context, "target_uuid"));
        }))));
    }

    //Private auxiliar events

    private int Execute(ServerPlayer originPlayer, ServerLevel originLevel, CommandSourceStack extraContext, UUID uuidToHighlight){
        //Prepare the response
        int success = 0;

        try {
            //Try to find a Entity with the informed UUID
            Entity targetEntity = originLevel.getEntity(uuidToHighlight);

            //Try to get the Living Entity of it
            LivingEntity foundLivingEntity = null;
            if (targetEntity instanceof LivingEntity livingEntity)
                foundLivingEntity = livingEntity;

            //If found the Living Entity...
            if (foundLivingEntity != null){
                //Apply the Glow effect for 8 seconds
                foundLivingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0, false, false));
                //Send the callback to the Player
                //extraContext.sendSuccess(() -> Component.literal("Entity highlighted!"), true);
                //Notify the Player
                originPlayer.displayClientMessage(Component.translatable("command.villagecensus.highlight.found").withStyle(ChatFormatting.GOLD), true);
                //Inform success
                success = 1;
            }
            //If not found the Living Entity...
            if (foundLivingEntity == null){
                //Send the callback to the Player
                //extraContext.sendFailure(Component.literal("Entity not found or too far away."));
                //Notify the Player
                originPlayer.displayClientMessage(Component.translatable("command.villagecensus.highlight.not_found").withStyle(ChatFormatting.RED), true);
            }
        }
        catch (Exception e){
            //Notify the error
            extraContext.sendFailure(Component.translatable("command.villagecensus.highlight"));
        }

        //Return the response
        return success;
    }
}