package com.hbm.ntm.explosion;

import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import com.hbm.ntm.entity.logic.NukeExplosionMk5Entity;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import net.minecraft.world.level.Level;

public final class CustomNukeExplosion {
    public static final int MAX_TNT = 150;
    public static final int MAX_NUKE = 200;
    public static final int MAX_HYDRO = 350;
    public static final int MAX_AMAT = 350;
    public static final int MAX_SCHRAB = 250;

    public static void explode(Level level, double x, double y, double z, float tnt, float nuke, float hydro, float amat,
            float dirty, float schrab, float euph) {
        if (level == null || level.isClientSide()) {
            return;
        }

        dirty = Math.min(dirty, 100.0F);

        if (euph > 0.0F) {
            level.addFreshEntity(NukeExplosionMk3Entity.createFleija(level, x, y, z, 150));
            return;
        }

        if (schrab > 0.0F) {
            schrab = adjustedSchrab(tnt, nuke, hydro, amat, schrab);
            level.addFreshEntity(NukeExplosionMk3Entity.createFleija(level, x + 0.5D, y + 0.5D, z + 0.5D, (int) schrab));
            return;
        }

        if (amat > 0.0F) {
            amat = adjustedAntimatter(tnt, nuke, hydro, amat);
            WeaponExplosionUtil.spawnBalefire(level, x + 0.5D, y + 0.5D, z + 0.5D, (int) amat);
            return;
        }

        if (hydro > 0.0F) {
            hydro = adjustedHydrogen(tnt, nuke, hydro);
            dirty *= 0.25F;
            level.addFreshEntity(NukeExplosionMk5Entity.create(level, (int) hydro, x + 0.5D, y + 0.5D, z + 0.5D)
                    .moreFallout((int) dirty));
            return;
        }

        if (nuke > 0.0F) {
            nuke = adjustedNuclear(tnt, nuke);
            level.addFreshEntity(NukeExplosionMk5Entity.create(level, (int) nuke, x + 0.5D, y + 5.0D, z + 0.5D)
                    .moreFallout((int) dirty));
            return;
        }

        if (tnt >= 75.0F) {
            tnt = Math.min(tnt, MAX_TNT);
            level.addFreshEntity(NukeExplosionMk5Entity.createNoFallout(level, (int) tnt, x + 0.5D, y + 0.5D, z + 0.5D));
        } else if (tnt > 0.0F) {
            ExplosionLarge.explode(level, x + 0.5D, y + 0.5D, z + 0.5D, tnt, true, true, true);
        }
    }

    public static float adjustedNuclear(float tnt, float nuke) {
        return nuke == 0.0F ? 0.0F : Math.min(nuke + tnt / 2.0F, MAX_NUKE);
    }

    public static float adjustedHydrogen(float tnt, float nuke, float hydro) {
        return hydro == 0.0F ? 0.0F : Math.min(hydro + nuke / 2.0F + tnt / 4.0F, MAX_HYDRO);
    }

    public static float adjustedAntimatter(float tnt, float nuke, float hydro, float amat) {
        return amat == 0.0F ? 0.0F : Math.min(amat + hydro / 2.0F + nuke / 4.0F + tnt / 8.0F, MAX_AMAT);
    }

    public static float adjustedSchrab(float tnt, float nuke, float hydro, float amat, float schrab) {
        return schrab == 0.0F ? 0.0F
                : Math.min(schrab + amat / 2.0F + hydro / 4.0F + nuke / 8.0F + tnt / 16.0F, MAX_SCHRAB);
    }

    private CustomNukeExplosion() {
    }
}
