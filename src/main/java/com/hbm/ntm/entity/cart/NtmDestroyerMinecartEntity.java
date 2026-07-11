package com.hbm.ntm.entity.cart;

import com.hbm.ntm.menu.CartDestroyerMenu;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

public class NtmDestroyerMinecartEntity extends NtmMinecartEntity implements MenuProvider {
    private static final int FILTER_SLOTS = 18;
    private static final String ITEMS_KEY = "Items";
    private static final String SLOT_KEY = "Slot";

    private final ItemStackHandler filters = new ItemStackHandler(FILTER_SLOTS) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            super.setStackInSlot(slot, normalizeFilterStack(stack));
        }
    };

    public NtmDestroyerMinecartEntity(EntityType<? extends NtmDestroyerMinecartEntity> type, Level level) {
        super(type, level);
    }

    public NtmDestroyerMinecartEntity(Level level, double x, double y, double z, NtmMinecartBase base) {
        super(ModEntityTypes.NTM_CART_DESTROYER.get(), level, x, y, z, base);
    }

    @Override
    public NtmMinecartType cartType() {
        return NtmMinecartType.DESTROYER;
    }

    public ItemStackHandler filters() {
        return filters;
    }

    public boolean stillValid(Player player) {
        return !isRemoved() && player.distanceToSqr(this) <= 64.0D;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(this::createMenu, getDisplayName()),
                    buffer -> buffer.writeInt(getId()));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public Component getDisplayName() {
        Component customName = getCustomName();
        return customName != null ? customName : Component.translatable("container.minecart");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CartDestroyerMenu(containerId, inventory, this);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && tickCount % 5 == 0) {
            destroyMatchingItems();
        }

        if (level().isClientSide && tickCount % 5 == 0) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    getX(), getY() + 0.75D, getZ(), 0.0D, 0.01D, 0.0D);
        }
    }

    private void destroyMatchingItems() {
        AABB bounds = new AABB(getX() - 2.5D, getY() - 1.5D, getZ() - 2.5D,
                getX() + 2.5D, getY() + 2.0D, getZ() + 2.5D);
        boolean sound = false;
        for (ItemEntity itemEntity : level().getEntitiesOfClass(ItemEntity.class, bounds)) {
            ItemStack stack = itemEntity.getItem();
            if (matchesExactFilter(stack) || matchesWildcardFilter(stack)) {
                itemEntity.discard();
                sound = true;
            }
        }

        if (sound) {
            LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(), "mob.zombie.woodbreak",
                    0.5F, 0.5F + random.nextFloat() * 0.2F);
        }
    }

    private boolean matchesExactFilter(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack filter = filters.getStackInSlot(slot);
            if (!filter.isEmpty() && filter.getItem() == stack.getItem()
                    && filter.getDamageValue() == stack.getDamageValue()) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesWildcardFilter(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int slot = 9; slot < FILTER_SLOTS; slot++) {
            ItemStack filter = filters.getStackInSlot(slot);
            if (!filter.isEmpty() && filter.getItem() == stack.getItem()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag items = new ListTag();
        for (int slot = 0; slot < FILTER_SLOTS; slot++) {
            ItemStack stack = filters.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte(SLOT_KEY, (byte) slot);
                stack.save(itemTag);
                items.add(itemTag);
            }
        }
        tag.put(ITEMS_KEY, items);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        for (int slot = 0; slot < FILTER_SLOTS; slot++) {
            filters.setStackInSlot(slot, ItemStack.EMPTY);
        }
        ListTag items = tag.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < items.size(); index++) {
            CompoundTag itemTag = items.getCompound(index);
            int slot = itemTag.getByte(SLOT_KEY) & 255;
            if (slot >= 0 && slot < FILTER_SLOTS) {
                filters.setStackInSlot(slot, ItemStack.of(itemTag));
            }
        }
    }

    private static ItemStack normalizeFilterStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }
}
