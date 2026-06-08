package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class NeutronStream {
    private final NeutronNode origin;
    private final Vec3 vector;
    private final NeutronType type;
    private double fluxQuantity;
    private double fluxRatio;

    protected NeutronStream(NeutronNode origin, Vec3 vector) {
        this(origin, vector, 0.0D, 0.0D, NeutronType.DUMMY, false);
    }

    protected NeutronStream(NeutronNode origin, Vec3 vector, double fluxQuantity, double fluxRatio, NeutronType type) {
        this(origin, vector, fluxQuantity, fluxRatio, type, true);
    }

    protected NeutronStream(NeutronNode origin, Vec3 vector, double fluxQuantity, double fluxRatio, NeutronType type, boolean register) {
        this.origin = origin;
        this.vector = vector;
        this.fluxQuantity = fluxQuantity;
        this.fluxRatio = fluxRatio;
        this.type = type;
        if (register) {
            Level level = origin.getBlockEntity().getLevel();
            if (level != null) {
                NeutronNodeWorld.getOrAddWorld(level).addStream(this);
            }
        }
    }

    public NeutronNode getOrigin() {
        return origin;
    }

    public Vec3 getVector() {
        return vector;
    }

    public NeutronType getType() {
        return type;
    }

    public double getFluxQuantity() {
        return fluxQuantity;
    }

    public void setFluxQuantity(double fluxQuantity) {
        this.fluxQuantity = fluxQuantity;
    }

    public double getFluxRatio() {
        return fluxRatio;
    }

    public void setFluxRatio(double fluxRatio) {
        this.fluxRatio = fluxRatio;
    }

    public Iterator<BlockPos> getBlocks(int range) {
        BlockPos originPos = origin.getPos();
        return new Iterator<>() {
            private int index = 1;

            @Override
            public boolean hasNext() {
                return index <= range;
            }

            @Override
            public BlockPos next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int x = Mth.floor(0.5D + vector.x * index);
                int y = Mth.floor(0.5D + vector.y * index);
                int z = Mth.floor(0.5D + vector.z * index);
                index++;
                return originPos.offset(x, y, z);
            }
        };
    }

    public abstract void runStreamInteraction(Level level, NeutronNodeWorld.StreamWorld streamWorld);
}
