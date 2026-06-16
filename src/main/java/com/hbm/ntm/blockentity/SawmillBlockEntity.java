package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
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

public class SawmillBlockEntity extends BlockEntity
        implements HbmLegacyLoadedTile, LegacyLookOverlayProvider, HbmPersistentBlockState {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_SAWDUST = 2;
    public static final int SLOT_COUNT = 3;
    public static final double DIFFUSION = 0.1D;
    public static final int MIN_HEAT = 100;
    public static final int MAX_HEAT = 300;
    public static final int PROCESSING_TIME = 600;
    public static final int OVERSPEED_WARNING_TICKS = 60;
    public static final int OVERSPEED_LIMIT = 300;

    private static final String TAG_INVENTORY = "items";
    private static final String TAG_HEAT = "heat";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_HAS_BLADE = "hasBlade";
    private static final String TAG_WARN_COOLDOWN = "warnCooldown";
    private static final String TAG_OVERSPEED = "overspeed";
    private static final String TAG_SPIN = "spin";
    private static final String TAG_LAST_SPIN = "lastSpin";
    private static final String TAG_MISSING_BLADE = "missingBlade";
    private static final TagKey<Item> WOODEN_RODS =
            ItemTags.create(new ResourceLocation("forge", "rods/wooden"));

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndUpdate();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_INPUT && canAcceptInput(stack);
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(AccessibleItemHandler::new);

    private int heat;
    private int progress;
    private int warnCooldown;
    private int overspeed;
    private boolean hasBlade = true;
    private float spin;
    private float lastSpin;

    public SawmillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SAWMILL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SawmillBlockEntity sawmill) {
        if (level.isClientSide) {
            return;
        }
        int oldHeat = sawmill.heat;
        int oldProgress = sawmill.progress;
        boolean oldBlade = sawmill.hasBlade;

        if (sawmill.hasBlade) {
            sawmill.tryPullHeat(level, pos);
            if (sawmill.warnCooldown > 0) {
                sawmill.warnCooldown--;
            }
            if (sawmill.heat >= MIN_HEAT) {
                sawmill.processInput(level);
            } else {
                sawmill.progress = 0;
            }
            sawmill.tickOverspeed(level, pos);
        } else {
            sawmill.overspeed = 0;
            sawmill.warnCooldown = 0;
            sawmill.progress = 0;
        }

        sawmill.networkPackNT(150);
        sawmill.heat = 0;
        if (oldHeat != sawmill.heat || oldProgress != sawmill.progress || oldBlade != sawmill.hasBlade
                || level.getGameTime() % 20L == 0L) {
            sawmill.setChangedAndUpdate();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SawmillBlockEntity sawmill) {
        if (!level.isClientSide) {
            return;
        }
        float momentum = sawmill.heat * 25.0F / (float) MAX_HEAT;
        sawmill.lastSpin = sawmill.spin;
        sawmill.spin += momentum;
        if (sawmill.spin >= 360.0F) {
            sawmill.spin -= 360.0F;
            sawmill.lastSpin -= 360.0F;
        }
    }

    private void tryPullHeat(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos.below());
        if (blockEntity instanceof HeatSource source) {
            int pulled = (int) (source.getHeatStored() * DIFFUSION);
            if (pulled > 0) {
                source.useUpHeat(pulled);
                heat += pulled;
                return;
            }
        }
        heat = Math.max(heat - Math.max(heat / 1000, 1), 0);
    }

    private void processInput(Level level) {
        ItemStack result = getOutputFor(items.getStackInSlot(SLOT_INPUT));
        if (result.isEmpty() || !items.getStackInSlot(SLOT_OUTPUT).isEmpty()
                || !items.getStackInSlot(SLOT_SAWDUST).isEmpty()) {
            progress = 0;
            return;
        }
        progress += heat / 10;
        if (progress < PROCESSING_TIME) {
            return;
        }

        progress = 0;
        items.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
        items.setStackInSlot(SLOT_OUTPUT, result.copy());
        if (!result.is(ModItems.POWDER_SAWDUST.get())) {
            float chance = result.is(Items.STICK) ? 0.1F : 0.5F;
            if (level.random.nextFloat() < chance) {
                items.setStackInSlot(SLOT_SAWDUST, new ItemStack(ModItems.POWDER_SAWDUST.get()));
            }
        }
    }

    private void tickOverspeed(Level level, BlockPos pos) {
        if (heat <= MAX_HEAT) {
            overspeed = 0;
            return;
        }
        overspeed++;
        if (overspeed > OVERSPEED_WARNING_TICKS && warnCooldown == 0) {
            warnCooldown = 100;
            LegacySoundPlayer.playSoundEffect(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                    "hbm:block.warnOverspeed", SoundSource.BLOCKS, 2.0F, 1.0F);
        }
        if (overspeed > OVERSPEED_LIMIT) {
            hasBlade = false;
            level.explode(null, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                    5.0F, false, Level.ExplosionInteraction.NONE);
            setChangedAndUpdate();
        }
    }

    public boolean installBlade() {
        if (hasBlade) {
            return false;
        }
        hasBlade = true;
        overspeed = 0;
        warnCooldown = 0;
        setChangedAndUpdate();
        return true;
    }

    public boolean hasBlade() {
        return hasBlade;
    }

    public boolean hasOutputs() {
        return !items.getStackInSlot(SLOT_OUTPUT).isEmpty() || !items.getStackInSlot(SLOT_SAWDUST).isEmpty();
    }

    public boolean takeOutputs(Player player) {
        if (!hasOutputs()) {
            return false;
        }
        for (int slot = SLOT_OUTPUT; slot <= SLOT_SAWDUST; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        setChangedAndUpdate();
        return true;
    }

    public boolean insertHeldInput(Player player, ItemStack held) {
        if (held.isEmpty() || !items.getStackInSlot(SLOT_INPUT).isEmpty() || hasOutputs() || getOutputFor(held).isEmpty()) {
            return false;
        }
        items.setStackInSlot(SLOT_INPUT, held.copyWithCount(1));
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        setChangedAndUpdate();
        return true;
    }

    public boolean canAcceptInput(ItemStack stack) {
        return !stack.isEmpty()
                && items.getStackInSlot(SLOT_INPUT).isEmpty()
                && !hasOutputs()
                && !getOutputFor(stack).isEmpty();
    }

    public ItemStack getOutputFor(ItemStack input) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (input.is(WOODEN_RODS)) {
            return new ItemStack(ModItems.POWDER_SAWDUST.get());
        }
        if (input.is(ItemTags.LOGS)) {
            return craftLogOutput(input);
        }
        if (input.is(ItemTags.PLANKS)) {
            return new ItemStack(Items.STICK, 6);
        }
        if (input.is(ItemTags.SAPLINGS)) {
            return new ItemStack(Items.STICK);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack craftLogOutput(ItemStack input) {
        if (level == null) {
            return ItemStack.EMPTY;
        }
        CraftingContainer crafting = new TransientCraftingContainer(new OneSlotCraftingMenu(), 1, 1);
        crafting.setItem(0, input.copyWithCount(1));
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level)
                .map(recipe -> recipe.assemble(crafting, level.registryAccess()))
                .filter(stack -> !stack.isEmpty())
                .map(stack -> {
                    ItemStack copy = stack.copy();
                    copy.setCount(copy.getCount() * 6 / 4);
                    return copy;
                })
                .orElse(ItemStack.EMPTY);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public int getHeat() {
        return heat;
    }

    public int getProgress() {
        return progress;
    }

    public float getSpin(float partialTick) {
        return lastSpin + (spin - lastSpin) * partialTick;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
        lines.add(net.minecraft.network.chat.Component.literal(heat + "TU/t"));
        lines.add(LegacyLookOverlayLines.percent(heat, MAX_HEAT));
        lines.add(net.minecraft.network.chat.Component.literal("Progress: ")
                .append(LegacyLookOverlayLines.percent(progress, PROCESSING_TIME)));
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                lines.add(LegacyLookOverlayLines.itemStack(slot == SLOT_INPUT, stack));
            }
        }
        if (heat > MAX_HEAT) {
            lines.add(LegacyLookOverlayLines.blinkingWarning("OVERSPEED"));
        }
        if (!hasBlade) {
            lines.add(LegacyLookOverlayLines.error("Blade missing!"));
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    @Override
    public void writePersistentState(CompoundTag persistent) {
        if (!hasBlade) {
            persistent.putBoolean(TAG_MISSING_BLADE, true);
        }
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        hasBlade = !persistent.getBoolean(TAG_MISSING_BLADE);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putInt(TAG_HEAT, heat);
        tag.putInt(TAG_PROGRESS, progress);
        tag.putBoolean(TAG_HAS_BLADE, hasBlade);
        tag.putInt(TAG_WARN_COOLDOWN, warnCooldown);
        tag.putInt(TAG_OVERSPEED, overspeed);
        tag.putFloat(TAG_SPIN, spin);
        tag.putFloat(TAG_LAST_SPIN, lastSpin);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        heat = Math.max(0, tag.getInt(TAG_HEAT));
        progress = Math.max(0, tag.getInt(TAG_PROGRESS));
        hasBlade = !tag.contains(TAG_HAS_BLADE) || tag.getBoolean(TAG_HAS_BLADE);
        warnCooldown = Math.max(0, tag.getInt(TAG_WARN_COOLDOWN));
        overspeed = Math.max(0, tag.getInt(TAG_OVERSPEED));
        spin = tag.getFloat(TAG_SPIN);
        lastSpin = tag.getFloat(TAG_LAST_SPIN);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
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

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private final class AccessibleItemHandler implements IItemHandler {
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
            if (slot != SLOT_INPUT || stack.isEmpty() || !canAcceptInput(stack)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            if (!simulate) {
                items.setStackInSlot(SLOT_INPUT, stack.copyWithCount(1));
            }
            return remainder.isEmpty() ? ItemStack.EMPTY : remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if ((slot != SLOT_OUTPUT && slot != SLOT_SAWDUST) || amount <= 0) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_INPUT ? 1 : 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_INPUT && canAcceptInput(stack);
        }
    }

    private static final class OneSlotCraftingMenu extends AbstractContainerMenu {
        private OneSlotCraftingMenu() {
            super(null, -1);
        }

        @Override
        public @NotNull ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    }
}
