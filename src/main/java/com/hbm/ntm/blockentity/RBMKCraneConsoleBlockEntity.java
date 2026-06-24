package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.RBMKCraneConsoleBlock;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.neutron.NeutronHandler;
import com.hbm.ntm.neutron.RBMKCranePlanner;
import com.hbm.ntm.neutron.RBMKFuelRodSpec;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.neutron.RBMKStructureDimensions;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RBMKCraneConsoleBlockEntity extends BlockEntity implements HbmLegacyLoadedTile {
    private static final int LAYOUT_REPAIR_INTERVAL = 20;
    private static final double CRANE_HORIZONTAL_RENDER_PADDING = 4.0D;
    private static final double CRANE_VERTICAL_RENDER_PADDING = 4.0D;
    private static final double CRANE_LIFT_EXTENSION = 4.0D;

    private static final String TAG_CRANE = "crane";
    private static final String TAG_CRANE_ROTATION = "craneRotationOffset";
    private static final String TAG_CENTER_X = "centerX";
    private static final String TAG_CENTER_Y = "centerY";
    private static final String TAG_CENTER_Z = "centerZ";
    private static final String TAG_SPAN_F = "spanF";
    private static final String TAG_SPAN_B = "spanB";
    private static final String TAG_SPAN_L = "spanL";
    private static final String TAG_SPAN_R = "spanR";
    private static final String TAG_HEIGHT = "height";
    private static final String TAG_POS_FRONT = "posFront";
    private static final String TAG_POS_LEFT = "posLeft";
    private static final String TAG_PROGRESS = "progress";

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private int centerX;
    private int centerY;
    private int centerZ;
    private int spanF;
    private int spanB;
    private int spanL;
    private int spanR;
    private int height;
    private boolean setUpCrane;
    private int craneRotationOffset;
    private double lastTiltFront;
    private double lastTiltLeft;
    private double tiltFront;
    private double tiltLeft;
    private double lastPosFront;
    private double lastPosLeft;
    private double posFront;
    private double posLeft;
    private double syncFront;
    private double syncLeft;
    private boolean goesDown;
    private double lastProgress = 1.0D;
    private double progress = 1.0D;
    private double syncProgress = 1.0D;
    private ItemStack loadedItem = ItemStack.EMPTY;
    private boolean clientHasLoaded;
    private double loadedHeat;
    private double loadedEnrichment;
    private int turnProgress;

    public RBMKCraneConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RBMK_CRANE_CONSOLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKCraneConsoleBlockEntity console) {
        if (!console.ensureConsoleLayout(level, pos)) {
            return;
        }
        if (!console.setUpCrane) {
            console.networkPackNT(RBMKCranePlanner.NETWORK_RANGE);
            return;
        }
        console.tickServer(level);
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

    public static void clientTick(Level level, BlockPos pos, BlockState state, RBMKCraneConsoleBlockEntity console) {
        console.lastTiltFront = console.tiltFront;
        console.lastTiltLeft = console.tiltLeft;
        console.lastPosFront = console.posFront;
        console.lastPosLeft = console.posLeft;
        console.lastProgress = console.progress;

        RBMKCranePlanner.CraneClientTickPlan plan = RBMKCranePlanner.planClientTick(console.clientState());
        RBMKCranePlanner.CraneClientState next = plan.state();
        console.posFront = next.posFront();
        console.posLeft = next.posLeft();
        console.progress = next.progress();
        console.syncFront = next.syncFront();
        console.syncLeft = next.syncLeft();
        console.syncProgress = next.syncProgress();
        console.turnProgress = next.turnProgress();
    }

    public boolean setTarget(BlockPos targetColumnCore) {
        if (level == null || targetColumnCore == null || !hasCompleteLayout()) {
            return false;
        }
        targetColumnCore = normalizeColumnTarget(targetColumnCore);
        if (targetColumnCore == null) {
            return false;
        }
        int columnHeightAbove = RBMKStructureDimensions.columnHeightAboveCore();
        BlockPos center = targetColumnCore.above(columnHeightAbove + 1);
        Direction facing = facing();
        int girderY = center.getY() + 6;
        Direction spanFront = facing.getOpposite();
        Direction spanRight = spanFront.getClockWise();
        Direction spanBack = spanRight.getClockWise();
        Direction spanLeft = spanBack.getClockWise();

        RBMKCranePlanner.SetupPlan plan = RBMKCranePlanner.planSetup(targetColumnCore, columnHeightAbove,
                findRoomExtent(center, girderY, spanFront),
                findRoomExtent(center, girderY, spanBack),
                findRoomExtent(center, girderY, spanLeft),
                findRoomExtent(center, girderY, spanRight));
        centerX = plan.center().getX();
        centerY = plan.center().getY();
        centerZ = plan.center().getZ();
        spanF = plan.spanFront();
        spanB = plan.spanBack();
        spanL = plan.spanLeft();
        spanR = plan.spanRight();
        height = plan.height();
        setUpCrane = plan.setUpCrane();
        setChangedAndSync();
        return true;
    }

    public void cycleCraneRotation() {
        if (!hasCompleteLayout()) {
            return;
        }
        craneRotationOffset = RBMKCranePlanner.cycleRotation(craneRotationOffset);
        setChangedAndSync();
    }

    public boolean hasItemLoaded() {
        return hasCompleteLayout() && (!loadedItem.isEmpty() || clientHasLoaded);
    }

    public boolean isCraneLoading() {
        return hasCompleteLayout() && progress != 1.0D;
    }

    public boolean isAboveValidTarget() {
        return targetColumn() != null;
    }

    public RBMKCranePlanner.CraneState cranePlannerState() {
        if (!hasCompleteLayout()) {
            return new RBMKCranePlanner.CraneState(false, 0, BlockPos.ZERO,
                    new RBMKCranePlanner.CraneBounds(0, 0, 0, 0), 0,
                    new RBMKCranePlanner.CranePosition(0.0D, 0.0D));
        }
        return new RBMKCranePlanner.CraneState(
                setUpCrane,
                craneRotationOffset,
                new BlockPos(centerX, centerY, centerZ),
                new RBMKCranePlanner.CraneBounds(spanF, spanB, spanL, spanR),
                height,
                new RBMKCranePlanner.CranePosition(posFront, posLeft));
    }

    public com.hbm.ntm.client.renderer.LegacyRbmkMachineRenderer.CraneConsoleState consoleRenderState() {
        if (!hasCompleteLayout()) {
            return new com.hbm.ntm.client.renderer.LegacyRbmkMachineRenderer.CraneConsoleState(
                    0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, false, false, false);
        }
        return new com.hbm.ntm.client.renderer.LegacyRbmkMachineRenderer.CraneConsoleState(
                lastTiltFront,
                tiltFront,
                lastTiltLeft,
                tiltLeft,
                loadedHeat,
                loadedEnrichment,
                isCraneLoading(),
                hasItemLoaded(),
                isAboveValidTarget());
    }

    public com.hbm.ntm.client.renderer.LegacyRbmkMachineRenderer.CraneState craneRenderState() {
        if (!hasCompleteLayout()) {
            return new com.hbm.ntm.client.renderer.LegacyRbmkMachineRenderer.CraneState(
                    false, 0,
                    new com.hbm.ntm.client.renderer.LegacyRbmkMachineRenderer.CraneSpans(0, 0, 0, 0),
                    0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0);
        }
        return new com.hbm.ntm.client.renderer.LegacyRbmkMachineRenderer.CraneState(
                setUpCrane,
                height,
                new com.hbm.ntm.client.renderer.LegacyRbmkMachineRenderer.CraneSpans(spanF, spanB, spanL, spanR),
                lastPosFront,
                posFront,
                lastPosLeft,
                posLeft,
                lastProgress,
                progress,
                craneRotationOffset);
    }

    public BlockPos craneCenter() {
        return new BlockPos(centerX, centerY, centerZ);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB box = new AABB(worldPosition).inflate(2.0D);
        if (!setUpCrane) {
            return box;
        }
        double horizontalRadius = Math.max(Math.max(spanF, spanB), Math.max(spanL, spanR))
                + CRANE_HORIZONTAL_RENDER_PADDING;
        double minY = centerY - CRANE_LIFT_EXTENSION - CRANE_VERTICAL_RENDER_PADDING;
        double maxY = centerY + Math.max(height, RBMKCranePlanner.CRANE_HEIGHT) + CRANE_VERTICAL_RENDER_PADDING;
        AABB craneBox = new AABB(
                centerX + 0.5D - horizontalRadius,
                minY,
                centerZ + 0.5D - horizontalRadius,
                centerX + 0.5D + horizontalRadius,
                maxY,
                centerZ + 0.5D + horizontalRadius);
        return box.minmax(craneBox);
    }

    public double getViewDistance() {
        return Math.sqrt(RBMKCranePlanner.MAX_RENDER_DISTANCE_SQ);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        writeCraneTag(tag, true);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        readCraneTag(tag, true);
        syncFront = posFront;
        syncLeft = posLeft;
        syncProgress = progress;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        writeCraneTag(tag, true);
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
        writeCraneTag(tag, true);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        readLegacyLoadedTileClientTag(tag);
        readCraneTag(tag, true);
        syncFront = posFront;
        syncLeft = posLeft;
        syncProgress = progress;
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeBoolean(setUpCrane);
        if (!setUpCrane) {
            return;
        }
        data.writeInt(craneRotationOffset);
        data.writeInt(centerX);
        data.writeInt(centerY);
        data.writeInt(centerZ);
        data.writeInt(spanF);
        data.writeInt(spanB);
        data.writeInt(spanL);
        data.writeInt(spanR);
        data.writeInt(height);
        data.writeDouble(posFront);
        data.writeDouble(posLeft);
        data.writeDouble(progress);
        data.writeBoolean(!loadedItem.isEmpty());
        data.writeDouble(loadedHeat);
        data.writeDouble(loadedEnrichment);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        setUpCrane = data.readBoolean();
        if (!setUpCrane) {
            return;
        }
        craneRotationOffset = data.readInt();
        centerX = data.readInt();
        centerY = data.readInt();
        centerZ = data.readInt();
        spanF = data.readInt();
        spanB = data.readInt();
        spanL = data.readInt();
        spanR = data.readInt();
        height = data.readInt();
        syncFront = data.readDouble();
        syncLeft = data.readDouble();
        syncProgress = data.readDouble();
        clientHasLoaded = data.readBoolean();
        if (!clientHasLoaded) {
            loadedItem = ItemStack.EMPTY;
        }
        loadedHeat = data.readDouble();
        loadedEnrichment = data.readDouble();
        turnProgress = 5;
    }

    private void tickServer(Level level) {
        RBMKColumnBlockEntity target = targetColumn();
        if (target != null) {
            target.setCraneIndicator(RBMKCranePlanner.COLUMN_INDICATOR_TICKS);
        }

        boolean targetLoadable = target != null && target.isCraneLoadable();
        boolean canInteract = target != null && RBMKCranePlanner.canTargetInteract(
                targetLoadable,
                !loadedItem.isEmpty(),
                target.canCraneLoad(loadedItem),
                target.canCraneUnload());
        RBMKCranePlanner.LoadMotionPlan loadPlan = RBMKCranePlanner.planLoadMotion(
                progress,
                goesDown,
                targetLoadable,
                canInteract,
                !loadedItem.isEmpty());
        progress = loadPlan.progress();
        goesDown = loadPlan.goesDown();
        executeInteraction(target, loadPlan.interaction());

        Direction facing = facing();
        Direction operationSide = facing.getClockWise();
        RBMKCranePlanner.CraneOperationBox box =
                RBMKCranePlanner.operationBox(worldPosition, facing, operationSide);
        List<Player> players = level.getEntitiesOfClass(Player.class,
                new AABB(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()));
        tiltFront = 0.0D;
        tiltLeft = 0.0D;
        if (!players.isEmpty() && !isCraneLoading()) {
            RBMKCranePlanner.MovePlan move = RBMKCranePlanner.planMove(
                    new RBMKCranePlanner.CranePosition(posFront, posLeft),
                    movementInput(players.get(0)),
                    new RBMKCranePlanner.CraneBounds(spanF, spanB, spanL, spanR),
                    isCraneLoading());
            posFront = move.position().posFront();
            posLeft = move.position().posLeft();
            tiltFront = move.tiltFront();
            tiltLeft = move.tiltLeft();
            if (HbmPlayerProperties.isCraneLoadPressed(players.get(0))) {
                goesDown = true;
            }
        }

        updateLoadedRodInfo();
        setChanged();
        networkPackNT(RBMKCranePlanner.NETWORK_RANGE);
    }

    private void executeInteraction(@Nullable RBMKColumnBlockEntity target, RBMKCranePlanner.Interaction interaction) {
        if (target == null || interaction == RBMKCranePlanner.Interaction.NONE) {
            return;
        }
        if (interaction == RBMKCranePlanner.Interaction.LOAD_TO_COLUMN) {
            if (!loadedItem.isEmpty() && target.craneLoad(loadedItem)) {
                loadedItem = ItemStack.EMPTY;
                setChangedAndSync();
            }
            return;
        }
        if (interaction == RBMKCranePlanner.Interaction.UNLOAD_FROM_COLUMN && loadedItem.isEmpty()) {
            ItemStack unloaded = target.craneUnload();
            if (!unloaded.isEmpty()) {
                loadedItem = unloaded;
                setChangedAndSync();
            }
        }
    }

    @Nullable
    private RBMKColumnBlockEntity targetColumn() {
        if (level == null || !setUpCrane) {
            return null;
        }
        BlockPos targetPos = RBMKCranePlanner.targetColumnPos(craneCenter(), facing(), facing().getCounterClockWise(),
                posFront, posLeft);
        return RBMKColumnBlockEntity.resolveOperationalColumn(level, targetPos);
    }

    @Nullable
    private BlockPos normalizeColumnTarget(BlockPos pos) {
        RBMKColumnBlockEntity column = RBMKColumnBlockEntity.resolveOperationalColumn(level, pos);
        return column == null ? null : column.getBlockPos();
    }

    private int findRoomExtent(BlockPos center, int y, Direction direction) {
        boolean[] air = new boolean[RBMKCranePlanner.MAX_ROOM_EXTENT - 1];
        for (int i = 1; i < RBMKCranePlanner.MAX_ROOM_EXTENT; i++) {
            air[i - 1] = level != null && level.isEmptyBlock(new BlockPos(
                    center.getX() + direction.getStepX() * i,
                    y,
                    center.getZ() + direction.getStepZ() * i));
        }
        return RBMKCranePlanner.planRoomExtent(air);
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(RBMKCraneConsoleBlock.FACING)
                ? state.getValue(RBMKCraneConsoleBlock.FACING)
                : Direction.SOUTH;
    }

    private RBMKCranePlanner.MovementInput movementInput(Player player) {
        return new RBMKCranePlanner.MovementInput(
                HbmPlayerProperties.isCraneUpPressed(player),
                HbmPlayerProperties.isCraneDownPressed(player),
                HbmPlayerProperties.isCraneLeftPressed(player),
                HbmPlayerProperties.isCraneRightPressed(player));
    }

    private RBMKCranePlanner.CraneClientState clientState() {
        return new RBMKCranePlanner.CraneClientState(
                posFront,
                posLeft,
                progress,
                syncFront,
                syncLeft,
                syncProgress,
                tiltFront,
                tiltLeft,
                lastPosFront,
                lastPosLeft,
                lastProgress,
                lastTiltFront,
                lastTiltLeft,
                turnProgress);
    }

    private void updateLoadedRodInfo() {
        if (loadedItem.getItem() instanceof RBMKFuelRodItem item) {
            RBMKFuelRodSpec spec = item.getSpec();
            RBMKFuelRodState state = item.getState(loadedItem);
            RBMKCranePlanner.RodInfoPlan info =
                    RBMKCranePlanner.rodInfo(spec, state, item.getLegacyRodId());
            loadedHeat = info.hullHeat();
            loadedEnrichment = info.enrichment();
        } else {
            loadedHeat = 0.0D;
            loadedEnrichment = 0.0D;
        }
        clientHasLoaded = !loadedItem.isEmpty();
    }

    private void writeCraneTag(CompoundTag tag, boolean includeHeld) {
        tag.putBoolean(TAG_CRANE, setUpCrane);
        tag.putInt(TAG_CRANE_ROTATION, craneRotationOffset);
        tag.putInt(TAG_CENTER_X, centerX);
        tag.putInt(TAG_CENTER_Y, centerY);
        tag.putInt(TAG_CENTER_Z, centerZ);
        tag.putInt(TAG_SPAN_F, spanF);
        tag.putInt(TAG_SPAN_B, spanB);
        tag.putInt(TAG_SPAN_L, spanL);
        tag.putInt(TAG_SPAN_R, spanR);
        tag.putInt(TAG_HEIGHT, height);
        tag.putDouble(TAG_POS_FRONT, posFront);
        tag.putDouble(TAG_POS_LEFT, posLeft);
        tag.putDouble(TAG_PROGRESS, progress);
        tag.putDouble("loadedHeat", loadedHeat);
        tag.putDouble("loadedEnrichment", loadedEnrichment);
        tag.putBoolean("hasLoaded", !loadedItem.isEmpty() || clientHasLoaded);
        if (includeHeld && !loadedItem.isEmpty()) {
            tag.put(RBMKCranePlanner.HELD_ITEM_NBT_KEY, loadedItem.save(new CompoundTag()));
        }
    }

    private void readCraneTag(CompoundTag tag, boolean includeHeld) {
        setUpCrane = tag.getBoolean(TAG_CRANE);
        craneRotationOffset = tag.getInt(TAG_CRANE_ROTATION);
        centerX = tag.getInt(TAG_CENTER_X);
        centerY = tag.getInt(TAG_CENTER_Y);
        centerZ = tag.getInt(TAG_CENTER_Z);
        spanF = tag.getInt(TAG_SPAN_F);
        spanB = tag.getInt(TAG_SPAN_B);
        spanL = tag.getInt(TAG_SPAN_L);
        spanR = tag.getInt(TAG_SPAN_R);
        height = tag.getInt(TAG_HEIGHT);
        posFront = tag.getDouble(TAG_POS_FRONT);
        posLeft = tag.getDouble(TAG_POS_LEFT);
        progress = tag.contains(TAG_PROGRESS) ? tag.getDouble(TAG_PROGRESS) : 1.0D;
        loadedHeat = tag.getDouble("loadedHeat");
        loadedEnrichment = tag.getDouble("loadedEnrichment");
        clientHasLoaded = tag.getBoolean("hasLoaded");
        if (includeHeld && tag.contains(RBMKCranePlanner.HELD_ITEM_NBT_KEY)) {
            loadedItem = ItemStack.of(tag.getCompound(RBMKCranePlanner.HELD_ITEM_NBT_KEY));
        } else if (!clientHasLoaded) {
            loadedItem = ItemStack.EMPTY;
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            if (!level.isClientSide) {
                networkPackNT(RBMKCranePlanner.NETWORK_RANGE);
            }
        }
    }

    private boolean hasCompleteLayout() {
        return level != null && MultiblockHelper.isOperationalCoreLayoutComplete(level, worldPosition);
    }
}
