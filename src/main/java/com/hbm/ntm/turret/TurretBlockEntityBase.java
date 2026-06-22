package com.hbm.ntm.turret;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.BulletCasingEjectUtil;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.HbmEnergyBlockEntity;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.compat.CompatTurretTargetRegistry;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.item.TurretBiometryItem;
import com.hbm.ntm.menu.TurretMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmWorldUtil;
import com.hbm.ntm.api.redstoneoverradio.ROR;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class TurretBlockEntityBase extends HbmEnergyBlockEntity implements MenuProvider, HbmEnergyReceiver, RORInteractive {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_IS_ON = "isOn";
    private static final String TAG_TARGET_PLAYERS = "targetPlayers";
    private static final String TAG_TARGET_FRIENDLY = "targetAnimals";
    private static final String TAG_TARGET_HOSTILE = "targetMobs";
    private static final String TAG_TARGET_MACHINES = "targetMachines";
    private static final String TAG_STATTRAK = "stattrak";
    private static final String TAG_ROTATION_YAW = "RotationYaw";
    private static final String TAG_ROTATION_PITCH = "RotationPitch";
    private static final String TAG_TARGET_PRESENT = "TargetPresent";
    private static final String TAG_TARGET_X = "TargetX";
    private static final String TAG_TARGET_Y = "TargetY";
    private static final String TAG_TARGET_Z = "TargetZ";
    private static final String TAG_SPIN = "Spin";
    private static final String TAG_BARREL_LEFT = "BarrelLeft";
    private static final String TAG_BARREL_RIGHT = "BarrelRight";
    private static final String TAG_BEAM_TICKS = "BeamTicks";
    private static final String TAG_BEAM_DISTANCE = "BeamDistance";
    private static final String TAG_ACTION = "Action";
    private static final String TAG_TYPE = "Type";
    private static final String TAG_NAME = "Name";
    private static final String TAG_INDEX = "Index";
    private static final long DEFAULT_MAX_POWER = 100_000L;
    private static final double LEGACY_SYNC_RANGE = 250.0D;
    private static final double RENDER_BOUNDS_HORIZONTAL = 6.0D;
    private static final double RENDER_BOUNDS_HEIGHT = 7.0D;
    private static final double RENDER_BEAM_PAD = 1.0D;

    public static final int SLOT_CHIP = 0;
    public static final int SLOT_AMMO_START = 1;
    public static final int SLOT_AMMO_END = 9;
    public static final int SLOT_BATTERY = 10;
    public static final int SLOT_COUNT = 11;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> externalItemHandler =
            LazyOptional.of(() -> new TurretExternalItemHandler(this, items));

    private double rotationYaw;
    private double rotationPitch;
    private double lastRotationYaw;
    private double lastRotationPitch;
    private double syncRotationYaw;
    private double syncRotationPitch;
    private float spin;
    private float lastSpin;
    protected float barrelLeftPos;
    protected float lastBarrelLeftPos;
    protected float barrelRightPos;
    protected float lastBarrelRightPos;
    private int beamTicks;
    private double beamDistance;
    private int casingDelay;
    @Nullable
    private BulletConfig queuedCasingConfig;
    private int turnProgress;
    private boolean isOn;
    private boolean aligned;
    private int searchTimer;
    private boolean targetPlayers;
    private boolean targetFriendly;
    private boolean targetHostile = true;
    private boolean targetMachines = true;
    @Nullable
    private Entity target;
    @Nullable
    private Vec3 targetPos;
    private int stattrak;

    protected TurretBlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, new HbmEnergyStorage(DEFAULT_MAX_POWER, DEFAULT_MAX_POWER, 0L));
    }

    protected TurretBlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state, long maxPower) {
        super(type, pos, state, new HbmEnergyStorage(maxPower, maxPower, 0L));
    }

    protected TurretBlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state, long maxPower,
            long maxReceive) {
        super(type, pos, state, new HbmEnergyStorage(maxPower, Math.min(maxReceive, maxPower), 0L));
    }

    @Override
    public AABB getRenderBoundingBox() {
        Vec3 pivot = Vec3.atLowerCornerOf(worldPosition).add(getRenderHorizontalOffset());
        AABB bounds = new AABB(
                pivot.x - RENDER_BOUNDS_HORIZONTAL, pivot.y, pivot.z - RENDER_BOUNDS_HORIZONTAL,
                pivot.x + RENDER_BOUNDS_HORIZONTAL, pivot.y + RENDER_BOUNDS_HEIGHT,
                pivot.z + RENDER_BOUNDS_HORIZONTAL);
        if (beamTicks > 0 && beamDistance > 0.0D) {
            Vec3 start = getTurretPos();
            Vec3 end = start.add(getBarrelHeading().scale(beamDistance));
            bounds = bounds.minmax(new AABB(start, end).inflate(RENDER_BEAM_PAD));
        }
        return bounds;
    }

    @Override
    public long getReceiverSpeed() {
        return energy.getReceiverSpeed();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TurretBlockEntityBase turret) {
        long oldPower = turret.getPower();
        boolean oldOn = turret.isActive();
        boolean oldAligned = turret.aligned;
        double oldYaw = turret.rotationYaw;
        double oldPitch = turret.rotationPitch;
        float oldBarrelLeft = turret.barrelLeftPos;
        float oldBarrelRight = turret.barrelRightPos;
        int oldBeamTicks = turret.beamTicks;
        double oldBeamDistance = turret.beamDistance;

        turret.decayServerAnimations();
        turret.tickServerSpecificAnimations();
        if (turret.usesEnergy()) {
            turret.subscribeEnergyReceiverToPorts();
        }
        turret.updateServerTick();
        turret.tickTargeting();
        if (turret.usesEnergy()) {
            HbmEnergyUtil.chargeStorageFromItem(turret.items.getStackInSlot(SLOT_BATTERY), turret, turret.getReceiverSpeed());
        }
        turret.updateServerTickAfterTargeting();

        boolean changed = oldPower != turret.getPower()
                || oldOn != turret.isActive()
                || oldAligned != turret.aligned
                || oldYaw != turret.rotationYaw
                || oldPitch != turret.rotationPitch
                || oldBarrelLeft != turret.barrelLeftPos
                || oldBarrelRight != turret.barrelRightPos
                || oldBeamTicks != turret.beamTicks
                || oldBeamDistance != turret.beamDistance;
        if (changed) {
            turret.setChanged();
        }
        turret.networkPackTurretLikeLegacy();
        turret.updateServerTickAfterLegacyNetworkPack();
        turret.tickCasingDelay();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TurretBlockEntityBase turret) {
        turret.lastRotationPitch = turret.rotationPitch;
        turret.lastRotationYaw = turret.rotationYaw;
        turret.lastSpin = turret.spin;
        turret.lastBarrelLeftPos = turret.barrelLeftPos;
        turret.lastBarrelRightPos = turret.barrelRightPos;
        turret.decayClientBeam();
        turret.tickClientSpecificAnimations();
        turret.rotationPitch = turret.syncRotationPitch;
        turret.rotationYaw = turret.syncRotationYaw;
        turret.lastRotationYaw = legacyYawInterpolationStart(turret.lastRotationYaw, turret.rotationYaw);
        turret.decayClientAnimations();
    }

    private static double legacyYawInterpolationStart(double previousYaw, double currentYaw) {
        // 1.7.10 fixed turret interpolation at the 360 degree seam by moving the previous yaw.
        if (Math.abs(previousYaw - currentYaw) > Math.PI) {
            return previousYaw + (previousYaw < currentYaw ? Math.PI * 2.0D : -Math.PI * 2.0D);
        }
        return previousYaw;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public boolean isOn() {
        return isActive();
    }

    public boolean targetPlayers() {
        return targetPlayers;
    }

    public boolean targetFriendly() {
        return targetFriendly;
    }

    public boolean targetHostile() {
        return targetHostile;
    }

    public boolean targetMachines() {
        return targetMachines;
    }

    public int getStattrak() {
        return stattrak;
    }

    protected boolean isAligned() {
        return aligned;
    }

    public double getRotationYaw() {
        return rotationYaw;
    }

    public double getRotationPitch() {
        return rotationPitch;
    }

    public double getLastRotationYaw() {
        return lastRotationYaw;
    }

    public double getLastRotationPitch() {
        return lastRotationPitch;
    }

    public float getSpin() {
        return spin;
    }

    public float getLastSpin() {
        return lastSpin;
    }

    public float getBarrelLeftPos() {
        return barrelLeftPos;
    }

    public float getLastBarrelLeftPos() {
        return lastBarrelLeftPos;
    }

    public float getBarrelRightPos() {
        return barrelRightPos;
    }

    public float getLastBarrelRightPos() {
        return lastBarrelRightPos;
    }

    public int getBeamTicks() {
        return beamTicks;
    }

    public double getBeamDistance() {
        return beamDistance;
    }

    @Nullable
    public Vec3 getTargetPos() {
        return targetPos;
    }

    public List<String> getWhitelist() {
        ItemStack stack = items.getStackInSlot(SLOT_CHIP);
        if (isWhitelistChip(stack)) {
            return TurretBiometryItem.getNames(stack);
        }
        return List.of();
    }

    public int getPowerBarHeight(int maxHeight) {
        return getMaxPower() <= 0L ? 0 : (int) (getPower() * maxHeight / getMaxPower());
    }

    public ResourceLocation getGuiTexture() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/weapon/" + getGuiTextureFile());
    }

    protected String getGuiTextureFile() {
        return "gui_turret_base.png";
    }

    protected void tickTargeting() {
        aligned = false;
        if (level == null || level.isClientSide) {
            return;
        }

        if (target != null && !target.isAlive()) {
            target = null;
            targetPos = null;
            stattrak++;
        }
        if (target != null && !canKeepCurrentTarget(target)) {
            target = null;
            targetPos = null;
        }

        if (target != null) {
            targetPos = getEntityPos(target);
        }
        updateManualTargeting();
        updateTargetingBeforeMovement();

        if (isActive() && hasPower()) {
            if (!canSeekNewTarget() && shouldClearTargetWhenSeekingDisabled()) {
                clearTarget();
            }
            if (targetPos != null && shouldTurnTowardTargetPosition()) {
                turnTowards(targetPos);
            }
            searchTimer--;
            long consumption = getConsumption();
            if (consumption > 0L) {
                setPower(getPower() - consumption);
            }
            if (searchTimer <= 0) {
                searchTimer = getDetectorInterval();
                if (target == null && canSeekNewTarget()) {
                    seekNewTarget();
                }
            }
        } else {
            searchTimer = 0;
            clearTarget();
        }

        if (aligned && shouldUpdateFiringTick()) {
            updateFiringTick();
        }
    }

    protected void updateFiringTick() {
    }

    protected void updateServerTick() {
    }

    protected void updateServerTickAfterTargeting() {
    }

    protected void updateServerTickAfterLegacyNetworkPack() {
    }

    protected boolean hasPower() {
        return getPower() >= getConsumption();
    }

    protected boolean isActive() {
        return isOn;
    }

    protected boolean usesEnergy() {
        return true;
    }

    protected long getConsumption() {
        return 100L;
    }

    protected int getDetectorInterval() {
        return 10;
    }

    protected double getDetectorRange() {
        return 32.0D;
    }

    protected double getDetectorGrace() {
        return 3.0D;
    }

    protected double getAcceptableInaccuracy() {
        return 5.0D;
    }

    protected double getTurretYawSpeed() {
        return 4.5D;
    }

    protected double getTurretPitchSpeed() {
        return 3.0D;
    }

    protected double getTurretDepression() {
        return 30.0D;
    }

    protected double getTurretElevation() {
        return 30.0D;
    }

    protected double getHeightOffset() {
        return 1.5D;
    }

    protected double getBarrelLength() {
        return 1.0D;
    }

    protected boolean usesCasings() {
        return false;
    }

    protected int casingDelay() {
        return 0;
    }

    protected int legacyCasingEjectorId() {
        return -1;
    }

    protected boolean hasThermalVision() {
        return true;
    }

    protected Vec3 getHorizontalOffset() {
        Direction facing = getLegacyFacing();
        return switch (facing) {
            case NORTH -> new Vec3(1.0D, 0.0D, 1.0D);
            case EAST -> new Vec3(0.0D, 0.0D, 1.0D);
            case WEST -> new Vec3(1.0D, 0.0D, 0.0D);
            default -> new Vec3(0.0D, 0.0D, 0.0D);
        };
    }

    protected Direction getLegacyFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return standardTurretEnergyPorts();
    }

    protected List<EnergyPort> standardTurretEnergyPorts() {
        Direction dir = getLegacyFacing().getOpposite();
        Direction rot = dir.getClockWise();
        return List.of(
                energyPort(dir, rot, -1, 0, 0, dir.getOpposite()),
                energyPort(dir, rot, -1, -1, 0, dir.getOpposite()),
                energyPort(dir, rot, 0, -2, 0, rot.getOpposite()),
                energyPort(dir, rot, 1, -2, 0, rot.getOpposite()),
                energyPort(dir, rot, 0, 1, 0, rot),
                energyPort(dir, rot, 1, 1, 0, rot),
                energyPort(dir, rot, 2, 0, 0, dir),
                energyPort(dir, rot, 2, -1, 0, dir));
    }

    protected List<EnergyPort> artilleryTurretEnergyPorts() {
        Direction dir = getLegacyFacing().getOpposite();
        Direction rot = dir.getClockWise();
        List<EnergyPort> ports = new ArrayList<>(32);
        for (int y = 0; y < 2; y++) {
            for (int j = 0; j < 4; j++) {
                ports.add(energyPort(dir, rot, -1 + j, -3, y, Direction.SOUTH));
                ports.add(energyPort(dir, rot, -1 + j, 2, y, Direction.NORTH));
                ports.add(energyPort(dir, rot, -2, 1 - j, y, Direction.EAST));
                ports.add(energyPort(dir, rot, 3, 1 - j, y, Direction.WEST));
            }
        }
        return List.copyOf(ports);
    }

    protected static EnergyPort energyPort(Direction dir, Direction rot, int forward, int side, int y,
            Direction portDirection) {
        return new EnergyPort(LegacyMultiblockOffsets.relative(dir, rot, forward, side, y), portDirection);
    }

    protected Vec3 getTurretPos() {
        Vec3 offset = getHorizontalOffset();
        return Vec3.atLowerCornerOf(worldPosition).add(offset.x, getHeightOffset(), offset.z);
    }

    public Vec3 getRenderHorizontalOffset() {
        return getHorizontalOffset();
    }

    protected Vec3 getBarrelHeading() {
        return rotateLegacyLocal(new Vec3(1.0D, 0.0D, 0.0D)).normalize();
    }

    protected Vec3 rotateLegacyLocal(Vec3 local) {
        return local.zRot((float) -rotationPitch).yRot((float) -(rotationYaw + Math.PI * 0.5D));
    }

    protected Vec3 rotateLegacyYawOnly(Vec3 local) {
        return local.yRot((float) -rotationYaw);
    }

    protected Vec3 getMuzzlePos() {
        return getTurretPos().add(rotateLegacyLocal(new Vec3(getBarrelLength(), 0.0D, 0.0D)));
    }

    @Nullable
    protected Entity getTarget() {
        return target;
    }

    protected Vec3 getEntityPos(Entity entity) {
        return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ());
    }

    protected void seekNewTarget() {
        if (level == null) {
            return;
        }
        Vec3 pos = getTurretPos();
        double range = getDetectorRange();
        AABB bounds = new AABB(pos, pos).inflate(range);
        Entity closestTarget = null;
        double closest = range;
        for (Entity entity : level.getEntitiesOfClass(Entity.class, bounds)) {
            Vec3 entityPos = getEntityPos(entity);
            double distance = entityPos.distanceTo(pos);
            if (distance > range || !entityAcceptableTarget(entity) || !canAcquireTarget(entity)) {
                continue;
            }
            if (distance < closest) {
                closest = distance;
                closestTarget = entity;
            }
        }
        target = closestTarget;
        targetPos = closestTarget == null ? null : getEntityPos(closestTarget);
    }

    protected void turnTowards(Vec3 entityPos) {
        Vec3 pos = getTurretPos();
        Vec3 delta = entityPos.subtract(pos);
        if (delta.lengthSqr() <= 0.0D) {
            return;
        }
        double targetPitch = Math.asin(delta.y / delta.length());
        double targetYaw = -Math.atan2(delta.x, delta.z);
        turnTowardsAngle(targetPitch, targetYaw);
    }

    protected void turnTowardsAngle(double targetPitch, double targetYaw) {
        double turnYaw = Math.toRadians(getTurretYawSpeed());
        double turnPitch = Math.toRadians(getTurretPitchSpeed());
        double pi2 = Math.PI * 2.0D;

        if (Math.abs(rotationPitch - targetPitch) < turnPitch || Math.abs(rotationPitch - targetPitch) > pi2 - turnPitch) {
            rotationPitch = targetPitch;
        } else if (targetPitch > rotationPitch) {
            rotationPitch += turnPitch;
        } else {
            rotationPitch -= turnPitch;
        }

        double deltaYaw = (targetYaw - rotationYaw) % pi2;
        int dir = 0;
        if (deltaYaw < -Math.PI) {
            dir = 1;
        } else if (deltaYaw < 0.0D) {
            dir = -1;
        } else if (deltaYaw > Math.PI) {
            dir = -1;
        } else if (deltaYaw > 0.0D) {
            dir = 1;
        }

        if (Math.abs(rotationYaw - targetYaw) < turnYaw || Math.abs(rotationYaw - targetYaw) > pi2 - turnYaw) {
            rotationYaw = targetYaw;
        } else {
            rotationYaw += turnYaw * dir;
        }

        double deltaPitch = targetPitch - rotationPitch;
        deltaYaw = targetYaw - rotationYaw;
        double deltaAngle = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
        rotationYaw = rotationYaw % pi2;
        rotationPitch = rotationPitch % pi2;
        aligned = deltaAngle <= Math.toRadians(getAcceptableInaccuracy());
    }

    protected boolean entityInLineOfSight(Entity entity) {
        if (level == null || !entity.isAlive()) {
            return false;
        }
        if (!hasThermalVision() && entity instanceof LivingEntity living && living.isInvisible()) {
            return false;
        }
        Vec3 pos = getTurretPos();
        Vec3 entityPos = getEntityPos(entity);
        Vec3 delta = entityPos.subtract(pos);
        double length = delta.length();
        if (length < getDetectorGrace() || length > getDetectorRange() * 1.1D) {
            return false;
        }
        double pitch = Math.asin(delta.normalize().y);
        double pitchDeg = Math.toDegrees(pitch);
        if (pitchDeg < -getTurretDepression() || pitchDeg > getTurretElevation()) {
            return false;
        }
        return !HbmWorldUtil.isObstructedOpaque(level, entityPos, pos);
    }

    protected boolean entityInSurfaceTargetEnvelope(Entity entity) {
        if (level == null || !entity.isAlive()) {
            return false;
        }
        Vec3 pos = getTurretPos();
        Vec3 entityPos = getEntityPos(entity);
        double length = entityPos.distanceTo(pos);
        if (length < getDetectorGrace() || length > getDetectorRange() * 1.1D) {
            return false;
        }
        int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING,
                Mth.floor(entity.getX()), Mth.floor(entity.getZ()));
        return height < entity.getY() + entity.getBbHeight();
    }

    protected boolean canAcquireTarget(Entity entity) {
        return doesLineOfSightCheck() ? entityInLineOfSight(entity) : true;
    }

    protected boolean canKeepCurrentTarget(Entity entity) {
        return canAcquireTarget(entity);
    }

    protected boolean doesLineOfSightCheck() {
        return true;
    }

    protected boolean canSeekNewTarget() {
        return true;
    }

    protected boolean shouldClearTargetWhenSeekingDisabled() {
        return true;
    }

    protected void updateManualTargeting() {
    }

    protected void updateTargetingBeforeMovement() {
    }

    protected boolean shouldTurnTowardTargetPosition() {
        return true;
    }

    protected boolean shouldUpdateFiringTick() {
        return true;
    }

    protected void clearTarget() {
        target = null;
        targetPos = null;
    }

    protected void setManualTarget(@Nullable Vec3 pos) {
        target = null;
        targetPos = pos;
    }

    protected boolean entityAcceptableTarget(Entity entity) {
        if (!entity.isAlive()) {
            return false;
        }
        if (CompatTurretTargetRegistry.isBlacklisted(entity)) {
            return false;
        }
        CompatTurretTargetRegistry.TargetDecision decision =
                CompatTurretTargetRegistry.evaluateConditions(entity, this);
        if (decision == CompatTurretTargetRegistry.TargetDecision.REJECT) {
            return false;
        }
        if (decision == CompatTurretTargetRegistry.TargetDecision.ACCEPT) {
            return true;
        }

        List<String> whitelist = getWhitelist();
        if (!whitelist.isEmpty()) {
            if (entity instanceof Player player && whitelist.contains(player.getDisplayName().getString())) {
                return false;
            }
            Component customName = entity instanceof Mob ? entity.getCustomName() : null;
            if (customName != null && whitelist.contains(customName.getString())) {
                return false;
            }
        }

        if (targetFriendly && CompatTurretTargetRegistry.isFriendly(entity)) {
            return true;
        }
        if (targetHostile) {
            if (entity instanceof EnderDragon) {
                return false;
            }
            if (entity instanceof EnderDragonPart || CompatTurretTargetRegistry.isHostile(entity)) {
                return true;
            }
        }
        if (targetMachines && CompatTurretTargetRegistry.isMachine(entity, this)) {
            return true;
        }
        return targetPlayers && CompatTurretTargetRegistry.isPlayer(entity);
    }

    protected List<BulletConfig> getAmmoConfigs() {
        return List.of();
    }

    public List<ItemStack> getAmmoTypesForDisplay() {
        List<ItemStack> stacks = new ArrayList<>();
        for (BulletConfig config : getAmmoConfigs()) {
            Item item = ammoItem(config);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    @Nullable
    protected BulletConfig getFirstConfigLoaded() {
        List<BulletConfig> configs = getAmmoConfigs();
        if (configs.isEmpty()) {
            return null;
        }
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            for (BulletConfig config : configs) {
                Item item = ammoItem(config);
                if (item != null && stack.is(item)) {
                    return config;
                }
            }
        }
        return null;
    }

    protected boolean hasAmmo(BulletConfig config) {
        Item item = ammoItem(config);
        if (item == null) {
            return false;
        }
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    protected boolean consumeAmmo(BulletConfig config) {
        Item item = ammoItem(config);
        if (item == null) {
            return false;
        }
        for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.is(item)) {
                stack.shrink(1);
                items.setStackInSlot(slot, stack);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Item ammoItem(BulletConfig config) {
        if (config == null || config.ammo() == null) {
            return null;
        }
        ResourceLocation itemId = config.ammo().itemId();
        if (itemId == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        return item == null || item == Items.AIR ? null : item;
    }

    protected boolean spawnBullet(BulletConfig config, float baseDamage) {
        return spawnBullet(config, baseDamage, null);
    }

    protected boolean spawnBullet(BulletConfig config, float baseDamage, @Nullable Entity homingTarget) {
        return spawnBullet(config, baseDamage, config == null ? 0.0F : config.spread(), homingTarget);
    }

    protected boolean spawnBullet(BulletConfig config, float baseDamage, float gunSpread,
            @Nullable Entity homingTarget) {
        if (level == null || level.isClientSide || config == null) {
            return false;
        }
        BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedMk4LaunchPlan(config, getMuzzlePos(),
                getBarrelHeading(), 1.0F, gunSpread, level.random);
        if (!plan.valid()) {
            return false;
        }
        BulletProjectileEntity bullet = BulletProjectileEntity.fromLaunchPlan(level, plan, null);
        bullet.overrideDamage = legacyTurretDamage(config, baseDamage);
        bullet.setHomingTargetEntity(homingTarget);
        return level.addFreshEntity(bullet);
    }

    private static float legacyTurretDamage(BulletConfig config, float baseDamage) {
        return baseDamage * config.damageMin();
    }

    protected void scheduleCasing(BulletConfig config) {
        if (!usesCasings() || config == null || config.spentCasingName().isBlank()) {
            return;
        }
        if (casingDelay() == 0) {
            spawnLegacyCasing(config);
        } else {
            queuedCasingConfig = config;
            casingDelay = casingDelay();
        }
    }

    private void tickCasingDelay() {
        if (!usesCasings() || queuedCasingConfig == null) {
            return;
        }
        if (casingDelay > 0) {
            casingDelay--;
            return;
        }
        spawnLegacyCasing(queuedCasingConfig);
        queuedCasingConfig = null;
    }

    protected void spawnLegacyCasing(BulletConfig config) {
        if (level == null || level.isClientSide || config == null || config.spentCasingName().isBlank()
                || legacyCasingEjectorId() < 0) {
            return;
        }
        Vec3 pos = getCasingSpawnPos();
        BulletCasingEjectUtil.execute(level, BulletCasingEjectUtil.legacyEjectorRequest(pos,
                legacyCasingEjectorId(), config.spentCasingName(), (float) -rotationPitch, (float) rotationYaw,
                false));
    }

    protected void spawnDirectCasing(Vec3 position, float yaw, float pitch, double frontMotion, double heightMotion,
            double sideMotion, double motionVariance, float momentumPitch, float momentumYaw, String casingName,
            boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
        if (level == null || level.isClientSide || position == null || casingName == null || casingName.isBlank()) {
            return;
        }
        BulletCasingEjectUtil.execute(level, BulletCasingEjectUtil.directAtPosition(position, yaw, pitch,
                frontMotion, heightMotion, sideMotion, motionVariance, momentumPitch, momentumYaw, casingName,
                smoking, smokeLife, smokeLift, nodeLife, level.random));
    }

    protected Vec3 getCasingSpawnPos() {
        return getTurretPos();
    }

    protected void spawnMuzzleLargeExplode(float size, int count) {
        spawnMuzzleLargeExplodeAt(getMuzzlePos(), size, count);
    }

    protected void spawnMuzzleLargeExplodeAt(Vec3 pos, float size, int count) {
        if (level == null || level.isClientSide) {
            return;
        }
        ParticleUtil.spawnVanillaExtLargeExplode(level, pos.x, pos.y, pos.z, size, count);
    }

    protected void spawnTauMuzzleParticles(int count) {
        if (level == null || level.isClientSide) {
            return;
        }
        Vec3 pos = getMuzzlePos();
        ParticleUtil.spawnTau(level, pos.x, pos.y, pos.z, count, false);
    }

    protected void playTurretSound(String sound, float volume, float pitch) {
        if (level != null && !level.isClientSide) {
            LegacySoundPlayer.playSoundEffect(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    sound, SoundSource.BLOCKS, volume, pitch);
        }
    }

    protected void playTurretSoundAtEntity(Entity entity, String sound, float volume, float pitch) {
        if (level != null && !level.isClientSide && entity != null) {
            LegacySoundPlayer.playSoundAtEntity(entity, sound, SoundSource.BLOCKS, volume, pitch);
        }
    }

    protected void triggerBarrelSpin(float degrees) {
        spin = normalizeAnimationAngle(spin + degrees);
    }

    protected void triggerClientBarrelSpin(float degrees) {
        lastSpin = spin;
        triggerBarrelSpin(degrees);
        if (Math.abs(lastSpin - spin) > 180.0F) {
            lastSpin += lastSpin < spin ? 360.0F : -360.0F;
        }
    }

    protected void triggerLeftBarrelRecoil() {
        barrelLeftPos = 1.0F;
    }

    protected void triggerRightBarrelRecoil() {
        barrelRightPos = 1.0F;
    }

    protected void triggerDualBarrelRecoil() {
        triggerLeftBarrelRecoil();
        triggerRightBarrelRecoil();
    }

    protected boolean shouldSyncBarrelRecoilPositions() {
        return true;
    }

    protected boolean shouldSyncBeamState() {
        return true;
    }

    protected void triggerBeam(int ticks) {
        Vec3 target = getTargetPos();
        if (target == null) {
            return;
        }
        beamTicks = ticks;
        beamDistance = Math.max(0.0D, target.distanceTo(getTurretPos()));
    }

    protected void triggerClientBeamFromTarget(int ticks) {
        updateClientBeamDistanceFromTarget();
        beamTicks = ticks;
    }

    protected void updateClientBeamDistanceFromTarget() {
        Vec3 target = getTargetPos();
        if (target != null) {
            beamDistance = Math.max(0.0D, target.distanceTo(getTurretPos()));
        }
    }

    protected void decayClientAnimations() {
        barrelLeftPos = Math.max(0.0F, barrelLeftPos - 0.2F);
        barrelRightPos = Math.max(0.0F, barrelRightPos - 0.2F);
    }

    protected void decayServerAnimations() {
        barrelLeftPos = Math.max(0.0F, barrelLeftPos - 0.2F);
        barrelRightPos = Math.max(0.0F, barrelRightPos - 0.2F);
        if (beamTicks > 0) {
            beamTicks--;
        }
    }

    protected void tickClientSpecificAnimations() {
    }

    protected void tickServerSpecificAnimations() {
    }

    protected void syncRuntimeToTracking() {
        setChanged();
        if (level != null && !level.isClientSide) {
            networkPackNT(LEGACY_SYNC_RANGE);
        }
    }

    private void networkPackTurretLikeLegacy() {
        networkPackNT(LEGACY_SYNC_RANGE);
    }

    private void decayClientBeam() {
        if (beamTicks > 0) {
            beamTicks--;
        }
    }

    private static float normalizeAnimationAngle(float angle) {
        angle %= 360.0F;
        return angle < 0.0F ? angle + 360.0F : angle;
    }

    public static CompoundTag controlTag(String action) {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_ACTION, action);
        return tag;
    }

    public static CompoundTag targetControlTag(String type) {
        CompoundTag tag = controlTag("toggle_target");
        tag.putString(TAG_TYPE, type);
        return tag;
    }

    public static CompoundTag addWhitelistTag(String name) {
        CompoundTag tag = controlTag("add_whitelist");
        tag.putString(TAG_NAME, name);
        return tag;
    }

    public static CompoundTag removeWhitelistTag(int index) {
        CompoundTag tag = controlTag("remove_whitelist");
        tag.putInt(TAG_INDEX, index);
        return tag;
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return player.containerMenu instanceof TurretMenu menu && menu.getBlockEntity() == this
                && HbmInventoryMenuHelper.stillValidBlockEntity(player, this, 64.0D);
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (tag.contains("del")) {
            removeName(tag.getInt("del"));
            return;
        }
        if (tag.contains("name")) {
            addName(tag.getString("name"));
            return;
        }
        String action = tag.getString(TAG_ACTION);
        switch (action) {
            case "toggle_power" -> isOn = !isOn;
            case "toggle_target" -> toggleTarget(tag.getString(TAG_TYPE));
            case "add_whitelist" -> addName(tag.getString(TAG_NAME));
            case "remove_whitelist" -> removeName(tag.getInt(TAG_INDEX));
            default -> {
            }
        }
        syncRuntimeToTracking();
    }

    private void toggleTarget(String type) {
        switch (type) {
            case "players" -> targetPlayers = !targetPlayers;
            case "friendly" -> targetFriendly = !targetFriendly;
            case "hostile" -> targetHostile = !targetHostile;
            case "machine" -> targetMachines = !targetMachines;
            default -> {
            }
        }
    }

    private void addName(String name) {
        ItemStack stack = items.getStackInSlot(SLOT_CHIP);
        if (isWhitelistChip(stack)) {
            TurretBiometryItem.addName(stack, name);
            setChanged();
        }
    }

    private void removeName(int index) {
        ItemStack stack = items.getStackInSlot(SLOT_CHIP);
        if (isWhitelistChip(stack)) {
            TurretBiometryItem.removeName(stack, index);
            setChanged();
        }
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                ROR.functionInfo("setActive", "active (0 or 1)"),
                ROR.functionInfo("targetPlayers", "enabled (0 or 1)"),
                ROR.functionInfo("targetAnimals", "enabled (0 or 1)"),
                ROR.functionInfo("targetMobs", "enabled (0 or 1)"),
                ROR.functionInfo("targetMachines", "enabled (0 or 1)"),
                ROR.functionInfo("addWhitelist", "name"),
                ROR.functionInfo("removeWhitelist", "name"),
        };
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if (params == null) {
            params = new String[0];
        }
        if (ROR.function("setActive").equals(name) && params.length > 0) {
            Boolean value = parseLegacyBool(params[0]);
            if (value != null) {
                isOn = value;
                syncRuntimeToTracking();
            }
        }
        if (ROR.function("targetPlayers").equals(name) && params.length > 0) {
            Boolean value = parseLegacyBool(params[0]);
            if (value != null) {
                targetPlayers = value;
                syncRuntimeToTracking();
            }
        }
        if (ROR.function("targetAnimals").equals(name) && params.length > 0) {
            Boolean value = parseLegacyBool(params[0]);
            if (value != null) {
                targetFriendly = value;
                syncRuntimeToTracking();
            }
        }
        if (ROR.function("targetMobs").equals(name) && params.length > 0) {
            Boolean value = parseLegacyBool(params[0]);
            if (value != null) {
                targetHostile = value;
                syncRuntimeToTracking();
            }
        }
        if (ROR.function("targetMachines").equals(name) && params.length > 0) {
            Boolean value = parseLegacyBool(params[0]);
            if (value != null) {
                targetMachines = value;
                syncRuntimeToTracking();
            }
        }
        if (ROR.function("addWhitelist").equals(name) && params.length > 0) {
            addName(params[0]);
            syncRuntimeToTracking();
        }
        if (ROR.function("removeWhitelist").equals(name) && params.length > 0) {
            List<String> whitelist = getWhitelist();
            int index = whitelist.indexOf(params[0]);
            if (index >= 0) {
                removeName(index);
                syncRuntimeToTracking();
            }
        }
        return null;
    }

    private static @Nullable Boolean parseLegacyBool(String value) {
        try {
            return Integer.parseInt(value) == 1;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean isWhitelistChip(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.TURRET_CHIP.get());
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_INVENTORY, items);
        tag.putLong(TAG_LEGACY_POWER, getPower());
        tag.putBoolean(TAG_IS_ON, isActive());
        tag.putBoolean(TAG_TARGET_PLAYERS, targetPlayers);
        tag.putBoolean(TAG_TARGET_FRIENDLY, targetFriendly);
        tag.putBoolean(TAG_TARGET_HOSTILE, targetHostile);
        tag.putBoolean(TAG_TARGET_MACHINES, targetMachines);
        tag.putInt(TAG_STATTRAK, stattrak);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        if (tag.contains(TAG_LEGACY_POWER)) {
            setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        isOn = tag.getBoolean(TAG_IS_ON);
        targetPlayers = tag.getBoolean(TAG_TARGET_PLAYERS);
        targetFriendly = tag.getBoolean(TAG_TARGET_FRIENDLY);
        targetHostile = !tag.contains(TAG_TARGET_HOSTILE) || tag.getBoolean(TAG_TARGET_HOSTILE);
        targetMachines = !tag.contains(TAG_TARGET_MACHINES) || tag.getBoolean(TAG_TARGET_MACHINES);
        stattrak = tag.getInt(TAG_STATTRAK);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        writeTurretSync(tag);
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        handleClientSyncTag(packet.getTag());
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        turnProgress = 2;
        if (tag.getBoolean(TAG_TARGET_PRESENT)) {
            targetPos = new Vec3(tag.getDouble(TAG_TARGET_X), tag.getDouble(TAG_TARGET_Y), tag.getDouble(TAG_TARGET_Z));
        } else {
            targetPos = null;
        }
        syncRotationPitch = tag.getDouble(TAG_ROTATION_PITCH);
        syncRotationYaw = tag.getDouble(TAG_ROTATION_YAW);
        if (tag.contains(TAG_SPIN)) {
            lastSpin = spin;
            spin = tag.getFloat(TAG_SPIN);
            if (Math.abs(lastSpin - spin) > 180.0F) {
                lastSpin += lastSpin < spin ? 360.0F : -360.0F;
            }
        }
        if (tag.contains(TAG_BARREL_LEFT) && tag.contains(TAG_BARREL_RIGHT)) {
            lastBarrelLeftPos = barrelLeftPos;
            lastBarrelRightPos = barrelRightPos;
            barrelLeftPos = tag.getFloat(TAG_BARREL_LEFT);
            barrelRightPos = tag.getFloat(TAG_BARREL_RIGHT);
        }
        if (tag.contains(TAG_BEAM_TICKS) && tag.contains(TAG_BEAM_DISTANCE)) {
            beamTicks = tag.getInt(TAG_BEAM_TICKS);
            beamDistance = tag.getDouble(TAG_BEAM_DISTANCE);
        }
        isOn = tag.getBoolean(TAG_IS_ON);
        targetPlayers = tag.getBoolean(TAG_TARGET_PLAYERS);
        targetFriendly = tag.getBoolean(TAG_TARGET_FRIENDLY);
        targetHostile = tag.getBoolean(TAG_TARGET_HOSTILE);
        targetMachines = tag.getBoolean(TAG_TARGET_MACHINES);
        stattrak = tag.getInt(TAG_STATTRAK);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    private void writeTurretSync(CompoundTag tag) {
        tag.putBoolean(TAG_TARGET_PRESENT, targetPos != null);
        if (targetPos != null) {
            tag.putDouble(TAG_TARGET_X, targetPos.x);
            tag.putDouble(TAG_TARGET_Y, targetPos.y);
            tag.putDouble(TAG_TARGET_Z, targetPos.z);
        }
        tag.putDouble(TAG_ROTATION_PITCH, rotationPitch);
        tag.putDouble(TAG_ROTATION_YAW, rotationYaw);
        if (shouldSyncBarrelRecoilPositions()) {
            tag.putFloat(TAG_BARREL_LEFT, barrelLeftPos);
            tag.putFloat(TAG_BARREL_RIGHT, barrelRightPos);
        }
        if (shouldSyncBeamState()) {
            tag.putInt(TAG_BEAM_TICKS, beamTicks);
            tag.putDouble(TAG_BEAM_DISTANCE, beamDistance);
        }
        tag.putBoolean(TAG_IS_ON, isActive());
        tag.putBoolean(TAG_TARGET_PLAYERS, targetPlayers);
        tag.putBoolean(TAG_TARGET_FRIENDLY, targetFriendly);
        tag.putBoolean(TAG_TARGET_HOSTILE, targetHostile);
        tag.putBoolean(TAG_TARGET_MACHINES, targetMachines);
        tag.putInt(TAG_STATTRAK, stattrak);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getContainerKey());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory,
            Player player) {
        return new TurretMenu(containerId, inventory, this);
    }

    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret";
    }

    protected int[] externalAccessibleSlots() {
        return new int[] {SLOT_AMMO_START, 2, 3, 4, 5, 6, 7, 8, SLOT_AMMO_END};
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        externalItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side != null) {
                return externalItemHandler.cast();
            }
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private static final class TurretExternalItemHandler implements IItemHandler {
        private final TurretBlockEntityBase turret;
        private final IItemHandlerModifiable items;

        private TurretExternalItemHandler(TurretBlockEntityBase turret, IItemHandlerModifiable items) {
            this.turret = turret;
            this.items = items;
        }

        @Override
        public int getSlots() {
            return turret.externalAccessibleSlots().length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(mapSlot(slot));
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mappedSlot = mapSlot(slot);
            if (!items.isItemValid(mappedSlot, stack)) {
                return stack;
            }
            return items.insertItem(mappedSlot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return items.extractItem(mapSlot(slot), amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(mapSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return items.isItemValid(mapSlot(slot), stack);
        }

        private int mapSlot(int slot) {
            int[] slots = turret.externalAccessibleSlots();
            if (slot < 0 || slot >= slots.length) {
                throw new RuntimeException("Slot " + slot + " not in valid range - [0," + slots.length + ")");
            }
            return slots[slot];
        }
    }
}
