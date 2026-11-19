package net.phoenix.phoenix_fission.gtm8.client.render;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.FusionReactorMachine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The plasma of the Tokamak Fusion Reactor, drawn the way a real one looks: a ring of hot gas in the vacuum vessel that
 * glows pink-magenta (the hydrogen lines) with a white-hot core and a cooler violet-blue edge, held in a twist of
 * helical field lines that run round the ring and drift along it. The ring follows the vacuum tube of the structure,
 * the rounded octagon of the racetrack, and is thin and tight when the field holds it and fat and unruly when it does
 * not. As the drift climbs it kinks, flares at the edge and shades to orange and red; a scram turns it red as it dies.
 */
public class TokamakRender extends DynamicRender<FusionReactorMachine, TokamakRender> {

    public static final TokamakRender INSTANCE = new TokamakRender();
    public static final Codec<TokamakRender> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<FusionReactorMachine, TokamakRender> TYPE = new DynamicRenderType<>(CODEC);

    private static final float[] HALO = { 0.50f, 0.32f, 1.0f };
    private static final float[] MID = { 1.0f, 0.26f, 0.68f };
    private static final float[] CORE = { 1.0f, 0.86f, 0.96f };
    private static final float[] FIELD = { 0.38f, 0.78f, 1.0f };
    private static final float[] WARM = { 1.0f, 0.58f, 0.2f };
    private static final float[] ALERT = { 1.0f, 0.22f, 0.18f };

    // where the middle of the vacuum tube is, from the controller: 23.5 blocks behind it and half a block to its right
    private static final float CENTRE_BACK = 23.5f;
    private static final float CENTRE_RIGHT = 0.5f;
    private static final float CENTRE_UP = 0.5f;

    private static final int N = 168;
    private static final float[] PATH_U = new float[N];
    private static final float[] PATH_V = new float[N];
    private static final float[] TAN_U = new float[N];
    private static final float[] TAN_V = new float[N];

    static {
        // the racetrack of the tube, a rounded octagon: right and front of its middle, in blocks
        float[][] octagon = { { -9.5f, -18.5f }, { -18.5f, -9.5f }, { -18.5f, 9.5f }, { -9.5f, 18.5f },
                { 9.5f, 18.5f }, { 18.5f, 9.5f }, { 18.5f, -9.5f }, { 9.5f, -18.5f } };
        float[][] cut = new float[16][];
        for (int i = 0; i < 8; i++) {
            float[] a = octagon[i];
            float[] b = octagon[(i + 1) % 8];
            cut[2 * i] = new float[] { Mth.lerp(0.15f, a[0], b[0]), Mth.lerp(0.15f, a[1], b[1]) };
            cut[2 * i + 1] = new float[] { Mth.lerp(0.85f, a[0], b[0]), Mth.lerp(0.85f, a[1], b[1]) };
        }
        float[] length = new float[17];
        for (int i = 0; i < 16; i++) {
            float[] a = cut[i];
            float[] b = cut[(i + 1) % 16];
            length[i + 1] = length[i] + Mth.sqrt((b[0] - a[0]) * (b[0] - a[0]) + (b[1] - a[1]) * (b[1] - a[1]));
        }
        int seg = 0;
        for (int i = 0; i < N; i++) {
            float at = length[16] * i / N;
            while (seg < 15 && length[seg + 1] < at) seg++;
            float f = (at - length[seg]) / Math.max(1.0e-4f, length[seg + 1] - length[seg]);
            float[] a = cut[seg];
            float[] b = cut[(seg + 1) % 16];
            PATH_U[i] = Mth.lerp(f, a[0], b[0]);
            PATH_V[i] = Mth.lerp(f, a[1], b[1]);
        }
        // one pass of smoothing takes the corners off
        float[] su = PATH_U.clone();
        float[] sv = PATH_V.clone();
        for (int i = 0; i < N; i++) {
            int p = (i + N - 1) % N;
            int q = (i + 1) % N;
            PATH_U[i] = 0.5f * su[i] + 0.25f * (su[p] + su[q]);
            PATH_V[i] = 0.5f * sv[i] + 0.25f * (sv[p] + sv[q]);
        }
        for (int i = 0; i < N; i++) {
            int p = (i + N - 1) % N;
            int q = (i + 1) % N;
            float du = PATH_U[q] - PATH_U[p];
            float dv = PATH_V[q] - PATH_V[p];
            float len = Mth.sqrt(du * du + dv * dv);
            TAN_U[i] = du / len;
            TAN_V[i] = dv / len;
        }
    }

