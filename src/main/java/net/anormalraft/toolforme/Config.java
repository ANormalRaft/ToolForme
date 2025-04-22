package net.anormalraft.toolforme;

import java.util.*;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ToolForme.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue FORME_TIMER = BUILDER
            .comment("The timer bound to items which determines the amount of time in ticks an item may be in Forme change")
            .defineInRange("formeTimer", 60, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FORME_PLAYER_COOLDOWN = BUILDER
            .comment("The amount of time in ticks the player can Forme change (global cooldown)")
            .defineInRange("formePlayerCooldown", 160, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue MULTIPLIER = BUILDER
            .comment("The multiplier for attack damage")
            .defineInRange("multiplier", 0.2, 0.0, Double.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<String> BINDINGS = BUILDER
            .comment("String to be transformed into JSON by Gson to store information about what Forme applies to what list of weapons").define("bindings", "{\"minecraft:trident\": \"shovel$\", \"minecraft:mace\": \"_axe$\"}");

    private static final ModConfigSpec.ConfigValue<Boolean> SHIELD_CROUCH = BUILDER.comment("Should the shield be only activated on crouch instead of right click").define("shieldCrouch", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int formeTimer;
    public static int formePlayerCooldown;
    public static double multiplier;
    public static JsonObject bindings;
    public static HashMap<String, Item[]> bindingsHashMap = HashMap.newHashMap(3);
    public static boolean shieldCrouch;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        formeTimer = FORME_TIMER.get();
        formePlayerCooldown = FORME_PLAYER_COOLDOWN.get();
        multiplier = MULTIPLIER.get();
        bindings = new Gson().fromJson(BINDINGS.get(), JsonObject.class);
        //Put stuff in HashMap
        for(var entry : bindings.asMap().entrySet()){
            //You cannot do like in KubeJS where you can use "matches()". You have to do all these steps because Java devs fucked up
            String output = entry.getValue().toString();
            String stringPattern = output.substring(1, output.length()-1);
            Pattern pattern = Pattern.compile(stringPattern);

            Item[] allMatchesArray = BuiltInRegistries.ITEM.stream().filter((item) -> pattern.matcher(item.toString()).find()).toArray(Item[]::new);
            bindingsHashMap.put(entry.getKey(), allMatchesArray);
        }
        shieldCrouch = SHIELD_CROUCH.get();
    }
}
