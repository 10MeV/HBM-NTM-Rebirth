package com.hbm.ntm.client.overlay;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.renderer.LegacyScreenQuadRenderer;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class LegacyHelmetOverlayRenderer {
    private static final ResourceLocation OVERLAY_DARK = texture("overlay_dark");
    private static final ResourceLocation[] OVERLAY_GOGGLES = overlaySet("overlay_goggles");
    private static final ResourceLocation[] OVERLAY_GASMASK = overlaySet("overlay_gasmask");

    public static void render(RenderGuiOverlayEvent.Post event) {
        Player player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ResourceLocation texture = textureFor(helmet);
        if (texture == null) {
            return;
        }

        LegacyScreenQuadRenderer.unitQuad(texture, event.getGuiGraphics(),
                0, 0, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(),
                0.0D, 1.0D, 1.0D, 1.0D, 1.0D, 0.0D, 0.0D, 0.0D,
                0xFFFFFF, 255, LegacyScreenQuadRenderer.BlendMode.NORMAL);
    }

    private static ResourceLocation textureFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
        return switch (path) {
            case "goggles", "gas_mask_m65", "hazmat_helmet_red", "hazmat_helmet_grey" ->
                    OVERLAY_GOGGLES[damageIndex(stack)];
            case "gas_mask" -> OVERLAY_GASMASK[damageIndex(stack)];
            case "liquidator_helmet" -> OVERLAY_DARK;
            default -> null;
        };
    }

    private static int damageIndex(ItemStack stack) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) {
            return 0;
        }
        return Mth.clamp((int) ((double) stack.getDamageValue() / (double) maxDamage * 6.0D), 0, 5);
    }

    private static ResourceLocation[] overlaySet(String name) {
        ResourceLocation[] textures = new ResourceLocation[6];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = texture(name + "_" + i);
        }
        return textures;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/misc/" + name + ".png");
    }

    private LegacyHelmetOverlayRenderer() {
    }
}
