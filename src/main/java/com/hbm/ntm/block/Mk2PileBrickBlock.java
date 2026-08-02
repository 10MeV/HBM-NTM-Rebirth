package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.Mk2PileCoreBlockEntity;
import com.hbm.ntm.blockentity.Mk2PileMemberBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** 1.7.10 BlockPileBrick direct, source-built dynamic-cube formation entry. */
public final class Mk2PileBrickBlock extends Block implements Toolable {
    public static final int MIN_SIZE = 5;
    public static final int MAX_SIZE = 15;

    public Mk2PileBrickBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        if (tool == ToolType.HAND_DRILL && onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos corePos, Direction clickedSide, Vec3 hit,
            ToolType tool) {
        if (tool != ToolType.HAND_DRILL || !clickedSide.getAxis().isHorizontal()) {
            return false;
        }
        if (!level.isClientSide) {
            form(level, corePos, clickedSide.getOpposite());
        }
        return true;
    }

    private static void form(Level level, BlockPos corePos, Direction facing) {
        Direction sideAxis = facing.getCounterClockWise();
        int up = scan(level, corePos, Direction.UP, MAX_SIZE - 1);
        int down = scan(level, corePos, Direction.DOWN, MAX_SIZE - up - 1);
        int left = scan(level, corePos, sideAxis, MAX_SIZE - 1);
        int right = scan(level, corePos, sideAxis.getOpposite(), MAX_SIZE - left - 1);
        int forward = scan(level, corePos, facing, MAX_SIZE);
        int height = up + down + 1;
        int width = left + right + 1;
        int depth = forward + 1;
        if (height < MIN_SIZE || width < MIN_SIZE || depth < MIN_SIZE
                || up == 0 || down == 0 || left == 0 || right == 0) {
            return;
        }
        for (int vertical = -down; vertical <= up; vertical++) {
            for (int lateral = -left; lateral <= right; lateral++) {
                for (int front = 0; front <= forward; front++) {
                    BlockPos target = corePos.above(vertical).relative(sideAxis, lateral).relative(facing, front);
                    if (!level.getBlockState(target).is(ModBlocks.PILE_BRICK.get())) {
                        return;
                    }
                }
            }
        }
        for (int vertical = -down; vertical <= up; vertical++) {
            for (int lateral = -left; lateral <= right; lateral++) {
                for (int front = 0; front <= forward; front++) {
                    BlockPos target = corePos.above(vertical).relative(sideAxis, lateral).relative(facing, front);
                    boolean core = target.equals(corePos);
                    int boundary = (vertical == -down || vertical == up ? 1 : 0)
                            + (lateral == -left || lateral == right ? 1 : 0)
                            + (front == 0 || front == forward ? 1 : 0);
                    Mk2PileStructureBlock.Role role = core ? Mk2PileStructureBlock.Role.CORE
                            : boundary > 1 ? Mk2PileStructureBlock.Role.EDGE : Mk2PileStructureBlock.Role.DUMMY;
                    level.setBlock(target, ModBlocks.PILE_BLOCK.get().defaultBlockState()
                            .setValue(Mk2PileStructureBlock.ROLE, role), Block.UPDATE_ALL);
                    if (core) {
                        if (level.getBlockEntity(target) instanceof Mk2PileCoreBlockEntity entity) {
                            entity.configure(height, width, depth, left, up, facing);
                        }
                    } else if (level.getBlockEntity(target) instanceof Mk2PileMemberBlockEntity entity) {
                        entity.setCorePos(corePos);
                    }
                }
            }
        }
    }

    private static int scan(Level level, BlockPos origin, Direction direction, int maximum) {
        int found = 0;
        for (int distance = 1; distance <= maximum; distance++) {
            if (!level.getBlockState(origin.relative(direction, distance)).is(ModBlocks.PILE_BRICK.get())) {
                break;
            }
            found = distance;
        }
        return found;
    }
}
