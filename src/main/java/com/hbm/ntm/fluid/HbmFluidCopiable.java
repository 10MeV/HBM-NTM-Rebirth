package com.hbm.ntm.fluid;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface HbmFluidCopiable {
    String TAG_FLUID_IDS = "fluidID";

    default int[] getFluidIdsToCopy() {
        if (!(this instanceof HbmFluidUser user)) {
            return new int[0];
        }

        List<Integer> ids = new ArrayList<>();
        for (HbmFluidTank tank : user.getAllTanks()) {
            FluidType type = tank.getTankType();
            if (type != null && !type.hasNoId()) {
                ids.add(type.getId());
            }
        }

        int[] result = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            result[i] = ids.get(i);
        }
        return result;
    }

    @Nullable
    default HbmFluidTank getTankToPasteFluidSettings() {
        if (this instanceof HbmStandardFluidReceiver receiver) {
            List<HbmFluidTank> tanks = receiver.getReceivingTanks();
            return tanks.isEmpty() ? null : tanks.get(0);
        }
        return null;
    }

    default CompoundTag getFluidSettings() {
        CompoundTag tag = new CompoundTag();
        int[] ids = getFluidIdsToCopy();
        if (ids.length > 0) {
            tag.putIntArray(TAG_FLUID_IDS, ids);
        }
        return tag;
    }

    default boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable Player player, boolean recursive) {
        HbmFluidTank tank = getTankToPasteFluidSettings();
        if (tank == null || tag == null || !tag.contains(TAG_FLUID_IDS)) {
            return false;
        }

        int[] ids = tag.getIntArray(TAG_FLUID_IDS);
        if (ids.length <= 0) {
            return false;
        }

        int safeIndex = index >= 0 && index < ids.length ? index : 0;
        tank.setTankType(HbmFluids.fromId(ids[safeIndex]));
        onFluidSettingsPasted();
        return true;
    }

    default List<Component> fluidSettingsDisplayInfo() {
        int[] ids = getFluidIdsToCopy();
        List<Component> lines = new ArrayList<>(ids.length);
        for (int id : ids) {
            lines.add(HbmFluids.fromId(id).getDisplayName());
        }
        return lines;
    }

    default void onFluidSettingsPasted() {
    }
}
