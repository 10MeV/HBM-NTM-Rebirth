package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ISatChip;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.saveddata.satellites.SatelliteScanner;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.RegistryObject;

/**
 * Legacy package facade for the 1.7.10 neutrino lens armor module.
 */
@Deprecated(forRemoval = false)
public class ItemModLens extends ItemArmorMod implements ISatChip {
    private static final int SCANNER_RANGE_CHUNKS = 3;
    private static final int SCANNER_MAX_HITS = 100;
    private static final List<ScannerTarget> SCANNER_TARGETS = List.of(
            ScannerTarget.legacy("ore_alexandrite", 1, "Alexandrite", 0x00ffff),
            ScannerTarget.legacy("ore_oil", 300, "Oil", 0xa0a0a0),
            ScannerTarget.legacy("ore_bedrock_oil", 300, "Bedrock Oil", 0xa0a0a0),
            ScannerTarget.legacy("ore_coltan", 5, "Coltan", 0xa0a000),
            ScannerTarget.legacy("stone_gneiss", 5000, "Schist", 0x8080ff),
            ScannerTarget.legacy("ore_australium", 1000, "Australium", 0xffff00),
            ScannerTarget.vanilla(Blocks.END_PORTAL_FRAME, 1, "End Portal", 0x40b080),
            ScannerTarget.legacy("volcano_core", 1, "Volcano Core", 0xff4000),
            ScannerTarget.legacy("bobblehead", 1, "A Treasure!", 0xff0000),
            ScannerTarget.legacy("ore_bedrock", 1, "Bedrock Ore", 0xff0000)
    );

    public ItemModLens() {
        this(new Item.Properties());
    }

    protected ItemModLens(Item.Properties properties) {
        super(properties, ArmorModHandler.extra, true, false, false, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Satellite Frequency: " + getFreq(stack)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.empty());
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(Component.literal("  ")
                .append(stack.getHoverName())
                .append(Component.literal(" (Freq: " + getFreq(stack) + ")"))
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {
        if (!(entity instanceof ServerPlayer player) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack lens = ArmorModHandler.pryMods(armor)[ArmorModHandler.extra];
        if (lens.isEmpty()) {
            return;
        }

        Satellite satellite = SatelliteSavedData.getData(level).getSatFromFreq(getFreq(lens));
        if (!(satellite instanceof SatelliteScanner)) {
            return;
        }

        scanAndMark(level, player);
    }

    private void scanAndMark(ServerLevel level, ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        int minY = level.getMinBuildHeight();
        int maxSpan = Math.max(1, level.getMaxBuildHeight() - minY);
        int heightSpan = Mth.clamp(playerPos.getY() - minY + 10, 64, maxSpan);
        int segY = minY + (int) (level.getGameTime() % heightSpan);
        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int hits = 0;
        BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();

        for (int chunkX = centerChunkX - SCANNER_RANGE_CHUNKS; chunkX <= centerChunkX + SCANNER_RANGE_CHUNKS; chunkX++) {
            for (int chunkZ = centerChunkZ - SCANNER_RANGE_CHUNKS; chunkZ <= centerChunkZ + SCANNER_RANGE_CHUNKS; chunkZ++) {
                LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                for (int ix = 0; ix < 16; ix++) {
                    for (int iz = 0; iz < 16; iz++) {
                        scanPos.set((chunkX << 4) + ix, segY, (chunkZ << 4) + iz);
                        Block block = chunk.getBlockState(scanPos).getBlock();
                        for (ScannerTarget target : SCANNER_TARGETS) {
                            if (target.tryMark(block, scanPos, player)) {
                                hits++;
                                if (hits > SCANNER_MAX_HITS) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private record ScannerTarget(@Nullable String legacyName, @Nullable Block vanillaBlock, int chance,
                                 @Nullable String label, int color) {
        private static ScannerTarget legacy(String legacyName, int chance, @Nullable String label, int color) {
            return new ScannerTarget(legacyName, null, chance, label, color);
        }

        private static ScannerTarget vanilla(Block block, int chance, @Nullable String label, int color) {
            return new ScannerTarget(null, block, chance, label, color);
        }

        private boolean tryMark(Block block, BlockPos pos, ServerPlayer player) {
            Block target = targetBlock();
            if (target != block || player.getRandom().nextInt(chance) != 0) {
                return false;
            }
            CompoundTag data = new CompoundTag();
            data.putString("type", ParticleUtil.TYPE_MARKER);
            data.putInt("color", color);
            data.putInt("expires", 15_000);
            data.putDouble("dist", 300.0D);
            if (label != null) {
                data.putString("label", label);
            }
            ModMessages.sendAuxParticle(player, pos.getX(), pos.getY(), pos.getZ(), data);
            return true;
        }

        @Nullable
        private Block targetBlock() {
            if (vanillaBlock != null) {
                return vanillaBlock;
            }
            RegistryObject<? extends Block> object = ModBlocks.legacyBlock(legacyName);
            return object == null ? null : object.get();
        }
    }
}
