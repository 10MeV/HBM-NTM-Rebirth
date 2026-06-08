package com.hbm.ntm.ability;

import com.hbm.ntm.HbmNtm;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class AvailableAbilities {
    private final LinkedHashMap<IBaseAbility, Integer> abilities = new LinkedHashMap<>();

    public AvailableAbilities addAbility(IBaseAbility ability, int level) {
        if (level < 0 || level >= ability.levels()) {
            HbmNtm.LOGGER.warn("Illegal level {} for ability {}.", level, ability.getName());
            level = Math.max(0, ability.levels() - 1);
        }

        if (abilities.containsKey(ability)) {
            HbmNtm.LOGGER.warn("Ability {} already had level {}, overwriting with level {}.",
                    ability.getName(), abilities.get(ability), level);
        }

        abilities.put(ability, level);
        return this;
    }

    public AvailableAbilities addToolAbilities() {
        addAbility(ToolAreaAbilities.NONE, 0);
        addAbility(ToolHarvestAbilities.NONE, 0);
        return this;
    }

    public AvailableAbilities removeAbility(IBaseAbility ability) {
        abilities.remove(ability);
        return this;
    }

    public boolean supportsAbility(IBaseAbility ability) {
        return abilities.containsKey(ability) && ability.isAllowed();
    }

    public int maxLevel(IBaseAbility ability) {
        return ability.isAllowed() ? abilities.getOrDefault(ability, -1) : -1;
    }

    public Map<IBaseAbility, Integer> get() {
        return Collections.unmodifiableMap(abilities);
    }

    public Map<IWeaponAbility, Integer> getWeaponAbilities() {
        return allowedAbilities(entry -> entry.getKey() instanceof IWeaponAbility)
                .collect(Collectors.toMap(entry -> (IWeaponAbility) entry.getKey(), Map.Entry::getValue,
                        (a, b) -> b, LinkedHashMap::new));
    }

    public Map<IBaseAbility, Integer> getToolAbilities() {
        return allowedAbilities(entry -> entry.getKey() instanceof IToolAreaAbility || entry.getKey() instanceof IToolHarvestAbility)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> b, LinkedHashMap::new));
    }

    public Map<IToolAreaAbility, Integer> getToolAreaAbilities() {
        return allowedAbilities(entry -> entry.getKey() instanceof IToolAreaAbility)
                .collect(Collectors.toMap(entry -> (IToolAreaAbility) entry.getKey(), Map.Entry::getValue,
                        (a, b) -> b, LinkedHashMap::new));
    }

    public Map<IToolHarvestAbility, Integer> getToolHarvestAbilities() {
        return allowedAbilities(entry -> entry.getKey() instanceof IToolHarvestAbility)
                .collect(Collectors.toMap(entry -> (IToolHarvestAbility) entry.getKey(), Map.Entry::getValue,
                        (a, b) -> b, LinkedHashMap::new));
    }

    public int size() {
        return abilities.size();
    }

    public boolean isEmpty() {
        return abilities.isEmpty();
    }

    public void addInformation(List<Component> tooltip) {
        List<Map.Entry<IBaseAbility, Integer>> toolAbilities = allowedAbilities(entry ->
                        (entry.getKey() instanceof IToolAreaAbility && entry.getKey() != ToolAreaAbilities.NONE)
                                || (entry.getKey() instanceof IToolHarvestAbility && entry.getKey() != ToolHarvestAbilities.NONE))
                .sorted(Comparator.comparing(Map.Entry<IBaseAbility, Integer>::getKey)
                        .thenComparing(Map.Entry<IBaseAbility, Integer>::getValue))
                .collect(Collectors.toList());

        if (!toolAbilities.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.abilities"));
            toolAbilities.forEach(entry -> tooltip.add(Component.literal("  ")
                    .append(entry.getKey().getFullName(entry.getValue()).withStyle(ChatFormatting.GOLD))));
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.abilities.cycle"));
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.abilities.reset"));
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.abilities.customize"));
        }

        List<Map.Entry<IBaseAbility, Integer>> weaponAbilities = allowedAbilities(entry ->
                        entry.getKey() instanceof IWeaponAbility && entry.getKey() != WeaponAbilities.NONE)
                .sorted(Comparator.comparing(Map.Entry<IBaseAbility, Integer>::getKey)
                        .thenComparing(Map.Entry<IBaseAbility, Integer>::getValue))
                .collect(Collectors.toList());

        if (!weaponAbilities.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.weapon_modifiers"));
            weaponAbilities.forEach(entry -> tooltip.add(Component.literal("  ")
                    .append(entry.getKey().getFullName(entry.getValue()).withStyle(ChatFormatting.RED))));
        }
    }

    private java.util.stream.Stream<Map.Entry<IBaseAbility, Integer>> allowedAbilities(
            Predicate<Map.Entry<IBaseAbility, Integer>> predicate) {
        return abilities.entrySet().stream()
                .filter(entry -> entry.getKey().isAllowed())
                .filter(predicate);
    }
}
