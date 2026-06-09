package com.hbm.ntm.client.screen;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientSatelliteData;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteInterfaceItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
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
            centerX = minecraft.player.getBlockX();
            centerZ = minecraft.player.getBlockZ();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!isHeldSatelliteInterface()) {
            onClose();
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
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && snapshot != null
                && snapshot.satellite().interfaceActions().contains(Satellite.InterfaceAction.CAN_CLICK)
                && isInsideMap((int) mouseX, (int) mouseY)) {
            ModMessages.sendSatLaser(hand, worldX((int) mouseX), worldZ((int) mouseY), currentFrequency());
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.TOOL_TECH_BLEEP.get(), 1.0F));
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
            int y = minecraft.level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
            BlockPos pos = new BlockPos(x, y, z);
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
        AABB area = new AABB(centerX - 100, minecraft.level.getMinBuildHeight(), centerZ - 100,
                centerX + 100, minecraft.level.getMaxBuildHeight(), centerZ + 100);
        for (Entity entity : minecraft.level.getEntities(minecraft.player, area,
                entity -> entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() >= 0.5D)) {
            int x = (int) ((entity.getX() - centerX) / 201.0D * 192.0D) - 4;
            int z = (int) ((entity.getZ() - centerZ) / 201.0D * 192.0D) - 13;
            int type = entity instanceof Player ? 7 : entity instanceof Mob ? 6 : 5;
            graphics.blit(TEXTURE, leftPos + 108 + x, topPos + 117 + z, 216, 8 * type, 8, 8);
        }
    }

    private int oreColor(BlockState state) {
        Block block = state.getBlock();
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        String path = key == null ? "" : key.getPath();
        if (path.contains("coal")) return 0x333333;
        if (path.contains("iron")) return 0xB2AA92;
        if (path.contains("gold")) return 0xFFE460;
        if (path.contains("silver")) return 0xE5E5E5;
        if (path.contains("diamond")) return 0x6ED5EF;
        if (path.contains("emerald")) return 0x6CF756;
        if (path.contains("lapis")) return 0x092F7A;
        if (path.contains("redstone")) return 0xE50000;
        if (path.contains("tin")) return 0xA09797;
        if (path.contains("copper")) return 0xD16208;
        if (path.contains("lead")) return 0x384B68;
        if (path.contains("aluminum") || path.contains("aluminium")) return 0xDBDBDB;
        if (path.contains("tungsten")) return 0x333333;
        if (path.contains("titanium")) return 0xDDDDDD;
        if (path.contains("uranium")) return 0x3E4F3C;
        if (path.contains("beryllium")) return 0x8E8D7D;
        if (path.contains("sulfur")) return 0x9B9309;
        if (path.contains("salpeter") || path.contains("niter")) return 0xA5A09D;
        if (path.contains("fluorite")) return 0xFFFFFF;
        if (path.contains("schrabidium")) return 0x1CFFFF;
        if (path.contains("rare_earth")) return 0xFFCC99;
        return state.is(BlockTags.create(new ResourceLocation("forge", "ores"))) ? 0xBA00AF : 0;
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

    private boolean isHeldSatelliteInterface() {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        ItemStack stack = minecraft.player.getItemInHand(hand);
        return stack.getItem() instanceof SatelliteInterfaceItem item
                && item.mode() == SatelliteInterfaceItem.Mode.PANEL;
    }
}
