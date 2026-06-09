package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.redstoneoverradio.RTTYTelexState;
import com.hbm.ntm.menu.RadioTelexMenu;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class RadioTelexScreen extends AbstractContainerScreen<RadioTelexMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/machine/gui_telex.png");
    private static final int BUTTON_SIZE = 18;

    private EditBox txFrequency;
    private EditBox rxFrequency;
    private boolean textFocus;
    private String[] txBuffer;
    private int cursorPos;

    public RadioTelexScreen(RadioTelexMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 256;
        imageHeight = 244;
        titleLabelY = 1000;
        inventoryLabelY = 1000;
        txBuffer = menu.getBlockEntity().telexState().txCopy();
        for (int i = RTTYTelexState.LINE_COUNT - 1; i > 0; i--) {
            if (!txBuffer[i].isEmpty()) {
                cursorPos = i;
                break;
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        RTTYTelexState state = state();
        txFrequency = LegacyGuiElements.createLegacyTextField(font, leftPos + 29, topPos + 110, 90, 14, 10,
                state.txChannel());
        rxFrequency = LegacyGuiElements.createLegacyTextField(font, leftPos + 29, topPos + 224, 90, 14, 10,
                state.rxChannel());
        addRenderableWidget(txFrequency);
        addRenderableWidget(rxFrequency);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        drawTxBuffer(graphics);
        drawRxBuffer(graphics);
        drawWaveform(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTelexTooltip(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        textFocus = LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 7, 7, 242, 74);
        char character = '\0';
        String command = null;

        if (hovering(mouseX, mouseY, 7, 85)) {
            character = RTTYTelexState.BELL;
        } else if (hovering(mouseX, mouseY, 27, 85)) {
            character = RTTYTelexState.PRINT;
        } else if (hovering(mouseX, mouseY, 47, 85)) {
            character = RTTYTelexState.CLEAR;
        } else if (hovering(mouseX, mouseY, 67, 85)) {
            character = '\u00a7';
        } else if (hovering(mouseX, mouseY, 87, 85)) {
            character = RTTYTelexState.PAUSE;
        } else if (hovering(mouseX, mouseY, 127, 105) || hovering(mouseX, mouseY, 127, 219)) {
            command = "sve";
        } else if (hovering(mouseX, mouseY, 147, 105)) {
            command = "snd";
        } else if (hovering(mouseX, mouseY, 167, 105)) {
            command = "rxdel";
            clearTx();
            sendTxBuffer(null);
        } else if (hovering(mouseX, mouseY, 147, 219)) {
            command = "rxprt";
        } else if (hovering(mouseX, mouseY, 167, 219)) {
            command = "rxcls";
        }

        if (command != null) {
            LegacyGuiElements.playClickSound();
            sendCommand(command);
            return true;
        }
        if (character != '\0') {
            LegacyGuiElements.playClickSound();
            setTextFocus();
            submitChar(character);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (txFrequency.keyPressed(keyCode, scanCode, modifiers) || rxFrequency.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (textFocus) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                textFocus = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_UP) {
                cursorPos = Math.max(0, cursorPos - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_DOWN) {
                cursorPos = Math.min(RTTYTelexState.LINE_COUNT - 1, cursorPos + 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !txBuffer[cursorPos].isEmpty()) {
                txBuffer[cursorPos] = txBuffer[cursorPos].substring(0, txBuffer[cursorPos].length() - 1);
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (txFrequency.charTyped(codePoint, modifiers) || rxFrequency.charTyped(codePoint, modifiers)) {
            return true;
        }
        if (textFocus && SharedConstants.isAllowedChatCharacter(codePoint)) {
            submitChar(codePoint);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        sendTxBuffer(null);
        super.onClose();
    }

    private void drawTxBuffer(GuiGraphics graphics) {
        for (int line = 0; line < RTTYTelexState.LINE_COUNT; line++) {
            drawTelexLine(graphics, txBuffer[line], 11, 11 + 14 * line, true);
            if (System.currentTimeMillis() % 1000L < 500L && textFocus && cursorPos == line) {
                int x = Math.max(11 + 7 * txBuffer[line].length(), 11);
                graphics.drawString(font, "|", leftPos + x, topPos + 11 + 14 * line, 0x00FF00, false);
            }
        }
    }

    private void drawRxBuffer(GuiGraphics graphics) {
        RTTYTelexState state = state();
        for (int line = 0; line < RTTYTelexState.LINE_COUNT; line++) {
            drawTelexLine(graphics, state.rxLine(line), 11, 145 + 14 * line, false);
        }
    }

    private void drawTelexLine(GuiGraphics graphics, String text, int baseX, int baseY, boolean showControls) {
        String format = ChatFormatting.RESET.toString();
        int x = baseX;
        for (int index = 0; index < text.length(); index++) {
            char c = text.charAt(index);
            if (c == '\u00a7' && text.length() > index + 1) {
                format = "\u00a7" + text.charAt(index + 1);
            }
            String glyph = format + c;
            if (showControls) {
                if (c == RTTYTelexState.BELL) {
                    glyph = ChatFormatting.RED + "B";
                } else if (c == RTTYTelexState.PRINT) {
                    glyph = ChatFormatting.RED + "P";
                } else if (c == RTTYTelexState.CLEAR) {
                    glyph = ChatFormatting.RED + "<";
                } else if (c == RTTYTelexState.PAUSE) {
                    glyph = ChatFormatting.RED + "W";
                }
            } else if (c == '\u00a7' || (index > 0 && text.charAt(index - 1) == '\u00a7')) {
                glyph = "";
            }
            if (!glyph.isEmpty()) {
                int centeredX = x + (7 - font.width(String.valueOf(c))) / 2;
                graphics.drawString(font, glyph, leftPos + centeredX, topPos + baseY, 0x00FF00, false);
            }
            x += 7;
        }
    }

    private void drawWaveform(GuiGraphics graphics) {
        char sendingChar = state().sendingChar();
        Random random = new Random(sendingChar);
        int previousY = topPos + 94;
        for (int i = 0; i < 48; i++) {
            int nextY = topPos + 94;
            if (sendingChar != ' ' && i > 4 && i < 43) {
                nextY += Math.max(-7, Math.min(7, (int) Math.round(random.nextGaussian() * 7.0D)));
            }
            int x0 = leftPos + 199 + i;
            int x1 = x0 + 1;
            graphics.hLine(Math.min(x0, x1), Math.max(x0, x1), previousY, 0xFF00FF00);
            graphics.vLine(x1, Math.min(previousY, nextY), Math.max(previousY, nextY), 0xFF00FF00);
            previousY = nextY;
        }
    }

    private void renderTelexTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hovering(mouseX, mouseY, 7, 85)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.GOLD + "BELL", "Plays a bell when this character is received");
        } else if (hovering(mouseX, mouseY, 27, 85)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.GOLD + "PRINT", "Forces recipient to print message after transmission ends");
        } else if (hovering(mouseX, mouseY, 47, 85)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.GOLD + "CLEAR SCREEN", "Wipes message buffer when this character is received");
        } else if (hovering(mouseX, mouseY, 67, 85)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.GOLD + "FORMAT", "Inserts format character for message formatting");
        } else if (hovering(mouseX, mouseY, 87, 85)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.GOLD + "PAUSE", "Pauses message transmission for one second");
        } else if (hovering(mouseX, mouseY, 127, 105) || hovering(mouseX, mouseY, 127, 219)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.GREEN + "SAVE ID");
        } else if (hovering(mouseX, mouseY, 147, 105)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.YELLOW + "SEND MESSAGE");
        } else if (hovering(mouseX, mouseY, 167, 105)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.RED + "DELETE MESSAGE BUFFER");
        } else if (hovering(mouseX, mouseY, 147, 219)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.AQUA + "PRINT MESSAGE");
        } else if (hovering(mouseX, mouseY, 167, 219)) {
            tooltip(graphics, mouseX, mouseY, ChatFormatting.RED + "CLEAR SCREEN");
        }
    }

    private void tooltip(GuiGraphics graphics, int mouseX, int mouseY, String... lines) {
        graphics.renderComponentTooltip(font, List.of(lines).stream()
                .map(line -> (Component) Component.literal(line))
                .toList(), mouseX, mouseY);
    }

    private boolean hovering(double mouseX, double mouseY, int x, int y) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, BUTTON_SIZE, BUTTON_SIZE);
    }

    private void setTextFocus() {
        textFocus = true;
        txFrequency.setFocused(false);
        rxFrequency.setFocused(false);
    }

    private void submitChar(char c) {
        String line = txBuffer[cursorPos];
        if (line.length() < RTTYTelexState.LINE_WIDTH) {
            txBuffer[cursorPos] = line + c;
        }
    }

    private void clearTx() {
        for (int i = 0; i < txBuffer.length; i++) {
            txBuffer[i] = "";
        }
    }

    private void sendCommand(String command) {
        CompoundTag tag = new CompoundTag();
        tag.putString("cmd", command);
        if ("snd".equals(command)) {
            putTxBuffer(tag);
        }
        if ("sve".equals(command)) {
            tag.putString("txChan", txFrequency.getValue());
            tag.putString("rxChan", rxFrequency.getValue());
        }
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }

    private void sendTxBuffer(String command) {
        CompoundTag tag = new CompoundTag();
        if (command != null) {
            tag.putString("cmd", command);
        }
        putTxBuffer(tag);
        ModMessages.sendTileControl(menu.getBlockEntity().getBlockPos(), tag);
    }

    private void putTxBuffer(CompoundTag tag) {
        for (int i = 0; i < txBuffer.length; i++) {
            tag.putString("tx" + i, txBuffer[i]);
        }
    }

    private RTTYTelexState state() {
        return menu.getBlockEntity().telexState();
    }
}
