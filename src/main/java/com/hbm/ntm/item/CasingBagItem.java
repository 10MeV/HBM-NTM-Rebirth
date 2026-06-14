package com.hbm.ntm.item;

import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.menu.CasingBagMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public class CasingBagItem extends Item {
    public static final int SLOT_COUNT = 15;

    public CasingBagItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Component title = stack.hasCustomHoverName()
                    ? stack.getHoverName()
                    : Component.translatable("container.casingBag");
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((containerId, inventory, owner) ->
                            new CasingBagMenu(containerId, inventory, hand), title),
                    buffer -> buffer.writeEnum(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static boolean pushCasing(ItemStack bag, ItemStack casing, float amount) {
        if (bag.isEmpty() || casing.isEmpty() || amount <= 0.0F) {
            return false;
        }
        CompoundTag tag = bag.getOrCreateTag();
        String key = legacyCasingCounterKey(casing);
        boolean accepted = false;
        if (tag.getFloat(key) < 1.0F) {
            tag.putFloat(key, tag.getFloat(key) + amount);
            accepted = true;
        }
        if (tag.getFloat(key) < 1.0F) {
            return accepted;
        }

        NonNullList<ItemStack> slots = HbmItemStackUtil.readStacksFromNbt(bag, SLOT_COUNT);
        while (tag.getFloat(key) >= 1.0F) {
            ItemStack toAdd = casing.copy();
            boolean inserted = insertOneCasingStack(slots, toAdd);
            if (!inserted) {
                break;
            }
            tag.putFloat(key, tag.getFloat(key) - 1.0F);
            accepted = true;
        }
        HbmItemStackUtil.setStacksToNbt(bag, slots, false);
        return accepted;
    }

    private static boolean insertOneCasingStack(NonNullList<ItemStack> slots, ItemStack toAdd) {
        boolean didSomething = false;
        for (int slot = 0; slot < slots.size() && !toAdd.isEmpty(); slot++) {
            ItemStack existing = slots.get(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, toAdd)) {
                int moved = Math.min(toAdd.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (moved > 0) {
                    existing.grow(moved);
                    toAdd.shrink(moved);
                    didSomething = true;
                }
            }
        }
        for (int slot = 0; slot < slots.size() && !toAdd.isEmpty(); slot++) {
            if (slots.get(slot).isEmpty()) {
                slots.set(slot, toAdd.copy());
                toAdd.setCount(0);
                didSomething = true;
                break;
            }
        }
        return didSomething;
    }

    private static String legacyCasingCounterKey(ItemStack casing) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(casing.getItem());
        String legacyName = id == null ? casing.getDescriptionId() : "item." + id.getPath();
        return legacyName + "@0";
    }
}
