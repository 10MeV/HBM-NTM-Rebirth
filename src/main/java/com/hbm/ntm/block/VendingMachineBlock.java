package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.VendingMachineBlockEntity;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.LegacyStateMultiblockBlockItem;
import com.hbm.ntm.itempool.HbmItemPoolIds;
import com.hbm.ntm.itempool.HbmItemPoolRegistry;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class VendingMachineBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 1);
    private static final int[] DIMENSIONS = { 1, 0, 0, 0, 0, 0 };

    public VendingMachineBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(VARIANT, 0));
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return 0;
    }

    @Nullable
    @Override
    public BlockState getDirectPlacementState(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, getFacingForPlacement(context))
                .setValue(VARIANT, getStackVariant(context.getItemInHand()));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VendingMachineBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.COIN_TOKEN.get())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            BlockPos clickedPos = clickedBlockPos(hit);
            String pool = state.getValue(VARIANT) == 0 ? HbmItemPoolIds.POOL_SODA : HbmItemPoolIds.POOL_SNACKS;
            ItemStack stack = HbmItemPoolRegistry.getStack(serverLevel, pool, Vec3.atCenterOf(clickedPos));
            if (!stack.isEmpty()) {
                Direction facing = state.getValue(FACING);
                ItemEntity item = new ItemEntity(level,
                        clickedPos.getX() + 0.5D + facing.getStepX() * 0.75D,
                        pos.getY() + 0.25D,
                        clickedPos.getZ() + 0.5D + facing.getStepZ() * 0.75D,
                        stack);
                level.addFreshEntity(item);
            }
            LegacySoundPlayer.playLegacyBoltOpen(level, clickedPos, 1.0F, 0.75F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        Block.popResource(level, pos, createVariantStack(state.getValue(VARIANT)));
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return java.util.List.of();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VARIANT);
    }

    private static int getStackVariant(ItemStack stack) {
        if (stack.getItem() instanceof LegacyStateBlockItem item) {
            return item.getVariant(stack);
        }
        if (stack.getItem() instanceof LegacyStateMultiblockBlockItem item) {
            return item.getVariant(stack);
        }
        return 0;
    }

    private ItemStack createVariantStack(int variant) {
        if (asItem() instanceof LegacyStateBlockItem item) {
            return LegacyStateBlockItem.createStack(item, variant);
        }
        if (asItem() instanceof LegacyStateMultiblockBlockItem item) {
            return LegacyStateMultiblockBlockItem.createStack(item, variant);
        }
        if (asItem() instanceof BlockItem) {
            return new ItemStack(asItem());
        }
        return ItemStack.EMPTY;
    }

    private static BlockPos clickedBlockPos(BlockHitResult hit) {
        Vec3 location = hit.getLocation();
        Direction direction = hit.getDirection();
        double epsilon = 1.0E-5D;
        return BlockPos.containing(
                location.x - direction.getStepX() * epsilon,
                location.y - direction.getStepY() * epsilon,
                location.z - direction.getStepZ() * epsilon);
    }
}
