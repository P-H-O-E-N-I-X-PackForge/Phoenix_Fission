package net.phoenix.phoenix_fission.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.phoenix.phoenix_fission.api.machine.multiblock.FusionReactorMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.TokamakTracker;
import net.phoenix.phoenix_fission.client.render.GlowDraw;
import net.phoenix.phoenix_fission.client.render.TokamakRender;

import java.util.ArrayList;

/**
 * Draws the Tokamak plasma after the level, for every loaded Tokamak within range. A block entity is drawn only while
 * its own block is in view, which a structure forty blocks across cannot rely on, so the plasma is drawn from here
 * instead and shows whenever the Tokamak is within render distance.
 */
public final class TokamakStageRenderer {

    private static final double RANGE = 192.0;

    private TokamakStageRenderer() {}

    public static void onStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        var all = TokamakTracker.all();
        if (all.isEmpty()) return;
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        boolean drew = false;
        for (FusionReactorMachine machine : new ArrayList<>(all)) {
            if (machine.getLevel() != mc.level || !machine.isFormed()) continue;
            var pos = machine.getBlockPos();
            if (Vec3.atCenterOf(pos).distanceToSqr(cam) > RANGE * RANGE) continue;
            var pose = event.getPoseStack();
            pose.pushPose();
            pose.translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
            TokamakRender.INSTANCE.draw(machine, partial, pose, buffer);
            pose.popPose();
            drew = true;
        }
        if (drew) buffer.endBatch(GlowDraw.GLOW);
    }
}
