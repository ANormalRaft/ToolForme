package net.anormalraft.toolforme.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow public abstract ItemStack getItem();

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void denyPickup(Player player, CallbackInfoReturnable<Boolean> cir){
        if(this.getItem().has(PREVIOUS_ITEM_DATA.value())){
            player.displayClientMessage(Component.literal("This Forme cannot be moved"), true);
            cir.setReturnValue(false);
        }
    }
}
