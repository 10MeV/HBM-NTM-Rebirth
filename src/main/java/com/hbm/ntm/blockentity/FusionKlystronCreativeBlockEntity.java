package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fusion.FusionKlystronProvider;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeExtraData;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.uninos.networkproviders.KlystronNetwork;
import com.hbm.ntm.uninos.networkproviders.KlystronNode;
import com.hbm.ntm.uninos.networkproviders.KlystronNodespace;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FusionKlystronCreativeBlockEntity extends BlockEntity
        implements FusionKlystronProvider, HbmLegacyLoadedTile {
    public static final long MAX_OUTPUT = 10_000_000L;
    public static final float FAN_ACCELERATION = 0.125F;
    private static final String TAG_CONNECTED = "connected";

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private KlystronNode klystronNode;
    private boolean connected;
    private float fan;
    private float prevFan;
    private float fanSpeed;
    private Object audio;

    public FusionKlystronCreativeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_KLYSTRON_CREATIVE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            FusionKlystronCreativeBlockEntity klystron) {
        klystron.ensureNode(level);
        klystron.networkPackNT(100);
        if (level.getGameTime() % 20L == 0L) {
            klystron.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state,
            FusionKlystronCreativeBlockEntity klystron) {
        klystron.prevFan = klystron.fan;
        klystron.fanSpeed += klystron.connected ? FAN_ACCELERATION : -FAN_ACCELERATION;
        klystron.fanSpeed = Math.max(0.0F, Math.min(5.0F, klystron.fanSpeed));
        klystron.fan += klystron.fanSpeed;
        if (klystron.fan >= 360.0F) {
            klystron.fan -= 360.0F;
            klystron.prevFan -= 360.0F;
        }
        float speed = klystron.fanSpeed / 5.0F;
        klystron.audio = LegacyMachineAudioBridge.updateLoop(klystron.audio, klystron, "FEL_LOOP",
                klystron.fanSpeed > 0.0F, 30.0D, 15.0F, klystron.getVolume(speed), speed);
    }

    public float getFan(float partialTick) {
        return prevFan + (fan - prevFan) * partialTick;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public long provideKlystronEnergy() {
        return 0L;
    }

    private long getCreativeOutput() {
        if (level == null) {
            return MAX_OUTPUT;
        }
        return GenericMachineRecipeRuntime.recipes(level, GenericMachineRecipe.Machine.FUSION_REACTOR).stream()
                .flatMap(recipe -> recipe.getExtraData().fusion().stream())
                .mapToLong(GenericMachineRecipeExtraData.Fusion::ignitionTemp)
                .max()
                .orElse(MAX_OUTPUT);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = HbmLegacyLoadedTile.super.getClientSyncTag();
        tag.putBoolean(TAG_CONNECTED, connected);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        HbmLegacyLoadedTile.super.handleClientSyncTag(tag);
        connected = tag.getBoolean(TAG_CONNECTED);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeBoolean(connected);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        connected = data.readBoolean();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-4, 0, -4), worldPosition.offset(5, 5, 5));
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && klystronNode != null) {
            KlystronNodespace.destroyNode(level, klystronNode.getPos());
        }
        super.setRemoved();
    }

    private void ensureNode(Level level) {
        Direction direction = facing().getOpposite();
        BlockPos nodePos = worldPosition.relative(direction, 4).above(2);
        if (klystronNode == null || klystronNode.isExpired()) {
            KlystronNode existing = KlystronNodespace.getNode(level, nodePos);
            klystronNode = existing == null
                    ? KlystronNodespace.createNode(level, new KlystronNode(nodePos, Set.of(direction)))
                    : existing;
        }
        KlystronNetwork network = klystronNode.getKlystronNet();
        connected = FusionKlystronBlockEntity.provideKyU(network, getCreativeOutput());
        if (network != null) {
            network.addProvider(this);
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }
}
