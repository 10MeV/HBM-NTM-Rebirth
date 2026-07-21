package com.hbm.ntm.satellite;

import com.hbm.ntm.network.HbmCoordinateActionReceiver;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
        // 1.7.10 exposed only EntityPlayer#getHeldItem(): both satellite remotes were
        // consequently main-hand-only. Do not turn the 1.20 off hand into a second,
        // source-less remote-control channel.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.hbm.ntm.client.SatelliteScreenBridge.open(hand, mode));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof ServerPlayer player) || player.tickCount % 2 != 0
                || !isLegacyHeldMainHandStack(player, stack)) {
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
        // SatCoordPacket and SatLaserPacket both checked p.getHeldItem() in the
        // legacy handler. Keep the packet boundary aligned with the tick-sync and
        // right-click boundaries above; the packet intentionally does not constrain
        // PANEL versus COORD mode because the legacy packets did not either.
        if (!isLegacyHeldMainHandStack(player, stack)) {
            return false;
        }
        // The common 1.20 packet has an extensible action field, whereas the
        // legacy satellite boundary had exactly two distinct packet types:
        // SatCoordPacket and SatLaserPacket.  Do not let an unrelated action
        // fall through to the coordinate path merely because this item happens
        // to be held.
        if (action != ACTION_COORD && action != ACTION_LASER) {
            return false;
        }
        if (frequency != getFrequency(stack)) {
            return false;
        }
        Satellite satellite = SatelliteSavedData.get(player.serverLevel()).getSatellite(frequency);
        if (satellite == null) {
            return false;
        }
        return true;
    }

    @Override
    public void handleCoordinateAction(ServerPlayer player, ItemStack stack, BlockPos pos, int action, int value,
                                       int frequency, CompoundTag data) {
        Satellite satellite = SatelliteSavedData.get(player.serverLevel()).getSatellite(frequency);
        if (satellite == null) {
            return;
        }
        if (action == ACTION_LASER) {
            // SatLaserPacket dispatched the legacy virtual directly.  The
            // modern tryClick helper is intentionally only an opt-in result
            // API for built-in satellites; using it here would silently skip
            // a source-compatible public satellite that overrides onClick.
            satellite.onClick(player.serverLevel(), pos.getX(), pos.getZ());
            return;
        }
        // Same contract as SatCoordPacket: retain the public onCoordAction
        // dispatch point rather than requiring a non-legacy try... override.
        satellite.onCoordAction(player.serverLevel(), player, pos.getX(), pos.getY(), pos.getZ());
    }

    public Mode mode() {
        return mode;
    }

    /**
     * {@code ItemSatInterface#onUpdate} used {@code EntityPlayer#getHeldItem()}, which was
     * the sole main-hand stack in 1.7.10.  The panel snapshot is therefore intentionally not
     * refreshed for a modern off-hand copy or merely because an inventory callback marks a
     * stack selected.
     */
    private static boolean isLegacyHeldMainHandStack(Player player, ItemStack stack) {
        return player.getMainHandItem() == stack;
    }
}
