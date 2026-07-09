package com.hbm.ntm.item;

import com.hbm.ntm.entity.effect.VortexEntity;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class LegacyConserveItem extends Item {
    private final Type type;

    public LegacyConserveItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public Type type() {
        return type;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.canEat(false)) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            player.getInventory().add(new ItemStack(ModItems.CAN_KEY.get()));
            switch (type) {
                case BHOLE -> {
                    VortexEntity vortex = new VortexEntity(level, 0.5F);
                    vortex.setShrinkRate(0.01F);
                    vortex.noBreak();
                    vortex.moveTo(player.getX(), player.getY(), player.getZ(), 0.0F, 0.0F);
                    level.addFreshEntity(vortex);
                }
                case RECURSION -> {
                    if (level.random.nextInt(10) > 0) {
                        player.getInventory().add(new ItemStack(ModItems.CANNED_RECURSION.get()));
                    }
                }
                case FIST -> player.hurt(level.damageSources().magic(), 2.0F);
                default -> {
                }
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int i = 0; i < type.tooltipLines; i++) {
            tooltip.add(Component.translatable(getDescriptionId() + ".desc." + i));
        }
    }

    public enum Type {
        BEEF(0, "canned_beef", 8, 0.75F),
        TUNA(1, "canned_tuna", 4, 0.75F),
        MYSTERY(2, "canned_mystery", 6, 0.5F),
        PASHTET(3, "canned_pashtet", 4, 0.5F),
        CHEESE(4, "canned_cheese", 3, 1.0F),
        SLIME(5, "canned_slime", 15, 5.0F),
        MILK(6, "canned_milk", 5, 0.25F),
        ASS(7, "canned_ass", 6, 0.75F),
        PIZZA(8, "canned_pizza", 8, 75.0F),
        TUBE(9, "canned_tube", 2, 0.25F),
        TOMATO(10, "canned_tomato", 4, 0.5F),
        ASBESTOS(11, "canned_asbestos", 7, 1.0F),
        BHOLE(12, "canned_bhole", 10, 1.0F),
        HOTDOGS(13, "canned_hotdogs", 5, 0.75F),
        LEFTOVERS(14, "canned_leftovers", 1, 0.1F),
        YOGURT(15, "canned_yogurt", 3, 0.5F),
        STEW(16, "canned_stew", 5, 0.5F),
        CHINESE(17, "canned_chinese", 6, 0.1F),
        OIL(18, "canned_oil", 3, 1.0F),
        FIST(19, "canned_fist", 6, 0.75F),
        SPAM(20, "canned_spam", 8, 1.0F, 19),
        FRIED(21, "canned_fried", 10, 0.75F),
        NAPALM(22, "canned_napalm", 6, 1.0F),
        DIESEL(23, "canned_diesel", 6, 1.0F),
        KEROSENE(24, "canned_kerosene", 6, 1.0F),
        RECURSION(25, "canned_recursion", 1, 1.0F),
        BARK(26, "canned_bark", 2, 1.0F);

        private final int legacyMeta;
        private final String registryName;
        private final int foodLevel;
        private final float saturation;
        private final int tooltipLines;

        Type(int legacyMeta, String registryName, int foodLevel, float saturation) {
            this(legacyMeta, registryName, foodLevel, saturation, 1);
        }

        Type(int legacyMeta, String registryName, int foodLevel, float saturation, int tooltipLines) {
            this.legacyMeta = legacyMeta;
            this.registryName = registryName;
            this.foodLevel = foodLevel;
            this.saturation = saturation;
            this.tooltipLines = tooltipLines;
        }

        public int legacyMeta() {
            return legacyMeta;
        }

        public String registryName() {
            return registryName;
        }

        public FoodProperties foodProperties() {
            return new FoodProperties.Builder()
                    .nutrition(foodLevel)
                    .saturationMod(saturation)
                    .build();
        }
    }
}
