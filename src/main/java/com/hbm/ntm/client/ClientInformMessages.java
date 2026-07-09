package com.hbm.ntm.client;

import com.hbm.ntm.config.HbmClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientInformMessages {
    private static final int DEFAULT_MILLIS = 3_000;
    private static final int DEFAULT_COLOR = 0xFFFFFF;
    private static final int BACKGROUND_COLOR = 0x7F404040;
    private static final int BACKGROUND_LEFT_PADDING = 5;
    private static final int BACKGROUND_RIGHT_PADDING = 5;
    private static final int BACKGROUND_TOP_PADDING = 5;
    private static final int BACKGROUND_BOTTOM_PADDING = 2;
    private static final int LINE_HEIGHT = 10;
    private static final Map<Integer, Notice> NOTICES = new LinkedHashMap<>();
    private static final List<ClientInformMessageListener> LISTENERS = new ArrayList<>();
    private static final List<Notice> RENDER_NOTICES = new ArrayList<>();
    private static final Comparator<Notice> NOTICE_DURATION_ORDER = Comparator.comparingInt(Notice::durationMillis);

    public static void show(Component message, int id, int millis) {
        int duration = millis > 0 ? millis : DEFAULT_MILLIS;
        long now = System.currentTimeMillis();
        NOTICES.put(id, new Notice(message, now, duration, now + duration, colorOf(message)));
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
        RENDER_NOTICES.clear();

        Iterator<Map.Entry<Integer, Notice>> iterator = NOTICES.entrySet().iterator();
        while (iterator.hasNext()) {
            Notice notice = iterator.next().getValue();
            if (notice.expiresAtMillis <= now) {
                iterator.remove();
            } else {
                RENDER_NOTICES.add(notice);
            }
        }
        if (RENDER_NOTICES.isEmpty()) {
            return;
        }

        try {
            RENDER_NOTICES.sort(NOTICE_DURATION_ORDER);
            int longest = 0;
            for (Notice notice : RENDER_NOTICES) {
                longest = Math.max(longest, font.width(notice.message()));
            }

            int mode = HbmClientConfig.infoPosition();
            int x = switch (mode) {
                case 1 -> width - longest - 15;
                case 2 -> width / 2 + 7;
                case 3 -> width / 2 - longest - 6;
                default -> 15;
            } + HbmClientConfig.infoOffsetHorizontal();
            int y = (mode == 0 || mode == 1 ? 15 : height / 2 + 7) + HbmClientConfig.infoOffsetVertical();

            graphics.fill(x - BACKGROUND_LEFT_PADDING, y - BACKGROUND_TOP_PADDING,
                    x + longest + BACKGROUND_RIGHT_PADDING,
                    y + RENDER_NOTICES.size() * LINE_HEIGHT + BACKGROUND_BOTTOM_PADDING,
                    BACKGROUND_COLOR);
            int offset = 0;
            for (Notice notice : RENDER_NOTICES) {
                graphics.drawString(font, notice.message(), x, y + offset, fadeColor(notice, now), false);
                offset += LINE_HEIGHT;
            }
        } finally {
            RENDER_NOTICES.clear();
        }
    }

    public static InformHudPlan informHudPlan(int width, int height, Font font, long now) {
        List<Notice> notices = new ArrayList<>();
        for (Notice notice : NOTICES.values()) {
            if (notice.expiresAtMillis() > now) {
                notices.add(notice);
            }
        }
        notices.sort(NOTICE_DURATION_ORDER);
        int longest = 0;
        for (Notice notice : notices) {
            longest = Math.max(longest, font.width(notice.message()));
        }

        int mode = HbmClientConfig.infoPosition();
        int x = switch (mode) {
            case 1 -> width - longest - 15;
            case 2 -> width / 2 + 7;
            case 3 -> width / 2 - longest - 6;
            default -> 15;
        } + HbmClientConfig.infoOffsetHorizontal();
        int y = (mode == 0 || mode == 1 ? 15 : height / 2 + 7) + HbmClientConfig.infoOffsetVertical();

        List<InformMessagePlan> messagePlans = new ArrayList<>(notices.size());
        int offset = 0;
        for (Notice notice : notices) {
            messagePlans.add(new InformMessagePlan(notice.message(), x, y + offset, fadeColor(notice, now)));
            offset += LINE_HEIGHT;
        }

        ScreenRect background = new ScreenRect(x - BACKGROUND_LEFT_PADDING, y - BACKGROUND_TOP_PADDING,
                longest + BACKGROUND_LEFT_PADDING + BACKGROUND_RIGHT_PADDING,
                notices.size() * LINE_HEIGHT + BACKGROUND_TOP_PADDING + BACKGROUND_BOTTOM_PADDING);
        return new InformHudPlan(background, BACKGROUND_COLOR, longest, messagePlans);
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

    private static int colorOf(Component message) {
        TextColor color = message == null ? null : message.getStyle().getColor();
        return color == null ? DEFAULT_COLOR : color.getValue() & 0xFFFFFF;
    }

    private static int fadeColor(Notice notice, long now) {
        int millis = Math.max(1, notice.durationMillis());
        int elapsed = (int) Math.max(0L, now - notice.startMillis());
        int alpha = Math.max(Math.min(510 * (millis - elapsed) / millis, 255), 5);
        return (alpha << 24) | (notice.color() & 0xFFFFFF);
    }

    public record InformHudPlan(ScreenRect background, int backgroundColor, int longestTextWidth,
                                List<InformMessagePlan> messages) {
    }

    public record InformMessagePlan(Component message, int x, int y, int argbColor) {
    }

    public record ScreenRect(int x, int y, int width, int height) {
    }

    private record Notice(Component message, long startMillis, int durationMillis, long expiresAtMillis, int color) {
    }

    private ClientInformMessages() {
    }
}
