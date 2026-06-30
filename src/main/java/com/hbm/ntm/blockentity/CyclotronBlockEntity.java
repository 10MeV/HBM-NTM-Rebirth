package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.CyclotronMenu;
import com.hbm.ntm.multiblock.LegacyProxyDelegateProvider;
import com.hbm.ntm.recipe.CyclotronRecipeRuntime;
import com.hbm.ntm.recipe.CyclotronRecipeRuntime.CyclotronRecipe;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CyclotronBlockEntity extends HbmFluidNetworkBlockEntity implements MenuProvider, HbmEnergyReceiver,
        HbmStandardFluidTransceiver, LegacyUpgradeInfoProvider, LegacyProxyDelegateProvider {
    public static final int SLOT_PARTICLE_START = 0;
    public static final int SLOT_TARGET_START = 3;
    public static final int SLOT_OUTPUT_START = 6;
    public static final int SLOT_BATTERY = 9;
    public static final int SLOT_UPGRADE_0 = 10;
    public static final int SLOT_UPGRADE_1 = 11;
    public static final int SLOT_COUNT = 12;
    public static final int MAX_POWER = 100_000_000;
    public static final int BASE_CONSUMPTION = 1_000_000;
    public static final int DURATION = 690;

    private static final String TAG_ITEMS = "items";
    private static final String TAG_POWER = "power";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_PLUGS = "plugs";
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.EFFECT, 3);

    private final HbmEnergyStorage energy = new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot >= SLOT_PARTICLE_START && slot < SLOT_PARTICLE_START + 3) {
                return CyclotronRecipeRuntime.isValidParticle(stack);
            }
            if (slot >= SLOT_TARGET_START && slot < SLOT_TARGET_START + 3) {
                return CyclotronRecipeRuntime.isValidInput(stack);
            }
            if (slot == SLOT_BATTERY) {
                return HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
            }
            if (slot == SLOT_UPGRADE_0 || slot == SLOT_UPGRADE_1) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
            return false;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + 3
                    ? super.extractItem(slot, amount, simulate)
                    : ItemStack.EMPTY;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(CoreItemHandler::new);
    private final LazyOptional<IItemHandler>[] laneItemHandlers;
    private final LazyOptional<IEnergyStorage> energyHandler =
            LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
    private final IFluidHandler proxyFluidHandler;
    private final ICapabilityProvider[] proxyDelegates = new ICapabilityProvider[3];

    private int progress;
    private byte plugs;

    @SuppressWarnings("unchecked")
    public CyclotronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CYCLOTRON.get(), pos, state,
                List.of(new HbmFluidTank(HbmFluids.WATER, 32_000),
                        new HbmFluidTank(HbmFluids.SPENTSTEAM, 32_000),
                        new HbmFluidTank(HbmFluids.AMAT, 8_000)));
        getAllTanks().get(0).setTankType(HbmFluids.WATER);
        getAllTanks().get(1).setTankType(HbmFluids.SPENTSTEAM);
        getAllTanks().get(2).setTankType(HbmFluids.AMAT);
        proxyFluidHandler = new ForgeFluidHandlerAdapter(List.of(water()), List.of(spentSteam(), amat()), 0, true, true,
                this::onFluidContentsChanged);
        laneItemHandlers = new LazyOptional[3];
        for (int lane = 0; lane < 3; lane++) {
            int captured = lane;
            laneItemHandlers[lane] = LazyOptional.of(() -> new LaneItemHandler(captured));
            proxyDelegates[lane] = new ProxyCapabilityDelegate(lane);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CyclotronBlockEntity cyclotron) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, cyclotron);
        long oldPower = cyclotron.energy.getPower();
        int oldProgress = cyclotron.progress;
        int oldWater = cyclotron.water().getFill();
        int oldSteam = cyclotron.spentSteam().getFill();
        int oldAmat = cyclotron.amat().getFill();

        HbmEnergyUtil.chargeStorageFromItem(cyclotron.items.getStackInSlot(SLOT_BATTERY), cyclotron.energy,
                cyclotron.energy.getReceiverSpeed());
        if (level.getGameTime() % 20L == 0L) {
            HbmEnergyUtil.subscribeReceiverToPorts(level, pos, cyclotron.energyPorts(), cyclotron);
        }

        if (cyclotron.canProcess()) {
            cyclotron.progress += cyclotron.getSpeed();
            cyclotron.energy.setPower(cyclotron.energy.getPower() - cyclotron.getConsumption());
            int coolant = cyclotron.getCoolantConsumption();
            cyclotron.water().drain(coolant, false);
            cyclotron.spentSteam().fill(HbmFluids.SPENTSTEAM, coolant, 0, false);
            if (cyclotron.progress >= DURATION) {
                cyclotron.process();
                cyclotron.progress = 0;
            }
        } else {
            cyclotron.progress = 0;
        }

        cyclotron.trySendFluids();
        boolean changed = oldPower != cyclotron.energy.getPower()
                || oldProgress != cyclotron.progress
                || oldWater != cyclotron.water().getFill()
                || oldSteam != cyclotron.spentSteam().getFill()
                || oldAmat != cyclotron.amat().getFill();
        if (changed) {
            cyclotron.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        cyclotron.networkPackNT(25);
    }

    private boolean canProcess() {
        if (energy.getPower() < getConsumption()) {
            return false;
        }
        int coolant = getCoolantConsumption();
        if (water().getFill() < coolant || spentSteam().getFill() + coolant > spentSteam().getMaxFill()) {
            return false;
        }
        for (int lane = 0; lane < 3; lane++) {
            Optional<CyclotronRecipe> recipe = recipe(lane);
            if (recipe.isPresent() && canAcceptOutput(lane, recipe.get().output())) {
                return true;
            }
        }
        return false;
    }

    private void process() {
        for (int lane = 0; lane < 3; lane++) {
            Optional<CyclotronRecipe> recipe = recipe(lane);
            if (recipe.isEmpty() || !canAcceptOutput(lane, recipe.get().output())) {
                continue;
            }
            int particleSlot = SLOT_PARTICLE_START + lane;
            int targetSlot = SLOT_TARGET_START + lane;
            int outputSlot = SLOT_OUTPUT_START + lane;
            items.extractItem(particleSlot, 1, false);
            items.extractItem(targetSlot, 1, false);
            ItemStack existing = items.getStackInSlot(outputSlot);
            ItemStack output = recipe.get().output().copy();
            if (existing.isEmpty()) {
                items.setStackInSlot(outputSlot, output);
            } else {
                existing.grow(output.getCount());
                items.setStackInSlot(outputSlot, existing);
            }
            amat().fill(HbmFluids.AMAT, recipe.get().antimatterMb(), 0, false);
        }
    }

    private Optional<CyclotronRecipe> recipe(int lane) {
        return CyclotronRecipeRuntime.find(items.getStackInSlot(SLOT_PARTICLE_START + lane),
                items.getStackInSlot(SLOT_TARGET_START + lane));
    }

    private boolean canAcceptOutput(int lane, ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT_START + lane);
        return existing.isEmpty()
                || (ItemStack.isSameItemSameTags(existing, output)
                        && existing.getCount() + output.getCount() <= existing.getMaxStackSize());
    }

    private void trySendFluids() {
        if (!spentSteam().isEmpty()) {
            tryProvideFluidToPorts(HbmFluids.SPENTSTEAM, 0, this);
        }
        if (!amat().isEmpty()) {
            tryProvideFluidToPorts(HbmFluids.AMAT, 0, this);
        }
    }

    public int getSpeed() {
        return upgradeLevels().getLevel(UpgradeType.SPEED) + 1;
    }

    public int getConsumption() {
        return BASE_CONSUMPTION - 100_000 * upgradeLevels().getLevel(UpgradeType.POWER);
    }

    public int getCoolantConsumption() {
        return 500 / (upgradeLevels().getLevel(UpgradeType.EFFECT) + 1) * getSpeed();
    }

    private LegacyMachineUpgradeManager.Levels upgradeLevels() {
        return LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE_0, SLOT_UPGRADE_1, VALID_UPGRADES);
    }

    public void setPlug(int index) {
        if (index < 0 || index > 3) {
            return;
        }
        plugs = (byte) (plugs | (1 << index));
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public boolean getPlug(int index) {
        return index >= 0 && index <= 3 && (plugs & (1 << index)) != 0;
    }

    public boolean hasAllPlugs() {
        return (plugs & 0x0F) == 0x0F;
    }

    @Nullable
    public static Item itemForPlug(int index) {
        return switch (index) {
            case 0 -> legacyItem("powder_balefire");
            case 1 -> legacyItem("book_of_");
            case 2 -> legacyItem("diamond_gavel");
            case 3 -> legacyItem("coin_maskman");
            default -> null;
        };
    }

    @Nullable
    private static Item legacyItem(String name) {
        return ModItems.legacyItem(name) == null ? null : ModItems.legacyItem(name).get();
    }

    public boolean tryInstallPlug(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            Item item = itemForPlug(i);
            if (item != null && stack.is(item) && !getPlug(i)) {
                stack.shrink(1);
                setPlug(i);
                if (level != null) {
                    LegacySoundPlayer.playSoundEffect(level, worldPosition, "hbm:item.upgradePlug",
                            net.minecraft.sounds.SoundSource.BLOCKS, 1.5F, 1.0F);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public ICapabilityProvider getLegacyProxyDelegate(BlockPos proxyPos) {
        int lane = laneForProxy(proxyPos);
        return lane >= 0 ? proxyDelegates[lane] : null;
    }

    private int laneForProxy(BlockPos proxyPos) {
        BlockPos rel = proxyPos.subtract(worldPosition);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction rot = direction.getClockWise();
            BlockPos center = new BlockPos(direction.getStepX() * 2, 0, direction.getStepZ() * 2);
            if (rel.equals(center.offset(rot.getStepX(), 0, rot.getStepZ()))) {
                return 0;
            }
            if (rel.equals(center)) {
                return 1;
            }
            if (rel.equals(center.offset(-rot.getStepX(), 0, -rot.getStepZ()))) {
                return 2;
            }
        }
        return -1;
    }

    private List<FluidPort> fluidPorts() {
        return List.of(
                FluidPort.of(3, 0, 1, Direction.EAST),
                FluidPort.of(3, 0, -1, Direction.EAST),
                FluidPort.of(-3, 0, 1, Direction.WEST),
                FluidPort.of(-3, 0, -1, Direction.WEST),
                FluidPort.of(1, 0, 3, Direction.SOUTH),
                FluidPort.of(-1, 0, 3, Direction.SOUTH),
                FluidPort.of(1, 0, -3, Direction.NORTH),
                FluidPort.of(-1, 0, -3, Direction.NORTH));
    }

    private List<HbmEnergyUtil.EnergyPort> energyPorts() {
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

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts();
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == HbmFluids.WATER;
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == HbmFluids.SPENTSTEAM || type == HbmFluids.AMAT;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null ? HbmFluidSideMode.BOTH : HbmFluidSideMode.NONE;
    }

    @Override
    protected boolean showsLegacyFluidLookOverlay() {
        return false;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(water());
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(spentSteam(), amat());
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidTransceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmEnergyStorage getEnergyStorage() {
        return energy;
    }

    public HbmFluidTank water() {
        return getAllTanks().get(0);
    }

    public HbmFluidTank spentSteam() {
        return getAllTanks().get(1);
    }

    public HbmFluidTank amat() {
        return getAllTanks().get(2);
    }

    public int getProgress() {
        return progress;
    }

    public long getPower() {
        return energy.getPower();
    }

    @Override
    public void setPower(long power) {
        energy.setPower(power);
    }

    @Override
    public long getMaxPower() {
        return energy.getMaxPower();
    }

    @Override
    public long getReceiverSpeed() {
        return energy.getReceiverSpeed();
    }

    public int getPlugs() {
        return plugs & 0xFF;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Map<UpgradeType, Integer> getValidUpgrades() {
        return VALID_UPGRADES;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.cyclotron", "Cyclotron");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CyclotronMenu(containerId, inventory, this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 4, 3));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putByte(TAG_PLUGS, plugs);
        water().writeToNbt(tag, "t0");
        spentSteam().writeToNbt(tag, "t1");
        amat().writeToNbt(tag, "t2");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadItems(tag);
        energy.setPower(tag.getLong(TAG_POWER));
        progress = tag.getInt(TAG_PROGRESS);
        plugs = tag.getByte(TAG_PLUGS);
        if (tag.contains("t0")) {
            water().readFromNbt(tag, "t0");
        }
        if (tag.contains("t1")) {
            spentSteam().readFromNbt(tag, "t1");
        }
        if (tag.contains("t2")) {
            amat().readFromNbt(tag, "t2");
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
        for (LazyOptional<IItemHandler> laneHandler : laneItemHandlers) {
            laneHandler.invalidate();
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private void loadItems(CompoundTag tag) {
        if (tag.contains(HbmInventoryMenuHelper.LEGACY_ITEMS_TAG, Tag.TAG_LIST)
                || tag.contains("Items", Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
    }

    private class ProxyCapabilityDelegate implements ICapabilityProvider {
        private final int lane;

        private ProxyCapabilityDelegate(int lane) {
            this.lane = lane;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
            if (capability == ForgeCapabilities.ITEM_HANDLER) {
                return laneItemHandlers[lane].cast();
            }
            if (capability == ForgeCapabilities.ENERGY) {
                return energyHandler.cast();
            }
            if (capability == ForgeCapabilities.FLUID_HANDLER) {
                return LazyOptional.of(() -> proxyFluidHandler).cast();
            }
            return LazyOptional.empty();
        }
    }

    private class LaneItemHandler implements IItemHandler {
        private final int lane;

        private LaneItemHandler(int lane) {
            this.lane = lane;
        }

        @Override
        public int getSlots() {
            return 5;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapSlot(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = mapSlot(slot);
            return mapped == SLOT_PARTICLE_START + lane || mapped == SLOT_TARGET_START + lane
                    ? items.insertItem(mapped, stack, simulate)
                    : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapSlot(slot);
            return mapped >= SLOT_OUTPUT_START && mapped < SLOT_OUTPUT_START + 3
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapSlot(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = mapSlot(slot);
            return mapped >= 0 && items.isItemValid(mapped, stack);
        }

        private int mapSlot(int slot) {
            return switch (slot) {
                case 0 -> SLOT_PARTICLE_START + lane;
                case 1 -> SLOT_TARGET_START + lane;
                case 2 -> SLOT_OUTPUT_START;
                case 3 -> SLOT_OUTPUT_START + 1;
                case 4 -> SLOT_OUTPUT_START + 2;
                default -> -1;
            };
        }
    }

    private class CoreItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 3;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapSlot(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapSlot(slot);
            return mapped >= SLOT_OUTPUT_START && mapped < SLOT_OUTPUT_START + 3
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapSlot(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        private int mapSlot(int slot) {
            return switch (slot) {
                case 0 -> SLOT_OUTPUT_START;
                case 1 -> SLOT_OUTPUT_START + 1;
                case 2 -> SLOT_OUTPUT_START + 2;
                default -> -1;
            };
        }
    }
}
