package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class LegacyPeasItem extends Item {
    private static final double QUACKOS_RANGE = 50.0D;

    public LegacyPeasItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            AABB box = player.getBoundingBox().inflate(QUACKOS_RANGE, QUACKOS_RANGE, QUACKOS_RANGE);
            for (Entity entity : level.getEntities(player, box, LegacyPeasItem::isLegacyQuackos)) {
                entity.discard();
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".desc"));
    }

    private static boolean isLegacyQuackos(Entity entity) {
        return hasLegacyClassName(entity, "EntityQuackos")
                || hasLegacyClassName(entity, "com.hbm.entity.mob.EntityQuackos");
    }

    private static boolean hasLegacyClassName(Entity entity, String legacyName) {
        boolean fullName = legacyName.indexOf('.') >= 0;
        Class<?> type = entity.getClass();
        while (type != null) {
            String typeName = type.getName();
            if (fullName ? typeName.equals(legacyName)
                    : type.getSimpleName().equals(legacyName) || typeName.endsWith("." + legacyName)) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }
}
