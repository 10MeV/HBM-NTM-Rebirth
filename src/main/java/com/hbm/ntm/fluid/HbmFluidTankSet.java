package com.hbm.ntm.fluid;

import java.util.ArrayList;
import java.util.List;

public final class HbmFluidTankSet {
    private HbmFluidTankSet() {
    }

    public static TankSetInspection inspectReceivingTanks(Iterable<HbmFluidTank> tanks, FluidType type, int pressure) {
        FluidType normalizedType = normalize(type);
        int normalizedPressure = pressure;
        List<TankInspection> details = inspectTanks(tanks, normalizedType, normalizedPressure);
        long demand = 0L;
        int matchingTanks = 0;
        for (TankInspection detail : details) {
            if (detail.matchesTypeAndPressure()) {
                matchingTanks++;
                demand += detail.spaceMb();
            }
        }
        int[] range = pressureRange(tanks, normalizedType);
        return new TankSetInspection(normalizedType, normalizedPressure, matchingTanks, demand, 0L,
                range[0], range[1], details);
    }

    public static TankSetInspection inspectSendingTanks(Iterable<HbmFluidTank> tanks, FluidType type, int pressure) {
        FluidType normalizedType = normalize(type);
        int normalizedPressure = pressure;
        List<TankInspection> details = inspectTanks(tanks, normalizedType, normalizedPressure);
        long available = 0L;
        int matchingTanks = 0;
        for (TankInspection detail : details) {
            if (detail.matchesTypeAndPressure()) {
                matchingTanks++;
                available += detail.fillMb();
            }
        }
        int[] range = pressureRange(tanks, normalizedType);
        return new TankSetInspection(normalizedType, normalizedPressure, matchingTanks, 0L, available,
                range[0], range[1], details);
    }

    public static TankTransferReport previewReceive(Iterable<HbmFluidTank> tanks, FluidType type, int pressure,
            long amount) {
        return receive(tanks, type, pressure, amount, true);
    }

    public static TankTransferReport receive(Iterable<HbmFluidTank> tanks, FluidType type, int pressure, long amount,
            boolean simulate) {
        FluidType normalizedType = normalize(type);
        int normalizedPressure = pressure;
        if (amount <= 0L || tanks == null || normalizedType == HbmFluids.NONE) {
            return TankTransferReport.empty(normalizedType, normalizedPressure, amount, simulate);
        }
        List<TankTransferDetail> details = new ArrayList<>();
        List<HbmFluidTank> tankList = asList(tanks);
        int[] virtualFill = fills(tankList);
        int matchingTanks = countMatching(tankList, normalizedType, normalizedPressure);
        long remaining = amount;
        if (matchingTanks > 1) {
            int firstRound = (int) Math.floor((double) amount / (double) matchingTanks);
            for (int i = 0; i < tankList.size(); i++) {
                HbmFluidTank tank = tankList.get(i);
                if (matches(tank, normalizedType, normalizedPressure)) {
                    int before = virtualFill[i];
                    int accepted = acceptIntoTank(tank, virtualFill, i, firstRound, normalizedType, normalizedPressure,
                            simulate);
                    remaining -= accepted;
                    details.add(TankTransferDetail.forTank(i, tank, before, virtualFill[i], accepted));
                }
            }
        }
        if (remaining > 0L) {
            for (int i = 0; i < tankList.size(); i++) {
                HbmFluidTank tank = tankList.get(i);
                if (matches(tank, normalizedType, normalizedPressure)) {
                    int before = virtualFill[i];
                    int accepted = acceptIntoTank(tank, virtualFill, i,
                            (int) Math.min(Integer.MAX_VALUE, remaining), normalizedType, normalizedPressure,
                            simulate);
                    remaining -= accepted;
                    details.add(TankTransferDetail.forTank(i, tank, before, virtualFill[i], accepted));
                }
            }
        }
        return new TankTransferReport(normalizedType, normalizedPressure, amount, Math.max(0L, amount - remaining),
                Math.max(0L, remaining), simulate, matchingTanks, List.copyOf(details));
    }

