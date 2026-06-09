package com.hbm.ntm.item;

import com.hbm.ntm.api.item.GasMask;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.radiation.ArmorUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class GasMaskArmorItem extends ArmorItem implements GasMask {
    private final List<HazardClass> blacklist;

    public GasMaskArmorItem(ArmorMaterial material, Properties properties, boolean mono) {
        this(material, properties, mono
                ? List.of(HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING, HazardClass.BACTERIA)
                : List.of(HazardClass.GAS_BLISTERING));
    }

    public GasMaskArmorItem(ArmorMaterial material, Properties properties, List<HazardClass> blacklist) {
        super(material, Type.HELMET, properties);
        this.blacklist = List.copyOf(blacklist);
    }

    @Override
    public List<HazardClass> getBlacklist(ItemStack stack, LivingEntity entity) {
        return blacklist;
    }

    @Override
    public ItemStack getFilter(ItemStack stack, LivingEntity entity) {
        return ArmorUtil.getGasMaskFilter(stack);
    }

    @Override
    public boolean isFilterApplicable(ItemStack stack, LivingEntity entity, ItemStack filter) {
        return true;
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ArmorUtil.addGasMaskTooltip(stack, null, tooltip, flag);
        ArmorUtil.addGasMaskBlacklistTooltip(stack, null, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            ItemStack filter = getFilter(stack, player);
            if (!filter.isEmpty()) {
                if (!level.isClientSide) {
                    ArmorUtil.removeFilter(stack);
                    if (!player.getInventory().add(filter)) {
                        player.drop(filter, true);
                    }
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        }
        return super.use(level, player, hand);
    }
}
