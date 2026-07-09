package com.hbm.ntm.entity.projectile;

import com.hbm.config.GeneralConfig;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WastePearlEntity extends LegacyThrowableEntity {
    public WastePearlEntity(EntityType<? extends WastePearlEntity> type, Level level) {
        super(type, level);
    }

    public WastePearlEntity(Level level) {
        this(ModEntityTypes.WASTE_PEARL.get(), level);
    }

    public WastePearlEntity(Level level, LivingEntity thrower) {
        this(level);
        setOwner(thrower);
        setPos(thrower.getX(), thrower.getEyeY() - 0.1D, thrower.getZ());
    }

    public WastePearlEntity(Level level, double x, double y, double z) {
        this(level);
        setPos(x, y, z);
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (hit instanceof EntityHitResult entityHit) {
            entityHit.getEntity().hurt(level().damageSources().thrown(this, getOwner()), 0.0F);
        }
        if (!level().isClientSide() && GeneralConfig.enableExtendedLogging()) {
            HbmNtm.LOGGER.info("[GREN] Set off grenade at {} / {} / {} by {}!",
                    (int) getX(), (int) getY(), (int) getZ(), throwerLogName());
        }
        explode();
    }

    public void explode() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        discard();

        int x = Mth.floor(getX());
        int y = Mth.floor(getY());
        int z = Mth.floor(getZ());
        BlockState fallout = ModBlocks.FALLOUT.get().defaultBlockState();

        for (int ix = x - 3; ix <= x + 3; ix++) {
            for (int iy = y - 3; iy <= y + 3; iy++) {
                for (int iz = z - 3; iz <= z + 3; iz++) {
                    BlockPos pos = new BlockPos(ix, iy, iz);
                    if (serverLevel.isOutsideBuildHeight(pos)) {
                        continue;
                    }
                    BlockState state = serverLevel.getBlockState(pos);
                    if (serverLevel.random.nextInt(3) == 0
                            && state.canBeReplaced()
                            && fallout.canSurvive(serverLevel, pos)) {
                        serverLevel.setBlock(pos, fallout, Block.UPDATE_ALL);
                    } else if (state.isAir()) {
                        BlockState gas = random.nextBoolean()
                                ? ModBlocks.GAS_RADON.get().defaultBlockState()
                                : ModBlocks.GAS_RADON_DENSE.get().defaultBlockState();
                        serverLevel.setBlock(pos, gas, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private String throwerLogName() {
        Entity owner = getOwner();
        return owner instanceof Player player ? player.getDisplayName().getString() : "null";
    }
}
