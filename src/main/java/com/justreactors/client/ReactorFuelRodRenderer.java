package com.justreactors.client;

import com.justreactors.JustReactors;
import com.justreactors.blockentity.ReactorControllerBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * Renders live Uranium fuel (top) and Cyanite waste (bottom) levels inside each
 * {@code ReactorFuelRodBlock} of an assembled reactor so players can see burn progress through Reactor Glass.
 */
public class ReactorFuelRodRenderer implements BlockEntityRenderer<ReactorControllerBlockEntity> {
    private static final float MIN_XZ = 2.94F / 16.0F;
    private static final float MAX_XZ = 13.06F / 16.0F;
    private static final float Y_MIN = 2.0F / 16.0F;
    private static final float CAVITY_H = 12.0F / 16.0F;

    public ReactorFuelRodRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static final class State {
        int lightCoords;
        boolean visible;
        boolean active;
        float fuelRatio;
        float wasteRatio;
        int[] rodOffsets = new int[0];
        @Nullable
        TextureAtlasSprite fuelSprite;
        @Nullable
        TextureAtlasSprite wasteSprite;
        int light;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }


    @Override
    public void render(ReactorControllerBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        State state = new State();
        state.lightCoords = packedLight;
        extractRenderState(be, state, partialTicks);
        submit(state, poseStack, buffers);
    }

    private void extractRenderState(ReactorControllerBlockEntity be, State state, float partialTicks) {
        int[] offsets = be.getClientFuelRodOffsets();
        float fuel = be.getClientFuelRatio();
        float waste = be.getClientWasteRatio();
        if (!be.isClientAssembled() || offsets.length < 3 || (fuel <= 0.005F && waste <= 0.005F)) {
            state.visible = false;
            return;
        }

        state.visible = true;
        state.active = be.isClientActive();
        state.fuelRatio = fuel;
        state.wasteRatio = waste;
        state.rodOffsets = offsets;

        TextureAtlas blocks = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        state.fuelSprite = blocks.getSprite(JustReactors.id("block/uranium_block"));
        state.wasteSprite = blocks.getSprite(JustReactors.id("block/cyanite_block"));

        int blockLight = state.active ? 14 : 7;
        state.light = LightTexture.pack(
                Math.max(blockLight, LightTexture.block(state.lightCoords)),
                Math.max(state.active ? 12 : 6, LightTexture.sky(state.lightCoords))
        );
    }

    private void submit(State state, PoseStack poseStack, MultiBufferSource buffers) {
        if (!state.visible || state.fuelSprite == null || state.wasteSprite == null) {
            return;
        }

        TextureAtlasSprite fuelSprite = state.fuelSprite;
        TextureAtlasSprite wasteSprite = state.wasteSprite;
        int light = state.light;
        float wasteFrac = Math.min(1.0F, Math.max(0.0F, state.wasteRatio));
        float fuelFrac = Math.min(1.0F - wasteFrac, Math.max(0.0F, state.fuelRatio));
        int fuelColor = state.active ? 0xE6D8FF6B : 0xB898C446;
        int wasteColor = 0xD84AC0F0;

        {
            PoseStack.Pose pose = poseStack.last();
            VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentCull(fuelSprite.atlasLocation()));
            int[] offsets = state.rodOffsets;
            for (int i = 0; i + 2 < offsets.length; i += 3) {
                float ox = offsets[i];
                float oy = offsets[i + 1];
                float oz = offsets[i + 2];

                float curBottom = oy + Y_MIN;
                if (wasteFrac > 0.005F) {
                    float h = CAVITY_H * wasteFrac;
                    drawBox(pose, consumer, wasteSprite, wasteColor, light,
                            ox + MIN_XZ, curBottom, oz + MIN_XZ,
                            ox + MAX_XZ, curBottom + h, oz + MAX_XZ);
                    curBottom += h;
                }
                if (fuelFrac > 0.005F) {
                    float h = CAVITY_H * fuelFrac;
                    drawBox(pose, consumer, fuelSprite, fuelColor, light,
                            ox + MIN_XZ, curBottom, oz + MIN_XZ,
                            ox + MAX_XZ, curBottom + h, oz + MAX_XZ);
                }
            }
        }
    }

    private static void drawBox(PoseStack.Pose p, VertexConsumer c, TextureAtlasSprite sprite, int color, int light,
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
        c.vertex(p.pose(), x, y, z)
                .color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(p.normal(), nx, ny, nz)
                .endVertex();
    }
}
