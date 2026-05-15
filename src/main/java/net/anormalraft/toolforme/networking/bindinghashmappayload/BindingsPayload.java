package net.anormalraft.toolforme.networking.bindinghashmappayload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BindingsPayload(String formeItem, List<ItemStack> itemList) implements CustomPacketPayload {

    public static final Type<BindingsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("toolforme", "bindinghashmappayload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BindingsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            BindingsPayload::formeItem,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
            BindingsPayload::itemList,
            BindingsPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
