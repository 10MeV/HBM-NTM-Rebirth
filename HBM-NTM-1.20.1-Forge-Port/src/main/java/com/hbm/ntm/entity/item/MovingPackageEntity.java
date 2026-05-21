package com.hbm.ntm.entity.item;

import com.hbm.ntm.api.conveyor.IConveyorPackage;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MovingPackageEntity extends MovingConveyorObjectEntity implements IConveyorPackage {
    private ItemStack[] contents = new ItemStack[0];

    public MovingPackageEntity(EntityType<? extends MovingPackageEntity> type, Level level) {
        super(type, level);
    }

    public MovingPackageEntity(Level level, ItemStack[] contents) {
        this(ModEntityTypes.MOVING_PACKAGE.get(), level);
        setItemStacks(contents);
    }

    public void setItemStacks(ItemStack[] stacks) {
        contents = copyStacks(stacks);
    }

    @Override
    public ItemStack[] getItemStacks() {
        return copyStacks(contents);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && !isRemoved()) {
            for (ItemStack stack : contents) {
                if (!stack.isEmpty() && !player.getInventory().add(stack.copy())) {
                    level().addFreshEntity(new ItemEntity(level(), getX(), getY() + 0.125D, getZ(), stack.copy()));
                }
            }
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && !isRemoved()) {
            dropContents(position(), getDeltaMovement().multiply(2.0D, 0.0D, 2.0D).add(0.0D, 0.1D, 0.0D));
            discard();
        }
        return true;
    }

    @Override
    public void enterBlock(IEnterableBlock enterable, BlockPos pos, Direction side) {
        if (isRemoved()) {
            return;
        }

        if (enterable.canPackageEnter(level(), pos, side, this)) {
            enterable.onPackageEnter(level(), pos, side, this);
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
        dropContents(spawnPos, new Vec3(motion.x * 2.0D, 0.1D, motion.z * 2.0D));
        discard();
        return true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        contents = new ItemStack[tag.getInt("count")];
        ListTag list = tag.getList("contents", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag stackTag = list.getCompound(i);
            int slot = stackTag.getByte("slot") & 255;
            if (slot >= 0 && slot < contents.length) {
                contents[slot] = ItemStack.of(stackTag);
            }
        }
        contents = copyStacks(contents);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        ListTag list = new ListTag();
        for (int i = 0; i < contents.length; i++) {
            if (!contents[i].isEmpty()) {
                CompoundTag stackTag = contents[i].save(new CompoundTag());
                stackTag.putByte("slot", (byte) i);
                list.add(stackTag);
            }
        }
        tag.put("contents", list);
        tag.putInt("count", contents.length);
    }

    private void dropContents(Vec3 spawnPos, Vec3 motion) {
        for (ItemStack stack : contents) {
            if (stack.isEmpty()) {
                continue;
            }

            ItemEntity item = new ItemEntity(level(), spawnPos.x, spawnPos.y, spawnPos.z, stack.copy());
            item.setDeltaMovement(motion);
            item.hasImpulse = true;
            level().addFreshEntity(item);
        }
    }

    private static ItemStack[] copyStacks(ItemStack[] stacks) {
        if (stacks == null) {
            return new ItemStack[0];
        }

        ItemStack[] copy = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            copy[i] = stacks[i] == null ? ItemStack.EMPTY : stacks[i].copy();
        }
        return copy;
    }
}
