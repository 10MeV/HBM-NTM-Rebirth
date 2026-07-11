package com.hbm.ntm.entity.cart;

import com.hbm.ntm.menu.CartCrateMenu;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NtmCrateMinecartEntity extends NtmMinecartEntity implements Container, MenuProvider {
    public static final int SLOT_COUNT = 54;
    private static final String ITEMS_KEY = "Items";
    private static final String SLOT_KEY = "Slot";
    private static final int MAX_COMPRESSED_DROP_BYTES = 6_000;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT);
    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> items);

    public NtmCrateMinecartEntity(EntityType<? extends NtmCrateMinecartEntity> type, Level level) {
        super(type, level);
        setBase(NtmMinecartBase.VANILLA);
    }

    public NtmCrateMinecartEntity(Level level, double x, double y, double z, ItemStack sourceStack) {
        super(ModEntityTypes.NTM_CART_CRATE.get(), level, x, y, z, NtmMinecartBase.VANILLA);
        loadFromCartStack(sourceStack);
    }

    @Override
    public NtmMinecartType cartType() {
        return NtmMinecartType.CRATE;
    }

    public ItemStackHandler items() {
        return items;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return ModBlocks.CRATE_STEEL.get().defaultBlockState();
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
        return new CartCrateMenu(containerId, inventory, this);
    }

    @Override
    public void destroy(DamageSource damageSource) {
        kill();
        if (!level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }

        CompoundTag contentsTag = saveContentsToItemTag();
        ItemStack stack = getCartItem();
        if (!contentsTag.isEmpty()) {
            stack.setTag(contentsTag.copy());
        }
        if (hasCustomName()) {
            stack.setHoverName(getCustomName());
        }

        if (isOversizedDrop(contentsTag)) {
            level().explode(this, getX(), getY(), getZ(), 2.0F, true, Level.ExplosionInteraction.BLOCK);
            spawnAtLocation(getCartItem());
        }

        spawnAtLocation(stack);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (!items.getStackInSlot(slot).isEmpty()) {
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
        return items.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.getStackInSlot(slot);
        items.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!copy.isEmpty() && copy.getCount() > getMaxStackSize()) {
            copy.setCount(getMaxStackSize());
        }
        items.setStackInSlot(slot, copy);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return !isRemoved() && player.distanceToSqr(this) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag itemList = new ListTag();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte(SLOT_KEY, (byte) slot);
                stack.save(itemTag);
                itemList.add(itemTag);
            }
        }
        tag.put(ITEMS_KEY, itemList);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        clearContent();
        ListTag itemList = tag.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < itemList.size(); index++) {
            CompoundTag itemTag = itemList.getCompound(index);
            int slot = itemTag.getByte(SLOT_KEY) & 255;
            if (slot >= 0 && slot < SLOT_COUNT) {
                items.setStackInSlot(slot, ItemStack.of(itemTag));
            }
        }
    }

    private void loadFromCartStack(ItemStack stack) {
        clearContent();
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (tag.contains("slot" + slot, Tag.TAG_COMPOUND)) {
                items.setStackInSlot(slot, ItemStack.of(tag.getCompound("slot" + slot)));
            }
        }
    }

    private CompoundTag saveContentsToItemTag() {
        CompoundTag tag = new CompoundTag();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                tag.put("slot" + slot, stack.save(new CompoundTag()));
            }
        }
        return tag;
    }

    private static boolean isOversizedDrop(CompoundTag tag) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            NbtIo.writeCompressed(tag, output);
            return output.toByteArray().length > MAX_COMPRESSED_DROP_BYTES;
        } catch (IOException exception) {
            return false;
        }
    }
}
