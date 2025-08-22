package net.anormalraft.toolforme.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.anormalraft.toolforme.Config;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//Requires a datapack to allow riptide to be enchantable with loyalty through the enchantment data in riptide.json
@Mixin(TridentItem.class)
public class TridentRiptideFixIfDatapackMixin extends Item {

    public TridentRiptideFixIfDatapackMixin(Properties properties) {
        super(properties);
    }

    //Allow Riptide tridents to be thrown
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResultHolder;fail(Ljava/lang/Object;)Lnet/minecraft/world/InteractionResultHolder;", ordinal = 1), cancellable = true)
    public void allowRiptideThrow(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        if(Config.TRIDENT_RIPTIDE_FIX_IF_DATAPACK.get()) {
            ItemStack itemstack = player.getItemInHand(hand);
            player.startUsingItem(hand);
            cir.setReturnValue(InteractionResultHolder.consume(itemstack));
        }
    }

    //Remove inconvenient item use condition restriction
    //The ordinal is very important, otherwise it doesn't work properly
    @Definition(id = "f", local = @Local(type = float.class))
    @Expression("f > 0.0")
    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private boolean modifyInitialCheck(boolean original){
        if(Config.TRIDENT_RIPTIDE_FIX_IF_DATAPACK.get()) {
            return false;
        } else {
            return original;
        }
    }

    //Allow for Loyalty through riptide
    @Definition(id = "f", local = @Local(type = float.class))
    @Expression("f == 0.0")
    @ModifyExpressionValue(method = "releaseUsing", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean allowLoyaltyThroughRiptide(boolean original, @Local(argsOnly = true) LivingEntity livingEntity){
        if(Config.TRIDENT_RIPTIDE_FIX_IF_DATAPACK.get()) {
            return !(!original && livingEntity.isInWaterOrRain());
        } else {
            return original;
        }
    }

    //Block throw if Riptide and player is in water condition
    @Definition(id = "f", local = @Local(type = float.class))
    @Expression("f > 0.0")
    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1))
    private boolean blockThrowConditionIfInWater(boolean original, @Local(argsOnly = true) LivingEntity livingEntity){
        if(Config.TRIDENT_RIPTIDE_FIX_IF_DATAPACK.get()) {
            return original && livingEntity.isInWaterOrRain();
        } else {
            return original;
        }
    }
}
