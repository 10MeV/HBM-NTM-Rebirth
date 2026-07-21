package com.hbm.items.armor;
import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;
/** Legacy carrier; behavior remains in {@link ArmorModItems.Shield}. */
@Deprecated(forRemoval = false)
public class ItemModShield extends ArmorModItems.Shield { public ItemModShield(float shield) { super(new Item.Properties(), shield); } }
