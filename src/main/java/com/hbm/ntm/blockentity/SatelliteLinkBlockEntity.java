package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.world.WorldUtil;
import com.hbm.saveddata.satellites.SatelliteRayScan;
import com.hbm.saveddata.satellites.SatelliteBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Runtime state and ROR contract of legacy {@code TileEntityMachineSatLink}. */
public class SatelliteLinkBlockEntity extends BlockEntity
        implements RORValueProvider, RORInteractive, LegacyLookOverlayProvider {
    public static final float SPEED = 0.25F;
    public static final float ACTIVE_ROT = -15.0F;
    public static final float ACTIVE_LIFT = -45.0F;
    public static final float INACTIVE_ROT = 0.0F;
    public static final float INACTIVE_LIFT = -85.0F;

    private boolean connected;
    private int frequency;
    private float rotation = INACTIVE_ROT;
    private float previousRotation = INACTIVE_ROT;
    private float lift = INACTIVE_LIFT;
    private float previousLift = INACTIVE_LIFT;

    public SatelliteLinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_SATLINK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SatelliteLinkBlockEntity link) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        boolean wasConnected = link.connected;
        link.connected = WorldUtil.legacyGetHeightValue(level, pos.getX(), pos.getZ()) <= pos.getY()
                && SatelliteSavedData.getData(serverLevel).isFreqTaken(link.frequency);
        if (wasConnected != link.connected || level.getGameTime() % 150L == 0L) {
            link.sync();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SatelliteLinkBlockEntity link) {
        link.previousRotation = link.rotation;
        link.previousLift = link.lift;
        float targetRotation = link.connected ? ACTIVE_ROT : INACTIVE_ROT;
        float targetLift = link.connected ? ACTIVE_LIFT : INACTIVE_LIFT;
        link.rotation = approach(link.rotation, targetRotation);
        link.lift = approach(link.lift, targetLift);
    }

    private static float approach(float current, float target) {
        if (Math.abs(current - target) <= SPEED) {
            return target;
        }
        return current < target ? current + SPEED : current - SPEED;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
        sync();
    }

    public float getRotation() {
        return rotation;
    }

    public float getPreviousRotation() {
        return previousRotation;
    }

    public float getLift() {
        return lift;
    }

    public float getPreviousLift() {
        return previousLift;
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                RORInfo.PREFIX_VALUE + "connected",
                RORInfo.PREFIX_VALUE + "freq",
                RORInfo.PREFIX_VALUE + "rx",
                RORInfo.PREFIX_FUNCTION + "setfreq" + RORInteractive.NAME_SEPARATOR + "freq",
                RORInfo.PREFIX_FUNCTION + "tx" + RORInteractive.NAME_SEPARATOR + "payload"
        };
    }

    @Override
    public @Nullable String provideRORValue(String name) {
        if ((RORInfo.PREFIX_VALUE + "connected").equals(name)) {
            return connected ? "TRUE" : "FALSE";
        }
        if ((RORInfo.PREFIX_VALUE + "freq").equals(name)) {
            return Integer.toString(frequency);
        }
        if ((RORInfo.PREFIX_VALUE + "rx").equals(name) && level instanceof ServerLevel serverLevel) {
            com.hbm.ntm.satellite.Satellite satellite = SatelliteSavedData.getData(serverLevel)
                    .getSatFromFreq(frequency);
            return satellite instanceof SatelliteBase satelliteBase ? satelliteBase.tx : "";
        }
        return null;
    }

    @Override
    public @Nullable String runRORFunction(String name, String[] params) {
        if ((RORInfo.PREFIX_FUNCTION + "setfreq").equals(name) && params.length == 1) {
            setFrequency(RORInteractive.parseInt(params[0], 0, 100_000));
        }
        if ((RORInfo.PREFIX_FUNCTION + "tx").equals(name) && level instanceof ServerLevel serverLevel) {
            com.hbm.ntm.satellite.Satellite satellite = SatelliteSavedData.getData(serverLevel)
                    .getSatFromFreq(frequency);
            String[] command = String.join(RORInteractive.PARAM_SEPARATOR, params).split(" ");
            if (satellite instanceof SatelliteBase satelliteBase) {
                satelliteBase.onCommand(serverLevel, command);
            }
            SatelliteRayScan.reportEvent(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    SatelliteRayScan.RayEvent.INFO_RADIO, 300);
            sync();
        }
        return null;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                net.minecraft.network.chat.Component.literal("Freq: " + frequency),
                net.minecraft.network.chat.Component.literal("Connected: " + (connected ? "Yes" : "No"))
                        .withStyle(connected ? ChatFormatting.GREEN : ChatFormatting.RED)));
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2,
                worldPosition.getX() + 3, worldPosition.getY() + 10, worldPosition.getZ() + 3);
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("freq", frequency);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        frequency = tag.getInt("freq");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("connected", connected);
        tag.putInt("freq", frequency);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        connected = tag.getBoolean("connected");
        frequency = tag.getInt("freq");
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        handleUpdateTag(packet.getTag());
    }
}
