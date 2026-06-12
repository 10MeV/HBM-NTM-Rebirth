package com.hbm.ntm.satellite;

import com.hbm.ntm.network.HbmCoordinateActionReceiver;
import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class SatelliteInterfaceItem extends SatelliteChipItem implements HbmCoordinateActionReceiver {
    private static final int ACTION_COORD = 0;
    private static final int ACTION_LASER = 1;

    public enum Mode {
        PANEL,
        COORD
    }

    private final Mode mode;

    public SatelliteInterfaceItem(Properties properties, Mode mode) {
        super(properties, null);
        this.mode = mode;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.hbm.ntm.client.SatelliteScreenBridge.open(hand, mode));
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Satellite satellite = SatelliteSavedData.get(serverPlayer.serverLevel()).getSatellite(getFrequency(stack));
            if (satellite == null) {
                serverPlayer.displayClientMessage(Component.translatable("satchip.no_satellite"), true);
            } else {
                serverPlayer.displayClientMessage(Component.translatable(
                        mode == Mode.COORD ? "satchip.coord.ready" : "satchip.interface.ready",
                        satellite.legacyName(),
                        getFrequency(stack)), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof ServerPlayer player) || player.tickCount % 2 != 0
                || !isHeldStack(player, stack, selected)) {
            return;
        }
        Satellite satellite = SatelliteSavedData.get(player.serverLevel()).getSatellite(getFrequency(stack));
        if (satellite == null) {
            return;
        }
        CompoundTag data = new CompoundTag();
        satellite.writeToNBT(data);
        data.putString("legacyName", satellite.legacyName());
        data.putInt("frequency", getFrequency(stack));
        ModMessages.syncSatellitePanelData(player, satellite.legacyId(), data);
    }

    @Override
    public boolean canReceiveCoordinateAction(ServerPlayer player, ItemStack stack, BlockPos pos, int action, int value,
                                              int frequency, CompoundTag data) {
        if (frequency != getFrequency(stack)) {
            return false;
        }
        Satellite satellite = SatelliteSavedData.get(player.serverLevel()).getSatellite(frequency);
        if (satellite == null) {
            return false;
        }
        if (isLaserAction(action, data)) {
            return mode == Mode.PANEL && satellite.satelliteInterface() == Satellite.SatelliteInterface.SAT_PANEL;
        }
        return mode == Mode.COORD && satellite.satelliteInterface() == Satellite.SatelliteInterface.SAT_COORD;
    }

    @Override
    public void handleCoordinateAction(ServerPlayer player, ItemStack stack, BlockPos pos, int action, int value,
                                       int frequency, CompoundTag data) {
        Satellite satellite = SatelliteSavedData.get(player.serverLevel()).getSatellite(frequency);
        if (satellite == null) {
            return;
        }
        if (isLaserAction(action, data)) {
            satellite.tryClick(player.serverLevel(), pos.getX(), pos.getZ());
            return;
        }
        satellite.tryCoordAction(player.serverLevel(), player, pos.getX(), pos.getY(), pos.getZ());
    }

    public Mode mode() {
        return mode;
    }

    private static boolean isLaserAction(int action, CompoundTag data) {
        return action == ACTION_LASER || data.getBoolean("laser")
                || HbmNetworkActions.SATELLITE_LASER.toString().equals(data.getString("actionType"));
    }

    private static boolean isHeldStack(Player player, ItemStack stack, boolean selected) {
        return selected || player.getMainHandItem() == stack || player.getOffhandItem() == stack;
    }
}
