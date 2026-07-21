package com.hbm.ntm.entity.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.DroneItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.world.ChunkShapeHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashSet;
import java.util.Set;

/** 18-slot patrol transport drone. Chunk ticket ownership is wired in the logistics service slice. */
public class DeliveryDroneEntity extends DroneBaseEntity {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> EXPRESS =
            net.minecraft.network.syncher.SynchedEntityData.defineId(DeliveryDroneEntity.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private final net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(18, ItemStack.EMPTY);
    private FluidStack fluid = FluidStack.EMPTY;
    private boolean chunkLoading;
    private final Set<Long> forcedChunks = new HashSet<>();

    public DeliveryDroneEntity(EntityType<? extends DeliveryDroneEntity> type, Level level) { super(type, level); }
    public void setExpress(boolean express) { entityData.set(EXPRESS, express); }
    public boolean express() { return entityData.get(EXPRESS); }
    public void setChunkLoading(boolean chunkLoading) { this.chunkLoading = chunkLoading; if (!chunkLoading) clearChunkLoader(); }
    public boolean chunkLoading() { return chunkLoading; }
    /** EntityDeliveryDrone overrides the base speed to 0.375, or 1.125 for express variants. */
    @Override public double speed() { return express() ? 1.125D : 0.375D; }
    public ItemStack getCargo(int slot) { return items.get(slot); }
    public void setCargo(int slot, ItemStack stack) { items.set(slot, stack.copy()); }
    public FluidStack fluid() { return fluid; }
    public void setFluid(FluidStack fluid) { this.fluid = fluid == null ? FluidStack.EMPTY : fluid.copy(); }

    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(EXPRESS, false); }
    @Override protected void beforeServerMove(Vec3 motion) { if (chunkLoading) loadNeighboringChunks(motion); }
    @Override public boolean hurt(DamageSource source, float amount) {
        // EntityDeliveryDrone#hitByEntity returned immediately once setDead() had run.
        // Keep a repeated damage callback from duplicating its cargo/drone drops before the
        // removed entity is fully pruned from the level.
        if (isRemoved()) return false;
        if (!level().isClientSide && source.getEntity() instanceof Player) {
            dropCargo();
            spawnAtLocation(DroneItem.withType(new ItemStack(ModItems.DRONE.get()), chunkLoading
                    ? express() ? DroneItem.DroneType.PATROL_EXPRESS_CHUNKLOADING : DroneItem.DroneType.PATROL_CHUNKLOADING
                    : express() ? DroneItem.DroneType.PATROL_EXPRESS : DroneItem.DroneType.PATROL));
            discard();
        }
        return false;
    }
    private void dropCargo() { for (ItemStack stack : items) if (!stack.isEmpty()) spawnAtLocation(stack.copy()); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); ContainerHelper.saveAllItems(tag, items); tag.put("fluid", fluid.writeToNBT(new CompoundTag()));
        tag.putBoolean("load", express()); tag.putBoolean("chunkLoading", chunkLoading);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); ContainerHelper.loadAllItems(tag, items); fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("fluid"));
        setExpress(tag.getBoolean("load")); chunkLoading = tag.getBoolean("chunkLoading");
    }
    /**
     * Exact modern equivalent of EntityDeliveryDrone#loadNeighboringChunks: replace the
     * entity-owned ticket set every tick with chunks along the imminent motion line, padded
     * by eight blocks.  Saved ticket ids are deliberately not reused; the first post-load
     * movement tick recreates them for this entity.
     */
    private void loadNeighboringChunks(Vec3 motion) {
        if (!(level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        clearChunkLoader();
        int startX = (int) Math.floor(getX()), startZ = (int) Math.floor(getZ());
        int endX = (int) Math.floor(getX() + motion.x), endZ = (int) Math.floor(getZ() + motion.z);
        for (ChunkPos chunk : ChunkShapeHelper.getChunksAlongLineSegment(startX, startZ, endX, endZ, 8.0D)) {
            ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, chunk.x, chunk.z, true, true);
            forcedChunks.add(chunk.toLong());
        }
    }
    private void clearChunkLoader() {
        if (!(level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) { forcedChunks.clear(); return; }
        for (long packed : forcedChunks) {
            ChunkPos chunk = new ChunkPos(packed);
            ForgeChunkManager.forceChunk(serverLevel, HbmNtm.MOD_ID, this, chunk.x, chunk.z, false, true);
        }
        forcedChunks.clear();
    }
    @Override public void remove(RemovalReason reason) { clearChunkLoader(); super.remove(reason); }
}
