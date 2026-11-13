package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;
import net.phoenix.phoenix_fission.api.block.FusionCoilBlock;
import net.phoenix.phoenix_fission.client.gui.AscensionGraphsPage;
import net.phoenix.phoenix_fission.client.gui.AscensionHistory;
import net.phoenix.phoenix_fission.client.gui.AscensionHud;
import net.phoenix.phoenix_fission.client.gui.CanvasWidget;
import net.phoenix.phoenix_fission.client.gui.FissionGraphsPage;
import net.phoenix.phoenix_fission.client.gui.FissionMathPage;
import net.phoenix.phoenix_fission.client.gui.FissionPanelUi;
import net.phoenix.phoenix_fission.client.gui.FissionReactorHud;
import net.phoenix.phoenix_fission.client.gui.FissionTabbedScreen;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

import java.util.List;
import java.util.Random;

/**
 * The Ascension Reactor: the real fission reactor of this mod (its fuel rods, moderators, blankets and coolers, its
 * heat
 * model and coolant, its fuel recipes and SCRAM hatches), standing inside a ring of fusion coils, with a puzzle laid on
 * top that has to be kept up.
 *
 * <p>
 * Three gauges are read from the real systems: FLUX is how much of the time the core is really burning, HEAT is the
 * real core heat as a share of what the vessel can take, and PLASMA is the field of the fusion ring, which takes its
 * strength from the coils that are actually built and its fuel from the gas actually fed. Each is pushed by a seeded
 * disturbance. The rods set how long the core burns, the coils and the injection set the plasma, and the plasma feeds
 * back into the flux and the heat. Hold all three in their bands to climb through ignition, coupling, sustain and
 * ascension. Let the field stability reach zero, or press SCRAM, and the climb is lost and the core is held scrammed
 * while it cools off.
 */
public class AscensionReactorMachine extends FissionWorkableElectricMultiblockMachine {

    public static final int UI_WIDTH = 264;
    public static final int UI_HEIGHT = 272;

    public static final int OFF = 0;
    public static final int IGNITION = 1;
    public static final int COUPLING = 2;
    public static final int SUSTAIN = 3;
    public static final int ASCENDED = 4;

    /** Seconds spent in the bands that finish each phase. */
    public static final int[] PHASE_SECONDS = { 0, 20, 30, 90, 0 };
    /** Bands, in percent: flux, heat (of the most the vessel can take), plasma. */
    public static final double[][] BANDS = { { 40, 70 }, { 30, 65 }, { 45, 80 } };
    public static final String[] GAUGES = { "FLUX", "HEAT", "PLASMA" };

    public static final int EVENT_NONE = 0;
    public static final int EVENT_COOLANT = 1;
    public static final int EVENT_SPIKE = 2;
    public static final int EVENT_KINK = 3;
    public static final String[] EVENT_NAMES = { "none", "COOLANT LOSS", "FLUX SPIKE", "PLASMA KINK" };
    private static final int EVENT_LENGTH = 12;
    private static final int EVENT_WARNING = 5;

    /** The most energy a tick the fusion ring adds on top of what the fission core makes. */
    private static final long MAX_FUSION_EUT = 4_194_304L;
    private static final long BASE_RING_UPKEEP = 4096L;
    private static final int COOLDOWN_SECONDS = 30;
    /** The coils the ring is meant to have, for the field to be at full strength. */
    private static final int FULL_RING_COILS = 36;

    // the puzzle's own state
    @SaveField
    @SyncToClient
    private int fluxX10;

    @SaveField
    @SyncToClient
    private int heatPctX10;

    @SaveField
    @SyncToClient
    private int plasmaX10;

    @SaveField
    @SyncToClient
    private int stabX10 = 1000;

    @SaveField
    @SyncToClient
    private int phase = OFF;

    @SaveField
    @SyncToClient
    private int phaseTimer;

    @SaveField
    @SyncToClient
    private int rod = 50;

    @SaveField
    @SyncToClient
    private int coil = 50;

    @SaveField
    @SyncToClient
    private int inject = 50;

    @SaveField
    @SyncToClient
    private int runSeed;

    @SaveField
    @SyncToClient
    private int runSeconds;

    @SaveField
    @SyncToClient
    private int event;

