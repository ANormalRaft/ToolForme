package net.anormalraft.toolforme;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import net.anormalraft.toolforme.command.ModCommands;
import net.anormalraft.toolforme.component.ModDataComponents;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.ClientFormeItemTimerPayloadHandler;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.FormeItemTimerPayload;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.ServerFormeItemTimerPayloadHandler;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.ClientFormePlayerCooldownPayloadHandler;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.FormePlayerCooldownPayload;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.ServerFormePlayerCooldownPayloadHandler;
import net.anormalraft.toolforme.networking.itemstackpayload.ClientItemStackPayloadHandler;
import net.anormalraft.toolforme.networking.itemstackpayload.ItemStackPayload;
import net.anormalraft.toolforme.networking.itemstackpayload.ServerItemStackPayloadHandler;
import net.anormalraft.toolforme.sound.Sounds;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
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


import java.util.function.Supplier;

//import static net.anormalraft.toolforme.component.ModDataComponents.FORME_TIMER;
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
        // Register the commonSetup method for mod loading
//        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerBindings);
        modEventBus.addListener(this::modifyComponents);
        modEventBus.addListener(this::registerPayload);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ToolForme) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        //Components
        ModDataComponents.REGISTRAR.register(modEventBus);
        //Attachments
        ATTACHMENT_TYPES.register(modEventBus);
        //Sounds
        Sounds.SOUND_EVENTS.register(modEventBus);
    }

    //Register my Keybind
    public void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(KEY_MAPPING.get());
    }

    //Register Payloads
    public void registerPayload(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                ItemStackPayload.TYPE,
                ItemStackPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<ItemStackPayload>(
                        ClientItemStackPayloadHandler::handleDataOnNetwork,
                        ServerItemStackPayloadHandler::handleDataOnNetwork
                )
        );
        registrar.playBidirectional(
                FormePlayerCooldownPayload.TYPE,
                FormePlayerCooldownPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<FormePlayerCooldownPayload>(
                        ClientFormePlayerCooldownPayloadHandler::handleDataOnNetwork,
                        ServerFormePlayerCooldownPayloadHandler::handleDataOnNetwork
                )
        );
        registrar.playBidirectional(
                FormeItemTimerPayload.TYPE,
                FormeItemTimerPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<FormeItemTimerPayload>(
                        ClientFormeItemTimerPayloadHandler::handleDataOnNetwork,
                        ServerFormeItemTimerPayloadHandler::handleDataOnNetwork
                )
        );
    }

    //Default Item Component injection
    public void modifyComponents(ModifyDefaultComponentsEvent event) {
        Config.bindingsHashMap.forEach((key,itemArray) -> {
            for(Item item : itemArray) {
                Item searchedItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(item.toString()));
                event.modify(searchedItem, builder ->
                        builder.set(FORME_BOOL.value(), new ModDataComponents.FormeBoolRecord(true))
                );
            }
        });
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
                            if (player.getData(FORMEPLAYERCOOLDOWN) <= 0 && player.getData(FORMEITEMTIMER) == -1) {
                                if (!itemStack.getComponents().has(PREVIOUS_ITEM_DATA.value())) {
                                    findItemAndApplyDataComponents(itemStack, player);
                                } else {
                                    player.displayClientMessage(Component.literal("Item already in Forme change!"), true);
                                }
                            } else {
                                double minutesLeft = (double) player.getData(FORMEPLAYERCOOLDOWN) / 20 / 60;
                                player.displayClientMessage(Component.literal("Forme change on cooldown for " + String.format("%.2f", minutesLeft) + " minutes"), true);
                                System.out.println(player.getData(FORMEITEMTIMER));
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

    //Finds the corresponding item according to the config, puts it into the player's slot and attaches data components to the item and data attachments to the player to handle the timing of the item
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

    @SubscribeEvent
    //This event fires on both the client and server side. We want to check on which side it is fired on first by checking the level using a neoforge isClientSide method. Responsible for reverting to the original item upon timer expiration, and counting down timers
    public void onPlayerTickEvent(PlayerTickEvent.Pre event){
        //If we are on the server-sided event
        if(!event.getEntity().level().isClientSide) {
            Player player = event.getEntity();
            ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
            int formeCooldown = player.getData(FORMEPLAYERCOOLDOWN);
            int itemCooldown = player.getData(FORMEITEMTIMER);
            if (formeCooldown > 0) {
                player.setData(FORMEPLAYERCOOLDOWN, formeCooldown - 1);
                PacketDistributor.sendToPlayer(serverPlayer, new FormePlayerCooldownPayload(formeCooldown));
            }
            //Item Timer handling and reversion
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

                        System.out.println("PlayerTick: " + player.getData(FORMEITEMTIMER));
                        System.out.println("Goal Reached");
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

    @SubscribeEvent
    //Sync Data Attachments to the Player on login. Thanks Gauner on the Neoforge discord, who went through this bullshit before I
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();
        ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
        int formePlayerCooldown = player.getData(FORMEPLAYERCOOLDOWN);

        //Send FormePlayerCooldown data to the client
        PacketDistributor.sendToPlayer(serverPlayer, new FormePlayerCooldownPayload(formePlayerCooldown));

        //Send FormeItemTimer data to both the client and the server(?). If your timer de-syncs, then it is here that you would put 0 and -1 respectively. Added a first join check
        if(player.hasData(FORMEITEMTIMER)) {
            int itemCooldown = player.getData(FORMEITEMTIMER);
            PacketDistributor.sendToPlayer(serverPlayer, new FormeItemTimerPayload(itemCooldown + 1));
            player.setData(FORMEITEMTIMER, itemCooldown);
        } else {
            PacketDistributor.sendToPlayer(serverPlayer, new FormeItemTimerPayload(0));
            player.setData(FORMEITEMTIMER, -1);
        }
    }

    //Attachment section
    // Create the DeferredRegister for attachment types
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "toolforme");

    // Serialization via codec
    public static final Supplier<AttachmentType<Integer>> FORMEPLAYERCOOLDOWN = ATTACHMENT_TYPES.register(
            "formeplayercooldown", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());

    public static final Supplier<AttachmentType<Integer>> FORMEITEMTIMER = ATTACHMENT_TYPES.register(
            "formeitemtimer", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());

    @SubscribeEvent
    //Custom Commands. For some reason I don't need to register all of them, but will do anyways for readability
    public void registerCustomCommands(RegisterCommandsEvent event){
        event.getDispatcher().register(ModCommands.checkPlayer);
        event.getDispatcher().register(ModCommands.resetPlayer);
        event.getDispatcher().register(ModCommands.revertItem);
    }

//    @SubscribeEvent
//    //Prevent Forme changed item toss
//    public void onItemToss(ItemTossEvent event){
//        if(event.getEntity().getItem().has(PREVIOUS_ITEM_DATA.value())){
//            event.setCanceled(true);
//        }
//    }
}
