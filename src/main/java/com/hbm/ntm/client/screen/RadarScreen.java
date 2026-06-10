package com.hbm.ntm.client.screen;

import com.hbm.ntm.api.entity.RadarEntry;
import com.hbm.ntm.api.entity.RadarControl;
import com.hbm.ntm.api.entity.RadarControlPanel;
import com.hbm.ntm.api.entity.RadarDisplayProjection;
import com.hbm.ntm.api.entity.RadarGuiHitProfile;
import com.hbm.ntm.api.entity.RadarGuiLayout;
import com.hbm.ntm.api.entity.RadarGuiTargetProfile;
import com.hbm.ntm.api.entity.RadarLaunchKeyProfile;
import com.hbm.ntm.api.entity.RadarMap;
import com.hbm.ntm.api.entity.RadarScreenActionProfile;
import com.hbm.ntm.api.entity.RadarScreenHoverProfile;
import com.hbm.ntm.api.entity.RadarScreenTooltipProfile;
import com.hbm.ntm.api.entity.RadarScreenViewProfile;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.client.renderer.LegacyRadarDisplayRenderer;
import com.hbm.ntm.menu.RadarMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class RadarScreen extends AbstractContainerScreen<RadarMenu> {
    private static final ResourceLocation LINK_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_radar_link.png");
    private static final ResourceLocation RADAR_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/machine/gui_radar_nt.png");

    private final RandomSource random = RandomSource.create();
    private RadarScreenViewProfile view = RadarScreenViewProfile.MAIN;
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
        if (view.main()) {
            renderMainRadar(graphics, partialTick);
            return;
        }

        graphics.blit(LINK_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int powerWidth = menu.getPowerBarWidth(RadarGuiLayout.SLOT_ENERGY_WIDTH);
        if (powerWidth > 0) {
            graphics.blit(LINK_TEXTURE, leftPos + RadarGuiLayout.SLOT_ENERGY_X,
                    topPos + RadarGuiLayout.SLOT_ENERGY_Y, RadarGuiLayout.SLOT_ENERGY_U,
                    RadarGuiLayout.SLOT_ENERGY_V, powerWidth, RadarGuiLayout.SLOT_ENERGY_HEIGHT);
        }
        renderToggleStrip(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (view.main()) {
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
        if (view.main()) {
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
        if (view.main()) {
            return handleScreenAction(RadarScreenActionProfile.mainClick(leftPos, topPos, mouseX, mouseY), true);
        }

        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        return handleScreenAction(RadarScreenActionProfile.slotClick(leftPos, topPos, mouseX, mouseY), handled);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (view.main()) {
            if (keyCode == 256 || minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                onClose();
                return true;
            }
            int linkSlot = RadarLaunchKeyProfile.linkSlotForKey(keyCode);
            if (linkSlot >= 0 && RadarGuiHitProfile.hitsRadarArea(leftPos, topPos, lastMouseX, lastMouseY)) {
                sendLaunchCommand(linkSlot);
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderMainRadar(GuiGraphics graphics, float partialTick) {
        RadarBlockEntity radar = menu.getBlockEntity();
        graphics.blit(RADAR_TEXTURE, leftPos, topPos, 0, 0, RadarGuiLayout.MAIN_WIDTH, RadarGuiLayout.MAIN_HEIGHT);
        graphics.blit(RADAR_TEXTURE, leftPos + RadarGuiLayout.MAIN_SIDE_STRIP_X,
                topPos + RadarGuiLayout.MAIN_SIDE_STRIP_TOP_Y, RadarGuiLayout.MAIN_SIDE_STRIP_U,
                RadarGuiLayout.MAIN_SIDE_STRIP_TOP_V, RadarGuiLayout.MAIN_SIDE_STRIP_WIDTH,
                RadarGuiLayout.MAIN_SIDE_STRIP_TOP_HEIGHT);
        graphics.blit(RADAR_TEXTURE, leftPos + RadarGuiLayout.MAIN_SIDE_STRIP_X,
                topPos + RadarGuiLayout.MAIN_SIDE_STRIP_BOTTOM_Y, RadarGuiLayout.MAIN_SIDE_STRIP_U,
                RadarGuiLayout.MAIN_SIDE_STRIP_BOTTOM_V, RadarGuiLayout.MAIN_SIDE_STRIP_WIDTH,
                RadarGuiLayout.MAIN_SIDE_STRIP_BOTTOM_HEIGHT);

        int powerWidth = menu.getPowerBarWidth(RadarGuiLayout.MAIN_ENERGY_WIDTH);
        if (powerWidth > 0) {
            graphics.blit(RADAR_TEXTURE, leftPos + RadarGuiLayout.MAIN_ENERGY_X,
                    topPos + RadarGuiLayout.MAIN_ENERGY_Y, RadarGuiLayout.MAIN_ENERGY_U,
                    RadarGuiLayout.MAIN_ENERGY_V, powerWidth, RadarGuiLayout.MAIN_ENERGY_TEXTURE_HEIGHT);
        }

        for (RadarControlPanel.Button controlButton : RadarControlPanel.buttons()) {
            if (active(controlButton) ^ (menu.jammed() && random.nextBoolean())) {
                graphics.blit(RADAR_TEXTURE, leftPos + controlButton.mainX(), topPos + controlButton.mainY(),
                        controlButton.iconU(), controlButton.iconV(), RadarControlPanel.BUTTON_SIZE,
                        RadarControlPanel.BUTTON_SIZE);
            }
        }

        if (!menu.hasOperatingPower(RadarBlockEntity.CONSUMPTION)) {
            return;
        }

        if (menu.jammed()) {
            for (int x = 0; x < 5; x++) {
                for (int z = 0; z < 5; z++) {
                    LegacyRadarDisplayRenderer.renderGuiNoiseTile(RADAR_TEXTURE, graphics,
                            leftPos + RadarGuiLayout.RADAR_AREA_X + x * 40,
                            topPos + RadarGuiLayout.RADAR_AREA_Y + z * 40,
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
                int x = RadarGuiLayout.mapPixelX(leftPos, i);
                int y = RadarGuiLayout.mapPixelY(topPos, i);
                graphics.fill(x, y, x + 1, y + 1, 0xFF000000 | (RadarMap.green(height) << 8));
            }
        }
    }

    private void renderSweep(GuiGraphics graphics, float partialTick) {
        RadarBlockEntity radar = menu.getBlockEntity();
        LegacyRadarDisplayRenderer.renderGuiSweep(graphics,
                leftPos + RadarGuiLayout.RADAR_CENTER_X, topPos + RadarGuiLayout.RADAR_CENTER_Y,
                Mth.lerp(partialTick, radar.getPreviousRotation(), radar.getRotation()));
    }

    private void renderBlips(GuiGraphics graphics, RadarBlockEntity radar) {
        for (RadarEntry entry : radar.getEntries()) {
            RadarDisplayProjection.ScreenOffset offset =
                    RadarDisplayProjection.guiBlipOffset(entry.pos(), radar.getBlockPos(), radar.getRange());
            LegacyRadarDisplayRenderer.renderGuiBlip(RADAR_TEXTURE, graphics,
                    leftPos + RadarGuiLayout.RADAR_CENTER_X + offset.x(),
                    topPos + RadarGuiLayout.RADAR_CENTER_Y + offset.z(), entry.blipLevel());
        }
    }

    private void renderToggleStrip(GuiGraphics graphics) {
        for (RadarControlPanel.Button controlButton : RadarControlPanel.buttons()) {
            int x = leftPos + controlButton.slotX();
            int y = topPos + controlButton.slotY();
            int border = active(controlButton) ? 0xFF1F8F32 : 0xFF4C4C4C;
            graphics.fill(x - 1, y - 1, x + 9, y + 9, 0xFF101010);
            graphics.fill(x - 1, y - 1, x + 9, y, border);
            graphics.fill(x - 1, y + 8, x + 9, y + 9, border);
            graphics.fill(x - 1, y, x, y + 8, border);
            graphics.fill(x + 8, y, x + 9, y + 8, border);
            if (active(controlButton)) {
                graphics.blit(RADAR_TEXTURE, x, y, controlButton.iconU(), controlButton.iconV(),
                        RadarControlPanel.BUTTON_SIZE, RadarControlPanel.BUTTON_SIZE);
            }
        }
    }

    private void renderHoveredTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        RadarScreenHoverProfile.Hover hover =
                RadarScreenHoverProfile.slots(leftPos, topPos, mouseX, mouseY);
        renderChromeTooltip(graphics, hover, true, mouseX, mouseY);
    }

    private void renderMainTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        RadarScreenHoverProfile.Hover chromeHover =
                RadarScreenHoverProfile.mainChrome(leftPos, topPos, mouseX, mouseY);
        if (renderChromeTooltip(graphics, chromeHover, false, mouseX, mouseY)) {
            return;
        }

        RadarBlockEntity radar = menu.getBlockEntity();
        RadarGuiTargetProfile.Target entryTarget = RadarGuiTargetProfile.hoveredEntry(radar.getEntries(),
                radar.getBlockPos(), radar.getRange(), leftPos, topPos, mouseX, mouseY);
        if (entryTarget != null && entryTarget.hasEntry()) {
            graphics.renderTooltip(font, split(RadarScreenTooltipProfile.entry(radarName(entryTarget.entry().name()),
                    entryTarget.entry().pos())), mouseX, mouseY);
            return;
        }

        if (RadarGuiHitProfile.hitsRadarArea(leftPos, topPos, mouseX, mouseY)) {
            RadarGuiTargetProfile.Target target = RadarGuiTargetProfile.positionTarget(radar.getBlockPos(),
                    radar.getRange(), leftPos, topPos, mouseX, mouseY);
            graphics.renderTooltip(font, split(RadarScreenTooltipProfile.target(target.x(), target.z())),
                    mouseX, mouseY);
        }
    }

    private boolean renderChromeTooltip(GuiGraphics graphics, RadarScreenHoverProfile.Hover hover,
            boolean includeControlState, int mouseX, int mouseY) {
        switch (hover.type()) {
            case ENERGY -> graphics.renderTooltip(font, split(RadarScreenTooltipProfile.energy(menu.getPower(),
                    menu.getMaxPower(), menu.getRedPower())), mouseX, mouseY);
            case CONTROL -> {
                RadarControlPanel.Button button = hover.button();
                graphics.renderTooltip(font, split(includeControlState
                        ? RadarScreenTooltipProfile.control(button.tooltipKey(), active(button))
                        : localizedLines(button.tooltipKey())), mouseX, mouseY);
            }
            case TOGGLE_VIEW -> graphics.renderTooltip(font,
                    split(localizedLines(RadarScreenTooltipProfile.TOGGLE_GUI_KEY)), mouseX, mouseY);
            case CLEAR_MAP -> graphics.renderTooltip(font,
                    split(localizedLines(RadarScreenTooltipProfile.CLEAR_MAP_KEY)), mouseX, mouseY);
            case NONE -> {
                return false;
            }
        }
        return true;
    }

    private boolean active(RadarControlPanel.Button controlButton) {
        return menu.getState().controlActive(controlButton.control());
    }

    private void sendButton(RadarControl button) {
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(),
                RadarBlockEntity.controlTag(button)));
    }

    private void sendLaunchCommand(int linkSlot) {
        RadarBlockEntity radar = menu.getBlockEntity();
        RadarGuiTargetProfile.Target entryTarget = RadarGuiTargetProfile.hoveredEntry(radar.getEntries(),
                radar.getBlockPos(), radar.getRange(), leftPos, topPos, lastMouseX, lastMouseY);
        if (entryTarget != null && entryTarget.hasEntry()) {
            ModMessages.sendToServer(new TileControlPacket(radar.getBlockPos(),
                    RadarBlockEntity.launchEntityTag(linkSlot, entryTarget.entry().entityId())));
            return;
        }

        RadarGuiTargetProfile.Target target = RadarGuiTargetProfile.positionTarget(radar.getBlockPos(),
                radar.getRange(), leftPos, topPos, lastMouseX, lastMouseY);
        ModMessages.sendToServer(new TileControlPacket(radar.getBlockPos(),
                RadarBlockEntity.launchPositionTag(linkSlot, target.x(), target.z())));
    }

    private boolean handleScreenAction(RadarScreenActionProfile.Action action, boolean fallback) {
        return switch (action.type()) {
            case CONTROL -> {
                sendButton(action.control());
                yield true;
            }
            case VIEW -> {
                setView(action.view());
                yield true;
            }
            case CONSUME -> true;
            case NONE -> fallback;
        };
    }

    private void setView(RadarScreenViewProfile view) {
        this.view = view == null ? RadarScreenViewProfile.MAIN : view;
        applyViewDimensions();
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;
    }

    private void applyViewDimensions() {
        imageWidth = view.width();
        imageHeight = view.height();
        inventoryLabelY = view.inventoryLabelY();
    }

    private static Component radarName(String name) {
        return I18n.exists(name) ? Component.translatable(name) : Component.literal(name);
    }

    private static List<FormattedCharSequence> split(List<Component> tooltip) {
        return RadarScreenTooltipProfile.split(tooltip);
    }

    private static List<Component> localizedLines(String key) {
        return RadarScreenTooltipProfile.localizedLines(key);
    }
}
