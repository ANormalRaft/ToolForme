package net.anormalraft.toolforme.networking.bindinghashmappayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public record BindingHashMapPayload(String formeItem, List<ItemStack> itemList) implements CustomPacketPayload {

    public static final Type<BindingHashMapPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("toolforme", "bindinghashmappayload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BindingHashMapPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            BindingHashMapPayload::formeItem,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BindingHashMapPayload::itemList,
            BindingHashMapPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
