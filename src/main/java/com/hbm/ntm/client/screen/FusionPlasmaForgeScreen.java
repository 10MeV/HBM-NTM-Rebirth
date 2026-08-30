package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.FusionPlasmaForgeBlockEntity;
import com.hbm.ntm.menu.FusionPlasmaForgeMenu;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeExtraData;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.BobMathUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FusionPlasmaForgeScreen extends AbstractContainerScreen<FusionPlasmaForgeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/reactors/gui_fusion_plasmaforge.png");
    private static final List<String> BOOSTER_ITEMS = List.of(
            "nugget_co60", "billet_co60", "ingot_co60", "powder_co60",
            "nugget_sr90", "powder_sr90_tiny", "billet_sr90", "ingot_sr90", "powder_sr90",
            "nugget_au198", "billet_au198", "ingot_au198", "powder_au198",
            "powder_i131_tiny", "powder_i131", "powder_xe135_tiny", "powder_xe135",
            "powder_cs137_tiny", "powder_cs137", "powder_at209");

    public FusionPlasmaForgeScreen(FusionPlasmaForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 244;
        inventoryLabelX = 8;
        inventoryLabelY = 150;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int power = menu.getPowerBarHeight(62);
        graphics.blit(TEXTURE, leftPos + 152, topPos + 80 - power, 176, 62 - power, 16, power);
        int progress = menu.getProgressWidth(70);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 62, topPos + 81, 176, 62, progress, 16);
        }
        GenericMachineRecipe recipe = selectedRecipe();
        if (menu.getBlockEntity().didProcess()) {
            graphics.blit(TEXTURE, leftPos + 51, topPos + 76, 195, 0, 3, 6);
            graphics.blit(TEXTURE, leftPos + 56, topPos + 76, 195, 0, 3, 6);
        } else if (recipe != null) {
            graphics.blit(TEXTURE, leftPos + 51, topPos + 76, 192, 0, 3, 6);
            if (menu.getPower() >= recipe.getPower()) {
                graphics.blit(TEXTURE, leftPos + 56, topPos + 76, 192, 0, 3, 6);
            }
        }
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 34, topPos + 124,
                plasmaInputGauge(recipe), 5, 2, 1, 0xA00000);
        LegacyGuiElements.drawSmoothGauge(graphics, leftPos + 70, topPos + 124,
                menu.getMaxBooster() <= 0 ? 0.0D : menu.getBooster() / (double) menu.getMaxBooster(),
                5, 2, 1, 0xA00000);
        graphics.renderItem(recipe == null ? LegacyGuiElements.templateFolderStack() : recipe.getIcon(),
                leftPos + 8, topPos + 81);
        LegacyRecipeGhostRenderer.renderItemInputGhosts(graphics, minecraft, menu, TEXTURE, leftPos, topPos,
                recipe, new int[] {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14});
        LegacyFluidGuiRenderer.renderVerticalTank(graphics, leftPos + 80, topPos + 70, 16, 52, menu.getInputTank());
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 70 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isLegacyHovering(7, 80, 18, 18, mouseX, mouseY)) {
            GenericMachineRecipe recipe = selectedRecipe();
            if (recipe != null) {
                LegacyGuiElements.renderRecipeTooltip(graphics, font, recipe.getDisplayLines(), mouseX, mouseY);
            } else {
                graphics.renderTooltip(font,
                        Component.translatableWithFallback("gui.recipe.setRecipe", "Select recipe")
                                .withStyle(ChatFormatting.YELLOW), mouseX, mouseY);
            }
        } else if (isLegacyHovering(80, 18, 16, 52, mouseX, mouseY)) {
            LegacyGuiElements.renderFluidTooltip(graphics, font, menu.getInputTank(),
                    menu.getInputTank().tooltip(hasShiftDown()), mouseX, mouseY);
        } else if (isLegacyHovering(152, 18, 16, 62, mouseX, mouseY)) {
            LegacyGuiElements.renderElectricityTooltip(graphics, font, mouseX, mouseY,
                    leftPos + 152, topPos + 18, 16, 62, menu.getPower(), menu.getMaxPower());
        } else if (isLegacyHovering(25, 115, 18, 18, mouseX, mouseY)) {
            renderIgnitionTooltip(graphics, mouseX, mouseY);
        } else if (isHovering(98, 116, 16, 16, mouseX, mouseY)
                && menu.getSlot(FusionPlasmaForgeBlockEntity.SLOT_BOOSTER).getItem().isEmpty()
                && menu.getCarried().isEmpty()) {
            renderBoosterTooltip(graphics, mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, 7, 80, 18, 18)) {
            minecraft.setScreen(new FusionPlasmaForgeRecipeSelectorScreen(this));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static String shortNumber(long value) {
        return BobMathUtil.getShortNumber(value);
    }

    private void renderIgnitionTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        GenericMachineRecipe recipe = selectedRecipe();
        Component line;
        if (recipe == null) {
            line = Component.literal("0TU / 0TU");
        } else {
            long ignition = recipe.getExtraData().plasmaForge()
                    .map(GenericMachineRecipeExtraData.PlasmaForge::ignitionTemp).orElse(0L);
            line = Component.empty()
                    .append(Component.literal("-> ").withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(shortNumber(menu.getPlasmaEnergy()) + "TU / "
                            + shortNumber(ignition) + "TU"));
        }
        LegacyGuiElements.renderTooltip(graphics, font, List.of(line), mouseX, mouseY);
    }

    private void renderBoosterTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ItemStack> boosters = boosterStacks();
        if (boosters.isEmpty()) {
            return;
        }
        int selectedIndex = 0;
        Component selectedName = boosters.get(0).getHoverName();
        if (boosters.size() > 1) {
            selectedIndex = (int) ((System.currentTimeMillis() % (1000L * boosters.size())) / 1000L);
            selectedName = boosters.get(selectedIndex).getHoverName();
            ItemStack selected = boosters.get(selectedIndex).copy();
            selected.setCount(0);
            boosters.set(selectedIndex, selected);
        }

        List<List<LegacyGuiElements.StackTextPart>> lines = new ArrayList<>();
        lines.add(List.of(LegacyGuiElements.StackTextPart.text("Booster Isotope:")));
        if (boosters.size() < 10) {
            lines.add(stackLine(boosters));
        } else if (boosters.size() < 24) {
            lines.add(stackLine(boosters.subList(0, boosters.size() / 2)));
            lines.add(stackLine(boosters.subList(boosters.size() / 2, boosters.size())));
        } else {
            int bound0 = (int) Math.ceil(boosters.size() / 3.0D);
            int bound1 = (int) Math.ceil(boosters.size() / 3.0D * 2.0D);
            lines.add(stackLine(boosters.subList(0, bound0)));
            lines.add(stackLine(boosters.subList(bound0, bound1)));
            lines.add(stackLine(boosters.subList(bound1, boosters.size())));
        }
        lines.add(List.of(LegacyGuiElements.StackTextPart.text(selectedName)));
        LegacyGuiElements.renderStackText(graphics, font, lines, mouseX, mouseY);
    }

    private static List<ItemStack> boosterStacks() {
        List<ItemStack> result = new ArrayList<>(BOOSTER_ITEMS.size());
        for (String name : BOOSTER_ITEMS) {
            var item = ModItems.legacyItem(name);
            if (item != null) {
                result.add(new ItemStack(item.get()));
            }
        }
        return result;
    }

    private static List<LegacyGuiElements.StackTextPart> stackLine(List<ItemStack> stacks) {
        List<LegacyGuiElements.StackTextPart> parts = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            parts.add(LegacyGuiElements.StackTextPart.stack(stack));
        }
        return parts;
    }

    private boolean isLegacyHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return LegacyGuiElements.checkClick(mouseX, mouseY, leftPos, topPos, x, y, width, height);
    }

    private GenericMachineRecipe selectedRecipe() {
        return menu.getBlockEntity().getSelectedRecipeDefinition();
    }

    private double plasmaInputGauge(GenericMachineRecipe recipe) {
        if (recipe == null) {
            return 0.0D;
        }
        long ignition = recipe.getExtraData().plasmaForge()
                .map(GenericMachineRecipeExtraData.PlasmaForge::ignitionTemp)
                .orElse(0L);
        return ignition <= 0L ? 0.0D : Math.min(menu.getPlasmaEnergy() / (double) ignition, 1.5D) / 1.5D;
    }
}
