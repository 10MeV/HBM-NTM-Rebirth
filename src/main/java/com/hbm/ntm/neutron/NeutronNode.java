package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class NeutronNode {
    private final NeutronType type;
    private final BlockPos pos;
    private final BlockEntity blockEntity;
    private final Map<String, Object> data = new HashMap<>();

    protected NeutronNode(BlockEntity blockEntity, NeutronType type) {
        this.type = type;
        this.blockEntity = blockEntity;
        this.pos = blockEntity.getBlockPos().immutable();
    }

    public NeutronType getType() {
        return type;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public boolean isValid() {
        return !blockEntity.isRemoved() && blockEntity.getLevel() != null;
    }

    public List<BlockPos> collectStaleNodePositions(NeutronNodeWorld.StreamWorld streamWorld) {
        return List.of();
    }
}
