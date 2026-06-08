package com.hbm.ntm.ability;

import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class WeaponAbilities {
    public static final IWeaponAbility NONE = new BaseWeaponAbility("", 0) {
        @Override
        public void onHit(int level, WeaponHitContext context) {
        }
    };

    public static final IWeaponAbility RADIATION = new BaseWeaponAbility("weapon.ability.radiation", 1) {
        private final float[] radAtLevel = { 15.0F, 50.0F, 500.0F };

        @Override
        public int levels() {
            return radAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + radAtLevel[level] + ")";
        }

        @Override
        public void onHit(int level, WeaponHitContext context) {
            if (context.victim() instanceof LivingEntity living) {
                RadiationUtil.contaminate(living, HazardType.RADIATION, RadiationUtil.ContaminationType.CREATIVE, radAtLevel[level]);
            }
        }
    };

    public static final IWeaponAbility VAMPIRE = new BaseWeaponAbility("weapon.ability.vampire", 2) {
        private final float[] amountAtLevel = { 2.0F, 3.0F, 5.0F, 10.0F, 50.0F };

        @Override
        public int levels() {
            return amountAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + amountAtLevel[level] + ")";
        }

        @Override
        public void onHit(int level, WeaponHitContext context) {
            if (context.victim() instanceof LivingEntity living && living.getHealth() > 0.0F) {
                float amount = amountAtLevel[level];
                living.setHealth(Math.max(0.0F, living.getHealth() - amount));
                context.player().heal(amount);
            }
        }
    };

    public static final IWeaponAbility STUN = new DurationWeaponAbility("weapon.ability.stun", 3, new int[] { 2, 3, 5, 10, 15 }) {
        @Override
        public void onHit(int level, WeaponHitContext context) {
            if (context.victim() instanceof LivingEntity living) {
                int duration = durationAtLevel[level] * 20;
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 4));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 4));
            }
        }
    };

    public static final IWeaponAbility PHOSPHORUS = new DurationWeaponAbility("weapon.ability.phosphorus", 4, new int[] { 60, 90 }) {
        @Override
        public void onHit(int level, WeaponHitContext context) {
            if (context.victim() instanceof LivingEntity living) {
                HbmLivingProperties.ensurePhosphorus(living, durationAtLevel[level] * 20);
            }
        }
    };

    public static final IWeaponAbility FIRE = new DurationWeaponAbility("weapon.ability.fire", 6, new int[] { 5, 10 }) {
        @Override
        public void onHit(int level, WeaponHitContext context) {
            context.victim().setSecondsOnFire(durationAtLevel[level]);
        }
    };

    public static final IWeaponAbility CHAINSAW = new BaseWeaponAbility("weapon.ability.chainsaw", 7) {
        private final int[] dividerAtLevel = { 15, 10 };

        @Override
        public int levels() {
            return dividerAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (1:" + dividerAtLevel[level] + ")";
        }

        @Override
        public void onHit(int level, WeaponHitContext context) {
        }
    };

    public static final IWeaponAbility BEHEADER = new BaseWeaponAbility("weapon.ability.beheader", 8) {
        @Override
        public void onHit(int level, WeaponHitContext context) {
        }
    };

    public static final IWeaponAbility BOBBLE = new BaseWeaponAbility("weapon.ability.bobble", 9) {
        @Override
        public void onHit(int level, WeaponHitContext context) {
        }
    };

    public static final IWeaponAbility[] ABILITIES = { NONE, RADIATION, VAMPIRE, STUN, PHOSPHORUS, FIRE, CHAINSAW, BEHEADER, BOBBLE };

    public static IWeaponAbility getByName(String name) {
        for (IWeaponAbility ability : ABILITIES) {
            if (ability.getName().equals(name)) {
                return ability;
            }
        }
        return NONE;
    }

    private abstract static class BaseWeaponAbility implements IWeaponAbility {
        private final String name;
        private final int sort;

        private BaseWeaponAbility(String name, int sort) {
            this.name = name;
            this.sort = sort;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int sortOrder() {
            return SORT_ORDER_BASE + sort;
        }
    }

    private abstract static class DurationWeaponAbility extends BaseWeaponAbility {
        protected final int[] durationAtLevel;

        private DurationWeaponAbility(String name, int sort, int[] durationAtLevel) {
            super(name, sort);
            this.durationAtLevel = durationAtLevel;
        }

        @Override
        public int levels() {
            return durationAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + durationAtLevel[level] + ")";
        }
    }

    private WeaponAbilities() {
    }
}
