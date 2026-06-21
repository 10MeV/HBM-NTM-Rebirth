package com.hbm.ntm.turret;

import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmMathUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TurretMaxwellBlockEntity extends TurretBlockEntityBase implements LegacyUpgradeInfoProvider {
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = createValidUpgrades();

    private int speedLevel;
    private int effectLevel;
    private int powerLevel;
    private int afterburnLevel;
    private int overdriveLevel;
    private int checkDelay;
    private boolean screm;
    private boolean beamShotPacket;

    public TurretMaxwellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_MAXWELL.get(), pos, state, 10_000_000L, 10_000_000L);
    }

    @Override
    protected String getContainerKey() {
        return "container.hbm_ntm_rebirth.turret_maxwell";
    }

    @Override
    protected String getGuiTextureFile() {
        return "gui_turret_maxwell.png";
    }

    @Override
    public List<ItemStack> getAmmoTypesForDisplay() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(ModItems.UPGRADE_SPEED_1.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_SPEED_2.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_SPEED_3.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_EFFECT_1.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_EFFECT_2.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_EFFECT_3.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_POWER_1.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_POWER_2.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_POWER_3.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_AFTERBURN_1.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_AFTERBURN_2.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_AFTERBURN_3.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_OVERDRIVE_1.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_OVERDRIVE_2.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_OVERDRIVE_3.get()));
        stacks.add(new ItemStack(ModItems.UPGRADE_SCREM.get()));
        return stacks;
    }

    @Override
    protected void updateServerTick() {
        if (checkDelay <= 0) {
            checkDelay = 20;
            LegacyMachineUpgradeManager.Levels levels = LegacyMachineUpgradeManager.checkSlots(
                    getItems(), SLOT_AMMO_START, SLOT_AMMO_END, VALID_UPGRADES);
            speedLevel = levels.getLevel(UpgradeType.SPEED);
            effectLevel = levels.getLevel(UpgradeType.EFFECT);
            powerLevel = levels.getLevel(UpgradeType.POWER);
            afterburnLevel = levels.getLevel(UpgradeType.AFTERBURN);
            overdriveLevel = levels.getLevel(UpgradeType.OVERDRIVE);
            screm = false;
            for (int slot = SLOT_AMMO_START; slot <= SLOT_AMMO_END; slot++) {
                if (getItems().getStackInSlot(slot).is(ModItems.UPGRADE_SCREM.get())) {
                    screm = true;
                    break;
                }
            }
        }
        checkDelay--;
    }

    @Override
    protected long getConsumption() {
        return 10_000L - powerLevel * 300L;
    }

    @Override
    protected double getDetectorRange() {
        return 64.0D + effectLevel * 3.0D;
    }

    @Override
    protected double getDetectorGrace() {
        return 5.0D;
    }

    @Override
    protected double getAcceptableInaccuracy() {
        return 2.0D;
    }

    @Override
    protected double getTurretYawSpeed() {
        return 9.0D;
    }

    @Override
    protected double getTurretPitchSpeed() {
        return 6.0D;
    }

    @Override
    protected double getTurretDepression() {
        return 35.0D;
    }

    @Override
    protected double getTurretElevation() {
        return 40.0D;
    }

    @Override
    protected double getHeightOffset() {
        return 2.0D;
    }

    @Override
    protected double getBarrelLength() {
        return 2.125D;
    }

    @Override
    protected void tickClientSpecificAnimations() {
        updateClientBeamDistanceFromTarget();
    }

    @Override
    protected void updateFiringTick() {
        if (level == null) {
            return;
        }
        long demand = getConsumption() * 10L;
        if (getPower() < demand) {
            return;
        }
        Entity target = getTarget();
        if (target == null) {
            return;
        }
        float damage = (overdriveLevel * 10.0F + speedLevel + 1.0F) * 0.25F;
        EntityDamageUtil.attackEntityFromIgnoreIFrame(target, ModDamageSources.source(level, ModDamageSources.MICROWAVE), damage);
        if (afterburnLevel > 0) {
            target.setSecondsOnFire(afterburnLevel * 3);
        }
        if (!target.isAlive() && target instanceof LivingEntity) {
            ParticleUtil.spawnGiblets(target, ParticleUtil.GIBLET_MEAT);
            if (screm) {
                LegacySoundPlayer.playSoundEffect(level, target.getX(), target.getY(), target.getZ(),
                        "hbm:block.screm", SoundSource.HOSTILE, 20.0F, 1.0F);
            } else {
                LegacySoundPlayer.playSoundEffect(level, target.getX(), target.getY(), target.getZ(),
                        "mob.zombie.woodbreak", SoundSource.HOSTILE,
                        2.0F, 0.95F + level.random.nextFloat() * 0.2F);
            }
        }
        setPower(getPower() - demand);
        sendBeamShotPacket();
    }

    @Override
    protected boolean shouldSyncBeamState() {
        return false;
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeBoolean(beamShotPacket);
        if (!beamShotPacket) {
            super.serializeLegacyBufPacket(data);
        }
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        boolean shot = data.readBoolean();
        if (shot) {
            triggerClientBeamFromTarget(5);
        } else {
            super.deserializeLegacyBufPacket(data);
        }
    }

    private void sendBeamShotPacket() {
        beamShotPacket = true;
        sendBufPacketThreaded(250);
        beamShotPacket = false;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<Component> info, boolean extendedInfo) {
        info.add(Component.literal(">>> ")
                .append(Component.translatableWithFallback("block.hbm_ntm_rebirth.turret_maxwell",
                        "High-Energy MASER Turret \"Maxwell\""))
                .append(" <<<")
                .withStyle(ChatFormatting.YELLOW));
        switch (type) {
            case SPEED -> info.add(Component.literal("Damage +0." + (level * 25) + "/t")
                    .withStyle(ChatFormatting.GREEN));
            case POWER -> info.add(Component.translatableWithFallback(KEY_CONSUMPTION, "Consumption %s",
                    "-" + (level * 3) + "%").withStyle(ChatFormatting.GREEN));
            case EFFECT -> info.add(Component.translatableWithFallback(KEY_RANGE, "Range %s",
                    "+" + (level * 3) + "m").withStyle(ChatFormatting.GREEN));
            case AFTERBURN -> info.add(Component.literal("Afterburn +3s").withStyle(ChatFormatting.GREEN));
            case OVERDRIVE -> info.add(Component.literal("YES")
                    .withStyle(HbmMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public Map<UpgradeType, Integer> getValidUpgrades() {
        return VALID_UPGRADES;
    }

    private static Map<UpgradeType, Integer> createValidUpgrades() {
        EnumMap<UpgradeType, Integer> upgrades = new EnumMap<>(UpgradeType.class);
        upgrades.put(UpgradeType.SPEED, 27);
        upgrades.put(UpgradeType.POWER, 27);
        upgrades.put(UpgradeType.EFFECT, 27);
        upgrades.put(UpgradeType.AFTERBURN, 27);
        upgrades.put(UpgradeType.OVERDRIVE, 27);
        return Map.copyOf(upgrades);
    }
}
