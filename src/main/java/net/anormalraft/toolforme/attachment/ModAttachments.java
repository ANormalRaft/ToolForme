package net.anormalraft.toolforme.attachment;

import com.mojang.serialization.Codec;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.FormeItemTimerPayload;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.FormePlayerCooldownPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

@EventBusSubscriber(modid = "toolforme")
public class ModAttachments {

    // Create the DeferredRegister for attachment types
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "toolforme");

    // Serialization via codec
    public static final Supplier<AttachmentType<Integer>> FORMEPLAYERCOOLDOWN = ATTACHMENT_TYPES.register("formeplayercooldown", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());

    public static final Supplier<AttachmentType<Integer>> FORMEITEMTIMER = ATTACHMENT_TYPES.register("formeitemtimer", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());

    @SubscribeEvent
    //Sync Data Attachments to the Player on login. Thanks Gauner on the Neoforge discord, who went through this bullshit before I
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();
        ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
        int formePlayerCooldown = player.getData(FORMEPLAYERCOOLDOWN);

        //Send FormePlayerCooldown data to the client
        PacketDistributor.sendToPlayer(serverPlayer, new FormePlayerCooldownPayload(formePlayerCooldown));

        //Send FormeItemTimer data to both the client and the server(?). If your timer de-syncs, then it is here that you would put 0 and -1 respectively. Added a first join check
        if(player.hasData(FORMEITEMTIMER)) {
            int itemCooldown = player.getData(FORMEITEMTIMER);
            PacketDistributor.sendToPlayer(serverPlayer, new FormeItemTimerPayload(itemCooldown + 1));
            player.setData(FORMEITEMTIMER, itemCooldown);
        } else {
            PacketDistributor.sendToPlayer(serverPlayer, new FormeItemTimerPayload(0));
            player.setData(FORMEITEMTIMER, -1);
        }
    }
}
