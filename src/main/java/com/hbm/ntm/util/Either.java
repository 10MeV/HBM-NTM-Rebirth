package com.hbm.ntm.util;

import java.util.function.Function;

/**
 * Legacy-name either type.
 */
@Deprecated(forRemoval = false)
@SuppressWarnings("unchecked")
public final class Either<L, R> {
    private final Object value;
    private final boolean left;

    private Either(Object value, boolean left) {
        this.value = value;
        this.left = left;
    }

    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(value, true);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(value, false);
    }

    public static <L, R> Either<L, R> fromModern(HbmEither<L, R> either) {
        return either.isLeft() ? left(either.left()) : right(either.right());
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

    public HbmEither<L, R> modern() {
        return left ? HbmEither.left((L) value) : HbmEither.right((R) value);
    }
}