    public static TankTransferReport previewUseUp(Iterable<HbmFluidTank> tanks, FluidType type, int pressure,
            long amount) {
        return useUp(tanks, type, pressure, amount, true);
    }

    public static TankTransferReport useUp(Iterable<HbmFluidTank> tanks, FluidType type, int pressure, long amount,
            boolean simulate) {
        FluidType normalizedType = normalize(type);
        int normalizedPressure = pressure;
        if (amount <= 0L || tanks == null || normalizedType == HbmFluids.NONE) {
            return TankTransferReport.empty(normalizedType, normalizedPressure, amount, simulate);
        }
        List<TankTransferDetail> details = new ArrayList<>();
        List<HbmFluidTank> tankList = asList(tanks);
        int[] virtualFill = fills(tankList);
        int matchingTanks = countMatching(tankList, normalizedType, normalizedPressure);
        long remaining = amount;
        if (matchingTanks > 1) {
            int firstRound = (int) Math.floor((double) amount / (double) matchingTanks);
            for (int i = 0; i < tankList.size(); i++) {
                HbmFluidTank tank = tankList.get(i);
                if (matches(tank, normalizedType, normalizedPressure)) {
                    int before = virtualFill[i];
                    int removed = Math.min(firstRound, virtualFill[i]);
                    if (!simulate && removed > 0) {
                        removed = tank.drainReport(removed, false).movedMb();
                    }
                    virtualFill[i] -= removed;
                    remaining -= removed;
                    details.add(TankTransferDetail.forTank(i, tank, before, virtualFill[i], removed));
                }
            }
        }
        if (remaining > 0L) {
            for (int i = 0; i < tankList.size(); i++) {
                HbmFluidTank tank = tankList.get(i);
                if (matches(tank, normalizedType, normalizedPressure)) {
                    int before = virtualFill[i];
                    int removed = (int) Math.min(remaining, virtualFill[i]);
                    if (!simulate && removed > 0) {
                        removed = tank.drainReport(removed, false).movedMb();
                    }
                    virtualFill[i] -= removed;
                    remaining -= removed;
                    details.add(TankTransferDetail.forTank(i, tank, before, virtualFill[i], removed));
                }
            }
        }
        return new TankTransferReport(normalizedType, normalizedPressure, amount, Math.max(0L, amount - remaining),
                Math.max(0L, remaining), simulate, matchingTanks, List.copyOf(details));
    }

    public static int[] pressureRange(Iterable<HbmFluidTank> tanks, FluidType type) {
        FluidType normalizedType = normalize(type);
        int lowest = HbmFluidUser.HIGHEST_VALID_PRESSURE;
        int highest = 0;
        if (tanks != null) {
            for (HbmFluidTank tank : tanks) {
                if (tank != null && tank.getTankType() == normalizedType) {
                    lowest = Math.min(lowest, tank.getPressure());
                    highest = Math.max(highest, tank.getPressure());
                }
            }
        }
        return lowest <= highest ? new int[] {lowest, highest} : HbmFluidUser.DEFAULT_PRESSURE_RANGE.clone();
    }

    private static List<TankInspection> inspectTanks(Iterable<HbmFluidTank> tanks, FluidType type, int pressure) {
        List<TankInspection> details = new ArrayList<>();
        if (tanks == null) {
            return details;
        }
        int index = 0;
        for (HbmFluidTank tank : tanks) {
            if (tank != null) {
                details.add(new TankInspection(index, tank.getTankType(), tank.getPressure(), tank.getFill(),
                        tank.getMaxFill(), tank.getSpace(), tank.getTankType() == type,
                        tank.getTankType() == type && tank.getPressure() == pressure));
            }
            index++;
        }
        return details;
    }

    private static List<HbmFluidTank> asList(Iterable<HbmFluidTank> tanks) {
        List<HbmFluidTank> list = new ArrayList<>();
        if (tanks != null) {
            for (HbmFluidTank tank : tanks) {
                if (tank != null) {
                    list.add(tank);
                }
            }
        }
        return list;
    }

