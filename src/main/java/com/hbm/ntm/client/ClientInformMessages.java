package com.hbm.ntm.client;

import com.hbm.ntm.config.HbmClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientInformMessages {
    private static final int DEFAULT_MILLIS = 3_000;
    private static final Map<Integer, Notice> NOTICES = new LinkedHashMap<>();
    private static final List<ClientInformMessageListener> LISTENERS = new ArrayList<>();

    public static void show(Component message, int id, int millis) {
        int duration = millis > 0 ? millis : DEFAULT_MILLIS;
        NOTICES.put(id, new Notice(message, System.currentTimeMillis() + duration));
        for (ClientInformMessageListener listener : List.copyOf(LISTENERS)) {
            listener.onClientInformMessage(message, id, duration);
        }
    }

    public static void render(GuiGraphics graphics, int width, int height) {
        if (NOTICES.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        Font font = Minecraft.getInstance().font;
        int longest = 0;
        for (Notice notice : NOTICES.values()) {
            longest = Math.max(longest, font.width(notice.message));
        }

        int mode = HbmClientConfig.infoPosition();
        int x = switch (mode) {
            case 1 -> width - longest - 15;
            case 2 -> width / 2 + 7;
            case 3 -> width / 2 - longest - 6;
            default -> 15;
        } + HbmClientConfig.INFO_OFFSET_HORIZONTAL.get();
        int y = (mode == 0 || mode == 1 ? 15 : height / 2 + 7) + HbmClientConfig.INFO_OFFSET_VERTICAL.get();

        Iterator<Map.Entry<Integer, Notice>> iterator = NOTICES.entrySet().iterator();
        while (iterator.hasNext()) {
            Notice notice = iterator.next().getValue();
            if (notice.expiresAtMillis <= now) {
                iterator.remove();
                continue;
            }

            graphics.drawString(font, notice.message, x, y, 0xFFFFFF, true);
            y += 10;
        }
    }

    public static void clearAll() {
        NOTICES.clear();
        LISTENERS.clear();
    }

    public static int noticeCount() {
        return NOTICES.size();
    }

    public static void addListener(ClientInformMessageListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(ClientInformMessageListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
    }

    private record Notice(Component message, long expiresAtMillis) {
    }

    private ClientInformMessages() {
    }
}
