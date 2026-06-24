package com.hbm.ntm.blockentity;

import com.hbm.ntm.menu.ReactorControlMenu;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReactorControlBlockEntity extends BlockEntity
        implements MenuProvider, HbmLegacyLoadedTile, HbmLegacyControlReceiver {
    private static final String TAG_INVENTORY = "items";
    private static final String TAG_LINKED = "isLinked";
    private static final String TAG_LEVEL_LOWER = "levelLower";
    private static final String TAG_LEVEL_UPPER = "levelUpper";
    private static final String TAG_HEAT_LOWER = "heatLower";
    private static final String TAG_HEAT_UPPER = "heatUpper";
    private static final String TAG_FUNCTION = "function";
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && stack.is(ModItems.REACTOR_SENSOR.get());
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private boolean linked;
    private int flux;
    private double reactorLevel;
    private int heat;
    private double levelLower;
    private double levelUpper;
    private double heatLower;
    private double heatUpper;
    private RodFunction function = RodFunction.LINEAR;

    public ReactorControlBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REACTOR_CONTROL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ReactorControlBlockEntity control) {
        control.linked = control.establishLink();
        if (control.linked) {
            double lowerBound = Math.min(control.heatLower, control.heatUpper);
            double upperBound = Math.max(control.heatLower, control.heatUpper);
            double fauxLevel;
            if (control.heat < lowerBound) {
                fauxLevel = control.levelLower;
            } else if (control.heat > upperBound) {
                fauxLevel = control.levelUpper;
            } else {
                fauxLevel = control.getTargetLevel(control.function, control.heat);
            }
            double targetLevel = Mth.clamp(fauxLevel * 0.01D, 0.0D, 1.0D);
            ResearchReactorBlockEntity reactor = control.linkedReactor();
            if (reactor != null && targetLevel != control.reactorLevel) {
                reactor.setTargetLevel(targetLevel);
            }
        }
        control.networkPackNT(150);
        if (level.getGameTime() % 20L == 0L) {
            control.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private boolean establishLink() {
        ResearchReactorBlockEntity reactor = linkedReactor();
        if (reactor == null) {
            flux = 0;
            reactorLevel = 0.0D;
            heat = 0;
            return false;
        }
        flux = reactor.getTotalFlux();
        reactorLevel = reactor.getLevelValue();
        heat = reactor.getHeat();
        return true;
    }

    @Nullable
    private ResearchReactorBlockEntity linkedReactor() {
        if (level == null) {
            return null;
        }
        ItemStack stack = items.getStackInSlot(0);
        CompoundTag tag = stack.getTag();
        if (stack.isEmpty() || !stack.is(ModItems.REACTOR_SENSOR.get()) || tag == null) {
            return null;
        }
        BlockPos sensorPos = new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
        return MultiblockHelper.resolveCoreBlockEntity(level, sensorPos) instanceof ResearchReactorBlockEntity reactor
                ? reactor
                : null;
    }

    public double getTargetLevel(RodFunction function, int heat) {
        return switch (function) {
            case LINEAR -> (heat - heatLower) * ((levelUpper - levelLower) / (heatUpper - heatLower)) + levelLower;
            case LOG -> Math.pow((heat - heatUpper) / (heatLower - heatUpper), 2) * (levelLower - levelUpper)
                    + levelUpper;
            case QUAD -> Math.pow((heat - heatLower) / (heatUpper - heatLower), 2) * (levelUpper - levelLower)
                    + levelLower;
        };
    }

    public int[] getDisplayData() {
        return linked
                ? new int[] { (int) (reactorLevel * 100.0D), flux, getTemperatureDisplay() }
                : new int[] { 0, 0, 0 };
    }

    public int getTemperatureDisplay() {
        return (int) Math.round(heat * 0.00002D * 980.0D + 20.0D);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isLinked() {
        return linked;
    }

    public int getFlux() {
        return flux;
    }

    public double getReactorLevel() {
        return reactorLevel;
    }

    public int getHeat() {
        return heat;
    }

    public double getLevelLower() {
        return levelLower;
    }

    public double getLevelUpper() {
        return levelUpper;
    }

    public double getHeatLower() {
        return heatLower;
    }

    public double getHeatUpper() {
        return heatUpper;
    }

    public RodFunction getFunction() {
        return function;
    }

    public int getLevelLowerInt() {
        return (int) levelLower;
    }

    public int getLevelUpperInt() {
        return (int) levelUpper;
    }

    public int getHeatLowerDiv50() {
        return (int) heatLower / 50;
    }

    public int getHeatUpperDiv50() {
        return (int) heatUpper / 50;
    }

    public int getFunctionOrdinal() {
        return function.ordinal();
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 400.0D;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains(TAG_FUNCTION)) {
            int ordinal = Mth.clamp(data.getInt(TAG_FUNCTION), 0, RodFunction.values().length - 1);
            function = RodFunction.values()[ordinal];
        } else {
            levelLower = data.getDouble(TAG_LEVEL_LOWER);
            levelUpper = data.getDouble(TAG_LEVEL_UPPER);
            heatLower = data.getDouble(TAG_HEAT_LOWER);
            heatUpper = data.getDouble(TAG_HEAT_UPPER);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putBoolean(TAG_LINKED, linked);
        tag.putDouble(TAG_LEVEL_LOWER, levelLower);
        tag.putDouble(TAG_LEVEL_UPPER, levelUpper);
        tag.putDouble(TAG_HEAT_LOWER, heatLower);
        tag.putDouble(TAG_HEAT_UPPER, heatUpper);
        tag.putInt(TAG_FUNCTION, function.ordinal());
        tag.putInt("flux", flux);
        tag.putDouble("level", reactorLevel);
        tag.putInt("heat", heat);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        linked = tag.getBoolean(TAG_LINKED);
        levelLower = tag.getDouble(TAG_LEVEL_LOWER);
        levelUpper = tag.getDouble(TAG_LEVEL_UPPER);
        heatLower = tag.getDouble(TAG_HEAT_LOWER);
        heatUpper = tag.getDouble(TAG_HEAT_UPPER);
        int ordinal = Mth.clamp(tag.getInt(TAG_FUNCTION), 0, RodFunction.values().length - 1);
        function = RodFunction.values()[ordinal];
        flux = tag.getInt("flux");
        reactorLevel = tag.getDouble("level");
        heat = tag.getInt("heat");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeInt(heat);
        data.writeDouble(reactorLevel);
        data.writeInt(flux);
        data.writeBoolean(linked);
        data.writeDouble(levelLower);
        data.writeDouble(levelUpper);
        data.writeDouble(heatLower);
        data.writeDouble(heatUpper);
        data.writeByte(function.ordinal());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        heat = data.readInt();
        reactorLevel = data.readDouble();
        flux = data.readInt();
        linked = data.readBoolean();
        levelLower = data.readDouble();
        levelUpper = data.readDouble();
        heatLower = data.readDouble();
        heatUpper = data.readDouble();
        int ordinal = Mth.clamp(data.readByte(), 0, RodFunction.values().length - 1);
        function = RodFunction.values()[ordinal];
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.reactorControl", "Reactor Remote Control Block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ReactorControlMenu(containerId, inventory, this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    public enum RodFunction {
        LINEAR,
        QUAD,
        LOG
    }
}
