package com.hbm.items.armor;
import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;
/** Legacy carrier; behavior remains in {@link ArmorModItems.Bathwater}. */
@Deprecated(forRemoval = false)
public class ItemModBathwater extends ArmorModItems.Bathwater { public ItemModBathwater(boolean wither) { super(new Item.Properties(), wither); } }
