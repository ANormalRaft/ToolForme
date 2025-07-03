package net.anormalraft.toolforme.mixin;

import com.llamalad7.mixinextras.sugar.Local;
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

import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

@Mixin(TridentItem.class)
public class TridentMixin extends Item {

    public TridentMixin(Properties properties) {
        super(properties);
    }

    //Impedes the removal of the ItemStack in hand when throwing a trident without loyalty and imposes a cooldown instead. This stops the trident from being lost accidentally when it is the Forme item
    //Will not trigger in creative
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target ="Lnet/minecraft/world/entity/player/Inventory;removeItem(Lnet/minecraft/world/item/ItemStack;)V"), cancellable = true)
    public void denyCopy(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft, CallbackInfo ci, @Local ThrownTrident throwntrident){
        //&& stack.getEnchantmentLevel(Minecraft.getInstance().level.holderOrThrow(Enchantments.LOYALTY)) >= 1
        if(stack.has(PREVIOUS_ITEM_DATA.value())){
            throwntrident.pickup = AbstractArrow.Pickup.DISALLOWED;
            //Applies a cooldown if the thrower is a player and if the item doesn't have loyalty
            if(entityLiving instanceof Player && stack.getTagEnchantments().keySet().stream().noneMatch(enchantmentHolder -> enchantmentHolder.is(Enchantments.LOYALTY))){
                    ((Player) entityLiving).getCooldowns().addCooldown(this, 120);
            }
            ci.cancel();
        }
    }
}
