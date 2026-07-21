package com.hbm.ntm.entity.item;

import com.hbm.ntm.blockentity.SupplyCrateBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import java.util.ArrayList;
import java.util.List;

/** Source-backed falling C130 payload container. */
public class ParachuteCrateEntity extends Entity {
    private final List<ItemStack> items = new ArrayList<>();

    public ParachuteCrateEntity(EntityType<? extends ParachuteCrateEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }
    public ParachuteCrateEntity(Level level) { this(ModEntityTypes.PARACHUTE_CRATE.get(), level); }
    public void addItem(ItemStack stack) { if (!stack.isEmpty()) items.add(stack.copy()); }

    @Override public void tick() {
        super.tick();
        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);
        if (getDeltaMovement().y > -0.2D) setDeltaMovement(getDeltaMovement().add(0.0D, -0.02D, 0.0D));
        if (getY() > 600.0D) setPos(getX(), 600.0D, getZ());
        BlockPos hitPos = BlockPos.containing(getX(), getY(), getZ());
        if (!level().getBlockState(hitPos).isAir()) {
            if (!level().isClientSide()) {
                BlockPos cratePos = hitPos.above();
                level().setBlock(cratePos, ModBlocks.CRATE_SUPPLY.get().defaultBlockState(), 3);
                if (level().getBlockEntity(cratePos) instanceof SupplyCrateBlockEntity crate) crate.addItems(items);
            }
            discard();
        }
    }
    @Override protected void defineSynchedData() { }
    @Override public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        items.clear();
        ListTag list = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) addItem(ItemStack.of(list.getCompound(i)));
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ItemStack item : items) list.add(item.save(new CompoundTag()));
        tag.put("items", list);
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
