package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyTexturedLineRenderer;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.util.HbmMathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.phys.Vec3;

/**
 * Data plans for repeated legacy TESR render states and animated quads.
 */
public final class LegacyTileRenderPlans {
    public static final int GL_SRC_ALPHA = 770;
    public static final int GL_ONE_MINUS_SRC_ALPHA = 771;
    public static final int GL_ONE = 1;
    public static final int GL_ZERO = 0;
    public static final float LEGACY_FULLBRIGHT_LIGHTMAP_X = 240.0F;
    public static final float LEGACY_FULLBRIGHT_LIGHTMAP_Y = 240.0F;
    public static final double ASSEMBLY_SPARK_WIDE = 0.1875D;
    public static final double ASSEMBLY_SPARK_NARROW = 0.0D;
    public static final double ASSEMBLY_SPARK_LENGTH = 1.25D;
    public static final double ASSEMBLY_SPARK_EPSILON = 0.01D;
    public static final double ASSEMBLY_SPARK_MIRROR_U_OFFSET = 0.5D;
    public static final double ASSEMBLY_STRIKER_SPARK_THRESHOLD = -0.375D;
    public static final double BIG_ASS_TANK_FLUID_SIDE_OFFSET = 5.9375D;
    public static final double BIG_ASS_TANK_FLUID_BASE_Y = 1.75D;
    public static final double BIG_ASS_TANK_FLUID_HALF_WIDTH = 0.25D;
    public static final double BIG_ASS_TANK_FLUID_HEIGHT = 1.5D;
    public static final double BIG_ASS_TANK_FLUID_U_PERIOD = 250.0D;
    public static final double BIG_ASS_TANK_FLUID_U_SCALE = 0.5D;
    public static final double CHEMICAL_FACTORY_FAN_PIVOT_X = 1.0D;
    public static final double CHEMICAL_FACTORY_FAN_ROTATION_SCALE = -45.0D;
    public static final double CHEMICAL_PLANT_FLUID_ALPHA = 0.5D;
    public static final double BASIC_PRESS_HEAD_TRAVEL = 0.875D;
    public static final double BASIC_PRESS_ITEM_TRANSLATE_X = 0.5D;
    public static final double BASIC_PRESS_ITEM_TRANSLATE_Y = 0.896875D;
    public static final double BASIC_PRESS_ITEM_TRANSLATE_Z = 0.5D;
    public static final double BASIC_PRESS_ITEM_ROTATION_Y = 180.0D;
    public static final double BASIC_PRESS_ITEM_ROTATION_X = 90.0D;
    public static final double BASIC_PRESS_ITEM_SCALE = 0.45D;
    public static final double PYRO_OVEN_SLIDER_ANIM_SCALE = 0.125D;
    public static final double PYRO_OVEN_SLIDER_TRAVEL_SCALE = 0.5D;
    public static final double PYRO_OVEN_SLIDER_BASE_X = -0.5D;
    public static final double PYRO_OVEN_FAN_PIVOT_X = 1.5D;
    public static final double PYRO_OVEN_FAN_PIVOT_Z = 1.5D;
    public static final double PYRO_OVEN_FAN_ROTATION_SCALE = 45.0D;
    public static final double SMALL_REACTOR_CHERENKOV_START = 0.285D;
    public static final double SMALL_REACTOR_CHERENKOV_END = 0.7D;
    public static final double SMALL_REACTOR_CHERENKOV_STEP = 0.025D;
    public static final double SMALL_REACTOR_CHERENKOV_CENTER_Y = 1.375D;
    public static final double RBMK_CHERENKOV_TRANSLATE_Y = 0.75D;
    public static final double RBMK_CHERENKOV_STEP = 0.25D;
    public static final double SOLAR_BOILER_BEAM_START_Y = 1.0625D;
    public static final double SOLAR_BOILER_BEAM_HALF_WIDTH = 0.5D;
    public static final float SOLAR_BOILER_BEAM_MIN_ALPHA = 0.005F;
    public static final float SOLAR_BOILER_BEAM_MAX_ALPHA = 0.01F;
    public static final double REFUELER_FLUID_CLIP_D = -0.125D;
    public static final double REFUELER_FLUID_TRAVEL = -0.625D;
    public static final double LIQUEFACTOR_FLUID_PIVOT_Y = 1.0D;
    public static final double SOLIDIFIER_FLUID_PIVOT_Y = 1.25D;
    public static final double BATTERY_REDD_TRAIL_LENGTH = 4.25D;
    public static final double BATTERY_REDD_TRAIL_WIDTH = 0.125D;
    public static final double BATTERY_REDD_TRAIL_X_OFFSET = 0.8125D;
    public static final double BATTERY_REDD_TRAIL_SPAN_SCALE = 0.75D;
    public static final int BATTERY_REDD_TRAIL_SPOKES = 8;
    public static final double BATTERY_REDD_PLASMA_PERIOD = 1000.0D;
    public static final double BATTERY_REDD_SPARKLE_SPIN_PERIOD = 250.0D;
    public static final double CORE_STANDBY_BASE_SCALE = 0.25D;
    public static final double CORE_STANDBY_GLOW_SCALE = 0.3125D;
    public static final double CORE_ORB_BASE_SCALE = 0.25D;
    public static final int CORE_ORB_GLOW_SHELLS = 17;
    public static final int CORE_FLARE_RAYS = 150;
    public static final long CORE_FLARE_RANDOM_SEED = 432L;
    public static final int CORE_VOID_LAYERS = 10;
    public static final int CORE_VOID_SEGMENTS = 32;
    public static final long CORE_VOID_RANDOM_SEED = 31110L;
    public static final double RADAR_SCAN_PERIOD = 56.0D;
    public static final double RADAR_SCAN_DIVISOR = 30.0D;
    public static final double RADAR_SCAN_THICKNESS = 0.125D;
    public static final double RADAR_SCREEN_X = 0.38D;
    public static final double RADAR_SCREEN_MIN_Y = 0.125D;
    public static final double RADAR_SCREEN_MAX_Y = 1.875D;
    public static final double RADAR_SCREEN_MIN_Z = -0.375D;
    public static final double RADAR_SCREEN_MAX_Z = 1.375D;
    public static final double RADAR_BLIP_SIZE = 0.0625D;
    public static final double EXCAVATOR_DROP_PERIOD_MILLIS = 250.0D;
    public static final double EXCAVATOR_CHUTE_CENTER_Z = 2.5D;
    public static final double EXCAVATOR_UPPER_HALF_WIDTH = 0.125D;
    public static final double EXCAVATOR_LOWER_HALF_DEPTH = 0.0625D;
    public static final double SOYUZ_SMALL_BLOCK_PIXEL = 1.0D / 16.0D;
    public static final double SOYUZ_SMALL_BLOCK_MIN = 11.0D * SOYUZ_SMALL_BLOCK_PIXEL / 2.0D;
    public static final double SOYUZ_SMALL_BLOCK_MAX = 1.0D - SOYUZ_SMALL_BLOCK_MIN;
    public static final float SOYUZ_GHOST_ALPHA = 0.75F;
    public static final int RBMK_NUMITRON_COUNT = 2;
    public static final int RBMK_NUMITRON_DIGITS = 7;
    public static final long RBMK_NUMITRON_LEFT_DIGIT_MASK = 0x40L;
    public static final double RBMK_NUMITRON_TRANSLATE_X = 0.25D;
    public static final double RBMK_NUMITRON_ROW_STEP = -0.5D;
    public static final double RBMK_NUMITRON_Y_START = 0.25D;
    public static final double RBMK_NUMITRON_DIGIT_X = 0.03135D;
    public static final double RBMK_NUMITRON_DIGIT_Y = 0.5625D;
    public static final double RBMK_NUMITRON_DIGIT_Z_STEP = 0.1D;
    public static final double RBMK_NUMITRON_DIGIT_SCALE = 200.0D;
    public static final double RBMK_NUMITRON_DIGIT_WIDTH = 8.0D / RBMK_NUMITRON_DIGIT_SCALE;
    public static final double RBMK_NUMITRON_DIGIT_HEIGHT = 13.0D / RBMK_NUMITRON_DIGIT_SCALE;
    public static final double RBMK_NUMITRON_LABEL_X = 0.01D;
    public static final double RBMK_NUMITRON_LABEL_Y = 0.3125D;
    public static final float RBMK_NUMITRON_LABEL_MAX_SCALE = 0.0125F;
    public static final float RBMK_NUMITRON_LABEL_WIDTH_SCALE = 0.75F;
    public static final int RBMK_NUMITRON_LABEL_COLOR = 0x00FF00;
    public static final double STRAND_CASTER_FLUID_HEIGHT = 0.675D;
    public static final double STRAND_CASTER_PLATE_TRAVEL = 0.375D;
    public static final double STRAND_CASTER_PLATE_BASE_Z = 3.4D;
    public static final double STRAND_CASTER_LAVA_BASE_Y = 2.3D;
    public static final double STRAND_CASTER_LAVA_HALF_WIDTH = 0.9D;
    public static final double STRAND_CASTER_LAVA_HALF_DEPTH = 0.999D;
    public static final int MAXWELL_BEAM_COUNT = 8;
    public static final double MAXWELL_BARREL_LENGTH = 2.125D;
    public static final double MAXWELL_BEAM_TRANSLATE_Y = 2.0D;
    public static final int MAXWELL_BEAM_COLOR = 0x2020FF;
    public static final float MAXWELL_BEAM_SIZE = 0.375F;
    public static final int MAXWELL_BEAM_LAYERS = 2;
    public static final float MAXWELL_BEAM_THICKNESS = 0.05F;
    public static final double MAXWELL_BEAM_SPIN_SPEED = -50.0D;
    public static final double MAXWELL_BEAM_PHASE_STEP = 45.0D;
    public static final double RBMK_COLUMN_WIDTH = 0.0625D * 0.75D;
    public static final double RBMK_COLUMN_DOT_WIDTH = 0.03125D;
    public static final double RBMK_COLUMN_DOT_EDGE = 0.022097D;
    public static final double RBMK_COLUMN_DOT_X_OFFSET = 0.01D;
    public static final int RBMK_COLUMN_INDICATOR_COLOR = 0xFFFF00;
    public static final double RBMK_COLUMN_GRID_STEP = 0.125D;
    public static final double RBMK_DISPLAY_TRANSLATE_Y = 0.5D;
    public static final double RBMK_DISPLAY_SCALE_YZ = 8.0D / 7.0D;
    public static final double RBMK_DISPLAY_COLUMN_X = 0.28125D;
    public static final int RBMK_DISPLAY_COLUMNS_PER_ROW = 7;
    public static final int RBMK_DISPLAY_Z_CENTER = 3;
    public static final double RBMK_DISPLAY_Y_START = 0.875D;
    public static final double RBMK_CONSOLE_MODEL_TRANSLATE_X = 0.5D;
    public static final double RBMK_CONSOLE_COLUMN_X = -0.3725D;
    public static final int RBMK_CONSOLE_COLUMNS_PER_ROW = 15;
    public static final int RBMK_CONSOLE_Z_CENTER = 7;
    public static final double RBMK_CONSOLE_Y_START = 3.625D;
    public static final double TAUON_BEAM_TRANSLATE_Y = 1.5D;
    public static final int TAUON_BEAM_OUTER_COLOR = 0xFFA200;
    public static final int TAUON_BEAM_INNER_COLOR = 0xFFD000;
    public static final float TAUON_BEAM_SIZE = 0.1F;
    public static final double CREATIVE_BATTERY_HORSE_SCALE = 0.75D;
    public static final double CREATIVE_BATTERY_BEAM_TRANSLATE_Y = 0.75D;
    public static final double CREATIVE_BATTERY_BEAM_XZ = 0.4375D;
    public static final double CREATIVE_BATTERY_BEAM_Y = 1.1875D;
    public static final int CREATIVE_BATTERY_BEAM_OUTER_COLOR = 0x404040;
    public static final int CREATIVE_BATTERY_BEAM_INNER_COLOR = 0x002040;
    public static final int CREATIVE_BATTERY_BEAM_RANDOM_BOUND = 4;
    public static final int CREATIVE_BATTERY_BEAM_PERIOD_MILLIS = 1000;
    public static final int CREATIVE_BATTERY_BEAM_START_DIVISOR = 50;
    public static final int CREATIVE_BATTERY_LONG_BEAM_SEGMENTS = 15;
    public static final int CREATIVE_BATTERY_SHORT_BEAM_SEGMENTS = 1;
    public static final float CREATIVE_BATTERY_LONG_BEAM_SIZE = 0.0625F;
    public static final float CREATIVE_BATTERY_SHORT_BEAM_SIZE = 0.0F;
    public static final int CREATIVE_BATTERY_BEAM_LAYERS = 3;
    public static final float CREATIVE_BATTERY_BEAM_THICKNESS = 0.025F;
    public static final int LASER_MINER_BEAM_COUNT = 3;
    public static final double LASER_MINER_EMITTER_OFFSET = 1.5D;
    public static final int LASER_MINER_BEAM_COLOR = 0xA00000;
    public static final float LASER_MINER_BEAM_SIZE = 0.075F;
    public static final int LASER_MINER_BEAM_LAYERS = 3;
    public static final float LASER_MINER_BEAM_THICKNESS = 0.025F;
    public static final int LASER_MINER_PHASE_STEP = 120;
    public static final int ICF_LASER_OUTER_COLOR = 0x202020;
    public static final int ICF_LASER_INNER_COLOR = 0x100000;
    public static final int ICF_LASER_LAYERS = 10;
    public static final float ICF_LASER_THICKNESS = 0.125F;
    public static final int TESLA_BEAM_COLOR = 0x404040;
    public static final float TESLA_BEAM_SIZE = 0.125F;
    public static final int TESLA_BEAM_LAYERS = 2;
    public static final float TESLA_BEAM_THICKNESS = 0.03125F;
    public static final int EXPOSURE_RANDOM_DURATION = 8;
    public static final int EXPOSURE_RANDOM_CHANCE = 2;
    public static final int EXPOSURE_RANDOM_BLUE_COLOR = 0x80D0FF;
    public static final int EXPOSURE_RANDOM_WHITE_COLOR = 0xFFFFFF;
    public static final double EXPOSURE_CORE_BOB_PERIOD = Math.PI * 16.0D;
    public static final double EXPOSURE_CORE_BOB_SPEED = 0.125D;
    public static final double EXPOSURE_CORE_BOB_AMOUNT = 0.0625D;
    public static final int DFC_EMITTER_DEPTH_COLOR = 0x404000;
    public static final int DFC_EMITTER_RANDOM_COLOR = 0x401500;
    public static final int DFC_INJECTOR_INNER_COLOR = 0x808080;
    public static final int DFC_STABILIZER_OUTER_COLOR = 0xFFA200;
    public static final int DFC_STABILIZER_INNER_COLOR = 0xFFD000;
    public static final double DFC_BEAM_TRANSLATE_Y = 0.5D;
    public static final double ANNIHILATOR_ROLLER_PIVOT_Y = 1.75D;
    public static final double ANNIHILATOR_ROLLER_ROTATION_SPEED = 0.15D;
    public static final double ANNIHILATOR_BELT_PERIOD_MILLIS = 3000.0D;
    public static final double CONVEYOR_PRESS_PISTON_TRAVEL = 0.75D;
    public static final int CONVEYOR_PRESS_BELT_PERIOD_TICKS = 16;
    public static final int CONVEYOR_PRESS_BELT_TICK_OFFSET = -2;
    public static final double ARC_FURNACE_CONTENT_TRAVEL = 1.75D;
    public static final double ARC_FURNACE_CONTENT_BASE_Y = -1.75D;
    public static final double ARC_FURNACE_LID_TRAVEL = 2.0D;
    public static final double ARC_FURNACE_LID_WOBBLE = 0.005D;
    public static final double ARC_FURNACE_CABLE_PIVOT_Y = 5.5D;
    public static final double ARC_FURNACE_CABLE_WOBBLE_DEGREES = 30.0D;
    public static final double STEAM_ENGINE_MODEL_TRANSLATE_X = 2.0D;
    public static final double STEAM_ENGINE_FLYWHEEL_PIVOT_X = 2.0D;
    public static final double STEAM_ENGINE_FLYWHEEL_PIVOT_Y = 1.375D;
    public static final double STEAM_ENGINE_SHAFT_PIVOT_Y = 1.375D;
    public static final double STEAM_ENGINE_SHAFT_PIVOT_Z = -0.5D;
    public static final double STEAM_ENGINE_TRANSMISSION_PIVOT_X = 2.25D;
    public static final double STEAM_ENGINE_TRANSMISSION_PIVOT_Y = 1.375D;
    public static final double STEAM_ENGINE_CRANK_RADIUS = 0.25D;
    public static final double STEAM_ENGINE_CRANK_SIN_OFFSET = -0.25D;
    public static final double STEAM_ENGINE_ROD_LENGTH = 1.875D;
    public static final double STEAM_ENGINE_PISTON_CATH_SQUARED = 3.515625D;
    public static final double BAT9000_FLUID_BASE_Y = 1.5D;
    public static final double BAT9000_FLUID_HEIGHT = 1.5D;
    public static final double BAT9000_FLUID_OFFSET = 2.2D;
    public static final double BAT9000_FLUID_HALF_WIDTH = 0.5D;
    public static final double WAND_STRUCTURE_Y_OFFSET = 1.0D;
    public static final int WAND_STRUCTURE_BRIGHTNESS = 240;
    public static final double AUTOSAW_DEFAULT_ANGLE = 80.0D;
    public static final double AUTOSAW_ENGINE_BOB = 0.01D;
    public static final double AUTOSAW_ARM_PIVOT_Y = 1.75D;
    public static final double AUTOSAW_ARM_LOWER_Z = -4.0D;
    public static final double AUTOSAW_ARM_TIP_Z = -8.0D;
    public static final double AUTOSAW_BLADE_Z = -10.0D;
    public static final double AUTOSAW_ARM_LOWER_NUDGE_X = -0.01D;
    public static final double THRESHER_DEFAULT_ANGLE = 82.5D;
    public static final double THRESHER_ITEM_ANGLE = 80.0D;
    public static final double THRESHER_ENGINE_BOB = 0.01D;
    public static final double THRESHER_ARM_PIVOT_Y = 0.5D;
    public static final double THRESHER_ARM_UPPER_Z = -1.0D;
    public static final double THRESHER_ARM_LOWER_Z = -5.0D;
    public static final double THRESHER_FRONT_Z = -9.0D;
    public static final double THRESHER_WHEEL_Z = -11.0D;
    public static final double THRESHER_ARM_LOWER_NUDGE_X = -0.01D;
    public static final double THRESHER_FRONT_NUDGE_X = 0.01D;
    public static final double TURBOFAN_BLADE_PIVOT_Y = 1.5D;
    public static final double INDUSTRIAL_TURBINE_PIVOT_Y = 1.5D;
    public static final double INDUSTRIAL_TURBINE_GAUGE_STEAM = 135.0D;
    public static final double INDUSTRIAL_TURBINE_GAUGE_HOTSTEAM = 45.0D;
    public static final double INDUSTRIAL_TURBINE_GAUGE_SUPERHOTSTEAM = -45.0D;
    public static final double INDUSTRIAL_TURBINE_GAUGE_ULTRAHOTSTEAM = -135.0D;
    public static final double INDUSTRIAL_TURBINE_ITEM_FLYWHEEL_PERIOD = 5.0D;
    public static final double INDUSTRIAL_TURBINE_ITEM_FLYWHEEL_MODULO = 336.0D;
    public static final double PUMPJACK_ROTOR_PIVOT_Y = 1.5D;
    public static final double PUMPJACK_ROTOR_PIVOT_Z = -5.5D;
    public static final double PUMPJACK_HEAD_PIVOT_Y = 3.5D;
    public static final double PUMPJACK_HEAD_PIVOT_Z = -3.5D;
    public static final double PUMPJACK_HEAD_ROTATION_SCALE = 0.25D;
    public static final double PUMPJACK_BACK_ROD_X = 0.53125D;
    public static final double PUMPJACK_BACK_ROD_Z_WIDTH = 0.0625D;
    public static final double PUMPJACK_FRONT_ROD_HALF_THICKNESS = 0.03125D;
    public static final double PUMPJACK_FRONT_ROD_WIDTH = 0.25D;
    public static final double PUMPJACK_FRONT_ROD_RADIUS = 2.5D;
    public static final double PUMPJACK_FRONT_ROD_DIST = 0.03125D;
    public static final double PUMPJACK_FRONT_ROD_CUTLET = 360.0D / 32.0D;
    public static final int PUMPJACK_FRONT_ROD_SEGMENTS = 4;
    public static final int PUMPJACK_BACK_ROD_COLOR = 0x808080;
    public static final int PUMPJACK_FRONT_ROD_COLOR = 0x333333;
    public static final double BIG_TURBINE_BASE_ROTATION_Y = 90.0D;
    public static final double BIG_TURBINE_TRANSLATE_Z = -1.0D;
    public static final double BIG_TURBINE_BLADE_PIVOT_Y = 1.0D;
    public static final double BOILER_OVERPRESSURE_THRESHOLD = 0.9D;
    public static final double BOILER_OVERPRESSURE_PERIOD_DIVISOR = 50.0D;
    public static final double BOILER_OVERPRESSURE_SCALE = 0.01D;
    public static final double CRANE_CONSOLE_TRANSLATE_X = 0.5D;
    public static final double CRANE_JOYSTICK_PIVOT_X = 0.75D;
    public static final double CRANE_JOYSTICK_PIVOT_Y = 1.0D;
    public static final double CRANE_JOYSTICK_RESTORE_Y = -1.015D;
    public static final double CRANE_METER_PIVOT_Y = 1.25D;
    public static final double CRANE_METER_1_Z = 0.75D;
    public static final double CRANE_METER_2_Z = 0.25D;
    public static final double CRANE_METER_BASE_DEGREES = 135.0D;
    public static final double CRANE_METER_RANGE_DEGREES = 270.0D;
    public static final double CRANE_METER_WOBBLE_SCALE = 0.05D;
    public static final double CRANE_METER_WOBBLE_SPEED = 0.01D;
    public static final double CRANE_LIFT_TRAVEL = 3.25D;
    public static final int CRANE_LAMP_LOADING_COLOR = 0xCCCC00;
    public static final int CRANE_LAMP_LOADED_COLOR = 0x00FF00;
    public static final int CRANE_LAMP_UNLOADED_COLOR = 0x001900;
    public static final int CRANE_LAMP_TARGET_VALID_COLOR = 0x00FF00;
    public static final int CRANE_LAMP_TARGET_INVALID_COLOR = 0xFF0000;

