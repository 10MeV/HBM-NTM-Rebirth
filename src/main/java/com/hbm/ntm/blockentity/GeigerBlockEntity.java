package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.api.tile.IInfoProviderEC;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.ContaminationUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GeigerBlockEntity extends BlockEntity implements IInfoProviderEC, RORValueProvider {
    private final RORDispatcher ror;
    private int timer;
    private float ticker;

    public GeigerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEIGER.get(), pos, state);
        this.ror = RORDispatcher.builder()
                .value("rad", () -> Integer.toString((int) Math.ceil(ticker)))
                .build();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeigerBlockEntity geiger) {
        if (level.isClientSide) {
            return;
        }

        geiger.timer++;
        if (geiger.timer == 10) {
            geiger.timer = 0;
            geiger.ticker = geiger.checkRadiation();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        if (geiger.timer % 5 == 0) {
            geiger.playTickSound(level, pos);
        }
    }

    public float checkRadiation() {
        if (level == null) {
            return 0.0F;
        }
        return ChunkRadiationManager.getRadiation(level, worldPosition);
    }

    private void playTickSound(Level level, BlockPos pos) {
        if (ticker > 0.0F) {
            List<Integer> candidates = new ArrayList<>();
            if (ticker < 1.0F) candidates.add(0);
            if (ticker < 5.0F) candidates.add(0);
            if (ticker < 10.0F) candidates.add(1);
            if (ticker > 5.0F && ticker < 15.0F) candidates.add(2);
            if (ticker > 10.0F && ticker < 20.0F) candidates.add(3);
            if (ticker > 15.0F && ticker < 25.0F) candidates.add(4);
            if (ticker > 20.0F && ticker < 30.0F) candidates.add(5);
            if (ticker > 25.0F) candidates.add(6);

            int sound = candidates.get(level.random.nextInt(candidates.size()));
            if (sound > 0) {
                LegacySoundPlayer.playLegacyGeiger(level, pos.getX(), pos.getY(), pos.getZ(), sound);
            }
        } else if (level.random.nextInt(50) == 0) {
            LegacySoundPlayer.playLegacyGeiger(level, pos.getX(), pos.getY(), pos.getZ(), 1);
        }
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        int rads = (int) Math.ceil(ticker);
        data.putString(CompatEnergyControl.S_CHUNKRAD,
                ContaminationUtil.getPreffixFromRad(rads) + rads + " RAD/s");
    }

    @Override
    public String[] getFunctionInfo() {
        return ror.getFunctionInfo();
    }

    @Override
    public String provideRORValue(String name) {
        return ror.provideValue(name);
    }
}
