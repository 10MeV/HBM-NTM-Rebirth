package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.PneumaticStorageAccessMenu;
import com.hbm.ntm.network.ModMessages;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;

/** Client controls for the legacy 8-by-6 pneumatic cache projection. */
public class PneumaticStorageAccessScreen extends AbstractContainerScreen<PneumaticStorageAccessMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/storage/gui_pneumatic_access.png");
    private EditBox search;
    private int page;
    private int sorting;
    private boolean detailedSearch;
    private boolean startFocused;

    public PneumaticStorageAccessScreen(PneumaticStorageAccessMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 210;
        imageHeight = 251;
        inventoryLabelX = 42;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        page = menu.getPage();
        sorting = menu.getSortMode().ordinal();
        detailedSearch = menu.isDetailedSearch();
        search = new EditBox(font, leftPos + 79, topPos + 127, 86, 12, Component.empty());
        search.setTextColor(0xFFFFFFFF);
        search.setTextColorUneditable(0xFFA0A0A0);
        search.setBordered(false);
        search.setMaxLength(50);
        search.setResponder(this::sendSearch);
        search.setValue(menu.getSearch());
        search.setFocused(startFocused);
        addRenderableWidget(search);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos + 34, topPos, 0, 0, 176, imageHeight, 256, 256);
        graphics.blit(TEXTURE, leftPos, topPos, 176, 15, 32, 122, 256, 256);
        graphics.blit(TEXTURE, leftPos + 7, topPos + 7 + sorting * 18, 208, 0, 18, 18, 256, 256);
        if (startFocused) graphics.blit(TEXTURE, leftPos + 7, topPos + 79, 208, 18, 18, 18, 256, 256);
        if (detailedSearch) graphics.blit(TEXTURE, leftPos + 7, topPos + 97, 208, 18, 18, 18, 256, 256);
        int scrollBounds = Math.max(1, menu.getPageLimit());
        int scrollY = 17 + (int) ((double) page / scrollBounds * 91.0D);
        graphics.blit(TEXTURE, leftPos + 188, topPos + scrollY, 176, 0, 12, 15, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 122 - font.width(title) / 2, 5, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(0.5F, 0.5F, 1.0F);
        for (int index = 0; index < PneumaticStorageAccessMenu.CACHE_SLOT_COUNT; index++) {
            Slot slot = menu.slots.get(index);
            if (!slot.hasItem() || slot.getItem().getTag() == null) continue;
            String amount = Long.toString(slot.getItem().getTag().getLong(PneumaticStorageAccessMenu.CACHE_AMOUNT_TAG));
            int x = (slot.x + 16) * 2 - font.width(amount);
            int y = (slot.y + 16) * 2 - font.lineHeight;
            graphics.drawString(font, amount, x, y, 0xFFFFFFFF, true);
        }
        pose.popPose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Page and upper bound are DataSlots, so server-side cache changes (or a clamp after a
        // search) are reflected before the scroll thumb is drawn.
        page = menu.getPage();
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!renderCacheTooltip(graphics, mouseX, mouseY)) renderTooltip(graphics, mouseX, mouseY);
        renderControlTooltips(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cacheSlot = cacheSlotAt(mouseX, mouseY);
        if (cacheSlot >= 0 && button <= 1) {
            int action = button == 0 && hasShiftDown() ? PneumaticStorageAccessMenu.ACTION_CACHE_SHIFT
                    : button == 0 ? PneumaticStorageAccessMenu.ACTION_CACHE_LEFT
                    : PneumaticStorageAccessMenu.ACTION_CACHE_RIGHT;
            ModMessages.sendMenuAction(action, cacheSlot);
            playClick();
            return true;
        }
        if (button == 0 && legacyHover(mouseX, mouseY, 7, 7, 18, 18)) return setSort(0);
        if (button == 0 && legacyHover(mouseX, mouseY, 7, 25, 18, 18)) return setSort(1);
        if (button == 0 && legacyHover(mouseX, mouseY, 7, 43, 18, 18)) return setSort(2);
        if (button == 0 && legacyHover(mouseX, mouseY, 7, 61, 18, 18)) return setSort(3);
        if (button == 0 && legacyHover(mouseX, mouseY, 7, 79, 18, 18)) {
            startFocused = !startFocused;
            if (startFocused) search.setFocused(true);
            playClick();
            return true;
        }
        if (button == 0 && legacyHover(mouseX, mouseY, 7, 97, 18, 18)) {
            detailedSearch = !detailedSearch;
            ModMessages.sendMenuAction(PneumaticStorageAccessMenu.ACTION_TOGGLE_DETAILED_SEARCH);
            playClick();
            return true;
        }
        if (button == 0 && legacyHover(mouseX, mouseY, 187, 16, 14, 108)) {
            int limit = Math.max(1, menu.getPageLimit());
            page = Math.max(0, Math.min(limit, (int) Math.round((mouseY - topPos - 24.0D) / 92.0D * limit)));
            ModMessages.sendMenuAction(PneumaticStorageAccessMenu.ACTION_SET_PAGE, page);
            playClick();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (legacyHover(mouseX, mouseY, 0, 0, imageWidth, imageHeight)) {
            int next = Math.max(0, Math.min(Math.max(1, menu.getPageLimit()), page + (delta < 0.0D ? 1 : -1)));
            if (next != page) {
                page = next;
                ModMessages.sendMenuAction(PneumaticStorageAccessMenu.ACTION_SET_PAGE, page);
                playClick();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return search != null && search.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return search != null && search.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    private boolean setSort(int value) {
        sorting = value;
        page = 0;
        ModMessages.sendMenuAction(PneumaticStorageAccessMenu.ACTION_SET_SORT, value);
        playClick();
        return true;
    }

    private void sendSearch(String value) {
        page = 0;
        CompoundTag data = new CompoundTag();
        data.putString("search", value);
        ModMessages.sendMenuAction(PneumaticStorageAccessMenu.ACTION_SET_SEARCH, 0, data);
    }

    private int cacheSlotAt(double mouseX, double mouseY) {
        for (int index = 0; index < PneumaticStorageAccessMenu.CACHE_SLOT_COUNT; index++) {
            Slot slot = menu.slots.get(index);
            if (legacyHover(mouseX, mouseY, slot.x, slot.y, 16, 16)) return index;
        }
        return -1;
    }

    private boolean renderCacheTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int index = 0; index < PneumaticStorageAccessMenu.CACHE_SLOT_COUNT; index++) {
            Slot slot = menu.slots.get(index);
            if (!legacyHover(mouseX, mouseY, slot.x, slot.y, 16, 16) || !slot.hasItem() || slot.getItem().getTag() == null) continue;
            long amount = slot.getItem().getTag().getLong(PneumaticStorageAccessMenu.CACHE_AMOUNT_TAG);
            int stacks = slot.getItem().getTag().getInt(PneumaticStorageAccessMenu.CACHE_STACKS_TAG);
            graphics.renderComponentTooltip(font, List.of(slot.getItem().getHoverName(), Component.literal("x" + amount),
                    Component.literal("in " + stacks + " stacks")), mouseX, mouseY);
            return true;
        }
        return false;
    }

    private void renderControlTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        String tooltip = null;
        if (legacyHover(mouseX, mouseY, 7, 7, 18, 18)) tooltip = "Sorting: " + ChatFormatting.YELLOW + "Amount";
        else if (legacyHover(mouseX, mouseY, 7, 25, 18, 18)) tooltip = "Sorting: " + ChatFormatting.YELLOW + "Item ID";
        else if (legacyHover(mouseX, mouseY, 7, 43, 18, 18)) tooltip = "Sorting: " + ChatFormatting.YELLOW + "Name";
        else if (legacyHover(mouseX, mouseY, 7, 61, 18, 18)) tooltip = "Sorting: " + ChatFormatting.YELLOW + "Internal Name";
        else if (legacyHover(mouseX, mouseY, 7, 79, 18, 18)) tooltip = "Focus search by default: " + (startFocused ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        else if (legacyHover(mouseX, mouseY, 7, 97, 18, 18)) tooltip = "Include tooltips in search: " + (detailedSearch ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        if (tooltip != null) graphics.renderTooltip(font, Component.literal(tooltip), mouseX, mouseY);
    }

    private boolean legacyHover(double mouseX, double mouseY, int x, int y, int width, int height) {
        return leftPos + x <= mouseX && leftPos + x + width > mouseX && topPos + y < mouseY && topPos + y + height >= mouseY;
    }

    private void playClick() {
        if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