    public static FullbrightStatePlan fullbrightStatePlan() {
        return new FullbrightStatePlan(true, true, true, true,
                LEGACY_FULLBRIGHT_LIGHTMAP_X, LEGACY_FULLBRIGHT_LIGHTMAP_Y);
    }

    public static FullbrightStatePlan lightmapOnlyFullbrightPlan() {
        return new FullbrightStatePlan(false, false, true, false,
                LEGACY_FULLBRIGHT_LIGHTMAP_X, LEGACY_FULLBRIGHT_LIGHTMAP_Y);
    }

    public static FullbrightStatePlan batteryReddManualFullbrightPlan() {
        return new FullbrightStatePlan(false, false, true, true,
                LEGACY_FULLBRIGHT_LIGHTMAP_X, LEGACY_FULLBRIGHT_LIGHTMAP_Y);
    }

    public static BlendStatePlan normalAlphaBlendPlan(boolean depthWrite, float alphaThreshold) {
        return new BlendStatePlan(true, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO,
                alphaThreshold, depthWrite, depthWrite
                ? LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE
                : LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
    }

    public static BlendStatePlan additiveBlendPlan(boolean depthWrite, float alphaThreshold) {
        return new BlendStatePlan(true, GL_SRC_ALPHA, GL_ONE, GL_ONE, GL_ZERO,
                alphaThreshold, depthWrite, depthWrite
                ? LegacyTexturedRenderMode.ADDITIVE_DEPTH_WRITE
                : LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE);
    }

    public static BlendStatePlan oneOneAdditiveBlendPlan(boolean depthWrite, float alphaThreshold) {
        return new BlendStatePlan(true, GL_ONE, GL_ONE, GL_ONE, GL_ZERO,
                alphaThreshold, depthWrite, depthWrite
                ? LegacyTexturedRenderMode.ADDITIVE_DEPTH_WRITE
                : LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE);
    }

    public static AssemblySparkRenderPlan assemblySparkPlan(long worldTime, float partialTicks,
            double slide1, double slide2, double arm2BladeAngle, double arm2StrikerOffset,
            double arm4BladeAngle, double arm4StrikerOffset) {
        LegacyUvAnimation.Range u = LegacyUvAnimation.assemblyFactorySparkU(worldTime, partialTicks);
        List<AssemblySparkBladePlan> blades = new ArrayList<>();
        if (arm2StrikerOffset <= ASSEMBLY_STRIKER_SPARK_THRESHOLD) {
            blades.add(assemblySparkBlade("blade_2", 0.5D + slide1, -arm2BladeAngle / 45.0D,
                    ASSEMBLY_SPARK_LENGTH, u));
        }
        if (arm4StrikerOffset <= ASSEMBLY_STRIKER_SPARK_THRESHOLD) {
            blades.add(assemblySparkBlade("blade_4", -0.5D - slide2, arm4BladeAngle / 45.0D,
                    -ASSEMBLY_SPARK_LENGTH, u));
        }
        return new AssemblySparkRenderPlan(!blades.isEmpty(), u.min(), u.max(),
                ASSEMBLY_SPARK_MIRROR_U_OFFSET, ASSEMBLY_SPARK_EPSILON,
                normalAlphaBlendPlan(true, 0.0F), fullbrightStatePlan(), List.copyOf(blades));
    }

    public static BigAssTankFluidPlan bigAssTankFluidPlan(long worldTime, float partialTicks,
            int fill, int maxFill) {
        double height = maxFill <= 0 ? 0.0D : Math.max(0.0D, (double) fill) * BIG_ASS_TANK_FLUID_HEIGHT / (double) maxFill;
        LegacyUvAnimation.Range u = LegacyUvAnimation.bigAssTankFluidU(
                LegacyUvAnimation.tickTime(worldTime, partialTicks), BIG_ASS_TANK_FLUID_U_SCALE);
        double fluidV = LegacyUvAnimation.bigAssTankFluidV(height, BIG_ASS_TANK_FLUID_U_SCALE);
        return bigAssTankFluidPlan(height, u.min(), u.max(), fluidV);
    }

    public static BigAssTankFluidPlan bigAssTankFluidPlan(double height, double minU,
            double maxU, double fluidV) {
        List<TexturedQuadPlan> quads = List.of(
                new TexturedQuadPlan("neg_x_fluid_face", List.of(
                        vertex(-BIG_ASS_TANK_FLUID_SIDE_OFFSET, BIG_ASS_TANK_FLUID_BASE_Y,
                                -BIG_ASS_TANK_FLUID_HALF_WIDTH, minU, 0.0D, 192),
                        vertex(-BIG_ASS_TANK_FLUID_SIDE_OFFSET, BIG_ASS_TANK_FLUID_BASE_Y + height,
                                -BIG_ASS_TANK_FLUID_HALF_WIDTH, minU, fluidV, 192),
                        vertex(-BIG_ASS_TANK_FLUID_SIDE_OFFSET, BIG_ASS_TANK_FLUID_BASE_Y + height,
                                BIG_ASS_TANK_FLUID_HALF_WIDTH, maxU, fluidV, 192),
                        vertex(-BIG_ASS_TANK_FLUID_SIDE_OFFSET, BIG_ASS_TANK_FLUID_BASE_Y,
                                BIG_ASS_TANK_FLUID_HALF_WIDTH, maxU, 0.0D, 192))),
                new TexturedQuadPlan("pos_x_fluid_face", List.of(
                        vertex(BIG_ASS_TANK_FLUID_SIDE_OFFSET, BIG_ASS_TANK_FLUID_BASE_Y,
                                -BIG_ASS_TANK_FLUID_HALF_WIDTH, maxU, 0.0D, 192),
                        vertex(BIG_ASS_TANK_FLUID_SIDE_OFFSET, BIG_ASS_TANK_FLUID_BASE_Y + height,
                                -BIG_ASS_TANK_FLUID_HALF_WIDTH, maxU, fluidV, 192),
                        vertex(BIG_ASS_TANK_FLUID_SIDE_OFFSET, BIG_ASS_TANK_FLUID_BASE_Y + height,
                                BIG_ASS_TANK_FLUID_HALF_WIDTH, minU, fluidV, 192),
                        vertex(BIG_ASS_TANK_FLUID_SIDE_OFFSET, BIG_ASS_TANK_FLUID_BASE_Y,
                                BIG_ASS_TANK_FLUID_HALF_WIDTH, minU, 0.0D, 192))));
        return new BigAssTankFluidPlan(height, minU, maxU, fluidV,
                normalAlphaBlendPlan(true, 0.0F), quads);
    }

    public static ChemicalPlantFluidPlan chemicalPlantFluidPlan(double animation, List<Integer> outputFluidColors,
            List<Integer> inputFluidColors) {
        ColorAveragePlan color = averageColor(!outputFluidColors.isEmpty() ? outputFluidColors : inputFluidColors);
        if (color.count() == 0) {
            return new ChemicalPlantFluidPlan(false, color, 0.0D, 0.0D, CHEMICAL_PLANT_FLUID_ALPHA,
                    normalAlphaBlendPlan(false, 0.0F), false);
        }
        return new ChemicalPlantFluidPlan(true, color,
                LegacyUvAnimation.chemicalPlantFluidU(animation),
                LegacyUvAnimation.chemicalPlantFluidV(animation),
                CHEMICAL_PLANT_FLUID_ALPHA, normalAlphaBlendPlan(false, 0.0F), false);
    }

    public static ChemicalFactoryPlan chemicalFactoryPlan(double animation) {
        double angle = animation * CHEMICAL_FACTORY_FAN_ROTATION_SCALE % 360.0D;
        return new ChemicalFactoryPlan(List.of(
                new RotatingModelPartPlan("chemical_factory_fan_1", "Fan1",
                        CHEMICAL_FACTORY_FAN_PIVOT_X, 0.0D, 0.0D,
                        0.0F, 1.0F, 0.0F, angle),
                new RotatingModelPartPlan("chemical_factory_fan_2", "Fan2",
                        -CHEMICAL_FACTORY_FAN_PIVOT_X, 0.0D, 0.0D,
                        0.0F, 1.0F, 0.0F, angle)));
    }

    public static BasicPressPlan basicPressPlan(boolean hasStack, double press, double maxPress) {
        double progress = maxPress <= 0.0D ? 0.0D : Math.max(0.0D, Math.min(1.0D, press / maxPress));
        return new BasicPressPlan(
                new TranslatedModelPartPlan("basic_press_head", "Head", true,
                        0.0D, (1.0D - progress) * BASIC_PRESS_HEAD_TRAVEL, 0.0D),
                new ItemTransformPlan("basic_press_item", hasStack,
                        BASIC_PRESS_ITEM_TRANSLATE_X, BASIC_PRESS_ITEM_TRANSLATE_Y, BASIC_PRESS_ITEM_TRANSLATE_Z,
                        BASIC_PRESS_ITEM_ROTATION_Y, BASIC_PRESS_ITEM_ROTATION_X, 0.0D,
                        BASIC_PRESS_ITEM_SCALE));
    }

    public static PyroOvenPlan pyroOvenPlan(double animation) {
        return new PyroOvenPlan(
                new TranslatedModelPartPlan("pyro_oven_slider", "Slider", true,
                        LegacyObjTransforms.softPeakSine(animation * PYRO_OVEN_SLIDER_ANIM_SCALE)
                                * PYRO_OVEN_SLIDER_TRAVEL_SCALE + PYRO_OVEN_SLIDER_BASE_X,
                        0.0D, 0.0D),
                new RotatingModelPartPlan("pyro_oven_fan", "Fan",
                        PYRO_OVEN_FAN_PIVOT_X, 0.0D, PYRO_OVEN_FAN_PIVOT_Z,
                        0.0F, 1.0F, 0.0F,
                        animation * PYRO_OVEN_FAN_ROTATION_SCALE % 360.0D));
    }

    public static PumpjackPlan pumpjackPlan(double rotationDegrees) {
        double radians = Math.toRadians(rotationDegrees);
        double sin = Math.sin(radians);
        double pRot = -sin * PUMPJACK_HEAD_ROTATION_SCALE;
        Vec3 backPos = rotateX(new Vec3(0.0D, 0.0D, -2.0D), pRot);
        Vec3 rot = rotateX(new Vec3(0.0D, 0.5D, 0.0D), -Math.toRadians(rotationDegrees - 90.0D));
        List<UntexturedQuadPlan> rods = new ArrayList<>();

        for (int i = -1; i <= 1; i += 2) {
            rods.add(new UntexturedQuadPlan("pumpjack_back_rod_" + i, List.of(
                    vertex(PUMPJACK_BACK_ROD_X * i,
                            PUMPJACK_ROTOR_PIVOT_Y + rot.y,
                            PUMPJACK_ROTOR_PIVOT_Z + rot.z - PUMPJACK_BACK_ROD_Z_WIDTH,
                            rgba(PUMPJACK_BACK_ROD_COLOR, 1.0F)),
                    vertex(PUMPJACK_BACK_ROD_X * i,
                            PUMPJACK_ROTOR_PIVOT_Y + rot.y,
                            PUMPJACK_ROTOR_PIVOT_Z + rot.z + PUMPJACK_BACK_ROD_Z_WIDTH,
                            rgba(PUMPJACK_BACK_ROD_COLOR, 1.0F)),
                    vertex(PUMPJACK_BACK_ROD_X * i,
                            PUMPJACK_HEAD_PIVOT_Y + backPos.y,
                            PUMPJACK_HEAD_PIVOT_Z + backPos.z + PUMPJACK_BACK_ROD_Z_WIDTH,
                            rgba(PUMPJACK_BACK_ROD_COLOR, 1.0F)),
                    vertex(PUMPJACK_BACK_ROD_X * i,
                            PUMPJACK_HEAD_PIVOT_Y + backPos.y,
                            PUMPJACK_HEAD_PIVOT_Z + backPos.z - PUMPJACK_BACK_ROD_Z_WIDTH,
                            rgba(PUMPJACK_BACK_ROD_COLOR, 1.0F)))));
        }

        double height = -sin;
        for (int i = -1; i <= 1; i += 2) {
            Vec3 frontPos = rotateX(new Vec3(0.0D, 0.0D, 1.0D), pRot);
            Vec3 frontRad = rotateX(new Vec3(0.0D, 0.0D,
                    PUMPJACK_FRONT_ROD_RADIUS + PUMPJACK_FRONT_ROD_DIST), pRot);
            frontRad = rotateX(frontRad, -Math.toRadians(PUMPJACK_FRONT_ROD_CUTLET * -3.0D));
            for (int j = 0; j < PUMPJACK_FRONT_ROD_SEGMENTS; j++) {
                Vec3 start = fixedPumpjackFrontRodPoint(frontPos, frontRad, PUMPJACK_FRONT_ROD_DIST);
                Vec3 nextRad = rotateX(frontRad, -Math.toRadians(PUMPJACK_FRONT_ROD_CUTLET));
                Vec3 end = fixedPumpjackFrontRodPoint(frontPos, nextRad, PUMPJACK_FRONT_ROD_DIST);
                rods.add(pumpjackFrontRodQuad("pumpjack_front_rod_" + i + "_" + j, i, start, end));
                frontRad = nextRad;
            }
            Vec3 tail = fixedPumpjackFrontRodPoint(frontPos, frontRad, PUMPJACK_FRONT_ROD_DIST);
            rods.add(new UntexturedQuadPlan("pumpjack_front_tail_" + i, List.of(
                    vertex((PUMPJACK_FRONT_ROD_WIDTH + PUMPJACK_FRONT_ROD_HALF_THICKNESS) * i,
                            PUMPJACK_HEAD_PIVOT_Y + tail.y, PUMPJACK_HEAD_PIVOT_Z + tail.z,
                            rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)),
                    vertex((PUMPJACK_FRONT_ROD_WIDTH - PUMPJACK_FRONT_ROD_HALF_THICKNESS) * i,
                            PUMPJACK_HEAD_PIVOT_Y + tail.y, PUMPJACK_HEAD_PIVOT_Z + tail.z,
                            rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)),
                    vertex((PUMPJACK_FRONT_ROD_WIDTH - PUMPJACK_FRONT_ROD_HALF_THICKNESS) * i,
                            2.0D + height, 0.0D,
                            rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)),
                    vertex((PUMPJACK_FRONT_ROD_WIDTH + PUMPJACK_FRONT_ROD_HALF_THICKNESS) * i,
                            2.0D + height, 0.0D,
                            rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)))));
        }

        double p = PUMPJACK_FRONT_ROD_HALF_THICKNESS;
        rods.add(new UntexturedQuadPlan("pumpjack_center_rod_a", List.of(
                vertex(p, height + 1.5D, p, rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)),
                vertex(-p, height + 1.5D, -p, rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)),
                vertex(-p, 0.75D, -p, rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)),
                vertex(p, 0.75D, p, rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)))));
        rods.add(new UntexturedQuadPlan("pumpjack_center_rod_b", List.of(
                vertex(-p, height + 1.5D, p, rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)),
                vertex(p, height + 1.5D, -p, rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)),
                vertex(p, 0.75D, -p, rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)),
                vertex(-p, 0.75D, p, rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F)))));

        return new PumpjackPlan(rotationDegrees,
                new RotatingModelPartPlan("pumpjack_rotor", "Rotor",
                        0.0D, PUMPJACK_ROTOR_PIVOT_Y, PUMPJACK_ROTOR_PIVOT_Z,
                        1.0F, 0.0F, 0.0F, rotationDegrees - 90.0D),
                new RotatingModelPartPlan("pumpjack_head", "Head",
                        0.0D, PUMPJACK_HEAD_PIVOT_Y, PUMPJACK_HEAD_PIVOT_Z,
                        1.0F, 0.0F, 0.0F,
                        Math.toDegrees(sin) * PUMPJACK_HEAD_ROTATION_SCALE),
                new TranslatedModelPartPlan("pumpjack_carriage", "Carriage", true,
                        0.0D, -sin, 0.0D),
                List.copyOf(rods));
    }

    public static TankDangerDiamondPlan smallTankDangerDiamondPlan(boolean hasFluid) {
        return new TankDangerDiamondPlan(hasFluid, List.of(
                new DiamondTransformPlan("front", -0.25D, 0.5D, -1.501D,
                        90.0F, 1.0F, 0.375F, 0.375F),
                new DiamondTransformPlan("back", 0.25D, 0.5D, 1.501D,
                        -90.0F, 1.0F, 0.375F, 0.375F)));
    }

    public static TankDangerDiamondPlan bigAssTankDangerDiamondPlan(boolean hasFluid) {
        return radialDangerDiamondPlan(hasFluid, 2, 22.5F, 180.0F, 5.5D, 2.0D,
                0.0D, 1.0F, 1.0F, 1.0F);
    }

    public static TankDangerDiamondPlan bat9000DangerDiamondPlan(boolean hasFluid) {
        return radialDangerDiamondPlan(hasFluid, 4, 45.0F, 90.0F, 2.5D, 2.25D,
                0.0D, 1.0F, 0.75F, 0.75F);
    }

    public static CherenkovShellPlan smallReactorCherenkovPlan(double totalFlux,
            boolean submerged, double randomUnit) {
        return smallReactorCherenkovPlan(totalFlux, submerged, List.of(randomUnit));
    }

    public static CherenkovShellPlan smallReactorCherenkovPlan(double totalFlux,
            boolean submerged, List<Double> randomUnits) {
        boolean active = totalFlux > 10.0D && submerged;
        List<UntexturedQuadPlan> shells = new ArrayList<>();
        if (active) {
            int shell = 0;
            for (double d = SMALL_REACTOR_CHERENKOV_START; d < SMALL_REACTOR_CHERENKOV_END;
                    d += SMALL_REACTOR_CHERENKOV_STEP) {
                double randomUnit = shell < randomUnits.size() ? randomUnits.get(shell) : 0.0D;
                int alpha = alphaByte(0.025D + clamp01(randomUnit) * 0.015D + 0.125D * totalFlux / 1000.0D);
                RgbaPlan color = rgba(0.4F, 0.9F, 1.0F, alpha / 255.0F);
                double bottom = SMALL_REACTOR_CHERENKOV_CENTER_Y - d;
                double top = SMALL_REACTOR_CHERENKOV_CENTER_Y + d;
                shells.addAll(glowBoxQuads("small_reactor_cherenkov_" + shells.size(),
                        -d, bottom, -d, d, top, d, color));
                shell++;
            }
        }
        return new CherenkovShellPlan(active, additiveBlendPlan(true, 0.0F),
                false, false, shells);
    }

    public static RbmkFuelChannelGlowPlan rbmkFuelChannelCherenkovPlan(boolean cherenkov, int offset) {
        List<UntexturedQuadPlan> quads = new ArrayList<>();
        if (cherenkov) {
            RgbaPlan color = rgba(0.4F, 0.9F, 1.0F, 0.1F);
            double max = Math.max(0, offset);
            for (double y = 0.0D; y <= max; y += RBMK_CHERENKOV_STEP) {
                quads.add(new UntexturedQuadPlan("rbmk_cherenkov_plane_" + quads.size(), List.of(
                        vertex(-0.5D, y, -0.5D, color),
                        vertex(-0.5D, y, 0.5D, color),
                        vertex(0.5D, y, 0.5D, color),
                        vertex(0.5D, y, -0.5D, color))));
            }
        }
        return new RbmkFuelChannelGlowPlan(cherenkov, Math.max(0, offset),
                RBMK_CHERENKOV_TRANSLATE_Y, additiveBlendPlan(true, 0.0F),
                false, false, quads);
    }

    public static SolarBoilerBeamPlan solarBoilerBeamPlan(List<SolarBeamTargetPlan> targets, int limit) {
        List<SolarBeamPlan> beams = new ArrayList<>();
        int safeLimit = Math.max(0, limit);
        for (SolarBeamTargetPlan target : targets) {
            if (beams.size() >= safeLimit) {
                break;
            }
            double dist = Math.sqrt(target.dx() * target.dx() + target.dy() * target.dy()
                    + target.dz() * target.dz());
            if (dist <= 0.0D) {
                continue;
            }
            double pitch = Math.toDegrees(-Math.asin((target.dy() + 0.5D) / dist)) + 90.0D;
            double yaw = Math.toDegrees(-Math.atan2(target.dz(), target.dx())) + 180.0D;
            beams.add(new SolarBeamPlan(target, dist, yaw, pitch,
                    additiveBlendPlan(false, 0.0F), solarBeamQuads(beams.size(), dist)));
        }
        return new SolarBoilerBeamPlan(!beams.isEmpty(), safeLimit, List.copyOf(beams));
    }

    public static RefuelerFluidPlan refuelerFluidPlan(double fillLevel, int fluidColor) {
        double fill = clamp01(fillLevel);
        return new RefuelerFluidPlan(fill, (1.0D - fill) * REFUELER_FLUID_TRAVEL,
                new ClipPlanePlan(0.0D, 1.0D, 0.0D, REFUELER_FLUID_CLIP_D),
                rgba(fluidColor, 0.75F), additiveBlendPlan(true, 0.0F));
    }

    public static ModelPartTintPlan radgenLightPlan(boolean on) {
        return new ModelPartTintPlan("radgen_light", "Light", true,
                on ? rgba(0.0F, 1.0F, 0.0F, 1.0F) : rgba(0.0F, 0.1F, 0.0F, 1.0F),
                lightmapOnlyFullbrightPlan(), null, false);
    }

    public static ModelPartTintPlan radgenGlassPlan() {
        return new ModelPartTintPlan("radgen_glass", "Glass", true,
                rgba(0.5F, 0.75F, 1.0F, 0.3F), null,
                normalAlphaBlendPlan(false, 0.0F), false);
    }

    public static ScaledModelPartPlan liquefactorFluidPlan(int fill, int maxFill, int fluidColor) {
        double height = maxFill <= 0 ? 0.0D : clamp01((double) Math.max(0, fill) / (double) maxFill);
        return new ScaledModelPartPlan("liquefactor_fluid", "Fluid", height > 0.0D,
                rgba(fluidColor, 1.0F), LIQUEFACTOR_FLUID_PIVOT_Y,
                1.0D, height, 1.0D, null, false);
    }

    public static ModelPartTintPlan liquefactorGlassPlan() {
        return new ModelPartTintPlan("liquefactor_glass", "Glass", true,
                rgba(0.75F, 1.0F, 1.0F, 0.15F), null,
                normalAlphaBlendPlan(false, 0.0F), false);
    }

    public static ScaledModelPartPlan solidifierFluidPlan(int fill, int maxFill, int fluidColor) {
        double height = maxFill <= 0 ? 0.0D : clamp01((double) Math.max(0, fill) / (double) maxFill);
        return new ScaledModelPartPlan("solidifier_fluid", "Fluid", height > 0.0D,
                rgba(fluidColor, 1.0F), SOLIDIFIER_FLUID_PIVOT_Y,
                1.0D, height, 1.0D, null, false);
    }

    public static ModelPartTintPlan solidifierGlassPlan() {
        return new ModelPartTintPlan("solidifier_glass", "Glass", true,
                rgba(0.75F, 1.0F, 1.0F, 0.15F), null,
                normalAlphaBlendPlan(false, 0.0F), false);
    }

    public static ModelPartTintPlan crystallizerFluidPlan(boolean spinning) {
        return new ModelPartTintPlan("crystallizer_fluid", "Fluid", spinning,
                rgba(1.0F, 1.0F, 1.0F, 1.0F), null,
                normalAlphaBlendPlan(false, 0.1F), true);
    }

    public static BatteryReddTrailPlan batteryReddWheelTrailPlan(double speed) {
        double span = Math.max(0.0D, speed) * BATTERY_REDD_TRAIL_SPAN_SCALE;
        List<UntexturedQuadPlan> quads = new ArrayList<>();
        if (span > 0.0D) {
            for (int sign = -1; sign <= 1; sign += 2) {
                double xOffset = BATTERY_REDD_TRAIL_X_OFFSET * sign;
                for (int spoke = 0; spoke < BATTERY_REDD_TRAIL_SPOKES; spoke++) {
                    double startAngle = spoke * 45.0D;
                    quads.add(trailSegment("battery_redd_trail_" + sign + "_" + spoke + "_0",
                            xOffset, startAngle, startAngle + span, 0.75F, 0.5F));
                    quads.add(trailSegment("battery_redd_trail_" + sign + "_" + spoke + "_1",
                            xOffset, startAngle + span, startAngle + span * 2.0D, 0.5F, 0.25F));
                    quads.add(trailSegment("battery_redd_trail_" + sign + "_" + spoke + "_2",
                            xOffset, startAngle + span * 2.0D, startAngle + span * 3.0D, 0.25F, 0.0F));
                }
            }
        }
        return new BatteryReddTrailPlan(span > 0.0D, speed, span,
                additiveBlendPlan(false, 0.0F), batteryReddManualFullbrightPlan(), quads);
    }

    public static BatteryReddPlasmaPlan batteryReddPlasmaPlan(long currentMillis,
            double speed, boolean renderExtraLayers) {
        double time = (double) currentMillis;
        float alpha = 0.45F + (float) (Math.sin(time / BATTERY_REDD_PLASMA_PERIOD) * 0.15D);
        float alphaMultiplier = (float) Math.max(0.0D, speed) / 15.0F;
        double mainOsc = LegacyObjTransforms.softPeakSine(time / BATTERY_REDD_PLASMA_PERIOD) % 1.0D;
        double sparkleSpin = time / BATTERY_REDD_SPARKLE_SPIN_PERIOD * -1.0D % 1.0D;
        double sparkleOsc = Math.sin(time / BATTERY_REDD_PLASMA_PERIOD) * 0.5D % 1.0D;
        List<TextureMatrixPartPlan> layers = new ArrayList<>();
        layers.add(new TextureMatrixPartPlan("plasma", "Plasma",
                rgba(1.0F, 0.25F, 0.75F, alpha * alphaMultiplier),
                new LegacyUvAnimation.TextureMatrixPlan(
                        LegacyUvAnimation.TextureMatrixOrder.SCALE_ROTATE_TRANSLATE,
                        1.0D, 1.0D, 0.0D, 0.0D, mainOsc)));
        if (renderExtraLayers) {
            layers.add(new TextureMatrixPartPlan("sparkle", "Plasma",
                    rgba(2.0F, 0.5F, 1.5F, 0.75F * alphaMultiplier),
                    new LegacyUvAnimation.TextureMatrixPlan(
                            LegacyUvAnimation.TextureMatrixOrder.SCALE_ROTATE_TRANSLATE,
                            1.0D, 1.0D, 0.0D, sparkleSpin, sparkleOsc)));
        }
        return new BatteryReddPlasmaPlan(speed > 0.0D, alpha, alphaMultiplier,
                additiveBlendPlan(false, 0.0F), batteryReddManualFullbrightPlan(), List.copyOf(layers));
    }

    public static CoreStandbyPlan coreStandbyPlan(long currentMillis) {
        boolean sparkTick = currentMillis / 100L % 10L == 0L;
        List<SparkInvocationPlan> sparks = new ArrayList<>();
        if (sparkTick) {
            for (int i = 0; i < 3; i++) {
                sparks.add(new SparkInvocationPlan((int) (currentMillis / 100L) + i * 10000,
                        0.0D, 0.0D, 0.0D, 1.5F, 5, 10, 0xFFFF00, 0xFFFFFF));
                sparks.add(new SparkInvocationPlan((int) (currentMillis / 50L) + i * 10000,
                        0.0D, 0.0D, 0.0D, 1.5F, 5, 10, 0xFFFF00, 0xFFFFFF));
            }
        }
        return new CoreStandbyPlan(
                new ModelSpherePlan("standby_base", "sphere_uv", CORE_STANDBY_BASE_SCALE,
                        rgba(0.5F, 0.5F, 0.5F, 1.0F), null, false, true, false),
                new ModelSpherePlan("standby_glow", "sphere_uv", CORE_STANDBY_GLOW_SCALE,
                        rgba(0.1F, 0.1F, 0.1F, 1.0F), additiveBlendPlan(true, 0.1F), false, true, false),
                sparkTick, List.copyOf(sparks));
    }

    public static CoreOrbPlan coreOrbPlan(int color, int fill, int totalFill, long worldTime) {
        double fillScale = totalFill <= 0 ? 0.5D : 4.5D * Math.max(0, fill) / (double) totalFill + 0.5D;
        double pulse = corePulse(worldTime * 0.1D);
        RgbaPlan baseColor = rgbaLegacy256(color, 0.4F, 1.0F);
        List<ModelSpherePlan> shells = new ArrayList<>();
        for (int i = 0; i < CORE_ORB_GLOW_SHELLS; i++) {
            double scale = fillScale * CORE_ORB_BASE_SCALE * (1.0D + 0.25D * i
                    + pulse * (20.0D - i) * 0.125D);
            shells.add(new ModelSpherePlan("orb_glow_shell_" + i, "sphere_ruv", scale,
                    baseColor, additiveBlendPlan(true, 0.1F), false, true, false));
        }
        return new CoreOrbPlan(fillScale, pulse,
                new ModelSpherePlan("orb_base", "sphere_ruv", fillScale * CORE_ORB_BASE_SCALE,
                        baseColor, null, false, true, false), List.copyOf(shells));
    }

    public static CoreFlarePlan coreFlarePlan(int color, long worldTime) {
        double pulse = corePulse(worldTime * 0.2D);
        double scale = 0.875D + pulse * 0.125D;
        double spin = worldTime / 200.0D * 90.0D;
        Random random = new Random(CORE_FLARE_RANDOM_SEED);
        List<CoreFlareRayPlan> rays = new ArrayList<>();
        RgbaPlan center = rgba(color, 1.0F);
        RgbaPlan edge = rgba(color, 0.0F);
        for (int i = 0; i < CORE_FLARE_RAYS; i++) {
            rays.add(new CoreFlareRayPlan(i,
                    random.nextFloat() * 360.0F,
                    random.nextFloat() * 360.0F,
                    random.nextFloat() * 360.0F,
                    random.nextFloat() * 360.0F,
                    random.nextFloat() * 360.0F,
                    random.nextFloat() * 360.0F + spin,
                    random.nextFloat() * 2.0F + 5.0F,
                    random.nextFloat() * 1.0F + 1.0F,
                    center, edge));
        }
        return new CoreFlarePlan(scale, additiveBlendPlan(false, 0.0F), false, false,
                true, List.copyOf(rays));
    }

    public static CoreVoidPlan coreVoidPlan(int fill, int totalFill, long worldTime) {
        Random random = new Random(CORE_VOID_RANDOM_SEED);
        float textureTranslateY = worldTime % 500L / 500.0F;
        TextureGenOffsetPlan initialTextureOffset = new TextureGenOffsetPlan(
                random.nextFloat(), textureTranslateY, random.nextFloat());
        double radius = totalFill <= 0 ? 0.5D : 2.25D * Math.max(0, fill) / (double) totalFill + 0.5D;
        List<CoreVoidLayerPlan> layers = new ArrayList<>();
        for (int i = 0; i < CORE_VOID_LAYERS; i++) {
            float f5 = CORE_VOID_LAYERS - i;
            float f7 = 1.0F / (f5 + 1.0F);
            String texture = "portal";
            BlendStatePlan blend = oneOneAdditiveBlendPlan(true, 0.1F);
            if (i == 0) {
                texture = "end_sky";
                f7 = 0.0F;
                blend = normalAlphaBlendPlan(true, 0.1F);
            } else if (i == 1) {
                texture = "end_portal";
                blend = oneOneAdditiveBlendPlan(true, 0.1F);
            }
            TextureGenOffsetPlan layerOffset = new TextureGenOffsetPlan(
                    random.nextFloat() * (1.0F - f7),
                    random.nextFloat() * (1.0F - f7),
                    random.nextFloat() * (1.0F - f7));
            float rotation = 360.0F / CORE_VOID_LAYERS * i
                    + 360.0F / CORE_VOID_LAYERS * random.nextFloat();
            float red = ((float) random.nextDouble() * 0.5F + 0.4F) * f7;
            float green = ((float) random.nextDouble() * 0.5F + 0.4F) * f7;
            float blue = ((float) random.nextDouble() * 0.5F + 2.0F) * f7;
            if (i == 0) {
                red = 0.0F;
                green = 0.0F;
                blue = 0.0F;
            }
            layers.add(new CoreVoidLayerPlan(i, texture, blend, layerOffset, 0.8F,
                    rotation, rgba(red, green, blue, 1.0F), radius,
                    coreVoidTriangles(i, radius, rgba(red, green, blue, 1.0F))));
        }
        return new CoreVoidPlan(true, true, initialTextureOffset, CORE_VOID_LAYERS,
                CORE_VOID_SEGMENTS, List.copyOf(layers));
    }

    public static RadarScreenPlan radarScreenPlan(boolean linked, long worldTime,
            float partialTicks, int range, int refX, int refZ, List<RadarBlipInputPlan> blips,
            int offlineTextureOffset) {
        if (linked) {
            double offset = ((worldTime % (long) RADAR_SCAN_PERIOD) + partialTicks) / RADAR_SCAN_DIVISOR;
            UntexturedQuadPlan scan = new UntexturedQuadPlan("radar_scanline", List.of(
                    vertex(RADAR_SCREEN_X, 2.0D - offset, RADAR_SCREEN_MAX_Z, rgba(0.0F, 1.0F, 0.0F, 0.0F)),
                    vertex(RADAR_SCREEN_X, 2.0D - offset, RADAR_SCREEN_MIN_Z, rgba(0.0F, 1.0F, 0.0F, 0.0F)),
                    vertex(RADAR_SCREEN_X, 2.0D - offset - RADAR_SCAN_THICKNESS, RADAR_SCREEN_MIN_Z,
                            rgba(0.0F, 1.0F, 0.0F, 50.0F / 255.0F)),
                    vertex(RADAR_SCREEN_X, 2.0D - offset - RADAR_SCAN_THICKNESS, RADAR_SCREEN_MAX_Z,
                            rgba(0.0F, 1.0F, 0.0F, 50.0F / 255.0F))));
            List<TexturedQuadPlan> blipQuads = new ArrayList<>();
            int safeRange = Math.max(0, range);
            for (RadarBlipInputPlan blip : blips) {
                double sX = (blip.posX() - refX) / ((double) safeRange + 1.0D) * 0.875D;
                double sZ = (blip.posZ() - refZ) / ((double) safeRange + 1.0D) * 0.875D;
                blipQuads.add(radarBlipQuad(blipQuads.size(), sX, sZ, blip.blipLevel()));
            }
            return new RadarScreenPlan(true, normalAlphaBlendPlan(false, 0.1F),
                    scan, List.copyOf(blipQuads), null);
        }
        return new RadarScreenPlan(false, normalAlphaBlendPlan(true, 0.1F),
                null, List.of(), radarOfflineQuad(offlineTextureOffset));
    }

    public static ExcavatorChutePlan excavatorChutePlan(boolean active, boolean crusherEnabled,
            long currentMillis) {
        if (!active) {
            return new ExcavatorChutePlan(false, crusherEnabled, 0.0D, 0.0D, List.of(), List.of());
        }
        double dropU = -((double) (currentMillis % (long) EXCAVATOR_DROP_PERIOD_MILLIS)
                / EXCAVATOR_DROP_PERIOD_MILLIS);
        double dropL = dropU + 4.0D;
        List<NormalTexturedQuadPlan> upper = fallingColumnQuads("excavator_upper_cobble",
                EXCAVATOR_UPPER_HALF_WIDTH, EXCAVATOR_UPPER_HALF_WIDTH,
                3.0D, 2.0D, 1.0D, 0.0D, dropU, dropL);
        double widthX = crusherEnabled ? 0.5D : 0.25D;
        double widthZ = EXCAVATOR_LOWER_HALF_DEPTH;
        double uMax = crusherEnabled ? 4.0D : 2.0D;
        List<NormalTexturedQuadPlan> lower = fallingColumnQuads(
                crusherEnabled ? "excavator_lower_gravel" : "excavator_lower_cobble",
                widthX, widthZ, 2.0D, 1.0D, uMax, 0.5D, dropU, dropL);
        return new ExcavatorChutePlan(true, crusherEnabled, dropU, dropL, upper, lower);
    }

    public static DoorGenericStatePlan doorGenericStatePlan(List<ClipPlanePlan> clippingPlanes,
            boolean sednaRenderer, boolean animatedModel) {
        return new DoorGenericStatePlan(sednaRenderer, animatedModel, List.copyOf(clippingPlanes),
                normalAlphaBlendPlan(true, 0.1F), true);
    }

    public static SoyuzMultiblockGhostPlan soyuzMultiblockGhostPlan() {
        List<SoyuzGhostRangePlan> ranges = List.of(
                new SoyuzGhostRangePlan("struct_launcher", -6, 6, 3, 4, -6, 6),
                new SoyuzGhostRangePlan("struct_launcher", -1, 1, 3, 4, -8, -7),
                new SoyuzGhostRangePlan("struct_launcher", -2, 2, 3, 4, 7, 9),
                new SoyuzGhostRangePlan("struct_launcher", -2, 2, 51, 51, 5, 9),
                new SoyuzGhostRangePlan("struct_launcher", -1, 1, 38, 38, -8, -6),
                new SoyuzGhostRangePlan("concrete_smooth", 3, 6, 0, 2, 3, 6),
                new SoyuzGhostRangePlan("concrete_smooth", -6, -3, 0, 2, 3, 6),
                new SoyuzGhostRangePlan("concrete_smooth", -6, -3, 0, 2, -6, -3),
                new SoyuzGhostRangePlan("concrete_smooth", 3, 6, 0, 2, -6, -3),
                new SoyuzGhostRangePlan("concrete_smooth", -1, 1, 0, 2, -8, -6),
                new SoyuzGhostRangePlan("concrete_smooth", -2, 2, 0, 2, 5, 9),
                new SoyuzGhostRangePlan("struct_scaffold", -1, 1, 5, 50, 6, 8),
                new SoyuzGhostRangePlan("struct_scaffold", 0, 0, 5, 37, -7, -7));
        int blockCount = 0;
        for (SoyuzGhostRangePlan range : ranges) {
            blockCount += range.blockCount();
        }
        return new SoyuzMultiblockGhostPlan(normalAlphaBlendPlan(true, 0.0F),
                rgba(1.0F, 1.0F, 1.0F, SOYUZ_GHOST_ALPHA),
                true, false, SOYUZ_SMALL_BLOCK_MIN, SOYUZ_SMALL_BLOCK_MAX,
                blockCount, ranges, soyuzSmallBlockQuads());
    }

    public static RbmkNumitronPlan rbmkNumitronPlan(List<RbmkNumitronInputPlan> inputs) {
        List<RbmkNumitronUnitPlan> units = new ArrayList<>();
        int count = inputs == null ? 0 : Math.min(RBMK_NUMITRON_COUNT, inputs.size());
        for (int i = 0; i < count; i++) {
            RbmkNumitronInputPlan input = inputs.get(i);
            if (input == null || !input.active()) {
                continue;
            }
            units.add(rbmkNumitronUnitPlan(input, i));
        }
        return new RbmkNumitronPlan(!units.isEmpty(), RBMK_NUMITRON_COUNT,
                fullbrightStatePlan(), List.copyOf(units));
    }

    public static StrandCasterPlan strandCasterPlan(int amount, int capacity, int moldCost, int moltenColor) {
        boolean active = amount != 0 && capacity > 0 && moldCost > 0;
        RgbaPlan color = rgba(moltenColor, 1.0F);
        double level = active ? (double) amount / (double) capacity * STRAND_CASTER_FLUID_HEIGHT : 0.0D;
        double offset = active ? (double) amount / (double) moldCost * STRAND_CASTER_PLATE_TRAVEL : 0.0D;
        StrandCasterPlatePlan plate = null;
        StrandCasterLavaPlan lava = null;
        if (active) {
            plate = new StrandCasterPlatePlan("plate", Math.max(-offset + STRAND_CASTER_PLATE_BASE_Z, 0.0D),
                    new ClipPlanePlan(0.0D, 0.0D, -1.0D, 0.5D), color, true);
            lava = new StrandCasterLavaPlan("lava_gray", lightmapOnlyFullbrightPlan(),
                    false, color, strandCasterLavaQuad(level));
        }
        return new StrandCasterPlan(active, amount, capacity, moldCost, level, offset, color, plate, lava);
    }

    public static MaxwellBeamPlan maxwellBeamPlan(int beamTicks, double beamDistance,
            long worldTime, float partialTicks) {
        return maxwellBeamPlan(beamTicks, beamDistance, MAXWELL_BARREL_LENGTH,
                LegacyUvAnimation.tickTime(worldTime, partialTicks));
    }

    public static MaxwellBeamPlan maxwellBeamPlan(int beamTicks, double beamDistance,
            double barrelLength, double renderTime) {
        double length = Math.max(0.0D, beamDistance - barrelLength);
        boolean active = beamTicks > 0 && length > 0.0D;
        List<LegacyBeamRenderer.BeamPlan> beams = new ArrayList<>();
        if (active) {
            int segments = (int) (beamDistance + 1.0D);
            for (int i = 0; i < MAXWELL_BEAM_COUNT; i++) {
                int start = (int) ((renderTime * MAXWELL_BEAM_SPIN_SPEED + i * MAXWELL_BEAM_PHASE_STEP) % 360.0D);
                beams.add(LegacyBeamRenderer.beamPlan(false, length, 0.0D, 0.0D,
                        LegacyBeamRenderer.WaveType.SPIRAL, LegacyBeamRenderer.BeamType.SOLID,
                        MAXWELL_BEAM_COLOR, MAXWELL_BEAM_COLOR, start, segments,
                        MAXWELL_BEAM_SIZE, MAXWELL_BEAM_LAYERS, MAXWELL_BEAM_THICKNESS));
            }
        }
        return new MaxwellBeamPlan(active, beamTicks, beamDistance, barrelLength, length,
                barrelLength, MAXWELL_BEAM_TRANSLATE_Y, 0.0D,
                lightmapOnlyFullbrightPlan(), additiveBlendPlan(false, 0.0F),
                false, false, false, List.copyOf(beams));
    }

    public static TauonBeamPlan tauonBeamPlan(int beamTicks, double beamDistance, long worldTime) {
        return tauonBeamPlan(beamTicks, beamDistance, (double) worldTime);
    }

    public static TauonBeamPlan tauonBeamPlan(int beamTicks, double beamDistance, double renderTime) {
        boolean active = beamTicks > 0 && beamDistance > 0.0D;
        LegacyBeamRenderer.BeamPlan beam = active
                ? LegacyBeamRenderer.beamPlan(beamDistance, 0.0D, 0.0D,
                        LegacyBeamRenderer.WaveType.RANDOM, LegacyBeamRenderer.BeamType.LINE,
                        TAUON_BEAM_OUTER_COLOR, TAUON_BEAM_INNER_COLOR,
                        (int) ((renderTime / 5.0D) % 360.0D), (int) beamDistance + 1,
                        TAUON_BEAM_SIZE, 0, 0.0F)
                : null;
        return new TauonBeamPlan(active, beamTicks, beamDistance,
                0.0D, TAUON_BEAM_TRANSLATE_Y, 0.0D,
                lightmapOnlyFullbrightPlan(), beam);
    }

    public static CreativeBatterySocketPlan creativeBatterySocketPlan(long worldTime, long currentMillis,
            float partialTicks) {
        double horseYaw = ((worldTime % 360L) + partialTicks) * 25.0D;
        Random random = new Random(worldTime / 5L);
        random.nextBoolean();
        int start = (int) (currentMillis % CREATIVE_BATTERY_BEAM_PERIOD_MILLIS)
                / CREATIVE_BATTERY_BEAM_START_DIVISOR;
        List<LegacyBeamRenderer.BeamPlan> beams = new ArrayList<>();
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                if (random.nextInt(CREATIVE_BATTERY_BEAM_RANDOM_BOUND) != 0) {
                    continue;
                }
                double x = CREATIVE_BATTERY_BEAM_XZ * i;
                double z = CREATIVE_BATTERY_BEAM_XZ * j;
                beams.add(creativeBatterySocketBeam(x, z, start, CREATIVE_BATTERY_LONG_BEAM_SEGMENTS,
                        CREATIVE_BATTERY_LONG_BEAM_SIZE));
                beams.add(creativeBatterySocketBeam(x, z, start, CREATIVE_BATTERY_SHORT_BEAM_SEGMENTS,
                        CREATIVE_BATTERY_SHORT_BEAM_SIZE));
            }
        }
        return new CreativeBatterySocketPlan(CREATIVE_BATTERY_HORSE_SCALE, horseYaw,
                0.0D, CREATIVE_BATTERY_BEAM_TRANSLATE_Y, 0.0D, List.copyOf(beams));
    }

    public static LaserMinerBeamPlan laserMinerBeamPlan(boolean active, double vx, double vy,
            double vz, long worldTime) {
        double lengthToTarget = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (!active || lengthToTarget <= 1.0E-5D) {
            return new LaserMinerBeamPlan(false, vx, vy, vz, 0.0D, 0.0D, 0.0D,
                    0.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0, List.of());
        }
        double nx = vx / lengthToTarget * LASER_MINER_EMITTER_OFFSET;
        double ny = vy / lengthToTarget * LASER_MINER_EMITTER_OFFSET;
        double nz = vz / lengthToTarget * LASER_MINER_EMITTER_OFFSET;
        double bx = vx - nx;
        double by = vy - ny;
        double bz = vz - nz;
        double beamLength = Math.sqrt(bx * bx + by * by + bz * bz);
        int range = (int) Math.ceil(beamLength * 0.5D);
        int baseStart = (int) (worldTime * -25L % 360L);
        List<LegacyBeamRenderer.BeamPlan> beams = new ArrayList<>();
        for (int i = 0; i < LASER_MINER_BEAM_COUNT; i++) {
            beams.add(LegacyBeamRenderer.beamPlan(bx, by, bz,
                    LegacyBeamRenderer.WaveType.SPIRAL, LegacyBeamRenderer.BeamType.SOLID,
                    LASER_MINER_BEAM_COLOR, LASER_MINER_BEAM_COLOR,
                    baseStart + i * LASER_MINER_PHASE_STEP, range * 2,
                    LASER_MINER_BEAM_SIZE, LASER_MINER_BEAM_LAYERS,
                    LASER_MINER_BEAM_THICKNESS));
        }
        double yaw = Math.toDegrees(Math.atan2(bx, bz));
        double pitch = Math.toDegrees(Math.atan2(by, Math.sqrt(bx * bx + bz * bz)));
        return new LaserMinerBeamPlan(true, vx, vy, vz, bx, by, bz,
                nx, ny - 1.0D, nz, beamLength, yaw, pitch, range, List.copyOf(beams));
    }

    public static IcfLaserBeamPlan icfLaserBeamPlan(double laserLength) {
        boolean active = laserLength > 0.0D;
        LegacyBeamRenderer.BeamPlan beam = active
                ? LegacyBeamRenderer.beamPlan(laserLength, 0.0D, 0.0D,
                        LegacyBeamRenderer.WaveType.SPIRAL, LegacyBeamRenderer.BeamType.SOLID,
                        ICF_LASER_OUTER_COLOR, ICF_LASER_INNER_COLOR,
                        0, 1, 0.0F, ICF_LASER_LAYERS, ICF_LASER_THICKNESS)
                : null;
        return new IcfLaserBeamPlan(active, laserLength, lightmapOnlyFullbrightPlan(),
                false, false, beam);
    }

    public static TeslaBeamPlan teslaBeamPlan(double sourceX, double sourceY, double sourceZ,
            List<TeslaTargetPlan> targets, long worldTime) {
        List<TeslaTargetBeamPlan> beams = new ArrayList<>();
        if (targets != null) {
            for (int i = 0; i < targets.size(); i++) {
                TeslaTargetPlan target = targets.get(i);
                double dx = target.x() - sourceX;
                double dy = target.y() - sourceY;
                double dz = target.z() - sourceZ;
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                LegacyBeamRenderer.BeamPlan beam = LegacyBeamRenderer.beamPlan(
                        -dx, dy, -dz, LegacyBeamRenderer.WaveType.RANDOM,
                        LegacyBeamRenderer.BeamType.SOLID, TESLA_BEAM_COLOR, TESLA_BEAM_COLOR,
                        (int) (worldTime % 1000L) + 1, (int) (length * 5.0D),
                        TESLA_BEAM_SIZE, TESLA_BEAM_LAYERS, TESLA_BEAM_THICKNESS);
                beams.add(new TeslaTargetBeamPlan(target, length, beam));
            }
        }
        return new TeslaBeamPlan(!beams.isEmpty(), sourceX, sourceY, sourceZ,
                false, List.copyOf(beams));
    }

    public static ExposureChamberPlan exposureChamberPlan(boolean on, double prevRotation,
            double rotation, long worldTime, long currentMillis, float partialTicks) {
        double interpolatedRotation = prevRotation + (rotation - prevRotation) * partialTicks;
        double coreBob = on
                ? Math.sin(((double) worldTime % EXPOSURE_CORE_BOB_PERIOD + partialTicks)
                        * EXPOSURE_CORE_BOB_SPEED) * EXPOSURE_CORE_BOB_AMOUNT
                : 0.0D;
        List<TranslatedBeamPlan> beams = new ArrayList<>();
        if (on) {
            int randomColor = worldTime % EXPOSURE_RANDOM_DURATION >= EXPOSURE_RANDOM_DURATION / 2
                    ? EXPOSURE_RANDOM_BLUE_COLOR : EXPOSURE_RANDOM_WHITE_COLOR;
            Random random = new Random(worldTime / EXPOSURE_RANDOM_DURATION);
            random.nextInt(EXPOSURE_RANDOM_CHANCE);
            addExposureRandomBeam(beams, random, randomColor, 0.0D, 3.675D, -7.5D, currentMillis, "random_top");
            addExposureRandomBeam(beams, random, randomColor, 1.1875D, 2.5D, -7.5D, currentMillis, "random_right");
            addExposureRandomBeam(beams, random, randomColor, -1.1875D, 2.5D, -7.5D, currentMillis, "random_left");
            int loopStart = (int) (currentMillis % 1000L) / 50;
            beams.add(translatedBeam("vertical_blue", 0.0D, 1.75D, 0.0D,
                    0.0D, 1.5D, 0.0D, LegacyBeamRenderer.WaveType.RANDOM,
                    LegacyBeamRenderer.BeamType.LINE, 0x80D0FF, 0xFFFFFF,
                    loopStart, 10, 0.125F, 1, 0.0F));
            beams.add(translatedBeam("vertical_purple", 0.0D, 1.75D, 0.0D,
                    0.0D, 1.5D, 0.0D, LegacyBeamRenderer.WaveType.RANDOM,
                    LegacyBeamRenderer.BeamType.LINE, 0x8080FF, 0xFFFFFF,
                    (int) (currentMillis + 5L) / 50, 10, 0.125F, 1, 0.0F));
            beams.add(translatedBeam("spiral_yellow", 0.0D, 2.5D, 0.0D,
                    0.0D, 0.0D, -1.0D, LegacyBeamRenderer.WaveType.SPIRAL,
                    LegacyBeamRenderer.BeamType.LINE, 0xFFFF80, 0xFFFFFF,
                    (int) (currentMillis % 360L), 15, 0.125F, 1, 0.0F));
            beams.add(translatedBeam("spiral_red", 0.0D, 2.5D, 0.0D,
                    0.0D, 0.0D, -1.0D, LegacyBeamRenderer.WaveType.SPIRAL,
                    LegacyBeamRenderer.BeamType.LINE, 0xFF8080, 0xFFFFFF,
                    (int) (currentMillis % 360L) + 180, 15, 0.125F, 1, 0.0F));
        }
        return new ExposureChamberPlan(on, interpolatedRotation, interpolatedRotation / 2.0D,
                coreBob, lightmapOnlyFullbrightPlan(), false, false, List.copyOf(beams));
    }

    public static DfcBeamPlan dfcEmitterBeamPlan(int range, long worldTime) {
        List<TranslatedBeamPlan> beams = new ArrayList<>();
        if (range > 0) {
            beams.add(translatedDepthBeam("emitter_depth", 0.0D, DFC_BEAM_TRANSLATE_Y, 0.0D,
                    0.0D, 0.0D, range, LegacyBeamRenderer.WaveType.SPIRAL,
                    LegacyBeamRenderer.BeamType.SOLID, DFC_EMITTER_DEPTH_COLOR, DFC_EMITTER_DEPTH_COLOR,
                    0, 1, 0.0F, 2, 0.0625F));
            beams.add(translatedDepthBeam("emitter_random_a", 0.0D, DFC_BEAM_TRANSLATE_Y, 0.0D,
                    0.0D, 0.0D, range, LegacyBeamRenderer.WaveType.RANDOM,
                    LegacyBeamRenderer.BeamType.SOLID, DFC_EMITTER_RANDOM_COLOR, DFC_EMITTER_RANDOM_COLOR,
                    (int) (worldTime % 1000L), range * 2, 0.125F, 4, 0.0625F));
            beams.add(translatedDepthBeam("emitter_random_b", 0.0D, DFC_BEAM_TRANSLATE_Y, 0.0D,
                    0.0D, 0.0D, range, LegacyBeamRenderer.WaveType.RANDOM,
                    LegacyBeamRenderer.BeamType.SOLID, DFC_EMITTER_RANDOM_COLOR, DFC_EMITTER_RANDOM_COLOR,
                    (int) (worldTime % 1000L) + 1, range * 2, 0.125F, 4, 0.0625F));
        }
        return new DfcBeamPlan("emitter", range, false, false, List.copyOf(beams));
    }

    public static DfcBeamPlan dfcInjectorBeamPlan(int range, int tank0Fill, int tank0Color,
            int tank1Fill, int tank1Color, long worldTime) {
        List<TranslatedBeamPlan> beams = new ArrayList<>();
        if (range > 0 && tank0Fill > 0) {
            beams.add(translatedBeam("injector_tank_0", 0.0D, DFC_BEAM_TRANSLATE_Y, 0.0D,
                    0.0D, 0.0D, range, LegacyBeamRenderer.WaveType.RANDOM,
                    LegacyBeamRenderer.BeamType.LINE, tank0Color, DFC_INJECTOR_INNER_COLOR,
                    (int) (worldTime % 1000L), range, 0.0625F, 0, 0.0F));
        }
        if (range > 0 && tank1Fill > 0) {
            beams.add(translatedBeam("injector_tank_1", 0.0D, DFC_BEAM_TRANSLATE_Y, 0.0D,
                    0.0D, 0.0D, range, LegacyBeamRenderer.WaveType.RANDOM,
                    LegacyBeamRenderer.BeamType.LINE, tank1Color, DFC_INJECTOR_INNER_COLOR,
                    (int) (worldTime % 1000L) + 1, range, 0.0625F, 0, 0.0F));
        }
        return new DfcBeamPlan("injector", range, false, false, List.copyOf(beams));
    }

    public static DfcBeamPlan dfcStabilizerBeamPlan(int range, long worldTime) {
        List<TranslatedBeamPlan> beams = new ArrayList<>();
        if (range > 0) {
            beams.add(translatedBeam("stabilizer_fast", 0.0D, DFC_BEAM_TRANSLATE_Y, 0.0D,
                    0.0D, 0.0D, range, LegacyBeamRenderer.WaveType.SPIRAL,
                    LegacyBeamRenderer.BeamType.LINE, DFC_STABILIZER_OUTER_COLOR, DFC_STABILIZER_INNER_COLOR,
                    (int) (worldTime * -25L % 360L), range * 3, 0.125F, 0, 0.0F));
            beams.add(translatedBeam("stabilizer_mid", 0.0D, DFC_BEAM_TRANSLATE_Y, 0.0D,
                    0.0D, 0.0D, range, LegacyBeamRenderer.WaveType.SPIRAL,
                    LegacyBeamRenderer.BeamType.LINE, DFC_STABILIZER_OUTER_COLOR, DFC_STABILIZER_INNER_COLOR,
                    (int) (worldTime * -15L % 360L) + 180, range * 3, 0.125F, 0, 0.0F));
            beams.add(translatedBeam("stabilizer_slow", 0.0D, DFC_BEAM_TRANSLATE_Y, 0.0D,
                    0.0D, 0.0D, range, LegacyBeamRenderer.WaveType.SPIRAL,
                    LegacyBeamRenderer.BeamType.LINE, DFC_STABILIZER_OUTER_COLOR, DFC_STABILIZER_INNER_COLOR,
                    (int) (worldTime * -5L % 360L) + 180, range * 3, 0.125F, 0, 0.0F));
        }
        return new DfcBeamPlan("stabilizer", range, false, false, List.copyOf(beams));
    }

    public static RotatingModelPartPlan annihilatorRollerPlan(long currentMillis) {
        return new RotatingModelPartPlan("annihilator_roller", "Roller",
                0.0D, ANNIHILATOR_ROLLER_PIVOT_Y, 0.0D,
                0.0F, 0.0F, -1.0F,
                (currentMillis * ANNIHILATOR_ROLLER_ROTATION_SPEED) % 360.0D);
    }

    public static TextureMatrixPartPlan annihilatorBeltPlan(long currentMillis) {
        return new TextureMatrixPartPlan("annihilator_belt", "Belt", rgba(0xFFFFFF, 1.0F),
                new LegacyUvAnimation.TextureMatrixPlan(
                        LegacyUvAnimation.TextureMatrixOrder.SCALE_ROTATE_TRANSLATE,
                        1.0D, 1.0D, 0.0D,
                        LegacyUvAnimation.annihilatorBeltU(currentMillis), 0.0D));
    }

    public static ConveyorPressPlan conveyorPressPlan(boolean hasStack, double lastPress,
            double renderPress, float partialTicks, long worldTime) {
        return new ConveyorPressPlan(conveyorPressPistonPlan(hasStack, lastPress, renderPress, partialTicks),
                conveyorPressBeltPlan(worldTime));
    }

    public static TranslatedModelPartPlan conveyorPressPistonPlan(boolean hasStack, double lastPress,
            double renderPress, float partialTicks) {
        double piston = lastPress + (renderPress - lastPress) * partialTicks;
        return new TranslatedModelPartPlan("conveyor_press_piston", "Piston", hasStack,
                0.0D, -piston * CONVEYOR_PRESS_PISTON_TRAVEL, 0.0D);
    }

    public static TextureMatrixPartPlan conveyorPressBeltPlan(long worldTime) {
        int ticks = (int) (worldTime % CONVEYOR_PRESS_BELT_PERIOD_TICKS) + CONVEYOR_PRESS_BELT_TICK_OFFSET;
        return new TextureMatrixPartPlan("conveyor_press_belt", "Belt", rgba(0xFFFFFF, 1.0F),
                new LegacyUvAnimation.TextureMatrixPlan(
                        LegacyUvAnimation.TextureMatrixOrder.SCALE_ROTATE_TRANSLATE,
                        1.0D, 1.0D, 0.0D, 0.0D,
                        (double) ticks / CONVEYOR_PRESS_BELT_PERIOD_TICKS));
    }

    public static ArcFurnacePlan arcFurnacePlan(double prevLid, double lid, boolean progressing,
            long worldTime, float partialTicks, int liquidAmount, int maxLiquid, boolean hasMaterial,
            List<ArcElectrodeState> electrodes) {
        double time = LegacyUvAnimation.tickTime(worldTime, partialTicks);
        double lift = prevLid + (lid - prevLid) * partialTicks;
        TranslatedModelPartPlan contentsHot = null;
        TranslatedModelPartPlan contentsCold = null;
        if (liquidAmount > 0 && maxLiquid > 0) {
            contentsHot = new TranslatedModelPartPlan("arc_furnace_contents_hot", "ContentsHot", true,
                    0.0D, ARC_FURNACE_CONTENT_BASE_Y
                            + liquidAmount * ARC_FURNACE_CONTENT_TRAVEL / (double) maxLiquid,
                    0.0D);
        } else if (hasMaterial) {
            contentsCold = new TranslatedModelPartPlan("arc_furnace_contents_cold", "ContentsCold",
                    true, 0.0D, 0.0D, 0.0D);
        }
        TranslatedModelPartPlan lidPlan = new TranslatedModelPartPlan("arc_furnace_lid", "Lid", true,
                0.0D, ARC_FURNACE_LID_TRAVEL * lift,
                progressing ? Math.sin(time) * ARC_FURNACE_LID_WOBBLE : 0.0D);
        List<ArcElectrodePlan> electrodePlans = new ArrayList<>();
        int count = electrodes == null ? 0 : Math.min(3, electrodes.size());
        for (int i = 0; i < count; i++) {
            ArcElectrodeState state = electrodes.get(i) == null ? ArcElectrodeState.NONE : electrodes.get(i);
            electrodePlans.add(arcElectrodePlan(i, state));
        }
        for (int i = count; i < 3; i++) {
            electrodePlans.add(arcElectrodePlan(i, ArcElectrodeState.NONE));
        }
        double cableAngle = progressing ? Math.sin(time / 2.0D) * ARC_FURNACE_CABLE_WOBBLE_DEGREES : 0.0D;
        return new ArcFurnacePlan(lift, progressing, fullbrightStatePlan(),
                contentsHot, contentsCold, lidPlan, List.copyOf(electrodePlans),
                arcCablePlans(electrodePlans, cableAngle));
    }

    public static ArcFurnacePlan arcFurnaceStaticPreviewPlan() {
        return arcFurnacePlan(0.0D, 0.0D, false, 0L, 0.0F, 0, 0, false,
                List.of(ArcElectrodeState.FRESH, ArcElectrodeState.FRESH, ArcElectrodeState.FRESH));
    }

    public static SteamEnginePlan steamEnginePlan(double lastRotor, double rotor, float partialTicks) {
        return steamEnginePlan(lastRotor + (rotor - lastRotor) * partialTicks);
    }

    public static SteamEnginePlan steamEngineItemPlan(boolean cog, long currentMillis) {
        return steamEnginePlan(cog ? currentMillis % 3600L * 0.1D : 0.0D);
    }

    public static SteamEnginePlan steamEnginePlan(double rotorDegrees) {
        double radians = Math.toRadians(rotorDegrees);
        double sin = Math.sin(radians) * STEAM_ENGINE_CRANK_RADIUS + STEAM_ENGINE_CRANK_SIN_OFFSET;
        double cos = Math.cos(radians) * STEAM_ENGINE_CRANK_RADIUS;
        double transmissionAngle = Math.toDegrees(Math.acos(cos / STEAM_ENGINE_ROD_LENGTH)) - 90.0D;
        double cath = Math.sqrt(STEAM_ENGINE_PISTON_CATH_SQUARED - (cos * cos) / 2.0D);
        return new SteamEnginePlan(rotorDegrees, STEAM_ENGINE_MODEL_TRANSLATE_X,
                new RotatingModelPartPlan("steam_engine_flywheel", "Flywheel",
                        STEAM_ENGINE_FLYWHEEL_PIVOT_X, STEAM_ENGINE_FLYWHEEL_PIVOT_Y, 0.0D,
                        0.0F, 0.0F, -1.0F, rotorDegrees),
                new RotatingModelPartPlan("steam_engine_shaft", "Shaft",
                        0.0D, STEAM_ENGINE_SHAFT_PIVOT_Y, STEAM_ENGINE_SHAFT_PIVOT_Z,
                        1.0F, 0.0F, 0.0F, rotorDegrees * 2.0D),
                new SteamEngineTransmissionPlan(sin, cos, transmissionAngle,
                        STEAM_ENGINE_TRANSMISSION_PIVOT_X, STEAM_ENGINE_TRANSMISSION_PIVOT_Y),
                new TranslatedModelPartPlan("steam_engine_piston", "Piston", true,
                        STEAM_ENGINE_ROD_LENGTH - cath + sin, 0.0D, 0.0D));
    }

    public static Bat9000FluidPlan bat9000FluidPlan(int fill, int maxFill, int fluidColor) {
        double height = maxFill <= 0 ? 0.0D : Math.max(0, fill) * BAT9000_FLUID_HEIGHT / (double) maxFill;
        boolean active = fill > 0 && maxFill > 0;
        return new Bat9000FluidPlan(active, height, rgba(fluidColor, 1.0F),
                false, false, false, bat9000DangerDiamondPlan(active),
                active ? bat9000FluidQuads(height, rgba(fluidColor, 1.0F)) : List.of());
    }

    public static WandStructureBoundsPlan wandStructureBoundsPlan(int sizeX, int sizeY, int sizeZ) {
        double x1 = 0.0D;
        double y1 = WAND_STRUCTURE_Y_OFFSET;
        double z1 = 0.0D;
        double x2 = sizeX;
        double y2 = sizeY + WAND_STRUCTURE_Y_OFFSET;
        double z2 = sizeZ;
        List<UntexturedLinePlan> lines = List.of(
                line("top_west", x1, y2, z1, x1, y2, z2),
                line("top_south", x1, y2, z2, x2, y2, z2),
                line("top_east", x2, y2, z2, x2, y2, z1),
                line("top_north", x2, y2, z1, x1, y2, z1),
                line("bottom_west", x1, y1, z1, x1, y1, z2),
                line("bottom_south", x1, y1, z2, x2, y1, z2),
                line("bottom_east", x2, y1, z2, x2, y1, z1),
                line("bottom_north", x2, y1, z1, x1, y1, z1),
                line("side_nw", x1, y1, z1, x1, y2, z1),
                line("side_ne", x2, y1, z1, x2, y2, z1),
                line("side_se", x2, y1, z2, x2, y2, z2),
                line("side_sw", x1, y1, z2, x1, y2, z2));
        return new WandStructureBoundsPlan(sizeX, sizeY, sizeZ, false, false,
                WAND_STRUCTURE_BRIGHTNESS, rgba(1.0F, 1.0F, 1.0F, 1.0F), lines);
    }

    public static AutosawPlan autosawPlan(double prevYaw, double yaw, double prevPitch,
            double pitch, double lastSpin, double spin, boolean on, long worldTime, float partialTicks) {
        double turn = prevYaw + (yaw - prevYaw) * partialTicks;
        double angle = AUTOSAW_DEFAULT_ANGLE - (prevPitch + (pitch - prevPitch) * partialTicks);
        double bladeSpin = lastSpin + (spin - lastSpin) * partialTicks;
        double engine = on ? Math.sin((worldTime * 2.0D) % (Math.PI * 2.0D) + partialTicks) : 0.0D;
        return autosawPlan(turn, angle, bladeSpin, engine);
    }

    public static AutosawPlan autosawItemPlan(long currentMillis) {
        return autosawPlan(0.0D, AUTOSAW_DEFAULT_ANGLE, currentMillis % 3600L * 0.1D, 0.0D);
    }

    public static AutosawPlan autosawPlan(double turn, double angle, double spin, double engine) {
        return new AutosawPlan(turn, engine * AUTOSAW_ENGINE_BOB,
                pivotedPart("autosaw_arm_upper", "ArmUpper", 0.0D, AUTOSAW_ARM_PIVOT_Y, 0.0D,
                        1.0F, 0.0F, 0.0F, angle, 0.0D, 0.0D, 0.0D),
                pivotedPart("autosaw_arm_lower", "ArmLower", 0.0D, AUTOSAW_ARM_PIVOT_Y, AUTOSAW_ARM_LOWER_Z,
                        1.0F, 0.0F, 0.0F, angle * -2.0D, AUTOSAW_ARM_LOWER_NUDGE_X, 0.0D, 0.0D),
                pivotedPart("autosaw_arm_tip", "ArmTip", 0.0D, AUTOSAW_ARM_PIVOT_Y, AUTOSAW_ARM_TIP_Z,
                        1.0F, 0.0F, 0.0F, angle, 0.0D, 0.0D, 0.0D),
                pivotedPart("autosaw_blade", "Sawblade", 0.0D, AUTOSAW_ARM_PIVOT_Y, AUTOSAW_BLADE_Z,
                        0.0F, -1.0F, 0.0F, spin, 0.0D, 0.0D, 0.0D));
    }

    public static ThresherPlan thresherPlan(double prevAngle, double angle, double lastSpin,
            double spin, boolean on, long worldTime, float partialTicks) {
        double armAngle = THRESHER_DEFAULT_ANGLE - (prevAngle + (angle - prevAngle) * partialTicks);
        double wheelSpin = lastSpin + (spin - lastSpin) * partialTicks;
        double engine = on ? Math.sin((worldTime * 2.0D) % (Math.PI * 2.0D) + partialTicks) : 0.0D;
        return thresherPlan(armAngle, wheelSpin, engine);
    }

    public static ThresherPlan thresherItemPlan(long currentMillis) {
        return thresherPlan(THRESHER_ITEM_ANGLE, currentMillis % 3600L * 0.25D, 0.0D);
    }

    public static ThresherPlan thresherPlan(double angle, double spin, double engine) {
        return new ThresherPlan(angle, spin, engine * THRESHER_ENGINE_BOB,
                pivotedPart("thresher_arm_upper", "ArmUpper", 0.0D, THRESHER_ARM_PIVOT_Y, THRESHER_ARM_UPPER_Z,
                        1.0F, 0.0F, 0.0F, angle, 0.0D, 0.0D, 0.0D),
                pivotedPart("thresher_arm_lower", "ArmLower", 0.0D, THRESHER_ARM_PIVOT_Y, THRESHER_ARM_LOWER_Z,
                        1.0F, 0.0F, 0.0F, angle * -2.0D, THRESHER_ARM_LOWER_NUDGE_X, 0.0D, 0.0D),
                pivotedPart("thresher_front", "Front", 0.0D, THRESHER_ARM_PIVOT_Y, THRESHER_FRONT_Z,
                        1.0F, 0.0F, 0.0F, angle, THRESHER_FRONT_NUDGE_X, 0.0D, 0.0D),
                pivotedPart("thresher_wheel", "Wheel", 0.0D, THRESHER_ARM_PIVOT_Y, THRESHER_WHEEL_Z,
                        1.0F, 0.0F, 0.0F, -spin, 0.0D, 0.0D, 0.0D));
    }

    public static TurbofanPlan turbofanPlan(double lastSpin, double spin, float partialTicks, int afterburner) {
        double bladeSpin = lastSpin + (spin - lastSpin) * partialTicks;
        return new TurbofanPlan(bladeSpin, afterburner != 0,
                afterburner == 0 ? "turbofan_back" : "turbofan_afterburner",
                new RotatingModelPartPlan("turbofan_blades", "Blades",
                        0.0D, TURBOFAN_BLADE_PIVOT_Y, 0.0D,
                        0.0F, 0.0F, -1.0F, bladeSpin));
    }

    public static IndustrialTurbinePlan industrialTurbinePlan(double gaugeDegrees,
            double lastRotor, double rotor, float partialTicks) {
        return industrialTurbinePlan(gaugeDegrees, lastRotor + (rotor - lastRotor) * partialTicks);
    }

    public static IndustrialTurbinePlan industrialTurbineItemPlan(long currentMillis) {
        return industrialTurbinePlan(INDUSTRIAL_TURBINE_GAUGE_STEAM,
                currentMillis / INDUSTRIAL_TURBINE_ITEM_FLYWHEEL_PERIOD
                        % INDUSTRIAL_TURBINE_ITEM_FLYWHEEL_MODULO);
    }

    public static IndustrialTurbinePlan industrialTurbinePlan(double gaugeDegrees, double flywheelDegrees) {
        return new IndustrialTurbinePlan(gaugeDegrees, flywheelDegrees,
                new RotatingModelPartPlan("industrial_turbine_gauge", "Gauge",
                        0.0D, INDUSTRIAL_TURBINE_PIVOT_Y, 0.0D,
                        0.0F, 0.0F, 1.0F, gaugeDegrees),
                new RotatingModelPartPlan("industrial_turbine_flywheel", "Flywheel",
                        0.0D, INDUSTRIAL_TURBINE_PIVOT_Y, 0.0D,
                        0.0F, 0.0F, -1.0F, flywheelDegrees));
    }

    public static BigTurbinePlan bigTurbinePlan(double lastRotor, double rotor, float partialTicks) {
        double spin = lastRotor + (rotor - lastRotor) * partialTicks;
        return new BigTurbinePlan(false, BIG_TURBINE_BASE_ROTATION_Y, BIG_TURBINE_TRANSLATE_Z,
                "universal_bright", new RotatingModelPartPlan("big_turbine_blades", "Blades",
                        0.0D, BIG_TURBINE_BLADE_PIVOT_Y, 0.0D,
                        0.0F, 0.0F, 1.0F, spin));
    }

    public static BoilerPlan boilerPlan(boolean hasExploded, int steamFill, int steamMaxFill,
            long currentMillis) {
        boolean overpressure = !hasExploded && steamMaxFill > 0
                && steamFill > steamMaxFill * BOILER_OVERPRESSURE_THRESHOLD;
        double sine = overpressure
                ? Math.sin((currentMillis / BOILER_OVERPRESSURE_PERIOD_DIVISOR) % (Math.PI * 2.0D))
                        * BOILER_OVERPRESSURE_SCALE
                : 0.0D;
        return new BoilerPlan(hasExploded, overpressure,
                1.0D - sine, 1.0D + sine, 1.0D - sine,
                hasExploded ? "boiler_burst" : "boiler", hasExploded ? false : true);
    }

    public static CraneConsolePlan craneConsolePlan(double lastTiltFront, double tiltFront,
            double lastTiltLeft, double tiltLeft, double loadedHeat, double loadedEnrichment,
            boolean craneLoading, boolean itemLoaded, boolean validTarget, CraneSetupInputPlan craneSetup,
            long currentMillis, float partialTicks) {
        double joystickFront = lastTiltFront + (tiltFront - lastTiltFront) * partialTicks;
        double joystickLeft = lastTiltLeft + (tiltLeft - lastTiltLeft) * partialTicks;
        double wobble = Math.sin((currentMillis * CRANE_METER_WOBBLE_SPEED) % 360.0D)
                * 180.0D / Math.PI * CRANE_METER_WOBBLE_SCALE;
        CraneRigPlan rig = craneSetup == null || !craneSetup.setUpCrane()
                ? null
                : craneRigPlan(craneSetup, partialTicks);
        return new CraneConsolePlan(CRANE_CONSOLE_TRANSLATE_X,
                new CraneJoystickPlan(CRANE_JOYSTICK_PIVOT_X, CRANE_JOYSTICK_PIVOT_Y,
                        CRANE_JOYSTICK_RESTORE_Y, joystickFront, joystickLeft),
                new CraneMeterPlan("Meter1", CRANE_METER_1_Z,
                        wobble + CRANE_METER_BASE_DEGREES - CRANE_METER_RANGE_DEGREES * loadedHeat),
                new CraneMeterPlan("Meter2", CRANE_METER_2_Z,
                        wobble + CRANE_METER_BASE_DEGREES - CRANE_METER_RANGE_DEGREES * loadedEnrichment),
                new ModelPartTintPlan("crane_lamp_loading", "Lamp1", true,
                        rgba(craneLoading ? CRANE_LAMP_LOADING_COLOR
                                : itemLoaded ? CRANE_LAMP_LOADED_COLOR : CRANE_LAMP_UNLOADED_COLOR, 1.0F),
                        lightmapOnlyFullbrightPlan(), null, false),
                new ModelPartTintPlan("crane_lamp_target", "Lamp2", true,
                        rgba(validTarget ? CRANE_LAMP_TARGET_VALID_COLOR : CRANE_LAMP_TARGET_INVALID_COLOR, 1.0F),
                        lightmapOnlyFullbrightPlan(), null, false),
                rig);
    }

    public static PylonWireLinePlan pylonWireLinePlan(double x0, double y0, double z0,
            double x1, double y1, double z1, boolean hang, int color) {
        int wireColor = color == 0 ? 0xFFFFFF : color;
        String textureRole = color == 0 ? "wire" : "wire_greyscale";
        LegacyTexturedLineRenderer.WireOffsets offsets = LegacyTexturedLineRenderer.pylonWireOffsets(
                x0, y0, z0, x1, y1, z1, LegacyTexturedLineRenderer.PYLON_WIRE_GIRTH);
        List<LegacyTexturedLineRenderer.WireSubSegment> sourceSegments = hang
                ? LegacyTexturedLineRenderer.saggedPylonSegments(x0, y0, z0, x1, y1, z1,
                        LegacyTexturedLineRenderer.PYLON_HANG_SEGMENTS)
                : List.of(new LegacyTexturedLineRenderer.WireSubSegment(
                        x0, y0, z0, x1, y1, z1,
                        (x0 + x1) * 0.5D, (y0 + y1) * 0.5D, (z0 + z1) * 0.5D));
        List<PylonWireSegmentPlan> segments = new ArrayList<>();
        for (LegacyTexturedLineRenderer.WireSubSegment segment : sourceSegments) {
            LegacyTexturedLineRenderer.WireWrap wrap = LegacyTexturedLineRenderer.wireWrap(
                    segment.x0(), segment.y0(), segment.z0(), segment.x1(), segment.y1(), segment.z1(),
                    LegacyTexturedLineRenderer.PYLON_WIRE_U_WRAP_PER_BLOCK);
            segments.add(new PylonWireSegmentPlan(segment, wrap));
        }
        return new PylonWireLinePlan(textureRole, wireColor, hang, false, false,
                LegacyTexturedLineRenderer.PYLON_WIRE_GIRTH,
                LegacyTexturedLineRenderer.PYLON_WIRE_U_WRAP_PER_BLOCK,
                LegacyTexturedLineRenderer.PYLON_HANG_SEGMENTS,
                LegacyTexturedLineRenderer.PYLON_MAX_HANG,
                LegacyTexturedLineRenderer.PYLON_HANG_DIVISOR,
                offsets, List.copyOf(segments));
    }

    public static RbmkColumnGridPlan rbmkDisplayColumnGridPlan(List<RbmkColumnInputPlan> columns) {
        return rbmkColumnGridPlan("rbmk_display", columns, 0.0D, RBMK_DISPLAY_TRANSLATE_Y, 0.0D,
                1.0D, RBMK_DISPLAY_SCALE_YZ, RBMK_DISPLAY_SCALE_YZ,
                RBMK_DISPLAY_COLUMN_X, RBMK_DISPLAY_COLUMNS_PER_ROW, RBMK_DISPLAY_Z_CENTER,
                RBMK_DISPLAY_Y_START);
    }

    public static RbmkColumnGridPlan rbmkConsoleColumnGridPlan(List<RbmkColumnInputPlan> columns) {
        return rbmkColumnGridPlan("rbmk_console", columns, RBMK_CONSOLE_MODEL_TRANSLATE_X, 0.0D, 0.0D,
                1.0D, 1.0D, 1.0D,
                RBMK_CONSOLE_COLUMN_X, RBMK_CONSOLE_COLUMNS_PER_ROW, RBMK_CONSOLE_Z_CENTER,
                RBMK_CONSOLE_Y_START);
    }

    private static TankDangerDiamondPlan radialDangerDiamondPlan(boolean hasFluid, int count,
            float initialYaw, float yawStep, double translateX, double translateY, double translateZ,
            float scaleX, float scaleY, float scaleZ) {
        List<DiamondTransformPlan> transforms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            transforms.add(new DiamondTransformPlan("radial_" + i, translateX, translateY, translateZ,
                    initialYaw + yawStep * i, scaleX, scaleY, scaleZ));
        }
        return new TankDangerDiamondPlan(hasFluid, List.copyOf(transforms));
    }

    private static ArcElectrodePlan arcElectrodePlan(int index, ArcElectrodeState state) {
        int number = index + 1;
        return new ArcElectrodePlan(index, state,
                state != ArcElectrodeState.NONE ? "Ring" + number : null,
                state == ArcElectrodeState.FRESH ? "Electrode" + number : null,
                state == ArcElectrodeState.USED ? "Electrode" + number + "Hot" : null,
                state == ArcElectrodeState.DEPLETED ? "Electrode" + number + "Short" : null);
    }

    private static List<RotatingModelPartPlan> arcCablePlans(List<ArcElectrodePlan> electrodes, double angle) {
        List<RotatingModelPartPlan> cables = new ArrayList<>();
        double[] pivotZ = {0.5D, 0.0D, -0.5D};
        for (int i = 0; i < Math.min(3, electrodes.size()); i++) {
            if (electrodes.get(i).state() != ArcElectrodeState.NONE) {
                cables.add(new RotatingModelPartPlan("arc_furnace_cable_" + (i + 1), "Cable" + (i + 1),
                        0.0D, ARC_FURNACE_CABLE_PIVOT_Y, pivotZ[i],
                        1.0F, 0.0F, 0.0F, angle));
            }
        }
        return List.copyOf(cables);
    }

    private static PivotedModelPartPlan pivotedPart(String role, String partName,
            double pivotX, double pivotY, double pivotZ, float axisX, float axisY, float axisZ,
            double angleDegrees, double translateX, double translateY, double translateZ) {
        return new PivotedModelPartPlan(role, partName, pivotX, pivotY, pivotZ,
                axisX, axisY, axisZ, angleDegrees, translateX, translateY, translateZ);
    }

    private static UntexturedQuadPlan pumpjackFrontRodQuad(String role, int side, Vec3 start, Vec3 end) {
        RgbaPlan color = rgba(PUMPJACK_FRONT_ROD_COLOR, 1.0F);
        return new UntexturedQuadPlan(role, List.of(
                vertex((PUMPJACK_FRONT_ROD_WIDTH - PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                        PUMPJACK_HEAD_PIVOT_Y + start.y, PUMPJACK_HEAD_PIVOT_Z + start.z, color),
                vertex((PUMPJACK_FRONT_ROD_WIDTH + PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                        PUMPJACK_HEAD_PIVOT_Y + start.y, PUMPJACK_HEAD_PIVOT_Z + start.z, color),
                vertex((PUMPJACK_FRONT_ROD_WIDTH + PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                        PUMPJACK_HEAD_PIVOT_Y + end.y, PUMPJACK_HEAD_PIVOT_Z + end.z, color),
                vertex((PUMPJACK_FRONT_ROD_WIDTH - PUMPJACK_FRONT_ROD_HALF_THICKNESS) * side,
                        PUMPJACK_HEAD_PIVOT_Y + end.y, PUMPJACK_HEAD_PIVOT_Z + end.z, color)));
    }

    private static Vec3 fixedPumpjackFrontRodPoint(Vec3 frontPos, Vec3 frontRad, double dist) {
        double y = frontPos.y + frontRad.y;
        double z = frontPos.z + frontRad.z;
        if (frontRad.y < 0.0D) {
            z = -PUMPJACK_HEAD_PIVOT_Z + dist * 0.5D;
        }
        return new Vec3(0.0D, y, z);
    }

    private static Vec3 rotateX(Vec3 vec, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(vec.x, vec.y * cos + vec.z * sin, vec.z * cos - vec.y * sin);
    }

    private static CraneRigPlan craneRigPlan(CraneSetupInputPlan input, float partialTicks) {
        double posFront = input.lastPosFront() + (input.posFront() - input.lastPosFront()) * partialTicks;
        double posLeft = input.lastPosLeft() + (input.posLeft() - input.lastPosLeft()) * partialTicks;
        double progress = input.lastProgress() + (input.progress() - input.lastProgress()) * partialTicks;
        int rotation = input.craneRotationOffset();
        int girderSpan = 0;
        double girderTranslateX = 0.0D;
        double girderTranslateZ = 0.0D;
        switch (rotation) {
            case 0 -> {
                girderSpan = input.spanFront() + input.spanBack() + 1;
                girderTranslateX = posFront + input.spanBack();
            }
            case 90 -> {
                girderSpan = input.spanLeft() + input.spanRight() + 1;
                girderTranslateZ = -posLeft - input.spanRight();
            }
            case 180 -> {
                girderSpan = input.spanFront() + input.spanBack() + 1;
                girderTranslateX = posFront - input.spanFront();
            }
            case 270 -> {
                girderSpan = input.spanLeft() + input.spanRight() + 1;
                girderTranslateZ = -posLeft + input.spanLeft();
            }
            default -> {
            }
        }
        return new CraneRigPlan(true, input.centerOffsetX(), input.centerOffsetY() + 1.0D,
                input.centerOffsetZ(), -posFront, 0.0D, posLeft, rotation,
                Math.max(0, input.height() - 6), Math.max(0, girderSpan),
                girderTranslateX, 0.0D, girderTranslateZ,
                -CRANE_LIFT_TRAVEL * (1.0D - progress));
    }

    private static List<UntexturedQuadPlan> bat9000FluidQuads(double height, RgbaPlan color) {
        double off = BAT9000_FLUID_OFFSET;
        double base = BAT9000_FLUID_BASE_Y;
        double top = base + height;
        double half = BAT9000_FLUID_HALF_WIDTH;
        return List.of(
                new UntexturedQuadPlan("bat9000_fluid_west", List.of(
                        vertex(-off, base, -half, color),
                        vertex(-off, top, -half, color),
                        vertex(-off, top, half, color),
                        vertex(-off, base, half, color))),
                new UntexturedQuadPlan("bat9000_fluid_east", List.of(
                        vertex(off, base, -half, color),
                        vertex(off, top, -half, color),
                        vertex(off, top, half, color),
                        vertex(off, base, half, color))),
                new UntexturedQuadPlan("bat9000_fluid_north", List.of(
                        vertex(-half, base, -off, color),
                        vertex(-half, top, -off, color),
                        vertex(half, top, -off, color),
                        vertex(half, base, -off, color))),
                new UntexturedQuadPlan("bat9000_fluid_south", List.of(
                        vertex(-half, base, off, color),
                        vertex(-half, top, off, color),
                        vertex(half, top, off, color),
                        vertex(half, base, off, color))));
    }

    private static UntexturedLinePlan line(String role, double x0, double y0, double z0,
            double x1, double y1, double z1) {
        return new UntexturedLinePlan(role, x0, y0, z0, x1, y1, z1);
    }

    private static AssemblySparkBladePlan assemblySparkBlade(String role, double translateX,
            double translateZ, double length, LegacyUvAnimation.Range u) {
        return new AssemblySparkBladePlan(role, translateX, 1.0625D, translateZ, length,
                List.of(
                        assemblySparkQuad("mirrored_left", -ASSEMBLY_SPARK_EPSILON, length,
                                u.min() + ASSEMBLY_SPARK_MIRROR_U_OFFSET, u.max() + ASSEMBLY_SPARK_MIRROR_U_OFFSET),
                        assemblySparkQuad("right", ASSEMBLY_SPARK_EPSILON, length, u.min(), u.max())));
    }

    private static TexturedQuadPlan assemblySparkQuad(String role, double x, double length,
            double uMin, double uMax) {
        boolean right = role.equals("right");
        return new TexturedQuadPlan(role, List.of(
                vertex(x, -ASSEMBLY_SPARK_WIDE, length, uMin, right ? 1.0D : 0.0D, 0),
                vertex(x, ASSEMBLY_SPARK_WIDE, length, uMin, right ? 0.0D : 1.0D, 0),
                vertex(x, ASSEMBLY_SPARK_NARROW, 0.0D, uMax, right ? 0.0D : 1.0D, 255),
                vertex(x, -ASSEMBLY_SPARK_NARROW, 0.0D, uMax, right ? 1.0D : 0.0D, 255)));
    }

    private static QuadVertexPlan vertex(double x, double y, double z, double u, double v, int alpha) {
        return new QuadVertexPlan(x, y, z, u, v, 0xFFFFFF, Math.max(0, Math.min(255, alpha)));
    }

    private static UntexturedVertexPlan vertex(double x, double y, double z, RgbaPlan color) {
        return new UntexturedVertexPlan(x, y, z, color);
    }

    private static List<UntexturedQuadPlan> glowBoxQuads(String role, double minX, double minY,
            double minZ, double maxX, double maxY, double maxZ, RgbaPlan color) {
        return List.of(
                new UntexturedQuadPlan(role + "_pos_x", List.of(
                        vertex(maxX, minY, minZ, color), vertex(maxX, maxY, minZ, color),
                        vertex(maxX, maxY, maxZ, color), vertex(maxX, minY, maxZ, color))),
                new UntexturedQuadPlan(role + "_neg_x", List.of(
                        vertex(minX, minY, minZ, color), vertex(minX, maxY, minZ, color),
                        vertex(minX, maxY, maxZ, color), vertex(minX, minY, maxZ, color))),
                new UntexturedQuadPlan(role + "_pos_z", List.of(
                        vertex(minX, minY, maxZ, color), vertex(minX, maxY, maxZ, color),
                        vertex(maxX, maxY, maxZ, color), vertex(maxX, minY, maxZ, color))),
                new UntexturedQuadPlan(role + "_neg_z", List.of(
                        vertex(minX, minY, minZ, color), vertex(minX, maxY, minZ, color),
                        vertex(maxX, maxY, minZ, color), vertex(maxX, minY, minZ, color))),
                new UntexturedQuadPlan(role + "_top", List.of(
                        vertex(minX, maxY, minZ, color), vertex(minX, maxY, maxZ, color),
                        vertex(maxX, maxY, maxZ, color), vertex(maxX, maxY, minZ, color))),
                new UntexturedQuadPlan(role + "_bottom", List.of(
                        vertex(minX, minY, minZ, color), vertex(minX, minY, maxZ, color),
                        vertex(maxX, minY, maxZ, color), vertex(maxX, minY, minZ, color))));
    }

    private static List<UntexturedQuadPlan> solarBeamQuads(int index, double distance) {
        RgbaPlan near = rgba(1.0F, 1.0F, 1.0F, SOLAR_BOILER_BEAM_MAX_ALPHA);
        RgbaPlan far = rgba(1.0F, 1.0F, 1.0F, SOLAR_BOILER_BEAM_MIN_ALPHA);
        double w = SOLAR_BOILER_BEAM_HALF_WIDTH;
        double y0 = SOLAR_BOILER_BEAM_START_Y;
        return List.of(
                new UntexturedQuadPlan("solar_beam_" + index + "_pos_x", List.of(
                        vertex(w, y0, w, near), vertex(w, y0, -w, near),
                        vertex(w, distance, -w, far), vertex(w, distance, w, far))),
                new UntexturedQuadPlan("solar_beam_" + index + "_neg_x", List.of(
                        vertex(-w, y0, w, near), vertex(-w, y0, -w, near),
                        vertex(-w, distance, -w, far), vertex(-w, distance, w, far))),
                new UntexturedQuadPlan("solar_beam_" + index + "_pos_z", List.of(
                        vertex(w, y0, w, near), vertex(-w, y0, w, near),
                        vertex(-w, distance, w, far), vertex(w, distance, w, far))),
                new UntexturedQuadPlan("solar_beam_" + index + "_neg_z", List.of(
                        vertex(w, y0, -w, near), vertex(-w, y0, -w, near),
                        vertex(-w, distance, -w, far), vertex(w, distance, -w, far))));
    }

    private static UntexturedQuadPlan trailSegment(String role, double x, double startDegrees,
            double endDegrees, float startAlpha, float endAlpha) {
        RgbaPlan startColor = rgba(1.0F, 1.0F, 0.0F, startAlpha);
        RgbaPlan endColor = rgba(1.0F, 1.0F, 0.0F, endAlpha);
        TrailPoint start = trailPoint(startDegrees);
        TrailPoint end = trailPoint(endDegrees);
        return new UntexturedQuadPlan(role, List.of(
                vertex(x, start.innerY(), start.innerZ(), startColor),
                vertex(x, start.outerY(), start.outerZ(), startColor),
                vertex(x, end.outerY(), end.outerZ(), endColor),
                vertex(x, end.innerY(), end.innerZ(), endColor)));
    }

    private static TrailPoint trailPoint(double degrees) {
        double radians = Math.toRadians(degrees);
        double y = Math.cos(radians);
        double z = Math.sin(radians);
        return new TrailPoint(
                y * BATTERY_REDD_TRAIL_LENGTH - y * BATTERY_REDD_TRAIL_WIDTH,
                z * BATTERY_REDD_TRAIL_LENGTH - z * BATTERY_REDD_TRAIL_WIDTH,
                y * BATTERY_REDD_TRAIL_LENGTH + y * BATTERY_REDD_TRAIL_WIDTH,
                z * BATTERY_REDD_TRAIL_LENGTH + z * BATTERY_REDD_TRAIL_WIDTH);
    }

    private static double corePulse(double time) {
        double ix = time % (Math.PI * 2.0D);
        double t = 0.8D;
        double pulse = (1.0D / t) * Math.atan((t * Math.sin(ix)) / (1.0D - t * Math.cos(ix)));
        return (pulse + 1.0D) / 2.0D;
    }

    private static List<UntexturedTrianglePlan> coreVoidTriangles(int layer, double radius, RgbaPlan color) {
        List<UntexturedTrianglePlan> triangles = new ArrayList<>();
        for (int segment = 0; segment < CORE_VOID_SEGMENTS; segment++) {
            double startAngle = Math.PI * 2.0D / CORE_VOID_SEGMENTS * segment - 0.0025D;
            double endAngle = startAngle + Math.PI * 2.0D / CORE_VOID_SEGMENTS + 0.005D;
            triangles.add(new UntexturedTrianglePlan("core_void_" + layer + "_" + segment, List.of(
                    vertex(0.5D + Math.cos(startAngle) * radius, 1.0D, 0.5D + Math.sin(startAngle) * radius, color),
                    vertex(0.5D + Math.cos(endAngle) * radius, 1.0D, 0.5D + Math.sin(endAngle) * radius, color),
                    vertex(0.5D, 1.0D, 0.5D, color))));
        }
        return triangles;
    }

    private static TexturedQuadPlan radarBlipQuad(int index, double sX, double sZ, int blipLevel) {
        double size = RADAR_BLIP_SIZE;
        double minU = 216.0D / 256.0D;
        double maxU = 224.0D / 256.0D;
        double minV = blipLevel * 8.0D / 256.0D;
        double maxV = (blipLevel * 8.0D + 8.0D) / 256.0D;
        return new TexturedQuadPlan("radar_blip_" + index, List.of(
                vertex(RADAR_SCREEN_X, 1.0D - sZ + size, 0.5D - sX + size, minU, maxV, 255),
                vertex(RADAR_SCREEN_X, 1.0D - sZ + size, 0.5D - sX - size, maxU, maxV, 255),
                vertex(RADAR_SCREEN_X, 1.0D - sZ - size, 0.5D - sX - size, maxU, minV, 255),
                vertex(RADAR_SCREEN_X, 1.0D - sZ - size, 0.5D - sX + size, minU, minV, 255)));
    }

    private static TexturedQuadPlan radarOfflineQuad(int offset) {
        int clampedOffset = Math.max(0, Math.min(216, offset));
        double minU = 216.0D / 256.0D;
        double maxU = 256.0D / 256.0D;
        double minV = clampedOffset / 256.0D;
        double maxV = (clampedOffset + 40.0D) / 256.0D;
        return new TexturedQuadPlan("radar_offline", List.of(
                vertex(RADAR_SCREEN_X, RADAR_SCREEN_MAX_Y, RADAR_SCREEN_MAX_Z, minU, maxV, 255),
                vertex(RADAR_SCREEN_X, RADAR_SCREEN_MAX_Y, RADAR_SCREEN_MIN_Z, maxU, maxV, 255),
                vertex(RADAR_SCREEN_X, RADAR_SCREEN_MIN_Y, RADAR_SCREEN_MIN_Z, maxU, minV, 255),
                vertex(RADAR_SCREEN_X, RADAR_SCREEN_MIN_Y, RADAR_SCREEN_MAX_Z, minU, minV, 255)));
    }

    private static List<NormalTexturedQuadPlan> fallingColumnQuads(String role, double halfX, double halfZ,
            double topY, double bottomY, double uMax, double sideUMax, double dropU, double dropL) {
        double zPos = EXCAVATOR_CHUTE_CENTER_Z + halfZ;
        double zNeg = EXCAVATOR_CHUTE_CENTER_Z - halfZ;
        return List.of(
                normalQuad(role + "_pos_z", 0.0F, 0.0F, 1.0F,
                        normalVertex(halfX, topY, zPos, 0.0D, dropU),
                        normalVertex(-halfX, topY, zPos, uMax, dropU),
                        normalVertex(-halfX, bottomY, zPos, uMax, dropL),
                        normalVertex(halfX, bottomY, zPos, 0.0D, dropL)),
                normalQuad(role + "_neg_z", 0.0F, 0.0F, -1.0F,
                        normalVertex(-halfX, topY, zNeg, uMax, dropU),
                        normalVertex(halfX, topY, zNeg, 0.0D, dropU),
                        normalVertex(halfX, bottomY, zNeg, 0.0D, dropL),
                        normalVertex(-halfX, bottomY, zNeg, uMax, dropL)),
                normalQuad(role + "_neg_x", -1.0F, 0.0F, 0.0F,
                        normalVertex(-halfX, topY, zPos, 0.0D, dropU),
                        normalVertex(-halfX, topY, zNeg, sideUMax, dropU),
                        normalVertex(-halfX, bottomY, zNeg, sideUMax, dropL),
                        normalVertex(-halfX, bottomY, zPos, 0.0D, dropL)),
                normalQuad(role + "_pos_x", 1.0F, 0.0F, 0.0F,
                        normalVertex(halfX, topY, zNeg, sideUMax, dropU),
                        normalVertex(halfX, topY, zPos, 0.0D, dropU),
                        normalVertex(halfX, bottomY, zPos, 0.0D, dropL),
                        normalVertex(halfX, bottomY, zNeg, sideUMax, dropL)));
    }

    private static NormalTexturedQuadPlan normalQuad(String role, float normalX, float normalY, float normalZ,
            NormalTexturedVertexPlan v0, NormalTexturedVertexPlan v1,
            NormalTexturedVertexPlan v2, NormalTexturedVertexPlan v3) {
        return new NormalTexturedQuadPlan(role, normalX, normalY, normalZ, List.of(v0, v1, v2, v3));
    }

    private static NormalTexturedVertexPlan normalVertex(double x, double y, double z, double u, double v) {
        return new NormalTexturedVertexPlan(x, y, z, u, v);
    }

    private static List<TexturedQuadPlan> soyuzSmallBlockQuads() {
        double min = SOYUZ_SMALL_BLOCK_MIN;
        double max = SOYUZ_SMALL_BLOCK_MAX;
        return List.of(
                new TexturedQuadPlan("soyuz_small_block_north", List.of(
                        vertex(max, max, min, 1.0D, 0.0D, 255),
                        vertex(min, max, min, 0.0D, 0.0D, 255),
                        vertex(min, min, min, 0.0D, 1.0D, 255),
                        vertex(max, min, min, 1.0D, 1.0D, 255))),
                new TexturedQuadPlan("soyuz_small_block_west", List.of(
                        vertex(max, max, max, 1.0D, 0.0D, 255),
                        vertex(max, max, min, 0.0D, 0.0D, 255),
                        vertex(max, min, min, 0.0D, 1.0D, 255),
                        vertex(max, min, max, 1.0D, 1.0D, 255))),
                new TexturedQuadPlan("soyuz_small_block_south", List.of(
                        vertex(min, max, max, 1.0D, 0.0D, 255),
                        vertex(max, max, max, 0.0D, 0.0D, 255),
                        vertex(max, min, max, 0.0D, 1.0D, 255),
                        vertex(min, min, max, 1.0D, 1.0D, 255))),
                new TexturedQuadPlan("soyuz_small_block_east", List.of(
                        vertex(min, max, min, 1.0D, 0.0D, 255),
                        vertex(min, max, max, 0.0D, 0.0D, 255),
                        vertex(min, min, max, 0.0D, 1.0D, 255),
                        vertex(min, min, min, 1.0D, 1.0D, 255))),
                new TexturedQuadPlan("soyuz_small_block_bottom", List.of(
                        vertex(max, max, max, 1.0D, 0.0D, 255),
                        vertex(min, max, max, 0.0D, 0.0D, 255),
                        vertex(min, max, min, 0.0D, 1.0D, 255),
                        vertex(max, max, min, 1.0D, 1.0D, 255))),
                new TexturedQuadPlan("soyuz_small_block_top", List.of(
                        vertex(min, min, max, 1.0D, 0.0D, 255),
                        vertex(max, min, max, 0.0D, 0.0D, 255),
                        vertex(max, min, min, 0.0D, 1.0D, 255),
                        vertex(min, min, min, 1.0D, 1.0D, 255))));
    }

    private static RbmkNumitronUnitPlan rbmkNumitronUnitPlan(RbmkNumitronInputPlan input, int index) {
        String value = rbmkNumitronValue(input.value(), input.shortenNumber(), input.leadingZeroes());
        List<NumitronDigitPlan> digits = new ArrayList<>();
        for (int digit = 0; digit < RBMK_NUMITRON_DIGITS; digit++) {
            if ((input.activeDigits() & (RBMK_NUMITRON_LEFT_DIGIT_MASK >> digit)) == 0L) {
                continue;
            }
            NumitronDigitUvPlan uv = rbmkNumitronDigitUv(value.charAt(digit));
            if (uv.blank()) {
                continue;
            }
            double zOffset = (digit - 3) * RBMK_NUMITRON_DIGIT_Z_STEP;
            TexturedQuadPlan quad = new TexturedQuadPlan("rbmk_numitron_digit_" + index + "_" + digit, List.of(
                    vertex(RBMK_NUMITRON_DIGIT_X, -RBMK_NUMITRON_DIGIT_HEIGHT + RBMK_NUMITRON_DIGIT_Y,
                            RBMK_NUMITRON_DIGIT_WIDTH - zOffset, uv.u(), uv.v() + 0.5D, 255),
                    vertex(RBMK_NUMITRON_DIGIT_X, RBMK_NUMITRON_DIGIT_HEIGHT + RBMK_NUMITRON_DIGIT_Y,
                            RBMK_NUMITRON_DIGIT_WIDTH - zOffset, uv.u(), uv.v(), 255),
                    vertex(RBMK_NUMITRON_DIGIT_X, RBMK_NUMITRON_DIGIT_HEIGHT + RBMK_NUMITRON_DIGIT_Y,
                            -RBMK_NUMITRON_DIGIT_WIDTH - zOffset, uv.u() + 0.1D, uv.v(), 255),
                    vertex(RBMK_NUMITRON_DIGIT_X, -RBMK_NUMITRON_DIGIT_HEIGHT + RBMK_NUMITRON_DIGIT_Y,
                            -RBMK_NUMITRON_DIGIT_WIDTH - zOffset, uv.u() + 0.1D, uv.v() + 0.5D, 255)));
            digits.add(new NumitronDigitPlan(digit, value.charAt(digit), zOffset, uv, quad));
        }
        String label = input.label() == null ? "" : input.label();
        NumitronLabelPlan labelPlan = new NumitronLabelPlan(!label.isEmpty(), label,
                RBMK_NUMITRON_LABEL_X, RBMK_NUMITRON_LABEL_Y, 0.0D,
                RBMK_NUMITRON_LABEL_MAX_SCALE, RBMK_NUMITRON_LABEL_WIDTH_SCALE,
                0.0F, 0.0F, -1.0F, 90.0F, RBMK_NUMITRON_LABEL_COLOR, fullbrightStatePlan());
        return new RbmkNumitronUnitPlan(index, true, RBMK_NUMITRON_TRANSLATE_X,
                index * RBMK_NUMITRON_ROW_STEP + RBMK_NUMITRON_Y_START, 0.0D,
                value, input.activeDigits(), List.copyOf(digits), labelPlan);
    }

    public static String rbmkNumitronValue(long value, boolean shortenNumber, boolean leadingZeroes) {
        String formatted;
        if (shortenNumber) {
            formatted = HbmMathUtil.getShortNumber(value);
        } else if (value > 9_999_999L) {
            formatted = "9999999";
        } else if (value < -999_999L) {
            formatted = "-999999";
        } else {
            formatted = Long.toString(value);
        }
        if (formatted.isEmpty()) {
            formatted = " ";
        }
        if (formatted.length() > RBMK_NUMITRON_DIGITS) {
            formatted = formatted.substring(0, RBMK_NUMITRON_DIGITS);
        }
        if (formatted.length() < RBMK_NUMITRON_DIGITS && formatted.charAt(0) == '-' && leadingZeroes) {
            formatted = formatted.substring(1);
            while (formatted.length() < RBMK_NUMITRON_DIGITS - 1) {
                formatted = "0" + formatted;
            }
            return "-" + formatted;
        }
        String fill = leadingZeroes ? "0" : " ";
        while (formatted.length() < RBMK_NUMITRON_DIGITS) {
            formatted = fill + formatted;
        }
        return formatted;
    }

    public static NumitronDigitUvPlan rbmkNumitronDigitUv(char character) {
        return switch (character) {
            case ' ' -> new NumitronDigitUvPlan(0.0D, 0.0D, true);
            case '.' -> new NumitronDigitUvPlan(0.9D, 0.5D, false);
            case '-' -> new NumitronDigitUvPlan(0.8D, 0.5D, false);
            case 'k' -> new NumitronDigitUvPlan(0.0D, 0.5D, false);
            case 'M' -> new NumitronDigitUvPlan(0.1D, 0.5D, false);
            case 'G' -> new NumitronDigitUvPlan(0.2D, 0.5D, false);
            case 'T' -> new NumitronDigitUvPlan(0.3D, 0.5D, false);
            case 'P' -> new NumitronDigitUvPlan(0.4D, 0.5D, false);
            case 'E' -> new NumitronDigitUvPlan(0.5D, 0.5D, false);
            default -> {
                int digit = character - '0';
                if (digit >= 0 && digit <= 9) {
                    yield new NumitronDigitUvPlan(0.1D * digit, 0.0D, false);
                }
                yield new NumitronDigitUvPlan(0.8D, 0.5D, false);
            }
        };
    }

    private static NormalTexturedQuadPlan strandCasterLavaQuad(double level) {
        double y = STRAND_CASTER_LAVA_BASE_Y + level;
        return normalQuad("strand_caster_lava_surface", 0.0F, 1.0F, 0.0F,
                normalVertex(-STRAND_CASTER_LAVA_HALF_WIDTH, y, -STRAND_CASTER_LAVA_HALF_DEPTH, 0.0D, 0.0D),
                normalVertex(-STRAND_CASTER_LAVA_HALF_WIDTH, y, STRAND_CASTER_LAVA_HALF_DEPTH, 0.0D, 1.0D),
                normalVertex(STRAND_CASTER_LAVA_HALF_WIDTH, y, STRAND_CASTER_LAVA_HALF_DEPTH, 1.0D, 1.0D),
                normalVertex(STRAND_CASTER_LAVA_HALF_WIDTH, y, -STRAND_CASTER_LAVA_HALF_DEPTH, 1.0D, 0.0D));
    }

    private static RbmkColumnGridPlan rbmkColumnGridPlan(String role, List<RbmkColumnInputPlan> columns,
            double translateX, double translateY, double translateZ,
            double scaleX, double scaleY, double scaleZ,
            double columnX, int columnsPerRow, int zCenter, double yStart) {
        List<RbmkColumnPlan> plans = new ArrayList<>();
        int count = columns == null ? 0 : columns.size();
        for (int i = 0; i < count; i++) {
            RbmkColumnInputPlan column = columns.get(i);
            if (column == null || column.type() == null) {
                continue;
            }
            double y = -(i / columnsPerRow) * RBMK_COLUMN_GRID_STEP + yStart;
            double z = -(i % columnsPerRow) * RBMK_COLUMN_GRID_STEP + RBMK_COLUMN_GRID_STEP * zCenter;
            RgbaPlan baseColor = column.indicator() > 0
                    ? rgba(RBMK_COLUMN_INDICATOR_COLOR, 1.0F)
                    : rbmkColumnBaseColor(i, column);
            List<UntexturedQuadPlan> dotQuads = rbmkColumnDotQuads(role + "_dot_" + i,
                    columnX + RBMK_COLUMN_DOT_X_OFFSET, y, z, column);
            plans.add(new RbmkColumnPlan(i, columnX, y, z, baseColor,
                    rbmkColumnQuad(role + "_column_" + i, columnX, y, z, baseColor),
                    List.copyOf(dotQuads)));
        }
        return new RbmkColumnGridPlan(role, false, true, LEGACY_FULLBRIGHT_LIGHTMAP_X,
                translateX, translateY, translateZ, scaleX, scaleY, scaleZ, List.copyOf(plans));
    }

    private static UntexturedQuadPlan rbmkColumnQuad(String role, double x, double y, double z, RgbaPlan color) {
        double width = RBMK_COLUMN_WIDTH;
        return new UntexturedQuadPlan(role, List.of(
                vertex(x, y + width, z - width, color),
                vertex(x, y + width, z + width, color),
                vertex(x, y - width, z + width, color),
                vertex(x, y - width, z - width, color)));
    }

    private static List<UntexturedQuadPlan> rbmkColumnDotQuads(String role, double x, double y, double z,
            RbmkColumnInputPlan column) {
        RgbaPlan color = rbmkColumnDotColor(column);
        if (color == null) {
            return List.of();
        }
        double width = RBMK_COLUMN_DOT_WIDTH;
        double edge = RBMK_COLUMN_DOT_EDGE;
        return List.of(
                new UntexturedQuadPlan(role + "_upper", List.of(
                        vertex(x, y + width, z, color),
                        vertex(x, y + edge, z + edge, color),
                        vertex(x, y, z + width, color),
                        vertex(x, y - edge, z + edge, color))),
                new UntexturedQuadPlan(role + "_lower", List.of(
                        vertex(x, y + edge, z - edge, color),
                        vertex(x, y + width, z, color),
                        vertex(x, y - edge, z - edge, color),
                        vertex(x, y, z - width, color))),
                new UntexturedQuadPlan(role + "_vertical", List.of(
                vertex(x, y + width, z, color),
                vertex(x, y - edge, z + edge, color),
                vertex(x, y - width, z, color),
                vertex(x, y - edge, z - edge, color))));
    }

    private static void addExposureRandomBeam(List<TranslatedBeamPlan> beams, Random random, int color,
            double translateX, double translateY, double translateZ, long currentMillis, String role) {
        if (random.nextInt(EXPOSURE_RANDOM_CHANCE) == 0) {
            beams.add(translatedBeam(role, translateX, translateY, translateZ,
                    0.0D, 0.0D, 5.0D, LegacyBeamRenderer.WaveType.RANDOM,
                    LegacyBeamRenderer.BeamType.LINE, color, 0xFFFFFF,
                    (int) (currentMillis % 1000L) / 50, 15, 0.125F, 1, 0.0F));
        }
    }

    private static TranslatedBeamPlan translatedBeam(String role, double translateX, double translateY,
            double translateZ, double x, double y, double z, LegacyBeamRenderer.WaveType wave,
            LegacyBeamRenderer.BeamType beamType, int outerColor, int innerColor,
            int start, int segments, float size, int layers, float thickness) {
        return new TranslatedBeamPlan(role, translateX, translateY, translateZ,
                LegacyBeamRenderer.beamPlan(x, y, z, wave, beamType, outerColor, innerColor,
                        start, segments, size, layers, thickness));
    }

    private static TranslatedBeamPlan translatedDepthBeam(String role, double translateX, double translateY,
            double translateZ, double x, double y, double z, LegacyBeamRenderer.WaveType wave,
            LegacyBeamRenderer.BeamType beamType, int outerColor, int innerColor,
            int start, int segments, float size, int layers, float thickness) {
        return new TranslatedBeamPlan(role, translateX, translateY, translateZ,
                LegacyBeamRenderer.beamPlanWithDepth(x, y, z, wave, beamType, outerColor, innerColor,
                        start, segments, size, layers, thickness));
    }

    private static LegacyBeamRenderer.BeamPlan creativeBatterySocketBeam(double x, double z, int start,
            int segments, float size) {
        return LegacyBeamRenderer.beamPlan(x, CREATIVE_BATTERY_BEAM_Y, z,
                LegacyBeamRenderer.WaveType.RANDOM, LegacyBeamRenderer.BeamType.SOLID,
                CREATIVE_BATTERY_BEAM_OUTER_COLOR, CREATIVE_BATTERY_BEAM_INNER_COLOR,
                start, segments, size, CREATIVE_BATTERY_BEAM_LAYERS, CREATIVE_BATTERY_BEAM_THICKNESS);
    }

    private static RgbaPlan rbmkColumnBaseColor(int index, RbmkColumnInputPlan column) {
        if (column.color() >= 0) {
            return rgba(switch (column.color()) {
                case 0 -> 0xFF0000;
                case 1 -> 0xFFFF00;
                case 2 -> 0x008000;
                case 3 -> 0x0000FF;
                case 4 -> 0x8000FF;
                default -> 0xFFFFFF;
            }, 1.0F);
        }
        double heat = column.maxHeat() == 0.0D ? 0.0D : column.heat() / column.maxHeat();
        double base = 0.65D + (index % 2) * 0.05D;
        return rgba((float) (base + ((1.0D - base) * heat)), (float) base, (float) base, 1.0F);
    }

    private static RgbaPlan rbmkColumnDotColor(RbmkColumnInputPlan column) {
        return switch (column.type()) {
            case FUEL, FUEL_SIM -> rgba(0.0F, (float) (0.25D + column.enrichment() * 0.75D), 0.0F, 1.0F);
            case CONTROL -> rgba((float) column.level(), (float) column.level(), 0.0F, 1.0F);
            case CONTROL_AUTO -> rgba((float) column.level(), 0.0F, (float) column.level(), 1.0F);
            default -> null;
        };
    }

    private static RgbaPlan rgba(int color, float alpha) {
        return rgba((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F, alpha);
    }

    private static RgbaPlan rgbaLegacy256(int color, float multiplier, float alpha) {
        return rgba(((color >> 16 & 255) / 256.0F) * multiplier,
                ((color >> 8 & 255) / 256.0F) * multiplier,
                ((color & 255) / 256.0F) * multiplier, alpha);
    }

    private static RgbaPlan rgba(float red, float green, float blue, float alpha) {
        return new RgbaPlan(red, green, blue, alpha,
                alphaByte(red), alphaByte(green), alphaByte(blue), alphaByte(alpha));
    }

    private static int alphaByte(double value) {
        return Math.max(0, Math.min(255, (int) (value * 255.0D)));
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static ColorAveragePlan averageColor(List<Integer> colors) {
        int count = 0;
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int color : colors) {
            red += color >> 16 & 255;
            green += color >> 8 & 255;
            blue += color & 255;
            count++;
        }
        if (count == 0) {
            return new ColorAveragePlan(0, 255, 255, 255, 0xFFFFFF);
        }
        int avgRed = red / count;
        int avgGreen = green / count;
        int avgBlue = blue / count;
        return new ColorAveragePlan(count, avgRed, avgGreen, avgBlue,
                avgRed << 16 | avgGreen << 8 | avgBlue);
    }

    public record FullbrightStatePlan(boolean savesPreviousLightmap,
                                      boolean pushesMatrix,
                                      boolean pushesLightingAttrib,
                                      boolean disablesLightingAndCull,
                                      float lightmapX,
                                      float lightmapY) {
    }

    public record BlendStatePlan(boolean blendEnabled, int srcRgb, int dstRgb,
                                 int srcAlpha, int dstAlpha, float alphaThreshold,
                                 boolean depthWrite, LegacyTexturedRenderMode modernRenderMode) {
    }

    public record AssemblySparkRenderPlan(boolean active, double uMin, double uMax,
                                          double mirroredUOffset, double epsilon,
                                          BlendStatePlan blend, FullbrightStatePlan fullbright,
                                          List<AssemblySparkBladePlan> blades) {
    }

    public record AssemblySparkBladePlan(String role, double translateX, double translateY,
                                         double translateZ, double length,
                                         List<TexturedQuadPlan> quads) {
    }

    public record BigAssTankFluidPlan(double height, double minU, double maxU,
                                      double fluidV, BlendStatePlan blend,
                                      List<TexturedQuadPlan> quads) {
    }

    public record ChemicalPlantFluidPlan(boolean active, ColorAveragePlan color,
                                         double textureTranslateU, double textureTranslateV,
                                         double alpha, BlendStatePlan blend,
                                         boolean depthWrite) {
    }

    public record ChemicalFactoryPlan(List<RotatingModelPartPlan> fans) {
    }

    public record BasicPressPlan(TranslatedModelPartPlan head, ItemTransformPlan item) {
    }

    public record ItemTransformPlan(String role, boolean active,
                                    double translateX, double translateY, double translateZ,
                                    double rotateYDegrees, double rotateXDegrees, double rotateZDegrees,
                                    double scale) {
    }

    public record PyroOvenPlan(TranslatedModelPartPlan slider, RotatingModelPartPlan fan) {
    }

    public record PumpjackPlan(double rotationDegrees, RotatingModelPartPlan rotor,
                               RotatingModelPartPlan head, TranslatedModelPartPlan carriage,
                               List<UntexturedQuadPlan> rods) {
    }

    public record CherenkovShellPlan(boolean active, BlendStatePlan blend,
                                     boolean textureEnabled, boolean alphaTestEnabled,
                                     List<UntexturedQuadPlan> shells) {
    }

    public record RbmkFuelChannelGlowPlan(boolean active, int offset, double translateY,
                                          BlendStatePlan blend, boolean textureEnabled,
                                          boolean alphaTestEnabled, List<UntexturedQuadPlan> quads) {
    }

    public record SolarBoilerBeamPlan(boolean active, int beamLimit,
                                      List<SolarBeamPlan> beams) {
    }

    public record SolarBeamTargetPlan(int dx, int dy, int dz) {
    }

    public record SolarBeamPlan(SolarBeamTargetPlan target, double distance,
                                double yawDegrees, double pitchDegrees,
                                BlendStatePlan blend, List<UntexturedQuadPlan> quads) {
    }

    public record RefuelerFluidPlan(double fillLevel, double translateY,
                                    ClipPlanePlan clipPlane, RgbaPlan color,
                                    BlendStatePlan blend) {
    }

    public record ClipPlanePlan(double x, double y, double z, double d) {
    }

    public record ModelPartTintPlan(String role, String partName, boolean active,
                                    RgbaPlan color, FullbrightStatePlan fullbright,
                                    BlendStatePlan blend, boolean textured) {
    }

    public record ScaledModelPartPlan(String role, String partName, boolean active,
                                      RgbaPlan color, double pivotY,
                                      double scaleX, double scaleY, double scaleZ,
                                      BlendStatePlan blend, boolean textured) {
    }

    public record BatteryReddTrailPlan(boolean active, double speed, double spanDegrees,
                                       BlendStatePlan blend, FullbrightStatePlan fullbright,
                                       List<UntexturedQuadPlan> quads) {
    }

    public record BatteryReddPlasmaPlan(boolean active, float baseAlpha, float alphaMultiplier,
                                        BlendStatePlan blend, FullbrightStatePlan fullbright,
                                        List<TextureMatrixPartPlan> layers) {
    }

    public record TextureMatrixPartPlan(String role, String partName, RgbaPlan color,
                                        LegacyUvAnimation.TextureMatrixPlan textureMatrix) {
    }

    public record CoreStandbyPlan(ModelSpherePlan base, ModelSpherePlan glow,
                                  boolean sparkTick, List<SparkInvocationPlan> sparks) {
    }

    public record ModelSpherePlan(String role, String legacyModel, double scale,
                                  RgbaPlan color, BlendStatePlan blend,
                                  boolean textureEnabled, boolean cullEnabled,
                                  boolean lightingEnabled) {
    }

    public record SparkInvocationPlan(int seed, double x, double y, double z,
                                      float length, int width, int steps,
                                      int innerColor, int outerColor) {
    }

    public record CoreOrbPlan(double fillScale, double pulse,
                              ModelSpherePlan base, List<ModelSpherePlan> glowShells) {
    }

    public record CoreFlarePlan(double scale, BlendStatePlan blend,
                                boolean textureEnabled, boolean alphaTestEnabled,
                                boolean cullEnabled, List<CoreFlareRayPlan> rays) {
    }

    public record CoreFlareRayPlan(int index, float rotateX0, float rotateY0,
                                   float rotateZ0, float rotateX1, float rotateY1,
                                   double rotateZ1, float length, float radius,
                                   RgbaPlan centerColor, RgbaPlan edgeColor) {
    }

    public record CoreVoidPlan(boolean textureGenEnabled, boolean lightingDisabled,
                               TextureGenOffsetPlan initialTextureOffset,
                               int layerCount, int segmentCount,
                               List<CoreVoidLayerPlan> layers) {
    }

    public record TextureGenOffsetPlan(double x, double y, double z) {
    }

    public record CoreVoidLayerPlan(int layer, String textureRole, BlendStatePlan blend,
                                    TextureGenOffsetPlan layerOffset, float scale,
                                    float rotationDegrees, RgbaPlan color,
                                    double radius, List<UntexturedTrianglePlan> triangles) {
    }

    public record RadarScreenPlan(boolean linked, BlendStatePlan blend,
                                  UntexturedQuadPlan scanline,
                                  List<TexturedQuadPlan> blips,
                                  TexturedQuadPlan offlineStatic) {
    }

    public record RadarBlipInputPlan(int posX, int posZ, int blipLevel) {
    }

    public record ExcavatorChutePlan(boolean active, boolean crusherEnabled,
                                     double dropU, double dropL,
                                     List<NormalTexturedQuadPlan> upperStream,
                                     List<NormalTexturedQuadPlan> lowerStream) {
    }

    public record NormalTexturedQuadPlan(String role, float normalX, float normalY,
                                         float normalZ, List<NormalTexturedVertexPlan> vertices) {
    }

    public record NormalTexturedVertexPlan(double x, double y, double z, double u, double v) {
    }

    public record DoorGenericStatePlan(boolean sednaRenderer, boolean animatedModel,
                                       List<ClipPlanePlan> clippingPlanes,
                                       BlendStatePlan blend, boolean smoothShade) {
    }

    public record SoyuzMultiblockGhostPlan(BlendStatePlan blend, RgbaPlan color,
                                           boolean cullEnabled, boolean alphaTestEnabled,
                                           double smallBlockMin, double smallBlockMax,
                                           int blockCount, List<SoyuzGhostRangePlan> ranges,
                                           List<TexturedQuadPlan> smallBlockQuads) {
    }

    public record SoyuzGhostRangePlan(String textureRole, int minX, int maxX,
                                      int minY, int maxY, int minZ, int maxZ) {
        public int blockCount() {
            return Math.max(0, maxX - minX + 1)
                    * Math.max(0, maxY - minY + 1)
                    * Math.max(0, maxZ - minZ + 1);
        }
    }

    public record RbmkNumitronPlan(boolean active, int unitLimit,
                                   FullbrightStatePlan fullbright,
                                   List<RbmkNumitronUnitPlan> units) {
    }

    public record RbmkNumitronInputPlan(boolean active, long value,
                                        boolean shortenNumber, boolean leadingZeroes,
                                        long activeDigits, String label) {
    }

    public record RbmkNumitronUnitPlan(int index, boolean active,
                                       double translateX, double translateY, double translateZ,
                                       String formattedValue, long activeDigits,
                                       List<NumitronDigitPlan> digits,
                                       NumitronLabelPlan label) {
    }

    public record NumitronDigitPlan(int index, char character, double zOffset,
                                    NumitronDigitUvPlan uv, TexturedQuadPlan quad) {
    }

    public record NumitronDigitUvPlan(double u, double v, boolean blank) {
    }

    public record NumitronLabelPlan(boolean active, String label,
                                    double translateX, double translateY, double translateZ,
                                    float maxScale, float widthScaleNumerator,
                                    float normalX, float normalY, float normalZ,
                                    float rotateYDegrees, int color,
                                    FullbrightStatePlan fullbright) {
        public float scaleForWidth(int width) {
            return Math.min(maxScale, widthScaleNumerator / Math.max(width, 1));
        }
    }

    public record StrandCasterPlan(boolean active, int amount, int capacity, int moldCost,
                                   double level, double offset, RgbaPlan color,
                                   StrandCasterPlatePlan plate,
                                   StrandCasterLavaPlan lava) {
    }

    public record StrandCasterPlatePlan(String partName, double translateZ,
                                        ClipPlanePlan clipPlane, RgbaPlan color,
                                        boolean lightingDisabled) {
    }

    public record StrandCasterLavaPlan(String textureRole, FullbrightStatePlan fullbright,
                                       boolean cullEnabled, RgbaPlan color,
                                       NormalTexturedQuadPlan quad) {
    }

    public record MaxwellBeamPlan(boolean active, int beamTicks, double beamDistance,
                                  double barrelLength, double length,
                                  double translateX, double translateY, double translateZ,
                                  FullbrightStatePlan fullbright, BlendStatePlan blend,
                                  boolean textureEnabled, boolean alphaTestEnabled,
                                  boolean depthWrite, List<LegacyBeamRenderer.BeamPlan> beams) {
    }

    public record TranslatedBeamPlan(String role, double translateX, double translateY,
                                     double translateZ, LegacyBeamRenderer.BeamPlan beam) {
    }

    public record TauonBeamPlan(boolean active, int beamTicks, double beamDistance,
                                double translateX, double translateY, double translateZ,
                                FullbrightStatePlan fullbright,
                                LegacyBeamRenderer.BeamPlan beam) {
    }

    public record CreativeBatterySocketPlan(double horseScale, double horseYawDegrees,
                                            double beamTranslateX, double beamTranslateY,
                                            double beamTranslateZ,
                                            List<LegacyBeamRenderer.BeamPlan> beams) {
    }

    public record LaserMinerBeamPlan(boolean active, double targetVectorX,
                                     double targetVectorY, double targetVectorZ,
                                     double beamVectorX, double beamVectorY, double beamVectorZ,
                                     double translateX, double translateY, double translateZ,
                                     double beamLength, double yawDegrees, double pitchDegrees,
                                     int range, List<LegacyBeamRenderer.BeamPlan> beams) {
    }

    public record IcfLaserBeamPlan(boolean active, double laserLength,
                                   FullbrightStatePlan fullbright,
                                   boolean textureEnabled, boolean lightingEnabled,
                                   LegacyBeamRenderer.BeamPlan beam) {
    }

    public record TeslaTargetPlan(double x, double y, double z) {
    }

    public record TeslaBeamPlan(boolean active, double sourceX, double sourceY,
                                double sourceZ, boolean cullEnabled,
                                List<TeslaTargetBeamPlan> targetBeams) {
    }

    public record TeslaTargetBeamPlan(TeslaTargetPlan target, double length,
                                      LegacyBeamRenderer.BeamPlan beam) {
    }

    public record ExposureChamberPlan(boolean on, double rotationDegrees,
                                      double coreRotationDegrees, double coreBobY,
                                      FullbrightStatePlan fullbright,
                                      boolean lightingEnabled, boolean cullEnabled,
                                      List<TranslatedBeamPlan> beams) {
    }

    public record DfcBeamPlan(String role, int range, boolean lightingEnabled,
                              boolean cullEnabled, List<TranslatedBeamPlan> beams) {
    }

    public record RotatingModelPartPlan(String role, String partName,
                                        double pivotX, double pivotY, double pivotZ,
                                        float axisX, float axisY, float axisZ,
                                        double angleDegrees) {
    }

    public record PivotedModelPartPlan(String role, String partName,
                                       double pivotX, double pivotY, double pivotZ,
                                       float axisX, float axisY, float axisZ,
                                       double angleDegrees,
                                       double translateX, double translateY, double translateZ) {
    }

    public record TranslatedModelPartPlan(String role, String partName, boolean active,
                                          double translateX, double translateY, double translateZ) {
    }

    public record ConveyorPressPlan(TranslatedModelPartPlan piston,
                                    TextureMatrixPartPlan belt) {
    }

    public enum ArcElectrodeState {
        NONE,
        FRESH,
        USED,
        DEPLETED
    }

    public record ArcFurnacePlan(double lidLift, boolean progressing,
                                 FullbrightStatePlan fullbright,
                                 TranslatedModelPartPlan contentsHot,
                                 TranslatedModelPartPlan contentsCold,
                                 TranslatedModelPartPlan lid,
                                 List<ArcElectrodePlan> electrodes,
                                 List<RotatingModelPartPlan> cables) {
    }

    public record ArcElectrodePlan(int index, ArcElectrodeState state,
                                   String ringPartName, String freshPartName,
                                   String usedHotPartName, String depletedShortPartName) {
    }

    public record SteamEnginePlan(double rotorDegrees, double translateX,
                                  RotatingModelPartPlan flywheel,
                                  RotatingModelPartPlan shaft,
                                  SteamEngineTransmissionPlan transmission,
                                  TranslatedModelPartPlan piston) {
    }

    public record SteamEngineTransmissionPlan(double translateX, double translateY,
                                              double angleDegrees,
                                              double pivotX, double pivotY) {
    }

    public record Bat9000FluidPlan(boolean active, double height, RgbaPlan color,
                                   boolean textureEnabled, boolean lightingEnabled,
                                   boolean cullEnabled, TankDangerDiamondPlan dangerDiamonds,
                                   List<UntexturedQuadPlan> quads) {
    }

    public record WandStructureBoundsPlan(int sizeX, int sizeY, int sizeZ,
                                          boolean textureEnabled, boolean lightingEnabled,
                                          int brightness, RgbaPlan color,
                                          List<UntexturedLinePlan> lines) {
    }

    public record AutosawPlan(double turnDegrees, double engineTranslateY,
                              PivotedModelPartPlan armUpper,
                              PivotedModelPartPlan armLower,
                              PivotedModelPartPlan armTip,
                              PivotedModelPartPlan sawBlade) {
    }

    public record ThresherPlan(double angleDegrees, double spinDegrees, double engineTranslateY,
                               PivotedModelPartPlan armUpper,
                               PivotedModelPartPlan armLower,
                               PivotedModelPartPlan front,
                               PivotedModelPartPlan wheel) {
    }

    public record TurbofanPlan(double bladeSpinDegrees, boolean afterburner,
                               String afterburnerTextureRole,
                               RotatingModelPartPlan blades) {
    }

    public record IndustrialTurbinePlan(double gaugeDegrees, double flywheelDegrees,
                                        RotatingModelPartPlan gauge,
                                        RotatingModelPartPlan flywheel) {
    }

    public record BigTurbinePlan(boolean cullEnabled, double baseRotationY,
                                 double translateZ, String bladeTextureRole,
                                 RotatingModelPartPlan blades) {
    }

    public record BoilerPlan(boolean exploded, boolean overpressure,
                             double scaleX, double scaleY, double scaleZ,
                             String modelRole, boolean cullEnabled) {
    }

    public record CraneConsolePlan(double translateX, CraneJoystickPlan joystick,
                                   CraneMeterPlan meterHeat, CraneMeterPlan meterEnrichment,
                                   ModelPartTintPlan loadingLamp,
                                   ModelPartTintPlan targetLamp,
                                   CraneRigPlan rig) {
    }

    public record CraneJoystickPlan(double pivotX, double pivotY, double restoreY,
                                    double tiltFrontDegrees, double tiltLeftDegrees) {
    }

    public record CraneMeterPlan(String partName, double pivotZ, double angleDegrees) {
        public double pivotY() {
            return CRANE_METER_PIVOT_Y;
        }
    }

    public record CraneSetupInputPlan(boolean setUpCrane, int height,
                                      double centerOffsetX, double centerOffsetY, double centerOffsetZ,
                                      double lastPosFront, double posFront,
                                      double lastPosLeft, double posLeft,
                                      int craneRotationOffset,
                                      int spanFront, int spanBack,
                                      int spanLeft, int spanRight,
                                      double lastProgress, double progress) {
    }

    public record CraneRigPlan(boolean active,
                               double centerTranslateX, double centerTranslateY, double centerTranslateZ,
                               double carriageTranslateX, double carriageTranslateY, double carriageTranslateZ,
                               int rotationOffsetDegrees, int tubeCount, int girderSpan,
                               double girderTranslateX, double girderTranslateY, double girderTranslateZ,
                               double liftTranslateY) {
    }

    public record PylonWireLinePlan(String textureRole, int color, boolean hang,
                                    boolean lightingEnabled, boolean cullEnabled,
                                    double girth, double uWrapPerBlock, int hangSegments,
                                    double maxHang, double hangDivisor,
                                    LegacyTexturedLineRenderer.WireOffsets offsets,
                                    List<PylonWireSegmentPlan> segments) {
    }

    public record PylonWireSegmentPlan(LegacyTexturedLineRenderer.WireSubSegment segment,
                                       LegacyTexturedLineRenderer.WireWrap wrap) {
    }

    public enum RbmkColumnType {
        FUEL,
        FUEL_SIM,
        CONTROL,
        CONTROL_AUTO,
        OTHER
    }

    public record RbmkColumnInputPlan(RbmkColumnType type, int color, int indicator,
                                      double heat, double maxHeat,
                                      double enrichment, double level) {
    }

    public record RbmkColumnGridPlan(String role, boolean textureEnabled, boolean fullBright,
                                     float brightness, double translateX, double translateY,
                                     double translateZ, double scaleX, double scaleY,
                                     double scaleZ, List<RbmkColumnPlan> columns) {
    }

    public record RbmkColumnPlan(int index, double x, double y, double z,
                                 RgbaPlan baseColor, UntexturedQuadPlan column,
                                 List<UntexturedQuadPlan> dotQuads) {
    }

    public record TankDangerDiamondPlan(boolean hasFluid, List<DiamondTransformPlan> transforms) {
    }

    public record DiamondTransformPlan(String role, double translateX, double translateY,
                                       double translateZ, float yawDegrees,
                                       float scaleX, float scaleY, float scaleZ) {
    }

    public record TexturedQuadPlan(String role, List<QuadVertexPlan> vertices) {
    }

    public record QuadVertexPlan(double x, double y, double z, double u, double v,
                                 int color, int alpha) {
    }

    public record UntexturedQuadPlan(String role, List<UntexturedVertexPlan> vertices) {
    }

    public record UntexturedTrianglePlan(String role, List<UntexturedVertexPlan> vertices) {
    }

    public record UntexturedLinePlan(String role, double x0, double y0, double z0,
                                     double x1, double y1, double z1) {
    }

    public record UntexturedVertexPlan(double x, double y, double z, RgbaPlan color) {
    }

    public record RgbaPlan(float red, float green, float blue, float alpha,
                           int redByte, int greenByte, int blueByte, int alphaByte) {
    }

    public record ColorAveragePlan(int count, int red, int green, int blue, int color) {
    }

    private record TrailPoint(double innerY, double innerZ, double outerY, double outerZ) {
    }

    private LegacyTileRenderPlans() {
    }
}
