package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ICFControllerBlockEntity;
import com.hbm.ntm.blockentity.ICFAssembledBlockEntity;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ICFControllerBlock extends HorizontalMachineBlock implements EntityBlock {
    private static final int MAX_SIZE = 1024;

    public ICFControllerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ICFControllerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof ICFControllerBlockEntity controller) {
            if (!controller.isAssembled()) {
                assemble(level, pos, state, controller, player instanceof ServerPlayer ? (ServerPlayer) player : null);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.ICF_CONTROLLER.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (tickLevel, tickPos, tickState, blockEntity) ->
                    ICFControllerBlockEntity.clientTick(tickLevel, tickPos, tickState,
                            (ICFControllerBlockEntity) blockEntity);
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                ICFControllerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (ICFControllerBlockEntity) blockEntity);
    }

    private void assemble(Level level, BlockPos pos, BlockState state, ICFControllerBlockEntity controller,
            @Nullable ServerPlayer player) {
        AssemblyResult result = scanAssembly(level, pos, state);
        if (result.ok()) {
            for (Map.Entry<BlockPos, PartState> entry : result.parts().entrySet()) {
                BlockPos partPos = entry.getKey();
                PartState partState = entry.getValue();
                level.setBlock(partPos, ModBlocks.ICF_BLOCK.get().defaultBlockState()
                        .setValue(ICFAssembledBlock.PORT, partState.part() == ICFPart.PORT), Block.UPDATE_ALL);
                if (level.getBlockEntity(partPos) instanceof ICFAssembledBlockEntity assembledBlock) {
                    assembledBlock.setOriginal(partState.state(), pos, partState.part() == ICFPart.PORT);
                }
            }
            controller.setup(result.ports(), result.cells(), result.emitters(), result.capacitors(),
                    result.turbochargers());
        } else {
            controller.clearAssembly();
            if (player != null) {
                sendErrorMarker(player, result.errorPos(), result.error());
            }
        }
    }

    private AssemblyResult scanAssembly(Level level, BlockPos controllerPos, BlockState controllerState) {
        Direction dir = controllerState.getValue(FACING).getOpposite();
        Map<BlockPos, PartState> parts = new LinkedHashMap<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> ports = new HashSet<>();
        Set<BlockPos> cells = new HashSet<>();
        Set<BlockPos> emitters = new HashSet<>();
        Set<BlockPos> capacitors = new HashSet<>();
        Set<BlockPos> turbochargers = new HashSet<>();
        visited.add(controllerPos.immutable());
        AssemblyResult result = floodFill(level, controllerPos.relative(dir), parts, visited, ports, cells, emitters,
                capacitors, turbochargers);
        return result.ok()
                ? AssemblyResult.success(parts, ports, cells, emitters, capacitors, turbochargers)
                : result;
    }

    private AssemblyResult floodFill(Level level, BlockPos pos, Map<BlockPos, PartState> parts, Set<BlockPos> visited,
            Set<BlockPos> ports, Set<BlockPos> cells, Set<BlockPos> emitters, Set<BlockPos> capacitors,
            Set<BlockPos> turbochargers) {
        if (visited.contains(pos)) {
            return AssemblyResult.success(parts, ports, cells, emitters, capacitors, turbochargers);
        }
        if (visited.size() >= MAX_SIZE) {
            return AssemblyResult.error(pos, "Max size exceeded");
        }
        BlockState state = level.getBlockState(pos);
        ICFPart part = ICFPart.fromBlock(state.getBlock());
        if (part == null) {
            return AssemblyResult.error(pos, "Non-laser block");
        }
        visited.add(pos);
        parts.put(pos.immutable(), new PartState(part, state));
        switch (part) {
            case PORT -> {
                ports.add(pos);
                return AssemblyResult.success(parts, ports, cells, emitters, capacitors, turbochargers);
            }
            case CELL -> cells.add(pos);
            case EMITTER -> emitters.add(pos);
            case CAPACITOR -> capacitors.add(pos);
            case TURBO -> turbochargers.add(pos);
            case CASING -> {
                return AssemblyResult.success(parts, ports, cells, emitters, capacitors, turbochargers);
            }
        }
        for (Direction direction : Direction.values()) {
            AssemblyResult result = floodFill(level, pos.relative(direction), parts, visited, ports, cells, emitters,
                    capacitors, turbochargers);
            if (!result.ok()) {
                return result;
            }
        }
        return AssemblyResult.success(parts, ports, cells, emitters, capacitors, turbochargers);
    }

    private static void sendErrorMarker(ServerPlayer player, BlockPos pos, String message) {
        CompoundTag data = new CompoundTag();
        data.putString("type", ParticleUtil.TYPE_MARKER);
        data.putInt("color", 0xff0000);
        data.putInt("expires", 5_000);
        data.putDouble("dist", 128D);
        if (message != null && !message.isBlank()) {
            data.putString("label", message);
        }
        ModMessages.sendAuxParticle(player, pos.getX(), pos.getY(), pos.getZ(), data);
    }

    public enum ICFPart {
        CASING,
        PORT,
        CELL,
        EMITTER,
        CAPACITOR,
        TURBO;

        public static ICFPart fromBlock(Block block) {
            if (block == ModBlocks.ICF_LASER_CASING.get()) return CASING;
            if (block == ModBlocks.ICF_LASER_PORT.get()) return PORT;
            if (block == ModBlocks.ICF_LASER_CELL.get()) return CELL;
            if (block == ModBlocks.ICF_LASER_EMITTER.get()) return EMITTER;
            if (block == ModBlocks.ICF_LASER_CAPACITOR.get()) return CAPACITOR;
            if (block == ModBlocks.ICF_LASER_TURBO.get()) return TURBO;
            return null;
        }
    }

    private record PartState(ICFPart part, BlockState state) {
    }

    private record AssemblyResult(boolean ok, String error, BlockPos errorPos, Map<BlockPos, PartState> parts,
            Set<BlockPos> ports, Set<BlockPos> cells, Set<BlockPos> emitters, Set<BlockPos> capacitors,
            Set<BlockPos> turbochargers) {
        private static AssemblyResult success(Map<BlockPos, PartState> parts, Set<BlockPos> ports, Set<BlockPos> cells,
                Set<BlockPos> emitters, Set<BlockPos> capacitors, Set<BlockPos> turbochargers) {
            return new AssemblyResult(true, "", BlockPos.ZERO, parts, ports, cells, emitters, capacitors,
                    turbochargers);
        }

        private static AssemblyResult error(BlockPos pos, String message) {
            return new AssemblyResult(false, message, pos, Map.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of());
        }
    }
}
