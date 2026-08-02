package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.Mk2PileDeviceBlockEntity;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.item.Mk2PileDeviceBlockItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** The three legacy pile_device metadata families, represented as state instead of raw metadata. */
public final class Mk2PileDeviceBlock extends BaseEntityBlock implements Toolable {
    public static final DirectionProperty FACING = HorizontalMachineBlock.FACING;
    public static final EnumProperty<Kind> KIND = EnumProperty.create("kind", Kind.class);

    public Mk2PileDeviceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(KIND, Kind.LOADER));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Kind kind = Mk2PileDeviceBlockItem.kind(context.getItemInHand());
        Direction facing = kind == Kind.CONTROL
                ? context.getHorizontalDirection().getOpposite()
                : context.getClickedFace().getAxis().isHorizontal() ? context.getClickedFace() : Direction.NORTH;
        return defaultBlockState().setValue(FACING, facing).setValue(KIND, kind);
    }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new Mk2PileDeviceBlockEntity(pos, state); }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.MK2_PILE_DEVICE.get()
                ? createTickerHelper(type, ModBlockEntities.MK2_PILE_DEVICE.get(), Mk2PileDeviceBlockEntity::tick)
                : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown() || state.getValue(KIND) != Kind.LOADER) return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof Mk2PileDeviceBlockEntity device
                && device.useLoader(player, hand)) return InteractionResult.sidedSuccess(level.isClientSide);
        return InteractionResult.PASS;
    }

    /** Old BlockPileDevice#onScrew delegated the tool to the actual channel entry. */
    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (level.isClientSide) return true;
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) return false;
        BlockPos target;
        Direction targetSide;
        if (state.getValue(KIND) == Kind.CONTROL) {
            target = pos.below();
            targetSide = Direction.UP;
        } else {
            targetSide = state.getValue(FACING);
            target = pos.relative(targetSide.getOpposite());
        }
        BlockState targetState = level.getBlockState(target);
        return targetState.is(ModBlocks.PILE_BLOCK.get())
                && targetState.getBlock() instanceof Mk2PileStructureBlock pile
                && pile.onToolUse(level, player, target, targetSide, hit, tool);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, KIND);
    }

    public enum Kind implements StringRepresentable {
        LOADER, VENT, CONTROL;
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
}
