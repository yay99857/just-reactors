package com.justreactors.client;

import com.justreactors.JustReactors;
import com.justreactors.block.ReactorControllerBlock;
import com.justreactors.blockentity.ReactorControllerBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
 * Renders live Uranium fuel (top) and Cyanite waste (bottom) inside each {@code ReactorFuelRodBlock}
 * with a high-contrast animated top-to-bottom fission pulse when active, plus an emissive controller overlay.
 */
public class ReactorFuelRodRenderer implements BlockEntityRenderer<ReactorControllerBlockEntity, ReactorFuelRodRenderer.State> {
    private static final float MIN_XZ = 3.6F / 16.0F;
    private static final float MAX_XZ = 12.4F / 16.0F;
    private static final float AURA_MIN_XZ = 3.35F / 16.0F;
    private static final float AURA_MAX_XZ = 12.65F / 16.0F;
    private static final float Y_MIN = 2.5F / 16.0F;
    private static final float CAVITY_H = 11.0F / 16.0F;
    private static final int SLICES = 8;
    private static final int FULL_BRIGHT = LightCoordsUtil.pack(15, 15);

    public ReactorFuelRodRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static final class State extends BlockEntityRenderState {
        boolean visible;
        boolean active;
        float fuelRatio;
        float wasteRatio;
        float timeSec;
        float activity;
        float fuelHeat;
        int interiorMinY;
        int interiorHeight;
        Direction controllerFacing = Direction.NORTH;
        int[] rodOffsets = new int[0];
        @Nullable
        TextureAtlasSprite fuelSprite;
        @Nullable
        TextureAtlasSprite wasteSprite;
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
    public AABB getRenderBoundingBox(ReactorControllerBlockEntity be) {
        BlockPos min = be.getClientMin();
        BlockPos max = be.getClientMax();
        if (be.isClientAssembled() && min != null && max != null) {
            return new AABB(min.getX(), min.getY(), min.getZ(),
                    max.getX() + 1.0, max.getY() + 1.0, max.getZ() + 1.0);
        }
        return BlockEntityRenderer.super.getRenderBoundingBox(be);
    }

    @Override
    public void extractRenderState(ReactorControllerBlockEntity be, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        int[] offsets = be.getClientFuelRodOffsets();
        float fuel = be.getClientFuelRatio();
        float waste = be.getClientWasteRatio();
        boolean active = be.isClientActive();
        if (!be.isClientAssembled() || offsets.length < 3 || (!active && fuel <= 0.005F && waste <= 0.005F)) {
            state.visible = false;
            return;
        }

        state.visible = true;
        state.active = active;
        state.fuelRatio = fuel;
        state.wasteRatio = waste;
        state.activity = be.getClientActivity();
        state.fuelHeat = be.getClientFuelHeat();
        state.rodOffsets = offsets;
        state.timeSec = be.getLevel() != null ? ((be.getLevel().getGameTime() % 24000L) + partialTicks) / 20.0F : 0.0F;

        BlockPos min = be.getClientMin();
        BlockPos max = be.getClientMax();
        BlockPos ctrlPos = be.getBlockPos();
        state.interiorMinY = min != null ? (min.getY() + 1 - ctrlPos.getY()) : 0;
        state.interiorHeight = (min != null && max != null)
                ? Math.max(1, (max.getY() - 1) - (min.getY() + 1) + 1)
                : 1;

        BlockState blockState = be.getBlockState();
        state.controllerFacing = blockState.hasProperty(ReactorControllerBlock.FACING)
                ? blockState.getValue(ReactorControllerBlock.FACING)
                : Direction.NORTH;

        TextureAtlas blocks = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        state.fuelSprite = blocks.getSprite(JustReactors.id("block/uranium_block"));
        state.wasteSprite = blocks.getSprite(JustReactors.id("block/cyanite_block"));
        state.pulseSprite = blocks.getSprite(JustReactors.id("block/reactor_pulse_glow"));
        state.controllerOnSprite = blocks.getSprite(JustReactors.id("block/reactor_controller_on"));

        int blockLight = state.active ? 11 : 6;
        state.light = LightCoordsUtil.pack(
                Math.max(blockLight, LightCoordsUtil.block(state.lightCoords)),
                Math.max(state.active ? 12 : 6, LightCoordsUtil.sky(state.lightCoords))
        );
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible || state.fuelSprite == null || state.wasteSprite == null) {
            return;
        }

        TextureAtlasSprite fuelSprite = state.fuelSprite;
        TextureAtlasSprite wasteSprite = state.wasteSprite;
        TextureAtlasSprite pulseSprite = state.pulseSprite != null ? state.pulseSprite : fuelSprite;
        float wasteFrac = Math.min(1.0F, Math.max(0.0F, state.wasteRatio));
        float fuelFrac = Math.min(1.0F - wasteFrac, Math.max(0.0F, state.fuelRatio));
        boolean pulsing = state.active && state.activity > 0.01F;
        float heatNorm = Mth.clamp((state.fuelHeat - 100.0F) / 1400.0F, 0.0F, 1.0F);
        float speed = 0.55F + 0.60F * state.activity + 0.35F * heatNorm;
        float interiorTopY = state.interiorMinY + state.interiorHeight;

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucentCull(fuelSprite.atlasLocation()), (pose, consumer) -> {
            int[] offsets = state.rodOffsets;

            if (!pulsing) {
                int fuelColor = state.active ? 0xE2B8F25A : 0xB078A83A;
                int wasteColor = 0xD44AC0F0;
                for (int i = 0; i + 2 < offsets.length; i += 3) {
                    float ox = offsets[i];
                    float oy = offsets[i + 1];
                    float oz = offsets[i + 2];
                    float curBottom = oy + Y_MIN;
                    if (wasteFrac > 0.005F) {
                        float h = CAVITY_H * wasteFrac;
                        drawBox(pose, consumer, wasteSprite, wasteColor, state.light,
                                ox + MIN_XZ, curBottom, oz + MIN_XZ,
                                ox + MAX_XZ, curBottom + h, oz + MAX_XZ,
                                0.0F, wasteFrac, true, fuelFrac <= 0.005F);
                        curBottom += h;
                    }
                    if (fuelFrac > 0.005F) {
                        float h = CAVITY_H * fuelFrac;
                        drawBox(pose, consumer, fuelSprite, fuelColor, state.light,
                                ox + MIN_XZ, curBottom, oz + MIN_XZ,
                                ox + MAX_XZ, curBottom + h, oz + MAX_XZ,
                                wasteFrac, wasteFrac + fuelFrac, wasteFrac <= 0.005F, true);
                    }
                }
            } else {
                float wasteBot = 0.0F;
                float wasteTop = wasteFrac;
                float fuelBot = wasteFrac;
                float fuelTop = wasteFrac + fuelFrac;

                for (int i = 0; i + 2 < offsets.length; i += 3) {
                    float ox = offsets[i];
                    float oy = offsets[i + 1];
                    float oz = offsets[i + 2];
                    float colPhase = (((Math.round(ox) * 7 + Math.round(oz) * 13) & 3) * 0.14F);

                    for (int s = 0; s < SLICES; s++) {
                        float s0 = (float) s / SLICES;
                        float s1 = (float) (s + 1) / SLICES;
                        float sliceMidY = oy + Y_MIN + ((s0 + s1) * 0.5F) * CAVITY_H;
                        float hNorm = Mth.clamp((interiorTopY - sliceMidY) / (float) state.interiorHeight, 0.0F, 1.0F);
                        float wave = (state.timeSec * speed + colPhase) % 1.0F;
                        float pulse = computePulse(wave, hNorm) * (0.45F + 0.55F * state.activity);

                        int sliceBlockLight = 7 + Math.round(8.0F * pulse);
                        int sliceLight = LightCoordsUtil.pack(sliceBlockLight, Math.max(sliceBlockLight, 10));

                        float w0 = Math.max(s0, wasteBot);
                        float w1 = Math.min(s1, wasteTop);
                        boolean hasWaste = (w1 - w0) > 0.002F;
                        if (hasWaste) {
                            int wasteColor = lerpColor(0xE8, 0x20, 0x7A, 0xB8, 0xB8, 0xF8, 0xFF, pulse);
                            drawBox(pose, consumer, wasteSprite, wasteColor, sliceLight,
                                    ox + MIN_XZ, oy + Y_MIN + w0 * CAVITY_H, oz + MIN_XZ,
                                    ox + MAX_XZ, oy + Y_MIN + w1 * CAVITY_H, oz + MAX_XZ,
                                    w0, w1, w0 <= wasteBot + 0.001F, (w1 >= wasteTop - 0.001F) && fuelFrac <= 0.005F);
                        }

                        float f0 = Math.max(s0, fuelBot);
                        float f1 = Math.min(s1, fuelTop);
                        boolean hasFuel = (f1 - f0) > 0.002F;
                        if (hasFuel) {
                            int baseR = 0x2E;
                            int baseG = 0x7A;
                            int baseB = 0x16;
                            if (hNorm > 0.82F && state.fuelHeat >= 150.0F) {
                                float cherenkov = Mth.clamp((hNorm - 0.82F) / 0.18F, 0.0F, 1.0F) * 0.35F;
                                baseR = Math.round(Mth.lerp(cherenkov, baseR, 0x3C));
                                baseG = Math.round(Mth.lerp(cherenkov, baseG, 0xD8));
                                baseB = Math.round(Mth.lerp(cherenkov, baseB, 0xFF));
                            }
                            int fuelColor = lerpColor(0xF0, baseR, baseG, baseB, 0xF2, 0xFF, 0xD4, pulse);
                            drawBox(pose, consumer, fuelSprite, fuelColor, sliceLight,
                                    ox + MIN_XZ, oy + Y_MIN + f0 * CAVITY_H, oz + MIN_XZ,
                                    ox + MAX_XZ, oy + Y_MIN + f1 * CAVITY_H, oz + MAX_XZ,
                                    f0, f1, (f0 <= fuelBot + 0.001F) && wasteFrac <= 0.005F, f1 >= fuelTop - 0.001F);
                        }

                        if (pulse > 0.12F && (hasFuel || hasWaste)) {
                            float a0 = hasWaste && !hasFuel ? w0 : hasFuel && !hasWaste ? f0 : Math.min(w0, f0);
                            float a1 = hasWaste && !hasFuel ? w1 : hasFuel && !hasWaste ? f1 : Math.max(w1, f1);
                            int auraAlpha = Math.min(240, Math.round(pulse * 0.88F * 255.0F));
                            int auraColor = hasFuel
                                    ? ((auraAlpha << 24) | 0xD4FF70)
                                    : ((auraAlpha << 24) | 0x7CF4FF);
                            drawBox(pose, consumer, pulseSprite, auraColor, FULL_BRIGHT,
                                    ox + AURA_MIN_XZ, oy + Y_MIN + a0 * CAVITY_H, oz + AURA_MIN_XZ,
                                    ox + AURA_MAX_XZ, oy + Y_MIN + a1 * CAVITY_H, oz + AURA_MAX_XZ,
                                    a0, a1, false, false);
                        }
                    }
                }
            }

            if (state.active && state.controllerOnSprite != null) {
                drawControllerScreenGlow(pose, consumer, state.controllerOnSprite, state.controllerFacing);
            }
        });
    }

    private static float computePulse(float wave, float hNorm) {
        float delta = (wave - hNorm + 1.0F) % 1.0F;
        if (delta < 0.28F) {
            return (float) Math.pow(1.0F - delta / 0.28F, 1.45);
        }
        if (delta > 0.90F) {
            return (float) Math.pow((delta - 0.90F) / 0.10F, 2.0);
        }
        return 0.0F;
    }

    private static int lerpColor(int alpha, int r0, int g0, int b0, int r1, int g1, int b1, float t) {
        float clamped = Mth.clamp(t, 0.0F, 1.0F);
        int r = Math.round(r0 + (r1 - r0) * clamped);
        int g = Math.round(g0 + (g1 - g0) * clamped);
        int b = Math.round(b0 + (b1 - b0) * clamped);
        return (alpha << 24) | (r << 16) | (g << 8) | b;
    }

    static void drawControllerScreenGlow(PoseStack.Pose p, VertexConsumer c, TextureAtlasSprite sprite, Direction facing) {
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

    private static void drawBox(PoseStack.Pose p, VertexConsumer c, TextureAtlasSprite sprite, int color, int light,
                                float x0, float y0, float z0, float x1, float y1, float z1,
                                float fracBottom, float fracTop, boolean drawBottom, boolean drawTop) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float svTop = Mth.lerp(fracTop, v1, v0);
        float svBot = Mth.lerp(fracBottom, v1, v0);

        if (drawTop) {
            quad(p, c, color, light, 0, 1, 0,
                    x0, y1, z0, u0, v0, x0, y1, z1, u0, v1, x1, y1, z1, u1, v1, x1, y1, z0, u1, v0);
        }
        if (drawBottom) {
            quad(p, c, color, light, 0, -1, 0,
                    x0, y0, z1, u0, v1, x0, y0, z0, u0, v0, x1, y0, z0, u1, v0, x1, y0, z1, u1, v1);
        }
        quad(p, c, color, light, 0, 0, -1,
                x1, y0, z0, u1, svBot, x0, y0, z0, u0, svBot, x0, y1, z0, u0, svTop, x1, y1, z0, u1, svTop);
        quad(p, c, color, light, 0, 0, 1,
                x0, y0, z1, u0, svBot, x1, y0, z1, u1, svBot, x1, y1, z1, u1, svTop, x0, y1, z1, u0, svTop);
        quad(p, c, color, light, -1, 0, 0,
                x0, y0, z0, u0, svBot, x0, y0, z1, u1, svBot, x0, y1, z1, u1, svTop, x0, y1, z0, u0, svTop);
        quad(p, c, color, light, 1, 0, 0,
                x1, y0, z1, u1, svBot, x1, y0, z0, u0, svBot, x1, y1, z0, u0, svTop, x1, y1, z1, u1, svTop);
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
