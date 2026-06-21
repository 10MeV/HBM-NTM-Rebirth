package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.HeldItemNbtPacket;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Legacy held-item NBT packet facade. It keeps the old item-id/count/damage/NBT
 * byte order, then delegates client application to HeldItemNbtPacket.
 */
public class HeldItemNBTPacket extends ThreadedPacket {
    private ItemStack stack = ItemStack.EMPTY;

    public HeldItemNBTPacket() {
    }

    public HeldItemNBTPacket(ItemStack stack) {
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        if (stack.isEmpty()) {
            buffer.writeShort(-1);
            return;
        }
        buffer.writeShort(BuiltInRegistries.ITEM.getId(stack.getItem()));
        buffer.writeByte(stack.getCount());
        buffer.writeShort(stack.getDamageValue());
        buffer.writeNbt(stack.getTag());
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        short id = buffer.readShort();
        if (id < 0) {
            stack = ItemStack.EMPTY;
            return;
        }
        byte quantity = buffer.readByte();
        short damage = buffer.readShort();
        Item item = BuiltInRegistries.ITEM.byId(id);
        if (item == Items.AIR) {
            stack = ItemStack.EMPTY;
            buffer.readNbt();
            return;
        }
        stack = new ItemStack(item, Byte.toUnsignedInt(quantity));
        stack.setDamageValue(damage);
        CompoundTag tag = buffer.readNbt();
        if (tag != null) {
            stack.setTag(tag);
        }
    }

    @Override
    public Object toModernPacket() {
        return ModMessages.heldItemNbtPacket(InteractionHand.MAIN_HAND, stack)
                .<Object>map(packet -> packet)
                .orElseGet(this::emptyPacket);
    }

    public ItemStack stack() {
        return stack.copy();
    }

    private HeldItemNbtPacket emptyPacket() {
        ResourceLocation air = BuiltInRegistries.ITEM.getKey(Items.AIR);
        return new HeldItemNbtPacket(InteractionHand.MAIN_HAND, air, 0, new CompoundTag());
    }
}
