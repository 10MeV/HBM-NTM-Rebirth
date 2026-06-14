package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.item.ArmorDashProvider;
import com.hbm.ntm.client.renderer.LegacyObjArmorRenderer;
import com.hbm.ntm.radiation.HazmatRegistry;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationData;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

public class FsbArmorItem extends ArmorItem implements ArmorDashProvider {
    private static final String TAG_NEXT_STEP_SOUND_DISTANCE = "hbm_fsb_next_step_sound_distance";
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString("81746137-8b5a-44f9-a89b-ad48dcc7dc11");
    private final ResourceLocation fsbMaterialId;
    private final List<FullSetEffect> fullSetEffects;
    private final boolean noHelmet;
    private final int dashCount;
    private final FullSetTraits fullSetTraits;

    public FsbArmorItem(HbmArmorMaterials material, Type type, Properties properties, List<FullSetEffect> fullSetEffects) {
        this(material, type, properties, fullSetEffects, false, 0, FullSetTraits.NONE);
    }

    public FsbArmorItem(HbmArmorMaterials material, Type type, Properties properties, List<FullSetEffect> fullSetEffects,
            boolean noHelmet, int dashCount) {
        this(material, type, properties, fullSetEffects, noHelmet, dashCount, FullSetTraits.NONE);
    }

    public FsbArmorItem(HbmArmorMaterials material, Type type, Properties properties, List<FullSetEffect> fullSetEffects,
            boolean noHelmet, int dashCount, FullSetTraits fullSetTraits) {
        super(material, type, properties.stacksTo(1));
        this.fsbMaterialId = ResourceLocation.tryParse(material.getName());
        this.fullSetEffects = List.copyOf(fullSetEffects);
        this.noHelmet = noHelmet;
        this.dashCount = Math.max(0, dashCount);
        this.fullSetTraits = fullSetTraits == null ? FullSetTraits.NONE : fullSetTraits;
    }

    public ResourceLocation fsbMaterialId(ItemStack stack) {
        return fsbMaterialId;
    }

    public boolean noHelmetForFsbSet(ItemStack chestplate) {
        return noHelmet;
    }

    public boolean isArmorEnabled(ItemStack stack) {
        return true;
    }

    public boolean hasFullSet(Player player) {
        return hasFullFsbSet(player, false);
    }

    public boolean hasFullSetIgnoreCharge(Player player) {
        return hasFullFsbSet(player, true);
    }

    @Override
    public int getDashes() {
        return dashCount;
    }

