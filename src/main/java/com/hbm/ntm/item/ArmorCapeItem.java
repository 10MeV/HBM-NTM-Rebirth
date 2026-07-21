package com.hbm.ntm.item;

import com.hbm.ntm.client.renderer.LegacyArmorCapeRenderer;
import java.util.function.Consumer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Source-shaped carrier for the four legacy-equipped cape chest items. */
public class ArmorCapeItem extends ArmorItem {
    public ArmorCapeItem(ArmorMaterial material, Properties properties) {
        super(material, Type.CHESTPLATE, properties.stacksTo(1));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyArmorCapeRenderer.acceptExtensions(consumer);
    }
}
