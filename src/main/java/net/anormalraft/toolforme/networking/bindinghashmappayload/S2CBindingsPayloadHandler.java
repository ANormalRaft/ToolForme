package net.anormalraft.toolforme.networking.bindinghashmappayload;

import net.anormalraft.toolforme.ClientTasks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

//Unused
public class S2CBindingsPayloadHandler {
    public static void handleDataOnNetwork(final BindingsPayload bindingsPayload, IPayloadContext context){
        // Do something with the data, on the main thread
        context.enqueueWork(() -> {
            Item[] itemArray = new Item[bindingsPayload.itemList().size()];

            for(int i=0; i < itemArray.length; i++){
                itemArray[i] = bindingsPayload.itemList().get(i).getItem();
            }

            ClientTasks.clientBindingsHashMap.put(bindingsPayload.formeItem(), itemArray);
        })
        .exceptionally(e -> {
            // Handle exception
            context.disconnect(Component.translatable("toolforme.networking.BindingHashMapPayloadFailed", e.getMessage()));
            return null;
        });
    }
}
