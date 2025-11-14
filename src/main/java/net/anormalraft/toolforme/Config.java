package net.anormalraft.toolforme;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //These IntValues and DoubleValues have their defaults added by Neoforge in the config file
    public static final ModConfigSpec.IntValue FORME_ITEM_TIMER = BUILDER
            .comment("The Item Timer, bound to the transformation's duration in ticks (20 ticks = 1 second).")
            .defineInRange("formeItemTimer", 1800, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue FORME_PLAYER_COOLDOWN = BUILDER
            .comment("The Player Cooldown, bound to the amount of time the player has to wait before being able to transform an item again in ticks. Shouldn't be lower than or equal to the formeItemTimer.")
            .defineInRange("formePlayerCooldown", 3200, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue SCALING = BUILDER
            .comment("By default, the damage attribute of the Forme item will be a scaled value from the base item based on the multiplier (below). Should this scaling formula apply? If false, the damage attribute of the Forme item will remain untouched (the multiplier below will not be used). \nDefault: true")
            .define("scaling", true);

    public static final ModConfigSpec.DoubleValue MULTIPLIER = BUILDER
            .comment("The multiplier for the attack damage. An input of 1.25 means that the Forme item will have a damage attribute equal to its base form's damage (100%) + 25% of the base form's damage, resulting in a Forme item with a damage attribute equal to 125% of the base form's damage.")
            .defineInRange("multiplier", 1.25, 0.0, Double.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> BINDINGS = BUILDER
            .comment("String that stores information about what Forme item (Item id) is applied to what base items (either RegEx or strictly formatted list). Follow the syntax of the default value closely. \nDefault:  \"{\\\"minecraft:trident\\\": \\\"shovel$\\\", \\\"minecraft:mace\\\": \\\"[minecraft:wooden_axe, minecraft:stone_axe, minecraft:golden_axe]\\\"}\"")
            .define("bindings", "{\"minecraft:trident\": \"shovel$\", \"minecraft:mace\": \"[minecraft:wooden_axe, minecraft:stone_axe, minecraft:golden_axe]\"}");

    public static final ModConfigSpec.BooleanValue PLAYER_RESET_ON_DEATH = BUILDER
            .comment("Should the Forme item (if any) and both the player's timers be reset upon death? SHOULD REMAIN TRUE FOR NON-KEEPINVENTORY SETUPS (Ex: remain true if the Gravestones mod is present). \nDefault: true")
            .define("playerResetOnDeath", true);

    public static final ModConfigSpec.ConfigValue<String> LOYALTY_COOLDOWNS = BUILDER
            .comment("String listing the cooldown timings in ticks that each level of loyalty should incur on a Forme trident when thrown. The first position is for a trident without loyalty. \nDefault: \"140,80,60,40\"")
            .define("loyaltyCooldowns", "140,80,60,40");

    public static final ModConfigSpec.BooleanValue TRIDENT_RIPTIDE_FIX_IF_DATAPACK = BUILDER
            .comment("Makes it so riptide tridents can be thrown if the player isn't in water. Complements well with a datapack that changes riptide's enchantment data so that loyalty can be applied alongside it. \nDefault: false")
            .define("tridentRiptideFixIfDatapack", false);

    public static final ModConfigSpec.BooleanValue SHIELD_CROUCH = BUILDER
            .comment("Should the shield be only activated on crouch instead of right click. Will disable right click for the shield. \nDefault: false")
            .define("shieldCrouch", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
