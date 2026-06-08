package com.hbm.ntm.util;

import java.util.Objects;

public final class HbmTuples {
    private static final int PRIME = 27644437;

    private HbmTuples() {
    }

    public static final class Pair<X, Y> {
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
            return PRIME * (PRIME + Objects.hashCode(key)) + Objects.hashCode(value);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj
                    || (obj instanceof Pair<?, ?> other
                    && Objects.equals(key, other.key)
                    && Objects.equals(value, other.value));
        }
    }

    public static final class Triplet<X, Y, Z> {
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
            return ((PRIME * (PRIME + Objects.hashCode(x)) + Objects.hashCode(y)) * PRIME)
                    + Objects.hashCode(z);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj
                    || (obj instanceof Triplet<?, ?, ?> other
                    && Objects.equals(x, other.x)
                    && Objects.equals(y, other.y)
                    && Objects.equals(z, other.z));
        }
    }

    public static final class Quartet<W, X, Y, Z> implements Cloneable {
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

        @Override
        public Quartet<W, X, Y, Z> clone() {
            return new Quartet<>(w, x, y, z);
        }

        @Override
        public int hashCode() {
            int result = PRIME + Objects.hashCode(w);
            result = PRIME * result + Objects.hashCode(x);
            result = PRIME * result + Objects.hashCode(y);
            result = PRIME * result + Objects.hashCode(z);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj
                    || (obj instanceof Quartet<?, ?, ?, ?> other
                    && Objects.equals(w, other.w)
                    && Objects.equals(x, other.x)
                    && Objects.equals(y, other.y)
                    && Objects.equals(z, other.z));
        }
    }

    public static final class Quintet<V, W, X, Y, Z> {
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
            result = PRIME * result + Objects.hashCode(z);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj
                    || (obj instanceof Quintet<?, ?, ?, ?, ?> other
                    && Objects.equals(v, other.v)
                    && Objects.equals(w, other.w)
                    && Objects.equals(x, other.x)
                    && Objects.equals(y, other.y)
                    && Objects.equals(z, other.z));
        }
    }
}
