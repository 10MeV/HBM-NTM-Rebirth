package com.hbm.ntm.item;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.particle.ParticleUtil;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DieselSuitArmorItem extends FsbFueledArmorItem {
    public DieselSuitArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, int maxFuel, int fillRate, int consumption, int drain,
            FluidType... acceptedFuelTypes) {
        super(material, type, properties, fullSetEffects, maxFuel, fillRate, consumption, drain, acceptedFuelTypes);
    }

    public DieselSuitArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, int maxFuel, int fillRate, int consumption, int drain,
            FullSetTraits fullSetTraits, FluidType... acceptedFuelTypes) {
        super(material, type, properties, fullSetEffects, maxFuel, fillRate, consumption, drain,
                fullSetTraits, acceptedFuelTypes);
    }

    @Override
    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        super.tickEquippedArmor(stack, level, player);
        if (!level.isClientSide && getType() == Type.LEGGINGS && hasFullSet(player) && level.getGameTime() % 3L == 0L) {
            ParticleUtil.spawnBnuuy(level, player);
        }
    }
}
