package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.TrinketBlockEntity;
import com.hbm.ntm.item.TrinketBlockItem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TrinketBlock extends BaseEntityBlock {
    private static final VoxelShape BOBBLEHEAD_SHAPE = box(5.5D, 0.0D, 5.5D, 10.5D, 10.0D, 10.5D);
    private static final VoxelShape SNOWGLOBE_SHAPE = box(4.0D, 0.0D, 4.0D, 12.0D, 5.0D, 12.0D);
    private static final VoxelShape PLUSHIE_SHAPE = box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);

    private final TrinketVariant.Kind kind;
    private final VoxelShape shape;

    public TrinketBlock(BlockBehaviour.Properties properties, TrinketVariant.Kind kind) {
        super(properties);
        this.kind = kind;
        this.shape = switch (kind) {
            case BOBBLEHEAD -> BOBBLEHEAD_SHAPE;
            case SNOWGLOBE -> SNOWGLOBE_SHAPE;
            case PLUSHIE -> PLUSHIE_SHAPE;
        };
    }

    public TrinketVariant.Kind kind() {
        return kind;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrinketBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.TRINKET.get(), TrinketBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof TrinketBlockEntity blockEntity) {
            int yaw = placer == null ? 0 : (((int) Math.floor((placer.getYRot() + 180.0F) * 16.0F / 360.0F + 0.5D)) & 15);
            blockEntity.setVariant(TrinketBlockItem.getVariant(stack));
            blockEntity.setYawSteps(yaw);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative() && level.getBlockEntity(pos) instanceof TrinketBlockEntity blockEntity) {
            popResource(level, pos, TrinketBlockItem.createStack(asItem(), blockEntity.variant()));
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof TrinketBlockEntity blockEntity) {
            return TrinketBlockItem.createStack(asItem(), blockEntity.variant());
        }
        return TrinketBlockItem.createStack(asItem(), TrinketVariant.firstCreativeVariant(kind));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (kind == TrinketVariant.Kind.PLUSHIE && level.getBlockEntity(pos) instanceof TrinketBlockEntity blockEntity) {
            blockEntity.startSquish();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.updateNeighborsAt(pos, this);
        }
    }
}
