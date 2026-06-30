package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmChargeableItem;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmLegacyBatteryMaps;
import com.hbm.ntm.energy.HbmLoadedEnergy;
import com.hbm.ntm.energy.HbmPowerNet;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegistryObject;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EnergyMk2GameTests {
    private EnergyMk2GameTests() {
    }

    public static void main(String[] args) {
        runStandalone();
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(EnergyMk2GameTests.class);
    }

    public static void runStandalone() {
        powerNetDistributesLikeLegacyMk2();
        powerNetProviderRoundingDrainsExactlyTransferredPower();
        powerNetPrunesTimeoutAndUnloadedSubscribers();
        sendPowerDiodeKeepsLegacyUnclampedReceiverShape();
        forgeEnergyBridgeIsOneToOneAndIntCapped();
    }

    public static void runAll() {
        powerNetDistributesLikeLegacyMk2();
        powerNetProviderRoundingDrainsExactlyTransferredPower();
        powerNetPrunesTimeoutAndUnloadedSubscribers();
        sendPowerDiodeKeepsLegacyUnclampedReceiverShape();
        forgeEnergyBridgeIsOneToOneAndIntCapped();
        batteryDefaultsAndLegacyTransferEdgesStayRaw();
        legacyBatteryDisplayListMatchesGasCentrifugeNeiOrder();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void powerNetDistributesLikeLegacyMk2(GameTestHelper helper) {
        powerNetDistributesLikeLegacyMk2();
        helper.succeed();
    }

    private static void powerNetDistributesLikeLegacyMk2() {
        HbmPowerNet net = new HbmPowerNet(0L);
        TestProvider providerA = new TestProvider(120L, 120L);
        TestProvider providerB = new TestProvider(80L, 80L);
        TestReceiver high = new TestReceiver(0L, 100L, 100L, HbmEnergyReceiver.ConnectionPriority.HIGH);
        TestReceiver normalA = new TestReceiver(0L, 100L, 100L, HbmEnergyReceiver.ConnectionPriority.NORMAL);
        TestReceiver normalB = new TestReceiver(0L, 100L, 100L, HbmEnergyReceiver.ConnectionPriority.NORMAL);

        net.addProvider(providerA);
        net.addProvider(providerB);
        net.addReceiver(high);
        net.addReceiver(normalA);
        net.addReceiver(normalB);

        assertEquals(200L, net.update(), "transferred HE");
        assertEquals(100L, high.getPower(), "high priority receiver charge");
        assertEquals(50L, normalA.getPower(), "normal receiver A charge");
        assertEquals(50L, normalB.getPower(), "normal receiver B charge");
        assertEquals(0L, providerA.getPower(), "weighted provider A drain");
        assertEquals(0L, providerB.getPower(), "weighted provider B drain");
        assertEquals(200L, net.getEnergyTracker(), "energy tracker");
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void powerNetProviderRoundingDrainsExactlyTransferredPower(GameTestHelper helper) {
        powerNetProviderRoundingDrainsExactlyTransferredPower();
        helper.succeed();
    }

    private static void powerNetProviderRoundingDrainsExactlyTransferredPower() {
        HbmPowerNet net = new HbmPowerNet(0L);
        TestProvider providerA = new TestProvider(1L, 1L);
        TestProvider providerB = new TestProvider(1L, 1L);
        TestReceiver receiver = new TestReceiver(0L, 1L, 1L, HbmEnergyReceiver.ConnectionPriority.NORMAL);

        net.addProvider(providerA);
        net.addProvider(providerB);
        net.addReceiver(receiver);

        assertEquals(1L, net.update(), "rounded transfer");
        assertEquals(1L, receiver.getPower(), "receiver got one HE");
        assertEquals(1L, providerA.getPower() + providerB.getPower(), "random provider remainder drained exactly once");
        assertEquals(1L, net.getEnergyTracker(), "rounded tracker");
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void powerNetPrunesTimeoutAndUnloadedSubscribers(GameTestHelper helper) {
        powerNetPrunesTimeoutAndUnloadedSubscribers();
        helper.succeed();
    }

    private static void powerNetPrunesTimeoutAndUnloadedSubscribers() {
        HbmPowerNet timeoutNet = new HbmPowerNet();
        TestProvider expiredProvider = new TestProvider(100L, 100L);
        TestReceiver expiredReceiver = new TestReceiver(0L, 100L, 100L, HbmEnergyReceiver.ConnectionPriority.NORMAL);
        timeoutNet.addProvider(expiredProvider);
        timeoutNet.addReceiver(expiredReceiver);
        forceSubscriptionLastSeen(timeoutNet, 0L);
        assertEquals(0L, timeoutNet.update(), "expired subscriptions should not transfer");
        assertEquals(0, timeoutNet.getProviderCount(), "expired provider pruned");
        assertEquals(0, timeoutNet.getReceiverCount(), "expired receiver pruned");

        HbmPowerNet loadedNet = new HbmPowerNet(0L);
        TestProvider unloadedProvider = new TestProvider(100L, 100L);
        TestReceiver unloadedReceiver = new TestReceiver(0L, 100L, 100L, HbmEnergyReceiver.ConnectionPriority.NORMAL);
        unloadedProvider.loaded = false;
        unloadedReceiver.loaded = false;
        loadedNet.addProvider(unloadedProvider);
        loadedNet.addReceiver(unloadedReceiver);
        assertEquals(0L, loadedNet.update(), "unloaded subscribers should not transfer");
        assertEquals(0, loadedNet.getProviderCount(), "unloaded provider pruned");
        assertEquals(0, loadedNet.getReceiverCount(), "unloaded receiver pruned");
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void sendPowerDiodeKeepsLegacyUnclampedReceiverShape(GameTestHelper helper) {
        sendPowerDiodeKeepsLegacyUnclampedReceiverShape();
        helper.succeed();
    }

    private static void sendPowerDiodeKeepsLegacyUnclampedReceiverShape() {
        HbmPowerNet net = new HbmPowerNet(0L);
        TestReceiver overfull = new TestReceiver(150L, 100L, 100L, HbmEnergyReceiver.ConnectionPriority.NORMAL);
        TestReceiver receiver = new TestReceiver(0L, 100L, 100L, HbmEnergyReceiver.ConnectionPriority.NORMAL);
        net.addReceiver(overfull);
        net.addReceiver(receiver);

        assertEquals(-50L, net.sendPowerDiode(50L), "legacy diode remainder can go negative");
        assertEquals(150L, overfull.getPower(), "overfull receiver ignored non-positive send");
        assertEquals(100L, receiver.getPower(), "positive receiver got unclamped weighted send");
        assertEquals(100L, net.getEnergyTracker(), "diode tracker counts accepted HE");
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void forgeEnergyBridgeIsOneToOneAndIntCapped(GameTestHelper helper) {
        forgeEnergyBridgeIsOneToOneAndIntCapped();
        helper.succeed();
    }

    private static void forgeEnergyBridgeIsOneToOneAndIntCapped() {
        HbmEnergyStorage storage = new HbmEnergyStorage(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);
        storage.setPower(100L);
        ForgeEnergyAdapter adapter = new ForgeEnergyAdapter(storage);

        assertEquals(250, adapter.receiveEnergy(250, true), "simulate receive FE");
        assertEquals(100L, storage.getPower(), "simulate receive must not mutate HE");
        assertEquals(250, adapter.receiveEnergy(250, false), "receive FE equals accepted HE");
        assertEquals(350L, storage.getPower(), "receive writes same HE value");
        assertEquals(125, adapter.extractEnergy(125, true), "simulate extract FE");
        assertEquals(350L, storage.getPower(), "simulate extract must not mutate HE");
        assertEquals(125, adapter.extractEnergy(125, false), "extract FE equals removed HE");
        assertEquals(225L, storage.getPower(), "extract removes same HE value");

        storage.setPower(Long.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, adapter.getEnergyStored(), "FE getter caps internal long HE");
        assertEquals(Long.MAX_VALUE, storage.getPower(), "FE getter must not truncate internal HE");
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batteryDefaultsAndLegacyTransferEdgesStayRaw(GameTestHelper helper) {
        batteryDefaultsAndLegacyTransferEdgesStayRaw();
        helper.succeed();
    }

    private static void batteryDefaultsAndLegacyTransferEdgesStayRaw() {
        HbmBatteryItem battery = requireBattery(ModItems.BATTERY_POTATO.get(), "battery_potato");
        ItemStack stack = new ItemStack(battery);
        long maxCharge = battery.getMaxCharge(stack);
        assertEquals(maxCharge, battery.peekCharge(stack), "plain battery defaults full without NBT");
        assertTrue(!stack.hasTag(), "peekCharge must not create NBT");
        assertEquals(maxCharge, battery.getCharge(stack), "plain battery getCharge returns full default");
        assertEquals(maxCharge, stack.getTag().getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG), "plain battery writes legacy charge key");

        HbmBatteryPackItem pack = requireBatteryPack(ModItems.BATTERY_REDSTONE.get(), "battery_redstone");
        ItemStack packStack = new ItemStack(pack);
        assertEquals(0L, pack.peekCharge(packStack), "battery pack runtime defaults empty without NBT");
        assertTrue(!packStack.hasTag(), "battery pack peekCharge must not create NBT");
        assertEquals(0L, pack.getCharge(packStack), "battery pack getCharge writes empty default");

        battery.setCharge(stack, maxCharge + 50L);
        assertEquals(60L, HbmBatteryTransfer.chargeItemsFromPower(stack, 10L, maxCharge + 1_000L),
                "overfull raw battery reverses helper charge by discharging");
        assertEquals(maxCharge, battery.getCharge(stack), "overfull raw battery discharged back toward max");

        battery.setCharge(stack, -20L);
        assertEquals(80L, HbmBatteryTransfer.chargePowerFromItem(stack, 100L, maxCharge + 200L),
                "underempty raw battery reverses helper discharge by charging");
        assertEquals(0L, battery.getCharge(stack), "underempty raw battery charged back toward zero");

        ItemStack creativeStack = new ItemStack(ModItems.BATTERY_CREATIVE.get());
        HbmBatteryTransfer.setCreativeBatteryPredicate(s -> ItemStack.isSameItemSameTags(s, creativeStack));
        try {
            assertEquals(0L, HbmBatteryTransfer.chargeItemsFromPower(creativeStack, 100L, 1_000L),
                    "creative battery swallows machine charge slot power");
            assertEquals(1_000L, HbmBatteryTransfer.chargePowerFromItem(creativeStack, 100L, 1_000L),
                    "creative battery fills machine discharge slot power");
        } finally {
            HbmBatteryTransfer.setCreativeBatteryPredicate(null);
        }
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void legacyBatteryDisplayListMatchesGasCentrifugeNeiOrder(GameTestHelper helper) {
        legacyBatteryDisplayListMatchesGasCentrifugeNeiOrder();
        helper.succeed();
    }

    private static void legacyBatteryDisplayListMatchesGasCentrifugeNeiOrder() {
        List<ItemStack> stacks = HbmLegacyBatteryMaps.legacyMachineRecipeBatteryDisplayStacks();
        int expectedSize = 3
                + HbmLegacyBatteryMaps.batteryPacksByLegacyMeta().size()
                + HbmLegacyBatteryMaps.selfChargingByLegacyMeta().size()
                + 1;
        assertEquals(expectedSize, stacks.size(), "legacy gas centrifuge battery display stack count");

        int index = 0;
        assertDisplayStack(stacks, index++, ModItems.BATTERY_POTATO, "battery_potato");
        assertDisplayStack(stacks, index++, ModItems.BATTERY_POTATOS, "battery_potatos");
        assertDisplayStack(stacks, index++, ModItems.ENERGY_CORE, "energy_core");
        for (RegistryObject<Item> item : HbmLegacyBatteryMaps.batteryPacksByLegacyMeta()) {
            assertDisplayStack(stacks, index++, item, "battery_pack legacy meta display");
        }
        for (RegistryObject<Item> item : HbmLegacyBatteryMaps.selfChargingByLegacyMeta()) {
            assertDisplayStack(stacks, index++, item, "battery_sc legacy meta display");
        }
        assertDisplayStack(stacks, index, ModItems.BATTERY_CREATIVE, "battery_creative");
    }

    private static void assertDisplayStack(List<ItemStack> stacks, int index, RegistryObject<Item> expected,
            String label) {
        ItemStack stack = stacks.get(index);
        if (stack.isEmpty()) {
            throw new AssertionError(label + ": display stack at index " + index + " is empty");
        }
        if (stack.getItem() != expected.get()) {
            throw new AssertionError(label + ": expected " + expected.getId() + " at index " + index
                    + " but got " + stack.getItem());
        }
        assertTrue(HbmInventoryMenuHelper.isLegacyBatteryItem(stack),
                label + ": display stack must be accepted by legacy machine battery slots");
    }

    private static HbmBatteryItem requireBattery(net.minecraft.world.item.Item item, String name) {
        if (item instanceof HbmBatteryItem battery) {
            return battery;
        }
        throw new AssertionError(name + " is not an HBM battery item");
    }

    private static HbmBatteryPackItem requireBatteryPack(net.minecraft.world.item.Item item, String name) {
        if (item instanceof HbmBatteryPackItem battery) {
            return battery;
        }
        throw new AssertionError(name + " is not an HBM battery pack item");
    }

    private static void forceSubscriptionLastSeen(HbmPowerNet net, long lastSeen) {
        forceMapValues(net, "providerEntries", lastSeen);
        forceMapValues(net, "receiverEntries", lastSeen);
    }

    @SuppressWarnings("unchecked")
    private static void forceMapValues(HbmPowerNet net, String fieldName, long value) {
        try {
            Field field = HbmPowerNet.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Map<Object, Long> entries = (Map<Object, Long>) field.get(net);
            for (Object key : entries.keySet()) {
                entries.put(key, value);
            }
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to force HbmPowerNet " + fieldName + " timestamps", exception);
        }
    }

    private static void assertEquals(long expected, long actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static final class TestProvider implements HbmEnergyProvider, HbmLoadedEnergy {
        private long power;
        private final long rate;
        private boolean loaded = true;

        private TestProvider(long power, long rate) {
            this.power = power;
            this.rate = rate;
        }

        @Override
        public long getPower() {
            return power;
        }

        @Override
        public void setPower(long power) {
            this.power = power;
        }

        @Override
        public long getMaxPower() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getProviderSpeed() {
            return rate;
        }

        @Override
        public boolean isEnergyLoaded() {
            return loaded;
        }
    }

    private static final class TestReceiver implements HbmEnergyReceiver, HbmLoadedEnergy {
        private long power;
        private final long maxPower;
        private final long rate;
        private final ConnectionPriority priority;
        private boolean loaded = true;

        private TestReceiver(long power, long maxPower, long rate, ConnectionPriority priority) {
            this.power = power;
            this.maxPower = maxPower;
            this.rate = rate;
            this.priority = priority;
        }

        @Override
        public long getPower() {
            return power;
        }

        @Override
        public void setPower(long power) {
            this.power = power;
        }

        @Override
        public long getMaxPower() {
            return maxPower;
        }

        @Override
        public long getReceiverSpeed() {
            return rate;
        }

        @Override
        public ConnectionPriority getPriority() {
            return priority;
        }

        @Override
        public boolean isEnergyLoaded() {
            return loaded;
        }
    }
}
