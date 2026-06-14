package com.hbm.lib;

import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmEnergyConnectionUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidConnectorBlock;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.util.HbmEntitySearchUtil;
import com.hbm.ntm.util.HbmMathUtil;
import com.hbm.ntm.util.HbmWorldUtil;
import com.hbm.ntm.util.RayTraceUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Narrow legacy facade for Energy MK2 helpers from 1.7.10 {@code com.hbm.lib.Library}.
 */
@Deprecated(forRemoval = false)
public final class Library {
    private static final Random RAND = new Random();

    public static final Direction POS_X = Direction.EAST;
    public static final Direction NEG_X = Direction.WEST;
    public static final Direction POS_Y = Direction.UP;
    public static final Direction NEG_Y = Direction.DOWN;
    public static final Direction POS_Z = Direction.SOUTH;
    public static final Direction NEG_Z = Direction.NORTH;

    private Library() {
    }

    public static boolean checkForHeld(Player player, Item item) {
        return HbmWorldUtil.checkForHeld(player, item);
    }

    public static boolean canConnect(BlockGetter level, int x, int y, int z, Direction cableSide) {
        return canConnect(level, new BlockPos(x, y, z), cableSide);
    }

    public static boolean canConnect(BlockGetter level, BlockPos targetPos, Direction cableSide) {
        return HbmEnergyConnectionUtil.canConnectLegacy(level, targetPos, cableSide);
    }

    public static boolean canConnectFluid(BlockGetter level, int x, int y, int z, Direction ductSide, FluidType type) {
        return canConnectFluid(level, new BlockPos(x, y, z), ductSide, type);
    }

    public static boolean canConnectFluid(BlockGetter level, BlockPos targetPos, Direction ductSide, FluidType type) {
        if (level == null || targetPos == null || ductSide == null || type == null) {
            return false;
        }
        Direction machineSide = ductSide.getOpposite();
        BlockEntity blockEntity = level.getBlockEntity(targetPos);
        if (blockEntity instanceof HbmFluidConnector connector && connector.canConnectFluid(type, machineSide)) {
            return true;
        }
        return level.getBlockState(targetPos).getBlock() instanceof HbmFluidConnectorBlock connectorBlock
                && connectorBlock.canConnectFluid(level, targetPos, type, machineSide);
    }

    public static LivingEntity getClosestEntityForChopper(Level level, double x, double y, double z, double radius) {
        return HbmEntitySearchUtil.getClosestVulnerableLiving(level, new Vec3(x, y, z), radius, entity -> true);
    }

    public static Player getClosestPlayerForSound(Level level, double x, double y, double z, double radius) {
        return HbmEntitySearchUtil.getClosestPlayerForSound(level, new Vec3(x, y, z), radius);
    }

    public static BlockHitResult rayTrace(Player player, double length, float partialTick) {
        return RayTraceUtil.rayTrace(player, length, partialTick);
    }

    public static BlockHitResult rayTrace(Player player, double length, float partialTick, boolean allowLiquids,
            boolean disallowNonCollidingBlocks, boolean hitOnMiss) {
        ClipContext.Fluid fluidMode = allowLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        ClipContext.Block blockMode = disallowNonCollidingBlocks ? ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE;
        return RayTraceUtil.rayTrace(player, length, partialTick, fluidMode, blockMode);
    }

    public static Vec3 getPosition(float partialTick, Player player) {
        return RayTraceUtil.getPosition(player, partialTick);
    }

    public static List<int[]> getBlockPosInPath(int x, int y, int z, int length, Vec3 direction) {
        List<int[]> list = new ArrayList<>();
        if (direction == null) {
            return list;
        }
        for (HbmWorldUtil.PathStep step : HbmWorldUtil.getBlockPosInPath(new BlockPos(x, y, z), length, direction)) {
            BlockPos pos = step.pos();
            list.add(new int[] { pos.getX(), pos.getY(), pos.getZ(), step.distance() });
        }
        return list;
    }

    public static long chargeItemsFromTE(ItemStack[] slots, int index, long power, long maxPower) {
        if (!isValidSlot(slots, index)) {
            return clampLegacyPower(power, maxPower);
        }
        ItemStack stack = slots[index];
        return stack == null ? clampLegacyPower(power, maxPower)
                : HbmBatteryTransfer.chargeItemsFromPower(stack, power, maxPower);
    }

    public static long chargeItemsFromTE(List<ItemStack> slots, int index, long power, long maxPower) {
        if (!isValidSlot(slots, index)) {
            return clampLegacyPower(power, maxPower);
        }
        ItemStack stack = slots.get(index);
        return stack == null ? clampLegacyPower(power, maxPower)
                : HbmBatteryTransfer.chargeItemsFromPower(stack, power, maxPower);
    }

    public static long chargeTEFromItems(ItemStack[] slots, int index, long power, long maxPower) {
        if (!isValidSlot(slots, index)) {
            return power;
        }
        ItemStack stack = slots[index];
        return stack == null ? power : HbmBatteryTransfer.chargePowerFromItem(stack, power, maxPower);
    }

    public static long chargeTEFromItems(List<ItemStack> slots, int index, long power, long maxPower) {
        if (!isValidSlot(slots, index)) {
            return power;
        }
        ItemStack stack = slots.get(index);
        return stack == null ? power : HbmBatteryTransfer.chargePowerFromItem(stack, power, maxPower);
    }

    public static void ffgeua(int x, int y, int z, boolean newTact, Object that, Level level) {
    }

    public static double smoothstep(double value, double edge0, double edge1) {
        return HbmMathUtil.smoothstep(value, edge0, edge1);
    }

    public static float smoothstep(float value, float edge0, float edge1) {
        return HbmMathUtil.smoothstep(value, edge0, edge1);
    }

    public static boolean isObstructed(Level level, double x, double y, double z, double a, double b, double c) {
        return HbmWorldUtil.isObstructed(level, new Vec3(x, y, z), new Vec3(a, b, c));
    }

    public static boolean isObstructedOpaque(Level level, double x, double y, double z, double a, double b, double c) {
        return HbmWorldUtil.isObstructedOpaque(level, new Vec3(x, y, z), new Vec3(a, b, c));
    }

    public static Block getRandomConcrete() {
        int value = RAND.nextInt(20);
        if (value <= 1) {
            return legacyBlock("brick_concrete_broken");
        }
        if (value <= 4) {
            return legacyBlock("brick_concrete_cracked");
        }
        if (value <= 10) {
            return legacyBlock("brick_concrete_mossy");
        }
        return legacyBlock("brick_concrete");
    }

    private static boolean isValidSlot(ItemStack[] slots, int index) {
        return slots != null && index >= 0 && index < slots.length;
    }

    private static boolean isValidSlot(List<ItemStack> slots, int index) {
        return slots != null && index >= 0 && index < slots.size();
    }

    private static long clampLegacyPower(long power, long maxPower) {
        if (power < 0L) {
            return 0L;
        }
        if (power > maxPower) {
            return maxPower;
        }
        return power;
    }

    private static Block legacyBlock(String legacyName) {
        return Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(HbmNtm.MOD_ID, legacyName)),
                "Missing migrated legacy block: " + legacyName);
    }
}
