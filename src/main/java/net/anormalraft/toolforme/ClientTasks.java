package net.anormalraft.toolforme;

import com.mojang.blaze3d.platform.InputConstants;
import net.anormalraft.toolforme.component.ModDataComponents;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.FormeItemTimerPayload;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.FormePlayerCooldownPayload;
import net.anormalraft.toolforme.networking.itemstackpayload.ItemStackPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.regex.Pattern;

import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEITEMTIMER;
import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEPLAYERCOOLDOWN;
import static net.anormalraft.toolforme.component.ModDataComponents.FORME_BOOL;
import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

public class ClientTasks {

    public static HashMap<String, Item[]> clientBindingsHashMap = HashMap.newHashMap(3);

    // Key mapping is lazily initialized so it doesn't exist until it is registered
    public static final Lazy<KeyMapping> KEY_MAPPING = Lazy.of(() -> new KeyMapping("key.toolforme.changeforme", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.toolforme.toolformecategory"));

    //Helper Method. Search for items in the server config and apply the FORME_BOOL to them
    public static ItemStack[] searchAndPrepareIfItemStackInConfig(ItemStack itemStack){
        //Data to add (the .copy() is important, or else you get stackoverflow)
        ModDataComponents.PreviousItemData previousItemData = new ModDataComponents.PreviousItemData(itemStack.copy());
        ModDataComponents.FormeBoolRecord formeBool = new ModDataComponents.FormeBoolRecord(true);
        //Array for containment purposes
        ItemStack[] itemStackArray = {itemStack, null};
        //Loop Config Map
        clientBindingsHashMap.forEach((key, itemArray) -> {
            for(Item item : itemArray) {
                if(item.toString().equals(itemStack.getItem().toString())) {
                    //Modify base item with FORME_BOOL
                    itemStack.set(FORME_BOOL.value(), new ModDataComponents.FormeBoolRecord(true));

                    //Prepare Forme ItemStack
                    ItemStack formeChangeItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(key)).getDefaultInstance();
                    formeChangeItem.set(PREVIOUS_ITEM_DATA.value(), previousItemData);
                    formeChangeItem.set(FORME_BOOL.value(), formeBool);

                    //Modify stats
                    double previousAttackDamageValue = previousItemData.value().getAttributeModifiers().modifiers().stream().filter(attributeEntry -> attributeEntry.modifier().is(ResourceLocation.parse("minecraft:base_attack_damage"))).findFirst().get().modifier().amount();

                    ItemAttributeModifiers itemAttributeModifiers =  formeChangeItem.getAttributeModifiers().withModifierAdded(Attributes.ATTACK_DAMAGE, new AttributeModifier(ResourceLocation.parse("minecraft:base_attack_damage"),previousAttackDamageValue * Config.MULTIPLIER.get() , AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.MAINHAND);

                    formeChangeItem.set(DataComponents.ATTRIBUTE_MODIFIERS, itemAttributeModifiers);

                    //Apply existing enchantments
                    EnchantmentHelper.setEnchantments(formeChangeItem, previousItemData.value().getTagEnchantments());

                    //Apply custom name
                    formeChangeItem.set(DataComponents.CUSTOM_NAME, previousItemData.value().getComponents().get(DataComponents.CUSTOM_NAME));

                    //Make unbreakable
                    formeChangeItem.set(DataComponents.UNBREAKABLE, new Unbreakable(true));

                    //Shove it into the exported array
                    itemStackArray[1] = formeChangeItem;
                }
            }
        });
        return itemStackArray;
    }

    //Helper Method that finds the corresponding item according to the config, puts it into the player's slot and attaches data components to the item and data attachments to the player to handle the timing of the item
    public static void switchBaseItemToToolFormeItem(ItemStack[] itemStackArray, Player player){
        ItemStack formeChangeItem = itemStackArray[1].copy();
        //Make a sound
        ResourceLocation rl = ResourceLocation.tryParse("toolforme:up_sound");
        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(rl);
        player.playSound(soundEvent, 0.7f, 1f);

        //Send the item swap request to the server
        PacketDistributor.sendToServer(new ItemStackPayload(formeChangeItem));

        //Data attachment of the item's timer. Both the client and server need to be notified
        int itemTimerValue = Config.FORME_TIMER.get();
        player.setData(FORMEITEMTIMER, itemTimerValue);
        PacketDistributor.sendToServer(new FormeItemTimerPayload(itemTimerValue + 1));

        //Data attachment of the player's cooldown. Both the client and server need to be notified
        int playerCooldown = Config.FORME_PLAYER_COOLDOWN.get();
        player.setData(FORMEPLAYERCOOLDOWN, playerCooldown);
        PacketDistributor.sendToServer(new FormePlayerCooldownPayload(playerCooldown + 1));
    }

    // Fire when pressing the mod's keybind
    public static void checkModKeyPress(ClientTickEvent.Post event){
        while (KEY_MAPPING.get().consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack itemStack = player.getMainHandItem();
                if (!itemStack.isEmpty()) {
                    ItemStack[] itemStackArray = searchAndPrepareIfItemStackInConfig(itemStack);
                    ItemStack baseItem = itemStackArray[0].copy();
                    if (baseItem.getComponents().has(FORME_BOOL.value())) {
                        if(baseItem.getComponents().get(FORME_BOOL.value()).value()) {
                            //Check to impede softlock when dying. For some reason, the client FORMEITEMTIMER doesn't sync on death, while its server counterpart does, as well as the entirety of FORMEPLAYERCOOLDOWN. This if statement takes the FORMEITEMTIMER out back, resets it to -1, and fires findItemAndApplyDataComponents
                            if(!(player.getData(FORMEPLAYERCOOLDOWN) == 0 && player.getData(FORMEITEMTIMER) == 0)) {
                                if (player.getData(FORMEPLAYERCOOLDOWN) <= 0 && player.getData(FORMEITEMTIMER) == -1) {
                                    if (!baseItem.getComponents().has(PREVIOUS_ITEM_DATA.value())) {
                                        switchBaseItemToToolFormeItem(itemStackArray, player);
                                    } else {
                                        player.displayClientMessage(Component.literal("Item already in Forme change!"), true);
                                    }
                                } else {
                                    double minutesLeft = (double) player.getData(FORMEPLAYERCOOLDOWN) / 20 / 60;
                                    player.displayClientMessage(Component.literal("Forme change on cooldown for " + String.format("%.0f", Math.floor(minutesLeft)) + " minutes and " + String.format("%.0f", (minutesLeft - Math.floor(minutesLeft)) * 60) + " seconds"), true);
//                                    System.out.println("FormePlayerCooldown: " + player.getData(FORMEPLAYERCOOLDOWN));
//                                    System.out.println("FormeItemTimer: " + player.getData(FORMEITEMTIMER));
                                }
                            } else {
                                player.setData(FORMEITEMTIMER, -1);
                                switchBaseItemToToolFormeItem(itemStackArray, player);
                            }
                        } else {
                            player.displayClientMessage(Component.literal("This item's Forme has been sealed"), true);
                        }
                    } else {
                        player.displayClientMessage(Component.literal("This item cannot change Forme"), true);
                    }
                }
            }
        }
    }
}
