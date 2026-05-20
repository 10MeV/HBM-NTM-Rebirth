package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

public final class HbmEnergyUtil {
    private HbmEnergyUtil() {
    }

    public static int toForgeInt(long value) {
        if (value <= 0L) {
            return 0;
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    public static long movePower(HbmEnergyProvider provider, HbmEnergyReceiver receiver, long maxTransfer) {
        if (provider == null || receiver == null || provider == receiver || maxTransfer <= 0L) {
            return 0L;
        }
        long toTransfer = Math.min(maxTransfer, Math.min(provider.getPower(), provider.getProviderSpeed()));
        toTransfer = Math.min(toTransfer, receiver.getReceiverSpeed());
        long accepted = toTransfer - receiver.transferPower(toTransfer);
        if (accepted > 0L) {
            provider.usePower(accepted);
        }
        return accepted;
    }

    public static long chargeItemFromStorage(ItemStack stack, HbmEnergyProvider provider, long maxTransfer) {
        if (stack.isEmpty() || provider == null || maxTransfer <= 0L || provider.getPower() <= 0L) {
            return 0L;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY, null)
                .map(itemEnergy -> {
                    long transfer = Math.min(maxTransfer, Math.min(provider.getPower(), provider.getProviderSpeed()));
                    int received = itemEnergy.receiveEnergy(toForgeInt(transfer), false);
                    if (received > 0) {
                        provider.usePower(received);
                    }
                    return (long) received;
                })
                .orElse(0L);
    }

    public static long chargeStorageFromItem(ItemStack stack, HbmEnergyReceiver receiver, long maxTransfer) {
        if (stack.isEmpty() || receiver == null || maxTransfer <= 0L) {
            return 0L;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY, null)
                .map(itemEnergy -> extractIntoReceiver(itemEnergy, receiver, maxTransfer))
                .orElse(0L);
    }

    public static long moveForgeEnergy(IEnergyStorage from, IEnergyStorage to, int maxTransfer) {
        if (from == null || to == null || from == to || maxTransfer <= 0) {
            return 0L;
        }
        int extracted = from.extractEnergy(maxTransfer, true);
        int accepted = to.receiveEnergy(extracted, false);
        if (accepted > 0) {
            from.extractEnergy(accepted, false);
        }
        return accepted;
    }

    public static long moveForgeEnergy(ItemStack from, ItemStack to, int maxTransfer) {
        if (from.isEmpty() || to.isEmpty() || maxTransfer <= 0) {
            return 0L;
        }
        return from.getCapability(ForgeCapabilities.ENERGY, null)
                .map(fromEnergy -> to.getCapability(ForgeCapabilities.ENERGY, null)
                        .map(toEnergy -> moveForgeEnergy(fromEnergy, toEnergy, maxTransfer))
                        .orElse(0L))
                .orElse(0L);
    }

    public static long pullFromNeighbor(Level level, BlockPos pos, Direction side, HbmEnergyReceiver receiver, long maxTransfer) {
        if (level == null || pos == null || side == null || receiver == null || maxTransfer <= 0L) {
            return 0L;
        }
        BlockEntity neighbor = level.getBlockEntity(pos.relative(side));
        if (neighbor == null) {
            return 0L;
        }
        Direction neighborSide = side.getOpposite();
        return neighbor.getCapability(ForgeCapabilities.ENERGY, neighborSide)
                .map(neighborEnergy -> extractIntoReceiver(neighborEnergy, receiver, maxTransfer))
                .orElse(0L);
    }

    public static long pushToNeighbor(Level level, BlockPos pos, Direction side, HbmEnergyProvider provider, long maxTransfer) {
        if (level == null || pos == null || side == null || provider == null || maxTransfer <= 0L) {
            return 0L;
        }
        BlockEntity neighbor = level.getBlockEntity(pos.relative(side));
        if (neighbor == null) {
            return 0L;
        }
        Direction neighborSide = side.getOpposite();
        return neighbor.getCapability(ForgeCapabilities.ENERGY, neighborSide)
                .map(neighborEnergy -> insertFromProvider(provider, neighborEnergy, maxTransfer))
                .orElse(0L);
    }

    public static long pullFromAllNeighbors(Level level, BlockPos pos, HbmEnergyReceiver receiver, long maxTransferPerSide) {
        long transferred = 0L;
        for (Direction side : Direction.values()) {
            transferred += pullFromNeighbor(level, pos, side, receiver, maxTransferPerSide);
        }
        return transferred;
    }

    public static long pullFromAllNeighborsCapped(Level level, BlockPos pos, HbmEnergyReceiver receiver, long totalMaxTransfer) {
        if (totalMaxTransfer <= 0L) {
            return 0L;
        }
        long transferred = 0L;
        for (Direction side : Direction.values()) {
            long remaining = totalMaxTransfer - transferred;
            if (remaining <= 0L) {
                break;
            }
            transferred += pullFromNeighbor(level, pos, side, receiver, remaining);
        }
        return transferred;
    }

    public static long pushToAllNeighbors(Level level, BlockPos pos, HbmEnergyProvider provider, long maxTransferPerSide) {
        long transferred = 0L;
        for (Direction side : Direction.values()) {
            transferred += pushToNeighbor(level, pos, side, provider, maxTransferPerSide);
        }
        return transferred;
    }

    public static long pushToAllNeighborsCapped(Level level, BlockPos pos, HbmEnergyProvider provider, long totalMaxTransfer) {
        if (totalMaxTransfer <= 0L) {
            return 0L;
        }
        long transferred = 0L;
        for (Direction side : Direction.values()) {
            long remaining = totalMaxTransfer - transferred;
            if (remaining <= 0L) {
                break;
            }
            transferred += pushToNeighbor(level, pos, side, provider, remaining);
        }
        return transferred;
    }

    private static long extractIntoReceiver(IEnergyStorage itemEnergy, HbmEnergyReceiver receiver, long maxTransfer) {
        long transfer = Math.min(maxTransfer, receiver.getReceiverSpeed());
        transfer = Math.min(transfer, Math.max(0L, receiver.getMaxPower() - receiver.getPower()));
        int extracted = itemEnergy.extractEnergy(toForgeInt(transfer), true);
        if (extracted <= 0) {
            return 0L;
        }
        long accepted = extracted - receiver.transferPower(extracted);
        if (accepted > 0L) {
            itemEnergy.extractEnergy(toForgeInt(accepted), false);
        }
        return accepted;
    }

    private static long insertFromProvider(HbmEnergyProvider provider, IEnergyStorage target, long maxTransfer) {
        long transfer = Math.min(maxTransfer, Math.min(provider.getPower(), provider.getProviderSpeed()));
        int accepted = target.receiveEnergy(toForgeInt(transfer), false);
        if (accepted > 0) {
            provider.usePower(accepted);
        }
        return accepted;
    }
}
