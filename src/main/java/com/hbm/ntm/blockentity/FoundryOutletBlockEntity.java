package com.hbm.ntm.blockentity;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.block.FoundryOutletBlock;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.util.CrucibleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FoundryOutletBlockEntity extends FoundryBaseBlockEntity {
    private static final String TAG_INVERT = "invert";
    private static final String TAG_INVERT_FILTER = "invertFilter";
    private static final String TAG_FILTER = "filter";

    private final boolean slagTap;
    private NTMMaterial filter;
    private boolean invertFilter;
    private boolean invertRedstone;

    public FoundryOutletBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FOUNDRY_OUTLET.get(), pos, state, false);
    }

    public static FoundryOutletBlockEntity slagTap(BlockPos pos, BlockState state) {
        return new FoundryOutletBlockEntity(ModBlockEntities.FOUNDRY_SLAGTAP.get(), pos, state, true);
    }

    private FoundryOutletBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> entityType,
            BlockPos pos, BlockState state, boolean slagTap) {
        super(entityType, pos, state);
        this.slagTap = slagTap;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryOutletBlockEntity outlet) {
        if (!level.isClientSide && level.getGameTime() % 20L == 0L) {
            outlet.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public NTMMaterial getFilter() {
        return filter;
    }

    public boolean isInvertFilter() {
        return invertFilter;
    }

    public boolean isInvertRedstone() {
        return invertRedstone;
    }

    public boolean isClosed() {
        return level != null && (invertRedstone ^ level.hasNeighborSignal(worldPosition));
    }

    public void setFilter(NTMMaterial filter) {
        this.filter = filter;
        setChangedAndUpdate();
    }

    public void clearFilter() {
        filter = null;
        invertFilter = false;
        setChangedAndUpdate();
    }

    public void toggleInvertFilter() {
        invertFilter = !invertFilter;
        setChangedAndUpdate();
    }

    public void toggleInvertRedstone() {
        invertRedstone = !invertRedstone;
        setChangedAndUpdate();
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return false;
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return stack;
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        if (stack == null || stack.material == null) {
            return false;
        }
        if (filter != null && ((filter != stack.material) ^ invertFilter)) {
            return false;
        }
        if (isClosed() || side != facing().getOpposite()) {
            return false;
        }
        return slagTap ? slagTarget(level) != null : pouringTarget(level).acceptor() != null
                && pouringTarget(level).acceptor().canAcceptPartialPour(level, pouringTarget(level).pos(),
                pouringTarget(level).hit(), Direction.UP, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        if (stack == null || stack.material == null || stack.amount <= 0) {
            return null;
        }
        if (slagTap) {
            return flowSlag(level, side, stack);
        }
        CrucibleUtil.HitTarget target = pouringTarget(level);
        if (target.acceptor() == null) {
            return stack;
        }
        MaterialStack left = target.acceptor().pour(level, target.pos(), target.hit(), Direction.UP, stack);
        spawnPourParticle(level, side, stack.material.moltenColor, target.hit(), 4.0F);
        return left;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_INVERT, invertRedstone);
        tag.putBoolean(TAG_INVERT_FILTER, invertFilter);
        tag.putShort(TAG_FILTER, (short) (filter == null ? -1 : filter.id));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        invertRedstone = tag.getBoolean(TAG_INVERT);
        invertFilter = tag.getBoolean(TAG_INVERT_FILTER);
        filter = Mats.matById.get((int) tag.getShort(TAG_FILTER));
    }

    private MaterialStack flowSlag(Level level, Direction side, MaterialStack stack) {
        BlockHitResult hit = slagTarget(level);
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return stack;
        }
        boolean didFlow = fillSlag(level, hit.getBlockPos(), stack);
        if (stack.amount > 0) {
            didFlow |= fillSlag(level, hit.getBlockPos().above(), stack);
        }
        if (didFlow) {
            spawnPourParticle(level, side, stack.material.moltenColor, hit.getLocation(), 15.0F);
        }
        return stack.amount <= 0 ? null : stack;
    }

    private boolean fillSlag(Level level, BlockPos pos, MaterialStack stack) {
        if (level.isOutsideBuildHeight(pos) || stack.amount <= 0) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.is(com.hbm.ntm.registry.ModBlocks.FOUNDRY_SLAG.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FoundrySlagBlockEntity slag && slag.getMaterialType() == stack.material) {
                int transfer = Math.min(FoundrySlagBlockEntity.MAX_AMOUNT - slag.getAmount(), stack.amount);
                if (transfer <= 0) {
                    return false;
                }
                slag.addMaterial(stack.material, transfer);
                stack.amount -= transfer;
                level.scheduleTick(pos, state.getBlock(), 1);
                return true;
            }
            return false;
        }
        if (state.canBeReplaced()) {
            level.setBlock(pos, com.hbm.ntm.registry.ModBlocks.FOUNDRY_SLAG.get().defaultBlockState(), Block.UPDATE_ALL);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FoundrySlagBlockEntity slag) {
                int transfer = Math.min(FoundrySlagBlockEntity.MAX_AMOUNT, stack.amount);
                slag.addMaterial(stack.material, transfer);
                stack.amount -= transfer;
                level.scheduleTick(pos, com.hbm.ntm.registry.ModBlocks.FOUNDRY_SLAG.get(), 1);
                return transfer > 0;
            }
        }
        return false;
    }

    private CrucibleUtil.HitTarget pouringTarget(Level level) {
        Vec3 start = new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY() - 0.125D, worldPosition.getZ() + 0.5D);
        Vec3 end = new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.125D - 4.0D, worldPosition.getZ() + 0.5D);
        return CrucibleUtil.getPouringTarget(level, start, end);
    }

    private BlockHitResult slagTarget(Level level) {
        Vec3 start = new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY() - 0.125D, worldPosition.getZ() + 0.5D);
        Vec3 end = new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.125D - 15.0D, worldPosition.getZ() + 0.5D);
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, null));
        return hit.getType() == HitResult.Type.BLOCK ? hit : null;
    }

    private void spawnPourParticle(Level level, Direction side, int color, Vec3 hit, float fallbackLength) {
        Direction direction = side.getOpposite();
        float length = hit == null ? fallbackLength
                : Math.max(1.0F, (float) (worldPosition.getY() - Math.ceil(hit.y) + (slagTap ? 0.0D : 0.875D)));
        ParticleUtil.spawnFoundryOutletPour(level, worldPosition, direction, color, length);
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(FoundryOutletBlock.FACING) ? state.getValue(FoundryOutletBlock.FACING) : Direction.NORTH;
    }
}
