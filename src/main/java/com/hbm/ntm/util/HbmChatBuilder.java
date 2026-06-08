package com.hbm.ntm.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayDeque;
import java.util.Deque;

public final class HbmChatBuilder {
    private final MutableComponent root;
    private MutableComponent last;

    private HbmChatBuilder(MutableComponent root) {
        this.root = root;
        this.last = root;
    }

    public static HbmChatBuilder start(String text) {
        return new HbmChatBuilder(Component.literal(text));
    }

    public static HbmChatBuilder startTranslation(String key, Object... args) {
        return new HbmChatBuilder(Component.translatable(key, args));
    }

    public HbmChatBuilder next(String text) {
        MutableComponent append = Component.literal(text);
        last.append(append);
        last = append;
        return this;
    }

    public HbmChatBuilder nextTranslation(String key, Object... args) {
        MutableComponent append = Component.translatable(key, args);
        last.append(append);
        last = append;
        return this;
    }

    public HbmChatBuilder color(ChatFormatting format) {
        last.setStyle(last.getStyle().withColor(format));
        return this;
    }

    public HbmChatBuilder colorAll(ChatFormatting format) {
        Deque<MutableComponent> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            MutableComponent component = queue.removeFirst();
            component.setStyle(component.getStyle().withColor(format));
            for (Component sibling : component.getSiblings()) {
                if (sibling instanceof MutableComponent mutable) {
                    queue.addLast(mutable);
                }
            }
        }
        return this;
    }

    public HbmChatBuilder style(Style style) {
        last.setStyle(style);
        return this;
    }

    public MutableComponent flush() {
        return root;
    }
}
