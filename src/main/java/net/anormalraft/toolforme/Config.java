package net.anormalraft.toolforme;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = ToolForme.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue FORME_TIMER = BUILDER
            .comment("The timer bound to items which determines the amount of time in ticks an item may be in Forme change")
            .defineInRange("formeTimer", 60, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FORME_PLAYER_COOLDOWN = BUILDER
            .comment("The amount of time in ticks the player can Forme change (global cooldown)")
            .defineInRange("formePlayerCooldown", 60, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue MULTIPLIER = BUILDER
            .comment("The multiplier for attack damage")
            .defineInRange("multiplier", 0.2, 0.0, Double.MAX_VALUE);

//    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
//            .comment("What you want the introduction message to be for the magic number")
//            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
//    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
//            .comment("A list of items to log on common setup.")
//            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);
    private static final ModConfigSpec.ConfigValue<String> BINDINGS = BUILDER
            .comment("String to be transformed into JSON by Gson to store information about what Forme applies to what list of weapons").define("bindings", "{}");

//            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int formeTimer;
    public static int formePlayerCooldown;
    public static double multiplier;
    public static JsonObject bindings;
    public static HashMap<String, Item[]> bindingsHashMap = HashMap.newHashMap(3);
//    public static Set<Item> items;

//    private static boolean validateItemName(final Object obj) {
//        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
//    }

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
            //Search all items matching the regex
//            for(Item item: BuiltInRegistries.ITEM){
//                Matcher m = pattern.matcher(item.toString());
//                if(m.find()){
//                    System.out.println(item);
//                }
//            }
            Item[] allMatchesArray = BuiltInRegistries.ITEM.stream().filter((item) -> pattern.matcher(item.toString()).find()).toArray(Item[]::new);
            bindingsHashMap.put(entry.getKey(), allMatchesArray);
        }

        // convert the list of strings into a set of items
//        items = BINDINGS.get().stream()
//                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
//                .collect(Collectors.toSet());
    }
}
