package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.Mk2PileStructureBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Old TileEntityPileBaseMK2 equivalent: only tracks its owning dynamic core. */
public final class Mk2PileMemberBlockEntity extends BlockEntity implements LegacyLookOverlayProvider {
    private BlockPos corePos = BlockPos.ZERO;

    public Mk2PileMemberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MK2_PILE_MEMBER.get(), pos, state);
    }

    public void setCorePos(BlockPos corePos) {
        this.corePos = corePos == null ? BlockPos.ZERO : corePos.immutable();
        setChanged();
    }

    public BlockPos corePos() {
        return corePos;
    }

    /**
     * BlockPile#printHook is role-local.  This dynamic pile deliberately does not use a fixed
     * MultiblockCoreBlock layout, so a hit member must provide its own source-backed port line.
     */
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        if (!worldPosition.equals(viewedPos)) {
            return null;
        }
        Mk2PileStructureBlock.Role role = getBlockState().getValue(Mk2PileStructureBlock.ROLE);
        String line = switch (role) {
            case FUEL_IN -> "Fuel Loading Port";
            case FUEL_OUT -> "Fuel Ejection Port";
            case AIR_IN -> "Air Inlet";
            case AIR_OUT -> "Air Outlet";
            case CONTROL -> "Control Rod Channel";
            default -> null;
        };
        return line == null ? null : LegacyLookOverlay.forBlock(this, List.of(Component.literal(line)));
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder().with(Mk2PileConnectedTextureData.CONNECTION_MASK,
                Mk2PileConnectedTextureData.connectionMask(level, worldPosition)).build();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("cX", corePos.getX());
        tag.putInt("cY", corePos.getY());
        tag.putInt("cZ", corePos.getZ());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        corePos = new BlockPos(tag.getInt("cX"), tag.getInt("cY"), tag.getInt("cZ"));
    }
}
