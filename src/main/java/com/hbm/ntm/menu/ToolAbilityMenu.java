package com.hbm.ntm.menu;

import com.hbm.ntm.ability.AvailableAbilities;
import com.hbm.ntm.ability.ToolAbilityConfiguration;
import com.hbm.ntm.item.HbmAbilityToolItem;
import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.network.HbmTypedMenuActionReceiver;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ToolAbilityMenu extends AbstractContainerMenu implements HbmTypedMenuActionReceiver {
    private final Inventory inventory;
    private final InteractionHand hand;

    public ToolAbilityMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readEnum(InteractionHand.class));
    }

    public ToolAbilityMenu(int containerId, Inventory inventory, InteractionHand hand) {
        super(ModMenuTypes.TOOL_ABILITY.get(), containerId);
        this.inventory = inventory;
        this.hand = hand;
    }

    public ItemStack getToolStack() {
        return inventory.player.getItemInHand(hand);
    }

    public HbmAbilityToolItem getTool() {
        ItemStack stack = getToolStack();
        return stack.getItem() instanceof HbmAbilityToolItem tool ? tool : null;
    }

    public AvailableAbilities getAvailableAbilities() {
        HbmAbilityToolItem tool = getTool();
        return tool == null ? new AvailableAbilities().addToolAbilities() : tool.availableAbilities();
    }

    public ToolAbilityConfiguration getConfiguration() {
        HbmAbilityToolItem tool = getTool();
        return tool == null ? new ToolAbilityConfiguration() : tool.getConfiguration(getToolStack());
    }

    @Override
    public boolean stillValid(Player player) {
        return player == inventory.player && getTool() != null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canReceiveTypedMenuAction(ServerPlayer player, ResourceLocation actionType, int value, CompoundTag data) {
        return HbmNetworkActions.TOOL_ABILITY_CONFIG.equals(actionType)
                && player == inventory.player
                && getTool() != null;
    }

    @Override
    public void handleTypedMenuAction(ServerPlayer player, ResourceLocation actionType, int value, CompoundTag data) {
        HbmAbilityToolItem tool = getTool();
        if (tool == null || !HbmNetworkActions.TOOL_ABILITY_CONFIG.equals(actionType)) {
            return;
        }

        ToolAbilityConfiguration configuration = new ToolAbilityConfiguration();
        if (data.contains(ToolAbilityConfiguration.TAG_ACTIVE_PRESET)
                && data.contains(ToolAbilityConfiguration.TAG_PRESETS, Tag.TAG_LIST)) {
            configuration.readFromNBT(data);
            configuration.restrictTo(tool.availableAbilities());
        } else {
            configuration.reset(tool.availableAbilities());
        }
        tool.setConfiguration(getToolStack(), configuration);
    }
}
