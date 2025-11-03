package net.anormalraft.toolforme;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //These IntValues and DoubleValues have their defaults added by Neoforge in the config file
    public static final ModConfigSpec.IntValue FORME_TIMER = BUILDER
            .comment("The timer bound to items which determines the amount of time in ticks an item may be in Forme change")
            .defineInRange("formeTimer", 1800, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue FORME_PLAYER_COOLDOWN = BUILDER
            .comment("The amount of time in ticks the player can Forme change (global cooldown). Shouldn't be lower than or equal to the formeTimer")
            .defineInRange("formePlayerCooldown", 3200, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue MULTIPLIER = BUILDER
            .comment("The multiplier for attack damage. An input of 1.25 means that the weapon will deal its original damage (1) + 25% of its original damage, resulting in a 125% damage output")
            .defineInRange("multiplier", 1.25, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> BINDINGS = BUILDER
            .comment("String that stores information about what Forme (Item id) is applied to what items (RegEx) (You can use an online regex tester to find out if your regex matches your desired item ids). Follow the syntax of the default value closely. \nDefault: \"{\\\"minecraft:trident\\\": \\\"shovel$\\\", \\\"minecraft:mace\\\": \\\"_axe$\\\"}\"")
            .define("bindings", "{\"minecraft:trident\": \"shovel$\", \"minecraft:mace\": \"_axe$\"}");

    public static final ModConfigSpec.BooleanValue SHIELD_CROUCH = BUILDER
            .comment("Should the shield be only activated on crouch instead of right click. Will disable right click for the shield. \nDefault: false")
            .define("shieldCrouch", false);

    public static final ModConfigSpec.BooleanValue TRIDENT_RIPTIDE_FIX_IF_DATAPACK = BUILDER
            .comment("Can both loyalty and riptide be used on an item if a datapack allowing loyalty and riptide to not be incompatible exists (through modifying riptide enchantment components). \nDefault: false")
            .define("tridentRiptideFixIfDatapack", false);

    public static final ModConfigSpec.BooleanValue PLAYER_RESET_ON_DEATH = BUILDER
            .comment("Should the item and player's timers be reset upon death (also reverting the Forme item if any)? SHOULD REMAIN TRUE FOR NON-KEEPINVENTORY SETUPS (Ex: remain true if the Gravestones mod is present). \nDefault: true")
            .define("playerResetOnDeath", true);

    public static final ModConfigSpec.ConfigValue<String> LOYALTY_COOLDOWNS = BUILDER
            .comment("String listing the cooldown timings in ticks (20 ticks = 1 second) that each level of loyalty should incur on a Forme Trident when thrown. The first position is for a trident without loyalty. \nDefault: \"140,80,60,40\"")
            .define("loyaltyCooldowns", "140,80,60,40");

    public static final ModConfigSpec SPEC = BUILDER.build();
}
