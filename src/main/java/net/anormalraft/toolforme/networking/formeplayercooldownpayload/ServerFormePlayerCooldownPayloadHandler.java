package net.anormalraft.toolforme.networking.formeplayercooldownpayload;

import net.anormalraft.toolforme.ToolForme;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEPLAYERCOOLDOWN;

public class ServerFormePlayerCooldownPayloadHandler {
    public static void handleDataOnNetwork(final FormePlayerCooldownPayload formePlayerCooldownPayload, IPayloadContext context){
        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
                    //This flip needs to happen or else the server won't get notified (server)
                    ToolForme.isFormeActive = true;
                    Player player = context.player();
                    player.setData(FORMEPLAYERCOOLDOWN, formePlayerCooldownPayload.cooldownValue() - 1);
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable("toolforme.networking.failedformeplayercooldown", e.getMessage()));
                    return null;
                });
    }
}
