package com.hbm.ntm.item;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.PWRAssembledBlockEntity;
import com.hbm.ntm.blockentity.PWRControllerBlockEntity;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.PWRPrinterSnapshotPacket;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PWRPrinterItem extends Item {
    private static final int MAX_SCAN = 4096;

    public PWRPrinterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (!(level.getBlockEntity(clicked) instanceof PWRControllerBlockEntity controller)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.CONSUME;
        }
        Snapshot snapshot = collectSnapshot(level, clicked, controller);
        if (snapshot == null) {
            player.displayClientMessage(Component.literal("PWR structure is too large to print safely"), true);
            return InteractionResult.CONSUME;
        }
        ModMessages.sendToPlayer(new PWRPrinterSnapshotPacket(snapshot.min(), snapshot.max(),
                snapshot.direction(), snapshot.states()), player);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Use on a constructed PWR controller to generate construction diagrams"));
    }

    @Nullable
    private static Snapshot collectSnapshot(Level level, BlockPos controllerPos, PWRControllerBlockEntity controller) {
        Direction direction = controller.getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? controller.getBlockState().getValue(HorizontalMachineBlock.FACING).getOpposite()
                : Direction.NORTH;
        FloodFillResult floodResult = floodFill(level, controllerPos.relative(direction));
        if (floodResult.exceededLimit()) {
            return null;
        }
        Set<BlockPos> fill = floodResult.positions();
        fill.add(controllerPos.immutable());
        BlockPos min = controllerPos;
        BlockPos max = controllerPos;
        for (BlockPos pos : fill) {
            min = new BlockPos(Math.min(min.getX(), pos.getX()), Math.min(min.getY(), pos.getY()),
                    Math.min(min.getZ(), pos.getZ()));
            max = new BlockPos(Math.max(max.getX(), pos.getX()), Math.max(max.getY(), pos.getY()),
                    Math.max(max.getZ(), pos.getZ()));
        }
        if (!PWRPrinterSnapshotPacket.isValidBounds(min, max)) {
            return null;
        }

        Map<BlockPos, BlockState> statesByPos = new HashMap<>();
        statesByPos.put(controllerPos.immutable(), controller.getBlockState());
        for (BlockPos pos : fill) {
            if (pos.equals(controllerPos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.PWR_BLOCK.get())
                    && level.getBlockEntity(pos) instanceof PWRAssembledBlockEntity assembledBlock
                    && assembledBlock.getOriginalState() != null) {
                statesByPos.put(pos.immutable(), assembledBlock.getOriginalState());
            } else if (state.is(ModBlocks.PWR_BLOCK.get())) {
                statesByPos.put(pos.immutable(), state);
            }
        }

        int volume = PWRPrinterSnapshotPacket.snapshotVolume(min, max);
        List<BlockState> states = new ArrayList<>(volume);
        BlockState air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    states.add(statesByPos.getOrDefault(new BlockPos(x, y, z), air));
                }
            }
        }
        return new Snapshot(min, max, direction, states);
    }

    private static FloodFillResult floodFill(Level level, BlockPos start) {
        Set<BlockPos> fill = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.immutable());
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (fill.contains(pos) || !level.getBlockState(pos).is(ModBlocks.PWR_BLOCK.get())) {
                continue;
            }
            fill.add(pos.immutable());
            // Do not silently crop a connected reactor at the scan boundary:
            // the first node beyond the permitted legacy-sized scan invalidates
            // the whole snapshot before its bounds or states are allocated.
            if (fill.size() > MAX_SCAN) {
                return new FloodFillResult(Set.of(), true);
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!fill.contains(next)) {
                    queue.add(next);
                }
            }
        }
        return new FloodFillResult(fill, false);
    }

    private record Snapshot(BlockPos min, BlockPos max, Direction direction, List<BlockState> states) {
    }

    private record FloodFillResult(Set<BlockPos> positions, boolean exceededLimit) {
    }
}
