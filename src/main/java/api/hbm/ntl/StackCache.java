package api.hbm.ntl;

import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticStackCache;
import net.minecraft.core.BlockPos;

/**
 * Legacy-package bridge for a pneumatic storage access cache.
 *
 * <p>1.7.10 keyed cache slots with a numeric item-id/NBT hash. The modern
 * implementation intentionally uses {@link StackIdentity}, which preserves
 * item, damage and copied tag data without reintroducing numeric registry IDs
 * or their collision-prone hash.</p>
 */
@Deprecated(forRemoval = false)
public class StackCache extends PneumaticStackCache {
    public StackCache(int x, int y, int z) {
        super(new BlockPos(x, y, z));
    }

    public int getLegacyX() {
        return getPos().getX();
    }

    public int getLegacyY() {
        return getPos().getY();
    }

    public int getLegacyZ() {
        return getPos().getZ();
    }
}
