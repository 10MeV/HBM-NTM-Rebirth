package com.hbm.items.armor;

import com.hbm.ntm.item.HbmArmorMaterials;
import com.hbm.ntm.item.TrenchmasterArmorItem;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;

/**
 * Legacy package carrier for the Trenchmaster FSB armor set.
 *
 * <p>All runtime behavior remains in {@link TrenchmasterArmorItem}; this class
 * exists so legacy consumers and the registered set retain the original class
 * boundary without creating a second armor or damage-handling path.</p>
 */
@Deprecated(forRemoval = false)
public class ArmorTrenchmaster extends TrenchmasterArmorItem {
    public ArmorTrenchmaster(HbmArmorMaterials material, ArmorItem.Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, boolean noHelmet, int dashCount, FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, noHelmet, dashCount, fullSetTraits);
    }

    /** Legacy spelling retained for source-level consumers. */
    public static boolean isTrenchMaster(Player player) {
        return hasTrenchmasterFullSet(player);
    }

    /** Legacy spelling retained for source-level consumers. */
    public static boolean hasAoS(Player player) {
        return hasAceOfSpades(player);
    }
}
