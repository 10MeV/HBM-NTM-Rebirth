package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.CrateBlock;
import com.hbm.ntm.menu.CrateMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StorageCrateBlockEntity extends BlockEntity implements MenuProvider {
    public static final String LEGACY_NAME_TAG = "name";
    public static final String LEGACY_SPIDERS_TAG = "spiders";
    private static final int LEGACY_MAX_TOOLTIP_SCAN = 104;

    private final Kind kind;
    private final ItemStackHandler items;
    private final LazyOptional<ItemStackHandler> itemCapability;
    private String customName;
    private boolean hasSpiders;

    public StorageCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORAGE_CRATE.get(), pos, state);
        this.kind = kindFromState(state);
        this.items = new ItemStackHandler(kind.slotCount()) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.itemCapability = LazyOptional.of(() -> items);
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

    public void playOpenSound() {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, com.hbm.ntm.registry.ModSounds.BLOCK_CRATE_OPEN.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public void playCloseSound() {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, com.hbm.ntm.registry.ModSounds.BLOCK_CRATE_CLOSE.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
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
        if (customName != null && !customName.isBlank()) {
            tag.putString(LEGACY_NAME_TAG, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmItemStackUtil.loadLegacyItems(tag, items);
        hasSpiders = tag.getBoolean(LEGACY_SPIDERS_TAG);
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
        IRON(36, "container.crateIron", "gui_crate_iron"),
        STEEL(54, "container.crateSteel", "gui_crate_steel");

        private final int slotCount;
        private final String titleKey;
        private final String textureName;

        Kind(int slotCount, String titleKey, String textureName) {
            this.slotCount = slotCount;
            this.titleKey = titleKey;
            this.textureName = textureName;
        }

        public int slotCount() {
            return slotCount;
        }

        public int rows() {
            return slotCount / 9;
        }

        public String titleKey() {
            return titleKey;
        }

        public String textureName() {
            return textureName;
        }
    }
}
