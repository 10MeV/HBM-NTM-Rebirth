package com.hbm.ntm.fluid;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.DataSlot;

import java.util.ArrayList;
import java.util.List;

public final class HbmFluidGuiHelper {
    private HbmFluidGuiHelper() {
    }

    public static TankData watchTank(DataSlotSink sink, HbmFluidTank tank) {
        TankData data = new TankData(tank);
        data.addTo(sink);
        return data;
    }

    public static int scaledFill(int fill, int capacity, int maxSize) {
        return capacity <= 0 || fill <= 0 || maxSize <= 0 ? 0 : fill * maxSize / capacity;
    }

    public static Component tankInfo(FluidType type, int fill, int capacity, int pressure) {
        FluidType displayType = type == null ? HbmFluids.NONE : type;
        MutableComponent info = displayType.getDisplayName().copy()
                .append(Component.literal(": " + fill + " / " + capacity + " mB"));
        if (pressure > 0) {
            info.append(Component.literal(" | Pressure: " + pressure + " PU").withStyle(ChatFormatting.RED));
        }
        return info;
    }

    public static Component tankInfo(HbmFluidTank tank, int fill, int capacity) {
        return tankInfo(tank.getTankType(), fill, capacity, tank.getPressure());
    }

    public static final class TankData {
        private final HbmFluidTank tank;
        private int fill;
        private int capacity;
        private int typeId;
        private int pressure;

        private TankData(HbmFluidTank tank) {
            this.tank = tank;
        }

        public int fill() {
            return fill;
        }

        public int capacity() {
            return capacity;
        }

        public FluidType type() {
            return HbmFluids.fromId(typeId);
        }

        public int pressure() {
            return pressure;
        }

        public int guiTint() {
            return type().getGuiTint();
        }

        public boolean isEmpty() {
            return type() == HbmFluids.NONE || fill <= 0;
        }

        public int scaledFill(int maxSize) {
            return HbmFluidGuiHelper.scaledFill(fill, capacity, maxSize);
        }

        public Component info() {
            return tankInfo(type(), fill, capacity, pressure);
        }

        public List<Component> tooltip() {
            List<Component> tooltip = new ArrayList<>();
            FluidType type = type();
            tooltip.add(type.getDisplayName());
            tooltip.add(Component.literal(fill + " / " + capacity + " mB"));
            if (pressure > 0) {
                tooltip.add(Component.literal("Pressure: " + pressure + " PU").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("Pressurized, use compressor!").withStyle(ChatFormatting.DARK_RED));
            }
            type.appendInfo(tooltip, false);
            return tooltip;
        }

        private void addTo(DataSlotSink sink) {
            sink.add(new DataSlot() {
                @Override
                public int get() {
                    return tank.getFill();
                }

                @Override
                public void set(int value) {
                    fill = value;
                }
            });
            sink.add(new DataSlot() {
                @Override
                public int get() {
                    return tank.getMaxFill();
                }

                @Override
                public void set(int value) {
                    capacity = value;
                }
            });
            sink.add(new DataSlot() {
                @Override
                public int get() {
                    return tank.getTankType().getId();
                }

                @Override
                public void set(int value) {
                    typeId = value;
                }
            });
            sink.add(new DataSlot() {
                @Override
                public int get() {
                    return tank.getPressure();
                }

                @Override
                public void set(int value) {
                    pressure = HbmFluidTank.clampPressure(value);
                }
            });
        }
    }

    @FunctionalInterface
    public interface DataSlotSink {
        void add(DataSlot slot);
    }
}
