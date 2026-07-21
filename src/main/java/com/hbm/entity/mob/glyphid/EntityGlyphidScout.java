package com.hbm.entity.mob.glyphid;

import com.hbm.config.MobConfig;
import com.hbm.entity.logic.EntityWaypoint;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.world.WorldUtil;
import com.hbm.ntm.world.feature.GlyphidHive;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

/** Source-backed Glyphid Scout hive-expansion state machine. */
@Deprecated(forRemoval = false)
public class EntityGlyphidScout extends EntityGlyphid {
    private boolean hasTarget;
    private int timer;
    private int scoutingRange = 45;
    private int minDistanceToHive = 8;
    private boolean useLargeHive;

    public EntityGlyphidScout(EntityType<? extends EntityGlyphidScout> type, Level level) {
        super(type, level);
    }

    public EntityGlyphidScout(Level level) {
        this(ModEntityTypes.GLYPHID_SCOUT.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.5D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 10.0D);
    }

    @Override
    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid_scout.png");
    }

    @Override
    public float getScale() {
        return 0.75F;
    }

    /** Scouts never use the base Glyphid soot/rampant extended-targeting path in 1.7.10. */
    @Override
    public boolean useExtendedTargeting() {
        return false;
    }

    @Override
    protected double getTargetRange() {
        return 10.0D;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target) && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20, 3), this);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount, 2.0D), 100.0D);
    }

    @Override
    protected float getArmorThresholdMultiplier() {
        return 0.5F;
    }

    @Override
    protected float getArmorResistanceMultiplier() {
        return 0.5F;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        if (getTarget() != null && tickCount % 60 == 0) {
            Player player = level().getNearestPlayer(this, 10.0D);
            setTarget(player);
        }

        // Deliberately preserves the source's OR condition.
        if ((getCurrentTask() != TASK_BUILD_HIVE || getCurrentTask() != TASK_TERRAFORM) && getWaypoint() == null) {
            if (MobConfig.rampantGlyphidGuidance() && getRampantTargetDirection() != null) {
                if (!hasTarget) {
                    Vec3 targetPosition = playerBaseDirFinder(position(), getPlayerTargetDirection());
                    EntityWaypoint target = new EntityWaypoint(level());
                    target.moveTo(targetPosition.x, targetPosition.y, targetPosition.z, 0.0F, 0.0F);
                    target.maxAge = 300;
                    target.radius = 6;
                    target.setWaypointType(TASK_BUILD_HIVE);
                    level().addFreshEntity(target);
                    hasTarget = true;
                    setCurrentTask(TASK_RETREAT_FOR_REINFORCEMENTS, target);
                }
                if (super.isAtDestination()) {
                    setCurrentTask(TASK_BUILD_HIVE, null);
                    hasTarget = false;
                }
            } else {
                setCurrentTask(TASK_BUILD_HIVE, null);
            }
        }

        if (getCurrentTask() != TASK_BUILD_HIVE && getCurrentTask() != TASK_TERRAFORM) {
            return;
        }
        if (!hasTarget) {
            if (scoutingRange != 60 && hasNuclearGlyphidNearby()) {
                setCurrentTask(TASK_TERRAFORM, null);
            }
            if (expandHive()) {
                addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 180 * 20, 1));
                hasTarget = true;
            }
        }
        if (getWaypoint() == null && hasTarget) {
            hasTarget = false;
        }
        if (getCurrentTask() == TASK_TERRAFORM && super.isAtDestination() && canBuildHiveHere()) {
            communicate(TASK_TERRAFORM, getWaypoint());
        }
        if (tickCount % 10 == 0 && isAtDestination() && canBuildHiveHere()) {
            timer++;
            if (timer == 1) {
                EntityWaypoint additional = new EntityWaypoint(level());
                additional.moveTo(getX(), getY(), getZ(), 0.0F, 0.0F);
                additional.setWaypointType(TASK_IDLE);

                EntityWaypoint home = new EntityWaypoint(level());
                home.setWaypointType(TASK_RETREAT_FOR_REINFORCEMENTS);
                home.setAdditionalWaypoint(additional);
                home.moveTo(homeX, homeY, homeZ, 0.0F, 0.0F);
                home.maxAge = 1200;
                home.radius = 6;
                level().addFreshEntity(home);

                taskWaypoint = home;
                addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40 * 20, 10));
                communicate(TASK_RETREAT_FOR_REINFORCEMENTS, home);
            } else if (timer >= 5) {
                level().explode(this, getX(), getY(), getZ(), 5.0F, false, Level.ExplosionInteraction.NONE);
                GlyphidHive.generateSmall(level(), blockPosition(), random, getSubtype() != TYPE_NORMAL, false);
                discard();
            } else {
                communicate(TASK_FOLLOW, getWaypoint());
            }
        }
    }

    /** Returns false and resets work when a source glyphid-base hive shell is already nearby. */
    public boolean canBuildHiveHere() {
        RegistryObject<? extends Block> glyphidBase = ModBlocks.legacyBlock("glyphid_base");
        if (glyphidBase == null) {
            throw new IllegalStateException("Missing source-backed glyphid_base block");
        }
        int length = useLargeHive ? 16 : 8;
        Vec3 start = new Vec3(getX(), getY() + 1.0D, getZ());
        for (int index = 0; index < 8; index++) {
            double angle = Math.toRadians(360.0D / 16.0D * index);
            Vec3 end = start.add(Math.sin(angle) * length, 0.0D, Math.cos(angle) * length);
            BlockHitResult hit = level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE, this));
            if (hit.getType() == HitResult.Type.BLOCK && level().getBlockState(hit.getBlockPos()).is(glyphidBase.get())) {
                setCurrentTask(TASK_IDLE, null);
                hasTarget = false;
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAtDestination() {
        return getCurrentTask() == TASK_BUILD_HIVE && super.isAtDestination();
    }

    public boolean hasNuclearGlyphidNearby() {
        AABB box = getBoundingBox().inflate(8.0D);
        return level().getEntities(this, box, entity -> entity instanceof EntityGlyphid glyphid && glyphid.isNuclearGlyphid())
                .stream().findAny().isPresent();
    }

    @Override
    public boolean expandHive() {
        int nestX = random.nextInt(scoutingRange * 2) + homeX - scoutingRange;
        int nestZ = random.nextInt(scoutingRange * 2) + homeZ - scoutingRange;
        int nestY = WorldUtil.legacyGetHeightValue(level(), nestX, nestZ);
        BlockPos floorPos = new BlockPos(nestX, nestY - 1, nestZ);
        BlockState floor = level().getBlockState(floorPos);
        RegistryObject<? extends Block> glyphidBase = ModBlocks.legacyBlock("glyphid_base");
        RegistryObject<? extends Block> basalt = ModBlocks.legacyBlock("basalt");
        if (glyphidBase == null || basalt == null) {
            throw new IllegalStateException("Missing source-backed Glyphid hive placement block");
        }
        boolean farEnough = new Vec3(nestX - homeX, nestY - homeY, nestZ - homeZ).length() > minDistanceToHive;
        if (!farEnough || floor.isAir() || !floor.isSolidRender(level(), floorPos) || floor.is(glyphidBase.get())) {
            return false;
        }
        if (floor.is(basalt.get())) {
            useLargeHive = true;
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * 20, 3));
        }

        EntityWaypoint nest = new EntityWaypoint(level());
        nest.setWaypointType(getCurrentTask());
        nest.radius = 5;
        if (useLargeHive) {
            nest.setHighPriority();
        }
        nest.moveTo(nestX, nestY, nestZ, 0.0F, 0.0F);
        level().addFreshEntity(nest);
        setCurrentTask(getCurrentTask(), nest);
        communicate(TASK_BUILD_HIVE, nest);
        return true;
    }

    @Override
    protected void carryOutTask() {
        if (!level().isClientSide() && getWaypoint() == null) {
            if (getCurrentTask() == TASK_INITIATE_RETREAT) {
                removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 20, 4));
                EntityWaypoint additional = new EntityWaypoint(level());
                additional.moveTo(getX(), getY(), getZ(), 0.0F, 0.0F);
                additional.setWaypointType(TASK_IDLE);
                EntityWaypoint home = new EntityWaypoint(level());
                home.setWaypointType(TASK_BUILD_HIVE);
                home.setAdditionalWaypoint(additional);
                home.setHighPriority();
                home.radius = 6;
                home.moveTo(homeX, homeY, homeZ, 0.0F, 0.0F);
                level().addFreshEntity(home);
                communicate(TASK_FOLLOW, home);
            } else if (getCurrentTask() == TASK_TERRAFORM) {
                scoutingRange = 60;
                minDistanceToHive = 20;
            }
        }
        super.carryOutTask();
    }

    public static Vec3 playerBaseDirFinder(Vec3 currentLocation, Vec3 target) {
        Vec3 direction = currentLocation.subtract(target).normalize();
        return currentLocation.add(direction.scale(10.0D));
    }

    private Vec3 getPlayerTargetDirection() {
        Player player = level().getNearestPlayer(this, 300.0D);
        return player == null ? getRampantTargetDirection() : player.position();
    }

    private Vec3 getRampantTargetDirection() {
        return PollutionManager.getRampantTarget(level())
                .map(Vec3::atLowerCornerOf)
                .orElse(PollutionHandler.targetCoords);
    }

    /**
     * 1.7.10 overrides the normal mob light test without its RNG branch and
     * uses the combined block/sky brightness with a thunder sky-darkening
     * value of ten.  The base Glyphid's block-light-only check is not an
     * equivalent carrier for natural Scout spawning.
     */
    @Override
    public boolean isValidLightLevel() {
        BlockPos pos = BlockPos.containing(getX(), getBoundingBox().minY, getZ());
        int skyDarken = level().isThundering() ? 10 : level().getSkyDarken();
        return level().getRawBrightness(pos, skyDarken) <= 7;
    }
}
