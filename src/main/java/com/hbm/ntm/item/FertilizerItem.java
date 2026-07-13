package com.hbm.ntm.item;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.UUID;

/** Legacy industrial fertilizer: applies bonemeal across a 3x3x3 volume. */
public class FertilizerItem extends Item {
    private static final GameProfile DISPENSER_PROFILE = new GameProfile(
            UUID.fromString("c3d99e69-bc3b-42a9-aa04-ae9428ad9859"), "[HBM Fertilizer]");
    private static final DefaultDispenseItemBehavior DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private boolean succeeded;

        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
            succeeded = useFertilizer(stack, source.getLevel(), source.getPos().relative(facing));
            return stack;
        }

        @Override
        protected void playSound(BlockSource source) {
            source.getLevel().levelEvent(succeeded ? 1000 : 1001, source.getPos(), 0);
        }
    };

    public FertilizerItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, DISPENSE_BEHAVIOR);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }

        if (!context.getLevel().isClientSide) {
            boolean didSomething = fertilizeArea(context.getLevel(), context.getClickedPos(), player,
                    context.getItemInHand());
            if (didSomething && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        // The old onItemUse deliberately returned false after applying fertilizer.
        return InteractionResult.PASS;
    }

    public static boolean useFertilizer(ItemStack stack, Level level, BlockPos center) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        FakePlayer player = FakePlayerFactory.get(serverLevel, DISPENSER_PROFILE);
        boolean didSomething = fertilizeArea(serverLevel, center, player, stack);
        if (didSomething) {
            stack.shrink(1);
        }
        return didSomething;
    }

    public static boolean fertilizeArea(Level level, BlockPos center, Player player) {
        return fertilizeArea(level, center, player, ItemStack.EMPTY);
    }

    public static boolean fertilizeArea(Level level, BlockPos center, Player player, ItemStack fertilizerStack) {
        boolean didSomething = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            boolean success = fertilize(level, pos, player, fertilizerStack, pos.equals(center));
            didSomething |= success;
            if (success && !level.isClientSide) {
                level.levelEvent(2005, pos, 0);
            }
        }
        return didSomething;
    }

    public static boolean fertilize(Level level, BlockPos pos, Player player, boolean force) {
        return fertilize(level, pos, player, ItemStack.EMPTY, force);
    }

    public static boolean fertilize(Level level, BlockPos pos, Player player, ItemStack fertilizerStack, boolean force) {
        BlockState state = level.getBlockState(pos);
        BonemealEvent event = new BonemealEvent(player, level, pos, state, fertilizerStack);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return false;
        }
        if (event.getResult() == Event.Result.ALLOW) {
            return true;
        }
        if (!(state.getBlock() instanceof BonemealableBlock growable)
                || !growable.isValidBonemealTarget(level, pos, state, level.isClientSide)) {
            return false;
        }
        if (!level.isClientSide && (force || growable.isBonemealSuccess(level, level.random, pos, state))) {
            growable.performBonemeal((ServerLevel) level, level.random, pos, state);
        }
        return true;
    }
}
