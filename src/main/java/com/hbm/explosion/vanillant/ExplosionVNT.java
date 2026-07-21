package com.hbm.explosion.vanillant;

import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;
import com.hbm.explosion.vanillant.interfaces.IBlockProcessor;
import com.hbm.explosion.vanillant.interfaces.IEntityProcessor;
import com.hbm.explosion.vanillant.interfaces.IExplosionSFX;
import com.hbm.explosion.vanillant.interfaces.IPlayerProcessor;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy-named facade over the single modern VNT explosion runtime.
 * Modernized interface parameters deliberately use 1.20.1 world types.
 */
@Deprecated(forRemoval = false)
public class ExplosionVNT extends ExplosionVnt {
    public ExplosionVNT(Level level, double x, double y, double z, float size) {
        super(level, x, y, z, size);
    }

    public ExplosionVNT(Level level, double x, double y, double z, float size, @Nullable Entity exploder) {
        super(level, x, y, z, size, exploder);
    }

    public ExplosionVNT(Level level, double x, double y, double z, float size, @Nullable Entity exploder,
                        boolean fire, Explosion.BlockInteraction blockInteraction) {
        super(level, x, y, z, size, exploder, fire, blockInteraction);
    }

    public ExplosionVNT setBlockAllocator(IBlockAllocator blockAllocator) {
        super.setBlockAllocator(blockAllocator);
        return this;
    }

    public ExplosionVNT setEntityProcessor(IEntityProcessor entityProcessor) {
        super.setEntityProcessor(entityProcessor);
        return this;
    }

    public ExplosionVNT setBlockProcessor(IBlockProcessor blockProcessor) {
        super.setBlockProcessor(blockProcessor);
        return this;
    }

    public ExplosionVNT setPlayerProcessor(IPlayerProcessor playerProcessor) {
        super.setPlayerProcessor(playerProcessor);
        return this;
    }

    public ExplosionVNT setSFX(IExplosionSFX... effects) {
        super.setSFX(effects);
        return this;
    }

    @Override
    public ExplosionVNT makeStandard() {
        super.makeStandard();
        return this;
    }

    @Override
    public ExplosionVNT makeAmat() {
        super.makeAmat();
        return this;
    }
}
