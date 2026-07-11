package com.hbm.items.armor;

import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.item.GasMaskArmorItem;
import com.hbm.util.ArmorUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Legacy package facade for the 1.7.10 full gas-mask armor item.
 */
@Deprecated(forRemoval = false)
public class ArmorGasMask extends GasMaskArmorItem {
    private final boolean mono;

    public ArmorGasMask() {
        this(false);
    }

    public ArmorGasMask(boolean mono) {
        super(ArmorMaterials.IRON, new Properties(), mono);
        this.mono = mono;
    }

    @Override
    public ArrayList<HazardClass> getBlacklist(ItemStack stack, LivingEntity entity) {
        if (isMono(stack)) {
            return new ArrayList<>(List.of(HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING,
                    HazardClass.BACTERIA));
        }
        return new ArrayList<>(List.of(HazardClass.GAS_BLISTERING));
    }

    @Override
    public ItemStack getFilter(ItemStack stack, LivingEntity entity) {
        return ArmorUtil.getGasMaskFilter(stack);
    }

    @Override
    public void installFilter(ItemStack stack, LivingEntity entity, ItemStack filter) {
        ArmorUtil.installGasMaskFilter(stack, filter);
    }

    @Override
    public void damageFilter(ItemStack stack, LivingEntity entity, int damage) {
        ArmorUtil.damageGasMaskFilter(stack, damage);
    }

    @Override
    public boolean isFilterApplicable(ItemStack stack, LivingEntity entity, ItemStack filter) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ArmorUtil.addGasMaskTooltip(stack, null, tooltip, flag);
        addLegacyBlacklistTooltip(stack, null, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            ItemStack filter = getFilter(stack, player);
            if (filter != null && !filter.isEmpty()) {
                if (!level.isClientSide) {
                    ArmorUtil.removeGasMaskFilterToInventory(stack, player);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }
        return super.use(level, player, hand);
    }

    private boolean isMono(ItemStack stack) {
        if (mono) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && "gas_mask_mono".equals(id.getPath());
    }

    private void addLegacyBlacklistTooltip(ItemStack stack, @Nullable LivingEntity entity,
                                           List<Component> tooltip) {
        List<HazardClass> blacklist = getBlacklist(stack, entity);
        if (blacklist.isEmpty()) {
            return;
        }
        tooltip.add(Component.translatable("hazard.neverProtects").withStyle(ChatFormatting.RED));
        for (HazardClass hazardClass : blacklist) {
            tooltip.add(Component.literal(" -")
                    .append(Component.translatable(hazardClass.translationKey()))
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }
}
