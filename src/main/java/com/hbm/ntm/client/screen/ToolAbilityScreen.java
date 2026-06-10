package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.ability.AvailableAbilities;
import com.hbm.ntm.ability.IBaseAbility;
import com.hbm.ntm.ability.IToolAreaAbility;
import com.hbm.ntm.ability.IToolHarvestAbility;
import com.hbm.ntm.ability.ToolAbilityConfiguration;
import com.hbm.ntm.ability.ToolAreaAbilities;
import com.hbm.ntm.ability.ToolHarvestAbilities;
import com.hbm.ntm.ability.ToolPreset;
import com.hbm.ntm.item.HbmAbilityToolItem;
import com.hbm.ntm.menu.ToolAbilityMenu;
import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModSounds;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

public class ToolAbilityScreen extends AbstractContainerScreen<ToolAbilityMenu> {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/tool/gui_tool_ability.png");

    private static final int MAX_PRESETS = 99;
    private static final int BASE_WIDTH = 186;
    private static final int WINDOW_HEIGHT = 76;
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final List<AbilityInfo<IToolAreaAbility>> AREA_ABILITIES = List.of(
            new AbilityInfo<>(ToolAreaAbilities.NONE, 0, 91),
            new AbilityInfo<>(ToolAreaAbilities.RECURSION, 32, 91),
            new AbilityInfo<>(ToolAreaAbilities.HAMMER, 64, 91),
            new AbilityInfo<>(ToolAreaAbilities.HAMMER_FLAT, 96, 91),
            new AbilityInfo<>(ToolAreaAbilities.EXPLOSION, 128, 91));
    private static final List<AbilityInfo<IToolHarvestAbility>> HARVEST_ABILITIES = List.of(
            new AbilityInfo<>(ToolHarvestAbilities.NONE, 0, 107),
            new AbilityInfo<>(ToolHarvestAbilities.SILK, 32, 107),
            new AbilityInfo<>(ToolHarvestAbilities.LUCK, 64, 107),
            new AbilityInfo<>(ToolHarvestAbilities.SMELTER, 96, 107),
            new AbilityInfo<>(ToolHarvestAbilities.SHREDDER, 128, 107),
            new AbilityInfo<>(ToolHarvestAbilities.CENTRIFUGE, 160, 107),
            new AbilityInfo<>(ToolHarvestAbilities.CRYSTALLIZER, 192, 107),
            new AbilityInfo<>(ToolHarvestAbilities.MERCURY, 224, 107));

    private final int insetWidth;
    private ToolAbilityConfiguration configuration;
    private AvailableAbilities availableAbilities;
    private boolean saved;
    private int hoverIdxArea = -1;
    private int hoverIdxHarvest = -1;
    private int hoverIdxExtraButton = -1;

