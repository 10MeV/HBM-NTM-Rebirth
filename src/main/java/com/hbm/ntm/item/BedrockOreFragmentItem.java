package com.hbm.ntm.item;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BedrockOreFragmentItem extends Item {
    public static final ResourceLocation VARIANT_PROPERTY =
            new ResourceLocation("hbm_ntm_rebirth", "bedrock_ore_fragment_variant");
    private static final String TAG_TYPE = "type";
    private static final String TAG_MATERIAL = "mat";
    private static final String TAG_MATERIAL_NAME = "name";

    public BedrockOreFragmentItem(Properties properties) {
        super(properties);
    }

    public static ItemStack make(BedrockOreType type, int amount) {
        ItemStack stack = new ItemStack(ModItems.BEDROCK_ORE_FRAGMENT.get(), amount);
        stack.getOrCreateTag().putString(TAG_TYPE, (type == null ? BedrockOreType.LIGHT_METAL : type).suffix());
        return stack;
    }

    public static ItemStack make(NTMMaterial material, int amount) {
        ItemStack stack = new ItemStack(ModItems.BEDROCK_ORE_FRAGMENT.get(), amount);
        setMaterial(stack, material);
        return stack;
    }

    public static void setMaterial(ItemStack stack, NTMMaterial material) {
        if (stack == null || stack.isEmpty() || material == null) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_MATERIAL, material.id);
        tag.putString(TAG_MATERIAL_NAME, material.names[0]);
    }

    public static NTMMaterial getMaterial(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        NTMMaterial material = tag.contains(TAG_MATERIAL) ? Mats.matById.get(tag.getInt(TAG_MATERIAL)) : null;
        if (material == null && tag.contains(TAG_MATERIAL_NAME)) {
            material = Mats.matByName.get(tag.getString(TAG_MATERIAL_NAME));
        }
        return material;
    }

    public static MaterialStack getMaterialStack(ItemStack stack) {
        NTMMaterial material = getMaterial(stack);
        return material == null ? null : new MaterialStack(material, MaterialShapes.FRAGMENT.q(stack.getCount()));
    }

    public static List<NTMMaterial> fragmentMaterials() {
        return Mats.orderedList.stream()
                .filter(material -> material.autogen.contains(MaterialShapes.FRAGMENT))
                .toList();
    }

    public static float modelVariant(ItemStack stack) {
        NTMMaterial material = getMaterial(stack);
        if (material == null) {
            return 0.0F;
        }
        int index = fragmentMaterials().indexOf(material);
        return index < 0 ? 0.0F : index + 1.0F;
    }

    public static void addCreativeStacks(CreativeModeTab.Output output) {
        for (NTMMaterial material : fragmentMaterials()) {
            output.accept(make(material, 1));
        }
    }

    public static BedrockOreType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? BedrockOreType.LIGHT_METAL : BedrockOreType.bySuffix(tag.getString(TAG_TYPE));
    }

    public static int tint(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) {
            return 0xFFFFFF;
        }
        NTMMaterial material = getMaterial(stack);
        // Bismuth uses the source-backed explicit texture override.  Other
        // materials use the shared fragment sprite with the material's solid
        // color, matching ItemAutogen's generated fragment icon contract.
        return material == null || material == Mats.MAT_BISMUTH
                ? (material == null ? getType(stack).lightColor() : 0xFFFFFF)
                : material.solidColorLight;
    }

    @Override
    public Component getName(ItemStack stack) {
        NTMMaterial material = getMaterial(stack);
        if (material != null) {
            return Component.translatable("item.hbm_ntm_rebirth.bedrock_ore_fragment",
                    Component.translatableWithFallback(material.getUnlocalizedName(), material.names[0]));
        }
        return Component.translatable("item.hbm_ntm_rebirth.bedrock_ore_fragment",
                Component.translatable(getType(stack).translationKey()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        NTMMaterial material = getMaterial(stack);
        if (material != null) {
            tooltip.add(Component.translatableWithFallback(material.getUnlocalizedName(), material.names[0]));
            return;
        }
        tooltip.add(Component.translatable(getType(stack).translationKey()));
    }
}
