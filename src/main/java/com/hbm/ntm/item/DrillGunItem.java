package com.hbm.ntm.item;

import com.hbm.ntm.api.fluid.IFillableItem;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaMagazineConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.bullet.SednaWeaponModEvaluator;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.network.HbmLegacyItemAnimationReceiver;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.energy.HbmBatteryItemCapabilityProvider;
import com.hbm.ntm.energy.IBatteryItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFillableItemCapabilityProvider;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.player.HbmPlayerProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DrillGunItem extends SednaGunItem implements IFillableItem, IBatteryItem, HbmLegacyItemAnimationReceiver {
    private static final int LIQUID_CONSUMPTION = 10;
    private static final int TRANSFER_SPEED = 50;
    private static final double REACH = 5.0D;
    private static final float DT_NEGATION = 2.0F;
    private static final float PIERCING = 0.15F;
    private static final int BASE_AOE = 1;
    private static final int BASE_HARVEST_LEVEL = 2;
    private static final SednaWeaponModEvaluator.DrillStats BASE_STATS =
            new SednaWeaponModEvaluator.DrillStats(REACH, DT_NEGATION, PIERCING, BASE_AOE, BASE_HARVEST_LEVEL, 0);

    public DrillGunItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties, gunConfig);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide) {
            int animation = legacyAnimation(stack, 0);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.hbm.ntm.client.DrillGunItemClient.tick(stack, level, entity, selected, animation));
        }
    }

    @Override
    public void handleLegacyItemAnimation(ItemStack stack, int selectedSlot, short animationType, int receiverIndex,
            int itemIndex) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.hbm.ntm.client.DrillGunItemClient.handleAnimation(
                        stack, selectedSlot, animationType, receiverIndex, itemIndex));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        ICapabilityProvider fluid = new HbmFillableItemCapabilityProvider(stack, this, effectiveEngineMagazine(stack).capacity());
        ICapabilityProvider energy = new HbmBatteryItemCapabilityProvider(stack, this);
        return new CombinedCapabilityProvider(fluid, energy);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        // ItemGunDrill#canHarvestBlock in 1.7.10 returns true unconditionally.
        // The drill's harvest-level mod hook was exposed separately, but it did
        // not turn this player-held tool check into a tier gate.
        return true;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        // The legacy item has no destroy-speed override. Its instant drill action
        // is handled by XFactoryDrill#breakExtraBlock instead.
        return super.getDestroySpeed(stack, state);
    }

    /**
     * Client-side selection rendering must use the same mod-evaluated reach as the authoritative drill action.
     */
    public double highlightReach(ItemStack stack) {
        return drillStats(stack).reach();
    }

    /**
     * Client-side selection rendering must use the same mod-evaluated area as the authoritative drill action.
     */
    public int highlightArea(ItemStack stack) {
        return drillStats(stack).area();
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        SednaMagazineConfig engine = effectiveEngineMagazine(stack);
        return type != null && engine.kind() == SednaMagazineConfig.Kind.LIQUID_ENGINE
                && engine.acceptedFluidNames().contains(type.getName());
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {
        if (!acceptsFluid(type, stack)) {
            return amount;
        }
        SednaMagazineConfig engine = effectiveEngineMagazine(stack);
        int fill = getFill(stack);
        int toFill = Math.min(Math.min(amount, TRANSFER_SPEED), engine.capacity() - fill);
        setMagazineCount(stack, engine, fill + toFill);
        return amount - toFill;
    }

    @Override
    public boolean providesFluid(FluidType type, ItemStack stack) {
        return false;
    }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        return amount;
    }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        return firstAcceptedFluid(stack);
    }

    @Override
    public int getFill(ItemStack stack) {
        return magazineCount(stack, effectiveEngineMagazine(stack));
    }

    @Override
    public long getMaxCharge(ItemStack stack) {
        SednaMagazineConfig engine = effectiveEngineMagazine(stack);
        return engine.kind() == SednaMagazineConfig.Kind.ELECTRIC_ENGINE ? engine.capacity() : 0L;
    }

    @Override
    public long getChargeRate(ItemStack stack) {
        return 50_000L;
    }

    @Override
    public long getDischargeRate(ItemStack stack) {
        return 0L;
    }

    @Override
    public long getCharge(ItemStack stack) {
        SednaMagazineConfig engine = effectiveEngineMagazine(stack);
        return engine.kind() == SednaMagazineConfig.Kind.ELECTRIC_ENGINE ? magazineCount(stack, engine) : 0L;
    }

    @Override
    public void setCharge(ItemStack stack, long charge) {
        SednaMagazineConfig engine = effectiveEngineMagazine(stack);
        if (engine.kind() == SednaMagazineConfig.Kind.ELECTRIC_ENGINE) {
            setMagazineCount(stack, engine, Mth.clamp((int) charge, 0, engine.capacity()));
        }
    }

    @Override
    public long chargeBattery(ItemStack stack, long amount) {
        SednaMagazineConfig engine = effectiveEngineMagazine(stack);
        if (engine.kind() != SednaMagazineConfig.Kind.ELECTRIC_ENGINE || amount <= 0L) {
            return 0L;
        }
        int before = magazineCount(stack, engine);
        int accepted = (int) Math.min(amount, engine.capacity() - before);
        setMagazineCount(stack, engine, before + accepted);
        return accepted;
    }

    @Override
    public long dischargeBattery(ItemStack stack, long amount) {
        SednaMagazineConfig engine = effectiveEngineMagazine(stack);
        if (engine.kind() != SednaMagazineConfig.Kind.ELECTRIC_ENGINE || amount <= 0L) {
            return 0L;
        }
        int before = magazineCount(stack, engine);
        int drained = (int) Math.min(amount, before);
        setMagazineCount(stack, engine, before - drained);
        return drained;
    }

    @Override
    protected Optional<LoadedRound> getLoadedRound(Player player, ItemStack stack, SednaMagazineConfig magazine) {
        if (availableOperations(stack) <= 0) {
            return Optional.empty();
        }
        return Optional.of(new LoadedRound(LegacySednaRuntimeBulletConfigs.STONE, availableOperations(stack)));
    }

    @Override
    protected void fire(Level level, Player player, ItemStack stack, GunParts gun, LoadedRound round) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        playLegacyAnimation(stack, gun.mode().configIndex(), LEGACY_ANIM_CYCLE);
        ModMessages.sendLegacyItemAnimation(serverPlayer, LEGACY_ANIM_CYCLE, 0, gun.mode().configIndex());
        SednaWeaponModEvaluator.DrillStats stats = drillStats(stack);
        HitResult hit = pick(serverPlayer, stats.reach());
        if (hit instanceof EntityHitResult entityHit) {
            damageEntity(serverLevel, serverPlayer, entityHit.getEntity(), gun.receiver(), stats);
        } else if (hit instanceof BlockHitResult blockHit) {
            breakDrillArea(serverLevel, serverPlayer, stack, blockHit.getBlockPos(), stats);
        }
        consumeOperation(stack);
    }

    @Override
    protected void clickPrimary(ServerPlayer player, ItemStack stack, GunParts gun) {
        int configIndex = gun.mode().configIndex();
        SednaGunConfig.GunState state = gunState(stack, configIndex);
        if (state == SednaGunConfig.GunState.IDLE) {
            if (availableOperations(stack) > 0) {
                fire(player.level(), player, stack, gun,
                        new LoadedRound(LegacySednaRuntimeBulletConfigs.STONE, availableOperations(stack)));
                setGunState(stack, configIndex, SednaGunConfig.GunState.COOLDOWN);
                setTimer(stack, configIndex, gun.receiver().delayAfterFire());
            } else if (gun.receiver().doesDryFire()) {
                playLegacyAnimation(stack, configIndex, LEGACY_ANIM_CYCLE_DRY);
                ModMessages.sendLegacyItemAnimation(player, LEGACY_ANIM_CYCLE_DRY, 0, configIndex);
                setGunState(stack, configIndex, gun.receiver().refireAfterDry()
                        ? SednaGunConfig.GunState.COOLDOWN
                        : SednaGunConfig.GunState.DRAWING);
                setTimer(stack, configIndex, gun.receiver().delayAfterDryFire());
            }
        }
    }

    private void damageEntity(ServerLevel level, ServerPlayer player, Entity target, SednaReceiverConfig receiver,
            SednaWeaponModEvaluator.DrillStats stats) {
        if (target == player) {
            return;
        }
        if (!(target instanceof LivingEntity)) {
            target.hurt(level.damageSources().playerAttack(player), receiver.baseDamage());
            return;
        }
        EntityDamageUtil.attackEntityFromNt(target, level.damageSources().playerAttack(player),
                receiver.baseDamage(), true, true, 0.1D, stats.dtNegation(), stats.piercing());
    }

    private void breakDrillArea(ServerLevel level, ServerPlayer player, ItemStack stack, BlockPos origin,
            SednaWeaponModEvaluator.DrillStats stats) {
        int aoe = player.isShiftKeyDown() ? 0 : stats.area();
        boolean didPlink = false;
        Map<Enchantment, Integer> originalEnchantments = applyTemporaryFortune(stack, stats.fortuneBonus());
        try {
            for (int dx = -aoe; dx <= aoe; dx++) {
                for (int dy = -aoe; dy <= aoe; dy++) {
                    for (int dz = -aoe; dz <= aoe; dz++) {
                        BlockPos pos = origin.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(pos);
                        if (state.isAir()) {
                            continue;
                        }
                        if (!breakExtraBlock(level, player, pos, origin) && !didPlink) {
                            HbmPlayerProperties.plink(player, SoundEvents.ITEM_BREAK, 0.5F,
                                    0.8F + player.getRandom().nextFloat() * 0.6F);
                            didPlink = true;
                        }
                    }
                }
            }
        } finally {
            restoreEnchantments(stack, originalEnchantments);
        }
    }

    private boolean breakExtraBlock(ServerLevel level, ServerPlayer player, BlockPos pos, BlockPos origin) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        if (!pos.equals(origin)) {
            BlockState reference = level.getBlockState(origin);
            float referenceStrength = reference.getDestroyProgress(player, level, origin);
            float strength = state.getDestroyProgress(player, level, pos);
            if (strength <= 0.0F || referenceStrength < 0.0F || referenceStrength / strength > 10.0F) {
                return false;
            }
        }
        GameType previous = player.gameMode.getGameModeForPlayer();
        boolean destroyed = player.gameMode.destroyBlock(pos);
        if (previous != player.gameMode.getGameModeForPlayer()) {
            player.setGameMode(previous);
        }
        return destroyed;
    }

    private int availableOperations(ItemStack stack) {
        int cost = operationCost(stack);
        return cost <= 0 ? 0 : getFill(stack) / cost;
    }

    private void consumeOperation(ItemStack stack) {
        SednaMagazineConfig engine = effectiveEngineMagazine(stack);
        setMagazineCount(stack, engine, Math.max(0, getFill(stack) - operationCost(stack)));
    }

    private int operationCost(ItemStack stack) {
        return effectiveEngineMagazine(stack).kind() == SednaMagazineConfig.Kind.ELECTRIC_ENGINE
                ? 1_000
                : LIQUID_CONSUMPTION;
    }

    private HitResult pick(ServerPlayer player, double reach) {
        HitResult blockHit = player.pick(reach, 0.0F, false);
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(reach));
        EntityHitResult entityHit = raycastEntities(player, eye, end);
        if (entityHit == null) {
            return blockHit;
        }
        double entityDistance = entityHit.getLocation().distanceToSqr(eye);
        double blockDistance = blockHit == null ? Double.MAX_VALUE : blockHit.getLocation().distanceToSqr(eye);
        return entityDistance < blockDistance ? entityHit : blockHit;
    }

    private EntityHitResult raycastEntities(ServerPlayer player, Vec3 start, Vec3 end) {
        AABB search = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);
        Entity closest = null;
        Vec3 closestHit = null;
        double closestDistance = Double.MAX_VALUE;
        for (Entity candidate : player.level().getEntities(player, search, entity -> entity.isPickable() && entity.isAlive())) {
            AABB box = candidate.getBoundingBox().inflate(candidate.getPickRadius());
            Optional<Vec3> hit = box.clip(start, end);
            if (box.contains(start)) {
                hit = Optional.of(start);
            }
            if (hit.isEmpty()) {
                continue;
            }
            double distance = start.distanceToSqr(hit.get());
            if (distance < closestDistance) {
                closest = candidate;
                closestHit = hit.get();
                closestDistance = distance;
            }
        }
        return closest == null ? null : new EntityHitResult(closest, closestHit);
    }

    private FluidType firstAcceptedFluid(ItemStack stack) {
        SednaMagazineConfig engine = effectiveEngineMagazine(stack);
        if (engine.acceptedFluidNames().isEmpty()) {
            return HbmFluids.NONE;
        }
        return HbmFluids.fromName(engine.acceptedFluidNames().get(0));
    }

    private SednaMagazineConfig effectiveEngineMagazine(ItemStack stack) {
        SednaMagazineConfig base = gunConfig().magazines().stream()
                .filter(config -> config.kind() == SednaMagazineConfig.Kind.LIQUID_ENGINE)
                .findFirst()
                .orElseThrow();
        return stack.isEmpty()
                ? base
                : SednaWeaponModEvaluator.effectiveMagazine(stack, gunConfig().legacyName(), 0, base);
    }

    private SednaWeaponModEvaluator.DrillStats drillStats(ItemStack stack) {
        return stack.isEmpty()
                ? BASE_STATS
                : SednaWeaponModEvaluator.effectiveDrillStats(stack, 0, BASE_STATS);
    }

    @Nullable
    private Map<Enchantment, Integer> applyTemporaryFortune(ItemStack stack, int fortuneBonus) {
        if (fortuneBonus <= 0) {
            return null;
        }
        Map<Enchantment, Integer> original = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(stack));
        int current = original.getOrDefault(Enchantments.BLOCK_FORTUNE, 0);
        Map<Enchantment, Integer> modified = new LinkedHashMap<>(original);
        modified.put(Enchantments.BLOCK_FORTUNE, current + fortuneBonus);
        EnchantmentHelper.setEnchantments(modified, stack);
        return original;
    }

    private void restoreEnchantments(ItemStack stack, @Nullable Map<Enchantment, Integer> originalEnchantments) {
        if (originalEnchantments != null) {
            EnchantmentHelper.setEnchantments(originalEnchantments, stack);
        }
    }

    private static final class CombinedCapabilityProvider implements ICapabilityProvider {
        private final ICapabilityProvider first;
        private final ICapabilityProvider second;

        private CombinedCapabilityProvider(ICapabilityProvider first, ICapabilityProvider second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
                @Nullable net.minecraft.core.Direction side) {
            LazyOptional<T> result = first.getCapability(capability, side);
            return result.isPresent() ? result : second.getCapability(capability, side);
        }
    }
}
