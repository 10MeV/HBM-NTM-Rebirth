package com.hbm.packet.toserver;

import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TypedMenuActionPacket;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy anvil construction craft request facade.
 */
public class AnvilCraftPacket implements LegacyPacketAdapter {
    public int recipeIndex;
    public int mode;

    public AnvilCraftPacket() {
    }

    public AnvilCraftPacket(int recipeIndex, int mode) {
        this.recipeIndex = recipeIndex;
        this.mode = mode;
    }

    public AnvilCraftPacket(int recipeIndex) {
        this(recipeIndex, 0);
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        recipeIndex = buffer.readInt();
        mode = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(recipeIndex);
        buffer.writeInt(mode);
    }

    @Override
    public TypedMenuActionPacket toModernPacket() {
        return ModMessages.anvilCraftPacket(recipeIndex, mode);
    }
}
