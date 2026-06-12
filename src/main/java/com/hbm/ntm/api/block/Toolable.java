package com.hbm.ntm.api.block;

import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface Toolable {
    boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool);

    enum ToolType {
        SCREWDRIVER,
        HAND_DRILL,
        DEFUSER,
        WRENCH,
        TORCH,
        BOLT;

        private final List<ItemStack> stacksForDisplay = new ArrayList<>();

        public void register(ItemStack stack) {
            if (stack != null && !stack.isEmpty()) {
                stacksForDisplay.add(HbmItemStackUtil.carefulCopyWithSize(stack, 1));
            }
        }

        public List<ItemStack> stacksForDisplay() {
            return Collections.unmodifiableList(stacksForDisplay);
        }

        public static ToolType getType(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return null;
            }
            for (ToolType type : values()) {
                for (ItemStack tool : type.stacksForDisplay) {
                    if (HbmItemStackUtil.doesStackDataMatch(tool, stack)) {
                        return type;
                    }
                }
            }
            return null;
        }
    }
}
