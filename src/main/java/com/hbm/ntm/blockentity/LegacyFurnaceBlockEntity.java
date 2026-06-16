package com.hbm.ntm.blockentity;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.fuel.LegacyBurnTimeModule;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.LegacyFurnaceMenu;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LegacyFurnaceBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyLoadedTile {
    private static final String TAG_INVENTORY = "items";
    private static final int IRON_BASE_TIME = 160;
    private static final int STEEL_PROCESS_TIME = 40_000;
    private static final int STEEL_MAX_HEAT = 100_000;
    private static final double STEEL_DIFFUSION = 0.05D;
    private static final Map<UpgradeType, Integer> IRON_UPGRADES = Map.of(UpgradeType.SPEED, 3);

    private final Kind kind;
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final ItemStackHandler items = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (kind) {
                case IRON -> switch (slot) {
                    case 0 -> smeltingResult(stack).isPresent();
                    case 1, 2 -> burnModule.getBurnTime(stack) > 0;
                    case 4 -> stack.getItem() instanceof ItemMachineUpgrade;
                    default -> false;
                };
                case STEEL -> slot < 3 && smeltingResult(stack).isPresent();
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private final LegacyBurnTimeModule burnModule = new LegacyBurnTimeModule()
            .setLigniteTimeMod(1.25D)
            .setCoalTimeMod(1.25D)
            .setCokeTimeMod(1.5D)
            .setSolidTimeMod(2.0D)
            .setRocketTimeMod(2.0D)
            .setBalefireTimeMod(2.0D);

    private int maxBurnTime;
    private int burnTime;
    private int ironProgress;
    private int ironProcessingTime = IRON_BASE_TIME;
    private boolean wasOn;
    private int heat;
    private int[] steelProgress = new int[] {0, 0, 0};
    private int[] steelBonus = new int[] {0, 0, 0};
    private ItemStack[] lastItems = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    public LegacyFurnaceBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, inferKind(state));
    }

    public LegacyFurnaceBlockEntity(BlockPos pos, BlockState state, Kind kind) {
        super(ModBlockEntities.LEGACY_FURNACE.get(), pos, state);
        this.kind = kind;
    }

    private static Kind inferKind(BlockState state) {
        if (state.is(ModBlocks.FURNACE_STEEL.get())) {
            return Kind.STEEL;
        }
        return Kind.IRON;
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyFurnaceBlockEntity furnace) {
        boolean changed = furnace.kind == Kind.IRON ? furnace.tickIron(level) : furnace.tickSteel(level, pos);
        if (changed) {
            furnace.setChanged();
        }
        furnace.networkPackNT(25);
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LegacyFurnaceBlockEntity furnace) {
        if (!furnace.wasOn) {
            return;
        }
        Direction dir = state.hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        Direction rot = dir.getClockWise();
        if (furnace.kind == Kind.IRON) {
            double offset = furnace.ironProgress % 2 == 0 ? 1.0D : 0.5D;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    pos.getX() + 0.5D - dir.getStepX() * offset - rot.getStepX() * 0.1875D,
                    pos.getY() + 2.0D,
                    pos.getZ() + 0.5D - dir.getStepZ() * offset - rot.getStepZ() * 0.1875D,
                    0.0D, 0.01D, 0.0D);
            if (furnace.ironProgress % 5 == 0) {
                double rand = level.random.nextDouble();
                level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                        pos.getX() + 0.5D + dir.getStepX() * 0.25D + rot.getStepX() * rand,
                        pos.getY() + 0.25D + level.random.nextDouble() * 0.25D,
                        pos.getZ() + 0.5D + dir.getStepZ() * 0.25D + rot.getStepZ() * rand,
                        0.0D, 0.0D, 0.0D);
            }
            return;
        }

        level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                pos.getX() + 0.5D - dir.getStepX() * 1.125D - rot.getStepX() * 0.75D,
                pos.getY() + 2.625D,
                pos.getZ() + 0.5D - dir.getStepZ() * 1.125D - rot.getStepZ() * 0.75D,
                0.0D, 0.05D, 0.0D);
        if (level.random.nextInt(20) == 0) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                    pos.getX() + 0.5D + dir.getStepX() * 0.75D,
                    pos.getY() + 2.0D,
                    pos.getZ() + 0.5D + dir.getStepZ() * 0.75D,
                    0.0D, 0.05D, 0.0D);
        }
        if (level.random.nextInt(15) == 0) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.LAVA,
                    pos.getX() + 0.5D + dir.getStepX() * 1.5D + rot.getStepX() * (level.random.nextDouble() - 0.5D),
                    pos.getY() + 0.75D,
                    pos.getZ() + 0.5D + dir.getStepZ() * 1.5D + rot.getStepZ() * (level.random.nextDouble() - 0.5D),
                    dir.getStepX() * 0.5D, 0.05D, dir.getStepZ() * 0.5D);
        }
    }

    private boolean tickIron(Level level) {
        int oldBurn = burnTime;
        int oldProgress = ironProgress;
        boolean oldOn = wasOn;
        LegacyMachineUpgradeManager.Levels levels = LegacyMachineUpgradeManager.checkSlots(items, 4, 4,
                IRON_UPGRADES);
        ironProcessingTime = IRON_BASE_TIME - ((IRON_BASE_TIME / 2)
                * Math.min(levels.getLevel(UpgradeType.SPEED), 3) / 3);
        wasOn = false;

        if (burnTime <= 0) {
            consumeFuel();
        }

        if (canSmeltIron()) {
            wasOn = true;
            ironProgress++;
            burnTime--;
            if (ironProgress >= ironProcessingTime) {
                smeltIron();
                ironProgress = 0;
            }
            if (level.getGameTime() % 20L == 0L) {
                PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT,
                        PollutionHandler.SOOT_PER_SECOND);
            }
        } else {
            ironProgress = 0;
        }
        return oldBurn != burnTime || oldProgress != ironProgress || oldOn != wasOn;
    }

    private boolean tickSteel(Level level, BlockPos pos) {
        int oldHeat = heat;
        boolean oldOn = wasOn;
        int[] oldProgress = steelProgress.clone();
        int[] oldBonus = steelBonus.clone();
        tryPullHeat(level, pos);
        wasOn = false;
        int burn = (heat - STEEL_MAX_HEAT / 3) / 10;

        for (int i = 0; i < 3; i++) {
            ItemStack input = items.getStackInSlot(i);
            if (input.isEmpty() || lastItems[i].isEmpty() || !ItemStack.isSameItemSameTags(input, lastItems[i])) {
                steelProgress[i] = 0;
                steelBonus[i] = 0;
            }
            if (canSmeltSteel(i)) {
                steelProgress[i] += burn;
                heat -= burn;
                wasOn = true;
                if (level.getGameTime() % 20L == 0L) {
                    PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT,
                            PollutionHandler.SOOT_PER_SECOND * 2.0F);
                }
            }
            lastItems[i] = input.copy();
            if (steelProgress[i] >= STEEL_PROCESS_TIME) {
                smeltSteel(i);
                steelProgress[i] = 0;
            }
        }
        return oldHeat != heat || oldOn != wasOn
                || !java.util.Arrays.equals(oldProgress, steelProgress)
                || !java.util.Arrays.equals(oldBonus, steelBonus);
    }

    private void consumeFuel() {
        for (int slot = 1; slot <= 2; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            int fuel = burnModule.getBurnTime(stack);
            if (fuel <= 0) {
                continue;
            }
            maxBurnTime = burnTime = fuel;
            ItemStack remainder = stack.getCraftingRemainingItem();
            stack.shrink(1);
            items.setStackInSlot(slot, stack.isEmpty() ? remainder : stack);
            return;
        }
    }

    private boolean canSmeltIron() {
        if (burnTime <= 0) {
            return false;
        }
        return smeltingResult(items.getStackInSlot(0)).filter(result -> canMergeOutput(3, result)).isPresent();
    }

    private void smeltIron() {
        smeltingResult(items.getStackInSlot(0)).ifPresent(result -> {
            mergeOutput(3, result);
            items.extractItem(0, 1, false);
        });
    }

    private boolean canSmeltSteel(int index) {
        if (heat < STEEL_MAX_HEAT / 3) {
            return false;
        }
        return smeltingResult(items.getStackInSlot(index))
                .filter(result -> canMergeOutput(index + 3, result)).isPresent();
    }

    private void smeltSteel(int index) {
        smeltingResult(items.getStackInSlot(index)).ifPresent(result -> {
            mergeOutput(index + 3, result);
            addBonus(items.getStackInSlot(index), index);
            while (steelBonus[index] >= 100) {
                ItemStack output = items.getStackInSlot(index + 3);
                output.grow(Math.min(result.getCount(), output.getMaxStackSize() - output.getCount()));
                items.setStackInSlot(index + 3, output);
                steelBonus[index] -= 100;
            }
            items.extractItem(index, 1, false);
        });
    }

    private void addBonus(ItemStack stack, int index) {
        for (String name : HbmItemStackUtil.getTagNames(stack)) {
            String path = name.contains(":") ? name.substring(name.indexOf(':') + 1) : name;
            if (path.startsWith("ore")) {
                steelBonus[index] += 25;
                return;
            }
            if (path.startsWith("log") || path.equals("any_tar") || path.equals("anyTar")) {
                steelBonus[index] += 50;
                return;
            }
        }
    }

    private void tryPullHeat(Level level, BlockPos pos) {
        if (heat >= STEEL_MAX_HEAT) {
            return;
        }
        BlockEntity below = level.getBlockEntity(pos.below());
        if (below instanceof HeatSource source) {
            int diff = source.getHeatStored() - heat;
            if (diff > 0) {
                diff = (int) Math.ceil(diff * STEEL_DIFFUSION);
                source.useUpHeat(diff);
                heat = Math.min(STEEL_MAX_HEAT, heat + diff);
                return;
            }
            if (diff == 0) {
                return;
            }
        }
        heat = Math.max(heat - Math.max(heat / 1000, 1), 0);
    }

    private Optional<ItemStack> smeltingResult(ItemStack input) {
        if (level == null || input.isEmpty()) {
            return Optional.empty();
        }
        Container container = new SimpleContainer(input);
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> recipe.getResultItem(level.registryAccess()))
                .map(stack -> stack.copyWithCount(stack.getCount()));
    }

    private boolean canMergeOutput(int slot, ItemStack result) {
        ItemStack existing = items.getStackInSlot(slot);
        return existing.isEmpty()
                || ItemStack.isSameItemSameTags(existing, result)
                && existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    private void mergeOutput(int slot, ItemStack result) {
        ItemStack existing = items.getStackInSlot(slot);
        if (existing.isEmpty()) {
            items.setStackInSlot(slot, result.copy());
        } else {
            existing.grow(result.getCount());
            items.setStackInSlot(slot, existing);
        }
    }

    public Kind kind() {
        return kind;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getIronProgress() {
        return ironProgress;
    }

    public int getIronProcessingTime() {
        return Math.max(1, ironProcessingTime);
    }

    public boolean wasOn() {
        return wasOn;
    }

    public int getHeat() {
        return heat;
    }

    public int getSteelProgress(int index) {
        return index >= 0 && index < steelProgress.length ? steelProgress[index] : 0;
    }

    public int getSteelBonus(int index) {
        return index >= 0 && index < steelBonus.length ? steelBonus[index] : 0;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        tag.putString("kind", kind.name());
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putInt("maxBurnTime", maxBurnTime);
        tag.putInt("burnTime", burnTime);
        tag.putInt("progress", ironProgress);
        tag.putInt("processingTime", ironProcessingTime);
        tag.putBoolean("wasOn", wasOn);
        tag.putInt("heat", heat);
        tag.putIntArray("steelProgress", steelProgress);
        tag.putIntArray("bonus", steelBonus);
        ListTag list = new ListTag();
        for (int i = 0; i < lastItems.length; i++) {
            if (!lastItems[i].isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("lastItem", (byte) i);
                lastItems[i].save(entry);
                list.add(entry);
            }
        }
        tag.put("lastItems", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        maxBurnTime = tag.getInt("maxBurnTime");
        burnTime = tag.getInt("burnTime");
        ironProgress = tag.getInt("progress");
        ironProcessingTime = Math.max(1, tag.getInt("processingTime"));
        wasOn = tag.getBoolean("wasOn");
        heat = tag.getInt("heat");
        steelProgress = normalizedIntArray(tag.getIntArray(tag.contains("steelProgress") ? "steelProgress" : "progress"));
        steelBonus = normalizedIntArray(tag.getIntArray("bonus"));
        lastItems = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
        ListTag list = tag.getList("lastItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("lastItem");
            if (slot >= 0 && slot < lastItems.length) {
                lastItems[slot] = ItemStack.of(entry);
            }
        }
    }

    private static int[] normalizedIntArray(int[] source) {
        int[] result = new int[] {0, 0, 0};
        for (int i = 0; i < Math.min(source.length, result.length); i++) {
            result[i] = source[i];
        }
        return result;
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
        data.writeNbt(saveWithoutMetadata());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            load(tag);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    @Override
    public Component getDisplayName() {
        return kind == Kind.STEEL
                ? Component.translatableWithFallback("container.furnaceSteel", "Steel Furnace")
                : Component.translatableWithFallback("container.furnaceIron", "Iron Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LegacyFurnaceMenu(containerId, inventory, this);
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
            return kind == Kind.IRON ? 4 : 6;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            if (kind == Kind.IRON) {
                if (mapped < 0 || mapped == 3) {
                    return stack;
                }
                return items.insertItem(mapped, stack, simulate);
            }
            if (mapped >= 0 && mapped < 3) {
                return items.insertItem(mapped, stack, simulate);
            }
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            boolean canExtract = kind == Kind.IRON ? mapped == 3 : mapped > 2;
            return canExtract ? items.extractItem(mapped, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = mapExternalSlot(slot);
            return mapped >= 0 && items.isItemValid(mapped, stack);
        }

        private int mapExternalSlot(int slot) {
            if (kind == Kind.IRON) {
                return switch (slot) {
                    case 0 -> 0;
                    case 1 -> 1;
                    case 2 -> 2;
                    case 3 -> 3;
                    default -> -1;
                };
            }
            return slot >= 0 && slot < 6 ? slot : -1;
        }
    }

    public enum Kind {
        IRON,
        STEEL
    }
}
