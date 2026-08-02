package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.Mk2PileCoreBlockEntity;
import com.hbm.ntm.blockentity.Mk2PileMemberBlockEntity;
import com.hbm.ntm.client.ClientGeometryInvalidationBridge;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/** Converted block states of one dynamic MK2 Pile cube. */
public final class Mk2PileStructureBlock extends BaseEntityBlock implements Toolable {
    public static final EnumProperty<Role> ROLE = EnumProperty.create("role", Role.class);

    public Mk2PileStructureBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ROLE, Role.DUMMY));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(ROLE) == Role.CORE ? new Mk2PileCoreBlockEntity(pos, state)
                : new Mk2PileMemberBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        return tool == ToolType.HAND_DRILL && onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)
                ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, net.minecraft.core.Direction side, Vec3 hit,
            ToolType tool) {
        if (tool != ToolType.HAND_DRILL || level.isClientSide) return tool == ToolType.HAND_DRILL;
        BlockPos core = level.getBlockEntity(pos) instanceof Mk2PileMemberBlockEntity member ? member.corePos() : pos;
        return level.getBlockEntity(core) instanceof Mk2PileCoreBlockEntity entity
                && entity.drillChannel(pos, side.getOpposite());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return state.getValue(ROLE) == Role.CORE ? createTickerHelper(type, com.hbm.ntm.registry.ModBlockEntities.MK2_PILE_CORE.get(),
                com.hbm.ntm.blockentity.Mk2PileCoreBlockEntity::serverTick) : null;
    }

    @Override
    public List<net.minecraft.world.item.ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // BlockPile#getItemDropped returned null: the converted state is never a recoverable item drop.
        return Collections.emptyList();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean movedByPiston) {
        if (level.isClientSide && !state.is(replacement.getBlock())) refreshConnectedTextureNeighborhood(level, pos);
        if (!level.isClientSide && !state.is(replacement.getBlock())) {
            if (state.getValue(ROLE) == Role.CORE) {
                if (level.getBlockEntity(pos) instanceof Mk2PileCoreBlockEntity core && !core.isRestoring()) {
                    core.restoreBricks();
                }
            } else if (level.getBlockEntity(pos) instanceof Mk2PileMemberBlockEntity member) {
                Mk2PileCoreBlockEntity.restoreFrom(level, member.corePos());
            }
        }
        super.onRemove(state, level, pos, replacement, movedByPiston);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide && !state.is(oldState.getBlock())) refreshConnectedTextureNeighborhood(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.level.block.Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide) refreshConnectedTextureNeighborhood(level, pos);
    }

    /** CTContext reads diagonals too, so every nearby pile BE must invalidate after a local change. */
    private static void refreshConnectedTextureNeighborhood(Level level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof Mk2PileCoreBlockEntity || entity instanceof Mk2PileMemberBlockEntity) {
                entity.requestModelDataUpdate();
                ClientGeometryInvalidationBridge.schedule(pos);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(ROLE);
    }

    public enum Role implements StringRepresentable {
        DUMMY, CORE, CHANNEL, FUEL_IN, FUEL_OUT, AIR_IN, AIR_OUT, CONTROL, EDGE;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
