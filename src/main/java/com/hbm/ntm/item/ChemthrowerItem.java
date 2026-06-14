package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaMagazineConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.entity.projectile.ChemicalProjectileEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFillableItemCapabilityProvider;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ChemthrowerItem extends SednaGunItem implements IFillableItem {
    public static final int CONSUMPTION = 3;
    public static final int TRANSFER_SPEED = 50;

    public ChemthrowerItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties, gunConfig);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new HbmFillableItemCapabilityProvider(stack, this, magazine().capacity());
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        return type != null && type != HbmFluids.NONE && (getFluidType(stack) == type || getFill(stack) == 0);
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {
        if (!acceptsFluid(type, stack)) {
            return amount;
        }
        if (getFill(stack) == 0) {
            setFluidType(stack, type);
        }
        int fill = getFill(stack);
        int requested = magazine().capacity() - fill;
        int toFill = Math.min(Math.min(amount, requested), TRANSFER_SPEED);
        setFill(stack, fill + toFill);
        return amount - toFill;
    }

    @Override
    public boolean providesFluid(FluidType type, ItemStack stack) {
        return type != null && type != HbmFluids.NONE && getFluidType(stack) == type;
    }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        if (!providesFluid(type, stack)) {
            return 0;
        }
        int toUnload = Math.min(Math.min(getFill(stack), amount), TRANSFER_SPEED);
        setFill(stack, getFill(stack) - toUnload);
        if (getFill(stack) <= 0) {
            setFluidType(stack, HbmFluids.NONE);
        }
        return toUnload;
    }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        return getFluidType(stack);
    }

    @Override
    public int getFill(ItemStack stack) {
        return magazineCount(stack, magazine());
    }

    @Override
    protected Optional<LoadedRound> getLoadedRound(Player player, ItemStack stack, SednaMagazineConfig magazine) {
        if (magazine.kind() != SednaMagazineConfig.Kind.FLUID || getFill(stack) < CONSUMPTION
                || getFluidType(stack) == HbmFluids.NONE) {
            return Optional.empty();
        }
        return Optional.of(new LoadedRound(LegacySednaRuntimeBulletConfigs.FLAME_DIESEL, getFill(stack)));
    }

    @Override
    protected void fire(Level level, Player player, ItemStack stack, GunParts gun, LoadedRound round) {
        FluidType type = getFluidType(stack);
        if (type == HbmFluids.NONE || getFill(stack) < CONSUMPTION) {
            return;
        }
        ChemicalProjectileEntity chemical = new ChemicalProjectileEntity(level);
        chemical.setOwner(player);
        chemical.setFluid(type);
        SednaReceiverConfig receiver = gun.receiver();
        SednaReceiverConfig.Offset offset = isAiming(stack)
                ? receiver.projectileOffsetScoped()
                : receiver.projectileOffset();
        Vec3 localOffset = new Vec3(offset.side(), offset.up(), offset.forward())
                .xRot(-player.getXRot() * ((float) Math.PI / 180.0F))
                .yRot(-player.getYRot() * ((float) Math.PI / 180.0F));
        Vec3 position = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ()).add(localOffset);
        Vec3 direction = player.getLookAngle();
        chemical.setPos(position.x, position.y, position.z);
        chemical.setDeltaMovement(direction.scale(1.5D));
        chemical.setYRot(player.getYRot());
        chemical.setXRot(player.getXRot());
        chemical.yRotO = player.getYRot();
        chemical.xRotO = player.getXRot();
        level.addFreshEntity(chemical);

        if (!player.getAbilities().instabuild) {
            setFill(stack, Math.max(0, getFill(stack) - CONSUMPTION));
            if (getFill(stack) <= 0) {
                setFluidType(stack, HbmFluids.NONE);
            }
        }
        addWearClamped(stack, gun.mode().configIndex(), 1, gun.mode().durability());
        LegacySoundPlayer.playLegacyFlamethrowerShoot(player, 1.0F, 1.0F);
    }

    @Override
    protected void clickPrimary(ServerPlayer player, ItemStack stack, GunParts gun) {
        super.clickPrimary(player, stack, gun);
    }

    private FluidType getFluidType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? HbmFluids.NONE : HbmFluids.fromId(tag.getInt(magazine().nbtTypeKey()));
    }

    private void setFluidType(ItemStack stack, FluidType type) {
        stack.getOrCreateTag().putInt(magazine().nbtTypeKey(), type == null ? HbmFluids.NONE.getId() : type.getId());
    }

    private void setFill(ItemStack stack, int amount) {
        setMagazineCount(stack, magazine(), Math.min(Math.max(0, amount), magazine().capacity()));
    }

    private SednaMagazineConfig magazine() {
        return gunConfig().magazines().stream()
                .filter(config -> config.kind() == SednaMagazineConfig.Kind.FLUID)
                .findFirst()
                .orElseThrow();
    }
}