    /**
     * What to draw instead of what the machine says, set by the /tokamak command so the look can be tuned without
     * running a reactor. A null value means "use the machine". It applies to every Tokamak the client can see.
     */
    public static final class Tune {

        public static volatile Float speed;
        public static volatile Float size;
        public static volatile Float power;
        public static volatile Float drift;
        public static volatile Float field;
        public static volatile Boolean scram;
        public static volatile float[] tint;
        public static volatile Theme theme;

        /** True to draw the ribbon-built plasma, as it does unless the shader one is asked for. */
        public static volatile boolean classic = true;

        /** How fine the shader's steps are, 0.3 to 1. */
        public static volatile float detail = 1f;

        private Tune() {}

        public static void reset() {
            speed = size = power = drift = field = null;
            scram = null;
            tint = null;
            theme = null;
            classic = true;
            detail = 1f;
        }
    }

    /** The thickest the plasma is allowed to be when the reactor sets it: wider and it leaves the tube. */
    public static final float NATURAL_SIZE = 0.6f;

    /**
     * Fusion recipes that only make the plasma pretty. A theme gives the ring a gradient of its own colours, and a
     * character of its own: Miku's teal shimmer, Teto's red drills, the phoenix of the Phoenix Forge logo flying round
     * the ring in a trail of fire. They are picked by recipe id: {@code tokamak_pretty_<name>}.
     */
    public enum Theme {

        NONE("", 0, 0, 0, 0, 0, 0),
        MIKU("miku", 0xFF73BF, 2f, 0.12f, 4, 6f, 1.05f, 0x1FCCC2, 0xCCFFFF),
        TETO("teto", 0xFFBFCC, 3f, 0.09f, 2, 15f, 1.5f, 0xC70A2E, 0xFF6B85),
        PFT("pft", 0xFFE6FF, 1f, 0.05f, 6, 5f, 1.1f, 0x5C14EB, 0xF538F5),

        // the rest of the cast, the same colours as /pftfx gives the other renders of PhoenixCore
        RIN("rin", 0xFFF3B0, 2f, 0.1f, 4, 6f, 1.05f, 0xFFC21F, 0xFF8A1F),
        LUKA("luka", 0xFFF0D0, 2f, 0.08f, 4, 6f, 1.05f, 0xFF8FB8, 0xFFD27A),
        KAITO("kaito", 0xFFFFFF, 2f, 0.09f, 4, 6f, 1.05f, 0x2A5CFF, 0xBFD9FF),
        MEIKO("meiko", 0xFFC9A0, 2f, 0.08f, 4, 6f, 1.05f, 0xB5121B, 0x7A3B22),
        GUMI("gumi", 0xE8FFD0, 2f, 0.1f, 4, 6f, 1.05f, 0x4FD34F, 0xFFA83F),

        OCEAN("ocean", 0xDFFFFF, 2f, 0.07f, 4, 6f, 1.05f, 0x0A3FB5, 0x18D6C8),
        TOXIC("toxic", 0xF4FFC0, 2f, 0.14f, 4, 6f, 1.05f, 0x2BD92B, 0xE0F52A),
        ICE("ice", 0xFFFFFF, 3f, 0.06f, 4, 6f, 1.05f, 0x9FD8FF, 0xFFFFFF),
        VOID("void", 0xD9B8FF, 1f, 0.04f, 4, 6f, 1.05f, 0x2A0E7A, 0x8A2BE2),
        BLOOD("blood", 0xFFB0B8, 2f, 0.11f, 4, 6f, 1.05f, 0x5E0008, 0xE01028),
        GOLD("gold", 0xFFFFFF, 3f, 0.08f, 4, 6f, 1.05f, 0xB8860B, 0xFFD700, 0xFFF1A8),
        EMBER("ember", 0xFFF0C0, 2f, 0.16f, 4, 6f, 1.05f, 0x8A1100, 0xFF4A00, 0xFFB000),
        SUNSET("sunset", 0xFFE0B0, 1f, 0.05f, 4, 6f, 1.05f, 0xFF7A1A, 0xFF3D7F, 0x7B2FBE),
        AURORA("aurora", 0xE0FFF4, 1f, 0.06f, 4, 6f, 1.05f, 0x1FE08A, 0x18B8D6, 0x8B5CF6),
        RAINBOW("rainbow", 0xFFFFFF, 1f, 0.08f, 6, 6f, 1.05f, 0xFF2D2D, 0xFF9A1F, 0xFFE81F, 0x2DDB4F, 0x2D8CFF,
                0x7A3DFF),
        TRANS("trans", 0xFFFFFF, 1f, 0.06f, 4, 6f, 1.05f, 0x5BCEFA, 0xF5A9B8, 0xFFFFFF, 0xF5A9B8);

