package com.hbm.ntm.menu;

import com.hbm.ntm.block.CrateBlock;
import com.hbm.ntm.blockentity.StorageCrateBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Item-side equivalent of 1.7.10 {@code ItemBlockStorageCrate.InventoryCrate}.
 * Its direct {@code slot0..slotN} tag format is deliberately different from
 * modern list-backed bag items.
 */
public class HeldCrateMenu extends AbstractContainerMenu {
    private static final String TAG_STACK_LOCK = "stacklock";

    private final Inventory playerInventory;
    private final InteractionHand hand;
    private final ItemStack heldStack;
    private final Item crateItem;
    private final StorageCrateBlockEntity.Kind kind;
    private final HeldCrateContainer crateInventory;
    private final int slotCount;

    public HeldCrateMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readEnum(InteractionHand.class));
    }

    public HeldCrateMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.HELD_STORAGE_CRATE.get(), containerId);
        this.playerInventory = playerInventory;
        this.hand = hand;
        ItemStack held = playerInventory.player.getItemInHand(hand);
        if (!(held.getItem() instanceof com.hbm.ntm.item.CrateBlockItem crateBlockItem)
                || !(crateBlockItem.getBlock() instanceof CrateBlock crateBlock)) {
            throw new IllegalStateException("Expected held storage crate item");
        }
        this.heldStack = held;
        this.crateItem = held.getItem();
        this.kind = crateBlock.kind();
        this.slotCount = kind.slotCount();
        this.crateInventory = new HeldCrateContainer(held, slotCount);

        for (int row = 0; row < kind.rows(); row++) {
            for (int column = 0; column < kind.columns(); column++) {
                int slot = column + row * kind.columns();
                addSlot(HbmInventoryMenuHelper.legacyContainerSlot(crateInventory, slot,
                        kind.slotX() + column * 18, kind.slotY() + row * 18));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, kind.playerInventoryX(),
                kind.playerInventoryY(), kind.hotbarY(), hand == InteractionHand.MAIN_HAND ? playerInventory.selected : -1);
    }

    public StorageCrateBlockEntity.Kind kind() {
        return kind;
    }

    public boolean isHot() {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == playerInventory.player && player.getItemInHand(hand) == heldStack
                && player.getItemInHand(hand).getItem() == crateItem;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < slotCount) {
            if (!moveItemStackTo(stack, slotCount, slotCount + 36, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, slotCount, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        crateInventory.save();
        return result;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (hand == InteractionHand.MAIN_HAND
                && HbmInventoryMenuHelper.shouldBlockOpenItemContainerClick(slotId, button, clickType,
                        playerInventory, slotCount)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
        crateInventory.save();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        crateInventory.close(player);
    }

    private static final class HeldCrateContainer extends SimpleContainer {
        private final ItemStack crateStack;
        private boolean loading = true;

        private HeldCrateContainer(ItemStack crateStack, int slotCount) {
            super(slotCount);
            this.crateStack = crateStack;
            CompoundTag tag = crateStack.getTag();
            if (tag != null) {
                for (int slot = 0; slot < slotCount; slot++) {
                    if (tag.contains("slot" + slot, Tag.TAG_COMPOUND)) {
                        super.setItem(slot, ItemStack.of(tag.getCompound("slot" + slot)));
                    }
                }
            }
            // Legacy InventoryCrate creates this temporary non-stack marker as soon
            // as its item inventory opens, even before the first slot change.
            crateStack.getOrCreateTag().putLong(TAG_STACK_LOCK, RandomSource.create().nextLong());
            this.loading = false;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (!loading) {
                save();
            }
        }

        private void save() {
            if (crateStack.isEmpty()) {
                return;
            }
            CompoundTag tag = crateStack.getOrCreateTag();
            for (int slot = 0; slot < getContainerSize(); slot++) {
                tag.remove("slot" + slot);
                ItemStack stack = getItem(slot);
                if (!stack.isEmpty()) {
                    tag.put("slot" + slot, stack.save(new CompoundTag()));
                }
            }
            tag.putLong(TAG_STACK_LOCK, RandomSource.create().nextLong());
        }

        private void close(Player player) {
            save();
            CompoundTag tag = crateStack.getTag();
            if (tag == null) {
                return;
            }
            if (exceedsLegacyItemNbtLimit(tag)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "Warning: Container NBT exceeds 6kB, contents will be ejected!")
                        .withStyle(net.minecraft.ChatFormatting.RED), false);
                ejectContents(player);
                crateStack.setTag(null);
                return;
            }
            tag.remove(TAG_STACK_LOCK);
            if (tag.isEmpty()) {
                crateStack.setTag(null);
            }
        }

        private static boolean exceedsLegacyItemNbtLimit(CompoundTag tag) {
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                net.minecraft.nbt.NbtIo.writeCompressed(tag, output);
                return output.size() > 6_000;
            } catch (IOException ignored) {
                return false;
            }
        }

        private void ejectContents(Player player) {
            RandomSource random = RandomSource.create();
            for (int slot = 0; slot < getContainerSize(); slot++) {
                ItemStack remaining = getItem(slot).copy();
                while (!remaining.isEmpty()) {
                    int count = Math.min(remaining.getCount(), random.nextInt(21) + 10);
                    ItemStack drop = remaining.split(count);
                    ItemEntity entity = new ItemEntity(player.level(),
                            player.getX() + random.nextFloat() * 0.8D + 0.1D,
                            player.getY() + random.nextFloat() * 0.8D + 0.1D,
                            player.getZ() + random.nextFloat() * 0.8D + 0.1D, drop);
                    entity.setDeltaMovement(random.nextGaussian() * 0.05D + player.getDeltaMovement().x,
                            random.nextGaussian() * 0.05D + 0.2D + player.getDeltaMovement().y,
                            random.nextGaussian() * 0.05D + player.getDeltaMovement().z);
                    player.level().addFreshEntity(entity);
                }
            }
        }
    }
}
