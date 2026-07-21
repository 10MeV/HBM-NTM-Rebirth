package com.hbm.ntm.entity.missile;

import com.hbm.ntm.blockentity.SoyuzCapsuleBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

public class SoyuzCapsuleEntity extends Entity {
    public static final int PAYLOAD_SLOTS = 18;

    // Public and nullable exactly as EntitySoyuzCapsule#payload.  Soyuz cargo
    // deployment assigns its whole ItemStack[] reference here.
    public ItemStack[] payload = new ItemStack[PAYLOAD_SLOTS];
    // EntitySoyuzCapsule stores this as an ordinary public field, not a data
    // watcher entry. Its live spawn/update packets therefore do not carry the
    // recovered-rocket skin; only the entity's NBT save path restores it.
    public int soyuz;

    public SoyuzCapsuleEntity(EntityType<? extends SoyuzCapsuleEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public SoyuzCapsuleEntity(Level level) {
        this(ModEntityTypes.SOYUZ_CAPSULE.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        // Entity requires this modern lifecycle hook, but the 1.7.10 capsule
        // registered no data watcher entries.
    }

    @Override
    public void tick() {
        // EntitySoyuzCapsule overrides EntityThrowable#onUpdate without entering
        // its parent update.  Keep only the previous-position bookkeeping that
        // the old override performed; parent ticking would add modern entity
        // physics and advance state the legacy capsule never touched.
        xo = getX();
        yo = getY();
        zo = getZ();

        // EntitySoyuzCapsule moves first, then applies gravity.  Applying gravity
        // before movement advances every landing check by one descent increment.
        double nextY = getY() + getDeltaMovement().y;
        if (nextY > 600.0D) {
            nextY = 600.0D;
        }
        setPos(getX() + getDeltaMovement().x, nextY, getZ() + getDeltaMovement().z);

        if (getDeltaMovement().y > -0.2D) {
            setDeltaMovement(getDeltaMovement().x, getDeltaMovement().y - 0.02D, getDeltaMovement().z);
        }

        // EntitySoyuzCapsule queried world.getBlock((int) posX, (int) posY,
        // (int) posZ) on both sides. Do not replace this with BlockPos.containing:
        // it floors negative positions and moves a cargo Soyuz landing column by one
        // block west/north. The client must also disappear on impact immediately.
        BlockPos current = new BlockPos((int) getX(), (int) getY(), (int) getZ());
        if (!level().isEmptyBlock(current)) {
            if (level().isClientSide) {
                discard();
            } else {
                land(current.above());
            }
        }
    }

    private void land(BlockPos targetPos) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }

        // EntitySoyuzCapsule calls setDead before placing its recovery block
        // and transferring the payload. Keep that lifecycle ordering rather
        // than leaving the falling entity live during the server-side writes.
        discard();
        BlockState capsuleState = ModBlocks.SOYUZ_CAPSULE.get().defaultBlockState();
        serverLevel.setBlock(targetPos, capsuleState, 3);
        if (serverLevel.getBlockEntity(targetPos) instanceof SoyuzCapsuleBlockEntity capsule) {
            for (int slot = 0; slot < PAYLOAD_SLOTS; slot++) {
                // The old TileEntity inventory accepted null.  Forge's
                // ItemStackHandler cannot, so preserve null on the entity side
                // and translate only at this capability-inventory boundary.
                capsule.setCargoSlot(slot, payload[slot] == null ? ItemStack.EMPTY : payload[slot]);
            }
            // EntitySoyuzCapsule constructed its recovered missile with the
            // raw soyuz watcher value as item metadata; its renderer/item UI
            // decides how out-of-range skins display later.
            capsule.setRocketStack(SoyuzRocketItem.stackForEntitySkin(ModItems.MISSILE_SOYUZ.get(), skin()));
        }
    }

    public int skin() {
        return soyuz;
    }

    public void setSkin(int skin) {
        // EntitySoyuzCapsule persists this ordinary field verbatim. Keep that
        // NBT contract separate from the item stack's intentional skin-NBT
        // clamp, and do not turn it into live entity synchronization.
        soyuz = skin;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setSkin(tag.getInt("soyuz"));
        // Match EntitySoyuzCapsule#readEntityFromNBT: only slots represented
        // in the legacy list are replaced; absent slots retain prior cargo.
        HbmItemStackUtil.loadSlottedItems(tag.getList("items", net.minecraft.nbt.Tag.TAG_COMPOUND),
                "slot", payload);
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
