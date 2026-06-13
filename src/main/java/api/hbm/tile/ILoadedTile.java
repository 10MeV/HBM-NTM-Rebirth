package api.hbm.tile;

import com.hbm.ntm.api.tile.LoadedTile;
import com.hbm.util.Compat;
import com.hbm.util.Tuple.Quartet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy 1.7.10 package bridge for tiles that can report loaded state.
 */
@Deprecated(forRemoval = false)
public interface ILoadedTile extends com.hbm.ntm.api.tile.ILoadedTile {
    /**
     * Modernized shape of the 1.7.10 cache used by Energy/Fluid MK2 default
     * methods. It caches safe block-entity lookups, but keeps the modern loaded
     * chunk boundary from {@link Compat#getTileStandard(Level, BlockPos)}.
     */
    class TileAccessCache {
        public static final Map<Quartet<Integer, Integer, Integer, ResourceKey<Level>>, TileAccessCache> cache =
                new HashMap<>();
        public static int NULL_CACHE = 20;
        public static int NONNULL_CACHE = 60;

        public BlockEntity tile;
        public long expiresOn;

        public TileAccessCache(BlockEntity tile, long expiresOn) {
            this.tile = tile;
            this.expiresOn = expiresOn;
        }

        public boolean hasExpired(long worldTime) {
            if (tile != null && tile.isRemoved()) {
                return true;
            }
            if (worldTime >= expiresOn) {
                return true;
            }
            return tile instanceof LoadedTile loadedTile && !loadedTile.isLoaded();
        }

        public static Quartet<Integer, Integer, Integer, ResourceKey<Level>> publicCumRag =
                new Quartet<>(0, 0, 0, null);

        public static BlockEntity getTileOrCache(Level level, BlockPos pos) {
            if (level == null || pos == null) {
                return null;
            }
            return getTileOrCache(level, pos.getX(), pos.getY(), pos.getZ());
        }

        public static BlockEntity getTileOrCache(Level level, int x, int y, int z) {
            if (level == null) {
                return null;
            }
            publicCumRag.mangle(x, y, z, level.dimension());
            TileAccessCache cached = TileAccessCache.cache.get(publicCumRag);
            long worldTime = level.getGameTime();

            if (cached == null || cached.hasExpired(worldTime)) {
                BlockEntity tile = Compat.getTileStandard(level, x, y, z);
                cached = new TileAccessCache(tile, worldTime + (tile == null ? NULL_CACHE : NONNULL_CACHE));
                TileAccessCache.cache.put(publicCumRag.clone(), cached);
                return tile;
            }
            return cached.tile;
        }
    }
}
