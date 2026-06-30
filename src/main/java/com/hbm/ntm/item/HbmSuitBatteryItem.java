package com.hbm.ntm.item;

import com.hbm.ntm.armor.FsbPoweredArmor;
import com.hbm.ntm.energy.IBatteryItem;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.HbmTextUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class HbmSuitBatteryItem extends Item {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private final long charge;

    public HbmSuitBatteryItem(Properties properties, long charge) {
        super(properties);
        this.charge = Math.max(0L, charge);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!FsbPoweredArmor.hasFullPoweredSetIgnoreCharge(player) || !(chest.getItem() instanceof FsbPoweredArmor)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack armor = player.getItemBySlot(slot);
                if (armor.getItem() instanceof IBatteryItem battery) {
                    long maxCharge = battery.getMaxCharge(armor);
                    long currentCharge = battery.getCharge(armor);
                    battery.setCharge(armor, Math.min(currentCharge + charge, maxCharge));
                }
            }

            stack.shrink(1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_BATTERY.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Charges all worn armor pieces by " + HbmTextUtil.shortNumber(charge) + "HE")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("[Requires full electric set to be worn]"));
    }
}
