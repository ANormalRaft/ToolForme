package net.anormalraft.toolforme.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.anormalraft.toolforme.component.ModDataComponents;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.FormeItemTimerPayload;
import net.anormalraft.toolforme.networking.itemstackpayload.ItemStackPayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEITEMTIMER;
import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEPLAYERCOOLDOWN;
import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

@EventBusSubscriber(modid = "toolforme")
public class ModCommands {
    //Took a long time to get this working
    private static LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("toolforme").requires(source -> source.hasPermission(2));

    public static LiteralArgumentBuilder<CommandSourceStack> checkPlayer = root.then(Commands.literal("checkPlayer").then(Commands.argument("player", EntityArgument.player()).executes(ctx -> displayToolFormeValuesCheck(EntityArgument.getPlayer(ctx, "player")))));

    public static LiteralArgumentBuilder<CommandSourceStack> playerReset = root.then(Commands.literal("playerReset").then(Commands.argument("player", EntityArgument.player()).executes(ctx -> resetPlayerData(EntityArgument.getPlayer(ctx, "player")))));

    public static LiteralArgumentBuilder<CommandSourceStack> revertItem = root.then(Commands.literal("revertItem").executes(ctx -> revertFormeItem(ctx.getSource().getPlayer())));

    private static int displayToolFormeValuesCheck(Player player) {
        Integer formePlayerCooldown = player.getData(FORMEPLAYERCOOLDOWN);
        Integer formeItemTimer = player.getData(FORMEITEMTIMER);
        String playerName = player.getName().getString();
        player.displayClientMessage(Component.literal(playerName + "'s FormePlayerCooldown: " + formePlayerCooldown + ", FormeItemTimer: " + formeItemTimer), false);
        return 1;
    }

    private static int resetPlayerData(ServerPlayer player){
        String playerName = player.getName().getString();
        PacketDistributor.sendToPlayer(player, new FormeItemTimerPayload(0));
        player.setData(FORMEITEMTIMER, -1);
        player.displayClientMessage(Component.literal(playerName + "'s Forme data has been reset!"), false);
        return 1;
    }

    private static int revertFormeItem(Player player){
        boolean foundFormeItem = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = player.getSlot(i).get();
            if (itemStack.has(PREVIOUS_ITEM_DATA)) {
                foundFormeItem = true;
                ModDataComponents.PreviousItemData itemData = itemStack.getComponents().get(PREVIOUS_ITEM_DATA.value());
                //Set the itemstack
                player.getSlot(i).set(itemData.value());
            }
        }
        if(!foundFormeItem){
            player.displayClientMessage(Component.literal("No item with PREVIOUS_ITEM_DATA found in inventory!"), false);
        }
        return 1;
    }

    @SubscribeEvent
    //Custom Commands. For some reason I don't need to register all of them, but will do anyways for readability
    public static void registerCustomCommands(RegisterCommandsEvent event){
        event.getDispatcher().register(ModCommands.checkPlayer);
        event.getDispatcher().register(ModCommands.playerReset);
        event.getDispatcher().register(ModCommands.revertItem);
    }
}
