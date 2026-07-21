package com.hbm.items.armor;
import com.hbm.ntm.armor.ArmorModItems;
import net.minecraft.world.item.Item;
/** Legacy carrier; dash behavior remains in {@link ArmorModItems.BottledCloud}. */
@Deprecated(forRemoval = false)
public class ItemModCloud extends ArmorModItems.BottledCloud implements com.hbm.interfaces.IArmorModDash {
    public ItemModCloud() {
        super(new Item.Properties());
    }
}
