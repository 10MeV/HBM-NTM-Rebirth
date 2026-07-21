package com.hbm.items.armor;
import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;
/** Legacy carrier; broadcaster handling remains in {@link ArmorModItems.Charm}. */
@Deprecated(forRemoval = false)
public class ItemModCharm extends ArmorModItems.Charm { public ItemModCharm() { this(false); } public ItemModCharm(boolean negateBroadcastDamage) { super(new Item.Properties(), negateBroadcastDamage); } }
