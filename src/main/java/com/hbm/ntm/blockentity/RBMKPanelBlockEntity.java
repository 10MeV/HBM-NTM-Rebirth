package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.block.RBMKPanelBlock;
import com.hbm.ntm.menu.RBMKPanelMenu;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
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

import java.util.ArrayList;
import java.util.List;

public class RBMKPanelBlockEntity extends BlockEntity
        implements MenuProvider, HbmLegacyLoadedTile, LegacyLookOverlayProvider {
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private RBMKPanelPlanner.GaugeUnit[] gauges = defaultGauges();
    private RBMKPanelPlanner.GraphUnit[] graphs = defaultGraphs();
    private RBMKPanelPlanner.IndicatorUnit[] indicators = defaultIndicators();
    private RBMKPanelPlanner.KeyUnit[] keys = defaultKeys();
    private RBMKPanelPlanner.LeverUnit[] levers = defaultLevers();
    private RBMKPanelPlanner.NumitronUnit[] numitrons = defaultNumitrons();

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

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKPanelBlockEntity panel) {
        boolean changed = switch (panel.panelType()) {
            case GAUGE -> panel.tickGauges(level);
            case GRAPH -> level.getGameTime() % RBMKPanelPlanner.GRAPH_SAMPLE_INTERVAL == 0 && panel.tickGraphs(level);
            case INDICATOR -> panel.tickIndicators(level);
            case KEYPAD -> panel.tickKeys(level);
            case LEVER -> panel.tickLevers(level);
            case NUMITRON -> panel.tickNumitrons(level);
            case TERMINAL, DISPLAY -> false;
        };
        if (changed) {
            panel.setChangedAndSync(false);
            panel.networkPackNT(50);
        } else if (level.getGameTime() % 20L == 0L) {
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
            level.playSound(null, worldPosition, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.0F);
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
        CompoundTag tag = new CompoundTag();
        writePanelData(tag);
        data.writeNbt(tag);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            readPanelData(tag);
        }
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

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<Component> lines = new ArrayList<>();
        for (String channel : activeChannels()) {
            if (!channel.isEmpty()) {
                lines.add(Component.literal("RTTY: " + channel));
            }
        }
        return LegacyLookOverlay.forBlock(this, lines);
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
                level.playSound(null, worldPosition, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            if (plan.arcFlash()) {
                level.playSound(null, worldPosition, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            changed |= !plan.unit().equals(levers[i]) || plan.broadcast() != null;
            levers[i] = plan.unit();
        }
        return changed;
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
            case TERMINAL, DISPLAY -> {
            }
        }
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
            case TERMINAL, DISPLAY -> {
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
            case TERMINAL, DISPLAY -> {
            }
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

    private List<String> activeChannels() {
        List<String> channels = new ArrayList<>();
        switch (panelType()) {
            case GAUGE -> {
                for (RBMKPanelPlanner.GaugeUnit unit : gauges) channels.add(unit.rtty());
            }
            case GRAPH -> {
                for (RBMKPanelPlanner.GraphUnit unit : graphs) channels.add(unit.rtty());
            }
            case INDICATOR -> {
                for (RBMKPanelPlanner.IndicatorUnit unit : indicators) channels.add(unit.rtty());
            }
            case KEYPAD -> {
                for (RBMKPanelPlanner.KeyUnit unit : keys) channels.add(unit.rtty());
            }
            case LEVER -> {
                for (RBMKPanelPlanner.LeverUnit unit : levers) channels.add(unit.rtty());
            }
            case NUMITRON -> {
                for (RBMKPanelPlanner.NumitronUnit unit : numitrons) channels.add(unit.rtty());
            }
            case TERMINAL, DISPLAY -> {
            }
        }
        return channels.stream().distinct().toList();
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
