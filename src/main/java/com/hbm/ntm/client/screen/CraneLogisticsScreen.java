package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity;
import com.hbm.ntm.menu.CraneLogisticsMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class CraneLogisticsScreen extends AbstractContainerScreen<CraneLogisticsMenu> {
    private static final ResourceLocation EXTRACTOR = texture("gui_crane_ejector");
    private static final ResourceLocation INSERTER = texture("gui_crane_inserter");
    private static final ResourceLocation GRABBER = texture("gui_crane_grabber");
    private static final ResourceLocation ROUTER = texture("gui_crane_router");
    private static final ResourceLocation BOXER = texture("gui_crane_boxer");
    private static final ResourceLocation UNBOXER = texture("gui_crane_unboxer");
    private static final ResourceLocation PARTITIONER = texture("gui_crane_router");

    public CraneLogisticsScreen(CraneLogisticsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        CraneLogisticsBlockEntity.Kind kind = menu.kind();
        this.imageWidth = switch (kind) {
            case EXTRACTOR -> 212;
            case ROUTER -> 256;
            default -> 176;
        };
        this.imageHeight = switch (kind) {
            case ROUTER -> 201;
            case PARTITIONER -> 303;
            default -> 185;
        };
        this.inventoryLabelX = switch (kind) {
            case EXTRACTOR -> 26;
            case ROUTER -> 47;
            default -> 8;
        };
        this.inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        CraneLogisticsBlockEntity.Kind kind = menu.kind();
        if (kind == CraneLogisticsBlockEntity.Kind.PARTITIONER) {
            graphics.blit(PARTITIONER, leftPos, topPos, 40, 0, 176, 93);
            graphics.blit(PARTITIONER, leftPos, topPos + 102, 40, 0, 176, 93);
            graphics.blit(PARTITIONER, leftPos, topPos + 194, 40, 93, 176, 108);
            return;
        }

        ResourceLocation texture = textureFor(kind);
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        CraneLogisticsBlockEntity crane = menu.getBlockEntity();
        if (kind == CraneLogisticsBlockEntity.Kind.EXTRACTOR) {
            if (crane.isMaxEject()) {
                graphics.blit(texture, leftPos + 187, topPos + 34, 212, 0, 18, 18);
            }
            graphics.blit(texture, leftPos + 139, topPos + (crane.isWhitelist() ? 33 : 47), 212, 18, 3, 6);
        } else if (kind == CraneLogisticsBlockEntity.Kind.GRABBER) {
            graphics.blit(texture, leftPos + 108, topPos + (crane.isWhitelist() ? 33 : 47), 176, 0, 3, 6);
        } else if (kind == CraneLogisticsBlockEntity.Kind.ROUTER) {
            graphics.blit(texture, leftPos, topPos, 0, 0, 256, 93);
            graphics.blit(texture, leftPos + 39, topPos + 93, 39, 93, 176, 108);
            for (int group = 0; group < 2; group++) {
                for (int row = 0; row < 3; row++) {
                    int index = group * 3 + row;
                    graphics.blit(texture, leftPos + 7 + group * 222, topPos + 16 + row * 26,
                            238, 93 + crane.getRouterMode(index) * 18, 18, 18);
                }
            }
        } else if (kind == CraneLogisticsBlockEntity.Kind.BOXER) {
            graphics.blit(texture, leftPos + 151, topPos + 34, 176, crane.getMode() * 18, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        CraneLogisticsBlockEntity.Kind kind = menu.kind();
        if (kind == CraneLogisticsBlockEntity.Kind.EXTRACTOR
                && isHovering(187, 34, 18, 18, mouseX, mouseY)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal(
                    "Only take maximum possible: " + (menu.getBlockEntity().isMaxEject() ? "ON" : "OFF"))),
                    mouseX, mouseY);
        } else if (kind == CraneLogisticsBlockEntity.Kind.ROUTER) {
            renderRouterTooltips(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        CraneLogisticsBlockEntity.Kind kind = menu.kind();
        if (kind == CraneLogisticsBlockEntity.Kind.EXTRACTOR) {
            if (sendIfHovered(187, 34, 18, 18, mouseX, mouseY, "maxEject")) {
                return true;
            }
            if (sendIfHovered(128, 30, 14, 26, mouseX, mouseY, "whitelist")) {
                return true;
            }
        } else if (kind == CraneLogisticsBlockEntity.Kind.GRABBER) {
            if (sendIfHovered(97, 30, 14, 26, mouseX, mouseY, "whitelist")) {
                return true;
            }
        } else if (kind == CraneLogisticsBlockEntity.Kind.BOXER) {
            if (sendIfHovered(151, 34, 18, 18, mouseX, mouseY, "toggle")) {
                return true;
            }
        } else if (kind == CraneLogisticsBlockEntity.Kind.ROUTER) {
            for (int group = 0; group < 2; group++) {
                for (int row = 0; row < 3; row++) {
                    if (isHovering(7 + group * 222, 16 + row * 26, 18, 18, mouseX, mouseY)) {
                        playButtonClick();
                        CompoundTag tag = new CompoundTag();
                        tag.putInt("toggle", group * 3 + row);
                        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
                        return true;
                    }
                }
            }
        }
        return handled;
    }

    private void renderRouterTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int group = 0; group < 2; group++) {
            for (int row = 0; row < 3; row++) {
                if (isHovering(7 + group * 222, 16 + row * 26, 18, 18, mouseX, mouseY)) {
                    int mode = menu.getBlockEntity().getRouterMode(group * 3 + row);
                    graphics.renderComponentTooltip(font, switch (mode) {
                        case 1 -> List.of(Component.literal("WHITELIST"),
                                Component.literal("Route if filter matches"));
                        case 2 -> List.of(Component.literal("BLACKLIST"),
                                Component.literal("Route if filter doesn't match"));
                        case 3 -> List.of(Component.literal("WILDCARD"),
                                Component.literal("Route if no other route is valid"));
                        default -> List.of(Component.literal("OFF"));
                    }, mouseX, mouseY);
                }
            }
        }
    }

    private boolean sendIfHovered(int x, int y, int width, int height, double mouseX, double mouseY, String key) {
        if (!isHovering(x, y, width, height, mouseX, mouseY)) {
            return false;
        }
        playButtonClick();
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(key, true);
        ModMessages.sendToServer(new TileControlPacket(menu.getBlockEntity().getBlockPos(), tag));
        return true;
    }

    private void playButtonClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/storage/" + name + ".png");
    }

    private static ResourceLocation textureFor(CraneLogisticsBlockEntity.Kind kind) {
        return switch (kind) {
            case EXTRACTOR -> EXTRACTOR;
            case INSERTER -> INSERTER;
            case GRABBER -> GRABBER;
            case ROUTER -> ROUTER;
            case BOXER -> BOXER;
            case UNBOXER -> UNBOXER;
            case PARTITIONER -> PARTITIONER;
        };
    }
}
