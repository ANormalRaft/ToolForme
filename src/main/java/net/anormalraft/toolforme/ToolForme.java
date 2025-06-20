package net.anormalraft.toolforme;

import com.mojang.blaze3d.platform.InputConstants;
import net.anormalraft.toolforme.attachment.ModAttachments;
import net.anormalraft.toolforme.component.ModDataComponents;
import net.anormalraft.toolforme.networking.PayloadHousekeeping;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.FormeItemTimerPayload;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.FormePlayerCooldownPayload;
import net.anormalraft.toolforme.networking.itemstackpayload.ItemStackPayload;
import net.anormalraft.toolforme.sound.ModSounds;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.minecraft.world.item.Item;

import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEITEMTIMER;
import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEPLAYERCOOLDOWN;
import static net.anormalraft.toolforme.component.ModDataComponents.FORME_BOOL;
import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ToolForme.MODID)
public class ToolForme {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "toolforme";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Key mapping is lazily initialized so it doesn't exist until it is registered
    public static final Lazy<KeyMapping> KEY_MAPPING = Lazy.of(() -> new KeyMapping("key.toolforme.changeforme", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.toolforme.toolformecategory"));
    //Boolean to control the item swap previously handled by the Mixin
    public boolean isFormeActive = false;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ToolForme(IEventBus modEventBus, ModContainer modContainer) {
        //Listener for Custom Keybind
        modEventBus.addListener(this::registerBindings);
        //Listener for Default Item Component injection
        modEventBus.addListener(ModDataComponents::modifyComponents);
        //Listener for Payloads
        modEventBus.addListener(PayloadHousekeeping::registerPayload);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ToolForme) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        //Components
        ModDataComponents.REGISTRAR.register(modEventBus);
        //Attachments
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        //Sounds
        ModSounds.SOUND_EVENTS.register(modEventBus);

        //ModCommands and ModAttachments have been tagged with @EventBusSubscriber (replaces Neoforge.EVENT_BUS.register)
    }

    //Register my Keybind
    public void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(KEY_MAPPING.get());
    }

