package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.GasFlareBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class GasFlareBlock extends LegacyVisibleMultiblockMachineBlock {
    public static final BooleanProperty TILTED = BooleanProperty.create("tilted");

    public GasFlareBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
        registerDefaultState(defaultBlockState().setValue(TILTED, false));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // Exact 1.7.10 MachineGasFlare#addInformation literals.
        tooltip.add(Component.literal("Can burn fluids and vent gasses").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Burns up to ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("10mB/t").withStyle(ChatFormatting.RED)));
        tooltip.add(Component.literal("Vents up to ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("50mB/t").withStyle(ChatFormatting.RED)));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Fuel efficiency:").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("-Flammable Gasses: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("20%").withStyle(ChatFormatting.RED)));
        tooltip.add(Component.literal("-Flammable Liquids: ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("10%").withStyle(ChatFormatting.RED)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GasFlareBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (state.hasProperty(TILTED) && state.getValue(TILTED)) {
            return super.getRenderShape(state);
        }
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        // MachineGasFlare delegates to BlockDummyable#standardOpenBehavior:
        // sneaking consumes the interaction but cannot mutate the tank or open a menu.
        if (player.isShiftKeyDown()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof GasFlareBlockEntity gasFlare) {
            NetworkHooks.openScreen(serverPlayer, gasFlare, gasFlare.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.GAS_FLARE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                GasFlareBlockEntity.clientTick(tickLevel, tickPos, tickState, (GasFlareBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                GasFlareBlockEntity.serverTick(tickLevel, tickPos, tickState, (GasFlareBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof GasFlareBlockEntity gasFlare) {
            for (ItemStack stack : gasFlare.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TILTED);
    }
}
