package com.hbm.ntm.util;

import java.util.Objects;

public final class HbmTuple {
    private static final int PRIME = 27_644_437;

    private HbmTuple() {
    }

    public static class Pair<X, Y> {
        public X key;
        public Y value;

        public Pair(X key, Y value) {
            this.key = key;
            this.value = value;
        }

        public X getKey() {
            return key;
        }

        public Y getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return PRIME * Objects.hashCode(key) + Objects.hashCode(value);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Pair<?, ?> pair
                    && Objects.equals(key, pair.key)
                    && Objects.equals(value, pair.value);
        }
    }

    public static class Triplet<X, Y, Z> {
        private final X x;
        private final Y y;
        private final Z z;

        public Triplet(X x, Y y, Z z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public X getX() {
            return x;
        }

        public Y getY() {
            return y;
        }

        public Z getZ() {
            return z;
        }

        @Override
        public int hashCode() {
            return ((PRIME + Objects.hashCode(x)) * PRIME + Objects.hashCode(y)) * PRIME + Objects.hashCode(z);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Triplet<?, ?, ?> triplet
                    && Objects.equals(x, triplet.x)
                    && Objects.equals(y, triplet.y)
                    && Objects.equals(z, triplet.z);
        }
    }

    public static class Quartet<W, X, Y, Z> {
        private W w;
        private X x;
        private Y y;
        private Z z;

        public Quartet(W w, X x, Y y, Z z) {
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public W getW() {
            return w;
        }

        public X getX() {
            return x;
        }

        public Y getY() {
            return y;
        }

        public Z getZ() {
            return z;
        }

        public void mangle(W w, X x, Y y, Z z) {
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Quartet<W, X, Y, Z> copy() {
            return new Quartet<>(w, x, y, z);
        }

        @Override
        public Quartet<W, X, Y, Z> clone() {
            return copy();
        }

        @Override
        public int hashCode() {
            int result = PRIME + Objects.hashCode(w);
            result = PRIME * result + Objects.hashCode(x);
            result = PRIME * result + Objects.hashCode(y);
            return PRIME * result + Objects.hashCode(z);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Quartet<?, ?, ?, ?> quartet
                    && Objects.equals(w, quartet.w)
                    && Objects.equals(x, quartet.x)
                    && Objects.equals(y, quartet.y)
                    && Objects.equals(z, quartet.z);
        }
    }

    public static class Quintet<V, W, X, Y, Z> {
        private final V v;
        private final W w;
        private final X x;
        private final Y y;
        private final Z z;

        public Quintet(V v, W w, X x, Y y, Z z) {
            this.v = v;
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public V getV() {
            return v;
        }

        public W getW() {
            return w;
        }

        public X getX() {
            return x;
        }

        public Y getY() {
            return y;
        }

        public Z getZ() {
            return z;
        }

        @Override
        public int hashCode() {
            int result = PRIME + Objects.hashCode(v);
            result = PRIME * result + Objects.hashCode(w);
            result = PRIME * result + Objects.hashCode(x);
            result = PRIME * result + Objects.hashCode(y);
            return PRIME * result + Objects.hashCode(z);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Quintet<?, ?, ?, ?, ?> quintet
                    && Objects.equals(v, quintet.v)
                    && Objects.equals(w, quintet.w)
                    && Objects.equals(x, quintet.x)
                    && Objects.equals(y, quintet.y)
                    && Objects.equals(z, quintet.z);
        }
    }
}
