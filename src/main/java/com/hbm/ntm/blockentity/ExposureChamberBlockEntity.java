package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.ExposureChamberMenu;
import com.hbm.ntm.recipe.ExposureChamberRecipe;
import com.hbm.ntm.recipe.ExposureChamberRecipeRuntime;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExposureChamberBlockEntity extends HbmEnergyBlockEntity implements MenuProvider {
    public static final int SLOT_PARTICLE = 0;
    public static final int SLOT_PARTICLE_CACHE = 1;
    public static final int SLOT_CONTAINER = 2;
    public static final int SLOT_INGREDIENT = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int SLOT_BATTERY = 5;
    public static final int SLOT_UPGRADE_0 = 6;
    public static final int SLOT_UPGRADE_1 = 7;
    public static final int SLOT_COUNT = 8;

    public static final long MAX_POWER = 1_000_000L;
    public static final int PROCESS_TIME_BASE = 200;
    public static final int CONSUMPTION_BASE = 10_000;
    public static final int MAX_PARTICLES = 8;

    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);
    private static final String TAG_ITEMS = "items";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_PARTICLE -> isParticleInput(stack);
                case SLOT_INGREDIENT -> isIngredientInput(stack);
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_UPGRADE_0, SLOT_UPGRADE_1 -> stack.getItem() instanceof ItemMachineUpgrade;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private int progress;
    private int processTime = PROCESS_TIME_BASE;
    private int consumption = CONSUMPTION_BASE;
    private int savedParticles;
    private boolean isOn;
    private float rotation;
    private float prevRotation;

    public ExposureChamberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXPOSURE_CHAMBER.get(), pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExposureChamberBlockEntity chamber) {
        long oldPower = chamber.energy.getPower();
        int oldProgress = chamber.progress;
        int oldProcessTime = chamber.processTime;
        int oldConsumption = chamber.consumption;
        int oldSaved = chamber.savedParticles;
        boolean oldOn = chamber.isOn;

        HbmEnergyUtil.chargeStorageFromItem(chamber.items.getStackInSlot(SLOT_BATTERY),
                chamber.energy, chamber.energy.getReceiverSpeed());
        if (level.getGameTime() % 20L == 0L) {
            chamber.subscribeEnergyReceiverToPorts();
        }

        chamber.updateUpgradeFactors();
        chamber.isOn = false;
        chamber.loadParticleCache(level);
        chamber.processRecipe(level);
        if (chamber.savedParticles <= 0) {
            chamber.items.setStackInSlot(SLOT_PARTICLE_CACHE, ItemStack.EMPTY);
        }

        boolean changed = oldPower != chamber.energy.getPower()
                || oldProgress != chamber.progress
                || oldProcessTime != chamber.processTime
                || oldConsumption != chamber.consumption
                || oldSaved != chamber.savedParticles
                || oldOn != chamber.isOn;
        chamber.networkPackNT(50);
        if (changed) {
            chamber.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ExposureChamberBlockEntity chamber) {
        chamber.prevRotation = chamber.rotation;
        if (chamber.isOn) {
            chamber.rotation += 10.0F;
            if (chamber.rotation >= 720.0F) {
                chamber.rotation -= 720.0F;
                chamber.prevRotation -= 720.0F;
            }
        }
    }

    private void updateUpgradeFactors() {
        LegacyMachineUpgradeManager.Levels levels = LegacyMachineUpgradeManager.checkSlots(items,
                SLOT_UPGRADE_0, SLOT_UPGRADE_1, VALID_UPGRADES);
        int speedLevel = levels.getLevel(UpgradeType.SPEED);
        int powerLevel = levels.getLevel(UpgradeType.POWER);
        int overdriveLevel = levels.getLevel(UpgradeType.OVERDRIVE);

        consumption = CONSUMPTION_BASE;
        processTime = PROCESS_TIME_BASE - PROCESS_TIME_BASE / 4 * speedLevel;
        consumption *= (speedLevel / 2 + 1);
        processTime *= (powerLevel / 2 + 1);
        consumption /= (powerLevel + 1);
        processTime /= (overdriveLevel + 1);
        consumption *= (overdriveLevel * 2 + 1);
        processTime = Math.max(1, processTime);
        consumption = Math.max(1, consumption);
    }

    private void loadParticleCache(Level level) {
        if (!items.getStackInSlot(SLOT_PARTICLE_CACHE).isEmpty()
                || items.getStackInSlot(SLOT_PARTICLE).isEmpty()
                || items.getStackInSlot(SLOT_INGREDIENT).isEmpty()
                || savedParticles > 0) {
            return;
        }
        ExposureChamberRecipe recipe = ExposureChamberRecipeRuntime.find(level,
                items.getStackInSlot(SLOT_PARTICLE), items.getStackInSlot(SLOT_INGREDIENT));
        if (recipe == null) {
            return;
        }

        ItemStack container = items.getStackInSlot(SLOT_PARTICLE).getCraftingRemainingItem();
        if (!container.isEmpty() && !canMergeContainer(container)) {
            return;
        }
        if (!container.isEmpty()) {
            mergeContainer(container);
        }
        ItemStack cached = items.getStackInSlot(SLOT_PARTICLE).copy();
        cached.setCount(1);
        items.setStackInSlot(SLOT_PARTICLE_CACHE, cached);
        items.extractItem(SLOT_PARTICLE, 1, false);
        savedParticles = MAX_PARTICLES;
    }

    private void processRecipe(Level level) {
        if (items.getStackInSlot(SLOT_PARTICLE_CACHE).isEmpty()
                || savedParticles <= 0
                || energy.getPower() < consumption) {
            progress = 0;
            return;
        }
        ExposureChamberRecipe recipe = ExposureChamberRecipeRuntime.find(level,
                items.getStackInSlot(SLOT_PARTICLE_CACHE), items.getStackInSlot(SLOT_INGREDIENT));
        if (recipe == null || !canOutput(recipe.output())) {
            progress = 0;
            return;
        }

        progress++;
        energy.setPower(energy.getPower() - consumption);
        isOn = true;
        if (progress >= processTime) {
            progress = 0;
            savedParticles--;
            items.extractItem(SLOT_INGREDIENT, recipe.ingredient().count(), false);
            mergeOutput(recipe.output());
        }
    }

    private boolean isParticleInput(ItemStack stack) {
        if (level == null) {
            return true;
        }
        if (!items.getStackInSlot(SLOT_PARTICLE).isEmpty()) {
            return true;
        }
        ItemStack particle = !items.getStackInSlot(SLOT_PARTICLE_CACHE).isEmpty()
                ? items.getStackInSlot(SLOT_PARTICLE_CACHE)
                : items.getStackInSlot(SLOT_PARTICLE);
        if (particle.isEmpty() && !items.getStackInSlot(SLOT_INGREDIENT).isEmpty()) {
            return ExposureChamberRecipeRuntime.find(level, stack, items.getStackInSlot(SLOT_INGREDIENT)) != null;
        }
        if (particle.isEmpty() && items.getStackInSlot(SLOT_INGREDIENT).isEmpty()) {
            return ExposureChamberRecipeRuntime.isParticle(level, stack);
        }
        return ExposureChamberRecipeRuntime.isParticle(level, stack);
    }

    private boolean isIngredientInput(ItemStack stack) {
        if (level == null) {
            return true;
        }
        if (!items.getStackInSlot(SLOT_INGREDIENT).isEmpty()) {
            return true;
        }
        ItemStack particle = !items.getStackInSlot(SLOT_PARTICLE_CACHE).isEmpty()
                ? items.getStackInSlot(SLOT_PARTICLE_CACHE)
                : items.getStackInSlot(SLOT_PARTICLE);
        if (!particle.isEmpty()) {
            return ExposureChamberRecipeRuntime.find(level, particle, stack) != null;
        }
        return ExposureChamberRecipeRuntime.isIngredient(level, stack);
    }

    private boolean canMergeContainer(ItemStack container) {
        ItemStack existing = items.getStackInSlot(SLOT_CONTAINER);
        return existing.isEmpty()
                || ItemStack.isSameItemSameTags(existing, container)
                && existing.getCount() + container.getCount() <= existing.getMaxStackSize();
    }

    private void mergeContainer(ItemStack container) {
        ItemStack existing = items.getStackInSlot(SLOT_CONTAINER);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_CONTAINER, container.copy());
        } else {
            existing.grow(container.getCount());
            items.setStackInSlot(SLOT_CONTAINER, existing);
        }
    }

    private boolean canOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        return existing.isEmpty()
                || ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    private void mergeOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, output.copy());
        } else {
            existing.grow(output.getCount());
            items.setStackInSlot(SLOT_OUTPUT, existing);
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction rot = facing.getCounterClockWise();
        return List.of(
                port(rot, 7, facing, 2, facing),
                port(rot, 7, facing, -2, facing.getOpposite()),
                port(rot, 8, facing, 2, facing),
                port(rot, 8, facing, -2, facing.getOpposite()),
                port(rot, 9, facing, 0, rot));
    }

    private static EnergyPort port(Direction rot, int rotDistance, Direction facing, int facingDistance,
            Direction side) {
        return EnergyPort.of(
                rot.getStepX() * rotDistance + facing.getStepX() * facingDistance,
                0,
                rot.getStepZ() * rotDistance + facing.getStepZ() * facingDistance,
                side);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return Math.max(1, processTime);
    }

    public int getConsumption() {
        return consumption;
    }

    public int getSavedParticles() {
        return savedParticles;
    }

    public boolean isOn() {
        return isOn;
    }

    public float getRotation(float partialTick) {
        return prevRotation + (rotation - prevRotation) * partialTick;
    }

    public float getPrevRotation() {
        return prevRotation;
    }

    public float getRawRotation() {
        return rotation;
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            if (slot == SLOT_PARTICLE_CACHE) {
                items.setStackInSlot(slot, ItemStack.EMPTY);
                continue;
            }
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return drops;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.exposureChamber", "Exposure Chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ExposureChamberMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        tag.putLong("power", energy.getPower());
        tag.putInt("progress", progress);
        tag.putInt("savedParticles", savedParticles);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadItems(tag);
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        progress = tag.getInt("progress");
        savedParticles = tag.getInt("savedParticles");
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt("progress", progress);
        tag.putInt("processTime", processTime);
        tag.putInt("consumption", consumption);
        tag.putInt("savedParticles", savedParticles);
        tag.putBoolean("isOn", isOn);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        progress = tag.getInt("progress");
        processTime = tag.contains("processTime") ? Math.max(1, tag.getInt("processTime")) : PROCESS_TIME_BASE;
        consumption = tag.contains("consumption") ? Math.max(1, tag.getInt("consumption")) : CONSUMPTION_BASE;
        savedParticles = tag.getInt("savedParticles");
        isOn = tag.getBoolean("isOn");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
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

    private void loadItems(CompoundTag tag) {
        if (tag.contains(HbmInventoryMenuHelper.LEGACY_ITEMS_TAG, Tag.TAG_LIST)
                || tag.contains("Items", Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 4;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped >= 0 ? items.getStackInSlot(mapped) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            return mapped == SLOT_PARTICLE || mapped == SLOT_INGREDIENT
                    ? items.insertItem(mapped, stack, simulate)
                    : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            return mapped == SLOT_CONTAINER || mapped == SLOT_OUTPUT
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped >= 0 ? items.getSlotLimit(mapped) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = mapExternalSlot(slot);
            return mapped >= 0 && items.isItemValid(mapped, stack);
        }

        private int mapExternalSlot(int slot) {
            return switch (slot) {
                case 0 -> SLOT_PARTICLE;
                case 1 -> SLOT_CONTAINER;
                case 2 -> SLOT_INGREDIENT;
                case 3 -> SLOT_OUTPUT;
                default -> -1;
            };
        }
    }
}