    public ToolAbilityScreen(ToolAbilityMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        insetWidth = 20 * Math.max(AREA_ABILITIES.size() - 4, HARVEST_ABILITIES.size() - 8);
        imageWidth = BASE_WIDTH + insetWidth;
        imageHeight = WINDOW_HEIGHT;
        inventoryLabelY = 10_000;
        titleLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        availableAbilities = menu.getAvailableAbilities();
        configuration = menu.getConfiguration();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawStretchedRect(graphics, leftPos, topPos, 0, 0, imageWidth, BASE_WIDTH, imageHeight, 74, 87);

        ToolPreset activePreset = configuration.getActivePreset();
        hoverIdxArea = drawSwitches(graphics, AREA_ABILITIES, activePreset.areaAbility, activePreset.areaAbilityLevel,
                leftPos + 15, topPos + 25, mouseX, mouseY);
        hoverIdxHarvest = drawSwitches(graphics, HARVEST_ABILITIES, activePreset.harvestAbility,
                activePreset.harvestAbilityLevel, leftPos + 15, topPos + 45, mouseX, mouseY);

        drawNumber(graphics, configuration.currentPreset() + 1, leftPos + insetWidth + 115, topPos + 25);
        drawNumber(graphics, configuration.presets().size(), leftPos + insetWidth + 149, topPos + 25);

        int extraButtonsX = leftPos + imageWidth - 86;
        hoverIdxExtraButton = -1;
        for (int i = 0; i < 7; i++) {
            int x = extraButtonsX + i * 11;
            if (isInAabb(mouseX, mouseY, x, topPos + 11, 9, 9)) {
                hoverIdxExtraButton = i;
                blit(graphics, x, topPos + 11, 193 + i * 9, 0, 9, 9);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderHoverTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ToolPreset activePreset = configuration.getActivePreset();

        Selection<IToolAreaAbility> area = handleSwitchesClicked(
                AREA_ABILITIES, activePreset.areaAbility, activePreset.areaAbilityLevel, hoverIdxArea);
        activePreset.areaAbility = area.ability();
        activePreset.areaAbilityLevel = area.level();

        Selection<IToolHarvestAbility> harvest = handleSwitchesClicked(
                HARVEST_ABILITIES, activePreset.harvestAbility, activePreset.harvestAbilityLevel, hoverIdxHarvest);
        activePreset.harvestAbility = harvest.ability();
        activePreset.harvestAbilityLevel = harvest.level();

        if (!activePreset.areaAbility.allowsHarvest(activePreset.areaAbilityLevel)) {
            activePreset.harvestAbility = ToolHarvestAbilities.NONE;
            activePreset.harvestAbilityLevel = 0;
        }

        if (hoverIdxExtraButton != -1) {
            switch (hoverIdxExtraButton) {
                case 0 -> configuration.reset(availableAbilities);
                case 1 -> deletePreset();
                case 2 -> addPreset();
                case 3 -> configuration.setCurrentPreset(0);
                case 4 -> nextPreset(false);
                case 5 -> previousPreset(false);
                case 6 -> onClose();
                default -> {
                }
            }
            playClick(0.5F);
            return true;
        }

        if (!isInAabb((int) mouseX, (int) mouseY, leftPos, topPos, imageWidth, imageHeight)) {
            onClose();
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta < 0.0D) {
            previousPreset(true);
            return true;
        }
        if (delta > 0.0D) {
            nextPreset(true);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        saveConfiguration();
        super.onClose();
    }

    @Override
    public void removed() {
        saveConfiguration();
        super.removed();
    }

    private <T extends IBaseAbility> int drawSwitches(GuiGraphics graphics, List<AbilityInfo<T>> abilities,
            T selectedAbility, int selectedLevel, int x, int y, int mouseX, int mouseY) {
        int hoverIdx = -1;
        for (int i = 0; i < abilities.size(); i++) {
            AbilityInfo<T> info = abilities.get(i);
            boolean available = abilityAvailable(info.ability());
            boolean selected = info.ability() == selectedAbility;

            blit(graphics, x + 20 * i, y, info.textureU() + (available ? 16 : 0), info.textureV(), 16, 16);

            if (info.ability().levels() > 1) {
                int level = selected ? selectedLevel + 1 : 0;
                if (level > 10 || level < 0) {
                    level = -1;
                }
                blit(graphics, x + 20 * i + 17, y + 1, 188 + level * 2, 70, 2, 14);
            }

            boolean hovered = isInAabb(mouseX, mouseY, x + 20 * i, y, 16, 16);
            if (hovered) {
                hoverIdx = i;
            }
            if (selected) {
                blit(graphics, x + 20 * i - 1, y - 1, 220, 9, 18, 18);
            } else if (available && hovered) {
                blit(graphics, x + 20 * i - 1, y - 1, 238, 9, 18, 18);
            }
        }
        return hoverIdx;
    }

    private <T extends IBaseAbility> Selection<T> handleSwitchesClicked(List<AbilityInfo<T>> abilities,
            T selectedAbility, int selectedLevel, int hoverIdx) {
        if (hoverIdx == -1) {
            return new Selection<>(selectedAbility, selectedLevel);
        }

        T hoveredAbility = abilities.get(hoverIdx).ability();
        if (!abilityAvailable(hoveredAbility)) {
            return new Selection<>(selectedAbility, selectedLevel);
        }

        int availableLevels = availableAbilities.maxLevel(hoveredAbility) + 1;
        if (hoveredAbility != selectedAbility || availableLevels > 1) {
            playAbilityClick(2.0F);
        }

        if (hoveredAbility == selectedAbility) {
            selectedLevel = (selectedLevel + 1) % availableLevels;
        } else {
            selectedAbility = hoveredAbility;
            selectedLevel = 0;
        }
        return new Selection<>(selectedAbility, selectedLevel);
    }

    private boolean abilityAvailable(IBaseAbility ability) {
        if (!availableAbilities.supportsAbility(ability)) {
            return false;
        }

        ToolPreset activePreset = configuration.getActivePreset();
        return !(ability instanceof IToolHarvestAbility)
                || ability == ToolHarvestAbilities.NONE
                || activePreset.areaAbility.allowsHarvest(activePreset.areaAbilityLevel);
    }

    private void renderHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        Component tooltip = null;
        ToolPreset activePreset = configuration.getActivePreset();
        if (hoverIdxArea != -1) {
            AbilityInfo<IToolAreaAbility> info = AREA_ABILITIES.get(hoverIdxArea);
            int level = info.ability() == activePreset.areaAbility ? activePreset.areaAbilityLevel : 0;
            tooltip = info.ability().getFullName(level);
        } else if (hoverIdxHarvest != -1) {
            AbilityInfo<IToolHarvestAbility> info = HARVEST_ABILITIES.get(hoverIdxHarvest);
            int level = info.ability() == activePreset.harvestAbility ? activePreset.harvestAbilityLevel : 0;
            tooltip = info.ability().getFullName(level);
        } else if (hoverIdxExtraButton != -1) {
            tooltip = switch (hoverIdxExtraButton) {
                case 0 -> Component.translatable("container.hbm_ntm_rebirth.tool_ability.reset");
                case 1 -> Component.translatable("container.hbm_ntm_rebirth.tool_ability.delete");
                case 2 -> Component.translatable("container.hbm_ntm_rebirth.tool_ability.add");
                case 3 -> Component.translatable("container.hbm_ntm_rebirth.tool_ability.first");
                case 4 -> Component.translatable("container.hbm_ntm_rebirth.tool_ability.next");
                case 5 -> Component.translatable("container.hbm_ntm_rebirth.tool_ability.prev");
                case 6 -> Component.translatable("container.hbm_ntm_rebirth.tool_ability.done");
                default -> null;
            };
        }

        if (tooltip == null || tooltip.getString().isEmpty()) {
            return;
        }

        int tooltipWidth = Math.max(6, font.width(tooltip));
        int tooltipX = leftPos + imageWidth / 2 - tooltipWidth / 2;
        int tooltipY = topPos + imageHeight + 5;
        drawStretchedRect(graphics, tooltipX - 5, tooltipY - 4, 0, 76, tooltipWidth + 10, 186, 15, 3, 3);
        graphics.drawString(font, tooltip, tooltipX, tooltipY, 0xFFFFFFFF, false);
    }

    private void deletePreset() {
        if (configuration.presets().size() <= 1) {
            return;
        }
        configuration.presets().remove(configuration.currentPreset());
        configuration.setCurrentPreset(configuration.currentPreset());
    }

    private void addPreset() {
        if (configuration.presets().size() >= MAX_PRESETS) {
            return;
        }
        int index = configuration.currentPreset() + 1;
        configuration.presets().add(index, new ToolPreset());
        configuration.setCurrentPreset(index);
    }

    private void nextPreset(boolean bound) {
        if (bound) {
            if (configuration.currentPreset() < configuration.presets().size() - 1) {
                configuration.setCurrentPreset(configuration.currentPreset() + 1);
            }
            return;
        }
        configuration.setCurrentPreset((configuration.currentPreset() + 1) % configuration.presets().size());
    }

    private void previousPreset(boolean bound) {
        if (bound) {
            if (configuration.currentPreset() > 0) {
                configuration.setCurrentPreset(configuration.currentPreset() - 1);
            }
            return;
        }
        int size = configuration.presets().size();
        configuration.setCurrentPreset((configuration.currentPreset() + size - 1) % size);
    }

    private void saveConfiguration() {
        if (saved || configuration == null) {
            return;
        }
        saved = true;
        CompoundTag data = new CompoundTag();
        configuration.writeToNBT(data);
        HbmAbilityToolItem tool = menu.getTool();
        if (tool != null) {
            tool.setConfiguration(menu.getToolStack(), configuration);
        }
        ModMessages.sendTypedMenuAction(HbmNetworkActions.TOOL_ABILITY_CONFIG, 0, data);
    }

    private void playClick(float pitch) {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
        }
    }

    private void playAbilityClick(float pitch) {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.TOOL_TECH_BOOP.get(), pitch));
        }
    }

    private void drawStretchedRect(GuiGraphics graphics, int x, int y, int u, int v, int realWidth, int width,
            int height, int keepLeft, int keepRight) {
        int midWidth = width - keepLeft - keepRight;
        int realMidWidth = realWidth - keepLeft - keepRight;
        blit(graphics, x, y, u, v, keepLeft, height);
        for (int i = 0; i < realMidWidth; i += midWidth) {
            blit(graphics, x + keepLeft + i, y, u + keepLeft, v, Math.min(midWidth, realMidWidth - i), height);
        }
        blit(graphics, x + keepLeft + realMidWidth, y, u + keepLeft + midWidth, v, keepRight, height);
    }

    private void drawNumber(GuiGraphics graphics, int number, int x, int y) {
        number += 100;
        drawDigit(graphics, number / 10 % 10, x, y);
        drawDigit(graphics, number % 10, x + 12, y);
    }

    private void drawDigit(GuiGraphics graphics, int digit, int x, int y) {
        blit(graphics, x, y, digit * 10, 123, 10, 15);
    }

    private void blit(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(TEXTURE, x, y, u, v, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static boolean isInAabb(int mouseX, int mouseY, int x, int y, int width, int height) {
        return x <= mouseX && x + width > mouseX && y <= mouseY && y + height > mouseY;
    }

    private record AbilityInfo<T extends IBaseAbility>(T ability, int textureU, int textureV) {
    }

    private record Selection<T extends IBaseAbility>(T ability, int level) {
    }
}