    // Fire when pressing the mod's keybind. Event is on the NeoForge event bus only on the physical client
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        while (KEY_MAPPING.get().consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack itemStack = player.getMainHandItem();
                if (!itemStack.isEmpty()) {
                    if (itemStack.getComponents().has(FORME_BOOL.value())) {
                        if(itemStack.getComponents().get(FORME_BOOL.value()).value()) {
                            //Check to impede softlock when dying. For some reason, the client FORMEITEMTIMER doesn't sync on death, while its server counterpart does, as well as the entirety of FORMEPLAYERCOOLDOWN. This if statement takes the FORMEITEMTIMER out back, resets it to -1, and fires findItemAndApplyDataComponents
                            if(!(player.getData(FORMEPLAYERCOOLDOWN) == 0 && player.getData(FORMEITEMTIMER) == 0)) {
                                if (player.getData(FORMEPLAYERCOOLDOWN) <= 0 && player.getData(FORMEITEMTIMER) == -1) {
                                    if (!itemStack.getComponents().has(PREVIOUS_ITEM_DATA.value())) {
                                        findItemAndApplyDataComponents(itemStack, player);
                                    } else {
                                        player.displayClientMessage(Component.literal("Item already in Forme change!"), true);
                                    }
                                } else {
                                    double minutesLeft = (double) player.getData(FORMEPLAYERCOOLDOWN) / 20 / 60;
                                    player.displayClientMessage(Component.literal("Forme change on cooldown for " + String.format("%.0f", minutesLeft) + " minutes and " + String.format("%.0f", ((minutesLeft - Math.floor(minutesLeft)) * 100 * 30) / 60) + " seconds"), true);
//                                    System.out.println("FormePlayerCooldown: " + player.getData(FORMEPLAYERCOOLDOWN));
//                                    System.out.println("FormeItemTimer: " + player.getData(FORMEITEMTIMER));
                                }
                            } else {
                                player.setData(FORMEITEMTIMER, -1);
                                findItemAndApplyDataComponents(itemStack, player);
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

    //Helper Method that finds the corresponding item according to the config, puts it into the player's slot and attaches data components to the item and data attachments to the player to handle the timing of the item
    public void findItemAndApplyDataComponents(ItemStack itemStack, Player player){
        //Data to add (the .copy() is important, or else you get stackoverflow)
        ModDataComponents.PreviousItemData previousItemData = new ModDataComponents.PreviousItemData(itemStack.copy());
        ModDataComponents.FormeBoolRecord formeBool = new ModDataComponents.FormeBoolRecord(true);
        //Put Forme item in mainhand with the new Component Data
        Config.bindingsHashMap.forEach((key,itemArray) -> {
            for(Item item : itemArray) {
                if(item.toString().equals(itemStack.getItem().toString())) {
                    ItemStack formeChangeItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(key)).getDefaultInstance();
                    //Set the previous item data into the item as well as the forme boolean. Does not require server shenanigans
                    formeChangeItem.set(PREVIOUS_ITEM_DATA.value(), previousItemData);
                    formeChangeItem.set(FORME_BOOL.value(), formeBool);

                    //Modify stats
                    double previousAttackDamageValue = previousItemData.value().getAttributeModifiers().modifiers().stream().filter(attributeEntry -> attributeEntry.modifier().is(ResourceLocation.parse("minecraft:base_attack_damage"))).findFirst().get().modifier().amount();

                    ItemAttributeModifiers itemAttributeModifiers =  formeChangeItem.getAttributeModifiers().withModifierAdded(Attributes.ATTACK_DAMAGE, new AttributeModifier(ResourceLocation.parse("minecraft:base_attack_damage"),previousAttackDamageValue * 1.2 , AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.MAINHAND);

                    formeChangeItem.set(DataComponents.ATTRIBUTE_MODIFIERS, itemAttributeModifiers);

                    //Apply existing enchantments
                    EnchantmentHelper.setEnchantments(formeChangeItem, previousItemData.value().getTagEnchantments());

                    //Apply custom name
                    formeChangeItem.set(DataComponents.CUSTOM_NAME, previousItemData.value().getComponents().get(DataComponents.CUSTOM_NAME));

                    //Make unbreakable
                    formeChangeItem.set(DataComponents.UNBREAKABLE, new Unbreakable(true));

                    //Make a sound
                    ResourceLocation rl = ResourceLocation.tryParse("toolforme:up_sound");
                    SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(rl);
                    player.playSound(soundEvent, 0.7f, 1f);

                    //Send the item swap request to the server
                    PacketDistributor.sendToServer(new ItemStackPayload(formeChangeItem));

                    //Data attachment of the item's timer. Both the client and server need to be notified
                    System.out.println("PENUSSSSSSSSSSSSSSSSSS");
                    int itemTimerValue = Config.formeTimer;
                    player.setData(FORMEITEMTIMER, itemTimerValue);
                    PacketDistributor.sendToServer(new FormeItemTimerPayload(itemTimerValue + 1));

                    //Data attachment of the player's cooldown. Both the client and server need to be notified
                    int playerCooldown = Config.formePlayerCooldown;
                    player.setData(FORMEPLAYERCOOLDOWN, playerCooldown);
                    PacketDistributor.sendToServer(new FormePlayerCooldownPayload(playerCooldown + 1));

                    //Set the forme control boolean to true
                    isFormeActive = true;
                    return;
                }
            }
        });
    }

    //Cancels the use of the shield on right click
    @SubscribeEvent
    public void onPlayerInteractRightClick(PlayerInteractEvent.RightClickItem event){
        if(Config.shieldCrouch) {
            if (event.getItemStack().getItem() instanceof ShieldItem) {
                event.setCanceled(true);
            }
        }
    }

    //This event fires on both the client and server side. We want to check on which side it is fired on first by checking the level using a neoforge isClientSide method. Responsible for reverting to the original item upon timer expiration, and counting down timers
    @SubscribeEvent
    public void onPlayerTickEvent(PlayerTickEvent.Pre event){
        //If we are on the server-sided event
        if(!event.getEntity().level().isClientSide) {
            Player player = event.getEntity();
            ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());

            //Shield section (part of the code responsible for enabling shield on crouch. The rest is in MinecraftMixin)
            if(Config.shieldCrouch) {
                ItemStack offhandItem = player.getOffhandItem();
                ItemStack mainhandItem = player.getMainHandItem();
                if (player.isCrouching() && (offhandItem.getItem() instanceof ShieldItem || mainhandItem.getItem() instanceof ShieldItem)) {
                    //Offhand has priority
                    if (offhandItem.getItem() instanceof ShieldItem && !player.getCooldowns().isOnCooldown(offhandItem.getItem())) {
                        if (!(player.getUseItem() == offhandItem)) {
                            offhandItem.use(player.level(), player, InteractionHand.OFF_HAND);
                        }
                    } else {
                        if(mainhandItem.getItem() instanceof ShieldItem && !player.getCooldowns().isOnCooldown(mainhandItem.getItem())) {
                            if (!(player.getUseItem() == mainhandItem)) {
                                mainhandItem.use(player.level(), player, InteractionHand.MAIN_HAND);
                            }
                        }
                    }
                }
            }

            //Forme section
            int formeCooldown = player.getData(FORMEPLAYERCOOLDOWN);
            int itemCooldown = player.getData(FORMEITEMTIMER);
            //Forme Cooldown handling
            if (formeCooldown > 0) {
                player.setData(FORMEPLAYERCOOLDOWN, formeCooldown - 1);
                PacketDistributor.sendToPlayer(serverPlayer, new FormePlayerCooldownPayload(formeCooldown));
            }
            //Item Timer handling and tool reversion
            if (isFormeActive && itemCooldown == 0 ) {
                //Find the slot first
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack itemStack = player.getSlot(i).get();
                    if (itemStack.getComponents().has(PREVIOUS_ITEM_DATA.value())) {
                        //Extract the PREVIOUS_ITEM_DATA
                        ModDataComponents.PreviousItemData itemData = itemStack.getComponents().get(PREVIOUS_ITEM_DATA.value());
                        //Set the itemstack
                        player.getSlot(i).set(itemData.value());
                        //Forme control boolean to false
                        isFormeActive = false;
                        //Forme timer here so that the timer doesn't go into further negative numbers and to lock the player missing their item
                        player.setData(FORMEITEMTIMER, itemCooldown - 1);
                        PacketDistributor.sendToPlayer(serverPlayer, new FormeItemTimerPayload(itemCooldown));
//                        System.out.println("PlayerTick: " + player.getData(FORMEITEMTIMER));
//                        System.out.println("Goal Reached");
                        return;
                    }
                }
//            throw new NoSuchElementException("No Forme Change item was found in inventory on item forme timer running out");
                player.displayClientMessage(Component.literal("Pick up your dropped Forme Item or reset your ToolForme timers with commands"), true);
            } else if (itemCooldown > 0) {
                player.setData(FORMEITEMTIMER, itemCooldown - 1);
                PacketDistributor.sendToPlayer(serverPlayer, new FormeItemTimerPayload(itemCooldown));
            }
        }
    }
}