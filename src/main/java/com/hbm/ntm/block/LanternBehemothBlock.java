package com.hbm.ntm.block;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.LanternBehemothBlockEntity;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** The hidden worldgen-only, repairable old-lantern variant. */
public class LanternBehemothBlock extends LegacyLanternBlock implements Toolable, LegacyLookOverlayBlockProvider {
    private static final TagKey<Item> STEEL_PLATES = ItemTags.create(new ResourceLocation("forge", "plates/steel"));

    public LanternBehemothBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(SEGMENT) == 0 ? new LanternBehemothBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || state.getValue(SEGMENT) != 0) return null;
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            if (blockEntity instanceof LanternBehemothBlockEntity behemoth) {
                LanternBehemothBlockEntity.serverTick(tickLevel, tickPos, tickState, behemoth);
            }
        };
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return List.of();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        return tool == ToolType.TORCH && onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)
                ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.TORCH || !(level.getBlockEntity(basePos(pos, level.getBlockState(pos)))
                instanceof LanternBehemothBlockEntity lantern) || !lantern.isBroken()) return false;
        if (level.isClientSide) return true;
        if (!consumeRepairMaterials(player)) return false;
        lantern.repair();
        if (HbmPlayerProperties.getReputation(player) < 25) HbmPlayerProperties.incrementReputation(player);
        return true;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState) {
        return null;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, Player player, BlockPos viewedPos, BlockState viewedState) {
        if (!viewedState.is(this) || ToolType.getType(player.getMainHandItem()) != ToolType.TORCH
                || !(level.getBlockEntity(basePos(viewedPos, viewedState)) instanceof LanternBehemothBlockEntity lantern)
                || !lantern.isBroken()) return null;
        ItemStack plate = new ItemStack(ModItems.legacyItem("plate_steel").get());
        ItemStack circuit = new ItemStack(ModItems.legacyItem("circuit_basic").get());
        return LegacyLookOverlay.forBlockState(viewedState, List.of(
                Component.literal("Repair with:").withStyle(ChatFormatting.GOLD),
                Component.literal("- ").append(plate.getHoverName()).append(" x2"),
                Component.literal("- ").append(circuit.getHoverName()).append(" x1")));
    }

    private static BlockPos basePos(BlockPos pos, BlockState state) {
        return pos.below(state.getValue(SEGMENT));
    }

    private static boolean consumeRepairMaterials(Player player) {
        if (player.getAbilities().instabuild) return true;
        Inventory inventory = player.getInventory();
        Item circuit = ModItems.legacyItem("circuit_basic").get();
        if (count(inventory.items, STEEL_PLATES) < 2 || count(inventory.items, circuit) < 1) return false;
        consume(inventory.items, STEEL_PLATES, 2);
        consume(inventory.items, circuit, 1);
        return true;
    }

    private static int count(List<ItemStack> stacks, TagKey<Item> tag) {
        int total = 0;
        for (ItemStack stack : stacks) if (stack.is(tag)) total += stack.getCount();
        return total;
    }

    private static int count(List<ItemStack> stacks, Item item) {
        int total = 0;
        for (ItemStack stack : stacks) if (stack.is(item)) total += stack.getCount();
        return total;
    }

    private static void consume(List<ItemStack> stacks, TagKey<Item> tag, int remaining) {
        for (ItemStack stack : stacks) {
            if (remaining <= 0) return;
            if (stack.is(tag)) {
                int used = Math.min(remaining, stack.getCount());
                stack.shrink(used);
                remaining -= used;
            }
        }
    }

    private static void consume(List<ItemStack> stacks, Item item, int remaining) {
        for (ItemStack stack : stacks) {
            if (remaining <= 0) return;
            if (stack.is(item)) {
                int used = Math.min(remaining, stack.getCount());
                stack.shrink(used);
                remaining -= used;
            }
        }
    }
}
