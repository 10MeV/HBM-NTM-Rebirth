package com.hbm.ntm.entity.item;

import com.hbm.ntm.api.conveyor.IConveyorPackage;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.network.HbmEntitySyncable;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MovingPackageEntity extends MovingConveyorObjectEntity implements IConveyorPackage, HbmEntitySyncable {
    private static final String TAG_CONTENTS = "contents";
    private static final String TAG_COUNT = "count";
    private static final String TAG_SLOT = "slot";

    private ItemStack[] contents = new ItemStack[0];
    private boolean needsSync = true;

    public MovingPackageEntity(EntityType<? extends MovingPackageEntity> type, Level level) {
        super(type, level);
    }

    public MovingPackageEntity(Level level, ItemStack[] contents) {
        this(ModEntityTypes.MOVING_PACKAGE.get(), level);
        setItemStacks(contents);
    }

    public void setItemStacks(ItemStack[] stacks) {
        contents = copyStacks(stacks);
        needsSync = true;
    }

    @Override
    public ItemStack[] getItemStacks() {
        return copyStacks(contents);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && !isRemoved()) {
            for (ItemStack stack : contents) {
                HbmItemStackUtil.giveOrDrop(player, stack, level(), getX(), getY() + 0.125D, getZ());
            }
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && !isRemoved() && needsSync) {
            ModMessages.syncEntityToTracking(this, this);
            needsSync = false;
        }
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
        readContents(tag);
        needsSync = true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        writeContents(tag);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        writeContents(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        readContents(tag);
    }

    private void readContents(CompoundTag tag) {
        contents = new ItemStack[tag.getInt(TAG_COUNT)];
        HbmItemStackUtil.loadSlottedItems(tag, TAG_CONTENTS, TAG_SLOT, contents);
        contents = copyStacks(contents);
    }

    private void writeContents(CompoundTag tag) {
        HbmItemStackUtil.saveSlottedItemsToTag(tag, TAG_CONTENTS, TAG_SLOT, contents);
        tag.putInt(TAG_COUNT, contents.length);
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
        return HbmItemStackUtil.carefulCopyArray(stacks);
    }
}
