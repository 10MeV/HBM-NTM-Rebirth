package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.LegacyFanBlockEntity;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyFanBlock extends BaseEntityBlock implements Toolable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final int INFORM_ID_FAN_MODE = 15;

    public LegacyFanBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    public boolean isFaceSturdy(BlockState state, BlockGetter level, BlockPos pos, Direction direction,
            SupportType supportType) {
        return state.getValue(FACING).getAxis() != direction.getAxis();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (!(level.getBlockEntity(pos) instanceof LegacyFanBlockEntity fan)) {
            return false;
        }
        if (tool == ToolType.SCREWDRIVER) {
            if (!level.isClientSide) {
                level.setBlock(pos, fanFlippedState(level.getBlockState(pos)), 3);
            }
            return true;
        }
        if (tool == ToolType.HAND_DRILL) {
            if (!level.isClientSide) {
                fan.setFalloff(!fan.falloff());
                inform(player, Component.translatable("block.hbm_ntm_rebirth.fan."
                        + (fan.falloff() ? "falloff_on" : "falloff_off")));
                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.BLOCKS, 0.5F, 0.5F);
            }
            return true;
        }
        if (tool == ToolType.DEFUSER) {
            if (!level.isClientSide) {
                fan.setSuck(!fan.suck());
                inform(player, Component.translatable("block.hbm_ntm_rebirth.fan."
                        + (fan.suck() ? "suck_on" : "suck_off")));
                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.BLOCKS, 0.5F, 0.5F);
            }
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        if (tool != null && onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
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
        return createTickerHelper(type, ModBlockEntities.LEGACY_FAN.get(), LegacyFanBlockEntity::tick);
    }

    private static BlockState fanFlippedState(BlockState state) {
        if (!state.hasProperty(FACING)) {
            return state;
        }
        return state.setValue(FACING, state.getValue(FACING).getOpposite());
    }

    private static void inform(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModMessages.sendToPlayer(ModMessages.playerInformPacket(message, INFORM_ID_FAN_MODE), serverPlayer);
        }
    }
}
