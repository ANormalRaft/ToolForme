package net.anormalraft.toolforme.networking.formeitemtimerpayload;

import net.anormalraft.toolforme.networking.formeplayercooldownpayload.FormePlayerCooldownPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEITEMTIMER;

public class ClientFormeItemTimerPayloadHandler {
    public static void handleDataOnNetwork(final FormeItemTimerPayload formeItemTimerPayload, IPayloadContext context){
        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
                    Player player = context.player();
                    //Play a sound if the itemtimer is about to end
                    if(player.getData(FORMEITEMTIMER) == 1){
                        ResourceLocation rl = ResourceLocation.tryParse("toolforme:down_sound");
                        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(rl);
                        player.playSound(soundEvent, 0.7f, 1f);
                    }
                    player.setData(FORMEITEMTIMER, formeItemTimerPayload.timerValue() - 1);
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable("toolforme.networking.failedformeitemtimercooldown", e.getMessage()));
                    return null;
                });
    }
}
