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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin extends Entity {

    @Shadow public abstract ItemStack getWeaponItem();

    public ThrownTridentMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //Removes any doubles that a Forme trident could generate
    @Inject(method = "tryPickup", at = @At(value = "HEAD"), cancellable = true)
    public void denyPlayerTouch(Player player, CallbackInfoReturnable<Boolean> cir){
        if(this.getWeaponItem().has(PREVIOUS_ITEM_DATA.value())) {
            this.discard();
            cir.cancel();
        }
    }
}
