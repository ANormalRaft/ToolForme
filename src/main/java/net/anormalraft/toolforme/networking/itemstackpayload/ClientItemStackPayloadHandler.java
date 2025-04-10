package net.anormalraft.toolforme.networking.itemstackpayload;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientItemStackPayloadHandler {
    public static void handleDataOnNetwork(final ItemStackPayload formeChangeItemStackPayload, IPayloadContext context){
        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
                    Player player = context.player();
                    player.setItemSlot(EquipmentSlot.MAINHAND ,formeChangeItemStackPayload.itemStack());
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
                    return null;
                });
    }
}
