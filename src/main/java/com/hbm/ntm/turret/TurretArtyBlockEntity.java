package com.hbm.ntm.turret;

import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.entity.projectile.ArtilleryShellEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TurretArtyBlockEntity extends TurretBlockEntityBase implements ArtilleryTargetReceiver {
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
    private int timer;
    private int artyCasingDelay;
    private String queuedArtyCasingName = "";
    private final ArtilleryTargetQueue targetQueue = new ArtilleryTargetQueue();

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

    private int getFirstArtyShellIndexLoaded() {
        LegacyArtilleryAmmoCatalog.ArtyShell shell = getFirstArtyShellLoaded();
        return shell == null ? -1 : LegacyArtilleryAmmoCatalog.artyShells().indexOf(shell);
    }

    private boolean consumeArtyShell(int shellIndex) {
        if (shellIndex < 0 || shellIndex >= LegacyArtilleryAmmoCatalog.artyShells().size()) {
            return false;
        }
        LegacyArtilleryAmmoCatalog.ArtyShell shell = LegacyArtilleryAmmoCatalog.artyShells().get(shellIndex);
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            ItemStack stack = getItems().getStackInSlot(slot);
            if (LegacyArtilleryAmmoCatalog.findArtyShell(stack) == shell) {
                stack.shrink(1);
                getItems().setStackInSlot(slot, stack);
                return true;
            }
        }
        return false;
    }

    private ItemStack cargoForFirstShell(int shellIndex) {
        if (shellIndex < 0 || shellIndex >= LegacyArtilleryAmmoCatalog.artyShells().size()) {
            return ItemStack.EMPTY;
        }
        LegacyArtilleryAmmoCatalog.ArtyShell shell = LegacyArtilleryAmmoCatalog.artyShells().get(shellIndex);
        if (shell != LegacyArtilleryAmmoCatalog.AMMO_ARTY_CARGO) {
            return ItemStack.EMPTY;
        }
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            ItemStack stack = getItems().getStackInSlot(slot);
            if (LegacyArtilleryAmmoCatalog.findArtyShell(stack) == shell
                    && stack.hasTag() && stack.getTag().contains("cargo")) {
                return ItemStack.of(stack.getTag().getCompound("cargo"));
            }
        }
        return ItemStack.EMPTY;
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
    protected boolean canAcquireTarget(Entity entity) {
        return doesLineOfSightCheck() ? super.canAcquireTarget(entity) : entityInSurfaceTargetEnvelope(entity);
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
        timer++;
        int delay = mode == MODE_ARTILLERY ? 300 : 40;
        if (timer % delay != 0) {
            return;
        }

        Vec3 target = getTargetPos();
        int shellIndex = getFirstArtyShellIndexLoaded();
        if (target != null && shellIndex >= 0) {
            ItemStack cargo = cargoForFirstShell(shellIndex);
            if (spawnShell(shellIndex, target, cargo) && consumeArtyShell(shellIndex)) {
                scheduleArtyCasing(LegacyArtilleryAmmoCatalog.artyShells().get(shellIndex).legacyName());
                playTurretSound("hbm:turret.jeremy_fire", 25.0F, 1.0F);
                spawnMuzzleLargeExplode(0.0F, 5);
                triggerBarrelRetract();
                setChanged();
            }
        }

        if (mode == MODE_MANUAL && !targetQueue.isEmpty()) {
            targetQueue.removeFirst();
            clearTarget();
            syncRuntimeToTracking();
        }
    }

    private boolean spawnShell(int shellIndex, Vec3 target, ItemStack cargo) {
        if (level == null || level.isClientSide || target == null) {
            return false;
        }
        ArtilleryShellEntity shell = new ArtilleryShellEntity(level);
        Vec3 muzzle = getMuzzlePos();
        shell.setPos(muzzle.x, muzzle.y, muzzle.z);
        shell.shoot(getBarrelHeading(), (float) getLaunchVelocity(), 0.0F);
        shell.setTarget(target.x, target.y, target.z);
        shell.setType(shellIndex);
        if (!cargo.isEmpty()) {
            shell.setCargo(cargo);
        }
        if (mode != MODE_CANNON) {
            shell.setWhistle(true);
        }
        return level.addFreshEntity(shell);
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

    protected void triggerBarrelRetract() {
        startBarrelRetract();
        syncRuntimeToTracking();
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
        tickArtyCasingDelay();
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

    private void scheduleArtyCasing(String casingName) {
        if (casingName == null || casingName.isBlank()) {
            return;
        }
        queuedArtyCasingName = casingName;
        artyCasingDelay = 7;
    }

    private void tickArtyCasingDelay() {
        if (queuedArtyCasingName.isBlank()) {
            return;
        }
        if (artyCasingDelay > 0) {
            artyCasingDelay--;
            return;
        }
        spawnDirectCasing(getTurretPos(),
                (float) Math.toDegrees(getRotationYaw()),
                (float) -Math.toDegrees(getRotationPitch()),
                -0.6D, 0.3D, 0.0D, 0.01D,
                level == null ? 0.0F : level.random.nextFloat() * 20.0F - 10.0F,
                0.0F,
                queuedArtyCasingName,
                true, 200, 1.0D, 20);
        queuedArtyCasingName = "";
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if ("cycle_artillery_mode".equals(tag.getString("Action"))) {
            mode = (mode + 1) % 3;
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mode = tag.getShort(TAG_MODE);
        barrelPos = 0.0F;
        lastBarrelPos = barrelPos;
        barrelRetracting = false;
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
