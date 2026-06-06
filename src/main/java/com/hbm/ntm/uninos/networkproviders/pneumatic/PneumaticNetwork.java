package com.hbm.ntm.uninos.networkproviders.pneumatic;

import com.hbm.ntm.uninos.HbmNodeNet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PneumaticNetwork extends HbmNodeNet<PneumaticNode> {
    public static final byte SEND_FIRST = 0;
    public static final byte SEND_LAST = 1;
    public static final byte SEND_RANDOM = 2;
    public static final byte RECEIVE_ROBIN = 0;
    public static final byte RECEIVE_RANDOM = 1;

    public static final int TIMEOUT_MS = 1_000;
    public static final int ITEMS_PER_TRANSFER = 64;

    private final Random random = new Random();
    private final Map<PneumaticReceiver, Long> receivers = new LinkedHashMap<>();
    private final LinkedHashSet<PneumaticStackCache> accessors = new LinkedHashSet<>();
    private final LinkedHashSet<PneumaticSlotMonitorProvider> storages = new LinkedHashSet<>();

    private int lastTransfer;

    public void addReceiver(PneumaticReceiver receiver) {
        if (receiver != null) {
            receivers.put(receiver, System.currentTimeMillis());
        }
    }

    public void removeReceiver(PneumaticReceiver receiver) {
        receivers.remove(receiver);
    }

    public void addStackCache(PneumaticStackCache accessor) {
        if (accessor != null && accessors.add(accessor)) {
            for (PneumaticSlotMonitorProvider storage : storages) {
                storage.onNewCacheHasJoined(accessor, this);
            }
        }
    }

    public void removeStackCache(PneumaticStackCache accessor) {
        if (accessor != null) {
            accessors.remove(accessor);
        }
    }

    public void addStorage(PneumaticSlotMonitorProvider storage) {
        if (storage != null) {
            storages.add(storage);
        }
    }

    public void removeStorage(PneumaticSlotMonitorProvider storage) {
        storages.remove(storage);
    }

    public int getReceiverCount() {
        pruneStale(System.currentTimeMillis());
        return receivers.size();
    }

    public int getAccessorCount() {
        accessors.removeIf(PneumaticStackCache::hasExpired);
        return accessors.size();
    }

    public int getStorageCount() {
        return storages.size();
    }

    public int getLastTransfer() {
        return lastTransfer;
    }

    public DebugSnapshot createDebugSnapshot() {
        pruneStale(System.currentTimeMillis());
        accessors.removeIf(PneumaticStackCache::hasExpired);
        storages.removeIf(PneumaticSlotMonitorProvider::hasExpired);
        return new DebugSnapshot(isValid(), linkCount(), receivers.size(), accessors.size(), storages.size(), lastTransfer, TIMEOUT_MS);
    }

    public LinkedHashSet<PneumaticStackCache> getAccessors() {
        return new LinkedHashSet<>(accessors);
    }

    @Override
    public void destroy() {
        super.destroy();
        receivers.clear();
        accessors.clear();
        storages.clear();
        lastTransfer = 0;
    }

    @Override
    public void joinNetwork(HbmNodeNet<PneumaticNode> network) {
        if (!(network instanceof PneumaticNetwork pneumaticNetwork) || pneumaticNetwork == this) {
            super.joinNetwork(network);
            return;
        }

        Map<PneumaticReceiver, Long> oldReceivers = new LinkedHashMap<>(pneumaticNetwork.receivers);
        LinkedHashSet<PneumaticStackCache> oldAccessors = new LinkedHashSet<>(pneumaticNetwork.accessors);
        LinkedHashSet<PneumaticSlotMonitorProvider> oldStorages = new LinkedHashSet<>(pneumaticNetwork.storages);
        super.joinNetwork(network);
        receivers.putAll(oldReceivers);
        accessors.addAll(oldAccessors);
        storages.addAll(oldStorages);
    }

    public void resetTrackers() {
        lastTransfer = 0;
    }

    public void update() {
        pruneStale(System.currentTimeMillis());
        accessors.removeIf(PneumaticStackCache::hasExpired);
        storages.removeIf(PneumaticSlotMonitorProvider::hasExpired);
    }

    public boolean send(PneumaticItemAccess source, PneumaticEndpoint sender, int sendOrder, int receiveOrder, int maxRange, int nextReceiver) {
        if (source == null || source.handler() == null || sender == null) {
            return false;
        }

        pruneStale(System.currentTimeMillis());
        if (receivers.isEmpty()) {
            return false;
        }

        int[] sourceSlots = slotOrder(source.handler(), sendOrder, random);
        if (!hasSendableItem(source.handler(), sourceSlots, sender)) {
            return false;
        }

        List<Map.Entry<PneumaticReceiver, Long>> receiverList = new ArrayList<>(receivers.entrySet());
        if (receiveOrder == RECEIVE_ROBIN) {
            receiverList.sort(new ReceiverComparator(sender.getPneumaticPos()));
        } else if (receiveOrder == RECEIVE_RANDOM) {
            Collections.shuffle(receiverList, random);
        }

        int attempts = 0;
        int maxAttempts = receiverList.size();
        while (attempts < maxAttempts && !receiverList.isEmpty()) {
            int index = receiveOrder == RECEIVE_ROBIN ? Math.floorMod(nextReceiver + attempts, receiverList.size()) : attempts;
            PneumaticReceiver receiver = receiverList.get(index).getKey();
            if (!isValidReceiver(receiver)) {
                receivers.remove(receiver);
                attempts++;
                continue;
            }
            if (!inRange(source.pos(), receiver.access().pos(), maxRange)) {
                attempts++;
                continue;
            }

            int moved = moveItems(source.handler(), receiver, sender, sourceSlots);
            if (moved > 0) {
                lastTransfer = moved;
                return true;
            }
            attempts++;
        }
        return false;
    }

    private int moveItems(IItemHandler source, PneumaticReceiver receiver, PneumaticEndpoint sender, int[] sourceSlots) {
        int transferMassLeft = ITEMS_PER_TRANSFER;
        int movedItems = 0;
        int itemHardCap = Math.max(1, receiver.endpoint().itemHardCap());

        for (int sourceSlot : sourceSlots) {
            ItemStack sourceStack = source.extractItem(sourceSlot, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty() || !passesFilter(sender, sourceStack)) {
                continue;
            }
            if (receiver.endpoint() != sender && !passesFilter(receiver.endpoint(), sourceStack)) {
                continue;
            }

            int proportionalValue = Math.max(1, Math.min(64, 64 / Math.max(1, sourceStack.getMaxStackSize())));
            int movedFromSlot = moveIntoMatchingStacks(source, sourceSlot, receiver.handler(), sourceStack, transferMassLeft, proportionalValue, itemHardCap);
            transferMassLeft -= movedFromSlot * proportionalValue;
            movedItems += movedFromSlot;
            if (transferMassLeft <= 0) {
                break;
            }

            sourceStack = source.extractItem(sourceSlot, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty()) {
                continue;
            }
            int movedIntoEmpty = moveIntoEmptySlots(source, sourceSlot, receiver.handler(), sourceStack, transferMassLeft, proportionalValue, itemHardCap);
            transferMassLeft -= movedIntoEmpty * proportionalValue;
            movedItems += movedIntoEmpty;
            if (transferMassLeft <= 0) {
                break;
            }
        }
        return movedItems;
    }

    private static int moveIntoMatchingStacks(IItemHandler source, int sourceSlot, IItemHandler destination,
            ItemStack sourceStack, int transferMassLeft, int proportionalValue, int itemHardCap) {
        int moved = 0;
        for (int destSlot = 0; destSlot < destination.getSlots() && transferMassLeft > 0; destSlot++) {
            ItemStack destStack = destination.getStackInSlot(destSlot);
            if (destStack.isEmpty() || !ItemStack.isSameItemSameTags(sourceStack, destStack)) {
                continue;
            }
            int maxByMass = transferMassLeft / proportionalValue;
            int toMove = Math.min(Math.min(sourceStack.getCount(), maxByMass), itemHardCap);
            int accepted = simulateAccepted(destination, destSlot, sourceStack, toMove);
            if (accepted <= 0) {
                continue;
            }
            moved += moveExact(source, sourceSlot, destination, destSlot, sourceStack, accepted);
            transferMassLeft -= accepted * proportionalValue;
            sourceStack = source.extractItem(sourceSlot, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty()) {
                break;
            }
        }
        return moved;
    }

    private static int moveIntoEmptySlots(IItemHandler source, int sourceSlot, IItemHandler destination,
            ItemStack sourceStack, int transferMassLeft, int proportionalValue, int itemHardCap) {
        int moved = 0;
        for (int destSlot = 0; destSlot < destination.getSlots() && transferMassLeft > 0; destSlot++) {
            if (!destination.getStackInSlot(destSlot).isEmpty()) {
                continue;
            }
            int maxByMass = transferMassLeft / proportionalValue;
            int toMove = Math.min(Math.min(sourceStack.getCount(), maxByMass), itemHardCap);
            int accepted = simulateAccepted(destination, destSlot, sourceStack, toMove);
            if (accepted <= 0) {
                continue;
            }
            moved += moveExact(source, sourceSlot, destination, destSlot, sourceStack, accepted);
            transferMassLeft -= accepted * proportionalValue;
            sourceStack = source.extractItem(sourceSlot, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty()) {
                break;
            }
        }
        return moved;
    }

    private static int simulateAccepted(IItemHandler destination, int destSlot, ItemStack template, int amount) {
        if (amount <= 0) {
            return 0;
        }
        ItemStack offer = copyWithCount(template, amount);
        ItemStack remaining = destination.insertItem(destSlot, offer, true);
        return amount - remaining.getCount();
    }

    private static int moveExact(IItemHandler source, int sourceSlot, IItemHandler destination, int destSlot, ItemStack template, int amount) {
        ItemStack extracted = source.extractItem(sourceSlot, amount, false);
        if (extracted.isEmpty()) {
            return 0;
        }
        ItemStack remaining = destination.insertItem(destSlot, extracted, false);
        int moved = extracted.getCount() - remaining.getCount();
        if (!remaining.isEmpty()) {
            source.insertItem(sourceSlot, remaining, false);
        }
        return moved;
    }

    private static boolean hasSendableItem(IItemHandler source, int[] sourceSlots, PneumaticEndpoint sender) {
        for (int slot : sourceSlots) {
            ItemStack stack = source.extractItem(slot, Integer.MAX_VALUE, true);
            if (!stack.isEmpty() && passesFilter(sender, stack)) {
                return true;
            }
        }
        return false;
    }

    private static boolean passesFilter(PneumaticEndpoint endpoint, ItemStack stack) {
        boolean match = endpoint.matchesFilter(stack);
        return endpoint.isWhitelist() == match;
    }

    private void pruneStale(long timestamp) {
        Iterator<Map.Entry<PneumaticReceiver, Long>> iterator = receivers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<PneumaticReceiver, Long> entry = iterator.next();
            if (timestamp - entry.getValue() > TIMEOUT_MS || !isValidReceiver(entry.getKey())) {
                iterator.remove();
            }
        }
    }

    private static boolean isValidReceiver(PneumaticReceiver receiver) {
        return receiver != null
                && receiver.handler() != null
                && receiver.endpoint() != null
                && receiver.endpoint().isPneumaticLoaded();
    }

    private static boolean inRange(@Nullable BlockPos source, @Nullable BlockPos destination, int maxRange) {
        if (source == null || destination == null) {
            return true;
        }
        return source.distSqr(destination) <= (double) maxRange * (double) maxRange;
    }

    private static int[] slotOrder(IItemHandler handler, int order, Random random) {
        int[] slots = new int[handler.getSlots()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        if (order == SEND_LAST) {
            reverse(slots);
        } else if (order == SEND_RANDOM) {
            shuffle(slots, random);
        }
        return slots;
    }

    private static void reverse(int[] values) {
        for (int i = 0; i < values.length / 2; i++) {
            int other = values.length - i - 1;
            int temp = values[i];
            values[i] = values[other];
            values[other] = temp;
        }
    }

    private static void shuffle(int[] values, Random random) {
        for (int i = values.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = values[index];
            values[index] = values[i];
            values[i] = temp;
        }
    }

    private static ItemStack copyWithCount(ItemStack stack, int count) {
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    private static final class ReceiverComparator implements Comparator<Map.Entry<PneumaticReceiver, Long>> {
        private final BlockPos origin;

        private ReceiverComparator(BlockPos origin) {
            this.origin = origin;
        }

        @Override
        public int compare(Map.Entry<PneumaticReceiver, Long> first, Map.Entry<PneumaticReceiver, Long> second) {
            BlockPos firstPos = first.getKey().endpoint().getPneumaticPos();
            BlockPos secondPos = second.getKey().endpoint().getPneumaticPos();
            double firstDistance = firstPos.distSqr(origin);
            double secondDistance = secondPos.distSqr(origin);
            if (firstDistance == secondDistance) {
                return PneumaticUtil.identifier(firstPos) - PneumaticUtil.identifier(secondPos);
            }
            return Double.compare(firstDistance, secondDistance);
        }
    }

    public record DebugSnapshot(
            boolean valid,
            int links,
            int receivers,
            int accessors,
            int storages,
            int lastTransfer,
            int timeoutMs) {
    }
}
