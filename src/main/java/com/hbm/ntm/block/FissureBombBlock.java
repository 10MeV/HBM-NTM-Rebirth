package com.hbm.ntm.block;

import com.hbm.ntm.explosion.ExplosionNukeSmall;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Legacy fissure bomb with the shared TNT ignition and chain-explosion contract. */
public class FissureBombBlock extends LegacyTntBaseBlock {

    public FissureBombBlock(Properties properties) {
        super(properties, Kind.TNT);
    }

    @Override
    public void explodeEntity(Level level, Vec3 position, @Nullable Entity source) {
        if (!level.isClientSide) {
            ExplosionNukeSmall.explode(level, position.x, position.y, position.z, ExplosionNukeSmall.PARAMS_MEDIUM);
        }
    }
}
