package com.hbm.ntm.fluid;

import com.hbm.ntm.compat.CompatExternal;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.VentRadiationFluidTrait;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

/**
 * Source-backed block impact side effects for old EntityChemical fluid splashes.
 */
public final class HbmFluidBlockImpactEffects {
    public static final int CHEMICAL_PROJECTILE_RELEASE_MB = 5;

    public static ChemicalImpactReport applyChemicalProjectileImpact(Level level, BlockPos pos, FluidType fluid,
            ChemicalImpactStyle style) {
        FluidType type = fluid == null ? HbmFluids.NONE : fluid;
        ChemicalImpactStyle impactStyle = style == null ? ChemicalImpactStyle.NULL : style;
        float radiation = applyBlockImpactRadiation(level, pos, type, CHEMICAL_PROJECTILE_RELEASE_MB);
        int firePlaced = impactStyle.placesFire() ? placeAdjacentFire(level, pos, type == HbmFluids.BALEFIRE) : 0;
        int fireExtinguished = isExtinguishing(type, impactStyle) ? extinguishAdjacentFire(level, pos) : 0;
        HbmExtinguishType extinguishType = extinguishingType(type);
        boolean repaired = tryExtinguishRepairable(level, pos, extinguishType);
        int falloutCleared = extinguishType == HbmExtinguishType.WATER && impactStyle == ChemicalImpactStyle.LIQUID
                ? clearFalloutAround(level, pos)
                : 0;
        int seedMutations = type == HbmFluids.SEEDSLURRY ? applySeedSlurry(level, pos) : 0;
        return new ChemicalImpactReport(radiation, firePlaced, fireExtinguished, repaired, falloutCleared, seedMutations);
    }

    public static boolean isExtinguishing(FluidType type, ChemicalImpactStyle style) {
        FluidType fluid = type == null ? HbmFluids.NONE : type;
        return style == ChemicalImpactStyle.LIQUID
                && fluid.getTemperature() < 50
                && !fluid.hasTrait(FlammableFluidTrait.class);
    }

    @Nullable
    public static HbmExtinguishType extinguishingType(FluidType type) {
        if (type == HbmFluids.CARBONDIOXIDE) {
            return HbmExtinguishType.CO2;
        }
        if (type == HbmFluids.WATER || type == HbmFluids.HEAVYWATER || type == HbmFluids.COOLANT) {
            return HbmExtinguishType.WATER;
        }
        return null;
    }

    private static float applyBlockImpactRadiation(Level level, BlockPos pos, FluidType type, int amountMb) {
        VentRadiationFluidTrait trait = type.getTrait(VentRadiationFluidTrait.class);
        if (trait == null || amountMb <= 0) {
            return 0.0F;
        }
        float radiation = trait.getRadiationPerMb() * amountMb;
        if (radiation > 0.0F) {
            ChunkRadiationManager.incrementRadiation(level, pos, radiation);
        }
        return radiation;
    }

    private static int placeAdjacentFire(Level level, BlockPos pos, boolean balefire) {
        int placed = 0;
        for (Direction direction : Direction.values()) {
            BlockPos firePos = pos.relative(direction);
            if (!level.getBlockState(firePos).isAir()) {
                continue;
            }
            BlockState fire = balefire
                    ? ModBlocks.BALEFIRE.get().defaultBlockState()
                    : BaseFireBlock.getState(level, firePos);
            if (level.setBlock(firePos, fire, 3)) {
                placed++;
            }
        }
        return placed;
    }

