package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.MediumPylonBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacyMediumPylonBlock extends LegacyPylonBlock {
    private static final int[] LEGACY_DIMENSIONS = { 6, 0, 0, 0, 0, 0 };

    private final Kind kind;

    public LegacyMediumPylonBlock(Properties properties, Kind kind) {
        super(properties, LEGACY_DIMENSIONS);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MediumPylonBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getPylonBlockEntityType() {
        return ModBlockEntities.RED_PYLON_MEDIUM.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        appendLegacyWireTooltip(tooltip, "Triple", 45);
    }

    public enum Kind {
        WOOD(false, false),
        WOOD_TRANSFORMER(false, true),
        STEEL(true, false),
        STEEL_TRANSFORMER(true, true);

        private final boolean steel;
        private final boolean transformer;

        Kind(boolean steel, boolean transformer) {
            this.steel = steel;
            this.transformer = transformer;
        }

        public boolean steel() {
            return steel;
        }

        public boolean transformer() {
            return transformer;
        }
    }
}
