package com.hbm.ntm.uninos;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HbmNodeNet<L extends HbmNetworkNode> {
    private final Set<L> links = new LinkedHashSet<>();
    private boolean valid = true;

    public boolean isValid() {
        return valid;
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

    public HbmNodeNet<L> joinLink(L node) {
        if (node == null) {
            return this;
        }
        HbmNodeNet<?> oldNet = node.getNet();
        if (oldNet != null && oldNet != this) {
            oldNet.leaveAnyLink(node);
        }
        return forceJoinLink(node);
    }

    public HbmNodeNet<L> forceJoinLink(L node) {
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
        links.remove(node);
        if (node.getNet() == this) {
            node.setNet(null);
        }
    }

    public void joinNetwork(HbmNodeNet<L> network) {
        if (network == null || network == this) {
            return;
        }
        List<L> oldLinks = new ArrayList<>(network.links);
        for (L link : oldLinks) {
            forceJoinLink(link);
        }
        network.links.clear();
        network.destroy();
    }

    public void invalidate() {
        valid = false;
    }

    public void destroy() {
        invalidate();
        for (L link : links) {
            if (link.getNet() == this) {
                link.setNet(null);
            }
        }
        links.clear();
    }

    private void leaveAnyLink(HbmNetworkNode node) {
        links.remove(node);
        if (node.getNet() == this) {
            node.setNet(null);
        }
    }
}
