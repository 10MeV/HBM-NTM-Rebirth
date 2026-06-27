package com.hbm.ntm.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
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

    public default boolean supportsFluidSettingsCopy() {
        return getFluidIdsToCopy().length > 0 || getTankToPasteFluidSettings() != null;
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
        if (tank == null) {
            return false;
        }

        OptionalInt id = copiedFluidIdAt(tag, index);
        if (id.isEmpty()) {
            return false;
        }

        tank.setTankType(HbmFluids.fromId(id.getAsInt()));
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

    static OptionalInt copiedFluidIdAt(@Nullable CompoundTag tag, int index) {
        if (tag == null || !tag.contains(TAG_FLUID_IDS)) {
            return OptionalInt.empty();
        }
        int[] ids = tag.getIntArray(TAG_FLUID_IDS);
        if (index < 0 || index >= ids.length) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(ids[index]);
    }

    static OptionalInt copiedPipeFluidIdAt(@Nullable CompoundTag tag, int index) {
        if (tag == null || !tag.contains(TAG_FLUID_IDS)) {
            return OptionalInt.empty();
        }
        int[] ids = tag.getIntArray(TAG_FLUID_IDS);
        if (ids.length == 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(index >= 0 && index < ids.length ? ids[index] : HbmFluids.NONE.getId());
    }
}
