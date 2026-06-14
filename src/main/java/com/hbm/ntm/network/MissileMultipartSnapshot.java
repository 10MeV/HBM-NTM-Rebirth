package com.hbm.ntm.network;

import com.hbm.ntm.item.missile.CustomMissileItem;
import com.hbm.ntm.item.missile.CustomMissilePartProfile;
import com.hbm.ntm.item.missile.MissilePartItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public record MissileMultipartSnapshot(ResourceLocation warhead, ResourceLocation fuselage,
                                        ResourceLocation fins, ResourceLocation thruster) {
    public static final MissileMultipartSnapshot EMPTY = new MissileMultipartSnapshot(null, null, null, null);

    public MissileMultipartSnapshot {
        warhead = normalize(warhead);
        fuselage = normalize(fuselage);
        fins = normalize(fins);
        thruster = normalize(thruster);
    }

    public static MissileMultipartSnapshot of(ResourceLocation warhead, ResourceLocation fuselage,
                                              ResourceLocation fins, ResourceLocation thruster) {
        return new MissileMultipartSnapshot(warhead, fuselage, fins, thruster);
    }

    public static MissileMultipartSnapshot of(ItemStack warhead, ItemStack fuselage, ItemStack fins, ItemStack thruster) {
        return new MissileMultipartSnapshot(itemId(warhead), itemId(fuselage), itemId(fins), itemId(thruster));
    }

    public static MissileMultipartSnapshot ofMissile(ItemStack missile) {
        if (!(missile.getItem() instanceof CustomMissileItem)) {
            return EMPTY;
        }
        return new MissileMultipartSnapshot(
                CustomMissileItem.getPartId(missile, CustomMissileItem.TAG_WARHEAD),
                CustomMissileItem.getPartId(missile, CustomMissileItem.TAG_FUSELAGE),
                CustomMissileItem.getPartId(missile, CustomMissileItem.TAG_STABILITY),
                CustomMissileItem.getPartId(missile, CustomMissileItem.TAG_THRUSTER));
    }

    public static MissileMultipartSnapshot decode(FriendlyByteBuf buffer) {
        return new MissileMultipartSnapshot(readOptionalId(buffer), readOptionalId(buffer),
                readOptionalId(buffer), readOptionalId(buffer));
    }

    public void encode(FriendlyByteBuf buffer) {
        writeOptionalId(buffer, warhead);
        writeOptionalId(buffer, fuselage);
        writeOptionalId(buffer, fins);
        writeOptionalId(buffer, thruster);
    }

    public boolean isEmpty() {
        return warhead == null && fuselage == null && fins == null && thruster == null;
    }

    @Nullable
    public CustomMissilePartProfile.Assembly toAssembly() {
        if (isEmpty()) {
            return null;
        }
        return new CustomMissilePartProfile.Assembly(
                null,
                CustomMissilePartProfile.resolve(warhead, MissilePartItem.PartType.WARHEAD),
                CustomMissilePartProfile.resolve(fuselage, MissilePartItem.PartType.FUSELAGE),
                CustomMissilePartProfile.resolve(fins, MissilePartItem.PartType.FINS),
                CustomMissilePartProfile.resolve(thruster, MissilePartItem.PartType.THRUSTER));
    }

    private static ResourceLocation itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return ForgeRegistries.ITEMS.getKey(stack.getItem());
    }

    private static ResourceLocation normalize(ResourceLocation id) {
        return id == null ? null : id;
    }

    private static ResourceLocation readOptionalId(FriendlyByteBuf buffer) {
        return buffer.readBoolean() ? buffer.readResourceLocation() : null;
    }

    private static void writeOptionalId(FriendlyByteBuf buffer, ResourceLocation id) {
        buffer.writeBoolean(id != null);
        if (id != null) {
            buffer.writeResourceLocation(id);
        }
    }
}
