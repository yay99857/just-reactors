package com.justreactors.turbine.client;

import com.justreactors.JustReactors;
import com.justreactors.turbine.block.TurbineControllerBlock;
import com.justreactors.turbine.blockentity.TurbineControllerBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Renders the rotating central shaft, machined hub collars, aerodynamic blades, and glowing
 * electromagnetic induction rings inside an assembled Steam Turbine, plus an emissive controller overlay.
 */
public class TurbineRotorRenderer implements BlockEntityRenderer<TurbineControllerBlockEntity, TurbineRotorRenderer.State> {
    private static final int FULL_BRIGHT = LightCoordsUtil.pack(15, 15);

    public TurbineRotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static final class State extends BlockEntityRenderState {
        boolean spinning;
        boolean active;
        double rpm;
        float timeSec;
        float bearingRelX;
        float bearingRelY;
        float bearingRelZ;
        Direction inwardDir = Direction.UP;
        Direction controllerFacing = Direction.NORTH;
        int shaftLength;
        float angleDeg;
        int[] bladeOffsets = new int[0];
        @Nullable
        TextureAtlasSprite shaftSprite;
        @Nullable
        TextureAtlasSprite bladeSprite;
        @Nullable
        TextureAtlasSprite pulseSprite;
        @Nullable
        TextureAtlasSprite controllerOnSprite;
        int light;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public AABB getRenderBoundingBox(TurbineControllerBlockEntity be) {
        BlockPos min = be.getClientMin();
        BlockPos max = be.getClientMax();
        if (be.isClientAssembled() && min != null && max != null) {
            return new AABB(min.getX(), min.getY(), min.getZ(),
                    max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0);
        }
        return BlockEntityRenderer.super.getRenderBoundingBox(be);
    }

    @Override
    public void extractRenderState(TurbineControllerBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        BlockPos bearing = be.getClientBearingPos();
        boolean assembled = be.isClientAssembled();
        boolean spinning = assembled && be.isClientSpinning() && bearing != null && be.getClientShaftLength() > 0;
        boolean active = assembled && be.isClientActive();
        if (!spinning && !active) {
            state.spinning = false;
            state.active = false;
            return;
        }

        state.spinning = spinning;
        state.active = active;
        state.rpm = be.getClientRpm();
        state.timeSec = be.getLevel() != null ? ((be.getLevel().getGameTime() % 24000L) + partialTicks) / 20.0F : 0.0F;

        BlockState blockState = be.getBlockState();
        state.controllerFacing = blockState.hasProperty(TurbineControllerBlock.FACING)
                ? blockState.getValue(TurbineControllerBlock.FACING)
                : Direction.NORTH;

        TextureAtlas blocks = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        state.controllerOnSprite = blocks.getSprite(JustReactors.id("block/turbine_controller_on"));

        if (spinning) {
            BlockPos ctrlPos = be.getBlockPos();
            state.bearingRelX = bearing.getX() - ctrlPos.getX();
            state.bearingRelY = bearing.getY() - ctrlPos.getY();
            state.bearingRelZ = bearing.getZ() - ctrlPos.getZ();
            state.inwardDir = be.getClientInwardDir();
            state.shaftLength = be.getClientShaftLength();
            state.angleDeg = be.getInterpolatedRotorAngle(partialTicks);
            state.bladeOffsets = be.getClientBladeOffsets();
            state.shaftSprite = blocks.getSprite(JustReactors.id("block/turbine_rotor_shaft"));
            state.bladeSprite = blocks.getSprite(JustReactors.id("block/turbine_rotor_blade"));
            state.pulseSprite = blocks.getSprite(JustReactors.id("block/reactor_pulse_glow"));
            state.light = LightCoordsUtil.pack(
                    Math.max(12, LightCoordsUtil.block(state.lightCoords)),
                    Math.max(12, LightCoordsUtil.sky(state.lightCoords))
            );
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.spinning && !state.active) {
            return;
        }

        if (state.spinning && state.shaftSprite != null && state.bladeSprite != null) {
            poseStack.pushPose();
            poseStack.translate(state.bearingRelX + 0.5F, state.bearingRelY + 0.5F, state.bearingRelZ + 0.5F);

            Direction.Axis axis = state.inwardDir.getAxis();
            switch (axis) {
                case X -> poseStack.rotateDegrees(Axis.XP, state.angleDeg);
                case Y -> poseStack.rotateDegrees(Axis.YP, state.angleDeg);
                case Z -> poseStack.rotateDegrees(Axis.ZP, state.angleDeg);
            }

            TextureAtlasSprite shaftSprite = state.shaftSprite;
            TextureAtlasSprite bladeSprite = state.bladeSprite;
            int light = state.light;

            collector.submitCustomGeometry(poseStack, RenderTypes.entitySolid(shaftSprite.atlasLocation()), (pose, consumer) -> {
                // 1. Shaft segments + machined hub collars from step=1..shaftLength along inwardDir
                for (int step = 1; step <= state.shaftLength; step++) {
                    float cx = state.inwardDir.getStepX() * step;
                    float cy = state.inwardDir.getStepY() * step;
                    float cz = state.inwardDir.getStepZ() * step;

                    float rx = axis == Direction.Axis.X ? 0.5F : 0.25F;
                    float ry = axis == Direction.Axis.Y ? 0.5F : 0.25F;
                    float rz = axis == Direction.Axis.Z ? 0.5F : 0.25F;
                    drawTexturedBox(pose, consumer, shaftSprite, 0xFFFFFFFF, light,
                            cx - rx, cy - ry, cz - rz,
                            cx + rx, cy + ry, cz + rz);

                    float hx = axis == Direction.Axis.X ? 0.16F : 0.32F;
                    float hy = axis == Direction.Axis.Y ? 0.16F : 0.32F;
                    float hz = axis == Direction.Axis.Z ? 0.16F : 0.32F;
                    drawTexturedBox(pose, consumer, shaftSprite, 0xFFE6EEF6, light,
                            cx - hx, cy - hy, cz - hz,
                            cx + hx, cy + hy, cz + hz);
                }

                // 2. Slim aerodynamic rotor blades radiating around the shaft
                int[] blades = state.bladeOffsets;
                for (int i = 0; i + 2 < blades.length; i += 3) {
                    float bx = blades[i];
                    float by = blades[i + 1];
                    float bz = blades[i + 2];

                    float dx = axis == Direction.Axis.X ? 0.0F : bx;
                    float dy = axis == Direction.Axis.Y ? 0.0F : by;
                    float dz = axis == Direction.Axis.Z ? 0.0F : bz;

                    float hx;
                    float hy;
                    float hz;
                    if (axis == Direction.Axis.Y) {
                        hy = 0.08F;
                        if (Math.abs(dx) >= Math.abs(dz)) {
                            hx = 0.5F;
                            hz = 0.3125F;
                        } else {
                            hx = 0.3125F;
                            hz = 0.5F;
                        }
                    } else if (axis == Direction.Axis.X) {
                        hx = 0.08F;
                        if (Math.abs(dy) >= Math.abs(dz)) {
                            hy = 0.5F;
                            hz = 0.3125F;
                        } else {
                            hy = 0.3125F;
                            hz = 0.5F;
                        }
                    } else {
                        hz = 0.08F;
                        if (Math.abs(dx) >= Math.abs(dy)) {
                            hx = 0.5F;
                            hy = 0.3125F;
                        } else {
                            hx = 0.3125F;
                            hy = 0.5F;
                        }
                    }

                    drawTexturedBox(pose, consumer, bladeSprite, 0xFFFFFFFF, light,
                            bx - hx, by - hy, bz - hz,
                            bx + hx, by + hy, bz + hz);
                }
            });

            // 3. Glowing electromagnetic induction halo right below coil entry + kinetic shaft collar glow
            if (state.rpm > 50.0 && state.pulseSprite != null) {
                TextureAtlasSprite pulseSprite = state.pulseSprite;
                int maxBladeStep = computeMaxBladeStep(state.bladeOffsets, state.inwardDir, state.shaftLength);
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentCull(pulseSprite.atlasLocation()), (pose, consumer) -> {
                    float rpmFrac = (float) Math.min(1.0, state.rpm / 900.0);
                    float pulse = 0.5F + 0.5F * Mth.sin(state.timeSec * 4.5F);
                    int alpha = Math.min(255, Math.round((0.38F + 0.37F * pulse) * rpmFrac * 255.0F));
                    int r = Math.round(Mth.lerp(pulse, 0x52, 0xFF));
                    int g = Math.round(Mth.lerp(pulse, 0xE4, 0xE4));
                    int b = Math.round(Mth.lerp(pulse, 0xFF, 0x7A));
                    int haloColor = (alpha << 24) | (r << 16) | (g << 8) | b;

                    float haloStep = maxBladeStep + 0.44F;
                    drawRing(pose, consumer, pulseSprite, haloColor, axis,
                            state.inwardDir.getStepX() * haloStep,
                            state.inwardDir.getStepY() * haloStep,
                            state.inwardDir.getStepZ() * haloStep,
                            0.34F, 0.68F, 0.05F);

                    int collarAlpha = Math.min(255, Math.round(0.32F * rpmFrac * 255.0F));
                    int collarColor = (collarAlpha << 24) | 0x5CE8FF;
                    for (int step = 1; step <= maxBladeStep; step++) {
                        drawRing(pose, consumer, pulseSprite, collarColor, axis,
                                state.inwardDir.getStepX() * step,
                                state.inwardDir.getStepY() * step,
                                state.inwardDir.getStepZ() * step,
                                0.33F, 0.38F, 0.055F);
                    }
                });
            }

            poseStack.popPose();
        }

        if (state.active && state.controllerOnSprite != null) {
            TextureAtlasSprite ctrlSprite = state.controllerOnSprite;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentCull(ctrlSprite.atlasLocation()), (pose, consumer) ->
                    drawControllerScreenGlow(pose, consumer, ctrlSprite, state.controllerFacing));
        }
    }

    private static int computeMaxBladeStep(int[] bladeOffsets, Direction inwardDir, int shaftLength) {
        int maxStep = 0;
        for (int i = 0; i + 2 < bladeOffsets.length; i += 3) {
            int step = bladeOffsets[i] * inwardDir.getStepX()
                    + bladeOffsets[i + 1] * inwardDir.getStepY()
                    + bladeOffsets[i + 2] * inwardDir.getStepZ();
            if (step > maxStep) {
                maxStep = step;
            }
        }
        return maxStep > 0 ? maxStep : Math.max(1, shaftLength - 1);
    }

    private static void drawRing(PoseStack.Pose p, VertexConsumer c, TextureAtlasSprite sprite,
                                 int color, Direction.Axis axis, float cx, float cy, float cz,
                                 float rIn, float rOut, float halfT) {
        switch (axis) {
            case Y -> {
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - rOut, cy - halfT, cz - rOut, cx + rOut, cy + halfT, cz - rIn);
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - rOut, cy - halfT, cz + rIn, cx + rOut, cy + halfT, cz + rOut);
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - rOut, cy - halfT, cz - rIn, cx - rIn, cy + halfT, cz + rIn);
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx + rIn, cy - halfT, cz - rIn, cx + rOut, cy + halfT, cz + rIn);
            }
            case X -> {
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - halfT, cy - rOut, cz - rOut, cx + halfT, cy + rOut, cz - rIn);
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - halfT, cy - rOut, cz + rIn, cx + halfT, cy + rOut, cz + rOut);
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - halfT, cy - rOut, cz - rIn, cx + halfT, cy - rIn, cz + rIn);
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - halfT, cy + rIn, cz - rIn, cx + halfT, cy + rOut, cz + rIn);
            }
            case Z -> {
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - rOut, cy - rOut, cz - halfT, cx + rOut, cy - rIn, cz + halfT);
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - rOut, cy + rIn, cz - halfT, cx + rOut, cy + rOut, cz + halfT);
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx - rOut, cy - rIn, cz - halfT, cx - rIn, cy + rIn, cz + halfT);
                drawTexturedBox(p, c, sprite, color, FULL_BRIGHT, cx + rIn, cy - rIn, cz - halfT, cx + rOut, cy + rIn, cz + halfT);
            }
        }
    }

    private static void drawControllerScreenGlow(PoseStack.Pose p, VertexConsumer c, TextureAtlasSprite sprite, Direction facing) {
        float min = 3.0F / 16.0F;
        float max = 13.0F / 16.0F;
        float depth = 0.72F / 16.0F;
        float u0 = Mth.lerp(min, sprite.getU0(), sprite.getU1());
        float u1 = Mth.lerp(max, sprite.getU0(), sprite.getU1());
        float v0 = Mth.lerp(min, sprite.getV0(), sprite.getV1());
        float v1 = Mth.lerp(max, sprite.getV0(), sprite.getV1());
        int color = 0xD8FFFFFF;

        switch (facing) {
            case SOUTH -> quad(p, c, color, FULL_BRIGHT, 0, 0, 1,
                    min, min, 1.0F - depth, u0, v1,
                    max, min, 1.0F - depth, u1, v1,
                    max, max, 1.0F - depth, u1, v0,
                    min, max, 1.0F - depth, u0, v0);
            case WEST -> quad(p, c, color, FULL_BRIGHT, -1, 0, 0,
                    depth, min, min, u0, v1,
                    depth, min, max, u1, v1,
                    depth, max, max, u1, v0,
                    depth, max, min, u0, v0);
            case EAST -> quad(p, c, color, FULL_BRIGHT, 1, 0, 0,
                    1.0F - depth, min, max, u0, v1,
                    1.0F - depth, min, min, u1, v1,
                    1.0F - depth, max, min, u1, v0,
                    1.0F - depth, max, max, u0, v0);
            default -> quad(p, c, color, FULL_BRIGHT, 0, 0, -1,
                    max, min, depth, u0, v1,
                    min, min, depth, u1, v1,
                    min, max, depth, u1, v0,
                    max, max, depth, u0, v0);
        }
    }

    private static void drawTexturedBox(PoseStack.Pose p, VertexConsumer c, TextureAtlasSprite sprite, int color, int light,
                                        float x0, float y0, float z0, float x1, float y1, float z1) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        quad(p, c, color, light, 0, 1, 0,
                x0, y1, z0, u0, v0, x0, y1, z1, u0, v1, x1, y1, z1, u1, v1, x1, y1, z0, u1, v0);
        quad(p, c, color, light, 0, -1, 0,
                x0, y0, z1, u0, v1, x0, y0, z0, u0, v0, x1, y0, z0, u1, v0, x1, y0, z1, u1, v1);
        quad(p, c, color, light, 0, 0, -1,
                x1, y0, z0, u1, v1, x0, y0, z0, u0, v1, x0, y1, z0, u0, v0, x1, y1, z0, u1, v0);
        quad(p, c, color, light, 0, 0, 1,
                x0, y0, z1, u0, v1, x1, y0, z1, u1, v1, x1, y1, z1, u1, v0, x0, y1, z1, u0, v0);
        quad(p, c, color, light, -1, 0, 0,
                x0, y0, z0, u0, v1, x0, y0, z1, u1, v1, x0, y1, z1, u1, v0, x0, y1, z0, u0, v0);
        quad(p, c, color, light, 1, 0, 0,
                x1, y0, z1, u1, v1, x1, y0, z0, u0, v1, x1, y1, z0, u0, v0, x1, y1, z1, u1, v0);
    }

    private static void quad(PoseStack.Pose p, VertexConsumer c, int color, int light, float nx, float ny, float nz,
                             float ax, float ay, float az, float au, float av,
                             float bx, float by, float bz, float bu, float bv,
                             float cx, float cy, float cz, float cu, float cv,
                             float dx, float dy, float dz, float du, float dv) {
        vertex(p, c, color, light, nx, ny, nz, ax, ay, az, au, av);
        vertex(p, c, color, light, nx, ny, nz, bx, by, bz, bu, bv);
        vertex(p, c, color, light, nx, ny, nz, cx, cy, cz, cu, cv);
        vertex(p, c, color, light, nx, ny, nz, dx, dy, dz, du, dv);
    }

    private static void vertex(PoseStack.Pose p, VertexConsumer c, int color, int light, float nx, float ny, float nz,
                               float x, float y, float z, float u, float v) {
        c.addVertex(p, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(p, nx, ny, nz);
    }
}
