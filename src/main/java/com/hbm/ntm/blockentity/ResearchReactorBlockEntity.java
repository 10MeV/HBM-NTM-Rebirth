package com.hbm.ntm.blockentity;

import com.hbm.ntm.menu.ResearchReactorMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.api.tile.IInfoProviderEC;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.recipe.ResearchReactorFuelRuntime;
import com.hbm.ntm.recipe.ResearchReactorFuelRuntime.FuelSpec;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResearchReactorBlockEntity extends BlockEntity
        implements MenuProvider, HbmLegacyLoadedTile, HbmLegacyControlReceiver, IInfoProviderEC {
    private static final String TAG_INVENTORY = "items";
    private static final String TAG_HEAT = "heat";
    private static final String TAG_WATER = "water";
    private static final String TAG_LEVEL = "level";
    private static final String TAG_TARGET_LEVEL = "targetLevel";
    private static final String TAG_SLOT_FLUX = "slotFlux";
    private static final String TAG_TOTAL_FLUX = "totalFlux";
    private static final int SLOT_COUNT = 12;
    private static final int MAX_HEAT = 50_000;
    private static final double ROD_SPEED = 0.04D;
    private static final int[][] NEIGHBORS = {
            {1, 5}, {0, 6}, {3, 7}, {2, 4, 8}, {3, 9}, {0, 6, 10},
            {1, 5, 11}, {2, 8}, {3, 7, 9}, {4, 8}, {5, 11}, {6, 10}
    };

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final int[] slotFlux = new int[SLOT_COUNT];
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < SLOT_COUNT && ResearchReactorFuelRuntime.isFuel(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private double lastLevel;
    private double rodLevel;
    private double targetLevel;
    private int heat;
    private int water;
    private int totalFlux;

    public ResearchReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_REACTOR.get(), pos, state);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ResearchReactorBlockEntity reactor) {
        if (level.isClientSide) {
            reactor.lastLevel = reactor.rodLevel;
            return;
        }
        reactor.rodControl();
        reactor.totalFlux = 0;
        boolean changed = false;
        if (reactor.rodLevel > 0.0D) {
            changed |= reactor.reaction();
        }
        changed |= reactor.cool();
        if (reactor.heat > MAX_HEAT) {
            reactor.explode(level);
            return;
        }
        if (reactor.rodLevel > 0.0D && reactor.heat > 0 && !reactor.fullyShielded()) {
            ChunkRadiationManager.incrementRadiation(level, pos, reactor.heat / (float) MAX_HEAT * 50.0F);
        }
        reactor.networkPackNT(150);
        if (changed || level.getGameTime() % 20L == 0L) {
            reactor.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void rodControl() {
        if (rodLevel < targetLevel) {
            rodLevel = Math.min(targetLevel, rodLevel + ROD_SPEED);
        } else if (rodLevel > targetLevel) {
            rodLevel = Math.max(targetLevel, rodLevel - ROD_SPEED);
        }
    }

    private boolean reaction() {
        boolean changed = false;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = items.getStackInSlot(i);
            FuelSpec spec = ResearchReactorFuelRuntime.fuelFor(stack);
            if (spec == null) {
                slotFlux[i] = 0;
                continue;
            }
            int outFlux = ResearchReactorFuelRuntime.react(stack, slotFlux[i]);
            heat += outFlux * 2;
            totalFlux += outFlux;
            slotFlux[i] = 0;
            if (ResearchReactorFuelRuntime.getLife(stack) > spec.lifetime()) {
                items.setStackInSlot(i, spec.waste().copy());
            }
            int spread = (int) (outFlux * rodLevel);
            for (int neighbor : NEIGHBORS[i]) {
                slotFlux[neighbor] += spread;
            }
            changed = true;
        }
        return changed;
    }

    private boolean cool() {
        if (heat <= 0) {
            return false;
        }
        int oldHeat = heat;
        water = getWaterContacts();
        if (water > 0) {
            heat -= (int) (heat * 0.07F * water / 12.0F);
        } else {
            heat -= 1;
        }
        if (heat < 0) {
            heat = 0;
        }
        return oldHeat != heat;
    }

    private void explode(Level level) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        clearWaterContacts(level);
        level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.explode(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                18.0F, true, Level.ExplosionInteraction.BLOCK);
        level.setBlock(worldPosition, ModBlocks.legacyBlock("deco_steel").get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(worldPosition.above(), ModBlocks.CORIUM_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(worldPosition.above(2), ModBlocks.legacyBlock("deco_steel").get().defaultBlockState(),
                Block.UPDATE_ALL);
        ChunkRadiationManager.incrementRadiation(level, worldPosition, 50.0F);
    }

    private void clearWaterContacts(Level level) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isVertical()) {
                clearIfWater(level, worldPosition.above().relative(direction, 2));
            } else {
                for (int y = 0; y < 3; y++) {
                    clearIfWater(level, worldPosition.relative(direction).above(y));
                }
            }
        }
    }

    private static void clearIfWater(Level level, BlockPos pos) {
        if (level.getFluidState(pos).is(FluidTags.WATER)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public int getWaterContacts() {
        if (level == null) {
            return water;
        }
        int contacts = 0;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isVertical()) {
                if (isWater(worldPosition.above().relative(direction, 2))) {
                    contacts++;
                }
            } else {
                for (int y = 0; y < 3; y++) {
                    if (isWater(worldPosition.relative(direction).above(y))) {
                        contacts++;
                    }
                }
            }
        }
        return contacts;
    }

    public boolean isSubmerged() {
        return isWater(worldPosition.east().above())
                || isWater(worldPosition.west().above())
                || isWater(worldPosition.north().above())
                || isWater(worldPosition.south().above());
    }

    private boolean fullyShielded() {
        return blocksRadiation(worldPosition.east().above())
                && blocksRadiation(worldPosition.west().above())
                && blocksRadiation(worldPosition.north().above())
                && blocksRadiation(worldPosition.south().above());
    }

    private boolean isWater(BlockPos pos) {
        return level != null && level.getFluidState(pos).is(FluidTags.WATER);
    }

    private boolean blocksRadiation(BlockPos pos) {
        if (level == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if ((state.is(Blocks.WATER) || state.getFluidState().is(FluidTags.WATER))
                && state.getFluidState().isSource()) {
            return true;
        }
        Block block = state.getBlock();
        return block == ModBlocks.legacyBlock("block_lead").get()
                || block == ModBlocks.legacyBlock("block_desh").get()
                || block == ModBlocks.REACTOR_RESEARCH.get()
                || block == ModBlocks.MACHINE_REACTOR_BREEDING.get()
                || state.getBlock().getExplosionResistance() >= 100.0F;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getHeat() {
        return heat;
    }

    public int getWater() {
        return water;
    }

    public double getLevelValue() {
        return rodLevel;
    }

    public double getLastLevel() {
        return lastLevel;
    }

    public double getTargetLevel() {
        return targetLevel;
    }

    public int getLevelScaled() {
        return (int) Math.round(rodLevel * 10_000.0D);
    }

    public int getTargetLevelScaled() {
        return (int) Math.round(targetLevel * 10_000.0D);
    }

    public int getTotalFlux() {
        return totalFlux;
    }

    public int getTemperatureDisplay() {
        return (int) Math.round(heat * 0.00002D * 980.0D + 20.0D);
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putDouble(CompatEnergyControl.D_HEAT_C, getTemperatureDisplay());
        data.putInt(CompatEnergyControl.I_FLUX, totalFlux);
        data.putInt(CompatEnergyControl.I_WATER, water);
    }

    public int getSlotFlux(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? slotFlux[slot] : 0;
    }

    public void setTargetLevel(double targetLevel) {
        this.targetLevel = Math.max(0.0D, Math.min(1.0D, targetLevel));
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) < 400.0D;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains(TAG_LEVEL)) {
            setTargetLevel(data.getDouble(TAG_LEVEL));
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putInt(TAG_HEAT, heat);
        tag.putInt(TAG_WATER, water);
        tag.putDouble(TAG_LEVEL, rodLevel);
        tag.putDouble(TAG_TARGET_LEVEL, targetLevel);
        tag.putIntArray(TAG_SLOT_FLUX, slotFlux);
        tag.putInt(TAG_TOTAL_FLUX, totalFlux);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        heat = tag.getInt(TAG_HEAT);
        water = tag.getInt(TAG_WATER);
        rodLevel = tag.getDouble(TAG_LEVEL);
        targetLevel = tag.getDouble(TAG_TARGET_LEVEL);
        Arrays.fill(slotFlux, 0);
        int[] savedFlux = tag.getIntArray(TAG_SLOT_FLUX);
        System.arraycopy(savedFlux, 0, slotFlux, 0, Math.min(savedFlux.length, slotFlux.length));
        totalFlux = tag.getInt(TAG_TOTAL_FLUX);
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

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeInt(heat);
        data.writeByte(water);
        data.writeDouble(rodLevel);
        data.writeDouble(targetLevel);
        data.writeVarIntArray(slotFlux);
        data.writeInt(totalFlux);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        heat = data.readInt();
        water = data.readByte();
        rodLevel = data.readDouble();
        targetLevel = data.readDouble();
        Arrays.fill(slotFlux, 0);
        int[] packetFlux = data.readVarIntArray();
        System.arraycopy(packetFlux, 0, slotFlux, 0, Math.min(packetFlux.length, slotFlux.length));
        totalFlux = data.readInt();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this,
                new AABB(worldPosition.offset(-1, -1, -1), worldPosition.offset(2, 5, 2)));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.reactorResearch", "Research Reactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ResearchReactorMenu(containerId, inventory, this);
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
            ItemStack stack = getStackInSlot(slot);
            return stack.isEmpty() || ResearchReactorFuelRuntime.isFuel(stack)
                    ? ItemStack.EMPTY
                    : items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < SLOT_COUNT && items.isItemValid(slot, stack);
        }
    }
}

