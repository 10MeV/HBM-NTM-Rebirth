package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.HbmInventoryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The source-backed {@code ItemStarterKit#missile_kit} branch.  Keep this
 * separate from the legacy catch-all starter-kit class: most of that class is
 * unrelated to missiles or is excluded from the modern migration.
 */
public class MissileStarterKitItem extends Item {
    public MissileStarterKitItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        giveContents(player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_UNPACK.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getInventory().setChanged();
        stack.shrink(1);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Please empty inventory before opening!"));
    }

    private static void giveContents(Player player) {
        give(player, new ItemStack(ModBlocks.LAUNCH_PAD.get()));
        give(player, new ItemStack(ModItems.DESIGNATOR.get()));
        give(player, new ItemStack(ModItems.DESIGNATOR_RANGE.get()));
        give(player, new ItemStack(ModItems.DESIGNATOR_MANUAL.get()));
        give(player, new ItemStack(ModItems.MISSILE_GENERIC.get()));
        give(player, new ItemStack(ModItems.MISSILE_STRONG.get()));
        give(player, new ItemStack(ModItems.MISSILE_BURST.get()));
        give(player, new ItemStack(ModItems.MISSILE_INCENDIARY.get()));
        give(player, new ItemStack(ModItems.MISSILE_INCENDIARY_STRONG.get()));
        give(player, new ItemStack(ModItems.MISSILE_INFERNO.get()));
        give(player, new ItemStack(ModItems.MISSILE_CLUSTER.get()));
        give(player, new ItemStack(ModItems.MISSILE_CLUSTER_STRONG.get()));
        give(player, new ItemStack(ModItems.MISSILE_RAIN.get()));
        give(player, new ItemStack(ModItems.MISSILE_BUSTER.get()));
        give(player, new ItemStack(ModItems.MISSILE_BUSTER_STRONG.get()));
        give(player, new ItemStack(ModItems.MISSILE_DRILL.get()));
        give(player, new ItemStack(ModItems.MISSILE_NUCLEAR.get()));
        give(player, new ItemStack(ModItems.MISSILE_NUCLEAR_CLUSTER.get()));
        give(player, new ItemStack(ModItems.MISSILE_VOLCANO.get()));
        give(player, new ItemStack(ModItems.MISSILE_DOOMSDAY.get()));
        give(player, new ItemStack(ModItems.MISSILE_TAINT.get()));
        give(player, new ItemStack(ModItems.MISSILE_MICRO.get()));
        give(player, new ItemStack(ModItems.MISSILE_BHOLE.get()));
        give(player, new ItemStack(ModItems.MISSILE_SCHRABIDIUM.get()));
        give(player, new ItemStack(ModItems.MISSILE_EMP.get()));
    }

    private static void give(Player player, ItemStack stack) {
        ItemStack remainder = HbmInventoryUtil.tryAddItemToInventory(player.getInventory(), stack);
        if (!remainder.isEmpty()) {
            player.drop(remainder, false);
        }
    }
}
