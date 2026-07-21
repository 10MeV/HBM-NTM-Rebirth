package com.hbm.ntm.entity.item;

import com.hbm.ntm.block.LegacySpotlightBlock;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacyFallingBlockEntity extends Entity {
    private static final EntityDataAccessor<Integer> BLOCK_STATE_ID =
            SynchedEntityData.defineId(LegacyFallingBlockEntity.class, EntityDataSerializers.INT);

    private int fallingTicks;
    private boolean canDrop = true;
    private boolean destroyOnLand;
    private boolean canHurtEntities;
    private int damageCap = 40;
    private float damageAmount = 2.0F;
    @Nullable
    private CompoundTag tileData;

    public LegacyFallingBlockEntity(EntityType<? extends LegacyFallingBlockEntity> type, Level level) {
        super(type, level);
        blocksBuilding = true;
    }

    public LegacyFallingBlockEntity(Level level) {
        this(ModEntityTypes.LEGACY_FALLING_BLOCK.get(), level);
    }

    public static LegacyFallingBlockEntity create(Level level, BlockPos pos, BlockState state) {
        LegacyFallingBlockEntity entity = new LegacyFallingBlockEntity(level);
        entity.setBlockState(state);
        entity.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.xo = entity.getX();
        entity.yo = entity.getY();
        entity.zo = entity.getZ();
        return entity;
    }

    public static LegacyFallingBlockEntity createNoDrop(Level level, BlockPos pos, BlockState state) {
        LegacyFallingBlockEntity entity = create(level, pos, state);
        entity.canDrop = false;
        return entity;
    }

    @Override
    public void tick() {
        super.tick();

        BlockState fallingState = blockState();
        if (fallingState.isAir() || fallingState.getBlock() instanceof LegacySpotlightBlock) {
            discard();
            return;
        }

        fallingTicks++;
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(motion.x, motion.y - 0.04D, motion.z);
        move(MoverType.SELF, getDeltaMovement());
        motion = getDeltaMovement();
        setDeltaMovement(motion.x * 0.98D, motion.y * 0.98D, motion.z * 0.98D);

        if (!level().isClientSide()) {
            BlockPos pos = BlockPos.containing(getX(), getY(), getZ());
            if (fallingTicks == 1) {
                if (level().getBlockState(pos).getBlock() != fallingState.getBlock()) {
                    discard();
                    return;
                }
                level().removeBlock(pos, false);
            }

            if (onGround()) {
                motion = getDeltaMovement();
                setDeltaMovement(motion.x * 0.7D, motion.y * -0.5D, motion.z * 0.7D);
                if (!level().getBlockState(pos).is(Blocks.MOVING_PISTON)) {
                    discard();
                    landAt(pos, fallingState);
                }
            } else if (shouldTimeout(pos)) {
                dropCarriedItem(fallingState);
                discard();
            }
        }
    }

    private void landAt(BlockPos pos, BlockState fallingState) {
        if (!destroyOnLand && replacementCheck(pos, fallingState) && level().setBlock(pos, fallingState, 3)) {
            restoreTileData(pos);
        } else if (canDrop && !destroyOnLand) {
            dropCarriedItem(fallingState);
        }
    }

    private boolean replacementCheck(BlockPos pos, BlockState fallingState) {
        if (level().isOutsideBuildHeight(pos)) {
            return false;
        }
        return level().getBlockState(pos).canBeReplaced() && fallingState.canSurvive(level(), pos);
    }

    private boolean shouldTimeout(BlockPos pos) {
        if (fallingTicks > 600) {
            return true;
        }
        return fallingTicks > 100
                && (pos.getY() < level().getMinBuildHeight() + 1 || pos.getY() > level().getMaxBuildHeight());
    }

    private void restoreTileData(BlockPos pos) {
        if (tileData == null || !blockState().hasBlockEntity()) {
            return;
        }
        BlockEntity blockEntity = level().getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }

        CompoundTag merged = blockEntity.saveWithoutMetadata();
        for (String key : tileData.getAllKeys()) {
            if (!"x".equals(key) && !"y".equals(key) && !"z".equals(key)) {
                Tag value = tileData.get(key);
                if (value != null) {
                    merged.put(key, value.copy());
                }
            }
        }
        blockEntity.load(merged);
        blockEntity.setChanged();
    }

    private void dropCarriedItem(BlockState state) {
        if (!canDrop) {
            return;
        }
        if (state.getBlock().asItem() != Items.AIR) {
            spawnAtLocation(new ItemStack(state.getBlock().asItem()), 0.0F);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (!canHurtEntities) {
            return false;
        }
        int fall = Mth.ceil(fallDistance - 1.0F);
        if (fall <= 0) {
            return false;
        }

        DamageSource damageSource = isVanillaAnvil(blockState())
                ? level().damageSources().anvil(this)
                : level().damageSources().fallingBlock(this);
        float damage = Math.min(Mth.floor(fall * damageAmount), damageCap);
        List<Entity> targets = level().getEntities(this, getBoundingBox());
        for (Entity target : targets) {
            target.hurt(damageSource, damage);
        }

        if (isVanillaAnvil(blockState()) && random.nextFloat() < 0.05F + fall * 0.05F) {
            BlockState damaged = nextAnvilDamageState(blockState());
            if (damaged == null) {
                destroyOnLand = true;
            } else {
                setBlockState(damaged);
            }
        }
        return false;
    }

    private static boolean isVanillaAnvil(BlockState state) {
        return state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL);
    }

    @Nullable
    private static BlockState nextAnvilDamageState(BlockState state) {
        if (state.is(Blocks.ANVIL)) {
            return Blocks.CHIPPED_ANVIL.defaultBlockState();
        }
        if (state.is(Blocks.CHIPPED_ANVIL)) {
            return Blocks.DAMAGED_ANVIL.defaultBlockState();
        }
        return null;
    }

    public BlockState blockState() {
        BlockState state = Block.stateById(entityData.get(BLOCK_STATE_ID));
        return state == null ? Blocks.SAND.defaultBlockState() : state;
    }

    public void setBlockState(BlockState state) {
        entityData.set(BLOCK_STATE_ID, Block.getId(state == null ? Blocks.SAND.defaultBlockState() : state));
    }

    public void setCanDrop(boolean canDrop) {
        this.canDrop = canDrop;
    }

    public void setCanHurtEntities(boolean canHurtEntities) {
        this.canHurtEntities = canHurtEntities;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !isRemoved();
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(BLOCK_STATE_ID, Block.getId(Blocks.SAND.defaultBlockState()));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("BlockState", Tag.TAG_COMPOUND)) {
            setBlockState(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("BlockState")));
        } else if (tag.contains("blockState", Tag.TAG_ANY_NUMERIC)) {
            setBlockState(Block.stateById(tag.getInt("blockState")));
        }
        if (blockState().isAir()) {
            setBlockState(Blocks.SAND.defaultBlockState());
        }

        fallingTicks = tag.getByte("Time") & 255;
        if (tag.contains("DropItem", Tag.TAG_ANY_NUMERIC)) {
            canDrop = tag.getBoolean("DropItem");
        }
        if (tag.contains("HurtEntities", Tag.TAG_ANY_NUMERIC)) {
            canHurtEntities = tag.getBoolean("HurtEntities");
            damageAmount = tag.getFloat("FallHurtAmount");
            damageCap = tag.getInt("FallHurtMax");
        } else if (isVanillaAnvil(blockState())) {
            canHurtEntities = true;
        }
        if (tag.contains("TileEntityData", Tag.TAG_COMPOUND)) {
            tileData = tag.getCompound("TileEntityData").copy();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        BlockState state = blockState();
        tag.put("BlockState", NbtUtils.writeBlockState(state));
        tag.putInt("blockState", Block.getId(state));
        tag.putByte("Time", (byte) fallingTicks);
        tag.putBoolean("DropItem", canDrop);
        tag.putBoolean("HurtEntities", canHurtEntities);
        tag.putFloat("FallHurtAmount", damageAmount);
        tag.putInt("FallHurtMax", damageCap);
        if (tileData != null) {
            tag.put("TileEntityData", tileData.copy());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
