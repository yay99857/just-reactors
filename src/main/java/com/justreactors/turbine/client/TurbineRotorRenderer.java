package com.justreactors.turbine.client;

import com.justreactors.JustReactors;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Renders the rotating central shaft and aerodynamic blades inside an assembled Steam Turbine
 * at an angular speed driven by the live rotor RPM.
 */
public class TurbineRotorRenderer implements BlockEntityRenderer<TurbineControllerBlockEntity, TurbineRotorRenderer.State> {
    public TurbineRotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static final class State extends BlockEntityRenderState {
        boolean spinning;
        float bearingRelX;
        float bearingRelY;
        float bearingRelZ;
        Direction inwardDir = Direction.UP;
        int shaftLength;
        float angleDeg;
        int[] bladeOffsets = new int[0];
        @Nullable
        TextureAtlasSprite shaftSprite;
        @Nullable
        TextureAtlasSprite bladeSprite;
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
        if (!be.isClientAssembled() || !be.isClientSpinning() || bearing == null || be.getClientShaftLength() <= 0) {
            state.spinning = false;
            return;
        }

        state.spinning = true;
        BlockPos ctrlPos = be.getBlockPos();
        state.bearingRelX = bearing.getX() - ctrlPos.getX();
        state.bearingRelY = bearing.getY() - ctrlPos.getY();
        state.bearingRelZ = bearing.getZ() - ctrlPos.getZ();
        state.inwardDir = be.getClientInwardDir();
        state.shaftLength = be.getClientShaftLength();
        state.angleDeg = be.getInterpolatedRotorAngle(partialTicks);
        state.bladeOffsets = be.getClientBladeOffsets();

        TextureAtlas blocks = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        state.shaftSprite = blocks.getSprite(JustReactors.id("block/turbine_rotor_shaft"));
        state.bladeSprite = blocks.getSprite(JustReactors.id("block/turbine_rotor_blade"));
        state.light = LightCoordsUtil.pack(
                Math.max(11, LightCoordsUtil.block(state.lightCoords)),
                Math.max(11, LightCoordsUtil.sky(state.lightCoords))
        );
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.spinning || state.shaftSprite == null || state.bladeSprite == null) {
            return;
        }

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
            // 1. Shaft segments from step=1..shaftLength along inwardDir
            for (int step = 1; step <= state.shaftLength; step++) {
                float cx = state.inwardDir.getStepX() * step;
                float cy = state.inwardDir.getStepY() * step;
                float cz = state.inwardDir.getStepZ() * step;

                float rx = axis == Direction.Axis.X ? 0.5F : 0.25F;
                float ry = axis == Direction.Axis.Y ? 0.5F : 0.25F;
                float rz = axis == Direction.Axis.Z ? 0.5F : 0.25F;

                drawTexturedBox(pose, consumer, shaftSprite, light,
                        cx - rx, cy - ry, cz - rz,
                        cx + rx, cy + ry, cz + rz);
            }

            // 2. Slim aerodynamic rotor blades radiating around the shaft
            int[] blades = state.bladeOffsets;
            for (int i = 0; i + 2 < blades.length; i += 3) {
                float bx = blades[i];
                float by = blades[i + 1];
                float bz = blades[i + 2];

                // Distance from the shaft center line along the two perpendicular axes
                float dx = axis == Direction.Axis.X ? 0.0F : bx;
                float dy = axis == Direction.Axis.Y ? 0.0F : by;
                float dz = axis == Direction.Axis.Z ? 0.0F : bz;

                // Determine half-extents: slim 2.5px along shaft axis, 16px along primary radial arm, 10px tangential
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

                drawTexturedBox(pose, consumer, bladeSprite, light,
                        bx - hx, by - hy, bz - hz,
                        bx + hx, by + hy, bz + hz);
            }
        });

        poseStack.popPose();
    }

    private static void drawTexturedBox(PoseStack.Pose p, VertexConsumer c, TextureAtlasSprite sprite, int light,
                                        float x0, float y0, float z0, float x1, float y1, float z1) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // Top (+Y)
        quad(p, c, light, 0, 1, 0,
                x0, y1, z0, u0, v0, x0, y1, z1, u0, v1, x1, y1, z1, u1, v1, x1, y1, z0, u1, v0);
        // Bottom (-Y)
        quad(p, c, light, 0, -1, 0,
                x0, y0, z1, u0, v1, x0, y0, z0, u0, v0, x1, y0, z0, u1, v0, x1, y0, z1, u1, v1);
        // North (-Z)
        quad(p, c, light, 0, 0, -1,
                x1, y0, z0, u1, v1, x0, y0, z0, u0, v1, x0, y1, z0, u0, v0, x1, y1, z0, u1, v0);
        // South (+Z)
        quad(p, c, light, 0, 0, 1,
                x0, y0, z1, u0, v1, x1, y0, z1, u1, v1, x1, y1, z1, u1, v0, x0, y1, z1, u0, v0);
        // West (-X)
        quad(p, c, light, -1, 0, 0,
                x0, y0, z0, u0, v1, x0, y0, z1, u1, v1, x0, y1, z1, u1, v0, x0, y1, z0, u0, v0);
        // East (+X)
        quad(p, c, light, 1, 0, 0,
                x1, y0, z1, u1, v1, x1, y0, z0, u0, v1, x1, y1, z0, u0, v0, x1, y1, z1, u1, v0);
    }

    private static void quad(PoseStack.Pose p, VertexConsumer c, int light, float nx, float ny, float nz,
                             float ax, float ay, float az, float au, float av,
                             float bx, float by, float bz, float bu, float bv,
                             float cx, float cy, float cz, float cu, float cv,
                             float dx, float dy, float dz, float du, float dv) {
        vertex(p, c, light, nx, ny, nz, ax, ay, az, au, av);
        vertex(p, c, light, nx, ny, nz, bx, by, bz, bu, bv);
        vertex(p, c, light, nx, ny, nz, cx, cy, cz, cu, cv);
        vertex(p, c, light, nx, ny, nz, dx, dy, dz, du, dv);
    }

    private static void vertex(PoseStack.Pose p, VertexConsumer c, int light, float nx, float ny, float nz,
                               float x, float y, float z, float u, float v) {
        c.addVertex(p, x, y, z)
                .setColor(0xFFFFFFFF)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(p, nx, ny, nz);
    }
}
