package com.hbm.ntm.client.screen;

import com.hbm.ntm.api.entity.RadarControl;
import com.hbm.ntm.api.entity.RadarControlPanel;
import com.hbm.ntm.api.entity.RadarGuiRenderProfile;
import com.hbm.ntm.api.entity.RadarGuiTargetProfile;
import com.hbm.ntm.api.entity.RadarLaunchCommand;
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

        blit(graphics, LINK_TEXTURE, RadarGuiRenderProfile.slotBackground(leftPos, topPos));

        int powerWidth = menu.getPowerBarWidth(RadarGuiRenderProfile.slotEnergyWidth());
        if (powerWidth > 0) {
            RadarGuiRenderProfile.EnergyBar bar =
                    RadarGuiRenderProfile.slotEnergyBar(leftPos, topPos, powerWidth);
            graphics.blit(LINK_TEXTURE, bar.x(), bar.y(), bar.u(), bar.v(), bar.width(), bar.height());
        }
        renderToggleStrip(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (view.main()) {
            return;
        }

        String name = title.getString();
        graphics.drawString(font, name, view.titleX(font.width(name)), view.titleY(), view.labelColor(), false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                view.labelColor(), false);
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
            return handleScreenAction(RadarScreenActionProfile.mainKey(keyCode,
                    minecraft.options.keyInventory.matches(keyCode, scanCode),
                    leftPos, topPos, lastMouseX, lastMouseY), true);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void renderMainRadar(GuiGraphics graphics, float partialTick) {
        RadarBlockEntity radar = menu.getBlockEntity();
        for (RadarGuiRenderProfile.TextureBlit background : RadarGuiRenderProfile.mainBackground(leftPos, topPos)) {
            blit(graphics, RADAR_TEXTURE, background);
        }

        int powerWidth = menu.getPowerBarWidth(RadarGuiRenderProfile.mainEnergyWidth());
        if (powerWidth > 0) {
            RadarGuiRenderProfile.EnergyBar bar =
                    RadarGuiRenderProfile.mainEnergyBar(leftPos, topPos, powerWidth);
            graphics.blit(RADAR_TEXTURE, bar.x(), bar.y(), bar.u(), bar.v(), bar.width(), bar.height());
        }

        for (RadarControlPanel.Button controlButton : RadarControlPanel.buttons()) {
            if (RadarGuiRenderProfile.shouldRenderMainControlIcon(active(controlButton), menu.jammed(),
                    random.nextBoolean())) {
                blit(graphics, RADAR_TEXTURE,
                        RadarGuiRenderProfile.mainControlIcon(leftPos, topPos, controlButton));
            }
        }

        if (!menu.hasOperatingPower(RadarBlockEntity.CONSUMPTION)) {
            return;
        }

        if (menu.jammed()) {
            for (RadarGuiRenderProfile.NoiseTile tile : RadarGuiRenderProfile.noiseTiles()) {
                LegacyRadarDisplayRenderer.renderGuiNoiseTile(RADAR_TEXTURE, graphics,
                        leftPos + tile.x(), topPos + tile.y(),
                        random.nextInt(RadarGuiRenderProfile.NOISE_TEXTURE_VARIANTS));
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
        RadarGuiRenderProfile.forEachMapPixel(leftPos, topPos, map, RadarBlockEntity.MAP_SIZE,
                pixel -> graphics.fill(pixel.x(), pixel.y(), pixel.x() + 1, pixel.y() + 1, pixel.color()));
    }

    private void renderSweep(GuiGraphics graphics, float partialTick) {
        RadarBlockEntity radar = menu.getBlockEntity();
        RadarGuiRenderProfile.ScreenPoint center = RadarGuiRenderProfile.sweepCenter(leftPos, topPos);
        LegacyRadarDisplayRenderer.renderGuiSweep(graphics, center.x(), center.y(),
                RadarGuiRenderProfile.sweepRotation(partialTick, radar.getPreviousRotation(), radar.getRotation()));
    }

    private void renderBlips(GuiGraphics graphics, RadarBlockEntity radar) {
        RadarGuiRenderProfile.forEachBlip(leftPos, topPos, radar.getEntries(), radar.getBlockPos(), radar.getRange(),
                blip -> LegacyRadarDisplayRenderer.renderGuiBlip(RADAR_TEXTURE, graphics,
                        blip.x(), blip.y(), blip.level()));
    }

    private void renderToggleStrip(GuiGraphics graphics) {
        for (RadarControlPanel.Button controlButton : RadarControlPanel.buttons()) {
            RadarGuiRenderProfile.SlotToggleFrame frame =
                    RadarGuiRenderProfile.slotToggleFrame(controlButton, active(controlButton));
            int x = leftPos + frame.x();
            int y = topPos + frame.y();
            int border = frame.borderColor();
            graphics.fill(leftPos + frame.outerLeft(), topPos + frame.outerTop(),
                    leftPos + frame.outerRight(), topPos + frame.outerBottom(),
                    RadarGuiRenderProfile.SLOT_TOGGLE_BACKGROUND);
            graphics.fill(leftPos + frame.outerLeft(), topPos + frame.outerTop(),
                    leftPos + frame.outerRight(), y, border);
            graphics.fill(leftPos + frame.outerLeft(), topPos + frame.iconBottom(),
                    leftPos + frame.outerRight(), topPos + frame.outerBottom(), border);
            graphics.fill(leftPos + frame.outerLeft(), y, x, topPos + frame.iconBottom(), border);
            graphics.fill(leftPos + frame.iconRight(), y, leftPos + frame.outerRight(),
                    topPos + frame.iconBottom(), border);
            if (active(controlButton)) {
                blit(graphics, RADAR_TEXTURE,
                        RadarGuiRenderProfile.slotControlIcon(leftPos, topPos, controlButton));
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
        RadarScreenTooltipProfile.mainTarget(radar.getEntries(), radar.getBlockPos(), radar.getRange(),
                leftPos, topPos, mouseX, mouseY, RadarScreen::radarName)
                .ifPresent(tooltip -> graphics.renderTooltip(font, split(tooltip.lines()), tooltip.x(), tooltip.y()));
    }

    private boolean renderChromeTooltip(GuiGraphics graphics, RadarScreenHoverProfile.Hover hover,
            boolean includeControlState, int mouseX, int mouseY) {
        if (hover.type() == RadarScreenHoverProfile.Type.NONE) {
            return false;
        }
        boolean active = hover.button() != null && active(hover.button());
        graphics.renderTooltip(font, split(RadarScreenTooltipProfile.chrome(hover, includeControlState,
                active, menu.getPower(), menu.getMaxPower(), menu.getRedPower())), mouseX, mouseY);
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
        RadarLaunchCommand command = RadarGuiTargetProfile.launchCommand(radar.getEntries(),
                radar.getBlockPos(), radar.getRange(), leftPos, topPos, lastMouseX, lastMouseY, linkSlot);
        ModMessages.sendToServer(new TileControlPacket(radar.getBlockPos(), command.toTag()));
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
            case CLOSE -> {
                onClose();
                yield true;
            }
            case LAUNCH -> {
                sendLaunchCommand(action.linkSlot());
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
        inventoryLabelX = view.inventoryLabelX();
    }

    private static Component radarName(String name) {
        return I18n.exists(name) ? Component.translatable(name) : Component.literal(name);
    }

    private static List<FormattedCharSequence> split(List<Component> tooltip) {
        return RadarScreenTooltipProfile.split(tooltip);
    }

    private static void blit(GuiGraphics graphics, ResourceLocation texture, RadarGuiRenderProfile.TextureBlit blit) {
        graphics.blit(texture, blit.x(), blit.y(), blit.u(), blit.v(), blit.width(), blit.height());
    }

}