    @SaveField
    @SyncToClient
    private int eventLeft;

    @SaveField
    @SyncToClient
    private int nextEventIn = 40;

    @SaveField
    @SyncToClient
    private int warnKind;

    @SaveField
    @SyncToClient
    private int cooldown;

    @SaveField
    @SyncToClient
    private int ascensions;

    @SaveField
    @SyncToClient
    private int scrams;

    @SyncToClient
    private boolean ringPowered = true;

    @SyncToClient
    private boolean gasOk = true;

    @SyncToClient
    private int fusionEut;

    @SyncToClient
    private int ringCoils;

    private double fluxAvg;
    private double plasma;
    private double stability = 100.0;
    private boolean puzzleLoaded;
    private double duty = 0.5;
    private boolean burningNow;
    private long ringUpkeep = BASE_RING_UPKEEP;
    private double ringStrength = 1.0;
    private long currentFusionEut;

    public AscensionReactorMachine(BlockEntityCreationInfo info, Object... args) {
        super(info);
    }

    // ------------------------------------------------------------ the shared model

    /** The disturbance on gauge {@code kind} (0 flux, 1 heat, 2 plasma) at second {@code t}, for a run's seed. */
    public static double disturbance(int seed, int kind, double t, double amplitude) {
        Random r = new Random(seed * 1_000_003L + kind * 7919L);
        double a1 = 6.0 + r.nextDouble() * 4.0;
        double p1 = 21.0 + r.nextDouble() * 24.0;
        double f1 = r.nextDouble() * Math.PI * 2.0;
        double a2 = 3.0 + r.nextDouble() * 3.0;
        double p2 = 9.0 + r.nextDouble() * 9.0;
        double f2 = r.nextDouble() * Math.PI * 2.0;
        return amplitude * (a1 * Math.sin(Math.PI * 2.0 * t / p1 + f1) + a2 * Math.sin(Math.PI * 2.0 * t / p2 + f2));
    }

    /** How strong the disturbances are in a phase. */
    public static double amplitudeOf(int phase) {
        return phase >= ASCENDED ? 1.6 : phase == SUSTAIN ? 1.4 : 1.0;
    }

    // ------------------------------------------------------------ getters for the screen

    public double flux() {
        return fluxX10 / 10.0;
    }

    public double heatPercent() {
        return heatPctX10 / 10.0;
    }

    public double plasma() {
        return plasmaX10 / 10.0;
    }

    public double stability() {
        return stabX10 / 10.0;
    }

    public double gauge(int kind) {
        return kind == 0 ? flux() : kind == 1 ? heatPercent() : plasma();
    }

    public int phase() {
        return phase;
    }

    public int phaseTimer() {
        return phaseTimer;
    }

    public int control(int which) {
        return which == 0 ? rod : which == 1 ? coil : inject;
    }

    public int runSeed() {
        return runSeed;
    }

    public int runSeconds() {
        return runSeconds;
    }

    public int event() {
        return event;
    }

    public int eventLeft() {
        return eventLeft;
    }

    public int nextEventIn() {
        return nextEventIn;
    }

    public int warnKind() {
        return warnKind;
    }

    public int cooldown() {
        return cooldown;
    }

    public int ascensions() {
        return ascensions;
    }

    public int scrams() {
        return scrams;
    }

    public boolean isRingPowered() {
        return ringPowered;
    }

    public boolean isGasOk() {
        return gasOk;
    }

    public int fusionEut() {
        return fusionEut;
    }

    public int ringCoils() {
        return ringCoils;
    }

    /** The real reactor's output plus the fusion ring's. */
    public long totalEut() {
        return lastGeneratedEUt + fusionEut;
    }

    @Override
    public boolean isScramActive() {
        return super.isScramActive() || cooldown > 0;
    }

    // ------------------------------------------------------------ controls

    public static final int C_ROD = 0;
    public static final int C_COIL = 1;
    public static final int C_INJECT = 2;
    public static final int C_SCRAM = 3;

