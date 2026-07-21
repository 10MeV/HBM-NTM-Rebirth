package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientMuzzleFlashEffects;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MuzzleFlashPacket(int entityId) implements HbmPreparablePacket {
    public static MuzzleFlashPacket decode(FriendlyByteBuf buffer) {
        return new MuzzleFlashPacket(buffer.readVarInt());
    }

    public static void encode(MuzzleFlashPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
    }

    public static void handle(MuzzleFlashPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            Entity entity = minecraft.level.getEntity(packet.entityId);
            // Source: 1.7.10 MuzzleFlashPacket.Handler.  The firing client
            // renders from its local gun state, while this packet only marks a
            // remote living entity that is still holding a Sedna gun.
            if (!(entity instanceof LivingEntity living) || entity == minecraft.player) {
                return;
            }
            ItemStack held = living.getMainHandItem();
            if (held.getItem() instanceof SednaGunItem) {
                ClientMuzzleFlashEffects.mark(packet.entityId);
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new MuzzleFlashPacket(entityId);
    }
}
