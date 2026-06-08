package com.hbm.ntm.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hbm.ntm.api.item.DepthRockTool;
import com.hbm.ntm.ability.AvailableAbilities;
import com.hbm.ntm.ability.IBaseAbility;
import com.hbm.ntm.ability.ToolAbilityConfiguration;
import com.hbm.ntm.ability.ToolDigContext;
import com.hbm.ntm.ability.ToolHarvestAbilities;
import com.hbm.ntm.ability.ToolHarvestContext;
import com.hbm.ntm.ability.ToolPreset;
import com.hbm.ntm.ability.WeaponHitContext;
import com.hbm.ntm.menu.ToolAbilityMenu;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmKeybindReceiver;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class HbmAbilityToolItem extends DiggerItem implements DepthRockTool, HbmKeybindReceiver {
    private static final int NOTICE_TOOL_ABILITY = 14;
    private static final int NOTICE_MILLIS = 2_000;
    private static final UUID MOVEMENT_SPEED_UUID = UUID.fromString("44a80d25-66e4-44ff-9f07-5da03ea6ce3e");
    private static final ThreadLocal<Boolean> HANDLING_ABILITY_BREAK = ThreadLocal.withInitial(() -> false);

    protected final AvailableAbilities availableAbilities = new AvailableAbilities().addToolAbilities();
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private final Tier toolTier;
    private final List<TagKey<Block>> effectiveMineableTags;
    private final float attackDamage;
    private final double movementModifier;
    private boolean shears;
    private boolean depthRockBreaker;

    public HbmAbilityToolItem(float attackDamageModifier, double movementModifier, Tier tier,
                              TagKey<Block> mineableBlocks, Properties properties) {
        this(attackDamageModifier, movementModifier, tier, List.of(mineableBlocks), properties);
    }

    protected HbmAbilityToolItem(float attackDamageModifier, double movementModifier, Tier tier,
                                 List<TagKey<Block>> mineableBlocks, Properties properties) {
        super(0.0F, 0.0F, tier, mineableBlocks.get(0), properties);
        this.toolTier = tier;
        this.effectiveMineableTags = List.copyOf(mineableBlocks);
        this.attackDamage = attackDamageModifier;
        this.movementModifier = movementModifier;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Tool modifier", attackDamageModifier, AttributeModifier.Operation.ADDITION));
        if (movementModifier != 0.0D) {
            builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(MOVEMENT_SPEED_UUID,
                    "Tool movement modifier", movementModifier, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        this.defaultModifiers = builder.build();
    }

    public static HbmAbilityToolItem pickaxe(float attackDamageModifier, double movementModifier, Tier tier,
                                             Properties properties) {
        return new HbmAbilityToolItem(attackDamageModifier, movementModifier, tier,
                BlockTags.MINEABLE_WITH_PICKAXE, properties);
    }

    public static HbmAbilityToolItem axe(float attackDamageModifier, double movementModifier, Tier tier,
                                         Properties properties) {
        return new HbmAbilityToolItem(attackDamageModifier, movementModifier, tier,
                BlockTags.MINEABLE_WITH_AXE, properties);
    }

    public static HbmAbilityToolItem shovel(float attackDamageModifier, double movementModifier, Tier tier,
                                            Properties properties) {
        return new HbmAbilityToolItem(attackDamageModifier, movementModifier, tier,
                BlockTags.MINEABLE_WITH_SHOVEL, properties);
    }

    public static HbmAbilityToolItem miner(float attackDamageModifier, double movementModifier, Tier tier,
                                           Properties properties) {
        return new HbmAbilityToolItem(attackDamageModifier, movementModifier, tier,
                List.of(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_SHOVEL), properties);
    }

    public HbmAbilityToolItem addAbility(IBaseAbility ability, int level) {
        availableAbilities.addAbility(ability, level);
        return this;
    }

    public HbmAbilityToolItem setShears() {
        this.shears = true;
        return this;
    }

    public HbmAbilityToolItem setDepthRockBreaker() {
        this.depthRockBreaker = true;
        return this;
    }

    public AvailableAbilities availableAbilities() {
        return availableAbilities;
    }

    public float legacyAttackDamage() {
        return attackDamage;
    }

    public double legacyMovementModifier() {
        return movementModifier;
    }

    public boolean canOperate(ItemStack stack) {
        return true;
    }

    public void hurtAbilityTool(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
    }

    public ToolAbilityConfiguration getConfiguration(ItemStack stack) {
        return ToolAbilityConfiguration.get(stack::getTag, availableAbilities);
    }

    public void setConfiguration(ItemStack stack, ToolAbilityConfiguration configuration) {
        configuration.writeToNBT(stack.getOrCreateTag());
    }

    public boolean handleAbilityBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player)) {
            return false;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || stack.getItem() != this || !canOperate(stack) || !canHarvestBlock(stack, event.getState())) {
            return false;
        }

        ToolAbilityConfiguration configuration = getConfiguration(stack);
        ToolPreset preset = configuration.getActivePreset();
        BlockPos origin = event.getPos();
        BlockState state = event.getState();
        BlockEntity blockEntity = level.getBlockEntity(origin);
        ToolHarvestContext harvestContext = ToolHarvestContext.create(level, origin, player, stack, state, blockEntity, origin);

        runWithAbilityBreakGuard(() -> {
            preset.harvestAbility.preHarvestAll(preset.harvestAbilityLevel, harvestContext);
            try {
                ToolDigContext digContext = new ToolDigContext(level, origin, player, stack,
                        this::breakExtraBlock, findHitResult(player, origin));
                boolean skipReferenceBlock = preset.areaAbility.onDig(preset.areaAbilityLevel, digContext);
                if (!skipReferenceBlock) {
                    harvestBlock(level, origin, player, origin, stack, preset, false);
                }
            } finally {
                preset.harvestAbility.postHarvestAll(preset.harvestAbilityLevel, harvestContext);
            }
        });

        return true;
    }

    protected void breakExtraBlock(Level level, BlockPos pos, Player player, BlockPos origin, ItemStack toolStack) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ToolAbilityConfiguration configuration = getConfiguration(toolStack);
        harvestBlock(serverLevel, pos, serverPlayer, origin, toolStack, configuration.getActivePreset(), true);
    }

    protected boolean harvestBlock(ServerLevel level, BlockPos pos, ServerPlayer player, BlockPos origin,
                                   ItemStack toolStack, ToolPreset preset, boolean postBreakEvent) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !canHarvestBlock(toolStack, state)
                || state.getDestroySpeed(level, pos) < 0.0F && !canBreakDepthRock(toolStack, level, player, state, pos)) {
            return false;
        }

        BlockState reference = level.getBlockState(origin);
        float referenceStrength = reference.getDestroyProgress(player, level, origin);
        float strength = state.getDestroyProgress(player, level, pos);
        if (strength <= 0.0F || referenceStrength < 0.0F || referenceStrength / strength > 10.0F) {
            return false;
        }

        if (postBreakEvent) {
            int experience = runWithAbilityBreakGuard(() -> ForgeHooks.onBlockBreakEvent(
                    level, currentGameType(player), player, pos));
            if (experience == -1) {
                return false;
            }
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        ToolHarvestContext context = ToolHarvestContext.create(level, pos, player, toolStack, state, blockEntity, origin);
        preset.harvestAbility.onHarvestBlock(preset.harvestAbilityLevel, context);
        return true;
    }

    protected boolean canHarvestBlock(ItemStack stack, BlockState state) {
        if (!canOperate(stack)) {
            return false;
        }
        if (getConfiguration(stack).getActivePreset().harvestAbility == ToolHarvestAbilities.SILK) {
            return true;
        }
        return stack.isCorrectToolForDrops(state) || getDestroySpeed(stack, state) > 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return canOperate(stack)
                && (getConfiguration(stack).getActivePreset().harvestAbility == ToolHarvestAbilities.SILK
                || isShearsCorrectToolForDrops(state)
                || super.isCorrectToolForDrops(stack, state)
                || isSecondaryEffectiveFor(state) && !state.requiresCorrectToolForDrops());
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!canOperate(stack)) {
            return 1.0F;
        }
        if (shears) {
            float shearSpeed = getShearsDestroySpeed(state);
            if (shearSpeed > 0.0F) {
                return shearSpeed;
            }
        }
        return isEffectiveFor(state) ? toolTier.getSpeed() : super.getDestroySpeed(stack, state);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (shears) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof GrowingPlantHeadBlock growingPlantHeadBlock && !growingPlantHeadBlock.isMaxAge(state)) {
                Player player = context.getPlayer();
                ItemStack stack = context.getItemInHand();
                level.playSound(player, pos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
                BlockState grownState = growingPlantHeadBlock.getMaxAgeState(state);
                level.setBlockAndUpdate(pos, grownState);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, grownState));
                if (player != null) {
                    hurtAbilityTool(stack, player);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useOn(context);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof ServerPlayer player && canOperate(stack)) {
            WeaponHitContext context = new WeaponHitContext(attacker.level(), player, victim, stack);
            availableAbilities.getWeaponAbilities().forEach((ability, level) -> ability.onHit(level, context));
        }
        return super.hurtEnemy(stack, victim, attacker);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!canOperate(stack)) {
            return false;
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || !getConfiguration(stack).getActivePreset().isNone();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<net.minecraft.network.chat.Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        availableAbilities.addInformation(tooltip);
        if (depthRockBreaker) {
            tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.hbm_ntm_rebirth.depth_rock_breaker")
                    .withStyle(net.minecraft.ChatFormatting.RED));
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return shears && ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction)
                || super.canPerformAction(stack, toolAction);
    }

    @Override
    public boolean canBreakRock(Level level, Player player, ItemStack tool, BlockState state, BlockPos pos) {
        return canOperate(tool) && depthRockBreaker;
    }

    @Override
    public boolean canHandleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind) {
        return keybind == HbmKeybind.ABILITY_CYCLE || keybind == HbmKeybind.ABILITY_ALT;
    }

    @Override
    public void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed) {
        if (!pressed || !canOperate(stack)) {
            return;
        }
        if (keybind == HbmKeybind.ABILITY_ALT) {
            NetworkHooks.openScreen(player,
                    new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                            new ToolAbilityMenu(containerId, inventory, InteractionHand.MAIN_HAND),
                            Component.translatable("container.hbm_ntm_rebirth.tool_ability")),
                    buffer -> buffer.writeEnum(InteractionHand.MAIN_HAND));
            return;
        }
        if (keybind != HbmKeybind.ABILITY_CYCLE) {
            return;
        }

        ToolAbilityConfiguration configuration = getConfiguration(stack);
        if (configuration.presets().size() < 2) {
            return;
        }

        configuration.cycle(player.isShiftKeyDown());
        setConfiguration(stack, configuration);
        ModMessages.informPlayer(player, configuration.getActivePreset().getMessage(), NOTICE_TOOL_ABILITY, NOTICE_MILLIS);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.25F, configuration.getActivePreset().isNone() ? 0.75F : 1.25F);
    }

    public static boolean isHandlingAbilityBreak() {
        return HANDLING_ABILITY_BREAK.get();
    }

    public static void runWithAbilityBreakGuard(Runnable action) {
        boolean previous = HANDLING_ABILITY_BREAK.get();
        HANDLING_ABILITY_BREAK.set(true);
        try {
            action.run();
        } finally {
            HANDLING_ABILITY_BREAK.set(previous);
        }
    }

    public static <T> T runWithAbilityBreakGuard(java.util.function.Supplier<T> action) {
        boolean previous = HANDLING_ABILITY_BREAK.get();
        HANDLING_ABILITY_BREAK.set(true);
        try {
            return action.get();
        } finally {
            HANDLING_ABILITY_BREAK.set(previous);
        }
    }

    @Nullable
    private static BlockHitResult findHitResult(ServerPlayer player, BlockPos pos) {
        HitResult hitResult = player.pick(5.0D, 0.0F, false);
        if (hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getBlockPos().equals(pos)) {
            return blockHitResult;
        }
        return null;
    }

    private static GameType currentGameType(ServerPlayer player) {
        return player.gameMode.getGameModeForPlayer();
    }

    private boolean isEffectiveFor(BlockState state) {
        return effectiveMineableTags.stream().anyMatch(state::is);
    }

    private boolean isSecondaryEffectiveFor(BlockState state) {
        return effectiveMineableTags.size() > 1 && effectiveMineableTags.stream().skip(1).anyMatch(state::is);
    }

    private boolean canBreakDepthRock(ItemStack stack, Level level, Player player, BlockState state, BlockPos pos) {
        return state.getBlock() instanceof com.hbm.ntm.block.LegacyDepthBlock
                && canBreakRock(level, player, stack, state, pos);
    }

    private boolean isShearsCorrectToolForDrops(BlockState state) {
        return shears && (state.is(Blocks.COBWEB)
                || state.is(Blocks.REDSTONE_WIRE)
                || state.is(Blocks.TRIPWIRE));
    }

    private float getShearsDestroySpeed(BlockState state) {
        if (state.is(Blocks.COBWEB) || state.is(BlockTags.LEAVES)) {
            return 15.0F;
        }
        if (state.is(BlockTags.WOOL)) {
            return 5.0F;
        }
        if (state.is(Blocks.VINE) || state.is(Blocks.GLOW_LICHEN)) {
            return 2.0F;
        }
        return 0.0F;
    }
}
