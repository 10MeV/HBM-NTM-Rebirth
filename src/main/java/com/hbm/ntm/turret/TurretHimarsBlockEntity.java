package com.hbm.ntm.turret;

import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TurretHimarsBlockEntity extends TurretBlockEntityBase {
    public static final int MODE_AUTO = 0;
    public static final int MODE_MANUAL = 1;

    private static final String TAG_MODE = "mode";

    private int mode;

    public TurretHimarsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_HIMARS.get(), pos, state, 1_000_000L);
    }

    public int getMode() {
        return mode;
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_himars";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_himars.png";
    }

    @Override
    protected double getDetectorRange() {
        return 5_000.0D;
    }

    @Override
    protected double getDetectorGrace() {
        return 250.0D;
    }

    @Override
    protected double getAcceptableInaccuracy() {
        return 5.0D;
    }

    @Override
    protected double getTurretYawSpeed() {
        return 1.0D;
    }

    @Override
    protected double getTurretPitchSpeed() {
        return 0.5D;
    }

    @Override
    protected double getHeightOffset() {
        return 5.0D;
    }

    @Override
    protected double getBarrelLength() {
        return 0.5D;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return artilleryTurretEnergyPorts();
    }

    @Override
    public List<ItemStack> getAmmoTypesForDisplay() {
        return LegacyArtilleryAmmoCatalog.himarsDisplayStacks();
    }

    protected LegacyArtilleryAmmoCatalog.HimarsRocket getFirstHimarsRocketLoaded() {
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            LegacyArtilleryAmmoCatalog.HimarsRocket rocket =
                    LegacyArtilleryAmmoCatalog.findHimarsRocket(getItems().getStackInSlot(slot));
            if (rocket != null) {
                return rocket;
            }
        }
        return null;
    }

    @Override
    protected boolean doesLineOfSightCheck() {
        return false;
    }

    @Override
    protected boolean canSeekNewTarget() {
        return mode != MODE_MANUAL;
    }

    @Override
    protected void turnTowards(Vec3 entityPos) {
        Vec3 delta = entityPos.subtract(getTurretPos());
        if (delta.lengthSqr() <= 0.0D) {
            return;
        }
        turnTowardsAngle(Math.PI / 4.0D, -Math.atan2(delta.x, delta.z));
    }

    @Override
    protected void updateFiringTick() {
        // Guided artillery rockets, reload crane state, and manual coordinate queue are deferred.
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if ("cycle_artillery_mode".equals(tag.getString("Action"))) {
            mode = mode == MODE_AUTO ? MODE_MANUAL : MODE_AUTO;
            clearTarget();
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
            return;
        }
        super.handleClientControl(player, tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putShort(TAG_MODE, (short) mode);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mode = tag.getShort(TAG_MODE);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putShort(TAG_MODE, (short) mode);
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putShort(TAG_MODE, (short) mode);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        mode = tag.getShort(TAG_MODE);
    }
}
