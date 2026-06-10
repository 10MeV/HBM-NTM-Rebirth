package com.hbm.util;

/**
 * Legacy package facade for the tuple helpers used by old registry extension lists.
 */
@Deprecated(forRemoval = false)
public final class Tuple {
    private Tuple() {
    }

    public static class Pair<X, Y> extends com.hbm.ntm.util.Tuple.Pair<X, Y> {
        public Pair(X key, Y value) {
            super(key, value);
        }
    }

    public static class Triplet<X, Y, Z> extends com.hbm.ntm.util.Tuple.Triplet<X, Y, Z> {
        public Triplet(X x, Y y, Z z) {
            super(x, y, z);
        }
    }

    public static class Quartet<W, X, Y, Z> extends com.hbm.ntm.util.Tuple.Quartet<W, X, Y, Z> {
        public Quartet(W w, X x, Y y, Z z) {
            super(w, x, y, z);
        }

        @Override
        public Quartet<W, X, Y, Z> clone() {
            return new Quartet<>(getW(), getX(), getY(), getZ());
        }
    }

    public static class Quintet<V, W, X, Y, Z> extends com.hbm.ntm.util.Tuple.Quintet<V, W, X, Y, Z> {
        public Quintet(V v, W w, X x, Y y, Z z) {
            super(v, w, x, y, z);
        }
    }
}
