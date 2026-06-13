package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.menu.BasicMachineMenu;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.PressRecipe;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.hbm.ntm.item.ItemPressStamp;

import java.util.List;

public class BasicMachineBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyLoadedTile {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_TICKS_EXISTED = "TicksExisted";
    private static final String TAG_BURN_TIME = "BurnTime";
    private static final String TAG_SPEED = "Speed";
    private static final String TAG_PRESS = "Press";
    private static final String TAG_RETRACTING = "Retracting";
    private static final String TAG_DELAY = "Delay";

    public static final int MAX_SPEED = 400;
    public static final int PROGRESS_AT_MAX = 25;
    public static final int MAX_PRESS = 200;
    public static final int FUEL_PER_OPERATION = 200;

    public static final int SLOT_FUEL = 0;
    public static final int SLOT_STAMP = 1;
    public static final int SLOT_INPUT = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_STORAGE_START = 4;
    public static final int SLOT_STORAGE_END = 13;

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final ItemStackHandler items = new ItemStackHandler(13) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_FUEL) {
                return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
            }
            if (slot == SLOT_STAMP) {
                return stack.getItem() instanceof ItemPressStamp;
            }
            return slot != SLOT_OUTPUT;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };
    private final LazyOptional<ItemStackHandler> itemHandler = LazyOptional.of(() -> items);

    private int burnTime;
    private int speed;
    private int press;
    private boolean retracting;
    private int delay;
    private long ticksExisted;
    private double renderPress;
    private double lastPress;
    private int syncPress;
    private int turnProgress;
    private boolean clientRenderInitialized;

    public BasicMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_MACHINE.get(), pos, state);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BasicMachineBlockEntity blockEntity) {
        blockEntity.ticksExisted++;
        PressRecipe recipe = blockEntity.findMatchingPressRecipe(level);
        boolean canProcess = recipe != null;

        if ((canProcess || blockEntity.retracting) && blockEntity.burnTime >= FUEL_PER_OPERATION) {
            blockEntity.speed = Math.min(MAX_SPEED, blockEntity.speed + 1);
        } else {
            blockEntity.speed = Math.max(0, blockEntity.speed - 1);
        }

        if (canProcess && blockEntity.burnTime < FUEL_PER_OPERATION) {
            blockEntity.consumeFuel();
        }

        if (blockEntity.delay > 0) {
            blockEntity.delay--;
        } else {
            int stampSpeed = blockEntity.speed * PROGRESS_AT_MAX / MAX_SPEED;
            if (blockEntity.retracting) {
                blockEntity.press -= stampSpeed;
                if (blockEntity.press <= 0) {
                    blockEntity.press = 0;
                    blockEntity.retracting = false;
                    blockEntity.delay = 5;
                }
            } else if (canProcess && blockEntity.burnTime >= FUEL_PER_OPERATION) {
                blockEntity.press += stampSpeed;
                if (blockEntity.press >= MAX_PRESS) {
                    blockEntity.finishPressRecipe(recipe);
                    blockEntity.press = MAX_PRESS;
                    blockEntity.retracting = true;
                    blockEntity.delay = 5;
                    blockEntity.burnTime -= FUEL_PER_OPERATION;
                }
            } else if (blockEntity.press > 0) {
                blockEntity.retracting = true;
            }
        }

        blockEntity.setChanged();
        blockEntity.networkPackNT(50);
        if (blockEntity.isPressActiveForSync(canProcess) || blockEntity.ticksExisted % 10 == 0) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BasicMachineBlockEntity blockEntity) {
        blockEntity.ticksExisted++;
        blockEntity.lastPress = blockEntity.renderPress;
        if (blockEntity.turnProgress > 0) {
            blockEntity.renderPress = blockEntity.renderPress
                    + ((blockEntity.syncPress - blockEntity.renderPress) / (double) blockEntity.turnProgress);
            blockEntity.turnProgress--;
        } else {
            blockEntity.renderPress = blockEntity.syncPress;
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return press;
    }

    public int getMaxProgress() {
        return MAX_PRESS;
    }

    public void setProgress(int progress) {
        this.press = progress;
    }

    public void setMaxProgress(int maxProgress) {
    }

    public long getTicksExisted() {
        return ticksExisted;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getPress() {
        return press;
    }

    public void setPress(int press) {
        this.press = press;
    }

    public double getInterpolatedPress(float partialTick) {
        return Mth.lerp(partialTick, lastPress, renderPress);
    }

    public int getStoredOperations() {
        return burnTime / FUEL_PER_OPERATION;
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos).inflate(0.25D, 2.0D, 0.25D);
    }

    public ItemStack getRenderStack() {
        return items.getStackInSlot(SLOT_INPUT);
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putLong(TAG_TICKS_EXISTED, ticksExisted);
        tag.putInt(TAG_BURN_TIME, burnTime);
        tag.putInt(TAG_SPEED, speed);
        tag.putInt(TAG_PRESS, press);
        tag.putBoolean(TAG_RETRACTING, retracting);
        tag.putInt(TAG_DELAY, delay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        ticksExisted = tag.getLong(TAG_TICKS_EXISTED);
        burnTime = tag.getInt(TAG_BURN_TIME);
        speed = tag.getInt(TAG_SPEED);
        int loadedPress = tag.getInt(TAG_PRESS);
        press = loadedPress;
        if (level != null && level.isClientSide) {
            boolean targetChanged = loadedPress != syncPress;
            syncPress = loadedPress;
            if (!clientRenderInitialized) {
                renderPress = loadedPress;
                lastPress = loadedPress;
                clientRenderInitialized = true;
            } else if (targetChanged) {
                turnProgress = 2;
            }
        }
        retracting = tag.getBoolean(TAG_RETRACTING);
        delay = tag.getInt(TAG_DELAY);
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hbm_ntm_rebirth.machine_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BasicMachineMenu(containerId, inventory, this);
    }

    private PressRecipe findMatchingPressRecipe(Level level) {
        SimpleContainer container = new SimpleContainer(items.getStackInSlot(SLOT_INPUT), items.getStackInSlot(SLOT_STAMP));
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.PRESS.type().get()).stream()
                .filter(recipe -> recipe.matches(container, level))
                .filter(this::canFitOutput)
                .findFirst()
                .orElse(null);
    }

    private boolean isPressActiveForSync(boolean canProcess) {
        return canProcess || retracting || press > 0 || speed > 0 || delay > 0;
    }

    private void consumeFuel() {
        ItemStack fuel = items.getStackInSlot(SLOT_FUEL);
        int fuelTime = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
        if (fuelTime <= 0) {
            return;
        }

        burnTime += fuelTime;
        ItemStack remainder = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        if (fuel.isEmpty() && !remainder.isEmpty()) {
            items.setStackInSlot(SLOT_FUEL, remainder);
        }
    }

    private boolean canFitOutput(PressRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, result);
    }

    private void finishPressRecipe(PressRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, result);

        level.playSound(null, worldPosition, ModSounds.BLOCK_PRESS_OPERATE.get(), SoundSource.BLOCKS, 1.5F, 1.0F);

        items.extractItem(SLOT_INPUT, 1, false);
        ItemStack stamp = items.getStackInSlot(SLOT_STAMP);
        if (stamp.isDamageableItem()) {
            stamp.hurt(1, level.random, null);
            if (stamp.getDamageValue() >= stamp.getMaxDamage()) {
                items.setStackInSlot(SLOT_STAMP, ItemStack.EMPTY);
            }
        }
    }
}
