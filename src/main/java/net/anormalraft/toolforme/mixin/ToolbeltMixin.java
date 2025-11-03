package net.anormalraft.toolforme.mixin;

import dev.gigaherz.toolbelt.network.SwapItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

//Toolbelt mod
@Mixin(SwapItems.class)
public class ToolbeltMixin {
    @Inject(method = "swapItem", at=@At(value = "HEAD"), cancellable = true)
    private static void disallowToolformeSwap(int swapWith, Player player, CallbackInfo ci){
        if(player.getMainHandItem().has(PREVIOUS_ITEM_DATA.value())){
            player.displayClientMessage(Component.literal("This Forme cannot be moved"), true);
            ci.cancel();
        }
    }
}
