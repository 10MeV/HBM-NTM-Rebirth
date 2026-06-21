package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BedrockOreItem extends Item {
    private static final String TAG_GRADE = "grade";
    private static final String TAG_TYPE = "type";

    public BedrockOreItem(Properties properties) {
        super(properties);
    }

    public static ItemStack make(BedrockOreGrade grade, BedrockOreType type) {
        return make(grade, type, 1);
    }

    public static ItemStack make(BedrockOreGrade grade, BedrockOreType type, int amount) {
        ItemStack stack = new ItemStack(ModItems.BEDROCK_ORE.get(), amount);
        setGrade(stack, grade);
        setType(stack, type);
        return stack;
    }

    public static BedrockOreGrade getGrade(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return BedrockOreGrade.BASE;
        }
        return BedrockOreGrade.bySerializedName(tag.getString(TAG_GRADE));
    }

    public static BedrockOreType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return BedrockOreType.LIGHT_METAL;
        }
        return BedrockOreType.bySuffix(tag.getString(TAG_TYPE));
    }

    public static void setGrade(ItemStack stack, BedrockOreGrade grade) {
        stack.getOrCreateTag().putString(TAG_GRADE, (grade == null ? BedrockOreGrade.BASE : grade).serializedName());
    }

    public static void setType(ItemStack stack, BedrockOreType type) {
        stack.getOrCreateTag().putString(TAG_TYPE, (type == null ? BedrockOreType.LIGHT_METAL : type).suffix());
    }

    public static int tint(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return getType(stack).lightColor();
        }
        if (tintIndex == 1) {
            return getGrade(stack).tint();
        }
        return 0xFFFFFF;
    }

    @Override
    public Component getName(ItemStack stack) {
        BedrockOreType type = getType(stack);
        BedrockOreGrade grade = getGrade(stack);
        Component typeName = Component.translatable(type.translationKey());
        return Component.translatable(grade.translationKey(), typeName);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (ProcessingTrait trait : getGrade(stack).traits()) {
            tooltip.add(Component.translatable("item.hbm_ntm_rebirth.bedrock_ore.trait."
                    + trait.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY));
        }
    }

    public enum BedrockOreType {
        LIGHT_METAL("light", 0xFFFFFF, 0x353535),
        HEAVY_METAL("heavy", 0x868686, 0x000000),
        RARE_EARTH("rare", 0xE6E6B6, 0x1C1C00),
        ACTINIDE("actinide", 0xC1C7BD, 0x2B3227),
        NON_METAL("nonmetal", 0xAFAFAF, 0x0F0F0F),
        CRYSTALLINE("crystal", 0xE2FFFA, 0x1E8A77);

        private final String suffix;
        private final int lightColor;
        private final int darkColor;

        BedrockOreType(String suffix, int lightColor, int darkColor) {
            this.suffix = suffix;
            this.lightColor = lightColor;
            this.darkColor = darkColor;
        }

        public String suffix() {
            return suffix;
        }

        public int lightColor() {
            return lightColor;
        }

        public int darkColor() {
            return darkColor;
        }

        public String translationKey() {
            return "item.hbm_ntm_rebirth.bedrock_ore.type." + suffix + ".name";
        }

        public static BedrockOreType bySuffix(String suffix) {
            for (BedrockOreType type : values()) {
                if (type.suffix.equalsIgnoreCase(suffix)) {
                    return type;
                }
            }
            return LIGHT_METAL;
        }
    }

    public enum ProcessingTrait {
        ROASTED,
        ARC,
        WASHED,
        CENTRIFUGED,
        SULFURIC,
        SOLVENT,
        RAD
    }

    public enum BedrockOreGrade {
        BASE(0xFFFFFF, "base"),
        BASE_ROASTED(0xCFCFCF, "base", ProcessingTrait.ROASTED),
        BASE_WASHED(0xDBE2CB, "base", ProcessingTrait.WASHED),
        PRIMARY(0xFFFFFF, "primary", ProcessingTrait.CENTRIFUGED),
        PRIMARY_ROASTED(0xCFCFCF, "primary", ProcessingTrait.ROASTED),
        PRIMARY_SULFURIC(0xFFFFD3, "primary", ProcessingTrait.SULFURIC),
        PRIMARY_NOSULFURIC(0xD3D4FF, "primary", ProcessingTrait.CENTRIFUGED, ProcessingTrait.SULFURIC),
        PRIMARY_SOLVENT(0xD3F0FF, "primary", ProcessingTrait.SOLVENT),
        PRIMARY_NOSOLVENT(0xFFDED3, "primary", ProcessingTrait.CENTRIFUGED, ProcessingTrait.SOLVENT),
        PRIMARY_RAD(0xECFFD3, "primary", ProcessingTrait.RAD),
        PRIMARY_NORAD(0xEBD3FF, "primary", ProcessingTrait.CENTRIFUGED, ProcessingTrait.RAD),
        PRIMARY_FIRST(0xFFD3D4, "primary", ProcessingTrait.CENTRIFUGED),
        PRIMARY_SECOND(0xD3FFEB, "primary", ProcessingTrait.CENTRIFUGED),
        CRUMBS(0xFFFFFF, "crumbs", ProcessingTrait.CENTRIFUGED),
        SULFURIC_BYPRODUCT(0xFFFFFF, "sulfuric", ProcessingTrait.CENTRIFUGED, ProcessingTrait.SULFURIC),
        SULFURIC_ROASTED(0xCFCFCF, "sulfuric", ProcessingTrait.ROASTED, ProcessingTrait.SULFURIC),
        SULFURIC_ARC(0xC3A2A2, "sulfuric", ProcessingTrait.ARC, ProcessingTrait.SULFURIC),
        SULFURIC_WASHED(0xDBE2CB, "sulfuric", ProcessingTrait.WASHED, ProcessingTrait.SULFURIC),
        SOLVENT_BYPRODUCT(0xFFFFFF, "solvent", ProcessingTrait.CENTRIFUGED, ProcessingTrait.SOLVENT),
        SOLVENT_ROASTED(0xCFCFCF, "solvent", ProcessingTrait.ROASTED, ProcessingTrait.SOLVENT),
        SOLVENT_ARC(0xC3A2A2, "solvent", ProcessingTrait.ARC, ProcessingTrait.SOLVENT),
        SOLVENT_WASHED(0xDBE2CB, "solvent", ProcessingTrait.WASHED, ProcessingTrait.SOLVENT),
        RAD_BYPRODUCT(0xFFFFFF, "rad", ProcessingTrait.CENTRIFUGED, ProcessingTrait.RAD),
        RAD_ROASTED(0xCFCFCF, "rad", ProcessingTrait.ROASTED, ProcessingTrait.RAD),
        RAD_ARC(0xC3A2A2, "rad", ProcessingTrait.ARC, ProcessingTrait.RAD),
        RAD_WASHED(0xDBE2CB, "rad", ProcessingTrait.WASHED, ProcessingTrait.RAD);

        private final int tint;
        private final String prefix;
        private final ProcessingTrait[] traits;

        BedrockOreGrade(int tint, String prefix, ProcessingTrait... traits) {
            this.tint = tint;
            this.prefix = prefix;
            this.traits = traits;
        }

        public int tint() {
            return tint;
        }

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String translationKey() {
            return "item.hbm_ntm_rebirth.bedrock_ore.grade." + serializedName() + ".name";
        }

        public ProcessingTrait[] traits() {
            return traits;
        }

        public static BedrockOreGrade bySerializedName(String name) {
            for (BedrockOreGrade grade : values()) {
                if (grade.serializedName().equalsIgnoreCase(name)) {
                    return grade;
                }
            }
            return BASE;
        }
    }
}
