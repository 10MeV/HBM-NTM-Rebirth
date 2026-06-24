package com.hbm.ntm.block;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.Toolable.ToolType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

final class LegacyToolConversionOverlay {
    static LegacyLookOverlay forTool(BlockState state, Player player, ToolType requiredTool, List<ItemCost> costs) {
        if (player == null || ToolType.getType(player.getMainHandItem()) != requiredTool || costs == null
                || costs.isEmpty()) {
            return null;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Requires:").withStyle(ChatFormatting.GOLD));
        lines.add(Component.literal("- ").withStyle(ChatFormatting.BLUE)
                .append(displayTool(player, requiredTool).getHoverName().copy().withStyle(ChatFormatting.BLUE)));
        for (ItemCost cost : costs) {
            lines.add(materialLine(cost));
        }
        return LegacyLookOverlay.forBlockState(state, lines);
    }

    private static ItemStack displayTool(Player player, ToolType tool) {
        List<ItemStack> tools = tool.stacksForDisplay();
        if (tools.isEmpty()) {
            return player.getMainHandItem();
        }
        int index = (int) (Math.abs(System.currentTimeMillis() / 1000L) % tools.size());
        return tools.get(index);
    }

    private static Component materialLine(ItemCost cost) {
        ItemStack stack = firstStackFor(cost.tag(), cost.count());
        if (!stack.isEmpty()) {
            return Component.literal("- ")
                    .append(stack.getHoverName().copy().withStyle(ChatFormatting.RESET))
                    .append(Component.literal(" x" + cost.count()).withStyle(ChatFormatting.RESET));
        }
        return Component.literal("- " + cost.tag().location() + " x" + cost.count()).withStyle(ChatFormatting.RED);
    }

    private static ItemStack firstStackFor(TagKey<Item> tag, int count) {
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ItemStack stack = new ItemStack(item, Math.max(1, count));
            if (stack.is(tag)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    record ItemCost(TagKey<Item> tag, int count) {
    }

    private LegacyToolConversionOverlay() {
    }
}
