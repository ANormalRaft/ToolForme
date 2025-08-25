package net.anormalraft.toolforme;

import java.util.*;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue FORME_TIMER = BUILDER
            .comment("The timer bound to items which determines the amount of time in ticks an item may be in Forme change")
            .defineInRange("formeTimer", 1800, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue FORME_PLAYER_COOLDOWN = BUILDER
            .comment("The amount of time in ticks the player can Forme change (global cooldown)")
            .defineInRange("formePlayerCooldown", 3600, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue MULTIPLIER = BUILDER
            .comment("The multiplier for attack damage")
            .defineInRange("multiplier", 0.25, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> BINDINGS = BUILDER
            .comment("String to be transformed into JSON by Gson to store information about what Forme applies to what list of weapons")
            .define("bindings", "{\"minecraft:trident\": \"shovel$\", \"minecraft:mace\": \"_axe$\"}");

    public static final ModConfigSpec.BooleanValue SHIELD_CROUCH = BUILDER
            .comment("Should the shield be only activated on crouch instead of right click. Default: false")
            .define("shieldCrouch", false);

    public static final ModConfigSpec.BooleanValue TRIDENT_RIPTIDE_FIX_IF_DATAPACK = BUILDER
            .comment("Can both loyalty and riptide be enchanted on an item if a datapack allowing loyalty and riptide to not be incompatible exists (through modifying riptide enchantment components). Default: false")
            .define("tridentRiptideFixIfDatapack", false);

    public static final ModConfigSpec.BooleanValue PLAYER_RESET_ON_DEATH = BUILDER
            .comment("Should the item and player's timers be reset upon death (also reverting the Forme item if any)? HIGHLY RECOMMENDED TO BE TRUE FOR NON-KEEPINVENTORY (DOES NOT INCLUDE GRAVESTONES!) PACKS. Default: true")
            .define("playerResetOnDeath", true);

    public static final ModConfigSpec.ConfigValue<String> LOYALTY_COOLDOWNS = BUILDER
            .comment("String listing the cooldown timings in ticks (20 ticks = 1 second) that each level of loyalty should incur on a Forme Trident. The first number is for a trident without loyalty")
            .define("loyaltyCooldowns", "140,80,60,40");

    public static final ModConfigSpec SPEC = BUILDER.build();
}
