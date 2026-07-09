package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.block.CableDiodeBlock;
import com.hbm.ntm.block.CapacitorBlock;
import com.hbm.ntm.block.CapacitorBusBlock;
import com.hbm.ntm.block.DfcMachineBlock;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.ChargerBlock;
import com.hbm.ntm.block.LegacyDirectionalShapeBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.block.PowerDetectorBlock;
import com.hbm.ntm.block.RedCableBoxBlock;
import com.hbm.ntm.blockentity.ArcFurnaceBlockEntity;
import com.hbm.ntm.blockentity.ArcWelderBlockEntity;
import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.blockentity.AutocrafterBlockEntity;
import com.hbm.ntm.blockentity.BatteryReddBlockEntity;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.blockentity.CableDiodeBlockEntity;
import com.hbm.ntm.blockentity.CapacitorBlockEntity;
import com.hbm.ntm.blockentity.CatalyticReformerBlockEntity;
import com.hbm.ntm.blockentity.ChargerBlockEntity;
import com.hbm.ntm.blockentity.ChemicalFactoryBlockEntity;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.blockentity.FensuBlockEntity;
import com.hbm.ntm.blockentity.ChungusBlockEntity;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.blockentity.CompressorBlockEntity;
import com.hbm.ntm.blockentity.ConveyorPressBlockEntity;
import com.hbm.ntm.blockentity.CyclotronBlockEntity;
import com.hbm.ntm.blockentity.DeuteriumExtractorBlockEntity;
import com.hbm.ntm.blockentity.DeuteriumTowerBlockEntity;
import com.hbm.ntm.blockentity.DieselGeneratorBlockEntity;
import com.hbm.ntm.blockentity.DfcEmitterBlockEntity;
import com.hbm.ntm.blockentity.DfcReceiverBlockEntity;
import com.hbm.ntm.blockentity.DfcStabilizerBlockEntity;
import com.hbm.ntm.blockentity.ElectrolyserBlockEntity;
import com.hbm.ntm.blockentity.ElectricFurnaceBlockEntity;
import com.hbm.ntm.blockentity.ElectricHeaterBlockEntity;
import com.hbm.ntm.blockentity.ElectricPressBlockEntity;
import com.hbm.ntm.blockentity.ExposureChamberBlockEntity;
import com.hbm.ntm.blockentity.FelBlockEntity;
import com.hbm.ntm.blockentity.FusionKlystronBlockEntity;
import com.hbm.ntm.blockentity.FusionMHDTBlockEntity;
import com.hbm.ntm.blockentity.FusionPlasmaForgeBlockEntity;
import com.hbm.ntm.blockentity.FusionTorusBlockEntity;
import com.hbm.ntm.blockentity.GasCentBlockEntity;
import com.hbm.ntm.blockentity.GasFlareBlockEntity;
import com.hbm.ntm.blockentity.HbmEnergyAndFluidBlockEntity;
import com.hbm.ntm.blockentity.HbmEnergyBlockEntity;
import com.hbm.ntm.blockentity.HydrotreaterBlockEntity;
import com.hbm.ntm.blockentity.ICFControllerBlockEntity;
import com.hbm.ntm.blockentity.IndustrialSteamTurbineBlockEntity;
import com.hbm.ntm.blockentity.ExcavatorBlockEntity;
import com.hbm.ntm.blockentity.IntakeBlockEntity;
import com.hbm.ntm.blockentity.LargeLaunchPadBlockEntity;
import com.hbm.ntm.blockentity.LaunchPadBlockEntity;
import com.hbm.ntm.blockentity.LaunchTableBlockEntity;
import com.hbm.ntm.blockentity.LegacyGenericSelectorMachineBlockEntity;
import com.hbm.ntm.blockentity.LegacyLargeTurbineBlockEntity;
import com.hbm.ntm.blockentity.LegacyLightBlockEntity;
import com.hbm.ntm.blockentity.LiquefactorBlockEntity;
import com.hbm.ntm.blockentity.MachineBatteryBlockEntity;
import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.blockentity.MicrowaveBlockEntity;
import com.hbm.ntm.blockentity.MiniRtgBlockEntity;
import com.hbm.ntm.blockentity.MiningLaserBlockEntity;
import com.hbm.ntm.blockentity.MixerBlockEntity;
import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.blockentity.OilDrillBlockEntity;
import com.hbm.ntm.blockentity.OreSlopperBlockEntity;
import com.hbm.ntm.blockentity.PADetectorBlockEntity;
import com.hbm.ntm.blockentity.PADipoleBlockEntity;
import com.hbm.ntm.blockentity.PAQuadrupoleBlockEntity;
import com.hbm.ntm.blockentity.PARfcBlockEntity;
import com.hbm.ntm.blockentity.PASourceBlockEntity;
import com.hbm.ntm.blockentity.PneumaticTubeBlockEntity;
import com.hbm.ntm.blockentity.PowerDetectorBlockEntity;
import com.hbm.ntm.blockentity.PoweredCondenserBlockEntity;
import com.hbm.ntm.blockentity.ProcessingMachineBlockEntity;
import com.hbm.ntm.blockentity.PyroOvenBlockEntity;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.blockentity.RadarLargeBlockEntity;
import com.hbm.ntm.blockentity.RadGenBlockEntity;
import com.hbm.ntm.blockentity.RadioboxBlockEntity;
import com.hbm.ntm.blockentity.RadiolysisBlockEntity;
import com.hbm.ntm.blockentity.RefineryBlockEntity;
import com.hbm.ntm.blockentity.RtgBlockEntity;
import com.hbm.ntm.blockentity.ShredderBlockEntity;
import com.hbm.ntm.blockentity.SolidifierBlockEntity;
import com.hbm.ntm.blockentity.SoyuzLauncherBlockEntity;
import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.blockentity.SteamTurbineBlockEntity;
import com.hbm.ntm.blockentity.StirlingBlockEntity;
import com.hbm.ntm.blockentity.SolderingStationBlockEntity;
import com.hbm.ntm.blockentity.TeleporterBlockEntity;
import com.hbm.ntm.blockentity.TeslaBlockEntity;
import com.hbm.ntm.blockentity.TurbineGasBlockEntity;
import com.hbm.ntm.blockentity.TurbofanBlockEntity;
import com.hbm.ntm.blockentity.VacuumDistillBlockEntity;
import com.hbm.ntm.blockentity.WaterPumpBlockEntity;
import com.hbm.ntm.blockentity.WoodBurnerBlockEntity;
import com.hbm.ntm.blockentity.CompactLauncherBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.blockentity.ForceFieldBlockEntity;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmChargeableItem;
import com.hbm.ntm.energy.HbmEnergyDebug;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmEnergyNodeHost;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmLegacyBatteryMaps;
import com.hbm.ntm.energy.HbmLegacyWireNode;
import com.hbm.ntm.energy.HbmLoadedEnergy;
import com.hbm.ntm.energy.HbmPowerNet;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.event.CommonForgeEvents;
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
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.multiblock.LegacyMultiblockPorts;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.recipe.AnvilConstructionRecipe;
import com.hbm.ntm.recipe.AnvilConstructionRecipeRuntime;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.turret.TurretArtyBlockEntity;
import com.hbm.ntm.turret.TurretBlockEntityBase;
import com.hbm.ntm.turret.TurretFritzBlockEntity;
import com.hbm.ntm.turret.TurretHimarsBlockEntity;
import com.hbm.ntm.uninos.HbmUninosNodespaces;
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
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2Capacitor")
    public static void legacyBlockCapacitorsKeepLegacyCapacitiesAndClickedFacing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            var player = FakePlayerFactory.getMinecraft(level);
            player.getInventory().clearContent();
            int index = 0;
            for (LegacyCapacitorCase capacitor : legacyCapacitorCases()) {
                BlockPos supportPos = helper.absolutePos(new BlockPos(2 + index * 2, 1, 2));
                BlockPos placedPos = supportPos.above();
                level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
                ItemStack stack = new ItemStack(capacitor.block().get().asItem());
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);

                var placeResult = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                        blockHit(supportPos, Direction.UP)));
                assertTrue(placeResult.consumesAction(),
                        capacitor.name() + " player useOn placement consumes action");
                BlockState placed = level.getBlockState(placedPos);
                assertTrue(placed.is(capacitor.block().get()), capacitor.name() + " places legacy block ID");
                assertSame(Direction.UP, placed.getValue(CapacitorBlock.FACING),
                        capacitor.name() + " stores clicked face as FACING");
                if (!(level.getBlockEntity(placedPos) instanceof CapacitorBlockEntity blockEntity)) {
                    throw new AssertionError("No capacitor block entity for " + capacitor.name());
                }
                assertEquals(capacitor.maxPower(), blockEntity.getMaxPower(),
                        capacitor.name() + " maxPower matches 1.7.10 HE capacity");
                index++;
            }
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2Capacitor")
    public static void legacyBlockCapacitorReceivesFromFacingInputSide(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos capacitorPos = helper.absolutePos(new BlockPos(4, 2, 3));
            BlockPos cablePos = capacitorPos.west();
            BlockPos batteryPos = cablePos.west();
            forceLoadedChunks(level, batteryPos, capacitorPos);

            level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
            MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
            while (battery.getRedLow() != MachineBatteryBlockEntity.MODE_OUTPUT) {
                battery.cycleRedLowMode();
            }
            battery.setPower(100_000L);

            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, cablePos);
            BlockState capacitorState = ModBlocks.CAPACITOR_COPPER.get()
                    .defaultBlockState()
                    .setValue(CapacitorBlock.FACING, Direction.WEST);
            level.setBlock(capacitorPos, capacitorState, Block.UPDATE_ALL);
            CapacitorBlockEntity capacitor = requireCapacitor(level, capacitorPos);

            assertTrue(HbmEnergyUtil.subscribeProviderToNeighborNetwork(level, batteryPos, Direction.EAST,
                    battery.getEnergyStorage()), "machine_battery output subscribes to capacitor input cable");
            CapacitorBlockEntity.serverTick(level, capacitorPos, capacitorState, capacitor);
            HbmEnergyNodespace.tick(level);
            HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, cablePos);
            assertTrue(powerNet != null && powerNet.isValid(), "capacitor input cable has valid power net");
            powerNet.update();

            assertTrue(capacitor.getPower() > 0L, "legacy capacitor receives HE from its FACING side");
            assertTrue(battery.getPower() < 100_000L, "machine_battery spent HE into legacy capacitor");
            assertEquals(0L, capacitor.getPowerSent(), "capacitor does not output without capacitor_bus chain");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2Capacitor")
    public static void legacyBlockCapacitorOutputsThroughStraightBusChain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos capacitorPos = helper.absolutePos(new BlockPos(3, 2, 3));
            BlockPos busA = capacitorPos.east();
            BlockPos busB = busA.east();
            BlockPos cablePos = busB.east();
            BlockPos batteryPos = cablePos.east();
            forceLoadedChunks(level, capacitorPos, batteryPos);

            BlockState capacitorState = ModBlocks.CAPACITOR_COPPER.get()
                    .defaultBlockState()
                    .setValue(CapacitorBlock.FACING, Direction.WEST);
            level.setBlock(capacitorPos, capacitorState, Block.UPDATE_ALL);
            CapacitorBlockEntity capacitor = requireCapacitor(level, capacitorPos);
            capacitor.setPower(100_000L);
            BlockState busState = ModBlocks.CAPACITOR_BUS.get()
                    .defaultBlockState()
                    .setValue(CapacitorBusBlock.FACING, Direction.EAST);
            level.setBlock(busA, busState, Block.UPDATE_ALL);
            level.setBlock(busB, busState, Block.UPDATE_ALL);
            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, cablePos);

            level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
            MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
            while (battery.getRedLow() != MachineBatteryBlockEntity.MODE_INPUT) {
                battery.cycleRedLowMode();
            }
            battery.setPower(0L);
            assertTrue(HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, batteryPos, Direction.WEST,
                    battery.getEnergyStorage()), "machine_battery input subscribes to capacitor bus output cable");

            CapacitorBlockEntity.serverTick(level, capacitorPos, capacitorState, capacitor);
            HbmEnergyNodespace.tick(level);
            HbmEnergyUtil.PortSetSnapshot ports = capacitor.inspectEnergyPorts();
            assertEquals(1, ports.totalPorts(), "legacy capacitor exposes one bus-resolved output port");
            assertTrue(ports.networkedPorts() >= 1,
                    "legacy capacitor bus output port sees the red_cable network: " + ports);
            HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, cablePos);
            assertTrue(powerNet != null && powerNet.isValid(), "capacitor bus output cable has valid power net");
            long transferred = powerNet.update();

            assertTrue(transferred > 0L, "capacitor bus output transfers HE through Energy Mk2 network");
            assertTrue(battery.getPower() > 0L, "machine_battery input receives HE from capacitor bus chain");
            assertTrue(capacitor.getPower() < 100_000L, "legacy capacitor spends HE through bus output");
            assertTrue(capacitor.getPowerSent() > 0L, "legacy capacitor records sent HE/t overlay counter");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2Capacitor")
    public static void legacyBlockCapacitorPersistentDropKeepsPowerAndMaxPower(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos capacitorPos = helper.absolutePos(new BlockPos(2, 2, 2));
            BlockState capacitorState = ModBlocks.CAPACITOR_SCHRABIDATE.get()
                    .defaultBlockState()
                    .setValue(CapacitorBlock.FACING, Direction.DOWN);
            level.setBlock(capacitorPos, capacitorState, Block.UPDATE_ALL);
            CapacitorBlockEntity capacitor = requireCapacitor(level, capacitorPos);
            capacitor.setPower(12_345_678L);

            List<ItemStack> drops = Block.getDrops(capacitorState, level, capacitorPos, capacitor);
            assertEquals(1, drops.size(), "legacy capacitor persistent drop has one stack");
            ItemStack drop = drops.get(0);
            assertSame(ModBlocks.CAPACITOR_SCHRABIDATE.get().asItem(), drop.getItem(),
                    "legacy capacitor drops bridge BlockItem");
            CompoundTag persistent = drop.getOrCreateTag().getCompound(HbmPersistentBlockState.TAG_PERSISTENT);
            assertEquals(12_345_678L, persistent.getLong("power"),
                    "legacy capacitor drop keeps stored HE under persistent.power");
            assertEquals(50_000_000_000L, persistent.getLong("maxPower"),
                    "legacy capacitor drop keeps capacity under persistent.maxPower");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
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

        assertLegacySingleBatteryStringStack("cube_power", ModItems.CUBE_POWER, "cube_power");
        assertLegacySingleBatteryStringStack("hbm:battery_potato", ModItems.BATTERY_POTATO, "battery_potato");
        assertLegacySingleBatteryStringStack("hbm:item.battery_potatos", ModItems.BATTERY_POTATOS,
                "battery_potatos");
        assertLegacySingleBatteryStringStack("item.hev_battery", ModItems.HEV_BATTERY, "hev_battery");
        assertLegacySingleBatteryStringStack("hbm:fusion_core", ModItems.FUSION_CORE, "fusion_core");
        assertLegacySingleBatteryStringStack("hbm:item.energy_core", ModItems.ENERGY_CORE, "energy_core");
        assertLegacySingleBatteryStringStack("hbm:item.battery_creative", ModItems.BATTERY_CREATIVE,
                "battery_creative");

        CompoundTag unknownMeta = legacyStackTag("hbm:battery_pack", 99, 1);
        assertTrue(!LegacyItemStackMigration.migrateItemStackTag(unknownMeta), "unknown battery_pack meta no-op");
        assertEquals("hbm:battery_pack", unknownMeta.getString("id"), "unknown meta keeps legacy id");
        assertEquals(99, unknownMeta.getShort("Damage"), "unknown meta keeps Damage");
        CompoundTag unknownMetaDiagnostic = legacyStackTag("hbm:battery_pack", 99, 1);
        LegacyItemStackMigration.Result unknownMetaResult =
                LegacyItemStackMigration.migrateAll(unknownMetaDiagnostic, LegacyWorldItemIdMap.empty());
        assertEquals(0, unknownMetaResult.migrated(), "unknown battery_pack meta still does not migrate");
        assertEquals(1, unknownMetaResult.unknownLegacyBatteryMetas(),
                "unknown battery_pack meta counted separately");
        assertEquals(0, unknownMetaResult.unknownNumericItemStacks(),
                "unknown battery_pack meta is not an unknown numeric id");
        assertEquals("hbm:battery_pack", unknownMetaDiagnostic.getString("id"),
                "unknown meta diagnostic keeps legacy id");
        assertEquals(99, unknownMetaDiagnostic.getShort("Damage"),
                "unknown meta diagnostic keeps Damage");

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

    private static void assertLegacySingleBatteryStringStack(String legacyId, RegistryObject<Item> expected,
            String label) {
        CompoundTag stack = legacyStackTag(legacyId, 0, 1);
        CompoundTag data = new CompoundTag();
        data.putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, 4321L);
        stack.put("tag", data);
        assertTrue(LegacyItemStackMigration.migrateItemStackTag(stack),
                "legacy " + label + " single-id stack migrated");
        assertEquals(expected.getId().toString(), stack.getString("id"),
                "legacy " + label + " single-id stack id");
        assertTrue(!stack.contains("Damage"), "legacy " + label + " single-id Damage removed");
        assertEquals(4321L, stack.getCompound("tag").getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG),
                "legacy " + label + " charge NBT preserved");
        assertEquals(1, stack.getByte("Count"), "legacy " + label + " Count preserved");
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void legacyBatteryNumericItemStackNbtMigrationUsesWorldItemData(GameTestHelper helper) {
        legacyBatteryNumericItemStackNbtMigrationUsesWorldItemData();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void legacyBatteryRealLocalWorldSamplesUseActualLevelDatItemData(GameTestHelper helper) {
        legacyBatteryRealLocalWorldSamplesUseActualLevelDatItemData();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2LegacyImport")
    public static void legacyBatteryRealLocalWorldSamplesLoadThroughServerMigrationCache(GameTestHelper helper) {
        legacyBatteryRealLocalWorldSamplesLoadThroughServerMigrationCache();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2LegacyImport")
    public static void legacyBatteryRealLocalWorldSamplesMigrateActualSavedStacks(GameTestHelper helper) {
        legacyBatteryRealLocalWorldSamplesMigrateActualSavedStacks();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2LegacyImport")
    public static void legacyBatteryDroppedItemEntityNbtMigrationPreservesCharge(GameTestHelper helper) {
        legacyBatteryDroppedItemEntityNbtMigrationPreservesCharge();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2LegacyImport")
    public static void legacyBatteryChunkDataEventsUseLoadedWorldItemMap(GameTestHelper helper) {
        LegacyWorldItemIdMap itemIds = LegacyWorldItemIdMap.fromLevelDatRoot(legacyModItemDataLevelDat());
        int previousBuild = setBlockMigrationCachedBuildNumberForTesting(2);
        BlockMigrationHelper.resetDiagnostics();
        BlockMigrationHelper.setLegacyItemIdsForTesting(itemIds);
        try {
            CompoundTag chunkData = legacyNumericRoot(6725, 5);
            chunkData.putInt(BlockMigrationHelper.NBT_KEY_BUILD_NUMBER, 1);
            CommonForgeEvents.onChunkDataLoad(new ChunkDataEvent.Load(
                    helper.getLevel().getChunk(helper.absolutePos(new BlockPos(1, 2, 1))),
                    chunkData,
                    ChunkStatus.ChunkType.LEVELCHUNK));

            ListTag migratedItems = chunkData.getList("Items", Tag.TAG_COMPOUND);
            assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(), migratedItems.getCompound(0).getString("id"),
                    "chunk data load event migrates numeric old battery_pack meta through world item map");
            BlockMigrationHelper.MigrationDiagnostics afterLoad = BlockMigrationHelper.diagnostics();
            assertEquals(1L, afterLoad.migratedChunks(), "chunk data load event counted migrated chunk");
            assertEquals(1L, afterLoad.migratedItemStacks(), "chunk data load event counted migrated Energy stack");
            assertEquals(0L, afterLoad.numericItemStacksWithoutMap(),
                    "chunk data load event used the loaded item id map");
            assertTrue(afterLoad.lastLoadResult().migrated(),
                    "chunk data load event records migrated load result");

            CompoundTag saveData = new CompoundTag();
            CommonForgeEvents.onChunkDataSave(new ChunkDataEvent.Save(
                    helper.getLevel().getChunk(helper.absolutePos(new BlockPos(1, 2, 1))),
                    helper.getLevel(),
                    saveData));
            assertEquals(2, saveData.getInt(BlockMigrationHelper.NBT_KEY_BUILD_NUMBER),
                    "chunk data save event writes current migration build marker");
            assertEquals(1L, BlockMigrationHelper.diagnostics().savedChunks(),
                    "chunk data save event counted saved chunk");
        } finally {
            BlockMigrationHelper.setLegacyItemIdsForTesting(LegacyWorldItemIdMap.empty());
            BlockMigrationHelper.resetDiagnostics();
            setBlockMigrationCachedBuildNumberForTesting(previousBuild);
        }
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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2NodespaceLifecycleTick")
    public static void energyNodespaceServerEndTickRebuildsChangedRealCableNetwork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos left = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos right = left.east();
            level.setBlock(left, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(right, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, left);
            refreshEnergyNodeAt(level, right);

            HbmEnergyNodespace.tick(level);
            HbmEnergyNodespace.Diagnostics joined = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, joined.uniqueNodes(), "server tick real red_cable joined nodes");
            assertEquals(1, joined.networks(), "server tick real red_cable joined network");
            assertEquals(2, joined.linkRefs(), "server tick real red_cable joined links");
            assertEquals(0, joined.dirtyNodes(), "server tick setup has no pending changed nodes");

            HbmEnergyNode rightNode = requireEnergyNodeAt(level, right);
            rightNode.markRecentlyChanged();
            HbmEnergyNodespace.Diagnostics marked = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(1, marked.dirtyNodes(), "server tick test marks one real red_cable node changed");

            CommonForgeEvents.onServerTick(new TickEvent.ServerTickEvent(
                    TickEvent.Phase.START, () -> true, level.getServer()));
            HbmEnergyNodespace.Diagnostics afterStart = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(1, afterStart.dirtyNodes(), "server START tick leaves Energy nodespace pending");
            assertEquals(1, afterStart.networks(), "server START tick leaves existing real red_cable network intact");

            CommonForgeEvents.onServerTick(new TickEvent.ServerTickEvent(
                    TickEvent.Phase.END, () -> true, level.getServer()));
            HbmEnergyNodespace.Diagnostics afterEnd = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(2, afterEnd.uniqueNodes(), "server END tick keeps real red_cable nodes");
            assertEquals(1, afterEnd.networks(), "server END tick keeps real red_cable network");
            assertEquals(2, afterEnd.linkRefs(), "server END tick keeps real red_cable links");
            assertEquals(0, afterEnd.dirtyNodes(), "server END tick clears pending Energy nodespace changes");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
            HbmFluidNodespace.unloadLevel(level);
            HbmUninosNodespaces.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2NodespaceLifecyclePrune")
    public static void energyNodespaceTickPrunesStaleUnloadedChunkNode(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos stalePos = null;
            for (int distance = 1024; distance <= 1_048_576; distance *= 4) {
                BlockPos candidate = new BlockPos(origin.getX() + distance, origin.getY(), origin.getZ() + distance);
                if (!level.hasChunk(candidate.getX() >> 4, candidate.getZ() >> 4)) {
                    stalePos = candidate;
                    break;
                }
            }
            assertTrue(stalePos != null, "server tick stale-node test found an unloaded chunk");

            HbmEnergyNode staleNode = new HbmEnergyNode(stalePos, Set.of(Direction.EAST));
            HbmEnergyNodespace.createNode(level, staleNode);
            HbmEnergyNodespace.Diagnostics stale = HbmEnergyNodespace.getDiagnostics(level);
            assertEquals(1, stale.uniqueNodes(), "stale unloaded-chunk node is present before server tick");
            assertEquals(1, stale.networks(), "stale unloaded-chunk node has a temporary network before server tick");
            assertEquals(1, stale.linkRefs(), "stale unloaded-chunk node has one temporary link before server tick");

            HbmEnergyNodespace.tick(level);
            HbmEnergyNodespace.Diagnostics afterTick = HbmEnergyNodespace.getDiagnostics(level);
            assertTrue(staleNode.isExpired(), "Energy nodespace tick expires stale unloaded-chunk node");
            assertEquals(0, afterTick.uniqueNodes(), "Energy nodespace tick prunes stale unloaded-chunk node");
            assertEquals(0, afterTick.networks(), "Energy nodespace tick removes stale unloaded-chunk network");
            assertEquals(0, afterTick.linkRefs(), "Energy nodespace tick removes stale unloaded-chunk links");
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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2CableRender")
    public static void redCableBoxLegacyVariantAnvilMatrixMatches1710(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        if (!(ModBlocks.RED_CABLE_BOX.get().asItem() instanceof RedCableBoxBlockItem boxItem)) {
            throw new AssertionError("red_cable_box must use RedCableBoxBlockItem for legacy size variants");
        }
        Item redCopper = ModItems.legacyItem("ingot_red_copper").get();
        Item polymerPlate = ModItems.legacyItem("plate_polymer").get();

        for (int variant = 0; variant < 5; variant++) {
            int recipeVariant = variant;
            AnvilConstructionRecipe construction = AnvilConstructionRecipeRuntime.recipeById(level,
                    new ResourceLocation(HbmNtm.MOD_ID, "anvil_construction/energy/red_cable_box_" + recipeVariant))
                    .orElseThrow(() -> new AssertionError(
                            "missing red_cable_box construction recipe " + recipeVariant));
            assertEquals(2, construction.tierLower(),
                    "red_cable_box construction variant " + variant + " tier lower");
            assertEquals(-1, construction.tierUpper(),
                    "red_cable_box construction variant " + variant + " has no tier upper cap");
            assertSame(AnvilConstructionRecipe.OverlayType.CONSTRUCTION, construction.overlay(),
                    "red_cable_box construction variant " + variant + " overlay");
            assertEquals(2, construction.inputs().size(),
                    "red_cable_box construction variant " + variant + " input count");
            assertEquals(1, construction.outputs().size(),
                    "red_cable_box construction variant " + variant + " output count");
            assertTrue(construction.matches(new SimpleContainer(new ItemStack(redCopper),
                    new ItemStack(polymerPlate)), level),
                    "red_cable_box construction variant " + variant + " accepts Mingrade ingot plus polymer plate");

            ItemStack constructionOutput = construction.outputs().get(0).representativeStack();
            assertSame(boxItem, constructionOutput.getItem(),
                    "red_cable_box construction variant " + variant + " output item");
            assertEquals(16, constructionOutput.getCount(),
                    "red_cable_box construction variant " + variant + " output count");
            assertEquals(variant, constructionOutput.getOrCreateTag().getInt(LegacyStateBlockItem.TAG_VARIANT),
                    "red_cable_box construction variant " + variant + " output legacy size");

            AnvilConstructionRecipe recycling = AnvilConstructionRecipeRuntime.recipeById(level,
                    new ResourceLocation(HbmNtm.MOD_ID, "anvil_construction/energy/red_cable_box_" + recipeVariant
                            + "_recycling"))
                    .orElseThrow(() -> new AssertionError(
                            "missing red_cable_box recycling recipe " + recipeVariant));
            assertEquals(2, recycling.tierLower(),
                    "red_cable_box recycling variant " + variant + " tier lower");
            assertEquals(-1, recycling.tierUpper(),
                    "red_cable_box recycling variant " + variant + " has no tier upper cap");
            assertSame(AnvilConstructionRecipe.OverlayType.RECYCLING, recycling.overlay(),
                    "red_cable_box recycling variant " + variant + " overlay");
            assertEquals(1, recycling.inputs().size(),
                    "red_cable_box recycling variant " + variant + " input count");
            assertEquals(2, recycling.outputs().size(),
                    "red_cable_box recycling variant " + variant + " output count");

            ItemStack recyclingInput = RedCableBoxBlockItem.createStack(boxItem, variant);
            recyclingInput.setCount(16);
            assertTrue(recycling.matches(new SimpleContainer(recyclingInput), level),
                    "red_cable_box recycling variant " + variant + " accepts matching size NBT");
            ItemStack wrongVariantInput = RedCableBoxBlockItem.createStack(boxItem, (variant + 1) % 5);
            wrongVariantInput.setCount(16);
            assertFalse(recycling.matches(new SimpleContainer(wrongVariantInput), level),
                    "red_cable_box recycling variant " + variant + " rejects a different size NBT");

            ItemStack redCopperOutput = recycling.outputs().get(0).representativeStack();
            assertSame(redCopper, redCopperOutput.getItem(),
                    "red_cable_box recycling variant " + variant + " returns red copper ingot");
            assertEquals(1, redCopperOutput.getCount(),
                    "red_cable_box recycling variant " + variant + " red copper count");
            ItemStack polymerOutput = recycling.outputs().get(1).representativeStack();
            assertSame(polymerPlate, polymerOutput.getItem(),
                    "red_cable_box recycling variant " + variant + " returns polymer plate");
            assertEquals(1, polymerOutput.getCount(),
                    "red_cable_box recycling variant " + variant + " polymer count");
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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2BatterySocketPlayerPlacement")
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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesArcFurnaceRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 22, anchor.getY() - 1, chunkStartZ + 260);
        forceLoadedChunks(level, supportPos.offset(-10, 0, -10), supportPos.offset(10, 6, 10));
        clearBox(level, supportPos.above().offset(-10, 0, -10), supportPos.above(6).offset(10, 0, 10));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack furnaceStack = new ItemStack(ModBlocks.MACHINE_ARC_FURNACE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, furnaceStack);

        var placeResult = furnaceStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ArcFurnaceBlockEntity furnace = findArcFurnaceAroundOrNull(level, supportPos.above(), 8);
        if (furnace == null) {
            throw new AssertionError("player useOn machine_arc_furnace placement returned " + placeResult
                    + " without placing a machine_arc_furnace core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_arc_furnace block item action after placing the core");
        BlockPos furnacePos = furnace.getBlockPos();
        assertTrue(level.getBlockState(furnacePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_arc_furnace faces south for the selected legacy west/south port");
        assertArcFurnaceHasResolvingPowerProxies(level, furnace);

        BlockPos portCablePos = furnacePos.west(3).south();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, furnacePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(furnace, level.getBlockEntity(furnacePos),
                "player-placed machine_arc_furnace core survives external west/south remote-port cable placement");
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                furnace, "player-placed machine_arc_furnace", 6, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_arc_furnace diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, furnacePos,
                "hbm energy port " + commandPos(furnacePos) + " -3 0 1 west", expectedLinks,
                "Energy port from " + furnacePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, furnacePos, "hbm energy ports " + commandPos(furnacePos), 1,
                "Energy ports at " + furnacePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesSolidifierRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 272);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack solidifierStack = new ItemStack(ModBlocks.MACHINE_SOLIDIFIER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, solidifierStack);

        var placeResult = solidifierStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        SolidifierBlockEntity solidifier = findSolidifierAroundOrNull(level, supportPos.above(), 5);
        if (solidifier == null) {
            throw new AssertionError("player useOn machine_solidifier placement returned " + placeResult
                    + " without placing a machine_solidifier core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_solidifier block item action after placing the core");
        BlockPos machinePos = solidifier.getBlockPos();
        assertFixedSixPortMachineHasResolvingProxies(level, machinePos, solidifier,
                "player-placed machine_solidifier");

        BlockPos portCablePos = machinePos.west(2).above();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(solidifier, level.getBlockEntity(machinePos),
                "player-placed machine_solidifier core survives external west remote-port cable placement");
        SolidifierBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), solidifier);
        assertFixedRemoteReceiverReceivesPower(level, batteryPos, portCablePos, solidifier,
                "player-placed machine_solidifier", 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_solidifier diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 1 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesLiquefactorRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 280);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack liquefactorStack = new ItemStack(ModBlocks.MACHINE_LIQUEFACTOR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, liquefactorStack);

        var placeResult = liquefactorStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        LiquefactorBlockEntity liquefactor = findLiquefactorAroundOrNull(level, supportPos.above(), 5);
        if (liquefactor == null) {
            throw new AssertionError("player useOn machine_liquefactor placement returned " + placeResult
                    + " without placing a machine_liquefactor core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_liquefactor block item action after placing the core");
        BlockPos machinePos = liquefactor.getBlockPos();
        assertFixedSixPortMachineHasResolvingProxies(level, machinePos, liquefactor,
                "player-placed machine_liquefactor");

        BlockPos portCablePos = machinePos.west(2).above();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(liquefactor, level.getBlockEntity(machinePos),
                "player-placed machine_liquefactor core survives external west remote-port cable placement");
        LiquefactorBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), liquefactor);
        assertFixedRemoteReceiverReceivesPower(level, batteryPos, portCablePos, liquefactor,
                "player-placed machine_liquefactor", 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_liquefactor diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 1 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesMixerAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 288);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack mixerStack = new ItemStack(ModBlocks.MACHINE_MIXER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, mixerStack);

        var placeResult = mixerStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        MixerBlockEntity mixer = findBlockEntityAroundOrNull(level, supportPos.above(), 5,
                MixerBlockEntity.class, ModBlocks.MACHINE_MIXER.get());
        if (mixer == null) {
            throw new AssertionError("player useOn machine_mixer placement returned " + placeResult
                    + " without placing a machine_mixer core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_mixer block item action after placing the core");
        BlockPos machinePos = mixer.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_mixer faces south while exposing adjacent legacy receiver ports");
        for (BlockPos offset : List.of(new BlockPos(0, 1, 0), new BlockPos(0, 2, 0))) {
            BlockPos proxyPos = machinePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_mixer vertical proxy exists at " + proxyPos);
            assertSame(mixer, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_mixer vertical proxy resolves to core at " + proxyPos);
        }

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(mixer, level.getBlockEntity(machinePos),
                "player-placed machine_mixer core survives adjacent west receiver-port cable placement");
        MixerBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), mixer);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                mixer, "player-placed machine_mixer", 5, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_mixer diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=5", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesIntakeRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 320);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack intakeStack = new ItemStack(ModBlocks.MACHINE_INTAKE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, intakeStack);

        var placeResult = intakeStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        IntakeBlockEntity intake = findBlockEntityAroundOrNull(level, supportPos.above(), 5,
                IntakeBlockEntity.class, ModBlocks.MACHINE_INTAKE.get());
        if (intake == null) {
            throw new AssertionError("player useOn machine_intake placement returned " + placeResult
                    + " without placing a machine_intake core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_intake block item action after placing the core");
        BlockPos machinePos = intake.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_intake faces south while exposing legacy remote receiver ports");

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(intake, level.getBlockEntity(machinePos),
                "player-placed machine_intake core survives external west receiver-port cable placement");
        IntakeBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), intake);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                intake, "player-placed machine_intake", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_intake diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesRadarAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 704);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack radarStack = new ItemStack(ModBlocks.MACHINE_RADAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, radarStack);

        var placeResult = radarStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        RadarBlockEntity radar = findBlockEntityAroundOrNull(level, supportPos.above(), 3,
                RadarBlockEntity.class, ModBlocks.MACHINE_RADAR.get());
        if (radar == null) {
            throw new AssertionError("player useOn machine_radar placement returned " + placeResult
                    + " without placing a machine_radar core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_radar block item action after placing the core");
        BlockPos machinePos = radar.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_radar faces south while exposing adjacent legacy receiver sides");

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(radar, level.getBlockEntity(machinePos),
                "player-placed machine_radar core survives adjacent west receiver-side cable placement");
        RadarBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), radar);

        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before machine_radar receiver transfer");
        radar.refreshEnergyConnections();

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_radar adjacent side cable has a valid power net");
        long transferred = powerNet.update();
        assertTrue(transferred > 0L,
                "player-placed machine_radar adjacent receiver side transferred HE through real red_cable");
        assertTrue(radar.getPower() > 0L,
                "player-placed machine_radar adjacent receiver side received HE");
        assertTrue(battery.getPower() < 100_000L,
                "machine_battery output spent HE into player-placed machine_radar");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesTeslaAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 736);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack teslaStack = new ItemStack(ModBlocks.TESLA.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, teslaStack);

        var placeResult = teslaStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        TeslaBlockEntity tesla = findBlockEntityAroundOrNull(level, supportPos.above(), 2,
                TeslaBlockEntity.class, ModBlocks.TESLA.get());
        if (tesla == null) {
            throw new AssertionError("player useOn tesla placement returned " + placeResult
                    + " without placing a tesla core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the tesla block item action after placing the core");
        BlockPos machinePos = tesla.getBlockPos();

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(tesla, level.getBlockEntity(machinePos),
                "player-placed tesla core survives adjacent west receiver-side cable placement");
        TeslaBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), tesla);
        assertAdjacentEnergyBlockReceiverReceivesPower(level, batteryPos, portCablePos, tesla,
                "player-placed tesla", 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed tesla adjacent side cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesTeleporterAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 752);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack teleporterStack = new ItemStack(ModBlocks.MACHINE_TELEPORTER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, teleporterStack);

        var placeResult = teleporterStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        TeleporterBlockEntity teleporter = findBlockEntityAroundOrNull(level, supportPos.above(), 2,
                TeleporterBlockEntity.class, ModBlocks.MACHINE_TELEPORTER.get());
        if (teleporter == null) {
            throw new AssertionError("player useOn machine_teleporter placement returned " + placeResult
                    + " without placing a machine_teleporter core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_teleporter block item action after placing the core");
        BlockPos machinePos = teleporter.getBlockPos();

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(teleporter, level.getBlockEntity(machinePos),
                "player-placed machine_teleporter core survives adjacent west receiver-side cable placement");
        TeleporterBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), teleporter);
        assertAdjacentEnergyBlockReceiverReceivesPower(level, batteryPos, portCablePos, teleporter,
                "player-placed machine_teleporter", 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_teleporter adjacent side cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesFloodlightBackReceiverPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos portCablePos = new BlockPos(chunkStartX + 20, anchor.getY(), chunkStartZ + 760);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos floodlightPos = portCablePos.east();
        forceLoadedChunks(level, batteryPos, floodlightPos);
        clearBox(level, batteryPos.offset(-1, -1, -2), floodlightPos.offset(1, 3, 2));
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(portCablePos.getX() + 0.5D, portCablePos.getY() + 1.0D, portCablePos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        Block floodlightBlock = ModBlocks.legacyBlock("floodlight").get();
        ItemStack floodlightStack = new ItemStack(floodlightBlock);
        player.setItemInHand(InteractionHand.MAIN_HAND, floodlightStack);

        var placeResult = floodlightStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(portCablePos, Direction.EAST)));

        if (!(level.getBlockEntity(floodlightPos) instanceof LegacyLightBlockEntity floodlight)) {
            throw new AssertionError("player useOn floodlight placement returned " + placeResult
                    + " without placing a floodlight block entity at " + floodlightPos
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the floodlight block item action after placing the block");
        assertSame(floodlightBlock, level.getBlockState(floodlightPos).getBlock(),
                "player-placed floodlight keeps the legacy floodlight block");
        assertSame(Direction.EAST, level.getBlockState(floodlightPos).getValue(LegacyDirectionalShapeBlock.FACE),
                "player-placed floodlight records clicked east face so its old back input is west");

        LegacyLightBlockEntity.tick(level, floodlightPos, level.getBlockState(floodlightPos), floodlight);
        assertAdjacentReceiverReceivesPower(level, batteryPos, portCablePos, floodlightPos, floodlight,
                "player-placed floodlight", 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed floodlight back-side cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, floodlightPos, "hbm energy ports " + commandPos(floodlightPos), 0,
                "No HBM energy port machine at " + floodlightPos.toShortString());

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void rbmkReasimControlRodBottomReceiverPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos controlPos = new BlockPos(chunkStartX + 20, anchor.getY(), chunkStartZ + 816);
        BlockPos portCablePos = controlPos.below();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, controlPos.above(4));
        clearBox(level, batteryPos.offset(-1, -1, -2), controlPos.above(4).offset(1, 1, 2));

        level.setBlock(controlPos, ModBlocks.RBMK_CONTROL_REASIM.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(controlPos) instanceof RBMKColumnBlockEntity control)) {
            throw new AssertionError("No rbmk_control_reasim block entity at " + controlPos);
        }
        assertTrue(control.hasOperationalLayout(), "rbmk_control_reasim direct placement fills the legacy column");
        assertTrue(control.isPoweredControlRod(), "rbmk_control_reasim is the powered control rod variant");
        assertEquals(50_000L, control.getMaxPower(),
                "rbmk_control_reasim preserves the 1.7.10 50,000 HE buffer");
        assertEquals(50_000L, control.getReceiverSpeed(),
                "rbmk_control_reasim accepts up to its full legacy buffer per Energy Mk2 update");
        assertSame(HbmEnergyReceiver.ConnectionPriority.LOW, control.getPriority(),
                "rbmk_control_reasim keeps the legacy LOW receiver priority");
        assertTrue(control.getCapability(ForgeCapabilities.ENERGY, Direction.DOWN).isPresent(),
                "rbmk_control_reasim exposes its 1 HE = 1 FE bridge only on the legacy bottom side");
        assertTrue(!control.getCapability(ForgeCapabilities.ENERGY, Direction.UP).isPresent(),
                "rbmk_control_reasim does not expose a top-side FE bridge");

        MachineBatteryBlockEntity battery =
                prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(control, level.getBlockEntity(controlPos),
                "rbmk_control_reasim core survives bottom red_cable placement");
        RBMKColumnBlockEntity.serverTick(level, controlPos, level.getBlockState(controlPos), control);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output subscribes before feeding rbmk_control_reasim");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "rbmk_control_reasim bottom red_cable has a valid power net");
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterTransfer = powerNet.createDebugSnapshot();
        assertTrue(transferred > 0L,
                "rbmk_control_reasim bottom receiver transferred HE; after=" + afterTransfer);
        assertTrue(control.getPower() > 0L,
                "rbmk_control_reasim received HE from its legacy bottom Energy Mk2 port");
        assertTrue(battery.getPower() < 100_000L,
                "machine_battery output spent HE into rbmk_control_reasim");
        for (int tick = 0; tick < 10 && control.getPower() < 5_000L; tick++) {
            RBMKColumnBlockEntity.serverTick(level, controlPos, level.getBlockState(controlPos), control);
            powerNet.update();
        }
        assertTrue(control.getPower() >= 5_000L,
                "rbmk_control_reasim can buffer enough bottom-side HE for legacy powered movement");

        long chargedPower = control.getPower();
        control.setControlTarget(1.0D);
        RBMKColumnBlockEntity.serverTick(level, controlPos, level.getBlockState(controlPos), control);
        assertTrue(control.controlHasPower(),
                "rbmk_control_reasim reports powered movement while it has at least 5,000 HE");
        assertTrue(control.controlLevel() > 0.0D,
                "rbmk_control_reasim moves after receiving bottom-side HE");
        assertTrue(control.getPower() < chargedPower,
                "rbmk_control_reasim spends legacy 5,000 HE when its rod moves");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, controlPos, "hbm energy ports " + commandPos(controlPos), 0,
                "No HBM energy port machine at " + controlPos.toShortString());

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void rbmkPlainControlRodDoesNotExposeBottomReceiver(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos controlPos = new BlockPos(chunkStartX + 20, anchor.getY(), chunkStartZ + 832);
        BlockPos portCablePos = controlPos.below();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, controlPos.above(4));
        clearBox(level, batteryPos.offset(-1, -1, -2), controlPos.above(4).offset(1, 1, 2));

        level.setBlock(controlPos, ModBlocks.RBMK_CONTROL.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(controlPos) instanceof RBMKColumnBlockEntity control)) {
            throw new AssertionError("No rbmk_control block entity at " + controlPos);
        }
        assertTrue(!control.isPoweredControlRod(), "plain rbmk_control is not a powered ReaSim control rod");
        assertEquals(0L, control.getMaxPower(), "plain rbmk_control keeps the legacy non-powered HE capacity");
        assertEquals(0L, control.getReceiverSpeed(), "plain rbmk_control keeps the legacy non-powered receiver speed");
        assertTrue(!control.getCapability(ForgeCapabilities.ENERGY, Direction.DOWN).isPresent(),
                "plain rbmk_control does not expose a bottom FE bridge");

        MachineBatteryBlockEntity battery =
                prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        RBMKColumnBlockEntity.serverTick(level, controlPos, level.getBlockState(controlPos), control);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output subscribes on plain rbmk_control negative case");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "plain rbmk_control bottom red_cable has a valid power net");
        assertEquals(0L, powerNet.update(),
                "plain rbmk_control bottom red_cable does not transfer HE without a receiver");
        assertEquals(0, powerNet.createDebugSnapshot().receivers(),
                "plain rbmk_control does not subscribe as an Energy Mk2 receiver");
        assertEquals(0L, control.getPower(), "plain rbmk_control remains unpowered");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesElectricFurnaceAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesAdjacentReceiverWithDiagnostics(helper, ModBlocks.MACHINE_ELECTRIC_FURNACE_OFF,
                ElectricFurnaceBlockEntity.class, "machine_electric_furnace_off", 768,
                ElectricFurnaceBlockEntity::serverTick);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesElectricPressAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesAdjacentReceiverWithDiagnostics(helper, ModBlocks.MACHINE_EPRESS,
                ElectricPressBlockEntity.class, "machine_epress", 784, ElectricPressBlockEntity::serverTick);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesConveyorPressHorizontalReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 800);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 6, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(6).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack pressStack = new ItemStack(ModBlocks.MACHINE_CONVEYOR_PRESS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, pressStack);

        var placeResult = pressStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        ConveyorPressBlockEntity press = findBlockEntityAroundOrNull(level, supportPos.above(), 5,
                ConveyorPressBlockEntity.class, ModBlocks.MACHINE_CONVEYOR_PRESS.get());
        if (press == null) {
            throw new AssertionError("player useOn machine_conveyor_press placement returned " + placeResult
                    + " without placing a machine_conveyor_press core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_conveyor_press block item action after placing the core");
        assertEquals(50_000L, press.getMaxPower(), "machine_conveyor_press preserves the 1.7.10 50,000 HE buffer");
        BlockPos machinePos = press.getBlockPos();

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(press, level.getBlockEntity(machinePos),
                "player-placed machine_conveyor_press core survives west horizontal receiver-port cable placement");
        ConveyorPressBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), press);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos, press,
                "player-placed machine_conveyor_press", 4, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_conveyor_press west receiver port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2ConveyorPress")
    public static void conveyorPressProcessesMovingItemWithLegacyPlateStamp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pressPos = helper.absolutePos(new BlockPos(3, 2, 3));
        forceLoadedChunks(level, pressPos.offset(-2, 0, -2), pressPos.offset(2, 3, 2));
        clearBox(level, pressPos.offset(-2, -1, -2), pressPos.offset(2, 3, 2));
        level.setBlock(pressPos, ModBlocks.MACHINE_CONVEYOR_PRESS.get().defaultBlockState(), Block.UPDATE_ALL);

        if (!(level.getBlockEntity(pressPos) instanceof ConveyorPressBlockEntity press)) {
            throw new AssertionError("machine_conveyor_press block entity missing at " + pressPos);
        }
        ItemStack stamp = new ItemStack(ModItems.IRON_PLATE_STAMP.get());
        assertTrue(press.installStamp(stamp, false), "machine_conveyor_press accepts legacy plate stamp");
        assertEquals(0, stamp.getCount(), "installing a stamp consumes one held stamp");
        press.setPower(2_000L);

        MovingItemEntity input = new MovingItemEntity(level, new ItemStack(Items.IRON_INGOT));
        input.moveTo(pressPos.getX() + 0.5D, pressPos.getY() + 1.25D, pressPos.getZ() + 0.5D, 0.0F, 0.0F);
        level.addFreshEntity(input);

        long initialPower = press.getPower();
        for (int tick = 0; tick < 8; tick++) {
            ConveyorPressBlockEntity.serverTick(level, pressPos, level.getBlockState(pressPos), press);
        }

        assertTrue(input.isRemoved(), "legacy conveyor press consumes the original moving item at full press");
        List<MovingItemEntity> outputs = level.getEntitiesOfClass(MovingItemEntity.class,
                new AABB(pressPos.getX(), pressPos.getY() + 1.0D, pressPos.getZ(),
                        pressPos.getX() + 1.0D, pressPos.getY() + 1.5D, pressPos.getZ() + 1.0D),
                entity -> !entity.isRemoved());
        assertEquals(1, outputs.size(), "legacy conveyor press spawns exactly one pressed moving item");
        ItemStack output = outputs.get(0).getItemStack();
        assertSame(ModItems.IRON_PLATE.get(), output.getItem(),
                "legacy plate stamp turns one moving iron ingot into one moving iron plate");
        assertEquals(1, output.getCount(), "legacy conveyor press keeps the moving output stack size at one");
        assertTrue(press.getPower() <= initialPower - ConveyorPressBlockEntity.USAGE * 8,
                "legacy conveyor press spends 100 HE for each extension tick");
        assertEquals(1, press.getStamp().getDamageValue(), "legacy conveyor press damages the stamp once per press");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesMicrowaveAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesAdjacentReceiverWithDiagnostics(helper, ModBlocks.MACHINE_MICROWAVE,
                MicrowaveBlockEntity.class, "machine_microwave", 816, MicrowaveBlockEntity::serverTick);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesRadioboxAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesAdjacentReceiverWithDiagnostics(helper, ModBlocks.RADIOBOX,
                RadioboxBlockEntity.class, "radiobox", 832, RadioboxBlockEntity::serverTick);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesPowerDetectorAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 840);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack detectorStack = new ItemStack(ModBlocks.MACHINE_DETECTOR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, detectorStack);

        var placeResult = detectorStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        PowerDetectorBlockEntity detector = findBlockEntityAroundOrNull(level, supportPos.above(), 5,
                PowerDetectorBlockEntity.class, ModBlocks.MACHINE_DETECTOR.get());
        if (detector == null) {
            throw new AssertionError("player useOn machine_detector placement returned " + placeResult
                    + " without placing a machine_detector around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_detector block item action after placing the block");
        assertEquals(5L, detector.getMaxPower(), "machine_detector preserves the 1.7.10 5 HE buffer");
        assertSame(HbmEnergyReceiver.ConnectionPriority.HIGH, detector.getPriority(),
                "machine_detector preserves the 1.7.10 HIGH receiver priority");

        BlockPos detectorPos = detector.getBlockPos();
        BlockPos portCablePos = detectorPos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, detectorPos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(detector, level.getBlockEntity(detectorPos),
                "player-placed machine_detector survives adjacent west receiver-side cable placement");
        PowerDetectorBlockEntity.serverTick(level, detectorPos, level.getBlockState(detectorPos), detector);
        assertAdjacentReceiverReceivesPower(level, batteryPos, portCablePos, detectorPos, detector,
                "player-placed machine_detector", 100_000L);
        PowerDetectorBlockEntity.serverTick(level, detectorPos, level.getBlockState(detectorPos), detector);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_detector adjacent cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, detectorPos, "hbm energy ports " + commandPos(detectorPos), 0,
                "No HBM energy port machine at " + detectorPos.toShortString());
        assertTrue(level.getBlockState(detectorPos).getValue(PowerDetectorBlock.ACTIVE),
                "machine_detector turns active for a tick after receiving HE");
        assertEquals(15, level.getSignal(detectorPos, Direction.WEST),
                "machine_detector emits redstone while active");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesShredderAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesAdjacentReceiverWithDiagnostics(helper, ModBlocks.MACHINE_SHREDDER,
                ShredderBlockEntity.class, "machine_shredder", 848, ShredderBlockEntity::serverTick);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesAutocrafterAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesAdjacentReceiverWithDiagnostics(helper, ModBlocks.MACHINE_AUTOCRAFTER,
                AutocrafterBlockEntity.class, "machine_autocrafter", 864, AutocrafterBlockEntity::serverTick);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesForceFieldAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ForceFieldBlockEntity forceField = assertPlayerUseOnPlacesAdjacentReceiverWithDiagnostics(helper,
                ModBlocks.MACHINE_FORCEFIELD,
                ForceFieldBlockEntity.class, "machine_forcefield", 880, ForceFieldBlockEntity::serverTick);
        assertTrue(!forceField.canConnectEnergy(Direction.UP), "machine_forcefield keeps the legacy no-top energy side");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesChargerBackEnergySideAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesChargerBackEnergySideWithDiagnostics(helper, 896);
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesDfcEmitterAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesAdjacentEnergyAndFluidReceiverWithDiagnostics(helper, ModBlocks.DFC_EMITTER,
                DfcEmitterBlockEntity.class, "dfc_emitter", 904, DfcEmitterBlockEntity::serverTick);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesDfcStabilizerAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesAdjacentReceiverWithDiagnostics(helper, ModBlocks.DFC_STABILIZER,
                DfcStabilizerBlockEntity.class, "dfc_stabilizer", 908, DfcStabilizerBlockEntity::serverTick);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesCyclotronRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() - 1, chunkStartZ + 912);
        forceLoadedChunks(level, supportPos.offset(-12, -1, -12), supportPos.offset(12, 7, 12));
        clearBox(level, supportPos.above().offset(-12, -1, -12), supportPos.above(7).offset(12, 0, 12));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack cyclotronStack = new ItemStack(ModBlocks.MACHINE_CYCLOTRON.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, cyclotronStack);

        var placeResult = cyclotronStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        CyclotronBlockEntity cyclotron = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                CyclotronBlockEntity.class, ModBlocks.MACHINE_CYCLOTRON.get());
        if (cyclotron == null) {
            throw new AssertionError("player useOn machine_cyclotron placement returned " + placeResult
                    + " without placing a machine_cyclotron core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_cyclotron block item action after placing the core");
        BlockPos machinePos = cyclotron.getBlockPos();

        BlockPos portCablePos = machinePos.west(3).south();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(cyclotron, level.getBlockEntity(machinePos),
                "player-placed machine_cyclotron core survives west/south remote-port cable placement");
        CyclotronBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), cyclotron);
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos,
                cyclotronEnergyPorts(), cyclotron, "player-placed machine_cyclotron", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_cyclotron diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -3 0 1 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesExposureChamberRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() - 1, chunkStartZ + 928);
        forceLoadedChunks(level, supportPos.offset(-14, -1, -12), supportPos.offset(14, 7, 12));
        clearBox(level, supportPos.above().offset(-14, -1, -12), supportPos.above(7).offset(14, 0, 12));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() - 6.5D);
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack chamberStack = new ItemStack(ModBlocks.MACHINE_EXPOSURE_CHAMBER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, chamberStack);

        var placeResult = chamberStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        ExposureChamberBlockEntity chamber = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                ExposureChamberBlockEntity.class, ModBlocks.MACHINE_EXPOSURE_CHAMBER.get());
        if (chamber == null) {
            throw new AssertionError("player useOn machine_exposure_chamber placement returned " + placeResult
                    + " without placing a machine_exposure_chamber core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_exposure_chamber block item action after placing the core");
        BlockPos machinePos = chamber.getBlockPos();
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        assertTrue(facing == Direction.NORTH,
                "player-placed machine_exposure_chamber faces north for the selected legacy west-side ports");

        BlockPos portCablePos = machinePos.west(9);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(chamber, level.getBlockEntity(machinePos),
                "player-placed machine_exposure_chamber core survives west remote-port cable placement");
        ExposureChamberBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), chamber);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                chamber, "player-placed machine_exposure_chamber", 5, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_exposure_chamber diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -9 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=5", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesFelRemoteReceiverPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() - 1, chunkStartZ + 944);
        forceLoadedChunks(level, supportPos.offset(-10, -1, -10), supportPos.offset(10, 6, 10));
        clearBox(level, supportPos.above().offset(-10, -1, -10), supportPos.above(6).offset(10, 0, 10));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() - 6.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 0.5D);
        player.setYRot(90.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack felStack = new ItemStack(ModBlocks.MACHINE_FEL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, felStack);

        var placeResult = felStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        FelBlockEntity fel = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                FelBlockEntity.class, ModBlocks.MACHINE_FEL.get());
        if (fel == null) {
            throw new AssertionError("player useOn machine_fel placement returned " + placeResult
                    + " without placing a machine_fel core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_fel block item action after placing the core");
        BlockPos machinePos = fel.getBlockPos();
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        assertTrue(facing == Direction.EAST,
                "player-placed machine_fel faces east for the selected legacy west-side receiver port");

        BlockPos portCablePos = machinePos.west(5).above();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(fel, level.getBlockEntity(machinePos),
                "player-placed machine_fel core survives west elevated remote-port cable placement");
        FelBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), fel);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                fel, "player-placed machine_fel", 1, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_fel diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -5 1 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=1", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesLargeRadarRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 720);
        forceLoadedChunks(level, supportPos.offset(-9, -2, -9), supportPos.offset(9, 8, 9));
        clearBox(level, supportPos.above().offset(-9, -2, -9), supportPos.above(8).offset(9, 0, 9));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack radarStack = new ItemStack(ModBlocks.MACHINE_RADAR_LARGE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, radarStack);

        var placeResult = radarStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        RadarLargeBlockEntity radar = findBlockEntityAroundOrNull(level, supportPos.above(), 8,
                RadarLargeBlockEntity.class, ModBlocks.MACHINE_RADAR_LARGE.get());
        if (radar == null) {
            throw new AssertionError("player useOn machine_radar_large placement returned " + placeResult
                    + " without placing a machine_radar_large core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_radar_large block item action after placing the core");
        BlockPos machinePos = radar.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_radar_large faces south while exposing legacy power proxy ports");
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, radar, "machine_radar_large",
                List.of(new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
                        new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)));

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(radar, level.getBlockEntity(machinePos),
                "player-placed machine_radar_large core survives west remote receiver-port cable placement");
        HbmEnergyUtil.PortSetSnapshot ports = radar.inspectEnergyPorts();
        assertEquals(4, ports.totalPorts(),
                "player-placed machine_radar_large exposes four legacy power-proxy receiver ports");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed machine_radar_large west remote receiver port sees the red_cable network: " + ports);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before machine_radar_large receiver transfer");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_radar_large diagnostic port has a valid power net");
        final int[] transferAttempts = {0};
        helper.onEachTick(() -> {
            radar.refreshEnergyConnections();
            HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
            long transferred = powerNet.update();
            if (transferred <= 0L) {
                if (++transferAttempts[0] >= 90) {
                    HbmEnergyUtil.PortSetSnapshot latePorts = radar.inspectEnergyPorts();
                    throw new AssertionError("player-placed machine_radar_large remote receiver port did not transfer HE; ports="
                            + latePorts + ", net=" + beforeUpdate + ", radarPower=" + radar.getPower()
                            + ", batteryPower=" + battery.getPower());
                }
                return;
            }
            HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
            assertTrue(radar.getPower() > 0L,
                    "player-placed machine_radar_large remote receiver port received HE after legacy periodic subscription; before="
                            + beforeUpdate + ", after=" + afterUpdate);
            assertTrue(battery.getPower() < 100_000L,
                    "machine_battery output spent HE into player-placed machine_radar_large");
            int expectedLinks = afterUpdate.links();
            assertCommandVisibleMessage(level, machinePos,
                    "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                    "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
            assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                    "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

            helper.succeed();
        });
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesGasCentAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 304);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 7, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(7).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack gasCentStack = new ItemStack(ModBlocks.MACHINE_GASCENT.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, gasCentStack);

        var placeResult = gasCentStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        GasCentBlockEntity gasCent = findBlockEntityAroundOrNull(level, supportPos.above(), 5,
                GasCentBlockEntity.class, ModBlocks.MACHINE_GASCENT.get());
        if (gasCent == null) {
            throw new AssertionError("player useOn machine_gascent placement returned " + placeResult
                    + " without placing a machine_gascent core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_gascent block item action after placing the core");
        BlockPos machinePos = gasCent.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_gascent faces south while exposing adjacent legacy receiver ports");
        assertGasCentHasResolvingPowerFluidProxies(level, gasCent);

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(gasCent, level.getBlockEntity(machinePos),
                "player-placed machine_gascent core survives adjacent west receiver-port cable placement");
        GasCentBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), gasCent);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                gasCent, "player-placed machine_gascent", 5, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_gascent diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=5", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesCentrifugeAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 1264);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 7, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(7).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack centrifugeStack = new ItemStack(ModBlocks.MACHINE_CENTRIFUGE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, centrifugeStack);

        var placeResult = centrifugeStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ProcessingMachineBlockEntity centrifuge = findBlockEntityAroundOrNull(level, supportPos.above(), 5,
                ProcessingMachineBlockEntity.class, ModBlocks.MACHINE_CENTRIFUGE.get());
        if (centrifuge == null) {
            throw new AssertionError("player useOn machine_centrifuge placement returned " + placeResult
                    + " without placing a machine_centrifuge core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_centrifuge block item action after placing the core");
        assertSame(ProcessingMachineBlockEntity.Kind.CENTRIFUGE, centrifuge.kind(),
                "player-placed machine_centrifuge keeps ProcessingMachine CENTRIFUGE kind");
        BlockPos machinePos = centrifuge.getBlockPos();
        assertProcessingCentrifugeHasResolvingPowerFluidProxies(level, centrifuge);

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(centrifuge, level.getBlockEntity(machinePos),
                "player-placed machine_centrifuge core survives adjacent west receiver-side cable placement");
        ProcessingMachineBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), centrifuge);
        assertAdjacentReceiverReceivesPower(level, batteryPos, portCablePos, machinePos, centrifuge,
                "player-placed machine_centrifuge", 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_centrifuge adjacent side cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesCrystallizerRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 1280);
        forceLoadedChunks(level, supportPos.offset(-10, -8, -10), supportPos.offset(10, 10, 10));
        clearBox(level, supportPos.above().offset(-10, -8, -10), supportPos.above(10).offset(10, 0, 10));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack crystallizerStack = new ItemStack(ModBlocks.MACHINE_CRYSTALLIZER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, crystallizerStack);

        var placeResult = crystallizerStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ProcessingMachineBlockEntity crystallizer = findBlockEntityAroundOrNull(level, supportPos.above(), 8,
                ProcessingMachineBlockEntity.class, ModBlocks.MACHINE_CRYSTALLIZER.get());
        if (crystallizer == null) {
            throw new AssertionError("player useOn machine_crystallizer placement returned " + placeResult
                    + " without placing a machine_crystallizer core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_crystallizer block item action after placing the core");
        assertSame(ProcessingMachineBlockEntity.Kind.CRYSTALLIZER, crystallizer.kind(),
                "player-placed machine_crystallizer keeps ProcessingMachine CRYSTALLIZER kind");
        BlockPos machinePos = crystallizer.getBlockPos();
        assertProcessingCrystallizerHasResolvingPowerFluidProxies(level, crystallizer);

        BlockPos portCablePos = machinePos.west(2).south();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(crystallizer, level.getBlockEntity(machinePos),
                "player-placed machine_crystallizer core survives external west/south receiver-port cable placement");
        ProcessingMachineBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), crystallizer);
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos,
                crystallizerEnergyPorts(), crystallizer, "player-placed machine_crystallizer", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_crystallizer diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 1 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesDeuteriumExtractorAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 464);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack extractorStack = new ItemStack(ModBlocks.MACHINE_DEUTERIUM_EXTRACTOR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, extractorStack);

        var placeResult = extractorStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        DeuteriumExtractorBlockEntity extractor = findBlockEntityAroundOrNull(level, supportPos.above(), 3,
                DeuteriumExtractorBlockEntity.class, ModBlocks.MACHINE_DEUTERIUM_EXTRACTOR.get());
        if (extractor == null) {
            throw new AssertionError("player useOn machine_deuterium_extractor placement returned " + placeResult
                    + " without placing a machine_deuterium_extractor core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_deuterium_extractor block item action after placing the core");
        BlockPos machinePos = extractor.getBlockPos();

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(extractor, level.getBlockEntity(machinePos),
                "player-placed machine_deuterium_extractor core survives adjacent west receiver-port cable placement");
        DeuteriumExtractorBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), extractor);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                extractor, "player-placed machine_deuterium_extractor", 6, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_deuterium_extractor diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesDeuteriumTowerRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 496);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 14, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(14).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack towerStack = new ItemStack(ModBlocks.MACHINE_DEUTERIUM_TOWER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, towerStack);

        var placeResult = towerStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        DeuteriumTowerBlockEntity tower = findBlockEntityAroundOrNull(level, supportPos.above(), 12,
                DeuteriumTowerBlockEntity.class, ModBlocks.MACHINE_DEUTERIUM_TOWER.get());
        if (tower == null) {
            throw new AssertionError("player useOn machine_deuterium_tower placement returned " + placeResult
                    + " without placing a machine_deuterium_tower core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_deuterium_tower block item action after placing the core");
        BlockPos machinePos = tower.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_deuterium_tower faces south while exposing eight legacy receiver ports");
        assertDeuteriumTowerHasResolvingPowerFluidProxies(level, tower);

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(tower, level.getBlockEntity(machinePos),
                "player-placed machine_deuterium_tower core survives external west receiver-port cable placement");
        DeuteriumTowerBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), tower);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                tower, "player-placed machine_deuterium_tower", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_deuterium_tower diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesOreSlopperRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 336);
        forceLoadedChunks(level, supportPos.offset(-14, -8, -14), supportPos.offset(14, 10, 14));
        clearBox(level, supportPos.above().offset(-14, -8, -14), supportPos.above(10).offset(14, 0, 14));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack oreSlopperStack = new ItemStack(ModBlocks.MACHINE_ORE_SLOPPER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, oreSlopperStack);

        var placeResult = oreSlopperStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        OreSlopperBlockEntity oreSlopper = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                OreSlopperBlockEntity.class, ModBlocks.MACHINE_ORE_SLOPPER.get());
        if (oreSlopper == null) {
            throw new AssertionError("player useOn machine_ore_slopper placement returned " + placeResult
                    + " without placing a machine_ore_slopper core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_ore_slopper block item action after placing the core");
        BlockPos machinePos = oreSlopper.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_ore_slopper faces south while exposing eight fixed legacy receiver ports");
        assertOreSlopperHasResolvingPowerFluidProxies(level, oreSlopper);

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(oreSlopper, level.getBlockEntity(machinePos),
                "player-placed machine_ore_slopper core survives external west receiver-port cable placement");
        OreSlopperBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), oreSlopper);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                oreSlopper, "player-placed machine_ore_slopper", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_ore_slopper diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesElectricHeaterRemoteReceiverPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 352);
        forceLoadedChunks(level, supportPos.offset(-9, 0, -9), supportPos.offset(9, 5, 12));
        clearBox(level, supportPos.above().offset(-9, 0, -9), supportPos.above(5).offset(9, 0, 12));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack heaterStack = new ItemStack(ModBlocks.HEATER_ELECTRIC.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, heaterStack);

        var placeResult = heaterStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ElectricHeaterBlockEntity heater = findBlockEntityAroundOrNull(level, supportPos.above(), 6,
                ElectricHeaterBlockEntity.class, ModBlocks.HEATER_ELECTRIC.get());
        if (heater == null) {
            throw new AssertionError("player useOn heater_electric placement returned " + placeResult
                    + " without placing a heater_electric core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the heater_electric block item action after placing the core");
        BlockPos machinePos = heater.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed heater_electric faces south while exposing the legacy forward receiver port");
        assertElectricHeaterHasResolvingPowerProxy(level, heater);

        heater.toggleSetting();
        assertTrue(heater.getConsumption() > 0L,
                "player-placed heater_electric setting 1 creates the legacy receiver demand window");

        BlockPos portCablePos = machinePos.south(3);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(heater, level.getBlockEntity(machinePos),
                "player-placed heater_electric core survives external south receiver-port cable placement");
        ElectricHeaterBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), heater);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                heater, "player-placed heater_electric", 1, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed heater_electric diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 0 0 3 south", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=1", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesPoweredCondenserRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 368);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 10, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(10).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack condenserStack = new ItemStack(ModBlocks.MACHINE_CONDENSER_POWERED.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, condenserStack);

        var placeResult = condenserStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        PoweredCondenserBlockEntity condenser = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                PoweredCondenserBlockEntity.class, ModBlocks.MACHINE_CONDENSER_POWERED.get());
        if (condenser == null) {
            throw new AssertionError("player useOn machine_condenser_powered placement returned " + placeResult
                    + " without placing a machine_condenser_powered core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_condenser_powered block item action after placing the core");
        BlockPos machinePos = condenser.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_condenser_powered faces south while exposing y+1 legacy receiver ports");
        assertPoweredCondenserHasResolvingPowerFluidProxies(level, condenser);

        BlockPos portCablePos = machinePos.west(4).above();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(condenser, level.getBlockEntity(machinePos),
                "player-placed machine_condenser_powered core survives external west receiver-port cable placement");
        PoweredCondenserBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), condenser);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                condenser, "player-placed machine_condenser_powered", 6, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_condenser_powered diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -4 1 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesPyroOvenRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 400);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 10, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(10).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack pyroOvenStack = new ItemStack(ModBlocks.MACHINE_PYROOVEN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, pyroOvenStack);

        var placeResult = pyroOvenStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        PyroOvenBlockEntity pyroOven = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                PyroOvenBlockEntity.class, ModBlocks.MACHINE_PYROOVEN.get());
        if (pyroOven == null) {
            throw new AssertionError("player useOn machine_pyrooven placement returned " + placeResult
                    + " without placing a machine_pyrooven core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_pyrooven block item action after placing the core");
        BlockPos machinePos = pyroOven.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_pyrooven faces south while exposing the legacy west receiver ports");
        assertPyroOvenHasResolvingPowerFluidProxies(level, pyroOven);

        BlockPos portCablePos = machinePos.west(3);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(pyroOven, level.getBlockEntity(machinePos),
                "player-placed machine_pyrooven core survives external west receiver-port cable placement");
        PyroOvenBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), pyroOven);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                pyroOven, "player-placed machine_pyrooven", 5, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_pyrooven diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -3 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=5", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesPurexRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 1360);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 10, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(10).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack purexStack = new ItemStack(ModBlocks.MACHINE_PUREX.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, purexStack);

        var placeResult = purexStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        LegacyGenericSelectorMachineBlockEntity purex = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                LegacyGenericSelectorMachineBlockEntity.class, ModBlocks.MACHINE_PUREX.get());
        if (purex == null) {
            throw new AssertionError("player useOn machine_purex placement returned " + placeResult
                    + " without placing a machine_purex core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_purex block item action after placing the core");
        assertSame(LegacyGenericSelectorMachineBlockEntity.Kind.PUREX, purex.kind(),
                "player-placed machine_purex keeps the PUREX selector kind");
        BlockPos machinePos = purex.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_purex faces south while exposing the fixed legacy floor-ring ports");
        assertPurexHasResolvingPowerFluidProxies(level, purex);

        BlockPos portCablePos = machinePos.west(3);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(purex, level.getBlockEntity(machinePos),
                "player-placed machine_purex core survives external west receiver-port cable placement");
        LegacyGenericSelectorMachineBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), purex);

        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        HbmEnergyUtil.PortSetSnapshot ports = purex.inspectEnergyPorts();
        assertEquals(20, ports.totalPorts(), "player-placed machine_purex exposes twenty legacy receiver ports");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed machine_purex west receiver port sees the red_cable network: " + ports);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before player-placed machine_purex receiver transfer");
        assertTrue(HbmEnergyUtil.subscribeReceiverToNetwork(level, portCablePos, Direction.WEST,
                purex.getEnergyStorage()),
                "player-placed machine_purex subscribed as receiver through its west legacy port");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_purex diagnostic port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(transferred > 0L,
                "player-placed machine_purex power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(purex.getPower() > 0L,
                "player-placed machine_purex remote port received HE from real red_cable");
        assertTrue(battery.getPower() < 100_000L,
                "machine_battery output spent HE into player-placed machine_purex");
        int expectedLinks = afterUpdate.links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -3 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=20", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesPrecassRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 1376);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 10, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(10).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack precassStack = new ItemStack(ModBlocks.MACHINE_PRECASS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, precassStack);

        var placeResult = precassStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        LegacyGenericSelectorMachineBlockEntity precass = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                LegacyGenericSelectorMachineBlockEntity.class, ModBlocks.MACHINE_PRECASS.get());
        if (precass == null) {
            throw new AssertionError("player useOn machine_precass placement returned " + placeResult
                    + " without placing a machine_precass core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_precass block item action after placing the core");
        assertSame(LegacyGenericSelectorMachineBlockEntity.Kind.PRECASS, precass.kind(),
                "player-placed machine_precass keeps the PRECASS selector kind");
        BlockPos machinePos = precass.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_precass faces south while exposing the fixed legacy floor-ring ports");
        assertGenericSelectorHasResolvingPowerFluidProxies(level, precass, "machine_precass", 1);

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(precass, level.getBlockEntity(machinePos),
                "player-placed machine_precass core survives external west receiver-port cable placement");
        LegacyGenericSelectorMachineBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), precass);

        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        HbmEnergyUtil.PortSetSnapshot ports = precass.inspectEnergyPorts();
        assertEquals(12, ports.totalPorts(), "player-placed machine_precass exposes twelve legacy receiver ports");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed machine_precass west receiver port sees the red_cable network: " + ports);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before player-placed machine_precass receiver transfer");
        assertTrue(HbmEnergyUtil.subscribeReceiverToNetwork(level, portCablePos, Direction.EAST,
                precass.getEnergyStorage()),
                "player-placed machine_precass subscribed as receiver through its west legacy port");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_precass diagnostic port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(transferred > 0L,
                "player-placed machine_precass power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(precass.getPower() > 0L,
                "player-placed machine_precass remote port received HE from real red_cable");
        assertTrue(battery.getPower() < 100_000L,
                "machine_battery output spent HE into player-placed machine_precass");
        int expectedLinks = afterUpdate.links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=12", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2CableDiode")
    public static void cableDiodeTransfersOneWayAcrossRealCableNetworksAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos providerPos = new BlockPos(chunkStartX + 8, anchor.getY() + 4, chunkStartZ + 1392);
        BlockPos inputFirstCablePos = providerPos.east();
        BlockPos inputLastCablePos = providerPos.east(3);
        BlockPos diodePos = providerPos.east(4);
        BlockPos outputFirstCablePos = providerPos.east(5);
        BlockPos outputLastCablePos = providerPos.east(7);
        BlockPos receiverPos = providerPos.east(8);
        forceLoadedChunks(level, providerPos.offset(-2, -2, -2), receiverPos.offset(2, 2, 2));
        clearBox(level, providerPos.offset(-2, -2, -2), receiverPos.offset(2, 2, 2));

        MachineBatteryBlockEntity providerBattery = prepareOutputBatteryAndCableLine(level, providerPos,
                inputFirstCablePos, inputLastCablePos, 100_000L);
        level.setBlock(diodePos, ModBlocks.CABLE_DIODE.get().defaultBlockState()
                .setValue(CableDiodeBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(diodePos) instanceof CableDiodeBlockEntity diode)) {
            throw new AssertionError("No cable_diode block entity at " + diodePos);
        }
        assertSame(Direction.EAST, diode.getOutputDirection(),
                "cable_diode with legacy facing WEST outputs east");
        assertTrue(!diode.canConnectEnergy(Direction.EAST),
                "cable_diode output side is not an Energy receiver input side");
        assertTrue(diode.canConnectEnergy(Direction.WEST),
                "cable_diode input side accepts Energy receiver subscription");

        level.setBlock(receiverPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity receiverBattery = requireMachineBattery(level, receiverPos);
        while (receiverBattery.getRedLow() != MachineBatteryBlockEntity.MODE_INPUT) {
            receiverBattery.cycleRedLowMode();
        }
        receiverBattery.setPower(0L);
        for (int x = outputFirstCablePos.getX(); x <= outputLastCablePos.getX(); x++) {
            BlockPos cablePos = new BlockPos(x, outputFirstCablePos.getY(), outputFirstCablePos.getZ());
            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, cablePos);
        }
        HbmEnergyNodespace.tick(level);

        var player = FakePlayerFactory.getMinecraft(level);
        CableDiodeBlock diodeBlock = (CableDiodeBlock) ModBlocks.CABLE_DIODE.get();
        assertTrue(diodeBlock.onToolUse(level, player, diodePos, Direction.UP, Vec3.atCenterOf(diodePos),
                Toolable.ToolType.SCREWDRIVER),
                "cable_diode accepts screwdriver level increase");
        assertTrue(diodeBlock.onToolUse(level, player, diodePos, Direction.UP, Vec3.atCenterOf(diodePos),
                Toolable.ToolType.DEFUSER),
                "cable_diode accepts defuser priority cycle");
        assertEquals(2, diode.getThroughputLevel(), "screwdriver raises cable_diode throughput level");
        assertEquals(100L, diode.getMaxPower(), "level 2 cable_diode max transfer is 10^2 HE/t");

        CableDiodeBlockEntity.serverTick(level, diodePos, level.getBlockState(diodePos), diode);
        assertOutputBatteryProviderSubscribes(level, providerPos, inputLastCablePos, providerBattery,
                "machine_battery output provider subscribes before cable_diode input transfer");
        if (!HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, receiverPos, Direction.WEST,
                receiverBattery.getEnergyStorage())) {
            refreshCableLine(level, outputFirstCablePos, outputLastCablePos);
            HbmEnergyNodespace.tick(level);
            assertTrue(HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, receiverPos, Direction.WEST,
                    receiverBattery.getEnergyStorage()),
                    "machine_battery input receiver subscribes to cable_diode output network");
        }

        HbmPowerNet inputNet = HbmEnergyUtil.getPowerNet(level, inputLastCablePos);
        HbmPowerNet outputNet = HbmEnergyUtil.getPowerNet(level, outputFirstCablePos);
        assertTrue(inputNet != null && inputNet.isValid(),
                "cable_diode west input cable has a valid HBM power net");
        assertTrue(outputNet != null && outputNet.isValid(),
                "cable_diode east output cable has a valid HBM power net");
        HbmPowerNet.DebugSnapshot beforeInputUpdate = inputNet.createDebugSnapshot();
        assertTrue(beforeInputUpdate.receivers() >= 1,
                "cable_diode server tick registered as receiver on the input-side network: " + beforeInputUpdate);
        long transferred = inputNet.update();
        HbmPowerNet.DebugSnapshot afterInputUpdate = inputNet.createDebugSnapshot();
        HbmPowerNet.DebugSnapshot afterOutputUpdate = outputNet.createDebugSnapshot();
        assertTrue(transferred > 0L,
                "cable_diode input net transferred HE; before=" + beforeInputUpdate
                        + ", afterInput=" + afterInputUpdate + ", afterOutput=" + afterOutputUpdate);
        assertTrue(receiverBattery.getPower() > 0L,
                "cable_diode forwarded HE into the east output network receiver");
        assertTrue(providerBattery.getPower() < 100_000L,
                "machine_battery output spent HE through cable_diode");
        assertTrue(diode.getPower() > 0L,
                "cable_diode tracked transferred HE during the current tick");

        CompoundTag diodeInfo = new CompoundTag();
        diode.provideExtraInfo(diodeInfo);
        assertEquals(2, diodeInfo.getInt("level"), "cable_diode EC extra info exposes throughput level");
        assertEquals("HIGH", diodeInfo.getString("priority"), "defuser cycles cable_diode priority from NORMAL to HIGH");
        assertEquals(100L, diodeInfo.getLong("maxRate"), "cable_diode EC extra info exposes max transfer");
        assertEquals("east", diodeInfo.getString("output"), "cable_diode EC extra info exposes output side");
        CompoundTag saved = diode.saveWithFullMetadata();
        assertEquals(2, saved.getInt("level"), "cable_diode saves legacy level NBT");
        assertEquals(HbmEnergyReceiver.ConnectionPriority.HIGH.ordinal(), saved.getByte("p"),
                "cable_diode saves legacy priority ordinal NBT");

        CompoundTag commandInfo = new CompoundTag();
        CompatEnergyControl.getEnergyData(diode, commandInfo);
        CompatEnergyControl.getExtraData(diode, commandInfo);
        assertCommandVisibleMessage(level, diodePos, "hbm energy info " + commandPos(diodePos), commandInfo.size(),
                "Energy info at " + diodePos.toShortString(), "level=", "maxRate=", "output=", "priority=");
        assertCommandVisibleMessage(level, inputLastCablePos, "hbm energy network " + commandPos(inputLastCablePos),
                afterInputUpdate.links(),
                "Energy network at " + inputLastCablePos.toShortString(), "providers=", "receivers=",
                "lastTransfer=");
        assertCommandVisibleMessage(level, outputFirstCablePos, "hbm energy node " + commandPos(outputFirstCablePos),
                afterOutputUpdate.links(),
                "Energy network at " + outputFirstCablePos.toShortString(), "providers=", "receivers=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesSolderingStationRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 432);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 10, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(10).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack solderingStationStack = new ItemStack(ModBlocks.MACHINE_SOLDERING_STATION.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, solderingStationStack);

        var placeResult = solderingStationStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        SolderingStationBlockEntity station = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                SolderingStationBlockEntity.class, ModBlocks.MACHINE_SOLDERING_STATION.get());
        if (station == null) {
            throw new AssertionError("player useOn machine_soldering_station placement returned " + placeResult
                    + " without placing a machine_soldering_station core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_soldering_station block item action after placing the core");
        BlockPos machinePos = station.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_soldering_station faces south while exposing eight legacy receiver ports");
        assertSolderingStationHasResolvingPowerFluidProxies(level, station);

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(station, level.getBlockEntity(machinePos),
                "player-placed machine_soldering_station core survives external west receiver-port cable placement");
        SolderingStationBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), station);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                station, "player-placed machine_soldering_station", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_soldering_station diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesExcavatorRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 448);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 10, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(10).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack excavatorStack = new ItemStack(ModBlocks.MACHINE_EXCAVATOR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, excavatorStack);

        var placeResult = excavatorStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ExcavatorBlockEntity excavator = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                ExcavatorBlockEntity.class, ModBlocks.MACHINE_EXCAVATOR.get());
        if (excavator == null) {
            throw new AssertionError("player useOn machine_excavator placement returned " + placeResult
                    + " without placing a machine_excavator core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_excavator block item action after placing the core");
        BlockPos machinePos = excavator.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_excavator faces south while exposing four legacy receiver ports");
        assertExcavatorHasResolvingPowerFluidProxies(level, excavator);

        BlockPos portCablePos = machinePos.west(4).above();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(excavator, level.getBlockEntity(machinePos),
                "player-placed machine_excavator core survives external west receiver-port cable placement");
        ExcavatorBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), excavator);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                excavator, "player-placed machine_excavator", 4, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_excavator diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -4 1 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesMiningLaserTopReceiverPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos expectedCorePos = new BlockPos(chunkStartX + 20, anchor.getY() + 1, chunkStartZ + 368);
        BlockPos portCablePos = expectedCorePos.above(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, expectedCorePos.offset(-10, -4, -10), expectedCorePos.offset(10, 6, 10));
        clearBox(level, expectedCorePos.offset(-10, -4, -10), expectedCorePos.above(6).offset(10, 0, 10));
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(expectedCorePos.getX() + 0.5D, expectedCorePos.getY() + 1.0D,
                expectedCorePos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack laserStack = new ItemStack(ModBlocks.MACHINE_MINING_LASER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, laserStack);

        var placeResult = laserStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(portCablePos, Direction.DOWN)));

        MiningLaserBlockEntity laser = findBlockEntityAroundOrNull(level, expectedCorePos, 6,
                MiningLaserBlockEntity.class, ModBlocks.MACHINE_MINING_LASER.get());
        if (laser == null) {
            throw new AssertionError("player useOn machine_mining_laser placement returned " + placeResult
                    + " without placing a machine_mining_laser core around " + expectedCorePos
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_mining_laser block item action after placing the core");
        BlockPos machinePos = laser.getBlockPos();
        assertTrue(expectedCorePos.equals(machinePos),
                "player-placed machine_mining_laser core uses the legacy ceiling height offset");
        assertMiningLaserHasResolvingPowerFluidProxies(level, laser);

        forceLoadedChunks(level, batteryPos, machinePos);
        assertSame(laser, level.getBlockEntity(machinePos),
                "player-placed machine_mining_laser core survives external top remote-port cable placement");
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                laser, "player-placed machine_mining_laser", 1, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_mining_laser diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 0 2 0 up", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=1", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFixedMachinePorts")
    public static void playerUseOnPlacesElectrolyserRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 624);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 12, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(12).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 10.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack electrolyserStack = new ItemStack(ModBlocks.MACHINE_ELECTROLYSER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, electrolyserStack);

        var placeResult = electrolyserStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ElectrolyserBlockEntity electrolyser = findBlockEntityAroundOrNull(level, supportPos.above(), 18,
                ElectrolyserBlockEntity.class, ModBlocks.MACHINE_ELECTROLYSER.get());
        if (electrolyser == null) {
            throw new AssertionError("player useOn machine_electrolyser placement returned " + placeResult
                    + " without placing a machine_electrolyser core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_electrolyser block item action after placing the core");
        BlockPos machinePos = electrolyser.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_electrolyser faces south while exposing six fixed legacy receiver ports");
        assertElectrolyserHasResolvingPowerFluidProxies(level, electrolyser);

        BlockPos portCablePos = machinePos.offset(-1, 0, -6);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(electrolyser, level.getBlockEntity(machinePos),
                "player-placed machine_electrolyser core survives external north receiver-port cable placement");
        ElectrolyserBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), electrolyser);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                electrolyser, "player-placed machine_electrolyser", 6, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_electrolyser diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 -6 north", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedOilMachinePorts")
    public static void playerUseOnPlacesHydrotreaterRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 560);
        forceLoadedChunks(level, supportPos.offset(-16, -8, -16), supportPos.offset(16, 10, 16));
        clearBox(level, supportPos.above().offset(-16, -8, -16), supportPos.above(10).offset(16, 0, 16));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack hydrotreaterStack = new ItemStack(ModBlocks.MACHINE_HYDROTREATER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, hydrotreaterStack);

        var placeResult = hydrotreaterStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        HydrotreaterBlockEntity hydrotreater = findBlockEntityAroundOrNull(level, supportPos.above(), 14,
                HydrotreaterBlockEntity.class, ModBlocks.MACHINE_HYDROTREATER.get());
        if (hydrotreater == null) {
            throw new AssertionError("player useOn machine_hydrotreater placement returned " + placeResult
                    + " without placing a machine_hydrotreater core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_hydrotreater block item action after placing the core");
        BlockPos machinePos = hydrotreater.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_hydrotreater faces south while exposing fixed legacy receiver ports");

        BlockPos portCablePos = machinePos.offset(-2, 0, 1);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(hydrotreater, level.getBlockEntity(machinePos),
                "player-placed machine_hydrotreater core survives external west receiver-port cable placement");
        HydrotreaterBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), hydrotreater);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                hydrotreater, "player-placed machine_hydrotreater", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_hydrotreater diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 1 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedOilMachinePorts")
    public static void playerUseOnPlacesCatalyticReformerRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 576);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 12, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(12).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack reformerStack = new ItemStack(ModBlocks.MACHINE_CATALYTIC_REFORMER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, reformerStack);

        var placeResult = reformerStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        CatalyticReformerBlockEntity reformer = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                CatalyticReformerBlockEntity.class, ModBlocks.MACHINE_CATALYTIC_REFORMER.get());
        if (reformer == null) {
            throw new AssertionError("player useOn machine_catalytic_reformer placement returned " + placeResult
                    + " without placing a machine_catalytic_reformer core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_catalytic_reformer block item action after placing the core");
        BlockPos machinePos = reformer.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_catalytic_reformer faces south while exposing rotated legacy receiver ports");

        BlockPos portCablePos = machinePos.west(3);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(reformer, level.getBlockEntity(machinePos),
                "player-placed machine_catalytic_reformer core survives external west receiver-port cable placement");
        CatalyticReformerBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), reformer);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                reformer, "player-placed machine_catalytic_reformer", 6, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_catalytic_reformer diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -3 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedOilMachinePorts")
    public static void playerUseOnPlacesVacuumDistillRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 592);
        forceLoadedChunks(level, supportPos.offset(-16, -8, -16), supportPos.offset(16, 12, 16));
        clearBox(level, supportPos.above().offset(-16, -8, -16), supportPos.above(12).offset(16, 0, 16));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack distillStack = new ItemStack(ModBlocks.MACHINE_VACUUM_DISTILL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, distillStack);

        var placeResult = distillStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        VacuumDistillBlockEntity distill = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                VacuumDistillBlockEntity.class, ModBlocks.MACHINE_VACUUM_DISTILL.get());
        if (distill == null) {
            throw new AssertionError("player useOn machine_vacuum_distill placement returned " + placeResult
                    + " without placing a machine_vacuum_distill core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_vacuum_distill block item action after placing the core");
        BlockPos machinePos = distill.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_vacuum_distill faces south while exposing fixed legacy receiver ports");

        BlockPos portCablePos = machinePos.offset(-2, 0, 1);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(distill, level.getBlockEntity(machinePos),
                "player-placed machine_vacuum_distill core survives external west receiver-port cable placement");
        VacuumDistillBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), distill);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                distill, "player-placed machine_vacuum_distill", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_vacuum_distill diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 1 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedOilMachinePorts")
    public static void playerUseOnPlacesRefineryRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 608);
        forceLoadedChunks(level, supportPos.offset(-16, -8, -16), supportPos.offset(16, 12, 16));
        clearBox(level, supportPos.above().offset(-16, -8, -16), supportPos.above(12).offset(16, 0, 16));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack refineryStack = new ItemStack(ModBlocks.MACHINE_REFINERY.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, refineryStack);

        var placeResult = refineryStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        RefineryBlockEntity refinery = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                RefineryBlockEntity.class, ModBlocks.MACHINE_REFINERY.get());
        if (refinery == null) {
            throw new AssertionError("player useOn machine_refinery placement returned " + placeResult
                    + " without placing a machine_refinery core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_refinery block item action after placing the core");
        BlockPos machinePos = refinery.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_refinery faces south while exposing fixed legacy receiver ports");

        BlockPos portCablePos = machinePos.offset(-2, 0, 1);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(refinery, level.getBlockEntity(machinePos),
                "player-placed machine_refinery core survives external west receiver-port cable placement");
        RefineryBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), refinery);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                refinery, "player-placed machine_refinery", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_refinery diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 1 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=8", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedArcWelderPorts")
    public static void playerUseOnPlacesArcWelderRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 624);
        forceLoadedChunks(level, supportPos.offset(-16, -8, -16), supportPos.offset(16, 12, 16));
        clearBox(level, supportPos.above().offset(-16, -8, -16), supportPos.above(12).offset(16, 0, 16));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack arcWelderStack = new ItemStack(ModBlocks.MACHINE_ARC_WELDER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, arcWelderStack);

        var placeResult = arcWelderStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ArcWelderBlockEntity arcWelder = findBlockEntityAroundOrNull(level, supportPos.above(), 12,
                ArcWelderBlockEntity.class, ModBlocks.MACHINE_ARC_WELDER.get());
        if (arcWelder == null) {
            throw new AssertionError("player useOn machine_arc_welder placement returned " + placeResult
                    + " without placing a machine_arc_welder core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_arc_welder block item action after placing the core");
        BlockPos machinePos = arcWelder.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_arc_welder faces south while exposing legacy receiver ports");

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(arcWelder, level.getBlockEntity(machinePos),
                "player-placed machine_arc_welder core survives external west receiver-port cable placement");
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos,
                arcWelderEnergyPorts(arcWelder), arcWelder, "player-placed machine_arc_welder", 10, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_arc_welder diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=10", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedCompressorPorts")
    public static void playerUseOnPlacesCompressorRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 22, anchor.getY() + 5, chunkStartZ + 292);
        forceLoadedChunks(level, supportPos.offset(-14, -8, -14), supportPos.offset(14, 10, 14));
        clearBox(level, supportPos.above().offset(-14, -8, -14), supportPos.above(10).offset(14, 0, 14));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack compressorStack = new ItemStack(ModBlocks.MACHINE_COMPRESSOR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, compressorStack);

        var placeResult = compressorStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        CompressorBlockEntity compressor = findCompressorAroundOrNull(level, supportPos.above(), 14,
                ModBlocks.MACHINE_COMPRESSOR.get());
        if (compressor == null) {
            throw new AssertionError("player useOn machine_compressor placement returned " + placeResult
                    + " without placing a machine_compressor core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_compressor block item action after placing the core");
        BlockPos machinePos = compressor.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_compressor faces south for the selected legacy west port");
        assertCompressorHasResolvingPowerProxies(level, compressor, false, "player-placed machine_compressor");

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(compressor, level.getBlockEntity(machinePos),
                "player-placed machine_compressor core survives external west remote-port cable placement");
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                compressor, "player-placed machine_compressor", 3, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_compressor diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=3", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedCompressorPorts")
    public static void playerUseOnPlacesCompactCompressorRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 22, anchor.getY() + 5, chunkStartZ + 308);
        forceLoadedChunks(level, supportPos.offset(-12, -5, -12), supportPos.offset(12, 8, 12));
        clearBox(level, supportPos.above().offset(-12, -5, -12), supportPos.above(8).offset(12, 0, 12));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 7.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack compressorStack = new ItemStack(ModBlocks.MACHINE_COMPRESSOR_COMPACT.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, compressorStack);

        var placeResult = compressorStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        CompressorBlockEntity compressor = findCompressorAroundOrNull(level, supportPos.above(), 12,
                ModBlocks.MACHINE_COMPRESSOR_COMPACT.get());
        if (compressor == null) {
            throw new AssertionError("player useOn machine_compressor_compact placement returned " + placeResult
                    + " without placing a machine_compressor_compact core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_compressor_compact block item action after placing the core");
        BlockPos machinePos = compressor.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_compressor_compact faces south for the selected legacy west port");
        assertCompressorHasResolvingPowerProxies(level, compressor, true,
                "player-placed machine_compressor_compact");

        BlockPos portCablePos = machinePos.west(4).above();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(compressor, level.getBlockEntity(machinePos),
                "player-placed machine_compressor_compact core survives external west remote-port cable placement");
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                compressor, "player-placed machine_compressor_compact", 6, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_compressor_compact diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -4 1 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedXrFloorRing")
    public static void playerUseOnPlacesAssemblyMachineRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 19, anchor.getY() - 1, chunkStartZ + 240);
        forceLoadedChunks(level, supportPos.offset(-6, 0, -6), supportPos.offset(6, 3, 6));
        clearBox(level, supportPos.above().offset(-6, 0, -6), supportPos.above(3).offset(6, 0, 6));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack assemblerStack = new ItemStack(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, assemblerStack);

        var placeResult = assemblerStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        AssemblyMachineBlockEntity assembler = findAssemblyMachineAroundOrNull(level, supportPos.above(), 4);
        if (assembler == null) {
            throw new AssertionError("player useOn machine_assembly_machine placement returned " + placeResult
                    + " without placing a machine_assembly_machine core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_assembly_machine block item action after placing the core");
        BlockPos machinePos = assembler.getBlockPos();
        assertXrFloorRingMachineHasResolvingProxies(level, machinePos, assembler,
                "player-placed machine_assembly_machine");

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(assembler, level.getBlockEntity(machinePos),
                "player-placed machine_assembly_machine core survives external west remote-port cable placement");
        assertXrFloorRingRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos,
                assembler.getEnergyStorage(), "player-placed machine_assembly_machine", 100_000L, Direction.EAST);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_assembly_machine diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=12", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedXrFloorRing")
    public static void playerUseOnPlacesChemicalPlantRemotePortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 19, anchor.getY() - 1, chunkStartZ + 248);
        forceLoadedChunks(level, supportPos.offset(-6, 0, -6), supportPos.offset(6, 3, 6));
        clearBox(level, supportPos.above().offset(-6, 0, -6), supportPos.above(3).offset(6, 0, 6));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack chemicalPlantStack = new ItemStack(ModBlocks.MACHINE_CHEMICAL_PLANT.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, chemicalPlantStack);

        var placeResult = chemicalPlantStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ChemicalPlantBlockEntity chemicalPlant = findChemicalPlantAroundOrNull(level, supportPos.above(), 4);
        if (chemicalPlant == null) {
            throw new AssertionError("player useOn machine_chemical_plant placement returned " + placeResult
                    + " without placing a machine_chemical_plant core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_chemical_plant block item action after placing the core");
        BlockPos machinePos = chemicalPlant.getBlockPos();
        assertXrFloorRingMachineHasResolvingProxies(level, machinePos, chemicalPlant,
                "player-placed machine_chemical_plant");

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(chemicalPlant, level.getBlockEntity(machinePos),
                "player-placed machine_chemical_plant core survives external west remote-port cable placement");
        assertXrFloorRingRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos,
                chemicalPlant.getEnergyStorage(), "player-placed machine_chemical_plant", 100_000L, Direction.EAST);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_chemical_plant diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=12", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFactoryPorts")
    public static void playerUseOnPlacesAssemblyFactoryRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 920);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 12, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(12).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack factoryStack = new ItemStack(ModBlocks.MACHINE_ASSEMBLY_FACTORY.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, factoryStack);

        var placeResult = factoryStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        AssemblyFactoryBlockEntity factory = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                AssemblyFactoryBlockEntity.class, ModBlocks.MACHINE_ASSEMBLY_FACTORY.get());
        if (factory == null) {
            throw new AssertionError("player useOn machine_assembly_factory placement returned " + placeResult
                    + " without placing a machine_assembly_factory core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_assembly_factory block item action after placing the core");
        BlockPos machinePos = factory.getBlockPos();
        Direction facing = level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        assertTrue(facing == Direction.SOUTH,
                "player-placed machine_assembly_factory faces south while exposing rotated legacy receiver ports");
        assertFactoryHasResolvingPowerFluidProxies(level, factory, "player-placed machine_assembly_factory");

        BlockPos portCablePos = machinePos.west(3);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(factory, level.getBlockEntity(machinePos),
                "player-placed machine_assembly_factory core survives external west receiver-port cable placement");
        List<HbmEnergyUtil.EnergyPort> energyPorts = LegacyMultiblockPorts.combineEnergyPorts(
                LegacyMultiblockPorts.factoryRecipeEnergyPorts(facing, false),
                LegacyMultiblockPorts.factoryCoolingEnergyPorts(facing));
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos, energyPorts,
                factory, "player-placed machine_assembly_factory", 20, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_assembly_factory diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -3 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=20", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedFactoryPorts")
    public static void playerUseOnPlacesChemicalFactoryRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 940);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 12, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(12).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack factoryStack = new ItemStack(ModBlocks.MACHINE_CHEMICAL_FACTORY.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, factoryStack);

        var placeResult = factoryStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ChemicalFactoryBlockEntity factory = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                ChemicalFactoryBlockEntity.class, ModBlocks.MACHINE_CHEMICAL_FACTORY.get());
        if (factory == null) {
            throw new AssertionError("player useOn machine_chemical_factory placement returned " + placeResult
                    + " without placing a machine_chemical_factory core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_chemical_factory block item action after placing the core");
        BlockPos machinePos = factory.getBlockPos();
        Direction facing = level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        assertTrue(facing == Direction.SOUTH,
                "player-placed machine_chemical_factory faces south while exposing rotated legacy receiver ports");
        assertFactoryHasResolvingPowerFluidProxies(level, factory, "player-placed machine_chemical_factory");

        BlockPos portCablePos = machinePos.west(3);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(factory, level.getBlockEntity(machinePos),
                "player-placed machine_chemical_factory core survives external west receiver-port cable placement");
        List<HbmEnergyUtil.EnergyPort> energyPorts = LegacyMultiblockPorts.combineEnergyPorts(
                LegacyMultiblockPorts.factoryRecipeEnergyPorts(facing, true),
                LegacyMultiblockPorts.factoryCoolingEnergyPorts(facing));
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos, energyPorts,
                factory, "player-placed machine_chemical_factory", 30, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_chemical_factory diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -3 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=30", "networked=");

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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesFensuBottomProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 5, chunkStartZ + 1328);
        forceLoadedChunks(level, supportPos.offset(-8, -4, -8), supportPos.offset(8, 8, 8));
        clearBox(level, supportPos.above().offset(-8, -4, -8), supportPos.above(8).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 5.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack fensuStack = new ItemStack(ModBlocks.MACHINE_FENSU.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, fensuStack);

        var placeResult = fensuStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        FensuBlockEntity fensu = findBlockEntityAroundOrNull(level, supportPos.above(), 8,
                FensuBlockEntity.class, ModBlocks.MACHINE_FENSU.get());
        if (fensu == null) {
            throw new AssertionError("player useOn machine_fensu placement returned " + placeResult
                    + " without placing a machine_fensu core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_fensu block item action after placing the core");
        BlockPos machinePos = fensu.getBlockPos();

        while (fensu.getRedLow() != MachineBatteryBlockEntity.MODE_OUTPUT) {
            fensu.cycleRedLowMode();
        }

        BlockPos portCablePos = machinePos.below();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos);
        assertSame(fensu, level.getBlockEntity(machinePos),
                "player-placed machine_fensu core survives bottom provider-port cable placement");
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, fensu,
                "player-placed machine_fensu", 1, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_fensu bottom port cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 0 -1 0 down", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=1", "networked=");

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
        clearBox(level, batteryPos.offset(-1, -2, -3), machinePos.offset(3, 2, 3));
        level.removeBlock(machinePos, false);
        level.setBlock(machinePos, ModBlocks.MACHINE_STIRLING.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof StirlingBlockEntity stirling)) {
            throw new AssertionError("No machine_stirling block entity at " + machinePos);
        }

        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertStirlingProvidesPowerFromHeatSource(level, batteryPos, portCablePos, battery, stirling,
                "machine_stirling", 4, 1_000_000);
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesStirlingRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 22, anchor.getY() + 5, chunkStartZ + 324);
        forceLoadedChunks(level, supportPos.offset(-8, -5, -8), supportPos.offset(8, 7, 8));
        clearBox(level, supportPos.above().offset(-8, -5, -8), supportPos.above(7).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack stirlingStack = new ItemStack(ModBlocks.MACHINE_STIRLING.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stirlingStack);

        var placeResult = stirlingStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        StirlingBlockEntity stirling = findStirlingAroundOrNull(level, supportPos.above(), 6);
        if (stirling == null) {
            throw new AssertionError("player useOn machine_stirling placement returned " + placeResult
                    + " without placing a machine_stirling core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_stirling block item action after placing the core");
        BlockPos machinePos = stirling.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                        == Direction.SOUTH,
                "player-placed machine_stirling faces south for the selected legacy west port");
        assertStirlingHasResolvingPowerProxies(level, stirling);

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(stirling, level.getBlockEntity(machinePos),
                "player-placed machine_stirling core survives external west remote-port cable placement");
        assertStirlingProvidesPowerFromHeatSource(level, batteryPos, portCablePos, battery, stirling,
                "player-placed machine_stirling", 4, 1_000_000);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_stirling diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesWoodBurnerRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 22, anchor.getY() + 5, chunkStartZ + 340);
        forceLoadedChunks(level, supportPos.offset(-8, -3, -8), supportPos.offset(8, 7, 8));
        clearBox(level, supportPos.above().offset(-8, -3, -8), supportPos.above(7).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack woodBurnerStack = new ItemStack(ModBlocks.MACHINE_WOOD_BURNER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, woodBurnerStack);

        var placeResult = woodBurnerStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        WoodBurnerBlockEntity woodBurner = findWoodBurnerAroundOrNull(level, supportPos.above(), 6);
        if (woodBurner == null) {
            throw new AssertionError("player useOn machine_wood_burner placement returned " + placeResult
                    + " without placing a machine_wood_burner core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_wood_burner block item action after placing the core");
        BlockPos machinePos = woodBurner.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_wood_burner faces south for the selected legacy north back port");
        assertWoodBurnerHasResolvingPowerFluidProxies(level, woodBurner);

        BlockPos portCablePos = machinePos.north(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(woodBurner, level.getBlockEntity(machinePos),
                "player-placed machine_wood_burner core survives external north remote-port cable placement");
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, woodBurner,
                "player-placed machine_wood_burner", 2, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_wood_burner diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 0 0 -2 north", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 2,
                "Energy ports at " + machinePos.toShortString(), "total=2", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesTurbofanRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 6, chunkStartZ + 356);
        forceLoadedChunks(level, supportPos.offset(-10, -4, -10), supportPos.offset(10, 7, 10));
        clearBox(level, supportPos.above().offset(-10, -4, -10), supportPos.above(7).offset(10, 0, 10));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 7.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack turbofanStack = new ItemStack(ModBlocks.MACHINE_TURBOFAN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, turbofanStack);

        var placeResult = turbofanStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        TurbofanBlockEntity turbofan = findBlockEntityAroundOrNull(level, supportPos.above(), 8,
                TurbofanBlockEntity.class, ModBlocks.MACHINE_TURBOFAN.get());
        if (turbofan == null) {
            throw new AssertionError("player useOn machine_turbofan placement returned " + placeResult
                    + " without placing a machine_turbofan core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_turbofan block item action after placing the core");
        BlockPos machinePos = turbofan.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_turbofan faces south for the selected legacy north back port");
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, turbofan, "player-placed machine_turbofan",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, -1, -1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, -3, 0, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, -3, -1, 0)));

        BlockPos portCablePos = machinePos.north(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(turbofan, level.getBlockEntity(machinePos),
                "player-placed machine_turbofan core survives external north remote-port cable placement");
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, turbofan,
                "player-placed machine_turbofan", 4, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_turbofan diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 0 0 -2 north", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesGasTurbineRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 26, anchor.getY() + 6, chunkStartZ + 372);
        forceLoadedChunks(level, supportPos.offset(-12, -4, -12), supportPos.offset(12, 8, 12));
        clearBox(level, supportPos.above().offset(-12, -4, -12), supportPos.above(8).offset(12, 0, 12));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack turbineStack = new ItemStack(ModBlocks.MACHINE_TURBINEGAS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, turbineStack);

        var placeResult = turbineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        TurbineGasBlockEntity turbine = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                TurbineGasBlockEntity.class, ModBlocks.MACHINE_TURBINEGAS.get());
        if (turbine == null) {
            throw new AssertionError("player useOn machine_turbinegas placement returned " + placeResult
                    + " without placing a machine_turbinegas core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_turbinegas block item action after placing the core");
        BlockPos machinePos = turbine.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_turbinegas faces south for the selected legacy west power port");
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, turbine, "player-placed machine_turbinegas",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 1, 1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, -1, -4, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 1, -4, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, 4, 1),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, -5, 1)));

        BlockPos portCablePos = machinePos.west(5).above();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(turbine, level.getBlockEntity(machinePos),
                "player-placed machine_turbinegas core survives external west remote-port cable placement");
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, turbine,
                "player-placed machine_turbinegas", 1, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_turbinegas diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -5 1 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=1", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesLargeTurbineRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 26, anchor.getY() + 6, chunkStartZ + 388);
        forceLoadedChunks(level, supportPos.offset(-12, -4, -12), supportPos.offset(12, 7, 12));
        clearBox(level, supportPos.above().offset(-12, -4, -12), supportPos.above(7).offset(12, 0, 12));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack turbineStack = new ItemStack(ModBlocks.MACHINE_LARGE_TURBINE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, turbineStack);

        var placeResult = turbineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        LegacyLargeTurbineBlockEntity turbine = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                LegacyLargeTurbineBlockEntity.class, ModBlocks.MACHINE_LARGE_TURBINE.get());
        if (turbine == null) {
            throw new AssertionError("player useOn machine_large_turbine placement returned " + placeResult
                    + " without placing a machine_large_turbine core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_large_turbine block item action after placing the core");
        BlockPos machinePos = turbine.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_large_turbine faces south for the selected legacy north power port");
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, turbine, "player-placed machine_large_turbine",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, -3, 0, 0),
                        LegacyMultiblockOffsets.relative(facing, 1, 0, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 0)));

        BlockPos portCablePos = machinePos.north(4);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(turbine, level.getBlockEntity(machinePos),
                "player-placed machine_large_turbine core survives external north remote-port cable placement");
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, turbine,
                "player-placed machine_large_turbine", 1, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_large_turbine diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 0 0 -4 north", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=1", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesSteamEngineRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 26, anchor.getY() + 6, chunkStartZ + 404);
        forceLoadedChunks(level, supportPos.offset(-12, -4, -12), supportPos.offset(12, 8, 12));
        clearBox(level, supportPos.above().offset(-12, -4, -12), supportPos.above(8).offset(12, 0, 12));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack steamEngineStack = new ItemStack(ModBlocks.MACHINE_STEAM_ENGINE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, steamEngineStack);

        var placeResult = steamEngineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        SteamEngineBlockEntity steamEngine = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                SteamEngineBlockEntity.class, ModBlocks.MACHINE_STEAM_ENGINE.get());
        if (steamEngine == null) {
            throw new AssertionError("player useOn machine_steam_engine placement returned " + placeResult
                    + " without placing a machine_steam_engine core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_steam_engine block item action after placing the core");
        BlockPos machinePos = steamEngine.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_steam_engine faces south for the selected legacy west center port");
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, steamEngine, "player-placed machine_steam_engine",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 1),
                        LegacyMultiblockOffsets.relative(facing, rot, 1, 1, 1),
                        LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 1)));

        BlockPos portCablePos = machinePos.west(2).above();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        steamEngine.getSteamTank().setTankType(HbmFluids.STEAM);
        steamEngine.getSteamTank().setFill(1_000);
        assertSame(steamEngine, level.getBlockEntity(machinePos),
                "player-placed machine_steam_engine core survives external west remote-port cable placement");
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed machine_steam_engine transfer");
        refreshCableLine(level, firstCablePos, portCablePos);
        HbmEnergyNodespace.tick(level);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver remains subscribed after player-placed machine_steam_engine "
                        + "remote-node refresh");
        SteamEngineBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), steamEngine);

        HbmEnergyUtil.PortSetSnapshot ports = steamEngine.inspectEnergyPorts();
        HbmEnergyUtil.PortSnapshot centerPort = HbmEnergyUtil.inspectPort(level, machinePos,
                HbmEnergyUtil.EnergyPort.of(-2, 1, 0, Direction.WEST));
        assertTrue(centerPort.connectable(),
                "player-placed machine_steam_engine expected SOUTH center conductor is connectable: "
                        + centerPort + ", block=" + level.getBlockState(portCablePos).getBlock());
        assertEquals(3, ports.totalPorts(),
                "player-placed machine_steam_engine exposes three legacy getConPos energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed machine_steam_engine remote provider port sees the red_cable network: " + ports);
        assertTrue(steamEngine.getLastPowerProduced() > 0L,
                "player-placed machine_steam_engine produced HE from steam");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_steam_engine diagnostic port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(transferred > 0L,
                "player-placed machine_steam_engine power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed machine_steam_engine");
        assertTrue(steamEngine.getPower() < steamEngine.getLastPowerProduced(),
                "player-placed machine_steam_engine spent HE into the legacy remote-port network");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 1 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=3", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesSteamTurbineAdjacentProviderPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() + 4, chunkStartZ + 1240);
        forceLoadedChunks(level, supportPos.offset(-8, -4, -8), supportPos.offset(8, 6, 8));
        clearBox(level, supportPos.above().offset(-8, -4, -8), supportPos.above(6).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack turbineStack = new ItemStack(ModBlocks.MACHINE_TURBINE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, turbineStack);

        var placeResult = turbineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        SteamTurbineBlockEntity turbine = findBlockEntityAroundOrNull(level, supportPos.above(), 3,
                SteamTurbineBlockEntity.class, ModBlocks.MACHINE_TURBINE.get());
        if (turbine == null) {
            throw new AssertionError("player useOn machine_turbine placement returned " + placeResult
                    + " without placing a machine_turbine around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_turbine block item action after placing the block");
        BlockPos machinePos = turbine.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_turbine faces south for the selected adjacent provider side");

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(turbine, level.getBlockEntity(machinePos),
                "player-placed machine_turbine survives adjacent west provider-side cable placement");
        turbine.getInputTank().setTankType(HbmFluids.STEAM);
        turbine.getInputTank().setFill(1_000);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed machine_turbine transfer");
        SteamTurbineBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), turbine);
        HbmEnergyNodespace.tick(level);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_turbine adjacent provider side has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(beforeUpdate.lastTransfer() > 0L || transferred > 0L,
                "player-placed machine_turbine power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed machine_turbine");
        assertTrue(turbine.getLastPowerProduced() > 0L,
                "player-placed machine_turbine produced HE from steam");
        assertTrue(turbine.getPower() < turbine.getLastPowerProduced(),
                "player-placed machine_turbine spent generated HE into the adjacent network");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=", "providers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesBatteryReddRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 30, anchor.getY() + 10, chunkStartZ + 424);
        forceLoadedChunks(level, supportPos.offset(-20, -8, -20), supportPos.offset(20, 14, 20));
        clearBox(level, supportPos.above().offset(-20, -8, -20), supportPos.above(14).offset(20, 0, 20));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 12.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack reddStack = new ItemStack(ModBlocks.MACHINE_BATTERY_REDD.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, reddStack);

        UseOnContext reddUseContext = new UseOnContext(player, InteractionHand.MAIN_HAND, blockHit(supportPos));
        var placeResult = reddStack.useOn(reddUseContext);

        BatteryReddBlockEntity redd = findBlockEntityAroundOrNull(level, supportPos.above(), 20,
                BatteryReddBlockEntity.class, ModBlocks.MACHINE_BATTERY_REDD.get());
        if (redd == null) {
            throw new AssertionError("player useOn machine_battery_redd placement returned " + placeResult
                    + " without placing a machine_battery_redd core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_battery_redd block item action after placing the core");
        BlockPos machinePos = redd.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                        == Direction.SOUTH,
                "player-placed machine_battery_redd faces south for the selected legacy west side port");
        Direction facing = level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, redd, "player-placed machine_battery_redd",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, rot, 2, 2, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 2, -2, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, -2, 2, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, -2, -2, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, 4, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, -4, 0)));

        while (redd.getRedLow() != BatteryReddBlockEntity.MODE_OUTPUT) {
            redd.receiveControl(null, BatteryReddBlockEntity.controlTag(BatteryReddBlockEntity.CONTROL_RED_LOW));
        }
        long startingPower = 100_000L;
        BlockPos portCablePos = machinePos.west(5);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        redd.setPower(startingPower);
        assertSame(redd, level.getBlockEntity(machinePos),
                "player-placed machine_battery_redd core survives external west remote-port cable placement");
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed machine_battery_redd transfer");
        BatteryReddBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), redd);
        HbmEnergyNodespace.tick(level);
        refreshCableLine(level, firstCablePos, portCablePos);
        HbmEnergyNodespace.tick(level);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver remains subscribed after player-placed machine_battery_redd "
                        + "remote-node refresh");
        BatteryReddBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), redd);

        HbmEnergyUtil.PortSetSnapshot ports = redd.inspectEnergyPorts();
        assertEquals(6, ports.totalPorts(),
                "player-placed machine_battery_redd exposes six legacy getConPos energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed machine_battery_redd remote provider port sees the red_cable network: " + ports);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_battery_redd diagnostic port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(transferred > 0L,
                "player-placed machine_battery_redd power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed machine_battery_redd");
        assertTrue(redd.getPower() < startingPower,
                "player-placed machine_battery_redd spent HE into the legacy remote-port network");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -5 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesIndustrialTurbineRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 26, anchor.getY() + 6, chunkStartZ + 448);
        forceLoadedChunks(level, supportPos.offset(-16, -4, -16), supportPos.offset(16, 8, 16));
        clearBox(level, supportPos.above().offset(-16, -4, -16), supportPos.above(8).offset(16, 0, 16));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack turbineStack = new ItemStack(ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, turbineStack);

        var placeResult = turbineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        IndustrialSteamTurbineBlockEntity turbine = findBlockEntityAroundOrNull(level, supportPos.above(), 14,
                IndustrialSteamTurbineBlockEntity.class, ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get());
        if (turbine == null) {
            throw new AssertionError("player useOn machine_industrial_turbine placement returned " + placeResult
                    + " without placing a machine_industrial_turbine core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_industrial_turbine block item action after placing the core");
        BlockPos machinePos = turbine.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_industrial_turbine faces south for the selected legacy north power port");
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, turbine,
                "player-placed machine_industrial_turbine",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, rot, 3, 1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 3, -1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, -1, -1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 3, 0, 2),
                        LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 2),
                        LegacyMultiblockOffsets.relative(facing, rot, -3, 0, 1)));

        BlockPos portCablePos = machinePos.north(4).above();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(turbine, level.getBlockEntity(machinePos),
                "player-placed machine_industrial_turbine core survives external north remote-port cable placement");
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, turbine,
                "player-placed machine_industrial_turbine", 1, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_industrial_turbine diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 0 1 -4 north", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=1", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesChungusRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 30, anchor.getY() + 8, chunkStartZ + 480);
        forceLoadedChunks(level, supportPos.offset(-22, -8, -22), supportPos.offset(22, 10, 22));
        clearBox(level, supportPos.above().offset(-22, -8, -22), supportPos.above(10).offset(22, 0, 22));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 12.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack chungusStack = new ItemStack(ModBlocks.MACHINE_CHUNGUS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, chungusStack);

        var placeResult = chungusStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        ChungusBlockEntity chungus = findBlockEntityAroundOrNull(level, supportPos.above(), 18,
                ChungusBlockEntity.class, ModBlocks.MACHINE_CHUNGUS.get());
        if (chungus == null) {
            throw new AssertionError("player useOn machine_chungus placement returned " + placeResult
                    + " without placing a machine_chungus core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_chungus block item action after placing the core");
        BlockPos machinePos = chungus.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_chungus faces south for the selected legacy north power port");
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, chungus, "player-placed machine_chungus",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, -2, 0, 2),
                        LegacyMultiblockOffsets.relative(facing, -10, 0),
                        LegacyMultiblockOffsets.relative(facing, side, 0, 2, 0),
                        LegacyMultiblockOffsets.relative(facing, side, 0, -2, 0)));

        BlockPos portCablePos = machinePos.north(11);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(chungus, level.getBlockEntity(machinePos),
                "player-placed machine_chungus core survives external north remote-port cable placement");
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, chungus,
                "player-placed machine_chungus", 1, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_chungus diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 0 0 -11 north", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=1", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesRtgAdjacentProviderPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() + 4, chunkStartZ + 492);
        forceLoadedChunks(level, supportPos.offset(-8, -4, -8), supportPos.offset(8, 6, 8));
        clearBox(level, supportPos.above().offset(-8, -4, -8), supportPos.above(6).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack rtgStack = new ItemStack(ModBlocks.MACHINE_RTG_GREY.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, rtgStack);

        var placeResult = rtgStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        RtgBlockEntity rtg = findBlockEntityAroundOrNull(level, supportPos.above(), 3,
                RtgBlockEntity.class, ModBlocks.MACHINE_RTG_GREY.get());
        if (rtg == null) {
            throw new AssertionError("player useOn machine_rtg_grey placement returned " + placeResult
                    + " without placing a machine_rtg_grey core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_rtg_grey block item action after placing the core");
        BlockPos machinePos = rtg.getBlockPos();

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(rtg, level.getBlockEntity(machinePos),
                "player-placed machine_rtg_grey survives adjacent west provider-side cable placement");

        var rtgPellet = ModItems.legacyItem("pellet_rtg_lead");
        assertTrue(rtgPellet != null, "pellet_rtg_lead is registered for machine_rtg_grey RTG heat");
        rtg.getItems().setStackInSlot(0, new ItemStack(rtgPellet.get()));
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed machine_rtg_grey transfer");
        RtgBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), rtg);
        RtgBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), rtg);
        HbmEnergyNodespace.tick(level);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_rtg_grey adjacent provider side has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(beforeUpdate.lastTransfer() > 0L || transferred > 0L,
                "player-placed machine_rtg_grey power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed machine_rtg_grey");
        assertTrue(rtg.getHeat() > 0,
                "player-placed machine_rtg_grey calculated RTG heat before provider transfer");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=", "providers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesMiniRtgAdjacentProviderPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesMiniRtgAdjacentProviderPortsAndDiagnostics(helper,
                ModBlocks.MACHINE_MINIRTG.get(), "machine_minirtg", 700L, 496);
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesPowerRtgAdjacentProviderPortsAndDiagnostics(GameTestHelper helper) {
        assertPlayerUseOnPlacesMiniRtgAdjacentProviderPortsAndDiagnostics(helper,
                ModBlocks.MACHINE_POWERRTG.get(), "machine_powerrtg", 2_500L, 504);
    }

    private static void assertPlayerUseOnPlacesMiniRtgAdjacentProviderPortsAndDiagnostics(GameTestHelper helper,
            Block block, String machineName, long expectedOutput, int zOffset) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() + 4, chunkStartZ + zOffset);
        forceLoadedChunks(level, supportPos.offset(-8, -4, -8), supportPos.offset(8, 6, 8));
        clearBox(level, supportPos.above().offset(-8, -4, -8), supportPos.above(6).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack machineStack = new ItemStack(block);
        player.setItemInHand(InteractionHand.MAIN_HAND, machineStack);

        var placeResult = machineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        MiniRtgBlockEntity rtg = findBlockEntityAroundOrNull(level, supportPos.above(), 3,
                MiniRtgBlockEntity.class, block);
        if (rtg == null) {
            throw new AssertionError("player useOn " + machineName + " placement returned " + placeResult
                    + " without placing a " + machineName + " block entity around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the " + machineName + " block item action after placing the block");
        BlockPos machinePos = rtg.getBlockPos();

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(rtg, level.getBlockEntity(machinePos),
                "player-placed " + machineName + " survives adjacent west provider-side cable placement");

        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed " + machineName + " transfer");
        MiniRtgBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), rtg);
        HbmEnergyNodespace.tick(level);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed " + machineName + " adjacent provider side has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(beforeUpdate.lastTransfer() > 0L || transferred > 0L,
                "player-placed " + machineName + " power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed " + machineName);
        assertEquals(expectedOutput, rtg.getOutput(),
                "player-placed " + machineName + " keeps legacy HE/t output");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=", "providers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesDieselGeneratorAdjacentProviderPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 4, chunkStartZ + 512);
        forceLoadedChunks(level, supportPos.offset(-8, -4, -8), supportPos.offset(8, 6, 8));
        clearBox(level, supportPos.above().offset(-8, -4, -8), supportPos.above(6).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack dieselStack = new ItemStack(ModBlocks.MACHINE_DIESEL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, dieselStack);

        var placeResult = dieselStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        DieselGeneratorBlockEntity diesel = findBlockEntityAroundOrNull(level, supportPos.above(), 4,
                DieselGeneratorBlockEntity.class, ModBlocks.MACHINE_DIESEL.get());
        if (diesel == null) {
            throw new AssertionError("player useOn machine_diesel placement returned " + placeResult
                    + " without placing a machine_diesel core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_diesel block item action after placing the core");
        BlockPos machinePos = diesel.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_diesel faces south while still exposing six adjacent legacy provider ports");

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(diesel, level.getBlockEntity(machinePos),
                "player-placed machine_diesel core survives adjacent west provider-port cable placement");

        diesel.getTank().setTankType(HbmFluids.DIESEL);
        diesel.getTank().setFill(1_000);
        diesel.setPower(0L);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed machine_diesel transfer");
        DieselGeneratorBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), diesel);
        HbmEnergyNodespace.tick(level);

        HbmEnergyUtil.PortSetSnapshot ports = diesel.inspectEnergyPorts();
        assertEquals(6, ports.totalPorts(),
                "player-placed machine_diesel exposes six adjacent legacy provider energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed machine_diesel adjacent provider port sees the red_cable network: " + ports);
        assertTrue(diesel.getLastPowerProduced() > 0L,
                "player-placed machine_diesel consumed diesel fuel and produced HE before network transfer");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_diesel diagnostic port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(beforeUpdate.lastTransfer() > 0L || transferred > 0L,
                "player-placed machine_diesel power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed machine_diesel");
        assertTrue(diesel.getPower() < diesel.getLastPowerProduced(),
                "player-placed machine_diesel spent generated HE into the legacy adjacent-port network");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesCombustionEngineRemoteProviderPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 26, anchor.getY() + 5, chunkStartZ + 528);
        forceLoadedChunks(level, supportPos.offset(-12, -4, -12), supportPos.offset(12, 8, 12));
        clearBox(level, supportPos.above().offset(-12, -4, -12), supportPos.above(8).offset(12, 0, 12));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack engineStack = new ItemStack(ModBlocks.MACHINE_COMBUSTION_ENGINE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, engineStack);

        var placeResult = engineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        CombustionEngineBlockEntity engine = findBlockEntityAroundOrNull(level, supportPos.above(), 8,
                CombustionEngineBlockEntity.class, ModBlocks.MACHINE_COMBUSTION_ENGINE.get());
        if (engine == null) {
            throw new AssertionError("player useOn machine_combustion_engine placement returned " + placeResult
                    + " without placing a machine_combustion_engine core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_combustion_engine block item action after placing the core");
        BlockPos machinePos = engine.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_combustion_engine faces south for the selected legacy front-left port");
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, engine,
                "player-placed machine_combustion_engine",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, side, 0, 1, 0),
                        LegacyMultiblockOffsets.relative(facing, side, 0, -1, 0),
                        LegacyMultiblockOffsets.relative(facing, side, -1, 1, 0),
                        LegacyMultiblockOffsets.relative(facing, side, -1, -1, 0)));

        BlockPos portCablePos = machinePos.south().west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(engine, level.getBlockEntity(machinePos),
                "player-placed machine_combustion_engine core survives external front-left provider-port cable placement");

        engine.getTank().setTankType(HbmFluids.DIESEL);
        engine.getTank().setFill(1_000);
        engine.getItems().setStackInSlot(CombustionEngineBlockEntity.SLOT_PISTON,
                new ItemStack(ModItems.PISTON_SET_STEEL.get()));
        engine.setThrottle(10);
        if (!engine.isOn()) {
            engine.toggleOn();
        }
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed machine_combustion_engine transfer");
        CombustionEngineBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), engine);
        HbmEnergyNodespace.tick(level);

        HbmEnergyUtil.PortSetSnapshot ports = engine.inspectEnergyPorts();
        assertEquals(4, ports.totalPorts(),
                "player-placed machine_combustion_engine exposes four legacy getConPos energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed machine_combustion_engine provider port sees the red_cable network: " + ports);
        assertTrue(engine.getLastPowerProduced() > 0L,
                "player-placed machine_combustion_engine burned diesel with a piston set and produced HE");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_combustion_engine diagnostic port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(beforeUpdate.lastTransfer() > 0L || transferred > 0L,
                "player-placed machine_combustion_engine power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed machine_combustion_engine");
        assertTrue(engine.getPower() < engine.getLastPowerProduced(),
                "player-placed machine_combustion_engine spent generated HE into the legacy remote-port network");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 1 south", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesGasFlareRemoteProviderPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 4, chunkStartZ + 824);
        forceLoadedChunks(level, supportPos.offset(-10, -4, -10), supportPos.offset(10, 14, 10));
        clearBox(level, supportPos.above().offset(-10, -4, -10), supportPos.above(14).offset(10, 0, 10));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 7.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack flareStack = new ItemStack(ModBlocks.MACHINE_GASFLARE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, flareStack);

        var placeResult = flareStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        GasFlareBlockEntity flare = findBlockEntityAroundOrNull(level, supportPos.above(), 12,
                GasFlareBlockEntity.class, ModBlocks.MACHINE_GASFLARE.get());
        if (flare == null) {
            throw new AssertionError("player useOn machine_flare placement returned " + placeResult
                    + " without placing a machine_flare core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_flare block item action after placing the core");
        BlockPos machinePos = flare.getBlockPos();
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, flare, "player-placed machine_flare",
                List.of(new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
                        new BlockPos(0, 0, 1), new BlockPos(0, 0, -1)));

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(flare, level.getBlockEntity(machinePos),
                "player-placed machine_flare core survives external west remote-port cable placement");
        assertRemoteProviderProvidesPower(level, batteryPos, portCablePos, battery, flare,
                "player-placed machine_flare", 4, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_flare diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesDfcReceiverAdjacentProviderPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() + 4, chunkStartZ + 1216);
        forceLoadedChunks(level, supportPos.offset(-8, -4, -8), supportPos.offset(8, 6, 8));
        clearBox(level, supportPos.above().offset(-8, -4, -8), supportPos.above(6).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack receiverStack = new ItemStack(ModBlocks.DFC_RECEIVER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, receiverStack);

        var placeResult = receiverStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        DfcReceiverBlockEntity receiver = findBlockEntityAroundOrNull(level, supportPos.above(), 3,
                DfcReceiverBlockEntity.class, ModBlocks.DFC_RECEIVER.get());
        if (receiver == null) {
            throw new AssertionError("player useOn dfc_receiver placement returned " + placeResult
                    + " without placing a dfc_receiver around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the dfc_receiver block item action after placing the core");
        BlockPos machinePos = receiver.getBlockPos();

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(receiver, level.getBlockEntity(machinePos),
                "player-placed dfc_receiver survives adjacent west provider-side cable placement");

        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed dfc_receiver transfer");
        receiver.getCryogelTank().setFill(20);
        Direction receiverFacing = dfcFacing(receiver.getBlockState());
        receiver.addEnergy(level, machinePos, 100L, receiverFacing.getOpposite());
        DfcReceiverBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), receiver);
        HbmEnergyNodespace.tick(level);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed dfc_receiver adjacent provider side has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(beforeUpdate.lastTransfer() > 0L || transferred > 0L,
                "player-placed dfc_receiver power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed dfc_receiver");
        assertTrue(receiver.getPower() < 500_000L,
                "player-placed dfc_receiver spent laser-derived HE into the adjacent network");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=", "providers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesRadiolysisRemoteProviderPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 4, chunkStartZ + 552);
        forceLoadedChunks(level, supportPos.offset(-8, -4, -8), supportPos.offset(8, 7, 8));
        clearBox(level, supportPos.above().offset(-8, -4, -8), supportPos.above(7).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack radiolysisStack = new ItemStack(ModBlocks.MACHINE_RADIOLYSIS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, radiolysisStack);

        var placeResult = radiolysisStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        RadiolysisBlockEntity radiolysis = findBlockEntityAroundOrNull(level, supportPos.above(), 8,
                RadiolysisBlockEntity.class, ModBlocks.MACHINE_RADIOLYSIS.get());
        if (radiolysis == null) {
            throw new AssertionError("player useOn machine_radiolysis placement returned " + placeResult
                    + " without placing a machine_radiolysis core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_radiolysis block item action after placing the core");
        BlockPos machinePos = radiolysis.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                        == Direction.SOUTH,
                "player-placed machine_radiolysis faces south while exposing fixed legacy provider ports");

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(radiolysis, level.getBlockEntity(machinePos),
                "player-placed machine_radiolysis core survives external west provider-port cable placement");

        var rtgPellet = ModItems.legacyItem("pellet_rtg_lead");
        assertTrue(rtgPellet != null, "pellet_rtg_lead is registered for machine_radiolysis RTG heat");
        radiolysis.getItems().setStackInSlot(RadiolysisBlockEntity.SLOT_RTG_START,
                new ItemStack(rtgPellet.get()));
        radiolysis.setPower(0L);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed machine_radiolysis transfer");
        RadiolysisBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), radiolysis);
        HbmEnergyNodespace.tick(level);

        HbmEnergyUtil.PortSetSnapshot ports = radiolysis.inspectEnergyPorts();
        assertEquals(4, ports.totalPorts(),
                "player-placed machine_radiolysis exposes four legacy getConPos energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed machine_radiolysis provider port sees the red_cable network: " + ports);
        assertTrue(radiolysis.getHeat() > 0,
                "player-placed machine_radiolysis calculated RTG heat before provider transfer");

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_radiolysis diagnostic port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(beforeUpdate.lastTransfer() > 0L || transferred > 0L,
                "player-placed machine_radiolysis power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed machine_radiolysis");
        assertTrue(radiolysis.getPower() < radiolysis.getHeat() * 10L,
                "player-placed machine_radiolysis spent generated RTG HE into the legacy remote-port network");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesRadGenRemoteProviderPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 28, anchor.getY() + 5, chunkStartZ + 576);
        forceLoadedChunks(level, supportPos.offset(-12, -5, -12), supportPos.offset(12, 8, 12));
        clearBox(level, supportPos.above().offset(-12, -5, -12), supportPos.above(8).offset(12, 0, 12));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack radGenStack = new ItemStack(ModBlocks.MACHINE_RADGEN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, radGenStack);

        var placeResult = radGenStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        RadGenBlockEntity radGen = findBlockEntityAroundOrNull(level, supportPos.above(), 10,
                RadGenBlockEntity.class, ModBlocks.MACHINE_RADGEN.get());
        if (radGen == null) {
            throw new AssertionError("player useOn machine_radgen placement returned " + placeResult
                    + " without placing a machine_radgen core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_radgen block item action after placing the core");
        BlockPos machinePos = radGen.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed machine_radgen faces south for the selected legacy back provider port");
        Direction facing = level.getBlockState(machinePos).getValue(HorizontalMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, radGen, "player-placed machine_radgen",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, rot, -3, 0, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 0)));

        BlockPos portCablePos = machinePos.north(4);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(radGen, level.getBlockEntity(machinePos),
                "player-placed machine_radgen core survives external north provider-port cable placement");

        long startingPower = 100_000L;
        radGen.setPower(startingPower);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed machine_radgen transfer");
        RadGenBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), radGen);
        HbmEnergyNodespace.tick(level);

        HbmEnergyUtil.PortSetSnapshot ports = radGen.inspectEnergyPorts();
        assertEquals(1, ports.totalPorts(),
                "player-placed machine_radgen exposes the legacy back getConPos provider port");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed machine_radgen provider port sees the red_cable network: " + ports);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_radgen diagnostic port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(beforeUpdate.lastTransfer() > 0L || transferred > 0L,
                "player-placed machine_radgen power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed machine_radgen");
        assertTrue(radGen.getPower() < startingPower,
                "player-placed machine_radgen spent HE into the legacy remote-port network");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 0 0 -4 north", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=1", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesOilWellAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 22, anchor.getY() - 1, chunkStartZ + 640);
        forceLoadedChunks(level, supportPos.offset(-8, -1, -8), supportPos.offset(8, 14, 8));
        clearBox(level, supportPos.above().offset(-8, -1, -8), supportPos.above(14).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 6.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack wellStack = new ItemStack(ModBlocks.MACHINE_WELL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wellStack);

        var placeResult = wellStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        OilDrillBlockEntity well = findBlockEntityAroundOrNull(level, supportPos.above(), 12,
                OilDrillBlockEntity.class, ModBlocks.MACHINE_WELL.get());
        if (well == null) {
            throw new AssertionError("player useOn machine_well placement returned " + placeResult
                    + " without placing a machine_well core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_well block item action after placing the core");
        BlockPos machinePos = well.getBlockPos();
        assertTrue(well.getKind() == OilDrillBlockEntity.Kind.WELL,
                "player-placed machine_well keeps the oil derrick kind");

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(well, level.getBlockEntity(machinePos),
                "player-placed machine_well core survives adjacent west receiver-port cable placement");
        OilDrillBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), well);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                well, "player-placed machine_well", 4, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_well diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesPumpjackRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 28, anchor.getY() - 1, chunkStartZ + 656);
        forceLoadedChunks(level, supportPos.offset(-14, -1, -14), supportPos.offset(14, 8, 14));
        clearBox(level, supportPos.above().offset(-14, -1, -14), supportPos.above(8).offset(14, 0, 14));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack pumpjackStack = new ItemStack(ModBlocks.MACHINE_PUMPJACK.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, pumpjackStack);

        var placeResult = pumpjackStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        OilDrillBlockEntity pumpjack = findBlockEntityAroundOrNull(level, supportPos.above(), 16,
                OilDrillBlockEntity.class, ModBlocks.MACHINE_PUMPJACK.get());
        if (pumpjack == null) {
            throw new AssertionError("player useOn machine_pumpjack placement returned " + placeResult
                    + " without placing a machine_pumpjack core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_pumpjack block item action after placing the core");
        BlockPos machinePos = pumpjack.getBlockPos();
        assertTrue(pumpjack.getKind() == OilDrillBlockEntity.Kind.PUMPJACK,
                "player-placed machine_pumpjack keeps the pumpjack kind");
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed machine_pumpjack faces south for the selected legacy remote port");

        BlockPos portCablePos = machinePos.west(4).south(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(pumpjack, level.getBlockEntity(machinePos),
                "player-placed machine_pumpjack core survives external west/south receiver-port cable placement");
        OilDrillBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), pumpjack);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                pumpjack, "player-placed machine_pumpjack", 4, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_pumpjack diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -4 0 2 north", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesFrackingTowerAdjacentReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 24, anchor.getY() + 9, chunkStartZ + 680);
        forceLoadedChunks(level, supportPos.offset(-18, -12, -18), supportPos.offset(18, 32, 18));
        clearBox(level, supportPos.above().offset(-18, -12, -18), supportPos.above(32).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack towerStack = new ItemStack(ModBlocks.MACHINE_FRACKING_TOWER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, towerStack);

        var placeResult = towerStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        OilDrillBlockEntity tower = findBlockEntityAroundOrNull(level, supportPos.above(), 20,
                OilDrillBlockEntity.class, ModBlocks.MACHINE_FRACKING_TOWER.get());
        if (tower == null) {
            throw new AssertionError("player useOn machine_fracking_tower placement returned " + placeResult
                    + " without placing a machine_fracking_tower core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the machine_fracking_tower block item action after placing the core");
        BlockPos machinePos = tower.getBlockPos();
        assertTrue(tower.getKind() == OilDrillBlockEntity.Kind.FRACKING_TOWER,
                "player-placed machine_fracking_tower keeps the fracking tower kind");

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(tower, level.getBlockEntity(machinePos),
                "player-placed machine_fracking_tower core survives adjacent west receiver-port cable placement");
        OilDrillBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), tower);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                tower, "player-placed machine_fracking_tower", 4, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed machine_fracking_tower diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -1 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesElectricPumpRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + 800);
        forceLoadedChunks(level, supportPos.offset(-8, -1, -8), supportPos.offset(8, 8, 8));
        clearBox(level, supportPos.above().offset(-8, -1, -8), supportPos.above(8).offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack pumpStack = new ItemStack(ModBlocks.PUMP_ELECTRIC.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, pumpStack);

        var placeResult = pumpStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        WaterPumpBlockEntity pump = findBlockEntityAroundOrNull(level, supportPos.above(), 5,
                WaterPumpBlockEntity.class, ModBlocks.PUMP_ELECTRIC.get());
        if (pump == null) {
            throw new AssertionError("player useOn pump_electric placement returned " + placeResult
                    + " without placing a pump_electric core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the pump_electric block item action after placing the core");
        BlockPos machinePos = pump.getBlockPos();
        assertTrue(pump.isElectric(), "player-placed pump_electric keeps the electric water pump runtime");
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                == Direction.SOUTH,
                "player-placed pump_electric faces south while exposing legacy remote power proxies");

        BlockPos portCablePos = machinePos.west(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(pump, level.getBlockEntity(machinePos),
                "player-placed pump_electric core survives external west receiver-port cable placement");
        List<HbmEnergyUtil.EnergyPort> energyPorts = List.of(
                HbmEnergyUtil.EnergyPort.of(2, 0, 0, Direction.EAST),
                HbmEnergyUtil.EnergyPort.of(-2, 0, 0, Direction.WEST),
                HbmEnergyUtil.EnergyPort.of(0, 0, 2, Direction.SOUTH),
                HbmEnergyUtil.EnergyPort.of(0, 0, -2, Direction.NORTH));
        assertRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos, energyPorts,
                pump, "player-placed pump_electric", 4, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed pump_electric diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=4", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2ReceiverPorts")
    public static void assembledIcfControllerRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos machinePos = new BlockPos(chunkStartX + 34, anchor.getY() + 8, chunkStartZ + 1000);
        forceLoadedChunks(level, machinePos.offset(-16, -8, -16), machinePos.offset(16, 12, 16));
        clearBox(level, machinePos.offset(-16, -8, -16), machinePos.offset(16, 12, 16));

        level.setBlock(machinePos, ModBlocks.ICF_CONTROLLER.get().defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(machinePos) instanceof ICFControllerBlockEntity controller)) {
            throw new AssertionError("No icf_controller block entity at " + machinePos);
        }

        BlockPos cellPos = machinePos.north();
        BlockPos emitterPos = cellPos.east();
        BlockPos capacitorPos = emitterPos.east();
        BlockPos turboPos = capacitorPos.east();
        BlockPos portBlockPos = machinePos.west(2);
        controller.setup(Set.of(cellPos, emitterPos, capacitorPos, turboPos, portBlockPos),
                Set.of(portBlockPos), Set.of(cellPos), Set.of(emitterPos), Set.of(capacitorPos), Set.of(turboPos));
        assertTrue(controller.isAssembled(), "icf_controller setup marks the laser as assembled");
        assertTrue(controller.getMaxPower() > 0L,
                "icf_controller has capacitor-backed legacy receiver demand after setup");

        BlockPos portCablePos = portBlockPos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(controller, level.getBlockEntity(machinePos),
                "icf_controller survives external assembled-port cable placement");
        ICFControllerBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), controller);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "icf_controller assembled diagnostic port has a valid power net");
        assertTrue(powerNet.createDebugSnapshot().receivers() >= 1,
                "icf_controller natural server tick subscribes receiver through assembled legacy port");
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                controller, "assembled icf_controller", 6, 100_000L);

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -3 0 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=6", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesFusionTorusRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 34, anchor.getY() + 8, chunkStartZ + 960);
        forceLoadedChunks(level, supportPos.offset(-24, -8, -24), supportPos.offset(24, 18, 24));
        clearBox(level, supportPos.above().offset(-24, -8, -24), supportPos.above(18).offset(24, 0, 24));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack machineStack = new ItemStack(ModBlocks.FUSION_TORUS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, machineStack);

        var placeResult = machineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        FusionTorusBlockEntity torus = findBlockEntityAroundOrNull(level, supportPos.above(), 22,
                FusionTorusBlockEntity.class, ModBlocks.FUSION_TORUS.get());
        if (torus == null) {
            throw new AssertionError("player useOn fusion_torus placement returned " + placeResult
                    + " without placing a fusion_torus core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the fusion_torus block item action after placing the core");
        BlockPos machinePos = torus.getBlockPos();
        assertTrue(level.getBlockState(machinePos).is(ModBlocks.FUSION_TORUS.get()),
                "player-placed fusion_torus core remains at " + machinePos);

        BlockPos portCablePos = machinePos.east(6).below();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(torus, level.getBlockEntity(machinePos),
                "player-placed fusion_torus core survives external side-bottom receiver-port cable placement");
        FusionTorusBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), torus);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                torus, "player-placed fusion_torus", 26, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed fusion_torus diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " 6 -1 0 down", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=26", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesFusionKlystronRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 34, anchor.getY() + 8, chunkStartZ + 840);
        forceLoadedChunks(level, supportPos.offset(-18, -8, -18), supportPos.offset(18, 14, 18));
        clearBox(level, supportPos.above().offset(-18, -8, -18), supportPos.above(14).offset(18, 0, 18));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 8.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 0.5D);
        player.setYRot(-90.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack machineStack = new ItemStack(ModBlocks.FUSION_KLYSTRON.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, machineStack);

        var placeResult = machineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        FusionKlystronBlockEntity klystron = findBlockEntityAroundOrNull(level, supportPos.above(), 14,
                FusionKlystronBlockEntity.class, ModBlocks.FUSION_KLYSTRON.get());
        if (klystron == null) {
            throw new AssertionError("player useOn fusion_klystron placement returned " + placeResult
                    + " without placing a fusion_klystron core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the fusion_klystron block item action after placing the core");
        BlockPos machinePos = klystron.getBlockPos();
        Direction facing = level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        assertTrue(facing == Direction.WEST,
                "player-placed fusion_klystron faces west for the selected legacy front receiver port");
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, klystron, "player-placed fusion_klystron",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, side, 3, 0, 2),
                        LegacyMultiblockOffsets.relative(facing, side, 0, 2, 0),
                        LegacyMultiblockOffsets.relative(facing, side, 0, -2, 0)));

        BlockPos portCablePos = machinePos.west(4).above(2);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(klystron, level.getBlockEntity(machinePos),
                "player-placed fusion_klystron core survives external front receiver-port cable placement");
        FusionKlystronBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), klystron);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                klystron, "player-placed fusion_klystron", 3, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed fusion_klystron diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -4 2 0 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=3", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedReceiverPorts")
    public static void playerUseOnPlacesFusionPlasmaForgeRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 34, anchor.getY() + 8, chunkStartZ + 880);
        forceLoadedChunks(level, supportPos.offset(-20, -8, -20), supportPos.offset(20, 16, 20));
        clearBox(level, supportPos.above().offset(-20, -8, -20), supportPos.above(16).offset(20, 0, 20));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack machineStack = new ItemStack(ModBlocks.FUSION_PLASMA_FORGE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, machineStack);

        var placeResult = machineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        FusionPlasmaForgeBlockEntity forge = findBlockEntityAroundOrNull(level, supportPos.above(), 18,
                FusionPlasmaForgeBlockEntity.class, ModBlocks.FUSION_PLASMA_FORGE.get());
        if (forge == null) {
            throw new AssertionError("player useOn fusion_plasma_forge placement returned " + placeResult
                    + " without placing a fusion_plasma_forge core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the fusion_plasma_forge block item action after placing the core");
        BlockPos machinePos = forge.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                        == Direction.SOUTH,
                "player-placed fusion_plasma_forge faces south for the selected legacy remote port");

        BlockPos portCablePos = machinePos.west(2).south(6);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(forge, level.getBlockEntity(machinePos),
                "player-placed fusion_plasma_forge core survives external receiver-port cable placement");
        FusionPlasmaForgeBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), forge);
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                forge, "player-placed fusion_plasma_forge", 10, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed fusion_plasma_forge diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -2 0 6 south", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=10", "networked=");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedProviderPorts")
    public static void playerUseOnPlacesFusionMhdtRemoteProviderPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 34, anchor.getY() + 8, chunkStartZ + 920);
        forceLoadedChunks(level, supportPos.offset(-22, -8, -22), supportPos.offset(22, 16, 22));
        clearBox(level, supportPos.above().offset(-22, -8, -22), supportPos.above(16).offset(22, 0, 22));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 8.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack machineStack = new ItemStack(ModBlocks.FUSION_MHDT.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, machineStack);

        var placeResult = machineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));
        FusionMHDTBlockEntity mhdt = findBlockEntityAroundOrNull(level, supportPos.above(), 20,
                FusionMHDTBlockEntity.class, ModBlocks.FUSION_MHDT.get());
        if (mhdt == null) {
            throw new AssertionError("player useOn fusion_mhdt placement returned " + placeResult
                    + " without placing a fusion_mhdt core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the fusion_mhdt block item action after placing the core");
        BlockPos machinePos = mhdt.getBlockPos();
        assertTrue(level.getBlockState(machinePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                        == Direction.SOUTH,
                "player-placed fusion_mhdt faces south for the selected legacy remote provider port");
        Direction facing = Direction.SOUTH;
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        assertPlayerPlacedProviderMachineHasResolvingProxies(level, mhdt, "player-placed fusion_mhdt",
                List.of(
                        LegacyMultiblockOffsets.relative(facing, side, 4, 3, 0),
                        LegacyMultiblockOffsets.relative(facing, side, 4, -3, 0),
                        LegacyMultiblockOffsets.relative(facing, side, 7, 0, 1)));

        BlockPos portCablePos = machinePos.west(4).south(4);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        MachineBatteryBlockEntity battery = prepareInputBatteryAndCableLine(level, batteryPos, firstCablePos,
                portCablePos);
        assertSame(mhdt, level.getBlockEntity(machinePos),
                "player-placed fusion_mhdt core survives external provider-port cable placement");
        mhdt.getColdTank().setFill(mhdt.getColdTank().getMaxFill());
        mhdt.receiveFusionPower(6_000_000L, 0.0D, 1.0F, 1.0F, 1.0F);
        FusionMHDTBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), mhdt);
        HbmEnergyNodespace.tick(level);

        HbmEnergyUtil.PortSetSnapshot ports = mhdt.inspectEnergyPorts();
        assertEquals(3, ports.totalPorts(), "player-placed fusion_mhdt exposes legacy remote provider ports");
        assertTrue(ports.networkedPorts() >= 1,
                "player-placed fusion_mhdt provider port sees the red_cable network: " + ports);
        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before player-placed fusion_mhdt transfer");
        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed fusion_mhdt diagnostic port has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long generatedPower = mhdt.getPower();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();
        assertTrue(transferred > 0L,
                "player-placed fusion_mhdt power net transferred HE; before=" + beforeUpdate
                        + ", after=" + afterUpdate + ", generatedPower=" + generatedPower);
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from player-placed fusion_mhdt");
        assertTrue(mhdt.getPower() < generatedPower,
                "player-placed fusion_mhdt spent generated HE into the legacy remote-port network");

        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, machinePos,
                "hbm energy port " + commandPos(machinePos) + " -4 0 4 west", expectedLinks,
                "Energy port from " + machinePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 1,
                "Energy ports at " + machinePos.toShortString(), "total=3", "networked=");

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
        BlockPos machinePos = portCablePos.east(2);
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
        BlockPos machinePos = portCablePos.east(5).below();
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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2PlayerPlacedRemotePorts")
    public static void playerUseOnPlacesLaunchTableRemotePortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 31, anchor.getY() - 1, chunkStartZ + 148);
        forceLoadedChunks(level, supportPos.offset(-8, 0, -8), supportPos.offset(8, 1, 8));
        clearSingleLayer(level, supportPos.above().offset(-8, 0, -8), supportPos.above().offset(8, 0, 8));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 7.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack tableStack = new ItemStack(ModBlocks.LAUNCH_TABLE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tableStack);

        var placeResult = tableStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        LaunchTableBlockEntity table = findLaunchTableAroundOrNull(level, supportPos.above(), 5);
        if (table == null) {
            throw new AssertionError("player useOn launch_table placement returned " + placeResult
                    + " without placing a launch_table core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the launch_table block item action after placing the core");
        BlockPos tablePos = table.getBlockPos();
        assertLaunchTableHasResolvingPortProxies(level, table);

        BlockPos portCablePos = tablePos.west(5);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, tablePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(table, level.getBlockEntity(tablePos),
                "player-placed launch_table core survives external west remote-port cable placement");
        assertEnergyAndFluidRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                table, "player-placed launch_table", 36, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed launch_table diagnostic port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, tablePos,
                "hbm energy port " + commandPos(tablePos) + " -5 0 0 west", expectedLinks,
                "Energy port from " + tablePos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, tablePos, "hbm energy ports " + commandPos(tablePos), 1,
                "Energy ports at " + tablePos.toShortString(), "total=36", "networked=");

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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2UseOnTurretSentry")
    public static void playerUseOnPlacesSentryTurretBottomPortAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 142);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 142);
        BlockPos turretPos = portCablePos.above();
        forceLoadedChunks(level, batteryPos.offset(-2, -1, -2), turretPos.offset(2, 4, 2));
        clearBox(level, batteryPos.offset(-2, -1, -2), turretPos.offset(2, 4, 2));
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(portCablePos.getX() + 0.5D, portCablePos.getY() + 1.0D, portCablePos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack turretStack = new ItemStack(ModBlocks.TURRET_SENTRY.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, turretStack);

        var placeResult = turretStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(portCablePos, Direction.UP)));
        if (!(level.getBlockEntity(turretPos) instanceof TurretBlockEntityBase turret)) {
            throw new AssertionError("player useOn turret_sentry placement returned " + placeResult
                    + " without placing turret_sentry above the bottom red_cable at " + portCablePos
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the turret_sentry block item action after placing the turret");
        assertSame(turret, level.getBlockEntity(turretPos),
                "player-placed turret_sentry survives bottom receiver-port cable placement");
        refreshEnergyNodeAt(level, portCablePos);
        HbmEnergyNodespace.tick(level);
        TurretBlockEntityBase.serverTick(level, turretPos, level.getBlockState(turretPos), turret);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                turret, "player-placed turret_sentry", 1, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed turret_sentry bottom cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, turretPos,
                "hbm energy port " + commandPos(turretPos) + " 0 -1 0 down", expectedLinks,
                "Energy port from " + turretPos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, turretPos, "hbm energy ports " + commandPos(turretPos), 1,
                "Energy ports at " + turretPos.toShortString(), "total=1", "networked=");

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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2UseOnTurretFritz")
    public static void playerUseOnPlacesFritzTurretRemoteReceiverPortsAndDiagnostics(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos batteryPos = new BlockPos(chunkStartX + 13, anchor.getY(), chunkStartZ + 144);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos portCablePos = new BlockPos(chunkStartX + 17, anchor.getY(), chunkStartZ + 144);
        BlockPos turretPos = portCablePos.east(2);
        BlockPos supportPos = turretPos.below();
        forceLoadedChunks(level, batteryPos.offset(-2, -2, -4), turretPos.offset(5, 4, 4));
        clearBox(level, batteryPos.offset(-2, -2, -4), turretPos.offset(5, 4, 4));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack turretStack = new ItemStack(ModBlocks.TURRET_FRITZ.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, turretStack);

        var placeResult = turretStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos, Direction.UP)));
        if (!(level.getBlockEntity(turretPos) instanceof TurretBlockEntityBase turret)) {
            throw new AssertionError("player useOn turret_fritz placement returned " + placeResult
                    + " without placing turret_fritz core at " + turretPos
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the turret_fritz block item action after placing the turret");
        assertTrue(level.getBlockState(turretPos).getValue(HorizontalMachineBlock.FACING) == Direction.SOUTH,
                "player-placed turret_fritz faces south for the selected legacy west receiver port");
        assertSame(turret, level.getBlockEntity(turretPos),
                "player-placed turret_fritz survives west remote receiver-port cable placement");
        refreshEnergyNodeAt(level, portCablePos);
        HbmEnergyNodespace.tick(level);
        TurretBlockEntityBase.serverTick(level, turretPos, level.getBlockState(turretPos), turret);
        assertEnergyBlockRemoteReceiverReceivesPower(level, batteryPos, portCablePos,
                turret, "player-placed turret_fritz", 8, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed turret_fritz west remote receiver port has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, turretPos,
                "hbm energy port " + commandPos(turretPos) + " -2 0 0 west", expectedLinks,
                "Energy port from " + turretPos.toShortString(), "conductor=", "lastTransfer=");
        assertCommandVisibleMessage(level, turretPos, "hbm energy ports " + commandPos(turretPos), 1,
                "Energy ports at " + turretPos.toShortString(), "total=8", "networked=");

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
        int received = turret.getTank().getFill();
        assertTrue(received > 0,
                "turret_fritz received diesel across the real fluid_duct_neo network");
        assertTrue(provider.getStoredFluid() < 1_000,
                "test diesel provider drained into the fluid_duct_neo network");
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

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2CommandDiagnostics")
    public static void powerNetToolUseOnKeepsLegacyCableDiagnosticBoundary(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos left = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos right = left.east();
            BlockPos stonePos = right.east();
            forceLoadedChunks(level, left, stonePos);
            level.setBlock(left, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(right, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(stonePos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            refreshEnergyNodeAt(level, left);
            refreshEnergyNodeAt(level, right);

            HbmEnergyNodespace.NetworkDebugSnapshot before =
                    HbmEnergyNodespace.getNetworkDebugSnapshot(level, left);
            assertTrue(before.nodePresent(), "power_net_tool fixture cable has a node");
            assertTrue(before.networkPresent(), "power_net_tool fixture cable has a valid network");
            assertEquals(2, before.network().links(), "power_net_tool fixture has two legacy cable links");
            assertEquals(0, before.network().providers(), "power_net_tool fixture has no providers");
            assertEquals(0, before.network().receivers(), "power_net_tool fixture has no receivers");
            HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, left);
            assertTrue(powerNet != null && powerNet.isValid(),
                    "power_net_tool fixture resolves the real cable power net");

            var player = FakePlayerFactory.getMinecraft(level);
            player.getInventory().clearContent();
            ItemStack tool = new ItemStack(ModItems.POWER_NET_TOOL.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);

            InteractionResult passResult = tool.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    blockHit(stonePos)));
            assertSame(InteractionResult.PASS, passResult,
                    "power_net_tool right-click on non-conductor stays legacy no-op/PASS");

            InteractionResult cableResult = tool.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    blockHit(left)));
            assertTrue(cableResult.consumesAction(),
                    "power_net_tool right-click on red_cable consumes action like legacy conductor diagnostic");

            HbmEnergyNodespace.NetworkDebugSnapshot after =
                    HbmEnergyNodespace.getNetworkDebugSnapshot(level, left);
            assertTrue(after.nodePresent(), "power_net_tool diagnostic leaves cable node present");
            assertTrue(after.networkPresent(), "power_net_tool diagnostic leaves cable network present");
            assertSame(powerNet, HbmEnergyUtil.getPowerNet(level, left),
                    "power_net_tool diagnostic observes the existing cable network without rebuilding it");
            assertEquals(2, after.network().links(), "power_net_tool diagnostic reports two cable links");
            assertEquals(0, after.network().providers(), "power_net_tool diagnostic reports zero providers");
            assertEquals(0, after.network().receivers(), "power_net_tool diagnostic reports zero receivers");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2CommandDiagnostics")
    public static void energyDebugParticleCommandTogglesServerSwitch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        HbmEnergyDebug.setParticleDebugEnabled(false);
        try {
            assertTrue(!HbmEnergyDebug.isParticleDebugEnabled(), "Energy debug particles default off");
            assertCommandVisibleMessage(level, anchor, "hbm energy nodespace", 0,
                    "Energy nodespace:", "debugParticles=false");

            assertCommandVisibleMessage(level, anchor, "hbm energy debug particles true", 1,
                    "Energy debug particles: true");
            assertTrue(HbmEnergyDebug.isParticleDebugEnabled(),
                    "Energy debug particle command enables the legacy network particle switch");
            assertCommandVisibleMessage(level, anchor, "hbm energy nodespace", 0,
                    "Energy nodespace:", "debugParticles=true");

            assertCommandVisibleMessage(level, anchor, "hbm energy debug particles", 0,
                    "Energy debug particles: false");
            assertTrue(!HbmEnergyDebug.isParticleDebugEnabled(),
                    "Energy debug particle toggle command disables the legacy network particle switch");

            assertCommandVisibleMessage(level, anchor, "hbm energy debug particles false", 0,
                    "Energy debug particles: false");
            assertTrue(!HbmEnergyDebug.isParticleDebugEnabled(),
                    "Energy debug particle false command keeps the switch disabled");
        } finally {
            HbmEnergyDebug.setParticleDebugEnabled(false);
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2ClientVisualAnchors")
    public static void energyClientVisualClasspathResourcesStayPublished(GameTestHelper helper) {
        assertClasspathResources("Energy Mk2 client visual anchors",
                "assets/hbm_ntm_rebirth/particles/network_power.json",
                "assets/hbm_ntm_rebirth/textures/particle/debug_power.png",
                "assets/hbm_ntm_rebirth/models/item/power_net_tool.json",
                "assets/hbm_ntm_rebirth/textures/item/power_net_tool.png",
                "assets/hbm_ntm_rebirth/textures/gui/storage/gui_battery.png",
                "assets/hbm_ntm_rebirth/textures/gui/storage/gui_battery_socket.png",
                "assets/hbm_ntm_rebirth/models/block/machine_battery.json",
                "assets/hbm_ntm_rebirth/models/block/machine_battery_socket.json",
                "assets/hbm_ntm_rebirth/models/block/machines/battery.obj",
                "assets/hbm_ntm_rebirth/models/block/machines/battery_pack_battery.json",
                "assets/hbm_ntm_rebirth/models/block/machines/battery_pack_capacitor.json",
                "assets/hbm_ntm_rebirth/models/block/machines/battery_socket_socket.json",
                "assets/hbm_ntm_rebirth/models/block/machines/battery_socket_supports.json",
                "assets/hbm_ntm_rebirth/models/blocks/capacitor.obj",
                "assets/hbm_ntm_rebirth/blockstates/capacitor_copper.json",
                "assets/hbm_ntm_rebirth/blockstates/capacitor_gold.json",
                "assets/hbm_ntm_rebirth/blockstates/capacitor_niobium.json",
                "assets/hbm_ntm_rebirth/blockstates/capacitor_tantalium.json",
                "assets/hbm_ntm_rebirth/blockstates/capacitor_schrabidate.json",
                "assets/hbm_ntm_rebirth/models/machines/conveyor_press.obj",
                "assets/hbm_ntm_rebirth/models/machines/conveyor_press.mtl",
                "assets/hbm_ntm_rebirth/textures/block/machines/conveyor_press.png",
                "assets/hbm_ntm_rebirth/textures/block/machines/conveyor_press_belt.png",
                "assets/hbm_ntm_rebirth/models/item/nuke_electric_kit.json",
                "assets/hbm_ntm_rebirth/textures/item/nuke_electric_kit.png",
                "assets/hbm_ntm_rebirth/blockstates/deco_toaster.json",
                "assets/hbm_ntm_rebirth/models/block/deco_toaster_iron.json",
                "assets/hbm_ntm_rebirth/models/block/deco_toaster_steel.json",
                "assets/hbm_ntm_rebirth/models/block/deco_toaster_wood.json",
                "assets/hbm_ntm_rebirth/models/blocks/toaster.obj",
                "assets/hbm_ntm_rebirth/textures/block/toaster_iron.png",
                "assets/hbm_ntm_rebirth/textures/block/toaster_steel.png",
                "assets/hbm_ntm_rebirth/textures/block/toaster_wood.png",
                "assets/hbm_ntm_rebirth/textures/gui/processing/gui_anvil.png",
                "assets/hbm_ntm_rebirth/models/blocks/anvil.obj");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "energyMk2CommandDiagnostics")
    public static void legacyWiringItemConnectsSmallPylonsAndClearsFailures(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HbmEnergyNodespace.unloadLevel(level);
        try {
            BlockPos left = helper.absolutePos(new BlockPos(1, 2, 1));
            BlockPos right = left.east(8);
            BlockPos sameNode = left.south(3);
            BlockPos farLeft = left.south(6);
            BlockPos farRight = farLeft.east(30);
            forceLoadedChunks(level, left, farRight);
            for (BlockPos pos : List.of(left, right, sameNode, farLeft, farRight)) {
                level.setBlock(pos, ModBlocks.RED_PYLON.get().defaultBlockState(), Block.UPDATE_ALL);
                refreshEnergyNodeAt(level, pos);
            }

            HbmLegacyWireNode leftWire = requireLegacyWireNodeAt(level, left);
            HbmLegacyWireNode rightWire = requireLegacyWireNodeAt(level, right);
            HbmLegacyWireNode sameWire = requireLegacyWireNodeAt(level, sameNode);
            HbmLegacyWireNode farLeftWire = requireLegacyWireNodeAt(level, farLeft);
            HbmLegacyWireNode farRightWire = requireLegacyWireNodeAt(level, farRight);
            assertEquals(0, leftWire.getWireConnections().size(), "legacy wiring starts with no left links");
            assertEquals(0, rightWire.getWireConnections().size(), "legacy wiring starts with no right links");

            var player = FakePlayerFactory.getMinecraft(level);
            player.getInventory().clearContent();
            ItemStack wiring = new ItemStack(ModItems.WIRING_RED_COPPER.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, wiring);

            InteractionResult startResult = wiring.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    blockHit(left)));
            assertTrue(startResult.consumesAction(), "wiring_red_copper first pylon click consumes action");
            assertStoredWireStart(wiring, left, "wiring_red_copper first pylon click stores legacy x/y/z");
            assertEquals(0, leftWire.getWireConnections().size(),
                    "wiring_red_copper first click does not connect immediately");

            InteractionResult endResult = wiring.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    blockHit(right)));
            assertTrue(endResult.consumesAction(), "wiring_red_copper second pylon click consumes action");
            assertTrue(wiring.getTag() == null, "wiring_red_copper successful second click clears stored start");
            assertTrue(leftWire.getWireConnections().contains(right),
                    "wiring_red_copper adds left-to-right legacy wire connection");
            assertTrue(rightWire.getWireConnections().contains(left),
                    "wiring_red_copper adds right-to-left legacy wire connection");
            assertEquals(1, leftWire.getWireConnections().size(), "left pylon has one legacy wire link");
            assertEquals(1, rightWire.getWireConnections().size(), "right pylon has one legacy wire link");

            wiring.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, blockHit(sameNode)));
            assertStoredWireStart(wiring, sameNode, "wiring_red_copper stores same-node failure start");
            InteractionResult sameResult = wiring.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    blockHit(sameNode)));
            assertTrue(sameResult.consumesAction(), "wiring_red_copper same-node failure consumes action");
            assertTrue(wiring.getTag() == null, "wiring_red_copper same-node failure clears stored start");
            assertEquals(0, sameWire.getWireConnections().size(),
                    "wiring_red_copper same-node failure does not add a self link");

            wiring.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, blockHit(farLeft)));
            assertStoredWireStart(wiring, farLeft, "wiring_red_copper stores too-far failure start");
            InteractionResult farResult = wiring.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    blockHit(farRight)));
            assertTrue(farResult.consumesAction(), "wiring_red_copper too-far failure consumes action");
            assertTrue(wiring.getTag() == null, "wiring_red_copper too-far failure clears stored start");
            assertEquals(0, farLeftWire.getWireConnections().size(),
                    "wiring_red_copper too-far failure leaves first pylon disconnected");
            assertEquals(0, farRightWire.getWireConnections().size(),
                    "wiring_red_copper too-far failure leaves second pylon disconnected");
        } finally {
            HbmEnergyNodespace.unloadLevel(level);
        }
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
    public static void machineBatterySocketMenuQuickMoveKeepsLegacyTransferShape(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos socketPos = helper.absolutePos(new BlockPos(1, 2, 1));
        MachineBatterySocketBlockEntity socket = placeBatterySocket(level, socketPos, Direction.SOUTH);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(socketPos.getX() + 0.5D, socketPos.getY() + 0.5D, socketPos.getZ() + 0.5D);
        player.getInventory().clearContent();
        MachineBatterySocketMenu menu = new MachineBatterySocketMenu(1, player.getInventory(), socket);
        assertEquals(37, menu.slots.size(), "battery_socket menu keeps one machine slot plus player inventory");
        assertTrue(menu.stillValid(player), "battery_socket menu is valid for a nearby player");

        player.getInventory().setItem(9, new ItemStack(Blocks.STONE, 16));
        ItemStack rejectedStone = menu.quickMoveStack(player, 1);
        assertTrue(rejectedStone.isEmpty(),
                "battery_socket player shift-click rejects non-battery stacks for the legacy socket slot");
        assertTrue(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY).isEmpty(),
                "battery_socket machine slot stays empty after rejected non-battery quick move");
        assertEquals(16, player.getInventory().getItem(9).getCount(),
                "battery_socket rejected player stack remains in its source slot");

        ItemStack partialBattery = chargedBatteryStack(1L);
        player.getInventory().setItem(10, partialBattery.copy());
        ItemStack movedBatteryToSocket = menu.quickMoveStack(player, 2);
        assertSame(partialBattery.getItem(), movedBatteryToSocket.getItem(),
                "battery_socket player quick move returns the moved battery copy");
        assertSame(partialBattery.getItem(), socket.getItems()
                        .getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY).getItem(),
                "battery_socket player shift-click fills the single legacy socket slot");
        assertBatteryCharge(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY), 1L,
                "battery_socket player shift-click preserves raw charge NBT");
        assertTrue(player.getInventory().getItem(10).isEmpty(),
                "battery_socket player source slot clears after battery quick move");

        player.getInventory().clearContent();
        ItemStack movedBatteryToPlayer = menu.quickMoveStack(player, 0);
        assertSame(partialBattery.getItem(), movedBatteryToPlayer.getItem(),
                "battery_socket machine-slot quick move returns moved battery copy");
        assertTrue(socket.getItems().getStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY).isEmpty(),
                "battery_socket shift-click from machine clears the legacy socket slot");
        assertSame(partialBattery.getItem(), player.getInventory().getItem(8).getItem(),
                "battery_socket machine-slot shift-click targets the hotbar end first");
        assertBatteryCharge(player.getInventory().getItem(8), 1L,
                "battery_socket machine-slot shift-click preserves raw charge NBT in player inventory");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatterySocketControlButtonsKeepLegacyCycles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos socketPos = helper.absolutePos(new BlockPos(1, 2, 1));
        MachineBatterySocketBlockEntity socket = placeBatterySocket(level, socketPos, Direction.SOUTH);
        var player = FakePlayerFactory.getMinecraft(level);

        assertEquals(MachineBatterySocketBlockEntity.MODE_INPUT, socket.getRedLow(),
                "battery_socket default low-redstone mode is input");
        assertEquals(MachineBatterySocketBlockEntity.MODE_OUTPUT, socket.getRedHigh(),
                "battery_socket default high-redstone mode is output");
        assertSame(HbmEnergyReceiver.ConnectionPriority.LOW, socket.getBatteryPriority(),
                "battery_socket default priority is legacy low");

        socket.handleClientControl(player,
                MachineBatterySocketBlockEntity.controlTag(MachineBatterySocketBlockEntity.CONTROL_RED_LOW));
        assertEquals(MachineBatterySocketBlockEntity.MODE_BUFFER, socket.getRedLow(),
                "battery_socket redLow button cycles input -> buffer");
        socket.handleClientControl(player,
                MachineBatterySocketBlockEntity.controlTag(MachineBatterySocketBlockEntity.CONTROL_RED_HIGH));
        assertEquals(MachineBatterySocketBlockEntity.MODE_NONE, socket.getRedHigh(),
                "battery_socket redHigh button cycles output -> none");
        socket.handleClientControl(player,
                MachineBatterySocketBlockEntity.controlTag(MachineBatterySocketBlockEntity.CONTROL_PRIORITY));
        assertSame(HbmEnergyReceiver.ConnectionPriority.NORMAL, socket.getBatteryPriority(),
                "battery_socket priority button cycles low -> normal");
        socket.handleClientControl(player,
                MachineBatterySocketBlockEntity.controlTag(MachineBatterySocketBlockEntity.CONTROL_PRIORITY));
        assertSame(HbmEnergyReceiver.ConnectionPriority.HIGH, socket.getBatteryPriority(),
                "battery_socket priority button cycles normal -> high");
        socket.handleClientControl(player,
                MachineBatterySocketBlockEntity.controlTag(MachineBatterySocketBlockEntity.CONTROL_PRIORITY));
        assertSame(HbmEnergyReceiver.ConnectionPriority.LOW, socket.getBatteryPriority(),
                "battery_socket priority button cycles high -> low");

        CompoundTag lowTag = new CompoundTag();
        lowTag.putBoolean("low", true);
        socket.receiveControl(player, lowTag);
        assertEquals(MachineBatterySocketBlockEntity.MODE_OUTPUT, socket.getRedLow(),
                "battery_socket legacy low NBT control cycles buffer -> output");
        CompoundTag highTag = new CompoundTag();
        highTag.putBoolean("high", true);
        socket.receiveControl(player, highTag);
        assertEquals(MachineBatterySocketBlockEntity.MODE_INPUT, socket.getRedHigh(),
                "battery_socket legacy high NBT control cycles none -> input");
        CompoundTag priorityTag = new CompoundTag();
        priorityTag.putBoolean("priority", true);
        socket.receiveControl(player, priorityTag);
        assertSame(HbmEnergyReceiver.ConnectionPriority.NORMAL, socket.getBatteryPriority(),
                "battery_socket legacy priority NBT control cycles low -> normal");

        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty")
    public static void machineBatterySocketMenuDataSlotsSyncLegacyEnergyState(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos socketPos = helper.absolutePos(new BlockPos(1, 2, 1));
        MachineBatterySocketBlockEntity socket = placeBatterySocket(level, socketPos, Direction.SOUTH);
        long storedPower = 4_294_967_555L;
        long delta = 8_589_934_777L;
        CompoundTag tag = new CompoundTag();
        tag.putShort("redLow", (short) MachineBatterySocketBlockEntity.MODE_BUFFER);
        tag.putShort("redHigh", (short) MachineBatterySocketBlockEntity.MODE_NONE);
        tag.putString("priority", HbmEnergyReceiver.ConnectionPriority.NORMAL.name());
        tag.putLong("Delta", delta);
        socket.load(tag);
        ItemStack batteryStack = chargedBatteryStack(storedPower);
        socket.getItems().setStackInSlot(MachineBatterySocketBlockEntity.SLOT_BATTERY, batteryStack);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(socketPos.getX() + 0.5D, socketPos.getY() + 0.5D, socketPos.getZ() + 0.5D);
        MachineBatterySocketMenu serverMenu = new MachineBatterySocketMenu(1, player.getInventory(), socket);
        MachineBatterySocketMenu clientMenu = new MachineBatterySocketMenu(2, player.getInventory(), socket);

        copyMenuDataSlots(serverMenu, clientMenu, 18);
        assertEquals(storedPower, clientMenu.getPower(),
                "battery_socket client menu receives full long power instead of low 32 bits");
        assertEquals(requireChargeable(batteryStack, "battery_redstone").getMaxCharge(batteryStack),
                clientMenu.getMaxPower(), "battery_socket client menu receives battery capacity");
        assertEquals(delta, clientMenu.getDelta(),
                "battery_socket client menu receives full long delta instead of low 32 bits");
        assertEquals(MachineBatterySocketBlockEntity.MODE_BUFFER, clientMenu.getRedLow(),
                "battery_socket client menu receives redLow mode");
        assertEquals(MachineBatterySocketBlockEntity.MODE_NONE, clientMenu.getRedHigh(),
                "battery_socket client menu receives redHigh mode");
        assertSame(HbmEnergyReceiver.ConnectionPriority.NORMAL, clientMenu.getPriority(),
                "battery_socket client menu receives priority");
        assertEquals(9, clientMenu.getPowerBarHeight(9),
                "battery_socket client menu clamps overfull legacy raw charge for display");

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
    public static void machineBatteryMenuDataSlotsSyncLegacyEnergyState(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos batteryPos = helper.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(batteryPos, ModBlocks.MACHINE_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        long storedPower = 765_432L;
        long delta = 4_294_967_321L;
        CompoundTag tag = new CompoundTag();
        tag.putLong("power", storedPower);
        tag.putShort("redLow", (short) MachineBatteryBlockEntity.MODE_OUTPUT);
        tag.putShort("redHigh", (short) MachineBatteryBlockEntity.MODE_NONE);
        tag.putString("priority", HbmEnergyReceiver.ConnectionPriority.HIGH.name());
        tag.putLong("Delta", delta);
        battery.load(tag);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(batteryPos.getX() + 0.5D, batteryPos.getY() + 0.5D, batteryPos.getZ() + 0.5D);
        MachineBatteryMenu serverMenu = new MachineBatteryMenu(1, player.getInventory(), battery);
        MachineBatteryMenu clientMenu = new MachineBatteryMenu(2, player.getInventory(), battery);

        copyMenuDataSlots(serverMenu, clientMenu, 18);
        assertEquals(storedPower, clientMenu.getPower(), "machine_battery client menu receives power");
        assertEquals(battery.getMaxPower(), clientMenu.getMaxPower(),
                "machine_battery client menu receives max power");
        assertEquals(delta, clientMenu.getDelta(),
                "machine_battery client menu receives full long delta instead of low 32 bits");
        assertEquals(MachineBatteryBlockEntity.MODE_OUTPUT, clientMenu.getRedLow(),
                "machine_battery client menu receives redLow mode");
        assertEquals(MachineBatteryBlockEntity.MODE_NONE, clientMenu.getRedHigh(),
                "machine_battery client menu receives redHigh mode");
        assertSame(HbmEnergyReceiver.ConnectionPriority.HIGH, clientMenu.getPriority(),
                "machine_battery client menu receives priority");
        assertEquals(7, clientMenu.getPowerBarHeight(10),
                "machine_battery client menu computes display bar from synced power/max");

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
        itemData.add(legacyItemData("\u0002hbm:item.battery_creative", 4729));
        itemData.add(legacyItemData("\u0002hbm:item.cube_power", 4730));
        itemData.add(legacyItemData("\u0002hbm:item.battery_potato", 4731));
        itemData.add(legacyItemData("\u0002hbm:item.battery_potatos", 4732));
        itemData.add(legacyItemData("\u0002hbm:item.hev_battery", 4733));
        itemData.add(legacyItemData("\u0002hbm:item.fusion_core", 4734));
        itemData.add(legacyItemData("\u0002hbm:item.energy_core", 4735));
        fml.put("ItemData", itemData);
        levelDat.put("FML", fml);

        LegacyWorldItemIdMap itemIds = LegacyWorldItemIdMap.fromLevelDatRoot(levelDat);
        assertEquals(9, itemIds.size(), "legacy item id map size");
        assertEquals("hbm:item.battery_pack", itemIds.legacyId(4727).orElseThrow(),
                "legacy battery_pack numeric id");
        assertEquals("hbm:item.battery_creative", itemIds.legacyId(4729).orElseThrow(),
                "legacy battery_creative numeric id");
        LegacyWorldItemIdMap dataWrappedItemIds =
                LegacyWorldItemIdMap.fromLevelDatRoot(legacyItemDataInsideDataLevelDat());
        assertEquals("hbm:item.battery_sc", dataWrappedItemIds.legacyId(4728).orElseThrow(),
                "legacy Data/FML/ItemData battery_sc numeric id");
        LegacyWorldItemIdMap oldModItemIds = LegacyWorldItemIdMap.fromLevelDatRoot(legacyModItemDataLevelDat());
        assertEquals(3, oldModItemIds.size(), "legacy ModItemData id map size");
        assertEquals("hbm:item.battery_pack", oldModItemIds.legacyId(6725).orElseThrow(),
                "legacy ModItemData battery_pack numeric id");
        LegacyItemStackMigration.Result oldModItemDataResult =
                LegacyItemStackMigration.migrateAll(legacyNumericRoot(6725, 5), oldModItemIds);
        assertEquals(1, oldModItemDataResult.migrated(), "legacy ModItemData numeric stack migrated");
        CompoundTag oldCreativeRoot = legacyNumericRoot(6727, 0);
        LegacyItemStackMigration.Result oldCreativeResult =
                LegacyItemStackMigration.migrateAll(oldCreativeRoot, oldModItemIds);
        assertEquals(1, oldCreativeResult.migrated(), "legacy ModItemData battery_creative stack migrated");
        assertEquals(ModItems.BATTERY_CREATIVE.getId().toString(),
                oldCreativeRoot.getList("Items", Tag.TAG_COMPOUND).getCompound(0).getString("id"),
                "legacy ModItemData battery_creative id");

        CompoundTag root = new CompoundTag();
        ListTag items = new ListTag();
        CompoundTag quantum = legacyNumericStackTag(4727, 5, 1);
        CompoundTag quantumData = new CompoundTag();
        quantumData.putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, 1234L);
        quantum.put("tag", quantumData);
        items.add(quantum);
        items.add(legacyNumericStackTag(4728, 2, 1));
        items.add(legacyNumericStackTag(4729, 0, 1));
        items.add(legacyNumericStackTag(4730, 0, 1));
        items.add(legacyNumericStackTag(4731, 0, 1));
        items.add(legacyNumericStackTag(4732, 0, 1));
        items.add(legacyNumericStackTag(4733, 0, 1));
        items.add(legacyNumericStackTag(4734, 0, 1));
        items.add(legacyNumericStackTag(4735, 0, 1));
        items.add(legacyNumericStackTag(4727, 99, 1));
        items.add(legacyNumericStackTag(9999, 0, 1));
        root.put("Items", items);

        LegacyItemStackMigration.Result result = LegacyItemStackMigration.migrateAll(root, itemIds);
        assertEquals(9, result.migrated(), "numeric legacy battery stacks migrated");
        assertEquals(0, result.numericItemStacksWithoutMap(), "numeric map was available");
        assertEquals(1, result.unknownNumericItemStacks(), "unknown numeric id counted");
        assertEquals(1, result.unknownLegacyBatteryMetas(), "unknown numeric battery_pack meta counted");
        assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(), items.getCompound(0).getString("id"),
                "numeric battery_pack meta 5 id");
        assertEquals(1234L, items.getCompound(0).getCompound("tag").getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG),
                "numeric legacy charge NBT preserved");
        assertEquals(ModItems.BATTERY_SC_RA226.getId().toString(), items.getCompound(1).getString("id"),
                "numeric battery_sc meta 2 id");
        assertNumericSingleBattery(items, 2, ModItems.BATTERY_CREATIVE, "battery_creative");
        assertNumericSingleBattery(items, 3, ModItems.CUBE_POWER, "cube_power");
        assertNumericSingleBattery(items, 4, ModItems.BATTERY_POTATO, "battery_potato");
        assertNumericSingleBattery(items, 5, ModItems.BATTERY_POTATOS, "battery_potatos");
        assertNumericSingleBattery(items, 6, ModItems.HEV_BATTERY, "hev_battery");
        assertNumericSingleBattery(items, 7, ModItems.FUSION_CORE, "fusion_core");
        assertNumericSingleBattery(items, 8, ModItems.ENERGY_CORE, "energy_core");
        assertEquals(4727, items.getCompound(9).getInt("id"), "unknown numeric battery_pack meta remains numeric");
        assertEquals(99, items.getCompound(9).getShort("Damage"), "unknown numeric battery_pack meta keeps Damage");
        assertEquals(9999, items.getCompound(10).getInt("id"), "unknown numeric id remains numeric");

        LegacyItemStackMigration.Result missingMap =
                LegacyItemStackMigration.migrateAll(legacyNumericRoot(4727, 5), LegacyWorldItemIdMap.empty());
        assertEquals(0, missingMap.migrated(), "numeric stack without map no-op");
        assertEquals(1, missingMap.numericItemStacksWithoutMap(), "numeric stack without map counted");
        assertEquals(0, missingMap.unknownLegacyBatteryMetas(),
                "numeric stack without map does not guess unknown battery meta");

        CompoundTag nestedRoot = new CompoundTag();
        CompoundTag crateStack = legacyStackTag("hbm:crate_steel", 0, 1);
        CompoundTag crateTag = new CompoundTag();
        ListTag nestedItems = new ListTag();
        CompoundTag nestedQuantum = legacyNumericStackTag(4727, 5, 1);
        CompoundTag nestedQuantumData = new CompoundTag();
        nestedQuantumData.putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, 5678L);
        nestedQuantum.put("tag", nestedQuantumData);
        nestedItems.add(nestedQuantum);
        nestedItems.add(legacyNumericStackTag(4728, 2, 1));
        nestedItems.add(legacyNumericStackTag(9999, 0, 1));
        crateTag.put("Items", nestedItems);
        crateStack.put("tag", crateTag);
        nestedRoot.put("CarriedCrate", crateStack);
        LegacyItemStackMigration.Result nestedResult = LegacyItemStackMigration.migrateAll(nestedRoot, itemIds);
        assertEquals(2, nestedResult.migrated(), "nested legacy inventory battery stacks migrated");
        assertEquals(1, nestedResult.unknownNumericItemStacks(), "nested unknown numeric id counted");
        ListTag migratedNestedItems = nestedRoot.getCompound("CarriedCrate").getCompound("tag")
                .getList("Items", Tag.TAG_COMPOUND);
        assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(), migratedNestedItems.getCompound(0).getString("id"),
                "nested numeric battery_pack meta 5 id");
        assertEquals(5678L, migratedNestedItems.getCompound(0).getCompound("tag")
                        .getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG),
                "nested numeric legacy charge NBT preserved");
        assertEquals(ModItems.BATTERY_SC_RA226.getId().toString(), migratedNestedItems.getCompound(1).getString("id"),
                "nested numeric battery_sc meta 2 id");
        assertEquals(9999, migratedNestedItems.getCompound(2).getInt("id"),
                "nested unknown numeric id remains numeric");

        CompoundTag toolboxRoot = new CompoundTag();
        CompoundTag toolboxStack = legacyStackTag("hbm:item.toolbox", 0, 1);
        CompoundTag toolboxTag = new CompoundTag();
        ListTag legacyToolboxItems = new ListTag();
        CompoundTag toolboxQuantum = legacyNumericStackTag(4727, 5, 1);
        CompoundTag toolboxQuantumData = new CompoundTag();
        toolboxQuantumData.putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, 9012L);
        toolboxQuantum.put("tag", toolboxQuantumData);
        toolboxQuantum.putByte("slot", (byte) 0);
        legacyToolboxItems.add(toolboxQuantum);
        CompoundTag toolboxRa226 = legacyNumericStackTag(4728, 2, 1);
        toolboxRa226.putByte("slot", (byte) 7);
        legacyToolboxItems.add(toolboxRa226);
        CompoundTag toolboxUnknown = legacyNumericStackTag(9999, 0, 1);
        toolboxUnknown.putByte("slot", (byte) 9);
        legacyToolboxItems.add(toolboxUnknown);
        toolboxTag.put("items", legacyToolboxItems);
        toolboxStack.put("tag", toolboxTag);
        toolboxRoot.put("CarriedToolbox", toolboxStack);
        LegacyItemStackMigration.Result toolboxResult = LegacyItemStackMigration.migrateAll(toolboxRoot, itemIds);
        assertEquals(2, toolboxResult.migrated(), "legacy item-container lowercase items battery stacks migrated");
        assertEquals(1, toolboxResult.unknownNumericItemStacks(),
                "legacy item-container lowercase items unknown numeric id counted");
        ListTag migratedToolboxItems = toolboxRoot.getCompound("CarriedToolbox").getCompound("tag")
                .getList("items", Tag.TAG_COMPOUND);
        assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(), migratedToolboxItems.getCompound(0).getString("id"),
                "legacy item-container battery_pack meta 5 id");
        assertEquals(0, migratedToolboxItems.getCompound(0).getByte("slot"),
                "legacy item-container battery_pack slot preserved");
        assertTrue(!migratedToolboxItems.getCompound(0).contains("Damage"),
                "legacy item-container battery_pack Damage removed");
        assertEquals(9012L, migratedToolboxItems.getCompound(0).getCompound("tag")
                        .getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG),
                "legacy item-container battery_pack charge preserved");
        assertEquals(ModItems.BATTERY_SC_RA226.getId().toString(),
                migratedToolboxItems.getCompound(1).getString("id"),
                "legacy item-container battery_sc meta 2 id");
        assertEquals(7, migratedToolboxItems.getCompound(1).getByte("slot"),
                "legacy item-container battery_sc slot preserved");
        assertTrue(!migratedToolboxItems.getCompound(1).contains("Damage"),
                "legacy item-container battery_sc Damage removed");
        assertEquals(9999, migratedToolboxItems.getCompound(2).getInt("id"),
                "legacy item-container unknown numeric id remains numeric");
        assertEquals(9, migratedToolboxItems.getCompound(2).getByte("slot"),
                "legacy item-container unknown numeric id slot preserved");

        CompoundTag chunkLikeRoot = legacyNumericRoot(4727, 5);
        chunkLikeRoot.getList("Items", Tag.TAG_COMPOUND).add(legacyNumericStackTag(4727, 99, 1));
        BlockMigrationHelper.resetDiagnostics();
        BlockMigrationHelper.setLegacyItemIdsForTesting(itemIds);
        try {
            BlockMigrationHelper.doMigration(null, chunkLikeRoot, 0, 1);
            BlockMigrationHelper.MigrationDiagnostics diagnostics = BlockMigrationHelper.diagnostics();
            assertEquals(1L, diagnostics.migratedItemStacks(), "numeric chunk migration diagnostic count");
            assertEquals(0L, diagnostics.numericItemStacksWithoutMap(), "numeric chunk migration had map");
            assertEquals(1L, diagnostics.unknownLegacyBatteryMetas(),
                    "numeric chunk migration counted unknown battery meta");
            assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(),
                    chunkLikeRoot.getList("Items", Tag.TAG_COMPOUND).getCompound(0).getString("id"),
                    "numeric chunk migration id");
            assertEquals(4727, chunkLikeRoot.getList("Items", Tag.TAG_COMPOUND).getCompound(1).getInt("id"),
                    "numeric chunk migration leaves unknown battery meta numeric");
        } finally {
            BlockMigrationHelper.setLegacyItemIdsForTesting(LegacyWorldItemIdMap.empty());
            BlockMigrationHelper.resetDiagnostics();
        }
    }

    private static void legacyBatteryDroppedItemEntityNbtMigrationPreservesCharge() {
        CompoundTag levelDat = new CompoundTag();
        CompoundTag fml = new CompoundTag();
        ListTag itemData = new ListTag();
        itemData.add(legacyItemData("\u0002hbm:item.battery_pack", 4727));
        itemData.add(legacyItemData("\u0002hbm:item.battery_sc", 4728));
        fml.put("ItemData", itemData);
        levelDat.put("FML", fml);
        LegacyWorldItemIdMap itemIds = LegacyWorldItemIdMap.fromLevelDatRoot(levelDat);

        CompoundTag droppedQuantumRoot = legacyDroppedItemEntityRoot(4727, 5, 2468L);
        LegacyItemStackMigration.Result directResult =
                LegacyItemStackMigration.migrateAll(droppedQuantumRoot, itemIds);
        assertEquals(1, directResult.migrated(), "legacy dropped EntityItem battery stack migrated");
        assertEquals(0, directResult.numericItemStacksWithoutMap(), "legacy dropped EntityItem map was available");
        CompoundTag droppedQuantum = droppedQuantumRoot.getCompound("Level")
                .getList("Entities", Tag.TAG_COMPOUND)
                .getCompound(0)
                .getCompound("Item");
        assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(), droppedQuantum.getString("id"),
                "legacy dropped EntityItem battery_pack meta 5 id");
        assertTrue(!droppedQuantum.contains("Damage"),
                "legacy dropped EntityItem Damage removed after split-id migration");
        assertEquals(2468L, droppedQuantum.getCompound("tag").getLong(HbmBatteryItem.DEFAULT_CHARGE_TAG),
                "legacy dropped EntityItem charge NBT preserved");
        assertEquals(1, droppedQuantum.getByte("Count"), "legacy dropped EntityItem Count preserved");

        CompoundTag mixedDroppedRoot = legacyDroppedItemEntityRoot(4728, 2, 0L);
        ListTag entities = mixedDroppedRoot.getCompound("Level").getList("Entities", Tag.TAG_COMPOUND);
        CompoundTag unknownEntity = new CompoundTag();
        unknownEntity.putString("id", "Item");
        unknownEntity.put("Item", legacyNumericStackTag(9999, 0, 1));
        entities.add(unknownEntity);
        CompoundTag unknownMetaEntity = new CompoundTag();
        unknownMetaEntity.putString("id", "Item");
        unknownMetaEntity.put("Item", legacyNumericStackTag(4727, 99, 1));
        entities.add(unknownMetaEntity);
        LegacyItemStackMigration.Result mixedResult =
                LegacyItemStackMigration.migrateAll(mixedDroppedRoot, itemIds);
        assertEquals(1, mixedResult.migrated(), "legacy dropped EntityItem battery_sc stack migrated");
        assertEquals(1, mixedResult.unknownNumericItemStacks(),
                "legacy dropped EntityItem unknown numeric id counted");
        assertEquals(1, mixedResult.unknownLegacyBatteryMetas(),
                "legacy dropped EntityItem unknown battery meta counted");
        assertEquals(ModItems.BATTERY_SC_RA226.getId().toString(),
                entities.getCompound(0).getCompound("Item").getString("id"),
                "legacy dropped EntityItem battery_sc meta 2 id");
        assertEquals(9999, entities.getCompound(1).getCompound("Item").getInt("id"),
                "legacy dropped EntityItem unknown numeric id remains numeric");
        assertEquals(4727, entities.getCompound(2).getCompound("Item").getInt("id"),
                "legacy dropped EntityItem unknown battery meta remains numeric");
        assertEquals(99, entities.getCompound(2).getCompound("Item").getShort("Damage"),
                "legacy dropped EntityItem unknown battery meta keeps Damage");

        CompoundTag chunkLikeRoot = legacyDroppedItemEntityRoot(4727, 5, 1357L);
        CompoundTag chunkUnknownMetaEntity = new CompoundTag();
        chunkUnknownMetaEntity.putString("id", "Item");
        chunkUnknownMetaEntity.put("Item", legacyNumericStackTag(4727, 99, 1));
        chunkLikeRoot.getCompound("Level").getList("Entities", Tag.TAG_COMPOUND).add(chunkUnknownMetaEntity);
        BlockMigrationHelper.resetDiagnostics();
        BlockMigrationHelper.setLegacyItemIdsForTesting(itemIds);
        try {
            BlockMigrationHelper.doMigration(null, chunkLikeRoot, 0, 1);
            BlockMigrationHelper.MigrationDiagnostics diagnostics = BlockMigrationHelper.diagnostics();
            assertEquals(1L, diagnostics.migratedItemStacks(),
                    "legacy dropped EntityItem chunk migration diagnostic count");
            assertEquals(0L, diagnostics.numericItemStacksWithoutMap(),
                    "legacy dropped EntityItem chunk migration had map");
            assertEquals(1L, diagnostics.unknownLegacyBatteryMetas(),
                    "legacy dropped EntityItem chunk migration counted unknown battery meta");
            assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(),
                    chunkLikeRoot.getCompound("Level")
                            .getList("Entities", Tag.TAG_COMPOUND)
                            .getCompound(0)
                            .getCompound("Item")
                            .getString("id"),
                    "legacy dropped EntityItem chunk migration id");
        } finally {
            BlockMigrationHelper.setLegacyItemIdsForTesting(LegacyWorldItemIdMap.empty());
            BlockMigrationHelper.resetDiagnostics();
        }
    }

    private static void assertNumericSingleBattery(ListTag items, int index, RegistryObject<Item> expected,
            String label) {
        CompoundTag stack = items.getCompound(index);
        assertEquals(expected.getId().toString(), stack.getString("id"),
                "numeric " + label + " single-id stack id");
        assertTrue(!stack.contains("Damage"), "numeric " + label + " single-id Damage removed");
    }

    private static void legacyBatteryRealLocalWorldSamplesUseActualLevelDatItemData() {
        int checked = 0;
        for (LegacyWorldSample sample : legacyWorldSamples()) {
            if (!Files.isDirectory(sample.root())) {
                continue;
            }
            LegacyWorldItemIdMap.LoadResult load = LegacyWorldItemIdMap.loadFromWorldRoot(sample.root());
            assertTrue(load.loaded(), "legacy sample " + sample.label() + " item id map loaded: " + load.summary());
            LegacyWorldItemIdMap itemIds = load.map();
            assertEquals("hbm:item.battery_pack", itemIds.legacyId(sample.batteryPackId()).orElseThrow(),
                    "legacy sample " + sample.label() + " battery_pack numeric id");
            assertEquals("hbm:item.battery_sc", itemIds.legacyId(sample.batteryScId()).orElseThrow(),
                    "legacy sample " + sample.label() + " battery_sc numeric id");
            assertEquals("hbm:item.battery_creative", itemIds.legacyId(sample.batteryCreativeId()).orElseThrow(),
                    "legacy sample " + sample.label() + " battery_creative numeric id");

            CompoundTag quantumRoot = legacyNumericRoot(sample.batteryPackId(), 5);
            LegacyItemStackMigration.Result quantumResult = LegacyItemStackMigration.migrateAll(quantumRoot, itemIds);
            assertEquals(1, quantumResult.migrated(),
                    "legacy sample " + sample.label() + " battery_pack meta 5 migrated");
            assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(),
                    quantumRoot.getList("Items", Tag.TAG_COMPOUND).getCompound(0).getString("id"),
                    "legacy sample " + sample.label() + " battery_pack meta 5 id");

            CompoundTag ra226Root = legacyNumericRoot(sample.batteryScId(), 2);
            LegacyItemStackMigration.Result ra226Result = LegacyItemStackMigration.migrateAll(ra226Root, itemIds);
            assertEquals(1, ra226Result.migrated(),
                    "legacy sample " + sample.label() + " battery_sc meta 2 migrated");
            assertEquals(ModItems.BATTERY_SC_RA226.getId().toString(),
                    ra226Root.getList("Items", Tag.TAG_COMPOUND).getCompound(0).getString("id"),
                    "legacy sample " + sample.label() + " battery_sc meta 2 id");

            CompoundTag creativeRoot = legacyNumericRoot(sample.batteryCreativeId(), 0);
            LegacyItemStackMigration.Result creativeResult =
                    LegacyItemStackMigration.migrateAll(creativeRoot, itemIds);
            assertEquals(1, creativeResult.migrated(),
                    "legacy sample " + sample.label() + " battery_creative migrated");
            assertEquals(ModItems.BATTERY_CREATIVE.getId().toString(),
                    creativeRoot.getList("Items", Tag.TAG_COMPOUND).getCompound(0).getString("id"),
                    "legacy sample " + sample.label() + " battery_creative id");
            checked++;
        }
        if (checked == 0) {
            return;
        }
        assertTrue(checked > 0, "local 1.7.10 HBM legacy sample world checked");
    }

    private static void legacyBatteryRealLocalWorldSamplesLoadThroughServerMigrationCache() {
        int checked = 0;
        BlockMigrationHelper.resetDiagnostics();
        try {
            for (LegacyWorldSample sample : legacyWorldSamples()) {
                if (!Files.isDirectory(sample.root())) {
                    continue;
                }
                LegacyWorldItemIdMap.LoadResult load = BlockMigrationHelper.loadLegacyItemIds(sample.root());
                assertTrue(load.loaded(),
                        "legacy sample " + sample.label() + " server migration cache loaded: " + load.summary());
                assertTrue(BlockMigrationHelper.diagnostics().legacyItemIdMapLoad().loaded(),
                        "legacy sample " + sample.label() + " diagnostics expose cached item id map");
                LegacyWorldItemIdMap itemIds = BlockMigrationHelper.legacyItemIds();
                assertEquals("hbm:item.battery_pack", itemIds.legacyId(sample.batteryPackId()).orElseThrow(),
                        "legacy sample " + sample.label() + " cached battery_pack numeric id");
                assertEquals("hbm:item.battery_sc", itemIds.legacyId(sample.batteryScId()).orElseThrow(),
                        "legacy sample " + sample.label() + " cached battery_sc numeric id");
                assertEquals("hbm:item.battery_creative", itemIds.legacyId(sample.batteryCreativeId()).orElseThrow(),
                        "legacy sample " + sample.label() + " cached battery_creative numeric id");

                CompoundTag cachedQuantumRoot = legacyNumericRoot(sample.batteryPackId(), 5);
                LegacyItemStackMigration.Result cachedQuantumResult =
                        LegacyItemStackMigration.migrateAll(cachedQuantumRoot, itemIds);
                assertEquals(1, cachedQuantumResult.migrated(),
                        "legacy sample " + sample.label() + " cached battery_pack meta 5 migrated");
                assertEquals(ModItems.BATTERY_QUANTUM.getId().toString(),
                        cachedQuantumRoot.getList("Items", Tag.TAG_COMPOUND).getCompound(0).getString("id"),
                        "legacy sample " + sample.label() + " cached battery_pack meta 5 id");
                checked++;
            }
        } finally {
            BlockMigrationHelper.setLegacyItemIdsForTesting(LegacyWorldItemIdMap.empty());
            BlockMigrationHelper.resetDiagnostics();
        }
        if (checked == 0) {
            return;
        }
        assertTrue(checked > 0, "local 1.7.10 HBM legacy sample world loaded through migration cache");
    }

    private static void legacyBatteryRealLocalWorldSamplesMigrateActualSavedStacks() {
        int checked = 0;
        for (LegacyWorldSample sample : legacyWorldSamples()) {
            if (!Files.isDirectory(sample.root())) {
                continue;
            }
            LegacyWorldItemIdMap.LoadResult load = LegacyWorldItemIdMap.loadFromWorldRoot(sample.root());
            assertTrue(load.loaded(), "legacy sample " + sample.label() + " item id map loaded: " + load.summary());
            LegacyWorldItemIdMap itemIds = load.map();

            SavedStackMigrationResult playerResult =
                    migrateLegacyPlayerDataSamples(sample.root().resolve("playerdata"), itemIds);
            assertNoSavedStackMigrationDiagnostics(playerResult,
                    "legacy sample " + sample.label() + " playerdata");
            assertEquals(sample.expectedPlayerMigrations(), playerResult.migrated(),
                    "legacy sample " + sample.label() + " playerdata migrated stack count");
            assertEquals(sample.expectedPlayerMigrations(), playerResult.count(ModItems.BATTERY_CREATIVE),
                    "legacy sample " + sample.label() + " playerdata battery_creative stacks");
            if (sample.expectedPlayerMigrations() > 0) {
                assertTrue(playerResult.sourcesWithMigrations() > 0,
                        "legacy sample " + sample.label() + " had playerdata sources with migrated stacks");
            }
            checked++;

            SavedStackMigrationResult regionResult = migrateLegacyWorldRegionSamples(sample.root(), itemIds);
            assertNoSavedStackMigrationDiagnostics(regionResult,
                    "legacy sample " + sample.label() + " world region replay");
            assertEquals(sample.expectedRegionMigrations(), regionResult.migrated(),
                    "legacy sample " + sample.label() + " region TileEntity migrated stack count");
            assertEquals(sample.expectedRegionCreative(), regionResult.count(ModItems.BATTERY_CREATIVE),
                    "legacy sample " + sample.label() + " region battery_creative stacks");
            assertEquals(sample.expectedRegionLead(), regionResult.count(ModItems.BATTERY_LEAD),
                    "legacy sample " + sample.label() + " region battery_lead stacks");
            assertEquals(sample.expectedRegionRedstone(), regionResult.count(ModItems.BATTERY_REDSTONE),
                    "legacy sample " + sample.label() + " region battery_redstone stacks");
            assertEquals(sample.expectedRegionDroppedEnergyEntities(), regionResult.droppedEnergyEntities(),
                    "legacy sample " + sample.label() + " region dropped Energy EntityItem stacks");
            assertEquals(sample.expectedRegionLowercaseItemContainers(), regionResult.lowercaseItemContainers(),
                    "legacy sample " + sample.label() + " region lowercase old item containers");
            assertEquals(sample.expectedRegionChargedEnergyStacks(), regionResult.chargedEnergyStacks(),
                    "legacy sample " + sample.label() + " region charged Energy stacks");
            if (sample.expectedRegionMigrations() > 0) {
                assertTrue(regionResult.sourcesWithMigrations() > 0,
                        "legacy sample " + sample.label() + " had region chunks with migrated stacks");
            }
            checked++;
        }
        if (checked == 0) {
            return;
        }
        assertTrue(checked > 0, "local 1.7.10 HBM legacy saved stack samples checked");
    }

    private static SavedStackMigrationResult migrateLegacyPlayerDataSamples(Path playerDataDir,
            LegacyWorldItemIdMap itemIds) {
        if (!Files.isDirectory(playerDataDir)) {
            return SavedStackMigrationResult.empty();
        }
        SavedStackMigrationResult result = SavedStackMigrationResult.empty();
        try (var paths = Files.list(playerDataDir)) {
            for (Path path : paths.filter(path -> path.getFileName().toString().endsWith(".dat")).toList()) {
                CompoundTag root = NbtIo.readCompressed(path.toFile());
                SavedStackMigrationResult fileResult = migrateAndCountSavedStackRoot(root, itemIds);
                result = result.plus(fileResult.migrated() > 0 ? fileResult.withSource() : fileResult);
            }
        } catch (IOException exception) {
            throw new AssertionError("Failed to read legacy playerdata samples under " + playerDataDir, exception);
        }
        return result;
    }

    private static SavedStackMigrationResult migrateLegacyWorldRegionSamples(Path worldRoot,
            LegacyWorldItemIdMap itemIds) {
        if (!Files.isDirectory(worldRoot)) {
            return SavedStackMigrationResult.empty();
        }
        SavedStackMigrationResult result = SavedStackMigrationResult.empty();
        try (var paths = Files.walk(worldRoot)) {
            List<Path> regionDirs = paths
                    .filter(Files::isDirectory)
                    .filter(path -> "region".equals(path.getFileName().toString()))
                    .toList();
            for (Path regionDir : regionDirs) {
                result = result.plus(migrateLegacyRegionSamples(regionDir, itemIds));
            }
        } catch (IOException exception) {
            throw new AssertionError("Failed to scan legacy region directories under " + worldRoot, exception);
        }
        return result;
    }

    private static SavedStackMigrationResult migrateLegacyRegionSamples(Path regionDir, LegacyWorldItemIdMap itemIds) {
        if (!Files.isDirectory(regionDir)) {
            return SavedStackMigrationResult.empty();
        }
        SavedStackMigrationResult result = SavedStackMigrationResult.empty();
        try (var paths = Files.list(regionDir)) {
            for (Path path : paths.filter(path -> path.getFileName().toString().endsWith(".mca")).toList()) {
                result = result.plus(migrateLegacyRegionFile(path, itemIds));
            }
        } catch (IOException exception) {
            throw new AssertionError("Failed to read legacy region samples under " + regionDir, exception);
        }
        return result;
    }

    private static SavedStackMigrationResult migrateLegacyRegionFile(Path regionFile, LegacyWorldItemIdMap itemIds) {
        byte[] data;
        try {
            data = Files.readAllBytes(regionFile);
        } catch (IOException exception) {
            throw new AssertionError("Failed to read legacy region file " + regionFile, exception);
        }
        SavedStackMigrationResult result = SavedStackMigrationResult.empty();
        for (int index = 0; index < 1024; index++) {
            int tableIndex = index * 4;
            if (tableIndex + 4 > data.length) {
                break;
            }
            int sectorOffset = ((data[tableIndex] & 0xFF) << 16)
                    | ((data[tableIndex + 1] & 0xFF) << 8)
                    | (data[tableIndex + 2] & 0xFF);
            int sectorCount = data[tableIndex + 3] & 0xFF;
            if (sectorOffset == 0 || sectorCount == 0) {
                continue;
            }
            int chunkOffset = sectorOffset * 4096;
            if (chunkOffset + 5 > data.length) {
                continue;
            }
            int length = ((data[chunkOffset] & 0xFF) << 24)
                    | ((data[chunkOffset + 1] & 0xFF) << 16)
                    | ((data[chunkOffset + 2] & 0xFF) << 8)
                    | (data[chunkOffset + 3] & 0xFF);
            if (length <= 1 || chunkOffset + 4 + length > data.length) {
                continue;
            }
            int compression = data[chunkOffset + 4] & 0xFF;
            try (InputStream compressed = new ByteArrayInputStream(data, chunkOffset + 5, length - 1);
                    InputStream inflated = legacyRegionChunkInput(compressed, compression);
                    DataInputStream input = new DataInputStream(inflated)) {
                CompoundTag chunk = NbtIo.read(input);
                SavedStackMigrationResult chunkResult = migrateAndCountSavedStackRoot(chunk, itemIds);
                result = result.plus(chunkResult.migrated() > 0 ? chunkResult.withSource() : chunkResult);
            } catch (IOException exception) {
                throw new AssertionError("Failed to read legacy region chunk " + regionFile + "#" + index, exception);
            }
        }
        return result;
    }

    private static InputStream legacyRegionChunkInput(InputStream compressed, int compression) throws IOException {
        return switch (compression) {
            case 1 -> new GZIPInputStream(compressed);
            case 2 -> new InflaterInputStream(compressed);
            default -> throw new IOException("Unsupported legacy region compression type " + compression);
        };
    }

    private static SavedStackMigrationResult migrateAndCountSavedStackRoot(CompoundTag root,
            LegacyWorldItemIdMap itemIds) {
        LegacyItemStackMigration.Result migration = LegacyItemStackMigration.migrateAll(root, itemIds);
        Map<String, Integer> migratedIds = new LinkedHashMap<>();
        SavedStackShapeCounts shapeCounts = countModernStackShapes(root, migratedIds);
        return new SavedStackMigrationResult(migration.migrated(), 0, migration.numericItemStacksWithoutMap(),
                migration.unknownNumericItemStacks(), migration.unknownLegacyBatteryMetas(), shapeCounts.chargedStacks(),
                shapeCounts.droppedEnergyEntities(), shapeCounts.lowercaseItemContainers(), migratedIds);
    }

    private static SavedStackShapeCounts countModernStackShapes(Tag tag, Map<String, Integer> counts) {
        SavedStackShapeCounts result = SavedStackShapeCounts.empty();
        if (tag instanceof CompoundTag compound) {
            if (compound.contains("items", Tag.TAG_LIST)
                    && listContainsModernEnergyStack(compound.getList("items", Tag.TAG_COMPOUND))) {
                result = result.plus(new SavedStackShapeCounts(0, 0, 1));
            }
            if (isLegacyDroppedItemEntity(compound)) {
                CompoundTag item = compound.getCompound("Item");
                if (isModernEnergyStack(item)) {
                    result = result.plus(new SavedStackShapeCounts(0, 1, 0));
                }
            }
            if (compound.contains("id", Tag.TAG_STRING) && compound.contains("Count")) {
                String id = compound.getString("id");
                if (id.startsWith(HbmNtm.MOD_ID + ":")) {
                    counts.merge(id, 1, Integer::sum);
                }
                if (isModernEnergyStack(compound) && compound.contains("tag", Tag.TAG_COMPOUND)
                        && compound.getCompound("tag").contains(HbmBatteryItem.DEFAULT_CHARGE_TAG, Tag.TAG_LONG)) {
                    result = result.plus(new SavedStackShapeCounts(1, 0, 0));
                }
            }
            for (String key : compound.getAllKeys()) {
                result = result.plus(countModernStackShapes(compound.get(key), counts));
            }
        } else if (tag instanceof ListTag list) {
            for (Tag child : list) {
                result = result.plus(countModernStackShapes(child, counts));
            }
        }
        return result;
    }

    private static boolean isLegacyDroppedItemEntity(CompoundTag compound) {
        return compound.contains("Item", Tag.TAG_COMPOUND)
                && compound.contains("id", Tag.TAG_STRING)
                && ("Item".equals(compound.getString("id")) || "EntityItem".equals(compound.getString("id")));
    }

    private static boolean listContainsModernEnergyStack(ListTag list) {
        for (Tag child : list) {
            if (child instanceof CompoundTag compound && isModernEnergyStack(compound)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isModernEnergyStack(CompoundTag compound) {
        if (!compound.contains("id", Tag.TAG_STRING) || !compound.contains("Count")) {
            return false;
        }
        String id = compound.getString("id");
        return id.equals(ModItems.BATTERY_LEAD.getId().toString())
                || id.equals(ModItems.BATTERY_REDSTONE.getId().toString())
                || id.equals(ModItems.BATTERY_CREATIVE.getId().toString())
                || id.startsWith(HbmNtm.MOD_ID + ":battery_")
                || id.equals(ModItems.ENERGY_CORE.getId().toString())
                || id.equals(ModItems.FUSION_CORE.getId().toString())
                || id.equals(ModItems.CUBE_POWER.getId().toString());
    }

    private static List<LegacyWorldSample> legacyWorldSamples() {
        Path versionsRoot = Path.of("E:\\", "\u6e38\u620f", "\u6211\u7684\u4e16\u754c", ".minecraft", "versions");
        Path saveRoot = versionsRoot.resolve("1.7.10-hbm-text").resolve("saves");
        return List.of(
                new LegacyWorldSample(saveRoot.resolve("\u65b0\u7684\u4e16\u754c"), 4727, 4728, 4729,
                        0, 1, 0, 1, 0, 0, 0, 0,
                        "1.7.10-hbm-text/world-a"),
                new LegacyWorldSample(saveRoot.resolve("\u65b0\u7684\u4e16\u754c-"), 6725, 6726, 6727,
                        1, 6, 2, 3, 1, 0, 2, 0,
                        "1.7.10-hbm-text/world-b"));
    }

    private record LegacyWorldSample(Path root, int batteryPackId, int batteryScId, int batteryCreativeId,
            int expectedPlayerMigrations, int expectedRegionMigrations, int expectedRegionCreative,
            int expectedRegionLead, int expectedRegionRedstone, int expectedRegionDroppedEnergyEntities,
            int expectedRegionLowercaseItemContainers, int expectedRegionChargedEnergyStacks,
            String label) {
    }

    private static void assertNoSavedStackMigrationDiagnostics(SavedStackMigrationResult result, String label) {
        assertEquals(0, result.numericItemStacksWithoutMap(), label + " numeric stacks without map");
        assertEquals(0, result.unknownNumericItemStacks(), label + " unknown numeric item stacks");
        assertEquals(0, result.unknownLegacyBatteryMetas(), label + " unknown legacy battery metas");
    }

    private record SavedStackMigrationResult(int migrated, int sourcesWithMigrations, int numericItemStacksWithoutMap,
            int unknownNumericItemStacks, int unknownLegacyBatteryMetas, int chargedEnergyStacks,
            int droppedEnergyEntities, int lowercaseItemContainers,
            Map<String, Integer> migratedIds) {
        static SavedStackMigrationResult empty() {
            return new SavedStackMigrationResult(0, 0, 0, 0, 0, 0, 0, 0, Map.of());
        }

        SavedStackMigrationResult withSource() {
            return new SavedStackMigrationResult(migrated, 1, numericItemStacksWithoutMap, unknownNumericItemStacks,
                    unknownLegacyBatteryMetas, chargedEnergyStacks, droppedEnergyEntities, lowercaseItemContainers,
                    migratedIds);
        }

        SavedStackMigrationResult plus(SavedStackMigrationResult other) {
            if (other == null || other.isEmpty()) {
                return this;
            }
            Map<String, Integer> merged = new LinkedHashMap<>(migratedIds);
            other.migratedIds.forEach((id, count) -> merged.merge(id, count, Integer::sum));
            return new SavedStackMigrationResult(
                    migrated + other.migrated,
                    sourcesWithMigrations + other.sourcesWithMigrations,
                    numericItemStacksWithoutMap + other.numericItemStacksWithoutMap,
                    unknownNumericItemStacks + other.unknownNumericItemStacks,
                    unknownLegacyBatteryMetas + other.unknownLegacyBatteryMetas,
                    chargedEnergyStacks + other.chargedEnergyStacks,
                    droppedEnergyEntities + other.droppedEnergyEntities,
                    lowercaseItemContainers + other.lowercaseItemContainers,
                    merged);
        }

        boolean isEmpty() {
            return migrated == 0
                    && sourcesWithMigrations == 0
                    && numericItemStacksWithoutMap == 0
                    && unknownNumericItemStacks == 0
                    && unknownLegacyBatteryMetas == 0
                    && chargedEnergyStacks == 0
                    && droppedEnergyEntities == 0
                    && lowercaseItemContainers == 0
                    && migratedIds.isEmpty();
        }

        int count(RegistryObject<Item> item) {
            return migratedIds.getOrDefault(item.getId().toString(), 0);
        }
    }

    private record SavedStackShapeCounts(int chargedStacks, int droppedEnergyEntities, int lowercaseItemContainers) {
        static SavedStackShapeCounts empty() {
            return new SavedStackShapeCounts(0, 0, 0);
        }

        SavedStackShapeCounts plus(SavedStackShapeCounts other) {
            return new SavedStackShapeCounts(chargedStacks + other.chargedStacks,
                    droppedEnergyEntities + other.droppedEnergyEntities,
                    lowercaseItemContainers + other.lowercaseItemContainers);
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

    private static CompoundTag legacyDroppedItemEntityRoot(int id, int damage, long charge) {
        CompoundTag root = new CompoundTag();
        CompoundTag level = new CompoundTag();
        ListTag entities = new ListTag();
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "Item");
        CompoundTag stack = legacyNumericStackTag(id, damage, 1);
        if (charge > 0L) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putLong(HbmBatteryItem.DEFAULT_CHARGE_TAG, charge);
            stack.put("tag", itemTag);
        }
        entity.put("Item", stack);
        entities.add(entity);
        level.put("Entities", entities);
        root.put("Level", level);
        return root;
    }

    private static CompoundTag legacyItemData(String key, int value) {
        CompoundTag tag = new CompoundTag();
        tag.putString("K", key);
        tag.putInt("V", value);
        return tag;
    }

    private static CompoundTag legacyItemDataInsideDataLevelDat() {
        CompoundTag levelDat = new CompoundTag();
        CompoundTag data = new CompoundTag();
        CompoundTag fml = new CompoundTag();
        ListTag itemData = new ListTag();
        itemData.add(legacyItemData("\u0002hbm:item.battery_pack", 4727));
        itemData.add(legacyItemData("\u0002hbm:item.battery_sc", 4728));
        fml.put("ItemData", itemData);
        data.put("FML", fml);
        levelDat.put("Data", data);
        return levelDat;
    }

    private static CompoundTag legacyModItemDataLevelDat() {
        CompoundTag levelDat = new CompoundTag();
        CompoundTag fml = new CompoundTag();
        ListTag modItemData = new ListTag();
        modItemData.add(legacyModItemData("hbm", "item.battery_pack", 6725));
        modItemData.add(legacyModItemData("hbm", "item.battery_sc", 6726));
        modItemData.add(legacyModItemData("hbm", "item.battery_creative", 6727));
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

    private static int setBlockMigrationCachedBuildNumberForTesting(int buildNumber) {
        try {
            Field field = BlockMigrationHelper.class.getDeclaredField("cachedBuildNumber");
            field.setAccessible(true);
            int previous = field.getInt(null);
            field.setInt(null, buildNumber);
            return previous;
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to set BlockMigrationHelper cached build number", exception);
        }
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

    private static HbmLegacyWireNode requireLegacyWireNodeAt(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof HbmLegacyWireNode wireNode) {
            return wireNode;
        }
        throw new AssertionError("No HBM legacy wire node at " + pos);
    }

    private static void assertStoredWireStart(ItemStack stack, BlockPos expected, String label) {
        CompoundTag tag = stack.getTag();
        assertTrue(tag != null, label + " tag exists");
        assertEquals(expected.getX(), tag.getInt("x"), label + " x");
        assertEquals(expected.getY(), tag.getInt("y"), label + " y");
        assertEquals(expected.getZ(), tag.getInt("z"), label + " z");
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

    private static CapacitorBlockEntity requireCapacitor(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CapacitorBlockEntity capacitor) {
            return capacitor;
        }
        throw new AssertionError("No legacy capacitor block entity at " + pos);
    }

    private static List<LegacyCapacitorCase> legacyCapacitorCases() {
        return List.of(
                new LegacyCapacitorCase("capacitor_copper", ModBlocks.CAPACITOR_COPPER, 1_000_000L),
                new LegacyCapacitorCase("capacitor_gold", ModBlocks.CAPACITOR_GOLD, 5_000_000L),
                new LegacyCapacitorCase("capacitor_niobium", ModBlocks.CAPACITOR_NIOBIUM, 25_000_000L),
                new LegacyCapacitorCase("capacitor_tantalium", ModBlocks.CAPACITOR_TANTALIUM, 150_000_000L),
                new LegacyCapacitorCase("capacitor_schrabidate", ModBlocks.CAPACITOR_SCHRABIDATE,
                        50_000_000_000L));
    }

    private record LegacyCapacitorCase(String name, RegistryObject<Block> block, long maxPower) {
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

    private static void assertStirlingProvidesPowerFromHeatSource(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, MachineBatteryBlockEntity battery, StirlingBlockEntity stirling,
            String machineName, int expectedPortCount, int sourceHeat) {
        BlockPos machinePos = stirling.getBlockPos();
        BoilerBlockEntity boiler = placeBoilerHeatSourceBelow(level, machinePos, sourceHeat, machineName);

        assertInputBatteryReceiverSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery input receiver subscribes before " + machineName + " provider transfer");
        StirlingBlockEntity.serverTick(level, machinePos, level.getBlockState(machinePos), stirling);
        long pulledHeat = Math.max(0L, (long) sourceHeat - (long) boiler.getHeatStored());
        long generatedPower = (long) (pulledHeat * (stirling.kind().creative()
                ? 1.0D
                : StirlingBlockEntity.EFFICIENCY));

        HbmEnergyUtil.PortSetSnapshot ports = stirling.inspectEnergyPorts();
        assertEquals(expectedPortCount, ports.totalPorts(),
                machineName + " exposes legacy remote provider energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                machineName + " remote provider port sees the cross-chunk red_cable network: " + ports);
        assertTrue(boiler.getHeatStored() < sourceHeat,
                machineName + " consumed heat from the below-block HeatSource");
        assertTrue(generatedPower > 0L,
                machineName + " produced HE from a below-block HeatSource; pulledHeat=" + pulledHeat
                        + ", boilerHeat=" + boiler.getHeatStored());

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " port cable has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();

        assertTrue(transferred > 0L || battery.getPower() > 0L,
                machineName + " transferred HE during the legacy provider tick or power net update; before=" + beforeUpdate
                        + ", after=" + afterUpdate
                        + ", powerBuffer=" + stirling.powerBuffer()
                        + ", generatedPower=" + generatedPower
                        + ", boilerHeat=" + boiler.getHeatStored());
        assertTrue(battery.getPower() > 0L,
                "machine_battery input received HE from " + machineName
                        + " across cross-chunk red_cable network");
        assertTrue(stirling.powerBuffer() < generatedPower,
                machineName + " spent generated HE into the legacy remote-port network");
        assertEquals(stirling.powerBuffer(), stirling.getEnergyStorage().getPower(),
                machineName + " keeps the 1 HE = 1 FE bridge storage synchronized with powerBuffer");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.providerEntries() >= 1,
                machineName + " registered as provider through its legacy remote port");
        assertTrue(afterTransfer.receiverEntries() >= 1,
                "machine_battery input registered as receiver on cable network");
    }

    private static BoilerBlockEntity placeBoilerHeatSourceBelow(ServerLevel level, BlockPos machinePos,
            int sourceHeat, String machineName) {
        BlockPos boilerPos = machinePos.below();
        level.removeBlock(boilerPos, false);
        level.setBlock(boilerPos, ModBlocks.MACHINE_BOILER.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(boilerPos) instanceof BoilerBlockEntity boiler)) {
            throw new AssertionError("No machine_boiler HeatSource below " + machineName + " at " + boilerPos);
        }
        boiler.addHeat(sourceHeat);
        assertTrue(boiler.getHeatStored() > 0,
                "machine_boiler below " + machineName + " stores heat for Stirling pull");
        return boiler;
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
        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " port cable has a valid power net");
        HbmPowerNet.DebugSnapshot afterReceiverSubscription = powerNet.createDebugSnapshot();
        assertTrue(receiverSubscriptions >= 1 || afterReceiverSubscription.receivers() >= 1,
                machineName + " subscribed as receiver through a legacy remote port; snapshot="
                        + afterReceiverSubscription);
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

    private static List<HbmEnergyUtil.EnergyPort> cyclotronEnergyPorts() {
        return List.of(
                HbmEnergyUtil.EnergyPort.of(3, 0, 1, Direction.EAST),
                HbmEnergyUtil.EnergyPort.of(3, 0, -1, Direction.EAST),
                HbmEnergyUtil.EnergyPort.of(-3, 0, 1, Direction.WEST),
                HbmEnergyUtil.EnergyPort.of(-3, 0, -1, Direction.WEST),
                HbmEnergyUtil.EnergyPort.of(1, 0, 3, Direction.SOUTH),
                HbmEnergyUtil.EnergyPort.of(-1, 0, 3, Direction.SOUTH),
                HbmEnergyUtil.EnergyPort.of(1, 0, -3, Direction.NORTH),
                HbmEnergyUtil.EnergyPort.of(-1, 0, -3, Direction.NORTH));
    }

    private static List<HbmEnergyUtil.EnergyPort> crystallizerEnergyPorts() {
        return List.of(
                HbmEnergyUtil.EnergyPort.of(2, 0, 1, Direction.EAST),
                HbmEnergyUtil.EnergyPort.of(2, 0, -1, Direction.EAST),
                HbmEnergyUtil.EnergyPort.of(-2, 0, 1, Direction.WEST),
                HbmEnergyUtil.EnergyPort.of(-2, 0, -1, Direction.WEST),
                HbmEnergyUtil.EnergyPort.of(1, 0, 2, Direction.SOUTH),
                HbmEnergyUtil.EnergyPort.of(-1, 0, 2, Direction.SOUTH),
                HbmEnergyUtil.EnergyPort.of(1, 0, -2, Direction.NORTH),
                HbmEnergyUtil.EnergyPort.of(-1, 0, -2, Direction.NORTH));
    }

    @SuppressWarnings("unchecked")
    private static Iterable<HbmEnergyUtil.EnergyPort> arcWelderEnergyPorts(ArcWelderBlockEntity arcWelder) {
        try {
            Method method = ArcWelderBlockEntity.class.getDeclaredMethod("connectionEnergyPorts", BlockState.class);
            method.setAccessible(true);
            return (Iterable<HbmEnergyUtil.EnergyPort>) method.invoke(arcWelder, arcWelder.getBlockState());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not inspect energy ports on "
                    + arcWelder.getClass().getSimpleName(), exception);
        }
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

    private static void assertAdjacentReceiverReceivesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, BlockPos machinePos, HbmEnergyReceiver receiver, String machineName,
            long startingPower) {
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before " + machineName + " adjacent receiver transfer");
        if (!HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, machinePos, Direction.WEST, receiver)) {
            refreshEnergyNodeAt(level, portCablePos);
            HbmEnergyNodespace.tick(level);
            assertTrue(HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, machinePos, Direction.WEST,
                    receiver), machineName + " subscribes as receiver on adjacent west side");
        }

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " adjacent cable has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();

        assertTrue(transferred > 0L,
                machineName + " power net transferred HE; before=" + beforeUpdate + ", after=" + afterUpdate);
        assertTrue(receiver.getPower() > 0L,
                machineName + " adjacent receiver side received HE from real red_cable");
        assertTrue(battery.getPower() < startingPower,
                "machine_battery output spent HE into " + machineName + " adjacent receiver side");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.receiverEntries() >= 1,
                machineName + " adjacent side registered as receiver on cable network");
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery output registered as provider on cable network");
    }

    private static void assertAdjacentEnergyBlockReceiverReceivesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, HbmEnergyBlockEntity machine, String machineName, long startingPower) {
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before " + machineName + " adjacent receiver transfer");
        if (!HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, machine.getBlockPos(), Direction.WEST,
                machine.getEnergyStorage())) {
            refreshEnergyNodeAt(level, portCablePos);
            HbmEnergyNodespace.tick(level);
            assertTrue(HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, machine.getBlockPos(), Direction.WEST,
                    machine.getEnergyStorage()), machineName + " subscribes as receiver on adjacent west side");
        }

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " adjacent cable has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();

        assertTrue(transferred > 0L,
                machineName + " power net transferred HE; before=" + beforeUpdate + ", after=" + afterUpdate);
        assertTrue(machine.getPower() > 0L,
                machineName + " adjacent receiver side received HE from real red_cable");
        assertTrue(battery.getPower() < startingPower,
                "machine_battery output spent HE into " + machineName + " adjacent receiver side");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.receiverEntries() >= 1,
                machineName + " adjacent side registered as receiver on cable network");
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery output registered as provider on cable network");
    }

    private static void assertAdjacentEnergyAndFluidReceiverReceivesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, HbmEnergyAndFluidBlockEntity machine, String machineName, long startingPower) {
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        HbmEnergyStorage receiver = energyStorage(machine);
        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes before " + machineName + " adjacent receiver transfer");
        if (!HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, machine.getBlockPos(), Direction.WEST,
                receiver)) {
            refreshEnergyNodeAt(level, portCablePos);
            HbmEnergyNodespace.tick(level);
            assertTrue(HbmEnergyUtil.subscribeReceiverToNeighborNetwork(level, machine.getBlockPos(), Direction.WEST,
                    receiver), machineName + " subscribes as receiver on adjacent west side");
        }

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), machineName + " adjacent cable has a valid power net");
        HbmPowerNet.DebugSnapshot beforeUpdate = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        HbmPowerNet.DebugSnapshot afterUpdate = powerNet.createDebugSnapshot();

        assertTrue(transferred > 0L,
                machineName + " power net transferred HE; before=" + beforeUpdate + ", after=" + afterUpdate);
        assertTrue(machine.getPower() > 0L,
                machineName + " adjacent receiver side received HE from real red_cable");
        assertTrue(battery.getPower() < startingPower,
                "machine_battery output spent HE into " + machineName + " adjacent receiver side");
        HbmEnergyNodespace.Diagnostics afterTransfer = HbmEnergyNodespace.getDiagnostics(level);
        assertTrue(afterTransfer.receiverEntries() >= 1,
                machineName + " adjacent side registered as receiver on cable network");
        assertTrue(afterTransfer.providerEntries() >= 1,
                "machine_battery output registered as provider on cable network");
    }

    private static Direction dfcFacing(BlockState state) {
        return state.hasProperty(DfcMachineBlock.FACING)
                ? state.getValue(DfcMachineBlock.FACING)
                : Direction.NORTH;
    }

    private static <T extends HbmEnergyBlockEntity> T assertPlayerUseOnPlacesAdjacentReceiverWithDiagnostics(
            GameTestHelper helper, RegistryObject<Block> block, Class<T> blockEntityClass, String machineName,
            int zOffset, AdjacentReceiverTicker<T> ticker) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + zOffset);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack machineStack = new ItemStack(block.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, machineStack);

        var placeResult = machineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        T machine = findBlockEntityAroundOrNull(level, supportPos.above(), 5, blockEntityClass, block.get());
        if (machine == null) {
            throw new AssertionError("player useOn " + machineName + " placement returned " + placeResult
                    + " without placing a " + machineName + " core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the " + machineName + " block item action after placing the core");
        BlockPos machinePos = machine.getBlockPos();

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(machine, level.getBlockEntity(machinePos),
                "player-placed " + machineName + " core survives adjacent west receiver-side cable placement");
        ticker.tick(level, machinePos, level.getBlockState(machinePos), machine);
        assertAdjacentEnergyBlockReceiverReceivesPower(level, batteryPos, portCablePos, machine,
                "player-placed " + machineName, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed " + machineName + " adjacent side cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        return machine;
    }

    private static <T extends HbmEnergyAndFluidBlockEntity> T assertPlayerUseOnPlacesAdjacentEnergyAndFluidReceiverWithDiagnostics(
            GameTestHelper helper, RegistryObject<Block> block, Class<T> blockEntityClass, String machineName,
            int zOffset, AdjacentEnergyAndFluidReceiverTicker<T> ticker) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos supportPos = new BlockPos(chunkStartX + 20, anchor.getY() - 1, chunkStartZ + zOffset);
        forceLoadedChunks(level, supportPos.offset(-7, 0, -7), supportPos.offset(7, 5, 7));
        clearBox(level, supportPos.above().offset(-7, 0, -7), supportPos.above(5).offset(7, 0, 7));
        level.setBlock(supportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(supportPos.getX() + 0.5D, supportPos.getY() + 1.0D, supportPos.getZ() + 4.5D);
        player.setYRot(180.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack machineStack = new ItemStack(block.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, machineStack);

        var placeResult = machineStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(supportPos)));

        T machine = findBlockEntityAroundOrNull(level, supportPos.above(), 5, blockEntityClass, block.get());
        if (machine == null) {
            throw new AssertionError("player useOn " + machineName + " placement returned " + placeResult
                    + " without placing a " + machineName + " core around " + supportPos.above()
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the " + machineName + " block item action after placing the core");
        BlockPos machinePos = machine.getBlockPos();

        BlockPos portCablePos = machinePos.west();
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        forceLoadedChunks(level, batteryPos, machinePos);
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);
        assertSame(machine, level.getBlockEntity(machinePos),
                "player-placed " + machineName + " core survives adjacent west receiver-side cable placement");
        ticker.tick(level, machinePos, level.getBlockState(machinePos), machine);
        assertAdjacentEnergyAndFluidReceiverReceivesPower(level, batteryPos, portCablePos, machine,
                "player-placed " + machineName, 100_000L);

        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(),
                "player-placed " + machineName + " adjacent side cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, machinePos, "hbm energy ports " + commandPos(machinePos), 0,
                "Energy ports at " + machinePos.toShortString(), "total=0", "networked=0");

        return machine;
    }

    private static void assertPlayerUseOnPlacesChargerBackEnergySideWithDiagnostics(GameTestHelper helper, int zOffset) {
        ServerLevel level = helper.getLevel();
        BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
        int chunkStartX = anchor.getX() & ~15;
        int chunkStartZ = anchor.getZ() & ~15;
        BlockPos portCablePos = new BlockPos(chunkStartX + 20, anchor.getY(), chunkStartZ + zOffset);
        BlockPos batteryPos = portCablePos.west(4);
        BlockPos firstCablePos = batteryPos.east();
        BlockPos chargerPos = portCablePos.east();
        forceLoadedChunks(level, batteryPos.offset(-2, -1, -2), chargerPos.offset(2, 4, 2));
        clearBox(level, batteryPos.offset(-2, -1, -2), chargerPos.offset(2, 4, 2));
        prepareOutputBatteryAndCableLine(level, batteryPos, firstCablePos, portCablePos, 100_000L);

        var player = helper.makeMockSurvivalPlayer();
        player.setPos(chargerPos.getX() + 0.5D, chargerPos.getY(), chargerPos.getZ() + 0.5D);
        player.setYRot(90.0F);
        player.setXRot(0.0F);
        player.getInventory().clearContent();
        ItemStack chargerStack = new ItemStack(ModBlocks.CHARGER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, chargerStack);

        var placeResult = chargerStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(portCablePos, Direction.EAST)));
        ChargerBlockEntity charger = findBlockEntityAroundOrNull(level, chargerPos, 1,
                ChargerBlockEntity.class, ModBlocks.CHARGER.get());
        if (charger == null) {
            throw new AssertionError("player useOn charger placement returned " + placeResult
                    + " without placing a charger at " + chargerPos
                    + "; hand stack=" + player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        assertTrue(placeResult.consumesAction(),
                "player useOn placement consumes the charger block item action after placing the charger");
        BlockState chargerState = level.getBlockState(chargerPos);
        assertTrue(chargerState.getValue(ChargerBlock.FACING) == Direction.EAST,
                "player-placed charger uses the legacy yaw quadrant rather than the clicked face");
        assertBoxEquals(0.0D, 4.0D / 16.0D, 5.0D / 16.0D,
                4.0D / 16.0D, 12.0D / 16.0D, 11.0D / 16.0D,
                chargerState.getShape(level, chargerPos).bounds(),
                "player-placed charger keeps the legacy meta 5 bounds");
        Direction inputSide = chargerState.getValue(ChargerBlock.FACING).getOpposite();
        assertTrue(inputSide == Direction.WEST,
                "player-placed charger exposes its legacy back input toward red_cable");
        assertTrue(charger.canConnectEnergy(inputSide), "charger back side accepts Energy Mk2 input");
        assertTrue(!charger.canConnectEnergy(inputSide.getOpposite()), "charger front side rejects Energy Mk2 input");

        BlockPos topSupportPos = chargerPos.south(2);
        BlockPos topChargerPos = topSupportPos.above();
        level.setBlock(topSupportPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        player.setPos(topChargerPos.getX() + 0.5D, topChargerPos.getY(), topChargerPos.getZ() + 0.5D);
        player.setYRot(0.0F);
        ItemStack topChargerStack = new ItemStack(ModBlocks.CHARGER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, topChargerStack);
        var topPlaceResult = topChargerStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                blockHit(topSupportPos, Direction.UP)));
        BlockState topChargerState = level.getBlockState(topChargerPos);
        assertTrue(topPlaceResult.consumesAction(),
                "top-click charger placement consumes the charger block item action");
        assertSame(ModBlocks.CHARGER.get(), topChargerState.getBlock(),
                "top-click charger placement creates a charger above the support block");
        assertTrue(topChargerState.getValue(ChargerBlock.FACING) == Direction.NORTH,
                "top-click charger placement still uses legacy yaw-only horizontal facing");
        assertBoxEquals(5.0D / 16.0D, 4.0D / 16.0D, 12.0D / 16.0D,
                11.0D / 16.0D, 12.0D / 16.0D, 1.0D,
                topChargerState.getShape(level, topChargerPos).bounds(),
                "top-click charger keeps the legacy meta 2 bounds instead of a floor shape");

        ChargerBlockEntity.serverTick(level, chargerPos, chargerState, charger);
        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, portCablePos);
        assertTrue(powerNet != null && powerNet.isValid(), "charger back-side cable has a valid power net");
        int expectedLinks = powerNet.createDebugSnapshot().links();
        assertCommandVisibleMessage(level, portCablePos, "hbm energy network " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=",
                "receivers=");
        assertCommandVisibleMessage(level, portCablePos, "hbm energy node " + commandPos(portCablePos),
                expectedLinks, "Energy network at " + portCablePos.toShortString(), "links=");
        assertCommandVisibleMessage(level, chargerPos, "hbm energy ports " + commandPos(chargerPos), 0,
                "Energy ports at " + chargerPos.toShortString(), "total=0", "networked=0");

        helper.succeed();
    }

    @FunctionalInterface
    private interface AdjacentReceiverTicker<T extends HbmEnergyBlockEntity> {
        void tick(ServerLevel level, BlockPos pos, BlockState state, T blockEntity);
    }

    @FunctionalInterface
    private interface AdjacentEnergyAndFluidReceiverTicker<T extends HbmEnergyAndFluidBlockEntity> {
        void tick(ServerLevel level, BlockPos pos, BlockState state, T blockEntity);
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
        assertXrFloorRingRemoteReceiverReceivesPower(level, batteryPos, portCablePos, machinePos, receiver,
                machineName, startingPower, Direction.WEST);
    }

    private static void assertXrFloorRingRemoteReceiverReceivesPower(ServerLevel level, BlockPos batteryPos,
            BlockPos portCablePos, BlockPos machinePos, HbmEnergyReceiver receiver, String machineName,
            long startingPower, Direction conductorSide) {
        MachineBatteryBlockEntity battery = requireMachineBattery(level, batteryPos);
        HbmEnergyUtil.PortSetSnapshot ports = HbmEnergyUtil.inspectPorts(level, machinePos,
                LegacyMultiblockPorts.xrFloorRingEnergyPorts(2));
        assertEquals(12, ports.totalPorts(), machineName + " exposes twelve legacy XR floor-ring energy ports");
        assertTrue(ports.networkedPorts() >= 1,
                machineName + " remote floor-ring port sees the cross-chunk red_cable network");

        assertOutputBatteryProviderSubscribes(level, batteryPos, portCablePos, battery,
                "machine_battery output provider subscribes to adjacent cable network");
        assertTrue(HbmEnergyUtil.subscribeReceiverToNetwork(level, portCablePos, conductorSide, receiver),
                machineName + " receiver subscribes through its floor-ring remote port");

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

    private static void copyMenuDataSlots(AbstractContainerMenu source, AbstractContainerMenu target, int count) {
        source.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu menu, int slotIndex, ItemStack stack) {
            }

            @Override
            public void dataChanged(AbstractContainerMenu menu, int dataSlotIndex, int value) {
                if (dataSlotIndex < count) {
                    target.setData(dataSlotIndex, value);
                }
            }
        });
        source.broadcastFullState();
    }

    private static void assertClasspathResources(String label, String... paths) {
        ClassLoader loader = EnergyMk2GameTests.class.getClassLoader();
        for (String path : paths) {
            if (loader.getResource(path) == null) {
                throw new AssertionError(label + ": missing classpath resource " + path);
            }
        }
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

    private static ArcFurnaceBlockEntity findArcFurnaceAroundOrNull(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockEntity(pos) instanceof ArcFurnaceBlockEntity furnace) {
                        return furnace;
                    }
                }
            }
        }
        return null;
    }

    private static void assertArcFurnaceHasResolvingPowerProxies(ServerLevel level, ArcFurnaceBlockEntity furnace) {
        BlockPos corePos = furnace.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(HorizontalMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        for (BlockPos offset : List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 2, 1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 2, -1, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 1, 2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, 1, -2, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, -2, 0))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_arc_furnace power proxy exists at " + proxyPos);
            assertSame(furnace, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_arc_furnace power proxy resolves to core at " + proxyPos);
        }
    }

    private static SolidifierBlockEntity findSolidifierAroundOrNull(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockEntity(pos) instanceof SolidifierBlockEntity solidifier) {
                        return solidifier;
                    }
                }
            }
        }
        return null;
    }

    private static LiquefactorBlockEntity findLiquefactorAroundOrNull(ServerLevel level, BlockPos center,
            int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockEntity(pos) instanceof LiquefactorBlockEntity liquefactor) {
                        return liquefactor;
                    }
                }
            }
        }
        return null;
    }

    private static void assertFixedSixPortMachineHasResolvingProxies(ServerLevel level, BlockPos corePos,
            BlockEntity core, String machineName) {
        for (BlockPos offset : List.of(
                new BlockPos(0, 3, 0),
                new BlockPos(1, 1, 0),
                new BlockPos(-1, 1, 0),
                new BlockPos(0, 1, 1),
                new BlockPos(0, 1, -1))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    machineName + " fixed-port proxy exists at " + proxyPos);
            assertSame(core, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    machineName + " fixed-port proxy resolves to core at " + proxyPos);
        }
    }

    private static CompressorBlockEntity findCompressorAroundOrNull(ServerLevel level, BlockPos center, int radius,
            Block expectedBlock) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockEntity(pos) instanceof CompressorBlockEntity compressor
                            && level.getBlockState(pos).is(expectedBlock)) {
                        return compressor;
                    }
                }
            }
        }
        return null;
    }

    private static void assertCompressorHasResolvingPowerProxies(ServerLevel level, CompressorBlockEntity compressor,
            boolean compact, String machineName) {
        BlockPos corePos = compressor.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(HorizontalMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        List<BlockPos> offsets = compact ? List.of(
                LegacyMultiblockOffsets.relative(facing, rot, 0, 3, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 0, -3, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 1, 1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, 1, -1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 1),
                LegacyMultiblockOffsets.relative(facing, rot, -1, -1, 1))
                : List.of(
                        LegacyMultiblockOffsets.relative(facing, rot, -1, 0, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, 1, 0),
                        LegacyMultiblockOffsets.relative(facing, rot, 0, -1, 0));
        for (BlockPos offset : offsets) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    machineName + " power/fluid proxy exists at " + proxyPos);
            assertSame(compressor, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    machineName + " power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static StirlingBlockEntity findStirlingAroundOrNull(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockEntity(pos) instanceof StirlingBlockEntity stirling) {
                        return stirling;
                    }
                }
            }
        }
        return null;
    }

    private static void assertStirlingHasResolvingPowerProxies(ServerLevel level, StirlingBlockEntity stirling) {
        BlockPos corePos = stirling.getBlockPos();
        for (BlockPos offset : LegacyMultiblockOffsets.cardinal(1)) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_stirling power proxy exists at " + proxyPos);
            assertSame(stirling, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_stirling power proxy resolves to core at " + proxyPos);
        }
    }

    private static WoodBurnerBlockEntity findWoodBurnerAroundOrNull(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (level.getBlockEntity(pos) instanceof WoodBurnerBlockEntity woodBurner) {
                        return woodBurner;
                    }
                }
            }
        }
        return null;
    }

    private static void assertWoodBurnerHasResolvingPowerFluidProxies(ServerLevel level,
            WoodBurnerBlockEntity woodBurner) {
        BlockPos corePos = woodBurner.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(HorizontalMachineBlock.FACING);
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        for (BlockPos offset : List.of(
                LegacyMultiblockOffsets.relative(facing, -1, 0, 0),
                LegacyMultiblockOffsets.relative(facing, rot, -1, 1, 0))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_wood_burner power/fluid proxy exists at " + proxyPos);
            assertSame(woodBurner, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_wood_burner power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertElectrolyserHasResolvingPowerFluidProxies(ServerLevel level,
            ElectrolyserBlockEntity electrolyser) {
        BlockPos corePos = electrolyser.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        for (int sideOffset : List.of(0, 1, -1)) {
            for (int forwardOffset : List.of(-5, 5)) {
                BlockPos proxyPos = corePos.offset(LegacyMultiblockOffsets.relative(facing, side,
                        forwardOffset, sideOffset, 0));
                assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                        "player-placed machine_electrolyser power/fluid proxy exists at " + proxyPos);
                assertSame(electrolyser, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                        "player-placed machine_electrolyser power/fluid proxy resolves to core at " + proxyPos);
            }
        }
    }

    private static void assertGasCentHasResolvingPowerFluidProxies(ServerLevel level,
            GasCentBlockEntity gasCent) {
        BlockPos corePos = gasCent.getBlockPos();
        for (BlockPos offset : List.of(new BlockPos(0, 1, 0), new BlockPos(0, 2, 0),
                new BlockPos(0, 3, 0))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_gascent power/fluid proxy exists at " + proxyPos);
            assertSame(gasCent, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_gascent power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertProcessingCentrifugeHasResolvingPowerFluidProxies(ServerLevel level,
            ProcessingMachineBlockEntity centrifuge) {
        BlockPos corePos = centrifuge.getBlockPos();
        for (BlockPos offset : List.of(new BlockPos(0, 1, 0), new BlockPos(0, 2, 0),
                new BlockPos(0, 3, 0))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_centrifuge power/fluid proxy exists at " + proxyPos);
            assertSame(centrifuge, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_centrifuge power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertProcessingCrystallizerHasResolvingPowerFluidProxies(ServerLevel level,
            ProcessingMachineBlockEntity crystallizer) {
        BlockPos corePos = crystallizer.getBlockPos();
        for (BlockPos offset : List.of(new BlockPos(1, 0, 1), new BlockPos(1, 0, -1),
                new BlockPos(-1, 0, 1), new BlockPos(-1, 0, -1))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_crystallizer power/fluid proxy exists at " + proxyPos);
            assertSame(crystallizer, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_crystallizer power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertDeuteriumTowerHasResolvingPowerFluidProxies(ServerLevel level,
            DeuteriumTowerBlockEntity tower) {
        BlockPos corePos = tower.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        for (BlockPos offset : List.of(
                LegacyMultiblockOffsets.relative(facing, side, -1, -1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, -1, 0),
                LegacyMultiblockOffsets.relative(facing, side, -1, 0, 0))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_deuterium_tower power/fluid proxy exists at " + proxyPos);
            assertSame(tower, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_deuterium_tower power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertOreSlopperHasResolvingPowerFluidProxies(ServerLevel level,
            OreSlopperBlockEntity oreSlopper) {
        BlockPos corePos = oreSlopper.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        for (BlockPos offset : List.of(
                LegacyMultiblockOffsets.relative(facing, side, 3, 0, 0),
                LegacyMultiblockOffsets.relative(facing, side, -3, 0, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 0, -1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 2, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, 2, -1, 0),
                LegacyMultiblockOffsets.relative(facing, side, -2, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, -2, -1, 0))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_ore_slopper power/fluid proxy exists at " + proxyPos);
            assertSame(oreSlopper, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_ore_slopper power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertElectricHeaterHasResolvingPowerProxy(ServerLevel level,
            ElectricHeaterBlockEntity heater) {
        BlockPos corePos = heater.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        BlockPos proxyPos = corePos.relative(facing, 2);
        assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                "player-placed heater_electric power proxy exists at " + proxyPos);
        assertSame(heater, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                "player-placed heater_electric power proxy resolves to core at " + proxyPos);
    }

    private static void assertPoweredCondenserHasResolvingPowerFluidProxies(ServerLevel level,
            PoweredCondenserBlockEntity condenser) {
        BlockPos corePos = condenser.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        for (BlockPos offset : List.of(
                LegacyMultiblockOffsets.relative(facing, side, 0, 3, 1),
                LegacyMultiblockOffsets.relative(facing, side, 0, -3, 1),
                LegacyMultiblockOffsets.relative(facing, side, 1, 1, 1),
                LegacyMultiblockOffsets.relative(facing, side, 1, -1, 1),
                LegacyMultiblockOffsets.relative(facing, side, -1, 1, 1),
                LegacyMultiblockOffsets.relative(facing, side, -1, -1, 1))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_condenser_powered power/fluid proxy exists at " + proxyPos);
            assertSame(condenser, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                "player-placed machine_condenser_powered power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertPyroOvenHasResolvingPowerFluidProxies(ServerLevel level,
            PyroOvenBlockEntity pyroOven) {
        BlockPos corePos = pyroOven.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyDownSide(facing);
        List<BlockPos> offsets = new ArrayList<>(
                LegacyMultiblockOffsets.lineAlongFacing(facing, side, -2, 2, 2, 0));
        offsets.add(LegacyMultiblockOffsets.relative(facing, side, 0, -1, 2));
        for (BlockPos offset : offsets) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_pyrooven power/fluid proxy exists at " + proxyPos);
            assertSame(pyroOven, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_pyrooven power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertPurexHasResolvingPowerFluidProxies(ServerLevel level,
            LegacyGenericSelectorMachineBlockEntity purex) {
        assertGenericSelectorHasResolvingPowerFluidProxies(level, purex, "machine_purex", 2);
    }

    private static void assertGenericSelectorHasResolvingPowerFluidProxies(ServerLevel level,
            LegacyGenericSelectorMachineBlockEntity machine, String machineName, int radius) {
        BlockPos corePos = machine.getBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x) != radius && Math.abs(z) != radius) {
                    continue;
                }
                BlockPos proxyPos = corePos.offset(x, 0, z);
                assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                        "player-placed " + machineName + " power/fluid proxy exists at " + proxyPos);
                assertSame(machine, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                        "player-placed " + machineName + " power/fluid proxy resolves to core at " + proxyPos);
            }
        }
    }

    private static void assertSolderingStationHasResolvingPowerFluidProxies(ServerLevel level,
            SolderingStationBlockEntity station) {
        BlockPos corePos = station.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        for (BlockPos offset : List.of(
                LegacyMultiblockOffsets.relative(facing, side, 0, 1, 0),
                LegacyMultiblockOffsets.relative(facing, side, -1, 0, 0),
                LegacyMultiblockOffsets.relative(facing, side, -1, 1, 0))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_soldering_station power/fluid proxy exists at " + proxyPos);
            assertSame(station, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_soldering_station power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertExcavatorHasResolvingPowerFluidProxies(ServerLevel level,
            ExcavatorBlockEntity excavator) {
        BlockPos corePos = excavator.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        for (BlockPos offset : List.of(
                LegacyMultiblockOffsets.relative(facing, side, 3, 1, 1),
                LegacyMultiblockOffsets.relative(facing, side, 3, -1, 1),
                LegacyMultiblockOffsets.relative(facing, side, 0, 3, 1),
                LegacyMultiblockOffsets.relative(facing, side, 0, -3, 1))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_excavator power/fluid proxy exists at " + proxyPos);
            assertSame(excavator, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_excavator power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertMiningLaserHasResolvingPowerFluidProxies(ServerLevel level,
            MiningLaserBlockEntity laser) {
        BlockPos corePos = laser.getBlockPos();
        for (BlockPos offset : List.of(
                new BlockPos(1, 0, 0),
                new BlockPos(-1, 0, 0),
                new BlockPos(0, 0, 1),
                new BlockPos(0, 0, -1),
                new BlockPos(0, 1, 0))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    "player-placed machine_mining_laser power/fluid proxy exists at " + proxyPos);
            assertSame(laser, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    "player-placed machine_mining_laser power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static void assertFactoryHasResolvingPowerFluidProxies(ServerLevel level, BlockEntity factory,
            String machineName) {
        BlockPos corePos = factory.getBlockPos();
        Direction facing = level.getBlockState(corePos).getValue(LegacyVisibleMultiblockMachineBlock.FACING);
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        for (BlockPos offset : LegacyMultiblockOffsets.combine(
                LegacyMultiblockOffsets.squarePerimeter(2),
                LegacyMultiblockOffsets.lineAlongFacing(facing, side, -2, 2, 2, 2),
                LegacyMultiblockOffsets.lineAlongFacing(facing, side, -2, 2, -2, 2))) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    machineName + " power/fluid proxy exists at " + proxyPos);
            assertSame(factory, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    machineName + " power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static <T extends BlockEntity> T findBlockEntityAroundOrNull(ServerLevel level, BlockPos center,
            int radius, Class<T> type, Block expectedBlock) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (type.isInstance(blockEntity) && level.getBlockState(pos).is(expectedBlock)) {
                        return type.cast(blockEntity);
                    }
                }
            }
        }
        return null;
    }

    private static void assertPlayerPlacedProviderMachineHasResolvingProxies(ServerLevel level, BlockEntity core,
            String machineName, List<BlockPos> offsets) {
        BlockPos corePos = core.getBlockPos();
        for (BlockPos offset : offsets) {
            BlockPos proxyPos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                    machineName + " power/fluid proxy exists at " + proxyPos);
            assertSame(core, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                    machineName + " power/fluid proxy resolves to core at " + proxyPos);
        }
    }

    private static AssemblyMachineBlockEntity findAssemblyMachineAroundOrNull(ServerLevel level, BlockPos center,
            int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                if (level.getBlockEntity(pos) instanceof AssemblyMachineBlockEntity assembler) {
                    return assembler;
                }
            }
        }
        return null;
    }

    private static ChemicalPlantBlockEntity findChemicalPlantAroundOrNull(ServerLevel level, BlockPos center,
            int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                if (level.getBlockEntity(pos) instanceof ChemicalPlantBlockEntity chemicalPlant) {
                    return chemicalPlant;
                }
            }
        }
        return null;
    }

    private static void assertXrFloorRingMachineHasResolvingProxies(ServerLevel level, BlockPos corePos,
            BlockEntity core, String machineName) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                BlockPos proxyPos = corePos.offset(dx, 0, dz);
                assertTrue(level.getBlockEntity(proxyPos) instanceof MultiblockDummyBlockEntity,
                        machineName + " floor-ring proxy exists at " + proxyPos);
                assertSame(core, MultiblockHelper.resolveCoreBlockEntity(level, proxyPos),
                        machineName + " floor-ring proxy resolves to core at " + proxyPos);
            }
        }
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

    private static LaunchTableBlockEntity findLaunchTableAroundOrNull(ServerLevel level, BlockPos center,
            int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                if (level.getBlockEntity(pos) instanceof LaunchTableBlockEntity table) {
                    return table;
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

    private static void assertLaunchTableHasResolvingPortProxies(ServerLevel level,
            LaunchTableBlockEntity table) {
        BlockPos corePos = table.getBlockPos();
        for (BlockPos offset : List.of(new BlockPos(4, 0, 4), new BlockPos(-4, 0, 4),
                new BlockPos(4, 0, -4), new BlockPos(-4, 0, -4))) {
            BlockPos pos = corePos.offset(offset);
            assertTrue(level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity,
                    "player-placed launch_table port proxy exists at " + pos);
            assertSame(table, MultiblockHelper.resolveCoreBlockEntity(level, pos),
                    "player-placed launch_table port proxy resolves to core at " + pos);
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

    private static BlockHitResult blockHit(BlockPos pos, Direction direction) {
        return new BlockHitResult(Vec3.atCenterOf(pos), direction, pos, false);
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

    private static void assertBoxEquals(double minX, double minY, double minZ, double maxX, double maxY,
            double maxZ, AABB actual, String label) {
        if (Double.compare(minX, actual.minX) != 0
                || Double.compare(minY, actual.minY) != 0
                || Double.compare(minZ, actual.minZ) != 0
                || Double.compare(maxX, actual.maxX) != 0
                || Double.compare(maxY, actual.maxY) != 0
                || Double.compare(maxZ, actual.maxZ) != 0) {
            throw new AssertionError(label + ": expected AABB["
                    + minX + ", " + minY + ", " + minZ + "] -> ["
                    + maxX + ", " + maxY + ", " + maxZ + "] but got " + actual);
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

    private static void assertFalse(boolean value, String label) {
        if (value) {
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
