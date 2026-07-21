package com.hbm.explosion;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Legacy-package bridge for the one modern ExplosionNT runtime.
 */
public class ExplosionNT extends com.hbm.ntm.explosion.ExplosionNT {
    public ExplosionNT(Level level, double x, double y, double z, float size) {
        super(level, x, y, z, size);
    }

    public ExplosionNT(Level level, @Nullable Entity source, double x, double y, double z, float size) {
        super(level, source, x, y, z, size);
    }

    @Override
    public ExplosionNT addAttrib(ExAttrib attribute) {
        super.addAttrib(attribute);
        return this;
    }

    @Override
    public ExplosionNT addAttrib(ExAttrib... attributes) {
        super.addAttrib(attributes);
        return this;
    }

    @Override
    public ExplosionNT addAttrib(Collection<ExAttrib> attributes) {
        super.addAttrib(attributes);
        return this;
    }

    @Override
    public ExplosionNT addAllAttrib(ExAttrib... attributes) {
        super.addAllAttrib(attributes);
        return this;
    }

    @Override
    public ExplosionNT addAllAttrib(Collection<ExAttrib> attributes) {
        super.addAllAttrib(attributes);
        return this;
    }

    @Override
    public ExplosionNT addNukeAttribs() {
        super.addNukeAttribs();
        return this;
    }

    @Override
    public ExplosionNT overrideResolution(int resolution) {
        super.overrideResolution(resolution);
        return this;
    }
}
