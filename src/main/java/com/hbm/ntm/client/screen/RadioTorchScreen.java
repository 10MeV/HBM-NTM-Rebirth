package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RTTYCounterState;
import com.hbm.ntm.api.redstoneoverradio.RTTYReaderState;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.blockentity.RadioTorchBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchControllerBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchCounterBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchDeviceBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchLogicBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchReaderBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchSenderBlockEntity;
import com.hbm.ntm.menu.RadioTorchMenu;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class RadioTorchScreen extends AbstractContainerScreen<RadioTorchMenu> {
    private static final ResourceLocation SENDER_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_rtty_sender.png");
    private static final ResourceLocation RECEIVER_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_rtty_receiver.png");
    private static final ResourceLocation LOGIC_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_rtty_logic_receiver.png");
    private static final ResourceLocation READER_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_rtty_reader.png");
    private static final ResourceLocation CONTROLLER_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_rtty_controller.png");
    private static final ResourceLocation COUNTER_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_rtty_counter.png");

    private EditBox channelField;
    private final List<EditBox> mappingFields = new ArrayList<>();
    private final List<EditBox> readerChannelFields = new ArrayList<>();
    private final List<EditBox> readerNameFields = new ArrayList<>();
    private final List<EditBox> counterChannelFields = new ArrayList<>();
    private final int[] conditions = new int[16];
    private boolean polling;
    private boolean customMap;
    private boolean descending;

    public RadioTorchScreen(RadioTorchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        if (menu.getBlockEntity() instanceof RadioTorchCounterBlockEntity) {
            imageWidth = 218;
            imageHeight = 238;
        } else if (menu.getBlockEntity() instanceof RadioTorchControllerBlockEntity) {
            // GUIScreenRadioTorchController deliberately used the compact
            // 256x42 strip, not the 256x204 device/reader texture sheet.
            imageWidth = 256;
            imageHeight = 42;
        } else {
            imageWidth = 256;
            imageHeight = 204;
        }
        titleLabelY = 6;
        inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        mappingFields.clear();
        readerChannelFields.clear();
        readerNameFields.clear();
        counterChannelFields.clear();

        RadioTorchBlockEntity torch = menu.getBlockEntity();
        if (torch instanceof RadioTorchDeviceBlockEntity device) {
            initDevice(device);
        } else if (torch instanceof RadioTorchLogicBlockEntity logic) {
            initLogic(logic);
        } else if (torch instanceof RadioTorchReaderBlockEntity reader) {
            initReader(reader);
        } else if (torch instanceof RadioTorchControllerBlockEntity controller) {
            initController(controller);
        } else if (torch instanceof RadioTorchCounterBlockEntity counter) {
            initCounter(counter);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RadioTorchBlockEntity torch = menu.getBlockEntity();
        ResourceLocation texture = texture();
        if (torch instanceof RadioTorchDeviceBlockEntity) {
            if (customMap) {
                graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
                graphics.blit(texture, leftPos + 137, topPos + 17, 0, 204, 18, 18);
                if (polling) {
                    graphics.blit(texture, leftPos + 173, topPos + 17, 0, 222, 18, 18);
                }
            } else {
                graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, 35);
                graphics.blit(texture, leftPos, topPos + 35, 0, 197, imageWidth, 7);
                if (polling) {
                    graphics.blit(texture, leftPos + 173, topPos + 17, 0, 222, 18, 18);
                }
            }
        } else {
            graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        }

        if (torch instanceof RadioTorchLogicBlockEntity logic) {
            if (descending) {
                graphics.blit(texture, leftPos + 137, topPos + 17, 0, 204, 18, 18);
            }
            if (polling) {
                graphics.blit(texture, leftPos + 173, topPos + 17, 0, 222, 18, 18);
            }
            for (int i = 0; i < conditions.length; i++) {
                int column = i / 8;
                int row = i % 8;
                int condition = conditions[i];
                if (logic.logicState().mapping(i).isEmpty()) {
                    if (condition != 0) {
                        graphics.blit(texture, leftPos + 7 + column * 130, topPos + 53 + row * 18,
                                18 + condition * 18, 222, 18, 18);
                    }
                } else {
                    graphics.blit(texture, leftPos + 7 + column * 130, topPos + 53 + row * 18,
                            18 + condition * 18, 204, 18, 18);
                    graphics.blit(texture, leftPos + 85 + column * 130, topPos + 57 + row * 18,
                            198, 204, 14, 10);
                }
            }
        } else if (torch instanceof RadioTorchReaderBlockEntity && polling) {
            graphics.blit(texture, leftPos + 173, topPos + 17, 0, 204, 18, 18);
        } else if (torch instanceof RadioTorchControllerBlockEntity && polling) {
            // GUIScreenRadioTorchController drew its checked polling glyph
            // from the second 18px strip of the same legacy texture.
            graphics.blit(CONTROLLER_TEXTURE, leftPos + 173, topPos + 17, 0, 42, 18, 18);
        } else if (torch instanceof RadioTorchCounterBlockEntity && polling) {
            graphics.blit(COUNTER_TEXTURE, leftPos + 193, topPos + 8, 218, 0, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        int center = menu.getBlockEntity() instanceof RadioTorchCounterBlockEntity ? 92 : imageWidth / 2;
        graphics.drawString(font, name, center - font.width(name) / 2, titleLabelY, 0x404040, false);
        if (menu.getBlockEntity() instanceof RadioTorchCounterBlockEntity) {
            graphics.drawString(font, playerInventoryTitle, 16, imageHeight - 94, 0x404040, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderControlTooltips(graphics, mouseX, mouseY);
        renderCounterFilterTooltip(graphics, mouseX, mouseY);
        renderLegacyRorInfoTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        RadioTorchBlockEntity torch = menu.getBlockEntity();
        if (torch instanceof RadioTorchDeviceBlockEntity) {
            if (hovering(137, 17, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                toggleCustom();
                return true;
            }
            if (hovering(173, 17, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                togglePolling();
                return true;
            }
            if (hovering(209, 17, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                save();
                return true;
            }
        } else if (torch instanceof RadioTorchLogicBlockEntity) {
            if (hovering(137, 17, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                toggleDescending();
                return true;
            }
            if (hovering(173, 17, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                togglePolling();
                return true;
            }
            if (hovering(209, 17, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                save();
                return true;
            }
            int condition = conditionAt(mouseX, mouseY);
            if (condition >= 0) {
                conditions[condition] = (conditions[condition] + 1) % 10;
                LegacyGuiElements.playClickSound();
                return true;
            }
        } else if (torch instanceof RadioTorchReaderBlockEntity
                || torch instanceof RadioTorchControllerBlockEntity) {
            if (hovering(173, 17, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                togglePolling();
                return true;
            }
            if (hovering(209, 17, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                save();
                return true;
            }
        } else if (torch instanceof RadioTorchCounterBlockEntity) {
            if (hovering(193, 8, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                togglePolling();
                return true;
            }
            if (hovering(193, 30, 18, 18, mouseX, mouseY)) {
                LegacyGuiElements.playClickSound();
                save();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (menu.getBlockEntity() instanceof RadioTorchLogicBlockEntity) {
            int condition = conditionAt(mouseX, mouseY);
            if (condition >= 0 && delta != 0.0D) {
                conditions[condition] = (conditions[condition] + (delta > 0.0D ? 1 : 9)) % 10;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void initDevice(RadioTorchDeviceBlockEntity device) {
        polling = device.radioState().polling();
        customMap = device.radioState().customMap();
        channelField = addTextBox(29, 21, 82, 14, 15, device.radioState().channel());
        boolean sender = device instanceof RadioTorchSenderBlockEntity;
        for (int i = 0; i < 16; i++) {
            int column = i / 8;
            int row = i % 8;
            EditBox field = addTextBox((sender ? 29 : 11) + column * 130, 57 + row * 18,
                    82, 14, 15, device.radioState().mapping(i));
            field.visible = customMap;
            field.active = customMap;
            mappingFields.add(field);
        }
    }

    private void initLogic(RadioTorchLogicBlockEntity logic) {
        polling = logic.logicState().polling();
        descending = logic.logicState().descending();
        channelField = addTextBox(29, 21, 82, 14, 15, logic.logicState().channel());
        for (int i = 0; i < 16; i++) {
            int column = i / 8;
            int row = i % 8;
            mappingFields.add(addTextBox(29 + column * 130, 57 + row * 18, 46, 14, 15,
                    logic.logicState().mapping(i)));
            conditions[i] = logic.logicState().condition(i);
        }
    }

    private void initReader(RadioTorchReaderBlockEntity reader) {
        polling = reader.readerState().polling();
        for (int i = 0; i < RTTYReaderState.SLOT_COUNT; i++) {
            // GUIScreenRadioTorchReader: (25 + 4, 53 + 4), 72 - 8;
            // (119 + 4, 53 + 4), 126 - 8.
            readerChannelFields.add(addTextBox(29, 57 + i * 18, 64, 14, 15, reader.readerState().channel(i)));
            readerNameFields.add(addTextBox(123, 57 + i * 18, 118, 14, 25, reader.readerState().name(i)));
        }
    }

    private void initController(RadioTorchControllerBlockEntity controller) {
        polling = controller.controllerState().polling();
        channelField = addTextBox(29, 21, 82, 14, 15, controller.controllerState().channel());
    }

    private void initCounter(RadioTorchCounterBlockEntity counter) {
        polling = counter.counterState().polling();
        for (int i = 0; i < RTTYCounterState.SLOT_COUNT; i++) {
            counterChannelFields.add(addTextBox(29, 21 + i * 44, 86, 14, 10, counter.counterState().channel(i)));
        }
    }

    private EditBox addTextBox(int x, int y, int width, int height, int maxLength, String value) {
        return addRenderableWidget(LegacyGuiElements.createLegacyTextField(font, leftPos + x, topPos + y,
                width, height, maxLength, value));
    }

    private void togglePolling() {
        polling = !polling;
        sendBoolean("p", polling);
    }

    private void toggleCustom() {
        customMap = !customMap;
        for (EditBox field : mappingFields) {
            field.visible = customMap;
            field.active = customMap;
        }
        sendBoolean("m", customMap);
    }

    private void toggleDescending() {
        descending = !descending;
        sendBoolean("d", descending);
    }

    private void sendBoolean(String key, boolean value) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, value);
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }

    private void save() {
        CompoundTag tag = new CompoundTag();
        RadioTorchBlockEntity torch = menu.getBlockEntity();
        tag.putBoolean("p", polling);
        if (torch instanceof RadioTorchDeviceBlockEntity) {
            tag.putString("c", text(channelField));
            tag.putBoolean("m", customMap);
            putMapping(tag);
        } else if (torch instanceof RadioTorchLogicBlockEntity) {
            tag.putString("c", text(channelField));
            tag.putBoolean("d", descending);
            putMapping(tag);
            for (int i = 0; i < conditions.length; i++) {
                tag.putInt("c" + i, conditions[i]);
            }
        } else if (torch instanceof RadioTorchReaderBlockEntity) {
            for (int i = 0; i < readerChannelFields.size(); i++) {
                tag.putString("c" + i, text(readerChannelFields.get(i)));
                tag.putString("n" + i, text(readerNameFields.get(i)));
            }
        } else if (torch instanceof RadioTorchControllerBlockEntity) {
            tag.putString("c", text(channelField));
        } else if (torch instanceof RadioTorchCounterBlockEntity) {
            for (int i = 0; i < counterChannelFields.size(); i++) {
                tag.putString("c" + i, text(counterChannelFields.get(i)));
            }
        }
        ModMessages.sendTileControl(torch.getBlockPos(), tag);
    }

    private void putMapping(CompoundTag tag) {
        for (int i = 0; i < mappingFields.size(); i++) {
            tag.putString("m" + i, text(mappingFields.get(i)));
        }
    }

    private ResourceLocation texture() {
        RadioTorchBlockEntity torch = menu.getBlockEntity();
        if (torch instanceof RadioTorchSenderBlockEntity) {
            return SENDER_TEXTURE;
        }
        if (torch instanceof RadioTorchDeviceBlockEntity) {
            return RECEIVER_TEXTURE;
        }
        if (torch instanceof RadioTorchLogicBlockEntity) {
            return LOGIC_TEXTURE;
        }
        if (torch instanceof RadioTorchReaderBlockEntity) {
            return READER_TEXTURE;
        }
        if (torch instanceof RadioTorchControllerBlockEntity) {
            return CONTROLLER_TEXTURE;
        }
        return COUNTER_TEXTURE;
    }

    private void renderControlTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        RadioTorchBlockEntity torch = menu.getBlockEntity();
        Component line = null;
        if (torch instanceof RadioTorchDeviceBlockEntity) {
            if (hovering(137, 17, 18, 18, mouseX, mouseY)) {
                line = Component.literal(customMap ? "Custom Mapping" : "Redstone Passthrough");
            } else if (hovering(173, 17, 18, 18, mouseX, mouseY)) {
                line = Component.literal(polling ? "Polling" : "State Change");
            } else if (hovering(209, 17, 18, 18, mouseX, mouseY)) {
                line = Component.literal("Save Settings");
            }
        } else if (torch instanceof RadioTorchLogicBlockEntity) {
            if (hovering(137, 17, 18, 18, mouseX, mouseY)) {
                line = Component.literal(descending ? "Descending Order" : "Ascending Order");
            } else if (hovering(173, 17, 18, 18, mouseX, mouseY)) {
                line = Component.literal(polling ? "Polling" : "State Change");
            } else if (hovering(209, 17, 18, 18, mouseX, mouseY)) {
                line = Component.literal("Save Settings");
            } else {
                int condition = conditionAt(mouseX, mouseY);
                if (condition >= 0) {
                    line = Component.translatableWithFallback("desc.gui.rttyLogic.cond" + conditions[condition],
                            conditionDescription(conditions[condition]));
                }
            }
        } else if (torch instanceof RadioTorchReaderBlockEntity
                || torch instanceof RadioTorchControllerBlockEntity) {
            if (hovering(173, 17, 18, 18, mouseX, mouseY)) {
                line = Component.literal(polling ? "Polling" : "State Change");
            } else if (hovering(209, 17, 18, 18, mouseX, mouseY)) {
                line = Component.literal("Save Settings");
            }
        } else if (torch instanceof RadioTorchCounterBlockEntity) {
            if (hovering(193, 8, 18, 18, mouseX, mouseY)) {
                line = Component.literal(polling ? "Polling" : "State Change");
            } else if (hovering(193, 30, 18, 18, mouseX, mouseY)) {
                line = Component.literal("Save Settings");
            }
        }
        if (line != null) {
            graphics.renderTooltip(font, line, mouseX, mouseY);
        }
    }

    private int conditionAt(double mouseX, double mouseY) {
        for (int i = 0; i < conditions.length; i++) {
            int column = i / 8;
            int row = i % 8;
            if (hovering(7 + column * 130, 53 + row * 18, 18, 18, mouseX, mouseY)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }

    private static String conditionDescription(int condition) {
        return switch (condition) {
            case 0 -> "Signal §6LESS THAN§r Constant";
            case 1 -> "Signal §6LESS THAN OR EQUAL TO§r Constant";
            case 2 -> "Signal §6GREATER THAN OR EQUAL TO§r Constant";
            case 3 -> "Signal §6GREATER THAN§r Constant";
            case 4 -> "Signal §6EQUAL TO§r Constant";
            case 5 -> "Signal §6NOT EQUAL TO§r Constant";
            case 6 -> "Signal §6MATCHES§r String";
            case 7 -> "Signal §6DOES NOT MATCH§r String";
            case 8 -> "Signal §6CONTAINS§r String";
            default -> "Signal §6DOES NOT CONTAIN§r String";
        };
    }

    private void renderCounterFilterTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        RadioTorchBlockEntity torch = menu.getBlockEntity();
        if (!(torch instanceof RadioTorchCounterBlockEntity counter) || !menu.getCarried().isEmpty()) {
            return;
        }
        Slot slot = hoveredSlot;
        int menuSlot = slot == null ? -1 : menu.slots.indexOf(slot);
        if (menuSlot < 0 || menuSlot >= RadioTorchCounterBlockEntity.FILTER_SLOT_COUNT || !slot.hasItem()) {
            return;
        }
        graphics.renderComponentTooltip(font, List.of(
                Component.literal("Right click to change"),
                Component.literal(counter.filterModeLabel(menuSlot))), mouseX, mouseY);
    }

    /**
     * The legacy reader/controller texture contains an info glyph. Hovering
     * it listed values/functions supplied by the block directly behind the
     * torch; this remains an informational UI affordance, not a placement
     * restriction.
     */
    private void renderLegacyRorInfoTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        RadioTorchBlockEntity torch = menu.getBlockEntity();
        boolean readerGlyph = torch instanceof RadioTorchReaderBlockEntity
                && mouseX >= leftPos + 29 && mouseX < leftPos + 47
                && mouseY >= topPos + 17 && mouseY < topPos + 35;
        boolean controllerGlyph = torch instanceof RadioTorchControllerBlockEntity
                && mouseX >= leftPos + 137 && mouseX < leftPos + 155
                && mouseY >= topPos + 17 && mouseY < topPos + 35;
        if (!readerGlyph && !controllerGlyph) {
            return;
        }

        BlockPos attachedPos = torch.attachedPos();
        BlockEntity attached = MultiblockHelper.resolveOperationalCoreBlockEntity(minecraft.level, attachedPos);
        List<Component> lines = new ArrayList<>();
        if (readerGlyph && attached instanceof RORValueProvider provider) {
            lines.add(Component.literal("Readable values:"));
            appendRorInfo(lines, provider, RORInfo.PREFIX_VALUE, ChatFormatting.LIGHT_PURPLE);
        } else if (controllerGlyph && attached instanceof RORInfo info) {
            lines.add(Component.literal("Usable functions:"));
            appendRorInfo(lines, info, RORInfo.PREFIX_FUNCTION, ChatFormatting.AQUA);
        }
        if (!lines.isEmpty()) {
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        }
    }

    private static void appendRorInfo(List<Component> lines, RORInfo info, String prefix, ChatFormatting color) {
        String[] entries = info.getFunctionInfo();
        if (entries == null) {
            return;
        }
        for (String entry : entries) {
            if (entry != null && entry.startsWith(prefix)) {
                lines.add(Component.literal(entry.substring(prefix.length())).withStyle(color));
            }
        }
    }

    private static String text(EditBox box) {
        return box == null ? "" : box.getValue();
    }

}
