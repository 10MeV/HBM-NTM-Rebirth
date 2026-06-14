package com.hbm.util;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Legacy 1.7.10 package bridge for chat component construction.
 */
@Deprecated(forRemoval = false)
public class ChatBuilder {
    private final MutableComponent text;
    private MutableComponent last;

    private ChatBuilder(String text) {
        this.text = Component.literal(text);
        this.last = this.text;
    }

    public static ChatBuilder start(String text) {
        return new ChatBuilder(text);
    }

    public static ChatBuilder startTranslation(String text, Object... args) {
        return new ChatBuilder("").nextTranslation(text, args);
    }

    public ChatBuilder next(String text) {
        MutableComponent append = Component.literal(text);
        last.append(append);
        last = append;
        return this;
    }

    public ChatBuilder nextTranslation(String text, Object... args) {
        MutableComponent append = Component.translatable(text, args);
        last.append(append);
        last = append;
        return this;
    }

    public ChatBuilder color(ChatFormatting format) {
        last.setStyle(last.getStyle().withColor(format));
        return this;
    }

    public ChatBuilder colorAll(ChatFormatting format) {
        Deque<MutableComponent> queue = new ArrayDeque<>();
        queue.add(text);
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

    public MutableComponent flush() {
        return text;
    }
}
