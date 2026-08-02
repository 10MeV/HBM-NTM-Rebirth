package com.hbm.ntm.blockentity;

import com.hbm.ntm.entity.item.FireworksEntity;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Server-side state/timing contract of 1.7.10 {@code TileEntityFireworks}. */
public final class FireworksBlockEntity extends BlockEntity {
    private static final String TAG_CHARGES = "charges";
    private static final String TAG_COLOR = "color";
    private static final String TAG_MESSAGE = "message";

    private int charges;
    private int color = 0xFF0000;
    private String message = "NUCLEAR TECH";
    private int index;
    private int delay;

    public FireworksBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIREWORKS.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FireworksBlockEntity fireworks) {
        if (!level.hasNeighborSignal(pos) || fireworks.message.isEmpty() || fireworks.charges <= 0) {
            fireworks.delay = 0;
            fireworks.index = 0;
            return;
        }
        if (--fireworks.delay > 0) {
            return;
        }
        fireworks.delay = 30;
        char character = fireworks.message.charAt(fireworks.index);
        int mod = fireworks.index % 9;
        double offsetX = (mod / 3 - 1) * 0.3125D;
        double offsetZ = (mod % 3 - 1) * 0.3125D;
        double x = pos.getX() + 0.5D + offsetX;
        double z = pos.getZ() + 0.5D + offsetZ;

        FireworksEntity entity = new FireworksEntity(ModEntityTypes.FIREWORKS.get(), level, x, pos.getY() + 1.5D, z,
                fireworks.color, character);
        level.addFreshEntity(entity);
        LegacySoundPlayer.playLegacyRocketFlame(entity, 3.0F, 1.0F);
        fireworks.charges--;
        fireworks.setChanged();
        // Legacy PacketThreading target range is 100, rather than the library default vanillaExt range.
        CompoundTag particle = new CompoundTag();
        particle.putString("type", ParticleUtil.TYPE_VANILLA_EXT);
        particle.putString("mode", ParticleUtil.VANILLA_FLAME);
        ParticleUtil.spawnAux(level, x, pos.getY() + 1.125D, z, particle, 100.0D);

        if (++fireworks.index >= fireworks.message.length()) {
            fireworks.index = 0;
            fireworks.delay = 100;
        }
    }

    public int charges() { return charges; }
    public int color() { return color; }
    public String message() { return message; }

    public void addCharges(int amount) {
        charges += amount;
        setChanged();
    }

    public void setColor(int color) {
        this.color = color;
        setChanged();
    }

    public void setMessage(String message) {
        this.message = message;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_CHARGES, charges);
        tag.putInt(TAG_COLOR, color);
        tag.putString(TAG_MESSAGE, message);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        charges = tag.getInt(TAG_CHARGES);
        color = tag.getInt(TAG_COLOR);
        message = tag.getString(TAG_MESSAGE);
    }
}
