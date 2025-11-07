package net.anormalraft.toolforme.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.anormalraft.toolforme.Config;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

@Mixin(TridentItem.class)
public class TridentMixin extends Item {

    public TridentMixin(Properties properties) {
        super(properties);
    }

    //Idea from Combat+ Core mod
    //Impedes the removal of the ItemStack in hand when throwing a trident without loyalty and imposes a cooldown instead. This stops the trident from being lost accidentally when it is the Forme item
    //Will not trigger in creative because that's where this code is in the original releaseUsing
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;removeItem(Lnet/minecraft/world/item/ItemStack;)V"), cancellable = true)
    public void denyCopy(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft, CallbackInfo ci, @Local ThrownTrident throwntrident) {
        //&& stack.getEnchantmentLevel(Minecraft.getInstance().level.holderOrThrow(Enchantments.LOYALTY)) >= 1
        if (stack.has(PREVIOUS_ITEM_DATA.value())) {
            throwntrident.pickup = AbstractArrow.Pickup.DISALLOWED;
            //Applies a cooldown if the thrower is a player
            if (entityLiving instanceof Player) {
                Stream<Holder<Enchantment>> itemEnchantmentStream = stack.getTagEnchantments().keySet().stream();

                String[] stringEnchantmentCooldownArray =  Config.LOYALTY_COOLDOWNS.get().split(",");
                int[] loyaltyCooldowns = new int[stringEnchantmentCooldownArray.length];
                for(int i = 0; i < stringEnchantmentCooldownArray.length; i++){
                    loyaltyCooldowns[i] = Integer.parseInt(stringEnchantmentCooldownArray[i]);
                }

                if (itemEnchantmentStream.noneMatch(enchantmentHolder -> enchantmentHolder.is(Enchantments.LOYALTY))) {
                    ((Player) entityLiving).getCooldowns().addCooldown(this, loyaltyCooldowns[0]);
                } else {
                    int loyaltyLevel =  stack.getTagEnchantments().getLevel(level.holderOrThrow(Enchantments.LOYALTY));
                    if (loyaltyLevel == 1) {
                        ((Player) entityLiving).getCooldowns().addCooldown(this, loyaltyCooldowns[1]);
                    } else if (loyaltyLevel == 2){
                        ((Player) entityLiving).getCooldowns().addCooldown(this, loyaltyCooldowns[2]);
                    } else if (loyaltyLevel == 3){
                        ((Player) entityLiving).getCooldowns().addCooldown(this, loyaltyCooldowns[3]);
                    }
                }
                ci.cancel();
            }
        }
    }
}
