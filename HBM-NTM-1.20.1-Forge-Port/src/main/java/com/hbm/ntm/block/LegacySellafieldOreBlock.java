package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class LegacySellafieldOreBlock extends LegacySellafieldSlakedBlock {
    public enum Kind {
        DIAMOND("diamond"),
        EMERALD("emerald"),
        URANIUM_SCORCHED("uranium_scorched"),
        SCHRABIDIUM("schrabidium"),
        RADGEM("radgem");

        private final String overlayTexture;

        Kind(String overlayTexture) {
            this.overlayTexture = overlayTexture;
        }

        public String overlayTexture() {
            return overlayTexture;
        }
    }

    private final Kind kind;

    public LegacySellafieldOreBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return asItem().getDefaultInstance();
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader level, RandomSource random, BlockPos pos, int fortune,
            int silktouch) {
        if (silktouch > 0) {
            return 0;
        }
        return switch (kind) {
            case DIAMOND, EMERALD, RADGEM -> random.nextIntBetweenInclusive(3, 7);
            default -> 0;
        };
    }

    public Item droppedItem() {
        return switch (kind) {
            case DIAMOND -> Items.DIAMOND;
            case EMERALD -> Items.EMERALD;
            case RADGEM -> ModItems.legacyItem("gem_rad").get();
            case URANIUM_SCORCHED, SCHRABIDIUM -> asItem();
        };
    }
}
