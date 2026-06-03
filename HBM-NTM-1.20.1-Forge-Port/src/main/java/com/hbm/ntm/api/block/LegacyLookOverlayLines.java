package com.hbm.ntm.api.block;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUser;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LegacyLookOverlayLines {
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    public static Component tank(boolean input, HbmFluidTank tank) {
        return arrow(input)
                .append(tank.getTankType().getDisplayName().copy().withStyle(ChatFormatting.RESET))
                .append(Component.literal(": " + NUMBER_FORMAT.format(tank.getFill()) + " / "
                        + NUMBER_FORMAT.format(tank.getMaxFill()) + "mB").withStyle(ChatFormatting.RESET));
    }

    public static Component fluidPort(boolean input, FluidType type) {
        return arrow(input).append(type.getDisplayName().copy().withStyle(ChatFormatting.RESET));
    }

    public static Component fluidName(FluidType type) {
        return type.getDisplayName().copy().withStyle(style -> style.withColor(type.getColor()));
    }

    public static Component rate(long amount, String unit) {
        return Component.literal(NUMBER_FORMAT.format(amount) + " " + unit);
    }

    public static Component shortRate(long amount, String unit) {
        return Component.literal(shortNumber(amount) + unit);
    }

    public static Component maxRate(long amount, String unit) {
        return Component.literal("Max.: " + shortNumber(amount) + unit);
    }

    public static Component counter(long amount) {
        return Component.literal("Counter: " + NUMBER_FORMAT.format(amount));
    }

    public static Component pumpLine(HbmFluidTank tank, long bufferSize) {
        return arrow(true)
                .append(tank.getTankType().getDisplayName().copy().withStyle(ChatFormatting.RESET))
                .append(Component.literal(" (" + tank.getPressure() + " PU): "
                        + shortNumber(bufferSize) + "mB/t").withStyle(ChatFormatting.RESET))
                .append(Component.literal(" ->").withStyle(ChatFormatting.RED));
    }

    public static Component priority(Enum<?> priority) {
        String name = priority == null ? "NORMAL" : priority.name();
        return Component.literal("Priority: ").append(Component.literal(name).withStyle(ChatFormatting.YELLOW));
    }

    public static Component freq(String channel) {
        return Component.literal("Freq: " + safe(channel)).withStyle(ChatFormatting.AQUA);
    }

    public static Component freq(int index, String channel) {
        return Component.literal("Freq " + index + ": " + safe(channel)).withStyle(ChatFormatting.AQUA);
    }

    public static Component signal(boolean state) {
        return Component.literal("Signal: " + state).withStyle(ChatFormatting.RED);
    }

    public static Component signal(int index, int state) {
        return Component.literal("Signal " + index + ": " + state).withStyle(ChatFormatting.RED);
    }

    public static Component buffered(long amount) {
        return Component.literal(shortNumber(amount) + "mB buffered");
    }

    public static Component itemStack(boolean input, ItemStack stack) {
        MutableComponent line = arrow(input).append(stack.getHoverName().copy().withStyle(ChatFormatting.RESET));
        if (stack.getCount() > 1) {
            line.append(Component.literal(" x" + stack.getCount()).withStyle(ChatFormatting.RESET));
        }
        return line;
    }

    public static Component warning(String message) {
        return Component.literal("! ! ! " + message + " ! ! !").withStyle(ChatFormatting.RED);
    }

    public static Component blinkingWarning(String message) {
        int color = System.currentTimeMillis() % 1000L < 500L ? 0xFF0000 : 0xFFFF00;
        return Component.literal("! ! ! " + message + " ! ! !").withStyle(style -> style.withColor(color));
    }

    public static Component error(String message) {
        return Component.literal(message).withStyle(ChatFormatting.RED);
    }

    public static Component colored(String message, int color) {
        return Component.literal(message).withStyle(style -> style.withColor(color));
    }

    public static Component recipeField(int index) {
        return Component.literal("-> ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("Recipe field [" + index + "]").withStyle(ChatFormatting.RESET));
    }

    public static Component heatTu(long heat) {
        return Component.literal(NUMBER_FORMAT.format(heat) + "TU");
    }

    public static Component energyOut(long power) {
        return Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal(shortNumber(power) + "HE").withStyle(ChatFormatting.WHITE));
    }

    public static Component energyOut(long power, Component suffix) {
        return Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal(shortNumber(power) + "HE ").withStyle(ChatFormatting.WHITE))
                .append(suffix.copy().withStyle(ChatFormatting.RESET));
    }

    public static Component energyStored(long power, long maxPower) {
        return Component.literal(shortNumber(power) + " / " + shortNumber(maxPower) + "HE");
    }

    public static Component chargePercent(long power, long maxPower) {
        return percent((double) power, (double) maxPower);
    }

    public static Component percent(double value, double maxValue) {
        double percent = maxValue <= 0.0D ? 0.0D : Math.max(0.0D, Math.min(1.0D, value / maxValue));
        int charge = (int) Math.floor(percent * 10_000.0D);
        int color = percentColor(percent);
        return Component.literal((charge / 100.0D) + "%").withStyle(style -> style.withColor(color));
    }

    public static int percentColor(double percent) {
        percent = Math.max(0.0D, Math.min(1.0D, percent));
        return ((int) (0xFF - 0xFF * percent)) << 16 | ((int) (0xFF * percent) << 8);
    }

    public static List<Component> energyStorage(long power, long maxPower) {
        return List.of(energyStored(power, maxPower), chargePercent(power, maxPower));
    }

    public static List<Component> fluidNames(FluidType... types) {
        List<Component> lines = new ArrayList<>();
        if (types != null) {
            for (FluidType type : types) {
                if (type != null) {
                    lines.add(type.getDisplayName().copy().withStyle(style -> style.withColor(type.getColor())));
                }
            }
        }
        return lines;
    }

    public static List<Component> fluidUserTanks(HbmFluidUser fluidUser) {
        return fluidUserTanks(fluidUser, false);
    }

    public static List<Component> allFluidUserTanks(HbmFluidUser fluidUser) {
        return fluidUserTanks(fluidUser, true);
    }

    public static List<Component> fluidUserTanks(HbmFluidUser fluidUser, boolean includeEmptyNoneTanks) {
        List<Component> lines = new ArrayList<>();
        List<HbmFluidTank> inputTanks = fluidUser instanceof HbmStandardFluidReceiver receiver
                ? receiver.getReceivingTanks()
                : List.of();
        List<HbmFluidTank> outputTanks = fluidUser instanceof HbmStandardFluidSender sender
                ? sender.getSendingTanks()
                : List.of();
        for (HbmFluidTank tank : fluidUser.getAllTanks()) {
            if (includeEmptyNoneTanks || tank.getTankType() != HbmFluids.NONE || tank.getFill() > 0) {
                boolean input = !outputTanks.contains(tank) || inputTanks.contains(tank);
                lines.add(tank(input, tank));
            }
        }
        return lines;
    }

    public static String shortNumber(long value) {
        long abs = Math.abs(value);
        if (abs >= 1_000_000_000L) {
            return String.format(Locale.US, "%.1fG", value / 1_000_000_000.0D);
        }
        if (abs >= 1_000_000L) {
            return String.format(Locale.US, "%.1fM", value / 1_000_000.0D);
        }
        if (abs >= 1_000L) {
            return String.format(Locale.US, "%.1fk", value / 1_000.0D);
        }
        return Long.toString(value);
    }

    private static MutableComponent arrow(boolean input) {
        return Component.literal(input ? "-> " : "<- ")
                .withStyle(input ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private LegacyLookOverlayLines() {
    }
}
