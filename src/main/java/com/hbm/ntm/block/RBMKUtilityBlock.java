package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RBMKSteamInletBlockEntity;
import com.hbm.ntm.blockentity.RBMKSteamOutletBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectorBlock;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RBMKUtilityBlock extends BaseEntityBlock implements HbmFluidConnectorBlock {
    private final Kind kind;

    public RBMKUtilityBlock(BlockBehaviour.Properties properties, Kind kind) {
        super(properties);
        this.kind = kind == null ? Kind.LOADER : kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        LegacyStandardInfoTooltip.append(tooltip, switch (kind) {
            case LOADER -> "rbmk_loader";
            case STEAM_INLET -> "rbmk_steam_inlet";
            case STEAM_OUTLET -> "rbmk_steam_outlet";
        });
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case STEAM_INLET -> new RBMKSteamInletBlockEntity(pos, state);
            case STEAM_OUTLET -> new RBMKSteamOutletBlockEntity(pos, state);
            case LOADER -> null;
        };
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return switch (kind) {
            case STEAM_INLET -> createTickerHelper(type, ModBlockEntities.RBMK_STEAM_INLET.get(),
                    RBMKSteamInletBlockEntity::serverTick);
            case STEAM_OUTLET -> createTickerHelper(type, ModBlockEntities.RBMK_STEAM_OUTLET.get(),
                    RBMKSteamOutletBlockEntity::serverTick);
            case LOADER -> null;
        };
    }

    @Override
    public boolean canConnectFluid(BlockGetter level, BlockPos pos, FluidType type, Direction side) {
        if (side == null || type == null || type == HbmFluids.NONE) {
            return false;
        }
        return switch (kind) {
            case LOADER -> side == Direction.UP
                    ? type.hasTrait(HeatableFluidTrait.class)
                    : type.hasTrait(CoolableFluidTrait.class) || type == HbmFluids.PERFLUOROMETHYL;
            case STEAM_INLET -> type == HbmFluids.WATER;
            case STEAM_OUTLET -> type == HbmFluids.SUPERHOTSTEAM;
        };
    }

    public enum Kind implements StringRepresentable {
        LOADER("loader"),
        STEAM_INLET("steam_inlet"),
        STEAM_OUTLET("steam_outlet");

        private final String serializedName;

        Kind(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
