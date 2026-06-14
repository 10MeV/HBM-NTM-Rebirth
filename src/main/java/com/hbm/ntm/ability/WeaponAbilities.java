package com.hbm.ntm.ability;

import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.item.TrinketBlockItem;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

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
                float remaining = living.getHealth() - amount;
                living.setHealth(Math.max(0.0F, remaining));
                if (remaining <= 0.0F) {
                    living.die(living.damageSources().magic());
                }
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
            if (!(context.level() instanceof ServerLevel serverLevel)
                    || !(context.victim() instanceof LivingEntity living)
                    || living.getHealth() > 0.0F) {
                return;
            }

            RegistryObject<Item> nitra = ModItems.legacyItem("nitra_small");
            if (nitra == null) {
                return;
            }

            int count = Math.min((int) Math.ceil(living.getMaxHealth() / dividerAtLevel[level]), 250);
            for (int i = 0; i < count; i++) {
                drop(living, new ItemStack(nitra.get()));
                serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, living.getX(), living.getY(), living.getZ(), 1));
            }

            ParticleUtil.spawnGiblets(living, ParticleUtil.GIBLET_MEAT);
            LegacySoundPlayer.playLegacyChainsaw(serverLevel,
                    living.getX(), living.getY() + living.getBbHeight() * 0.5D, living.getZ());
        }
    };

    public static final IWeaponAbility BEHEADER = new BaseWeaponAbility("weapon.ability.beheader", 8) {
        @Override
        public void onHit(int level, WeaponHitContext context) {
            if (!(context.victim() instanceof LivingEntity living) || living.getHealth() > 0.0F) {
                return;
            }

            if (living instanceof WitherSkeleton) {
                if (living.level().random.nextInt(20) == 0) {
                    drop(living, new ItemStack(Items.WITHER_SKELETON_SKULL));
                } else {
                    drop(living, new ItemStack(Items.COAL, 3));
                }
            } else if (living instanceof Skeleton) {
                drop(living, new ItemStack(Items.SKELETON_SKULL));
            } else if (living instanceof Zombie) {
                drop(living, new ItemStack(Items.ZOMBIE_HEAD));
            } else if (living instanceof Creeper) {
                drop(living, new ItemStack(Items.CREEPER_HEAD));
            } else if (living instanceof MagmaCube) {
                drop(living, new ItemStack(Items.MAGMA_CREAM, 3));
            } else if (living instanceof Slime) {
                drop(living, new ItemStack(Items.SLIME_BALL, 3));
            } else if (living instanceof Player player) {
                ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                CompoundTag tag = new CompoundTag();
                tag.putString("SkullOwner", player.getGameProfile().getName());
                head.setTag(tag);
                drop(living, head);
            } else {
                drop(living, new ItemStack(Items.ROTTEN_FLESH, 3));
                drop(living, new ItemStack(Items.BONE, 2));
            }
        }
    };

    public static final IWeaponAbility BOBBLE = new BaseWeaponAbility("weapon.ability.bobble", 9) {
        @Override
        public void onHit(int level, WeaponHitContext context) {
            if (!(context.victim() instanceof Monster mob) || mob.getHealth() > 0.0F) {
                return;
            }

            int chance = mob.getMaxHealth() > 20.0F ? 750 : 1000;
            if (mob.level().random.nextInt(chance) != 0) {
                return;
            }

            RegistryObject<? extends net.minecraft.world.level.block.Block> bobblehead = ModBlocks.legacyBlock("bobblehead");
            if (bobblehead == null) {
                return;
            }

            int variantCount = TrinketVariant.variantCount(TrinketVariant.Kind.BOBBLEHEAD);
            int variant = mob.level().random.nextInt(variantCount - 1) + 1;
            drop(mob, TrinketBlockItem.createStack(bobblehead.get().asItem(), variant));
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

    private static void drop(LivingEntity living, ItemStack stack) {
        living.spawnAtLocation(stack, 0.0F);
    }

    private WeaponAbilities() {
    }
}
