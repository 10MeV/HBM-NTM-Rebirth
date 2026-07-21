package com.hbm.items.armor;
import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;
/** Legacy carrier; capacity behavior remains in {@link ArmorModItems.ArmorBattery}. */
@Deprecated(forRemoval = false)
public class ItemModBattery extends ArmorModItems.ArmorBattery { public ItemModBattery(double multiplier) { super(new Item.Properties(), multiplier); } }
