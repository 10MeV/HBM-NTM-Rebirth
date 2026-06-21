package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.CrateBlock;
import api.hbm.block.ILaserable;
import com.hbm.ntm.menu.CrateMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StorageCrateBlockEntity extends BlockEntity implements MenuProvider, ILaserable {
    public static final String LEGACY_NAME_TAG = "name";
    public static final String LEGACY_SPIDERS_TAG = "spiders";
    public static final String LEGACY_HEAT_TIMER_TAG = "heatTimer";
    private static final int LEGACY_MAX_TOOLTIP_SCAN = 104;

    private final Kind kind;
    private final ItemStackHandler items;
    private final LazyOptional<IItemHandler> itemCapability;
    private String customName;
    private boolean hasSpiders;
    private int heatTimer;

    public StorageCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORAGE_CRATE.get(), pos, state);
        this.kind = kindFromState(state);
        this.items = new ItemStackHandler(kind.slotCount()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.itemCapability = LazyOptional.of(() -> new IItemHandler() {
            @Override
            public int getSlots() {
                return items.getSlots();
            }

            @Override
            public @NotNull ItemStack getStackInSlot(int slot) {
                return items.getStackInSlot(slot);
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return items.insertItem(slot, stack, simulate);
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                ItemStack stack = items.getStackInSlot(slot);
                if (kind == Kind.TUNGSTEN && isProtectedTungstenExtraction(stack)) {
                    return ItemStack.EMPTY;
                }
                return items.extractItem(slot, amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return items.getSlotLimit(slot);
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return items.isItemValid(slot, stack);
            }
        });
    }

    public Kind kind() {
        return kind;
    }

    public int slotCount() {
        return items.getSlots();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isHot() {
        return kind == Kind.TUNGSTEN && heatTimer > 0;
    }

    public void tick() {
        if (kind != Kind.TUNGSTEN || level == null) {
            return;
        }
        if (level.isClientSide) {
            if (heatTimer > 0) {
                spawnHeatParticles();
                heatTimer--;
            }
            return;
        }

        boolean wasHot = heatTimer > 0;
        if (heatTimer > 0) {
            heatTimer--;
        }
        boolean isHot = heatTimer > 0;
        if (wasHot != isHot) {
            setChangedAndUpdate();
        }
    }

    public void playOpenSound() {
        if (level != null && !level.isClientSide) {
            LegacySoundPlayer.playLegacyCrateOpen(level, worldPosition, 1.0F, 1.0F);
        }
    }

    public void playCloseSound() {
        if (level != null && !level.isClientSide) {
            LegacySoundPlayer.playLegacyCrateClose(level, worldPosition, 1.0F, 1.0F);
        }
    }

    public void clearItems() {
        for (int i = 0; i < items.getSlots(); i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = HbmItemStackUtil.clearToDrops(items);
        setChanged();
        return drops;
    }

    public ItemStack createDroppedStack() {
        ItemStack stack = new ItemStack(getBlockState().getBlock());
        saveToItemStack(stack);
        if (customName != null && !customName.isBlank()) {
            stack.setHoverName(Component.literal(customName));
        }
        return stack;
    }

    public void saveToItemStack(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean wroteAny = false;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack content = items.getStackInSlot(i);
            if (!content.isEmpty()) {
                tag.put("slot" + i, content.save(new CompoundTag()));
                wroteAny = true;
            }
        }
        if (hasSpiders) {
            tag.putBoolean(LEGACY_SPIDERS_TAG, true);
            wroteAny = true;
        }
        if (!wroteAny && tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public void loadFromPlacedStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            for (int i = 0; i < items.getSlots(); i++) {
                if (tag.contains("slot" + i, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                    items.setStackInSlot(i, ItemStack.of(tag.getCompound("slot" + i)));
                } else {
                    items.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            hasSpiders = tag.getBoolean(LEGACY_SPIDERS_TAG);
            heatTimer = tag.getInt(LEGACY_HEAT_TIMER_TAG);
        }
        if (stack.hasCustomHoverName()) {
            customName = stack.getHoverName().getString();
        }
        setChanged();
    }

    public Container asContainerView() {
        return new Container() {
            @Override
            public int getContainerSize() {
                return items.getSlots();
            }

            @Override
            public boolean isEmpty() {
                for (int i = 0; i < items.getSlots(); i++) {
                    if (!items.getStackInSlot(i).isEmpty()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public ItemStack getItem(int slot) {
                return items.getStackInSlot(slot);
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack removed = items.extractItem(slot, amount, false);
                setChanged();
                return removed;
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                ItemStack stack = items.getStackInSlot(slot);
                items.setStackInSlot(slot, ItemStack.EMPTY);
                return stack;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                items.setStackInSlot(slot, stack);
            }

            @Override
            public void setChanged() {
                StorageCrateBlockEntity.this.setChanged();
            }

            @Override
            public boolean stillValid(Player player) {
                return StorageCrateBlockEntity.this.stillValid(player);
            }

            @Override
            public void clearContent() {
                StorageCrateBlockEntity.this.clearItems();
            }
        };
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatable(kind.titleKey());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CrateMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmItemStackUtil.saveLegacyItemsToTag(tag, items);
        tag.putBoolean(LEGACY_SPIDERS_TAG, hasSpiders);
        if (heatTimer > 0) {
            tag.putInt(LEGACY_HEAT_TIMER_TAG, heatTimer);
        }
        if (customName != null && !customName.isBlank()) {
            tag.putString(LEGACY_NAME_TAG, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmItemStackUtil.loadLegacyItems(tag, items);
        hasSpiders = tag.getBoolean(LEGACY_SPIDERS_TAG);
        heatTimer = tag.getInt(LEGACY_HEAT_TIMER_TAG);
        customName = tag.contains(LEGACY_NAME_TAG) ? tag.getString(LEGACY_NAME_TAG) : null;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap,
            @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public void addEnergy(net.minecraft.world.level.Level level, BlockPos pos, long energy, @Nullable Direction side) {
        if (kind != Kind.TUNGSTEN || level == null || level.isClientSide) {
            return;
        }
        heatTimer = 5;
        smeltContents();
        setChangedAndUpdate();
    }

    private void smeltContents() {
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack result = getSmeltingResult(stack);
            if (result.isEmpty()) {
                continue;
            }
            int count = result.getCount() * stack.getCount();
            if (count <= result.getMaxStackSize()) {
                items.setStackInSlot(slot, result.copyWithCount(count));
            }
        }
    }

    private ItemStack getSmeltingResult(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), level)
                .map(recipe -> recipe.getResultItem(level.registryAccess()).copy())
                .orElse(ItemStack.EMPTY);
    }

    private boolean isProtectedTungstenExtraction(ItemStack stack) {
        return !stack.isEmpty() && !getSmeltingResult(stack).isEmpty();
    }

    private void spawnHeatParticles() {
        if (level == null) {
            return;
        }
        double x = worldPosition.getX();
        double y = worldPosition.getY();
        double z = worldPosition.getZ();
        spawnHeatParticle(x + level.random.nextDouble(), y + 1.1D, z + level.random.nextDouble());
        spawnHeatParticle(x - 0.1D, y + level.random.nextDouble(), z + level.random.nextDouble());
        spawnHeatParticle(x + 1.1D, y + level.random.nextDouble(), z + level.random.nextDouble());
        spawnHeatParticle(x + level.random.nextDouble(), y + level.random.nextDouble(), z - 0.1D);
        spawnHeatParticle(x + level.random.nextDouble(), y + level.random.nextDouble(), z + 1.1D);
    }

    private void spawnHeatParticle(double x, double y, double z) {
        if (level == null) {
            return;
        }
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    public static Kind kindFromState(BlockState state) {
        return state.getBlock() instanceof CrateBlock crate ? crate.kind() : Kind.IRON;
    }

    public static void appendLegacyTooltip(ItemStack stack, List<Component> tooltip) {
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.isEmpty()) {
            return;
        }
        if (tag.getBoolean(LEGACY_SPIDERS_TAG)) {
            tooltip.add(Component.literal("Skittering emanates from within...")
                    .withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC));
            return;
        }
        List<Component> contents = new ArrayList<>();
        int amount = 0;
        for (int i = 0; i < LEGACY_MAX_TOOLTIP_SCAN; i++) {
            if (!tag.contains("slot" + i, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                continue;
            }
            ItemStack content = ItemStack.of(tag.getCompound("slot" + i));
            if (content.isEmpty()) {
                continue;
            }
            amount++;
            if (contents.size() < 10) {
                Component line = Component.literal(" - ").append(content.getHoverName());
                if (content.getCount() > 1) {
                    line = line.copy().append(" x" + content.getCount());
                }
                contents.add(line.copy().withStyle(net.minecraft.ChatFormatting.AQUA));
            }
        }
        if (!contents.isEmpty()) {
            tooltip.add(Component.literal("Contains:").withStyle(net.minecraft.ChatFormatting.AQUA));
            tooltip.addAll(contents);
            int hidden = amount - contents.size();
            if (hidden > 0) {
                tooltip.add(Component.literal("...and " + hidden + " more.").withStyle(net.minecraft.ChatFormatting.AQUA));
            }
        }
    }

    public enum Kind {
        IRON(36, 9, "container.crateIron", "gui_crate_iron", null, 176, 186, 8, 18, 8, 104, 162),
        STEEL(54, 9, "container.crateSteel", "gui_crate_steel", null, 176, 222, 8, 18, 8, 140, 198),
        DESH(104, 13, "container.crateDesh", "gui_crate_desh", null, 248, 256, 8, 18, 44, 174, 232),
        TUNGSTEN(27, 9, "container.crateTungsten", "gui_crate_tungsten", "gui_crate_tungsten_hot", 176, 168, 8, 18, 8, 86, 144),
        SAFE(15, 5, "container.safe", "gui_safe", null, 176, 168, 44, 18, 8, 86, 144);

        private final int slotCount;
        private final int columns;
        private final String titleKey;
        private final String textureName;
        private final String hotTextureName;
        private final int imageWidth;
        private final int imageHeight;
        private final int slotX;
        private final int slotY;
        private final int playerInventoryX;
        private final int playerInventoryY;
        private final int hotbarY;

        Kind(int slotCount, int columns, String titleKey, String textureName, @Nullable String hotTextureName,
                int imageWidth, int imageHeight, int slotX, int slotY, int playerInventoryX, int playerInventoryY,
                int hotbarY) {
            this.slotCount = slotCount;
            this.columns = columns;
            this.titleKey = titleKey;
            this.textureName = textureName;
            this.hotTextureName = hotTextureName;
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
            this.slotX = slotX;
            this.slotY = slotY;
            this.playerInventoryX = playerInventoryX;
            this.playerInventoryY = playerInventoryY;
            this.hotbarY = hotbarY;
        }

        public int slotCount() {
            return slotCount;
        }

        public int rows() {
            return slotCount / columns;
        }

        public int columns() {
            return columns;
        }

        public String titleKey() {
            return titleKey;
        }

        public String textureName() {
            return textureName;
        }

        public String textureName(boolean hot) {
            return hot && hotTextureName != null ? hotTextureName : textureName;
        }

        public int imageWidth() {
            return imageWidth;
        }

        public int imageHeight() {
            return imageHeight;
        }

        public int slotX() {
            return slotX;
        }

        public int slotY() {
            return slotY;
        }

        public int playerInventoryX() {
            return playerInventoryX;
        }

        public int playerInventoryY() {
            return playerInventoryY;
        }

        public int hotbarY() {
            return hotbarY;
        }
    }
}
