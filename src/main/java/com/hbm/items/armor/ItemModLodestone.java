package com.hbm.items.armor;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ArmorModHandler;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy package facade for the 1.7.10 lodestone armor module item.
 */
@Deprecated(forRemoval = false)
public class ItemModLodestone extends ItemArmorMod {
    public final int range;

    public ItemModLodestone(int range) {
        super(ArmorModHandler.extra, true, true, true, true);
        this.range = range;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Attracts nearby items").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Item attraction range: " + range).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.empty());
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(Component.literal("  ")
                .append(stack.getHoverName())
                .append(Component.literal(" (Magnetic range: " + range + ")"))
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {
        if (entity instanceof Player player && !HbmPlayerProps.getData(player).isMagnetActive()) {
            return;
        }

        List<ItemEntity> items = entity.level().getEntitiesOfClass(ItemEntity.class,
                entity.getBoundingBox().inflate(range, range, range));
        for (ItemEntity item : items) {
            Vec3 pull = entity.position().subtract(item.position()).normalize();
            Vec3 motion = item.getDeltaMovement().add(pull.scale(0.05D));
            if (pull.y > 0.0D && motion.y < 0.04D) {
                motion = motion.add(0.0D, 0.2D, 0.0D);
            }
            item.setDeltaMovement(motion);
            item.hasImpulse = true;
        }
    }
}
