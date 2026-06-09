package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.redstoneoverradio.RTTYCounterState;
import com.hbm.ntm.api.redstoneoverradio.RTTYReaderState;
import com.hbm.ntm.blockentity.RadioTorchBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchControllerBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchCounterBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchDeviceBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchLogicBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchReaderBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchSenderBlockEntity;
import com.hbm.ntm.menu.RadioTorchMenu;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

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
    private final List<EditBox> conditionFields = new ArrayList<>();
    private final List<EditBox> readerChannelFields = new ArrayList<>();
    private final List<EditBox> readerNameFields = new ArrayList<>();
    private final List<EditBox> counterChannelFields = new ArrayList<>();
    private Button pollingButton;
    private Button customButton;
    private Button descendingButton;
    private boolean polling;
    private boolean customMap;
    private boolean descending;

    public RadioTorchScreen(RadioTorchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        if (menu.getBlockEntity() instanceof RadioTorchCounterBlockEntity) {
            imageWidth = 218;
            imageHeight = 238;
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
        conditionFields.clear();
        readerChannelFields.clear();
        readerNameFields.clear();
        counterChannelFields.clear();
        pollingButton = null;
        customButton = null;
        descendingButton = null;

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
        addSaveButton();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture(), leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, titleLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderCounterFilterTooltip(graphics, mouseX, mouseY);
    }

    private void initDevice(RadioTorchDeviceBlockEntity device) {
        polling = device.radioState().polling();
        customMap = device.radioState().customMap();
        channelField = addTextBox(25, 17, 86, 14, 10, device.radioState().channel());
        customButton = addRenderableWidget(Button.builder(customLabel(), button -> toggleCustom())
                .bounds(leftPos + 116, topPos + 16, 42, 18)
                .build());
        pollingButton = addRenderableWidget(Button.builder(pollingLabel(), button -> togglePolling())
                .bounds(leftPos + 160, topPos + 16, 42, 18)
                .build());
        for (int i = 0; i < 16; i++) {
            int column = i / 8;
            int row = i % 8;
            mappingFields.add(addTextBox(8 + column * 124, 53 + row * 18, 84, 14, 32,
                    device.radioState().mapping(i)));
        }
    }

    private void initLogic(RadioTorchLogicBlockEntity logic) {
        polling = logic.logicState().polling();
        descending = logic.logicState().descending();
        channelField = addTextBox(25, 17, 86, 14, 10, logic.logicState().channel());
        descendingButton = addRenderableWidget(Button.builder(descendingLabel(), button -> toggleDescending())
                .bounds(leftPos + 116, topPos + 16, 42, 18)
                .build());
        pollingButton = addRenderableWidget(Button.builder(pollingLabel(), button -> togglePolling())
                .bounds(leftPos + 160, topPos + 16, 42, 18)
                .build());
        for (int i = 0; i < 16; i++) {
            int column = i / 8;
            int row = i % 8;
            mappingFields.add(addTextBox(8 + column * 124, 53 + row * 18, 62, 14, 32,
                    logic.logicState().mapping(i)));
            conditionFields.add(addTextBox(72 + column * 124, 53 + row * 18, 20, 14, 1,
                    Integer.toString(logic.logicState().condition(i))));
        }
    }

    private void initReader(RadioTorchReaderBlockEntity reader) {
        polling = reader.readerState().polling();
        pollingButton = addRenderableWidget(Button.builder(pollingLabel(), button -> togglePolling())
                .bounds(leftPos + 160, topPos + 16, 42, 18)
                .build());
        for (int i = 0; i < RTTYReaderState.SLOT_COUNT; i++) {
            readerChannelFields.add(addTextBox(25, 43 + i * 18, 82, 14, 10, reader.readerState().channel(i)));
            readerNameFields.add(addTextBox(128, 43 + i * 18, 96, 14, 32, reader.readerState().name(i)));
        }
    }

    private void initController(RadioTorchControllerBlockEntity controller) {
        polling = controller.controllerState().polling();
        channelField = addTextBox(25, 17, 86, 14, 10, controller.controllerState().channel());
        pollingButton = addRenderableWidget(Button.builder(pollingLabel(), button -> togglePolling())
                .bounds(leftPos + 160, topPos + 16, 42, 18)
                .build());
    }

    private void initCounter(RadioTorchCounterBlockEntity counter) {
        polling = counter.counterState().polling();
        pollingButton = addRenderableWidget(Button.builder(pollingLabel(), button -> togglePolling())
                .bounds(leftPos + 193, topPos + 8, 18, 18)
                .build());
        for (int i = 0; i < RTTYCounterState.SLOT_COUNT; i++) {
            counterChannelFields.add(addTextBox(29, 21 + i * 44, 86, 14, 10, counter.counterState().channel(i)));
        }
    }

    private void addSaveButton() {
        if (menu.getBlockEntity() instanceof RadioTorchCounterBlockEntity) {
            addRenderableWidget(Button.builder(Component.literal("S"), button -> save())
                    .bounds(leftPos + 193, topPos + 30, 18, 18)
                    .build());
            return;
        }
        addRenderableWidget(Button.builder(Component.literal("Save"), button -> save())
                .bounds(leftPos + 206, topPos + 16, 42, 18)
                .build());
    }

    private EditBox addTextBox(int x, int y, int width, int height, int maxLength, String value) {
        return addRenderableWidget(LegacyGuiElements.createLegacyTextField(font, leftPos + x, topPos + y,
                width, height, maxLength, value));
    }

    private void togglePolling() {
        polling = !polling;
        if (pollingButton != null) {
            pollingButton.setMessage(pollingLabel());
        }
        sendBoolean("p", polling);
    }

    private void toggleCustom() {
        customMap = !customMap;
        if (customButton != null) {
            customButton.setMessage(customLabel());
        }
        sendBoolean("m", customMap);
    }

    private void toggleDescending() {
        descending = !descending;
        if (descendingButton != null) {
            descendingButton.setMessage(descendingLabel());
        }
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
            for (int i = 0; i < conditionFields.size(); i++) {
                tag.putInt("c" + i, Mth.clamp(parseInt(conditionFields.get(i).getValue()), 0, 9));
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

    private Component pollingLabel() {
        return Component.literal(polling ? "Poll" : "State");
    }

    private Component customLabel() {
        return Component.literal(customMap ? "Map" : "Pass");
    }

    private Component descendingLabel() {
        return Component.literal(descending ? "Desc" : "Asc");
    }

    private static String text(EditBox box) {
        return box == null ? "" : box.getValue();
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
