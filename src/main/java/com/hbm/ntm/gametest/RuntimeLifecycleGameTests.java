package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.redstoneoverradio.RTTYDeviceState;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import com.hbm.ntm.util.BufferUtil;
import com.hbm.ntm.world.BlockMigrationHelper;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Regression coverage for transient process-local state that is scoped to a level. */
@PrefixGameTestTemplate(false)
public final class RuntimeLifecycleGameTests {
    private RuntimeLifecycleGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(RuntimeLifecycleGameTests.class);
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "runtimeLifecycle")
    public static void rttyLevelClearRemovesQueuedAndBroadcastSignals(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        String channel = "gametest_runtime_lifecycle";
        RTTYSystem.clear(level);
        RTTYSystem.broadcast(level, channel, "broadcast");
        RTTYSystem.updateBroadcastQueue(level.getServer());
        RTTYSystem.broadcast(level, channel, "queued");
        RTTYSystem.Diagnostics beforeCleanup = RTTYSystem.diagnostics(level);
        if (beforeCleanup.levelQueuedMessages() != 1 || beforeCleanup.levelBroadcastChannels() == 0) {
            throw new AssertionError("RTTY queued and broadcast signals must be visible before level cleanup: "
                    + beforeCleanup.summary());
        }
        RTTYSystem.clear(level);
        RTTYSystem.Diagnostics diagnostics = RTTYSystem.diagnostics(level);
        if (diagnostics.levelQueuedMessages() != 0 || diagnostics.levelBroadcastChannels() != 0) {
            throw new AssertionError("RTTY level cleanup must remove all transient channels: " + diagnostics.summary());
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "runtimeLifecycle")
    public static void rttyStateTruncatesEveryPersistedAndWireStringIngress(GameTestHelper helper) {
        String overlong = "0123456789abcdef";
        String expected = "0123456789abcde";
        RTTYDeviceState state = new RTTYDeviceState();
        state.setChannel(overlong);
        state.setMapping(3, overlong);
        assertEquals(expected, state.channel(), "setter truncates channel");
        assertEquals(expected, state.mapping(3), "setter truncates mapping");

        CompoundTag saved = new CompoundTag();
        saved.putString("c", overlong);
        saved.putString("m3", overlong);
        state.load(saved);
        assertEquals(expected, state.channel(), "NBT load truncates channel");
        assertEquals(expected, state.mapping(3), "NBT load truncates mapping");

        CompoundTag control = new CompoundTag();
        control.putString("c", overlong);
        control.putString("m3", overlong);
        state.applyControl(control);
        assertEquals(expected, state.channel(), "control tag truncates channel");
        assertEquals(expected, state.mapping(3), "control tag truncates mapping");

        FriendlyByteBuf wire = new FriendlyByteBuf(Unpooled.buffer());
        wire.writeBoolean(false);
        wire.writeBoolean(false);
        BufferUtil.writeString(wire, overlong);
        for (int index = 0; index < 16; index++) {
            BufferUtil.writeString(wire, index == 3 ? overlong : "");
        }
        state.readLegacyWire(wire);
        assertEquals(expected, state.channel(), "legacy wire truncates channel");
        assertEquals(expected, state.mapping(3), "legacy wire truncates mapping");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "runtimeLifecycle")
    public static void mk3LevelClearRemovesFleijaAntiTeleportMarkers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos marker = helper.absolutePos(new BlockPos(6, 4, 6));
        NukeExplosionMk3Entity.clearAntiTeleportEntries(level);
        NukeExplosionMk3Entity.registerAntiTeleportEntry(level, marker.getX(), marker.getY(), marker.getZ(), 20);
        if (!NukeExplosionMk3Entity.hasAntiTeleportOverlap(level, marker.getX(), marker.getY(), marker.getZ())) {
            throw new AssertionError("F.L.E.I.J.A. anti-teleport marker must exist before level cleanup");
        }
        NukeExplosionMk3Entity.clearAntiTeleportEntries(level);
        if (NukeExplosionMk3Entity.hasAntiTeleportOverlap(level, marker.getX(), marker.getY(), marker.getZ())) {
            throw new AssertionError("F.L.E.I.J.A. anti-teleport marker survived level cleanup");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "runtimeLifecycle", timeoutTicks = 40)
    public static void mk3ExpiredFleijaAntiTeleportMarkersArePruned(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos marker = helper.absolutePos(new BlockPos(7, 4, 7));
        NukeExplosionMk3Entity.clearAntiTeleportEntries(level);
        NukeExplosionMk3Entity.registerAntiTeleportEntry(level, marker.getX(), marker.getY(), marker.getZ(), 1);
        helper.startSequence()
                .thenIdle(2)
                .thenExecute(() -> {
                    NukeExplosionMk3Entity.pruneExpiredAntiTeleportEntries(level);
                    if (NukeExplosionMk3Entity.hasAntiTeleportOverlap(level, marker.getX(), marker.getY(), marker.getZ())) {
                        throw new AssertionError("expired F.L.E.I.J.A. anti-teleport marker survived pruning");
                    }
                })
                .thenSucceed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "runtimeLifecycle")
    public static void chunkMigrationMarkerPreservesNativeSectionLists(GameTestHelper helper) {
        CompoundTag paletteEntry = new CompoundTag();
        paletteEntry.putString("Name", "minecraft:stone");
        ListTag palette = new ListTag();
        palette.add(paletteEntry);

        CompoundTag blockStates = new CompoundTag();
        blockStates.put("palette", palette);
        CompoundTag section = new CompoundTag();
        section.putByte("Y", (byte) 4);
        section.put("block_states", blockStates);
        ListTag sections = new ListTag();
        sections.add(section);
        CompoundTag chunkData = new CompoundTag();
        chunkData.put("sections", sections);
        chunkData.putString(BlockMigrationHelper.NBT_KEY_BUILD_NUMBER, "wrong-type-marker");

        BlockMigrationHelper.MigrationMarker marker = BlockMigrationHelper.inspectTag(chunkData);
        if (!marker.hasLegacyMarker() || marker.previousBuild() != 0) {
            throw new AssertionError("wrong-type migration marker did not preserve legacy numeric-read semantics");
        }
        assertNativeChunkNbtUnchanged(chunkData, sections, section, blockStates, palette,
                "reading the migration marker");
        BlockMigrationHelper.save(chunkData);
        assertNativeChunkNbtUnchanged(chunkData, sections, section, blockStates, palette,
                "writing the migration marker");
        assertEquals(BlockMigrationHelper.buildNumber(),
                chunkData.getInt(BlockMigrationHelper.NBT_KEY_BUILD_NUMBER),
                "chunk migration build marker");
        helper.succeed();
    }

    private static void assertNativeChunkNbtUnchanged(CompoundTag chunkData, ListTag sections,
                                                       CompoundTag section, CompoundTag blockStates,
                                                       ListTag palette, String operation) {
        if (chunkData.get("sections") != sections || sections.get(0) != section
                || section.get("block_states") != blockStates || blockStates.get("palette") != palette) {
            throw new AssertionError(operation + " replaced native chunk NBT objects");
        }
        if (sections.getClass() != ListTag.class || palette.getClass() != ListTag.class) {
            throw new AssertionError(operation + " replaced native ListTag implementations");
        }
        if (chunkData.getList("sections", Tag.TAG_COMPOUND).getCompound(0).getByte("Y") != 4) {
            throw new AssertionError(operation + " changed typed section reads");
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
