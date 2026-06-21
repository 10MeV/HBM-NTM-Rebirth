package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;

/**
 * Legacy MuzzleFlashPacket facade. The old packet wrote a fixed int entity id;
 * modern handling records the flash in ClientMuzzleFlashEffects.
 */
public class MuzzleFlashPacket extends ThreadedPacket {
    public int entityID;

    public MuzzleFlashPacket() {
    }

    public MuzzleFlashPacket(LivingEntity entity) {
        this(entity == null ? -1 : entity.getId());
    }

    public MuzzleFlashPacket(int entityId) {
        this.entityID = entityId;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(entityID);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        entityID = buffer.readInt();
    }

    @Override
    public com.hbm.ntm.network.packet.MuzzleFlashPacket toModernPacket() {
        return ModMessages.muzzleFlashPacket(entityID);
    }
}