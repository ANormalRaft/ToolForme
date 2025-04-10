package net.anormalraft.toolforme.mixin;

import net.anormalraft.toolforme.component.ModDataComponents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import java.util.function.UnaryOperator;

//import static net.anormalraft.toolforme.component.ModDataComponents.FORME_TIMER;
import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();

    @Shadow public abstract DataComponentMap getComponents();

    @Shadow @Nullable public abstract <T> T set(DataComponentType<? super T> component, @org.jetbrains.annotations.Nullable T value);

    @Shadow @Nullable public abstract <T> T update(DataComponentType<T> component, T defaultValue, UnaryOperator<T> updater);

    @Shadow public abstract void inventoryTick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem);

    //Counts down the FormeTimer in the item if it is above 0
//    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;inventoryTick(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"))
//    public void countdownItemFormeTimer(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci){
//        if(this.getComponents().has(PREVIOUS_ITEM_DATA.value())){
//            ModDataComponents.FormeTimerRecord timer = this.getComponents().get(FORME_TIMER.value());
//            if(timer.value() > 0) {
//                ModDataComponents.FormeTimerRecord timerRecord = new ModDataComponents.FormeTimerRecord(timer.value() - 1);
//                this.set(FORME_TIMER.value(), timerRecord);
//            } else {
//                ModDataComponents.PreviousItemData itemData = this.getComponents().get(PREVIOUS_ITEM_DATA.value());
//                entity.getSlot(inventorySlot).set(itemData.value());
//            }
//        }
//    }
}
