package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.block.RBMKPanelBlock;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.menu.RBMKPanelMenu;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.neutron.RBMKConsolePlanner;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.BufferUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RBMKPanelBlockEntity extends BlockEntity
        implements MenuProvider, HbmLegacyLoadedTile {
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private RBMKPanelPlanner.GaugeUnit[] gauges = defaultGauges();
    private RBMKPanelPlanner.GraphUnit[] graphs = defaultGraphs();
    private RBMKPanelPlanner.IndicatorUnit[] indicators = defaultIndicators();
    private RBMKPanelPlanner.KeyUnit[] keys = defaultKeys();
    private RBMKPanelPlanner.LeverUnit[] levers = defaultLevers();
    private RBMKPanelPlanner.NumitronUnit[] numitrons = defaultNumitrons();
    private RBMKPanelPlanner.TerminalState terminal = RBMKPanelPlanner.TerminalState.empty();
    private int targetX;
    private int targetY;
    private int targetZ;
    private int displayRotation;
    private RBMKConsolePlanner.ColumnSnapshot[] displayColumns = new RBMKConsolePlanner.ColumnSnapshot[
            RBMKPanelPlanner.DISPLAY_COLUMN_COUNT];

    public RBMKPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RBMK_PANEL.get(), pos, state);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public RBMKPanelPlanner.PanelType panelType() {
        return getBlockState().getBlock() instanceof RBMKPanelBlock panelBlock
                ? panelBlock.panelType() : RBMKPanelPlanner.PanelType.GAUGE;
    }

    public RBMKPanelPlanner.GaugeUnit[] gauges() {
        return gauges;
    }

    public RBMKPanelPlanner.GraphUnit[] graphs() {
        return graphs;
    }

    public RBMKPanelPlanner.IndicatorUnit[] indicators() {
        return indicators;
    }

    public RBMKPanelPlanner.KeyUnit[] keys() {
        return keys;
    }

    public RBMKPanelPlanner.LeverUnit[] levers() {
        return levers;
    }

    public RBMKPanelPlanner.NumitronUnit[] numitrons() {
        return numitrons;
    }

    public RBMKPanelPlanner.TerminalState terminal() {
        return terminal;
    }

    public RBMKConsolePlanner.ColumnSnapshot[] displayColumns() {
        return displayColumns.clone();
    }

    public boolean setDisplayTarget(BlockPos target) {
        if (target == null) {
            return false;
        }
        target = normalizeDisplayTarget(target);
        if (target == null) {
            return false;
        }
        targetX = target.getX();
        targetY = target.getY();
        targetZ = target.getZ();
        if (level != null && !level.isClientSide) {
            rescanDisplay(level);
        }
        setChangedAndSync(false);
        return true;
    }

    public void rotateDisplay() {
        displayRotation = RBMKPanelPlanner.rotateDisplay(displayRotation);
        if (level != null && !level.isClientSide) {
            rescanDisplay(level);
        }
        setChangedAndSync(false);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKPanelBlockEntity panel) {
        boolean changed = switch (panel.panelType()) {
            case GAUGE -> panel.tickGauges(level);
            case GRAPH -> level.getGameTime() % RBMKPanelPlanner.GRAPH_SAMPLE_INTERVAL == 0 && panel.tickGraphs(level);
            case INDICATOR -> panel.tickIndicators(level);
            case KEYPAD -> panel.tickKeys(level);
            case LEVER -> panel.tickLevers(level);
            case NUMITRON -> panel.tickNumitrons(level);
            case TERMINAL -> panel.tickTerminal(level);
            case DISPLAY -> level.getGameTime() % RBMKPanelPlanner.DISPLAY_SCAN_INTERVAL == 0 && panel.rescanDisplay(level);
        };
        if (changed) {
            panel.setChangedAndSync(false);
        }
        if (panel.panelType() == RBMKPanelPlanner.PanelType.DISPLAY) {
            if (level.getGameTime() % RBMKPanelPlanner.DISPLAY_SCAN_INTERVAL == 0) {
                panel.networkPackNT(50);
            }
        } else {
            panel.networkPackNT(50);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RBMKPanelBlockEntity panel) {
        if (panel.panelType() == RBMKPanelPlanner.PanelType.GAUGE) {
            for (int i = 0; i < panel.gauges.length; i++) {
                panel.gauges[i] = RBMKPanelPlanner.tickGaugeClient(panel.gauges[i]);
            }
        }
    }

    public void clickKey(int index) {
        if (index < 0 || index >= keys.length || level == null || level.isClientSide) {
            return;
        }
        RBMKPanelPlanner.KeyClickPlan plan = RBMKPanelPlanner.clickKey(keys[index]);
        keys[index] = plan.unit();
        broadcast(level, plan.broadcast());
        if (plan.clickSound()) {
            level.playSound(null, worldPosition, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS,
                    1.0F, keys[index].isPressed() ? 1.0F : 0.75F);
        }
        setChangedAndSync(false);
    }

    public void clickLever(int index) {
        if (index < 0 || index >= levers.length || level == null || level.isClientSide) {
            return;
        }
        RBMKPanelPlanner.LeverClickPlan plan = RBMKPanelPlanner.clickLever(levers[index]);
        levers[index] = plan.unit();
        if (plan.startSound()) {
            level.playSound(null, worldPosition, ModSounds.BLOCK_LEVER_START.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        setChangedAndSync(false);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RBMKPanelMenu(containerId, inventory, this);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        writeLegacyLoadedTileClientTag(tag);
        writePanelData(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        readLegacyLoadedTileClientTag(tag);
        readPanelData(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        writeLegacyPanelPacket(data);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        readLegacyPanelPacket(data);
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return tag != null && !tag.isEmpty()
                && RBMKPanelPlanner.planPanelPermission(player.distanceToSqr(
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D))
                .permitted();
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        applyControl(tag);
        setChangedAndSync(false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        writePanelData(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        readPanelData(tag);
    }

    private boolean tickGauges(Level level) {
        boolean changed = false;
        for (int i = 0; i < gauges.length; i++) {
            RBMKPanelPlanner.GaugeUnit next = RBMKPanelPlanner.tickGauge(gauges[i], signal(level, gauges[i].rtty()));
            changed |= !next.equals(gauges[i]);
            gauges[i] = next;
        }
        return changed;
    }

    private boolean tickGraphs(Level level) {
        boolean changed = false;
        for (int i = 0; i < graphs.length; i++) {
            RBMKPanelPlanner.GraphUnit next = RBMKPanelPlanner.tickGraph(graphs[i], signal(level, graphs[i].rtty()));
            changed |= !next.equals(graphs[i]);
            graphs[i] = next;
        }
        return changed;
    }

    private boolean tickIndicators(Level level) {
        boolean changed = false;
        for (int i = 0; i < indicators.length; i++) {
            RBMKPanelPlanner.IndicatorUnit next =
                    RBMKPanelPlanner.tickIndicator(indicators[i], signal(level, indicators[i].rtty()));
            changed |= !next.equals(indicators[i]);
            indicators[i] = next;
        }
        return changed;
    }

    private boolean tickKeys(Level level) {
        boolean changed = false;
        for (int i = 0; i < keys.length; i++) {
            RBMKPanelPlanner.KeyTickPlan plan = RBMKPanelPlanner.tickKey(keys[i]);
            broadcast(level, plan.broadcast());
            changed |= !plan.unit().equals(keys[i]) || plan.broadcast() != null;
            keys[i] = plan.unit();
        }
        return changed;
    }

    private boolean tickLevers(Level level) {
        boolean changed = false;
        for (int i = 0; i < levers.length; i++) {
            RBMKPanelPlanner.LeverTickPlan plan = RBMKPanelPlanner.tickLever(levers[i]);
            broadcast(level, plan.broadcast());
            if (plan.stopSound()) {
                level.playSound(null, worldPosition, ModSounds.BLOCK_LEVER_STOP.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            if (plan.arcFlash()) {
                level.playSound(null, worldPosition, ModSounds.BLOCK_SPARK.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                spawnLeverArcFlash(level, i);
            }
            changed |= !plan.unit().equals(levers[i]) || plan.broadcast() != null;
            levers[i] = plan.unit();
        }
        return changed;
    }

    private void spawnLeverArcFlash(Level level, int leverIndex) {
        if (panelType() != RBMKPanelPlanner.PanelType.LEVER) {
            return;
        }
        Direction facing = getBlockState().getBlock() instanceof RBMKPanelBlock
                ? getBlockState().getValue(RBMKPanelBlock.FACING) : Direction.NORTH;
        Direction rot = facing.getClockWise();
        for (int i = 0; i < 2; i++) {
            double x = worldPosition.getX() + 0.5D + facing.getStepX() * 0.4D
                    - rot.getStepX() * (leverIndex - 0.5D) * (i == 0 ? 0.375D : 0.625D);
            double y = worldPosition.getY() + 0.40625D;
            double z = worldPosition.getZ() + 0.5D + facing.getStepZ() * 0.4D
                    - rot.getStepZ() * (leverIndex - 0.5D) * (i == 0 ? 0.375D : 0.625D);
            ParticleUtil.spawnTau(level, x, y, z, 5, true);
        }
    }

    private boolean tickNumitrons(Level level) {
        boolean changed = false;
        for (int i = 0; i < numitrons.length; i++) {
            RBMKPanelPlanner.NumitronUnit next =
                    RBMKPanelPlanner.tickNumitron(numitrons[i], signal(level, numitrons[i].rtty()));
            changed |= !next.equals(numitrons[i]);
            numitrons[i] = next;
        }
        return changed;
    }

    private boolean tickTerminal(Level level) {
        RBMKPanelPlanner.TerminalTickPlan plan = RBMKPanelPlanner.tickTerminal(terminal);
        terminal = plan.state();
        broadcast(level, plan.broadcast());
        return plan.broadcast() != null;
    }

    private boolean rescanDisplay(Level level) {
        boolean changed = false;
        BlockPos target = new BlockPos(targetX, targetY, targetZ);
        BlockPos normalizedTarget = normalizeDisplayTarget(target);
        if (normalizedTarget == null) {
            if (!target.equals(BlockPos.ZERO)) {
                targetX = 0;
                targetY = 0;
                targetZ = 0;
                changed = true;
            }
            for (int index = 0; index < displayColumns.length; index++) {
                if (displayColumns[index] != null) {
                    displayColumns[index] = null;
                    changed = true;
                }
            }
            return changed;
        }
        if (!normalizedTarget.equals(target)) {
            target = normalizedTarget;
            targetX = target.getX();
            targetY = target.getY();
            targetZ = target.getZ();
            changed = true;
        }
        for (int index = 0; index < displayColumns.length; index++) {
            BlockPos scanPos = target.offset(
                    RBMKPanelPlanner.displayRelativeX(index, displayRotation),
                    0,
                    RBMKPanelPlanner.displayRelativeZ(index, displayRotation));
            RBMKConsolePlanner.ColumnSnapshot next = displayColumnAt(level, scanPos);
            if (!columnEquals(displayColumns[index], next)) {
                changed = true;
            }
            displayColumns[index] = next;
        }
        return changed;
    }

    private RBMKConsolePlanner.ColumnSnapshot displayColumnAt(Level level, BlockPos pos) {
        RBMKColumnBlockEntity column = RBMKColumnBlockEntity.resolveOperationalColumn(level, pos);
        return column == null ? null : column.displaySnapshot();
    }

    @Nullable
    private BlockPos normalizeDisplayTarget(BlockPos pos) {
        if (pos == null || level == null) {
            return null;
        }
        RBMKColumnBlockEntity column = RBMKColumnBlockEntity.resolveOperationalColumn(level, pos);
        return column == null ? null : column.getBlockPos();
    }

    private static boolean columnEquals(RBMKConsolePlanner.ColumnSnapshot left,
            RBMKConsolePlanner.ColumnSnapshot right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.type() == right.type() && left.data().equals(right.data());
    }

    private RBMKPanelPlanner.RttySignal signal(Level level, String channelName) {
        RTTYSystem.RTTYChannel channel = RTTYSystem.listen(level, channelName);
        if (channel == null) {
            return null;
        }
        return new RBMKPanelPlanner.RttySignal(channel.signalString(), channel.timeStamp() < level.getGameTime() - 1);
    }

    private static void broadcast(Level level, RBMKPanelPlanner.RttyBroadcast broadcast) {
        if (broadcast != null) {
            RTTYSystem.broadcast(level, broadcast.channel(), broadcast.command());
        }
    }

    private void applyControl(CompoundTag tag) {
        int active = tag.getByte("active");
        int polling = tag.getByte("polling");
        switch (panelType()) {
            case GAUGE -> gauges = RBMKPanelPlanner.planGaugeControl(gauges, active, polling, gaugeEntries(tag)).units();
            case GRAPH -> graphs = RBMKPanelPlanner.planGraphControl(graphs, active, polling, graphEntries(tag)).units();
            case INDICATOR ->
                    indicators = RBMKPanelPlanner.planIndicatorControl(indicators, active, polling, indicatorEntries(tag)).units();
            case KEYPAD -> keys = RBMKPanelPlanner.planKeyControl(keys, active, polling, keyEntries(tag)).units();
            case LEVER -> levers = RBMKPanelPlanner.planLeverControl(levers, active, polling, leverEntries(tag)).units();
            case NUMITRON -> numitrons = RBMKPanelPlanner.planNumitronControl(numitrons, active, polling,
                    tag.getByte("shorten_number"), tag.getByte("leading_zeroes"), numitronEntries(tag)).units();
            case TERMINAL -> {
                RBMKPanelPlanner.TerminalControlPlan plan =
                        RBMKPanelPlanner.planTerminalControl(terminal, tag.getString("cmd"), tag.contains("cmd"));
                terminal = plan.state();
                broadcast(level, plan.broadcast());
                if (plan.action() == RBMKPanelPlanner.TerminalAction.SELF_DESTRUCT) {
                    selfDestructTerminal();
                }
            }
            case DISPLAY -> {
            }
        }
    }

    private void selfDestructTerminal() {
        if (level == null || level.isClientSide) {
            return;
        }
        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 0.5D;
        double z = worldPosition.getZ() + 0.5D;
        level.destroyBlock(worldPosition, false);
        WeaponExplosionUtil.smooth(level, x, y, z, 5.0F, null, 50.0F, 1.0D, true, 5.0F, 0.5F).explode();
    }

    private RBMKPanelPlanner.GaugeControlEntry[] gaugeEntries(CompoundTag tag) {
        RBMKPanelPlanner.GaugeControlEntry[] entries = new RBMKPanelPlanner.GaugeControlEntry[RBMKPanelPlanner.GAUGE_COUNT];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new RBMKPanelPlanner.GaugeControlEntry(tag.getInt("color" + i), tag.getString("label" + i),
                    tag.getString("rtty" + i), tag.getLong("min" + i), tag.getLong("max" + i));
        }
        return entries;
    }

    private RBMKPanelPlanner.GraphControlEntry[] graphEntries(CompoundTag tag) {
        RBMKPanelPlanner.GraphControlEntry[] entries = new RBMKPanelPlanner.GraphControlEntry[RBMKPanelPlanner.GRAPH_COUNT];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new RBMKPanelPlanner.GraphControlEntry(tag.getString("label" + i), tag.getString("rtty" + i),
                    tag.contains("min" + i) ? tag.getLong("min" + i) : null,
                    tag.contains("max" + i) ? tag.getLong("max" + i) : null);
        }
        return entries;
    }

    private RBMKPanelPlanner.IndicatorControlEntry[] indicatorEntries(CompoundTag tag) {
        RBMKPanelPlanner.IndicatorControlEntry[] entries =
                new RBMKPanelPlanner.IndicatorControlEntry[RBMKPanelPlanner.INDICATOR_COUNT];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new RBMKPanelPlanner.IndicatorControlEntry(tag.getInt("color" + i), tag.getString("label" + i),
                    tag.getString("rtty" + i), tag.contains("min" + i) ? tag.getLong("min" + i) : null,
                    tag.contains("max" + i) ? tag.getLong("max" + i) : null);
        }
        return entries;
    }

    private RBMKPanelPlanner.KeyControlEntry[] keyEntries(CompoundTag tag) {
        RBMKPanelPlanner.KeyControlEntry[] entries = new RBMKPanelPlanner.KeyControlEntry[RBMKPanelPlanner.KEY_COUNT];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new RBMKPanelPlanner.KeyControlEntry(tag.getInt("color" + i), tag.getString("label" + i),
                    tag.getString("rtty" + i), tag.getString("cmd" + i));
        }
        return entries;
    }

    private RBMKPanelPlanner.LeverControlEntry[] leverEntries(CompoundTag tag) {
        RBMKPanelPlanner.LeverControlEntry[] entries = new RBMKPanelPlanner.LeverControlEntry[RBMKPanelPlanner.LEVER_COUNT];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new RBMKPanelPlanner.LeverControlEntry(tag.getString("label" + i), tag.getString("rtty" + i),
                    tag.getString("cmdOn" + i), tag.getString("cmdOff" + i));
        }
        return entries;
    }

    private RBMKPanelPlanner.NumitronControlEntry[] numitronEntries(CompoundTag tag) {
        RBMKPanelPlanner.NumitronControlEntry[] entries =
                new RBMKPanelPlanner.NumitronControlEntry[RBMKPanelPlanner.NUMITRON_COUNT];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new RBMKPanelPlanner.NumitronControlEntry(tag.getString("label" + i), tag.getString("rtty" + i));
        }
        return entries;
    }

    private void writePanelData(CompoundTag tag) {
        switch (panelType()) {
            case GAUGE -> writeGauges(tag);
            case GRAPH -> writeGraphs(tag);
            case INDICATOR -> writeIndicators(tag);
            case KEYPAD -> writeKeys(tag);
            case LEVER -> writeLevers(tag);
            case NUMITRON -> writeNumitrons(tag);
            case TERMINAL -> writeTerminal(tag);
            case DISPLAY -> writeDisplay(tag);
        }
    }

    private void writeLegacyPanelPacket(FriendlyByteBuf data) {
        switch (panelType()) {
            case GAUGE -> {
                for (RBMKPanelPlanner.GaugeUnit unit : gauges) {
                    data.writeBoolean(unit.active());
                    data.writeBoolean(unit.polling());
                    data.writeInt(unit.color());
                    BufferUtil.writeString(data, unit.label());
                    BufferUtil.writeString(data, unit.rtty());
                    data.writeLong(unit.min());
                    data.writeLong(unit.max());
                    data.writeLong(unit.value());
                }
            }
            case GRAPH -> {
                for (RBMKPanelPlanner.GraphUnit unit : graphs) {
                    data.writeBoolean(unit.active());
                    data.writeBoolean(unit.polling());
                    BufferUtil.writeString(data, unit.label());
                    BufferUtil.writeString(data, unit.rtty());
                    data.writeBoolean(unit.minBound());
                    if (unit.minBound()) {
                        data.writeLong(unit.min());
                    }
                    data.writeBoolean(unit.maxBound());
                    if (unit.maxBound()) {
                        data.writeLong(unit.max());
                    }
                    if (unit.active()) {
                        for (long value : unit.values()) {
                            data.writeLong(value);
                        }
                    }
                }
            }
            case INDICATOR -> {
                for (RBMKPanelPlanner.IndicatorUnit unit : indicators) {
                    data.writeBoolean(unit.active());
                    data.writeBoolean(unit.polling());
                    data.writeBoolean(unit.light());
                    data.writeInt(unit.color());
                    BufferUtil.writeString(data, unit.label());
                    BufferUtil.writeString(data, unit.rtty());
                    data.writeLong(unit.min());
                    data.writeLong(unit.max());
                }
            }
            case KEYPAD -> {
                for (RBMKPanelPlanner.KeyUnit unit : keys) {
                    data.writeBoolean(unit.active());
                    data.writeBoolean(unit.polling());
                    data.writeBoolean(unit.isPressed());
                    data.writeInt(unit.color());
                    BufferUtil.writeString(data, unit.label());
                    BufferUtil.writeString(data, unit.rtty());
                    BufferUtil.writeString(data, unit.command());
                }
            }
            case LEVER -> {
                for (RBMKPanelPlanner.LeverUnit unit : levers) {
                    data.writeBoolean(unit.active());
                    data.writeBoolean(unit.polling());
                    data.writeFloat(unit.flipProgress());
                    BufferUtil.writeString(data, unit.label());
                    BufferUtil.writeString(data, unit.rtty());
                    BufferUtil.writeString(data, unit.commandOn());
                    BufferUtil.writeString(data, unit.commandOff());
                }
            }
            case NUMITRON -> {
                for (RBMKPanelPlanner.NumitronUnit unit : numitrons) {
                    data.writeBoolean(unit.shortenNumber());
                    data.writeLong(unit.activeDigits());
                    data.writeBoolean(unit.leadingZeroes());
                    data.writeBoolean(unit.active());
                    data.writeBoolean(unit.polling());
                    BufferUtil.writeString(data, unit.label());
                    BufferUtil.writeString(data, unit.rtty());
                    data.writeLong(unit.value());
                }
            }
            case TERMINAL -> {
                data.writeBoolean(!terminal.repeatCommand().isEmpty());
                for (String line : terminal.history()) {
                    BufferUtil.writeString(data, line);
                }
            }
            case DISPLAY -> {
                for (RBMKConsolePlanner.ColumnSnapshot column : displayColumns) {
                    if (column == null || column.type() == null) {
                        data.writeByte(-1);
                    } else {
                        data.writeByte((byte) column.type().ordinal());
                        BufferUtil.writeNBT(data, column.data());
                    }
                }
            }
        }
    }

    private void readLegacyPanelPacket(FriendlyByteBuf data) {
        switch (panelType()) {
            case GAUGE -> {
                for (int i = 0; i < gauges.length; i++) {
                    RBMKPanelPlanner.GaugeUnit previous = gauges[i];
                    gauges[i] = new RBMKPanelPlanner.GaugeUnit(data.readBoolean(), data.readBoolean(),
                            data.readInt(), BufferUtil.readString(data), BufferUtil.readString(data),
                            data.readLong(), data.readLong(), data.readLong(),
                            previous.renderValue(), previous.lastRenderValue());
                }
            }
            case GRAPH -> {
                for (int i = 0; i < graphs.length; i++) {
                    RBMKPanelPlanner.GraphUnit previous = graphs[i];
                    boolean active = data.readBoolean();
                    boolean polling = data.readBoolean();
                    String label = BufferUtil.readString(data);
                    String rtty = BufferUtil.readString(data);
                    boolean minBound = data.readBoolean();
                    long min = minBound ? data.readLong() : previous.min();
                    boolean maxBound = data.readBoolean();
                    long max = maxBound ? data.readLong() : previous.max();
                    long[] values = previous.values().clone();
                    if (active) {
                        for (int valueIndex = 0; valueIndex < values.length; valueIndex++) {
                            values[valueIndex] = data.readLong();
                        }
                    }
                    graphs[i] = new RBMKPanelPlanner.GraphUnit(active, polling, label, rtty, values,
                            min, minBound, max, maxBound);
                }
            }
            case INDICATOR -> {
                for (int i = 0; i < indicators.length; i++) {
                    indicators[i] = new RBMKPanelPlanner.IndicatorUnit(data.readBoolean(), data.readBoolean(),
                            data.readBoolean(), data.readInt(), BufferUtil.readString(data),
                            BufferUtil.readString(data), data.readLong(), data.readLong());
                }
            }
            case KEYPAD -> {
                for (int i = 0; i < keys.length; i++) {
                    RBMKPanelPlanner.KeyUnit previous = keys[i];
                    keys[i] = new RBMKPanelPlanner.KeyUnit(data.readBoolean(), data.readBoolean(),
                            data.readBoolean(), data.readInt(), BufferUtil.readString(data),
                            BufferUtil.readString(data), BufferUtil.readString(data), previous.clickTimer());
                }
            }
            case LEVER -> {
                for (int i = 0; i < levers.length; i++) {
                    RBMKPanelPlanner.LeverUnit previous = levers[i];
                    boolean active = data.readBoolean();
                    boolean polling = data.readBoolean();
                    float syncProgress = data.readFloat();
                    levers[i] = new RBMKPanelPlanner.LeverUnit(i, active, polling, BufferUtil.readString(data),
                            BufferUtil.readString(data), BufferUtil.readString(data), BufferUtil.readString(data),
                            previous.isTurningOn(), syncProgress, previous.flipProgress());
                }
            }
            case NUMITRON -> {
                for (int i = 0; i < numitrons.length; i++) {
                    boolean shortenNumber = data.readBoolean();
                    long activeDigits = data.readLong();
                    boolean leadingZeroes = data.readBoolean();
                    boolean active = data.readBoolean();
                    boolean polling = data.readBoolean();
                    numitrons[i] = new RBMKPanelPlanner.NumitronUnit(active, polling, shortenNumber, activeDigits,
                            leadingZeroes, BufferUtil.readString(data), BufferUtil.readString(data), data.readLong());
                }
            }
            case TERMINAL -> {
                boolean doesRepeat = data.readBoolean();
                String[] history = new String[RBMKPanelPlanner.TERMINAL_HISTORY_SIZE];
                for (int i = 0; i < history.length; i++) {
                    history[i] = BufferUtil.readString(data);
                }
                terminal = new RBMKPanelPlanner.TerminalState(history, terminal.channel(),
                        doesRepeat ? terminal.repeatCommand() : "", false);
            }
            case DISPLAY -> {
                RBMKConsolePlanner.ColumnType[] types = RBMKConsolePlanner.ColumnType.values();
                for (int i = 0; i < displayColumns.length; i++) {
                    int ordinal = data.readByte();
                    if (ordinal < 0 || ordinal >= types.length) {
                        displayColumns[i] = null;
                    } else {
                        displayColumns[i] = new RBMKConsolePlanner.ColumnSnapshot(types[ordinal],
                                BufferUtil.readNBT(data));
                    }
                }
            }
        }
    }

    private void readPanelData(CompoundTag tag) {
        switch (panelType()) {
            case GAUGE -> {
                for (int i = 0; i < gauges.length; i++) {
                    gauges[i] = new RBMKPanelPlanner.GaugeUnit(tag.getBoolean("active" + i),
                            tag.getBoolean("polling" + i), tag.getInt("color" + i), tag.getString("label" + i),
                            tag.getString("rtty" + i), tag.getLong("min" + i), tag.getLong("max" + i),
                            tag.getLong("value" + i), tag.getDouble("renderValue" + i),
                            tag.getDouble("lastRenderValue" + i));
                }
            }
            case GRAPH -> {
                for (int i = 0; i < graphs.length; i++) {
                    long[] values = new long[RBMKPanelPlanner.GRAPH_HISTORY_SIZE];
                    for (int j = 0; j < values.length; j++) {
                        values[j] = tag.getLong("value" + i + "_" + j);
                    }
                    graphs[i] = new RBMKPanelPlanner.GraphUnit(tag.getBoolean("active" + i),
                            tag.getBoolean("polling" + i), tag.getString("label" + i), tag.getString("rtty" + i),
                            values, tag.getLong("min" + i), tag.getBoolean("minBound" + i),
                            tag.getLong("max" + i), tag.getBoolean("maxBound" + i));
                }
            }
            case INDICATOR -> {
                for (int i = 0; i < indicators.length; i++) {
                    indicators[i] = new RBMKPanelPlanner.IndicatorUnit(tag.getBoolean("active" + i),
                            tag.getBoolean("polling" + i), tag.getBoolean("light" + i), tag.getInt("color" + i),
                            tag.getString("label" + i), tag.getString("rtty" + i), tag.getLong("min" + i),
                            tag.getLong("max" + i));
                }
            }
            case KEYPAD -> {
                for (int i = 0; i < keys.length; i++) {
                    keys[i] = new RBMKPanelPlanner.KeyUnit(tag.getBoolean("active" + i),
                            tag.getBoolean("polling" + i), tag.getBoolean("isPressed" + i), tag.getInt("color" + i),
                            tag.getString("label" + i), tag.getString("rtty" + i), tag.getString("command" + i),
                            tag.getInt("clickTimer" + i));
                }
            }
            case LEVER -> {
                for (int i = 0; i < levers.length; i++) {
                    levers[i] = new RBMKPanelPlanner.LeverUnit(i, tag.getBoolean("active" + i),
                            tag.getBoolean("polling" + i), tag.getString("label" + i), tag.getString("rtty" + i),
                            tag.getString("commandOn" + i), tag.getString("commandOff" + i),
                            tag.getBoolean("isTurningOn" + i), tag.getFloat("flipProgress" + i),
                            tag.getFloat("prevFlipProgress" + i));
                }
            }
            case NUMITRON -> {
                for (int i = 0; i < numitrons.length; i++) {
                    numitrons[i] = new RBMKPanelPlanner.NumitronUnit(tag.getBoolean("active" + i),
                            tag.getBoolean("polling" + i), tag.getBoolean("shorten_number" + i),
                            tag.getLong("active_digits" + i), tag.getBoolean("leading_zeroes" + i),
                            tag.getString("label" + i), tag.getString("rtty" + i), tag.getLong("value" + i));
                }
            }
            case TERMINAL -> readTerminal(tag);
            case DISPLAY -> readDisplay(tag);
        }
    }

    private void writeGauges(CompoundTag tag) {
        for (int i = 0; i < gauges.length; i++) {
            RBMKPanelPlanner.GaugeUnit unit = gauges[i];
            tag.putBoolean("active" + i, unit.active());
            tag.putBoolean("polling" + i, unit.polling());
            tag.putInt("color" + i, unit.color());
            tag.putString("label" + i, unit.label());
            tag.putString("rtty" + i, unit.rtty());
            tag.putLong("min" + i, unit.min());
            tag.putLong("max" + i, unit.max());
            tag.putLong("value" + i, unit.value());
            tag.putDouble("renderValue" + i, unit.renderValue());
            tag.putDouble("lastRenderValue" + i, unit.lastRenderValue());
        }
    }

    private void writeGraphs(CompoundTag tag) {
        for (int i = 0; i < graphs.length; i++) {
            RBMKPanelPlanner.GraphUnit unit = graphs[i];
            tag.putBoolean("active" + i, unit.active());
            tag.putBoolean("polling" + i, unit.polling());
            tag.putString("label" + i, unit.label());
            tag.putString("rtty" + i, unit.rtty());
            tag.putBoolean("minBound" + i, unit.minBound());
            tag.putLong("min" + i, unit.min());
            tag.putBoolean("maxBound" + i, unit.maxBound());
            tag.putLong("max" + i, unit.max());
            for (int j = 0; j < unit.values().length; j++) {
                tag.putLong("value" + i + "_" + j, unit.values()[j]);
            }
        }
    }

    private void writeIndicators(CompoundTag tag) {
        for (int i = 0; i < indicators.length; i++) {
            RBMKPanelPlanner.IndicatorUnit unit = indicators[i];
            tag.putBoolean("active" + i, unit.active());
            tag.putBoolean("polling" + i, unit.polling());
            tag.putBoolean("light" + i, unit.light());
            tag.putInt("color" + i, unit.color());
            tag.putString("label" + i, unit.label());
            tag.putString("rtty" + i, unit.rtty());
            tag.putLong("min" + i, unit.min());
            tag.putLong("max" + i, unit.max());
        }
    }

    private void writeKeys(CompoundTag tag) {
        for (int i = 0; i < keys.length; i++) {
            RBMKPanelPlanner.KeyUnit unit = keys[i];
            tag.putBoolean("active" + i, unit.active());
            tag.putBoolean("polling" + i, unit.polling());
            tag.putBoolean("isPressed" + i, unit.isPressed());
            tag.putInt("color" + i, unit.color());
            tag.putString("label" + i, unit.label());
            tag.putString("rtty" + i, unit.rtty());
            tag.putString("command" + i, unit.command());
            tag.putInt("clickTimer" + i, unit.clickTimer());
        }
    }

    private void writeLevers(CompoundTag tag) {
        for (int i = 0; i < levers.length; i++) {
            RBMKPanelPlanner.LeverUnit unit = levers[i];
            tag.putBoolean("active" + i, unit.active());
            tag.putBoolean("polling" + i, unit.polling());
            tag.putBoolean("isTurningOn" + i, unit.isTurningOn());
            tag.putFloat("flipProgress" + i, unit.flipProgress());
            tag.putFloat("prevFlipProgress" + i, unit.prevFlipProgress());
            tag.putString("label" + i, unit.label());
            tag.putString("rtty" + i, unit.rtty());
            tag.putString("commandOn" + i, unit.commandOn());
            tag.putString("commandOff" + i, unit.commandOff());
        }
    }

    private void writeNumitrons(CompoundTag tag) {
        for (int i = 0; i < numitrons.length; i++) {
            RBMKPanelPlanner.NumitronUnit unit = numitrons[i];
            tag.putBoolean("shorten_number" + i, unit.shortenNumber());
            tag.putLong("active_digits" + i, unit.activeDigits());
            tag.putBoolean("leading_zeroes" + i, unit.leadingZeroes());
            tag.putBoolean("active" + i, unit.active());
            tag.putBoolean("polling" + i, unit.polling());
            tag.putString("label" + i, unit.label());
            tag.putString("rtty" + i, unit.rtty());
            tag.putLong("value" + i, unit.value());
        }
    }

    private void writeTerminal(CompoundTag tag) {
        RBMKPanelPlanner.TerminalNbtSnapshot snapshot = RBMKPanelPlanner.terminalNbtSnapshot(terminal);
        tag.putString("channel", snapshot.channel());
        tag.putString("repeatCmd", snapshot.repeatCommand());
        tag.putBoolean("ocMode", false);
        tag.putBoolean("doesRepeat", !terminal.repeatCommand().isEmpty());
        for (int i = 0; i < snapshot.history().length; i++) {
            tag.putString("history" + i, snapshot.history()[i]);
        }
    }

    private void readTerminal(CompoundTag tag) {
        String[] history = new String[RBMKPanelPlanner.TERMINAL_HISTORY_SIZE];
        for (int i = 0; i < history.length; i++) {
            history[i] = tag.getString("history" + i);
        }
        terminal = RBMKPanelPlanner.terminalStateFromNbt(tag.getString("channel"),
                tag.getString("repeatCmd"), tag.getBoolean("ocMode"), history);
    }

    private void writeDisplay(CompoundTag tag) {
        RBMKPanelPlanner.DisplayNbtSnapshot snapshot =
                RBMKPanelPlanner.displayNbtSnapshot(targetX, targetY, targetZ, displayRotation);
        tag.putInt("tX", snapshot.targetX());
        tag.putInt("tY", snapshot.targetY());
        tag.putInt("tZ", snapshot.targetZ());
        tag.putByte("rotation", (byte) snapshot.rotation());
        for (int i = 0; i < displayColumns.length; i++) {
            RBMKConsolePlanner.ColumnSnapshot column = displayColumns[i];
            if (column == null || column.type() == null) {
                tag.putByte("columnType" + i, (byte) -1);
            } else {
                tag.putByte("columnType" + i, (byte) column.type().ordinal());
                tag.put("columnData" + i, column.data().copy());
            }
        }
    }

    private void readDisplay(CompoundTag tag) {
        targetX = tag.getInt("tX");
        targetY = tag.getInt("tY");
        targetZ = tag.getInt("tZ");
        displayRotation = tag.getByte("rotation");
        RBMKConsolePlanner.ColumnType[] types = RBMKConsolePlanner.ColumnType.values();
        for (int i = 0; i < displayColumns.length; i++) {
            int ordinal = tag.getByte("columnType" + i);
            if (ordinal < 0 || ordinal >= types.length) {
                displayColumns[i] = null;
            } else {
                displayColumns[i] = new RBMKConsolePlanner.ColumnSnapshot(
                        types[ordinal], tag.getCompound("columnData" + i));
            }
        }
    }

    private void setChangedAndSync(boolean updateNeighbors) {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            if (updateNeighbors) {
                level.updateNeighborsAt(worldPosition, state.getBlock());
            }
        }
    }

    private static RBMKPanelPlanner.GaugeUnit[] defaultGauges() {
        RBMKPanelPlanner.GaugeUnit[] units = new RBMKPanelPlanner.GaugeUnit[RBMKPanelPlanner.GAUGE_COUNT];
        for (int i = 0; i < units.length; i++) units[i] = RBMKPanelPlanner.defaultGauge(i);
        return units;
    }

    private static RBMKPanelPlanner.GraphUnit[] defaultGraphs() {
        RBMKPanelPlanner.GraphUnit[] units = new RBMKPanelPlanner.GraphUnit[RBMKPanelPlanner.GRAPH_COUNT];
        for (int i = 0; i < units.length; i++) units[i] = RBMKPanelPlanner.defaultGraph(i);
        return units;
    }

    private static RBMKPanelPlanner.IndicatorUnit[] defaultIndicators() {
        RBMKPanelPlanner.IndicatorUnit[] units = new RBMKPanelPlanner.IndicatorUnit[RBMKPanelPlanner.INDICATOR_COUNT];
        for (int i = 0; i < units.length; i++) units[i] = RBMKPanelPlanner.defaultIndicator(i);
        return units;
    }

    private static RBMKPanelPlanner.KeyUnit[] defaultKeys() {
        RBMKPanelPlanner.KeyUnit[] units = new RBMKPanelPlanner.KeyUnit[RBMKPanelPlanner.KEY_COUNT];
        for (int i = 0; i < units.length; i++) units[i] = RBMKPanelPlanner.defaultKey(i);
        return units;
    }

    private static RBMKPanelPlanner.LeverUnit[] defaultLevers() {
        RBMKPanelPlanner.LeverUnit[] units = new RBMKPanelPlanner.LeverUnit[RBMKPanelPlanner.LEVER_COUNT];
        for (int i = 0; i < units.length; i++) units[i] = RBMKPanelPlanner.defaultLever(i);
        return units;
    }

    private static RBMKPanelPlanner.NumitronUnit[] defaultNumitrons() {
        RBMKPanelPlanner.NumitronUnit[] units = new RBMKPanelPlanner.NumitronUnit[RBMKPanelPlanner.NUMITRON_COUNT];
        for (int i = 0; i < units.length; i++) units[i] = RBMKPanelPlanner.defaultNumitron(i);
        return units;
    }
}
