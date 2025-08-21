package net.anormalraft.toolforme;

import java.util.*;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid = ToolForme.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {

    public static int formeTimer;
    public static int formePlayerCooldown;
    public static double multiplier;
    public static JsonObject bindings;
    public static HashMap<String, Item[]> bindingsHashMap = HashMap.newHashMap(3);
    public static boolean shieldCrouch;
    public static boolean tridentRiptideFixIfDatapack;
    public static boolean playerResetOnDeath;
    public static List<Integer> loyaltyCooldowns;


    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue FORME_TIMER = BUILDER
            .comment("The timer bound to items which determines the amount of time in ticks an item may be in Forme change")
            .defineInRange("formeTimer", 1800, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FORME_PLAYER_COOLDOWN = BUILDER
            .comment("The amount of time in ticks the player can Forme change (global cooldown)")
            .defineInRange("formePlayerCooldown", 3600, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue MULTIPLIER = BUILDER
            .comment("The multiplier for attack damage")
            .defineInRange("multiplier", 0.25, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<String> BINDINGS = BUILDER
            .comment("String to be transformed into JSON by Gson to store information about what Forme applies to what list of weapons")
            .define("bindings", "{\"minecraft:trident\": \"shovel$\", \"minecraft:mace\": \"_axe$\"}");

    private static final ModConfigSpec.BooleanValue SHIELD_CROUCH = BUILDER
            .comment("Should the shield be only activated on crouch instead of right click. Default: false")
            .define("shieldCrouch", false);

    private static final ModConfigSpec.BooleanValue TRIDENT_RIPTIDE_FIX_IF_DATAPACK = BUILDER
            .comment("Can both loyalty and riptide be enchanted on an item if a datapack allowing loyalty and riptide to not be incompatible exists (through modifying riptide enchantment components). Default: false")
            .define("tridentRiptideFixIfDatapack", false);

    private static final ModConfigSpec.BooleanValue PLAYER_RESET_ON_DEATH = BUILDER
            .comment("Should the item and player's timers be reset upon death (also reverting the Forme item if any)? HIGHLY RECOMMENDED TO BE TRUE FOR NON-KEEPINVENTORY (DOES NOT INCLUDE GRAVESTONES!) PACKS. Default: true")
            .define("playerResetOnDeath", true);

    private static final ModConfigSpec.ConfigValue<List<Integer>> LOYALTY_COOLDOWNS = BUILDER
            .comment("Array listing the cooldown timings in ticks (20 ticks = 1 second) that each level of loyalty should incur on a Forme Trident. The first number is for a trident without loyalty")
            .define("loyaltyCooldowns", Arrays.asList(140, 80, 60, 40));

    public static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        formeTimer = FORME_TIMER.get();
        formePlayerCooldown = FORME_PLAYER_COOLDOWN.get();
        multiplier = MULTIPLIER.get();
        bindings = new Gson().fromJson(BINDINGS.get(), JsonObject.class);
        //Put stuff in HashMap
        for(var entry : bindings.asMap().entrySet()){
            //You cannot do like in KubeJS where you can use "matches()". You have to do all these steps due to Java devs
            String output = entry.getValue().toString();
            String stringPattern = output.substring(1, output.length()-1);
            Pattern pattern = Pattern.compile(stringPattern);

            Item[] allMatchesArray = BuiltInRegistries.ITEM.stream().filter((item) -> pattern.matcher(item.toString()).find()).toArray(Item[]::new);
            bindingsHashMap.put(entry.getKey(), allMatchesArray);
        }
        shieldCrouch = SHIELD_CROUCH.getAsBoolean();
        tridentRiptideFixIfDatapack = TRIDENT_RIPTIDE_FIX_IF_DATAPACK.getAsBoolean();
        playerResetOnDeath = PLAYER_RESET_ON_DEATH.getAsBoolean();
        loyaltyCooldowns = LOYALTY_COOLDOWNS.get();
    }
}
