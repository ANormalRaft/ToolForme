package net.anormalraft.toolforme;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.anormalraft.toolforme.attachment.ModAttachments;
import net.anormalraft.toolforme.command.ModCommands;
import net.anormalraft.toolforme.component.ModDataComponents;
import net.anormalraft.toolforme.networking.PayloadHousekeeping;
import net.anormalraft.toolforme.networking.bindinghashmappayload.BindingHashMapPayload;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.FormeItemTimerPayload;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.FormePlayerCooldownPayload;
import net.anormalraft.toolforme.sound.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEITEMTIMER;
import static net.anormalraft.toolforme.attachment.ModAttachments.FORMEPLAYERCOOLDOWN;
import static net.anormalraft.toolforme.component.ModDataComponents.PREVIOUS_ITEM_DATA;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ToolForme.MODID)
public class ToolForme {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "toolforme";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Will Contain forme changing item mappings
    public static HashMap<String, Item[]> bindingsHashMap = null;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ToolForme(IEventBus modEventBus, ModContainer modContainer) {
        //Listener for Custom Keybind
        modEventBus.addListener(this::registerBindings);
        //Listener for Default Item Component injection (Unused)
//        modEventBus.addListener(ModDataComponents::modifyComponents);
        //Listener for Payloads
        modEventBus.addListener(PayloadHousekeeping::registerPayload);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ToolForme) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
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
        event.register(ClientTasks.KEY_MAPPING.get());
    }

    //Sync config of items that change forme on player login
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        bindingsHashMap = HashMap.newHashMap(3);
        //Gson-ify bindings
        JsonObject bindings = new Gson().fromJson(Config.BINDINGS.get(), JsonObject.class);
        for(var entry : bindings.asMap().entrySet()){
            //You cannot do like in KubeJS where you can use "matches()". You have to do all these steps due to Java devs
            String output = entry.getValue().toString();
            //I don't know why the Value appears with the "", but not the key. Maybe somewhere in the conversions, the key lost them?
            String stringPattern = output.substring(1, output.length()-1);
            Pattern pattern = Pattern.compile(stringPattern);

            Item[] allMatchesArray = BuiltInRegistries.ITEM.stream().filter((item) -> pattern.matcher(item.toString()).find()).toArray(Item[]::new);
            bindingsHashMap.put(entry.getKey(), allMatchesArray);
        }

        Player player = event.getEntity();
        ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
        bindingsHashMap.forEach((k,v) -> {
            ItemStack[] itemStackArray = new ItemStack[v.length];
            for(int i=0; i < itemStackArray.length; i++){
                itemStackArray[i] = v[i].getDefaultInstance();
            }
            PacketDistributor.sendToPlayer(serverPlayer, new BindingHashMapPayload(k, Arrays.asList(itemStackArray)));
        });
    }

    // Fire when pressing the mod's keybind. Event is on the NeoForge event bus only on the physical client
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        ClientTasks.checkModKeyPress(event);
    }

    //Cancels the use of the shield on right click
    @SubscribeEvent
    public void onPlayerInteractRightClick(PlayerInteractEvent.RightClickItem event){
        if(Config.SHIELD_CROUCH.get()) {
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
            if(Config.SHIELD_CROUCH.get()) {
                activateShieldOnCrouch(player);
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
            if (itemCooldown > 0) {
                player.setData(FORMEITEMTIMER, itemCooldown - 1);
                PacketDistributor.sendToPlayer(serverPlayer, new FormeItemTimerPayload(itemCooldown));
            } else if (itemCooldown == 0) {
                //Find the slot first
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack itemStack = player.getSlot(i).get();
                    if (itemStack.getComponents().has(PREVIOUS_ITEM_DATA.value())) {
                        //Extract the PREVIOUS_ITEM_DATA
                        ModDataComponents.PreviousItemData itemData = itemStack.getComponents().get(PREVIOUS_ITEM_DATA.value());
                        //Set the itemstack
                        player.getSlot(i).set(itemData.value());
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
            }
        }
    }

    public void activateShieldOnCrouch(Player player){
        ItemStack offhandItem = player.getOffhandItem();
        ItemStack mainhandItem = player.getMainHandItem();
        if (player.isShiftKeyDown() && (offhandItem.getItem() instanceof ShieldItem || mainhandItem.getItem() instanceof ShieldItem)) {
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

    @SubscribeEvent
    public void onLivingDeathEvent(LivingDeathEvent event){
        if(Config.PLAYER_RESET_ON_DEATH.get()) {
            Entity deadEntity = event.getEntity();
            if (deadEntity instanceof ServerPlayer) {
                ModCommands.resetPlayerDataOnDeath(Objects.requireNonNull(deadEntity.getServer()), (ServerPlayer) deadEntity);
            }
        }
    }
}