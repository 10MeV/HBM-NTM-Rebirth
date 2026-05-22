package com.hbm.ntm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientInformMessages {
    private static final int DEFAULT_MILLIS = 3_000;
    private static final Map<Integer, Notice> NOTICES = new LinkedHashMap<>();

    public static void show(Component message, int id, int millis) {
        int duration = millis > 0 ? millis : DEFAULT_MILLIS;
        NOTICES.put(id, new Notice(message, System.currentTimeMillis() + duration));
    }

    public static void render(GuiGraphics graphics, int width, int height) {
        if (NOTICES.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        Font font = Minecraft.getInstance().font;
        int y = height - 72;

        Iterator<Map.Entry<Integer, Notice>> iterator = NOTICES.entrySet().iterator();
        while (iterator.hasNext()) {
            Notice notice = iterator.next().getValue();
            if (notice.expiresAtMillis <= now) {
                iterator.remove();
                continue;
            }

            int x = (width - font.width(notice.message)) / 2;
            graphics.drawString(font, notice.message, x, y, 0xFFFFFF, true);
            y -= 10;
        }
    }

    private record Notice(Component message, long expiresAtMillis) {
    }

    private ClientInformMessages() {
    }
}
