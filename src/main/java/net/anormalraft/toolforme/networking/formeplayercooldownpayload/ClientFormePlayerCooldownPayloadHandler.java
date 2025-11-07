package net.anormalraft.toolforme.networking.formeplayercooldownpayload;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEPLAYERCOOLDOWN;

public class ClientFormePlayerCooldownPayloadHandler {
    public static void handleDataOnNetwork(final FormePlayerCooldownPayload formePlayerCooldownPayload, IPayloadContext context){
        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
            Player player = context.player();
            //Play a sound if the cooldown is about to end. Also display a message
            if(player.getData(FORMEPLAYERCOOLDOWN) == 1){
                ResourceLocation rl = ResourceLocation.tryParse("toolforme:flash_sound");
                SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(rl);
                player.playSound(soundEvent, 0.7f, 1f);
                player.displayClientMessage(Component.literal("Forme change available!"), true);
            }
            player.setData(FORMEPLAYERCOOLDOWN, formePlayerCooldownPayload.cooldownValue() - 1);
        }).exceptionally(e -> {
            // Handle exception
            context.disconnect(Component.translatable("toolforme.networking.failedformeplayercooldown", e.getMessage()));
            return null;
        });
    }
}
