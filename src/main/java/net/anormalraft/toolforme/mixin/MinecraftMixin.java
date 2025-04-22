package net.anormalraft.toolforme.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow @Nullable public LocalPlayer player;

    @Shadow @Nullable public MultiPlayerGameMode gameMode;

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
    public void denyUseItemCancel(MultiPlayerGameMode instance, Player player){
        if(!(player.isCrouching() && player.getUseItem().getItem() instanceof ShieldItem)){
            instance.releaseUsingItem(player);
        }
    }
}
