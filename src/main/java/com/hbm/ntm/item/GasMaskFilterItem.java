package com.hbm.ntm.item;

import com.hbm.ntm.api.item.GasMask;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GasMaskFilterItem extends Item {
    public GasMaskFilterItem(Properties properties) {
        super(properties.stacksTo(1).durability(20_000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack filter = player.getItemInHand(hand);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) {
            return InteractionResultHolder.pass(filter);
        }

        ItemStack maskStack = helmet;
        boolean attachedMask = false;
        if (!(maskStack.getItem() instanceof GasMask) && ArmorModHandler.hasMods(helmet)) {
            ItemStack mod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only);
            if (!mod.isEmpty() && mod.getItem() instanceof GasMask) {
                maskStack = mod;
                attachedMask = true;
            }
        }

        if (!(maskStack.getItem() instanceof GasMask mask) || !mask.isFilterApplicable(maskStack, player, filter)) {
            return InteractionResultHolder.pass(filter);
        }

        ItemStack replacement = installFilter(maskStack, mask, filter, player);
        if (attachedMask) {
            ArmorModHandler.applyMod(helmet, maskStack);
        }
        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), ModSounds.TOOL_GASMASK_SCREW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(replacement, level.isClientSide);
    }

    private static ItemStack installFilter(ItemStack maskStack, GasMask mask, ItemStack heldFilter, Player player) {
        ItemStack installed = heldFilter.copyWithCount(1);
        ItemStack current = mask.getFilter(maskStack, player);
        mask.installFilter(maskStack, player, installed);
        if (!current.isEmpty()) {
            return current;
        }
        if (player.getAbilities().instabuild) {
            return heldFilter;
        }
        heldFilter.shrink(1);
        return heldFilter;
    }
}
