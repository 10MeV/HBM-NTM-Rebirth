package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.RBMKConsoleBlock;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.neutron.RBMKConsolePlanner;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class RBMKConsoleBlockEntity extends BlockEntity implements HbmLegacyLoadedTile {
    private static final int LAYOUT_REPAIR_INTERVAL = 20;

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private BlockPos target = BlockPos.ZERO;
    private int rotation;
    private int[] fluxBuffer = new int[RBMKConsolePlanner.FLUX_BUFFER_SIZE];
    private RBMKConsolePlanner.ColumnSnapshot[] columns =
            new RBMKConsolePlanner.ColumnSnapshot[RBMKConsolePlanner.CONSOLE_GRID_SIZE * RBMKConsolePlanner.CONSOLE_GRID_SIZE];
    private RBMKConsolePlanner.ScreenState[] screens = RBMKConsolePlanner.defaultScreens();

    public RBMKConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RBMK_CONSOLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKConsoleBlockEntity console) {
        if (!console.ensureConsoleLayout(level, pos)) {
            return;
        }
        RBMKConsolePlanner.ConsoleTickPlan plan = RBMKConsolePlanner.planTick(level.getGameTime());
        if (plan.rescan()) {
            console.rescan(level);
        }
        if (plan.prepareScreenInfo()) {
            console.prepareScreenInfo();
        }
        console.networkPackNT(plan.networkRange());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            ensureConsoleLayoutNow(level, worldPosition);
        }
    }

    private boolean ensureConsoleLayout(Level level, BlockPos pos) {
        if (level.isClientSide || (level.getGameTime() + pos.asLong()) % LAYOUT_REPAIR_INTERVAL != 0) {
            return MultiblockHelper.isOperationalCoreLayoutComplete(level, pos);
        }
        return ensureConsoleLayoutNow(level, pos);
    }

    private static boolean ensureConsoleLayoutNow(Level level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCoreAt(level, pos);
        if (level.isClientSide || core == null) {
            return false;
        }
        return MultiblockHelper.ensureOperationalCoreLayoutComplete(level, core.pos());
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public BlockPos target() {
        return target;
    }

    public int rotation() {
        return rotation;
    }

    public int[] fluxBuffer() {
        return hasCompleteLayout() ? fluxBuffer.clone() : new int[fluxBuffer.length];
    }

    public RBMKConsolePlanner.ColumnSnapshot[] columns() {
        return hasCompleteLayout() ? columns.clone() : new RBMKConsolePlanner.ColumnSnapshot[columns.length];
    }

    public RBMKConsolePlanner.ScreenState[] screens() {
        return screens.clone();
    }

    public boolean setTarget(BlockPos target) {
        if (target == null || !hasCompleteLayout()) {
            return false;
        }
        BlockPos normalized = normalizeColumnTarget(target);
        if (normalized == null) {
            return false;
        }
        this.target = normalized;
        if (level != null && !level.isClientSide) {
            rescan(level);
            prepareScreenInfo();
        }
        setChangedAndSync();
        return true;
    }

    public void rotateConsole() {
        if (!hasCompleteLayout()) {
            return;
        }
        rotation = RBMKConsolePlanner.rotate(rotation);
        if (level != null && !level.isClientSide) {
            rescan(level);
            prepareScreenInfo();
        }
        setChangedAndSync();
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (level != null && getBlockState().getBlock() instanceof RBMKConsoleBlock block) {
            LegacyMultiblockLayout layout = block.getMultiblockLayout(getBlockState(), level, worldPosition);
            if (layout != null) {
                return layout.renderBoundingBox(worldPosition, 1.0D);
            }
        }
        return RBMKConsolePlanner.renderBoundingBox(worldPosition);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        writeConsoleData(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        readConsoleData(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        writeConsoleData(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = legacyLoadedTileClientTag();
        writeConsoleData(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        readLegacyLoadedTileClientTag(tag);
        readConsoleData(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            readConsoleData(tag);
        }
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return tag != null
                && hasCompleteLayout()
                && RBMKConsolePlanner.planPermission(player.distanceToSqr(
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D)).permitted();
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (tag == null || level == null || level.isClientSide || !hasCompleteLayout()) {
            return;
        }
        if (tag.contains("level")) {
            applyManualLevel(tag.getDouble("level"), selectedFromLegacyControl(tag));
        }
        if (tag.contains("toggle")) {
            RBMKConsolePlanner.ScreenTogglePlan plan =
                    RBMKConsolePlanner.planToggleScreen(tag.getByte("toggle"), screens);
            screens = plan.screens();
        }
        if (tag.contains("id")) {
            RBMKConsolePlanner.ScreenSelectionPlan plan = RBMKConsolePlanner.planScreenSelection(
                    tag.getByte("id"), selectedBooleans(tag), screens);
            screens = plan.screens();
        }
        if (tag.contains("assignColor")) {
            applyColor(tag.getByte("assignColor"), tag.getIntArray("cols"));
        }
        if (tag.contains("compressor")) {
            applyCompressorCycle(tag.getIntArray("cols"));
        }
        prepareScreenInfo();
        setChangedAndSync();
    }

    private void rescan(Level level) {
        BlockPos normalizedTarget = normalizeColumnTarget(target);
        if (normalizedTarget == null) {
            if (!target.equals(BlockPos.ZERO)) {
                target = BlockPos.ZERO;
                setChanged();
            }
            clearColumns();
            fluxBuffer = RBMKConsolePlanner.shiftFluxBuffer(fluxBuffer, 0.0D);
            setChanged();
            return;
        }
        if (!normalizedTarget.equals(target)) {
            target = normalizedTarget;
            setChanged();
        }
        double flux = 0.0D;
        int index = 0;
        for (BlockPos scanPos : RBMKConsolePlanner.scanPositions(target, RBMKConsolePlanner.CONSOLE_GRID_SIZE, rotation)) {
            RBMKColumnBlockEntity column = columnAt(level, scanPos);
            if (column == null) {
                columns[index++] = null;
                continue;
            }
            columns[index++] = column.displaySnapshot();
            if (column.kind().rod()) {
                flux += column.lastFluxQuantity();
            }
        }
        fluxBuffer = RBMKConsolePlanner.shiftFluxBuffer(fluxBuffer, flux);
        setChanged();
    }

    private void prepareScreenInfo() {
        for (int i = 0; i < screens.length; i++) {
            RBMKConsolePlanner.ScreenState screen = screens[i];
            RBMKConsolePlanner.ScreenDisplayPlan display =
                    RBMKConsolePlanner.prepareScreen(screen.type(), screen.columns(), columns);
            screens[i] = new RBMKConsolePlanner.ScreenState(screen.type(), screen.columns(), display.display());
        }
    }

    @Nullable
    private RBMKColumnBlockEntity columnAt(Level level, BlockPos pos) {
        return RBMKColumnBlockEntity.resolveOperationalColumn(level, pos);
    }

    @Nullable
    private BlockPos normalizeColumnTarget(BlockPos pos) {
        if (pos == null || level == null) {
            return null;
        }
        RBMKColumnBlockEntity column = RBMKColumnBlockEntity.resolveOperationalColumn(level, pos);
        return column == null ? null : column.getBlockPos();
    }

    private void clearColumns() {
        for (int i = 0; i < columns.length; i++) {
            columns[i] = null;
        }
    }

    private void applyManualLevel(double levelValue, int[] selectedColumns) {
        RBMKConsolePlanner.ManualLevelControlPlan plan =
                RBMKConsolePlanner.planManualLevelControl(levelValue, selectedColumns, target, rotation);
        for (RBMKConsolePlanner.ColumnAction action : plan.actions()) {
            RBMKColumnBlockEntity column = columnAt(level, action.worldPos());
            if (column != null && column.kind().control() && !column.kind().automatic()) {
                column.setControlTarget(plan.targetLevel());
            }
        }
    }

    private void applyColor(int rawColor, int[] selectedColumns) {
        RBMKConsolePlanner.ColorAssignmentPlan plan =
                RBMKConsolePlanner.planColorAssignment(rawColor, selectedColumns, target, rotation);
        for (RBMKConsolePlanner.ColumnAction action : plan.actions()) {
            RBMKColumnBlockEntity column = columnAt(level, action.worldPos());
            if (column != null && column.kind().control() && !column.kind().automatic()) {
                column.setManualControlColor(plan.color());
            }
        }
    }

    private void applyCompressorCycle(int[] selectedColumns) {
        RBMKConsolePlanner.CompressorCyclePlan plan =
                RBMKConsolePlanner.planCompressorCycle(selectedColumns, target, rotation);
        for (RBMKConsolePlanner.ColumnAction action : plan.actions()) {
            RBMKColumnBlockEntity column = columnAt(level, action.worldPos());
            if (column != null && column.kind() == com.hbm.ntm.block.RBMKColumnBlock.Kind.BOILER) {
                column.cycleBoilerCompressor();
            }
        }
    }

    private static int[] selectedFromLegacyControl(CompoundTag tag) {
        int[] selected = new int[RBMKConsolePlanner.CONSOLE_GRID_SIZE * RBMKConsolePlanner.CONSOLE_GRID_SIZE];
        int count = 0;
        for (String key : tag.getAllKeys()) {
            if (key.startsWith("sel_")) {
                selected[count++] = tag.getInt(key);
            }
        }
        int[] result = new int[count];
        System.arraycopy(selected, 0, result, 0, count);
        return result;
    }

    private static boolean[] selectedBooleans(CompoundTag tag) {
        boolean[] selected = new boolean[RBMKConsolePlanner.CONSOLE_GRID_SIZE * RBMKConsolePlanner.CONSOLE_GRID_SIZE];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = tag.getBoolean("s" + i);
        }
        return selected;
    }

    private void writeConsoleData(CompoundTag tag) {
        RBMKConsolePlanner.ConsoleNbtPlan plan = RBMKConsolePlanner.nbtSnapshot(target, rotation, screens);
        tag.putInt(plan.targetXTag(), plan.target().getX());
        tag.putInt(plan.targetYTag(), plan.target().getY());
        tag.putInt(plan.targetZTag(), plan.target().getZ());
        tag.putByte(plan.rotationTag(), plan.rotation());
        for (int i = 0; i < plan.screens().length; i++) {
            RBMKConsolePlanner.ScreenState screen = plan.screens()[i];
            tag.putByte(plan.screenTypeTagPrefix() + i, (byte) screen.type().ordinal());
            tag.putIntArray(plan.screenColumnsTagPrefix() + i, screen.columns());
            if (screen.display() != null) {
                tag.putString("display" + i, screen.display());
            }
        }
        tag.putIntArray("fluxBuffer", fluxBuffer);
        for (int i = 0; i < columns.length; i++) {
            RBMKConsolePlanner.ColumnSnapshot column = columns[i];
            if (column == null || column.type() == null) {
                tag.putByte("columnType" + i, (byte) -1);
            } else {
                tag.putByte("columnType" + i, (byte) column.type().ordinal());
                tag.put("columnData" + i, column.data().copy());
            }
        }
    }

    private void readConsoleData(CompoundTag tag) {
        target = new BlockPos(
                tag.getInt(RBMKConsolePlanner.TARGET_X_TAG),
                tag.getInt(RBMKConsolePlanner.TARGET_Y_TAG),
                tag.getInt(RBMKConsolePlanner.TARGET_Z_TAG));
        rotation = Math.floorMod(tag.getByte(RBMKConsolePlanner.ROTATION_TAG), 4);
        RBMKConsolePlanner.ScreenType[] screenTypes = RBMKConsolePlanner.ScreenType.values();
        RBMKConsolePlanner.ScreenState[] nextScreens = RBMKConsolePlanner.defaultScreens();
        for (int i = 0; i < nextScreens.length; i++) {
            int typeOrdinal = tag.getByte(RBMKConsolePlanner.SCREEN_TYPE_TAG_PREFIX + i);
            RBMKConsolePlanner.ScreenType type = typeOrdinal >= 0 && typeOrdinal < screenTypes.length
                    ? screenTypes[typeOrdinal] : RBMKConsolePlanner.ScreenType.NONE;
            nextScreens[i] = new RBMKConsolePlanner.ScreenState(type,
                    tag.getIntArray(RBMKConsolePlanner.SCREEN_COLUMNS_TAG_PREFIX + i),
                    tag.contains("display" + i) ? tag.getString("display" + i) : null);
        }
        screens = nextScreens;
        int[] loadedFlux = tag.getIntArray("fluxBuffer");
        if (loadedFlux.length == fluxBuffer.length) {
            fluxBuffer = loadedFlux;
        }
        RBMKConsolePlanner.ColumnType[] columnTypes = RBMKConsolePlanner.ColumnType.values();
        for (int i = 0; i < columns.length; i++) {
            int ordinal = tag.getByte("columnType" + i);
            if (ordinal < 0 || ordinal >= columnTypes.length) {
                columns[i] = null;
            } else {
                columns[i] = new RBMKConsolePlanner.ColumnSnapshot(
                        columnTypes[ordinal], tag.getCompound("columnData" + i));
            }
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            if (!level.isClientSide) {
                networkPackNT(RBMKConsolePlanner.NETWORK_RANGE);
            }
        }
    }

    private boolean hasCompleteLayout() {
        return level != null && MultiblockHelper.isOperationalCoreLayoutComplete(level, worldPosition);
    }
}
