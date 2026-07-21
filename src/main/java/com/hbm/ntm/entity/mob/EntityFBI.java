package com.hbm.ntm.entity.mob;

import com.hbm.ntm.config.MobConfig;
import com.hbm.ntm.entity.ai.LegacyMobBreakingGoal;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmModelRenderDistances;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Direct 1.20.1 migration of the ordinary 1.7.10 {@code EntityFBI} mob. */
public final class EntityFBI extends Monster {
    private static final double LEGACY_PARTIAL_PATH_RANGE = 16.0D;
    private static final Set<Block> LEGACY_BREAKABLE_BLOCKS = Set.of(
            Blocks.IRON_DOOR,
            ModBlocks.MACHINE_PRESS.get(), ModBlocks.MACHINE_EPRESS.get(),
            ModBlocks.MACHINE_CHEMICAL_PLANT.get(), ModBlocks.MACHINE_CHEMICAL_FACTORY.get(),
            ModBlocks.MACHINE_CRYSTALLIZER.get(), ModBlocks.MACHINE_TURBINE.get(),
            ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get(), ModBlocks.MACHINE_CHUNGUS.get(),
            ModBlocks.MACHINE_PUREX.get(), ModBlocks.CRATE_IRON.get(), ModBlocks.CRATE_STEEL.get(),
            ModBlocks.MACHINE_DIESEL.get(), ModBlocks.MACHINE_RTG_GREY.get(), ModBlocks.MACHINE_MINIRTG.get(),
            ModBlocks.MACHINE_POWERRTG.get(), ModBlocks.MACHINE_CYCLOTRON.get(), Blocks.CHEST, Blocks.TRAPPED_CHEST);

    public EntityFBI(EntityType<? extends EntityFBI> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new LegacyMobBreakingGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType spawnType, SpawnGroupData spawnGroupData,
            net.minecraft.nbt.CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(getRandom().nextBoolean()
                ? ModItems.GUN_HEAVY_REVOLVER.get() : ModItems.GUN_SPAS12.get()));
        if (getRandom().nextInt(5) == 0) {
            equipSecurityArmor();
        }
        if (!level().dimension().equals(Level.OVERWORLD)) {
            setItemSlot(EquipmentSlot.HEAD, new ItemStack(Blocks.GLASS));
            setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.PAA_PLATE.get()));
            setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.PAA_LEGS.get()));
            setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.PAA_BOOTS.get()));
        }
        return result;
    }

    private void equipSecurityArmor() {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.SECURITY_HELMET.get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.SECURITY_PLATE.get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.SECURITY_LEGS.get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.SECURITY_BOOTS.get()));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (getTarget() == null) {
            setTarget(level().getNearestPlayer(this, 128.0D));
        }
        if (getTarget() != null) {
            followLegacyPartialPath(getTarget());
        }
    }

    private void followLegacyPartialPath(LivingEntity target) {
        Vec3 toTarget = target.position().subtract(position());
        if (toTarget.lengthSqr() <= 0.0D) {
            return;
        }
        BlockPos endpoint = BlockPos.containing(position().add(toTarget.normalize().scale(LEGACY_PARTIAL_PATH_RANGE)));
        int walkY = findLegacyWalkableY(endpoint);
        Path path = getNavigation().createPath(new BlockPos(endpoint.getX(), walkY, endpoint.getZ()),
                (int) LEGACY_PARTIAL_PATH_RANGE);
        if (path != null) {
            getNavigation().moveTo(path, 1.0D);
        }
    }

    private int findLegacyWalkableY(BlockPos endpoint) {
        int minY = level().getMinBuildHeight();
        int maxY = level().getMaxBuildHeight() - 1;
        int initialY = Mth.clamp(endpoint.getY(), minY + 1, maxY);
        for (int y = initialY; y > Math.max(minY, initialY - 10); y--) {
            if (level().isEmptyBlock(new BlockPos(endpoint.getX(), y, endpoint.getZ()))
                    && level().getBlockState(new BlockPos(endpoint.getX(), y - 1, endpoint.getZ())).blocksMotion()) {
                return y;
            }
        }
        for (int y = Math.min(maxY, initialY + 10); y > initialY; y--) {
            if (level().isEmptyBlock(new BlockPos(endpoint.getX(), y, endpoint.getZ()))
                    && level().getBlockState(new BlockPos(endpoint.getX(), y - 1, endpoint.getZ())).blocksMotion()) {
                return y;
            }
        }
        return initialY;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide() || !isAlive()) {
            return;
        }
        if (tickCount % MobConfig.raidAttackDelay() == 0) {
            breakLegacyTargetBlock();
        }
        for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(1.5D))) {
            item.setSecondsOnFire(10);
        }
    }

    private void breakLegacyTargetBlock() {
        double reach = MobConfig.raidAttackReach();
        float rotation = (float) (Math.PI * 2.0D * getRandom().nextFloat());
        Vec3 vector = new Vec3(Mth.cos(rotation) * reach, 0.0D, -Mth.sin(rotation) * reach);
        Vec3 start = new Vec3(getX(), getY() + 0.5D + getRandom().nextFloat(), getZ());
        BlockHitResult hit = level().clip(new ClipContext(start, start.add(vector), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK && isLegacyBreakable(level().getBlockState(hit.getBlockPos()))) {
            level().destroyBlock(hit.getBlockPos(), false);
        }
    }

    private static boolean isLegacyBreakable(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(BlockTags.WOODEN_DOORS) || state.is(BlockTags.WOODEN_TRAPDOORS)
                || LEGACY_BREAKABLE_BLOCKS.contains(state.getBlock());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof EntityFBI) {
            return false;
        }
        if (getItemBySlot(EquipmentSlot.HEAD).is(Blocks.GLASS.asItem())
                && (source.is(ModDamageSources.OXYGEN_SUFFOCATION) || source.is(ModDamageSources.THERMAL))) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (!getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            return false;
        }
        if (!level().isClientSide()) {
            setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.GAS_MASK_M65.get()));
        }
        return false;
    }

    @Override
    public int getArmorValue() {
        return 20;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }
}
