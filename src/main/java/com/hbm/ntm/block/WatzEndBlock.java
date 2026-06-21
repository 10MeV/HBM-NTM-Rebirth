package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WatzEndBlock extends Block implements Toolable {
    public static final BooleanProperty RIVETED = BooleanProperty.create("riveted");
    private static final TagKey<Item> DURA_BOLTS = ItemTags.create(new ResourceLocation("forge", "bolts/dura_steel"));
    private static final int BOLT_COST = 4;

    public WatzEndBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RIVETED, false));
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        BlockState state = level.getBlockState(pos);
        if (tool != ToolType.BOLT || !state.is(this) || state.getValue(RIVETED)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        if (!consumeBolts(player)) {
            return false;
        }
        level.setBlock(pos, state.setValue(RIVETED, true), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 1.25F);
        return true;
    }

    private static boolean consumeBolts(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        Inventory inventory = player.getInventory();
        if (countBolts(inventory.items) + countBolts(inventory.offhand) < BOLT_COST) {
            return false;
        }
        int remaining = BOLT_COST;
        remaining = consumeBolts(inventory.items, remaining);
        consumeBolts(inventory.offhand, remaining);
        return true;
    }

    private static int countBolts(List<ItemStack> list) {
        int count = 0;
        for (ItemStack stack : list) {
            if (stack.is(DURA_BOLTS)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int consumeBolts(List<ItemStack> list, int remaining) {
        for (ItemStack stack : list) {
            if (remaining <= 0) {
                return 0;
            }
            if (!stack.is(DURA_BOLTS)) {
                continue;
            }
            int used = Math.min(remaining, stack.getCount());
            stack.shrink(used);
            remaining -= used;
        }
        return remaining;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RIVETED);
    }
}
