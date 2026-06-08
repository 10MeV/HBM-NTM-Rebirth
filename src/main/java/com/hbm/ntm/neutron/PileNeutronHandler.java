package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class PileNeutronHandler {
    private static PileNeutronSettings settings = PileNeutronSettings.legacyDefaults();

    private PileNeutronHandler() {
    }

    public static PileNeutronSettings settings() {
        return settings;
    }

    public static void setSettings(PileNeutronSettings settings) {
        PileNeutronHandler.settings = settings == null ? PileNeutronSettings.legacyDefaults() : settings;
    }

    public static PileNeutronNode makeNode(NeutronNodeWorld.StreamWorld streamWorld, BlockEntity blockEntity) {
        if (!(blockEntity instanceof PileNeutronColumn)) {
            throw new IllegalArgumentException("Pile neutron nodes require a PileNeutronColumn block entity");
        }
        NeutronNode node = streamWorld.getNode(blockEntity.getBlockPos());
        return node instanceof PileNeutronNode pileNode ? pileNode : new PileNeutronNode(blockEntity);
    }

    public static void castRandomRay(BlockEntity blockEntity, int flux) {
        if (!(blockEntity instanceof PileNeutronColumn) || blockEntity.getLevel() == null) {
            return;
        }
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        if (flux == 0) {
            NeutronNodeWorld.removeNode(level, pos);
            return;
        }

        NeutronNodeWorld.StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(level);
        PileNeutronNode node = makeNode(streamWorld, blockEntity);
        streamWorld.addNode(node);

        Vec3 neutronVector = new Vec3(1.0D, 0.0D, 0.0D);
        neutronVector = rotateZ(neutronVector, Math.PI * 2.0D * level.random.nextDouble());
        neutronVector = rotateY(neutronVector, Math.PI * 2.0D * level.random.nextDouble());
        neutronVector = rotateX(neutronVector, Math.PI * 2.0D * level.random.nextDouble());
        new PileNeutronStream(node, neutronVector, flux);
    }

    private static Vec3 rotateX(Vec3 vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(vector.x, vector.y * cos - vector.z * sin, vector.y * sin + vector.z * cos);
    }

    private static Vec3 rotateY(Vec3 vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(vector.x * cos + vector.z * sin, vector.y, vector.z * cos - vector.x * sin);
    }

    private static Vec3 rotateZ(Vec3 vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(vector.x * cos - vector.y * sin, vector.x * sin + vector.y * cos, vector.z);
    }

    public static class PileNeutronNode extends NeutronNode {
        public PileNeutronNode(BlockEntity blockEntity) {
            super(blockEntity, NeutronType.PILE);
        }
    }

    public static class PileNeutronStream extends NeutronStream {
        public PileNeutronStream(NeutronNode origin, Vec3 vector, double fluxQuantity) {
            super(origin, vector, fluxQuantity, 0.0D, NeutronType.PILE);
        }

        @Override
        public void runStreamInteraction(Level level, NeutronNodeWorld.StreamWorld streamWorld) {
            if (!(getOrigin().getBlockEntity() instanceof PileNeutronColumn)) {
                return;
            }

            BlockPos current = getOrigin().getPos();
            for (double distance = 1.0D; distance <= settings.range(); distance += settings.step()) {
                BlockPos nodePos = new BlockPos(
                        Mth.floor(current.getX() + 0.5D + getVector().x * distance),
                        Mth.floor(current.getY() + 0.5D + getVector().y * distance),
                        Mth.floor(current.getZ() + 0.5D + getVector().z * distance));

                if (nodePos.equals(current)) {
                    continue;
                }
                current = nodePos;

                BlockState state = level.getBlockState(nodePos);
                BlockEntity blockEntity = resolveBlockEntity(level, streamWorld, nodePos);
                if (!(blockEntity instanceof PileNeutronColumn)) {
                    PileNeutronBlockResult result = settings.blockRules().evaluate(level, nodePos, state, blockEntity);
                    if (result.shouldStop()) {
                        return;
                    }
                    setFluxQuantity(getFluxQuantity() * result.fluxMultiplier());
                    if (blockEntity == null && result.fluxMultiplier() == 1.0D) {
                        return;
                    }
                } else if (blockEntity == null) {
                    return;
                }

                if (blockEntity instanceof PileNeutronReceiver receiver) {
                    receiver.receiveNeutrons(Mth.floor(getFluxQuantity()));
                    if (!(receiver instanceof PileNeutronPassthroughReceiver passthrough)
                            || !passthrough.allowsPileNeutronPassthrough()) {
                        return;
                    }
                }

                settings.radiationHandler().radiateEntities(level, nodePos, (float) (getFluxQuantity() / 4.0D));
            }
        }

        private BlockEntity resolveBlockEntity(Level level, NeutronNodeWorld.StreamWorld streamWorld, BlockPos pos) {
            NeutronNode node = streamWorld.getNode(pos);
            if (node instanceof PileNeutronNode) {
                return node.getBlockEntity();
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PileNeutronColumn) {
                streamWorld.addNode(new PileNeutronNode(blockEntity));
            }
            return blockEntity;
        }
    }
}
