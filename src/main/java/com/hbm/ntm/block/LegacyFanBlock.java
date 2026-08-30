package com.hbm.ntm.block;

import com.hbm.main.ServerProxy;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.LegacyFanBlockEntity;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Exact six-direction, TESR-only migration of 1.7.10 MachineFan. */
@SuppressWarnings("deprecation")
public class LegacyFanBlock extends DirectionalBlock implements EntityBlock, Toolable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public LegacyFanBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // BlockPistonBase.determineOrientation: the piston/fan points away from the placer.
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    public boolean isFaceSturdy(BlockState state, BlockGetter level, BlockPos pos, Direction side,
            net.minecraft.world.level.block.SupportType supportType) {
        // Legacy isSideSolid: only faces perpendicular to the fan's axis are solid.
        return side.getAxis() != state.getValue(FACING).getAxis();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        LegacyStandardInfoTooltip.append(tooltip, "fan");
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyFanBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.FAN.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                LegacyFanBlockEntity.tick(tickLevel, tickPos, tickState, (LegacyFanBlockEntity) blockEntity);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) {
            return false;
        }
        if (tool == ToolType.SCREWDRIVER) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(FACING, state.getValue(FACING).getOpposite()),
                        Block.UPDATE_CLIENTS);
            }
            return true;
        }
        if (!(level.getBlockEntity(pos) instanceof LegacyFanBlockEntity fan)
                || (tool != ToolType.HAND_DRILL && tool != ToolType.DEFUSER)) {
            return false;
        }
        if (!level.isClientSide) {
            boolean enabled = tool == ToolType.HAND_DRILL ? fan.toggleFalloff() : fan.toggleSuck();
            String suffix = tool == ToolType.HAND_DRILL
                    ? (enabled ? ".falloffOn" : ".falloffOff")
                    : (enabled ? ".suckOn" : ".suckOff");
            if (player instanceof ServerPlayer serverPlayer) {
                ModMessages.informPlayer(serverPlayer,
                        Component.translatable("block.hbm_ntm_rebirth.fan" + suffix).withStyle(ChatFormatting.GOLD),
                        ServerProxy.ID_FAN_MODE);
            }
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.5F, 0.5F);
        }
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }
}
