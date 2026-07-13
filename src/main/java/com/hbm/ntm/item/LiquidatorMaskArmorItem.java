package com.hbm.ntm.item;

import com.google.common.collect.Multimap;
import api.hbm.item.IGasMask;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.ntm.client.renderer.LegacyHeadArmorRenderer;
import com.hbm.ntm.radiation.ArmorUtil;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class LiquidatorMaskArmorItem extends ArmorFSB implements IGasMask {
    public LiquidatorMaskArmorItem(Properties properties) {
        super(HbmArmorMaterials.LIQUIDATOR, Type.HELMET, properties, List.of(), false, 0,
                LiquidatorArmorItem.LIQUIDATOR_TRAITS);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.HEAD) {
            return super.getDefaultAttributeModifiers(slot);
        }
        return LiquidatorArmorItem.withLiquidatorModifiers(super.getDefaultAttributeModifiers(slot), getType());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyHeadArmorRenderer.acceptExtensions(consumer);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ArmorUtil.addGasMaskTooltip(stack, null, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            ItemStack filter = getFilter(stack, player);
            if (!filter.isEmpty()) {
                if (!level.isClientSide) {
                    ArmorUtil.removeGasMaskFilterToInventory(stack, player);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }
        return super.use(level, player, hand);
    }
}
