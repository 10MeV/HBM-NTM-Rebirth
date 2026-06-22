package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.WatzEndBlock;
import com.hbm.ntm.entity.projectile.ShrapnelEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidThermalExchange;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.item.WatzPelletItem;
import com.hbm.ntm.menu.WatzReactorMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.recipe.WatzFuelRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.AchievementHandler;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WatzReactorBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmLegacyControlReceiver {
    public static final int PELLET_SLOT_COUNT = 24;
    public static final int SLOT_COUNT = PELLET_SLOT_COUNT;
    public static final int TANK_CAPACITY = 64_000;
    public static final double USE_DISTANCE_SQR = 128.0D;

    private static final String TAG_ITEMS = "items";
    private static final String TAG_LOCKS = "locks";

    private final HbmFluidTank coolantTank;
    private final HbmFluidTank hotCoolantTank;
    private final HbmFluidTank mudTank;
    private final HbmFluidTank coolantDisplayTank;
    private final HbmFluidTank hotCoolantDisplayTank;
    private final HbmFluidTank mudDisplayTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < PELLET_SLOT_COUNT && isValidPelletForSlot(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private final ItemStack[] locks = new ItemStack[SLOT_COUNT];
    private int heat;
    private double fluxLastBase;
    private double fluxLastReaction;
    private double fluxDisplay;
    private boolean on;
    private boolean locked;
    private boolean suppressCoreDropsOnRemoval;

    public WatzReactorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.COOLANT, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.COOLANT_HOT, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.WATZ, TANK_CAPACITY));
    }

    private WatzReactorBlockEntity(BlockPos pos, BlockState state, HbmFluidTank coolantTank,
            HbmFluidTank hotCoolantTank, HbmFluidTank mudTank) {
        super(ModBlockEntities.WATZ_REACTOR.get(), pos, state, List.of(coolantTank, hotCoolantTank, mudTank));
        this.coolantTank = coolantTank;
        this.hotCoolantTank = hotCoolantTank;
        this.mudTank = mudTank;
        this.coolantDisplayTank = new HbmFluidTank(HbmFluids.COOLANT, TANK_CAPACITY);
        this.hotCoolantDisplayTank = new HbmFluidTank(HbmFluids.COOLANT_HOT, TANK_CAPACITY);
        this.mudDisplayTank = new HbmFluidTank(HbmFluids.WATZ, TANK_CAPACITY);
        copyTank(coolantTank, coolantDisplayTank);
        copyTank(hotCoolantTank, hotCoolantDisplayTank);
        copyTank(mudTank, mudDisplayTank);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WatzReactorBlockEntity reactor) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, reactor);
        if (reactor.hasWatzAbove(level)) {
            return;
        }
        boolean changed = reactor.tickTopSegment(level);
        if (changed || level.getGameTime() % 20L == 0L) {
            reactor.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return !isRemoved() && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= USE_DISTANCE_SQR;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains("lock")) {
            toggleLocks();
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getCoolantTank() {
        return coolantTank;
    }

    public HbmFluidTank getHotCoolantTank() {
        return hotCoolantTank;
    }

    public HbmFluidTank getMudTank() {
        return mudTank;
    }

    public HbmFluidTank getCoolantDisplayTank() {
        return coolantDisplayTank;
    }

    public HbmFluidTank getHotCoolantDisplayTank() {
        return hotCoolantDisplayTank;
    }

    public HbmFluidTank getMudDisplayTank() {
        return mudDisplayTank;
    }

    public int getHeat() {
        return heat;
    }

    public int getFluxDisplayScaled() {
        return (int) Math.round(fluxDisplay * 1000.0D);
    }

    public double getFluxDisplay() {
        return fluxDisplay;
    }

    public boolean isOn() {
        return on;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean suppressCoreDropsOnRemoval() {
        return suppressCoreDropsOnRemoval;
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>(HbmInventoryMenuHelper.clearToDrops(items));
        drops.add(new ItemStack(ModBlocks.WATZ_END.get(), 48));
        RegistryObject<net.minecraft.world.item.Item> duraBolt = ModItems.legacyItem("bolt_dura_steel");
        if (duraBolt != null) {
            drops.add(new ItemStack(duraBolt.get(), 64));
            drops.add(new ItemStack(duraBolt.get(), 64));
            drops.add(new ItemStack(duraBolt.get(), 64));
        }
        drops.add(new ItemStack(ModBlocks.WATZ_ELEMENT.get(), 36));
        drops.add(new ItemStack(ModBlocks.WATZ_COOLER.get(), 26));
        drops.add(new ItemStack(ModBlocks.STRUCT_WATZ_CORE.get()));
        return drops;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.watz", "Watz Power Plant");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new WatzReactorMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(coolantTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(hotCoolantTank, mudTank);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(coolantTank, hotCoolantTank, mudTank);
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
    public long getProviderSpeed(FluidType type, int pressure) {
        if (type == HbmFluids.COOLANT_HOT) {
            return Math.max(1L, hotCoolantTank.getFill());
        }
        if (type == HbmFluids.WATZ) {
            return Math.max(1L, mudTank.getFill());
        }
        return 1L;
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
        return type == HbmFluids.COOLANT;
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return (type == HbmFluids.COOLANT_HOT && hotCoolantTank.getFill() > 0)
                || (type == HbmFluids.WATZ && mudTank.getFill() > 0);
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return side == Direction.DOWN ? List.of() : List.of(coolantTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return side == Direction.DOWN ? List.of(hotCoolantTank, mudTank) : List.of();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        coolantTank.writeToNbt(tag, "t0");
        hotCoolantTank.writeToNbt(tag, "t1");
        mudTank.writeToNbt(tag, "t2");
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        saveLocks(tag);
        tag.putInt("heat", heat);
        tag.putDouble("lastFluxB", fluxLastBase);
        tag.putDouble("lastFluxR", fluxLastReaction);
        tag.putBoolean("isLocked", locked);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (hasTankTag(tag, "t0")) {
            coolantTank.readFromNbt(tag, "t0");
        }
        if (hasTankTag(tag, "t1")) {
            hotCoolantTank.readFromNbt(tag, "t1");
        }
        if (hasTankTag(tag, "t2")) {
            mudTank.readFromNbt(tag, "t2");
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        loadLocks(tag);
        heat = tag.getInt("heat");
        fluxLastBase = readSavedFlux(tag, "lastFluxB");
        fluxLastReaction = readSavedFlux(tag, "lastFluxR");
        fluxDisplay = fluxLastBase + fluxLastReaction;
        copyTank(coolantTank, coolantDisplayTank);
        copyTank(hotCoolantTank, hotCoolantDisplayTank);
        copyTank(mudTank, mudDisplayTank);
        on = false;
        locked = tag.getBoolean("isLocked");
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-3, 0, -3), worldPosition.offset(4, 3, 4));
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt("heat", heat);
        tag.putBoolean("isOn", on);
        tag.putBoolean("isLocked", locked);
        tag.putDouble("fluxDisplay", fluxDisplay);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        heat = tag.getInt("heat");
        on = tag.getBoolean("isOn");
        locked = tag.getBoolean("isLocked");
        fluxDisplay = tag.getDouble("fluxDisplay");
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeInt(heat);
        data.writeBoolean(on);
        data.writeBoolean(locked);
        data.writeDouble(getFluxDisplay());
        writeTank(data, coolantDisplayTank);
        writeTank(data, hotCoolantDisplayTank);
        writeTank(data, mudDisplayTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        heat = data.readInt();
        on = data.readBoolean();
        locked = data.readBoolean();
        fluxDisplay = data.readDouble();
        readTank(data, coolantDisplayTank);
        readTank(data, hotCoolantDisplayTank);
        readTank(data, mudDisplayTank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private boolean tickTopSegment(Level level) {
        List<WatzReactorBlockEntity> segments = collectSegments(level);
        boolean turnedOn = level.getBlockState(worldPosition.above(3)).is(ModBlocks.WATZ_PUMP.get())
                && level.hasNeighborSignal(worldPosition.above(5));
        SharedTanks shared = assembleSharedTanks(segments);

        for (int i = segments.size() - 1; i >= 0; i--) {
            segments.get(i).updateCoolant(shared);
        }
        for (int i = 0; i < segments.size(); i++) {
            WatzReactorBlockEntity above = i == 0 ? null : segments.get(i - 1);
            segments.get(i).updateReaction(above, shared, turnedOn);
        }
        for (WatzReactorBlockEntity segment : segments) {
            segment.on = turnedOn;
            segment.fluxDisplay = segment.fluxLastBase + segment.fluxLastReaction;
            segment.heat = (int) (segment.heat * 0.99D);
            segment.copySharedTanksForDisplay(shared);
            segment.networkPackNT(25);
        }
        distributeSharedTanks(segments, shared);
        WatzReactorBlockEntity bottom = segments.get(segments.size() - 1);
        bottom.tryProvideFluidToPorts(HbmFluids.COOLANT_HOT, bottom.hotCoolantTank.getPressure(), bottom);
        bottom.tryProvideFluidToPorts(HbmFluids.WATZ, bottom.mudTank.getPressure(), bottom);
        if (shared.mud.getFill() > 0) {
            explodeOnMudOverflow(level);
        }
        return true;
    }

    private void explodeOnMudOverflow(Level level) {
        on = false;
        clearAllPellets();
        clearUpperStructure(level);
        disassembleToMud(level);
        ChunkRadiationManager.incrementRadiation(level, worldPosition.above(), 1_000.0F);
        level.playSound(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 2.0D,
                worldPosition.getZ() + 0.5D, ModSounds.BLOCK_RBMK_EXPLOSION.get(), SoundSource.BLOCKS, 50.0F, 1.0F);
        ParticleUtil.spawnRbmkMush(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 2.0D,
                worldPosition.getZ() + 0.5D, 5.0F);
        awardWatzBoom(level);
    }

    private void awardWatzBoom(Level level) {
        AABB area = new AABB(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D, worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D).inflate(50.0D);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area)) {
            AchievementHandler.award(player, AchievementHandler.WATZ_BOOM);
        }
    }

    private void clearAllPellets() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < locks.length; slot++) {
            locks[slot] = ItemStack.EMPTY;
        }
        locked = false;
    }

    private void clearUpperStructure(Level level) {
        for (int x = -3; x <= 3; x++) {
            for (int y = 3; y < 6; y++) {
                for (int z = -3; z <= 3; z++) {
                    level.setBlock(worldPosition.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private void disassembleToMud(Level level) {
        spawnWatzShrapnel(level);
        suppressCoreDropsOnRemoval = true;
        level.setBlock(worldPosition, ModBlocks.MUD_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);

        setMud(worldPosition.above());
        setMud(worldPosition.above(2));

        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, 1, 0);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, 2, 0);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, 0, 1);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, 0, 2);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, -1, 0);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, -2, 0);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, 0, -1);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, 0, -2);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, 1, 1);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, 1, -1);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, -1, 1);
        setBrokenColumn(0, ModBlocks.WATZ_ELEMENT.get(), false, -1, -1);
        setBrokenColumn(0, ModBlocks.WATZ_COOLER.get(), false, 2, 1);
        setBrokenColumn(0, ModBlocks.WATZ_COOLER.get(), false, 2, -1);
        setBrokenColumn(0, ModBlocks.WATZ_COOLER.get(), false, 1, 2);
        setBrokenColumn(0, ModBlocks.WATZ_COOLER.get(), false, -1, 2);
        setBrokenColumn(0, ModBlocks.WATZ_COOLER.get(), false, -2, 1);
        setBrokenColumn(0, ModBlocks.WATZ_COOLER.get(), false, -2, -1);
        setBrokenColumn(0, ModBlocks.WATZ_COOLER.get(), false, 1, -2);
        setBrokenColumn(0, ModBlocks.WATZ_COOLER.get(), false, -1, -2);

        for (int j = -1; j < 2; j++) {
            setBrokenColumn(1, ModBlocks.WATZ_END.get(), true, 3, j);
            setBrokenColumn(1, ModBlocks.WATZ_END.get(), true, j, 3);
            setBrokenColumn(1, ModBlocks.WATZ_END.get(), true, -3, j);
            setBrokenColumn(1, ModBlocks.WATZ_END.get(), true, j, -3);
        }
        setBrokenColumn(1, ModBlocks.WATZ_END.get(), true, 2, 2);
        setBrokenColumn(1, ModBlocks.WATZ_END.get(), true, 2, -2);
        setBrokenColumn(1, ModBlocks.WATZ_END.get(), true, -2, 2);
        setBrokenColumn(1, ModBlocks.WATZ_END.get(), true, -2, -2);
    }

    private void spawnWatzShrapnel(Level level) {
        int count = 20;
        for (int i = 0; i < count * 5; i++) {
            ShrapnelEntity shrapnel = new ShrapnelEntity(level);
            shrapnel.setPos(worldPosition.getX() + 0.5D, worldPosition.getY() + 3.0D, worldPosition.getZ() + 0.5D);
            double motionY = ((level.random.nextFloat() * 0.5D) + 0.5D)
                    * (1.0D + count / (15.0D + level.random.nextInt(21)))
                    + (level.random.nextFloat() / 50.0D * count);
            double motionX = level.random.nextGaussian() * (1.0D + count / 100.0D);
            double motionZ = level.random.nextGaussian() * (1.0D + count / 100.0D);
            shrapnel.setDeltaMovement(motionX, motionY, motionZ);
            shrapnel.setWatz(true);
            level.addFreshEntity(shrapnel);
        }
    }

    private void setBrokenColumn(int minHeight, Block block, boolean riveted, int x, int z) {
        int height = minHeight + level.random.nextInt(3 - minHeight);
        for (int y = 0; y < 3; y++) {
            BlockPos target = worldPosition.offset(x, y, z);
            if (y <= height) {
                BlockState state = block.defaultBlockState();
                if (riveted && state.hasProperty(WatzEndBlock.RIVETED)) {
                    state = state.setValue(WatzEndBlock.RIVETED, true);
                }
                level.setBlock(target, state, Block.UPDATE_ALL);
            } else {
                setMud(target);
            }
        }
    }

    private void setMud(BlockPos pos) {
        level.setBlock(pos, ModBlocks.MUD_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    private List<WatzReactorBlockEntity> collectSegments(Level level) {
        List<WatzReactorBlockEntity> segments = new ArrayList<>();
        segments.add(this);
        for (BlockPos scan = worldPosition.below(3); scan.getY() >= level.getMinBuildHeight(); scan = scan.below(3)) {
            BlockEntity blockEntity = level.getBlockEntity(scan);
            if (blockEntity instanceof WatzReactorBlockEntity segment) {
                segments.add(segment);
            } else {
                break;
            }
        }
        return segments;
    }

    private boolean hasWatzAbove(Level level) {
        return level.getBlockEntity(worldPosition.above(3)) instanceof WatzReactorBlockEntity;
    }

    private SharedTanks assembleSharedTanks(List<WatzReactorBlockEntity> segments) {
        SharedTanks shared = new SharedTanks(0);
        for (WatzReactorBlockEntity segment : segments) {
            segment.setupCoolant();
            shared.coolant.changeTankSize(shared.coolant.getMaxFill() + segment.coolantTank.getMaxFill());
            shared.hotCoolant.changeTankSize(shared.hotCoolant.getMaxFill() + segment.hotCoolantTank.getMaxFill());
            shared.mud.changeTankSize(shared.mud.getMaxFill() + segment.mudTank.getMaxFill());
            shared.coolant.setFill(shared.coolant.getFill() + segment.coolantTank.getFill());
            shared.hotCoolant.setFill(shared.hotCoolant.getFill() + segment.hotCoolantTank.getFill());
            shared.mud.setFill(shared.mud.getFill() + segment.mudTank.getFill());
        }
        return shared;
    }

    private void distributeSharedTanks(List<WatzReactorBlockEntity> segments, SharedTanks shared) {
        for (int i = segments.size() - 1; i >= 0; i--) {
            WatzReactorBlockEntity segment = segments.get(i);
            fillFromShared(segment.coolantTank, shared.coolant);
            fillFromShared(segment.hotCoolantTank, shared.hotCoolant);
            fillFromShared(segment.mudTank, shared.mud);
            segment.onFluidContentsChanged();
        }
    }

    private void fillFromShared(HbmFluidTank target, HbmFluidTank shared) {
        int fill = Math.min(target.getMaxFill(), shared.getFill());
        shared.setFill(shared.getFill() - fill);
        target.setFill(fill);
    }

    private void setupCoolant() {
        coolantTank.setTankType(HbmFluids.COOLANT);
        HeatableFluidTrait trait = coolantTank.getTankType().getTrait(HeatableFluidTrait.class);
        HeatableFluidTrait.HeatingStep step = trait == null ? null : trait.getFirstStep();
        hotCoolantTank.setTankType(step == null ? HbmFluids.COOLANT_HOT : step.producedType());
    }

    private void updateCoolant(SharedTanks shared) {
        int heatToUse = (int) (heat * 0.2D);
        HbmFluidThermalExchange.ThermalResult result = HbmFluidThermalExchange.heat(
                shared.coolant, shared.hotCoolant, HeatingType.HEATEXCHANGER, heatToUse, false);
        heat -= result.heatUsed();
    }

    private void updateReaction(@Nullable WatzReactorBlockEntity above, SharedTanks shared, boolean turnedOn) {
        if (turnedOn) {
            List<ItemStack> pellets = collectActivePellets();
            double baseFlux = 0.0D;
            for (ItemStack stack : pellets) {
                baseFlux += WatzFuelRuntime.type(stack).passive();
            }
            double inputFlux = baseFlux + fluxLastReaction;
            double addedFlux = 0.0D;
            double addedHeat = 0.0D;
            for (ItemStack stack : pellets) {
                WatzFuelRuntime.Type type = WatzFuelRuntime.type(stack);
                WatzFuelRuntime.Curve burnFunc = type.burnFunc();
                if (burnFunc != null) {
                    double div = type.heatDiv() != null ? type.heatDiv().eval(heat) : 1.0D;
                    double burn = burnFunc.eval(inputFlux) / Math.max(div, 1.0E-9D);
                    WatzPelletItem.setYield(stack, WatzPelletItem.getYield(stack) - burn);
                    addedFlux += burn;
                    addedHeat += type.heatEmission() * burn;
                    shared.mud.setFill(shared.mud.getFill() + (int) Math.round(type.mudContent() * burn));
                }
            }
            for (ItemStack stack : pellets) {
                WatzFuelRuntime.Type type = WatzFuelRuntime.type(stack);
                WatzFuelRuntime.Curve absorbFunc = type.absorbFunc();
                if (absorbFunc != null) {
                    double absorb = absorbFunc.eval(inputFlux);
                    addedHeat += absorb;
                    WatzPelletItem.setYield(stack, WatzPelletItem.getYield(stack) - absorb);
                    shared.mud.setFill(shared.mud.getFill() + (int) Math.round(type.mudContent() * absorb));
                }
            }
            heat += (int) addedHeat;
            fluxLastBase = baseFlux;
            fluxLastReaction = addedFlux;
        } else {
            fluxLastBase = 0;
            fluxLastReaction = 0;
        }
        depleteSpentPellets();
        if (above != null) {
            fallAndSwapFromAbove(above);
        }
    }

    private List<ItemStack> collectActivePellets() {
        List<ItemStack> pellets = new ArrayList<>();
        for (int slot = 0; slot < PELLET_SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (WatzFuelRuntime.isPellet(stack)) {
                pellets.add(stack);
            }
        }
        return pellets;
    }

    private void depleteSpentPellets() {
        for (int slot = 0; slot < PELLET_SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (WatzFuelRuntime.isPellet(stack) && WatzPelletItem.getEnrichment(stack) <= 0.0D) {
                items.setStackInSlot(slot, WatzFuelRuntime.depletedProduct(stack));
            }
        }
    }

    private void fallAndSwapFromAbove(WatzReactorBlockEntity above) {
        for (int slot = 0; slot < PELLET_SLOT_COUNT; slot++) {
            ItemStack bottom = items.getStackInSlot(slot);
            ItemStack top = above.items.getStackInSlot(slot);
            if (bottom.isEmpty() && !top.isEmpty()) {
                items.setStackInSlot(slot, top.copy());
                above.items.setStackInSlot(slot, ItemStack.EMPTY);
            } else if (WatzFuelRuntime.isPellet(bottom) && WatzFuelRuntime.isDepletedPellet(top)) {
                items.setStackInSlot(slot, top.copy());
                above.items.setStackInSlot(slot, bottom.copy());
            }
        }
    }

    private boolean isValidPelletForSlot(int slot, ItemStack stack) {
        if (!WatzFuelRuntime.isPellet(stack)) {
            return false;
        }
        if (!locked) {
            return true;
        }
        ItemStack lockedStack = locks[slot];
        return !lockedStack.isEmpty() && lockedStack.getItem() == stack.getItem();
    }

    private void toggleLocks() {
        if (locked) {
            for (int slot = 0; slot < locks.length; slot++) {
                locks[slot] = ItemStack.EMPTY;
            }
        } else {
            for (int slot = 0; slot < locks.length; slot++) {
                locks[slot] = items.getStackInSlot(slot).copy();
            }
        }
        locked = !locked;
        setChanged();
    }

    private void saveLocks(CompoundTag tag) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < locks.length; slot++) {
            ItemStack stack = locks[slot];
            if (stack != null && !stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("slot", (byte) slot);
                stack.save(entry);
                list.add(entry);
            }
        }
        tag.put(TAG_LOCKS, list);
    }

    private void loadLocks(CompoundTag tag) {
        for (int slot = 0; slot < locks.length; slot++) {
            locks[slot] = ItemStack.EMPTY;
        }
        ListTag list = tag.getList(TAG_LOCKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("slot");
            if (slot >= 0 && slot < locks.length) {
                locks[slot] = entry.contains("stack", Tag.TAG_COMPOUND)
                        ? ItemStack.of(entry.getCompound("stack"))
                        : ItemStack.of(entry);
            }
        }
    }

    private void copySharedTanksForDisplay(SharedTanks shared) {
        copyTank(shared.coolant, coolantDisplayTank);
        copyTank(shared.hotCoolant, hotCoolantDisplayTank);
        copyTank(shared.mud, mudDisplayTank);
    }

    private static void copyTank(HbmFluidTank source, HbmFluidTank target) {
        target.changeTankSize(source.getMaxFill());
        target.setTankType(source.getTankType());
        target.withPressure(source.getPressure());
        target.setFill(source.getFill());
    }

    private static double readSavedFlux(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_INT) ? tag.getInt(key) / 1000.0D : tag.getDouble(key);
    }

    private List<FluidPort> fluidPorts() {
        return List.of(
                FluidPort.of(0, 3, 0, Direction.UP),
                FluidPort.of(2, 3, 0, Direction.UP),
                FluidPort.of(-2, 3, 0, Direction.UP),
                FluidPort.of(0, 3, 2, Direction.UP),
                FluidPort.of(0, 3, -2, Direction.UP),
                FluidPort.of(0, -1, 0, Direction.DOWN),
                FluidPort.of(2, -1, 0, Direction.DOWN),
                FluidPort.of(-2, -1, 0, Direction.DOWN),
                FluidPort.of(0, -1, 2, Direction.DOWN),
                FluidPort.of(0, -1, -2, Direction.DOWN));
    }

    private static void writeTank(FriendlyByteBuf data, HbmFluidTank tank) {
        data.writeInt(tank.getFill());
        data.writeInt(tank.getMaxFill());
        data.writeInt(tank.getTankType().getId());
        data.writeShort((short) tank.getPressure());
    }

    private static void readTank(FriendlyByteBuf data, HbmFluidTank tank) {
        int fill = data.readInt();
        int maxFill = data.readInt();
        FluidType type = HbmFluids.fromId(data.readInt());
        int pressure = data.readShort();
        tank.changeTankSize(maxFill);
        tank.withPressure(pressure);
        tank.setTankType(type);
        tank.setFill(fill);
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private static final class SharedTanks {
        private final HbmFluidTank coolant;
        private final HbmFluidTank hotCoolant;
        private final HbmFluidTank mud;

        private SharedTanks(int capacity) {
            coolant = new HbmFluidTank(HbmFluids.COOLANT, capacity);
            hotCoolant = new HbmFluidTank(HbmFluids.COOLANT_HOT, capacity);
            mud = new HbmFluidTank(HbmFluids.WATZ, capacity);
        }
    }

    private class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 0 || slot >= SLOT_COUNT) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = items.getStackInSlot(slot);
            if (WatzFuelRuntime.isPellet(stack)) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? 1 : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return items.isItemValid(slot, stack);
        }
    }
}
