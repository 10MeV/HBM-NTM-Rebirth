package com.hbm.ntm.item;

import com.hbm.ntm.energy.IBatteryItem;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LegacyPancakeItem extends Item {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    public LegacyPancakeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (canEatPancake(player)) {
            return super.use(level, player, hand);
        }

        if (!level.isClientSide) {
            player.displayClientMessage(Component.literal("Your teeth are too soft to eat this.")
                    .withStyle(ChatFormatting.YELLOW), false);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            chargeWornBatteries(player);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Can be eaten to recharge lunar cybernetic armor"));
        tooltip.add(Component.literal("Not for people with weak molars"));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Half burnt and smells horrible"));
    }

    private static boolean canEatPancake(Player player) {
        return FsbArmorItem.hasFullFsbSet(player, true)
                && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.BJ_HELMET.get());
    }

    private static void chargeWornBatteries(Player player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.getItem() instanceof IBatteryItem battery) {
                battery.setCharge(armor, battery.getMaxCharge(armor));
            }
        }
    }
}
