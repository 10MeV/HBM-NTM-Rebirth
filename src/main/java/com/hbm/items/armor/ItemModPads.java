package com.hbm.items.armor;
import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;
/** Legacy carrier; fall-damage behavior remains in {@link ArmorModItems.Pads}. */
@Deprecated(forRemoval = false)
public class ItemModPads extends ArmorModItems.Pads { public ItemModPads(float damageModifier, boolean staticCharge) { super(new Item.Properties(), damageModifier, staticCharge); } }