    private static int extinguishAdjacentFire(Level level, BlockPos pos) {
        int removed = 0;
        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            if (level.getBlockState(target).is(Blocks.FIRE) && level.removeBlock(target, false)) {
                removed++;
            }
        }
        return removed;
    }

    private static boolean tryExtinguishRepairable(Level level, BlockPos pos, @Nullable HbmExtinguishType type) {
        if (type == null) {
            return false;
        }
        BlockEntity core = CompatExternal.getCoreFromPos(level, pos);
        if (!(core instanceof HbmFluidRepairable repairable)) {
            return false;
        }
        repairable.tryExtinguish(type);
        return true;
    }

    private static int clearFalloutAround(Level level, BlockPos pos) {
        int removed = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos target = pos.offset(x, y, z);
                    if (level.getBlockState(target).is(ModBlocks.FALLOUT.get()) && level.removeBlock(target, false)) {
                        removed++;
                    }
                }
            }
        }
        return removed;
    }

    private static int applySeedSlurry(Level level, BlockPos pos) {
        int mutations = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    mutations += applySeedSlurryAt(level, pos.offset(x, y, z)) ? 1 : 0;
                }
            }
        }
        return mutations;
    }

    private static boolean applySeedSlurryAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (isDirtRecoverable(block) && hasGrassLight(level, pos.above())) {
            return setBlock(level, pos, Blocks.GRASS_BLOCK.defaultBlockState());
        }
        if (state.is(Blocks.COBBLESTONE)) {
            return setBlock(level, pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        }
        if (state.is(Blocks.STONE_BRICKS)) {
            return setBlock(level, pos, Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
        }
        if (isLegacyBlock(block, "waste_earth")) {
            return setBlock(level, pos, Blocks.GRASS_BLOCK.defaultBlockState());
        }
        if (isLegacyBlock(block, "brick_concrete")) {
            BlockState mossy = legacyDefaultState("brick_concrete_mossy");
            return mossy != null && setBlock(level, pos, mossy);
        }
        BlockState slab = legacyConcreteSlabMossyState(state);
        if (slab != null) {
            return setBlock(level, pos, slab);
        }
        BlockState stairs = legacyConcreteStairsMossyState(state);
        return stairs != null && setBlock(level, pos, stairs);
    }

    private static boolean isDirtRecoverable(Block block) {
        return block == Blocks.DIRT
                || isLegacyBlock(block, "waste_earth")
                || isLegacyBlock(block, "dirt_dead")
                || isLegacyBlock(block, "dirt_oily");
    }

    private static boolean hasGrassLight(Level level, BlockPos above) {
        return level.getRawBrightness(above, 0) >= 9
                && level.getBlockState(above).getLightBlock(level, above) <= 2;
    }

    @Nullable
    private static BlockState legacyConcreteSlabMossyState(BlockState state) {
        if (!isLegacyBlock(state.getBlock(), "concrete_brick_slab")) {
            return null;
        }
        BlockState mossy = legacyDefaultState("concrete_brick_slab_mossy");
        return mossy == null ? null : mossy;
    }

    @Nullable
    private static BlockState legacyConcreteStairsMossyState(BlockState state) {
        if (!isLegacyBlock(state.getBlock(), "brick_concrete_stairs")) {
            return null;
        }
        BlockState mossy = legacyDefaultState("brick_concrete_mossy_stairs");
        return mossy == null ? null : mossy;
    }

    @Nullable
    private static BlockState legacyDefaultState(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        return block == null ? null : block.get().defaultBlockState();
    }

    private static boolean isLegacyBlock(Block block, String legacyName) {
        RegistryObject<? extends Block> legacy = ModBlocks.legacyBlock(legacyName);
        return legacy != null && block == legacy.get();
    }

    private static boolean setBlock(Level level, BlockPos pos, BlockState state) {
        return !level.getBlockState(pos).equals(state) && level.setBlock(pos, state, 3);
    }

    public enum ChemicalImpactStyle {
        LIQUID,
        GAS,
        GASFLAME,
        BURNING,
        AMAT,
        LIGHTNING,
        NULL;

        private boolean placesFire() {
            return this == BURNING || this == GASFLAME;
        }
    }

    public record ChemicalImpactReport(float radiation, int firePlaced, int fireExtinguished,
                                       boolean repairExtinguished, int falloutCleared, int seedSlurryMutations) {
        public boolean changedWorld() {
            return radiation > 0.0F
                    || firePlaced > 0
                    || fireExtinguished > 0
                    || repairExtinguished
                    || falloutCleared > 0
                    || seedSlurryMutations > 0;
        }
    }

    private HbmFluidBlockImpactEffects() {
    }
}
