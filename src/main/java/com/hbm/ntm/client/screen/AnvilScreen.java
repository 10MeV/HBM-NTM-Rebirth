package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.menu.AnvilMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.recipe.AnvilConstructionRecipe;
import com.hbm.ntm.recipe.AnvilConstructionRecipeRuntime;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AnvilScreen extends AbstractContainerScreen<AnvilMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/processing/gui_anvil.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int VISIBLE_RECIPE_COUNT = 10;

    private EditBox search;
    private List<AnvilConstructionRecipe> recipes = List.of();
    private int pageIndex;
    private int maxPageIndex;
    private int selection = -1;
    private int lastInfoWidth = 1;

    public AnvilScreen(AnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        titleLabelX = 61;
        titleLabelY = 8;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        search = new EditBox(font, leftPos + 10, topPos + 111, 84, 12, Component.empty());
        search.setTextColor(0xFFFFFFFF);
        search.setTextColorUneditable(0xFFFFFFFF);
        search.setBordered(false);
        search.setMaxLength(25);
        search.setResponder(value -> regenerateRecipes());
        addRenderableWidget(search);
        regenerateRecipes();
    }

    private void regenerateRecipes() {
        if (minecraft == null || minecraft.level == null) {
            recipes = List.of();
        } else {
            recipes = AnvilConstructionRecipeRuntime.search(minecraft.level, menu.tier(),
                    search == null ? "" : search.getValue());
        }
        pageIndex = 0;
        selection = -1;
        maxPageIndex = Math.max(0, (int) Math.ceil((recipes.size() - VISIBLE_RECIPE_COUNT) / 2.0D));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderInfoPanelExtension(graphics);
        if (search != null && search.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 8, topPos + 108, 168, 222, 88, 16, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        renderButtonHovers(graphics, mouseX, mouseY);
        renderRecipes(graphics);
    }

    private void renderInfoPanelExtension(GuiGraphics graphics) {
        int slide = Math.max(0, lastInfoWidth - 42);
        int mul = 1;
        while (slide >= 51 * mul) {
            graphics.blit(TEXTURE, leftPos + 125 + 51 * mul, topPos + 17, 125, 17, 54, 108);
            mul++;
        }
        graphics.blit(TEXTURE, leftPos + 125 + slide, topPos + 17, 125, 17, 54, 108);
    }

    private void renderButtonHovers(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isLegacyHover(mouseX, mouseY, 7, 71, 9, 36)) {
            graphics.blit(TEXTURE, leftPos + 7, topPos + 71, 176, 186, 9, 36);
        }
        if (isLegacyHover(mouseX, mouseY, 106, 71, 9, 36)) {
            graphics.blit(TEXTURE, leftPos + 106, topPos + 71, 185, 186, 9, 36);
        }
        if (isLegacyHover(mouseX, mouseY, 52, 53, 18, 18)) {
            graphics.blit(TEXTURE, leftPos + 52, topPos + 53, 176, 150, 18, 18);
        }
    }

    private void renderRecipes(GuiGraphics graphics) {
        int first = pageIndex * 2;
        for (int i = first; i < first + VISIBLE_RECIPE_COUNT && i < recipes.size(); i++) {
            int local = i - first;
            int x = leftPos + 16 + 18 * (local / 2);
            int y = topPos + 71 + 18 * (local % 2);
            AnvilConstructionRecipe recipe = recipes.get(i);
            ItemStack display = recipe.displayStack();
            graphics.renderItem(display, x + 1, y + 1);
            graphics.renderItemDecorations(font, display, x + 1, y + 1);
            graphics.blit(TEXTURE, x, y, 18 + 18 * recipe.overlay().ordinal(), 222, 18, 18,
                    TEXTURE_SIZE, TEXTURE_SIZE);
            if (selection == i) {
                graphics.blit(TEXTURE, x, y, 0, 222, 18, 18, TEXTURE_SIZE, TEXTURE_SIZE);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = 61 - font.width(title) / 2;
        graphics.drawString(font, title, titleX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        renderSelectedRecipeInfo(graphics);
    }

    private void renderSelectedRecipeInfo(GuiGraphics graphics) {
        if (selection < 0 || selection >= recipes.size()) {
            lastInfoWidth = 0;
            return;
        }
        List<InfoLine> lines = recipeInfo(recipes.get(selection));
        int longest = lines.stream().mapToInt(line -> font.width(line.text())).max().orElse(0);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(0.5F, 0.5F, 1.0F);
        int y = 50;
        for (InfoLine line : lines) {
            graphics.drawString(font, line.text(), 260, y, line.color(), false);
            y += 9;
        }
        pose.popPose();
        lastInfoWidth = longest / 2;
    }

    private List<InfoLine> recipeInfo(AnvilConstructionRecipe recipe) {
        List<InfoLine> lines = new ArrayList<>();
        lines.add(new InfoLine(ChatFormatting.YELLOW + "Inputs:", 0xFFFFFF));
        for (HbmIngredient input : recipe.inputs()) {
            ItemStack display = input.displayStacks().stream().findFirst().orElse(ItemStack.EMPTY);
            String name = display.isEmpty() ? input.diagnosticName() : display.getHoverName().getString();
            boolean enough = hasInput(input);
            lines.add(new InfoLine(">" + input.count() + "x " + name, enough ? 0xFFFFFF : 0xFF5555));
        }
        lines.add(new InfoLine("", 0xFFFFFF));
        lines.add(new InfoLine(ChatFormatting.YELLOW + "Outputs:", 0xFFFFFF));
        for (HbmItemOutput output : recipe.outputs()) {
            output.displayLabels().forEach(label -> lines.add(new InfoLine(">" + label, 0xFFFFFF)));
        }
        return lines;
    }

    private boolean hasInput(HbmIngredient input) {
        int amount = 0;
        for (int i = 0; i < menu.playerInventory().getContainerSize(); i++) {
            ItemStack stack = menu.playerInventory().getItem(i);
            if (!stack.isEmpty() && input.test(stack, true)) {
                amount += stack.getCount();
                if (amount >= input.count()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isLegacyHover(mouseX, mouseY, 7, 71, 9, 36)) {
                playClick();
                if (pageIndex > 0) {
                    pageIndex--;
                }
                return true;
            }
            if (isLegacyHover(mouseX, mouseY, 106, 71, 9, 36)) {
                playClick();
                if (pageIndex < maxPageIndex) {
                    pageIndex++;
                }
                return true;
            }
            if (isLegacyHover(mouseX, mouseY, 52, 53, 18, 18)) {
                if (selection >= 0 && selection < recipes.size() && minecraft != null && minecraft.level != null) {
                    int index = AnvilConstructionRecipeRuntime.recipeIndex(minecraft.level, menu.tier(),
                            recipes.get(selection));
                    if (index >= 0) {
                        playClick();
                        ModMessages.sendAnvilCraftAction(index, Screen.hasShiftDown() ? 1 : 0);
                    }
                }
                return true;
            }
            int selectedRecipe = recipeAt(mouseX, mouseY);
            if (selectedRecipe >= 0) {
                selection = selection == selectedRecipe ? -1 : selectedRecipe;
                playClick();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isMouseInsideGui(mouseX, mouseY) && hoveredSlot == null) {
            if (delta > 0.0D && pageIndex > 0) {
                pageIndex--;
                return true;
            }
            if (delta < 0.0D && pageIndex < maxPageIndex) {
                pageIndex++;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (search != null && search.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (search != null && search.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private int recipeAt(double mouseX, double mouseY) {
        int first = pageIndex * 2;
        for (int i = first; i < first + VISIBLE_RECIPE_COUNT && i < recipes.size(); i++) {
            int local = i - first;
            int x = 16 + 18 * (local / 2);
            int y = 71 + 18 * (local % 2);
            if (isLegacyHover(mouseX, mouseY, x, y, 18, 18)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isMouseInsideGui(double mouseX, double mouseY) {
        return leftPos <= mouseX && leftPos + imageWidth > mouseX && topPos < mouseY && topPos + imageHeight >= mouseY;
    }

    private boolean isLegacyHover(double mouseX, double mouseY, int x, int y, int width, int height) {
        return leftPos + x <= mouseX && leftPos + x + width > mouseX
                && topPos + y < mouseY && topPos + y + height >= mouseY;
    }

    private void playClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private record InfoLine(String text, int color) {
    }
}