    public void applyControl(int which, int value) {
        int v = Mth.clamp(value, 0, 100);
        switch (which) {
            case C_ROD -> {
                rod = v;
                getSyncDataHolder().markClientSyncFieldDirty("rod");
            }
            case C_COIL -> {
                coil = v;
                getSyncDataHolder().markClientSyncFieldDirty("coil");
            }
            case C_INJECT -> {
                inject = v;
                getSyncDataHolder().markClientSyncFieldDirty("inject");
            }
            case C_SCRAM -> trip();
            default -> {}
        }
    }

    // ------------------------------------------------------------ the real reactor, steered

    /** The real reactor's own decision, without the rods or a trip. */
    private boolean baseShouldRun() {
        return super.shouldRunReactor();
    }

    /**
     * The control rods: the real reactor only burns for the share of each second the rods leave it, so the rods set
     * how hot it runs and how much fuel it eats.
     */
    @Override
    protected boolean shouldRunReactor() {
        if (!baseShouldRun() || cooldown > 0 || phase == OFF) {
            burningNow = false;
            return false;
        }
        int window = 20;
        int onTicks = (int) Math.round(window * duty);
        burningNow = (getOffsetTimer() % window) < onTicks;
        return burningNow;
    }

    @Override
    protected void reactorTick() {
        super.reactorTick();
        if (getLevel() == null || getLevel().isClientSide || !isFormed()) return;
        loadPuzzle();

        // the flux is how much of the time the core has really been burning, smoothed over a few seconds
        fluxAvg += ((burningNow ? 1.0 : 0.0) - fluxAvg) * 0.02;

        // the ring's upkeep and its extra output, every tick
        long upkeep = phase >= COUPLING ? ringUpkeep + (long) (coil * 64L + inject * 32L) : 0L;
        boolean powered = upkeep == 0 || getEnergyContainer().getEnergyStored() >= upkeep;
        if (powered && upkeep > 0) getEnergyContainer().changeEnergy(-upkeep);
        ringPowered = powered;
        if (currentFusionEut > 0 && powered) getEnergyContainer().addEnergy(currentFusionEut);

        if (getOffsetTimer() % 20 == 0) {
            second();
            publish();
        }
    }

    private void loadPuzzle() {
        if (puzzleLoaded) return;
        puzzleLoaded = true;
        fluxAvg = fluxX10 / 1000.0;
        plasma = plasmaX10 / 10.0;
        stability = stabX10 / 10.0;
    }

