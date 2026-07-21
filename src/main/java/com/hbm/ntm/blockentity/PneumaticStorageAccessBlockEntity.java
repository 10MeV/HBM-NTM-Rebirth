package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.ntl.PneumaticConnector;
import com.hbm.ntm.block.PneumaticStorageAccessBlock;
import com.hbm.ntm.menu.PneumaticStorageAccessMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNetwork;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNode;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticStackCache;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PneumaticStorageAccessBlockEntity extends BlockEntity implements MenuProvider, PneumaticConnector {
    private PneumaticNode node;
    private PneumaticStackCache cache;

    public PneumaticStorageAccessBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PNEUMATIC_STORAGE_ACCESS.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PneumaticStorageAccessBlockEntity access) {
        access.refreshNode();
        if (access.cache == null || access.cache.hasExpired()) {
            access.cache = new PneumaticStackCache(pos);
        }
        PneumaticNetwork network = access.getPneumaticNet();
        if (network != null) {
            network.addStackCache(access.cache);
        }
    }

    public PneumaticStackCache getCache() {
        return cache;
    }

    public PneumaticNetwork getPneumaticNet() {
        return node == null ? null : node.getPneumaticNet();
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != null && side == getBlockState().getValue(PneumaticStorageAccessBlock.FACING).getOpposite();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.pneumoStorageAccess", "Pneumatic Storage Access");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PneumaticStorageAccessMenu(containerId, inventory, this);
    }

    @Override
    public void setRemoved() {
        removeNode();
        dissolveCache();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        removeNode();
        dissolveCache();
        super.onChunkUnloaded();
    }

    private void refreshNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        // Legacy TileEntityPneumoStorageAccess uses setStandardConnections for
        // its node. Its facing only limits the block connector API, not the
        // six endpoint declarations consumed by UniNodespace.
        Set<Direction> connections = PneumaticUtil.allConnections();
        if (node != null && !node.isExpired() && !node.getConnections().equals(connections)) {
            removeNode();
        }
        if (node == null || node.isExpired()) {
            node = PneumaticNodespace.createNode(level, new PneumaticNode(worldPosition, connections));
        }
    }

    private void removeNode() {
        if (level != null && !level.isClientSide) {
            PneumaticNodespace.destroyNode(level, worldPosition);
        }
        node = null;
    }

    private void dissolveCache() {
        if (cache != null) {
            cache.dissolveCache();
            cache = null;
        }
    }
}