        public final String key;
        final float[] a;
        final float[] b;
        final float[][] stops;
        final float cycles;
        final float speed;
        final float[] accent;
        final int strands;
        final float twist;
        final float radius;

        /** {@code colours} are two to run between, or three or more to go through and round again. */
        Theme(String key, int accent, float cycles, float speed, int strands, float twist, float radius,
              int... colours) {
            this.key = key;
            this.cycles = cycles;
            this.speed = speed;
            this.strands = strands;
            this.twist = twist;
            this.radius = radius;
            this.accent = colours.length == 0 ? null : rgb(accent);
            this.stops = colours.length > 2 ? new float[colours.length][] : null;
            if (colours.length == 0) {
                this.a = null;
                this.b = null;
                return;
            }
            this.a = rgb(colours[0]);
            this.b = rgb(colours[1]);
            if (stops != null) for (int i = 0; i < colours.length; i++) stops[i] = rgb(colours[i]);
        }

        private static float[] rgb(int rgb) {
            return new float[] { (rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f };
        }

        /** The colour a place round the ring has at a time: {@code s} from 0 to 1 along it, {@code t} in seconds. */
        float[] colourAt(float s, float t) {
            if (stops != null) {
                float p = s * cycles - t * speed * 2f;
                p = (p - (float) Math.floor(p)) * stops.length;
                int i = Math.min(stops.length - 1, (int) p);
                float f = p - i;
                f = f * f * (3f - 2f * f);
                return GlowDraw.lerp3(stops[i], stops[(i + 1) % stops.length], f);
            }
            float g = 0.5f + 0.5f * Mth.sin(Mth.TWO_PI * (s * cycles - t * speed * 2f));
            return GlowDraw.lerp3(a, b, g);
        }

        /** The names of the themes, for the command. */
        public static java.util.List<String> keys() {
            var out = new java.util.ArrayList<String>();
            for (Theme t : values()) if (t != NONE) out.add(t.key);
            return out;
        }

        public static Theme byKey(String key) {
            for (Theme t : values()) if (t != NONE && t.key.equals(key)) return t;
            return null;
        }

        /** The theme of a recipe, from its id. */
        public static Theme of(GTRecipe recipe) {
            if (recipe == null || recipe.id == null) return NONE;
            String id = recipe.id.getPath();
            for (Theme t : values()) {
                if (t != NONE && id.contains("pretty_" + t.key)) return t;
            }
            return NONE;
        }
    }

    /**
     * Per reactor: how lit it is, so it fades up and down, how red a scram has made it, its own clock and its colour.
     */
    private static final class State {

        float glow;
        float scram;
        float phase;
        long last = -1L;
        float[] tint;
        Theme theme = Theme.NONE;
        long tintAt = -100000L;
    }

    private final Map<FusionReactorMachine, State> states = new WeakHashMap<>();

