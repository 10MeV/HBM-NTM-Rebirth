package com.hbm.ntm.explosion;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorBalefire;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorDigamma;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorErode;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorFire;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorLava;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorPlaceBlock;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.CompositeBlockMutator;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectStandard;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

public class ExplosionNT {
    public static final EnumSet<ExAttrib> NUKE_ATTRIBS = EnumSet.of(
            ExAttrib.FIRE,
            ExAttrib.NOPARTICLE,
            ExAttrib.NOSOUND,
            ExAttrib.NODROP,
            ExAttrib.NOHURT);

    private final Level level;
    @Nullable
    private final Entity source;
    private final double x;
    private final double y;
    private final double z;
    private final float size;
    private final EnumSet<ExAttrib> attributes = EnumSet.noneOf(ExAttrib.class);
    private int resolution = 16;

    public ExplosionNT(Level level, double x, double y, double z, float size) {
        this(level, null, x, y, z, size);
    }

    public ExplosionNT(Level level, @Nullable Entity source, double x, double y, double z, float size) {
        this.level = level;
        this.source = source;
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
    }

    public ExplosionNT addAttrib(ExAttrib attribute) {
        attributes.add(attribute);
        return this;
    }

    public ExplosionNT addAttrib(ExAttrib... attributes) {
        this.attributes.addAll(Arrays.asList(attributes));
        return this;
    }

    public ExplosionNT addAttrib(Collection<ExAttrib> attributes) {
        this.attributes.addAll(attributes);
        return this;
    }

    public ExplosionNT addAllAttrib(ExAttrib... attributes) {
        return addAttrib(attributes);
    }

    public ExplosionNT addAllAttrib(Collection<ExAttrib> attributes) {
        return addAttrib(attributes);
    }

    public ExplosionNT addNukeAttribs() {
        return addAttrib(NUKE_ATTRIBS);
    }

    public boolean hasAttrib(ExAttrib attribute) {
        return attributes.contains(attribute);
    }

    public ExplosionNT overrideResolution(int resolution) {
        this.resolution = Math.max(1, resolution);
        return this;
    }

    public void explode() {
        ExplosionVnt explosion = new ExplosionVnt(level, x, y, z, size, source, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY)
                .setBlockAllocator(new BlockAllocatorStandard(resolution))
                .setBlockProcessor(createBlockProcessor())
                .setEffects(new ExplosionEffectStandard(!attributes.contains(ExAttrib.NOSOUND), !attributes.contains(ExAttrib.NOPARTICLE)));

        if (!attributes.contains(ExAttrib.NOHURT)) {
            explosion.setEntityProcessor(new EntityProcessorStandard().allowSelfDamage())
                    .setPlayerProcessor(new PlayerProcessorStandard());
        }

        explosion.explode();
    }

    private BlockProcessorStandard createBlockProcessor() {
        BlockProcessorStandard processor = new BlockProcessorStandard();
        if (attributes.contains(ExAttrib.NODROP)) {
            processor.setNoDrop();
        } else if (attributes.contains(ExAttrib.ALLDROP)) {
            processor.setAllDrop();
        }

        CompositeBlockMutator mutators = new CompositeBlockMutator();
        boolean allMod = attributes.contains(ExAttrib.ALLMOD);
        boolean placeAllSurfaceEffects = allMod || attributes.contains(ExAttrib.DIGAMMA);
        if (attributes.contains(ExAttrib.ERRODE)) {
            mutators.add(new BlockMutatorErode(allMod));
        }
        if (attributes.contains(ExAttrib.FIRE)) {
            mutators.add(new BlockMutatorFire(placeAllSurfaceEffects));
        }
        if (attributes.contains(ExAttrib.BALEFIRE)) {
            mutators.add(new BlockMutatorBalefire(placeAllSurfaceEffects));
        }
        if (attributes.contains(ExAttrib.LAVA)) {
            mutators.add(new BlockMutatorLava(placeAllSurfaceEffects));
        }
        if (attributes.contains(ExAttrib.DIGAMMA_CIRCUIT)) {
            mutators.add(new BlockMutatorDigamma(true));
        } else if (attributes.contains(ExAttrib.DIGAMMA)) {
            mutators.add(new BlockMutatorDigamma(false));
        }
        if (attributes.contains(ExAttrib.LAVA_V)) {
            mutators.add(new BlockMutatorPlaceBlock(ModBlocks.VOLCANIC_LAVA_BLOCK.get().defaultBlockState()));
        }
        if (attributes.contains(ExAttrib.LAVA_R)) {
            mutators.add(new BlockMutatorPlaceBlock(ModBlocks.RAD_LAVA_BLOCK.get().defaultBlockState()));
        }
        if (!mutators.isEmpty()) {
            processor.withBlockEffect(mutators);
        }
        return processor;
    }

    public enum ExAttrib {
        FIRE,
        BALEFIRE,
        DIGAMMA,
        DIGAMMA_CIRCUIT,
        LAVA,
        LAVA_V,
        LAVA_R,
        ERRODE,
        ALLMOD,
        ALLDROP,
        NODROP,
        NOPARTICLE,
        NOSOUND,
        NOHURT
    }
}
