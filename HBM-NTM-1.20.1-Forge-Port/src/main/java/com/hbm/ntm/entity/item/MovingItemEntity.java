package com.hbm.ntm.entity.item;

import com.hbm.ntm.api.conveyor.IConveyorItem;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MovingItemEntity extends MovingConveyorObjectEntity implements IConveyorItem {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM =
            SynchedEntityData.defineId(MovingItemEntity.class, EntityDataSerializers.ITEM_STACK);

    public MovingItemEntity(EntityType<? extends MovingItemEntity> type, Level level) {
        super(type, level);
    }

    public MovingItemEntity(Level level, ItemStack stack) {
        this(ModEntityTypes.MOVING_ITEM.get(), level);
        setItemStack(stack);
    }

    public void setItemStack(ItemStack stack) {
        entityData.set(DATA_ITEM, stack.copy());
    }

    @Override
    public ItemStack getItemStack() {
        return entityData.get(DATA_ITEM);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && getItemStack().isEmpty()) {
            discard();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && !isRemoved()) {
            ItemStack stack = getItemStack().copy();
            if (!stack.isEmpty() && player.getInventory().add(stack)) {
                player.containerMenu.broadcastChanges();
                discard();
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && !isRemoved()) {
            dropAsItem(position(), getDeltaMovement().multiply(2.0D, 0.0D, 2.0D).add(0.0D, 0.1D, 0.0D));
            discard();
        }
        return true;
    }

    @Override
    public void enterBlock(IEnterableBlock enterable, BlockPos pos, Direction side) {
        if (isRemoved()) {
            return;
        }

        if (enterable.canItemEnter(level(), pos, side, this)) {
            enterable.onItemEnter(level(), pos, side, this);
            discard();
        }
    }

    @Override
    public boolean onLeaveConveyor() {
        if (isRemoved()) {
            return true;
        }

        Vec3 motion = getDeltaMovement();
        Vec3 spawnPos = position().add(motion.scale(2.0D));
        dropAsItem(spawnPos, new Vec3(motion.x * 2.0D, 0.1D, motion.z * 2.0D));
        discard();
        return true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setItemStack(ItemStack.of(tag.getCompound("Item")));
        if (getItemStack().isEmpty()) {
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        ItemStack stack = getItemStack();
        if (!stack.isEmpty()) {
            tag.put("Item", stack.save(new CompoundTag()));
        }
    }

    private void dropAsItem(Vec3 spawnPos, Vec3 motion) {
        ItemStack stack = getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        ItemEntity item = new ItemEntity(level(), spawnPos.x, spawnPos.y, spawnPos.z, stack.copy());
        item.setDeltaMovement(motion);
        item.hasImpulse = true;
        level().addFreshEntity(item);
    }
}
