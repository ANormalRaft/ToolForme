package net.anormalraft.toolforme.networking.formeitemtimerpayload;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.anormalraft.toolforme.ToolForme.FORMEITEMTIMER;

public class ServerFormeItemTimerPayloadHandler {
    public static void handleDataOnNetwork(final FormeItemTimerPayload formeItemTimerPayload, IPayloadContext context){
        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
                    Player player = context.player();
                    player.setData(FORMEITEMTIMER, formeItemTimerPayload.timerValue() - 1);
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable("toolforme.networking.failedformeitemtimercooldown", e.getMessage()));
                    return null;
                });
    }
}
