package com.hbm.ntm.event;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.command.ModCommands;
import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.ThreadedPacketDispatcher;
import com.hbm.ntm.network.packet.PlayerRadiationSyncPacket;
import com.hbm.ntm.network.HbmServerKeybinds;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.HazardRegistry;
import com.hbm.ntm.radiation.HazmatRegistry;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.ItemRadiationRegistry;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationData;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonForgeEvents {
    private static final String HAZARD_ENTITY_TICK_KEY = "hbmHazardTick";
    private static final String CONTAGION_ITEM_TAG = "ntmContagion";
    private static final Map<ResourceKey<Level>, Set<Integer>> TRACKED_ITEM_ENTITIES = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) {
            return;
        }

        for (ServerLevel level : event.getServer().getAllLevels()) {
            ChunkRadiationManager.tick(level);
            HbmEnergyNodespace.tick(level);
            HbmFluidNodespace.tick(level);
        }
        ThreadedPacketDispatcher.flush();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        applyInventoryRadiation(player);
        applyNetherAmbientRadiation(player);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
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

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || entity.isDeadOrDying()) {
            return;
        }

        handleChunkRadiation(entity);
        handleLegacyRadiationEffects(entity);
        handleDigammaEffects(entity);
        handleLegacyLongTermEffects(entity);

        if (entity.tickCount % 20 == 0) {
            RadiationData.flushEnvironmentBuffer(entity);
            if (entity instanceof ServerPlayer serverPlayer) {
                syncRadiation(serverPlayer);
            }
        }
    }

    private static Block legacyBlockOrNull(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        return block == null ? null : block.get();
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        TRACKED_ITEM_ENTITIES.computeIfAbsent(event.getLevel().dimension(), key -> new HashSet<>()).add(itemEntity.getId());
        applyDroppedItemHazards(itemEntity);
    }

    private static void applyInventoryRadiation(Player player) {
        applyInventoryHazards(player, HazardType.ASBESTOS);
        applyInventoryHazards(player, HazardType.COAL);
        applyInventoryHazards(player, HazardType.HOT);
        applyInventoryHazards(player, HazardType.BLINDING);
        applyInventoryHazards(player, HazardType.HYDROACTIVE);
        applyInventoryHazards(player, HazardType.EXPLOSIVE);
        float itemRadiation = 0.0F;
        for (ItemStack stack : player.getInventory().items) {
            itemRadiation += ItemRadiationRegistry.getRadiation(stack) * stack.getCount() / 20.0F;
        }
        for (ItemStack stack : player.getInventory().armor) {
            itemRadiation += ItemRadiationRegistry.getRadiation(stack) * stack.getCount() / 20.0F;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            itemRadiation += ItemRadiationRegistry.getRadiation(stack) * stack.getCount() / 20.0F;
        }

        if (itemRadiation > 0.0F) {
            RadiationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.CREATIVE, itemRadiation);
        }
    }

    private static void applyInventoryHazards(Player player, HazardType type) {
        for (ItemStack stack : player.getInventory().items) {
            applyInventoryHazard(player, stack, type);
        }
        for (ItemStack stack : player.getInventory().armor) {
            applyInventoryHazard(player, stack, type);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            applyInventoryHazard(player, stack, type);
        }
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
        Iterator<Integer> iterator = tracked.iterator();
        while (iterator.hasNext()) {
            Entity trackedEntity = level.getEntity(iterator.next());
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
    }

    private static void applyDroppedItemHazards(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty() || itemEntity.isRemoved()) {
            return;
        }
        float hydroactive = HazardRegistry.getHazardLevel(stack, HazardType.HYDROACTIVE);
        if (hydroactive > 0.0F && itemEntity.isInWaterOrRain() && itemEntity.level() instanceof ServerLevel level) {
            itemEntity.discard();
            WeaponExplosionUtil.explodeStandard(level, itemEntity.getX(), itemEntity.getY() + itemEntity.getBbHeight() * 0.5D, itemEntity.getZ(), hydroactive, itemEntity, true, false);
            return;
        }

        float explosive = HazardRegistry.getHazardLevel(stack, HazardType.EXPLOSIVE);
        if (explosive > 0.0F && itemEntity.isOnFire() && itemEntity.level() instanceof ServerLevel level) {
            itemEntity.discard();
            WeaponExplosionUtil.explodeStandard(level, itemEntity.getX(), itemEntity.getY() + itemEntity.getBbHeight() * 0.5D, itemEntity.getZ(), explosive, itemEntity, true, true);
        }
    }

    private static void applyInventoryHazard(Player player, ItemStack stack, HazardType type) {
        if (stack.isEmpty()) {
            return;
        }
        float level = HazardRegistry.getHazardLevel(stack, type, player);
        if (level <= 0.0F) {
            return;
        }
        int amount = (int) Math.min(level * Math.max(1, stack.getCount()), 10.0F);
        if (type == HazardType.ASBESTOS && !ArmorUtil.checkForHazmat(player)) {
            RadiationData.incrementAsbestos(player, amount);
        } else if (type == HazardType.COAL && !ArmorUtil.checkForHazmat(player)) {
            RadiationData.incrementBlackLung(player, amount);
        } else if (type == HazardType.HOT && !player.isInWaterOrRain()) {
            player.setSecondsOnFire((int) Math.ceil(level));
        } else if (type == HazardType.BLINDING) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) Math.ceil(level), 0));
        } else if (type == HazardType.HYDROACTIVE && player.isInWaterOrRain() && player.level() instanceof ServerLevel levelAccessor) {
            stack.shrink(stack.getCount());
            WeaponExplosionUtil.explodeStandard(levelAccessor, player.getX(), player.getEyeY(), player.getZ(), level, player, true, false);
        } else if (type == HazardType.EXPLOSIVE && player.isOnFire() && player.level() instanceof ServerLevel levelAccessor) {
            stack.shrink(stack.getCount());
            WeaponExplosionUtil.explodeStandard(levelAccessor, player.getX(), player.getEyeY(), player.getZ(), level, player, true, true);
        }
    }

    private static void applyNetherAmbientRadiation(Player player) {
        if (player.level().dimension() == Level.NETHER) {
            RadiationUtil.contaminate(player, HazardType.RADIATION, ContaminationType.CREATIVE, RadiationConfig.HELL_RAD.get().floatValue() / 20.0F);
        }
    }

    public static void syncRadiationNow(ServerPlayer player) {
        syncRadiation(player);
    }

    private static void syncRadiation(ServerPlayer player) {
        ModMessages.sendToPlayer(new PlayerRadiationSyncPacket(
                RadiationData.getRadiation(player),
                RadiationData.getDigamma(player),
                RadiationData.getRadBuf(player),
                ChunkRadiationManager.getRadiation(player.level(), player.blockPosition()),
                HazmatRegistry.getResistance(player),
                RadiationData.getAsbestos(player),
                RadiationData.getBlackLung(player),
                RadiationData.getBombTimer(player),
                RadiationData.getContagion(player),
                RadiationData.getOil(player),
                RadiationData.getFire(player),
                RadiationData.getPhosphorus(player),
                RadiationData.getBalefire(player),
                RadiationData.getBlackFire(player),
                RadiationData.getContaminationEffects(player)), player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        RadiationData.copyForRespawn(event.getOriginal(), event.getEntity());
        RadiationData.applyDigammaModifier(event.getEntity());
        if (event.getOriginal() instanceof ServerPlayer oldPlayer) {
            HbmServerKeybinds.clear(oldPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HbmServerKeybinds.clear(player);
        }
    }

    @SubscribeEvent
    public static void onChunkDataLoad(ChunkDataEvent.Load event) {
        if (event.getChunk().getWorldForge() instanceof ServerLevel level) {
            ChunkRadiationManager.loadLegacyChunkRadiation(level, event.getChunk().getPos(),
                    event.getData().getFloat(ChunkRadiationManager.LEGACY_CHUNK_NBT_KEY));
        }
    }

    @SubscribeEvent
    public static void onChunkDataSave(ChunkDataEvent.Save event) {
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
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof Level level) {
            HbmEnergyNodespace.unloadLevel(level);
            HbmFluidNodespace.unloadLevel(level);
        }
    }

    private static void handleChunkRadiation(LivingEntity entity) {
        if (RadiationUtil.isRadImmune(entity)) {
            return;
        }

        float chunkRadiation = ChunkRadiationManager.getRadiation(entity.level(), entity.blockPosition());
        if (chunkRadiation > 0.0F) {
            RadiationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, chunkRadiation / 20.0F);
        }
    }

    private static void handleLegacyRadiationEffects(LivingEntity entity) {
        if ((entity instanceof Player player && player.isCreative()) || entity.isDeadOrDying()) {
            return;
        }

        handleLegacyRadiationTransformations(entity);

        float radiation = RadiationData.getRadiation(entity);
        if (radiation < 200.0F) {
            return;
        }
        if (RadiationUtil.isRadImmune(entity)) {
            return;
        }
        if (radiation > 2500.0F) {
            RadiationData.setRadiation(entity, 2500.0F);
        }

        if (radiation >= 1000.0F) {
            entity.hurt(ModDamageSources.radiation(entity.level()), 1000.0F);
            RadiationData.setRadiation(entity, 0.0F);
            if (entity.isAlive()) {
                entity.setHealth(0.0F);
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
            RadiationUtil.addRadiationPoisoning(entity, 20 * 10, 0);
            addRandomEffect(entity, 300, MobEffects.CONFUSION, 5 * 20, 0);
            addRandomEffect(entity, 500, MobEffects.WEAKNESS, 5 * 20, 0);
        }

        handleRadiationParticles(entity, radiation);
    }

    private static void handleLegacyRadiationTransformations(LivingEntity entity) {
        float radiation = RadiationData.getRadiation(entity);
        if (radiation >= 200.0F && entity.getClass().equals(Creeper.class)) {
            entity.hurt(ModDamageSources.radiation(entity.level()), 100.0F);
        } else if (radiation >= 50.0F && entity instanceof Cow cow && !(entity instanceof MushroomCow) && cow.level() instanceof ServerLevel level) {
            MushroomCow mushroomCow = EntityType.MOOSHROOM.create(level);
            if (mushroomCow != null) {
                mushroomCow.moveTo(cow.getX(), cow.getY(), cow.getZ(), cow.getYRot(), cow.getXRot());
                level.addFreshEntity(mushroomCow);
                cow.discard();
            }
        } else if (radiation >= 500.0F && entity instanceof Villager villager && villager.level() instanceof ServerLevel level) {
            Zombie zombie = EntityType.ZOMBIE.create(level);
            if (zombie != null) {
                zombie.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
                level.addFreshEntity(zombie);
                villager.discard();
            }
        }
    }

    private static void handleRadiationParticles(LivingEntity entity, float radiation) {
        if (!(entity.level() instanceof ServerLevel level) || (entity instanceof Player player && player.isCreative())) {
            return;
        }

        long time = level.getGameTime();
        int seed = entity.getId();
        if (radiation > 600.0F && (time + Math.floorMod(seed * 31, 600)) % 600 < 20) {
            spawnVomit(level, entity, true);
            if ((time + Math.floorMod(seed * 31, 600)) % 600 == 1) {
                playVomit(level, entity);
            }
        } else if (radiation > 200.0F && (time + Math.floorMod(seed * 17, 1200)) % 1200 < 20) {
            spawnVomit(level, entity, false);
            if ((time + Math.floorMod(seed * 17, 1200)) % 1200 == 1) {
                playVomit(level, entity);
            }
        }

        if (radiation > 900.0F && entity.getRandom().nextInt(10) == 0) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    1, 0.25D, 0.35D, 0.25D, 0.02D);
        }
    }

    private static void handleDigammaEffects(LivingEntity entity) {
        RadiationData.applyDigammaModifier(entity);
        float digamma = RadiationData.getDigamma(entity);
        if (digamma < 0.01F) {
            return;
        }

        if ((entity.getMaxHealth() <= 0.0F || digamma >= 10.0F) && entity.isAlive()) {
            entity.setAbsorptionAmount(0.0F);
            entity.hurt(ModDamageSources.digamma(entity.level()), 500.0F);
            entity.setHealth(0.0F);
        }

        int chance = Math.max(10 - (int) digamma, 1);
        if (entity.level() instanceof ServerLevel serverLevel
                && (chance == 1 || entity.getRandom().nextInt(chance) == 0)) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SOUL_SAND.defaultBlockState()),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    1, 0.25D, 0.35D, 0.25D, 0.02D);
        }
    }

    private static void handleLegacyLongTermEffects(LivingEntity entity) {
        handleContaminationEffects(entity);
        handleContagion(entity);
        handleLungDisease(entity);
        handleOil(entity);
        handleTemperatureEffects(entity);
    }

    private static void handleContaminationEffects(LivingEntity entity) {
        net.minecraft.nbt.ListTag contamination = RadiationData.getContamination(entity);
        if (contamination.isEmpty()) {
            return;
        }
        net.minecraft.nbt.ListTag next = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < contamination.size(); i++) {
            net.minecraft.nbt.CompoundTag effect = contamination.getCompound(i);
            int time = effect.getInt("time");
            int maxTime = Math.max(1, effect.getInt("maxTime"));
            float maxRad = effect.getFloat("maxRad");
            boolean ignoreArmor = effect.getBoolean("ignoreArmor");
            if (time > 0) {
                float delta = maxRad * ((float) time / (float) maxTime);
                RadiationUtil.contaminate(entity, HazardType.RADIATION,
                        ignoreArmor ? ContaminationType.RAD_BYPASS : ContaminationType.CREATIVE,
                        delta);
                effect.putInt("time", time - 1);
                if (time - 1 > 0) {
                    next.add(effect);
                }
            }
        }
        RadiationData.setContamination(entity, next);
    }

    private static void handleContagion(LivingEntity entity) {
        if (!RadiationConfig.ENABLE_MKU.get()) {
            return;
        }

        int contagion = RadiationData.getContagion(entity);
        int hour = 60 * 60 * 20;
        int minute = 60 * 20;

        if (entity instanceof Player player) {
            handlePlayerInventoryContagion(player, contagion, hour);
        }

        if (contagion <= 0) {
            return;
        }

        RadiationData.setContagion(entity, contagion - 1);

        if (contagion < (2 * hour + 55 * minute) && contagion % 20 == 0 && entity.level() instanceof ServerLevel level) {
            double range = entity.isInWaterOrRain() ? 16.0D : 2.0D;
            AABB box = entity.getBoundingBox().inflate(range);
            for (Entity nearby : level.getEntities(entity, box)) {
                if (nearby instanceof LivingEntity living && RadiationData.getContagion(living) <= 0 && !ArmorUtil.checkForMkuProtection(living)) {
                    RadiationData.setContagion(living, 3 * hour);
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
            spawnVomit(level, entity, true);
            if ((contagion + entity.getId()) % 200 == 19) {
                playVomit(level, entity);
            }
        }
    }

    private static void handlePlayerInventoryContagion(Player player, int contagion, int hour) {
        ItemStack stack = randomInventoryContagionStack(player);
        if (stack.isEmpty() || stack.getMaxStackSize() != 1) {
            return;
        }

        if (contagion > 0) {
            tagContagious(stack);
        } else if (isTaggedContagious(stack) && !ArmorUtil.checkForMkuProtection(player)) {
            RadiationData.setContagion(player, 3 * hour);
        }
    }

    private static ItemStack randomInventoryContagionStack(Player player) {
        if (player.getRandom().nextInt(100) == 0) {
            return player.getInventory().armor.get(player.getRandom().nextInt(player.getInventory().armor.size()));
        }
        return player.getInventory().items.get(player.getRandom().nextInt(player.getInventory().items.size()));
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
            RadiationData.setBlackLung(entity, 0);
            RadiationData.setAsbestos(entity, 0);
            return;
        }

        int blackLungRaw = RadiationData.getBlackLung(entity);
        int maxBlackLung = 2 * 60 * 60 * 20;
        int maxAsbestos = 60 * 60 * 20;
        if (blackLungRaw > 0 && blackLungRaw < maxBlackLung * 0.5D) {
            RadiationData.setBlackLung(entity, blackLungRaw - 1);
        }

        double blackLung = Math.min(RadiationData.getBlackLung(entity), maxBlackLung);
        double asbestos = Math.min(RadiationData.getAsbestos(entity), maxAsbestos);
        boolean coughs = blackLung / maxBlackLung > 0.25D || asbestos / maxAsbestos > 0.25D;
        if (!coughs) {
            return;
        }

        boolean coughsCoal = blackLung / maxBlackLung > 0.5D;
        boolean coughsALotOfCoal = blackLung / maxBlackLung > 0.8D;
        boolean coughsBlood = asbestos / maxAsbestos > 0.75D || blackLung / maxBlackLung > 0.75D;
        double blackLungDelta = 1.0D - blackLung / maxBlackLung;
        double asbestosDelta = 1.0D - asbestos / maxAsbestos;
        double total = 1.0D - blackLungDelta * asbestosDelta;

        if (total > 0.75D) {
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
        }
        if (total > 0.95D) {
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        }

        int frequency = Math.max((int) (1000 - 950 * total), 20);
        if (entity.level().getGameTime() % frequency == Math.floorMod(entity.getId(), frequency) && entity.level() instanceof ServerLevel level) {
            if (coughsBlood) {
                spawnVomit(level, entity, true);
            }
            if (coughsCoal) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COAL_BLOCK.defaultBlockState()),
                        entity.getX(), entity.getY() + entity.getBbHeight() * 0.45D, entity.getZ(),
                        coughsALotOfCoal ? 50 : 10, 0.2D, 0.2D, 0.2D, 0.04D);
            }
        }
    }

    private static void handleOil(LivingEntity entity) {
        int oil = RadiationData.getOil(entity);
        if (oil <= 0) {
            return;
        }
        if (entity.isOnFire() && entity.level() instanceof ServerLevel level) {
            RadiationData.setOil(entity, 0);
            WeaponExplosionUtil.explodeStandard(level, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(), 3.0F, entity, true, true);
            return;
        }
        RadiationData.setOil(entity, oil - 1);
        if (entity.tickCount % 5 == 0 && entity.level() instanceof ServerLevel level) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COAL_BLOCK.defaultBlockState()),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.45D, entity.getZ(),
                    1, 0.2D, 0.2D, 0.2D, 0.02D);
        }
    }

    private static void handleTemperatureEffects(LivingEntity entity) {
        if (entity.fireImmune()) {
            RadiationData.setFire(entity, 0);
            RadiationData.setPhosphorus(entity, 0);
        }
        if (entity.isInWaterOrRain()) {
            RadiationData.setFire(entity, 0);
        }

        handleTemperatureEffect(entity, TemperatureEffect.FIRE);
        handleTemperatureEffect(entity, TemperatureEffect.PHOSPHORUS);
        handleTemperatureEffect(entity, TemperatureEffect.BALEFIRE);
        handleTemperatureEffect(entity, TemperatureEffect.BLACK_FIRE);
    }

    private static void handleTemperatureEffect(LivingEntity entity, TemperatureEffect effect) {
        int value = effect.get(entity);
        if (value <= 0) {
            return;
        }
        effect.set(entity, value - 1);
        if (effect.radiates) {
            RadiationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 5.0F);
        }
        int soundInterval = effect == TemperatureEffect.BLACK_FIRE ? 10 : 15;
        int damageInterval = effect == TemperatureEffect.BLACK_FIRE ? 10 : effect.radiates ? 20 : 40;
        if ((entity.tickCount + entity.getId()) % soundInterval == 0 && entity.level() instanceof ServerLevel level) {
            level.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.0F, 1.5F + entity.getRandom().nextFloat() * 0.5F);
        }
        if ((entity.tickCount + entity.getId()) % damageInterval == 0) {
            entity.hurt(entity.damageSources().onFire(), effect.damage);
        }
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, effect.particle.defaultBlockState()),
                    entity.getX(), entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight(), entity.getZ(),
                    1, 0.25D, 0.35D, 0.25D, 0.02D);
        }
    }

    private static void spawnVomit(ServerLevel level, LivingEntity entity, boolean blood) {
        if (blood) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.45D, entity.getZ(),
                    25, 0.2D, 0.2D, 0.2D, 0.04D);
        } else {
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.SLIME_BALL)),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.45D, entity.getZ(),
                    15, 0.2D, 0.2D, 0.2D, 0.04D);
        }
    }

    private static void playVomit(ServerLevel level, LivingEntity entity) {
        level.playSound(null, entity.blockPosition(), com.hbm.ntm.registry.ModSounds.PLAYER_VOMIT.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 19));
    }

    private static void addRandomEffect(LivingEntity entity, int chance, net.minecraft.world.effect.MobEffect effect, int duration, int amplifier) {
        if (entity.getRandom().nextInt(chance) == 0) {
            entity.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private enum TemperatureEffect {
        FIRE(Blocks.FIRE, 2.0F, false) {
            @Override int get(LivingEntity entity) { return RadiationData.getFire(entity); }
            @Override void set(LivingEntity entity, int value) { RadiationData.setFire(entity, value); }
        },
        PHOSPHORUS(Blocks.FIRE, 5.0F, false) {
            @Override int get(LivingEntity entity) { return RadiationData.getPhosphorus(entity); }
            @Override void set(LivingEntity entity, int value) { RadiationData.setPhosphorus(entity, value); }
        },
        BALEFIRE(Blocks.SOUL_FIRE, 5.0F, true) {
            @Override int get(LivingEntity entity) { return RadiationData.getBalefire(entity); }
            @Override void set(LivingEntity entity, int value) { RadiationData.setBalefire(entity, value); }
        },
        BLACK_FIRE(Blocks.BLACKSTONE, 10.0F, true) {
            @Override int get(LivingEntity entity) { return RadiationData.getBlackFire(entity); }
            @Override void set(LivingEntity entity, int value) { RadiationData.setBlackFire(entity, value); }
        };

        private final net.minecraft.world.level.block.Block particle;
        private final float damage;
        private final boolean radiates;

        TemperatureEffect(net.minecraft.world.level.block.Block particle, float damage, boolean radiates) {
            this.particle = particle;
            this.damage = damage;
            this.radiates = radiates;
        }

        abstract int get(LivingEntity entity);
        abstract void set(LivingEntity entity, int value);
    }

    private CommonForgeEvents() {
    }
}
