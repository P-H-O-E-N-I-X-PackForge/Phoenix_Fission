package net.phoenix.phoenix_fission.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.phoenix.phoenix_fission.PhoenixFission;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Draws the shader Tokamak: a box round the ring that the shader marches through from the camera. The ring is forty
 * blocks across, so it is drawn from the level stage, with the matrices set here, as the vertex version is.
 */
public final class TokamakVolume {

    private TokamakVolume() {}

    /** The Tokamak shader: see fx_tokamak.fsh. Null until the shaders have loaded, or if they failed to. */
    public static ShaderInstance SHADER;

    /** How much of the full look to draw, 0.3 to 1, lowered by {@link #adapt} when the frame rate is low. */
    private static float adaptive = 1f;
    private static int lastFps = -1;

    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    "phoenix_fission:fx_tokamak", DefaultVertexFormat.POSITION), s -> SHADER = s);
        } catch (IOException e) {
            PhoenixFission.LOGGER.error("[Tokamak] Failed to register the plasma shader: {}", e.getMessage());
        }
    }

    public static void adapt(int fps) {
        if (fps == lastFps || fps <= 0) return;
        lastFps = fps;
        if (fps < 40) adaptive = Math.max(0.3f, adaptive * 0.85f);
        else if (fps > 58) adaptive = Math.min(1f, adaptive * 1.08f);
    }

    public static float quality() {
        return adaptive;
    }

    /** Sets the view and projection of the frame, and returns what puts them back. */
    public static Runnable stageMatrices(Matrix4f view, Matrix4f projection) {
        PoseStack stack = RenderSystem.getModelViewStack();
        stack.pushPose();
        stack.last().pose().set(view);
        RenderSystem.applyModelViewMatrix();
        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting oldSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(new Matrix4f(projection), VertexSorting.DISTANCE_TO_ORIGIN);
        return () -> {
            stack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
        };
    }

    /**
     * @param m        the pose that puts block-local coordinates relative to the camera
     * @param c        the middle of the box, in blocks
     * @param h        half its size
     * @param detail   the player's setting, 0.3 to 1
     * @param uniforms sets the shader's own inputs, after the common ones are in
     */
    public static void draw(ShaderInstance shader, Matrix4f m, Vector3f c, Vector3f h, float detail,
                            Consumer<ShaderInstance> uniforms) {
        Vector3f centre = m.transformPosition(c.x, c.y, c.z, new Vector3f());
        RenderSystem.setShader(() -> shader);
        shader.safeGetUniform("Scale").set(1f);
        shader.safeGetUniform("Detail").set(Math.max(0.3f, Math.min(1f, detail * adaptive)));
        shader.safeGetUniform("Centre").set(centre.x, centre.y, centre.z);
        shader.safeGetUniform("BoxHalf").set(h.x, h.y, h.z);
        uniforms.accept(shader);

        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        float x0 = c.x - h.x, x1 = c.x + h.x, y0 = c.y - h.y, y1 = c.y + h.y, z0 = c.z - h.z, z1 = c.z + h.z;
        face(bb, m, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0);
        face(bb, m, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        face(bb, m, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        face(bb, m, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0);
        face(bb, m, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        face(bb, m, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1);
        additive(shader, bb.end());
    }

    private static void face(BufferBuilder bb, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        bb.vertex(m, ax, ay, az).endVertex();
        bb.vertex(m, bx, by, bz).endVertex();
        bb.vertex(m, cx, cy, cz).endVertex();
        bb.vertex(m, dx, dy, dz).endVertex();
    }

    /**
     * Blends here, not in the shader's json: the game caches a json blend and skips it if unchanged, which leaves later
     * frames opaque once a draw has turned blending off. apply() alone does not fill in the matrices either;
     * setDefaultUniforms does.
     */
    private static void additive(ShaderInstance shader, BufferBuilder.RenderedBuffer mesh) {
        // the shader reads the depth itself and stops its rays at the first thing in the way, so no depth test
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        shader.setSampler("DepthSampler", SceneDepth.textureId());
        if (shader.MODEL_VIEW_MATRIX != null) shader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
        if (shader.PROJECTION_MATRIX != null) shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        shader.apply();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        BufferUploader.draw(mesh);
        shader.clear();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }
}