    /** The colour of the plasma a recipe makes, from its output fluid, or null if it makes none. */
    public static float[] tintOf(GTRecipe recipe) {
        if (recipe == null) return null;
        try {
            for (var content : recipe.getOutputContents(FluidRecipeCapability.CAP)) {
                for (FluidStack stack : FluidRecipeCapability.CAP.of(content.content()).getStacks()) {
                    if (stack.isEmpty()) continue;
                    int argb = IClientFluidTypeExtensions.of(stack.getFluid()).getTintColor(stack);
                    return new float[] { (argb >> 16 & 255) / 255f, (argb >> 8 & 255) / 255f, (argb & 255) / 255f };
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static float[] saturate(float[] c) {
        float max = Math.max(c[0], Math.max(c[1], c[2]));
        if (max < 0.01f) return new float[] { 1f, 0.3f, 0.7f };
        return new float[] { Math.min(1f, c[0] / max), Math.min(1f, c[1] / max), Math.min(1f, c[2] / max) };
    }

    private TokamakRender() {}

    /**
     * Whether the plasma is drawn by the shader, as it is unless the command asks for the old one or it did not load.
     */
    public static boolean useShader() {
        return TokamakVolume.SHADER != null && !Tune.classic;
    }

    /**
     * The plasma as a volume. Everything that decided the look of the ribbon version is handed to the shader as it
     * stands, so the colours, the themes, the thickness, the drift and a scram all mean what they meant before.
     */
    private void drawShader(PoseStack poseStack, float t, float power, float fat, float shake, float alarm, float drift,
                            float field, float[] mid, float[] halo, float[] core, float[] tint, float[] baseHalo,
                            Theme theme, boolean themed, float rx, float rz, float fx, float fz, float ox, float oy,
                            float oz) {
        float strands = themed ? theme.strands : 6f;
        float twist = themed ? theme.twist : 6f;
        float radius = themed ? theme.radius : 1.05f;
        float[] fieldColour = tint != null ? GlowDraw.lerp3(FIELD, baseHalo, 0.35f) : FIELD;
        // the box has to hold the ring, the strands, and everything that leaps off it
        float out = 3.4f * Math.max(fat, radius * fat) + 0.8f + shake * 1.5f;
        float halfXZ = 18.5f + out + 1f;
        float halfY = out + shake + 1f;
        float detail = Tune.detail;
        float seed = 0.37f;
        TokamakVolume.draw(TokamakVolume.SHADER, poseStack.last().pose(), new org.joml.Vector3f(ox, oy, oz),
                new org.joml.Vector3f(halfXZ, halfY, halfXZ), detail, s -> {
                    s.safeGetUniform("Time").set(t);
                    s.safeGetUniform("Power").set(power);
                    s.safeGetUniform("Seed").set(seed);
                    s.safeGetUniform("Right").set(rx, rz);
                    s.safeGetUniform("Front").set(fx, fz);
                    s.safeGetUniform("ColorA").set(mid[0], mid[1], mid[2]);
                    s.safeGetUniform("ColorB").set(core[0], core[1], core[2]);
                    s.safeGetUniform("ColorC").set(halo[0], halo[1], halo[2]);
                    s.safeGetUniform("ColorD").set(fieldColour[0], fieldColour[1], fieldColour[2]);
                    s.safeGetUniform("P0").set(fat, shake, alarm, drift);
                    s.safeGetUniform("P1").set(strands, twist, radius, field);
                    float count = 0f;
                    if (themed) {
                        count = theme.stops != null ? theme.stops.length : 2f;
                        float[][] stops = theme.stops != null ? theme.stops : new float[][] { theme.a, theme.b };
                        for (int i = 0; i < 6; i++) {
                            float[] c = stops[Math.min(i, stops.length - 1)];
                            s.safeGetUniform("Stop" + i).set(c[0], c[1], c[2]);
                        }
                    }
                    s.safeGetUniform("P2").set(themed ? theme.cycles : 1f, themed ? theme.speed : 0.1f, count,
                            theme == Theme.PFT ? 1f : 0f);
                });
    }

    @Override
    public @NotNull DynamicRenderType<FusionReactorMachine, TokamakRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(FusionReactorMachine machine, @NotNull Vec3 cameraPos) {
        return machine.isFormed();
    }

    /**
     * The plasma is drawn by {@link net.phoenix.phoenix_fission.gtm8.client.TokamakStageRenderer} once the level is drawn,
     * not here: a block entity is only drawn while its own block is inside the view of the camera, and this one is
     * forty blocks across, so it would flicker in and out as the controller left the screen.
     */
    @Override
    public void render(FusionReactorMachine machine, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    /** Draws the plasma. The pose is the machine's block corner. */
    public void draw(FusionReactorMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        if (!machine.isFormed()) return;
        State state = states.computeIfAbsent(machine, k -> new State());
        long now = System.nanoTime();
        float dt = state.last < 0 ? 0f : Math.min(0.1f, (now - state.last) / 1.0e9f);
        // a frame's worth of time passes once, however many times this is asked in it
        if (dt > 0.0005f || state.last < 0) {
            state.last = now;
            Float speedTune = Tune.speed;
            state.phase += dt * (speedTune != null ? speedTune : 1f);
            float want = machine.isPlasmaOn() ? 1f : machine.isPowered() ? 0.07f : 0f;
            Float powerTune = Tune.power;
            if (powerTune != null) want = powerTune;
            float k = want > state.glow ? Math.min(1f, dt * 2.7f) : Math.min(1f, dt * 1.5f);
            state.glow += (want - state.glow) * k;
            boolean scrammed = Tune.scram != null ? Tune.scram : machine.isScrammed();
            // a scram or a quench turns it red as it dies
            if (scrammed) state.scram = Math.min(1f, state.scram + dt * 4.8f);
            else state.scram = Math.max(0f, state.scram - dt * 1.2f);
        }
        float t = state.phase;
        float power = state.glow;
        if (power < 0.01f) return;

        // the colour follows the recipe that is running, read every so often
        long tick = machine.getOffsetTimer();
        if (tick - state.tintAt > 20 || tick < state.tintAt) {
            state.tintAt = tick;
            state.tint = tintOf(machine.getRecipeLogic().getLastRecipe());
            state.theme = Theme.of(machine.getRecipeLogic().getLastRecipe());
        }
        float[] tint = Tune.tint != null ? Tune.tint : state.tint;
        Theme theme = Tune.theme != null ? Tune.theme : state.theme;
        boolean themed = theme != Theme.NONE;

        float drift = Tune.drift != null ? Tune.drift : (float) (machine.getStabilityPercent() / 100.0);
        float field = Tune.field != null ? Tune.field : (float) Mth.clamp(machine.getFieldRatio(), 0.0, 2.0);
        // a field that holds makes the ring thin and tight; one that slips lets it swell and shake
        float loose = Mth.clamp(1.25f - field, 0f, 1f);
        // thinner when the field holds it, up to the size at its widest; the reactor never goes past NATURAL_SIZE
        float variation = Mth.clamp(loose * 0.7f + drift * 0.5f, 0f, 1f);
        float fat = (Tune.size != null ? Tune.size : NATURAL_SIZE) * (0.67f + 0.33f * variation);
        float shake = 0.05f + 0.45f * drift + 0.25f * loose;
        float alarm = Math.max(state.scram, Mth.clamp((drift - 0.6f) / 0.4f, 0f, 1f));

        float[] baseMid = MID;
        float[] baseHalo = HALO;
        float[] core = CORE;
        if (tint != null) {
            float[] hue = saturate(tint);
            baseMid = hue;
            baseHalo = GlowDraw.lerp3(hue, HALO, 0.45f);
            core = GlowDraw.lerp3(CORE, hue, 0.25f);
        }
        float[] mid = GlowDraw.lerp3(baseMid, WARM, Mth.clamp(drift * 1.2f, 0f, 1f));
        float[] halo = GlowDraw.lerp3(baseHalo, WARM, Mth.clamp(drift * 0.9f, 0f, 1f));
        if (alarm > 0f) {
            float flicker = 0.5f + 0.5f * Mth.sin(t * 11f);
            float k = alarm * (0.4f + 0.4f * flicker);
            mid = GlowDraw.lerp3(mid, ALERT, k);
            halo = GlowDraw.lerp3(halo, ALERT, k);
            core = GlowDraw.lerp3(core, WARM, k);
        }

        Direction front = machine.getFrontFacing();
        Direction right = machine.isFlipped() ? front.getCounterClockWise() : front.getClockWise();
        float fx = front.getStepX();
        float fz = front.getStepZ();
        float rx = right.getStepX();
        float rz = right.getStepZ();
        float ox = 0.5f + rx * CENTRE_RIGHT - fx * CENTRE_BACK;
        float oz = 0.5f + rz * CENTRE_RIGHT - fz * CENTRE_BACK;
        float oy = CENTRE_UP;

        if (useShader()) {
            drawShader(poseStack, t, power, fat, shake, alarm, drift, field, mid, halo, core, tint, baseHalo, theme,
                    themed, rx, rz, fx, fz, ox, oy, oz);
            return;
        }

        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();
        GlowDraw.Cam cam = GlowDraw.Cam.now(machine.getBlockPos());
        VertexConsumer vc = buffer.getBuffer(GlowDraw.GLOW);

        double dx = cam.px() - ox;
        double dz = cam.pz() - oz;
        int step = dx * dx + dz * dz > 80.0 * 80.0 ? 2 : 1;
        int n = N / step;

        // the centre line of the ring, kinked by the drift
        float[] cx = new float[n];
        float[] cy = new float[n];
        float[] cz = new float[n];
        float[] bu = new float[n];
        float[] bv = new float[n];
        for (int k = 0; k < n; k++) {
            int i = k * step;
            float s = i / (float) N;
            float kinkSide = shake * (Mth.sin(s * Mth.TWO_PI * 3f + t * 1.7f) * 0.6f +
                    Mth.sin(s * Mth.TWO_PI * 7f - t * 2.9f) * 0.4f);
            float kinkUp = shake * 0.8f * Mth.sin(s * Mth.TWO_PI * 5f + t * 2.3f);
            // the side of the tube, across the way it runs
            bu[k] = -TAN_V[i];
            bv[k] = TAN_U[i];
            float u = PATH_U[i] + bu[k] * kinkSide;
            float v = PATH_V[i] + bv[k] * kinkSide;
            cx[k] = ox + rx * u + fx * v;
            cz[k] = oz + rz * u + fz * v;
            cy[k] = oy + kinkUp;
        }

        // the halo, the mid glow and the white-hot core: three soft strips along the ring that face the camera
        float[] a = new float[n];
        float[] haloC = halo;
        float[] midC = mid;
        float[] coreC = core;
        float alarmK = alarm > 0f ? alarm * (0.4f + 0.4f * (0.5f + 0.5f * Mth.sin(t * 11f))) : 0f;
        if (themed) {
            // a gradient of the theme's own colours running round the ring
            haloC = new float[3 * n];
            midC = new float[3 * n];
            coreC = new float[3 * n];
            for (int k = 0; k < n; k++) {
                float s = (k * step) / (float) N;
                float[] c = theme.colourAt(s, t);
                float[] h = GlowDraw.lerp3(c, new float[] { 0.5f, 0.35f, 1f }, 0.3f);
                float[] w = GlowDraw.lerp3(CORE, c, 0.3f);
                if (alarmK > 0f) {
                    c = GlowDraw.lerp3(c, ALERT, alarmK);
                    h = GlowDraw.lerp3(h, ALERT, alarmK);
                    w = GlowDraw.lerp3(w, WARM, alarmK);
                }
                System.arraycopy(h, 0, haloC, 3 * k, 3);
                System.arraycopy(c, 0, midC, 3 * k, 3);
                System.arraycopy(w, 0, coreC, 3 * k, 3);
            }
        }
        java.util.Arrays.fill(a, 0.1f * power);
        strip(vc, m, cam, cx, cy, cz, n, true, 3.2f * fat, haloC, a);
        for (int k = 0; k < n; k++) {
            float flick = 0.88f + 0.12f * Mth.sin(t * 9f + k * 0.45f);
            a[k] = 0.24f * power * flick;
        }
        strip(vc, m, cam, cx, cy, cz, n, true, 1.9f * fat, midC, a);
        for (int k = 0; k < n; k++) {
            float flick = 0.85f + 0.15f * Mth.sin(t * 13f + k * 0.6f);
            a[k] = 0.62f * power * flick;
        }
        strip(vc, m, cam, cx, cy, cz, n, true, 0.8f * fat, coreC, a);

        // the phoenix of the logo: a bright head flying round the ring, trailing fire that goes from magenta to purple
        if (theme == Theme.PFT) {
            float head = (t * 0.07f) % 1f;
            float[] tailC = new float[3 * n];
            float[] tailA = new float[n];
            for (int k = 0; k < n; k++) {
                float behind = (head - (k * step) / (float) N + 1f) % 1f;
                float f = behind < 0.32f ? 1f - behind / 0.32f : 0f;
                float[] c = GlowDraw.lerp3(theme.a, new float[] { 1f, 0.95f, 0.9f }, f * f);
                System.arraycopy(c, 0, tailC, 3 * k, 3);
                tailA[k] = 0.75f * f * f * power;
            }
            strip(vc, m, cam, cx, cy, cz, n, true, 1.4f * fat, tailC, tailA);
        }

        // the helical field lines: six strands twisting round the ring, drifting along it
        if (power > 0.1f) {
            int strands = themed ? theme.strands : 6;
            float twistTurns = themed ? theme.twist : 6f;
            float radius = (themed ? theme.radius : 1.05f) * fat;
            float strength = Mth.clamp(field * 0.55f, 0.15f, 1f) * power;
            float[] fieldColour = alarm > 0f ? GlowDraw.lerp3(FIELD, ALERT, alarm * 0.7f) : FIELD;
            if (tint != null) fieldColour = GlowDraw.lerp3(fieldColour, baseHalo, 0.35f);
            if (themed) fieldColour = GlowDraw.lerp3(theme.b, theme.accent, 0.4f);
            float[] sx = new float[n + 1];
            float[] sy = new float[n + 1];
            float[] sz = new float[n + 1];
            float[] sa = new float[n + 1];
            for (int strand = 0; strand < strands; strand++) {
                float phase = strand * Mth.TWO_PI / strands;
                for (int k = 0; k <= n; k++) {
                    int c = k % n;
                    float s = (k * step) / (float) N;
                    float twist = phase + s * Mth.TWO_PI * twistTurns + t * 1.6f;
                    float side = Mth.cos(twist) * radius;
                    float lift = Mth.sin(twist) * radius;
                    sx[k] = cx[c] + (rx * bu[c] + fx * bv[c]) * side;
                    sz[k] = cz[c] + (rz * bu[c] + fz * bv[c]) * side;
                    sy[k] = cy[c] + lift;
                    // the side facing the camera is the brighter one
                    sa[k] = (0.14f + 0.3f * (0.5f + 0.5f * Mth.sin(twist))) * strength;
                }
                strip(vc, m, cam, sx, sy, sz, n + 1, false, 0.16f * Math.max(0.6f, fat * 0.6f), fieldColour, sa);
            }
        }

        // bright knots running round the ring
        if (power > 0.2f) {
            for (int k = 0; k < 4; k++) {
                float s = (t * (0.035f + 0.02f * field) + k * 0.25f) % 1f;
                int c = (int) (s * n) % n;
                float[] knot = themed ? theme.accent : new float[] { 1f, 0.92f, 0.98f };
                GlowDraw.disc(vc, m, cam, cx[c], cy[c], cz[c], (0.5f + 0.3f * Mth.sin(t * 6f + k)) * fat, knot[0],
                        knot[1], knot[2], 0.7f * power, 0f);
            }
        }

        // flares at the edge: short wisps leaping off the plasma, more and longer as the drift climbs
        if (power > 0.3f) {
            int wisps = 3 + (int) (drift * 8f) + (theme == Theme.PFT ? 7 : 0);
            int bucket = (int) (t * 4f);
            float[] wx = new float[2];
            float[] wy = new float[2];
            float[] wz = new float[2];
            float[] wa = new float[2];
            for (int w = 0; w < wisps; w++) {
                float seed = GlowDraw.noise(w, bucket);
                int c = (int) (GlowDraw.noise(w + 40, bucket) * n) % n;
                float life = (t * 4f) % 1f;
                float angle = seed * Mth.TWO_PI;
                float reach = (1.0f + 1.6f * GlowDraw.noise(w + 80, bucket)) * (0.6f + drift) * Math.max(0.7f, fat);
                wx[0] = cx[c];
                wy[0] = cy[c];
                wz[0] = cz[c];
                wx[1] = cx[c] + (rx * bu[c] + fx * bv[c]) * Mth.cos(angle) * reach;
                wz[1] = cz[c] + (rz * bu[c] + fz * bv[c]) * Mth.cos(angle) * reach;
                wy[1] = cy[c] + Mth.sin(angle) * reach;
                float fade = (1f - life) * power;
                wa[0] = 0.5f * fade;
                wa[1] = 0f;
                strip(vc, m, cam, wx, wy, wz, 2, false, 0.22f, themed && theme == Theme.PFT ? theme.accent : mid, wa);
            }
        }
        poseStack.popPose();
    }

    /**
     * A soft strip along a path that faces the camera: full brightness down its middle and none at its edges, with its
     * sides worked out at each point from the way the path runs there, so the pieces join without the hard-edged
     * rectangles a strip made of separate segments shows.
     */
    private static void strip(VertexConsumer vc, Matrix4f m, GlowDraw.Cam cam, float[] x, float[] y, float[] z, int n,
                              boolean closed, float width, float[] c, float[] alpha) {
        // c is one colour, or a colour for each point
        int cs = c.length >= 3 * n && n > 1 ? 3 : 0;
        float[] sx = new float[n];
        float[] sy = new float[n];
        float[] sz = new float[n];
        float half = width / 2f;
        for (int i = 0; i < n; i++) {
            int p = closed ? (i + n - 1) % n : Math.max(0, i - 1);
            int q = closed ? (i + 1) % n : Math.min(n - 1, i + 1);
            float tx = x[q] - x[p];
            float ty = y[q] - y[p];
            float tz = z[q] - z[p];
            float vx = x[i] - cam.px();
            float vy = y[i] - cam.py();
            float vz = z[i] - cam.pz();
            float ex = ty * vz - tz * vy;
            float ey = tz * vx - tx * vz;
            float ez = tx * vy - ty * vx;
            float len = Mth.sqrt(ex * ex + ey * ey + ez * ez);
            if (len < 1.0e-5f) {
                // looking straight along the path: keep the side of the point before it
                ex = i > 0 ? sx[i - 1] : 1f;
                ey = i > 0 ? sy[i - 1] : 0f;
                ez = i > 0 ? sz[i - 1] : 0f;
            } else {
                ex /= len;
                ey /= len;
                ez /= len;
            }
            sx[i] = ex;
            sy[i] = ey;
            sz[i] = ez;
        }
        int segs = closed ? n : n - 1;
        for (int i = 0; i < segs; i++) {
            int j = (i + 1) % n;
            float ai = alpha[i];
            float aj = alpha[j];
            float r0 = c[cs * i];
            float g0 = c[cs * i + 1];
            float b0 = c[cs * i + 2];
            float r1 = c[cs * j];
            float g1 = c[cs * j + 1];
            float b1 = c[cs * j + 2];
            // the left half, then the right half, each fading from the middle to nothing at the edge
            GlowDraw.vtx(vc, m, x[i] - sx[i] * half, y[i] - sy[i] * half, z[i] - sz[i] * half, r0, g0, b0, 0f);
            GlowDraw.vtx(vc, m, x[i], y[i], z[i], r0, g0, b0, ai);
            GlowDraw.vtx(vc, m, x[j], y[j], z[j], r1, g1, b1, aj);
            GlowDraw.vtx(vc, m, x[j] - sx[j] * half, y[j] - sy[j] * half, z[j] - sz[j] * half, r1, g1, b1, 0f);
            GlowDraw.vtx(vc, m, x[i], y[i], z[i], r0, g0, b0, ai);
            GlowDraw.vtx(vc, m, x[i] + sx[i] * half, y[i] + sy[i] * half, z[i] + sz[i] * half, r0, g0, b0, 0f);
            GlowDraw.vtx(vc, m, x[j] + sx[j] * half, y[j] + sy[j] * half, z[j] + sz[j] * half, r1, g1, b1, 0f);
            GlowDraw.vtx(vc, m, x[j], y[j], z[j], r1, g1, b1, aj);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(FusionReactorMachine m) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 192;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(FusionReactorMachine m) {
        return new AABB(m.getBlockPos()).inflate(64);
    }
}
