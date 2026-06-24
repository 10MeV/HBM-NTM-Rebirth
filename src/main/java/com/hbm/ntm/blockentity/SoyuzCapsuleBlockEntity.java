package com.hbm.ntm.blockentity;

import com.hbm.ntm.menu.SoyuzCapsuleMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class SoyuzCapsuleBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CARGO_SLOT_COUNT = 18;
    public static final int SLOT_ROCKET = 18;
    public static final int SLOT_COUNT = 19;
    private static final String TAG_CUSTOM_NAME = "name";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private String customName;

    public SoyuzCapsuleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOYUZ_CAPSULE.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public void setCargoSlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < CARGO_SLOT_COUNT) {
            items.setStackInSlot(slot, stack.copy());
        }
    }

    public void setRocketStack(ItemStack stack) {
        items.setStackInSlot(SLOT_ROCKET, stack.copy());
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        if (hasCustomName()) {
            return Component.literal(customName);
        }
        return Component.translatableWithFallback("container.soyuzCapsule", "Cargo Landing Capsule");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SoyuzCapsuleMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        if (hasCustomName()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        customName = tag.getString(TAG_CUSTOM_NAME);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX() - 1.0D, pos.getY() - 1.0D, pos.getZ() - 1.0D,
                pos.getX() + 2.0D, pos.getY() + 3.0D, pos.getZ() + 2.0D);
    }

    private boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }
}
