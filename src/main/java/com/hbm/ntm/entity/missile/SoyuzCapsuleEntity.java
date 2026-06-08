package com.hbm.ntm.entity.missile;

import com.hbm.ntm.blockentity.SoyuzCapsuleBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class SoyuzCapsuleEntity extends Entity {
    public static final int PAYLOAD_SLOTS = 18;
    private static final EntityDataAccessor<Integer> SKIN =
            SynchedEntityData.defineId(SoyuzCapsuleEntity.class, EntityDataSerializers.INT);

    private final NonNullList<ItemStack> payload = NonNullList.withSize(PAYLOAD_SLOTS, ItemStack.EMPTY);

    public SoyuzCapsuleEntity(EntityType<? extends SoyuzCapsuleEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public SoyuzCapsuleEntity(Level level) {
        this(ModEntityTypes.SOYUZ_CAPSULE.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SKIN, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (getDeltaMovement().y > -0.2D) {
            setDeltaMovement(0.0D, getDeltaMovement().y - 0.01D, 0.0D);
        } else {
            setDeltaMovement(0.0D, -0.2D, 0.0D);
        }

        double nextY = getY() + getDeltaMovement().y;
        if (nextY > 600.0D) {
            nextY = 600.0D;
        }
        setPos(getX(), nextY, getZ());

        if (!level().isClientSide) {
            BlockPos current = BlockPos.containing(getX(), getY(), getZ());
            if (!level().isEmptyBlock(current)) {
                land(current.above());
            }
        }
    }

    private void land(BlockPos targetPos) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }

        BlockState capsuleState = ModBlocks.SOYUZ_CAPSULE.get().defaultBlockState();
        serverLevel.setBlock(targetPos, capsuleState, 3);
        if (serverLevel.getBlockEntity(targetPos) instanceof SoyuzCapsuleBlockEntity capsule) {
            for (int slot = 0; slot < PAYLOAD_SLOTS; slot++) {
                capsule.setCargoSlot(slot, payload.get(slot));
            }
            capsule.setRocketStack(SoyuzRocketItem.stackForSkin(ModItems.MISSILE_SOYUZ.get(), skin()));
        }
        discard();
    }

    public void setPayload(List<ItemStack> stacks) {
        for (int slot = 0; slot < Math.min(PAYLOAD_SLOTS, stacks.size()); slot++) {
            payload.set(slot, stacks.get(slot).copy());
        }
    }

    public int skin() {
        return entityData.get(SKIN);
    }

    public void setSkin(int skin) {
        entityData.set(SKIN, Math.max(0, Math.min(SoyuzRocketItem.SKIN_COUNT - 1, skin)));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setSkin(tag.getInt("soyuz"));
        HbmItemStackUtil.loadLegacyItems(tag, payload);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("soyuz", skin());
        HbmItemStackUtil.saveLegacyItemsToTag(tag, payload);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
