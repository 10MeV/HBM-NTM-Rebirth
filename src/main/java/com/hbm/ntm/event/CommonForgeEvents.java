package com.hbm.ntm.event;

import com.hbm.ntm.HbmNtm;
import com.hbm.items.armor.IAttackHandler;
import com.hbm.items.armor.IDamageHandler;
import com.hbm.ntm.command.ModCommands;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.config.ServerConfig;
import com.hbm.ntm.config.WeaponConfig;
import com.hbm.ntm.api.entity.RadarScanner;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.damage.DamageClass;
import com.hbm.ntm.damage.DamageResistanceHandler;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.entity.effect.BlackHoleEntity;
import com.hbm.ntm.entity.effect.QuasarEntity;
import com.hbm.ntm.entity.effect.RagingVortexEntity;
import com.hbm.ntm.entity.effect.VortexEntity;
import com.hbm.ntm.explosion.ExplosionChaos;
import com.hbm.ntm.explosion.ExplosionNukeSmall;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.item.EuphemiumArmorItem;
import com.hbm.ntm.item.DnsArmorItem;
import com.hbm.ntm.item.FsbArmorItem;
import com.hbm.ntm.item.HbmAbilityToolItem;
import com.hbm.ntm.item.NcrpaArmorItem;
import com.hbm.ntm.item.No9ArmorItem;
import com.hbm.ntm.item.TrenchmasterArmorItem;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.LoadedTileAccessCache;
import com.hbm.ntm.network.ServerTileBinaryControlTransfers;
import com.hbm.ntm.network.ThreadedPacketDispatcher;
import com.hbm.ntm.network.HbmServerKeybinds;
import com.hbm.ntm.player.HbmExtendedProperties;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.neutron.NeutronHandler;
import com.hbm.ntm.neutron.NeutronNodeWorld;
import com.hbm.ntm.particle.LegacyConfettiUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.CraterRadiationData;
import com.hbm.ntm.radiation.HazardExposureUtil;
import com.hbm.ntm.radiation.HazmatRegistry;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationData;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.uninos.HbmUninosNodespaces;
import com.hbm.ntm.util.AchievementHandler;
import com.hbm.ntm.util.HbmCraftingAdvancementUtil;
import com.hbm.ntm.world.BlockMigrationHelper;
import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonForgeEvents {
    private static final EquipmentSlot[] ARMOR_TICK_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private static final String HAZARD_ENTITY_TICK_KEY = "hbmHazardTick";
    private static final String CONTAGION_ITEM_TAG = "ntmContagion";
    private static final int CRATER_MELT_INTERVAL_TICKS = 4;
    private static final int CRATER_MELT_RADIUS = 64;
    private static final int CRATER_MELT_SAMPLES_PER_PLAYER = 32;
    private static final Map<ResourceKey<Level>, Set<Integer>> TRACKED_ITEM_ENTITIES = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (TrenchmasterArmorItem.tryCancelIncomingAttack(event)) {
            return;
        }
        if (DnsArmorItem.tryCancelIncomingAttack(event)) {
            return;
        }
        if (DamageResistanceHandler.isAbsolute(event.getSource())) {
            return;
        }

        LivingEntity entity = event.getEntity();
        dispatchLegacyArmorAttackHandlers(event, entity);
        if (event.isCanceled()) {
            return;
        }
        float amount = event.getAmount();

        if (EntityDamageUtil.allowSpecialCancel()
                && DamageResistanceHandler.breakdown(entity, event.getSource(), amount).fullyAbsorbed(amount)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        float amount = DamageResistanceHandler.applyLegacyPreResistanceDamageModifiers(entity, event.getSource(),
                event.getAmount());
        if (entity instanceof Player player) {
            amount = DnsArmorItem.applyLegacyPreResistanceHurt(player, event.getSource(), amount);
        }
        amount = DamageResistanceHandler.calculateDamage(entity, event.getSource(), amount);

        if (entity instanceof Player player) {
            amount = HbmPlayerProperties.absorbShieldDamage(player, amount);
            HbmPlayerProperties.markDamaged(player);
        }
        if (HbmLivingProperties.getContagion(entity) > 0 && amount < 100.0F) {
            amount *= 2.0F;
        }
        if (entity instanceof Player player && TrenchmasterArmorItem.ignoresSelfExplosion(player, event.getSource())) {
            amount = 0.0F;
        }

        event.setAmount(amount);
        dispatchLegacyArmorDamageHandlers(event, entity);
        DamageResistanceHandler.notifyDamageDealt(entity, event.getSource(), event.getAmount());
    }

    private static void dispatchLegacyArmorAttackHandlers(LivingAttackEvent event, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }
        for (EquipmentSlot slot : ARMOR_TICK_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.getItem() instanceof IAttackHandler handler) {
                handler.handleAttack(event, armor);
                if (event.isCanceled()) {
                    return;
                }
            }
        }
    }

    private static void dispatchLegacyArmorDamageHandlers(LivingHurtEvent event, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }
        for (EquipmentSlot slot : ARMOR_TICK_SLOTS) {
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.getItem() instanceof IDamageHandler handler) {
                handler.handleDamage(event, armor);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.getServer() == null) {
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            RTTYSystem.updateBroadcastQueue(event.getServer());
            RadarScanner.updateSystem(event.getServer().getAllLevels());
            for (ServerLevel level : event.getServer().getAllLevels()) {
                TomImpactSavedData.tickExistingImpactClimate(level);
            }
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        PollutionManager.tick(event.getServer().getAllLevels());
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ChunkRadiationManager.tick(level);
            HbmEnergyNodespace.tick(level);
            HbmFluidNodespace.tick(level);
            HbmUninosNodespaces.tick(level);
            meltCraterColdBlocks(level);
        }
        NeutronHandler.tick(event.getServer());
        ServerTileBinaryControlTransfers.pruneExpired(event.getServer().overworld().getGameTime());
        ThreadedPacketDispatcher.flush();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        HbmPlayerProperties.tickRuntime(player);
        tickEquippedHbmArmor(player);
        DnsArmorItem.reconcileSprintBoost(player);
        NcrpaArmorItem.reconcileSprintBoost(player);
        FsbArmorItem.reconcileStepHeight(player);
        HazardExposureUtil.updatePlayerInventory(player);
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            FsbArmorItem.handleFullSetJump(player);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            FsbArmorItem.handleFullSetFall(player, event.getDistance());
        }
    }

    private static void tickEquippedHbmArmor(Player player) {
        Level level = player.level();
        for (EquipmentSlot slot : ARMOR_TICK_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof FsbArmorItem armor) {
                armor.tickEquippedArmor(stack, level, player);
            } else if (stack.getItem() instanceof EuphemiumArmorItem armor) {
                armor.tickEquippedArmor(stack, level, player);
            } else if (stack.getItem() instanceof No9ArmorItem armor) {
                armor.tickEquippedArmor(stack, level, player);
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            HbmCraftingAdvancementUtil.fireCraftingAdvancement(serverPlayer, event.getCrafting());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        LoadedTileAccessCache.invalidate(level, event.getPos());
        if (!HbmAbilityToolItem.isHandlingAbilityBreak()
                && event.getPlayer().getMainHandItem().getItem() instanceof HbmAbilityToolItem abilityTool
                && abilityTool.handleAbilityBlockBreak(event)) {
            event.setCanceled(true);
            handleCoalGasOnBlockBreak(event, level);
            handleLeadPollutionOnBlockBreak(event, level);
            return;
        }
        handleCoalGasOnBlockBreak(event, level);
        handleLeadPollutionOnBlockBreak(event, level);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LoadedTileAccessCache.invalidate(level, event.getPos());
        }
    }

    private static void handleCoalGasOnBlockBreak(BlockEvent.BreakEvent event, ServerLevel level) {
        Block block = event.getState().getBlock();
        if (!event.getState().is(Blocks.COAL_ORE)
                && !event.getState().is(Blocks.DEEPSLATE_COAL_ORE)
                && !event.getState().is(Blocks.COAL_BLOCK)
                && block != legacyBlockOrNull("ore_lignite")) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos target = event.getPos().relative(direction);
            if (level.random.nextInt(2) == 0 && level.isEmptyBlock(target)) {
                level.setBlock(target, ModBlocks.GAS_COAL.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static void handleLeadPollutionOnBlockBreak(BlockEvent.BreakEvent event, ServerLevel level) {
        PollutionManager.applyLeadFromBlockBreak(event.getPlayer(), level, event.getPos());
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || entity.isDeadOrDying()) {
            return;
        }

        if (entity.tickCount % 20 == 0) {
            HbmLivingProperties.flushEnvironmentBuffer(entity);
        }

        handleBombTimer(entity);
        handleCraterRadiation(entity);
        handleContaminationEffects(entity);
        handleContagion(entity);
        handleLegacyRadiationEffects(entity);
        handleLegacyRadiationFx(entity);
        handleDigammaEffects(entity);
        handleLegacyPostRadiationLongTermEffects(entity);
        if (!(entity instanceof Player)) {
            HazardExposureUtil.updateLivingInventory(entity);
        }

        if (entity.tickCount % 20 == 0 && entity instanceof ServerPlayer serverPlayer) {
            syncRadiationThreaded(serverPlayer);
            syncPollution(serverPlayer);
        }
    }

    private static void handleBombTimer(LivingEntity entity) {
        int timer = HbmLivingProperties.getBombTimer(entity);
        if (timer <= 0) {
            return;
        }

        if (HbmLivingProperties.decrementBombTimer(entity) <= 0) {
            ExplosionNukeSmall.explode(entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                    ExplosionNukeSmall.PARAMS_MEDIUM);
        }
    }

    private static Block legacyBlockOrNull(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        return block == null ? null : block.get();
    }

    private static void meltCraterColdBlocks(ServerLevel level) {
        if (level.players().isEmpty() || level.getGameTime() % CRATER_MELT_INTERVAL_TICKS != 0) {
            return;
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }

            BlockPos center = player.blockPosition();
            for (int i = 0; i < CRATER_MELT_SAMPLES_PER_PLAYER; i++) {
                int x = center.getX() + level.random.nextInt(CRATER_MELT_RADIUS * 2 + 1) - CRATER_MELT_RADIUS;
                int z = center.getZ() + level.random.nextInt(CRATER_MELT_RADIUS * 2 + 1) - CRATER_MELT_RADIUS;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                cursor.set(x, Mth.clamp(surfaceY, level.getMinBuildHeight(), level.getMaxBuildHeight() - 1), z);
                if (CraterRadiationData.getZone(level, cursor) == CraterRadiationData.CraterZone.NONE) {
                    continue;
                }

                int top = Math.min(level.getMaxBuildHeight() - 1, surfaceY + 2);
                int bottom = Math.max(level.getMinBuildHeight(), surfaceY - 6);
                for (int y = top; y >= bottom; y--) {
                    cursor.set(x, y, z);
                    meltColdBlock(level, cursor);
                }
            }
        }
    }

    private static boolean meltColdBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW)) {
            level.removeBlock(pos, false);
            return true;
        }
        if (state.is(Blocks.ICE)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return true;
        }
        if (state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.FROSTED_ICE)) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onMobFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();
        PollutionManager.decorateMob(mob);
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        if (!RadiationConfig.rampantGlyphidGuidanceEnabled() || event.getEntity().level().isClientSide) {
            return;
        }
        event.getOptionalPos().ifPresent(pos -> PollutionManager.setRampantTarget(event.getEntity().level(), pos));
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        TRACKED_ITEM_ENTITIES.computeIfAbsent(event.getLevel().dimension(), key -> new HashSet<>()).add(itemEntity.getId());
        if (event.getLevel() instanceof ServerLevel level) {
            itemEntity.getPersistentData().putLong(HAZARD_ENTITY_TICK_KEY, level.getGameTime());
        }
        applyDroppedItemHazards(itemEntity);
    }

    @SubscribeEvent
    public static void onEntityTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide || !(event.level instanceof ServerLevel level)) {
            return;
        }
        Set<Integer> tracked = TRACKED_ITEM_ENTITIES.get(level.dimension());
        if (tracked == null || tracked.isEmpty()) {
            return;
        }
        if (level.getGameTime() % ServerConfig.droppedItemHazardTickRate() != 0L) {
            return;
        }
        for (Iterator<Integer> iterator = tracked.iterator(); iterator.hasNext();) {
            Integer entityId = iterator.next();
            Entity trackedEntity = level.getEntity(entityId);
            if (!(trackedEntity instanceof ItemEntity itemEntity) || itemEntity.isRemoved()) {
                iterator.remove();
                continue;
            }
            long lastTick = itemEntity.getPersistentData().getLong(HAZARD_ENTITY_TICK_KEY);
            if (level.getGameTime() == lastTick) {
                continue;
            }
            itemEntity.getPersistentData().putLong(HAZARD_ENTITY_TICK_KEY, level.getGameTime());
            applyDroppedItemHazards(itemEntity);
        }
        if (tracked.isEmpty()) {
            TRACKED_ITEM_ENTITIES.remove(level.dimension());
        }
    }

    private static void applyDroppedItemHazards(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty() || itemEntity.isRemoved()) {
            return;
        }
        if (HazardExposureUtil.updateDroppedItem(itemEntity)) {
            return;
        }

        if (itemEntity.onGround() && WeaponConfig.droppedSingularitiesEnabled() && itemEntity.level() instanceof ServerLevel level) {
            if (stack.is(ModItems.SINGULARITY.get())) {
                spawnDroppedVortex(itemEntity, level, 1.5F);
            } else if (stack.is(ModItems.SINGULARITY_COUNTER_RESONANT.get())
                    || stack.is(ModItems.SINGULARITY_SUPER_HEATED.get())) {
                spawnDroppedVortex(itemEntity, level, 2.5F);
            } else if (stack.is(ModItems.SINGULARITY_SPARK.get())) {
                itemEntity.discard();
                RagingVortexEntity vortex = new RagingVortexEntity(level, 3.5F);
                vortex.moveTo(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 0.0F, 0.0F);
                level.addFreshEntity(vortex);
            } else if (stack.is(ModItems.BLACK_HOLE.get())) {
                itemEntity.discard();
                BlackHoleEntity blackHole = new BlackHoleEntity(level, 1.5F);
                blackHole.moveTo(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 0.0F, 0.0F);
                level.addFreshEntity(blackHole);
            } else if (stack.is(ModItems.PARTICLE_DIGAMMA.get())) {
                itemEntity.discard();
                QuasarEntity quasar = new QuasarEntity(level, 5.0F);
                quasar.moveTo(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 0.0F, 0.0F);
                level.addFreshEntity(quasar);
            }
        }

        if (stack.is(ModItems.PELLET_ANTIMATTER.get()) && itemEntity.onGround()
                && WeaponConfig.droppedAntimatterCellsEnabled() && itemEntity.level() instanceof ServerLevel level) {
            itemEntity.discard();
            WeaponExplosionUtil.antimatter(level, itemEntity.getX(),
                    itemEntity.getY() + itemEntity.getBbHeight() * 0.5D, itemEntity.getZ(),
                    20.0F, itemEntity, 50.0F).explode();
        }

        if (itemEntity.onGround() && WeaponConfig.droppedXenCrystalsEnabled()
                && itemEntity.level() instanceof ServerLevel level && isLegacyItem(stack, "crystal_xen")) {
            itemEntity.discard();
            int x = Mth.floor(itemEntity.getX());
            int y = Mth.floor(itemEntity.getY());
            int z = Mth.floor(itemEntity.getZ());
            ExplosionChaos.floater(level, x, y, z, 25, 75);
            ExplosionChaos.move(level, x, y, z, 25, 0, 75, 0);
        }
    }

    private static void spawnDroppedVortex(ItemEntity itemEntity, ServerLevel level, float size) {
        itemEntity.discard();
        VortexEntity vortex = new VortexEntity(level, size);
        vortex.moveTo(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 0.0F, 0.0F);
        level.addFreshEntity(vortex);
    }

    private static boolean isLegacyItem(ItemStack stack, String legacyName) {
        var item = ModItems.legacyItem(legacyName);
        return item != null && stack.is(item.get());
    }

    public static void syncRadiationNow(ServerPlayer player) {
        syncRadiation(player);
    }

    public static void syncPollutionNow(ServerPlayer player) {
        syncPollution(player);
    }

    private static void syncRadiation(ServerPlayer player) {
        HbmExtendedProperties.sync(player,
                ChunkRadiationManager.getRadiation(player.level(), player.blockPosition()),
                HazmatRegistry.getResistance(player));
    }

    private static void syncRadiationThreaded(ServerPlayer player) {
        HbmExtendedProperties.syncThreaded(player,
                ChunkRadiationManager.getRadiation(player.level(), player.blockPosition()),
                HazmatRegistry.getResistance(player));
    }

    private static void syncPollution(ServerPlayer player) {
        ModMessages.syncPermaData(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        HbmLivingProperties.copyForRespawn(event.getOriginal(), event.getEntity());
        HbmPlayerProperties.copyForRespawn(event.getOriginal(), event.getEntity());
        HbmLivingProperties.applyDigammaModifier(event.getEntity());
        if (event.getOriginal() instanceof ServerPlayer oldPlayer) {
            HbmServerKeybinds.clear(oldPlayer);
            HbmPlayerProperties.clearRuntime(oldPlayer);
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            syncRadiation(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendMotd(player);
            syncRadiation(player);
        }
    }

    private static void sendMotd(ServerPlayer player) {
        if (!HbmCommonConfig.motdEnabled()) {
            return;
        }
        player.sendSystemMessage(Component.literal("Loaded world with " + modDisplayName() + " "
                + modVersion() + " for Minecraft " + SharedConstants.getCurrentVersion().getName() + "!"));
    }

    private static String modDisplayName() {
        return ModList.get().getModContainerById(HbmNtm.MOD_ID)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse("HBM NTM Rebirth");
    }

    private static String modVersion() {
        return ModList.get().getModContainerById(HbmNtm.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HbmServerKeybinds.clearMovement(player);
            syncRadiation(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HbmServerKeybinds.clear(player);
            HbmPlayerProperties.clearRuntime(player);
            ServerTileBinaryControlTransfers.clearPlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load event) {
        BlockMigrationHelper.load(event.getChunk(), event.getData());
        if (event.getChunk().getWorldForge() instanceof ServerLevel level) {
            ChunkRadiationManager.loadLegacyChunkRadiation(level, event.getChunk().getPos(),
                    event.getData().getFloat(ChunkRadiationManager.LEGACY_CHUNK_NBT_KEY));
        }
    }

    @SubscribeEvent
    public static void onChunkDataSave(ChunkDataEvent.Save event) {
        BlockMigrationHelper.save(event.getData());
        if (event.getLevel() instanceof ServerLevel level) {
            ChunkPos pos = event.getChunk().getPos();
            event.getData().putFloat(ChunkRadiationManager.LEGACY_CHUNK_NBT_KEY, ChunkRadiationManager.getChunkRadiation(level, pos));
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof Level level) {
            ChunkRadiationManager.unloadChunk(level, event.getChunk().getPos());
            HbmEnergyNodespace.unloadChunk(level, event.getChunk().getPos());
            HbmFluidNodespace.unloadChunk(level, event.getChunk().getPos());
            HbmUninosNodespaces.unloadChunk(level, event.getChunk().getPos());
            NeutronNodeWorld.unloadChunk(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            TomImpactSavedData.resetLastCached();
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof Level level) {
            ChunkRadiationManager.unloadLevel(level);
            HbmEnergyNodespace.unloadLevel(level);
            HbmFluidNodespace.unloadLevel(level);
            HbmUninosNodespaces.unloadLevel(level);
            NeutronNodeWorld.unloadLevel(level);
            PollutionManager.unloadLevel(level);
            LoadedTileAccessCache.clearLevel(level);
            TRACKED_ITEM_ENTITIES.remove(level.dimension());
        }
    }

    private static void handleCraterRadiation(LivingEntity entity) {
        float craterRadiation = CraterRadiationData.getAmbientRadiation(entity);
        if (craterRadiation > 0.0F) {
            RadiationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, craterRadiation / 20.0F);
        }
    }

    private static void handleChunkRadiationFx(LivingEntity entity) {
        float radiation = ChunkRadiationManager.getRadiation(entity.level(), entity.blockPosition());
        if (entity.level().dimension() == Level.NETHER) {
            float hellRadiation = RadiationConfig.hellRadiation();
            if (hellRadiation > 0.0F && radiation < hellRadiation) {
                radiation = hellRadiation;
            }
        }
        if (radiation > 0.0F) {
            RadiationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, radiation / 20.0F);
        }
    }

    private static void handleLegacyRadiationEffects(LivingEntity entity) {
        if ((entity instanceof Player player && player.isCreative()) || entity.isDeadOrDying()) {
            return;
        }

        if (handleLegacyRadiationTransformations(entity)) {
            return;
        }

        float radiation = HbmLivingProperties.getRadiation(entity);
        if (radiation < 200.0F) {
            return;
        }
        if (RadiationUtil.isRadImmune(entity)) {
            return;
        }
        if (radiation > 2500.0F) {
            HbmLivingProperties.setRadiation(entity, 2500.0F);
        }

        if (radiation >= 1000.0F) {
            var radiationSource = ModDamageSources.radiation(entity.level());
            entity.hurt(radiationSource, 1000.0F);
            HbmLivingProperties.setRadiation(entity, 0.0F);
            if (entity.isAlive()) {
                entity.setHealth(0.0F);
                entity.die(radiationSource);
            }
            if (entity.isDeadOrDying()) {
                ParticleUtil.spawnGiblets(entity, ParticleUtil.GIBLET_MEAT);
            }
            if (entity instanceof ServerPlayer player) {
                AchievementHandler.award(player, AchievementHandler.RAD_DEATH);
            }
        } else if (radiation >= 800.0F) {
            addRandomEffect(entity, 300, MobEffects.CONFUSION, 5 * 30, 0);
            addRandomEffect(entity, 300, MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 2);
            addRandomEffect(entity, 300, MobEffects.WEAKNESS, 10 * 20, 2);
            addRandomEffect(entity, 500, MobEffects.POISON, 3 * 20, 2);
            addRandomEffect(entity, 700, MobEffects.WITHER, 3 * 20, 1);
        } else if (radiation >= 600.0F) {
            addRandomEffect(entity, 300, MobEffects.CONFUSION, 5 * 30, 0);
            addRandomEffect(entity, 300, MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 2);
            addRandomEffect(entity, 300, MobEffects.WEAKNESS, 10 * 20, 2);
            addRandomEffect(entity, 500, MobEffects.POISON, 3 * 20, 1);
        } else if (radiation >= 400.0F) {
            addRandomEffect(entity, 300, MobEffects.CONFUSION, 5 * 30, 0);
            addRandomEffect(entity, 500, MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 0);
            addRandomEffect(entity, 300, MobEffects.WEAKNESS, 5 * 20, 1);
        } else {
            addRandomEffect(entity, 300, MobEffects.CONFUSION, 5 * 20, 0);
            addRandomEffect(entity, 500, MobEffects.WEAKNESS, 5 * 20, 0);
            if (entity instanceof ServerPlayer player) {
                AchievementHandler.award(player, AchievementHandler.RAD_POISON);
            }
        }

    }

    private static void handleLegacyRadiationFx(LivingEntity entity) {
        if (RadiationUtil.isRadImmune(entity)) {
            return;
        }
        handleChunkRadiationFx(entity);
        handleRadiationParticles(entity, HbmLivingProperties.getRadiation(entity));
    }

    private static boolean handleLegacyRadiationTransformations(LivingEntity entity) {
        float radiation = HbmLivingProperties.getRadiation(entity);
        if (radiation >= 200.0F && entity.getClass().equals(Creeper.class)) {
            entity.hurt(ModDamageSources.radiation(entity.level()), 100.0F);
            return true;
        } else if (radiation >= 50.0F && entity instanceof Cow cow && !(entity instanceof MushroomCow) && cow.level() instanceof ServerLevel level) {
            MushroomCow mushroomCow = EntityType.MOOSHROOM.create(level);
            if (mushroomCow != null) {
                mushroomCow.moveTo(cow.getX(), cow.getY(), cow.getZ(), cow.getYRot(), cow.getXRot());
                level.addFreshEntity(mushroomCow);
                cow.discard();
                return true;
            }
        } else if (radiation >= 500.0F && entity instanceof Villager villager && villager.level() instanceof ServerLevel level) {
            Zombie zombie = EntityType.ZOMBIE.create(level);
            if (zombie != null) {
                zombie.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
                level.addFreshEntity(zombie);
                villager.discard();
                return true;
            }
        }
        return false;
    }

    private static void handleRadiationParticles(LivingEntity entity, float radiation) {
        if (!(entity.level() instanceof ServerLevel level) || (entity instanceof Player player && player.isCreative())) {
            return;
        }

        long time = level.getGameTime();
        long randomSeed = legacyRandomSeed(entity.getId());
        long r600Step = legacyNextInt(randomSeed, 600);
        randomSeed = legacyStepSeed(r600Step);
        int r600 = legacyStepValue(r600Step);
        long r1200Step = legacyNextInt(randomSeed, 1200);
        randomSeed = legacyStepSeed(r1200Step);
        int r1200 = legacyStepValue(r1200Step);
        if (radiation > 600.0F && (time + r600) % 600 < 20) {
            spawnVomit(entity, ParticleUtil.VOMIT_BLOOD, 25);
            if ((time + r600) % 600 == 1) {
                playRadiationVomit(level, entity);
            }
        } else if (radiation > 200.0F && (time + r1200) % 1200 < 20) {
            spawnVomit(entity, ParticleUtil.VOMIT_NORMAL, 15);
            if ((time + r1200) % 1200 == 1) {
                playRadiationVomit(level, entity);
            }
        }

        if (radiation > 900.0F && (time + legacyStepValue(legacyNextInt(randomSeed, 10))) % 10 == 0) {
            ParticleUtil.spawnSweat(entity, Blocks.REDSTONE_BLOCK, 1);
        }
    }

    private static long legacyRandomSeed(int seed) {
        return (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1);
    }

    private static long legacyNextSeed(long seed) {
        return (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
    }

    private static long legacyNextInt(long seed, int bound) {
        if (bound <= 0 || bound > 0xFFFF) {
            throw new IllegalArgumentException("bound must fit in 16 bits");
        }
        long nextSeed = legacyNextSeed(seed);
        int bits = (int) (nextSeed >>> 17);
        if ((bound & -bound) == bound) {
            return legacyStep((int) ((bound * (long) bits) >> 31), nextSeed);
        }
        int value = bits % bound;
        while (bits - value + (bound - 1) < 0) {
            nextSeed = legacyNextSeed(nextSeed);
            bits = (int) (nextSeed >>> 17);
            value = bits % bound;
        }
        return legacyStep(value, nextSeed);
    }

    private static long legacyStep(int value, long seed) {
        return (seed << 16) | (value & 0xFFFFL);
    }

    private static long legacyStepSeed(long step) {
        return step >>> 16;
    }

    private static int legacyStepValue(long step) {
        return (int) (step & 0xFFFFL);
    }

    private static void handleDigammaEffects(LivingEntity entity) {
        HbmLivingProperties.applyDigammaModifier(entity);
        float digamma = HbmLivingProperties.getDigamma(entity);
        if (digamma < 0.01F) {
            return;
        }

        if ((entity.getMaxHealth() <= 0.0F || digamma >= 10.0F) && entity.isAlive()) {
            RadiationData.handleFatalDigamma(entity);
        }

        int chance = Math.max(10 - (int) digamma, 1);
        if (chance == 1 || entity.getRandom().nextInt(chance) == 0) {
            ParticleUtil.spawnSweat(entity, Blocks.SOUL_SAND, 1);
        }
    }

    private static void handleLegacyPostRadiationLongTermEffects(LivingEntity entity) {
        handleLungDisease(entity);
        handleOil(entity);
        handlePollution(entity);
        handleTemperatureEffects(entity);
    }

    private static void handleContaminationEffects(LivingEntity entity) {
        for (HbmLivingProperties.ContaminationEffect effect : HbmLivingProperties.tickContamination(entity)) {
            RadiationUtil.contaminate(entity, HazardType.RADIATION,
                    effect.ignoresArmor() ? ContaminationType.RAD_BYPASS : ContaminationType.CREATIVE,
                    effect.currentRadiation());
        }
    }

    private static void handleContagion(LivingEntity entity) {
        if (!ServerConfig.mkuEnabled()) {
            return;
        }

        int contagion = HbmLivingProperties.getContagion(entity);
        int hour = 60 * 60 * 20;
        int minute = 60 * 20;

        if (entity instanceof Player player) {
            handlePlayerInventoryContagion(player, contagion);
        }

        if (contagion <= 0) {
            return;
        }

        HbmLivingProperties.decrementContagion(entity);

        if (contagion < (2 * hour + 55 * minute) && contagion % 20 == 0 && entity.level() instanceof ServerLevel level) {
            double range = entity.isInWaterOrRain() ? 16.0D : 2.0D;
            AABB box = entity.getBoundingBox().inflate(range);
            for (Entity nearby : level.getEntities(entity, box)) {
                if (nearby instanceof LivingEntity living && HbmLivingProperties.getContagion(living) <= 0 && !ArmorUtil.checkForMkuProtection(living)) {
                    HbmLivingProperties.applyMkuContagion(living);
                } else if (nearby instanceof ItemEntity itemEntity) {
                    tagContagious(itemEntity.getItem());
                }
            }
        }

        if (contagion < 2 * hour && entity.getRandom().nextInt(1000) == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20, 0));
        }
        if (contagion < hour && entity.getRandom().nextInt(100) == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 4));
        }
        if (contagion < 30 * minute && entity.getRandom().nextInt(400) == 0) {
            entity.hurt(ModDamageSources.mku(entity.level()), 1.0F);
        }
        if (contagion < 5 * minute && entity.getRandom().nextInt(100) == 0) {
            entity.hurt(ModDamageSources.mku(entity.level()), 2.0F);
        }
        if (contagion < 30 * minute && (contagion + entity.getId()) % 200 < 20 && entity.level() instanceof ServerLevel level) {
            spawnVomit(entity, ParticleUtil.VOMIT_BLOOD, 25);
            if ((contagion + entity.getId()) % 200 == 19) {
                playContagionVomit(level, entity);
            }
        }
    }

    private static void handlePlayerInventoryContagion(Player player, int contagion) {
        ItemStack stack = randomInventoryContagionStack(player);
        if (stack.isEmpty() || stack.getMaxStackSize() != 1) {
            return;
        }

        if (contagion > 0) {
            tagContagious(stack);
        } else if (isTaggedContagious(stack) && !ArmorUtil.checkForMkuProtection(player)) {
            HbmLivingProperties.applyMkuContagion(player);
        }
    }

    private static ItemStack randomInventoryContagionStack(Player player) {
        var random = player.getRandom();
        ItemStack stack = player.getInventory().items.get(random.nextInt(player.getInventory().items.size()));
        if (random.nextInt(100) == 0) {
            stack = player.getInventory().armor.get(random.nextInt(player.getInventory().armor.size()));
        }
        return stack;
    }

    private static void tagContagious(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putBoolean(CONTAGION_ITEM_TAG, true);
        }
    }

    private static boolean isTaggedContagious(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(CONTAGION_ITEM_TAG);
    }

    private static void handleLungDisease(LivingEntity entity) {
        if (entity instanceof Player player && player.isCreative()) {
            HbmLivingProperties.clearBlackLung(entity);
            HbmLivingProperties.clearAsbestos(entity);
            return;
        }

        int blackLungRaw = HbmLivingProperties.getBlackLung(entity);
        int maxBlackLung = HbmLivingProperties.MAX_BLACK_LUNG;
        int maxAsbestos = HbmLivingProperties.MAX_ASBESTOS;
        if (blackLungRaw > 0 && blackLungRaw < maxBlackLung * 0.5D) {
            HbmLivingProperties.reduceBlackLung(entity, 1);
        }

        double blackLung = Math.min(HbmLivingProperties.getBlackLung(entity), maxBlackLung);
        double asbestos = Math.min(HbmLivingProperties.getAsbestos(entity), maxAsbestos);
        double soot = PollutionManager.getSootLungLoad(entity);
        boolean coughs = blackLung / maxBlackLung > 0.25D || asbestos / maxAsbestos > 0.25D || soot > 30.0D;
        if (!coughs) {
            return;
        }

        boolean coughsCoal = blackLung / maxBlackLung > 0.5D;
        boolean coughsALotOfCoal = blackLung / maxBlackLung > 0.8D;
        boolean coughsBlood = asbestos / maxAsbestos > 0.75D || blackLung / maxBlackLung > 0.75D;
        double blackLungDelta = 1.0D - blackLung / maxBlackLung;
        double asbestosDelta = 1.0D - asbestos / maxAsbestos;
        double conditionTotal = 1.0D - blackLungDelta * asbestosDelta;

        if (conditionTotal > 0.75D) {
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
        }
        if (conditionTotal > 0.95D) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        }

        double sootDelta = 1.0D - Math.min(soot / 100.0D, 1.0D);
        double total = 1.0D - blackLungDelta * asbestosDelta * sootDelta;
        int frequency = Math.max((int) (1000 - 950 * total), 20);
        if (entity.level().getGameTime() % frequency == Math.floorMod(entity.getId(), frequency) && entity.level() instanceof ServerLevel level) {
            LegacySoundPlayer.playLegacyPlayerCough(entity);
            if (coughsBlood) {
                spawnVomit(entity, ParticleUtil.VOMIT_BLOOD, 5);
            }
            if (coughsCoal) {
                spawnVomit(entity, ParticleUtil.VOMIT_SMOKE, coughsALotOfCoal ? 50 : 10);
            }
        }
    }

    private static void handlePollution(LivingEntity entity) {
        PollutionManager.applyEntityPollutionEffects(entity);
    }

    private static void handleOil(LivingEntity entity) {
        int oil = HbmLivingProperties.getOil(entity);
        if (oil <= 0) {
            return;
        }
        if (entity.isOnFire() && entity.level() instanceof ServerLevel level) {
            HbmLivingProperties.clearOil(entity);
            WeaponExplosionUtil.explodeStandard(level, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(), 3.0F, null, true, false);
        } else {
            HbmLivingProperties.decrementOil(entity);
        }
        if (entity.tickCount % 5 == 0 && entity.level() instanceof ServerLevel level) {
            ParticleUtil.spawnSweat(entity, Blocks.COAL_BLOCK, 1);
        }
    }

    private static void handleTemperatureEffects(LivingEntity entity) {
        if (entity.fireImmune()) {
            HbmLivingProperties.clearFire(entity);
            HbmLivingProperties.clearPhosphorus(entity);
        }
        if (entity.isInWaterOrRain()) {
            HbmLivingProperties.clearFire(entity);
        }

        handleTemperatureEffect(entity, TemperatureEffect.FIRE);
        handleTemperatureEffect(entity, TemperatureEffect.PHOSPHORUS);
        handleTemperatureEffect(entity, TemperatureEffect.BALEFIRE);
        handleTemperatureEffect(entity, TemperatureEffect.BLACK_FIRE);
        if (hasActiveTemperatureEffect(entity) && entity.isDeadOrDying()) {
            LegacyConfettiUtil.decideConfetti(entity, DamageClass.FIRE);
        }
    }

    private static void handleTemperatureEffect(LivingEntity entity, TemperatureEffect effect) {
        int value = effect.get(entity);
        if (value <= 0) {
            return;
        }
        effect.decrement(entity);
        if (effect.radiates) {
            RadiationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 5.0F);
        }
        int soundInterval = effect == TemperatureEffect.BLACK_FIRE ? 10 : 15;
        int damageInterval = effect == TemperatureEffect.BLACK_FIRE ? 10 : effect.radiates ? 20 : 40;
        if ((entity.tickCount + entity.getId()) % soundInterval == 0 && entity.level() instanceof ServerLevel level) {
            level.playSound(null, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.0F,
                    1.5F + entity.getRandom().nextFloat() * 0.5F);
        }
        if ((entity.tickCount + entity.getId()) % damageInterval == 0) {
            EntityDamageUtil.attackEntityFromNt(entity, entity.damageSources().onFire(), effect.damage);
        }
        if (entity.level() instanceof ServerLevel level) {
            double width = entity.getBbWidth();
            double x = entity.getX() - width * 0.5D + width * entity.getRandom().nextDouble();
            double y = entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight();
            double z = entity.getZ() - width * 0.5D + width * entity.getRandom().nextDouble();
            ParticleUtil.spawnLegacyFlameEffect(level, x, y, z, effect.flameMeta);
        }
    }

    private static boolean hasActiveTemperatureEffect(LivingEntity entity) {
        for (TemperatureEffect effect : TemperatureEffect.values()) {
            if (effect.get(entity) > 0) {
                return true;
            }
        }
        return false;
    }

    private static void spawnVomit(LivingEntity entity, String mode, int count) {
        if (canVomit(entity)) {
            ParticleUtil.spawnVomit(entity, mode, count);
        }
    }

    private static boolean canVomit(LivingEntity entity) {
        return entity.getType().getCategory() != MobCategory.WATER_CREATURE;
    }

    private static void playRadiationVomit(ServerLevel level, LivingEntity entity) {
        if (!canVomit(entity)) {
            return;
        }
        LegacySoundPlayer.playSoundEffect(level, Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ()),
                "hbm:player.vomit", SoundSource.HOSTILE, 1.0F, 1.0F);
        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 19));
    }

    private static void playContagionVomit(ServerLevel level, LivingEntity entity) {
        if (!canVomit(entity)) {
            return;
        }
        LegacySoundPlayer.playSoundEffect(level, entity.getX(), entity.getY(), entity.getZ(),
                "hbm:player.vomit", SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    private static void addRandomEffect(LivingEntity entity, int chance, net.minecraft.world.effect.MobEffect effect, int duration, int amplifier) {
        if (entity.level().random.nextInt(chance) == 0) {
            entity.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private enum TemperatureEffect {
        FIRE(ParticleUtil.FLAMETHROWER_META_FIRE, 2.0F, false) {
            @Override int get(LivingEntity entity) { return HbmLivingProperties.getFire(entity); }
            @Override void decrement(LivingEntity entity) { HbmLivingProperties.decrementFire(entity); }
        },
        PHOSPHORUS(ParticleUtil.FLAMETHROWER_META_FIRE, 5.0F, false) {
            @Override int get(LivingEntity entity) { return HbmLivingProperties.getPhosphorus(entity); }
            @Override void decrement(LivingEntity entity) { HbmLivingProperties.decrementPhosphorus(entity); }
        },
        BALEFIRE(ParticleUtil.FLAMETHROWER_META_BALEFIRE, 5.0F, true) {
            @Override int get(LivingEntity entity) { return HbmLivingProperties.getBalefire(entity); }
            @Override void decrement(LivingEntity entity) { HbmLivingProperties.decrementBalefire(entity); }
        },
        BLACK_FIRE(ParticleUtil.FLAMETHROWER_META_BLACK, 10.0F, true) {
            @Override int get(LivingEntity entity) { return HbmLivingProperties.getBlackFire(entity); }
            @Override void decrement(LivingEntity entity) { HbmLivingProperties.decrementBlackFire(entity); }
        };

        private final int flameMeta;
        private final float damage;
        private final boolean radiates;

        TemperatureEffect(int flameMeta, float damage, boolean radiates) {
            this.flameMeta = flameMeta;
            this.damage = damage;
            this.radiates = radiates;
        }

        abstract int get(LivingEntity entity);
        abstract void decrement(LivingEntity entity);
    }

    private CommonForgeEvents() {
    }
}
