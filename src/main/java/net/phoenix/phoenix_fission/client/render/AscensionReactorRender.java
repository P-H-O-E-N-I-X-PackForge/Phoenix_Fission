package net.phoenix.phoenix_fission.client.render;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.phoenix.phoenix_fission.api.machine.multiblock.AscensionReactorMachine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * The Ascension Reactor's ring of plasma in the gap between the fission core and the coils. Its thickness and
 * brightness
 * follow the plasma gauge, it wobbles as the field loses its grip, spokes of neutron light cross the gap in step with
 * the
 * flux, the gap warms to orange with the heat, and in the ascended phase the ring turns gold and shimmers. An event
 * tints it red.
 */
public class AscensionReactorRender extends DynamicRender<AscensionReactorMachine, AscensionReactorRender> {

    public static final AscensionReactorRender INSTANCE = new AscensionReactorRender();
    public static final MapCodec<AscensionReactorRender> CODEC = MapCodec.unit(INSTANCE);
    public static final DynamicRenderType<AscensionReactorMachine, AscensionReactorRender> TYPE = new DynamicRenderType<>(
            CODEC);

    private static final float[] PLASMA = { 0.85f, 0.45f, 1f };
    private static final float[] GOLD = { 1f, 0.86f, 0.4f };
    private static final float[] ALERT = { 1f, 0.3f, 0.3f };
    private static final float[] NEUTRON = { 0.45f, 0.75f, 1f };
    private static final float[] WARM = { 1f, 0.55f, 0.2f };
    private static final float GAP = 2.0f;

    private AscensionReactorRender() {}

    @Override
    public @NotNull DynamicRenderType<AscensionReactorMachine, AscensionReactorRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(AscensionReactorMachine machine, @NotNull Vec3 cameraPos) {
        return machine.isFormed();
    }

    @Override
    public void render(AscensionReactorMachine machine, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!machine.isFormed()) return;
        float t = (machine.getOffsetTimer() + partialTick) / 20f;
        Vec3 c = GlowDraw.chamber(machine.getFrontFacing(), machine.getBlockPos(), 4.0, 2.5);
        float flux = (float) (machine.flux() / 100.0);
        float heat = Mth.clamp((float) (machine.heatPercent() / 100.0), 0f, 1.2f);
        boolean idle = machine.phase() == AscensionReactorMachine.OFF;
        // switched off, the ring is still there as a faint glow, breathing slowly
        float plasma = idle ? 0.12f + 0.04f * Mth.sin(t * 1.5f) : (float) (machine.plasma() / 100.0);
        float stability = (float) (machine.stability() / 100.0);
        boolean ascended = machine.phase() == AscensionReactorMachine.ASCENDED;
        boolean event = machine.event() != AscensionReactorMachine.EVENT_NONE;
        float cx = (float) c.x;
        float cy = (float) c.y;
        float cz = (float) c.z;

        float[] col = ascended ? GlowDraw.lerp3(PLASMA, GOLD, 0.5f + 0.5f * Mth.sin(t * 2f)) : PLASMA;
        if (event) col = GlowDraw.lerp3(col, ALERT, 0.55f + 0.3f * Mth.sin(t * 9f));
        float wobble = (1f - stability) * 0.22f + (event ? 0.08f : 0f);

        poseStack.pushPose();
        Matrix4f m = poseStack.last().pose();
        GlowDraw.Cam cam = GlowDraw.Cam.now(machine.getBlockPos());
        VertexConsumer vc = buffer.getBuffer(GlowDraw.GLOW);

        // the gap warms with the heat: a low orange wash at the floor of it
        GlowDraw.ring(vc, m, cx, cy - 1.2f, cz, GlowDraw.X, GlowDraw.Z, GAP, 0.7f, 0f, Mth.TWO_PI, 48, WARM[0],
                WARM[1], WARM[2], 0.18f * heat);

        // spokes of neutron light across the gap, brighter and quicker with the flux
        int spokes = 12;
        for (int i = 0; i < spokes; i++) {
            float a = i * Mth.TWO_PI / spokes + t * 0.15f;
            float pulse = 0.5f + 0.5f * Mth.sin(t * (2f + 4f * flux) + i * 1.7f);
            float ca = Mth.cos(a);
            float sa = Mth.sin(a);
            GlowDraw.ribbon(vc, m, cam, cx + ca * 1.55f, cy + (i % 3 - 1) * 0.6f, cz + sa * 1.55f, cx + ca * 2.4f,
                    cy + (i % 3 - 1) * 0.6f, cz + sa * 2.4f, 0.05f, 0.05f, NEUTRON[0], NEUTRON[1], NEUTRON[2],
                    0.5f * flux * pulse, 0.05f * flux);
        }

        // the plasma: three stacked rings, thicker as the plasma climbs, wobbling as the field slips
        if (plasma > 0.02f) {
            float width = 0.18f + 0.55f * plasma;
            for (int layer = -1; layer <= 1; layer++) {
                float y = cy + layer * 0.36f;
                float a = (0.2f + 0.45f * plasma) * (layer == 0 ? 1f : 0.55f);
                int segs = 96;
                for (int i = 0; i < segs; i++) {
                    float a0 = Mth.TWO_PI * i / segs;
                    float a1 = Mth.TWO_PI * (i + 1) / segs;
                    float w0 = GAP + Mth.sin(a0 * 3f + t * 4f + layer) * wobble;
                    float w1 = GAP + Mth.sin(a1 * 3f + t * 4f + layer) * wobble;
                    float shimmer = ascended ? 0.7f + 0.3f * Mth.sin(a0 * 12f - t * 8f) : 1f;
                    GlowDraw.ribbon(vc, m, cam, cx + Mth.cos(a0) * w0, y, cz + Mth.sin(a0) * w0, cx + Mth.cos(a1) * w1,
                            y, cz + Mth.sin(a1) * w1, width, width, col[0], col[1], col[2], a * shimmer, a * shimmer);
                }
            }
            // a few bright knots running round it
            for (int k = 0; k < 4; k++) {
                float a = t * (0.9f + 0.5f * plasma) + k * Mth.HALF_PI;
                GlowDraw.disc(vc, m, cam, cx + Mth.cos(a) * GAP, cy, cz + Mth.sin(a) * GAP, 0.16f + 0.1f * plasma, 1f,
                        1f,
                        1f, 0.8f * plasma, 0f);
            }
        }
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(AscensionReactorMachine m) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(AscensionReactorMachine m) {
        return new AABB(m.getBlockPos()).inflate(8);
    }
}
