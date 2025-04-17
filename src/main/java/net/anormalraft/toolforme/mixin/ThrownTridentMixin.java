package net.anormalraft.toolforme.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends Entity {

    public ThrownTridentMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tryPickup", at = @At(value = "HEAD"), cancellable = true)
    public void denyPlayerTouch(Player player, CallbackInfoReturnable<Boolean> cir){
        this.discard();
        cir.cancel();
    }
}
