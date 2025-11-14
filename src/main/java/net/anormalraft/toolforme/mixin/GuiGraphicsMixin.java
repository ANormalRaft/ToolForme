package net.anormalraft.toolforme.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.anormalraft.toolforme.Config;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEITEMTIMER;
import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

//Renders the Item Cooldown behind the item
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Shadow public abstract void fill(RenderType renderType, int minX, int minY, int maxX, int maxY, int z, int color);

    @Shadow public abstract void fillGradient(RenderType renderType, int x1, int y1, int x2, int y2, int colorFrom, int colorTo, int z);

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    public void renderItemCooldown(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci, @Local LocalPlayer localPlayer){
        if(stack.has(PREVIOUS_ITEM_DATA.value())) {
            int playerCurrentItemCooldown = localPlayer.getData(FORMEITEMTIMER);
            int maxItemCooldown = Config.FORME_ITEM_TIMER.get();
            float f = Mth.clamp((float) playerCurrentItemCooldown /maxItemCooldown, 0.0F, 1.0F);
            //Used code used for the item cooldown render found in the same method
            int i1 = y + Mth.floor(16.0F * (1.0F - f));
            int j1 = i1 + Mth.ceil(16.0F * f);
            //z at -75 successfully goes behind ItemStacks with more than 1 item count. If I do -100, it doesn't show for items of count 1
            this.fillGradient(RenderType.gui(), x, i1, x + 16, j1, FastColor.ARGB32.color(90, 217, 177), FastColor.ARGB32.color(169, 36, 230), -75);
        }
    }
}
