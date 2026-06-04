package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.FluidTankBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FluidTankBlock extends LegacyVisibleMultiblockMachineBlock implements EntityBlock {
    public FluidTankBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidTankBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank && !tank.isExploded()) {
            ItemStack held = player.getItemInHand(hand);
            if (player.isShiftKeyDown() && held.getItem() instanceof IFluidIdentifierItem identifier) {
                FluidType type = identifier.getIdentifiedFluid(level, pos, held);
                return tank.setIdentifiedType(type) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            NetworkHooks.openScreen(serverPlayer, tank, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.FLUID_TANK.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                FluidTankBlockEntity.serverTick(tickLevel, tickPos, tickState, (FluidTankBlockEntity) blockEntity);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        restorePersistentState(level, pos, stack);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank ? tank.getComparatorPower() : 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getMultiblockCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return definition().hasCollisionShapeFactory() ? definition().collisionShape(state) : Shapes.block();
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return definition().layout(state);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank) {
            for (ItemStack stack : tank.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof FluidTankBlockEntity tank) {
            return List.of(tank.createPersistentBlockDrop(asItem()));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(HbmPersistentBlockState.TAG_PERSISTENT, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag persistent = tag.getCompound(HbmPersistentBlockState.TAG_PERSISTENT);
        HbmFluidTank tank = new HbmFluidTank(HbmFluids.NONE, 0);
        tank.readFromNbt(persistent, "tank");
        tooltip.add(HbmFluidGuiHelper.tankInfo(tank, tank.getFill(), tank.getMaxFill())
                .copy()
                .withStyle(ChatFormatting.YELLOW));
        if (persistent.getBoolean("hasExploded")) {
            tooltip.add(Component.translatable("container.fluidtank.damaged").withStyle(ChatFormatting.RED));
        }
        if (persistent.getBoolean("onFire")) {
            tooltip.add(Component.translatable("container.fluidtank.burning").withStyle(ChatFormatting.RED));
        }
    }

    private static void restorePersistentState(Level level, BlockPos pos, ItemStack stack) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof HbmPersistentBlockState persistent)) {
            return;
        }
        persistent.readPersistentStateFromStack(stack);
    }
}