    public FullSetTraits fullSetTraits() {
        return fullSetTraits;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyObjArmorRenderer.acceptFsbArmorExtensions(consumer);
    }

    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        if (getType() != Type.CHESTPLATE || !hasFullSet(player)) {
            return;
        }
        if (!level.isClientSide) {
            for (FullSetEffect effect : fullSetEffects) {
                player.addEffect(effect.create());
            }
            playArmorGeiger(level, player);
            fullSetTraits.handleStepSound(player);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!fullSetEffects.isEmpty() || dashCount > 0 || !fullSetTraits.isEmpty()) {
            tooltip.add(Component.translatable("armor.fullSetBonus").withStyle(ChatFormatting.GOLD));
            for (FullSetEffect effect : fullSetEffects) {
                tooltip.add(Component.literal("  " + effect.tooltip()).withStyle(ChatFormatting.AQUA));
            }
            if (dashCount > 0) {
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("armor.dash", dashCount))
                        .withStyle(ChatFormatting.AQUA));
            }
            for (Component line : fullSetTraits.tooltipLines()) {
                tooltip.add(line);
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public static FsbArmorItem chestplate(Player player) {
        if (player == null) {
            return null;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof FsbArmorItem fsb && fsb.hasFullSet(player)) {
            return fsb;
        }
        return null;
    }

    public static void handleFullSetJump(Player player) {
        FsbArmorItem chestplate = chestplate(player);
        if (chestplate == null) {
            return;
        }
        chestplate.fullSetTraits.playJump(player);
    }

    public static void handleFullSetFall(Player player, float fallDistance) {
        FsbArmorItem chestplate = chestplate(player);
        if (chestplate == null) {
            return;
        }
        chestplate.fullSetTraits.handleHardLanding(player, fallDistance);
        chestplate.fullSetTraits.playFall(player);
    }

    public static void reconcileStepHeight(Player player) {
        AttributeInstance stepHeight = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (stepHeight == null) {
            return;
        }
        stepHeight.removeModifier(STEP_HEIGHT_UUID);
        FsbArmorItem chestplate = chestplate(player);
        if (chestplate == null || chestplate.fullSetTraits.stepSize() <= 0) {
            return;
        }
        stepHeight.addTransientModifier(new AttributeModifier(STEP_HEIGHT_UUID, "FSB step height",
                chestplate.fullSetTraits.stepSize(), AttributeModifier.Operation.ADDITION));
    }

    public static boolean hasFullFsbSet(Player player, boolean ignoreCharge) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof FsbArmorItem chestplate)) {
            return false;
        }

        ResourceLocation material = chestplate.fsbMaterialId(chest);
        if (material == null) {
            return false;
        }

        EquipmentSlot[] slots = chestplate.noHelmetForFsbSet(chest)
                ? new EquipmentSlot[] {EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}
                : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (EquipmentSlot slot : slots) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!(armor.getItem() instanceof FsbArmorItem fsb)) {
                return false;
            }
            if (!material.equals(fsb.fsbMaterialId(armor))) {
                return false;
            }
            if (!ignoreCharge && !fsb.isArmorEnabled(armor)) {
                return false;
            }
        }
        return true;
    }

    public static FullSetEffect effect(MobEffect effect, int duration, int amplifier, String tooltip) {
        return effect(() -> effect, duration, amplifier, tooltip);
    }

    public static FullSetEffect effect(Supplier<? extends MobEffect> effect, int duration, int amplifier, String tooltip) {
        return new FullSetEffect(effect, duration, amplifier, tooltip);
    }

    public record FullSetEffect(Supplier<? extends MobEffect> effect, int duration, int amplifier, String tooltip) {
        public FullSetEffect {
            duration = Math.max(1, duration);
            amplifier = Math.max(0, amplifier);
            tooltip = tooltip == null || tooltip.isBlank() ? "Effect" : tooltip;
        }

        MobEffectInstance create() {
            return new MobEffectInstance(effect.get(), duration, amplifier, true, true);
        }
    }

    protected static ResourceLocation legacyMaterialId(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private void playArmorGeiger(Level level, Player player) {
        if (!fullSetTraits.geigerSound() || level.getGameTime() % 5L != 0L) {
            return;
        }
        if (player.getInventory().contains(new ItemStack(ModItems.GEIGER_COUNTER.get()))
                || player.getInventory().contains(new ItemStack(ModItems.DOSIMETER.get()))) {
            return;
        }
        float bufferedRadiation = RadiationData.getRadBuf(player) * HazmatRegistry.calculateRadiationModifier(player);
        if (bufferedRadiation <= 1.0E-5F) {
            return;
        }

        List<Integer> possibleSounds = new ArrayList<>();
        if (bufferedRadiation < 1.0F) {
            possibleSounds.add(0);
        }
        if (bufferedRadiation < 5.0F) {
            possibleSounds.add(0);
        }
        if (bufferedRadiation < 10.0F) {
            possibleSounds.add(1);
        }
        if (bufferedRadiation > 5.0F && bufferedRadiation < 15.0F) {
            possibleSounds.add(2);
        }
        if (bufferedRadiation > 10.0F && bufferedRadiation < 20.0F) {
            possibleSounds.add(3);
        }
        if (bufferedRadiation > 15.0F && bufferedRadiation < 25.0F) {
            possibleSounds.add(4);
        }
        if (bufferedRadiation > 20.0F && bufferedRadiation < 30.0F) {
            possibleSounds.add(5);
        }
        if (bufferedRadiation > 25.0F) {
            possibleSounds.add(6);
        }
        if (possibleSounds.isEmpty()) {
            return;
        }
        int sound = possibleSounds.get(level.random.nextInt(possibleSounds.size()));
        if (sound > 0) {
            LegacySoundPlayer.playLegacyGeiger(level, player, sound);
        }
    }

    public record FullSetTraits(boolean vats, boolean thermal, boolean geigerSound, boolean customGeiger,
            boolean hardLanding, int stepSize, String stepSound, String jumpSound, String fallSound) {
        public static final FullSetTraits NONE = builder().build();

        public boolean isEmpty() {
            return !vats && !thermal && !geigerSound && !customGeiger && !hardLanding
                    && stepSize <= 0 && stepSound == null && jumpSound == null && fallSound == null;
        }

        private List<Component> tooltipLines() {
            List<Component> lines = new ArrayList<>();
            if (geigerSound) {
                lines.add(Component.literal("  ")
                        .append(Component.translatable("armor.geigerSound"))
                        .withStyle(ChatFormatting.GOLD));
            }
            if (customGeiger) {
                lines.add(Component.literal("  ")
                        .append(Component.translatable("armor.geigerHUD"))
                        .withStyle(ChatFormatting.GOLD));
            }
            if (vats) {
                lines.add(Component.literal("  ")
                        .append(Component.translatable("armor.vats"))
                        .withStyle(ChatFormatting.RED));
            }
            if (thermal) {
                lines.add(Component.literal("  ")
                        .append(Component.translatable("armor.thermal"))
                        .withStyle(ChatFormatting.RED));
            }
            if (hardLanding) {
                lines.add(Component.literal("  ")
                        .append(Component.translatable("armor.hardLanding"))
                        .withStyle(ChatFormatting.RED));
            }
            if (stepSize > 0) {
                lines.add(Component.literal("  ")
                        .append(Component.translatable("armor.stepSize", stepSize))
                        .withStyle(ChatFormatting.BLUE));
            }
            return lines;
        }

        private void playJump(Player player) {
            play(player, jumpSound, 0.5F);
        }

        private void playFall(Player player) {
            play(player, fallSound, 0.5F);
        }

        private void handleStepSound(Player player) {
            if (stepSound == null || player == null || !player.onGround()) {
                return;
            }
            float next = player.getPersistentData().getFloat(TAG_NEXT_STEP_SOUND_DISTANCE);
            if (next <= 0.0F) {
                next = player.walkDist + 1.0F;
            }
            if (player.walkDist < next || player.getBlockStateOn().isAir()) {
                player.getPersistentData().putFloat(TAG_NEXT_STEP_SOUND_DISTANCE, next);
                return;
            }
            play(player, stepSound, 0.25F);
            player.getPersistentData().putFloat(TAG_NEXT_STEP_SOUND_DISTANCE, player.walkDist + 1.0F);
        }

        private void handleHardLanding(Player player, float fallDistance) {
            if (!hardLanding || fallDistance <= 10.0F || player.level().isClientSide) {
                return;
            }
            AABB area = player.getBoundingBox().inflate(3.0D, 0.0D, 3.0D);
            for (Entity entity : player.level().getEntities(player, area)) {
                if (entity instanceof ItemEntity) {
                    continue;
                }
                Vec3 offset = new Vec3(player.getX() - entity.getX(), 0.0D, player.getZ() - entity.getZ());
                double distance = offset.length();
                if (distance >= 3.0D) {
                    continue;
                }
                double intensity = 3.0D - distance;
                entity.push(offset.x * intensity * -2.0D, 0.1D * intensity, offset.z * intensity * -2.0D);
                entity.hurt(ModDamageSources.source(player.level(), ModDamageSources.RUBBLE, player),
                        (float) (intensity * 10.0D));
            }
        }

        private static void play(Player player, String sound, float volume) {
            if (player == null || sound == null || sound.isBlank()) {
                return;
            }
            LegacySoundPlayer.playSoundAtPlayer(player, sound, SoundSource.PLAYERS, volume, 1.0F);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private boolean vats;
            private boolean thermal;
            private boolean geigerSound;
            private boolean customGeiger;
            private boolean hardLanding;
            private int stepSize;
            private String stepSound;
            private String jumpSound;
            private String fallSound;

            public Builder vats() {
                this.vats = true;
                return this;
            }

            public Builder thermal() {
                this.thermal = true;
                return this;
            }

            public Builder geigerSound() {
                this.geigerSound = true;
                return this;
            }

            public Builder customGeiger() {
                this.customGeiger = true;
                return this;
            }

            public Builder hardLanding() {
                this.hardLanding = true;
                return this;
            }

            public Builder stepSize(int stepSize) {
                this.stepSize = Math.max(0, stepSize);
                return this;
            }

            public Builder step(String sound) {
                this.stepSound = sound;
                return this;
            }

            public Builder jump(String sound) {
                this.jumpSound = sound;
                return this;
            }

            public Builder fall(String sound) {
                this.fallSound = sound;
                return this;
            }

            public FullSetTraits build() {
                return new FullSetTraits(vats, thermal, geigerSound, customGeiger, hardLanding,
                        stepSize, stepSound, jumpSound, fallSound);
            }
        }
    }
}
