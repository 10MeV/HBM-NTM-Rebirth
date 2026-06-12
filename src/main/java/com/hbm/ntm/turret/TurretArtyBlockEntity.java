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

public class TurretArtyBlockEntity extends TurretBlockEntityBase {
    public static final int MODE_ARTILLERY = 0;
    public static final int MODE_CANNON = 1;
    public static final int MODE_MANUAL = 2;

    private static final String TAG_MODE = "mode";
    private static final String TAG_BARREL_POS = "BarrelPos";
    private static final String TAG_BARREL_RETRACTING = "BarrelRetracting";
    private static final double GRAVITY = 9.81D * 0.05D;

    private int mode;
    private float barrelPos;
    private float lastBarrelPos;
    private boolean barrelRetracting;

    public TurretArtyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_ARTY.get(), pos, state, 100_000L);
    }

    public int getMode() {
        return mode;
    }

    public float getBarrelPos() {
        return barrelPos;
    }

    public float getLastBarrelPos() {
        return lastBarrelPos;
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_arty";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_arty.png";
    }

    @Override
    protected double getDetectorRange() {
        return mode == MODE_CANNON ? 250.0D : 3_000.0D;
    }

    @Override
    protected double getDetectorGrace() {
        return mode == MODE_CANNON ? 32.0D : 250.0D;
    }

    @Override
    protected int getDetectorInterval() {
        return mode == MODE_CANNON ? 20 : 200;
    }

    @Override
    protected double getAcceptableInaccuracy() {
        return 0.0D;
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
    protected double getTurretDepression() {
        return 30.0D;
    }

    @Override
    protected double getTurretElevation() {
        return 90.0D;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return artilleryTurretEnergyPorts();
    }

    @Override
    public List<ItemStack> getAmmoTypesForDisplay() {
        return LegacyArtilleryAmmoCatalog.artyDisplayStacks();
    }

    protected LegacyArtilleryAmmoCatalog.ArtyShell getFirstArtyShellLoaded() {
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            LegacyArtilleryAmmoCatalog.ArtyShell shell =
                    LegacyArtilleryAmmoCatalog.findArtyShell(getItems().getStackInSlot(slot));
            if (shell != null) {
                return shell;
            }
        }
        return null;
    }

    @Override
    protected double getHeightOffset() {
        return 3.0D;
    }

    @Override
    protected double getBarrelLength() {
        return 9.0D;
    }

    @Override
    protected boolean doesLineOfSightCheck() {
        return mode == MODE_CANNON;
    }

    @Override
    protected boolean canSeekNewTarget() {
        return mode != MODE_MANUAL;
    }

    @Override
    protected void turnTowards(Vec3 entityPos) {
        Vec3 pos = getMuzzlePos();
        Vec3 delta = entityPos.subtract(pos);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontal <= 0.0D) {
            return;
        }
        double velocity = getLaunchVelocity();
        double velocitySquared = velocity * velocity;
        double discriminant = velocitySquared * velocitySquared
                - GRAVITY * (GRAVITY * horizontal * horizontal + 2.0D * delta.y * velocitySquared);
        if (discriminant < 0.0D) {
            super.turnTowards(entityPos);
            return;
        }
        double targetYaw = -Math.atan2(delta.x, delta.z);
        double upperLower = mode == MODE_CANNON ? -1.0D : 1.0D;
        double targetPitch = Math.atan((velocitySquared + Math.sqrt(discriminant) * upperLower)
                / (GRAVITY * horizontal));
        turnTowardsAngle(targetPitch, targetYaw);
    }

    protected double getLaunchVelocity() {
        return mode == MODE_CANNON ? 20.0D : 50.0D;
    }

    @Override
    protected void updateFiringTick() {
        // Artillery shell entities and manual coordinate queue are migrated in a later projectile/ROR batch.
    }

    protected void triggerBarrelRetract() {
        startBarrelRetract();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void startBarrelRetract() {
        barrelRetracting = true;
    }

    @Override
    protected void tickClientSpecificAnimations() {
        lastBarrelPos = barrelPos;
        tickBarrelAnimation();
    }

    @Override
    protected void tickServerSpecificAnimations() {
        tickBarrelAnimation();
    }

    private void tickBarrelAnimation() {
        if (barrelRetracting) {
            barrelPos += 0.5F;
            if (barrelPos >= 1.0F) {
                barrelPos = 1.0F;
                barrelRetracting = false;
            }
        } else if (barrelPos > 0.0F) {
            barrelPos = Math.max(0.0F, barrelPos - 0.05F);
        }
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if ("cycle_artillery_mode".equals(tag.getString("Action"))) {
            mode = (mode + 1) % 3;
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
        tag.putFloat(TAG_BARREL_POS, barrelPos);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mode = tag.getShort(TAG_MODE);
        barrelPos = tag.getFloat(TAG_BARREL_POS);
        lastBarrelPos = barrelPos;
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putShort(TAG_MODE, (short) mode);
        writeArtyAnimationSync(tag);
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putShort(TAG_MODE, (short) mode);
        writeArtyAnimationSync(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        mode = tag.getShort(TAG_MODE);
        if (tag.contains(TAG_BARREL_POS)) {
            lastBarrelPos = barrelPos;
            barrelPos = tag.getFloat(TAG_BARREL_POS);
        }
        if (tag.getBoolean(TAG_BARREL_RETRACTING)) {
            startBarrelRetract();
        }
    }

    private void writeArtyAnimationSync(CompoundTag tag) {
        tag.putFloat(TAG_BARREL_POS, barrelPos);
        tag.putBoolean(TAG_BARREL_RETRACTING, barrelRetracting);
    }
}
