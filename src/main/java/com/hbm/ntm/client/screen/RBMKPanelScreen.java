package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RBMKPanelBlockEntity;
import com.hbm.ntm.menu.RBMKPanelMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.hbm.ntm.neutron.RBMKPanelScreenPlanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class RBMKPanelScreen extends AbstractContainerScreen<RBMKPanelMenu> {
    private final RBMKPanelPlanner.PanelType panelType;
    private final RBMKPanelScreenPlanner.PanelScreenContract contract;
    private final Map<String, EditBox> fields = new HashMap<>();
    private final Map<String, Integer> masks = new HashMap<>();

    public RBMKPanelScreen(RBMKPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.panelType = menu.getBlockEntity().panelType();
        this.contract = RBMKPanelScreenPlanner.screenContract(panelType);
        this.imageWidth = contract.width();
        this.imageHeight = contract.height();
        this.titleLabelY = 6;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        fields.clear();
        masks.clear();
        initMasks();
        for (RBMKPanelScreenPlanner.TextFieldPlan plan : RBMKPanelScreenPlanner.textFields(panelType)) {
            EditBox box = LegacyGuiElements.createLegacyTextField(font, leftPos + plan.x(), topPos + plan.y(),
                    plan.width(), plan.height(), plan.maxLength(), valueFor(plan));
            fields.put(plan.packetKey(), addRenderableWidget(box));
            if (panelType == RBMKPanelPlanner.PanelType.TERMINAL) {
                box.setFocused(true);
                setFocused(box);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (panelType == RBMKPanelPlanner.PanelType.TERMINAL) {
            return;
        }
        if (panelType == RBMKPanelPlanner.PanelType.DISPLAY) {
            return;
        }
        ResourceLocation texture = texture();
        for (RBMKPanelScreenPlanner.TextureRect rect : RBMKPanelScreenPlanner.staticTextureRects(panelType)) {
            graphics.blit(texture, leftPos + rect.x(), topPos + rect.y(), rect.u(), rect.v(),
                    rect.width(), rect.height(), 256, 256);
        }
        for (RBMKPanelScreenPlanner.TogglePlan plan : RBMKPanelScreenPlanner.toggles(panelType)) {
            if ((masks.getOrDefault(plan.maskKey(), 0) & (1 << plan.unit())) != 0) {
                graphics.blit(texture, leftPos + plan.x(), topPos + plan.y(), plan.textureU(), plan.textureV(),
                        plan.textureWidth(), plan.textureHeight(), 256, 256);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (panelType == RBMKPanelPlanner.PanelType.TERMINAL) {
            return;
        }
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, titleLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (panelType != RBMKPanelPlanner.PanelType.TERMINAL) {
            renderBackground(graphics);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        if (panelType == RBMKPanelPlanner.PanelType.TERMINAL) {
            renderTerminalHelp(graphics);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (RBMKPanelScreenPlanner.TogglePlan plan : RBMKPanelScreenPlanner.toggles(panelType)) {
            if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos,
                    plan.x(), plan.y(), plan.width(), plan.height())) {
                toggle(plan);
                boolean enabled = (masks.getOrDefault(plan.maskKey(), 0) & (1 << plan.unit())) != 0;
                LegacyGuiElements.playClickSound(enabled ? 0.75F : 0.5F);
                return true;
            }
        }
        RBMKPanelScreenPlanner.SaveButtonPlan save = RBMKPanelScreenPlanner.saveButton(panelType);
        if (save.present() && LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos,
                save.x(), save.y(), save.width(), save.height())) {
            save();
            LegacyGuiElements.playClickSound();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (panelType == RBMKPanelPlanner.PanelType.TERMINAL
                && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            sendTerminalCommand();
            return true;
        }
        if (panelType == RBMKPanelPlanner.PanelType.TERMINAL
                && (keyCode == GLFW.GLFW_KEY_HOME || keyCode == GLFW.GLFW_KEY_LEFT
                || keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_END)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderTerminalHelp(GuiGraphics graphics) {
        RBMKPanelScreenPlanner.TerminalScreenPlan plan = RBMKPanelScreenPlanner.terminalScreenPlan(false);
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        for (RBMKPanelScreenPlanner.HelpLine line : plan.helpLines()) {
            graphics.drawString(font, line.text(), line.x(), line.y(), 0xFFFFFF, false);
        }
        graphics.pose().popPose();
    }

    private void sendTerminalCommand() {
        EditBox box = fields.get("cmd");
        if (box == null) {
            return;
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("cmd", box.getValue());
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
        box.setValue("");
    }

    private void initMasks() {
        RBMKPanelBlockEntity panel = menu.getBlockEntity();
        masks.put("active", activeMask(panel));
        masks.put("polling", pollingMask(panel));
        if (panelType == RBMKPanelPlanner.PanelType.NUMITRON) {
            int shorten = 0;
            int leading = 0;
            RBMKPanelPlanner.NumitronUnit[] units = panel.numitrons();
            for (int i = 0; i < units.length; i++) {
                if (units[i].shortenNumber()) {
                    shorten |= 1 << i;
                }
                if (units[i].leadingZeroes()) {
                    leading |= 1 << i;
                }
            }
            masks.put("shorten_number", shorten);
            masks.put("leading_zeroes", leading);
        }
    }

    private int activeMask(RBMKPanelBlockEntity panel) {
        return switch (panelType) {
            case GAUGE -> mask(panel.gauges().length, i -> panel.gauges()[i].active());
            case GRAPH -> mask(panel.graphs().length, i -> panel.graphs()[i].active());
            case INDICATOR -> mask(panel.indicators().length, i -> panel.indicators()[i].active());
            case KEYPAD -> mask(panel.keys().length, i -> panel.keys()[i].active());
            case LEVER -> mask(panel.levers().length, i -> panel.levers()[i].active());
            case NUMITRON -> mask(panel.numitrons().length, i -> panel.numitrons()[i].active());
            case TERMINAL, DISPLAY -> 0;
        };
    }

    private int pollingMask(RBMKPanelBlockEntity panel) {
        return switch (panelType) {
            case GAUGE -> mask(panel.gauges().length, i -> panel.gauges()[i].polling());
            case GRAPH -> mask(panel.graphs().length, i -> panel.graphs()[i].polling());
            case INDICATOR -> mask(panel.indicators().length, i -> panel.indicators()[i].polling());
            case KEYPAD -> mask(panel.keys().length, i -> panel.keys()[i].polling());
            case LEVER -> mask(panel.levers().length, i -> panel.levers()[i].polling());
            case NUMITRON -> mask(panel.numitrons().length, i -> panel.numitrons()[i].polling());
            case TERMINAL, DISPLAY -> 0;
        };
    }

    private String valueFor(RBMKPanelScreenPlanner.TextFieldPlan plan) {
        int i = plan.unit();
        RBMKPanelBlockEntity panel = menu.getBlockEntity();
        return switch (panelType) {
            case GAUGE -> gaugeValue(panel.gauges()[i], plan.name());
            case GRAPH -> graphValue(panel.graphs()[i], plan.name());
            case INDICATOR -> indicatorValue(panel.indicators()[i], plan.name());
            case KEYPAD -> keyValue(panel.keys()[i], plan.name());
            case LEVER -> leverValue(panel.levers()[i], plan.name());
            case NUMITRON -> numitronValue(panel.numitrons()[i], plan.name());
            case TERMINAL -> plan.name().equals("line") ? "" : "";
            case DISPLAY -> "";
        };
    }

    private void toggle(RBMKPanelScreenPlanner.TogglePlan plan) {
        int mask = masks.getOrDefault(plan.maskKey(), 0);
        mask ^= 1 << plan.unit();
        masks.put(plan.maskKey(), mask);
    }

    private void save() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("active", (byte) masks.getOrDefault("active", 0).intValue());
        tag.putByte("polling", (byte) masks.getOrDefault("polling", 0).intValue());
        if (panelType == RBMKPanelPlanner.PanelType.NUMITRON) {
            tag.putByte("shorten_number", (byte) masks.getOrDefault("shorten_number", 0).intValue());
            tag.putByte("leading_zeroes", (byte) masks.getOrDefault("leading_zeroes", 0).intValue());
        }
        for (RBMKPanelScreenPlanner.TextFieldPlan plan : RBMKPanelScreenPlanner.textFields(panelType)) {
            putField(tag, plan);
        }
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }

    private void putField(CompoundTag tag, RBMKPanelScreenPlanner.TextFieldPlan plan) {
        String value = fields.containsKey(plan.packetKey()) ? fields.get(plan.packetKey()).getValue() : "";
        if (plan.name().equals("color")) {
            Integer parsed = parseColor(value);
            if (parsed != null) {
                tag.putInt(plan.packetKey(), parsed);
            }
        } else if (plan.name().equals("min") || plan.name().equals("max")) {
            if (!value.isBlank()) {
                if (panelType == RBMKPanelPlanner.PanelType.GAUGE
                        || panelType == RBMKPanelPlanner.PanelType.INDICATOR) {
                    Integer parsed = parseInteger(value);
                    if (parsed != null) {
                        tag.putInt(plan.packetKey(), parsed);
                    }
                } else {
                    Long parsed = parseLong(value);
                    if (parsed != null) {
                        tag.putLong(plan.packetKey(), parsed);
                    }
                }
            }
        } else {
            tag.putString(plan.packetKey(), value);
        }
    }

    private ResourceLocation texture() {
        String name = switch (panelType) {
            case GAUGE -> "gui_rbmk_gauge.png";
            case GRAPH -> "gui_rbmk_graph.png";
            case INDICATOR -> "gui_rbmk_indicator.png";
            case KEYPAD -> "gui_rbmk_keypad.png";
            case LEVER -> "gui_rbmk_lever.png";
            case NUMITRON -> "gui_rbmk_numitron.png";
            case TERMINAL, DISPLAY -> "gui_rbmk_gauge.png";
        };
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/" + name);
    }

    private static String gaugeValue(RBMKPanelPlanner.GaugeUnit unit, String name) {
        return switch (name) {
            case "color" -> hexColor(unit.color());
            case "label" -> unit.label();
            case "rtty" -> unit.rtty();
            case "min" -> Long.toString(unit.min());
            case "max" -> Long.toString(unit.max());
            default -> "";
        };
    }

    private static String graphValue(RBMKPanelPlanner.GraphUnit unit, String name) {
        return switch (name) {
            case "label" -> unit.label();
            case "rtty" -> unit.rtty();
            case "min" -> unit.minBound() ? Long.toString(unit.min()) : "";
            case "max" -> unit.maxBound() ? Long.toString(unit.max()) : "";
            default -> "";
        };
    }

    private static String indicatorValue(RBMKPanelPlanner.IndicatorUnit unit, String name) {
        return switch (name) {
            case "color" -> hexColor(unit.color());
            case "label" -> unit.label();
            case "rtty" -> unit.rtty();
            case "min" -> Long.toString(unit.min());
            case "max" -> Long.toString(unit.max());
            default -> "";
        };
    }

    private static String keyValue(RBMKPanelPlanner.KeyUnit unit, String name) {
        return switch (name) {
            case "color" -> hexColor(unit.color());
            case "label" -> unit.label();
            case "rtty" -> unit.rtty();
            case "command" -> unit.command();
            default -> "";
        };
    }

    private static String leverValue(RBMKPanelPlanner.LeverUnit unit, String name) {
        return switch (name) {
            case "label" -> unit.label();
            case "rtty" -> unit.rtty();
            case "commandOn" -> unit.commandOn();
            case "commandOff" -> unit.commandOff();
            default -> "";
        };
    }

    private static String numitronValue(RBMKPanelPlanner.NumitronUnit unit, String name) {
        return switch (name) {
            case "label" -> unit.label();
            case "rtty" -> unit.rtty();
            default -> "";
        };
    }

    private static String hexColor(int value) {
        return String.format("%06x", value & 0xFFFFFF);
    }

    private static Integer parseColor(String value) {
        try {
            return Integer.parseInt(value.trim().replace("#", ""), 16);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Legacy RenderRBMKTerminal shows the input line only while this exact
     * terminal's screen is open. The world renderer uses this snapshot.
     */
    public static TerminalRenderInput terminalRenderInput(RBMKPanelBlockEntity terminal) {
        if (Minecraft.getInstance().screen instanceof RBMKPanelScreen screen
                && screen.panelType == RBMKPanelPlanner.PanelType.TERMINAL
                && screen.menu.getBlockEntity() == terminal) {
            EditBox line = screen.fields.get("cmd");
            return new TerminalRenderInput(line == null ? "" : line.getValue(),
                    (System.currentTimeMillis() / 500L) % 2L == 0L);
        }
        return TerminalRenderInput.EMPTY;
    }

    public record TerminalRenderInput(String workingLine, boolean cursor) {
        private static final TerminalRenderInput EMPTY = new TerminalRenderInput("", false);
    }

    private static int mask(int count, BoolAt predicate) {
        int mask = 0;
        for (int i = 0; i < count; i++) {
            if (predicate.get(i)) {
                mask |= 1 << i;
            }
        }
        return mask;
    }

    private interface BoolAt {
        boolean get(int index);
    }
}
