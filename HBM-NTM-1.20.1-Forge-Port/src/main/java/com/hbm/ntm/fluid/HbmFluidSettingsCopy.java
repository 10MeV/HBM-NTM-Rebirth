package com.hbm.ntm.fluid;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class HbmFluidSettingsCopy {
    public static Optional<CompoundTag> copy(Level level, BlockPos pos) {
        return copiableAt(level, pos).map(HbmFluidCopiable::getFluidSettings);
    }

    public static boolean paste(Level level, BlockPos pos, CompoundTag tag, int index,
            @Nullable Player player, boolean recursive) {
        return copiableAt(level, pos)
                .map(copiable -> copiable.pasteFluidSettings(tag, index, player, recursive))
                .orElse(false);
    }

    public static List<Component> displayInfo(Level level, BlockPos pos) {
        return copiableAt(level, pos)
                .map(HbmFluidCopiable::fluidSettingsDisplayInfo)
                .orElse(List.of());
    }

    public static Optional<HbmFluidCopiable> copiableAt(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return Optional.empty();
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof HbmFluidCopiable copiable ? Optional.of(copiable) : Optional.empty();
    }

    private HbmFluidSettingsCopy() {
    }
}
