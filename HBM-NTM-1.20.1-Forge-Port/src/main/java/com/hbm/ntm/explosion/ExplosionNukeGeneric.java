package com.hbm.ntm.explosion;

import com.hbm.ntm.config.BombConfig;
import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.RegistryObject;

public final class ExplosionNukeGeneric {
    public static void empBlast(Level level, int x, int y, int z, int bombStartStrength) {
        if (level == null || level.isClientSide() || bombStartStrength <= 0) {
            return;
        }

        applySphere(level, x, y, z, bombStartStrength, false, ExplosionNukeGeneric::emp);
    }

    public static void dealDamage(Level level, double x, double y, double z, double radius) {
        dealDamage(level, x, y, z, radius, 250.0F);
    }

    public static void dealDamage(Level level, double x, double y, double z, double radius, float maxDamage) {
        if (level == null || level.isClientSide() || radius <= 0.0D || maxDamage <= 0.0F) {
            return;
        }

        Vec3 origin = new Vec3(x, y, z);
        AABB bounds = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        for (Entity entity : level.getEntities(null, bounds)) {
            double distance = entity.distanceToSqr(x, y, z);
            if (distance > radius * radius || isExplosionExempt(entity)) {
                continue;
            }

            Vec3 target = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
            if (isObstructed(level, origin, target)) {
                continue;
            }

            double linearDistance = Math.sqrt(distance);
            float damage = (float) (maxDamage * (radius - linearDistance) / radius);
            entity.hurt(ModDamageSources.explosion(level, null), damage);
            entity.setSecondsOnFire(5);

            Vec3 knockback = target.subtract(origin);
            if (knockback.lengthSqr() > 1.0E-7D) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback.normalize().scale(0.2D)));
                entity.hurtMarked = true;
            }
        }
    }

    public static int destruction(Level level, int x, int y, int z) {
        return destruction(level, new BlockPos(x, y, z));
    }

    public static int destruction(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return 0;
        }

        BlockState state = level.getBlockState(pos);
        if (isLegacyEmpty(state)) {
            return 0;
        }

        float resistance = explosionResistance(state);
        if (resistance >= 200.0F) {
            int protection = (int) (resistance / 300.0F);
            if (isLegacy(state, "brick_concrete")) {
                if (level.random.nextInt(8) == 0) {
                    level.setBlock(pos, Blocks.GRAVEL.defaultBlockState(), 3);
                    return 0;
                }
            } else if (isLegacy(state, "brick_light")) {
                int random = level.random.nextInt(3);
                if (random == 0) {
                    setLegacy(level, pos, "waste_planks", Blocks.DARK_OAK_PLANKS.defaultBlockState());
                    return 0;
                } else if (random == 1) {
                    setLegacy(level, pos, "block_scrap", Blocks.IRON_BLOCK.defaultBlockState());
                    return 0;
                }
            } else if (isLegacy(state, "brick_obsidian")) {
                if (level.random.nextInt(20) == 0) {
                    level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            } else if (state.is(Blocks.OBSIDIAN)) {
                setLegacy(level, pos, "gravel_obsidian", Blocks.CRYING_OBSIDIAN.defaultBlockState());
                return 0;
            } else if (level.random.nextInt(protection + 3) == 0) {
                setLegacy(level, pos, "block_scrap", Blocks.IRON_BLOCK.defaultBlockState());
            }
            return protection;
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        return 0;
    }

    public static void vapor(Level level, int x, int y, int z, int bombStartStrength) {
        if (level == null || level.isClientSide() || bombStartStrength <= 0) {
            return;
        }

        applySphere(level, x, y, z, bombStartStrength * 2, false, (world, pos) -> vaporDest(world, pos));
    }

    public static int vaporDest(Level level, int x, int y, int z) {
        return vaporDest(level, new BlockPos(x, y, z));
    }

    public static int vaporDest(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return 0;
        }

        BlockState state = level.getBlockState(pos);
        if (isLegacyEmpty(state)) {
            return 0;
        }

        float resistance = explosionResistance(state);
        if (resistance < 0.5F || state.is(Blocks.COBWEB) || state.is(ModBlocks.RED_CABLE.get()) || !state.getFluidState().isEmpty()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            return 0;
        }
        if (resistance <= 3.0F && !state.isCollisionShapeFullBlock(level, pos)
                && !state.is(Blocks.CHEST) && !state.is(Blocks.TRAPPED_CHEST) && !state.is(Blocks.FARMLAND)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            return 0;
        }

        BlockPos above = pos.above();
        if (state.isFlammable(level, pos, Direction.UP) && level.getBlockState(above).isAir()) {
            level.setBlock(above, BaseFireBlock.getState(level, above), 2);
        }
        return (int) (resistance / 300.0F);
    }

    @SuppressWarnings("deprecation")
    private static float explosionResistance(BlockState state) {
        return state.getBlock().getExplosionResistance();
    }

    private static boolean isLegacyEmpty(BlockState state) {
        return state.isAir() && state.getFluidState().isEmpty();
    }

    public static void waste(Level level, int x, int y, int z, int radius) {
        if (level == null || level.isClientSide() || radius <= 0) {
            return;
        }

        applySphere(level, x, y, z, radius, true, ExplosionNukeGeneric::wasteDest);
    }

    public static void wasteDest(Level level, int x, int y, int z) {
        wasteDest(level, new BlockPos(x, y, z));
    }

    public static void wasteDest(Level level, BlockPos pos) {
        wasteDest(level, pos, true);
    }

    public static void wasteNoSchrab(Level level, int x, int y, int z, int radius) {
        if (level == null || level.isClientSide() || radius <= 0) {
            return;
        }

        applySphere(level, x, y, z, radius, true, ExplosionNukeGeneric::wasteDestNoSchrab);
    }

    public static void wasteDestNoSchrab(Level level, int x, int y, int z) {
        wasteDestNoSchrab(level, new BlockPos(x, y, z));
    }

    public static void wasteDestNoSchrab(Level level, BlockPos pos) {
        wasteDest(level, pos, false);
    }

    public static void emp(Level level, int x, int y, int z) {
        emp(level, new BlockPos(x, y, z));
    }

    public static void emp(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }

        boolean discharged = false;
        boolean forgeEnergy = false;
        if (blockEntity instanceof HbmEnergyHandler energyHandler && energyHandler.getPower() > 0L) {
            energyHandler.setPower(0L);
            discharged = true;
        }
        for (Direction direction : Direction.values()) {
            IEnergyStorage energy = blockEntity.getCapability(ForgeCapabilities.ENERGY, direction).orElse(null);
            if (energy != null && energy.canExtract() && energy.getEnergyStored() > 0) {
                energy.extractEnergy(energy.getEnergyStored(), false);
                discharged = true;
                forgeEnergy = true;
            }
        }
        IEnergyStorage energy = blockEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
        if (energy != null && energy.canExtract() && energy.getEnergyStored() > 0) {
            energy.extractEnergy(energy.getEnergyStored(), false);
            discharged = true;
            forgeEnergy = true;
        }

        if (discharged && level.random.nextInt(5) < (forgeEnergy ? 2 : 1)) {
            setLegacy(level, pos, "block_electrical_scrap", Blocks.IRON_BLOCK.defaultBlockState());
        } else if (discharged) {
            blockEntity.setChanged();
        }
    }

    public static void solinium(Level level, int x, int y, int z) {
        solinium(level, new BlockPos(x, y, z));
    }

    public static void solinium(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM)
                || state.is(ModBlocks.WASTE_EARTH.get()) || state.is(ModBlocks.WASTE_MYCELIUM.get())) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            return;
        }
        if (isSoliniumCleared(state)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static void wasteDest(Level level, BlockPos pos, boolean allowSchrabidium) {
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }

        if (state.is(BlockTags.DOORS)
                || (!allowSchrabidium && (state.is(BlockTags.LEAVES) || isLegacyGlass(state)))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        } else if (state.is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos, ModBlocks.WASTE_EARTH.get().defaultBlockState(), 3);
        } else if (state.is(Blocks.MYCELIUM)) {
            level.setBlock(pos, ModBlocks.WASTE_MYCELIUM.get().defaultBlockState(), 3);
        } else if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND)) {
            if (level.random.nextInt(20) == 1) {
                setLegacy(level, pos, state.is(Blocks.RED_SAND) ? "waste_trinitite_red" : "waste_trinitite",
                        legacyState("block_trinitite", Blocks.GLASS.defaultBlockState()));
            }
        } else if (state.is(Blocks.CLAY)) {
            level.setBlock(pos, Blocks.TERRACOTTA.defaultBlockState(), 3);
        } else if (state.is(Blocks.MOSSY_COBBLESTONE)) {
            level.setBlock(pos, Blocks.COAL_ORE.defaultBlockState(), 3);
        } else if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
            int bound = allowSchrabidium ? 10 : 30;
            int random = level.random.nextInt(bound);
            if (random >= 1 && random <= 3) {
                level.setBlock(pos, Blocks.DIAMOND_ORE.defaultBlockState(), 3);
            } else if (random == bound - 1) {
                level.setBlock(pos, Blocks.EMERALD_ORE.defaultBlockState(), 3);
            }
        } else if (state.is(BlockTags.LOGS)) {
            setLegacy(level, pos, "waste_log", ModBlocks.WASTE_LEAVES.get().defaultBlockState());
        } else if (state.is(BlockTags.PLANKS)) {
            setLegacy(level, pos, "waste_planks", Blocks.DARK_OAK_PLANKS.defaultBlockState());
        } else if (state.is(Blocks.MUSHROOM_STEM)) {
            setLegacy(level, pos, "waste_log", Blocks.AIR.defaultBlockState());
        } else if (state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        } else if (allowSchrabidium && isLegacy(state, "ore_uranium")) {
            setLegacy(level, pos, level.random.nextInt(schrabOreChance()) == 1 ? "ore_schrabidium" : "ore_uranium_scorched");
        } else if (allowSchrabidium && isLegacy(state, "ore_nether_uranium")) {
            setLegacy(level, pos, level.random.nextInt(schrabOreChance()) == 1 ? "ore_nether_schrabidium" : "ore_nether_uranium_scorched");
        } else if (allowSchrabidium && isLegacy(state, "ore_gneiss_uranium")) {
            setLegacy(level, pos, level.random.nextInt(schrabOreChance()) == 1 ? "ore_gneiss_schrabidium" : "ore_gneiss_uranium_scorched");
        }
    }

    private static int schrabOreChance() {
        if (RadiationConfig.ENABLE_LESS_BULLSHIT_MODE != null
                && RadiationConfig.ENABLE_LESS_BULLSHIT_MODE.get()
                && BombConfig.LBSM_SCHRAB_ORE_RATE != null) {
            return Math.max(1, BombConfig.LBSM_SCHRAB_ORE_RATE.get());
        }
        return 100;
    }

    private static boolean isObstructed(Level level, Vec3 origin, Vec3 target) {
        ClipContext context = new ClipContext(origin, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        return level.clip(context).getType() != HitResult.Type.MISS;
    }

    private static boolean isExplosionExempt(Entity entity) {
        if (entity instanceof Ocelot) {
            return true;
        }
        if (entity instanceof Player player) {
            return player.isCreative() || player.isSpectator();
        }
        return !(entity instanceof LivingEntity) && !entity.isAttackable();
    }

    private static boolean isSoliniumCleared(BlockState state) {
        return state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.VINE)
                || state.is(Blocks.MELON)
                || state.is(Blocks.PUMPKIN)
                || state.is(Blocks.SPONGE)
                || state.is(Blocks.WET_SPONGE)
                || state.getBlock() instanceof BushBlock
                || state.canBeReplaced();
    }

    private static boolean isLegacyGlass(BlockState state) {
        return state.is(Blocks.GLASS)
                || state.is(Blocks.TINTED_GLASS)
                || state.getBlock() instanceof StainedGlassBlock;
    }

    private static void applySphere(Level level, int x, int y, int z, int radius, boolean jagged, BlockOperation operation) {
        int radiusSquared = radius * radius;
        int halfRadiusSquared = radiusSquared / 2;
        int jaggedBound = Math.max(1, radiusSquared / 10);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -radius; xx < radius; xx++) {
            int worldX = xx + x;
            int xx2 = xx * xx;
            for (int yy = -radius; yy < radius; yy++) {
                int worldY = yy + y;
                int yy2 = xx2 + yy * yy;
                for (int zz = -radius; zz < radius; zz++) {
                    int distanceSquared = yy2 + zz * zz;
                    int threshold = halfRadiusSquared + (jagged ? level.random.nextInt(jaggedBound) : 0);
                    if (distanceSquared < threshold) {
                        operation.apply(level, cursor.set(worldX, worldY, zz + z));
                    }
                }
            }
        }
    }

    private static boolean isLegacy(BlockState state, String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return block != null && state.is(block.get());
    }

    private static void setLegacy(Level level, BlockPos pos, String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        if (block != null) {
            level.setBlock(pos, block.get().defaultBlockState(), 3);
        }
    }

    private static void setLegacy(Level level, BlockPos pos, String name, BlockState fallback) {
        level.setBlock(pos, legacyState(name, fallback), 3);
    }

    private static BlockState legacyState(String name, BlockState fallback) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return block == null ? fallback : block.get().defaultBlockState();
    }

    @FunctionalInterface
    private interface BlockOperation {
        void apply(Level level, BlockPos pos);
    }

    private ExplosionNukeGeneric() {
    }
}
