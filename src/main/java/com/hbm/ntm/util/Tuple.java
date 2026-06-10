package com.hbm.ntm.util;

/**
 * Legacy-name tuple container facade.
 */
@Deprecated(forRemoval = false)
public final class Tuple {
    private Tuple() {
    }

    public static class Pair<X, Y> extends HbmTuple.Pair<X, Y> {
        public Pair(X key, Y value) {
            super(key, value);
        }
    }

    public static class Triplet<X, Y, Z> extends HbmTuple.Triplet<X, Y, Z> {
        public Triplet(X x, Y y, Z z) {
            super(x, y, z);
        }
    }

    public static class Quartet<W, X, Y, Z> extends HbmTuple.Quartet<W, X, Y, Z> {
        public Quartet(W w, X x, Y y, Z z) {
            super(w, x, y, z);
        }

        @Override
        public Quartet<W, X, Y, Z> clone() {
            return new Quartet<>(getW(), getX(), getY(), getZ());
        }
    }

    public static class Quintet<V, W, X, Y, Z> extends HbmTuple.Quintet<V, W, X, Y, Z> {
        public Quintet(V v, W w, X x, Y y, Z z) {
            super(v, w, x, y, z);
        }
    }
}
