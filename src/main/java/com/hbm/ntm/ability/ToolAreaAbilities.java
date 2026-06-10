package com.hbm.ntm.ability;

import com.hbm.ntm.config.ToolConfig;
import com.hbm.ntm.explosion.ExplosionNT;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ToolAreaAbilities {
    private static final TagKey<Block> REDSTONE_ORES = TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores/redstone"));

    public static final IToolAreaAbility NONE = new BaseAreaAbility("", 0) {
        @Override
        public boolean onDig(int level, ToolDigContext context) {
            return false;
        }
    };

    public static final IToolAreaAbility RECURSION = new BaseAreaAbility("tool.ability.recursion", 1) {
        private final int[] radiusAtLevel = { 3, 4, 5, 6, 7, 9, 10 };
        private final List<BlockPos> offsets = createOffsets();
        private final Set<BlockPos> visited = new HashSet<>();

        @Override
        public boolean isAllowed() {
            return ToolConfig.veinAbilityEnabled();
        }

        @Override
        public int levels() {
            return radiusAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + radiusAtLevel[level] + ")";
        }

        @Override
        public boolean onDig(int level, ToolDigContext context) {
            Level world = context.level();
            BlockState reference = world.getBlockState(context.pos());

            if (reference.is(Blocks.STONE) && !ToolConfig.recursiveStoneEnabled()) {
                return false;
            }
            if (reference.is(Blocks.NETHERRACK) && !ToolConfig.recursiveNetherrackEnabled()) {
                return false;
            }

            visited.clear();
            recurse(context, context.pos(), context.pos(), reference, 0, radiusAtLevel[level]);
            return false;
        }

        private void recurse(ToolDigContext context, BlockPos pos, BlockPos origin, BlockState reference, int depth, int radius) {
            List<BlockPos> shuffledOffsets = new ArrayList<>(offsets);
            Collections.shuffle(shuffledOffsets);
            for (BlockPos offset : shuffledOffsets) {
                breakExtra(context, pos.offset(offset), origin, reference, depth, radius);
            }
        }

        private void breakExtra(ToolDigContext context, BlockPos pos, BlockPos origin, BlockState reference, int depth, int radius) {
            if (visited.contains(pos)) {
                return;
            }
            int nextDepth = depth + 1;
            if (nextDepth > ToolConfig.recursionDepth()) {
                return;
            }
            visited.add(pos);
            if (pos.equals(origin) || !withinRadius(pos, origin, radius)) {
                return;
            }

            BlockState state = context.level().getBlockState(pos);
            if (!isSameBlock(state, reference)) {
                return;
            }
            context.breakExtraBlock(pos);
            recurse(context, pos, origin, reference, nextDepth, radius);
        }
    };

    public static final IToolAreaAbility HAMMER = new CubicHammerAbility("tool.ability.hammer", 2);
    public static final IToolAreaAbility HAMMER_FLAT = new FlatHammerAbility();

    public static final IToolAreaAbility EXPLOSION = new BaseAreaAbility("tool.ability.explosion", 4) {
        private final float[] strengthAtLevel = { 2.5F, 5.0F, 10.0F, 15.0F };

        @Override
        public boolean isAllowed() {
            return ToolConfig.explosionAbilityEnabled();
        }

        @Override
        public int levels() {
            return strengthAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + strengthAtLevel[level] + ")";
        }

        @Override
        public boolean allowsHarvest(int level) {
            return false;
        }

        @Override
        public boolean onDig(int level, ToolDigContext context) {
            BlockPos pos = context.pos();
            new ExplosionNT(context.level(), context.player(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, strengthAtLevel[level])
                    .addAttrib(ExplosionNT.ExAttrib.ALLDROP, ExplosionNT.ExAttrib.NOHURT, ExplosionNT.ExAttrib.NOPARTICLE)
                    .explode();
            context.level().explode(context.player(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0.1F, false, ExplosionInteraction.NONE);
            return true;
        }
    };

    public static final IToolAreaAbility[] ABILITIES = { NONE, RECURSION, HAMMER, HAMMER_FLAT, EXPLOSION };

    public static IToolAreaAbility getByName(String name) {
        for (IToolAreaAbility ability : ABILITIES) {
            if (ability.getName().equals(name)) {
                return ability;
            }
        }
        return NONE;
    }

    private static boolean withinRadius(BlockPos pos, BlockPos origin, int radius) {
        return Math.sqrt(pos.distSqr(origin)) <= radius;
    }

    private static boolean isSameBlock(BlockState state, BlockState reference) {
        if (state.equals(reference)) {
            return true;
        }
        return state.is(REDSTONE_ORES) && reference.is(REDSTONE_ORES);
    }

    private static List<BlockPos> createOffsets() {
        List<BlockPos> result = new ArrayList<>(26);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        result.add(new BlockPos(dx, dy, dz));
                    }
                }
            }
        }
        return result;
    }

    private abstract static class BaseAreaAbility implements IToolAreaAbility {
        private final String name;
        private final int sort;

        private BaseAreaAbility(String name, int sort) {
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

    private static class CubicHammerAbility extends BaseAreaAbility {
        protected final int[] rangeAtLevel = { 1, 2, 3, 4 };

        private CubicHammerAbility(String name, int sort) {
            super(name, sort);
        }

        @Override
        public boolean isAllowed() {
            return ToolConfig.hammerAbilityEnabled();
        }

        @Override
        public int levels() {
            return rangeAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + rangeAtLevel[level] + ")";
        }

        @Override
        public boolean onDig(int level, ToolDigContext context) {
            int range = rangeAtLevel[level];
            BlockPos origin = context.pos();
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            context.breakExtraBlock(origin.offset(x, y, z));
                        }
                    }
                }
            }
            return false;
        }
    }

    private static final class FlatHammerAbility extends CubicHammerAbility {
        private FlatHammerAbility() {
            super("tool.ability.hammer_flat", 3);
        }

        @Override
        public boolean onDig(int level, ToolDigContext context) {
            if (context.hitResult() == null) {
                return true;
            }
            int range = rangeAtLevel[level];
            Direction side = context.hitResult().getDirection();
            int xRange = range;
            int yRange = range;
            int zRange = 0;
            if (side.getAxis() == Direction.Axis.Y) {
                yRange = 0;
                zRange = range;
            } else if (side.getAxis() == Direction.Axis.Z) {
                xRange = range;
                zRange = 0;
            } else if (side.getAxis() == Direction.Axis.X) {
                xRange = 0;
                zRange = range;
            }

            BlockPos origin = context.pos();
            for (int x = -xRange; x <= xRange; x++) {
                for (int y = -yRange; y <= yRange; y++) {
                    for (int z = -zRange; z <= zRange; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            context.breakExtraBlock(origin.offset(x, y, z));
                        }
                    }
                }
            }
            return false;
        }
    }

    private ToolAreaAbilities() {
    }
}
