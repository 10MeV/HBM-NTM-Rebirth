package com.hbm.items.armor;

import com.hbm.ntm.client.renderer.LegacyHeadArmorRenderer;
import com.hbm.ntm.item.ObjArmorItem;
import java.util.function.Consumer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Legacy package carrier for the registered ModelArmor items.
 *
 * <p>The registered goggles path uses the source-shaped head-model runtime.
 * The four legacy cape IDs use {@code ArmorCapeItem}, whose
 * source-shaped cloak model is shared by the client armor renderer.</p>
 */
@Deprecated(forRemoval = false)
public class ArmorModel extends ObjArmorItem {
    public ArmorModel(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyHeadArmorRenderer.acceptExtensions(consumer);
    }
}
