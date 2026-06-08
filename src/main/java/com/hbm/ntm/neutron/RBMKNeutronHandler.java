package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public final class RBMKNeutronHandler {
    public static final String DATA_HAS_LID = "hasLid";
    public static final String DATA_TYPE = "type";
    public static final String DATA_MODERATED = "moderated";
    private static RBMKNeutronSettings settings = RBMKNeutronSettings.legacyDefaults();

    private RBMKNeutronHandler() {
    }

    public static RBMKNeutronSettings settings() {
        return settings;
    }

    public static void setSettings(RBMKNeutronSettings settings) {
        RBMKNeutronHandler.settings = settings == null ? RBMKNeutronSettings.legacyDefaults() : settings;
    }

    public static RBMKNeutronNode makeNode(NeutronNodeWorld.StreamWorld streamWorld, BlockEntity blockEntity) {
        if (!(blockEntity instanceof RBMKNeutronColumn column)) {
            throw new IllegalArgumentException("RBMK neutron nodes require an RBMKNeutronColumn block entity");
        }
        BlockPos pos = blockEntity.getBlockPos();
        NeutronNode node = streamWorld.getNode(pos);
        return node instanceof RBMKNeutronNode rbmkNode ? rbmkNode : new RBMKNeutronNode(blockEntity, column);
    }

    public static void spreadCardinalFlux(BlockEntity blockEntity, double fluxQuantity, double fluxRatio) {
        if (!(blockEntity instanceof RBMKNeutronColumn) || blockEntity.getLevel() == null) {
            return;
        }
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        if (fluxQuantity == 0.0D) {
            NeutronNodeWorld.removeNode(level, pos);
            return;
        }

        NeutronNodeWorld.StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(level);
        RBMKNeutronNode node = makeNode(streamWorld, blockEntity);
        streamWorld.addNode(node);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Vec3 vector = new Vec3(direction.getStepX(), 0.0D, direction.getStepZ());
            new RBMKNeutronStream(node, vector, fluxQuantity, fluxRatio);
        }
    }

    public static void spreadReaSimFlux(BlockEntity blockEntity, double fluxQuantity, double fluxRatio) {
        if (!(blockEntity instanceof RBMKNeutronColumn) || blockEntity.getLevel() == null) {
            return;
        }
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        if (fluxQuantity == 0.0D) {
            NeutronNodeWorld.removeNode(level, pos);
            return;
        }

        NeutronNodeWorld.StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(level);
        RBMKNeutronNode node = makeNode(streamWorld, blockEntity);
        streamWorld.addNode(node);

        Vec3 vector = rotateY(new Vec3(1.0D, 0.0D, 0.0D), Math.toRadians(level.random.nextInt(4) * 9.0D));
        for (int i = 0; i < 8; i++) {
            new RBMKNeutronStream(node, vector, fluxQuantity * 0.75D, fluxRatio);
            vector = rotateY(vector, Math.toRadians(45.0D));
        }
    }

    private static Vec3 rotateY(Vec3 vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(vector.x * cos + vector.z * sin, vector.y, vector.z * cos - vector.x * sin);
    }

    public enum RBMKType {
        ROD,
        MODERATOR,
        CONTROL_ROD,
        REFLECTOR,
        ABSORBER,
        OUTGASSER,
        OTHER
    }

    public static class RBMKNeutronNode extends NeutronNode {
        public RBMKNeutronNode(BlockEntity blockEntity, RBMKNeutronColumn column) {
            super(blockEntity, NeutronType.RBMK);
            getData().put(DATA_HAS_LID, column.hasRBMKLid());
            getData().put(DATA_TYPE, column.getRBMKType());
            getData().put(DATA_MODERATED, column.isRBMKModerated());
        }

        public void addLid() {
            getData().put(DATA_HAS_LID, true);
        }

        public void removeLid() {
            getData().put(DATA_HAS_LID, false);
        }

        public boolean hasLid() {
            return Boolean.TRUE.equals(getData().get(DATA_HAS_LID));
        }

        public RBMKType rbmkType() {
            Object type = getData().get(DATA_TYPE);
            return type instanceof RBMKType rbmkType ? rbmkType : RBMKType.OTHER;
        }

        public boolean isModerated() {
            return Boolean.TRUE.equals(getData().get(DATA_MODERATED));
        }

        public Iterable<BlockPos> getReaSimNodes() {
            List<BlockPos> nodes = new ArrayList<>();
            int range = settings.reasimRange();
            BlockPos origin = getPos();
            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    if (x * x + z * z <= range * range) {
                        nodes.add(origin.offset(x, 0, z));
                    }
                }
            }
            return nodes;
        }

        @Override
        public List<BlockPos> collectStaleNodePositions(NeutronNodeWorld.StreamWorld streamWorld) {
            List<BlockPos> stale = new ArrayList<>();
            if (getBlockEntity() instanceof RBMKRodColumn rod && (!rod.hasFuelRod() || rod.lastFluxQuantity() == 0.0D)) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    RBMKNeutronStream stream = new RBMKNeutronStream(this, new Vec3(direction.getStepX(), 0.0D, direction.getStepZ()));
                    for (NeutronNode node : stream.getNodes(streamWorld, false)) {
                        if (node != null) {
                            stale.add(node.getPos());
                        }
                    }
                }
                return stale;
            }

            boolean hasActiveRod = false;
            for (BlockPos pos : getReaSimNodes()) {
                NeutronNode node = streamWorld.getNode(pos);
                if (node != null
                        && node.getBlockEntity() instanceof RBMKRodColumn rod
                        && rod.hasFuelRod()
                        && rod.lastFluxQuantity() > 0.0D) {
                    hasActiveRod = true;
                    break;
                }
            }
            if (!hasActiveRod && rbmkType() != RBMKType.ROD) {
                stale.add(getPos());
            }
            return stale;
        }
    }

    public static class RBMKNeutronStream extends NeutronStream {
        public RBMKNeutronStream(NeutronNode origin, Vec3 vector) {
            super(origin, vector);
        }

        public RBMKNeutronStream(NeutronNode origin, Vec3 vector, double fluxQuantity, double fluxRatio) {
            super(origin, vector, fluxQuantity, fluxRatio, NeutronType.RBMK);
        }

        public NeutronNode[] getNodes(NeutronNodeWorld.StreamWorld streamWorld, boolean addNode) {
            int fluxRange = settings.fluxRange();
            NeutronNode[] nodes = new NeutronNode[fluxRange];
            Level level = getOrigin().getBlockEntity().getLevel();
            if (level == null) {
                return nodes;
            }

            Iterator<BlockPos> positions = getBlocks(fluxRange);
            for (int i = 0; i < fluxRange && positions.hasNext(); i++) {
                BlockPos pos = positions.next();
                NeutronNode node = streamWorld.getNode(pos);
                if (node instanceof RBMKNeutronNode) {
                    nodes[i] = node;
                    continue;
                }

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof RBMKNeutronColumn) {
                    node = makeNode(streamWorld, blockEntity);
                    nodes[i] = node;
                    if (addNode) {
                        streamWorld.addNode(node);
                    }
                }
            }
            return nodes;
        }

        @Override
        public void runStreamInteraction(Level level, NeutronNodeWorld.StreamWorld streamWorld) {
            if (getFluxQuantity() == 0.0D || !(getOrigin().getBlockEntity() instanceof RBMKNeutronColumn originColumn)) {
                return;
            }

            RBMKNeutronNode originNode = ensureOriginNode(streamWorld);
            if (originNode == null) {
                return;
            }

            int moderatedCount = 0;
            Iterator<BlockPos> iterator = getBlocks(settings.fluxRange());
            while (iterator.hasNext()) {
                BlockPos targetPos = iterator.next();
                if (getFluxQuantity() == 0.0D) {
                    return;
                }

                RBMKNeutronNode targetNode = resolveNode(level, streamWorld, targetPos, true);
                if (targetNode == null) {
                    int hits = getHits(level, targetPos);
                    if (hits == settings.columnHeight()) {
                        return;
                    }
                    leakFromFlux(level, targetPos, hits);
                    if (hits > 0) {
                        setFluxQuantity(getFluxQuantity() * (1.0D - (double) hits / settings.columnHeight()));
                    }
                    continue;
                }

                BlockEntity targetBlockEntity = targetNode.getBlockEntity();
                if (!(targetBlockEntity instanceof RBMKNeutronColumn targetColumn)) {
                    continue;
                }

                RBMKType type = targetNode.rbmkType();
                if (type == RBMKType.OTHER) {
                    continue;
                }

                if (!targetNode.hasLid()) {
                    leak(level, targetPos, (float) (getFluxQuantity() * 0.05D));
                }

                if (type == RBMKType.MODERATOR || targetNode.isModerated()) {
                    moderatedCount++;
                    moderateStream();
                }

                if (targetColumn instanceof RBMKFluxReceiver receiver) {
                    if (type == RBMKType.ROD && targetColumn instanceof RBMKRodColumn rod && rod.hasFuelRod()) {
                        receiver.receiveFlux(this);
                        return;
                    }
                    if (type == RBMKType.OUTGASSER
                            && targetColumn instanceof RBMKOutgasserColumn outgasser
                            && outgasser.canProcessFlux()) {
                        receiver.receiveFlux(this);
                        return;
                    }
                } else if (type == RBMKType.CONTROL_ROD && targetColumn instanceof RBMKControlColumn control) {
                    if (control.controlLevel() > 0.0D) {
                        setFluxQuantity(getFluxQuantity() * control.controlMultiplier());
                        continue;
                    }
                    return;
                } else if (type == RBMKType.REFLECTOR) {
                    handleReflector(originColumn, moderatedCount);
                    if (settings.reflectorEfficiency() != 1.0D) {
                        setFluxQuantity(getFluxQuantity() * settings.reflectorEfficiency());
                        continue;
                    }
                    if (originColumn instanceof RBMKFluxReceiver receiver) {
                        receiver.receiveFlux(this);
                    }
                    return;
                } else if (type == RBMKType.ABSORBER && targetColumn instanceof RBMKAbsorberColumn absorber) {
                    absorber.addAbsorberHeat(settings.absorberHeatConversion() * getFluxQuantity());
                    if (settings.absorberEfficiency() == 1.0D) {
                        return;
                    }
                    setFluxQuantity(getFluxQuantity() * settings.absorberEfficiency());
                }
            }

            handleEscapedFlux(level, streamWorld);
        }

        public int getHits(Level level, BlockPos pos) {
            int hits = 0;
            for (int h = 0; h < settings.columnHeight(); h++) {
                BlockPos hitPos = pos.above(h);
                BlockState state = level.getBlockState(hitPos);
                if (state.isSolidRender(level, hitPos)) {
                    hits++;
                }
            }
            return hits;
        }

        public void moderateStream() {
            setFluxRatio(getFluxRatio() * (1.0D - settings.moderatorEfficiency()));
        }

        private RBMKNeutronNode ensureOriginNode(NeutronNodeWorld.StreamWorld streamWorld) {
            NeutronNode node = streamWorld.getNode(getOrigin().getPos());
            if (node instanceof RBMKNeutronNode rbmkNode) {
                return rbmkNode;
            }
            if (getOrigin().getBlockEntity() instanceof RBMKNeutronColumn) {
                RBMKNeutronNode rbmkNode = makeNode(streamWorld, getOrigin().getBlockEntity());
                streamWorld.addNode(rbmkNode);
                return rbmkNode;
            }
            return null;
        }

        private RBMKNeutronNode resolveNode(Level level, NeutronNodeWorld.StreamWorld streamWorld, BlockPos pos, boolean addNode) {
            NeutronNode node = streamWorld.getNode(pos);
            if (node instanceof RBMKNeutronNode rbmkNode) {
                return rbmkNode;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof RBMKNeutronColumn)) {
                return null;
            }
            RBMKNeutronNode rbmkNode = makeNode(streamWorld, blockEntity);
            if (addNode) {
                streamWorld.addNode(rbmkNode);
            }
            return rbmkNode;
        }

        private void handleReflector(RBMKNeutronColumn originColumn, int moderatedCount) {
            int count = moderatedCount;
            if (originColumn.isRBMKModerated()) {
                count++;
            }
            if (getFluxRatio() > 0.0D && count > 0) {
                for (int i = 0; i < count; i++) {
                    moderateStream();
                }
            }
        }

        private void handleEscapedFlux(Level level, NeutronNodeWorld.StreamWorld streamWorld) {
            NeutronNode[] nodes = getNodes(streamWorld, true);
            NeutronNode lastNode = nodes.length == 0 ? null : nodes[nodes.length - 1];
            if (!(lastNode instanceof RBMKNeutronNode rbmkNode)) {
                leakFromFlux(level, nextBlockAfter(getOrigin().getPos()), getHits(level, nextBlockAfter(getOrigin().getPos())));
                return;
            }

            if (rbmkNode.rbmkType() == RBMKType.CONTROL_ROD
                    && rbmkNode.getBlockEntity() instanceof RBMKControlColumn control
                    && control.controlMultiplier() > 0.0D) {
                setFluxQuantity(getFluxQuantity() * control.controlMultiplier());
                BlockPos posAfter = nextBlockAfter(rbmkNode.getPos());
                if (resolveNode(level, streamWorld, posAfter, true) == null) {
                    leakFromFlux(level, posAfter, getHits(level, posAfter));
                }
            }
        }

        private BlockPos nextBlockAfter(BlockPos pos) {
            int x = Mth.floor(0.5D + getVector().x);
            int y = Mth.floor(0.5D + getVector().y);
            int z = Mth.floor(0.5D + getVector().z);
            return pos.offset(x, y, z);
        }

        private void leakFromFlux(Level level, BlockPos pos, int hits) {
            leak(level, pos, (float) (getFluxQuantity() * 0.05D * (1.0D - (double) hits / settings.columnHeight())));
        }

        private void leak(Level level, BlockPos pos, float radiation) {
            if (radiation > 0.0F) {
                settings.leakHandler().leak(level, pos, radiation);
            }
        }
    }
}
