package net.anormalraft.toolforme.mixin;

import com.mojang.blaze3d.platform.WindowEventHandler;
import net.anormalraft.toolforme.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.neoforge.client.extensions.IMinecraftExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler, IMinecraftExtension {

    @Shadow @Nullable public LocalPlayer player;

    @Shadow @Nullable public MultiPlayerGameMode gameMode;

    @Shadow @Final public Options options;

    public MinecraftMixin(String name) {
        super(name);
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
    public boolean stepIntoKeyUseCheck(KeyMapping instance){
        if(Config.SHIELD_CROUCH.get()) {
            return false;
        } else {
            return instance.isDown();
        }
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
    public void denyUseItemCancel(MultiPlayerGameMode instance, Player player){
        if(Config.SHIELD_CROUCH.get()) {
            if (player.isShiftKeyDown()) {
                if (!(player.getUseItem().getItem() instanceof ShieldItem)) {
                    if (!this.options.keyUse.isDown()) {
                        instance.releaseUsingItem(player);
                    }
                }
            } else if (!(player.getUseItem().getItem() instanceof ShieldItem)) {
                if (!this.options.keyUse.isDown()) {
                    instance.releaseUsingItem(player);
                }
            } else {
                instance.releaseUsingItem(player);
            }
        } else {
            instance.releaseUsingItem(player);
        }
    }
}
