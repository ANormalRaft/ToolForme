package net.anormalraft.toolforme;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue FORME_TIMER = BUILDER
            .comment("The timer bound to items which determines the amount of time in ticks an item may be in Forme change. Default: 1800")
            .defineInRange("formeTimer", 1800, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue FORME_PLAYER_COOLDOWN = BUILDER
            .comment("The amount of time in ticks the player can Forme change (global cooldown). Default: 3200")
            .defineInRange("formePlayerCooldown", 3200, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue MULTIPLIER = BUILDER
            .comment("The multiplier for attack damage. An input of 1.25 means that the weapon will deal its original damage (1) + 25% of its original damage, resulting in a 125% damage output. Default: 1.25")
            .defineInRange("multiplier", 1.25, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> BINDINGS = BUILDER
            .comment("String that stores information about what Forme (Item id) is applied to what items (RegEx) (You can use an online regex tester to find out if your regex matches your desired item ids). Follow the syntax of the default value closely. Default: \"\\\"minecraft:trident\\\": \\\"shovel$\\\", \\\"minecraft:mace\\\": \\\"_axe$\\\"\"")
            .define("bindings", "\"minecraft:trident\": \"shovel$\", \"minecraft:mace\": \"_axe$\"");

    public static final ModConfigSpec.BooleanValue SHIELD_CROUCH = BUILDER
            .comment("Should the shield be only activated on crouch instead of right click. Will disable right click for the shield. Default: false")
            .define("shieldCrouch", false);

    public static final ModConfigSpec.BooleanValue TRIDENT_RIPTIDE_FIX_IF_DATAPACK = BUILDER
            .comment("Can both loyalty and riptide be used on an item if a datapack allowing loyalty and riptide to not be incompatible exists (through modifying riptide enchantment components). Default: false")
            .define("tridentRiptideFixIfDatapack", false);

    public static final ModConfigSpec.BooleanValue PLAYER_RESET_ON_DEATH = BUILDER
            .comment("Should the item and player's timers be reset upon death (also reverting the Forme item if any)? HIGHLY RECOMMENDED TO BE TRUE FOR NON-KEEPINVENTORY SETUPS (UNTESTED WITH GRAVESTONES!). Default: true")
            .define("playerResetOnDeath", true);

    public static final ModConfigSpec.ConfigValue<String> LOYALTY_COOLDOWNS = BUILDER
            .comment("String listing the cooldown timings in ticks (20 ticks = 1 second) that each level of loyalty should incur on a Forme Trident. The first position is for a trident without loyalty")
            .define("loyaltyCooldowns", "140,80,60,40");

    public static final ModConfigSpec SPEC = BUILDER.build();
}
