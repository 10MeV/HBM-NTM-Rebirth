package com.hbm.entity.mob.glyphid;

import com.hbm.lib.Library;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.projectile.RubbleEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.multiblock.DummyBlock;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@Deprecated(forRemoval = false)
public class EntityGlyphidDigger extends EntityGlyphid {
    private Entity lastTarget;
    private double lastX;
    private double lastY;
    private double lastZ;
    public int timer;

    public EntityGlyphidDigger(EntityType<? extends EntityGlyphidDigger> type, Level level) {
        super(type, level);
    }

    public EntityGlyphidDigger(Level level) {
        this(ModEntityTypes.GLYPHID_DIGGER.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid_digger.png");
    }

    @Override
    public float getScale() {
        return 1.3F;
    }

    @Override
    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount * 0.25D, 2.0D), 100.0D);
    }

    @Override
    protected float getArmorThresholdMultiplier() {
        return 3.0F;
    }

    @Override
    protected float getArmorResistanceMultiplier() {
        return 0.2F;
    }

    @Override
    public void tick() {
        super.tick();

        Entity target = getTarget();
        if (level().isClientSide || target == null || !isAlive()) {
            return;
        }

        lastX = target.getX();
        lastY = target.getY();
        lastZ = target.getZ();
        if (--timer <= 0) {
            groundSlam();
            timer = 120;
        }
    }

    /**
     * 1.7.10 EntityGlyphidDigger ground-slam: tear eligible blocks from the
     * forward fan and launch them as rubble on the same ballistic arc.
     */
    public void groundSlam() {
        Entity target = getTarget();
        if (level().isClientSide || target == null || distanceToSqr(target) >= 30.0D * 30.0D) {
            return;
        }

        int bugX = (int) getX();
        int bugY = (int) getY();
        int bugZ = (int) getZ();
        Vec3 pathDirection = getViewVector(1.0F);
        List<int[]> path = Library.getBlockPosInPath(bugX, bugY, bugZ, 6, pathDirection);
        for (int i = 0; i < 8; i++) {
            pathDirection = pathDirection.yRot(-1.0F / 16.0F);
            path.addAll(Library.getBlockPosInPath(bugX, bugY - 1, bugZ, 6, pathDirection));
        }

        double velocityX = target.getX() - lastX;
        double velocityY = target.getY() - lastY;
        double velocityZ = target.getZ() - lastZ;
        // The legacy class never assigns lastTarget; preserve that prediction boundary.
        if (lastTarget != target) {
            velocityX = 0.0D;
            velocityY = 0.0D;
            velocityZ = 0.0D;
        }

        boolean topAttack = distanceTo(target) > 20.0F;
        Vec3 delta = new Vec3(target.getX() - getX() + velocityX * 60.0D,
                target.getY() + target.getBbHeight() / 2.0D - (getY() + 1.0D) + velocityY * 60.0D,
                target.getZ() - getZ() + velocityZ * 60.0D);
        if (delta.length() < 3.0D) {
            return;
        }

        double targetYaw = -Math.atan2(delta.x, delta.z);
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double v0 = 1.2D;
        double v02 = v0 * v0;
        double gravity = 0.03D;
        double upperLower = topAttack ? 1.0D : -1.0D;
        double discriminant = v02 * v02 - gravity * (gravity * horizontalDistance * horizontalDistance
                + 2.0D * delta.y * v02);
        double targetPitch = Math.atan((v02 + Math.sqrt(discriminant) * upperLower) / (gravity * horizontalDistance));
        Vec3 launch = Double.isNaN(targetPitch) ? null
                : new Vec3(v0, 0.0D, 0.0D).zRot((float) -targetPitch)
                        .yRot((float) -(targetYaw + Math.PI * 0.5D));

        float concreteResistance = ModBlocks.legacyBlock("concrete").get().getExplosionResistance();
        for (int[] coordinates : path) {
            BlockPos pos = new BlockPos(coordinates[0], coordinates[1], coordinates[2]);
            BlockState state = level().getBlockState(pos);
            if (state.getExplosionResistance(level(), pos, null) >= concreteResistance
                    || !state.isSolidRender(level(), pos)
                    || state.getBlock() instanceof DummyBlock
                    || state.getBlock() instanceof MultiblockCoreBlock
                    || level().getBlockEntity(pos) != null) {
                continue;
            }

            RubbleEntity rubble = new RubbleEntity(level());
            rubble.moveTo(pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, 0.0F, 0.0F);
            rubble.setBlockState(state);
            rubble.setOwner(this);
            if (launch != null) {
                rubble.setDeltaMovement(legacyThrowVelocity(launch, v0, random.nextFloat()));
            }
            level().addFreshEntity(rubble);
            level().removeBlock(pos, false);
        }
    }

    private Vec3 legacyThrowVelocity(Vec3 direction, double velocity, float inaccuracy) {
        Vec3 normalized = direction.normalize();
        normalized = normalized.add(random.nextGaussian() * 0.0075D * inaccuracy,
                random.nextGaussian() * 0.0075D * inaccuracy,
                random.nextGaussian() * 0.0075D * inaccuracy);
        return normalized.scale(velocity);
    }

    @Override
    protected boolean canDig() {
        return true;
    }
}
