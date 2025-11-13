package net.anormalraft.toolforme.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow public abstract ItemStack getItem(int index);

    @Shadow @Final public Player player;

    //Disallow dropping a Forme item
    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    public void denyFormeRemoval(int index, int count, CallbackInfoReturnable<ItemStack> cir){
        if(this.getItem(index).has(PREVIOUS_ITEM_DATA.value())){
            this.player.displayClientMessage(Component.literal("This Forme cannot be dropped"), true);
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
