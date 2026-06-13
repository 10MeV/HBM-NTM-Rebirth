package com.hbm.ntm.turret;

import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.entity.projectile.ArtilleryRocketEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TurretHimarsBlockEntity extends TurretBlockEntityBase implements ArtilleryTargetReceiver {
    public static final int MODE_AUTO = 0;
    public static final int MODE_MANUAL = 1;

    private static final String TAG_MODE = "mode";
    private static final String TAG_TYPE_LOADED = "type";
    private static final String TAG_AMMO = "ammo";
    private static final String TAG_CRANE = "Crane";

    private int mode;
    private int typeLoaded = -1;
    private int ammo;
    private float crane;
    private float lastCrane;
    private int timer;
    private final ArtilleryTargetQueue targetQueue = new ArtilleryTargetQueue();

    public TurretHimarsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_HIMARS.get(), pos, state, 1_000_000L);
    }

    public int getMode() {
        return mode;
    }

    public int getTypeLoaded() {
        return typeLoaded;
    }

    public int getAmmoLoaded() {
        return ammo;
    }

    public float getCrane() {
        return crane;
    }

    public float getLastCrane() {
        return lastCrane;
    }

    public boolean hasAmmo() {
        return typeLoaded >= 0 && ammo > 0;
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

    private int getSpareRocketIndex() {
        List<LegacyArtilleryAmmoCatalog.HimarsRocket> rockets = LegacyArtilleryAmmoCatalog.himarsRockets();
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            LegacyArtilleryAmmoCatalog.HimarsRocket rocket =
                    LegacyArtilleryAmmoCatalog.findHimarsRocket(getItems().getStackInSlot(slot));
            if (rocket != null) {
                return rockets.indexOf(rocket);
            }
        }
        return -1;
    }

    private boolean consumeSpareRocket(int rocketIndex) {
        List<LegacyArtilleryAmmoCatalog.HimarsRocket> rockets = LegacyArtilleryAmmoCatalog.himarsRockets();
        if (rocketIndex < 0 || rocketIndex >= rockets.size()) {
            return false;
        }
        LegacyArtilleryAmmoCatalog.HimarsRocket rocket = rockets.get(rocketIndex);
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            ItemStack stack = getItems().getStackInSlot(slot);
            if (LegacyArtilleryAmmoCatalog.findHimarsRocket(stack) == rocket) {
                stack.shrink(1);
                getItems().setStackInSlot(slot, stack);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean doesLineOfSightCheck() {
        return false;
    }

    @Override
    protected boolean canAcquireTarget(Entity entity) {
        return entityInSurfaceTargetEnvelope(entity);
    }

    @Override
    protected boolean canSeekNewTarget() {
        return mode != MODE_MANUAL;
    }

    @Override
    protected boolean shouldClearTargetWhenSeekingDisabled() {
        return false;
    }

    @Override
    protected void updateManualTargeting() {
        targetQueue.applyManualTarget(this, mode == MODE_MANUAL);
    }

    @Override
    protected void updateTargetingBeforeMovement() {
        if (!isActive() || !hasPower() || (hasAmmo() && crane <= 0.0F)) {
            return;
        }
        turnTowardsAngle(0.0D, getRotationYaw());
        if (!isAligned()) {
            return;
        }
        float oldCrane = crane;
        int oldType = typeLoaded;
        int oldAmmo = ammo;
        if (hasAmmo()) {
            crane -= 0.0125F;
        } else {
            crane += 0.0125F;
            if (crane >= 1.0F) {
                int available = getSpareRocketIndex();
                if (available != -1) {
                    LegacyArtilleryAmmoCatalog.HimarsRocket rocket =
                            LegacyArtilleryAmmoCatalog.himarsRockets().get(available);
                    if (consumeSpareRocket(available)) {
                        typeLoaded = available;
                        ammo = rocket.amount();
                    }
                }
            }
        }
        crane = Mth.clamp(crane, 0.0F, 1.0F);
        if (oldCrane != crane || oldType != typeLoaded || oldAmmo != ammo) {
            setChanged();
        }
    }

    @Override
    protected boolean shouldTurnTowardTargetPosition() {
        return hasAmmo() && crane <= 0.0F;
    }

    @Override
    protected boolean shouldUpdateFiringTick() {
        return hasAmmo() && crane <= 0.0F;
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
        timer++;
        if (timer % 40 != 0) {
            return;
        }
        Vec3 target = getTargetPos();
        if (hasAmmo() && target != null && spawnShell(typeLoaded, target)) {
            ammo--;
            playTurretSound("hbm:weapon.rocketFlame", 25.0F, 1.0F);
            setChanged();
            syncRuntimeToTracking();
        }

        if (mode == MODE_MANUAL && !targetQueue.isEmpty()) {
            targetQueue.removeFirst();
            clearTarget();
            syncRuntimeToTracking();
        }
    }

    private boolean spawnShell(int rocketIndex, Vec3 targetPos) {
        if (level == null || level.isClientSide || targetPos == null) {
            return false;
        }
        ArtilleryRocketEntity rocket = new ArtilleryRocketEntity(level);
        Vec3 muzzle = getMuzzlePos();
        rocket.setPos(muzzle.x, muzzle.y, muzzle.z);
        rocket.shoot(getBarrelHeading(), 25.0F, 0.0F);
        Entity target = getTarget();
        if (target != null) {
            rocket.setTarget(target);
        } else {
            rocket.setTarget(targetPos.x, targetPos.y, targetPos.z);
        }
        rocket.setType(rocketIndex);
        return level.addFreshEntity(rocket);
    }

    @Override
    protected void tickClientSpecificAnimations() {
        lastCrane = crane;
    }

    @Override
    public boolean enqueueTarget(double x, double y, double z) {
        return targetQueue.enqueue(this, x, y, z);
    }

    @Override
    public boolean sendCommandPosition(int x, int y, int z) {
        return targetQueue.sendCommandPosition(this, x, y, z);
    }

    @Override
    public boolean sendCommandEntity(Entity target) {
        return targetQueue.sendCommandEntity(this, target);
    }

    @Override
    public String[] getFunctionInfo() {
        return targetQueue.appendFunctionInfo(super.getFunctionInfo());
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        super.runRORFunction(name, params);
        return targetQueue.runRORFunction(this, name, params);
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if ("cycle_artillery_mode".equals(tag.getString("Action"))) {
            mode = mode == MODE_AUTO ? MODE_MANUAL : MODE_AUTO;
            clearTarget();
            targetQueue.clear();
            syncRuntimeToTracking();
            return;
        }
        super.handleClientControl(player, tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putShort(TAG_MODE, (short) mode);
        tag.putShort(TAG_TYPE_LOADED, (short) typeLoaded);
        tag.putInt(TAG_AMMO, ammo);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mode = tag.getShort(TAG_MODE);
        typeLoaded = tag.contains(TAG_TYPE_LOADED) ? tag.getShort(TAG_TYPE_LOADED) : -1;
        ammo = tag.getInt(TAG_AMMO);
        crane = 0.0F;
        lastCrane = crane;
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putShort(TAG_MODE, (short) mode);
        writeHimarsLoadingSync(tag);
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putShort(TAG_MODE, (short) mode);
        writeHimarsLoadingSync(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        mode = tag.getShort(TAG_MODE);
        if (tag.contains(TAG_TYPE_LOADED)) {
            typeLoaded = tag.getShort(TAG_TYPE_LOADED);
        }
        if (tag.contains(TAG_AMMO)) {
            ammo = tag.getInt(TAG_AMMO);
        }
        if (tag.contains(TAG_CRANE)) {
            lastCrane = crane;
            crane = tag.getFloat(TAG_CRANE);
        }
    }

    private void writeHimarsLoadingSync(CompoundTag tag) {
        tag.putShort(TAG_TYPE_LOADED, (short) typeLoaded);
        tag.putInt(TAG_AMMO, ammo);
        tag.putFloat(TAG_CRANE, crane);
    }
}
