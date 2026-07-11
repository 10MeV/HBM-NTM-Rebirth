package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bobmazon.BobmazonOfferFactory;
import com.hbm.ntm.item.BobmazonCatalogItem;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BobmazonScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/gui_bobmazon.png");
    private static final int X_SIZE = 217;
    private static final int Y_SIZE = 229;
    private static final int OFFERS_PER_PAGE = 3;

    private final InteractionHand hand;
    private final List<BobmazonOfferFactory.Offer> offers;
    private final List<FolderButton> buttons = new ArrayList<>();

    private int leftPos;
    private int topPos;
    private int currentPage;

    public BobmazonScreen(InteractionHand hand) {
        super(Component.empty());
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        this.offers = BobmazonOfferFactory.standardOffers();
    }

    @Override
    protected void init() {
        leftPos = (width - X_SIZE) / 2;
        topPos = (height - Y_SIZE) / 2;
        updateButtons();
    }

    @Override
    public void tick() {
        if (minecraft == null || minecraft.player == null) {
            onClose();
            return;
        }
        ItemStack held = minecraft.player.getItemInHand(hand);
        if (!(held.getItem() instanceof BobmazonCatalogItem)) {
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, X_SIZE, Y_SIZE);

        for (FolderButton button : buttons) {
            button.drawButton(graphics, button.isMouseOnButton(mouseX, mouseY));
        }
        for (FolderButton button : buttons) {
            button.drawIcon(graphics);
        }
        for (int index = currentPage * OFFERS_PER_PAGE;
                index < Math.min(currentPage * OFFERS_PER_PAGE + OFFERS_PER_PAGE, offers.size());
                index++) {
            drawOfferDetails(graphics, offers.get(index), leftPos + 34,
                    topPos + 53 + 54 * index - currentPage * OFFERS_PER_PAGE * 54);
        }

        String pageText = (currentPage + 1) + "/" + (getPageCount() + 1);
        graphics.drawString(font, pageText, leftPos + X_SIZE / 2 - font.width(pageText) / 2,
                topPos + 205, 4210752, false);

        FolderButton hovered = buttonAt(mouseX, mouseY);
        if (hovered != null && hovered.info != null) {
            graphics.renderTooltip(font, Component.literal(hovered.info), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        FolderButton hovered = buttonAt((int) mouseX, (int) mouseY);
        if (hovered == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        playClick();
        hovered.execute();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE
                || Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getPageCount() {
        return Math.max(0, (offers.size() - 1) / OFFERS_PER_PAGE);
    }

    private void updateButtons() {
        currentPage = Math.max(0, Math.min(currentPage, getPageCount()));
        buttons.clear();
        for (int index = currentPage * OFFERS_PER_PAGE;
                index < Math.min(currentPage * OFFERS_PER_PAGE + OFFERS_PER_PAGE, offers.size());
                index++) {
            buttons.add(new FolderButton(leftPos + 34,
                    topPos + 35 + 54 * index - currentPage * OFFERS_PER_PAGE * 54,
                    ButtonType.OFFER, index, null));
        }
        if (currentPage != 0) {
            buttons.add(new FolderButton(leftPos + 7, topPos + 107, ButtonType.PREVIOUS, -1, "Previous"));
        }
        if (currentPage != getPageCount()) {
            buttons.add(new FolderButton(leftPos + 176, topPos + 107, ButtonType.NEXT, -1, "Next"));
        }
    }

    private FolderButton buttonAt(int mouseX, int mouseY) {
        for (FolderButton button : buttons) {
            if (button.isMouseOnButton(mouseX, mouseY)) {
                return button;
            }
        }
        return null;
    }

    private void drawOfferDetails(GuiGraphics graphics, BobmazonOfferFactory.Offer offer, int x, int y) {
        graphics.blit(TEXTURE, x + 19, y - 4, 217, 62, 39, 8);
        if (offer.displayedRatingBar() > 0) {
            graphics.blit(TEXTURE, x + 19, y - 4, 217, 54, offer.displayedRatingBar(), 8);
        }

        ItemStack stack = offer.stack();
        String count = stack.getCount() > 1 ? " x" + stack.getCount() : "";
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 0.5F);
        graphics.drawString(font, stack.getHoverName().copy().append(count),
                (x + 20) * 2, (y - 12) * 2, 4210752, false);
        graphics.pose().popPose();

        String price = offer.cost() + " Cap" + (offer.cost() == 1 ? "" : "s");
        graphics.drawString(font, price, x + 62, y - 3, 4210752, false);

        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 0.5F);
        if (!offer.author().isEmpty()) {
            graphics.drawString(font, "- " + offer.author(), (x + 20) * 2, (y + 18) * 2, 0x222222, false);
        }
        graphics.drawString(font, offer.comment(), (x + 20) * 2, (y + 8) * 2, 0x222222, false);
        graphics.pose().popPose();

        graphics.renderItem(offer.requirement().displayStack(), x + 1, y + 1);
    }

    private void playClick() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private enum ButtonType {
        OFFER,
        PREVIOUS,
        NEXT
    }

    private final class FolderButton {
        private final int x;
        private final int y;
        private final ButtonType type;
        private final int offerIndex;
        private final String info;

        private FolderButton(int x, int y, ButtonType type, int offerIndex, String info) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.offerIndex = offerIndex;
            this.info = info;
        }

        private boolean isMouseOnButton(int mouseX, int mouseY) {
            return x <= mouseX && x + 18 > mouseX && y < mouseY && y + 18 >= mouseY;
        }

        private void drawButton(GuiGraphics graphics, boolean hovered) {
            int u = hovered ? X_SIZE + 18 : X_SIZE;
            int v = switch (type) {
                case PREVIOUS -> 18;
                case NEXT -> 36;
                default -> 0;
            };
            graphics.blit(TEXTURE, x, y, u, v, 18, 18);
        }

        private void drawIcon(GuiGraphics graphics) {
            if (type == ButtonType.OFFER && offerIndex >= 0 && offerIndex < offers.size()) {
                graphics.renderItem(offers.get(offerIndex).stack(), x + 1, y + 1);
            }
        }

        private void execute() {
            switch (type) {
                case OFFER -> ModMessages.sendBobmazonOffer(hand, offerIndex);
                case PREVIOUS -> {
                    if (currentPage > 0) {
                        currentPage--;
                        updateButtons();
                    }
                }
                case NEXT -> {
                    if (currentPage < getPageCount()) {
                        currentPage++;
                        updateButtons();
                    }
                }
            }
        }
    }
}
