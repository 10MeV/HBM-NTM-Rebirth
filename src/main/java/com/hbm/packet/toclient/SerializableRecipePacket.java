package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.util.BufferUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy SerializableRecipePacket facade. The old packet carried either one
 * filename + byte array or a reinit marker; modern handling stores the bytes in
 * the legacy serializable recipe binary-data channel and marks it ready.
 */
public class SerializableRecipePacket extends ThreadedPacket {
    public String filename = "";
    public byte[] fileBytes = new byte[0];
    public boolean reinit;

    public SerializableRecipePacket() {
    }

    public SerializableRecipePacket(File recipeFile) {
        if (recipeFile == null) {
            return;
        }
        try {
            filename = recipeFile.getName();
            fileBytes = Files.readAllBytes(recipeFile.toPath());
        } catch (IOException ignored) {
            filename = "";
            fileBytes = new byte[0];
        }
    }

    public SerializableRecipePacket(String filename, byte[] fileBytes) {
        this.filename = filename == null ? "" : filename;
        this.fileBytes = fileBytes == null ? new byte[0] : fileBytes.clone();
    }

    public SerializableRecipePacket(boolean reinit) {
        this.reinit = reinit;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        reinit = buffer.readBoolean();
        if (reinit) {
            filename = "";
            fileBytes = new byte[0];
            return;
        }
        filename = BufferUtil.readString(buffer);
        fileBytes = new byte[Math.max(0, buffer.readInt())];
        buffer.readBytes(fileBytes);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(reinit);
        if (reinit) {
            return;
        }
        BufferUtil.writeString(buffer, filename);
        buffer.writeInt(fileBytes.length);
        buffer.writeBytes(fileBytes);
    }

    @Override
    public Object toModernPacket() {
        return reinit ? ModMessages.serializableRecipeReinitPacket()
                : ModMessages.serializableRecipePacket(filename, fileBytes);
    }
}