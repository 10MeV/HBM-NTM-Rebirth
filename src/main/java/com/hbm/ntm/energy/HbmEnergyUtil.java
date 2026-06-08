package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Objects;

public final class HbmEnergyUtil {
    private HbmEnergyUtil() {
    }

    /**
     * A 1.7.10-style remote energy port. The direction is the old DirPos direction
     * from the owning machine toward/out of the conductor position.
     */
    public record EnergyPort(BlockPos offset, Direction direction) {
        public EnergyPort {
            Objects.requireNonNull(offset, "offset");
            Objects.requireNonNull(direction, "direction");
        }

        public static EnergyPort of(int x, int y, int z, Direction direction) {
            return new EnergyPort(new BlockPos(x, y, z), direction);
        }

        public BlockPos conductorPos(BlockPos origin) {
            return origin.offset(offset);
        }

        public Direction conductorSide() {
            return direction.getOpposite();
        }
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
        if (stack.getItem() instanceof HbmBatteryItem) {
            long available = Math.min(provider.getPower(), maxTransfer);
            long remaining = HbmBatteryTransfer.chargeItemsFromPower(stack, available, provider.getMaxPower());
            long used = available - remaining;
            if (used != 0L) {
                provider.setPower(provider.getPower() - used);
            }
            return used;
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
        if (stack.getItem() instanceof HbmBatteryItem) {
            long before = receiver.getPower();
            long cappedMaxPower = Math.min(receiver.getMaxPower(), before + maxTransfer);
            long after = HbmBatteryTransfer.chargePowerFromItem(stack, before, cappedMaxPower);
            long accepted = after - before;
            if (accepted != 0L) {
                receiver.setPower(after);
            }
            return accepted;
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

    public static boolean subscribeProviderToNeighborNetwork(Level level, BlockPos pos, Direction side, HbmEnergyProvider provider) {
        if (level == null || pos == null || side == null || provider == null) {
            return false;
        }
        boolean subscribed = subscribeProviderToNetwork(level, pos.relative(side), side.getOpposite(), provider);
        HbmEnergyDebug.spawnProviderSubscription(level, pos, side, subscribed);
        return subscribed;
    }

    public static boolean subscribeReceiverToNeighborNetwork(Level level, BlockPos pos, Direction side, HbmEnergyReceiver receiver) {
        if (level == null || pos == null || side == null || receiver == null) {
            return false;
        }
        boolean subscribed = subscribeReceiverToNetwork(level, pos.relative(side), side.getOpposite(), receiver);
        HbmEnergyDebug.spawnReceiverSubscription(level, pos, side, subscribed);
        return subscribed;
    }

    public static boolean subscribeProviderToPort(Level level, BlockPos origin, EnergyPort port, HbmEnergyProvider provider) {
        if (level == null || origin == null || port == null || provider == null) {
            return false;
        }
        BlockPos conductorPos = port.conductorPos(origin);
        boolean subscribed = subscribeProviderToNetwork(level, conductorPos, port.conductorSide(), provider);
        HbmEnergyDebug.spawnRemoteProviderSubscription(level, conductorPos, port.direction(), subscribed);
        return subscribed;
    }

    public static boolean subscribeReceiverToPort(Level level, BlockPos origin, EnergyPort port, HbmEnergyReceiver receiver) {
        if (level == null || origin == null || port == null || receiver == null) {
            return false;
        }
        BlockPos conductorPos = port.conductorPos(origin);
        boolean subscribed = subscribeReceiverToNetwork(level, conductorPos, port.conductorSide(), receiver);
        HbmEnergyDebug.spawnRemoteReceiverSubscription(level, conductorPos, port.direction(), subscribed);
        return subscribed;
    }

    public static int subscribeProviderToPorts(Level level, BlockPos origin, Iterable<EnergyPort> ports, HbmEnergyProvider provider) {
        if (ports == null) {
            return 0;
        }
        int subscribed = 0;
        for (EnergyPort port : ports) {
            if (subscribeProviderToPort(level, origin, port, provider)) {
                subscribed++;
            }
        }
        return subscribed;
    }

    public static int subscribeReceiverToPorts(Level level, BlockPos origin, Iterable<EnergyPort> ports, HbmEnergyReceiver receiver) {
        if (ports == null) {
            return 0;
        }
        int subscribed = 0;
        for (EnergyPort port : ports) {
            if (subscribeReceiverToPort(level, origin, port, receiver)) {
                subscribed++;
            }
        }
        return subscribed;
    }

    public static boolean subscribeProviderToNetwork(Level level, BlockPos conductorPos, Direction conductorSide, HbmEnergyProvider provider) {
        HbmPowerNet powerNet = getConnectablePowerNet(level, conductorPos, conductorSide);
        if (provider == null || powerNet == null) {
            return false;
        }
        powerNet.addProvider(provider);
        return true;
    }

    public static boolean subscribeReceiverToNetwork(Level level, BlockPos conductorPos, Direction conductorSide, HbmEnergyReceiver receiver) {
        HbmPowerNet powerNet = getConnectablePowerNet(level, conductorPos, conductorSide);
        if (receiver == null || powerNet == null) {
            return false;
        }
        powerNet.addReceiver(receiver);
        return true;
    }

    public static boolean unsubscribeProviderFromNeighborNetwork(Level level, BlockPos pos, Direction side, HbmEnergyProvider provider) {
        if (level == null || pos == null || side == null || provider == null) {
            return false;
        }
        return unsubscribeProviderFromNetwork(level, pos.relative(side), side.getOpposite(), provider);
    }

    public static boolean unsubscribeReceiverFromNeighborNetwork(Level level, BlockPos pos, Direction side, HbmEnergyReceiver receiver) {
        if (level == null || pos == null || side == null || receiver == null) {
            return false;
        }
        return unsubscribeReceiverFromNetwork(level, pos.relative(side), side.getOpposite(), receiver);
    }

    public static boolean unsubscribeProviderFromPort(Level level, BlockPos origin, EnergyPort port, HbmEnergyProvider provider) {
        if (level == null || origin == null || port == null || provider == null) {
            return false;
        }
        return unsubscribeProviderFromNetwork(level, port.conductorPos(origin), port.conductorSide(), provider);
    }

    public static boolean unsubscribeReceiverFromPort(Level level, BlockPos origin, EnergyPort port, HbmEnergyReceiver receiver) {
        if (level == null || origin == null || port == null || receiver == null) {
            return false;
        }
        return unsubscribeReceiverFromNetwork(level, port.conductorPos(origin), port.conductorSide(), receiver);
    }

    public static boolean unsubscribeProviderFromNetwork(Level level, BlockPos conductorPos, Direction conductorSide, HbmEnergyProvider provider) {
        HbmPowerNet powerNet = getConnectablePowerNet(level, conductorPos, conductorSide);
        if (provider == null || powerNet == null || !powerNet.isProvider(provider)) {
            return false;
        }
        powerNet.removeProvider(provider);
        return true;
    }

    public static boolean unsubscribeReceiverFromNetwork(Level level, BlockPos conductorPos, Direction conductorSide, HbmEnergyReceiver receiver) {
        HbmPowerNet powerNet = getConnectablePowerNet(level, conductorPos, conductorSide);
        if (receiver == null || powerNet == null || !powerNet.isSubscribed(receiver)) {
            return false;
        }
        powerNet.removeReceiver(receiver);
        return true;
    }

    public static long tryProvideToNeighbor(Level level, BlockPos pos, Direction side, HbmEnergyProvider provider) {
        if (level == null || pos == null || side == null || provider == null) {
            return 0L;
        }
        subscribeProviderToNeighborNetwork(level, pos, side, provider);
        long direct = provideDirectlyToNeighbor(level, pos, side, provider);
        if (direct > 0L) {
            return direct;
        }
        return 0L;
    }

    public static int tryProvideToAllNeighbors(Level level, BlockPos pos, HbmEnergyProvider provider) {
        int touched = 0;
        for (Direction side : Direction.values()) {
            boolean subscribed = subscribeProviderToNeighborNetwork(level, pos, side, provider);
            long transferred = provideDirectlyToNeighbor(level, pos, side, provider);
            if (subscribed || transferred > 0L) {
                touched++;
            }
        }
        return touched;
    }

    public static int tryProvideToPorts(Level level, BlockPos origin, Iterable<EnergyPort> ports, HbmEnergyProvider provider) {
        if (ports == null) {
            return 0;
        }
        int touched = 0;
        for (EnergyPort port : ports) {
            boolean subscribed = subscribeProviderToPort(level, origin, port, provider);
            long transferred = provideDirectlyToPort(level, origin, port, provider);
            if (subscribed || transferred > 0L) {
                touched++;
            }
        }
        return touched;
    }

    public static int subscribeReceiverToAllNeighborNetworks(Level level, BlockPos pos, HbmEnergyReceiver receiver) {
        int subscribed = 0;
        for (Direction side : Direction.values()) {
            if (subscribeReceiverToNeighborNetwork(level, pos, side, receiver)) {
                subscribed++;
            }
        }
        return subscribed;
    }

    public static int unsubscribeProviderFromAllNeighborNetworks(Level level, BlockPos pos, HbmEnergyProvider provider) {
        int unsubscribed = 0;
        for (Direction side : Direction.values()) {
            if (unsubscribeProviderFromNeighborNetwork(level, pos, side, provider)) {
                unsubscribed++;
            }
        }
        return unsubscribed;
    }

    public static int unsubscribeReceiverFromAllNeighborNetworks(Level level, BlockPos pos, HbmEnergyReceiver receiver) {
        int unsubscribed = 0;
        for (Direction side : Direction.values()) {
            if (unsubscribeReceiverFromNeighborNetwork(level, pos, side, receiver)) {
                unsubscribed++;
            }
        }
        return unsubscribed;
    }

    public static HbmPowerNet getConnectablePowerNet(Level level, BlockPos conductorPos, Direction conductorSide) {
        if (level == null || conductorPos == null || conductorSide == null) {
            return null;
        }
        BlockEntity conductor = level.getBlockEntity(conductorPos);
        if (!(conductor instanceof HbmEnergyConnector connector) || !connector.canConnectEnergy(conductorSide)) {
            return null;
        }
        HbmEnergyNode node = HbmEnergyNodespace.getNode(level, conductorPos);
        HbmPowerNet powerNet = node == null ? null : node.getPowerNet();
        return powerNet != null && powerNet.isValid() ? powerNet : null;
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

    private static long provideDirectlyToNeighbor(Level level, BlockPos pos, Direction side, HbmEnergyProvider provider) {
        BlockEntity neighbor = level.getBlockEntity(pos.relative(side));
        if (!(neighbor instanceof HbmEnergyReceiver receiver)
                || !(neighbor instanceof HbmEnergyConnector connector)
                || receiver == provider
                || !receiver.allowDirectProvision()) {
            return 0L;
        }
        Direction neighborSide = side.getOpposite();
        if (!connector.canConnectEnergy(neighborSide)) {
            return 0L;
        }
        long transferred = movePower(provider, receiver, Math.min(provider.getProviderSpeed(), receiver.getReceiverSpeed()));
        HbmEnergyDebug.spawnDirectTransfer(level, pos, side, transferred);
        return transferred;
    }

    private static long provideDirectlyToPort(Level level, BlockPos origin, EnergyPort port, HbmEnergyProvider provider) {
        if (level == null || origin == null || port == null || provider == null) {
            return 0L;
        }
        BlockPos targetPos = port.conductorPos(origin);
        Direction targetSide = port.conductorSide();
        BlockEntity target = level.getBlockEntity(targetPos);
        if (target instanceof HbmEnergyReceiver receiver
                && target instanceof HbmEnergyConnector connector
                && receiver != provider
                && receiver.allowDirectProvision()
                && connector.canConnectEnergy(targetSide)) {
            long transferred = movePower(provider, receiver, Math.min(provider.getProviderSpeed(), receiver.getReceiverSpeed()));
            HbmEnergyDebug.spawnRemoteProviderSubscription(level, targetPos, port.direction(), transferred > 0L);
            return transferred;
        }
        if (target == null) {
            return 0L;
        }
        long transferred = target.getCapability(ForgeCapabilities.ENERGY, targetSide)
                .map(targetEnergy -> insertFromProvider(provider, targetEnergy, provider.getProviderSpeed()))
                .orElse(0L);
        HbmEnergyDebug.spawnRemoteProviderSubscription(level, targetPos, port.direction(), transferred > 0L);
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
