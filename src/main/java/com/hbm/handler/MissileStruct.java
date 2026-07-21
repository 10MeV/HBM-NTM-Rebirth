package com.hbm.handler;

import com.hbm.ntm.item.missile.MissilePartItem;
import com.hbm.ntm.network.MissileMultipartSnapshot;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Source-compatibility carrier for the legacy custom-missile multipart payload.
 *
 * <p>This retains the four consecutive runtime item-id integers used by the
 * 1.7.10 packet. Numeric registry ids are necessarily scoped to the running
 * 1.20.1 instance; this is not a 1.7.10 save or wire compatibility layer.</p>
 */
public class MissileStruct {
    @Nullable public MissilePartItem warhead;
    @Nullable public MissilePartItem fuselage;
    @Nullable public MissilePartItem fins;
    @Nullable public MissilePartItem thruster;

    public MissileStruct() {
    }

    public MissileStruct(@Nullable ItemStack warhead, @Nullable ItemStack fuselage, @Nullable ItemStack fins,
            @Nullable ItemStack thruster) {
        this(warhead == null ? null : warhead.getItem(), fuselage == null ? null : fuselage.getItem(),
                fins == null ? null : fins.getItem(), thruster == null ? null : thruster.getItem());
    }

    public MissileStruct(@Nullable Item warhead, @Nullable Item fuselage, @Nullable Item fins, @Nullable Item thruster) {
        this.warhead = asPart(warhead);
        this.fuselage = asPart(fuselage);
        this.fins = asPart(fins);
        this.thruster = asPart(thruster);
    }

    public void writeToByteBuffer(ByteBuf buffer) {
        writePart(buffer, warhead, MissilePartItem.PartType.WARHEAD);
        writePart(buffer, fuselage, MissilePartItem.PartType.FUSELAGE);
        writePart(buffer, fins, MissilePartItem.PartType.FINS);
        writePart(buffer, thruster, MissilePartItem.PartType.THRUSTER);
    }

    public static MissileStruct readFromByteBuffer(ByteBuf buffer) {
        MissileStruct multipart = new MissileStruct();
        multipart.warhead = readPart(buffer);
        multipart.fuselage = readPart(buffer);
        multipart.fins = readPart(buffer);
        multipart.thruster = readPart(buffer);
        return multipart;
    }

    public MissileMultipartSnapshot toSnapshot() {
        return MissileMultipartSnapshot.of(id(warhead), id(fuselage), id(fins), id(thruster));
    }

    private static void writePart(ByteBuf buffer, @Nullable MissilePartItem part, MissilePartItem.PartType expectedType) {
        buffer.writeInt(part != null && part.type() == expectedType ? BuiltInRegistries.ITEM.getId(part) : 0);
    }

    @Nullable
    private static MissilePartItem readPart(ByteBuf buffer) {
        int id = buffer.readInt();
        if (id == 0) {
            return null;
        }
        return asPart(BuiltInRegistries.ITEM.byId(id));
    }

    @Nullable
    private static ResourceLocation id(@Nullable MissilePartItem part) {
        return part == null ? null : BuiltInRegistries.ITEM.getKey(part);
    }

    @Nullable
    private static MissilePartItem asPart(@Nullable Item item) {
        return item instanceof MissilePartItem part ? part : null;
    }
}
