package com.hbm.items.armor;

import com.hbm.ntm.item.HbmArmorMaterials;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Legacy carrier for registered ordinary armor whose material selects its legacy layer texture. */
@Deprecated(forRemoval = false)
public class ModArmor extends ArmorItem {
    public ModArmor(HbmArmorMaterials material, Type type) {
        super(material, type, new Item.Properties());
    }

    /**
     * Preserves the legacy per-item layer split for the two RAGS helmets.  The
     * material alone cannot select a different texture for each mask.
     */
    @Override
    @Nullable
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
        return switch (path) {
            case "mask_rag" -> "hbm_ntm_rebirth:textures/models/armor/rag_damp.png";
            case "mask_piss" -> "hbm_ntm_rebirth:textures/models/armor/rag_piss.png";
            default -> null;
        };
    }
}
