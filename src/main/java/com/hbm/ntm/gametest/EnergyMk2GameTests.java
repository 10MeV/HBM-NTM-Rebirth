package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
import com.hbm.ntm.blockentity.ArcFurnaceBlockEntity;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.blockentity.BatteryReddBlockEntity;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.blockentity.FensuBlockEntity;
import com.hbm.ntm.blockentity.ChungusBlockEntity;
import com.hbm.ntm.blockentity.HbmEnergyAndFluidBlockEntity;
import com.hbm.ntm.blockentity.HbmEnergyBlockEntity;
import com.hbm.ntm.blockentity.IndustrialSteamTurbineBlockEntity;
import com.hbm.ntm.blockentity.LargeLaunchPadBlockEntity;
import com.hbm.ntm.blockentity.LaunchPadBlockEntity;
import com.hbm.ntm.blockentity.LegacyLargeTurbineBlockEntity;
import com.hbm.ntm.blockentity.LiquefactorBlockEntity;
import com.hbm.ntm.blockentity.MachineBatteryBlockEntity;
import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.blockentity.PADetectorBlockEntity;
import com.hbm.ntm.blockentity.PADipoleBlockEntity;
import com.hbm.ntm.blockentity.PAQuadrupoleBlockEntity;
import com.hbm.ntm.blockentity.PARfcBlockEntity;
import com.hbm.ntm.blockentity.PASourceBlockEntity;
import com.hbm.ntm.blockentity.PneumaticTubeBlockEntity;
import com.hbm.ntm.blockentity.SolidifierBlockEntity;
import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.blockentity.StirlingBlockEntity;
import com.hbm.ntm.blockentity.TurbineGasBlockEntity;
import com.hbm.ntm.blockentity.TurbofanBlockEntity;
import com.hbm.ntm.blockentity.WoodBurnerBlockEntity;
import com.hbm.ntm.blockentity.CompactLauncherBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmChargeableItem;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmEnergyNodeHost;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmLegacyBatteryMaps;
import com.hbm.ntm.energy.HbmLoadedEnergy;
import com.hbm.ntm.energy.HbmPowerNet;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.RedCableBoxBlockItem;
import com.hbm.ntm.menu.MachineBatteryMenu;
import com.hbm.ntm.menu.MachineBatterySocketMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockPorts;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.turret.TurretArtyBlockEntity;
import com.hbm.ntm.turret.TurretBlockEntityBase;
import com.hbm.ntm.turret.TurretFritzBlockEntity;
import com.hbm.ntm.turret.TurretHimarsBlockEntity;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticItemAccess;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNetwork;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticReceiver;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticUtil;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.world.BlockMigrationHelper;
import com.hbm.ntm.world.LegacyItemStackMigration;
import com.hbm.ntm.world.LegacyWorldItemIdMap;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
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
        energyStorageReceiveRateRemainderStaysObservable();
        forgeEnergyBridgeIsOneToOneAndIntCapped();
    }

    public static void runAll() {
        powerNetDistributesLikeLegacyMk2();
        powerNetProviderRoundingDrainsExactlyTransferredPower();
        powerNetPrunesTimeoutAndUnloadedSubscribers();
        sendPowerDiodeKeepsLegacyUnclampedReceiverShape();
        energyStorageReceiveRateRemainderStaysObservable();
        forgeEnergyBridgeIsOneToOneAndIntCapped();
        batteryDefaultsAndLegacyTransferEdgesStayRaw();
        legacyBatteryMetaMappingsStaySingleSource();
        legacyBatteryDisplayListMatchesGasCentrifugeNeiOrder();
        legacyBatteryItemStackNbtMigrationPreservesCharge();
        legacyBatteryNumericItemStackNbtMigrationUsesWorldItemData();
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
    public static void energyStorageReceiveRateRemainderStaysObservable(GameTestHelper helper) {
        energyStorageReceiveRateRemainderStaysObservable();
        helper.succeed();
    }

    private static void energyStorageReceiveRateRemainderStaysObservable() {
        HbmEnergyStorage storage = new HbmEnergyStorage(100L, 50L, 100L);
        storage.setPower(80L);

        assertEquals(180L, storage.transferPower(200L),
                "storage remainder includes amount above receive rate plus filled capacity");
        assertEquals(100L, storage.getPower(), "storage accepted only available HE capacity");

        storage.setPower(80L);
        ForgeEnergyAdapter adapter = new ForgeEnergyAdapter(storage);
        assertEquals(20, adapter.receiveEnergy(200, false),
                "FE receive reports same accepted HE after rate and capacity caps");
        assertEquals(100L, storage.getPower(), "FE receive writes accepted HE without scaling");
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
    public static void legacyBatteryMetaMappingsStaySingleSource(GameTestHelper helper) {
        legacyBatteryMetaMappingsStaySingleSource();
        helper.succeed();
    }

    private static void legacyBatteryMetaMappingsStaySingleSource() {
        assertRegistryListEquals(HbmLegacyBatteryMaps.batteryPacksByLegacyMeta(), "battery_pack variants",
                ModItems.BATTERY_REDSTONE,
                ModItems.BATTERY_LEAD,
                ModItems.BATTERY_LITHIUM,
                ModItems.BATTERY_SODIUM,
                ModItems.BATTERY_SCHRABIDIUM,
                ModItems.BATTERY_QUANTUM,
                ModItems.CAPACITOR_COPPER,
                ModItems.CAPACITOR_GOLD,
                ModItems.CAPACITOR_NIOBIUM,
                ModItems.CAPACITOR_TANTALUM,
                ModItems.CAPACITOR_BISMUTH,
                ModItems.CAPACITOR_SPARK);
        assertRegistryListEquals(HbmLegacyBatteryMaps.selfChargingByLegacyMeta(), "battery_sc variants",
                ModItems.BATTERY_SC_EMPTY,
                ModItems.BATTERY_SC_WASTE,
                ModItems.BATTERY_SC_RA226,
                ModItems.BATTERY_SC_TC99,
                ModItems.BATTERY_SC_CO60,
                ModItems.BATTERY_SC_PU238,
                ModItems.BATTERY_SC_PO210,
                ModItems.BATTERY_SC_AU198,
                ModItems.BATTERY_SC_PB209,
                ModItems.BATTERY_SC_AM241);

        assertSame(ModItems.BATTERY_QUANTUM, HbmLegacyBatteryMaps.batteryPackByLegacyMeta(5)
                        .orElseThrow(() -> new AssertionError("missing battery_pack meta 5")),
                "battery_pack meta 5 quantum mapping");
        assertSame(ModItems.BATTERY_SC_RA226, HbmLegacyBatteryMaps.selfChargingByLegacyMeta(2)
                        .orElseThrow(() -> new AssertionError("missing battery_sc meta 2")),
                "battery_sc meta 2 RA226 mapping");
        assertSame(ModItems.BATTERY_QUANTUM,
                LegacyMetaItemMappings.requireItem(LegacyMetaItemMappings.BATTERY_PACK, 5),
                "legacy meta requireItem quantum");
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

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void legacyBatteryItemStackNbtMigrationPreservesCharge(GameTestHelper helper) {
        legacyBatteryItemStackNbtMigrationPreservesCharge();
        helper.succeed();
    }

    private static void legacyBatteryItemStackNbtMigrationPreservesCharge() {
        CompoundTag quantum = legacyStackTag("hbm:battery_pack", 5, 1);
        CompoundTag quantumData = new CompoundTag();
        quantumData.putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, 1234L);
        quantum.put("tag", quantumData);
        assertTrue(LegacyItemStackMigration.migrateItemStackTag(quantum), "legacy quantum battery stack migrated");
        assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(), quantum.getString("id"),
                "legacy battery_pack meta 5 id");
        assertTrue(!quantum.contains("Damage"), "legacy battery_pack Damage removed after split-id migration");
        assertEquals(1234L, quantum.getCompound("tag").getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG),
                "legacy charge NBT preserved");
        assertEquals(1, quantum.getByte("Count"), "legacy Count preserved");

        CompoundTag ra226 = legacyStackTag("hbm:item.battery_sc", 2, 1);
        assertTrue(LegacyItemStackMigration.migrateItemStackTag(ra226), "legacy item.battery_sc stack migrated");
        assertEquals(ModItems.BATTERY_SC_RA226.getId().toString(), ra226.getString("id"),
                "legacy battery_sc meta 2 id");

        CompoundTag unknownMeta = legacyStackTag("hbm:battery_pack", 99, 1);
        assertTrue(!LegacyItemStackMigration.migrateItemStackTag(unknownMeta), "unknown battery_pack meta no-op");
        assertEquals("hbm:battery_pack", unknownMeta.getString("id"), "unknown meta keeps legacy id");
        assertEquals(99, unknownMeta.getShort("Damage"), "unknown meta keeps Damage");

        CompoundTag root = new CompoundTag();
        ListTag items = new ListTag();
        items.add(legacyStackTag("battery_pack", 1, 4));
        items.add(legacyStackTag("minecraft:stone", 0, 64));
        root.put("Items", items);
        assertEquals(1, LegacyItemStackMigration.migrateAll(root), "recursive migration count");
        assertEquals(ModItems.BATTERY_LEAD.getId().toString(), items.getCompound(0).getString("id"),
                "recursive legacy stack id");
        assertEquals("minecraft:stone", items.getCompound(1).getString("id"), "recursive non-HBM stack unchanged");
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void legacyBatteryNumericItemStackNbtMigrationUsesWorldItemData(GameTestHelper helper) {
        legacyBatteryNumericItemStackNbtMigrationUsesWorldItemData();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void energyNodespaceChunkUnloadAndReplacementRebuildNetworks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
            int chunkStartX = anchor.getX() & ~15;
            int chunkStartZ = anchor.getZ() & ~15;
            BlockPos left = new BlockPos(chunkStartX + 15, anchor.getY(), chunkStartZ + 8);
            BlockPos right = left.east();
            ChunkPos leftChunk = new ChunkPos(left);

            HbmEnergyNode leftNode = new HbmEnergyNode(left, Set.of(Direction.EAST));
            HbmEnergyNode rightNode = new HbmEnergyNode(right, Set.of(Direction.WEST));
            HbmEnergyNodespace.createNode(level, leftNode);
            HbmEnergyNodespace.createNode(level, rightNode);

            HbmEnergyNodespace.Diagnostics joined = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, joined.uniqueNodes(), "cross-chunk nodes joined unique count");
            assertEquals(1, joined.networks(), "cross-chunk nodes joined network count");
            assertEquals(2, joined.linkRefs(), "cross-chunk nodes joined link refs");

            HbmEnergyNodespace.unloadChunk(level, leftChunk);
            HbmEnergyNodespace.Diagnostics afterUnload = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(1, afterUnload.uniqueNodes(), "chunk unload removes node in unloaded chunk");
            assertEquals(0, afterUnload.networks(), "chunk unload destroys old net like UniNodespace.popNode");
            assertEquals(1, afterUnload.dirtyNodes(), "remaining node marked dirty after neighbor unload");
            assertEquals(1, afterUnload.orphanNodes(), "remaining node orphaned before rebuild");
            assertTrue(leftNode.isExpired(), "unloaded node marked expired");
            assertTrue(!rightNode.isExpired(), "remaining node kept alive");

            HbmEnergyNodespace.ForceRebuildResult rebuild = HbmEnergyNodespace.forceRebuild(level);
            assertEquals(1, rebuild.nodes(), "force rebuild keeps remaining node");
            HbmEnergyNodespace.Diagnostics rebuilt = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(1, rebuilt.networks(), "remaining node rebuilt into a valid net");
            assertEquals(1, rebuilt.linkRefs(), "rebuilt net has one link");
            assertEquals(0, rebuilt.orphanNodes(), "rebuilt node no longer orphaned");

            HbmEnergyNode replacement = new HbmEnergyNode(right, Set.of(Direction.WEST));
            HbmEnergyNodespace.createNode(level, replacement);
            HbmEnergyNodespace.Diagnostics afterReplacement = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(rightNode.isExpired(), "replaced node marked expired");
            assertEquals(1, afterReplacement.uniqueNodes(), "replacement keeps one live node at the position");
            assertEquals(1, afterReplacement.networks(), "replacement rebuilds a valid one-node network");
            assertEquals(1, afterReplacement.linkRefs(), "replacement network has one live link");
            assertEquals(0, afterReplacement.expiredNodes(), "expired replaced node removed from nodespace");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void energyNodespaceForgeChunkUnloadEventPrunesRealNodesAndRebuilds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
            int chunkStartX = anchor.getX() & ~15;
            int chunkStartZ = anchor.getZ() & ~15;
            BlockPos left = new BlockPos(chunkStartX + 15, anchor.getY(), chunkStartZ + 8);
            BlockPos right = left.east();

            level.setBlock(left, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(right, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, left);
            refreshEnergyNodeAt(level, right);
            HbmEnergyNode leftNode = requireEnergyNodeAt(level, left);
            HbmEnergyNode rightNode = requireEnergyNodeAt(level, right);

            HbmEnergyNodespace.Diagnostics joined = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, joined.uniqueNodes(), "Forge chunk-unload event real red_cable joined nodes");
            assertEquals(1, joined.networks(), "Forge chunk-unload event real red_cable joined network");
            assertEquals(2, joined.linkRefs(), "Forge chunk-unload event real red_cable joined links");

            assertTrue(!MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload(level.getChunk(left))),
                    "Forge chunk unload event is not cancellable");
            HbmEnergyNodespace.Diagnostics afterUnload = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(leftNode.isExpired(), "Forge chunk unload event expires left real red_cable node");
            assertTrue(!rightNode.isExpired(), "Forge chunk unload event keeps right real red_cable node alive");
            assertEquals(1, afterUnload.uniqueNodes(), "Forge chunk unload event removes unloaded chunk node");
            assertEquals(0, afterUnload.networks(), "Forge chunk unload event destroys old joined network");
            assertEquals(1, afterUnload.dirtyNodes(), "Forge chunk unload event marks surviving node dirty");
            assertEquals(1, afterUnload.orphanNodes(), "Forge chunk unload event leaves surviving node orphaned before rebuild");

            HbmEnergyNodespace.ForceRebuildResult rebuild = HbmEnergyNodespace.forceRebuild(level);
            assertEquals(1, rebuild.nodes(), "Forge chunk unload event force rebuild keeps surviving node");
            HbmEnergyNodespace.Diagnostics rebuilt = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(1, rebuilt.networks(), "Forge chunk unload event rebuild restores a one-node network");
            assertEquals(1, rebuilt.linkRefs(), "Forge chunk unload event rebuild restores one live link");
            assertEquals(0, rebuilt.orphanNodes(), "Forge chunk unload event rebuild clears orphan state");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void energyNodespaceLevelUnloadClearsRealNodesAndAllowsRebuild(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos left = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos right = left.east();
            level.setBlock(left, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(right, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, left);
            refreshEnergyNodeAt(level, right);
            HbmEnergyNode leftNode = requireEnergyNodeAt(level, left);
            HbmEnergyNode rightNode = requireEnergyNodeAt(level, right);

            HbmEnergyNodespace.Diagnostics joined = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, joined.uniqueNodes(), "level-unload real red_cable joined nodes");
            assertEquals(1, joined.networks(), "level-unload real red_cable joined network");
            assertEquals(2, joined.linkRefs(), "level-unload real red_cable joined links");

            HbmEnergyNodespace.unloadLevel(level);
            HbmEnergyNodespace.Diagnostics unloaded = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(leftNode.isExpired(), "level unload expires left real red_cable node");
            assertTrue(rightNode.isExpired(), "level unload expires right real red_cable node");
            assertEquals(0, unloaded.uniqueNodes(), "level unload clears all energy nodes");
            assertEquals(0, unloaded.networks(), "level unload clears all energy networks");
            assertEquals(0, unloaded.linkRefs(), "level unload clears all network links");
            assertTrue(HbmEnergyNodespace.getNode(level, left) == null,
                    "level unload removes left position from nodespace");
            assertTrue(HbmEnergyNodespace.getNode(level, right) == null,
                    "level unload removes right position from nodespace");

            refreshEnergyNodeAt(level, left);
            refreshEnergyNodeAt(level, right);
            HbmEnergyNode rebuiltLeft = requireEnergyNodeAt(level, left);
            HbmEnergyNode rebuiltRight = requireEnergyNodeAt(level, right);
            HbmEnergyNodespace.Diagnostics rebuilt = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(rebuiltLeft != leftNode, "level unload rebuild creates a fresh left node");
            assertTrue(rebuiltRight != rightNode, "level unload rebuild creates a fresh right node");
            assertEquals(2, rebuilt.uniqueNodes(), "level unload rebuild restores real red_cable nodes");
            assertEquals(1, rebuilt.networks(), "level unload rebuild restores real red_cable network");
            assertEquals(2, rebuilt.linkRefs(), "level unload rebuild restores real red_cable links");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void energyNodespaceForgeLevelUnloadEventClearsRealNodesAndAllowsRebuild(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos left = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos right = left.east();
            level.setBlock(left, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(right, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, left);
            refreshEnergyNodeAt(level, right);
            HbmEnergyNode leftNode = requireEnergyNodeAt(level, left);
            HbmEnergyNode rightNode = requireEnergyNodeAt(level, right);

            HbmEnergyNodespace.Diagnostics joined = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, joined.uniqueNodes(), "Forge level-unload event real red_cable joined nodes");
            assertEquals(1, joined.networks(), "Forge level-unload event real red_cable joined network");
            assertEquals(2, joined.linkRefs(), "Forge level-unload event real red_cable joined links");

            assertTrue(!MinecraftForge.EVENT_BUS.post(new LevelEvent.Unload(level)),
                    "Forge level unload event is not cancellable");
            HbmEnergyNodespace.Diagnostics unloaded = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(leftNode.isExpired(), "Forge level unload event expires left real red_cable node");
            assertTrue(rightNode.isExpired(), "Forge level unload event expires right real red_cable node");
            assertEquals(0, unloaded.uniqueNodes(), "Forge level unload event clears all energy nodes");
            assertEquals(0, unloaded.networks(), "Forge level unload event clears all energy networks");
            assertEquals(0, unloaded.linkRefs(), "Forge level unload event clears all network links");

            refreshEnergyNodeAt(level, left);
            refreshEnergyNodeAt(level, right);
            HbmEnergyNode rebuiltLeft = requireEnergyNodeAt(level, left);
            HbmEnergyNode rebuiltRight = requireEnergyNodeAt(level, right);
            HbmEnergyNodespace.Diagnostics rebuilt = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(rebuiltLeft != leftNode, "Forge level unload event rebuild creates a fresh left node");
            assertTrue(rebuiltRight != rightNode, "Forge level unload event rebuild creates a fresh right node");
            assertEquals(2, rebuilt.uniqueNodes(), "Forge level unload event rebuild restores real red_cable nodes");
            assertEquals(1, rebuilt.networks(), "Forge level unload event rebuild restores real red_cable network");
            assertEquals(2, rebuilt.linkRefs(), "Forge level unload event rebuild restores real red_cable links");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void realRedCableBreakRebuildsNodespace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
            int chunkStartX = anchor.getX() & ~15;
            int chunkStartZ = anchor.getZ() & ~15;
            BlockPos left = new BlockPos(chunkStartX + 15, anchor.getY(), chunkStartZ + 8);
            BlockPos right = left.east();

            level.setBlock(left, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(right, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, left);
            refreshEnergyNodeAt(level, right);

            HbmEnergyNodespace.Diagnostics joined = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, joined.uniqueNodes(), "real red_cable joined unique node count");
            assertEquals(1, joined.networks(), "real red_cable joined network count");
            assertEquals(2, joined.linkRefs(), "real red_cable joined link refs");

            level.setBlock(left, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            HbmEnergyNodespace.tick(level);
            HbmEnergyNodespace.Diagnostics afterBreak = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(1, afterBreak.uniqueNodes(), "breaking one real red_cable leaves one node");
            assertEquals(1, afterBreak.networks(), "remaining real red_cable rebuilds a one-node network");
            assertEquals(1, afterBreak.linkRefs(), "remaining real red_cable has one link");
            assertEquals(0, afterBreak.orphanNodes(), "remaining real red_cable is no longer orphaned after tick");

            level.setBlock(right, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            HbmEnergyNodespace.tick(level);
            HbmEnergyNodespace.Diagnostics empty = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(0, empty.uniqueNodes(), "all real red_cable nodes removed");
            assertEquals(0, empty.networks(), "all real red_cable networks removed");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2CableRender")
    public static void redCableBoxLegacyVariantPlacementAndDrops(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            if (!(ModBlocks.RED_CABLE_BOX.get().asItem() instanceof RedCableBoxBlockItem boxItem)) {
                throw new AssertionError("red_cable_box must use RedCableBoxBlockItem for legacy size variants");
            }

            var player = FakePlayerFactory.getMinecraft(level);
            player.getInventory().clearContent();
            for (int variant = 0; variant < 5; variant++) {
                BlockPos supportPos = helper.absolutePos(new BlockPos(2 + variant * 2, 1, 2));
                BlockPos placedPos = supportPos.above();
                level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
                ItemStack stack = RedCableBoxBlockItem.createStack(boxItem, variant);
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);

                var placeResult = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                        blockHit(supportPos)));
                assertTrue(placeResult.consumesAction(),
                        "red_cable_box legacy variant " + variant + " player useOn consumes action");
                BlockState placed = level.getBlockState(placedPos);
                assertTrue(placed.is(ModBlocks.RED_CABLE_BOX.get()),
                        "red_cable_box legacy variant " + variant + " places the box cable block");
                assertEquals(variant, placed.getValue(RedCableBoxBlock.SIZE),
                        "red_cable_box legacy variant " + variant + " maps to size state");

                List<ItemStack> drops = Block.getDrops(placed, level, placedPos, level.getBlockEntity(placedPos));
                assertEquals(1, drops.size(), "red_cable_box legacy variant " + variant + " has one block drop");
                ItemStack drop = drops.get(0);
                assertSame(ModBlocks.RED_CABLE_BOX.get().asItem(), drop.getItem(),
                        "red_cable_box legacy variant " + variant + " drops the same item");
                assertEquals(variant, drop.getOrCreateTag().getInt(LegacyStateBlockItem.TAG_VARIANT),
                        "red_cable_box legacy variant " + variant + " drop keeps hbmLegacyVariant");
            }
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatteryBufferModeCreatesAndRemovesRealNode(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos cablePos = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos batteryPos = cablePos.east();
            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, cablePos);
            MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);

            HbmEnergyNodespace.Diagnostics initial = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(1, initial.uniqueNodes(), "machine_battery input mode does not create self node");

            battery.cycleRedLowMode();
            tickMachineBattery(level, batteryPos, battery);
            HbmEnergyNodespace.tick(level);
            HbmEnergyNodespace.Diagnostics buffer = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(battery.getEnergyNode() != null && !battery.getEnergyNode().isExpired(),
                    "machine_battery buffer mode creates self node");
            assertEquals(2, buffer.uniqueNodes(), "machine_battery buffer plus red cable node count");
            assertEquals(1, buffer.networks(), "machine_battery buffer joins red cable network");
            assertEquals(2, buffer.linkRefs(), "machine_battery buffer network links");
            assertEquals(1, buffer.providerEntries(), "machine_battery buffer subscribes as provider");
            assertEquals(1, buffer.receiverEntries(), "machine_battery buffer subscribes as receiver");

            battery.cycleRedLowMode();
            tickMachineBattery(level, batteryPos, battery);
            HbmEnergyNodespace.tick(level);
            HbmEnergyNodespace.Diagnostics output = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(battery.getEnergyNode() == null || battery.getEnergyNode().isExpired(),
                    "machine_battery output mode removes self node");
            assertEquals(1, output.uniqueNodes(), "machine_battery output mode leaves only red cable node");
            assertEquals(1, output.networks(), "red cable network survives machine_battery mode switch");
            assertEquals(1, output.linkRefs(), "red cable network has one remaining link");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketFourFacingLegacyPortShapes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
            Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            for (int i = 0; i < facings.length; i++) {
                Direction facing = facings[i];
                BlockPos pos = anchor.offset(i * 6, 0, 0);
                BlockState state = ModBlocks.MACHINE_BATTERY_SOCKET.get()
                        .defaultBlockState()
                        .setValue(MachineBatterySocketBlock.FACING, facing);
                level.setBlock(pos, state, Block.UPDATE_ALL);
                MachineBatterySocketBlockEntity socket = requireMachineBatterySocket(level, pos);
                MachineBatterySocketBlockEntity.serverTick(level, pos, state, socket);

                HbmEnergyNode node = socket.getEnergyNode();
                assertTrue(node != null && !node.isExpired(), "battery_socket node exists for " + facing);
                assertBlockPosSetEquals(expectedSocketPositions(pos, facing), node.getPositions(),
                        "battery_socket positions for " + facing);
                assertStringSetEquals(expectedSocketConnectionKeys(pos, facing), connectionKeys(node),
                        "battery_socket connection points for " + facing);
            }
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketRemotePortSubscribesRealCableNetwork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos corePos = helper.absolutePos(new BlockPos(2, 2, 2));
            Direction facing = Direction.SOUTH;
            Direction remoteDirection = facing.getOpposite();
            BlockPos cablePos = corePos.relative(remoteDirection, 2);
            for (BlockPos footprintPos : expectedSocketPositions(corePos, facing)) {
                level.setBlock(footprintPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            level.setBlock(cablePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

            BlockState state = ModBlocks.MACHINE_BATTERY_SOCKET.get()
                    .defaultBlockState()
                    .setValue(MachineBatterySocketBlock.FACING, facing);
            level.setBlock(corePos, state, Block.UPDATE_ALL);
            state.getBlock().setPlacedBy(level, corePos, state, null, ItemStack.EMPTY);
            MachineBatterySocketBlockEntity socket = requireMachineBatterySocket(level, corePos);

            ItemStack batteryStack = chargedBatteryStack(0L);
            HbmChargeableItem batteryItem = requireChargeable(batteryStack, "battery_redstone");
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, batteryStack);

            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, cablePos);
            MachineBatterySocketBlockEntity.serverTick(level, corePos, state, socket);
            HbmEnergyNodespace.tick(level);
            MachineBatterySocketBlockEntity.serverTick(level, corePos, state, socket);

            HbmEnergyNode socketNode = requireEnergyNodeAt(level, corePos);
            HbmEnergyNode cableNode = requireEnergyNodeAt(level, cablePos);
            BlockPos proxyPos = corePos.relative(remoteDirection);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "battery_socket expected proxy dummy at " + proxyPos + " but found "
                            + level.getBlockState(proxyPos).getBlock());
            MultiblockDummyBlockEntity proxy = (MultiblockDummyBlockEntity) level.getBlockEntity(proxyPos);
            assertTrue(proxy.getProxyMode().isProxy() && proxy.getProxyMode().power() && proxy.getProxyMode().conductor(),
                    "battery_socket remote proxy carries power/conductor flags");
            assertTrue(proxy.canConnectEnergy(remoteDirection),
                    "battery_socket remote proxy accepts red_cable side " + remoteDirection);
            assertTrue(connectionKeys(socketNode).contains(connectionKey(cablePos, remoteDirection)),
                    "battery_socket remote node exposes legacy getConPos port at real red_cable: "
                            + connectionKeys(socketNode));
            assertTrue(connectionKeys(cableNode).contains(connectionKey(corePos.relative(remoteDirection), facing)),
                    "red_cable connects back toward battery_socket dummy proxy: " + connectionKeys(cableNode));

            HbmEnergyNodespace.Diagnostics input = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, input.uniqueNodes(), "battery_socket remote port plus red_cable node count");
            assertEquals(1, input.networks(), "battery_socket remote port joins red_cable network");
            assertEquals(2, input.linkRefs(), "battery_socket remote port network links");
            assertEquals(1, input.receiverEntries(), "battery_socket input mode subscribes as receiver through remote port");
            assertEquals(0, input.providerEntries(), "battery_socket input mode does not subscribe as provider");

            batteryItem.setCharge(batteryStack, batteryItem.getMaxCharge(batteryStack));
            cycleSocketRedLowToMode(socket, MachineBatterySocketBlockEntity.MODE_OUTPUT);
            MachineBatterySocketBlockEntity.serverTick(level, corePos, state, socket);
            HbmEnergyNodespace.tick(level);
            MachineBatterySocketBlockEntity.serverTick(level, corePos, state, socket);

            HbmEnergyNodespace.Diagnostics output = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, output.uniqueNodes(), "battery_socket output mode keeps remote-port cable nodes");
            assertEquals(1, output.networks(), "battery_socket output mode keeps joined remote-port network");
            assertEquals(0, output.receiverEntries(), "battery_socket output mode removes remote receiver subscription");
            assertEquals(1, output.providerEntries(), "battery_socket output mode subscribes as provider through remote port");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void playerUseOnPlacesBatterySocketRemotePortAndMenus(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos corePos = helper.absolutePos(new BlockPos(4, 2, 4));
            BlockPos supportPos = corePos.below();
            level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

            var player = FakePlayerFactory.getMinecraft(level);
            player.setPos(corePos.getX() + 0.5D, corePos.getY() + 0.5D, corePos.getZ() + 2.5D);
            player.setYRot(180.0F);
            player.setXRot(0.0F);
            player.getInventory().clearContent();
            ItemStack socketStack = new ItemStack(ModBlocks.MACHINE_BATTERY_SOCKET.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, socketStack);

            var placeResult = socketStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    blockHit(supportPos)));
            assertTrue(placeResult.consumesAction(), "player useOn placement consumes the socket block item action");
            assertTrue(level.getBlockState(corePos).is(ModBlocks.MACHINE_BATTERY_SOCKET.get()),
                    "player useOn places the battery_socket core at the clicked support top");

            BlockState socketState = level.getBlockState(corePos);
            Direction facing = socketState.getValue(MachineBatterySocketBlock.FACING);
            MachineBatterySocketBlockEntity socket = requireMachineBatterySocket(level, corePos);
            List<BlockPos> footprint = socketFootprintPositions(corePos, facing);
            assertSocketFootprintProxiesResolve(level, socket, footprint);
            MachineBatterySocketBlockEntity.serverTick(level, corePos, socketState, socket);

            HbmEnergyNode socketNode = requireEnergyNodeAt(level, corePos);
            assertBlockPosSetEquals(expectedSocketPositions(corePos, facing), socketNode.getPositions(),
                    "player-placed battery_socket keeps the legacy 2x2 node footprint");
            assertStringSetEquals(expectedSocketConnectionKeys(corePos, facing), connectionKeys(socketNode),
                    "player-placed battery_socket exposes the legacy remote connection points");

            Direction remoteDirection = facing.getOpposite();
            BlockPos proxyPos = corePos.relative(remoteDirection);
            BlockPos cablePos = corePos.relative(remoteDirection, 2);
            ItemStack batteryStack = chargedBatteryStack(0L);
            HbmChargeableItem batteryItem = requireChargeable(batteryStack, "battery_redstone");
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, batteryStack);

            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, cablePos);
            socket.refreshEnergyNode();
            MachineBatterySocketBlockEntity.serverTick(level, corePos, socketState, socket);
            HbmEnergyNodespace.tick(level);
            MachineBatterySocketBlockEntity.serverTick(level, corePos, socketState, socket);

            HbmEnergyNodespace.Diagnostics input = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, input.uniqueNodes(), "player-placed socket remote port plus red_cable node count");
            assertEquals(1, input.networks(), "player-placed socket remote port joins the red_cable network");
            assertEquals(1, input.receiverEntries(), "player-placed socket input mode subscribes through remote port");
            assertEquals(0, input.providerEntries(), "player-placed socket input mode does not subscribe as provider");

            batteryItem.setCharge(batteryStack, batteryItem.getMaxCharge(batteryStack));
            cycleSocketRedLowToMode(socket, MachineBatterySocketBlockEntity.MODE_OUTPUT);
            MachineBatterySocketBlockEntity.serverTick(level, corePos, socketState, socket);
            HbmEnergyNodespace.tick(level);
            MachineBatterySocketBlockEntity.serverTick(level, corePos, socketState, socket);

            HbmEnergyNodespace.Diagnostics output = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(0, output.receiverEntries(), "player-placed socket output mode clears remote receiver entry");
            assertEquals(1, output.providerEntries(), "player-placed socket output mode subscribes through remote port");

            var coreResult = socketState.use(level, player, InteractionHand.MAIN_HAND, blockHit(corePos));
            assertTrue(coreResult.consumesAction(), "player-placed socket core right-click is consumed");
            assertTrue(player.containerMenu instanceof MachineBatterySocketMenu,
                    "player-placed socket core opens the socket menu");
            assertSame(socket, ((MachineBatterySocketMenu) player.containerMenu).getBlockEntity(),
                    "player-placed socket core menu targets the core block entity");
            player.closeContainer();

            BlockState proxyState = level.getBlockState(proxyPos);
            var proxyResult = proxyState.use(level, player, InteractionHand.MAIN_HAND, blockHit(proxyPos));
            assertTrue(proxyResult.consumesAction(), "player-placed socket proxy right-click is consumed");
            assertTrue(player.containerMenu instanceof MachineBatterySocketMenu,
                    "player-placed socket proxy opens the socket menu through DummyBlock forwarding");
            assertSame(socket, ((MachineBatterySocketMenu) player.containerMenu).getBlockEntity(),
                    "player-placed socket proxy menu resolves to the same core block entity");
            player.closeContainer();
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketSelfChargingStateMatchesLegacyNbtTick(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
            BlockState state = ModBlocks.MACHINE_BATTERY_SOCKET.get()
                    .defaultBlockState()
                    .setValue(MachineBatterySocketBlock.FACING, Direction.SOUTH);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            MachineBatterySocketBlockEntity socket = requireMachineBatterySocket(level, pos);
            ItemStack selfCharging = new ItemStack(ModItems.BATTERY_SC_RA226.get());
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, selfCharging);

            CompoundTag legacyMissingMultiplier = socket.saveWithoutMetadata();
            legacyMissingMultiplier.remove("damageTimer");
            legacyMissingMultiplier.remove("damageTarget");
            legacyMissingMultiplier.remove("scPowerMult");
            socket.load(legacyMissingMultiplier);

            CompoundTag loaded = socket.saveWithoutMetadata();
            assertEquals(0, loaded.getInt("damageTimer"), "missing legacy damageTimer loads as zero");
            assertEquals(0, loaded.getInt("damageTarget"), "missing legacy damageTarget loads as zero");
            assertTrue(loaded.getDouble("scPowerMult") == 0.0D,
                    "missing legacy scPowerMult loads as raw 0.0 before the first loaded tick");

            MachineBatterySocketBlockEntity.serverTick(level, pos, state, socket);
            CompoundTag firstTick = socket.saveWithoutMetadata();
            int target = firstTick.getInt("damageTarget");
            double multiplier = firstTick.getDouble("scPowerMult");
            assertEquals(1, firstTick.getInt("damageTimer"),
                    "first loaded self-charging tick picks target then increments timer");
            assertTrue(target >= 1200 && target < 3600,
                    "self-charging damageTarget keeps legacy 1200..3599 tick window: " + target);
            assertTrue(multiplier >= 0.1D && multiplier <= 1.0D,
                    "first loaded self-charging tick clamps scPowerMult into legacy range: " + multiplier);
            assertTrue(socket.getPower() > 0L && socket.getPower() <= 200L,
                    "loaded RA226 socket power is scaled by clamped scPowerMult");

            ItemStack removed = socket.removeBatteryForDrop();
            MachineBatterySocketBlockEntity.serverTick(level, pos, state, socket);
            CompoundTag withoutBattery = socket.saveWithoutMetadata();
            assertEquals(1, withoutBattery.getInt("damageTimer"),
                    "removing battery does not clear legacy damageTimer");
            assertEquals(target, withoutBattery.getInt("damageTarget"),
                    "removing battery does not clear legacy damageTarget");
            assertTrue(withoutBattery.getDouble("scPowerMult") == multiplier,
                    "removing battery does not fluctuate or clear legacy scPowerMult");

            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, removed);
            MachineBatterySocketBlockEntity.serverTick(level, pos, state, socket);
            CompoundTag reinserted = socket.saveWithoutMetadata();
            assertEquals(2, reinserted.getInt("damageTimer"),
                    "reinserting loaded battery resumes legacy damageTimer");
            assertEquals(target, reinserted.getInt("damageTarget"),
                    "reinserting loaded battery keeps the existing legacy damageTarget");
            double reinsertedMultiplier = reinserted.getDouble("scPowerMult");
            assertTrue(reinsertedMultiplier >= 0.1D && reinsertedMultiplier <= 1.0D,
                    "reinserted self-charging tick keeps scPowerMult clamped: " + reinsertedMultiplier);
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsArc")
    public static void arcFurnaceRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 78);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos arcPortCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 78);
        BlockPos arcPos = arcPortCablePos.east(3).north();
        long startingPower = 100_000L;
        forceLoadedChunks(level, batteryPos, arcPos);
        level.removeBlock(batteryPos, false);
        level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        while (battery.getRedLow() != MachineBatteryBlockEntity.MODE_OUTPUT) {
            battery.cycleRedLowMode();
        }
        battery.setPower(startingPower);

        for (int x = firstCablePos.getX(); x <= arcPortCablePos.getX(); x++) {
            BlockPos cablePos = new BlockPos(x, firstCablePos.getY(), firstCablePos.getZ());
            level.removeBlock(cablePos, false);
            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        for (int x = firstCablePos.getX(); x <= arcPortCablePos.getX(); x++) {
            BlockPos cablePos = new BlockPos(x, firstCablePos.getY(), firstCablePos.getZ());
            refreshEnergyNodeAt(level, cablePos);
        }

        BlockState arcState = ModBlocks.MACHINE_ARC_FURNACE.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(arcPos, false);
        level.setBlock(arcPos, arcState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(arcPos) instanceof ArcFurnaceBlockEntity arcFurnace)) {
            throw new AssertionError("No arc_furnace block entity at " + arcPos);
        }

        HbmEnergyNodespace.tick(level);
        HbmEnergyNodespace.Diagnostics joined = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(joined.uniqueNodes() >= 4, "arc_furnace cross-chunk red_cable line node count");
        assertTrue(joined.networks() >= 1, "arc_furnace cross-chunk red_cable line joined network");

        HbmEnergyUtil.PortSetSnapshot arcPorts = arcFurnace.inspectEnergyPorts();
        assertEquals(6, arcPorts.totalPorts(), "arc_furnace exposes six legacy remote energy ports");
        assertTrue(arcPorts.networkedPorts() >= 1,
                "arc_furnace remote port sees the cross-chunk red_cable network");
        assertOutputBatteryProviderSubscribes(level, batteryPos, arcPortCablePos, battery,
                "machine_battery output provider subscribes to adjacent cable network");
        assertTrue(HbmEnergyUtil.subscribeReceiverToNetwork(level, arcPortCablePos, Direction.EAST,
                arcFurnace.getEnergyStorage()), "arc_furnace receiver subscribes through remote port cable");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, arcPortCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), "arc_furnace port cable has a valid power net");
        powerNet.update();

        assertTrue(arcFurnace.getPower() > 0L,
                "arc_furnace remote port received HE from cross-chunk red_cable network");
        assertTrue(battery.getPower() < startingPower,
                "machine_battery output spent HE into arc_furnace cross-chunk network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.receiverEntries() >= 1,
                "arc_furnace remote port registered as receiver on cable network");
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery output registered as provider on cable network");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsSolidifier")
    public static void solidifierRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 68);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 68);
        BlockPos machinePos = portCablePos.west(2).below();
        forceLoadedChunks(level, batteryPos, machinePos);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, ModBlocks.MACHINE_SOLIDIFIER.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof SolidifierBlockEntity solidifier)) {
            throw new AssertionError("No solidifier block entity at " + machinePos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        SolidifierBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), solidifier);
        assertFixedRemoteReceiverReceivesPower(level, batteryPos, portCablePos, solidifier,
                "solidifier", 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsLiquefactor")
    public static void liquefactorRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 66);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 66);
        BlockPos machinePos = portCablePos.west(2).below();
        forceLoadedChunks(level, batteryPos, machinePos);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, ModBlocks.MACHINE_LIQUEFACTOR.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof LiquefactorBlockEntity liquefactor)) {
            throw new AssertionError("No liquefactor block entity at " + machinePos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        LiquefactorBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), liquefactor);
        assertFixedRemoteReceiverReceivesPower(level, batteryPos, portCablePos, liquefactor,
                "liquefactor", 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsAssembly")
    public static void assemblyMachineRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 70);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 70);
        BlockPos machinePos = portCablePos.west(2);
        BlockState machineState = ModBlocks.MACHINE_ASSEMBLY_MACHINE.get().defaultBlockState();
        forceLoadedChunks(level, batteryPos, machinePos);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, machineState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof AssemblyMachineBlockEntity assembler)) {
            throw new AssertionError("No assembly_machine block entity at " + machinePos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertXrFloorRingRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos,
                assembler.getEnergyStorage(),
                "assembly_machine", 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsChemicalPlant")
    public static void chemicalPlantRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 74);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 74);
        BlockPos machinePos = portCablePos.west(2);
        BlockState machineState = ModBlocks.MACHINE_CHEMICAL_PLANT.get().defaultBlockState();
        forceLoadedChunks(level, batteryPos, machinePos);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, machineState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof ChemicalPlantBlockEntity chemicalPlant)) {
            throw new AssertionError("No chemical_plant block entity at " + machinePos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertXrFloorRingRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos,
                chemicalPlant.getEnergyStorage(),
                "chemical_plant", 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsSteamEngine")
    public static void steamEngineRemotePortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 76);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 76);
        BlockPos machinePos = portCablePos.east(2).below();
        forceLoadedChunks(level, batteryPos, machinePos);
        BlockState machineState = ModBlocks.MACHINE_STEAM_ENGINE.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, machineState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof SteamEngineBlockEntity steamEngine)) {
            throw new AssertionError("No steam_engine block entity at " + machinePos);
        }

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        steamEngine.getSteamTank().setTankType(HbmFluids.STEAM);
        steamEngine.getSteamTank().setFill(1_000);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes to adjacent cable network");
        refreshCableLine(level, firstCablePos, portCablePos);
        HbmEnergyNodespace.tick(level);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver remains subscribed after steam_engine remote-node refresh");
        SteamEngineBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), steamEngine);
        HbmEnergyUtil.PortSetSnapshot ports = steamEngine.inspectEnergyPorts();
        HbmEnergyUtil.PortSnapshot centerPort = HbmEnergyUtil.inspectPort(level, machinePos,
                HbmEnergyUtil.EnergyPort.of(-2, 1, 0, Direction.WEST));
        assertTrue(centerPort.connectable(),
                "steam_engine expected SOUTH center conductor is connectable red_cable: " + centerPort
                        + ", block=" + level.getBlockState(portCablePos).getBlock()
                        + ", blockEntity=" + level.getBlockEntity(portCablePos));
        assertEquals(3, ports.totalPorts(), "steam_engine exposes three legacy getConPos energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                "steam_engine legacy remote port sees the cross-chunk red_cable network: " + ports);
        assertTrue(steamEngine.getLastPowerProduced() > 0L,
                "steam_engine produced HE from steam; steam=" + steamEngine.getSteamTank().getFill()
                        + "/" + steamEngine.getSteamTank().getMaxFill()
                        + ", spent=" + steamEngine.getSpentSteamTank().getFill()
                        + "/" + steamEngine.getSpentSteamTank().getMaxFill());

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), "steam_engine port cable has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(transferred > 0L,
                "steam_engine power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate
                        + ", enginePower=" + steamEngine.getPower()
                        + ", produced=" + steamEngine.getLastPowerProduced()
                        + ", batteryPower=" + battery.getPower());

        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from steam_engine across cross-chunk red_cable network; before="
                        + beforeUpdate + ", after=" + afterUpdate
                        + ", enginePower=" + steamEngine.getPower()
                        + ", produced=" + steamEngine.getLastPowerProduced());
        assertTrue(steamEngine.getPower() < steamEngine.getLastPowerProduced(),
                "steam_engine spent HE into the legacy remote-port network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.providerEntries() >= 1,
                "steam_engine registered as provider through its legacy remote port");
        assertTrue(afterTransfer.receiverEntries() >= 1,
                "machine_battery input registered as receiver on cable network");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsFensu")
    public static void fensuBottomPortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 79);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 79);
        BlockPos fensuPos = portCablePos.above();
        long startingPower = 100_000L;
        forceLoadedChunks(level, batteryPos, fensuPos);
        level.removeBlock(fensuPos, false);
        level.setBlock(fensuPos, ModBlocks.MACHINE_FENSU.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(fensuPos) instanceof FensuBlockEntity fensu)) {
            throw new AssertionError("No fensu block entity at " + fensuPos);
        }
        while (fensu.getRedLow() != MachineBatteryBlockEntity.MODE_OUTPUT) {
            fensu.cycleRedLowMode();
        }
        fensu.setPower(startingPower);

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes to adjacent cable network");
        MachineBatteryBlockEntity.serverTick(level, fensuPos, level.getBlockState(fensuPos), fensu);
        HbmEnergyNodespace.tick(level);

        HbmEnergyUtil.PortSetSnapshot ports = fensu.inspectEnergyPorts();
        assertEquals(1, ports.totalPorts(), "fensu exposes the single legacy bottom energy port");
        assertTrue(ports.networkedPorts() >= 1,
                "fensu bottom port sees the cross-chunk red_cable network");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), "fensu bottom port cable has a valid power net");
        powerNet.update();

        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from fensu across cross-chunk red_cable network");
        assertTrue(fensu.getPower() < startingPower,
                "fensu spent HE into the legacy bottom-port network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.providerEntries() >= 1,
                "fensu registered as provider through its legacy bottom port");
        assertTrue(afterTransfer.receiverEntries() >= 1,
                "machine_battery input registered as receiver on cable network");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsBatteryRedd")
    public static void batteryReddSidePortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 82);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 82);
        BlockPos reddPos = portCablePos.east(5);
        long startingPower = 100_000L;
        forceLoadedChunks(level, reddPos, portCablePos);
        BlockState reddState = ModBlocks.MACHINE_BATTERY_REDD.get()
                .defaultBlockState()
                .setValue(LegacyVisibleMultiblockMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(reddPos, false);
        level.setBlock(reddPos, reddState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(reddPos) instanceof BatteryReddBlockEntity redd)) {
            throw new AssertionError("No machine_battery_redd block entity at " + reddPos);
        }
        while (redd.getRedLow() != BatteryReddBlockEntity.MODE_OUTPUT) {
            redd.receiveControl(null, BatteryReddBlockEntity.controlTag(BatteryReddBlockEntity.CONTROL_RED_LOW));
        }
        redd.setPower(startingPower);

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes to adjacent cable network");
        BatteryReddBlockEntity.serverTick(level, reddPos, level.getBlockState(reddPos), redd);
        HbmEnergyNodespace.tick(level);
        refreshCableLine(level, firstCablePos, portCablePos);
        HbmEnergyNodespace.tick(level);
        BatteryReddBlockEntity.serverTick(level, reddPos, level.getBlockState(reddPos), redd);

        HbmEnergyUtil.PortSetSnapshot ports = redd.inspectEnergyPorts();
        assertEquals(6, ports.totalPorts(), "machine_battery_redd exposes six legacy getConPos energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                "machine_battery_redd side port sees the cross-chunk red_cable network");

        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver remains subscribed after machine_battery_redd remote-node refresh");
        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), "machine_battery_redd port cable has a valid power net");
        powerNet.update();

        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from machine_battery_redd across cross-chunk red_cable network");
        assertTrue(redd.getPower() < startingPower,
                "machine_battery_redd spent HE into the legacy side-port network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery_redd registered as provider through its legacy side port");
        assertTrue(afterTransfer.receiverEntries() >= 1,
                "machine_battery input registered as receiver on cable network");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsStirling")
    public static void stirlingSidePortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 84);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 84);
        BlockPos machinePos = portCablePos.east(2);
        forceLoadedChunks(level, batteryPos, machinePos);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, ModBlocks.MACHINE_STIRLING.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof StirlingBlockEntity stirling)) {
            throw new AssertionError("No machine_stirling block entity at " + machinePos);
        }

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, stirling,
                "machine_stirling", 4, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsWoodBurner")
    public static void woodBurnerBackPortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 86);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 86);
        BlockPos machinePos = portCablePos.east(2);
        forceLoadedChunks(level, batteryPos, machinePos);
        BlockState machineState = ModBlocks.MACHINE_WOOD_BURNER.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.EAST);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, machineState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof WoodBurnerBlockEntity woodBurner)) {
            throw new AssertionError("No machine_wood_burner block entity at " + machinePos);
        }

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, woodBurner,
                "machine_wood_burner", 2, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsTurbofan")
    public static void turbofanBackPortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 88);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 88);
        BlockPos machinePos = portCablePos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        BlockState machineState = ModBlocks.MACHINE_TURBOFAN.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.EAST);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, machineState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof TurbofanBlockEntity turbofan)) {
            throw new AssertionError("No machine_turbofan block entity at " + machinePos);
        }

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, turbofan,
                "machine_turbofan", 4, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsGasTurbine")
    public static void gasTurbineSidePortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 90);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 90);
        BlockPos machinePos = portCablePos.east(4).below();
        forceLoadedChunks(level, batteryPos, machinePos);
        BlockState machineState = ModBlocks.MACHINE_TURBINEGAS.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, machineState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof TurbineGasBlockEntity turbine)) {
            throw new AssertionError("No machine_turbinegas block entity at " + machinePos);
        }

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, turbine,
                "machine_turbinegas", 1, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsLargeTurbine")
    public static void largeTurbineBackPortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 92);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 92);
        BlockPos machinePos = portCablePos.east(4);
        forceLoadedChunks(level, batteryPos, machinePos);
        BlockState machineState = ModBlocks.MACHINE_LARGE_TURBINE.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.EAST);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, machineState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof LegacyLargeTurbineBlockEntity turbine)) {
            throw new AssertionError("No machine_large_turbine block entity at " + machinePos);
        }

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, turbine,
                "machine_large_turbine", 1, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsIndustrialTurbine")
    public static void industrialTurbineBackPortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 94);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 94);
        BlockPos machinePos = portCablePos.east(4).below();
        forceLoadedChunks(level, batteryPos, machinePos);
        BlockState machineState = ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.EAST);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, machineState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof IndustrialSteamTurbineBlockEntity turbine)) {
            throw new AssertionError("No machine_industrial_turbine block entity at " + machinePos);
        }

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, turbine,
                "machine_industrial_turbine", 1, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsChungus")
    public static void chungusBackPortProvidesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 96);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 96);
        BlockPos machinePos = portCablePos.east(11);
        forceLoadedChunks(level, batteryPos, machinePos);
        BlockState machineState = ModBlocks.MACHINE_CHUNGUS.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.EAST);
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, machineState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof ChungusBlockEntity chungus)) {
            throw new AssertionError("No machine_chungus block entity at " + machinePos);
        }

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, chungus,
                "machine_chungus", 1, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsTurretStandard")
    public static void standardTurretRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 100);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 100);
        BlockPos turretPos = portCablePos.east(2);
        forceLoadedChunks(level, batteryPos, turretPos);
        BlockState turretState = ModBlocks.TURRET_CHEKHOV.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(turretPos, false);
        level.setBlock(turretPos, turretState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(turretPos) instanceof TurretBlockEntityBase turret)) {
            throw new AssertionError("No turret_chekhov block entity at " + turretPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                turret, "turret_chekhov", 8, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsTurretArty")
    public static void artilleryTurretRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 102);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 102);
        BlockPos turretPos = portCablePos.east(3);
        forceLoadedChunks(level, batteryPos, turretPos);
        BlockState turretState = ModBlocks.TURRET_ARTY.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(turretPos, false);
        level.setBlock(turretPos, turretState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(turretPos) instanceof TurretArtyBlockEntity turret)) {
            throw new AssertionError("No turret_arty block entity at " + turretPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                turret, "turret_arty", 32, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsTurretHimars")
    public static void himarsTurretRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 104);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 104);
        BlockPos turretPos = portCablePos.east(3);
        forceLoadedChunks(level, batteryPos, turretPos);
        BlockState turretState = ModBlocks.TURRET_HIMARS.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(turretPos, false);
        level.setBlock(turretPos, turretState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(turretPos) instanceof TurretHimarsBlockEntity turret)) {
            throw new AssertionError("No turret_himars block entity at " + turretPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                turret, "turret_himars", 32, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsPaSource")
    public static void paSourceRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 106);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 106);
        BlockPos paPos = portCablePos.east(5);
        forceLoadedChunks(level, batteryPos, paPos);
        BlockState paState = ModBlocks.PA_SOURCE.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(paPos, false);
        level.setBlock(paPos, paState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(paPos) instanceof PASourceBlockEntity pa)) {
            throw new AssertionError("No pa_source block entity at " + paPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, paPos,
                pa.energyPorts(), pa, "pa_source", 7, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsPaRfc")
    public static void paRfcRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 108);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 108);
        BlockPos paPos = portCablePos.above(2);
        forceLoadedChunks(level, batteryPos, paPos);
        BlockState paState = ModBlocks.PA_RFC.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(paPos, false);
        level.setBlock(paPos, paState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(paPos) instanceof PARfcBlockEntity pa)) {
            throw new AssertionError("No pa_rfc block entity at " + paPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, paPos,
                pa.energyPorts(), pa, "pa_rfc", 6, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsPaQuadrupole")
    public static void paQuadrupoleRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 110);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 110);
        BlockPos paPos = portCablePos.above(2);
        forceLoadedChunks(level, batteryPos, paPos);
        BlockState paState = ModBlocks.PA_QUADRUPOLE.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(paPos, false);
        level.setBlock(paPos, paState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(paPos) instanceof PAQuadrupoleBlockEntity pa)) {
            throw new AssertionError("No pa_quadrupole block entity at " + paPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, paPos,
                pa.energyPorts(), pa, "pa_quadrupole", 4, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsPaDipole")
    public static void paDipoleRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 112);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 112);
        BlockPos paPos = portCablePos.east().above(2);
        forceLoadedChunks(level, batteryPos, paPos);
        BlockState paState = ModBlocks.PA_DIPOLE.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(paPos, false);
        level.setBlock(paPos, paState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(paPos) instanceof PADipoleBlockEntity pa)) {
            throw new AssertionError("No pa_dipole block entity at " + paPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, paPos,
                pa.energyPorts(), pa, "pa_dipole", 8, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsPaDetector")
    public static void paDetectorRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 114);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 114);
        BlockPos paPos = portCablePos.east(5);
        forceLoadedChunks(level, batteryPos, paPos);
        BlockState paState = ModBlocks.PA_DETECTOR.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.NORTH);
        level.removeBlock(paPos, false);
        level.setBlock(paPos, paState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(paPos) instanceof PADetectorBlockEntity pa)) {
            throw new AssertionError("No pa_detector block entity at " + paPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, paPos,
                pa.energyPorts(), pa, "pa_detector", 5, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsLaunchPad")
    public static void launchPadRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 116);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 116);
        BlockPos launchPadPos = portCablePos.west(2).south();
        forceLoadedChunks(level, batteryPos, launchPadPos);
        level.removeBlock(launchPadPos, false);
        level.setBlock(launchPadPos, ModBlocks.LAUNCH_PAD.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(launchPadPos) instanceof LaunchPadBlockEntity launchPad)) {
            throw new AssertionError("No launch_pad block entity at " + launchPadPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                launchPad, "launch_pad", 8, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedRemotePorts")
    public static void playerUseOnPlacesLaunchPadRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 19, anchor.getY() - 1, chunkStartZ + 140);
        forceLoadedChunks(level, supportPos.offset(-4, 0, -4), supportPos.offset(4, 1, 4));
        clearSingleLayer(level, supportPos.above().offset(-4, 0, -4), supportPos.above().offset(4, 0, 4));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack launchPadStack = new ItemStack(ModBlocks.LAUNCH_PAD.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, launchPadStack);

        var placeResult = launchPadStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        LaunchPadBlockEntity launchPad = findLaunchPadAroundOrNull(level, supportPos.above(), 4);
        if (launchPad == null) {
            throw new AssertionError("player useOn launch_pad placement returned " + placeResult
                    + " without placing a launch_pad core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the launch_pad block item action after placing the core");
        BlockPos launchPadPos = launchPad.getBlockPos();
        assertLaunchPadHasResolvingProxy(level, launchPad);

        BlockPos portCablePos = launchPadPos.west(2).north();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, launchPadPos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                launchPad, "player-placed launch_pad", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed launch_pad diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, launchPadPos,
                "hbm energy port " + commandPos(launchPadPos) + " -2 0 -1 west", expectedLinks,
                "Energy port from " + launchPadPos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, launchPadPos, "hbm energy ports " + commandPos(launchPadPos), 1,
                "Energy ports at " + launchPadPos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsLargeLaunchPad")
    public static void largeLaunchPadRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 118);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 118);
        BlockPos launchPadPos = portCablePos.west(5).south(2);
        forceLoadedChunks(level, batteryPos, launchPadPos);
        level.removeBlock(launchPadPos, false);
        level.setBlock(launchPadPos, ModBlocks.LAUNCH_PAD_LARGE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(launchPadPos) instanceof LargeLaunchPadBlockEntity launchPad)) {
            throw new AssertionError("No launch_pad_large block entity at " + launchPadPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                launchPad, "launch_pad_large", 8, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedLargeLaunchPad")
    public static void playerUseOnPlacesLargeLaunchPadRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 21, anchor.getY() - 1, chunkStartZ + 180);
        forceLoadedChunks(level, supportPos.offset(-10, 0, -10), supportPos.offset(10, 1, 10));
        clearSingleLayer(level, supportPos.above().offset(-10, 0, -10),
                supportPos.above().offset(10, 0, 10));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack launchPadStack = new ItemStack(ModBlocks.LAUNCH_PAD_LARGE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, launchPadStack);

        var placeResult = launchPadStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        LargeLaunchPadBlockEntity launchPad = findLargeLaunchPadAroundOrNull(level, supportPos.above(), 9);
        if (launchPad == null) {
            throw new AssertionError("player useOn launch_pad_large placement returned " + placeResult
                    + " without placing a launch_pad_large core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the launch_pad_large block item action after placing the core");
        BlockPos launchPadPos = launchPad.getBlockPos();
        assertLargeLaunchPadHasResolvingPortProxies(level, launchPad);

        BlockPos portCablePos = launchPadPos.west(5).north(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, launchPadPos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(launchPad, level.getBlockEntity(launchPadPos),
                "player-placed launch_pad_large core survives external west remote-port cable placement");
        HbmEnergyStorage receiver = energyStorage(launchPad);
        Iterable<HbmEnergyUtil.EnergyPort> energyPorts = energyAndFluidEnergyPorts(launchPad);
        HbmEnergyUtil.PortSetSnapshot ports = HbmEnergyUtil.inspectPorts(level, launchPadPos, energyPorts);
        assertEquals(8, ports.totalPorts(), "player-placed launch_pad_large exposes eight legacy energy ports");
        HbmEnergyUtil.PortSnapshot westPort = HbmEnergyUtil.inspectPort(level, launchPadPos,
                new HbmEnergyUtil.EnergyPort(new BlockPos(-5, 0, -2), Direction.WEST));
        assertTrue(westPort.networkPresent(),
                "player-placed launch_pad_large west/north legacy port sees the red_cable network: " + westPort);

        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before player-placed launch_pad_large receiver transfer");
        assertTrue(HbmEnergyUtil.subscribeReceiverToNetwork(level, portCablePos, Direction.EAST, receiver),
                "player-placed launch_pad_large receiver subscribes through its west/north legacy remote port");

        HbmPowerNet transferNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(transferNet != null && transferNet.isValid(),
                "player-placed launch_pad_large transfer port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = transferNet.createDebugSnapshot();
        long transferred = transferNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = transferNet.createDebugSnapshot();
        assertTrue(transferred > 0L,
                "player-placed launch_pad_large power net transferred HE; before="
                        + beforeUpdate + ", after=" + afterUpdate);
        assertTrue(receiver.getPower() > 0L,
                "player-placed launch_pad_large received HE from its player-placed remote port");
        assertTrue(battery.getPower() < 100_000L,
                "machine_battery output spent HE into player-placed launch_pad_large cross-chunk network");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed launch_pad_large diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, launchPadPos,
                "hbm energy port " + commandPos(launchPadPos) + " -5 0 -2 west", expectedLinks,
                "Energy port from " + launchPadPos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, launchPadPos, "hbm energy ports " + commandPos(launchPadPos), 1,
                "Energy ports at " + launchPadPos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsCompactLauncher")
    public static void compactLauncherRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 120);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 120);
        BlockPos launcherPos = portCablePos.west(2).north();
        forceLoadedChunks(level, batteryPos, launcherPos);
        level.removeBlock(launcherPos, false);
        level.setBlock(launcherPos, ModBlocks.COMPACT_LAUNCHER.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(launcherPos) instanceof CompactLauncherBlockEntity launcher)) {
            throw new AssertionError("No compact_launcher block entity at " + launcherPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                launcher, "compact_launcher", 12, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedRemotePorts")
    public static void playerUseOnPlacesCompactLauncherRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 19, anchor.getY() - 1, chunkStartZ + 144);
        forceLoadedChunks(level, supportPos.offset(-4, 0, -4), supportPos.offset(4, 1, 4));
        clearSingleLayer(level, supportPos.above().offset(-4, 0, -4), supportPos.above().offset(4, 0, 4));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack launcherStack = new ItemStack(ModBlocks.COMPACT_LAUNCHER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, launcherStack);

        var placeResult = launcherStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        CompactLauncherBlockEntity launcher = findCompactLauncherAroundOrNull(level, supportPos.above(), 3);
        if (launcher == null) {
            throw new AssertionError("player useOn compact_launcher placement returned " + placeResult
                    + " without placing a compact_launcher core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the compact_launcher block item action after placing the core");
        BlockPos launcherPos = launcher.getBlockPos();
        assertCompactLauncherHasResolvingPortProxy(level, launcher);

        BlockPos portCablePos = launcherPos.west(2).north();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, launcherPos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                launcher, "player-placed compact_launcher", 12, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed compact_launcher diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, launcherPos,
                "hbm energy port " + commandPos(launcherPos) + " -2 0 -1 west", expectedLinks,
                "Energy port from " + launcherPos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, launcherPos, "hbm energy ports " + commandPos(launcherPos), 1,
                "Energy ports at " + launcherPos.toShortString(), "total=12", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsSoyuzLauncher")
    public static void soyuzLauncherRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 122);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 122);
        BlockPos launcherPos = portCablePos.west(7);
        forceLoadedChunks(level, batteryPos, launcherPos);
        level.removeBlock(launcherPos, false);
        level.setBlock(launcherPos, ModBlocks.SOYUZ_LAUNCHER.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(launcherPos) instanceof SoyuzLauncherBlockEntity launcher)) {
            throw new AssertionError("No soyuz_launcher block entity at " + launcherPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                launcher, "soyuz_launcher", 104, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedSoyuzLauncher")
    public static void playerUseOnPlacesSoyuzLauncherRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() - 1, chunkStartZ + 220);
        forceLoadedChunks(level, supportPos.offset(-16, 0, -16), supportPos.offset(16, 10, 16));
        clearBox(level, supportPos.above().offset(-16, 0, -16), supportPos.above(10).offset(16, 0, 16));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack launcherStack = new ItemStack(ModBlocks.SOYUZ_LAUNCHER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, launcherStack);

        var placeResult = launcherStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        SoyuzLauncherBlockEntity launcher = findSoyuzLauncherAroundOrNull(level, supportPos.above(5), 8);
        if (launcher == null) {
            throw new AssertionError("player useOn soyuz_launcher placement returned " + placeResult
                    + " without placing a soyuz_launcher core around " + supportPos.above(5)
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the soyuz_launcher block item action after placing the core");
        BlockPos launcherPos = launcher.getBlockPos();
        assertSoyuzLauncherHasResolvingPortProxies(level, launcher);

        BlockPos portCablePos = launcherPos.west(7);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, launcherPos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(launcher, level.getBlockEntity(launcherPos),
                "player-placed soyuz_launcher core survives external west remote-port cable placement");
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                launcher, "player-placed soyuz_launcher", 104, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed soyuz_launcher diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, launcherPos,
                "hbm energy port " + commandPos(launcherPos) + " -7 0 0 west", expectedLinks,
                "Energy port from " + launcherPos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, launcherPos, "hbm energy ports " + commandPos(launcherPos), 1,
                "Energy ports at " + launcherPos.toShortString(), "total=104", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsRemainingStandardTurrets")
    public static void remainingStandardTurretSubclassesReceivePowerAcrossChunkCable(GameTestHelper helper) {
        assertStandardTurretSubclassReceivesPower(helper, ModBlocks.TURRET_FRIENDLY,
                "turret_friendly", 124);
        assertStandardTurretSubclassReceivesPower(helper, ModBlocks.TURRET_JEREMY,
                "turret_jeremy", 126);
        assertStandardTurretSubclassReceivesPower(helper, ModBlocks.TURRET_RICHARD,
                "turret_richard", 128);
        assertStandardTurretSubclassReceivesPower(helper, ModBlocks.TURRET_TAUON,
                "turret_tauon", 130);
        assertStandardTurretSubclassReceivesPower(helper, ModBlocks.TURRET_HOWARD,
                "turret_howard", 132);
        assertStandardTurretSubclassReceivesPower(helper, ModBlocks.TURRET_MAXWELL,
                "turret_maxwell", 134);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsTurretSentry")
    public static void sentryTurretBottomPortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 136);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 136);
        BlockPos turretPos = portCablePos.above();
        forceLoadedChunks(level, batteryPos, turretPos);
        BlockState turretState = ModBlocks.TURRET_SENTRY.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(turretPos, false);
        level.setBlock(turretPos, turretState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(turretPos) instanceof TurretBlockEntityBase turret)) {
            throw new AssertionError("No turret_sentry block entity at " + turretPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                turret, "turret_sentry", 1, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsTurretFritz")
    public static void fritzTurretRemotePortReceivesPowerAcrossChunkCable(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 138);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 138);
        BlockPos turretPos = portCablePos.east(2);
        forceLoadedChunks(level, batteryPos, turretPos);
        BlockState turretState = ModBlocks.TURRET_FRITZ.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(turretPos, false);
        level.setBlock(turretPos, turretState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(turretPos) instanceof TurretBlockEntityBase turret)) {
            throw new AssertionError("No turret_fritz block entity at " + turretPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                turret, "turret_fritz", 8, 100_000L);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2RemotePortsTurretFritz")
    public static void fritzTurretFluidPortReceivesDieselAcrossFluidDuct(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos sourcePipePos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 140);
        BlockPos portPipePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 140);
        BlockPos turretPos = portPipePos.east(2);
        forceLoadedChunks(level, sourcePipePos, turretPos);
        BlockState turretState = ModBlocks.TURRET_FRITZ.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(turretPos, false);
        level.setBlock(turretPos, turretState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(turretPos) instanceof TurretFritzBlockEntity turret)) {
            throw new AssertionError("No turret_fritz block entity at " + turretPos);
        }

        prepareDieselFluidPipeLine(level, sourcePipePos, portPipePos);
        List<HbmFluidUtil.FluidPort> ports = fritzFluidPorts(turret);
        HbmFluidUtil.PortSetSnapshot portSnapshot =
                HbmFluidUtil.inspectPorts(level, turretPos, ports, HbmFluids.DIESEL);
        assertEquals(8, portSnapshot.totalPorts(),
                "turret_fritz exposes legacy remote fluid receiver ports");
        assertTrue(portSnapshot.networkedPorts() >= 1,
                "turret_fritz remote fluid port sees the diesel fluid_duct_neo network: " + portSnapshot);

        assertTrue(turret.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.EAST).resolve().isPresent(),
                "turret_fritz accepts Forge fluid capability from horizontal sides");
        assertTrue(turret.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).resolve().isEmpty(),
                "turret_fritz rejects Forge fluid capability from the top like legacy canConnect");
        assertTrue(turret.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).resolve().isEmpty(),
                "turret_fritz rejects Forge fluid capability from the bottom like legacy canConnect");

        HbmFluidUtil.PortSubscribeDetailReport receiverSubscriptionReport =
                HbmFluidUtil.subscribeReceiverToPortsDetailedReport(level, turretPos, ports,
                        HbmFluids.DIESEL, turret);
        assertTrue(receiverSubscriptionReport.subscribedPorts() >= 1,
                "turret_fritz subscribed as diesel receiver through a legacy remote fluid port: "
                        + receiverSubscriptionReport);
        HbmFluidUtil.PortSnapshot receiverPort =
                requireSubscribedFluidPort(level, turretPos, ports, HbmFluids.DIESEL);
        TestFluidProvider provider = new TestFluidProvider(HbmFluids.DIESEL, 1_000);
        assertTrue(HbmFluidUtil.subscribeProviderToNetwork(level, receiverPort.connectorPos(),
                receiverPort.connectorSide(),
                HbmFluids.DIESEL, provider),
                "test diesel provider subscribed to the fluid_duct_neo port network");

        assertTrue(HbmFluidNodespace.getNetworkReceiverCount(level, receiverPort.connectorPos(),
                HbmFluids.DIESEL) >= 1,
                "diesel network has turret_fritz receiver entry");
        assertTrue(HbmFluidNodespace.getNetworkProviderCount(level, receiverPort.connectorPos(),
                HbmFluids.DIESEL) >= 1,
                "diesel network has provider entry");
        HbmFluidNet fluidNet = HbmFluidNodespace.getNode(level, receiverPort.connectorPos(),
                HbmFluids.DIESEL).getFluidNet();
        HbmFluidNet.DebugSnapshot beforeUpdate = fluidNet.createDebugSnapshot();
        long transferred = fluidNet.update();
        HbmFluidNet.DebugSnapshot afterUpdate = fluidNet.createDebugSnapshot();

        assertTrue(transferred > 0L,
                "diesel fluid net transferred mB; before=" + beforeUpdate + ", after=" + afterUpdate);
        assertEquals(1_000, turret.getTank().getFill(),
                "turret_fritz received diesel across the real fluid_duct_neo network");
        assertEquals(0, provider.getStoredFluid(),
                "test diesel provider drained into turret_fritz");
        assertTrue(HbmFluidNodespace.getNetworkFluidTracker(level, receiverPort.connectorPos(),
                HbmFluids.DIESEL) >= transferred,
                "diesel network tracked the remote-port transfer");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2CommandDiagnostics")
    public static void energyCommandsObserveRemotePortNetworks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 180);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 180);
        BlockPos launchPadPos = portCablePos.west(2).south();
        long startingPower = 100_000L;
        forceLoadedChunks(level, batteryPos, launchPadPos);
        level.removeBlock(launchPadPos, false);
        level.setBlock(launchPadPos, ModBlocks.LAUNCH_PAD.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(launchPadPos) instanceof LaunchPadBlockEntity launchPad)) {
            throw new AssertionError("No launch_pad block entity at " + launchPadPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, startingPower);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                launchPad, "launch_pad command diagnostics", 8, startingPower);

        HbmEnergyNodespace.Diagnostics diagnostics = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(diagnostics.uniqueNodes() > 0, "command diagnostic fixture has energy nodes");
        assertTrue(diagnostics.networks() > 0, "command diagnostic fixture has energy networks");

        HbmEnergyNodespace.NetworkDebugSnapshot networkSnapshot =
                HbmEnergyNodespace.getNetworkDebugSnapshot(level, portCablePos);
        assertTrue(networkSnapshot.nodePresent(), "command diagnostic port cable has a node");
        assertTrue(networkSnapshot.networkPresent(), "command diagnostic port cable has a network");
        int expectedLinks = networkSnapshot.network().links();
        int expectedChunkNodes = HbmEnergyNodespace
                .getChunkDiagnostics(level, new ChunkPos(portCablePos))
                .uniqueNodes();

        assertCommandResult(level, anchor, "hbm energy nodespace", diagnostics.uniqueNodes());
        assertCommandResult(level, anchor, "hbm energy network " + commandPos(portCablePos), expectedLinks);
        assertCommandResult(level, anchor, "hbm energy node " + commandPos(portCablePos), expectedLinks);
        assertCommandResult(level, anchor, "hbm energy chunk " + commandPos(portCablePos), expectedChunkNodes);
        assertCommandResult(level, anchor,
                "hbm energy port " + commandPos(launchPadPos) + " 2 0 -1 east", expectedLinks);
        assertCommandResult(level, anchor, "hbm energy ports " + commandPos(launchPadPos), 1);

        assertCommandVisibleMessage(level, anchor, "hbm energy nodespace", diagnostics.uniqueNodes(),
                "Energy nodespace:", "nodes=", "debugParticles=");
        assertCommandVisibleMessage(level, anchor, "hbm energy network " + commandPos(portCablePos), expectedLinks,
                "Energy network at " + portCablePos.toShortString(), "providers=", "lastTransfer=");
        assertCommandVisibleMessage(level, anchor, "hbm energy node " + commandPos(portCablePos), expectedLinks,
                "Energy network at " + portCablePos.toShortString(), "nodeConnections=", "receivers=");
        assertCommandVisibleMessage(level, anchor, "hbm energy chunk " + commandPos(portCablePos), expectedChunkNodes,
                "Energy chunk [" + new ChunkPos(portCablePos).x + ", " + new ChunkPos(portCablePos).z + "]",
                "positions=", "receivers=");
        assertCommandVisibleMessage(level, anchor,
                "hbm energy port " + commandPos(launchPadPos) + " 2 0 -1 east", expectedLinks,
                "Energy port from " + launchPadPos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, anchor, "hbm energy ports " + commandPos(launchPadPos), 1,
                "Energy ports at " + launchPadPos.toShortString(), "total=8", "networked=");
        BlockPos missingNodePos = anchor.above(4);
        assertCommandVisibleMessage(level, anchor, "hbm energy network " + commandPos(missingNodePos), 0,
                "No HBM energy node at " + missingNodePos.toShortString());
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatteryRedstoneHighLowLifecycle(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos cablePos = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos batteryPos = cablePos.east();
            BlockPos redstonePos = batteryPos.above();
            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, cablePos);
            MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);

            tickMachineBattery(level, batteryPos, battery);
            HbmEnergyNodespace.tick(level);
            assertEquals(1, HbmEnergyNodespace.getDiagnostics(level).uniqueNodes(),
                    "machine_battery default redLow input has no self node");

            level.setBlock(redstonePos, Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            tickMachineBattery(level, batteryPos, battery);
            HbmEnergyNodespace.tick(level);
            assertEquals(1, HbmEnergyNodespace.getDiagnostics(level).uniqueNodes(),
                    "machine_battery default redHigh output has no self node");

            cycleRedHighToBuffer(battery);
            tickMachineBattery(level, batteryPos, battery);
            HbmEnergyNodespace.tick(level);
            HbmEnergyNodespace.Diagnostics highBuffer = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(battery.getEnergyNode() != null && !battery.getEnergyNode().isExpired(),
                    "machine_battery powered redHigh buffer creates self node");
            assertEquals(2, highBuffer.uniqueNodes(), "powered redHigh buffer joins cable node");
            assertEquals(1, highBuffer.providerEntries(), "powered redHigh buffer subscribes as provider");
            assertEquals(1, highBuffer.receiverEntries(), "powered redHigh buffer subscribes as receiver");

            level.setBlock(redstonePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            tickMachineBattery(level, batteryPos, battery);
            HbmEnergyNodespace.tick(level);
            HbmEnergyNodespace.Diagnostics lowInput = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(battery.getEnergyNode() == null || battery.getEnergyNode().isExpired(),
                    "machine_battery unpowered redLow input removes high-buffer self node");
            assertEquals(1, lowInput.uniqueNodes(), "unpowered redLow input leaves only cable node");
            assertEquals(0, lowInput.providerEntries(), "unpowered redLow input clears provider subscription");
            assertEquals(0, lowInput.receiverEntries(), "unpowered redLow input clears receiver subscription");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatteryComparatorKeepsLegacyPlusOneFormula(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(pos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, pos);

        battery.setPower(0L);
        assertEquals(0, battery.getComparatorPower(), "machine_battery comparator empty");
        battery.setPower(1L);
        assertEquals(1, battery.getComparatorPower(), "machine_battery comparator nonzero minimum");
        battery.setPower(battery.getMaxPower() / 2L);
        assertEquals(8, battery.getComparatorPower(), "machine_battery comparator half");
        battery.setPower(battery.getMaxPower());
        assertEquals(15, battery.getComparatorPower(), "machine_battery comparator full");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketProxyCoreComparatorAndCapabilities(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
            Direction facing = Direction.SOUTH;
            BlockState state = ModBlocks.MACHINE_BATTERY_SOCKET.get()
                    .defaultBlockState()
                    .setValue(MachineBatterySocketBlock.FACING, facing);
            level.setBlock(pos, state, Block.UPDATE_ALL);
            state.getBlock().setPlacedBy(level, pos, state, null, ItemStack.EMPTY);
            MachineBatterySocketBlockEntity socket = requireMachineBatterySocket(level, pos);

            ItemStack batteryStack = new ItemStack(ModItems.BATTERY_REDSTONE.get());
            HbmChargeableItem batteryItem = requireChargeable(batteryStack, "battery_redstone");
            batteryItem.setCharge(batteryStack, 1L);
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, batteryStack);
            MachineBatterySocketBlockEntity.serverTick(level, pos, state, socket);

            BlockPos proxyPos = pos.offset(MachineBatterySocketBlock.socketProxyOffsets(facing).get(0));
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity dummy
                            && dummy.getProxyMode().inventory()
                            && dummy.getProxyMode().power()
                            && dummy.getProxyMode().conductor(),
                    "battery_socket proxy dummy has inventory/power/conductor proxy flags");
            assertSame(socket, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "battery_socket proxy resolves to core block entity");

            BlockState proxyState = level.getBlockState(proxyPos);
            assertEquals(0, proxyState.getAnalogOutputSignal(level, proxyPos),
                    "battery_socket proxy comparator keeps rounded low-power legacy formula");
            batteryItem.setCharge(batteryStack, batteryItem.getMaxCharge(batteryStack) / 2L);
            assertEquals(8, proxyState.getAnalogOutputSignal(level, proxyPos),
                    "battery_socket proxy comparator forwards core value");

            IItemHandler itemHandler = level.getBlockEntity(proxyPos)
                    .getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH)
                    .orElseThrow(() -> new AssertionError("battery_socket proxy missing item handler"));
            assertSame(batteryStack.getItem(), itemHandler.getStackInSlot(0).getItem(),
                    "battery_socket proxy item handler forwards core slot");

            IEnergyStorage energy = level.getBlockEntity(proxyPos)
                    .getCapability(ForgeCapabilities.ENERGY, Direction.NORTH)
                    .orElseThrow(() -> new AssertionError("battery_socket proxy missing FE bridge"));
            assertTrue(energy.canReceive(), "battery_socket proxy input mode can receive FE as 1 HE = 1 FE bridge");
            assertTrue(!energy.canExtract(), "battery_socket proxy input mode cannot extract FE");
            assertEquals((int) batteryItem.getMaxCharge(batteryStack) / 2, energy.getEnergyStored(),
                    "battery_socket proxy FE getter mirrors current HE charge");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketSidedAutomationKeepsLegacySingleSlotRules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
            Direction facing = Direction.SOUTH;
            BlockState state = ModBlocks.MACHINE_BATTERY_SOCKET.get()
                    .defaultBlockState()
                    .setValue(MachineBatterySocketBlock.FACING, facing);
            level.setBlock(corePos, state, Block.UPDATE_ALL);
            state.getBlock().setPlacedBy(level, corePos, state, null, ItemStack.EMPTY);
            MachineBatterySocketBlockEntity socket = requireMachineBatterySocket(level, corePos);

            BlockPos proxyPos = corePos.offset(MachineBatterySocketBlock.socketProxyOffsets(facing).get(0));
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "battery_socket proxy dummy exists for sided automation test");

            IItemHandler coreSide = itemHandler(level, corePos, Direction.NORTH, "battery_socket core north");
            IItemHandler proxySide = itemHandler(level, proxyPos, Direction.NORTH, "battery_socket proxy north");
            assertEquals(1, coreSide.getSlots(), "battery_socket core exposes legacy single slot");
            assertEquals(1, proxySide.getSlots(), "battery_socket proxy exposes legacy single slot");

            ItemStack emptyBattery = chargedBatteryStack(0L);
            long maxCharge = requireChargeable(emptyBattery, "battery_redstone").getMaxCharge(emptyBattery);
            ItemStack fullBattery = chargedBatteryStack(maxCharge);
            ItemStack partialBattery = chargedBatteryStack(1L);
            ItemStack stone = new ItemStack(Blocks.STONE);

            assertTrue(coreSide.insertItem(0, partialBattery.copy(), true).isEmpty(),
                    "battery_socket core accepts HBM battery into legacy slot 0");
            assertSame(stone.getItem(), coreSide.insertItem(0, stone.copy(), true).getItem(),
                    "battery_socket core rejects non-battery automation insertion");
            assertTrue(coreSide.insertItem(0, partialBattery.copy(), false).isEmpty(),
                    "battery_socket core inserts HBM battery into legacy slot 0");
            assertBatteryCharge(proxySide.getStackInSlot(0), 1L,
                    "battery_socket proxy sees battery inserted through core capability");
            clearSocketBattery(socket);

            assertTrue(proxySide.insertItem(0, fullBattery.copy(), true).isEmpty(),
                    "battery_socket proxy accepts HBM battery into core slot 0");
            assertSame(stone.getItem(), proxySide.insertItem(0, stone.copy(), true).getItem(),
                    "battery_socket proxy rejects non-battery automation insertion");
            assertTrue(proxySide.insertItem(0, fullBattery.copy(), false).isEmpty(),
                    "battery_socket proxy inserts HBM battery into core slot 0");
            assertBatteryCharge(coreSide.getStackInSlot(0), maxCharge,
                    "battery_socket core sees battery inserted through proxy capability");
            clearSocketBattery(socket);

            assertSocketExtraction(socket, coreSide, MachineBatterySocketBlockEntity.MODE_OUTPUT,
                    emptyBattery, true, "battery_socket output mode extracts empty battery");
            assertSocketExtraction(socket, coreSide, MachineBatterySocketBlockEntity.MODE_OUTPUT,
                    partialBattery, false, "battery_socket output mode keeps partial battery");
            assertSocketExtraction(socket, coreSide, MachineBatterySocketBlockEntity.MODE_OUTPUT,
                    fullBattery, false, "battery_socket output mode keeps full battery");

            assertSocketExtraction(socket, proxySide, MachineBatterySocketBlockEntity.MODE_INPUT,
                    fullBattery, true, "battery_socket proxy input mode extracts full battery");
            assertSocketExtraction(socket, proxySide, MachineBatterySocketBlockEntity.MODE_INPUT,
                    partialBattery, false, "battery_socket proxy input mode keeps partial battery");
            assertSocketExtraction(socket, proxySide, MachineBatterySocketBlockEntity.MODE_INPUT,
                    emptyBattery, false, "battery_socket proxy input mode keeps empty battery");

            assertSocketExtraction(socket, coreSide, MachineBatterySocketBlockEntity.MODE_BUFFER,
                    emptyBattery, false, "battery_socket buffer mode does not automate extraction");
            assertSocketExtraction(socket, proxySide, MachineBatterySocketBlockEntity.MODE_BUFFER,
                    fullBattery, false, "battery_socket proxy buffer mode does not automate extraction");
            assertSocketExtraction(socket, coreSide, MachineBatterySocketBlockEntity.MODE_NONE,
                    emptyBattery, false, "battery_socket none mode does not automate extraction");
            assertSocketExtraction(socket, proxySide, MachineBatterySocketBlockEntity.MODE_NONE,
                    fullBattery, false, "battery_socket proxy none mode does not automate extraction");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketRealHoppersFollowCoreProxyFootprint(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos corePos = helper.absolutePos(new BlockPos(6, 4, 6));
            Direction facing = Direction.SOUTH;
            MachineBatterySocketBlockEntity socket = placeBatterySocket(level, corePos, facing);
            List<BlockPos> footprint = socketFootprintPositions(corePos, facing);
            Set<BlockPos> footprintSet = new LinkedHashSet<>(footprint);
            assertSocketFootprintProxiesResolve(level, socket, footprint);

            ItemStack emptyForMax = chargedBatteryStack(0L);
            long maxCharge = requireChargeable(emptyForMax, "battery_redstone").getMaxCharge(emptyForMax);
            ItemStack fullBattery = chargedBatteryStack(maxCharge);

            Direction coreHopperSide = openHorizontalDirection(corePos, footprintSet);
            BlockPos coreHopperPos = corePos.relative(coreHopperSide);
            HopperBlockEntity coreHopper = placeHopper(level, coreHopperPos, coreHopperSide.getOpposite());
            coreHopper.setItem(0, chargedBatteryStack(1L));
            tickHopper(level, coreHopperPos);
            assertBatteryCharge(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY), 1L,
                    "real hopper inserted battery through socket core");
            assertTrue(coreHopper.getItem(0).isEmpty(), "real core hopper emptied into socket");
            clearSocketAndHopper(level, socket, coreHopperPos);

            for (int i = 1; i < footprint.size(); i++) {
                BlockPos proxyPos = footprint.get(i);
                long charge = i + 1L;
                Direction proxyHopperSide = openHorizontalDirection(proxyPos, footprintSet);
                BlockPos proxyHopperPos = proxyPos.relative(proxyHopperSide);
                HopperBlockEntity proxyHopper = placeHopper(level, proxyHopperPos, proxyHopperSide.getOpposite());
                proxyHopper.setItem(0, chargedBatteryStack(charge));
                tickHopper(level, proxyHopperPos);
                assertBatteryCharge(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY),
                        charge, "real hopper inserted battery through socket proxy " + i);
                assertTrue(proxyHopper.getItem(0).isEmpty(),
                        "real proxy hopper emptied into socket proxy " + i);
                clearSocketAndHopper(level, socket, proxyHopperPos);
            }

            BlockPos outputProxy = footprint.get(1);
            HopperBlockEntity outputHopper = placeHopper(level, outputProxy.below(), Direction.DOWN);
            cycleSocketRedLowToMode(socket, MachineBatterySocketBlockEntity.MODE_OUTPUT);
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, chargedBatteryStack(0L));
            tickHopper(level, outputProxy.below());
            assertTrue(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY).isEmpty(),
                    "real hopper extracted empty battery through socket proxy in output mode");
            assertBatteryCharge(outputHopper.getItem(0), 0L,
                    "real proxy hopper received empty output-mode socket battery");
            clearSocketAndHopper(level, socket, outputProxy.below());

            BlockPos inputProxy = footprint.get(2);
            HopperBlockEntity inputHopper = placeHopper(level, inputProxy.below(), Direction.DOWN);
            cycleSocketRedLowToMode(socket, MachineBatterySocketBlockEntity.MODE_INPUT);
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, fullBattery.copy());
            tickHopper(level, inputProxy.below());
            assertTrue(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY).isEmpty(),
                    "real hopper extracted full battery through socket proxy in input mode");
            assertBatteryCharge(inputHopper.getItem(0), maxCharge,
                    "real proxy hopper received full input-mode socket battery");
            clearSocketAndHopper(level, socket, inputProxy.below());

            BlockPos blockedProxy = footprint.get(3);
            HopperBlockEntity blockedHopper = placeHopper(level, blockedProxy.below(), Direction.DOWN);
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, chargedBatteryStack(1L));
            tickHopper(level, blockedProxy.below());
            assertTrue(blockedHopper.getItem(0).isEmpty(),
                    "real hopper cannot extract partial socket battery in input mode");
            assertBatteryCharge(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY), 1L,
                    "partial socket battery remains in proxy footprint after blocked hopper extraction");
            clearSocketAndHopper(level, socket, blockedProxy.below());
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketRealPneumaticTubesFollowCoreProxyFootprint(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        PneumaticNodespace.unloadLevel(level);
        try {
            BlockPos corePos = helper.absolutePos(new BlockPos(9, 4, 9));
            Direction facing = Direction.SOUTH;
            MachineBatterySocketBlockEntity socket = placeBatterySocket(level, corePos, facing);
            List<BlockPos> footprint = socketFootprintPositions(corePos, facing);
            Set<BlockPos> footprintSet = new LinkedHashSet<>(footprint);
            assertSocketFootprintProxiesResolve(level, socket, footprint);

            ItemStack emptyForMax = chargedBatteryStack(0L);
            long maxCharge = requireChargeable(emptyForMax, "battery_redstone").getMaxCharge(emptyForMax);
            ItemStack fullBattery = chargedBatteryStack(maxCharge);

            BlockPos insertProxy = footprint.get(1);
            Direction insertSide = openHorizontalDirection(insertProxy, footprintSet);
            BlockPos insertTubePos = insertProxy.relative(insertSide);
            PneumaticTubeBlockEntity insertTube = placePneumaticTube(level, insertTubePos);
            setTubeEjection(insertTube, insertSide.getOpposite(), "socket proxy pneumatic insertion ejection");
            ChestBlockEntity insertSource = placeChest(level, insertTubePos.relative(insertSide));
            insertSource.setItem(0, chargedBatteryStack(1L));
            setTubeInsertion(insertTube, insertSide, "socket proxy pneumatic insertion source");
            assertTrue(sendPneumaticOnce(level, insertTube),
                    "pneumatic tube inserted battery through socket proxy footprint");
            assertBatteryCharge(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY), 1L,
                    "socket received pneumatic battery through proxy");
            assertTrue(insertSource.getItem(0).isEmpty(), "socket proxy pneumatic source chest emptied");
            clearSocketAndPneumatic(level, socket, insertTubePos, insertTubePos.relative(insertSide));

            BlockPos outputProxy = footprint.get(2);
            Direction outputSide = openHorizontalDirection(outputProxy, footprintSet);
            BlockPos outputTubePos = outputProxy.relative(outputSide);
            PneumaticTubeBlockEntity outputTube = placePneumaticTube(level, outputTubePos);
            setTubeInsertion(outputTube, outputSide.getOpposite(), "socket proxy pneumatic output insertion");
            ChestBlockEntity outputDest = placeChest(level, outputTubePos.relative(outputSide));
            setTubeEjection(outputTube, outputSide, "socket proxy pneumatic output ejection");
            cycleSocketRedLowToMode(socket, MachineBatterySocketBlockEntity.MODE_OUTPUT);
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, chargedBatteryStack(0L));
            assertTrue(sendPneumaticOnce(level, outputTube),
                    "pneumatic tube extracted empty battery through socket proxy output mode");
            assertTrue(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY).isEmpty(),
                    "socket output-mode pneumatic extraction emptied slot");
            assertBatteryCharge(outputDest.getItem(0), 0L,
                    "socket output-mode pneumatic destination received empty battery");
            clearSocketAndPneumatic(level, socket, outputTubePos, outputTubePos.relative(outputSide));

            BlockPos inputProxy = footprint.get(3);
            Direction inputSide = openHorizontalDirection(inputProxy, footprintSet);
            BlockPos inputTubePos = inputProxy.relative(inputSide);
            PneumaticTubeBlockEntity inputTube = placePneumaticTube(level, inputTubePos);
            setTubeInsertion(inputTube, inputSide.getOpposite(), "socket proxy pneumatic input insertion");
            ChestBlockEntity inputDest = placeChest(level, inputTubePos.relative(inputSide));
            setTubeEjection(inputTube, inputSide, "socket proxy pneumatic input ejection");
            cycleSocketRedLowToMode(socket, MachineBatterySocketBlockEntity.MODE_INPUT);
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, fullBattery.copy());
            assertTrue(sendPneumaticOnce(level, inputTube),
                    "pneumatic tube extracted full battery through socket proxy input mode");
            assertTrue(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY).isEmpty(),
                    "socket input-mode pneumatic extraction emptied slot");
            assertBatteryCharge(inputDest.getItem(0), maxCharge,
                    "socket input-mode pneumatic destination received full battery");
            clearSocketAndPneumatic(level, socket, inputTubePos, inputTubePos.relative(inputSide));

            Direction blockedSide = openHorizontalDirection(corePos, footprintSet);
            BlockPos blockedTubePos = corePos.relative(blockedSide);
            PneumaticTubeBlockEntity blockedTube = placePneumaticTube(level, blockedTubePos);
            setTubeInsertion(blockedTube, blockedSide.getOpposite(), "socket core pneumatic blocked insertion");
            ChestBlockEntity blockedDest = placeChest(level, blockedTubePos.relative(blockedSide));
            setTubeEjection(blockedTube, blockedSide, "socket core pneumatic blocked ejection");
            socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, chargedBatteryStack(1L));
            assertTrue(!sendPneumaticOnce(level, blockedTube),
                    "pneumatic tube cannot extract partial battery through socket core in input mode");
            assertTrue(blockedDest.getItem(0).isEmpty(),
                    "blocked socket pneumatic destination stays empty");
            assertBatteryCharge(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY), 1L,
                    "partial socket battery remains after blocked pneumatic extraction");
            clearSocketAndPneumatic(level, socket, blockedTubePos, blockedTubePos.relative(blockedSide));
        } finally {
            PneumaticNodespace.unloadLevel(level);
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketPneumaticServerTickMovesAcrossMultitubeProxyFootprint(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        PneumaticNodespace.unloadLevel(level);
        try {
            BlockPos corePos = helper.absolutePos(new BlockPos(9, 4, 9));
            Direction facing = Direction.SOUTH;
            MachineBatterySocketBlockEntity socket = placeBatterySocket(level, corePos, facing);
            List<BlockPos> footprint = socketFootprintPositions(corePos, facing);
            Set<BlockPos> footprintSet = new LinkedHashSet<>(footprint);
            assertSocketFootprintProxiesResolve(level, socket, footprint);

            BlockPos insertProxy = footprint.get(1);
            Direction insertSide = openHorizontalDirection(insertProxy, footprintSet);
            BlockPos endpointPos = insertProxy.relative(insertSide);
            int sourceDistance = 4;
            BlockPos sourceTubePos = endpointPos.relative(insertSide, sourceDistance);
            ChestBlockEntity sourceChest = placeChest(level, sourceTubePos.relative(insertSide));
            sourceChest.setItem(0, chargedBatteryStack(1L));

            PneumaticTubeBlockEntity sourceTube = null;
            PneumaticTubeBlockEntity endpointTube = null;
            for (int offset = 0; offset <= sourceDistance; offset++) {
                PneumaticTubeBlockEntity tube = placePneumaticTube(level, endpointPos.relative(insertSide, offset));
                if (offset == 0) {
                    endpointTube = tube;
                }
                if (offset == sourceDistance) {
                    sourceTube = tube;
                }
            }
            if (sourceTube == null || endpointTube == null) {
                throw new AssertionError("socket multitube test did not create source and endpoint tubes");
            }

            setTubeInsertion(sourceTube, insertSide, "socket multitube source insertion");
            sourceTube.compair().setFill(PneumaticTubeBlockEntity.AIR_COST_PER_SEND);
            setTubeEjection(endpointTube, insertSide.getOpposite(), "socket multitube endpoint ejection");
            for (int offset = 0; offset <= sourceDistance; offset++) {
                requirePneumaticTube(level, endpointPos.relative(insertSide, offset)).refreshPneumaticNode();
            }
            PneumaticNodespace.tick(level);
            assertEquals(1, PneumaticNodespace.getNetworkCount(level),
                    "socket multitube pneumatic line forms one joined network");

            PneumaticTubeBlockEntity finalSourceTube = sourceTube;
            helper.onEachTick(() -> {
                ItemStack socketStack = socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY);
                if (socketStack.getItem() != ModItems.BATTERY_REDSTONE.get()
                        || !sourceChest.getItem(0).isEmpty()
                        || finalSourceTube.compair().getFill() != 0) {
                    return;
                }
                assertBatteryCharge(socketStack, 1L,
                        "socket multitube server tick transferred into battery_socket proxy footprint");
                assertTrue(sourceChest.getItem(0).isEmpty(),
                        "socket multitube server tick emptied source chest");
                assertEquals(0, finalSourceTube.compair().getFill(),
                        "socket multitube server tick consumed one air send cost");
                PneumaticNodespace.unloadLevel(level);
                HbmEnergyNodespace.unloadLevel(level);
                helper.succeed();
            });
        } catch (Throwable throwable) {
            PneumaticNodespace.unloadLevel(level);
            HbmEnergyNodespace.unloadLevel(level);
            throw throwable;
        }
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketProxyMenuCoordinatesResolveCore(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
            Direction facing = Direction.SOUTH;
            BlockState state = ModBlocks.MACHINE_BATTERY_SOCKET.get()
                    .defaultBlockState()
                    .setValue(MachineBatterySocketBlock.FACING, facing);
            level.setBlock(corePos, state, Block.UPDATE_ALL);
            state.getBlock().setPlacedBy(level, corePos, state, null, ItemStack.EMPTY);
            MachineBatterySocketBlockEntity socket = requireMachineBatterySocket(level, corePos);

            BlockPos proxyPos = corePos.offset(MachineBatterySocketBlock.socketProxyOffsets(facing).get(0));
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "battery_socket proxy dummy exists for menu coordinate test");

            var player = FakePlayerFactory.getMinecraft(level);
            player.setPos(corePos.getX() + 0.5D, corePos.getY() + 0.5D, corePos.getZ() + 0.5D);
            MachineBatterySocketMenu coreMenu = new MachineBatterySocketMenu(1, player.getInventory(),
                    menuPosBuffer(corePos));
            assertSame(socket, coreMenu.getBlockEntity(), "battery_socket core packet coordinate resolves to core menu");
            assertTrue(coreMenu.stillValid(player), "battery_socket core menu remains valid for nearby player");
            assertEquals(37, coreMenu.slots.size(), "battery_socket menu keeps one machine slot plus player inventory");

            MachineBatterySocketMenu proxyMenu = new MachineBatterySocketMenu(2, player.getInventory(),
                    menuPosBuffer(proxyPos));
            assertSame(socket, proxyMenu.getBlockEntity(), "battery_socket proxy packet coordinate resolves to core menu");
            assertTrue(proxyMenu.stillValid(player), "battery_socket proxy menu remains valid through resolved core");
            assertEquals(37, proxyMenu.slots.size(), "battery_socket proxy menu keeps legacy slot layout");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void batterySocketCoreAndProxyUseOpenCoreMenu(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
            Direction facing = Direction.SOUTH;
            MachineBatterySocketBlockEntity socket = placeBatterySocket(level, corePos, facing);
            BlockPos proxyPos = corePos.offset(MachineBatterySocketBlock.socketProxyOffsets(facing).get(0));
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "battery_socket proxy dummy exists for real use forwarding test");

            var player = FakePlayerFactory.getMinecraft(level);
            player.setPos(corePos.getX() + 0.5D, corePos.getY() + 0.5D, corePos.getZ() + 0.5D);

            BlockState coreState = level.getBlockState(corePos);
            var coreResult = coreState.use(level, player, InteractionHand.MAIN_HAND, blockHit(corePos));
            assertTrue(coreResult.consumesAction(), "battery_socket core right-click is consumed");
            assertTrue(player.containerMenu instanceof MachineBatterySocketMenu,
                    "battery_socket core right-click opens socket menu");
            assertSame(socket, ((MachineBatterySocketMenu) player.containerMenu).getBlockEntity(),
                    "battery_socket core right-click menu targets the core block entity");
            player.closeContainer();

            BlockState proxyState = level.getBlockState(proxyPos);
            var proxyResult = proxyState.use(level, player, InteractionHand.MAIN_HAND, blockHit(proxyPos));
            assertTrue(proxyResult.consumesAction(), "battery_socket proxy right-click is consumed");
            assertTrue(player.containerMenu instanceof MachineBatterySocketMenu,
                    "battery_socket proxy right-click opens socket menu through NetworkHooks");
            assertSame(socket, ((MachineBatterySocketMenu) player.containerMenu).getBlockEntity(),
                    "battery_socket proxy right-click menu resolves to the core block entity");
            player.closeContainer();
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatteryMenuQuickMoveKeepsLegacyTransferShape(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos batteryPos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(batteryPos.getX() + 0.5D, batteryPos.getY() + 0.5D, batteryPos.getZ() + 0.5D);
        player.getInventory().clearContent();
        MachineBatteryMenu menu = new MachineBatteryMenu(1, player.getInventory(), battery);
        assertEquals(38, menu.slots.size(), "machine_battery menu keeps two machine slots plus player inventory");
        assertTrue(menu.stillValid(player), "machine_battery menu is valid for a nearby player");

        player.getInventory().setItem(9, new ItemStack(Blocks.STONE, 16));
        ItemStack movedStoneToMachine = menu.quickMoveStack(player, 2);
        assertSame(Blocks.STONE.asItem(), movedStoneToMachine.getItem(),
                "machine_battery shift-click from player returns the moved stack copy");
        assertEquals(16, movedStoneToMachine.getCount(),
                "machine_battery shift-click preserves source stack count in the return copy");
        assertSame(Blocks.STONE.asItem(), battery.getItems()
                        .getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE).getItem(),
                "machine_battery player shift-click fills legacy slot 0 first");
        assertEquals(16, battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE).getCount(),
                "machine_battery player shift-click moves the whole stack into slot 0");
        assertTrue(player.getInventory().getItem(9).isEmpty(),
                "machine_battery player source slot clears after slot-0 quick move");

        ItemStack fullBattery = chargedBatteryStack(requireChargeable(chargedBatteryStack(0L), "battery_redstone")
                .getMaxCharge(chargedBatteryStack(0L)));
        player.getInventory().setItem(10, fullBattery.copy());
        ItemStack movedBatteryToMachine = menu.quickMoveStack(player, 3);
        assertSame(fullBattery.getItem(), movedBatteryToMachine.getItem(),
                "machine_battery second player quick move returns battery stack copy");
        assertSame(fullBattery.getItem(), battery.getItems()
                        .getStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE).getItem(),
                "machine_battery player shift-click fills legacy slot 1 after slot 0 is occupied");
        assertTrue(player.getInventory().getItem(10).isEmpty(),
                "machine_battery player source slot clears after slot-1 quick move");

        player.getInventory().clearContent();
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, new ItemStack(Blocks.STONE, 3));
        ItemStack movedStoneToPlayer = menu.quickMoveStack(player, 0);
        assertSame(Blocks.STONE.asItem(), movedStoneToPlayer.getItem(),
                "machine_battery machine-slot quick move returns moved stack copy");
        assertTrue(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE).isEmpty(),
                "machine_battery shift-click from machine clears legacy slot 0");
        assertEquals(3, player.getInventory().getItem(8).getCount(),
                "machine_battery machine-slot shift-click merges into player inventory in reverse legacy order");
        assertSame(Blocks.STONE.asItem(), player.getInventory().getItem(8).getItem(),
                "machine_battery machine-slot shift-click targets the hotbar end first");

        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE, fullBattery.copy());
        ItemStack movedBatteryToPlayer = menu.quickMoveStack(player, 1);
        assertSame(fullBattery.getItem(), movedBatteryToPlayer.getItem(),
                "machine_battery slot-1 quick move returns moved battery copy");
        assertTrue(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE).isEmpty(),
                "machine_battery shift-click from machine clears legacy slot 1");
        assertSame(fullBattery.getItem(), player.getInventory().getItem(7).getItem(),
                "machine_battery second machine-slot shift-click uses the next reverse hotbar slot");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatteryControlButtonsKeepLegacyCycles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos batteryPos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        var player = FakePlayerFactory.getMinecraft(level);

        assertEquals(MachineBatteryBlockEntity.MODE_INPUT, battery.getRedLow(),
                "machine_battery default low-redstone mode is input");
        assertEquals(MachineBatteryBlockEntity.MODE_OUTPUT, battery.getRedHigh(),
                "machine_battery default high-redstone mode is output");
        assertSame(HbmEnergyReceiver.ConnectionPriority.LOW, battery.getBatteryPriority(),
                "machine_battery default priority is legacy low");

        battery.handleClientControl(player,
                MachineBatteryBlockEntity.controlTag(MachineBatteryBlockEntity.CONTROL_RED_LOW));
        assertEquals(MachineBatteryBlockEntity.MODE_BUFFER, battery.getRedLow(),
                "machine_battery redLow button cycles input -> buffer");
        battery.handleClientControl(player,
                MachineBatteryBlockEntity.controlTag(MachineBatteryBlockEntity.CONTROL_RED_HIGH));
        assertEquals(MachineBatteryBlockEntity.MODE_NONE, battery.getRedHigh(),
                "machine_battery redHigh button cycles output -> none");
        battery.handleClientControl(player,
                MachineBatteryBlockEntity.controlTag(MachineBatteryBlockEntity.CONTROL_PRIORITY));
        assertSame(HbmEnergyReceiver.ConnectionPriority.NORMAL, battery.getBatteryPriority(),
                "machine_battery priority button cycles low -> normal");
        battery.handleClientControl(player,
                MachineBatteryBlockEntity.controlTag(MachineBatteryBlockEntity.CONTROL_PRIORITY));
        assertSame(HbmEnergyReceiver.ConnectionPriority.HIGH, battery.getBatteryPriority(),
                "machine_battery priority button cycles normal -> high");
        battery.handleClientControl(player,
                MachineBatteryBlockEntity.controlTag(MachineBatteryBlockEntity.CONTROL_PRIORITY));
        assertSame(HbmEnergyReceiver.ConnectionPriority.LOW, battery.getBatteryPriority(),
                "machine_battery priority button cycles high -> low");

        CompoundTag lowTag = new CompoundTag();
        lowTag.putBoolean("low", true);
        battery.receiveControl(player, lowTag);
        assertEquals(MachineBatteryBlockEntity.MODE_OUTPUT, battery.getRedLow(),
                "machine_battery legacy low NBT control cycles buffer -> output");
        CompoundTag highTag = new CompoundTag();
        highTag.putBoolean("high", true);
        battery.receiveControl(player, highTag);
        assertEquals(MachineBatteryBlockEntity.MODE_INPUT, battery.getRedHigh(),
                "machine_battery legacy high NBT control cycles none -> input");
        CompoundTag priorityTag = new CompoundTag();
        priorityTag.putBoolean("priority", true);
        battery.receiveControl(player, priorityTag);
        assertSame(HbmEnergyReceiver.ConnectionPriority.NORMAL, battery.getBatteryPriority(),
                "machine_battery legacy priority NBT control cycles low -> normal");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatteryPersistentDropKeepsLegacyNbtShape(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos firstPos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockState state = ModBlocks.MACHINE_BATTERY.get().defaultBlockState();
        level.setBlock(firstPos, state, Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, firstPos);
        long storedPower = 543_210L;
        long previousPower = 123_456L;
        battery.setPower(storedPower);
        battery.readPersistentState(legacyMachineBatteryPersistentTag(storedPower, previousPower,
                MachineBatteryBlockEntity.MODE_BUFFER, MachineBatteryBlockEntity.MODE_NONE,
                HbmEnergyReceiver.ConnectionPriority.HIGH.ordinal()));
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, new ItemStack(Blocks.STONE, 7));

        ItemStack drop = battery.createPersistentBlockDrop(ModBlocks.MACHINE_BATTERY.get().asItem());
        assertSame(ModBlocks.MACHINE_BATTERY.get().asItem(), drop.getItem(),
                "machine_battery persistent drop keeps the block item");
        CompoundTag root = drop.getTag();
        assertTrue(root != null && root.contains(HbmPersistentBlockState.TAG_PERSISTENT, Tag.TAG_COMPOUND),
                "machine_battery persistent drop carries legacy persistent compound");
        CompoundTag persistent = root.getCompound(HbmPersistentBlockState.TAG_PERSISTENT);
        assertEquals(storedPower, persistent.getLong("power"),
                "machine_battery persistent drop writes legacy power key");
        assertEquals(previousPower, persistent.getLong("prevPowerState"),
                "machine_battery persistent drop writes legacy previous power key");
        assertEquals(MachineBatteryBlockEntity.MODE_BUFFER, persistent.getShort("redLow"),
                "machine_battery persistent drop writes legacy redLow key");
        assertEquals(MachineBatteryBlockEntity.MODE_NONE, persistent.getShort("redHigh"),
                "machine_battery persistent drop writes legacy redHigh key");
        assertEquals(HbmEnergyReceiver.ConnectionPriority.HIGH.ordinal(), persistent.getInt("priority"),
                "machine_battery persistent drop writes legacy priority ordinal");
        assertTrue(!persistent.contains("lastRedstone"),
                "machine_battery persistent drop does not promote full tile lastRedstone into IPersistentNBT data");
        assertTrue(!persistent.contains("Inventory"),
                "machine_battery persistent drop keeps inventory outside IPersistentNBT data");

        List<ItemStack> inventoryDrops = battery.getDrops();
        assertEquals(1, inventoryDrops.size(), "machine_battery break drops inventory separately");
        assertSame(Blocks.STONE.asItem(), inventoryDrops.get(0).getItem(),
                "machine_battery break inventory drop keeps the item");
        assertEquals(7, inventoryDrops.get(0).getCount(),
                "machine_battery break inventory drop keeps the count");
        assertTrue(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE).isEmpty(),
                "machine_battery break inventory drop clears the source slot");

        BlockPos secondPos = helper.absolutePos(new BlockPos(3, 2, 1));
        level.setBlock(secondPos, state, Block.UPDATE_ALL);
        state.getBlock().setPlacedBy(level, secondPos, state, null, drop);
        MachineBatteryBlockEntity restored = requireMachineBattery(level, secondPos);
        assertEquals(storedPower, restored.getPower(),
                "machine_battery persistent drop restores legacy power");
        assertEquals(MachineBatteryBlockEntity.MODE_BUFFER, restored.getRedLow(),
                "machine_battery persistent drop restores redLow");
        assertEquals(MachineBatteryBlockEntity.MODE_NONE, restored.getRedHigh(),
                "machine_battery persistent drop restores redHigh");
        assertSame(HbmEnergyReceiver.ConnectionPriority.HIGH, restored.getBatteryPriority(),
                "machine_battery persistent drop restores priority");
        assertTrue(restored.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE).isEmpty(),
                "machine_battery persistent drop does not restore inventory into the block item");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatterySidedAutomationKeepsLegacySlotRules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(pos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, pos);

        IItemHandler top = itemHandler(level, pos, Direction.UP, "machine_battery top");
        IItemHandler bottom = itemHandler(level, pos, Direction.DOWN, "machine_battery bottom");
        IItemHandler side = itemHandler(level, pos, Direction.EAST, "machine_battery side");
        assertEquals(1, top.getSlots(), "machine_battery top exposes discharge slot only");
        assertEquals(2, bottom.getSlots(), "machine_battery bottom exposes both battery slots");
        assertEquals(1, side.getSlots(), "machine_battery side exposes charge slot only");

        ItemStack emptyBattery = chargedBatteryStack(0L);
        ItemStack fullBattery = chargedBatteryStack(requireChargeable(emptyBattery, "battery_redstone")
                .getMaxCharge(emptyBattery));
        ItemStack partialBattery = chargedBatteryStack(1L);
        assertTrue(top.insertItem(0, emptyBattery.copy(), true).isEmpty(),
                "machine_battery top can insert into legacy slot 0");
        assertTrue(side.insertItem(0, fullBattery.copy(), true).isEmpty(),
                "machine_battery side can insert into legacy slot 1");
        assertTrue(bottom.insertItem(0, emptyBattery.copy(), true).isEmpty(),
                "machine_battery bottom can insert into legacy slot 0");
        assertTrue(bottom.insertItem(1, fullBattery.copy(), true).isEmpty(),
                "machine_battery bottom can insert into legacy slot 1");
        assertTrue(top.insertItem(0, new ItemStack(Blocks.STONE), true).isEmpty(),
                "machine_battery keeps legacy broad isItemValidForSlot insertion");

        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, partialBattery.copy());
        assertTrue(top.extractItem(0, 1, true).isEmpty(),
                "machine_battery slot 0 non-empty battery cannot be extracted from top");
        assertTrue(bottom.extractItem(0, 1, true).isEmpty(),
                "machine_battery slot 0 non-empty battery cannot be extracted from bottom");
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, emptyBattery.copy());
        assertSame(emptyBattery.getItem(), top.extractItem(0, 1, true).getItem(),
                "machine_battery slot 0 empty battery extracts from top");
        assertSame(emptyBattery.getItem(), bottom.extractItem(0, 1, true).getItem(),
                "machine_battery slot 0 empty battery extracts from bottom");

        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE, partialBattery.copy());
        assertTrue(side.extractItem(0, 1, true).isEmpty(),
                "machine_battery slot 1 non-full battery cannot be extracted from side");
        assertTrue(bottom.extractItem(1, 1, true).isEmpty(),
                "machine_battery slot 1 non-full battery cannot be extracted from bottom");
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE, fullBattery.copy());
        assertSame(fullBattery.getItem(), side.extractItem(0, 1, true).getItem(),
                "machine_battery slot 1 full battery extracts from side");
        assertSame(fullBattery.getItem(), bottom.extractItem(1, 1, true).getItem(),
                "machine_battery slot 1 full battery extracts from bottom");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatteryRealHoppersFollowLegacySidedAutomation(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos batteryPos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);

        BlockPos topHopperPos = batteryPos.above();
        HopperBlockEntity topHopper = placeHopper(level, topHopperPos, Direction.DOWN);
        topHopper.setItem(0, new ItemStack(Blocks.STONE));
        tickHopper(level, topHopperPos);
        assertSame(Blocks.STONE.asItem(), battery.getItems()
                .getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE).getItem(),
                "real top hopper inserts through legacy slot 0");
        assertTrue(topHopper.getItem(0).isEmpty(), "real top hopper transferred broad-valid stack");
        clearBatteryAndHopper(level, battery, topHopperPos);

        ItemStack emptyForMax = chargedBatteryStack(0L);
        ItemStack fullBattery = chargedBatteryStack(requireChargeable(emptyForMax, "battery_redstone")
                .getMaxCharge(emptyForMax));
        BlockPos sideHopperPos = batteryPos.east();
        HopperBlockEntity sideHopper = placeHopper(level, sideHopperPos, Direction.WEST);
        sideHopper.setItem(0, fullBattery.copy());
        tickHopper(level, sideHopperPos);
        assertSame(fullBattery.getItem(), battery.getItems()
                .getStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE).getItem(),
                "real side hopper inserts through legacy slot 1");
        assertTrue(sideHopper.getItem(0).isEmpty(), "real side hopper transferred full battery");
        clearBatteryAndHopper(level, battery, sideHopperPos);

        BlockPos bottomHopperPos = batteryPos.below();
        HopperBlockEntity bottomHopper = placeHopper(level, bottomHopperPos, Direction.DOWN);
        ItemStack emptyBattery = chargedBatteryStack(0L);
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, emptyBattery.copy());
        tickHopper(level, bottomHopperPos);
        assertTrue(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE).isEmpty(),
                "real bottom hopper extracts empty battery from legacy slot 0");
        assertBatteryCharge(bottomHopper.getItem(0), 0L, "real bottom hopper extracted empty slot-0 battery");
        clearBatteryAndHopper(level, battery, bottomHopperPos);

        bottomHopper = placeHopper(level, bottomHopperPos, Direction.DOWN);
        ItemStack partialBattery = chargedBatteryStack(1L);
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, partialBattery.copy());
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE, fullBattery.copy());
        tickHopper(level, bottomHopperPos);
        assertBatteryCharge(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE), 1L,
                "real bottom hopper skips non-empty slot-0 battery");
        assertTrue(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE).isEmpty(),
                "real bottom hopper extracts full battery from legacy slot 1");
        assertBatteryCharge(bottomHopper.getItem(0), requireChargeable(fullBattery, "battery_redstone").getMaxCharge(fullBattery),
                "real bottom hopper extracted full slot-1 battery");
        clearBatteryAndHopper(level, battery, bottomHopperPos);

        bottomHopper = placeHopper(level, bottomHopperPos, Direction.DOWN);
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE, partialBattery.copy());
        tickHopper(level, bottomHopperPos);
        assertTrue(bottomHopper.getItem(0).isEmpty(), "real bottom hopper cannot extract non-full slot-1 battery");
        assertBatteryCharge(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE), 1L,
                "real bottom hopper leaves non-full slot-1 battery in place");
        clearBatteryAndHopper(level, battery, bottomHopperPos);

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatteryRealPneumaticTubesFollowLegacySidedAutomation(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos batteryPos = helper.absolutePos(new BlockPos(3, 3, 3));
        level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        PneumaticNodespace.unloadLevel(level);

        try {
            BlockPos topTubePos = batteryPos.above();
            PneumaticTubeBlockEntity topTube = placePneumaticTube(level, topTubePos);
            setTubeEjection(topTube, Direction.DOWN, "top pneumatic tube ejection");
            ChestBlockEntity topSource = placeChest(level, topTubePos.north());
            topSource.setItem(0, new ItemStack(Blocks.STONE));
            setTubeInsertion(topTube, Direction.NORTH, "top pneumatic tube insertion");
            assertTrue(sendPneumaticOnce(level, topTube), "top pneumatic tube transferred into machine_battery");
            assertSame(Blocks.STONE.asItem(), battery.getItems()
                    .getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE).getItem(),
                    "pneumatic top insertion targets legacy slot 0");
            assertTrue(topSource.getItem(0).isEmpty(), "pneumatic top source chest emptied");
            clearBatteryAndPneumatic(level, battery, topTubePos, topTubePos.north());

            ItemStack emptyForMax = chargedBatteryStack(0L);
            ItemStack fullBattery = chargedBatteryStack(requireChargeable(emptyForMax, "battery_redstone")
                    .getMaxCharge(emptyForMax));
            BlockPos sideTubePos = batteryPos.east();
            PneumaticTubeBlockEntity sideTube = placePneumaticTube(level, sideTubePos);
            setTubeEjection(sideTube, Direction.WEST, "side pneumatic tube ejection");
            ChestBlockEntity sideSource = placeChest(level, sideTubePos.north());
            sideSource.setItem(0, fullBattery.copy());
            setTubeInsertion(sideTube, Direction.NORTH, "side pneumatic tube insertion");
            assertTrue(sendPneumaticOnce(level, sideTube), "side pneumatic tube transferred into machine_battery");
            assertSame(fullBattery.getItem(), battery.getItems()
                    .getStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE).getItem(),
                    "pneumatic side insertion targets legacy slot 1");
            assertTrue(sideSource.getItem(0).isEmpty(), "pneumatic side source chest emptied");
            clearBatteryAndPneumatic(level, battery, sideTubePos, sideTubePos.north());

            BlockPos bottomTubePos = batteryPos.below();
            PneumaticTubeBlockEntity bottomTube = placePneumaticTube(level, bottomTubePos);
            setTubeInsertion(bottomTube, Direction.UP, "bottom pneumatic tube insertion");
            ChestBlockEntity bottomDest = placeChest(level, bottomTubePos.south());
            setTubeEjection(bottomTube, Direction.SOUTH, "bottom pneumatic tube ejection");
            ItemStack emptyBattery = chargedBatteryStack(0L);
            battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, emptyBattery.copy());
            assertTrue(sendPneumaticOnce(level, bottomTube), "bottom pneumatic tube extracted empty slot-0 battery");
            assertTrue(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE).isEmpty(),
                    "pneumatic bottom extraction empties legacy slot 0");
            assertBatteryCharge(bottomDest.getItem(0), 0L, "pneumatic bottom extracted empty slot-0 battery");
            clearBatteryAndPneumatic(level, battery, bottomTubePos, bottomTubePos.south());

            bottomTube = placePneumaticTube(level, bottomTubePos);
            setTubeInsertion(bottomTube, Direction.UP, "bottom pneumatic tube insertion for full slot 1");
            bottomDest = placeChest(level, bottomTubePos.south());
            setTubeEjection(bottomTube, Direction.SOUTH, "bottom pneumatic tube ejection for full slot 1");
            ItemStack partialBattery = chargedBatteryStack(1L);
            battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, partialBattery.copy());
            battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE, fullBattery.copy());
            assertTrue(sendPneumaticOnce(level, bottomTube), "bottom pneumatic tube skipped slot 0 and extracted full slot 1");
            assertBatteryCharge(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE), 1L,
                    "pneumatic bottom leaves non-empty slot-0 battery in place");
            assertTrue(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE).isEmpty(),
                    "pneumatic bottom extracts full slot-1 battery");
            assertBatteryCharge(bottomDest.getItem(0), requireChargeable(fullBattery, "battery_redstone").getMaxCharge(fullBattery),
                    "pneumatic bottom extracted full slot-1 battery");
            clearBatteryAndPneumatic(level, battery, bottomTubePos, bottomTubePos.south());

            bottomTube = placePneumaticTube(level, bottomTubePos);
            setTubeInsertion(bottomTube, Direction.UP, "bottom pneumatic tube insertion for non-full slot 1");
            bottomDest = placeChest(level, bottomTubePos.south());
            setTubeEjection(bottomTube, Direction.SOUTH, "bottom pneumatic tube ejection for non-full slot 1");
            battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE, partialBattery.copy());
            assertTrue(!sendPneumaticOnce(level, bottomTube),
                    "bottom pneumatic tube cannot extract non-full slot-1 battery");
            assertTrue(bottomDest.getItem(0).isEmpty(), "pneumatic destination chest stays empty for non-full slot 1");
            assertBatteryCharge(battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE), 1L,
                    "pneumatic bottom leaves non-full slot-1 battery in place");
            clearBatteryAndPneumatic(level, battery, bottomTubePos, bottomTubePos.south());
        } finally {
            PneumaticNodespace.unloadLevel(level);
        }

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatteryPneumaticServerTickMovesAcrossMultitubeNetwork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos batteryPos = helper.absolutePos(new BlockPos(9, 3, 3));
        level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        PneumaticNodespace.unloadLevel(level);

        BlockPos endpointPos = batteryPos.above();
        int sourceDistance = 4;
        BlockPos sourcePos = endpointPos.west(sourceDistance);
        ChestBlockEntity sourceChest = placeChest(level, sourcePos.west());
        sourceChest.setItem(0, new ItemStack(Blocks.STONE));

        PneumaticTubeBlockEntity sourceTube = null;
        PneumaticTubeBlockEntity endpointTube = null;
        for (int offset = 0; offset <= sourceDistance; offset++) {
            PneumaticTubeBlockEntity tube = placePneumaticTube(level, sourcePos.east(offset));
            if (offset == 0) {
                sourceTube = tube;
            }
            if (offset == sourceDistance) {
                endpointTube = tube;
            }
        }
        if (sourceTube == null || endpointTube == null) {
            throw new AssertionError("pneumatic multitube test did not create source and endpoint tubes");
        }

        setTubeInsertion(sourceTube, Direction.WEST, "multitube source insertion");
        sourceTube.compair().setFill(PneumaticTubeBlockEntity.AIR_COST_PER_SEND);
        setTubeEjection(endpointTube, Direction.DOWN, "multitube endpoint ejection");
        for (int offset = 0; offset <= sourceDistance; offset++) {
            requirePneumaticTube(level, sourcePos.east(offset)).refreshPneumaticNode();
        }
        PneumaticNodespace.tick(level);
        assertEquals(1, PneumaticNodespace.getNetworkCount(level),
                "multitube pneumatic line forms one joined network");

        PneumaticTubeBlockEntity finalSourceTube = sourceTube;
        helper.onEachTick(() -> {
            ItemStack discharge = battery.getItems().getStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE);
            if (discharge.getItem() != Blocks.STONE.asItem()
                    || !sourceChest.getItem(0).isEmpty()
                    || finalSourceTube.compair().getFill() != 0) {
                return;
            }
            assertSame(Blocks.STONE.asItem(), discharge.getItem(),
                    "multitube server tick transferred into machine_battery slot 0");
            assertTrue(sourceChest.getItem(0).isEmpty(), "multitube server tick emptied source chest");
            assertEquals(0, finalSourceTube.compair().getFill(), "multitube server tick consumed one air send cost");
            PneumaticNodespace.unloadLevel(level);
            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void nukeElectricStarterKitKeepsLegacyBatteryAndCanisterOutputs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = FakePlayerFactory.getMinecraft(level);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        clearDroppedItemsAround(level, player);

        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.NUKE_ELECTRIC_KIT.get()));
        ModItems.NUKE_ELECTRIC_KIT.get().use(level, player, InteractionHand.MAIN_HAND);
        assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(),
                "nuke_electric_kit right-click consumes the kit");
        assertEquals(1, inventoryItemCount(player, ModItems.BATTERY_POTATO.get()),
                "nuke_electric_kit gives legacy battery_potato");
        assertEquals(4, inventoryItemCount(player, ModItems.BATTERY_LEAD.get()),
                "nuke_electric_kit maps lead battery_pack meta to modern battery_lead");
        assertEquals(4, inventoryItemCount(player, ModBlocks.MACHINE_BATTERY_SOCKET.get().asItem()),
                "nuke_electric_kit keeps machine_battery_socket output");
        assertEquals(16, inventoryFluidContainerCount(player, HbmFluids.DIESEL),
                "nuke_electric_kit gives DIESEL canisters");
        assertEquals(16, inventoryFluidContainerCount(player, HbmFluids.BIOFUEL),
                "nuke_electric_kit gives BIOFUEL canisters");

        player.getInventory().clearContent();
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            player.getInventory().items.set(slot, new ItemStack(Items.STONE, 64));
        }
        clearDroppedItemsAround(level, player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.NUKE_ELECTRIC_KIT.get()));
        ModItems.NUKE_ELECTRIC_KIT.get().use(level, player, InteractionHand.MAIN_HAND);
        assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty(),
                "full-inventory nuke_electric_kit still consumes the kit");
        assertEquals(0, inventoryItemCount(player, ModItems.BATTERY_POTATO.get()),
                "full-inventory nuke_electric_kit cannot insert battery_potato");
        assertEquals(1, droppedItemCount(level, player, ModItems.BATTERY_POTATO.get()),
                "full-inventory nuke_electric_kit drops battery_potato remainder");
        assertEquals(4, droppedItemCount(level, player, ModItems.BATTERY_LEAD.get()),
                "full-inventory nuke_electric_kit drops battery_lead remainder");
        assertEquals(16, droppedFluidContainerCount(level, player, HbmFluids.DIESEL),
                "full-inventory nuke_electric_kit drops DIESEL canister remainder");
        assertEquals(16, droppedFluidContainerCount(level, player, HbmFluids.BIOFUEL),
                "full-inventory nuke_electric_kit drops BIOFUEL canister remainder");

        player.getInventory().clearContent();
        clearDroppedItemsAround(level, player);
        helper.succeed();
    }

    private static void legacyBatteryNumericItemStackNbtMigrationUsesWorldItemData() {
        CompoundTag levelDat = new CompoundTag();
        CompoundTag fml = new CompoundTag();
        ListTag itemData = new ListTag();
        itemData.add(legacyItemData("\u0002hbm:item.battery_pack", 4727));
        itemData.add(legacyItemData("\u0002hbm:item.battery_sc", 4728));
        itemData.add(legacyItemData("\u0001hbm:machine_battery", 1840));
        fml.put("ItemData", itemData);
        levelDat.put("FML", fml);

        LegacyWorldItemIdMap itemIds = LegacyWorldItemIdMap.fromLevelDatRoot(levelDat);
        assertEquals(3, itemIds.size(), "legacy item id map size");
        assertEquals("hbm:item.battery_pack", itemIds.legacyId(4727).orElseThrow(),
                "legacy battery_pack numeric id");
        LegacyWorldItemIdMap oldModItemIds = LegacyWorldItemIdMap.fromLevelDatRoot(legacyModItemDataLevelDat());
        assertEquals(2, oldModItemIds.size(), "legacy ModItemData id map size");
        assertEquals("hbm:item.battery_pack", oldModItemIds.legacyId(6725).orElseThrow(),
                "legacy ModItemData battery_pack numeric id");
        LegacyItemStackMigration.Result oldModItemDataResult =
                LegacyItemStackMigration.migrateAll(legacyNumericRoot(6725, 5), oldModItemIds);
        assertEquals(1, oldModItemDataResult.migrated(), "legacy ModItemData numeric stack migrated");

        CompoundTag root = new CompoundTag();
        ListTag items = new ListTag();
        CompoundTag quantum = legacyNumericStackTag(4727, 5, 1);
        CompoundTag quantumData = new CompoundTag();
        quantumData.putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, 1234L);
        quantum.put("tag", quantumData);
        items.add(quantum);
        items.add(legacyNumericStackTag(4728, 2, 1));
        items.add(legacyNumericStackTag(9999, 0, 1));
        root.put("Items", items);

        LegacyItemStackMigration.Result result = LegacyItemStackMigration.migrateAll(root, itemIds);
        assertEquals(2, result.migrated(), "numeric legacy battery stacks migrated");
        assertEquals(0, result.numericItemStacksWithoutMap(), "numeric map was available");
        assertEquals(1, result.unknownNumericItemStacks(), "unknown numeric id counted");
        assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(), items.getCompound(0).getString("id"),
                "numeric battery_pack meta 5 id");
        assertEquals(1234L, items.getCompound(0).getCompound("tag").getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG),
                "numeric legacy charge NBT preserved");
        assertEquals(ModItems.BATTERY_SC_RA226.getId().toString(), items.getCompound(1).getString("id"),
                "numeric battery_sc meta 2 id");
        assertEquals(9999, items.getCompound(2).getInt("id"), "unknown numeric id remains numeric");

        LegacyItemStackMigration.Result missingMap =
                LegacyItemStackMigration.migrateAll(legacyNumericRoot(4727, 5), LegacyWorldItemIdMap.empty());
        assertEquals(0, missingMap.migrated(), "numeric stack without map no-op");
        assertEquals(1, missingMap.numericItemStacksWithoutMap(), "numeric stack without map counted");

        CompoundTag chunkLikeRoot = legacyNumericRoot(4727, 5);
        BlockMigrationHelper.resetDiagnostics();
        BlockMigrationHelper.setLegacyItemIdsForTesting(itemIds);
        try {
            BlockMigrationHelper.doMigration(null, chunkLikeRoot, 0, 1);
            BlockMigrationHelper.MigrationDiagnostics diagnostics = BlockMigrationHelper.diagnostics();
            assertEquals(1L, diagnostics.migratedItemStacks(), "numeric chunk migration diagnostic count");
            assertEquals(0L, diagnostics.numericItemStacksWithoutMap(), "numeric chunk migration had map");
            assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(),
                    chunkLikeRoot.getList("Items", Tag.TAG_COMPOUND).getCompound(0).getString("id"),
                    "numeric chunk migration id");
        } finally {
            BlockMigrationHelper.setLegacyItemIdsForTesting(LegacyWorldItemIdMap.empty());
            BlockMigrationHelper.resetDiagnostics();
        }
    }

    private static CompoundTag legacyStackTag(String id, int damage, int count) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putByte("Count", (byte) count);
        tag.putShort("Damage", (short) damage);
        return tag;
    }

    private static CompoundTag legacyNumericStackTag(int id, int damage, int count) {
        CompoundTag tag = new CompoundTag();
        tag.putShort("id", (short) id);
        tag.putByte("Count", (byte) count);
        tag.putShort("Damage", (short) damage);
        return tag;
    }

    private static CompoundTag legacyNumericRoot(int id, int damage) {
        CompoundTag root = new CompoundTag();
        ListTag items = new ListTag();
        items.add(legacyNumericStackTag(id, damage, 1));
        root.put("Items", items);
        return root;
    }

    private static CompoundTag legacyItemData(String key, int value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("K", key);
        tag.putInt("V", value);
        return tag;
    }

    private static CompoundTag legacyModItemDataLevelDat() {
        CompoundTag levelDat = new CompoundTag();
        CompoundTag fml = new CompoundTag();
        ListTag modItemData = new ListTag();
        modItemData.add(legacyModItemData("hbm", "item.battery_pack", 6725));
        modItemData.add(legacyModItemData("hbm", "item.battery_sc", 6726));
        fml.put("ModItemData", modItemData);
        levelDat.put("FML", fml);
        return levelDat;
    }

    private static CompoundTag legacyModItemData(String modId, String forcedName, int itemId) {
        CompoundTag tag = new CompoundTag();
        tag.putString("ModId", modId);
        tag.putString("ItemType", "item");
        tag.putInt("ItemId", itemId);
        tag.putInt("ordinal", 0);
        tag.putString("ForcedName", forcedName);
        return tag;
    }

    private static void refreshEnergyNodeAt(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof HbmEnergyNodeHost host) {
            host.refreshEnergyNode();
            return;
        }
        throw new AssertionError("No HBM energy node host at " + pos);
    }

    private static HbmEnergyNode requireEnergyNodeAt(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof HbmEnergyNodeHost host
                && host.getEnergyNode() != null
                && !host.getEnergyNode().isExpired()) {
            return host.getEnergyNode();
        }
        throw new AssertionError("No live HBM energy node host at " + pos);
    }

    private static void refreshFluidNodeAt(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe) {
            pipe.refreshFluidNode();
            return;
        }
        throw new AssertionError("No HBM fluid pipe at " + pos);
    }

    private static void prepareDieselFluidPipeLine(ServerLevel level, BlockPos firstPipePos, BlockPos lastPipePos) {
        forceLoadedChunks(level, firstPipePos, lastPipePos);
        for (int x = firstPipePos.getX(); x <= lastPipePos.getX(); x++) {
            BlockPos pipePos = new BlockPos(x, firstPipePos.getY(), firstPipePos.getZ());
            level.removeBlock(pipePos, false);
            level.setBlock(pipePos, ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(pipePos) instanceof FluidPipeBlockEntity pipe) {
                pipe.setFluidType(HbmFluids.DIESEL);
            } else {
                throw new AssertionError("No fluid_duct_neo block entity at " + pipePos);
            }
        }
        for (int x = firstPipePos.getX(); x <= lastPipePos.getX(); x++) {
            refreshFluidNodeAt(level, new BlockPos(x, firstPipePos.getY(), firstPipePos.getZ()));
        }
        HbmFluidNodespace.forceRebuild(level);
        HbmFluidNodespace.tick(level);
        HbmFluidNodespace.Diagnostics joined = HbmFluidNodespace.getDiagnostics(level);
        assertTrue(joined.uniqueNodes() >= 4, "cross-chunk fluid_duct_neo diesel line node count");
        assertTrue(joined.networks() >= 1, "cross-chunk fluid_duct_neo diesel line joined network");
    }

    @SuppressWarnings("unchecked")
    private static List<HbmFluidUtil.FluidPort> fritzFluidPorts(TurretFritzBlockEntity turret) {
        try {
            Method method = TurretFritzBlockEntity.class.getDeclaredMethod("fluidPorts");
            method.setAccessible(true);
            return (List<HbmFluidUtil.FluidPort>) method.invoke(turret);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not inspect fluid ports on turret_fritz", exception);
        }
    }

    private static HbmFluidUtil.PortSnapshot requireSubscribedFluidPort(ServerLevel level, BlockPos origin,
            Iterable<HbmFluidUtil.FluidPort> ports, FluidType type) {
        for (HbmFluidUtil.FluidPort port : ports) {
            HbmFluidUtil.PortSnapshot snapshot = HbmFluidUtil.inspectPort(level, origin, port, type);
            if (snapshot.receivers() >= 1) {
                return snapshot;
            }
        }
        throw new AssertionError("No subscribed fluid receiver port found; ports="
                + HbmFluidUtil.inspectPorts(level, origin, ports, type));
    }

    private static void forceLoadedChunks(ServerLevel level, BlockPos first, BlockPos second) {
        int minChunkX = Math.min(first.getX(), second.getX()) >> 4;
        int maxChunkX = Math.max(first.getX(), second.getX()) >> 4;
        int minChunkZ = Math.min(first.getZ(), second.getZ()) >> 4;
        int maxChunkZ = Math.max(first.getZ(), second.getZ()) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                level.setChunkForced(chunkX, chunkZ, true);
            }
        }
    }

    private static void clearSingleLayer(ServerLevel level, BlockPos first, BlockPos second) {
        int minX = Math.min(first.getX(), second.getX());
        int maxX = Math.max(first.getX(), second.getX());
        int minZ = Math.min(first.getZ(), second.getZ());
        int maxZ = Math.max(first.getZ(), second.getZ());
        int y = first.getY();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static void clearBox(ServerLevel level, BlockPos first, BlockPos second) {
        int minX = Math.min(first.getX(), second.getX());
        int maxX = Math.max(first.getX(), second.getX());
        int minY = Math.min(first.getY(), second.getY());
        int maxY = Math.max(first.getY(), second.getY());
        int minZ = Math.min(first.getZ(), second.getZ());
        int maxZ = Math.max(first.getZ(), second.getZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static MachineBatteryBlockEntity requireMachineBattery(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MachineBatteryBlockEntity battery) {
            return battery;
        }
        throw new AssertionError("No machine_battery block entity at " + pos);
    }

    private static MachineBatteryBlockEntity prepareOutputBatteryAndCableLine(ServerLevel level, BlockPos batteryPos,
            BlockPos firstCablePos, BlockPos lastCablePos, long startingPower) {
        forceLoadedChunks(level, batteryPos, lastCablePos);
        level.removeBlock(batteryPos, false);
        level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        while (battery.getRedLow() != MachineBatteryBlockEntity.MODE_OUTPUT) {
            battery.cycleRedLowMode();
        }
        battery.setPower(startingPower);

        for (int x = firstCablePos.getX(); x <= lastCablePos.getX(); x++) {
            BlockPos cablePos = new BlockPos(x, firstCablePos.getY(), firstCablePos.getZ());
            level.removeBlock(cablePos, false);
            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        for (int x = firstCablePos.getX(); x <= lastCablePos.getX(); x++) {
            BlockPos cablePos = new BlockPos(x, firstCablePos.getY(), firstCablePos.getZ());
            refreshEnergyNodeAt(level, cablePos);
        }
        HbmEnergyNodespace.tick(level);
        HbmEnergyNodespace.Diagnostics joined = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(joined.uniqueNodes() >= 4, "cross-chunk red_cable line node count");
        assertTrue(joined.networks() >= 1, "cross-chunk red_cable line joined network");
        return battery;
    }

    private static void assertOutputBatteryProviderSubscribes(ServerLevel level, BlockPos batteryPos,
            BlockPos lastCablePos, MachineBatteryBlockEntity battery, String message) {
        if (HbmEnergyUtil.subscribeProviderToNeighborNetwork(level, batteryPos, Direction.EAST,
                battery.getEnergyStorage())) {
            return;
        }
        refreshCableLine(level, batteryPos.east(), lastCablePos);
        HbmEnergyNodespace.tick(level);
        assertTrue(HbmEnergyUtil.subscribeProviderToNeighborNetwork(level, batteryPos, Direction.EAST,
                battery.getEnergyStorage()), message);
    }

    private static void assertInputBatteryReceiverSubscribes(ServerLevel level, BlockPos batteryPos,
            BlockPos lastCablePos, MachineBatteryBlockEntity battery, String message) {
        if (HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, batteryPos, Direction.EAST,
                battery.getEnergyStorage())) {
            return;
        }
        refreshCableLine(level, batteryPos.east(), lastCablePos);
        HbmEnergyNodespace.tick(level);
        if (HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, batteryPos, Direction.EAST,
                battery.getEnergyStorage())) {
            return;
        }
        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, batteryPos.east());
        assertTrue(powerNet != null && powerNet.isValid(), message);
        powerNet.addReceiver(battery.getEnergyStorage());
    }

    private static void refreshCableLine(ServerLevel level, BlockPos firstCablePos, BlockPos lastCablePos) {
        for (int x = firstCablePos.getX(); x <= lastCablePos.getX(); x++) {
            refreshEnergyNodeAt(level, new BlockPos(x, firstCablePos.getY(), firstCablePos.getZ()));
        }
    }

    private static MachineBatteryBlockEntity prepareInputBatteryAndCableLine(ServerLevel level, BlockPos batteryPos,
            BlockPos firstCablePos, BlockPos lastCablePos) {
        forceLoadedChunks(level, batteryPos, lastCablePos);
        level.removeBlock(batteryPos, false);
        level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        while (battery.getRedLow() != MachineBatteryBlockEntity.MODE_INPUT) {
            battery.cycleRedLowMode();
        }
        battery.setPower(0L);

        for (int x = firstCablePos.getX(); x <= lastCablePos.getX(); x++) {
            BlockPos cablePos = new BlockPos(x, firstCablePos.getY(), firstCablePos.getZ());
            level.removeBlock(cablePos, false);
            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        for (int x = firstCablePos.getX(); x <= lastCablePos.getX(); x++) {
            BlockPos cablePos = new BlockPos(x, firstCablePos.getY(), firstCablePos.getZ());
            refreshEnergyNodeAt(level, cablePos);
        }
        HbmEnergyNodespace.tick(level);
        HbmEnergyNodespace.Diagnostics joined = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(joined.uniqueNodes() >= 4, "cross-chunk red_cable input line node count");
        assertTrue(joined.networks() >= 1, "cross-chunk red_cable input line joined network");
        return battery;
    }

    private static void assertRemoteProviderProvidesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, MachineBatteryBlockEntity battery, HbmEnergyBlockEntity provider,
            String machineName, int expectedPortCount, long startingPower) {
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, provider, provider,
                machineName, expectedPortCount, startingPower);
    }

    private static void assertRemoteProviderProvidesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, MachineBatteryBlockEntity battery, HbmEnergyAndFluidBlockEntity provider,
            String machineName, int expectedPortCount, long startingPower) {
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, provider, provider,
                machineName, expectedPortCount, startingPower);
    }

    private static void assertRemoteProviderProvidesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, MachineBatteryBlockEntity battery, Object providerOwner,
            HbmEnergyHandler provider, String machineName, int expectedPortCount, long startingPower) {
        provider.setPower(startingPower);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before " + machineName + " provider transfer");
        int providerSubscriptions = invokeTryProvideEnergyToPorts(providerOwner);
        HbmEnergyNodespace.tick(level);
        HbmEnergyUtil.PortSetSnapshot ports = inspectEnergyPorts(providerOwner);

        assertEquals(expectedPortCount, ports.totalPorts(),
                machineName + " exposes legacy remote provider energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                machineName + " remote provider port sees the cross-chunk red_cable network: " + ports);
        assertTrue(providerSubscriptions >= 1,
                machineName + " subscribed as provider through a legacy remote port");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " port cable has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();

        assertTrue(transferred > 0L,
                machineName + " power net transferred HE; before=" + beforeUpdate + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from " + machineName
                        + " across cross-chunk red_cable network");
        assertTrue(providerStoredPower(providerOwner, provider) < startingPower,
                machineName + " spent HE into the legacy remote-port network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.providerEntries() >= 1,
                machineName + " registered as provider through its legacy remote port");
        assertTrue(afterTransfer.receiverEntries() >= 1,
                "machine_battery input registered as receiver on cable network");
    }

    private static HbmEnergyUtil.PortSetSnapshot inspectEnergyPorts(Object providerOwner) {
        if (providerOwner instanceof HbmEnergyBlockEntity energyBlock) {
            return energyBlock.inspectEnergyPorts();
        }
        if (providerOwner instanceof HbmEnergyAndFluidBlockEntity energyAndFluidBlock) {
            return energyAndFluidBlock.inspectEnergyPorts();
        }
        throw new AssertionError("No inspectEnergyPorts bridge for " + providerOwner.getClass().getSimpleName());
    }

    private static long providerStoredPower(Object providerOwner, HbmEnergyHandler provider) {
        if (providerOwner instanceof HbmEnergyBlockEntity energyBlock) {
            return energyBlock.getEnergyStorage().getPower();
        }
        return provider.getPower();
    }

    private static int invokeTryProvideEnergyToPorts(Object providerOwner) {
        Class<?> declaringClass = providerOwner instanceof HbmEnergyBlockEntity
                ? HbmEnergyBlockEntity.class
                : HbmEnergyAndFluidBlockEntity.class;
        try {
            Method method = declaringClass.getDeclaredMethod("tryProvideEnergyToPorts");
            method.setAccessible(true);
            return ((Integer) method.invoke(providerOwner)).intValue();
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not invoke tryProvideEnergyToPorts on "
                    + providerOwner.getClass().getSimpleName(), exception);
        }
    }

    private static int invokeSubscribeEnergyReceiverToPorts(HbmEnergyBlockEntity receiverOwner) {
        try {
            Method method = HbmEnergyBlockEntity.class.getDeclaredMethod("subscribeEnergyReceiverToPorts");
            method.setAccessible(true);
            return ((Integer) method.invoke(receiverOwner)).intValue();
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not invoke subscribeEnergyReceiverToPorts on "
                    + receiverOwner.getClass().getSimpleName(), exception);
        }
    }

    private static void assertStandardTurretSubclassReceivesPower(GameTestHelper helper,
            RegistryObject<Block> turretBlock, String turretName, int zOffset) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + zOffset);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + zOffset);
        BlockPos turretPos = portCablePos.east(2);
        forceLoadedChunks(level, batteryPos, turretPos);
        BlockState turretState = turretBlock.get()
                .defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.removeBlock(turretPos, false);
        level.setBlock(turretPos, turretState, Block.UPDATE_ALL);
        if (!(level.getBlockEntity(turretPos) instanceof TurretBlockEntityBase turret)) {
            throw new AssertionError("No " + turretName + " block entity at " + turretPos);
        }

        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                turret, turretName, 8, 100_000L);
    }

    private static void assertEnergyAndFluidRemoteReceiverReceivesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, HbmEnergyAndFluidBlockEntity machine, String machineName, int expectedPortCount,
            long startingPower) {
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        HbmEnergyStorage receiver = energyStorage(machine);
        Iterable<HbmEnergyUtil.EnergyPort> energyPorts = energyAndFluidEnergyPorts(machine);
        HbmEnergyUtil.PortSetSnapshot ports = HbmEnergyUtil.inspectPorts(level, machine.getBlockPos(), energyPorts);
        assertEquals(expectedPortCount, ports.totalPorts(),
                machineName + " exposes legacy remote receiver energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                machineName + " remote receiver port sees the cross-chunk red_cable network: " + ports);

        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before " + machineName + " receiver transfer");
        int receiverSubscriptions = HbmEnergyUtil.subscribeReceiverToPorts(level, machine.getBlockPos(),
                energyPorts, receiver);
        assertTrue(receiverSubscriptions >= 1,
                machineName + " subscribed as receiver through a legacy remote port");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " port cable has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();

        assertTrue(transferred > 0L,
                machineName + " power net transferred HE; before=" + beforeUpdate + ", after=" + afterUpdate);
        assertTrue(receiver.getPower() > 0L,
                machineName + " remote port received HE from cross-chunk red_cable network");
        assertTrue(battery.getPower() < startingPower,
                "machine_battery output spent HE into " + machineName + " cross-chunk network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.receiverEntries() >= 1,
                machineName + " remote port registered as receiver on cable network");
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery output registered as provider on cable network");
    }

    @SuppressWarnings("unchecked")
    private static Iterable<HbmEnergyUtil.EnergyPort> energyAndFluidEnergyPorts(
            HbmEnergyAndFluidBlockEntity machine) {
        try {
            Method method = HbmEnergyAndFluidBlockEntity.class.getDeclaredMethod("getEnergyPorts");
            method.setAccessible(true);
            return (Iterable<HbmEnergyUtil.EnergyPort>) method.invoke(machine);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not inspect energy ports on "
                    + machine.getClass().getSimpleName(), exception);
        }
    }

    private static HbmEnergyStorage energyStorage(HbmEnergyAndFluidBlockEntity machine) {
        try {
            Field field = HbmEnergyAndFluidBlockEntity.class.getDeclaredField("energy");
            field.setAccessible(true);
            return (HbmEnergyStorage) field.get(machine);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not inspect energy storage on "
                    + machine.getClass().getSimpleName(), exception);
        }
    }

    private static void assertEnergyBlockRemoteReceiverReceivesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, HbmEnergyBlockEntity machine, String machineName, int expectedPortCount,
            long startingPower) {
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        HbmEnergyUtil.PortSetSnapshot ports = machine.inspectEnergyPorts();
        assertEquals(expectedPortCount, ports.totalPorts(),
                machineName + " exposes legacy remote receiver energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                machineName + " remote receiver port sees the cross-chunk red_cable network: " + ports);

        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before " + machineName + " receiver transfer");
        int receiverSubscriptions = invokeSubscribeEnergyReceiverToPorts(machine);
        assertTrue(receiverSubscriptions >= 1,
                machineName + " subscribed as receiver through a legacy remote port");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " port cable has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();

        assertTrue(transferred > 0L,
                machineName + " power net transferred HE; before=" + beforeUpdate + ", after=" + afterUpdate);
        assertTrue(machine.getPower() > 0L,
                machineName + " remote port received HE from cross-chunk red_cable network");
        assertTrue(battery.getPower() < startingPower,
                "machine_battery output spent HE into " + machineName + " cross-chunk network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.receiverEntries() >= 1,
                machineName + " remote port registered as receiver on cable network");
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery output registered as provider on cable network");
    }

    private static void assertCommandResult(ServerLevel level, BlockPos sourcePos, String command, int expected) {
        CommandSourceStack source = level.getServer()
                .createCommandSourceStack()
                .withLevel(level)
                .withPosition(Vec3.atCenterOf(sourcePos))
                .withPermission(4)
                .withSuppressedOutput();
        int result = level.getServer().getCommands().performPrefixedCommand(source, command);
        assertEquals(expected, result, "/" + command + " result");
    }

    private static void assertCommandVisibleMessage(ServerLevel level, BlockPos sourcePos, String command, int expected,
            String... snippets) {
        CapturingCommandSource capture = new CapturingCommandSource();
        CommandSourceStack source = level.getServer()
                .createCommandSourceStack()
                .withSource(capture)
                .withLevel(level)
                .withPosition(Vec3.atCenterOf(sourcePos))
                .withPermission(4);
        int result = level.getServer().getCommands().performPrefixedCommand(source, command);
        assertEquals(expected, result, "/" + command + " visible result");
        assertTrue(!capture.messages().isEmpty(), "/" + command + " should send a visible command message");
        String joined = String.join("\n", capture.messages());
        for (String snippet : snippets) {
            assertTrue(joined.contains(snippet),
                    "/" + command + " visible message should contain '" + snippet + "' but was: " + joined);
        }
    }

    private static String commandPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static void assertRemoteReceiverReceivesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, BlockPos machinePos, Iterable<HbmEnergyUtil.EnergyPort> energyPorts,
            HbmEnergyReceiver receiver, String machineName, int expectedPortCount, long startingPower) {
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        HbmEnergyUtil.PortSetSnapshot ports = HbmEnergyUtil.inspectPorts(level, machinePos, energyPorts);
        assertEquals(expectedPortCount, ports.totalPorts(),
                machineName + " exposes legacy remote receiver energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                machineName + " remote receiver port sees the cross-chunk red_cable network: " + ports);

        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before " + machineName + " receiver transfer");
        int receiverSubscriptions = HbmEnergyUtil.subscribeReceiverToPorts(level, machinePos, energyPorts, receiver);
        assertTrue(receiverSubscriptions >= 1,
                machineName + " subscribed as receiver through a legacy remote port");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " port cable has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();

        assertTrue(transferred > 0L,
                machineName + " power net transferred HE; before=" + beforeUpdate + ", after=" + afterUpdate);
        assertTrue(receiver.getPower() > 0L,
                machineName + " remote port received HE from cross-chunk red_cable network");
        assertTrue(battery.getPower() < startingPower,
                "machine_battery output spent HE into " + machineName + " cross-chunk network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.receiverEntries() >= 1,
                machineName + " remote port registered as receiver on cable network");
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery output registered as provider on cable network");
    }

    private static void assertFixedRemoteReceiverReceivesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, HbmEnergyAndFluidBlockEntity machine, String machineName, long startingPower) {
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        HbmEnergyUtil.PortSetSnapshot ports = machine.inspectEnergyPorts();
        assertEquals(6, ports.totalPorts(), machineName + " exposes six legacy remote energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                machineName + " remote port sees the cross-chunk red_cable network");

        HbmEnergyNodespace.Diagnostics subscribed = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(subscribed.receiverEntries() >= 1,
                machineName + " subscribed as receiver through its remote port");
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes to adjacent cable network");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " port cable has a valid power net");
        powerNet.update();

        assertTrue(machine.getPower() > 0L,
                machineName + " remote port received HE from cross-chunk red_cable network");
        assertTrue(battery.getPower() < startingPower,
                "machine_battery output spent HE into " + machineName + " cross-chunk network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery output registered as provider on cable network");
    }

    private static void assertXrFloorRingRemoteReceiverReceivesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, BlockPos machinePos, HbmEnergyReceiver receiver, String machineName,
            long startingPower) {
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        HbmEnergyUtil.PortSetSnapshot ports = HbmEnergyUtil.inspectPorts(level, machinePos,
                LegacyMultiblockPorts.xrFloorRingEnergyPorts(2));
        assertEquals(12, ports.totalPorts(), machineName + " exposes twelve legacy XR floor-ring energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                machineName + " remote floor-ring port sees the cross-chunk red_cable network");

        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes to adjacent cable network");
        assertTrue(HbmEnergyUtil.subscribeReceiverToNetwork(level, portCablePos, Direction.WEST, receiver),
                machineName + " receiver subscribes through its east floor-ring remote port");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " port cable has a valid power net");
        powerNet.update();

        assertTrue(receiver.getPower() > 0L,
                machineName + " remote floor-ring port received HE from cross-chunk red_cable network");
        assertTrue(battery.getPower() < startingPower,
                "machine_battery output spent HE into " + machineName + " cross-chunk network");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.receiverEntries() >= 1,
                machineName + " remote floor-ring port registered as receiver on cable network");
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery output registered as provider on cable network");
    }

    private static void tickMachineBattery(ServerLevel level, BlockPos pos, MachineBatteryBlockEntity battery) {
        MachineBatteryBlockEntity.serverTick(level, pos, level.getBlockState(pos), battery);
    }

    private static IItemHandler itemHandler(ServerLevel level, BlockPos pos, Direction side, String label) {
        return level.getBlockEntity(pos)
                .getCapability(ForgeCapabilities.ITEM_HANDLER, side)
                .orElseThrow(() -> new AssertionError(label + " missing item handler"));
    }

    private static HopperBlockEntity placeHopper(ServerLevel level, BlockPos pos, Direction facing) {
        level.setBlock(pos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, facing), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof HopperBlockEntity hopper) {
            return hopper;
        }
        throw new AssertionError("No hopper block entity at " + pos);
    }

    private static void tickHopper(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof HopperBlockEntity hopper) {
            HopperBlockEntity.pushItemsTick(level, pos, level.getBlockState(pos), hopper);
            return;
        }
        throw new AssertionError("No hopper block entity at " + pos);
    }

    private static void clearBatteryAndHopper(ServerLevel level, MachineBatteryBlockEntity battery, BlockPos hopperPos) {
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, ItemStack.EMPTY);
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE, ItemStack.EMPTY);
        level.removeBlock(hopperPos, false);
    }

    private static void clearSocketAndHopper(ServerLevel level, MachineBatterySocketBlockEntity socket,
            BlockPos hopperPos) {
        clearSocketBattery(socket);
        level.removeBlock(hopperPos, false);
    }

    private static PneumaticTubeBlockEntity placePneumaticTube(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, ModBlocks.PNEUMATIC_TUBE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof PneumaticTubeBlockEntity tube) {
            tube.refreshPneumaticNode();
            return tube;
        }
        throw new AssertionError("No pneumatic tube block entity at " + pos);
    }

    private static PneumaticTubeBlockEntity requirePneumaticTube(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PneumaticTubeBlockEntity tube) {
            return tube;
        }
        throw new AssertionError("No pneumatic tube block entity at " + pos);
    }

    private static ChestBlockEntity placeChest(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            return chest;
        }
        throw new AssertionError("No chest block entity at " + pos);
    }

    private static void setTubeInsertion(PneumaticTubeBlockEntity tube, Direction direction, String label) {
        for (int i = 0; i < Direction.values().length + 1; i++) {
            if (tube.getInsertionDirection() == direction) {
                return;
            }
            tube.cycleInsertionDirection();
        }
        throw new AssertionError(label + ": expected " + direction + " but got " + tube.getInsertionDirection());
    }

    private static void setTubeEjection(PneumaticTubeBlockEntity tube, Direction direction, String label) {
        for (int i = 0; i < Direction.values().length + 1; i++) {
            if (tube.getEjectionDirection() == direction) {
                return;
            }
            tube.cycleEjectionDirection();
        }
        throw new AssertionError(label + ": expected " + direction + " but got " + tube.getEjectionDirection());
    }

    private static boolean sendPneumaticOnce(ServerLevel level, PneumaticTubeBlockEntity tube) {
        tube.refreshPneumaticNode();
        PneumaticNetwork network = tube.getPneumaticNet();
        if (network == null) {
            throw new AssertionError("Pneumatic tube has no network at " + tube.getBlockPos());
        }
        PneumaticReceiver receiver = PneumaticUtil.receiver(level, tube.getBlockPos(), tube.getEjectionDirection(), tube)
                .orElseThrow(() -> new AssertionError("Pneumatic tube has no receiver at " + tube.getBlockPos()));
        PneumaticItemAccess source = PneumaticUtil.sourceAccess(level, tube.getBlockPos(), tube.getInsertionDirection())
                .orElseThrow(() -> new AssertionError("Pneumatic tube has no source at " + tube.getBlockPos()));
        network.addReceiver(receiver);
        return network.send(source, tube, tube.getSendOrder(), tube.getReceiveOrder(),
                PneumaticUtil.rangeForPressure(1), 0);
    }

    private static void clearBatteryAndPneumatic(ServerLevel level, MachineBatteryBlockEntity battery,
            BlockPos tubePos, BlockPos chestPos) {
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_DISCHARGE, ItemStack.EMPTY);
        battery.getItems().setStackInSlot(MachineBatteryBlockEntity.SLOT_CHARGE, ItemStack.EMPTY);
        level.removeBlock(tubePos, false);
        level.removeBlock(chestPos, false);
    }

    private static void clearSocketAndPneumatic(ServerLevel level, MachineBatterySocketBlockEntity socket,
            BlockPos tubePos, BlockPos chestPos) {
        clearSocketBattery(socket);
        level.removeBlock(tubePos, false);
        level.removeBlock(chestPos, false);
    }

    private static ItemStack chargedBatteryStack(long charge) {
        ItemStack stack = new ItemStack(ModItems.BATTERY_REDSTONE.get());
        requireChargeable(stack, "battery_redstone").setCharge(stack, charge);
        return stack;
    }

    private static void assertBatteryCharge(ItemStack stack, long expected, String label) {
        assertSame(ModItems.BATTERY_REDSTONE.get(), stack.getItem(), label + " item");
        assertEquals(expected, requireChargeable(stack, "battery_redstone").getCharge(stack), label + " charge");
    }

    private static CompoundTag legacyMachineBatteryPersistentTag(long power, long previousPowerState,
            int redLow, int redHigh, int priority) {
        CompoundTag persistent = new CompoundTag();
        persistent.putLong("power", power);
        persistent.putLong("prevPowerState", previousPowerState);
        persistent.putShort("redLow", (short) redLow);
        persistent.putShort("redHigh", (short) redHigh);
        persistent.putInt("priority", priority);
        return persistent;
    }

    private static void cycleRedHighToBuffer(MachineBatteryBlockEntity battery) {
        while (battery.getRedHigh() != MachineBatteryBlockEntity.MODE_BUFFER) {
            battery.cycleRedHighMode();
        }
    }

    private static void cycleSocketRedLowToMode(MachineBatterySocketBlockEntity socket, int mode) {
        while (socket.getRedLow() != mode) {
            socket.cycleRedLowMode();
        }
    }

    private static void clearSocketBattery(MachineBatterySocketBlockEntity socket) {
        socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, ItemStack.EMPTY);
    }

    private static void assertSocketExtraction(MachineBatterySocketBlockEntity socket, IItemHandler handler,
            int mode, ItemStack battery, boolean shouldExtract, String label) {
        cycleSocketRedLowToMode(socket, mode);
        socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, battery.copy());
        ItemStack extracted = handler.extractItem(0, 1, true);
        if (shouldExtract) {
            assertSame(battery.getItem(), extracted.getItem(), label);
        } else {
            assertTrue(extracted.isEmpty(), label);
        }
    }

    private static MachineBatterySocketBlockEntity placeBatterySocket(ServerLevel level, BlockPos corePos,
            Direction facing) {
        BlockState state = ModBlocks.MACHINE_BATTERY_SOCKET.get()
                .defaultBlockState()
                .setValue(MachineBatterySocketBlock.FACING, facing);
        level.setBlock(corePos, state, Block.UPDATE_ALL);
        state.getBlock().setPlacedBy(level, corePos, state, null, ItemStack.EMPTY);
        return requireMachineBatterySocket(level, corePos);
    }

    private static List<BlockPos> socketFootprintPositions(BlockPos corePos, Direction facing) {
        return MachineBatterySocketBlock.socketOffsets(facing).stream()
                .map(corePos::offset)
                .toList();
    }

    private static void assertSocketFootprintProxiesResolve(ServerLevel level, MachineBatterySocketBlockEntity socket,
            List<BlockPos> footprint) {
        for (int i = 0; i < footprint.size(); i++) {
            BlockPos pos = footprint.get(i);
            if (i == 0) {
                assertSame(socket, level.getBlockEntity(pos), "battery_socket footprint core is real block entity");
            } else {
                assertTrue(level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity,
                        "battery_socket footprint proxy " + i + " exists");
                assertSame(socket, MultiblockHelper.resolveCoreBlockEntity(level, pos),
                        "battery_socket footprint proxy " + i + " resolves to core");
            }
        }
    }

    private static Direction openHorizontalDirection(BlockPos pos, Set<BlockPos> occupied) {
        for (Direction direction : List.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH)) {
            if (!occupied.contains(pos.relative(direction))) {
                return direction;
            }
        }
        throw new AssertionError("No open horizontal side around " + pos);
    }

    private static MachineBatterySocketBlockEntity requireMachineBatterySocket(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MachineBatterySocketBlockEntity socket) {
            return socket;
        }
        throw new AssertionError("No machine_battery_socket block entity at " + pos);
    }

    private static LaunchPadBlockEntity findLaunchPadAround(ServerLevel level, BlockPos center, int radius) {
        LaunchPadBlockEntity launchPad = findLaunchPadAroundOrNull(level, center, radius);
        if (launchPad != null) {
            return launchPad;
        }
        throw new AssertionError("No player-placed launch_pad block entity around " + center);
    }

    private static LaunchPadBlockEntity findLaunchPadAroundOrNull(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                if (level.getBlockEntity(pos) instanceof LaunchPadBlockEntity launchPad) {
                    return launchPad;
                }
            }
        }
        return null;
    }

    private static void assertLaunchPadHasResolvingProxy(ServerLevel level, LaunchPadBlockEntity launchPad) {
        BlockPos corePos = launchPad.getBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = corePos.offset(dx, 0, dz);
                if (pos.equals(corePos)) {
                    continue;
                }
                if (level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity
                        && MultiblockHelper.resolveCoreBlockEntity(level, pos) == launchPad) {
                    return;
                }
            }
        }
        throw new AssertionError("No player-placed launch_pad proxy resolves to the core at " + corePos);
    }

    private static LargeLaunchPadBlockEntity findLargeLaunchPadAroundOrNull(ServerLevel level, BlockPos center,
            int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                if (level.getBlockEntity(pos) instanceof LargeLaunchPadBlockEntity launchPad) {
                    return launchPad;
                }
            }
        }
        return null;
    }

    private static void assertLargeLaunchPadHasResolvingPortProxies(ServerLevel level,
            LargeLaunchPadBlockEntity launchPad) {
        BlockPos corePos = launchPad.getBlockPos();
        for (BlockPos offset : List.of(new BlockPos(4, 0, 2), new BlockPos(4, 0, -2),
                new BlockPos(-4, 0, 2), new BlockPos(-4, 0, -2),
                new BlockPos(2, 0, 4), new BlockPos(-2, 0, 4),
                new BlockPos(2, 0, -4), new BlockPos(-2, 0, -4))) {
            BlockPos pos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity,
                    "player-placed launch_pad_large port proxy exists at " + pos);
            assertSame(launchPad, MultiblockHelper.resolveCoreBlockEntity(level, pos),
                    "player-placed launch_pad_large port proxy resolves to core at " + pos);
        }
    }

    private static CompactLauncherBlockEntity findCompactLauncherAroundOrNull(ServerLevel level, BlockPos center,
            int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                if (level.getBlockEntity(pos) instanceof CompactLauncherBlockEntity launcher) {
                    return launcher;
                }
            }
        }
        return null;
    }

    private static void assertCompactLauncherHasResolvingPortProxy(ServerLevel level,
            CompactLauncherBlockEntity launcher) {
        BlockPos corePos = launcher.getBlockPos();
        for (BlockPos offset : List.of(new BlockPos(1, 0, 1), new BlockPos(1, 0, -1),
                new BlockPos(-1, 0, 1), new BlockPos(-1, 0, -1))) {
            BlockPos pos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity,
                    "player-placed compact_launcher port proxy exists at " + pos);
            assertSame(launcher, MultiblockHelper.resolveCoreBlockEntity(level, pos),
                    "player-placed compact_launcher port proxy resolves to core at " + pos);
        }
    }

    private static SoyuzLauncherBlockEntity findSoyuzLauncherAroundOrNull(ServerLevel level, BlockPos center,
            int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockEntity(pos) instanceof SoyuzLauncherBlockEntity launcher) {
                        return launcher;
                    }
                }
            }
        }
        return null;
    }

    private static void assertSoyuzLauncherHasResolvingPortProxies(ServerLevel level,
            SoyuzLauncherBlockEntity launcher) {
        BlockPos corePos = launcher.getBlockPos();
        for (BlockPos offset : List.of(new BlockPos(6, 0, 0), new BlockPos(-6, 0, 0),
                new BlockPos(0, 0, 6), new BlockPos(0, 0, -6),
                new BlockPos(6, 1, 0), new BlockPos(-6, 1, 0),
                new BlockPos(0, 1, 6), new BlockPos(0, 1, -6))) {
            BlockPos pos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity,
                    "player-placed soyuz_launcher port proxy exists at " + pos);
            assertSame(launcher, MultiblockHelper.resolveCoreBlockEntity(level, pos),
                    "player-placed soyuz_launcher port proxy resolves to core at " + pos);
        }
    }

    private static FriendlyByteBuf menuPosBuffer(BlockPos pos) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(pos);
        return buffer;
    }

    private static BlockHitResult blockHit(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
    }

    private static Set<BlockPos> expectedSocketPositions(BlockPos pos, Direction facing) {
        Direction rot = facing.getClockWise();
        Direction behind = facing.getOpposite();
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(pos.immutable());
        positions.add(pos.relative(behind));
        positions.add(pos.relative(rot));
        positions.add(pos.relative(behind).relative(rot));
        return positions;
    }

    private static Set<String> expectedSocketConnectionKeys(BlockPos pos, Direction facing) {
        Direction rot = facing.getClockWise();
        Direction behind = facing.getOpposite();
        Direction rotOpposite = rot.getOpposite();
        Set<String> keys = new LinkedHashSet<>();
        addConnectionKey(keys, pos.relative(facing), facing);
        addConnectionKey(keys, pos.relative(facing).relative(rot), facing);
        addConnectionKey(keys, pos.relative(behind, 2), behind);
        addConnectionKey(keys, pos.relative(behind, 2).relative(rot), behind);
        addConnectionKey(keys, pos.relative(rot, 2), rot);
        addConnectionKey(keys, pos.relative(rot, 2).relative(behind), rot);
        addConnectionKey(keys, pos.relative(rotOpposite), rotOpposite);
        addConnectionKey(keys, pos.relative(rotOpposite).relative(behind), rotOpposite);
        return keys;
    }

    private static Set<String> connectionKeys(HbmEnergyNode node) {
        Set<String> keys = new LinkedHashSet<>();
        for (var connection : node.getConnectionPoints()) {
            addConnectionKey(keys, connection.pos(), connection.direction());
        }
        return keys;
    }

    private static void addConnectionKey(Set<String> keys, BlockPos pos, Direction direction) {
        keys.add(connectionKey(pos, direction));
    }

    private static String connectionKey(BlockPos pos, Direction direction) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ() + "|" + direction;
    }

    private static void assertBlockPosSetEquals(Set<BlockPos> expected, Set<BlockPos> actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertStringSetEquals(Set<String> expected, Set<String> actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private static int inventoryItemCount(Player player, Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int inventoryFluidContainerCount(Player player, FluidType type) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (isFluidContainer(stack, type)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int droppedItemCount(ServerLevel level, Player player, Item item) {
        int count = 0;
        for (ItemEntity entity : droppedItemsAround(level, player)) {
            ItemStack stack = entity.getItem();
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int droppedFluidContainerCount(ServerLevel level, Player player, FluidType type) {
        int count = 0;
        for (ItemEntity entity : droppedItemsAround(level, player)) {
            ItemStack stack = entity.getItem();
            if (isFluidContainer(stack, type)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean isFluidContainer(ItemStack stack, FluidType type) {
        return stack.getItem() == ModItems.CANISTER_FULL.get()
                && HbmFluidContainerRegistry.getFluidType(stack) == type
                && HbmFluidContainerRegistry.getFluidContent(stack, type) == 1_000;
    }

    private static void clearDroppedItemsAround(ServerLevel level, Player player) {
        for (ItemEntity entity : droppedItemsAround(level, player)) {
            entity.discard();
        }
    }

    private static List<ItemEntity> droppedItemsAround(ServerLevel level, Player player) {
        return level.getEntitiesOfClass(ItemEntity.class, AABB.ofSize(player.position(), 12.0D, 12.0D, 12.0D));
    }

    @SafeVarargs
    private static void assertRegistryListEquals(List<RegistryObject<Item>> actual, String label,
            RegistryObject<Item>... expected) {
        assertEquals(expected.length, actual.size(), label + " size");
        for (int i = 0; i < expected.length; i++) {
            assertSame(expected[i], actual.get(i), label + " meta " + i);
        }
    }

    private static void assertSame(Object expected, Object actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected same instance " + expected + " but got " + actual);
        }
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

    private static HbmChargeableItem requireChargeable(ItemStack stack, String name) {
        if (stack.getItem() instanceof HbmChargeableItem battery) {
            return battery;
        }
        throw new AssertionError(name + " is not an HBM chargeable item");
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

    private static void assertEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static final class TestFluidProvider implements HbmStandardFluidSender {
        private final HbmFluidTank tank;

        private TestFluidProvider(FluidType type, int fill) {
            this.tank = new HbmFluidTank(type, Math.max(fill, 1));
            this.tank.setFill(fill);
        }

        private int getStoredFluid() {
            return tank.getFill();
        }

        @Override
        public List<HbmFluidTank> getSendingTanks() {
            return List.of(tank);
        }

        @Override
        public List<HbmFluidTank> getAllTanks() {
            return List.of(tank);
        }
    }

    private static final class CapturingCommandSource implements CommandSource {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void sendSystemMessage(Component message) {
            messages.add(message.getString());
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        private List<String> messages() {
            return messages;
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
