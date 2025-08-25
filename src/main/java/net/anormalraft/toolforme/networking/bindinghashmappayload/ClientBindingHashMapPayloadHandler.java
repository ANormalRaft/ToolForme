package net.anormalraft.toolforme.networking.bindinghashmappayload;

import net.anormalraft.toolforme.ClientTasks;
import net.anormalraft.toolforme.networking.itemstackpayload.ItemStackPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

//Unused
public class ClientBindingHashMapPayloadHandler {
    public static void handleDataOnNetwork(final BindingHashMapPayload bindingHashMapPayload, IPayloadContext context){
        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
                    Item[] itemArray = new Item[bindingHashMapPayload.itemList().size()];

                    for(int i=0; i < itemArray.length; i++){
                        itemArray[i] = bindingHashMapPayload.itemList().get(i).getItem();
                    }

                    ClientTasks.clientBindingsHashMap.put(bindingHashMapPayload.formeItem(), itemArray);
                })
                .exceptionally(e -> {
                    // Handle exception
                    context.disconnect(Component.translatable("toolforme.networking.BindingHashMapPayloadFailed", e.getMessage()));
                    return null;
                });
    }
}
