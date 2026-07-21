package com.hbm.interfaces;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Modern, dedicated-server-safe successor to the legacy item HUD hook.
 *
 * <p>The 1.7.10 contract received a Forge client overlay event directly. That
 * made every item implementing it depend on client-only classes. The modern
 * renderer supplies a small neutral drawing context instead, so item classes
 * keep their HUD contract without becoming unsafe to load on a dedicated
 * server.</p>
 */
public interface IItemHUD {
    void renderHUD(ItemHudRenderContext context, Player player, ItemStack stack);
}
