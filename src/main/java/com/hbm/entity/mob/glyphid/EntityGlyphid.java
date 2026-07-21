package com.hbm.entity.mob.glyphid;

import api.hbm.entity.IResistanceProvider;
import com.hbm.entity.logic.EntityWaypoint;
import com.hbm.config.MobConfig;
import com.hbm.ntm.entity.mob.EntityParasiteMaggot;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.damage.DamageResistanceHandler;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorGlyphidDig;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated(forRemoval = false)
public class EntityGlyphid extends Monster implements IResistanceProvider {
    private static final double NORMAL_TARGET_RANGE = 16.0D;
    private static final double EXTENDED_TARGET_RANGE = 128.0D;
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_INFECTED = 1;
    public static final int TYPE_RADIOACTIVE = 2;

    public static final int TASK_IDLE = 0;
    public static final int TASK_RETREAT_FOR_REINFORCEMENTS = 1;
    public static final int TASK_BUILD_HIVE = 2;
    public static final int TASK_INITIATE_RETREAT = 3;
    public static final int TASK_FOLLOW = 4;
    public static final int TASK_TERRAFORM = 5;
    public static final int TASK_DIG = 6;

    public static final int DW_WALL = 16;
    public static final int DW_ARMOR = 17;
    public static final int DW_SUBTYPE = 18;

    private static final EntityDataAccessor<Boolean> DATA_WALL =
            SynchedEntityData.defineId(EntityGlyphid.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_ARMOR =
            SynchedEntityData.defineId(EntityGlyphid.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_SUBTYPE =
            SynchedEntityData.defineId(EntityGlyphid.class, EntityDataSerializers.BYTE);

    protected boolean hasHome;
    protected int homeX;
    protected int homeY;
    protected int homeZ;
    protected int currentTask;
    protected int previousTask;
    protected EntityWaypoint previousWaypoint;
    protected int taskX;
    protected int taskY;
    protected int taskZ;
    protected boolean hasWaypoint;
    protected EntityWaypoint taskWaypoint;
    private int glyphidSwingTicks;

    public EntityGlyphid(EntityType<? extends EntityGlyphid> type, Level level) {
        super(type, level);
        xpReward = 5;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    public EntityGlyphid(Level level) {
        this(ModEntityTypes.GLYPHID.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, NORMAL_TARGET_RANGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return EntityGlyphid.this.getCurrentTask() == TASK_IDLE && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return EntityGlyphid.this.getCurrentTask() == TASK_IDLE && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                player -> !hasEffect(MobEffects.BLINDNESS)));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_WALL, false);
        entityData.define(DATA_ARMOR, (byte) 0b11111);
        entityData.define(DATA_SUBTYPE, (byte) TYPE_NORMAL);
    }

    @Override
    public void tick() {
        super.tick();
        if (glyphidSwingTicks > 0) {
            glyphidSwingTicks--;
        }
        if (!level().isClientSide() && !hasHome) {
            homeX = blockPosition().getX();
            homeY = blockPosition().getY();
            homeZ = blockPosition().getZ();
            hasHome = true;
        }
        if (!level().isClientSide() && getAttribute(Attributes.FOLLOW_RANGE) != null) {
            getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(getTargetRange());
        }
        if (!level().isClientSide()) {
            setBesideClimbableBlock(horizontalCollision);
            if (hasEffect(MobEffects.BLINDNESS)) {
                setTarget(null);
                getNavigation().stop();
                destroyLegacyLanternsWhileBlinded();
            }
            tickTaskState();
            if (tickCount % 100 == 0) {
                swingGlyphid();
            }
        }
    }

    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid.png");
    }

    public float getScale() {
        return 1.0F;
    }

    public int getSubtype() {
        return entityData.get(DATA_SUBTYPE);
    }

    /** 1.7.10 soot/rampant target acquisition predicate. */
    public boolean useExtendedTargeting() {
        return PollutionManager.shouldGlyphidUseExtendedTargeting(level(), blockPosition());
    }

    /** Source base range is 16, or 128 through rampant extended targeting. */
    protected double getTargetRange() {
        return useExtendedTargeting() ? EXTENDED_TARGET_RANGE : NORMAL_TARGET_RANGE;
    }

