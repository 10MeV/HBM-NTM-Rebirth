package com.hbm.ntm.ability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.hbm.ntm.item.HbmAbilityToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record ToolHarvestContext(Level level, BlockPos pos, Player player, ItemStack toolStack,
                                 BlockState state, @Nullable BlockEntity blockEntity, BlockPos dropOrigin,
                                 Map<Enchantment, Integer> originalEnchantments) {
    public static ToolHarvestContext create(Level level, BlockPos pos, Player player, ItemStack toolStack,
                                            BlockState state, @Nullable BlockEntity blockEntity, BlockPos dropOrigin) {
        return new ToolHarvestContext(level, pos, player, toolStack, state, blockEntity, dropOrigin,
                new LinkedHashMap<>(EnchantmentHelper.getEnchantments(toolStack)));
    }

    public boolean harvestBlock(boolean skipDefaultDrops) {
        if (skipDefaultDrops || !(level instanceof ServerLevel serverLevel)) {
            boolean destroyed = level.destroyBlock(pos, !skipDefaultDrops, player);
            if (destroyed && skipDefaultDrops) {
                hurtTool();
            }
            return destroyed;
        }

        List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
                state, serverLevel, pos, blockEntity, player, toolStack);
        boolean destroyed = level.destroyBlock(pos, false, player);
        if (destroyed) {
            drops.forEach(this::drop);
            hurtTool();
        }
        return destroyed;
    }

    public void addTemporaryEnchantment(Enchantment enchantment, int level) {
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(toolStack));
        enchantments.merge(enchantment, level, Math::max);
        EnchantmentHelper.setEnchantments(enchantments, toolStack);
    }

    public void restoreEnchantments() {
        EnchantmentHelper.setEnchantments(originalEnchantments, toolStack);
    }

    private void hurtTool() {
        if (!player.getAbilities().instabuild) {
            if (toolStack.getItem() instanceof HbmAbilityToolItem abilityTool) {
                abilityTool.hurtAbilityTool(toolStack, player);
                return;
            }
            toolStack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
    }

    public void drop(ItemStack stack) {
        if (stack.isEmpty() || !(level instanceof ServerLevel)) {
            return;
        }
        level.addFreshEntity(new ItemEntity(level,
                dropOrigin.getX() + 0.5D,
                dropOrigin.getY() + 0.5D,
                dropOrigin.getZ() + 0.5D,
                stack.copy()));
    }
}
