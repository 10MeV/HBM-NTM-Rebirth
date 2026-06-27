package com.hbm.ntm.block;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlocks;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

@SuppressWarnings("deprecation")
public class LegacyVolcanicLavaBlock extends LiquidBlock {
    private final boolean radioactive;

    public LegacyVolcanicLavaBlock(Supplier<? extends FlowingFluid> fluid, Properties properties,
            boolean radioactive) {
        super(fluid, properties);
        this.radioactive = radioactive;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        EntityDamageUtil.attackEntityFromNt(entity, level.damageSources().hotFloor(), 4.0F);
        if (radioactive && entity instanceof LivingEntity living) {
            RadiationUtil.contaminate(living, 5.0F, true);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        reactToNeighbors(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        reactToNeighbors(level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        if (!level.isClientSide()) {
            BlockState reaction = reactionFor(level, neighborPos, neighborState);
            if (reaction != null) {
                level.setBlock(neighborPos, reaction, Block.UPDATE_ALL);
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        reactToNeighbors(level, pos);
        trySolidify(state, level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        BlockState above = level.getBlockState(pos.above());
        if (above.isAir() && !above.isSolidRender(level, pos.above())) {
            if (random.nextInt(100) == 0) {
                double x = pos.getX() + random.nextFloat();
                double y = pos.getY() + 1.0D;
                double z = pos.getZ() + random.nextFloat();
                level.addParticle(ParticleTypes.LAVA, x, y, z, 0.0D, 0.0D, 0.0D);
                level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                        0.2F + random.nextFloat() * 0.2F,
                        0.9F + random.nextFloat() * 0.15F, false);
            }
            if (random.nextInt(200) == 0) {
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_AMBIENT,
                        SoundSource.BLOCKS, 0.2F + random.nextFloat() * 0.2F,
                        0.9F + random.nextFloat() * 0.15F, false);
            }
        }
        if (random.nextInt(10) == 0
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                && !level.getBlockState(pos.below(2)).blocksMotion()) {
            level.addParticle(ParticleTypes.DRIPPING_LAVA,
                    pos.getX() + random.nextFloat(),
                    pos.getY() - 1.05D,
                    pos.getZ() + random.nextFloat(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    private void reactToNeighbors(LevelAccessor level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (level.isOutsideBuildHeight(neighborPos)) {
                continue;
            }
            BlockState reaction = reactionFor(level, neighborPos, level.getBlockState(neighborPos));
            if (reaction != null) {
                level.setBlock(neighborPos, reaction, Block.UPDATE_ALL);
            }
        }
    }

    private BlockState reactionFor(LevelAccessor level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.WATER)) {
            return Blocks.STONE.defaultBlockState();
        }
        if (state.is(BlockTags.LOGS)) {
            return ModBlocks.WASTE_LOG.get().defaultBlockState();
        }
        if (state.is(BlockTags.PLANKS)) {
            return ModBlocks.WASTE_PLANKS.get().defaultBlockState();
        }
        if (state.is(BlockTags.LEAVES)) {
            return Blocks.FIRE.defaultBlockState();
        }
        if (state.is(Blocks.DIAMOND_ORE)) {
            return radioactive
                    ? ModBlocks.ORE_SELLAFIELD_RADGEM.get().defaultBlockState()
                    : basaltOreState(3);
        }
        if (radioactive && isLegacyUraniumOre(state)) {
            RandomSource random = level instanceof Level realLevel ? realLevel.random : RandomSource.create();
            return random.nextInt(5) == 0
                    ? ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM.get().defaultBlockState()
                    : ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED.get().defaultBlockState();
        }
        return null;
    }

    private void trySolidify(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int lavaCount = 0;
        int basaltCount = 0;
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.getBlock() == this) {
                lavaCount++;
            }
            if (isBasaltForCheck(neighbor)) {
                basaltCount++;
            }
        }
        boolean unstableFlow = !state.getFluidState().isSource() && lavaCount < 2;
        boolean randomCooling = random.nextInt(5) == 0 && lavaCount < 5;
        if ((unstableFlow || randomCooling) && level.getBlockState(pos.below()).getBlock() != this) {
            solidify(level, pos, lavaCount, basaltCount, random);
        }
    }

    private void solidify(ServerLevel level, BlockPos pos, int lavaCount, int basaltCount, RandomSource random) {
        if (radioactive) {
            int r = random.nextInt(400);
            boolean canMakeGem = lavaCount + basaltCount == 6 && lavaCount < 3
                    && isRadioactiveGemCap(level, pos.above(10));
            int oreLevel = 5 + random.nextInt(3);
            if (r < 2) {
                level.setBlock(pos, sellafieldOreState(ModBlocks.ORE_SELLAFIELD_DIAMOND.get(), oreLevel),
                        Block.UPDATE_ALL);
            } else if (r == 2) {
                level.setBlock(pos, sellafieldOreState(ModBlocks.ORE_SELLAFIELD_EMERALD.get(), oreLevel),
                        Block.UPDATE_ALL);
            } else if (r < 20 && canMakeGem) {
                level.setBlock(pos, sellafieldOreState(ModBlocks.ORE_SELLAFIELD_RADGEM.get(), oreLevel),
                        Block.UPDATE_ALL);
            } else {
                level.setBlock(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState()
                        .setValue(LegacySellafieldSlakedBlock.LEVEL, oreLevel), Block.UPDATE_ALL);
            }
            return;
        }
        int r = random.nextInt(200);
        boolean canMakeGem = lavaCount + basaltCount == 6 && lavaCount < 3 && isVolcanicGemCap(level, pos.above(10));
        if (r < 2) {
            level.setBlock(pos, basaltOreState(0), Block.UPDATE_ALL);
        } else if (r == 2) {
            level.setBlock(pos, basaltOreState(1), Block.UPDATE_ALL);
        } else if (r == 3) {
            level.setBlock(pos, basaltOreState(2), Block.UPDATE_ALL);
        } else if (r == 4) {
            level.setBlock(pos, basaltOreState(4), Block.UPDATE_ALL);
        } else if (r < 15 && canMakeGem) {
            level.setBlock(pos, basaltOreState(3), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, ModBlocks.legacyBlock("basalt").get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private boolean isBasaltForCheck(BlockState state) {
        if (radioactive) {
            return state.is(ModBlocks.SELLAFIELD_SLAKED.get());
        }
        return state.is(ModBlocks.legacyBlock("basalt").get());
    }

    private boolean isVolcanicGemCap(LevelAccessor level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.legacyBlock("basalt").get()) || state.getBlock() == this;
    }

    private boolean isRadioactiveGemCap(LevelAccessor level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.SELLAFIELD_SLAKED.get()) || state.getBlock() == this;
    }

    private static boolean isLegacyUraniumOre(BlockState state) {
        return state.is(ModBlocks.legacyBlock("ore_uranium").get())
                || state.is(ModBlocks.legacyBlock("ore_gneiss_uranium").get());
    }

    private static BlockState basaltOreState(int legacyMeta) {
        Block block = ModBlocks.ORE_BASALT.get();
        if (block instanceof LegacyBasaltOreBlock basaltOre) {
            return basaltOre.stateForVariant(legacyMeta);
        }
        return block.defaultBlockState();
    }

    private static BlockState sellafieldOreState(Block block, int level) {
        return block.defaultBlockState().setValue(LegacySellafieldSlakedBlock.LEVEL, level);
    }
}
