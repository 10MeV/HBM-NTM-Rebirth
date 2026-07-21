package com.hbm.ntm.client.screen;

import com.hbm.inventory.recipes.MachineRecipes;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientSatelliteData;
import com.hbm.ntm.client.sound.LegacyClientSoundPlayer;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.ntm.util.HbmRegistryUtil;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.lwjgl.glfw.GLFW;

public class SatellitePanelScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/gui/satellites/gui_sat_interface.png");
    private static final int IMAGE_WIDTH = 216;
    private static final int IMAGE_HEIGHT = 216;
    private static final int MAP_SIZE = 200;

    private final InteractionHand hand;
    private final int[][] map = new int[MAP_SIZE][MAP_SIZE];
    private int leftPos;
    private int topPos;
    private int centerX;
    private int centerZ;
    private int scanPos;
    private long lastScanMillis;

    public SatellitePanelScreen(InteractionHand hand) {
        super(Component.translatable("item.hbm_ntm_rebirth.sat_interface"));
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
    }

    @Override
    protected void init() {
        leftPos = (width - IMAGE_WIDTH) / 2;
        topPos = (height - IMAGE_HEIGHT) / 2;
        if (minecraft != null && minecraft.player != null) {
            // GUIScreenSatInterface used Java's (int) cast, which truncates toward
            // zero rather than flooring negative coordinates like getBlockX().
            centerX = (int) minecraft.player.getX();
            centerZ = (int) minecraft.player.getZ();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        ClientSatelliteData.SatelliteSnapshot snapshot = ClientSatelliteData.current().orElse(null);
        if (snapshot == null) {
            drawNotConnected(graphics);
        } else if (snapshot.satellite().satelliteInterface() != Satellite.SatelliteInterface.SAT_PANEL) {
            drawNoService(graphics);
        } else {
            Satellite satellite = snapshot.satellite();
            if (satellite.interfaceActions().contains(Satellite.InterfaceAction.HAS_MAP)) {
                drawMap(graphics);
            }
            if (satellite.interfaceActions().contains(Satellite.InterfaceAction.HAS_ORES)) {
                drawScan(graphics);
            }
            if (satellite.interfaceActions().contains(Satellite.InterfaceAction.HAS_RADAR)) {
                drawRadar(graphics);
            }
        }
        if (snapshot != null
                && snapshot.satellite().interfaceActions().contains(Satellite.InterfaceAction.SHOW_COORDS)
                && isInsideMap(mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(worldX(mouseX) + " / " + worldZ(mouseY)), mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ClientSatelliteData.SatelliteSnapshot snapshot = ClientSatelliteData.current().orElse(null);
        // GUIScreenSatInterface accepted its unused mouse-button argument and
        // dispatched any click inside the map.  Keeping this intentionally
        // includes right and middle clicks; only the area and CAN_CLICK action
        // gate the old SatLaserPacket path.
        if (minecraft != null && minecraft.player != null && snapshot != null
                && snapshot.satellite().interfaceActions().contains(Satellite.InterfaceAction.CAN_CLICK)
                && isInsideMap((int) mouseX, (int) mouseY)) {
            ModMessages.sendSatLaser(hand, worldX((int) mouseX), worldZ((int) mouseY), currentFrequency());
            LegacyClientSoundPlayer.playUi("hbm:item.techBleep", 1.0F);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == Minecraft.getInstance().options.keyInventory.getKey().getValue()) {
            onClose();
            return true;
        }
        if (minecraft.options.keyUp.matches(keyCode, scanCode)) {
            moveCenter(0, -50);
            return true;
        }
        if (minecraft.options.keyDown.matches(keyCode, scanCode)) {
            moveCenter(0, 50);
            return true;
        }
        if (minecraft.options.keyLeft.matches(keyCode, scanCode)) {
            moveCenter(-50, 0);
            return true;
        }
        if (minecraft.options.keyRight.matches(keyCode, scanCode)) {
            moveCenter(50, 0);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawMap(GuiGraphics graphics) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        for (int i = -100; i < 100; i++) {
            int x = centerX + i;
            int z = centerZ + scanPos - 100;
            int y = WorldUtil.legacyGetHeightValue(minecraft.level, x, z) - 1;
            BlockPos pos = new BlockPos(x, y, z);
            // GUIScreenSatInterface assigned every map column on every pass.
            // Keep unloaded modern chunks from retaining the color sampled at
            // this ring-buffer index 200 passes ago; the 1.7.10 client queried
            // its air fallback and therefore replaced that pixel.
            map[i + 100][scanPos] = 0;
            if (HbmRegistryUtil.hasChunkAt(minecraft.level, pos)) {
                BlockState state = minecraft.level.getBlockState(pos);
                map[i + 100][scanPos] = state.getMapColor(minecraft.level, pos).col;
            }
        }
        printMap(graphics);
        progressScan();
    }

    private void drawScan(GuiGraphics graphics) {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        for (int i = -100; i < 100; i++) {
            int x = centerX + i;
            int z = centerZ + scanPos - 100;
            for (int y = minecraft.level.getMaxBuildHeight() - 1; y >= minecraft.level.getMinBuildHeight(); y--) {
                BlockPos pos = new BlockPos(x, y, z);
                if (!HbmRegistryUtil.hasChunkAt(minecraft.level, pos)) {
                    continue;
                }
                int color = oreColor(minecraft.level.getBlockState(pos));
                if (color != 0) {
                    map[i + 100][scanPos] = color;
                    break;
                }
            }
        }
        printMap(graphics);
        progressScan();
    }

    private void drawRadar(GuiGraphics graphics) {
        if (minecraft == null || minecraft.level == null || minecraft.player == null) {
            return;
        }
        // GUIScreenSatInterface keeps its radar query centered on the player;
        // arrow-key map panning changes only the red-dot projection below.
        // The source used a deliberate 0..5000 entity interval, rather than
        // the ordinary block build height: it includes Soyuz/capsule flight at
        // Y=600. Modernize only the old lower world-bottom assumption; retain
        // the explicit high-altitude ceiling (or a future taller dimension).
        double maxRadarY = Math.max(5000.0D, minecraft.level.getMaxBuildHeight());
        AABB area = new AABB(minecraft.player.getX() - 100.0D, minecraft.level.getMinBuildHeight(),
                minecraft.player.getZ() - 100.0D, minecraft.player.getX() + 100.0D,
                maxRadarY, minecraft.player.getZ() + 100.0D);
        for (Entity entity : minecraft.level.getEntities(minecraft.player, area,
                entity -> entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() >= 0.5D)) {
            int x = (int) ((entity.getX() - centerX) / 201.0D * 192.0D) - 4;
            int z = (int) ((entity.getZ() - centerZ) / 201.0D * 192.0D) - 13;
            int type = entity instanceof Player ? 7 : entity instanceof Monster ? 6 : 5;
            graphics.blit(TEXTURE, leftPos + 108 + x, topPos + 117 + z, 216, 8 * type, 8, 8);
        }
    }

    private int oreColor(BlockState state) {
        // The legacy screen queried the old OreDictionary through mODE, not a
        // registry-name heuristic. Keep the exact old priority and delegate the
        // 1.20 tag conversion to the shared compatibility facade.
        ItemStack stack = new ItemStack(state.getBlock());
        if (MachineRecipes.mODE(stack, "oreCoal")) return 0x333333;
        if (MachineRecipes.mODE(stack, "oreIron")) return 0xB2AA92;
        if (MachineRecipes.mODE(stack, "oreGold")) return 0xFFE460;
        if (MachineRecipes.mODE(stack, "oreSilver")) return 0xE5E5E5;
        if (MachineRecipes.mODE(stack, "oreDiamond")) return 0x6ED5EF;
        if (MachineRecipes.mODE(stack, "oreEmerald")) return 0x6CF756;
        if (MachineRecipes.mODE(stack, "oreLapis")) return 0x092F7A;
        if (MachineRecipes.mODE(stack, "oreRedstone")) return 0xE50000;
        if (MachineRecipes.mODE(stack, "oreTin")) return 0xA09797;
        if (MachineRecipes.mODE(stack, "oreCopper")) return 0xD16208;
        if (MachineRecipes.mODE(stack, "oreLead")) return 0x384B68;
        if (MachineRecipes.mODE(stack, "oreAluminum")) return 0xDBDBDB;
        if (MachineRecipes.mODE(stack, "oreTungsten")) return 0x333333;
        if (MachineRecipes.mODE(stack, "oreTitanium")) return 0xDDDDDD;
        if (MachineRecipes.mODE(stack, "oreUranium")) return 0x3E4F3C;
        if (MachineRecipes.mODE(stack, "oreBeryllium")) return 0x8E8D7D;
        if (MachineRecipes.mODE(stack, "oreSulfur")) return 0x9B9309;
        if (MachineRecipes.mODE(stack, "oreSalpeter") || MachineRecipes.mODE(stack, "oreNiter")) return 0xA5A09D;
        if (MachineRecipes.mODE(stack, "oreFluorite")) return 0xFFFFFF;
        if (MachineRecipes.mODE(stack, "oreSchrabidium")) return 0x1CFFFF;
        if (MachineRecipes.mODE(stack, "oreRareEarth")) return 0xFFCC99;
        return LegacyOreDictionaryMappings.isAnyLegacyOre(stack) ? 0xBA00AF : 0;
    }

    private void printMap(GuiGraphics graphics) {
        for (int x = 0; x < MAP_SIZE; x++) {
            for (int z = 0; z < MAP_SIZE; z++) {
                if (map[x][z] != 0) {
                    graphics.fill(leftPos + 8 + x, topPos + 8 + z, leftPos + 9 + x, topPos + 9 + z,
                            0xFF000000 | map[x][z]);
                }
            }
        }
    }

    private void progressScan() {
        long now = System.currentTimeMillis();
        if (lastScanMillis + 25L < now) {
            lastScanMillis = now;
            scanPos++;
        }
        if (scanPos >= MAP_SIZE) {
            scanPos -= MAP_SIZE;
        }
    }

    private void drawNoService(GuiGraphics graphics) {
        graphics.blit(TEXTURE, (width - 77) / 2, (height - 12) / 2, 0, 228, 77, 12);
    }

    private void drawNotConnected(GuiGraphics graphics) {
        graphics.blit(TEXTURE, (width - 121) / 2, (height - 12) / 2, 0, 216, 121, 12);
    }

    private boolean isInsideMap(int mouseX, int mouseY) {
        return mouseX >= leftPos + 8 && mouseX < leftPos + 208
                && mouseY >= topPos + 8 && mouseY < topPos + 208;
    }

    private int worldX(int mouseX) {
        return centerX + mouseX - leftPos - 108;
    }

    private int worldZ(int mouseY) {
        return centerZ + mouseY - topPos - 108;
    }

    private int currentFrequency() {
        if (minecraft == null || minecraft.player == null) {
            return 0;
        }
        return ISatelliteChip.getFrequencyFromStack(minecraft.player.getItemInHand(hand));
    }

    private void moveCenter(int x, int z) {
        centerX += x;
        centerZ += z;
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                map[i][j] = 0;
            }
        }
    }

}
