package com.hbm.ntm.item;

import com.hbm.items.weapon.ItemGenericGrenade;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.ntm.bullet.BulletSpecialSpawnUtil;
import com.hbm.ntm.damage.DamageClass;
import com.hbm.ntm.entity.effect.FireLingeringEntity;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.entity.projectile.DynamiteStickEntity;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockMutatorFire;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCrossSmooth;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectWeapon;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class UniversalGrenadeItem extends ItemGenericGrenade {
    public static final String KEY_SHELL = "shell";
    public static final String KEY_FILLING = "filling";
    public static final String KEY_FUZE = "fuze";
    public static final String KEY_EXTRA = "extra";

    public UniversalGrenadeItem(Item.Properties properties) {
        super(15, properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getShell(stack).stackLimit();
    }

    @Override
    public int getMaxTimer(ItemStack stack) {
        return switch (getFuze(stack)) {
            case S3 -> 60;
            case S7 -> 140;
            case S15 -> 300;
            case IMPACT, AIRBURST -> Integer.MAX_VALUE;
        };
    }

    @Override
    public double getBounceMod(ItemStack stack) {
        return getShell(stack).bounce();
    }

    @Override
    public double getThrowForce(ItemStack stack) {
        return getShell(stack).yeetForce();
    }

    @Override
    public void onGrenadeTick(Entity grenade, ItemStack stack, int timer) {
        if (grenade.level().isClientSide() || grenade.isRemoved()) {
            return;
        }
        if (getFuze(stack) == Fuze.AIRBURST && timer >= 30 && airburstHit(grenade)) {
            explodeEntity(grenade);
            return;
        }
        if (getExtra(stack) == Extra.PROXY_FUZE && timer >= 10 && timer % 3 == 0 && proxyTriggered(grenade)) {
            explodeEntity(grenade);
        }
    }

    @Override
    public boolean onGrenadeBlockHit(Entity grenade, BlockHitResult hit, ItemStack stack, int timer) {
        if (!grenade.level().isClientSide() && getFuze(stack) == Fuze.IMPACT && timer >= 10) {
            grenade.setPos(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
            explodeEntity(grenade);
            return true;
        }
        if (getExtra(stack) == Extra.GLUE && grenade instanceof DynamiteStickEntity dynamite) {
            dynamite.stickTo(hit);
            return true;
        }
        return false;
    }

    @Override
    public boolean onGrenadeEntityHit(Entity grenade, EntityHitResult hit, ItemStack stack, int timer) {
        if (!grenade.level().isClientSide() && getFuze(stack) == Fuze.IMPACT && timer >= 10) {
            Vec3 location = hit.getLocation();
            grenade.setPos(location.x, location.y, location.z);
            explodeEntity(grenade);
        }
        return true;
    }

    @Override
    public void explode(Entity grenade, LivingEntity thrower, Level level, double x, double y, double z,
            ItemStack stack) {
        if (level.isClientSide()) {
            return;
        }
        Filling filling = getFilling(stack);
        Shell shell = getShell(stack);
        switch (filling) {
            case POWDER -> standardExplode(level, thrower, x, y, z, 5.0F, 10.0F, 5.0F, 0.0F);
            case HE -> standardExplode(level, thrower, x, y, z, 7.5F, 25.0F, 10.0F, 0.1F);
            case DEMO -> demoExplode(level, thrower, x, y, z);
            case INC -> incendiaryExplode(level, thrower, x, y, z, 2, 200, FireLingeringEntity.TYPE_DIESEL);
            case WP -> whitePhosphorusExplode(level, thrower, x, y, z);
            case CLUSTER -> {
                standardExplode(level, thrower, x, y, z, 7.5F, 15.0F, 10.0F, 0.1F);
                BulletProjectileEntity.spawnAll(level, BulletSpecialSpawnUtil.collectLegacyGrenadeClusterPellets(
                        level, thrower, new Vec3(x, y, z), false, shell == Shell.FRAG, level.random));
            }
            case EMP -> {
                WeaponExplosionUtil.explodeStandardEnergy(level, x, y, z, 3.0F, thrower, 15.0F,
                        DamageClass.ELECTRIC, 0.5F, 0.5F, 1.0F, 3.0F);
                ExplosionNukeGeneric.empBlast(level, floor(x), floor(y), floor(z), 5);
            }
            case PLASMA -> WeaponExplosionUtil.explodeStandardEnergy(level, x, y, z, 5.0F, thrower, 50.0F,
                    DamageClass.PLASMA, 0.5F, 1.0F, 0.5F, 4.0F);
            case LASER -> {
                WeaponExplosionUtil.tinySmooth(level, x, y, z, 2.0F, thrower, 5.0F).explode();
                BulletProjectileEntity.spawnAll(level, BulletSpecialSpawnUtil.collectLegacyGrenadeLaserBeams(
                        level, thrower, new Vec3(x, y, z), level.random));
            }
            case CLUSTER_HEAVY -> {
                standardExplode(level, thrower, x, y, z, 7.5F, 15.0F, 10.0F, 0.1F);
                BulletProjectileEntity.spawnAll(level, BulletSpecialSpawnUtil.collectLegacyGrenadeClusterPellets(
                        level, thrower, new Vec3(x, y, z), true, false, level.random));
            }
            case NUCLEAR -> nuclearExplode(level, thrower, x, y, z, false);
            case NUCLEAR_DEMO -> nuclearExplode(level, thrower, x, y, z, true);
            case SCHRAB -> {
                if (NuclearExplosionUtil.spawnFleija(level, x, y, z, 20)) {
                    level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                            100.0F, level.random.nextFloat() * 0.1F + 0.9F);
                }
            }
        }

        Extra extra = getExtra(stack);
        if (extra == Extra.FRAG_SLEEVE) {
            BulletProjectileEntity.spawnAll(level, BulletSpecialSpawnUtil.collectLegacyGrenadeFragmentation(
                    level, thrower, new Vec3(x, y, z), 25.0F, shell == Shell.FRAG, level.random));
        } else if (extra == Extra.TRIPLEX) {
            spawnTriplex(level, thrower, x, y, z, shell, filling);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getShell(stack).translationKey()).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable(getFilling(stack).translationKey()).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable(getFuze(stack).translationKey()).withStyle(ChatFormatting.YELLOW));
        Extra extra = getExtra(stack);
        if (extra != null) {
            tooltip.add(Component.translatable(extra.translationKey()).withStyle(ChatFormatting.RED));
        }
    }

    public static void addCreativeStacks(CreativeModeTab.Output output) {
        for (Shell shell : Shell.values()) {
            for (Filling filling : Filling.values()) {
                if (!filling.compatible(shell)) {
                    continue;
                }
                for (Fuze fuze : Fuze.values()) {
                    output.accept(make(shell, filling, fuze));
                    for (Extra extra : Extra.values()) {
                        output.accept(make(shell, filling, fuze, extra));
                    }
                }
            }
        }
    }

    public static ItemStack make(Shell shell, Filling filling, Fuze fuze) {
        return make(shell, filling, fuze, null, 1);
    }

    public static ItemStack make(Shell shell, Filling filling, Fuze fuze, @Nullable Extra extra) {
        return make(shell, filling, fuze, extra, 1);
    }

    public static ItemStack make(Shell shell, Filling filling, Fuze fuze, @Nullable Extra extra, int count) {
        ItemStack stack = new ItemStack(ModItems.GRENADE_UNIVERSAL.get(), count);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(KEY_SHELL, shell.ordinal());
        tag.putInt(KEY_FILLING, filling.ordinal());
        tag.putInt(KEY_FUZE, fuze.ordinal());
        if (extra != null) {
            tag.putInt(KEY_EXTRA, extra.ordinal());
        }
        return stack;
    }

    public static Shell getShell(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? Shell.FRAG : byOrdinal(Shell.values(), tag.getInt(KEY_SHELL), Shell.FRAG);
    }

    public static Filling getFilling(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? Filling.HE : byOrdinal(Filling.values(), tag.getInt(KEY_FILLING), Filling.HE);
    }

    public static Fuze getFuze(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? Fuze.S3 : byOrdinal(Fuze.values(), tag.getInt(KEY_FUZE), Fuze.S3);
    }

    @Nullable
    public static Extra getExtra(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(KEY_EXTRA)) {
            return null;
        }
        return byOrdinal(Extra.values(), tag.getInt(KEY_EXTRA), null);
    }

    public static String subtype(ItemStack stack) {
        return "shell=" + getShell(stack).name()
                + ";filling=" + getFilling(stack).name()
                + ";fuze=" + getFuze(stack).name()
                + ";extra=" + (getExtra(stack) == null ? "none" : getExtra(stack).name());
    }

    @Nullable
    public static Shell shellFromItem(Item item) {
        if (item == ModItems.GRENADE_SHELL_FRAG.get()) return Shell.FRAG;
        if (item == ModItems.GRENADE_SHELL_STICK.get()) return Shell.STICK;
        if (item == ModItems.GRENADE_SHELL_TECH.get()) return Shell.TECH;
        if (item == ModItems.GRENADE_SHELL_NUKE.get()) return Shell.NUKE;
        return null;
    }

    @Nullable
    public static Filling fillingFromItem(Item item) {
        if (item == ModItems.GRENADE_FILLING_POWDER.get()) return Filling.POWDER;
        if (item == ModItems.GRENADE_FILLING_HE.get()) return Filling.HE;
        if (item == ModItems.GRENADE_FILLING_DEMO.get()) return Filling.DEMO;
        if (item == ModItems.GRENADE_FILLING_INC.get()) return Filling.INC;
        if (item == ModItems.GRENADE_FILLING_WP.get()) return Filling.WP;
        if (item == ModItems.GRENADE_FILLING_CLUSTER.get()) return Filling.CLUSTER;
        if (item == ModItems.GRENADE_FILLING_EMP.get()) return Filling.EMP;
        if (item == ModItems.GRENADE_FILLING_PLASMA.get()) return Filling.PLASMA;
        if (item == ModItems.GRENADE_FILLING_LASER.get()) return Filling.LASER;
        if (item == ModItems.GRENADE_FILLING_CLUSTER_HEAVY.get()) return Filling.CLUSTER_HEAVY;
        if (item == ModItems.GRENADE_FILLING_NUCLEAR.get()) return Filling.NUCLEAR;
        if (item == ModItems.GRENADE_FILLING_NUCLEAR_DEMO.get()) return Filling.NUCLEAR_DEMO;
        if (item == ModItems.GRENADE_FILLING_SCHRAB.get()) return Filling.SCHRAB;
        return null;
    }

    @Nullable
    public static Fuze fuzeFromItem(Item item) {
        if (item == ModItems.GRENADE_FUZE_S3.get()) return Fuze.S3;
        if (item == ModItems.GRENADE_FUZE_S7.get()) return Fuze.S7;
        if (item == ModItems.GRENADE_FUZE_S15.get()) return Fuze.S15;
        if (item == ModItems.GRENADE_FUZE_IMPACT.get()) return Fuze.IMPACT;
        if (item == ModItems.GRENADE_FUZE_AIRBURST.get()) return Fuze.AIRBURST;
        return null;
    }

    @Nullable
    public static Extra extraFromItem(Item item) {
        if (item == ModItems.GRENADE_EXTRA_GLUE.get()) return Extra.GLUE;
        if (item == ModItems.GRENADE_EXTRA_PROXY_FUZE.get()) return Extra.PROXY_FUZE;
        if (item == ModItems.GRENADE_EXTRA_FRAG_SLEEVE.get()) return Extra.FRAG_SLEEVE;
        if (item == ModItems.GRENADE_EXTRA_TRIPLEX.get()) return Extra.TRIPLEX;
        return null;
    }

    private static boolean airburstHit(Entity grenade) {
        Vec3 start = grenade.position();
        Vec3 end = start.add(0.0D, -10.0D, 0.0D);
        HitResult hit = grenade.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, grenade));
        return hit.getType() == HitResult.Type.BLOCK;
    }

    private static boolean proxyTriggered(Entity grenade) {
        Entity owner = grenade instanceof DynamiteStickEntity dynamite ? dynamite.getOwner() : null;
        AABB area = grenade.getBoundingBox().inflate(10.0D);
        for (LivingEntity living : grenade.level().getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            if (living == owner) {
                continue;
            }
            if (living.distanceToSqr(grenade) <= 100.0D) {
                return true;
            }
        }
        return false;
    }

    private static void explodeEntity(Entity grenade) {
        if (grenade instanceof DynamiteStickEntity dynamite) {
            dynamite.explode();
        } else {
            grenade.discard();
        }
    }

    private static void standardExplode(Level level, LivingEntity thrower, double x, double y, double z,
            float range, float damage, float pierceDamageThreshold, float pierceDamageResistance) {
        WeaponExplosionUtil.smooth(level, x, y, z, range, thrower, damage, 1.0D, false,
                pierceDamageThreshold, pierceDamageResistance).explode();
    }

    private static void demoExplode(Level level, LivingEntity thrower, double x, double y, double z) {
        WeaponExplosionUtil.smooth(level, x, y, z, 5.0F, thrower, 10.0F, 1.0D, true).explode();
    }

    private static void incendiaryExplode(Level level, LivingEntity thrower, double x, double y, double z,
            int fireRadius, int duration, int fireType) {
        standardExplode(level, thrower, x, y, z, 3.0F, 10.0F, 0.0F, 0.0F);
        level.addFreshEntity(FireLingeringEntity.create(level, x, y, z, fireType, 6.0F, 2.0F, duration));
        placeFireAround(level, x, y, z, fireRadius);
    }

    private static void whitePhosphorusExplode(Level level, LivingEntity thrower, double x, double y, double z) {
        incendiaryExplode(level, thrower, x, y, z, 3, 600, FireLingeringEntity.TYPE_PHOSPHORUS);
        for (int i = 0; i < 3; i++) {
            CompoundTag haze = new CompoundTag();
            haze.putString("type", ParticleUtil.TYPE_HAZE);
            ParticleUtil.spawnAuxThreaded(level, x + level.random.nextGaussian() * 4.0D, y,
                    z + level.random.nextGaussian() * 4.0D, haze, 150.0D);
        }
    }

    private static void placeFireAround(Level level, double x, double y, double z, int radius) {
        BlockPos origin = BlockPos.containing(x, y, z);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (!level.getBlockState(pos).isAir()) {
                        continue;
                    }
                    for (Direction direction : Direction.values()) {
                        BlockPos neighborPos = pos.relative(direction);
                        BlockState neighbor = level.getBlockState(neighborPos);
                        if (neighbor.isFlammable(level, neighborPos, direction.getOpposite())) {
                            level.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void nuclearExplode(Level level, LivingEntity thrower, double x, double y, double z,
            boolean blockDamage) {
        ExplosionVnt explosion = new ExplosionVnt(level, x, y, z, 10.0F, thrower, false,
                blockDamage ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.KEEP);
        if (blockDamage) {
            explosion.setBlockAllocator(new BlockAllocatorStandard(64))
                    .setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorFire()));
        }
        explosion.setEntityProcessor(new EntityProcessorCrossSmooth(2.0D, blockDamage ? 50.0F : 100.0F)
                        .withRangeMod(1.5F))
                .setPlayerProcessor(new PlayerProcessorStandard());
        if (!blockDamage) {
            explosion.setEffects(new ExplosionEffectWeapon(10, 2.5F, 1.0F));
        }
        explosion.explode();

        incrementRad(level, x, y, z, blockDamage ? 1.5F : 1.0F);
        LegacySoundPlayer.playLegacyMukeExplosion(level, x, y, z);
        ParticleUtil.spawnMuke(level, x, y + 0.5D, z, level.random.nextInt(100) == 0);
    }

    private static void incrementRad(Level level, double x, double y, double z, float multiplier) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int distance = Math.abs(dx) + Math.abs(dz);
                if (distance < 4) {
                    ChunkRadiationManager.proxy.incrementRad(level,
                            BlockPos.containing(Math.floor(x + dx * 16.0D), Math.floor(y),
                                    Math.floor(z + dz * 16.0D)),
                            50.0F / (distance + 1.0F) * multiplier);
                }
            }
        }
    }

    private static void spawnTriplex(Level level, LivingEntity thrower, double x, double y, double z,
            Shell shell, Filling filling) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < 3; i++) {
            DynamiteStickEntity triplet = thrower == null
                    ? new DynamiteStickEntity(ModEntityTypes.DYNAMITE_STICK.get(), level)
                    : new DynamiteStickEntity(level, thrower);
            if (thrower != null) {
                triplet.setOwner(thrower);
            }
            triplet.setItem(make(shell, filling, Fuze.S3));
            triplet.setPos(x, y, z);
            triplet.setDeltaMovement(Math.cos(angle) * 0.25D, 0.75D, Math.sin(angle) * 0.25D);
            level.addFreshEntity(triplet);
            angle += Math.PI * 2.0D / 3.0D;
        }
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    @Nullable
    private static <E extends Enum<E>> E byOrdinal(E[] values, int ordinal, @Nullable E fallback) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    public enum Shell {
        FRAG("frag", 4, 30, 0.5D, 1.0D),
        STICK("stick", 4, 43, 0.25D, 1.5D),
        TECH("tech", 2, 30, 0.5D, 1.0D),
        NUKE("nuke", 1, 43, 0.25D, 1.5D);

        private final String suffix;
        private final int stackLimit;
        private final int drawDuration;
        private final double bounce;
        private final double yeetForce;

        Shell(String suffix, int stackLimit, int drawDuration, double bounce, double yeetForce) {
            this.suffix = suffix;
            this.stackLimit = stackLimit;
            this.drawDuration = drawDuration;
            this.bounce = bounce;
            this.yeetForce = yeetForce;
        }

        public String suffix() {
            return suffix;
        }

        public int stackLimit() {
            return stackLimit;
        }

        public int drawDuration() {
            return drawDuration;
        }

        public double bounce() {
            return bounce;
        }

        public double yeetForce() {
            return yeetForce;
        }

        public String itemId() {
            return "grenade_shell_" + suffix;
        }

        public String translationKey() {
            return "item.hbm_ntm_rebirth." + itemId();
        }
    }

    public enum Filling {
        POWDER("powder", Shell.FRAG, Shell.STICK),
        HE("he", Shell.FRAG, Shell.STICK),
        DEMO("demo", Shell.FRAG, Shell.STICK),
        INC("inc", Shell.FRAG, Shell.STICK),
        WP("wp", Shell.FRAG, Shell.STICK),
        CLUSTER("cluster", Shell.FRAG, Shell.STICK),
        EMP("emp", Shell.TECH),
        PLASMA("plasma", Shell.TECH),
        LASER("laser", Shell.TECH),
        CLUSTER_HEAVY("cluster_heavy", Shell.NUKE),
        NUCLEAR("nuclear", Shell.NUKE),
        NUCLEAR_DEMO("nuclear_demo", Shell.NUKE),
        SCHRAB("schrab", Shell.NUKE);

        private final String suffix;
        private final List<Shell> compatibleShells;

        Filling(String suffix, Shell... compatibleShells) {
            this.suffix = suffix;
            this.compatibleShells = List.of(compatibleShells);
        }

        public String suffix() {
            return suffix;
        }

        public boolean compatible(Shell shell) {
            return compatibleShells.contains(shell);
        }

        public String itemId() {
            return "grenade_filling_" + suffix;
        }

        public String textureSuffix() {
            return suffix;
        }

        public String translationKey() {
            return "item.hbm_ntm_rebirth." + itemId();
        }
    }

    public enum Fuze {
        S3("s3"),
        S7("s7"),
        S15("s15"),
        IMPACT("impact"),
        AIRBURST("airburst");

        private final String suffix;

        Fuze(String suffix) {
            this.suffix = suffix;
        }

        public String suffix() {
            return suffix;
        }

        public String itemId() {
            return "grenade_fuze_" + suffix;
        }

        public String translationKey() {
            return "item.hbm_ntm_rebirth." + itemId();
        }
    }

    public enum Extra {
        GLUE("glue"),
        PROXY_FUZE("proxy_fuze"),
        FRAG_SLEEVE("frag_sleeve"),
        TRIPLEX("triplex");

        private final String suffix;

        Extra(String suffix) {
            this.suffix = suffix;
        }

        public String suffix() {
            return suffix;
        }

        public String itemId() {
            return "grenade_extra_" + suffix;
        }

        public String textureSuffix() {
            return suffix;
        }

        public String translationKey() {
            return "item.hbm_ntm_rebirth." + itemId();
        }
    }
}
