package net.anormalraft.toolforme.networking.formeplayercooldownpayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record FormePlayerCooldownPayload(int cooldownValue) implements CustomPacketPayload {

    public static final Type<FormePlayerCooldownPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("toolforme", "formeplayercooldownpayload"));

    public static final StreamCodec<ByteBuf, FormePlayerCooldownPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FormePlayerCooldownPayload::cooldownValue,
            FormePlayerCooldownPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
