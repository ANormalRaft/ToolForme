package net.anormalraft.toolforme.mixin;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class Entity_SwimMixin extends AttachmentHolder implements IEntityExtension {
    @Shadow public abstract void setSwimming(boolean swimming);

    @Shadow public abstract boolean isSprinting();

    @Shadow public abstract boolean isInWater();

    @Shadow public abstract boolean isInFluidType();

    @Shadow public abstract boolean isPassenger();

    @Shadow public abstract boolean isDescending();

    @Shadow public abstract boolean isUnderWater();

    //Shouldn't affect mobs since they don't crouch allegedly (this.isDescending())
    @Redirect(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSwimming(Z)V", ordinal = 0))
    public void activateShieldEvenThroughSprint(Entity instance, boolean swimming){
        this.setSwimming(this.isSprinting() && (this.isInWater() || this.isInFluidType((fluidType, height) -> this.canSwimInFluidType(fluidType))) && !this.isPassenger() && !this.isDescending());
    }

    @Redirect(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSwimming(Z)V", ordinal = 1))
    public void cancelSwimIfDescending(Entity instance, boolean swimming){
        this.setSwimming(this.isSprinting() && (this.isUnderWater() || this.canStartSwimming()) && !this.isPassenger() && !this.isDescending());
    }
}