    public void setSubtype(int subtype) {
        entityData.set(DATA_SUBTYPE, (byte) subtype);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (isGlyphidSwingInProgress()) {
            return false;
        }
        swingGlyphid();
        if (getSubtype() == TYPE_INFECTED && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 2), this);
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0), this);
        }
        return super.doHurtTarget(target);
    }

    /** Source EntityGlyphid#swingDuration(), overridden by the Behemoth. */
    protected int getGlyphidSwingDuration() {
        return 15;
    }

    protected boolean isGlyphidSwingInProgress() {
        return glyphidSwingTicks > 0;
    }

    protected void swingGlyphid() {
        glyphidSwingTicks = getGlyphidSwingDuration();
        swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    public byte getArmorBits() {
        return entityData.get(DATA_ARMOR);
    }

    public void setArmorBits(byte armor) {
        entityData.set(DATA_ARMOR, (byte) (armor & 0b11111));
    }

    public int getGlyphidArmor() {
        byte armor = getArmorBits();
        int total = 0;
        for (int i = 0; i < 5; i++) {
            if ((armor & (1 << i)) != 0) {
                total++;
            }
        }
        return total;
    }

    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount * 0.6D, 2.0D), 100.0D);
    }

    private void destroyLegacyLanternsWhileBlinded() {
        if (getScale() < 1.25F || tickCount % 20 != 0) {
            return;
        }
        Vec3 start = new Vec3(getX(), getY() + 1.0D, getZ());
        for (int i = 0; i < 16; i++) {
            double angle = Math.toRadians(360.0D / 16.0D * i);
            Vec3 end = start.add(Math.sin(angle) * 4.0D, 0.0D, Math.cos(angle) * 4.0D);
            BlockHitResult hit = level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE, this));
            if (hit.getType() != HitResult.Type.BLOCK
                    || !level().getBlockState(hit.getBlockPos()).is(ModBlocks.legacyBlock("lantern").get())) {
                continue;
            }
            setYRot(360.0F / 16.0F * i);
            swingGlyphid();
            level().destroyBlock(hit.getBlockPos(), false);
        }
    }

    public void breakOffArmor() {
        byte armor = getArmorBits();
        List<Integer> indices = new ArrayList<>(List.of(0, 1, 2, 3, 4));
        Collections.shuffle(indices);
        for (int index : indices) {
            byte bit = (byte) (1 << index);
            if ((armor & bit) != 0) {
                setArmorBits((byte) (armor & ~bit));
                LegacySoundPlayer.playSoundAtEntity(this, "mob.zombie.woodbreak", 1.0F, 1.25F);
                break;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 1.7.10 EntityGlyphid#attackEntityFrom rejects every damage source
        // whose attacking entity is another Glyphid, not just acid damage.
        // This includes the source-backed digging/explosion paths.
        if (source.getEntity() instanceof EntityGlyphid) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public boolean attackSuperclass(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    @Override
    public float[] getCurrentDTDR(DamageSource damage, float amount, float pierceDT, float pierce) {
        if (DamageResistanceHandler.isAbsolute(damage)
                || DamageResistanceHandler.isUnblockableForLegacyResistance(damage)) {
            return new float[] {0.0F, 0.0F};
        }
        float threshold = getArmorThresholdMultiplier() * getGlyphidArmor() / 5.0F;
        float resistance = getArmorResistanceMultiplier();
        if (damage.is(ModDamageSources.NUCLEAR_BLAST)) {
            return new float[] {threshold * 0.25F, 0.0F};
        }
        String type = DamageResistanceHandler.exactTypeKey(damage);
        if ("laser".equals(type)) {
            return new float[] {threshold * 0.5F, resistance * 0.5F};
        }
        if ("electric".equals(type)) {
            return new float[] {threshold * 0.25F, resistance * 0.25F};
        }
        if ("subatomic".equals(type)) {
            return new float[] {0.0F, resistance * 0.1F};
        }
        if (damage.is(DamageTypeTags.IS_FIRE)) {
            return new float[] {0.0F, resistance * 0.2F};
        }
        if (damage.is(DamageTypeTags.IS_EXPLOSION)) {
            return new float[] {threshold * 0.5F, resistance * 0.35F};
        }
        return new float[] {threshold, resistance};
    }

    @Override
    public void onDamageDealt(DamageSource damage, float amount) {
        if (!level().isClientSide() && isArmorBroken(amount)) {
            breakOffArmor();
        }
    }

    protected float getArmorThresholdMultiplier() {
        return 1.0F;
    }

    protected float getArmorResistanceMultiplier() {
        return 0.1F;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level().isClientSide() || !doesInfectedSpawnMaggots() || getSubtype() != TYPE_INFECTED) {
            return;
        }

        int count = 2 + random.nextInt(3);
        for (int index = 0; index < count; index++) {
            float xOffset = (index % 2 - 0.5F) * 0.5F;
            float zOffset = (index / 2 - 0.5F) * 0.5F;
            EntityParasiteMaggot maggot = new EntityParasiteMaggot(ModEntityTypes.PARASITE_MAGGOT.get(), level());
            maggot.moveTo(getX() + xOffset, getY() + 0.5D, getZ() + zOffset, random.nextFloat() * 360.0F, 0.0F);
            maggot.setDeltaMovement(xOffset, 0.0D, zOffset);
            level().addFreshEntity(maggot);
        }
        LegacySoundPlayer.playSoundAtEntity(this, "mob.zombie.woodbreak", 2.0F,
                0.95F + level().random.nextFloat() * 0.2F);
        ParticleUtil.spawnGiblets(this, ParticleUtil.GIBLET_MEAT);
    }

    public boolean doesInfectedSpawnMaggots() {
        return true;
    }

    public boolean isNuclearGlyphid() {
        return false;
    }

    public int getCurrentTask() {
        return currentTask;
    }

    public EntityWaypoint getWaypoint() {
        return taskWaypoint;
    }

    public void setCurrentTask(int task, EntityWaypoint waypoint) {
        currentTask = task;
        taskWaypoint = waypoint;
        hasWaypoint = waypoint != null;
        if (waypoint != null) {
            taskX = (int) waypoint.getX();
            taskY = (int) waypoint.getY();
            taskZ = (int) waypoint.getZ();
            if (waypoint.highPriority) {
                setTarget(null);
                getNavigation().stop();
            }
        }
        carryOutTask();
    }

    protected void carryOutTask() {
        if (currentTask == TASK_RETREAT_FOR_REINFORCEMENTS && taskWaypoint != null) {
            communicate(TASK_FOLLOW, taskWaypoint);
            setCurrentTask(TASK_FOLLOW, taskWaypoint);
        } else if (currentTask == TASK_INITIATE_RETREAT && taskWaypoint == null && !level().isClientSide()) {
            EntityWaypoint additional = new EntityWaypoint(level());
            additional.moveTo(getX(), getY(), getZ(), 0.0F, 0.0F);
            EntityWaypoint home = new EntityWaypoint(level());
            home.setWaypointType(TASK_RETREAT_FOR_REINFORCEMENTS);
            home.setAdditionalWaypoint(additional);
            home.setHighPriority();
            home.moveTo(homeX, homeY, homeZ, 0.0F, 0.0F);
            level().addFreshEntity(home);
            taskWaypoint = home;
            communicate(TASK_FOLLOW, home);
            setCurrentTask(TASK_FOLLOW, home);
        }
    }

    public void communicate(int task, EntityWaypoint waypoint) {
        int radius = waypoint != null ? waypoint.radius : 4;
        AABB box = new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(radius);
        for (Entity entity : level().getEntities(this, box)) {
            if (entity instanceof EntityGlyphid glyphid && !(glyphid instanceof EntityGlyphidScout)
                    && glyphid.getCurrentTask() != task) {
                glyphid.setCurrentTask(task, waypoint);
            }
        }
    }

    public boolean isAtDestination() {
        int destinationRadius = taskWaypoint != null ? taskWaypoint.radius * taskWaypoint.radius : 25;
        return distanceToSqr(taskX, taskY, taskZ) <= destinationRadius;
    }

    /** Source extension point for the Scout hive-expansion task. */
    public boolean expandHive() {
        return false;
    }

    private void tickTaskState() {
        if (currentTask == TASK_FOLLOW && isAtDestination() && !hasWaypoint) {
            setCurrentTask(TASK_IDLE, null);
        }
        if (currentTask == TASK_DIG && tickCount % 20 == 0 && isAtDestination()) {
            swingGlyphid();
            new ExplosionVnt(level(), taskX, taskY + 2.0D, taskZ, getBlastSize(), this)
                    .setBlockAllocator(new BlockAllocatorGlyphidDig(getBlastResistanceToDig()))
                    .setBlockProcessor(new BlockProcessorStandard().setNoDrop())
                    .setEntityProcessor(null)
                    .setPlayerProcessor(null)
                    .explode();
            setCurrentTask(previousTask, previousWaypoint);
            return;
        }
        if (currentTask != TASK_IDLE && !isAtDestination() && getNavigation().isDone()) {
            BlockHitResult obstruction = findWaypointObstruction();
            if (canDig() && getScale() >= 1.0F && currentTask != TASK_DIG && obstruction != null) {
                digToWaypoint(obstruction);
                return;
            }
            getNavigation().moveTo(taskX, taskY, taskZ, 1.0D);
        }
    }

    /** Source task-dig reach, capped exactly as the legacy field initialization. */
    protected int getBlastSize() {
        return Math.min((int) (3.0F * getScale()) / 2, 5);
    }

    /** Source task-dig resistance limit, derived from the concrete Glyphid scale. */
    protected int getBlastResistanceToDig() {
        return Math.min((int) (50.0F * (getScale() * 2.0F)), 150);
    }

    /** 1.20.1 carrier for legacy {@code findWaypointObstruction()}. */
    protected BlockHitResult findWaypointObstruction() {
        Vec3 start = new Vec3(getX(), getEyeY(), getZ());
        Vec3 target = new Vec3(taskX, taskY, taskZ);
        BlockHitResult hit = level().clip(new ClipContext(start, target, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockState state = level().getBlockState(hit.getBlockPos());
        return state.getBlock().getExplosionResistance() <= getBlastResistanceToDig() ? hit : null;
    }

    /** 1.20.1 carrier for legacy {@code digToWaypoint(MovingObjectPosition)}. */
    protected void digToWaypoint(BlockHitResult obstacle) {
        EntityWaypoint target = new EntityWaypoint(level());
        BlockPos pos = obstacle.getBlockPos();
        target.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        target.radius = 5;
        level().addFreshEntity(target);

        previousTask = getCurrentTask();
        previousWaypoint = getWaypoint();
        setCurrentTask(TASK_DIG, target);
        getNavigation().moveTo(taskX, taskY, taskZ, 1.0D);
        communicate(TASK_DIG, target);
    }

    public boolean getCanSpawnHere() {
        return level().getDifficulty() != Difficulty.PEACEFUL && checkSpawnObstruction(level());
    }

    @Override
    public boolean onClimbable() {
        return isBesideClimbableBlock();
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 slowdownMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, slowdownMultiplier);
        }
    }

    public boolean isBesideClimbableBlock() {
        return entityData.get(DATA_WALL);
    }

    public void setBesideClimbableBlock(boolean climbable) {
        entityData.set(DATA_WALL, climbable);
    }

    protected boolean canDig() {
        return MobConfig.rampantDig();
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        return super.checkSpawnRules(level, reason) && isValidLightLevel(level, blockPosition());
    }

    public boolean isValidLightLevel() {
        return isValidLightLevel(level(), blockPosition());
    }

    protected boolean isValidLightLevel(LevelAccessor level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return getTarget() == null && getCurrentTask() == TASK_IDLE && tickCount > 100;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (random.nextInt(2) != 0) {
            return;
        }
        RegistryObject<Item> item = isOnFire() ? ModItems.GLYPHID_MEAT_GRILLED : ModItems.GLYPHID_MEAT;
        int count = ((int) getScale() * 2) + looting;
        if (count > 0) {
            spawnAtLocation(new ItemStack(item.get(), count));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("armor", getArmorBits());
        tag.putByte("subtype", (byte) getSubtype());
        tag.putBoolean("hasHome", hasHome);
        tag.putInt("homeX", homeX);
        tag.putInt("homeY", homeY);
        tag.putInt("homeZ", homeZ);
        tag.putBoolean("hasWaypoint", hasWaypoint);
        tag.putInt("taskX", taskX);
        tag.putInt("taskY", taskY);
        tag.putInt("taskZ", taskZ);
        tag.putInt("task", currentTask);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setArmorBits(tag.getByte("armor"));
        setSubtype(tag.getByte("subtype"));
        hasHome = tag.getBoolean("hasHome");
        homeX = tag.getInt("homeX");
        homeY = tag.getInt("homeY");
        homeZ = tag.getInt("homeZ");
        hasWaypoint = tag.getBoolean("hasWaypoint");
        taskX = tag.getInt("taskX");
        taskY = tag.getInt("taskY");
        taskZ = tag.getInt("taskZ");
        currentTask = tag.getInt("task");
    }

    public static boolean canSpawnAt(ServerLevelAccessor level, BlockPos pos) {
        return level.getBrightness(LightLayer.BLOCK, pos) <= 7;
    }
}
