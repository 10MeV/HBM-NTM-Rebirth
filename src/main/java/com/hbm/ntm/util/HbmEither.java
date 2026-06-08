package com.hbm.ntm.util;

import java.util.function.Function;

@SuppressWarnings("unchecked")
public final class HbmEither<L, R> {
    private final Object value;
    private final boolean left;

    private HbmEither(Object value, boolean left) {
        this.value = value;
        this.left = left;
    }

    public static <L, R> HbmEither<L, R> left(L value) {
        return new HbmEither<>(value, true);
    }

    public static <L, R> HbmEither<L, R> right(R value) {
        return new HbmEither<>(value, false);
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return !left;
    }

    public L left() {
        if (!left) {
            throw new IllegalStateException("Tried accessing value as the L type, but was R type");
        }
        return (L) value;
    }

    public R right() {
        if (left) {
            throw new IllegalStateException("Tried accessing value as the R type, but was L type");
        }
        return (R) value;
    }

    public L leftOrNull() {
        return left ? (L) value : null;
    }

    public R rightOrNull() {
        return left ? null : (R) value;
    }

    public <V> V cast() {
        return (V) value;
    }

    public <T> T run(Function<L, T> leftFunction, Function<R, T> rightFunction) {
        return left ? leftFunction.apply((L) value) : rightFunction.apply((R) value);
    }

    public <T> T runLeftOrNull(Function<L, T> function) {
        return left ? function.apply((L) value) : null;
    }

    public <T> T runRightOrNull(Function<R, T> function) {
        return left ? null : function.apply((R) value);
    }

    public <V, T> T runCasting(Function<V, T> function) {
        return function.apply((V) value);
    }
}
