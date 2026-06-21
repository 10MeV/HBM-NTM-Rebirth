package com.hbm.util;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class CrucibleUtil {
    private CrucibleUtil() {
    }

    public static MaterialStack pourSingleStack(Level level, double x, double y, double z, double range,
            boolean safe, MaterialStack stack, int quanta, @Nullable ImpactHolder impactPosHolder) {
        return pourSingleStackDetailed(level, x, y, z, range, safe, stack, quanta, impactPosHolder).leftover();
    }

    public static PourResult pourSingleStackDetailed(Level level, double x, double y, double z, double range,
            boolean safe, MaterialStack stack, int quanta, @Nullable ImpactHolder impactPosHolder) {
        if (level == null || stack == null || stack.isEmpty()) {
            return new PourResult(stack, null, null, false);
        }
        HitTarget target = getPouringTarget(level, new Vec3(x, y, z), new Vec3(x, y - range, z));
        if (target.acceptor() != null) {
            MaterialStack ret = tryPourStack(level, target.acceptor(), target.pos(), target.hit(), target.side(), stack,
                    impactPosHolder);
            if (ret != null) {
                int moved = Math.max(0, stack.amount - ret.amount);
                return new PourResult(ret, moved > 0 ? new MaterialStack(stack.material, moved) : null,
                        target.hit(), true);
            }
        }
        MaterialStack wasted = spill(target.hit(), safe, stack, quanta, impactPosHolder);
        return new PourResult(stack, wasted, target.hit(), false);
    }

    public static MaterialStack pourFullStack(Level level, double x, double y, double z, double range,
            boolean safe, List<MaterialStack> stacks, int quanta, @Nullable ImpactHolder impactPosHolder) {
        return pourFullStackDetailed(level, x, y, z, range, safe, stacks, quanta, impactPosHolder).moved();
    }

    public static PourResult pourFullStackDetailed(Level level, double x, double y, double z, double range,
            boolean safe, List<MaterialStack> stacks, int quanta, @Nullable ImpactHolder impactPosHolder) {
        if (level == null || stacks == null || stacks.isEmpty()) {
            return new PourResult(null, null, null, false);
        }
        HitTarget target = getPouringTarget(level, new Vec3(x, y, z), new Vec3(x, y - range, z));
        if (target.acceptor() != null) {
            for (MaterialStack stack : stacks) {
                if (stack == null || stack.isEmpty() || stack.material == null) {
                    continue;
                }
                int amountToPour = Math.min(stack.amount, quanta);
                MaterialStack toPour = new MaterialStack(stack.material, amountToPour);
                MaterialStack left = tryPourStack(level, target.acceptor(), target.pos(), target.hit(), target.side(),
                        toPour, impactPosHolder);
                if (left != null) {
                    int moved = amountToPour - left.amount;
                    stack.amount -= moved;
                    cleanup(stacks);
                    return new PourResult(left, moved > 0 ? new MaterialStack(stack.material, moved) : null,
                            target.hit(), true);
                }
            }
        }
        MaterialStack wasted = spill(target.hit(), safe, stacks, quanta, impactPosHolder);
        cleanup(stacks);
        return new PourResult(null, wasted, target.hit(), false);
    }

    @Nullable
    public static MaterialStack tryPourStack(Level level, ICrucibleAcceptor acceptor, BlockPos pos, Vec3 hit,
            Direction side, MaterialStack stack, @Nullable ImpactHolder impactPosHolder) {
        if (stack == null || stack.material == null || stack.material.smeltable != SmeltingBehavior.SMELTABLE) {
            return null;
        }
        if (!acceptor.canAcceptPartialPour(level, pos, hit, side, stack)) {
            return null;
        }
        MaterialStack left = acceptor.pour(level, pos, hit, side, stack);
        if (left == null) {
            left = new MaterialStack(stack.material, 0);
        }
        if (impactPosHolder != null) {
            impactPosHolder.set(hit);
        }
        return left;
    }

    public static HitTarget getPouringTarget(Level level, Vec3 start, Vec3 end) {
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY, null));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return HitTarget.empty(hit.getLocation());
        }
        BlockPos pos = hit.getBlockPos();
        Direction side = hit.getDirection();
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof ICrucibleAcceptor acceptor) {
            return new HitTarget(acceptor, pos, hit.getLocation(), side);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ICrucibleAcceptor acceptor) {
            return new HitTarget(acceptor, pos, hit.getLocation(), side);
        }
        return HitTarget.empty(hit.getLocation());
    }

    public static MaterialStack spill(@Nullable Vec3 hit, boolean safe, List<MaterialStack> stacks, int quanta,
            @Nullable ImpactHolder impactPosHolder) {
        if (stacks == null || stacks.isEmpty()) {
            return null;
        }
        for (MaterialStack stack : stacks) {
            MaterialStack wasted = spill(hit, safe, stack, quanta, impactPosHolder);
            if (wasted != null) {
                cleanup(stacks);
                return wasted;
            }
        }
        cleanup(stacks);
        return null;
    }

    public static MaterialStack spill(@Nullable Vec3 hit, boolean safe, MaterialStack stack, int quanta,
            @Nullable ImpactHolder impactPosHolder) {
        if (safe || stack == null || stack.isEmpty()) {
            return null;
        }
        MaterialStack toWaste = new MaterialStack(stack.material, Math.min(stack.amount, quanta));
        stack.amount -= toWaste.amount;
        if (impactPosHolder != null && hit != null) {
            impactPosHolder.set(hit);
        }
        return toWaste;
    }

    private static void cleanup(List<MaterialStack> stacks) {
        for (Iterator<MaterialStack> iterator = stacks.iterator(); iterator.hasNext();) {
            MaterialStack stack = iterator.next();
            if (stack == null || stack.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public record HitTarget(@Nullable ICrucibleAcceptor acceptor, @Nullable BlockPos pos, @Nullable Vec3 hit,
            Direction side) {
        public static HitTarget empty(@Nullable Vec3 hit) {
            return new HitTarget(null, null, hit, Direction.DOWN);
        }
    }

    public record PourResult(@Nullable MaterialStack leftover, @Nullable MaterialStack moved, @Nullable Vec3 impact,
            boolean accepted) {
    }

    public static final class ImpactHolder {
        private Vec3 value = Vec3.ZERO;

        public void set(Vec3 value) {
            this.value = value == null ? Vec3.ZERO : value;
        }

        public Vec3 value() {
            return value;
        }
    }
}
