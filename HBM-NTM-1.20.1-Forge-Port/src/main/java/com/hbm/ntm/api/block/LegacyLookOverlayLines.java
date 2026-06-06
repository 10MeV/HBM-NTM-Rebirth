package com.hbm.ntm.api.block;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidRepairMaterials.HbmRepairMaterial;
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

    public static Component compactTank(boolean input, HbmFluidTank tank) {
        return arrow(input)
                .append(tank.getTankType().getDisplayName().copy().withStyle(ChatFormatting.RESET))
                .append(Component.literal(": " + tank.getFill() + "/" + tank.getMaxFill() + "mB")
                        .withStyle(ChatFormatting.RESET));
    }

    public static Component groupedCompactTank(boolean input, HbmFluidTank tank) {
        return groupedCompactTank(input, tank.getTankType(), tank.getFill(), tank.getMaxFill());
    }

    public static Component groupedCompactTank(boolean input, FluidType type, long fill, long maxFill) {
        return arrow(input)
                .append(type.getDisplayName().copy().withStyle(ChatFormatting.RESET))
                .append(Component.literal(": " + NUMBER_FORMAT.format(fill) + "/"
                        + NUMBER_FORMAT.format(maxFill) + "mB").withStyle(ChatFormatting.RESET));
    }

    public static Component fluidPort(boolean input, FluidType type) {
        return arrow(input).append(type.getDisplayName().copy().withStyle(ChatFormatting.RESET));
    }

    public static Component powerPort(boolean input) {
        return arrow(input).append(Component.literal("Power").withStyle(ChatFormatting.RESET));
    }

    public static Component itemPort(boolean input, String name) {
        return arrow(input).append(Component.literal(name).withStyle(ChatFormatting.RESET));
    }

    public static List<Component> fluidPorts(boolean input, FluidType... types) {
        List<Component> lines = new ArrayList<>();
        if (types != null) {
            for (FluidType type : types) {
                if (type != null) {
                    lines.add(fluidPort(input, type));
                }
            }
        }
        return lines;
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
        return Component.literal("Counter: " + amount);
    }

    public static Component pumpLine(HbmFluidTank tank, long bufferSize) {
        return arrow(true)
                .append(tank.getTankType().getDisplayName().copy().withStyle(ChatFormatting.RESET))
                .append(Component.literal(" (" + tank.getPressure() + " PU): "
                        + NUMBER_FORMAT.format(bufferSize) + "mB/t").withStyle(ChatFormatting.RESET))
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
        return Component.literal(NUMBER_FORMAT.format(amount) + "mB buffered");
    }

    public static Component itemStack(boolean input, ItemStack stack) {
        MutableComponent line = arrow(input).append(stack.getHoverName().copy().withStyle(ChatFormatting.RESET));
        if (stack.getCount() > 1) {
            line.append(Component.literal(" x" + stack.getCount()).withStyle(ChatFormatting.RESET));
        }
        return line;
    }

    public static Component repairHeader() {
        return Component.literal("Repair with:").withStyle(ChatFormatting.GOLD);
    }

    public static Component repairMaterial(ItemStack stack) {
        MutableComponent line = Component.literal("- ");
        line.append(stack.getHoverName().copy().withStyle(ChatFormatting.RESET));
        if (stack.getCount() > 1) {
            line.append(Component.literal(" x" + stack.getCount()).withStyle(ChatFormatting.RESET));
        }
        return line;
    }

    public static Component repairMaterial(HbmRepairMaterial material) {
        if (material == null) {
            return Component.literal("- ERROR").withStyle(ChatFormatting.RED);
        }
        List<ItemStack> stacks = material.displayStacks();
        if (!stacks.isEmpty()) {
            return repairMaterial(stacks.get(0));
        }
        MutableComponent line = Component.literal("- ");
        line.append(material.fallbackName().copy().withStyle(ChatFormatting.RED));
        if (material.count() > 1) {
            line.append(Component.literal(" x" + material.count()).withStyle(ChatFormatting.RESET));
        }
        return line;
    }

    public static List<Component> repairMaterials(List<HbmRepairMaterial> materials) {
        List<Component> lines = new ArrayList<>();
        lines.add(repairHeader());
        if (materials != null) {
            for (HbmRepairMaterial material : materials) {
                lines.add(repairMaterial(material));
            }
        }
        return lines;
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

    public static Component legacyEncoded(String line) {
        String text = line == null ? "" : line;
        int color = 0xFFFFFF;
        if (text.startsWith("&[")) {
            int end = text.lastIndexOf("&]");
            color = Integer.parseInt(text.substring(2, end));
            text = text.substring(end + 2);
        }
        int lineColor = color;
        return Component.literal(text).withStyle(style -> style.withColor(lineColor));
    }

    public static List<Component> legacyEncodedLines(List<String> lines) {
        List<Component> components = new ArrayList<>();
        if (lines != null) {
            for (String line : lines) {
                components.add(legacyEncoded(line));
            }
        }
        return components;
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

    public static Component industrialTurbineEnergyOut(long power, String spinner, int spinPercent, double spin) {
        int color = ((int) (0xFF - 0xFF * spin)) << 16 | ((int) (0xFF * spin) << 8);
        return Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal(shortNumber(power) + "HE (").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(spinner + spinPercent + "%").withStyle(style -> style.withColor(color)))
                .append(Component.literal(")").withStyle(ChatFormatting.WHITE));
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

    public static List<Component> allCompactFluidUserTanks(HbmFluidUser fluidUser) {
        return fluidUserTanks(fluidUser, true, true);
    }

    public static List<Component> fluidUserTanks(HbmFluidUser fluidUser, boolean includeEmptyNoneTanks) {
        return fluidUserTanks(fluidUser, includeEmptyNoneTanks, false);
    }

    public static List<Component> fluidUserTanks(HbmFluidUser fluidUser, boolean includeEmptyNoneTanks,
            boolean compact) {
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
                lines.add(compact ? compactTank(input, tank) : tank(input, tank));
            }
        }
        return lines;
    }

    public static String shortNumber(long value) {
        double result;
        String suffix;
        double abs = Math.abs((double) value);
        if (abs >= Math.pow(10.0D, 18.0D)) {
            result = value / Math.pow(10.0D, 18.0D);
            suffix = "E";
        } else if (abs >= Math.pow(10.0D, 15.0D)) {
            result = value / Math.pow(10.0D, 15.0D);
            suffix = "P";
        } else if (abs >= Math.pow(10.0D, 12.0D)) {
            result = value / Math.pow(10.0D, 12.0D);
            suffix = "T";
        } else if (abs >= Math.pow(10.0D, 9.0D)) {
            result = value / Math.pow(10.0D, 9.0D);
            suffix = "G";
        } else if (abs >= Math.pow(10.0D, 6.0D)) {
            result = value / Math.pow(10.0D, 6.0D);
            suffix = "M";
        } else if (abs >= Math.pow(10.0D, 3.0D)) {
            result = value / Math.pow(10.0D, 3.0D);
            suffix = "k";
        } else {
            return Long.toString(value);
        }
        if (result <= -100.0D) {
            result = Math.round(result * 10.0D) / 10.0D;
        } else {
            result = Math.round(result * 100.0D) / 100.0D;
        }
        return result + suffix;
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
