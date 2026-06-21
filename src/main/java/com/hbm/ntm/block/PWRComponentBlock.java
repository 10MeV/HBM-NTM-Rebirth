package com.hbm.ntm.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;

public class PWRComponentBlock extends Block {
    private final Kind kind;

    public PWRComponentBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    public enum Kind {
        HEATEX,
        HEATSINK,
        NEUTRON_SOURCE,
        REFLECTOR,
        CASING,
        PORT
    }

    public static class Pillar extends RotatedPillarBlock {
        private final PillarKind kind;

        public Pillar(Properties properties, PillarKind kind) {
            super(properties);
            this.kind = kind;
        }

        public PillarKind kind() {
            return kind;
        }
    }

    public enum PillarKind {
        FUEL,
        CONTROL,
        CHANNEL
    }
}
