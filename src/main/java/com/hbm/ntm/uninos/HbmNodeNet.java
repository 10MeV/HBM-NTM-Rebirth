package com.hbm.ntm.uninos;

import com.hbm.ntm.api.tile.LoadedTile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Source-shaped common network state.  1.7.10 kept one typed subscriber pair
 * on this base class; specialised networks must inherit rather than hide it.
 */
public class HbmNodeNet<R, P, L extends HbmNetworkNode> {
    /** Global random source retained from 1.7.10 {@code NodeNet#rand}. */
    public static Random rand = new Random();

    /** Mutable legacy network validity flag; callers historically read and changed it directly. */
    public boolean valid = true;
    /** Mutable legacy link set; direct mutation intentionally does not assign {@link HbmNetworkNode#getNet()}. */
    public Set<L> links = new LinkedHashSet<>();
    /** Live legacy {@code NodeNet#receiverEntries} state. */
    public HashMap<R, Long> receiverEntries = new HashMap<>();
    /** Live legacy {@code NodeNet#providerEntries} state. */
    public HashMap<P, Long> providerEntries = new HashMap<>();
    /** Nodespace-owned active-set removal, installed when this net becomes live. */
    private Runnable invalidationHook = () -> {
    };

    public boolean isValid() {
        return valid;
    }

    /**
     * Legacy {@code NodeNet} exposes this lifecycle hook on every network;
     * specialised networks override it to reset their transfer accounting.
     */
    public void resetTrackers() {
    }

    /**
     * Legacy {@code NodeNet} exposes one network-update entry point.  The
     * generic core has no transfer work of its own, while specialised
     * networks override this hook.
     */
    public long update() {
        // 1.7.10's base hook is void.  The modern Power/Fluid implementations
        // additionally expose their transferred amount to normal machine code
        // and GameTests, so the common carrier returns zero for a no-op network.
        return 0L;
    }

    /** Compatibility equivalent of 1.7.10 {@code NodeNet#isBadLink(Object)}. */
    public static boolean isBadLink(Object link) {
        if (link instanceof LoadedTile loadedTile && !loadedTile.isLoaded()) {
            return true;
        }
        return link instanceof BlockEntity blockEntity && blockEntity.isRemoved();
    }

    public Set<L> getLinks() {
        return Set.copyOf(links);
    }

    public int linkCount() {
        return links.size();
    }

    public boolean containsLink(L node) {
        return links.contains(node);
    }

    public boolean isSubscribed(R receiver) {
        return receiverEntries.containsKey(receiver);
    }

    public void addReceiver(R receiver) {
        receiverEntries.put(receiver, System.currentTimeMillis());
    }

    public void removeReceiver(R receiver) {
        receiverEntries.remove(receiver);
    }

    public boolean isProvider(P provider) {
        return providerEntries.containsKey(provider);
    }

    public void addProvider(P provider) {
        providerEntries.put(provider, System.currentTimeMillis());
    }

    public void removeProvider(P provider) {
        providerEntries.remove(provider);
    }

    public HbmNodeNet<R, P, L> joinLink(L node) {
        if (node == null) {
            return this;
        }
        HbmNodeNet<?, ?, ?> oldNet = node.getNet();
        if (oldNet != null) {
            oldNet.leaveAnyLink(node);
        }
        return forceJoinLink(node);
    }

    public HbmNodeNet<R, P, L> forceJoinLink(L node) {
        if (node == null) {
            return this;
        }
        links.add(node);
        node.setNet(this);
        return this;
    }

    public void leaveLink(L node) {
        if (node == null) {
            return;
        }
        node.setNet(null);
        links.remove(node);
    }

    public void joinNetwork(HbmNodeNet<R, P, L> network) {
        if (network == null || network == this) {
            return;
        }
        List<L> oldLinks = new ArrayList<>(network.links);
        for (L link : oldLinks) {
            forceJoinLink(link);
        }
        network.links.clear();
        for (R receiver : network.receiverEntries.keySet()) {
            addReceiver(receiver);
        }
        for (P provider : network.providerEntries.keySet()) {
            addProvider(provider);
        }
        network.destroy();
    }

    /** Compatibility name retained from 1.7.10 {@code NodeNet#joinNetworks}. */
    public void joinNetworks(HbmNodeNet<R, P, L> network) {
        joinNetwork(network);
    }

    public void invalidate() {
        valid = false;
        invalidationHook.run();
    }

    /**
     * Binds the modern per-nodespace active-set bookkeeping to the legacy
     * {@link #invalidate()} contract.  Not a second lifecycle: it is the
     * modern carrier for old {@code UniNodespace.activeNodeNets.remove(this)}.
     */
    void setInvalidationHook(Runnable invalidationHook) {
        this.invalidationHook = invalidationHook == null ? () -> {
        } : invalidationHook;
    }

    public void destroy() {
        invalidate();
        for (L link : links) {
            if (link.getNet() == this) {
                link.setNet(null);
            }
        }
        links.clear();
        receiverEntries.clear();
        providerEntries.clear();
    }

    /**
     * Performs the deliberately shallow cleanup used by legacy
     * {@code /ntmreapnetworks}. The old command did not destroy a network: it
     * only expired its links, emptied them, and removed the network from the
     * active registry. Specialised networks therefore retain cache objects
     * until their real block-entity lifecycle decides otherwise.
     */
    public final void reapLegacy() {
        for (L link : links) {
            link.setExpired(true);
        }
        links.clear();
        receiverEntries.clear();
        providerEntries.clear();
        onLegacyReap();
    }

    /** Gives subscriber-based networks the command's explicit map cleanup. */
    protected void onLegacyReap() {
    }

    private void leaveAnyLink(HbmNetworkNode node) {
        node.setNet(null);
        links.remove(node);
    }
}
