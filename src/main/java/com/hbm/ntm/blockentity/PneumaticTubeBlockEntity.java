package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.ntl.PneumaticConnector;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.menu.PneumaticTubeMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticEndpoint;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticItemAccess;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNetwork;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNode;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticUtil;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.util.LegacyPatternMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PneumaticTubeBlockEntity extends HbmFluidNetworkBlockEntity implements MenuProvider,
        HbmStandardFluidReceiver, PneumaticEndpoint, PneumaticConnector, HbmLegacyControlReceiver {
    public static final int FILTER_SLOTS = 15;
    public static final int AIR_CAPACITY = 4_000;
    public static final int AIR_COST_PER_SEND = 50;
    public static final int SEND_INTERVAL_TICKS = 5;
    public static final int RECEIVER_INTERVAL_TICKS = 10;

    private static final String TAG_FILTER = "Filter";
    private static final String TAG_FILTER_SLOT = "Slot";
    private static final String TAG_INSERTION = "insertionDir";
    private static final String TAG_EJECTION = "ejectionDir";
    private static final String TAG_WHITELIST = "whitelist";
    private static final String TAG_REDSTONE = "redstone";
    private static final String TAG_SEND_ORDER = "sendOrder";
    private static final String TAG_RECEIVE_ORDER = "receiveOrder";
    private static final String TAG_SEND_COUNTER = "sendCounter";
    private static final String TAG_SOUND_DELAY = "soundDelay";

    private final LegacyPatternMatcher pattern = new LegacyPatternMatcher(FILTER_SLOTS);
    private final ItemStackHandler filter = new ItemStackHandler(FILTER_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndUpdate();
        }
    };
    private PneumaticNode pneumaticNode;
    private Direction insertionDirection;
    private Direction ejectionDirection;
    private boolean whitelist;
    private boolean redstone;
    private byte sendOrder = PneumaticNetwork.SEND_FIRST;
    private byte receiveOrder = PneumaticNetwork.RECEIVE_ROBIN;
    private int sendCounter;
    private int soundDelay;

    public PneumaticTubeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.PNEUMATIC_TUBE.get(), pos, state);
    }

    protected PneumaticTubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, List.of(new HbmFluidTank(HbmFluids.AIR, AIR_CAPACITY).withPressure(1)));
        getAllTanks().get(0).withPressure(1);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PneumaticTubeBlockEntity tube) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, tube);
        tube.serverTick();
        if (!level.isClientSide) {
            tube.networkPackNT(15);
        }
    }

    private void serverTick() {
        refreshPneumaticNode();
        if (soundDelay > 0) {
            soundDelay--;
        }

        if (level == null || level.isClientSide || pneumaticNode == null || pneumaticNode.getPneumaticNet() == null) {
            return;
        }

        if (isEndpoint() && level.getGameTime() % RECEIVER_INTERVAL_TICKS == 0L) {
            PneumaticUtil.receiver(level, worldPosition, ejectionDirection, this)
                    .ifPresent(receiver -> pneumaticNode.getPneumaticNet().addReceiver(receiver));
        }

        if (!isCompressor() || isRedstoneBlocked()) {
            return;
        }

        if ((level.getGameTime() + Math.abs(PneumaticUtil.identifier(worldPosition))) % SEND_INTERVAL_TICKS != 0L) {
            return;
        }
        if (compair().getFill() < AIR_COST_PER_SEND) {
            return;
        }

        Optional<PneumaticItemAccess> source = PneumaticUtil.sourceAccess(level, worldPosition, insertionDirection);
        if (source.isEmpty()) {
            return;
        }
        boolean sent = pneumaticNode.getPneumaticNet().send(
                source.get(),
                this,
                sendOrder,
                receiveOrder,
                PneumaticUtil.rangeForPressure(compair().getPressure()),
                sendCounter);
        sendCounter++;
        if (sent) {
            compair().drain(AIR_COST_PER_SEND, false);
            setChanged();
            if (soundDelay <= 0) {
                LegacySoundPlayer.playLegacyTubeFwoomp(level, worldPosition, 0.25F, 0.9F, 0.2F);
                soundDelay = 20;
            }
        }
    }

    public void refreshPneumaticNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        Set<Direction> connections = collectPneumaticConnections();
        if (pneumaticNode != null && !pneumaticNode.isExpired()
                && !pneumaticNode.getConnections().equals(connections)) {
            removePneumaticNode();
        }
        if (pneumaticNode == null || pneumaticNode.isExpired()) {
            pneumaticNode = PneumaticNodespace.createNode(level, new PneumaticNode(worldPosition, connections));
        }
    }

    public void removePneumaticNode() {
        if (level != null && !level.isClientSide) {
            PneumaticNodespace.destroyNode(level, worldPosition);
        }
        pneumaticNode = null;
    }

    public PneumaticNetwork getPneumaticNet() {
        return pneumaticNode == null ? null : pneumaticNode.getPneumaticNet();
    }

    public Direction getInsertionDirection() {
        return insertionDirection;
    }

    public Direction getEjectionDirection() {
        return ejectionDirection;
    }

    public HbmFluidTank compair() {
        return getAllTanks().get(0);
    }

    public boolean isRedstoneEnabled() {
        return redstone;
    }

    public int getSendOrder() {
        return sendOrder;
    }

    public int getReceiveOrder() {
        return receiveOrder;
    }

    public void cycleInsertionDirection() {
        insertionDirection = nextValidInventoryDirection(insertionDirection, ejectionDirection);
        onEndpointDirectionChanged();
    }

    public void cycleEjectionDirection() {
        ejectionDirection = nextValidInventoryDirection(ejectionDirection, insertionDirection);
        onEndpointDirectionChanged();
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
        setChanged();
    }

    public void setRedstone(boolean redstone) {
        this.redstone = redstone;
        setChanged();
    }

    public void setSendOrder(byte sendOrder) {
        this.sendOrder = (byte) Math.floorMod(sendOrder, 3);
        setChanged();
    }

    public void setReceiveOrder(byte receiveOrder) {
        this.receiveOrder = (byte) Math.floorMod(receiveOrder, 2);
        setChanged();
    }

    public ItemStack getFilterStack(int slot) {
        return slot >= 0 && slot < FILTER_SLOTS ? filter.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    public ItemStackHandler getFilterItems() {
        return filter;
    }

    public void setFilterStack(int slot, ItemStack stack) {
        if (slot < 0 || slot >= FILTER_SLOTS) {
            return;
        }
        filter.setStackInSlot(slot, HbmItemStackUtil.carefulCopyWithSize(stack, 1));
    }

    public int getModeIndex(int slot) {
        return pattern.getModeIndex(getFilterStack(slot), slot);
    }

    public void nextMode(int slot) {
        pattern.nextMode(getFilterStack(slot), slot);
        setChangedAndUpdate();
    }

    public void updatePatternSlot(int slot, ItemStack stack) {
        pattern.initPatternSmart(stack, slot);
        setChangedAndUpdate();
    }

    public boolean isCompressor() {
        return insertionDirection != null;
    }

    public boolean isEndpoint() {
        return ejectionDirection != null;
    }

    @Override
    public boolean matchesFilter(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < FILTER_SLOTS; slot++) {
            ItemStack filterStack = filter.getStackInSlot(slot);
            if (!filterStack.isEmpty() && pattern.isValidForFilter(filterStack, slot, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isWhitelist() {
        return whitelist;
    }

    @Override
    public BlockPos getPneumaticPos() {
        return worldPosition;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(compair());
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return type == HbmFluids.AIR && pressure == compair().getPressure()
                ? Math.max(1L, Math.min(100L, compair().getSpace() / 25L))
                : 0L;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == HbmFluids.AIR && isCompressor();
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return isCompressor();
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return isCompressor()
                && side != null
                && side != insertionDirection
                && side != ejectionDirection
                && type == HbmFluids.AIR;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != null;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return canConnectFluid(HbmFluids.AIR, side) ? HbmFluidSideMode.INPUT : HbmFluidSideMode.NONE;
    }

    @Override
    protected int getInputPressure(@Nullable Direction side) {
        return compair().getPressure();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte(TAG_INSERTION, directionToByte(insertionDirection));
        tag.putByte(TAG_EJECTION, directionToByte(ejectionDirection));
        tag.putBoolean(TAG_WHITELIST, whitelist);
        tag.putBoolean(TAG_REDSTONE, redstone);
        tag.putByte(TAG_SEND_ORDER, sendOrder);
        tag.putByte(TAG_RECEIVE_ORDER, receiveOrder);
        tag.putInt(TAG_SEND_COUNTER, sendCounter);
        tag.putInt(TAG_SOUND_DELAY, soundDelay);
        HbmItemStackUtil.saveSlottedItemsToTag(tag, TAG_FILTER, TAG_FILTER_SLOT, filter);
        pattern.writeToNbt(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        normalizeAirTank();
        insertionDirection = byteToDirection(tag.getByte(TAG_INSERTION));
        ejectionDirection = byteToDirection(tag.getByte(TAG_EJECTION));
        invalidateFluidHandlers();
        whitelist = tag.getBoolean(TAG_WHITELIST);
        redstone = tag.getBoolean(TAG_REDSTONE);
        sendOrder = tag.getByte(TAG_SEND_ORDER);
        receiveOrder = tag.getByte(TAG_RECEIVE_ORDER);
        sendCounter = tag.getInt(TAG_SEND_COUNTER);
        soundDelay = tag.getInt(TAG_SOUND_DELAY);
        HbmItemStackUtil.loadSlottedItems(tag, TAG_FILTER, TAG_FILTER_SLOT, filter);
        pattern.readFromNbt(tag);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.pneumoTube", "Pneumatic Tube");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PneumaticTubeMenu(containerId, inventory, this);
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 128.0D;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains("whitelist")) {
            whitelist = !whitelist;
        }
        if (data.contains("redstone")) {
            redstone = !redstone;
        }
        if (data.contains("pressure")) {
            int pressure = compair().getPressure() + 1;
            if (pressure > 5) {
                pressure = 1;
            }
            compair().withPressure(pressure);
        }
        if (data.contains("send")) {
            setSendOrder((byte) (sendOrder + 1));
        }
        if (data.contains("receive")) {
            setReceiveOrder((byte) (receiveOrder + 1));
        }
        setChangedAndUpdate();
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshPneumaticNode();
    }

    @Override
    public void setRemoved() {
        removePneumaticNode();
        super.setRemoved();
    }

    private Direction nextValidInventoryDirection(@Nullable Direction current, @Nullable Direction other) {
        Direction[] values = Direction.values();
        int start = current == null ? -1 : current.ordinal();
        for (int i = 0; i <= values.length; i++) {
            int next = (start + 1 + i) % (values.length + 1);
            Direction candidate = next == values.length ? null : values[next];
            if (candidate == null) {
                return null;
            }
            if (candidate == other) {
                continue;
            }
            if (level == null || PneumaticUtil.itemAccess(level, worldPosition.relative(candidate), candidate.getOpposite()).isPresent()) {
                return candidate;
            }
        }
        return null;
    }

    private Set<Direction> collectPneumaticConnections() {
        if (level == null) {
            return Set.of();
        }
        java.util.EnumSet<Direction> connections = java.util.EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof PneumaticConnector connector
                    && connector.canConnectPneumatic(direction.getOpposite())) {
                connections.add(direction);
            }
        }
        return connections;
    }

    private boolean isRedstoneBlocked() {
        return level != null && level.hasNeighborSignal(worldPosition) != redstone;
    }

    private void onEndpointDirectionChanged() {
        invalidateFluidHandlers();
        removePneumaticNode();
        refreshPneumaticNode();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    private void normalizeAirTank() {
        HbmFluidTank tank = compair();
        if (tank.getTankType() != HbmFluids.AIR) {
            tank.setTankType(HbmFluids.AIR);
        }
        if (tank.getPressure() <= 0) {
            tank.withPressure(1);
        }
    }

    private static byte directionToByte(@Nullable Direction direction) {
        return direction == null ? (byte) -1 : (byte) direction.ordinal();
    }

    @Nullable
    private static Direction byteToDirection(byte value) {
        Direction[] values = Direction.values();
        return value >= 0 && value < values.length ? values[value] : null;
    }
}
