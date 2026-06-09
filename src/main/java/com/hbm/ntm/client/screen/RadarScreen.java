package com.hbm.ntm.client.screen;

import com.hbm.ntm.api.entity.RadarEntry;
import com.hbm.ntm.api.entity.RadarMap;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.client.renderer.LegacyRadarDisplayRenderer;
import com.hbm.ntm.menu.RadarMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RadarScreen extends AbstractContainerScreen<RadarMenu> {
    private static final ResourceLocation LINK_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_radar_link.png");
    private static final ResourceLocation RADAR_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_radar_nt.png");
    private static final int[] CONTROLS = {
            RadarBlockEntity.CONTROL_SCAN_MISSILES,
            RadarBlockEntity.CONTROL_SCAN_SHELLS,
            RadarBlockEntity.CONTROL_SCAN_PLAYERS,
            RadarBlockEntity.CONTROL_SMART_MODE,
            RadarBlockEntity.CONTROL_RED_MODE,
            RadarBlockEntity.CONTROL_SHOW_MAP
    };
    private static final String[] TOOLTIP_KEYS = {
            "radar.detectMissiles",
            "radar.detectShells",
            "radar.detectPlayers",
            "radar.smartMode",
            "radar.redMode",
            "radar.showMap"
    };
    private static final int MAIN_WIDTH = 216;
    private static final int MAIN_HEIGHT = 234;
    private static final int LINK_WIDTH = 176;
    private static final int LINK_HEIGHT = 184;
    private static final int RADAR_AREA_X = 8;
    private static final int RADAR_AREA_Y = 17;
    private static final int RADAR_AREA_SIZE = 200;
    private static final int RADAR_CENTER_X = 108;
    private static final int RADAR_CENTER_Y = 117;

    private final RandomSource random = RandomSource.create();
    private boolean mainView = true;
    private int lastMouseX;
    private int lastMouseY;

    public RadarScreen(RadarMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        applyViewDimensions();
    }

    @Override
    protected void init() {
        applyViewDimensions();
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (mainView) {
            renderMainRadar(graphics, partialTick);
            return;
        }

        graphics.blit(LINK_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int powerWidth = menu.getPowerBarWidth(160);
        if (powerWidth > 0) {
            graphics.blit(LINK_TEXTURE, leftPos + 8, topPos + 64, 0, 185, powerWidth, 16);
        }
        renderToggleStrip(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (mainView) {
            return;
        }

        String name = title.getString();
        graphics.drawString(font, name, imageWidth / 2 - font.width(name) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        renderBackground(graphics);
        if (mainView) {
            renderBg(graphics, partialTick, mouseX, mouseY);
            renderMainTooltips(graphics, mouseX, mouseY);
            return;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        renderHoveredTooltips(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mainView) {
            for (int index = 0; index < CONTROLS.length; index++) {
                if (isHovering(-10, 88 + index * 10, 8, 8, mouseX, mouseY)) {
                    sendButton(CONTROLS[index]);
                    return true;
                }
            }
            if (isHovering(-10, 158, 8, 8, mouseX, mouseY)) {
                setMainView(false);
                return true;
            }
            if (isHovering(-10, 178, 8, 8, mouseX, mouseY)) {
                sendButton(RadarBlockEntity.CONTROL_CLEAR_MAP);
                return true;
            }
            return true;
        }

        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (isHovering(5, 5, 8, 8, mouseX, mouseY)) {
            setMainView(true);
            return true;
        }
        for (int index = 0; index < CONTROLS.length; index++) {
            if (isHovering(toggleX(index), 82, 8, 8, mouseX, mouseY)) {
                sendButton(CONTROLS[index]);
                return true;
            }
        }
        return handled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (mainView) {
            if (keyCode == 256 || minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                onClose();
                return true;
            }
            int linkSlot = linkSlotForKey(keyCode);
            if (linkSlot >= 0 && isHovering(RADAR_AREA_X, RADAR_AREA_Y, RADAR_AREA_SIZE, RADAR_AREA_SIZE,
                    lastMouseX, lastMouseY)) {
                sendLaunchCommand(linkSlot);
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderMainRadar(GuiGraphics graphics, float partialTick) {
        RadarBlockEntity radar = menu.getBlockEntity();
        graphics.blit(RADAR_TEXTURE, leftPos, topPos, 0, 0, MAIN_WIDTH, MAIN_HEIGHT);
        graphics.blit(RADAR_TEXTURE, leftPos - 14, topPos + 84, 224, 0, 14, 66);
        graphics.blit(RADAR_TEXTURE, leftPos - 14, topPos + 154, 224, 66, 14, 36);

        int powerWidth = menu.getPowerBarWidth(200);
        if (powerWidth > 0) {
            graphics.blit(RADAR_TEXTURE, leftPos + 8, topPos + 221, 0, 234, powerWidth, 16);
        }

        for (int index = 0; index < CONTROLS.length; index++) {
            if (active(index) ^ (menu.jammed() && random.nextBoolean())) {
                graphics.blit(RADAR_TEXTURE, leftPos - 10, topPos + 88 + index * 10,
                        238, 4 + index * 10, 8, 8);
            }
        }

        if (menu.getPower() < RadarBlockEntity.CONSUMPTION) {
            return;
        }

        if (menu.jammed()) {
            for (int x = 0; x < 5; x++) {
                for (int z = 0; z < 5; z++) {
                    LegacyRadarDisplayRenderer.renderGuiNoiseTile(RADAR_TEXTURE, graphics,
                            leftPos + RADAR_AREA_X + x * 40, topPos + RADAR_AREA_Y + z * 40,
                            random.nextInt(81));
                }
            }
            return;
        }

        if (menu.showMap()) {
            renderMap(graphics, radar.getMap());
        }
        renderSweep(graphics, partialTick);
        renderBlips(graphics, radar);
    }

    private void renderMap(GuiGraphics graphics, byte[] map) {
        int size = Math.min(map.length, RadarBlockEntity.MAP_SIZE);
        for (int i = 0; i < size; i++) {
            byte height = map[i];
            if (height > 0) {
                int x = leftPos + RADAR_AREA_X + i % RadarBlockEntity.MAP_WIDTH;
                int y = topPos + RADAR_AREA_Y + 1 + i / RadarBlockEntity.MAP_WIDTH;
                graphics.fill(x, y, x + 1, y + 1, 0xFF000000 | (RadarMap.green(height) << 8));
            }
        }
    }

    private void renderSweep(GuiGraphics graphics, float partialTick) {
        RadarBlockEntity radar = menu.getBlockEntity();
        LegacyRadarDisplayRenderer.renderGuiSweep(graphics, leftPos + RADAR_CENTER_X, topPos + RADAR_CENTER_Y,
                Mth.lerp(partialTick, radar.getPreviousRotation(), radar.getRotation()));
    }

    private void renderBlips(GuiGraphics graphics, RadarBlockEntity radar) {
        for (RadarEntry entry : radar.getEntries()) {
            LegacyRadarDisplayRenderer.ScreenOffset offset =
                    LegacyRadarDisplayRenderer.guiBlipOffset(entry.pos(), radar.getBlockPos(), radar.getRange());
            LegacyRadarDisplayRenderer.renderGuiBlip(RADAR_TEXTURE, graphics,
                    leftPos + RADAR_CENTER_X + offset.x(), topPos + RADAR_CENTER_Y + offset.z(), entry.blipLevel());
        }
    }

    private void renderToggleStrip(GuiGraphics graphics) {
        for (int index = 0; index < CONTROLS.length; index++) {
            int x = leftPos + toggleX(index);
            int y = topPos + 82;
            int border = active(index) ? 0xFF1F8F32 : 0xFF4C4C4C;
            graphics.fill(x - 1, y - 1, x + 9, y + 9, 0xFF101010);
            graphics.fill(x - 1, y - 1, x + 9, y, border);
            graphics.fill(x - 1, y + 8, x + 9, y + 9, border);
            graphics.fill(x - 1, y, x, y + 8, border);
            graphics.fill(x + 8, y, x + 9, y + 8, border);
            if (active(index)) {
                graphics.blit(RADAR_TEXTURE, x, y, 238, 4 + index * 10, 8, 8);
            }
        }
    }

    private void renderHoveredTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(8, 64, 160, 16, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE"),
                    Component.literal("Redstone: " + menu.getRedPower()).withStyle(ChatFormatting.RED)
            )), mouseX, mouseY);
            return;
        }
        for (int index = 0; index < TOOLTIP_KEYS.length; index++) {
            if (isHovering(toggleX(index), 82, 8, 8, mouseX, mouseY)) {
                List<Component> tooltip = localizedLines(TOOLTIP_KEYS[index]);
                tooltip.add(Component.translatable(active(index)
                        ? "container.hbm_ntm_rebirth.radar.enabled"
                        : "container.hbm_ntm_rebirth.radar.disabled")
                        .withStyle(active(index) ? ChatFormatting.GREEN : ChatFormatting.RED));
                graphics.renderTooltip(font, split(tooltip), mouseX, mouseY);
                return;
            }
        }
        if (isHovering(5, 5, 8, 8, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(localizedLines("radar.toggleGui")), mouseX, mouseY);
        }
    }

    private void renderMainTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isHovering(8, 221, 200, 7, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(List.of(
                    Component.literal(menu.getPower() + " / " + menu.getMaxPower() + " HE"),
                    Component.literal("Redstone: " + menu.getRedPower()).withStyle(ChatFormatting.RED)
            )), mouseX, mouseY);
            return;
        }

        for (int index = 0; index < TOOLTIP_KEYS.length; index++) {
            if (isHovering(-10, 88 + index * 10, 8, 8, mouseX, mouseY)) {
                graphics.renderTooltip(font, split(localizedLines(TOOLTIP_KEYS[index])), mouseX, mouseY);
                return;
            }
        }
        if (isHovering(-10, 158, 8, 8, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(localizedLines("radar.toggleGui")), mouseX, mouseY);
            return;
        }
        if (isHovering(-10, 178, 8, 8, mouseX, mouseY)) {
            graphics.renderTooltip(font, split(localizedLines("radar.clearMap")), mouseX, mouseY);
            return;
        }

        RadarBlockEntity radar = menu.getBlockEntity();
        for (RadarEntry entry : radar.getEntries()) {
            LegacyRadarDisplayRenderer.ScreenOffset offset =
                    LegacyRadarDisplayRenderer.guiBlipHitOffset(entry.pos(), radar.getBlockPos(), radar.getRange());
            int x = leftPos + (int) offset.x() + RADAR_CENTER_X;
            int z = topPos + (int) offset.z() + RADAR_CENTER_Y;
            if (mouseX + 5 > x && mouseX - 4 <= x && mouseY + 5 > z && mouseY - 4 <= z) {
                graphics.renderTooltip(font, split(List.of(
                        radarName(entry.name()),
                        Component.literal(entry.pos().getX() + " / " + entry.pos().getZ()),
                        Component.literal("Alt.: " + entry.pos().getY())
                )), mouseX, mouseY);
                return;
            }
        }

        if (isHovering(RADAR_AREA_X, RADAR_AREA_Y, RADAR_AREA_SIZE, RADAR_AREA_SIZE, mouseX, mouseY)) {
            int targetX = LegacyRadarDisplayRenderer.guiTargetX(mouseX - leftPos - RADAR_CENTER_X,
                    radar.getBlockPos(), radar.getRange());
            int targetZ = LegacyRadarDisplayRenderer.guiTargetZ(mouseY - topPos - RADAR_CENTER_Y,
                    radar.getBlockPos(), radar.getRange());
            graphics.renderTooltip(font, split(List.of(Component.literal(targetX + " / " + targetZ))),
                    mouseX, mouseY);
        }
    }

    private boolean active(int index) {
        return switch (index) {
            case 0 -> menu.scanMissiles();
            case 1 -> menu.scanShells();
            case 2 -> menu.scanPlayers();
            case 3 -> menu.smartMode();
            case 4 -> menu.redMode();
            case 5 -> menu.showMap();
            default -> false;
        };
    }

    private static int toggleX(int index) {
        return 52 + index * 12;
    }

    private void sendButton(int button) {
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(),
                RadarBlockEntity.controlTag(button)));
    }

    private void sendLaunchCommand(int linkSlot) {
        RadarBlockEntity radar = menu.getBlockEntity();
        for (RadarEntry entry : radar.getEntries()) {
            LegacyRadarDisplayRenderer.ScreenOffset offset =
                    LegacyRadarDisplayRenderer.guiBlipHitOffset(entry.pos(), radar.getBlockPos(), radar.getRange());
            int x = leftPos + (int) offset.x() + RADAR_CENTER_X;
            int z = topPos + (int) offset.z() + RADAR_CENTER_Y;
            if (lastMouseX + 5 > x && lastMouseX - 4 <= x && lastMouseY + 5 > z && lastMouseY - 4 <= z) {
                ModMessages.sendToServer(new TileControlPacket(radar.getBlockPos(),
                        RadarBlockEntity.launchEntityTag(linkSlot, entry.entityId())));
                return;
            }
        }

        int targetX = LegacyRadarDisplayRenderer.guiTargetX(lastMouseX - leftPos - RADAR_CENTER_X,
                radar.getBlockPos(), radar.getRange());
        int targetZ = LegacyRadarDisplayRenderer.guiTargetZ(lastMouseY - topPos - RADAR_CENTER_Y,
                radar.getBlockPos(), radar.getRange());
        ModMessages.sendToServer(new TileControlPacket(radar.getBlockPos(),
                RadarBlockEntity.launchPositionTag(linkSlot, targetX, targetZ)));
    }

    private static int linkSlotForKey(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_8) {
            return keyCode - GLFW.GLFW_KEY_1;
        }
        if (keyCode >= GLFW.GLFW_KEY_KP_1 && keyCode <= GLFW.GLFW_KEY_KP_8) {
            return keyCode - GLFW.GLFW_KEY_KP_1;
        }
        return -1;
    }

    private void setMainView(boolean mainView) {
        this.mainView = mainView;
        applyViewDimensions();
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;
    }

    private void applyViewDimensions() {
        imageWidth = mainView ? MAIN_WIDTH : LINK_WIDTH;
        imageHeight = mainView ? MAIN_HEIGHT : LINK_HEIGHT;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    private static Component radarName(String name) {
        return I18n.exists(name) ? Component.translatable(name) : Component.literal(name);
    }

    private static List<FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    private static List<Component> localizedLines(String key) {
        return new ArrayList<>(Arrays.stream(Component.translatable(key).getString().split("\\$"))
                .map(Component::literal)
                .toList());
    }
}
