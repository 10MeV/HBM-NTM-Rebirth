package com.hbm.ntm.block;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import org.jetbrains.annotations.Nullable;

public class PWRComponentBlock extends Block {
    private final Kind kind;

    public PWRComponentBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        appendStandardPwrInfo(tooltip, switch (kind) {
            case HEATEX -> "pwr_heatex";
            case HEATSINK -> "pwr_heatsink";
            case NEUTRON_SOURCE -> "pwr_neutron_source";
            case REFLECTOR -> "pwr_reflector";
            case CASING -> "pwr_casing";
            case PORT -> "pwr_port";
        });
    }

    public static void appendStandardPwrInfo(List<Component> tooltip, String id) {
        LegacyStandardInfoTooltip.append(tooltip, id);
    }

    public enum Kind {
        HEATEX,
        HEATSINK,
        NEUTRON_SOURCE,
        REFLECTOR,
        CASING,
        PORT
    }

    public static class Pillar extends RotatedPillarBlock {
        private final PillarKind kind;

        public Pillar(Properties properties, PillarKind kind) {
            super(properties);
            this.kind = kind;
        }

        public PillarKind kind() {
            return kind;
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
                TooltipFlag flag) {
            super.appendHoverText(stack, level, tooltip, flag);
            appendStandardPwrInfo(tooltip, switch (kind) {
                case FUEL -> "pwr_fuel";
                case CONTROL -> "pwr_control";
                case CHANNEL -> "pwr_channel";
            });
        }
    }

    public enum PillarKind {
        FUEL,
        CONTROL,
        CHANNEL
    }
}