    private static int countMatching(List<HbmFluidTank> tanks, FluidType type, int pressure) {
        int matching = 0;
        for (HbmFluidTank tank : tanks) {
            if (matches(tank, type, pressure)) {
                matching++;
            }
        }
        return matching;
    }

    private static boolean matches(HbmFluidTank tank, FluidType type, int pressure) {
        return tank != null && tank.getTankType() == type && tank.getPressure() == pressure;
    }

    private static int[] fills(List<HbmFluidTank> tanks) {
        int[] fills = new int[tanks.size()];
        for (int i = 0; i < tanks.size(); i++) {
            fills[i] = tanks.get(i).getFill();
        }
        return fills;
    }

    private static int acceptIntoTank(HbmFluidTank tank, int[] virtualFill, int index, int amount, FluidType type,
            int pressure, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, Math.max(0, tank.getMaxFill() - virtualFill[index]));
        if (!simulate && accepted > 0) {
            accepted = tank.fillReport(type, accepted, pressure, false).movedMb();
        }
        virtualFill[index] += accepted;
        return accepted;
    }

    private static FluidType normalize(FluidType type) {
        return type == null ? HbmFluids.NONE : type;
    }

    public record TankSetInspection(
            FluidType type,
            int pressure,
            int matchingTanks,
            long demandMb,
            long availableMb,
            int minPressure,
            int maxPressure,
            List<TankInspection> tanks) {
        public TankSetInspection {
            tanks = tanks == null ? List.of() : List.copyOf(tanks);
        }
    }

    public record TankInspection(
            int index,
            FluidType type,
            int pressure,
            int fillMb,
            int capacityMb,
            int spaceMb,
            boolean matchesType,
            boolean matchesTypeAndPressure) {
    }

    public record TankTransferReport(
            FluidType type,
            int pressure,
            long requestedMb,
            long movedMb,
            long remainderMb,
            boolean simulated,
            int matchingTanks,
            List<TankTransferDetail> details) {
        public TankTransferReport {
            details = details == null ? List.of() : List.copyOf(details);
        }

        public static TankTransferReport empty(FluidType type, int pressure, long requestedMb, boolean simulated) {
            return new TankTransferReport(normalize(type), pressure, Math.max(0L, requestedMb), 0L,
                    Math.max(0L, requestedMb), simulated, 0, List.of());
        }

        public boolean moved() {
            return movedMb > 0L;
        }
    }

    public record TankTransferDetail(
            int index,
            FluidType type,
            int pressure,
            int fillBeforeMb,
            int fillAfterMb,
            int capacityMb,
            int movedMb,
            HbmFluidTank.TankState beforeState,
            HbmFluidTank.TankState afterState) {
        public TankTransferDetail {
            beforeState = beforeState == null ? new HbmFluidTank.TankState(type, fillBeforeMb, capacityMb, pressure) : beforeState;
            afterState = afterState == null ? new HbmFluidTank.TankState(type, fillAfterMb, capacityMb, pressure) : afterState;
        }

        private static TankTransferDetail forTank(int index, HbmFluidTank tank, int fillBeforeMb, int fillAfterMb,
                int movedMb) {
            HbmFluidTank.TankState before = new HbmFluidTank.TankState(
                    tank.getTankType(), fillBeforeMb, tank.getMaxFill(), tank.getPressure());
            HbmFluidTank.TankState after = new HbmFluidTank.TankState(
                    tank.getTankType(), fillAfterMb, tank.getMaxFill(), tank.getPressure());
            return new TankTransferDetail(index, tank.getTankType(), tank.getPressure(), fillBeforeMb, fillAfterMb,
                    tank.getMaxFill(), movedMb, before, after);
        }

        public boolean changed() {
            return beforeState.type() != afterState.type()
                    || beforeState.fillMb() != afterState.fillMb()
                    || beforeState.capacityMb() != afterState.capacityMb()
                    || beforeState.pressure() != afterState.pressure();
        }
    }
}
