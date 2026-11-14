package net.phoenix.phoenix_fission.api.machine.multiblock;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** The Tokamaks this client has loaded, so their plasma can be drawn whether or not their controller is on screen. */
public final class TokamakTracker {

    private static final Set<FusionReactorMachine> ALL = Collections.newSetFromMap(new WeakHashMap<>());

    private TokamakTracker() {}

    public static void add(FusionReactorMachine machine) {
        ALL.add(machine);
    }

    public static void remove(FusionReactorMachine machine) {
        ALL.remove(machine);
    }

    public static Set<FusionReactorMachine> all() {
        return ALL;
    }
}
