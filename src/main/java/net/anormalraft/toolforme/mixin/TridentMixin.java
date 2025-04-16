package net.anormalraft.toolforme.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

@Mixin(TridentItem.class)
public class TridentMixin {
    //Will not trigger in creative or when the trident has loyalty
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;removeItem(Lnet/minecraft/world/item/ItemStack;)V"), cancellable = true)
    public void denyCopy(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft, CallbackInfo ci, @Local ThrownTrident throwntrident){
        System.out.println("test");
        if(stack.has(PREVIOUS_ITEM_DATA.value()) && stack.getEnchantmentLevel(Minecraft.getInstance().level.holderOrThrow(Enchantments.LOYALTY)) >= 1){
            throwntrident.pickup = AbstractArrow.Pickup.DISALLOWED;
            ci.cancel();
        }
    }
}
