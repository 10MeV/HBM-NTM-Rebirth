package com.hbm.ntm.block;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.block.LegacyToolConversionOverlay.ItemCost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FusionStructureComponentBlock extends Block implements Toolable, LegacyLookOverlayBlockProvider {
    private static final TagKey<Item> STEEL_CAST_PLATES =
            ItemTags.create(new ResourceLocation("forge", "cast_plates/steel"));

    private final Conversion conversion;

    public FusionStructureComponentBlock(Properties properties, Conversion conversion) {
        super(properties);
        this.conversion = conversion;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (conversion.tool() != tool || !level.getBlockState(pos).is(this)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        if (!consumeMaterials(player, conversion.costs())) {
            return false;
        }
        level.setBlock(pos, conversion.result().get().defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, pos, conversion.sound(), SoundSource.BLOCKS, 0.8F, conversion.pitch());
        return true;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState) {
        return null;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, Player player, BlockPos viewedPos, BlockState viewedState) {
        if (!viewedState.is(this)) {
            return null;
        }
        return LegacyToolConversionOverlay.forTool(viewedState, player, conversion.tool(), conversion.costs().stream()
                .map(cost -> new ItemCost(cost.tag(), cost.count()))
                .collect(Collectors.toList()));
    }

    private static boolean consumeMaterials(Player player, List<Cost> costs) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Inventory inventory = player.getInventory();
        for (Cost cost : costs) {
            if (countItems(inventory.items, cost.tag()) + countItems(inventory.offhand, cost.tag()) < cost.count()) {
                return false;
            }
        }
        for (Cost cost : costs) {
            int remaining = consumeItems(inventory.items, cost.tag(), cost.count());
            consumeItems(inventory.offhand, cost.tag(), remaining);
        }
        return true;
    }

    private static int countItems(List<ItemStack> stacks, TagKey<Item> tag) {
        int count = 0;
        for (ItemStack stack : stacks) {
            if (stack.is(tag)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int consumeItems(List<ItemStack> stacks, TagKey<Item> tag, int remaining) {
        for (ItemStack stack : stacks) {
            if (remaining <= 0) {
                return 0;
            }
            if (!stack.is(tag)) {
                continue;
            }
            int used = Math.min(remaining, stack.getCount());
            stack.shrink(used);
            remaining -= used;
        }
        return remaining;
    }

    public record Conversion(ToolType tool, Supplier<Block> result, List<Cost> costs, SoundEvent sound, float pitch) {
        public static Conversion bsccoCoil(Supplier<Block> result) {
            return new Conversion(ToolType.TORCH, result, List.of(new Cost(STEEL_CAST_PLATES, 1)),
                    SoundEvents.FIRECHARGE_USE, 1.0F);
        }
    }

    public record Cost(TagKey<Item> tag, int count) {
    }
}
