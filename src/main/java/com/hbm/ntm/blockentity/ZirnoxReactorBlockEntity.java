package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.api.tile.IInfoProviderEC;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.entity.projectile.ZirnoxDebrisEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.ZirnoxRodItem;
import com.hbm.ntm.menu.ZirnoxReactorMenu;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.neutron.RBMKDebrisPlanner.ZirnoxDebrisType;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.recipe.ZirnoxFuelRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.AchievementHandler;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ZirnoxReactorBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmLegacyControlReceiver, RORValueProvider,
        RORInteractive, IInfoProviderEC {
    public static final int ROD_SLOT_COUNT = 24;
    public static final int SLOT_CO2_INPUT = 24;
    public static final int SLOT_WATER_INPUT = 25;
    public static final int SLOT_CO2_OUTPUT = 26;
    public static final int SLOT_WATER_OUTPUT = 27;
    public static final int SLOT_COUNT = 28;
    public static final int MAX_HEAT = 100_000;
    public static final int MAX_PRESSURE = 100_000;
    private static final int FLOOR_COUNT = 9;
    private static final String LEGACY_STEAM_TANK = "steam";
    private static final String LEGACY_CARBON_DIOXIDE_TANK = "carbondioxide";
    private static final String LEGACY_WATER_TANK = "water";
    private static final String METEORITE_SWORD_BRED = "meteorite_sword_bred";
    private static final String METEORITE_SWORD_IRRADIATED = "meteorite_sword_irradiated";

    private static final String TAG_ITEMS = "items";
    private static final String[] ROR = {
            RORInfo.PREFIX_VALUE + "heat",
            RORInfo.PREFIX_VALUE + "pressure",
            RORInfo.PREFIX_VALUE + "water",
            RORInfo.PREFIX_VALUE + "steam",
            RORInfo.PREFIX_VALUE + "co2",
            RORInfo.PREFIX_VALUE + "state",
            RORInfo.PREFIX_FUNCTION + "setState" + RORInteractive.NAME_SEPARATOR + "active (0 or 1)",
            RORInfo.PREFIX_FUNCTION + "ventCO2"
    };
    private static final int[][] NEIGHBORS = {
            {1, 7}, {0, 2, 8}, {1, 9}, {4, 10}, {3, 5, 11}, {4, 6, 12}, {5, 13},
            {0, 8, 14}, {1, 7, 9, 15}, {2, 8, 16}, {3, 11, 17}, {4, 10, 12, 18},
            {5, 11, 13, 19}, {6, 12, 20}, {7, 15, 21}, {8, 14, 16, 22}, {9, 15, 23},
            {10, 18}, {11, 17, 19}, {12, 18, 20}, {13, 19}, {14, 22}, {15, 21, 23}, {16, 22}
    };

    private final HbmFluidTank steamTank;
    private final HbmFluidTank carbonDioxideTank;
    private final HbmFluidTank waterTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot >= 0 && slot < ROD_SLOT_COUNT) {
                return ZirnoxFuelRuntime.isRod(stack);
            }
            return (slot == SLOT_CO2_INPUT || slot == SLOT_WATER_INPUT) && !stack.isEmpty();
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private int heat;
    private int pressure;
    private int output;
    private int tiltBlocksChecked;
    private int tiltBlocksValid;
    private boolean on;
    private boolean redstonePowered;

    public ZirnoxReactorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.SUPERHOTSTEAM, 8_000),
                new HbmFluidTank(HbmFluids.CARBONDIOXIDE, 16_000),
                new HbmFluidTank(HbmFluids.WATER, 32_000));
    }

    private ZirnoxReactorBlockEntity(BlockPos pos, BlockState state, HbmFluidTank steamTank,
            HbmFluidTank carbonDioxideTank, HbmFluidTank waterTank) {
        super(ModBlockEntities.ZIRNOX_REACTOR.get(), pos, state, List.of(steamTank, carbonDioxideTank, waterTank));
        this.steamTank = steamTank;
        this.carbonDioxideTank = carbonDioxideTank;
        this.waterTank = waterTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ZirnoxReactorBlockEntity reactor) {
        boolean changed = reactor.checkTiltAgainstFoundation(level);
        if (changed || !reactor.isTilted() && level.getGameTime() % 20L == 0L) {
            HbmFluidNetworkBlockEntity.serverTick(level, pos, state, reactor);
        }
        changed |= reactor.tickServer(level);
        reactor.networkPackNT(150);
        if (changed || level.getGameTime() % 20L == 0L) {
            reactor.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public void setRedstonePowered(boolean powered) {
        if (!powered && redstonePowered) {
            on = false;
        }
        redstonePowered = powered;
        setChanged();
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 400.0D;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains("control")) {
            toggle();
        }
        if (data.contains("vent")) {
            ventCarbonDioxide();
        }
    }

    public void toggle() {
        if (!redstonePowered) {
            on = !on;
            setChanged();
        }
    }

    public void ventCarbonDioxide() {
        carbonDioxideTank.setFill(Math.max(carbonDioxideTank.getFill() - 1000, 0));
        onFluidContentsChanged();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getSteamTank() {
        return steamTank;
    }

    public HbmFluidTank getCarbonDioxideTank() {
        return carbonDioxideTank;
    }

    public HbmFluidTank getWaterTank() {
        return waterTank;
    }

    public int getHeat() {
        return heat;
    }

    public int getPressure() {
        return pressure;
    }

    public int getOutput() {
        return output;
    }

    public boolean isOn() {
        return on;
    }

    public boolean isRedstonePowered() {
        return redstonePowered;
    }

    public int getTemperatureDisplay() {
        return (int) Math.round(heat * 1.0E-5D * 780.0D + 20.0D);
    }

    public int getPressureDisplay() {
        return (int) Math.round(pressure * 1.0E-5D * 30.0D);
    }

    public int getHeatScaled() {
        return heat * 10_000 / MAX_HEAT;
    }

    public int getPressureScaled() {
        return pressure * 10_000 / MAX_PRESSURE;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.zirnox", "Zirnox Reactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ZirnoxReactorMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(waterTank, carbonDioxideTank);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(waterTank, steamTank, carbonDioxideTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(steamTank);
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
        return Math.max(1L, steamTank.getFill());
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts(getBlockState());
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return !isTilted();
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return !isTilted() && (type == waterTank.getTankType() || type == carbonDioxideTank.getTankType());
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return !isTilted() && type == steamTank.getTankType() && steamTank.getFill() > 0;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(waterTank, carbonDioxideTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(steamTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putInt("heat", heat);
        tag.putInt("pressure", pressure);
        tag.putBoolean("isOn", on);
        tag.putBoolean("redstonePowered", redstonePowered);
        steamTank.writeToNbt(tag, LEGACY_STEAM_TANK);
        carbonDioxideTank.writeToNbt(tag, LEGACY_CARBON_DIOXIDE_TANK);
        waterTank.writeToNbt(tag, LEGACY_WATER_TANK);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        heat = tag.getInt("heat");
        pressure = tag.getInt("pressure");
        on = tag.getBoolean("isOn");
        redstonePowered = tag.getBoolean("redstonePowered");
        output = 0;
        readLegacyTank(tag, LEGACY_STEAM_TANK, steamTank);
        readLegacyTank(tag, LEGACY_CARBON_DIOXIDE_TANK, carbonDioxideTank);
        readLegacyTank(tag, LEGACY_WATER_TANK, waterTank);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 5, 3));
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt("heat", heat);
        tag.putInt("pressure", pressure);
        tag.putBoolean("isOn", on);
        tag.putBoolean("redstonePowered", redstonePowered);
        tag.putInt("output", output);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        heat = tag.getInt("heat");
        pressure = tag.getInt("pressure");
        on = tag.getBoolean("isOn");
        redstonePowered = tag.getBoolean("redstonePowered");
        output = tag.getInt("output");
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeInt(heat);
        data.writeInt(pressure);
        data.writeBoolean(on);
        data.writeBoolean(redstonePowered);
        writeTank(data, steamTank);
        writeTank(data, carbonDioxideTank);
        writeTank(data, waterTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        heat = data.readInt();
        pressure = data.readInt();
        on = data.readBoolean();
        redstonePowered = data.readBoolean();
        readTank(data, steamTank);
        readTank(data, carbonDioxideTank);
        readTank(data, waterTank);
    }

    @Override
    public String[] getFunctionInfo() {
        return ROR;
    }

    @Override
    public String provideRORValue(String name) {
        if ((RORInfo.PREFIX_VALUE + "heat").equals(name)) return "" + getTemperatureDisplay();
        if ((RORInfo.PREFIX_VALUE + "pressure").equals(name)) return "" + getPressureDisplay();
        if ((RORInfo.PREFIX_VALUE + "water").equals(name)) return "" + waterTank.getFill();
        if ((RORInfo.PREFIX_VALUE + "steam").equals(name)) return "" + steamTank.getFill();
        if ((RORInfo.PREFIX_VALUE + "co2").equals(name)) return "" + carbonDioxideTank.getFill();
        if ((RORInfo.PREFIX_VALUE + "state").equals(name)) return "" + (on ? 1 : 0);
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if ((RORInfo.PREFIX_FUNCTION + "setState").equals(name) && params.length > 0) {
            if (!redstonePowered) {
                try {
                    on = Integer.parseInt(params[0]) == 1;
                    setChanged();
                } catch (NumberFormatException ignored) {
                }
            }
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "ventCO2").equals(name)) {
            ventCarbonDioxide();
            return null;
        }
        return null;
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putDouble(CompatEnergyControl.D_HEAT_C, Math.round(heat * 1.0E-5D * 780.0D + 20.0D));
        data.putDouble(CompatEnergyControl.D_MAXHEAT_C, Math.round(MAX_HEAT * 1.0E-5D * 780.0D + 20.0D));
        data.putLong(CompatEnergyControl.L_PRESSURE_BAR, Math.round(pressure * 1.0E-5D * 30.0D));
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_MB, output);
        data.putDouble(CompatEnergyControl.D_OUTPUT_MB, output);
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

    private boolean tickServer(Level level) {
        int oldHeat = heat;
        int oldPressure = pressure;
        int oldSteam = steamTank.getFill();
        int oldCo2 = carbonDioxideTank.getFill();
        int oldWater = waterTank.getFill();
        boolean oldOn = on;

        if (redstonePowered) {
            on = true;
        }
        output = 0;
        processFluidItemTransfers(items, List.of(
                HbmFluidItemTransfer.TankSlotTransfer.load(SLOT_CO2_INPUT, SLOT_CO2_OUTPUT, carbonDioxideTank),
                HbmFluidItemTransfer.TankSlotTransfer.load(SLOT_WATER_INPUT, SLOT_WATER_OUTPUT, waterTank)));

        if (on) {
            for (int slot = 0; slot < ROD_SLOT_COUNT; slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (ZirnoxFuelRuntime.isRod(stack)) {
                    decay(slot, stack);
                } else {
                    irradiateMeteoriteSword(slot, stack);
                }
            }
        }

        pressure = carbonDioxideTank.getFill() * 2
                + (int) (heat * (carbonDioxideTank.getFill() / (float) carbonDioxideTank.getMaxFill()));
        if (heat > 0 && heat < MAX_HEAT) {
            if (waterTank.getFill() > 0 && carbonDioxideTank.getFill() > 0
                    && steamTank.getFill() < steamTank.getMaxFill()) {
                generateSteam();
                heat -= (int) ((float) heat * (float) pressure / 1_000_000.0F);
            } else {
                heat -= 10;
            }
        }
        if (!isTilted()) {
            tryProvideFluidToPorts(steamTank.getTankType(), steamTank.getPressure(), this);
        }
        if (pressure > MAX_PRESSURE || heat > MAX_HEAT) {
            meltdown(level);
            return true;
        }
        return oldHeat != heat || oldPressure != pressure || oldSteam != steamTank.getFill()
                || oldCo2 != carbonDioxideTank.getFill() || oldWater != waterTank.getFill() || oldOn != on;
    }

    private boolean checkTiltAgainstFoundation(Level level) {
        if (!HbmCommonConfig.machineGravityEnabled()) {
            return setTiltedState(level, false);
        }
        if (FLOOR_COUNT <= 0) {
            return setTiltedState(level, false);
        }
        if ((level.getGameTime() + blockIdentity(worldPosition)) % 20L != 0L) {
            return false;
        }
        boolean changed = false;
        if (tiltBlocksChecked >= FLOOR_COUNT) {
            changed = setTiltedState(level, tiltBlocksValid < tiltBlocksChecked * 0.95D);
            tiltBlocksChecked = 0;
            tiltBlocksValid = 0;
        }

        BlockPos floor = standardFloor5x5(tiltBlocksChecked);
        tiltBlocksChecked++;
        if (isValidHeavyFoundation(level, floor)) {
            tiltBlocksValid++;
        }
        return changed;
    }

    private boolean setTiltedState(Level level, boolean tilted) {
        if (isTilted() == tilted) {
            return false;
        }
        if (tilted) {
            level.playSound(null, worldPosition, ModSounds.BLOCK_METAL_IMPACT.get(), SoundSource.BLOCKS, 3.0F, 1.0F);
        }
        setTilted(tilted);
        setChanged();
        return true;
    }

    private BlockPos standardFloor5x5(int index) {
        return new BlockPos(
                worldPosition.getX() - 2 + (index / 3) * 2,
                worldPosition.getY() - 1,
                worldPosition.getZ() - 2 + (index % 3) * 2);
    }

    private static int blockIdentity(BlockPos pos) {
        return (pos.getY() + pos.getZ() * 27_644_437) * 27_644_437 + pos.getX();
    }

    private static boolean isValidHeavyFoundation(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()
                || !state.isFaceSturdy(level, pos, Direction.UP)
                || !state.isSolidRender(level, pos)
                || isLooseHeavyFoundationMaterial(state)) {
            return false;
        }
        return state.getExplosionResistance(level, pos, null) >= Blocks.STONE.getExplosionResistance();
    }

    private static boolean isLooseHeavyFoundationMaterial(BlockState state) {
        return state.is(BlockTags.SAND)
                || state.is(BlockTags.WOOL)
                || state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.MUD)
                || state.is(Blocks.FARMLAND);
    }

    private void meltdown(Level level) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        steamTank.setFill(0);
        carbonDioxideTank.setFill(0);
        waterTank.setFill(0);
        on = false;

        Direction facing = getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        BlockState destroyed = ModBlocks.ZIRNOX_DESTROYED.get().defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, facing);
        level.setBlock(worldPosition, destroyed, Block.UPDATE_ALL);
        MultiblockHelper.fillLayout(level, worldPosition, ModBlocks.zirnoxDestroyedLayout(facing));
        level.playSound(null, worldPosition.getX(), worldPosition.getY() + 2.0D, worldPosition.getZ(),
                ModSounds.BLOCK_RBMK_EXPLOSION.get(), SoundSource.BLOCKS, 10.0F, 1.0F);
        level.explode(null, worldPosition.getX(), worldPosition.getY() + 3.0D, worldPosition.getZ(),
                12.0F, true, Level.ExplosionInteraction.BLOCK);
        spawnZirnoxDebris(level);
        ExplosionNukeGeneric.waste(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 35);
        awardZirnoxBoom(level);
    }

    private void awardZirnoxBoom(Level level) {
        AABB range = new AABB(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D).inflate(100.0D);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, range)) {
            AchievementHandler.award(player, AchievementHandler.ZIRNOX_BOOM);
            HbmPlayerProperties.markRadiationElementalTarget(player);
        }
    }

    private void spawnZirnoxDebris(Level level) {
        for (int i = 0; i < 2; i++) {
            spawnDebris(level, ZirnoxDebrisType.EXCHANGER);
        }
        for (int i = 0; i < 20; i++) {
            spawnDebris(level, ZirnoxDebrisType.CONCRETE);
            spawnDebris(level, ZirnoxDebrisType.BLANK);
        }
        for (int i = 0; i < 10; i++) {
            spawnDebris(level, ZirnoxDebrisType.ELEMENT);
            spawnDebris(level, ZirnoxDebrisType.GRAPHITE);
            spawnDebris(level, ZirnoxDebrisType.SHRAPNEL);
        }
    }

    private void spawnDebris(Level level, ZirnoxDebrisType type) {
        ZirnoxDebrisEntity debris = new ZirnoxDebrisEntity(level,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 4.0D,
                worldPosition.getZ() + 0.5D,
                type);
        double motionX = level.random.nextGaussian() * 0.75D;
        double motionZ = level.random.nextGaussian() * 0.75D;
        double motionY = 0.01D + level.random.nextDouble() * 1.25D;
        if (type == ZirnoxDebrisType.CONCRETE) {
            motionX *= 0.25D;
            motionY += level.random.nextDouble();
            motionZ *= 0.25D;
        }
        if (type == ZirnoxDebrisType.EXCHANGER) {
            motionX += 0.5D;
            motionY *= 0.1D;
            motionZ += 0.5D;
        }
        debris.setDeltaMovement(motionX, motionY, motionZ);
        level.addFreshEntity(debris);
    }

    private void generateSteam() {
        if (heat <= 10_256) {
            return;
        }
        int cycle = (int) (((heat - 10_256F) / MAX_HEAT)
                * Math.min(carbonDioxideTank.getFill() / 14_000F, 1.0F) * 25F * 5F);
        output = cycle;
        waterTank.setFill(waterTank.getFill() - cycle);
        steamTank.setFill(steamTank.getFill() + cycle);
        if (waterTank.getFill() < 0) {
            waterTank.setFill(0);
        }
        if (steamTank.getFill() > steamTank.getMaxFill()) {
            steamTank.setFill(steamTank.getMaxFill());
        }
    }

    private void decay(int slot, ItemStack stack) {
        int decay = neighborFuelCount(slot);
        if (!ZirnoxFuelRuntime.isBreeding(stack)) {
            decay++;
        }
        for (int i = 0; i < decay; i++) {
            heat += ZirnoxFuelRuntime.heat(stack);
            ZirnoxRodItem.incrementLifeTime(stack);
            if (ZirnoxRodItem.getLifeTime(stack) > stack.getMaxDamage()) {
                ItemStack product = ZirnoxFuelRuntime.product(stack);
                items.setStackInSlot(slot, product.isEmpty() ? ItemStack.EMPTY : product.copy());
                break;
            }
        }
    }

    private void irradiateMeteoriteSword(int slot, ItemStack stack) {
        RegistryObject<Item> bred = ModItems.legacyItem(METEORITE_SWORD_BRED);
        RegistryObject<Item> irradiated = ModItems.legacyItem(METEORITE_SWORD_IRRADIATED);
        if (bred != null && irradiated != null && stack.is(bred.get())) {
            items.setStackInSlot(slot, new ItemStack(irradiated.get()));
        }
    }

    private int neighborFuelCount(int slot) {
        int count = 0;
        for (int neighbor : NEIGHBORS[slot]) {
            if (ZirnoxFuelRuntime.isFuelRod(items.getStackInSlot(neighbor))) {
                count++;
            }
        }
        return count;
    }

    private static List<FluidPort> fluidPorts(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        Direction rot = facing.getClockWise();
        return List.of(
                FluidPort.of(rot.getStepX() * 3, 1, rot.getStepZ() * 3, rot),
                FluidPort.of(rot.getStepX() * 3, 3, rot.getStepZ() * 3, rot),
                FluidPort.of(rot.getStepX() * -3, 1, rot.getStepZ() * -3, rot.getOpposite()),
                FluidPort.of(rot.getStepX() * -3, 3, rot.getStepZ() * -3, rot.getOpposite()));
    }

    private static void writeTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.write(data, tank);
    }

    private static void readTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.read(data, tank);
    }

    private static void readLegacyTank(CompoundTag tag, String key, HbmFluidTank tank) {
        if (tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id")) {
            tank.readFromNbt(tag, key);
        }
    }

    private static boolean containsFluid(ItemStack stack, FluidType type) {
        if (stack.isEmpty() || type == null) {
            return false;
        }
        HbmFluidStack fluid = HbmFluidItemTransfer.getItemFluid(stack);
        return fluid.type() == type && fluid.amount() > 0;
    }

    private class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return ROD_SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < ROD_SLOT_COUNT ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot >= 0 && slot < ROD_SLOT_COUNT && ZirnoxFuelRuntime.isRod(stack)
                    ? items.insertItem(slot, stack, simulate)
                    : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 0 || slot >= ROD_SLOT_COUNT) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = items.getStackInSlot(slot);
            if (slot < ROD_SLOT_COUNT && ZirnoxFuelRuntime.isRod(stack)) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < ROD_SLOT_COUNT ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < ROD_SLOT_COUNT && ZirnoxFuelRuntime.isRod(stack);
        }
    }
}
