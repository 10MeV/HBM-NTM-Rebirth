package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.common.CopiableSettings;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ElectricHeaterBlockEntity extends HbmEnergyBlockEntity
        implements HeatSource, LegacyLookOverlayProvider, CopiableSettings {
    private static final String TAG_SETTING = "setting";
    private static final String TAG_HEAT = "heatEnergy";
    private static final String TAG_ACTIVE = "isOn";
    private static final String TAG_POWER = "power";

    private int heatEnergy;
    private int setting;
    private boolean active;
    private Object audioLoop;

    public ElectricHeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_HEATER.get(), pos, state, new HbmEnergyStorage(0L, 0L, 0L));
        updateEnergyLimit();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricHeaterBlockEntity heater) {
        if (level.isClientSide) {
            return;
        }
        boolean oldActive = heater.active;
        int oldHeat = heater.heatEnergy;
        long oldPower = heater.energy.getPower();

        if (level.getGameTime() % 20L == 0L) {
            heater.subscribeEnergyReceiverToPorts();
        }

        heater.heatEnergy = Math.max(0, (int) (heater.heatEnergy * 0.999D));
        heater.pullHeatFromBelow(level, pos);

        heater.active = false;
        long consumption = heater.getConsumption();
        if (heater.setting > 0 && consumption > 0L && heater.energy.getPower() >= consumption) {
            heater.energy.setPower(heater.energy.getPower() - consumption);
            heater.heatEnergy += heater.getHeatGen();
            heater.active = true;
        }

        heater.networkPackNT(25);
        if (oldActive != heater.active || oldHeat != heater.heatEnergy || oldPower != heater.energy.getPower()
                || level.getGameTime() % 20L == 0L) {
            heater.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ElectricHeaterBlockEntity heater) {
        if (!level.isClientSide) {
            return;
        }
        heater.audioLoop = LegacyMachineAudioBridge.updateLoop(heater.audioLoop, heater,
                "ELECTRIC_HUM_LOOP", heater.active, 7.5D, 7.5F, 0.25F, 1.0F);
    }

    private void pullHeatFromBelow(Level level, BlockPos pos) {
        BlockEntity sourceBlockEntity = level.getBlockEntity(pos.below());
        if (sourceBlockEntity instanceof HeatSource source) {
            int sourceHeat = Math.max(0, source.getHeatStored());
            if (sourceHeat > 0) {
                heatEnergy += (int) (sourceHeat * 0.85D);
                source.useUpHeat(sourceHeat);
            }
        }
    }

    public int getHeatEnergy() {
        return heatEnergy;
    }

    public int getSetting() {
        return setting;
    }

    public boolean isActive() {
        return active;
    }

    public long getConsumption() {
        return (long) (Math.pow(setting, 1.4D) * 200.0D);
    }

    public int getHeatGen() {
        return setting * 100;
    }

    public void toggleSetting() {
        setting++;
        if (setting > 10) {
            setting = 0;
        }
        updateEnergyLimit();
        syncState();
    }

    @Override
    public int getHeatStored() {
        return heatEnergy;
    }

    @Override
    public void useUpHeat(int heat) {
        heatEnergy = Math.max(0, heatEnergy - Math.max(0, heat));
        setChanged();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        return List.of(EnergyPort.of(
                facing.getStepX() * 3,
                0,
                facing.getStepZ() * 3,
                facing));
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.heatTu(heatEnergy),
                Component.literal("-> " + getConsumption() + " HE/t"),
                Component.literal("<- " + getHeatGen() + " TU/t")));
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putLong(CompatEnergyControl.D_CONSUMPTION_HE, getConsumption());
        data.putLong(CompatEnergyControl.L_ENERGY_TU, getHeatStored());
        data.putLong(CompatEnergyControl.D_OUTPUT_TU, getHeatGen());
        data.putBoolean(CompatEnergyControl.B_ACTIVE, active);
    }

    @Override
    public CompoundTag getSettings(Level level, BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_SETTING, setting);
        return tag;
    }

    @Override
    public void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos) {
        if (tag == null || !tag.contains(TAG_SETTING)) {
            return;
        }
        setting = Mth.clamp(tag.getInt(TAG_SETTING), 0, 10);
        updateEnergyLimit();
        syncState();
    }

    @Override
    public List<Component> infoForDisplay(Level level, BlockPos pos) {
        return List.of(Component.literal("Setting: " + setting));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putInt(TAG_SETTING, setting);
        tag.putInt(TAG_HEAT, heatEnergy);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_POWER, Tag.TAG_LONG)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        setting = Mth.clamp(tag.getInt(TAG_SETTING), 0, 10);
        heatEnergy = Math.max(0, tag.getInt(TAG_HEAT));
        updateEnergyLimit();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt(TAG_SETTING, setting);
        tag.putInt(TAG_HEAT, heatEnergy);
        tag.putBoolean(TAG_ACTIVE, active);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        setting = Mth.clamp(tag.getInt(TAG_SETTING), 0, 10);
        heatEnergy = Math.max(0, tag.getInt(TAG_HEAT));
        active = tag.getBoolean(TAG_ACTIVE);
        updateEnergyLimit();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    private void updateEnergyLimit() {
        long maxPower = getConsumption() * 20L;
        energy.setMaxPower(maxPower);
        energy.setTransferRates(maxPower, 0L);
    }

    private void syncState() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            if (!level.isClientSide) {
                networkPackNT(25);
            }
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }
}
