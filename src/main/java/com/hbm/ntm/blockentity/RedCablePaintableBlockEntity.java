package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.common.ICopiable;
import com.hbm.ntm.client.ClientGeometryInvalidationBridge;
import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Source: BlockCablePaintable.TileEntityCablePaintable. */
public class RedCablePaintableBlockEntity extends HbmEnergyNodeBlockEntity
        implements PaintableDuctBlockEntity, ICopiable {
    private static final String TAG_PAINT_BLOCK = "block";
    private static final String TAG_PAINT_META = "meta";
    private static final String TAG_PAINT_BLOCK_NAME = "paint_block";

    @Nullable
    private BlockState paintedState;
    private int paintedMeta;

    public RedCablePaintableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_CABLE_PAINTABLE.get(), pos, state);
    }

    /** Legacy TileEntityCableBaseNT uses its unconditional six-way PowerNode. */
    @Override
    protected HbmEnergyNode createEnergyNode() {
        return HbmEnergyNode.withStandardLegacyConnections(worldPosition);
    }

    @Nullable
    @Override
    public BlockState getPaintedState() {
        return paintedState;
    }

    @Override
    public int getPaintedMeta() {
        return paintedMeta;
    }

    @Override
    public void setPaintedState(@Nullable BlockState state, int legacyMeta) {
        if (state != null && state.isAir()) {
            state = null;
        }
        paintedState = state;
        paintedMeta = legacyMeta & 15;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        } else {
            refreshPaintModelData();
        }
    }

    @Override
    public @NotNull ModelData getModelData() {
        return paintedState == null
                ? ModelData.EMPTY
                : ModelData.builder().with(PaintableDuctBlockEntity.PAINTED_STATE_PROPERTY, paintedState).build();
    }

    @Override
    public CompoundTag getSettings(Level level, BlockPos pos) {
        return addPaintSettings(new CompoundTag());
    }

    @Override
    public void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos) {
        pastePaintSettings(tag);
    }

    @Override
    public List<Component> infoForDisplay(Level level, BlockPos pos) {
        return paintSettingsDisplayInfo();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        savePaint(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadPaint(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    /** Only legacy paint state is client-visible; the energy node stays server-only. */
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        savePaint(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        readClientSyncTag(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            readClientSyncTag(tag);
        }
    }

    private void savePaint(CompoundTag tag) {
        if (paintedState == null) {
            tag.remove(TAG_PAINT_BLOCK);
            tag.remove(TAG_PAINT_BLOCK_NAME);
        } else {
            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(paintedState.getBlock());
            if (key != null) {
                tag.putString(TAG_PAINT_BLOCK_NAME, key.toString());
            }
            int legacyId = Block.getId(paintedState);
            if (legacyId != 0) {
                tag.putInt(TAG_PAINT_BLOCK, legacyId);
            }
        }
        tag.putInt(TAG_PAINT_META, paintedMeta & 15);
    }

    private void loadPaint(CompoundTag tag) {
        BlockState state = null;
        if (tag.contains(TAG_PAINT_BLOCK_NAME)) {
            ResourceLocation key = ResourceLocation.tryParse(tag.getString(TAG_PAINT_BLOCK_NAME));
            Block block = key == null ? null : ForgeRegistries.BLOCKS.getValue(key);
            if (block != null && block != Blocks.AIR) {
                state = PaintableDuctBlockEntity.stateFromLegacyMeta(block, tag.getInt(TAG_PAINT_META));
            }
        }
        if (state == null && tag.contains(TAG_PAINT_BLOCK)) {
            BlockState legacyState = Block.stateById(tag.getInt(TAG_PAINT_BLOCK));
            if (!legacyState.isAir()) {
                state = PaintableDuctBlockEntity.stateFromLegacyMeta(legacyState.getBlock(),
                        tag.getInt(TAG_PAINT_META));
            }
        }
        paintedState = state;
        paintedMeta = tag.getInt(TAG_PAINT_META) & 15;
        refreshPaintModelData();
    }

    private void readClientSyncTag(CompoundTag tag) {
        loadPaint(tag);
    }

    private void refreshPaintModelData() {
        if (level != null && level.isClientSide) {
            requestModelDataUpdate();
            ClientGeometryInvalidationBridge.schedule(worldPosition);
        }
    }
}