    /** Count the ring's coils, and what they cost and give, from the blocks that are really there. */
    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName) || getLevel() == null || getLevel().isClientSide) return;
        int coils = 0;
        long upkeep = BASE_RING_UPKEEP;
        for (long packed : getDefaultPatternState().getCache().keySet()) {
            var block = getLevel().getBlockState(BlockPos.of(packed)).getBlock();
            if (block instanceof FusionCoilBlock fusion) {
                coils++;
                upkeep += Math.max(0L, fusion.getCoilType().getUpkeepEut());
            } else if (block instanceof CoilBlock gt) {
                coils++;
                upkeep += 256L * (gt.coilType.getTier() + 1);
            }
        }
        ringCoils = coils;
        ringUpkeep = upkeep;
        ringStrength = Mth.clamp(0.5 + 0.5 * coils / (double) FULL_RING_COILS, 0.5, 1.0);
        getSyncDataHolder().markClientSyncFieldDirty("ringCoils");
    }

    /** The reactor trips: the climb is lost, the core is held scrammed, and it has to cool off. */
    private void trip() {
        if (phase == ASCENDED) ascensions++;
        scrams++;
        phase = OFF;
        phaseTimer = 0;
        event = EVENT_NONE;
        eventLeft = 0;
        warnKind = EVENT_NONE;
        cooldown = COOLDOWN_SECONDS;
        plasma = 0;
        stability = 100.0;
        currentFusionEut = 0;
        burningNow = false;
        for (String f : new String[] { "phase", "phaseTimer", "event", "eventLeft", "warnKind", "cooldown", "scrams",
                "ascensions" }) {
            getSyncDataHolder().markClientSyncFieldDirty(f);
        }
    }

    /** Take a second's gas for the plasma from the fluid input hatches. */
    private boolean feedGas() {
        var cfg = PhoenixFissionConfigs.INSTANCE.fission.fusion;
        int amount = (int) Math.round(cfg.pressureGasPerSecondMb * inject / 100.0);
        if (amount <= 0) return true;
        ResourceLocation id = ResourceLocation.tryParse(cfg.pressureGasFluidId);
        var fluid = id == null ? null : BuiltInRegistries.FLUID.getOptional(id).orElse(null);
        if (fluid == null) return true;
        FluidStack gas = new FluidStack(fluid, amount);
        return HatchIO.fluid(this, gas, IO.IN, true) && HatchIO.fluid(this, gas, IO.IN, false);
    }

    private void second() {
        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) {
                runSeed = getLevel().random.nextInt();
                runSeconds = 0;
                nextEventIn = 40;
                getSyncDataHolder().markClientSyncFieldDirty("runSeed");
            }
            duty = 0.0;
            currentFusionEut = 0;
            return;
        }
        if (phase == OFF) {
            // the climb starts the moment the real reactor can run: fuel in the buses and rods in the core
            if (!baseShouldRun()) {
                duty = 0.5;
                return;
            }
            phase = IGNITION;
            runSeed = getLevel().random.nextInt();
            runSeconds = 0;
            getSyncDataHolder().markClientSyncFieldDirty("phase");
            getSyncDataHolder().markClientSyncFieldDirty("runSeed");
        }
        runSeconds++;

        double amp = amplitudeOf(phase);
        double t = runSeconds;
        double dFlux = disturbance(runSeed, 0, t, amp);
        double dHeat = disturbance(runSeed, 1, t, amp);
        double dPlasma = disturbance(runSeed, 2, t, amp);
        double dHeatStep = dHeat - disturbance(runSeed, 1, t - 1, amp);

        // events, in the later phases
        if (phase >= SUSTAIN) {
            if (eventLeft > 0) {
                if (--eventLeft == 0) event = EVENT_NONE;
            } else {
                if (nextEventIn > 0) nextEventIn--;
                if (nextEventIn == EVENT_WARNING && warnKind == EVENT_NONE) {
                    warnKind = 1 + getLevel().random.nextInt(3);
                }
                if (nextEventIn == 0 && warnKind != EVENT_NONE) {
                    event = warnKind;
                    eventLeft = EVENT_LENGTH;
                    warnKind = EVENT_NONE;
                    nextEventIn = (phase == ASCENDED ? 50 : 30) + getLevel().random.nextInt(20);
                }
            }
        }

        // the rods and the disturbance decide how long the real core burns; the plasma gives some neutrons back
        double want = (100 - rod) + dFlux + 0.25 * plasma + (event == EVENT_SPIKE ? 22 : 0);
        duty = Mth.clamp(want / 100.0, 0.0, 1.0);

        // the heat disturbance is real heat, in or out of the real core, and a coolant loss is real heat too
        double capacity = getMaxSafeHeatHU();
        double pushed = dHeatStep * 0.01 * capacity + (event == EVENT_COOLANT ? 0.02 * capacity : 0.0);
        setHeat(Math.max(0.0, getHeat() + pushed));

        double flux = fluxAvg * 100.0;
        double heatPct = capacity <= 0 ? 0 : getHeat() / capacity * 100.0;

        // the ring: its strength comes from the coils that are built, its fuel from the gas that is fed
        gasOk = feedGas();
        if (phase >= COUPLING && ringPowered && flux >= 35.0) {
            double gas = gasOk ? 1.0 : 0.3;
            double target = (0.65 * coil + 0.35 * inject * gas) * 0.95 * ringStrength -
                    0.35 * Math.max(0.0, heatPct - 60.0) + dPlasma - (event == EVENT_KINK ? 18 : 0);
            plasma = Mth.clamp(plasma + 0.25 * (target - plasma), 0, 100);
        } else {
            plasma = Mth.clamp(plasma * 0.7, 0, 100);
        }

        boolean fluxIn = flux >= BANDS[0][0] && flux <= BANDS[0][1];
        boolean heatIn = heatPct >= BANDS[1][0] && heatPct <= BANDS[1][1];
        boolean plasmaIn = plasma >= BANDS[2][0] && plasma <= BANDS[2][1];
        boolean needsPlasma = phase >= COUPLING;
        boolean allIn = fluxIn && heatIn && (!needsPlasma || plasmaIn);
        double miss = (fluxIn ? 0 : Math.max(BANDS[0][0] - flux, flux - BANDS[0][1])) +
                (heatIn ? 0 : Math.max(BANDS[1][0] - heatPct, heatPct - BANDS[1][1])) +
                (!needsPlasma || plasmaIn ? 0 : Math.max(BANDS[2][0] - plasma, plasma - BANDS[2][1]));
        if (allIn) stability = Math.min(100.0, stability + 1.5);
        else stability -= 0.8 + miss * 0.25;
        if (stability <= 0.0) {
            trip();
            return;
        }

        if (phase < ASCENDED) {
            phaseTimer = allIn && stability >= 50.0 ? phaseTimer + 1 : Math.max(0, phaseTimer - 1);
            if (phaseTimer >= PHASE_SECONDS[phase]) {
                phase++;
                phaseTimer = 0;
                getSyncDataHolder().markClientSyncFieldDirty("phase");
            }
        }

        // what the ring adds to the real reactor's output
        double quality = phase >= COUPLING ? plasma / 100.0 : 0.0;
        currentFusionEut = (long) (MAX_FUSION_EUT * quality * (stability / 100.0) * (phase == ASCENDED ? 2.0 : 1.0));
        // and in the ascended phase the real reactor's own output is doubled too
        if (phase == ASCENDED && lastGeneratedEUt > 0 && fusionBonusReady()) {
            currentFusionEut += lastGeneratedEUt;
        }
    }

    private boolean fusionBonusReady() {
        return ringPowered && plasma >= BANDS[2][0];
    }

    private void publish() {
        fluxX10 = (int) Math.round(fluxAvg * 1000.0);
        double capacity = getMaxSafeHeatHU();
        heatPctX10 = (int) Math.round(Mth.clamp(capacity <= 0 ? 0 : getHeat() / capacity * 100.0, 0, 200) * 10.0);
        plasmaX10 = (int) Math.round(plasma * 10.0);
        stabX10 = (int) Math.round(stability * 10.0);
        fusionEut = (int) Math.min(Integer.MAX_VALUE, currentFusionEut);
        for (String f : new String[] { "fluxX10", "heatPctX10", "plasmaX10", "stabX10", "phaseTimer", "runSeconds",
                "event", "eventLeft", "nextEventIn", "warnKind", "cooldown", "ringPowered", "gasOk", "fusionEut" }) {
            getSyncDataHolder().markClientSyncFieldDirty(f);
        }
    }

    // ------------------------------------------------------------ the screen

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        syncManager.registerServerSyncedAction("ascension_control", buf -> {
            int which = buf.readVarInt();
            int value = buf.readVarInt();
            applyControl(which, value);
        });
        boolean client = getLevel() != null && getLevel().isClientSide;
        FissionTabbedScreen screen = null;
        if (client) {
            var history = new AscensionHistory();
            AscensionHud.Controls controls = (which, value) -> syncManager.callSyncedAction("ascension_control",
                    buf -> {
                        buf.writeVarInt(which);
                        buf.writeVarInt(value);
                    });
            screen = new FissionTabbedScreen(List.of(
                    new FissionTabbedScreen.Tab("A", new AscensionHud(this, history, controls)),
                    new FissionTabbedScreen.Tab("R", new FissionReactorHud(this)),
                    new FissionTabbedScreen.Tab("M", new FissionMathPage(this)),
                    new FissionTabbedScreen.Tab("~", new AscensionGraphsPage(this, history)),
                    new FissionTabbedScreen.Tab("G", new FissionGraphsPage(this))));
        }
        final FissionTabbedScreen finalScreen = screen;
        var panel = MachineUIPanelBuilder.panelBuilder(this).attachInventory(false).addTitleBar(false)
                .drawGTLogo(false)
                .mainContents(main -> {
                    if (finalScreen != null) main.child(new CanvasWidget(finalScreen).size(UI_WIDTH, UI_HEIGHT));
                    FissionPanelUi.addInventory(main, UI_WIDTH, UI_HEIGHT);
                }).build(syncManager, settings);
        if (finalScreen != null) FissionPanelUi.apply(panel, finalScreen);
        return panel;
    }
}
