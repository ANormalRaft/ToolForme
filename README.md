# ToolForme
A mod that allows an item to momentarily become another. Inspired by the "empowered state" mechanic present in various video games. Meant to be used with tools/weapons

## What it do tho?
 When the mod's keybind is pressed, if the item in the mainhand can be transformed, it shall change form:

[toolforme_show.webm](https://github.com/user-attachments/assets/5f9b33e3-f53d-4edb-9232-af81cb05b57e)


(Here, the *Item Timer* and *Player Cooldown* values have been vastly reduced from the default for demonstrative purposes)

  A depleting timer called the *Item Timer* will appear behind the transformed item (itself, called the **Forme item**) in the hotbar to indicate when the **Forme Item** will transform back to its base form. 
  
Then, another timer called the *Player Cooldown* will end, allowing for the use of this transformation ability again. Pressing the mod's keybind while the *Player Cooldown* is not ready will display its remaining recharge time on the action bar.

  - The **Forme item** is unbreakable
  - By default, the **Forme item**'s damage attribute is the damage attribute of the base item multiplied by x1.25 (this multiplier is configurable)
  - Any enchantments and custom names on the base item will transfer to the **Forme item** and back
  - The *Item Timer* and the *Player Cooldown* persist after being disconnected (they will be remembered)

## Limitations
- Since the mod is based on having global timers, you cannot have an active **Forme item** that is able to transform further into another item


- You cannot have multiple items transform at the same time, only one will be able to be transformed


- In order to avoid having to extensively search the **Forme item** when the *Item Timer* finishes, and since the mod was made to be useful with only tools/weapons, the mod attempts to lock the **Forme item** to the hotbar slot it was transformed in

**Be warned:** there will 100% be mods that circumvent this (currently, the only such mod that has compat with Toolforme on this matter is Tool Belt). If the **Forme item** cannot be found, the mod makes the player responsible to find any **Forme item** to transform back into its base form; locking their transformation ability in the meantime. If the item disappears, welp let's just hope that an operator is on so they can use `/toolforme playerReset <playername>` :P

**Note:** the default config runs this command when a player dies, so you're only in trouble if the **Forme item** leaves the inventory by any other way 

- Since the mod's Bindings config only handles item IDs without data components, there isn't a way to handle items that rely on data components to be properly uniquely defined; such as modular items

## Configanigans

Item Timer
>The amount of time in ticks that the transformation should last

Player Cooldown
>The amount of time in ticks that the player can activate the transformation ability again. Should always be higher than the Item Timer

Scaling (True by default) 
> Whether or not to apply the mod's intended scaling formula. If false, the damage attribute of the *Forme item* will remain untouched. the Multiplier config will have no effect

Multiplier (Default: x1.25)
>The multiplier used in the scaling formula

Bindings
>These are the specifications of what *Forme item* should be bound to what base items. Two formats exist: Regex and "super specific string that is formatted like a list" (see details in the actual config). Multiple base items can be bound to a *Forme item*

Death Reset (Enabled by default)
>If both the Item Timer and the Player Cooldown should be reset, as well as if the *Forme item* (if any) should be reverted to its base form upon player death

Loyalty Cooldowns
>In order to keep a thrown *Forme item* trident within its slot, I opted to use the technique used by Combat+ (https://modrinth.com/mod/combatplus-core)'s `keepLoyaltyTridents` functionality, but made the thrown trident cooldown configurable

AStages Sealing (False by default)
>If the mod AStages is present and if this is true, all *Formes* will be locked behind their own stage programmatically set as the string "(mod):(item):toolforme_stage"

Riptide fix (Disabled by default)
>Imagine having a trident that allows having Riptide and Loyalty at the same time. Riptide when you're in water, throw that thang when not in water. Unfortunately, I cannot provide the datapack that slaps some sense into the riptide component data to allow it to coexist with loyalty without making it non-disable-able, but I can make riptide tridents throwable :)) (that's what it does)
