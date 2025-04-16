package net.anormalraft.toolforme.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    //Records
    public record FormeBoolRecord(boolean value) {
    }

    public record PreviousItemData(ItemStack value) {
    }


    //Codec
    public static final Codec<FormeBoolRecord> FORME_BOOL_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("value").forGetter(FormeBoolRecord::value)
            ).apply(instance, FormeBoolRecord::new)
    );
    public static final Codec<PreviousItemData> PREVIOUS_ITEM_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStack.CODEC.fieldOf("value").forGetter(PreviousItemData::value)
            ).apply(instance, PreviousItemData::new)
    );

    //StreamCodec
    public static final StreamCodec<ByteBuf, FormeBoolRecord> FORME_BOOL_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, FormeBoolRecord::value,
            FormeBoolRecord::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PreviousItemData> PREVIOUS_ITEM_STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, PreviousItemData::value,
            PreviousItemData::new
    );

    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "toolforme");

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FormeBoolRecord>> FORME_BOOL = REGISTRAR.registerComponentType(
            "forme_bool",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(FORME_BOOL_CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(FORME_BOOL_STREAM_CODEC)
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PreviousItemData>> PREVIOUS_ITEM_DATA = REGISTRAR.registerComponentType(
            "previous_item_data",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(PREVIOUS_ITEM_CODEC)
                    // The codec to read/write the data across the network
                    .networkSynchronized(PREVIOUS_ITEM_STREAM_CODEC)
    );
}
    

