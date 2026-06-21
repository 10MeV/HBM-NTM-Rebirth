package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.LegacyEmitterBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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
public class LegacyEmitterBlock extends BaseEntityBlock implements Toolable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final int[] LEGACY_DYE_COLORS = {
            0x1E1B1B, 0xB3312C, 0x3B511A, 0x51301A,
            0x253192, 0x7B2FBE, 0x287697, 0xABABAB,
            0x434343, 0xD88198, 0x41CD34, 0xDECF2A,
            0x6689D3, 0xC354CD, 0xEB8844, 0xF0F0F0
    };

    public LegacyEmitterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof DyeItem dye) || !(level.getBlockEntity(pos) instanceof LegacyEmitterBlockEntity emitter)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            emitter.setColor(legacyDyeColor(dye.getDyeColor()));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (!(level.getBlockEntity(pos) instanceof LegacyEmitterBlockEntity emitter)) {
            return false;
        }
        if (tool == ToolType.SCREWDRIVER) {
            if (!level.isClientSide) {
                emitter.adjustGirth(0.125F);
            }
            return true;
        }
        if (tool == ToolType.DEFUSER) {
            if (!level.isClientSide) {
                emitter.adjustGirth(-0.125F);
            }
            return true;
        }
        if (tool == ToolType.HAND_DRILL) {
            if (!level.isClientSide) {
                emitter.cycleEffect();
            }
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.literal("Use screwdriver to widen beam").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Use defuser to narrow beam").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Use hand drill to cycle special effects").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Use dye to change color").withStyle(ChatFormatting.GOLD));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyEmitterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.LEGACY_EMITTER.get(), LegacyEmitterBlockEntity::tick);
    }

    private static int legacyDyeColor(DyeColor color) {
        return LEGACY_DYE_COLORS[legacyDyeDamage(color)];
    }

    private static int legacyDyeDamage(DyeColor color) {
        return switch (color) {
            case BLACK -> 0;
            case RED -> 1;
            case GREEN -> 2;
            case BROWN -> 3;
            case BLUE -> 4;
            case PURPLE -> 5;
            case CYAN -> 6;
            case LIGHT_GRAY -> 7;
            case GRAY -> 8;
            case PINK -> 9;
            case LIME -> 10;
            case YELLOW -> 11;
            case LIGHT_BLUE -> 12;
            case MAGENTA -> 13;
            case ORANGE -> 14;
            case WHITE -> 15;
        };
    }
}
