package net.anormalraft.toolforme.networking.itemstackpayload;

import io.netty.buffer.ByteBuf;
import net.anormalraft.toolforme.ToolForme;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemStackPayload(ItemStack itemStack) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ItemStackPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("toolforme", "itemstackpayload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackPayload> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            ItemStackPayload::itemStack,
            ItemStackPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
