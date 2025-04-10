package net.anormalraft.toolforme.networking.formeitemtimerpayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record FormeItemTimerPayload(int timerValue) implements CustomPacketPayload {

    public static final Type<FormeItemTimerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("toolforme", "formeitemtimerpayload"));

    public static final StreamCodec<ByteBuf, FormeItemTimerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FormeItemTimerPayload::timerValue,
            FormeItemTimerPayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
