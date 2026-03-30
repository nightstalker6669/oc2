/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import li.cil.oc2.common.block.ProjectorBlock;
import li.cil.oc2.common.blockentity.ProjectorBlockEntity;
import li.cil.oc2.common.bus.device.vm.block.ProjectorDevice;
import li.cil.oc2.common.ext.MinecraftExt;
import li.cil.oc2.common.mixin.GameRendererAccessor;
import li.cil.oc2.common.util.FakePlayerUtils;
import li.cil.oc2.jcodec.common.model.Picture;
import li.cil.oc2.jcodec.scale.Yuv420jToRgb;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class ProjectorDepthRenderer {
    private static final int DEPTH_CAPTURE_SIZE = 256;

    private static final List<ProjectorBlockEntity> VISIBLE_PROJECTORS = new ArrayList<>();
    private static final DepthOnlyRenderTarget[] PROJECTOR_DEPTH_TARGETS = new DepthOnlyRenderTarget[ModShaders.MAX_PROJECTORS];
    private static final DynamicTexture[] PROJECTOR_COLOR_TARGETS = new DynamicTexture[ModShaders.MAX_PROJECTORS];
    private static final Matrix4f[] PROJECTOR_CAMERA_MATRICES = new Matrix4f[ModShaders.MAX_PROJECTORS];
    private static final Camera PROJECTOR_DEPTH_CAMERA = new Camera();
    private static DepthOnlyRenderTarget MAIN_CAMERA_DEPTH = new DepthOnlyRenderTarget(MainTarget.DEFAULT_WIDTH, MainTarget.DEFAULT_HEIGHT);
    private static final float PROJECTOR_FORWARD_SHIFT = 7 / 16f;
    private static final float PROJECTOR_NEAR = 0.5f - PROJECTOR_FORWARD_SHIFT;
    private static final float PROJECTOR_FAR = ProjectorBlockEntity.MAX_RENDER_DISTANCE;
    private static final int HALF_FRUSTUM_WIDTH = (ProjectorBlockEntity.MAX_WIDTH - 1) / 2;
    private static final int FRUSTUM_HEIGHT = ProjectorBlockEntity.MAX_HEIGHT - 1;
    private static final Matrix4f DEPTH_CAMERA_PROJECTION_MATRIX = getFrustumMatrix(
        PROJECTOR_NEAR, PROJECTOR_FAR,
        ProjectorBlockEntity.MAX_GOOD_RENDER_DISTANCE,
        -HALF_FRUSTUM_WIDTH, HALF_FRUSTUM_WIDTH,
        FRUSTUM_HEIGHT, 0
    );

    private static final Cache<ProjectorBlockEntity, RenderInfo> RENDER_INFO = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(5))
        .removalListener(ProjectorDepthRenderer::handleProjectorNoLongerRendering)
        .build();

    private static boolean isRenderingProjectorDepth;
    private static HitResult hitResultBak;
    private static boolean entityShadowsBak;
    private static Entity minecraftCameraEntityBak;
    private static Camera gameRendererMainCameraBak;

    static {
        for (int i = 0; i < ModShaders.MAX_PROJECTORS; i++) {
            PROJECTOR_DEPTH_TARGETS[i] = new DepthOnlyRenderTarget(DEPTH_CAPTURE_SIZE, DEPTH_CAPTURE_SIZE);
            PROJECTOR_CAMERA_MATRICES[i] = new Matrix4f();
        }
    }

    private ProjectorDepthRenderer() {
    }

    public static void addProjector(final ProjectorBlockEntity projector) {
        VISIBLE_PROJECTORS.add(projector);
    }

    public static boolean willRenderProjectorDepth() {
        return !VISIBLE_PROJECTORS.isEmpty();
    }

    public static boolean isIsRenderingProjectorDepth() {
        return isRenderingProjectorDepth;
    }

    public static void captureMainCameraDepth() {
        final RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        if (mainRenderTarget.width != MAIN_CAMERA_DEPTH.width || mainRenderTarget.height != MAIN_CAMERA_DEPTH.height) {
            MAIN_CAMERA_DEPTH.resize(mainRenderTarget.width, mainRenderTarget.height, Minecraft.ON_OSX);
        }
        if (mainRenderTarget.isStencilEnabled()) {
            MAIN_CAMERA_DEPTH.enableStencil();
        } else if (MAIN_CAMERA_DEPTH.isStencilEnabled()) {
            MAIN_CAMERA_DEPTH.destroyBuffers();
            MAIN_CAMERA_DEPTH = new DepthOnlyRenderTarget(mainRenderTarget.width, mainRenderTarget.height);
        }
        MAIN_CAMERA_DEPTH.copyDepthFrom(mainRenderTarget);
        mainRenderTarget.bindWrite(false);
    }

    @SubscribeEvent
    public static void renderProjectors(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || isRenderingProjectorDepth || VISIBLE_PROJECTORS.isEmpty()) {
            return;
        }

        try {
            final Minecraft minecraft = Minecraft.getInstance();
            final ClientLevel level = minecraft.level;
            final LocalPlayer player = minecraft.player;
            if (level == null || player == null) {
                return;
            }

            VISIBLE_PROJECTORS.sort((projector1, projector2) -> Double.compare(
                player.distanceToSqr(Vec3.atCenterOf(projector1.getBlockPos())),
                player.distanceToSqr(Vec3.atCenterOf(projector2.getBlockPos()))
            ));

            final int projectorCount = Math.min(VISIBLE_PROJECTORS.size(), ModShaders.MAX_PROJECTORS);
            renderProjectorDepths(minecraft, level, event.getPartialTick(), projectorCount);
            renderProjectorColors(minecraft, event.getModelViewMatrix(), event.getProjectionMatrix(), projectorCount);
        } finally {
            VISIBLE_PROJECTORS.clear();
            Arrays.fill(PROJECTOR_COLOR_TARGETS, null);
        }
    }

    @SubscribeEvent
    public static void handleFog(final ViewportEvent.RenderFog event) {
        if (isRenderingProjectorDepth) {
            FogRenderer.setupNoFog();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void handleNameplate(final RenderNameTagEvent event) {
        if (isRenderingProjectorDepth) {
            event.setCanRender(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void handleClientTick(final ClientTickEvent.Post event) {
        RENDER_INFO.cleanUp();
    }

    private static void handleProjectorNoLongerRendering(final RemovalNotification<ProjectorBlockEntity, RenderInfo> notification) {
        final ProjectorBlockEntity projector = notification.getKey();
        if (projector != null) {
            projector.setFrameConsumer(null);
        }

        final RenderInfo renderInfo = notification.getValue();
        if (renderInfo != null) {
            renderInfo.close();
        }
    }

    private static void renderProjectorDepths(final Minecraft minecraft, final ClientLevel level, final DeltaTracker deltaTracker, final int projectorCount) {
        final Vec3 mainCameraPosition = minecraft.gameRenderer.getMainCamera().getPosition();
        prepareDepthBufferRendering(minecraft, level);
        try {
            for (int projectorIndex = 0; projectorIndex < projectorCount; projectorIndex++) {
                final ProjectorBlockEntity projector = VISIBLE_PROJECTORS.get(projectorIndex);
                final Direction facing = projector.getBlockState().getValue(ProjectorBlock.FACING);
                final Vec3 projectorPosition = Vec3.atCenterOf(projector.getBlockPos()).add(new Vec3(facing.step()).scale(PROJECTOR_FORWARD_SHIFT));

                configureProjectorDepthCamera(level, projectorPosition, facing.toYRot());
                RenderSystem.setProjectionMatrix(DEPTH_CAMERA_PROJECTION_MATRIX, VertexSorting.DISTANCE_TO_ORIGIN);

                storeProjectorMatrix(projectorIndex, projectorPosition, mainCameraPosition);
                bindProjectorDepthRenderTarget(projectorIndex, minecraft);
                renderProjectorDepthBuffer(minecraft, deltaTracker);
                storeProjectorColorBuffer(projectorIndex, projector);

                projector.onRendering();
            }
        } finally {
            finishDepthBufferRendering(minecraft);
        }
    }

    private static void prepareDepthBufferRendering(final Minecraft minecraft, final ClientLevel level) {
        isRenderingProjectorDepth = true;

        hitResultBak = minecraft.hitResult;
        minecraft.hitResult = null;

        entityShadowsBak = minecraft.options.entityShadows().get();
        minecraft.options.entityShadows().set(false);

        minecraftCameraEntityBak = minecraft.getCameraEntity();
        minecraft.setCameraEntity(ProjectorCameraEntity.get(level, Vec3.ZERO, 0));
        final GameRendererAccessor gameRendererAccessor = (GameRendererAccessor) minecraft.gameRenderer;
        gameRendererMainCameraBak = gameRendererAccessor.oc2$getMainCamera();
        gameRendererAccessor.oc2$setMainCamera(PROJECTOR_DEPTH_CAMERA);

        RenderSystem.backupProjectionMatrix();
    }

    private static void finishDepthBufferRendering(final Minecraft minecraft) {
        minecraft.hitResult = hitResultBak;
        minecraft.options.entityShadows().set(entityShadowsBak);

        RenderSystem.restoreProjectionMatrix();

        ((MinecraftExt) minecraft).setMainRenderTargetOverride(null);
        minecraft.getMainRenderTarget().bindWrite(true);

        minecraft.setCameraEntity(minecraftCameraEntityBak);
        ((GameRendererAccessor) minecraft.gameRenderer).oc2$setMainCamera(gameRendererMainCameraBak);

        isRenderingProjectorDepth = false;
    }

    private static void configureProjectorDepthCamera(final ClientLevel level, final Vec3 position, final float rotationY) {
        PROJECTOR_DEPTH_CAMERA.setup(level, ProjectorCameraEntity.get(level, position, rotationY), false, false, 0);
    }

    private static void storeProjectorMatrix(final int projectorIndex, final Vec3 projectorPosition, final Vec3 mainCameraPosition) {
        final Quaternionf rotation = PROJECTOR_DEPTH_CAMERA.rotation().conjugate(new Quaternionf());
        final Matrix4f projectorViewMatrix = new Matrix4f()
            .rotation(rotation)
            .translate(
                (float) (mainCameraPosition.x() - projectorPosition.x()),
                (float) (mainCameraPosition.y() - projectorPosition.y()),
                (float) (mainCameraPosition.z() - projectorPosition.z())
            );
        PROJECTOR_CAMERA_MATRICES[projectorIndex].set(DEPTH_CAMERA_PROJECTION_MATRIX).mul(projectorViewMatrix);
    }

    private static void bindProjectorDepthRenderTarget(final int projectorIndex, final Minecraft minecraft) {
        final DepthOnlyRenderTarget projectorDepthTarget = PROJECTOR_DEPTH_TARGETS[projectorIndex];
        projectorDepthTarget.bindWrite(true);
        ((MinecraftExt) minecraft).setMainRenderTargetOverride(projectorDepthTarget);
    }

    private static void renderProjectorDepthBuffer(final Minecraft minecraft, final DeltaTracker deltaTracker) {
        final Quaternionf rotation = PROJECTOR_DEPTH_CAMERA.rotation().conjugate(new Quaternionf());
        final Matrix4f viewMatrix = new Matrix4f().rotation(rotation);
        final LevelRenderer levelRenderer = minecraft.levelRenderer;
        final LightTexture lightTexture = minecraft.gameRenderer.lightTexture();

        levelRenderer.prepareCullFrustum(PROJECTOR_DEPTH_CAMERA.getPosition(), viewMatrix, DEPTH_CAMERA_PROJECTION_MATRIX);
        levelRenderer.renderLevel(deltaTracker, false, PROJECTOR_DEPTH_CAMERA, minecraft.gameRenderer, lightTexture, viewMatrix, DEPTH_CAMERA_PROJECTION_MATRIX);
    }

    private static void storeProjectorColorBuffer(final int projectorIndex, final ProjectorBlockEntity projector) {
        PROJECTOR_COLOR_TARGETS[projectorIndex] = getColorBuffer(projector);
    }

    private static void renderProjectorColors(final Minecraft minecraft, final Matrix4f modelViewMatrix, final Matrix4f projectionMatrix, final int projectorCount) {
        prepareColorBufferRendering();
        try {
            prepareOrthographicRendering(minecraft);

            RenderSystem.setShader(ModShaders::getProjectorsShader);
            ModShaders.configureProjectorsShader(
                MAIN_CAMERA_DEPTH,
                constructInverseMainCameraMatrix(modelViewMatrix, projectionMatrix),
                PROJECTOR_COLOR_TARGETS,
                PROJECTOR_DEPTH_TARGETS,
                PROJECTOR_CAMERA_MATRICES,
                projectorCount
            );

            renderIntoScreenRect();
        } finally {
            finishColorBufferRendering();
        }
    }

    private static void prepareColorBufferRendering() {
        RenderSystem.backupProjectionMatrix();

        final Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }

    private static void finishColorBufferRendering() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.disableBlend();

        final Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
    }

    private static void prepareOrthographicRendering(final Minecraft minecraft) {
        final Matrix4f screenProjectionMatrix = new Matrix4f().setOrtho(
            0.0F,
            minecraft.getWindow().getWidth(),
            minecraft.getWindow().getHeight(),
            0.0F,
            1000.0F,
            3000.0F
        );
        RenderSystem.setProjectionMatrix(screenProjectionMatrix, VertexSorting.ORTHOGRAPHIC_Z);

        final Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.identity();
        modelViewStack.translate(0.0F, 0.0F, -2000.0F);
        RenderSystem.applyModelViewMatrix();
    }

    private static Matrix4f constructInverseMainCameraMatrix(final Matrix4f modelViewMatrix, final Matrix4f projectionMatrix) {
        return new Matrix4f(projectionMatrix).mul(modelViewMatrix).invert();
    }

    private static void renderIntoScreenRect() {
        final BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);
        builder.addVertex(0.0F, MAIN_CAMERA_DEPTH.height, 0.0F).setUv(0.0F, 0.0F);
        builder.addVertex(MAIN_CAMERA_DEPTH.width, MAIN_CAMERA_DEPTH.height, 0.0F).setUv(1.0F, 0.0F);
        builder.addVertex(MAIN_CAMERA_DEPTH.width, 0.0F, 0.0F).setUv(1.0F, 1.0F);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    private static Matrix4f getFrustumMatrix(final float near, final float far, final float dist, final float left, final float right, final float top, final float bottom) {
        return new Matrix4f().set(
            2 * dist / (right - left), 0, (right + left) / (right - left), 0,
            0, 2 * dist / (top - bottom), (top + bottom) / (top - bottom), 0,
            0, 0, -(far + near) / (far - near), -(2 * far * near) / (far - near),
            0, 0, -1, 0
        );
    }

    private static DynamicTexture getColorBuffer(final ProjectorBlockEntity projector) {
        try {
            return RENDER_INFO.get(projector, () -> {
                final DynamicTexture texture = new DynamicTexture(ProjectorDevice.WIDTH, ProjectorDevice.HEIGHT, false);
                texture.upload();
                final RenderInfo renderInfo = new RenderInfo(texture);
                projector.setFrameConsumer(renderInfo);
                return renderInfo;
            }).texture();
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private record RenderInfo(DynamicTexture texture) implements ProjectorBlockEntity.FrameConsumer {
        private static final ThreadLocal<byte[]> RGB = ThreadLocal.withInitial(() -> new byte[3]);

        public synchronized void close() {
            texture.close();
        }

        @Override
        public synchronized void processFrame(final Picture picture) {
            final NativeImage image = texture.getPixels();
            if (image == null) {
                return;
            }

            final byte[] y = picture.getPlaneData(0);
            final byte[] u = picture.getPlaneData(1);
            final byte[] v = picture.getPlaneData(2);

            int lumaIndex = 0;
            int chromaIndex = 0;
            for (int halfRow = 0; halfRow < ProjectorDevice.HEIGHT / 2; halfRow++, lumaIndex += ProjectorDevice.WIDTH * 2) {
                final int row = halfRow * 2;
                for (int halfCol = 0; halfCol < ProjectorDevice.WIDTH / 2; halfCol++, chromaIndex++) {
                    final int col = halfCol * 2;
                    final int yIndex = lumaIndex + col;
                    final byte cb = u[chromaIndex];
                    final byte cr = v[chromaIndex];
                    setFromYUV420(image, col, row, y[yIndex], cb, cr);
                    setFromYUV420(image, col + 1, row, y[yIndex + 1], cb, cr);
                    setFromYUV420(image, col, row + 1, y[yIndex + ProjectorDevice.WIDTH], cb, cr);
                    setFromYUV420(image, col + 1, row + 1, y[yIndex + ProjectorDevice.WIDTH + 1], cb, cr);
                }
            }

            texture.upload();
        }

        private static void setFromYUV420(final NativeImage image, final int col, final int row, final byte y, final byte cb, final byte cr) {
            final byte[] bytes = RGB.get();
            Yuv420jToRgb.YUVJtoRGB(y, cb, cr, bytes, 0);
            final int r = bytes[0] + 128;
            final int g = bytes[1] + 128;
            final int b = bytes[2] + 128;
            image.setPixelRGBA(col, row, r | (g << 8) | (b << 16) | (0xFF << 24));
        }
    }

    private static final class DepthOnlyRenderTarget extends TextureTarget {
        public DepthOnlyRenderTarget(final int width, final int height) {
            super(width, height, true, Minecraft.ON_OSX);
        }

        @Override
        public void createBuffers(final int width, final int height, final boolean isOnOSX) {
            super.createBuffers(width, height, isOnOSX);
            if (colorTextureId > -1) {
                if (frameBufferId > -1) {
                    GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBufferId);
                    GL11.glDrawBuffer(GL11.GL_NONE);
                    GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
                }
                TextureUtil.releaseTextureId(colorTextureId);
                colorTextureId = -1;
            }
        }
    }

    private static final class ProjectorCameraEntity extends Player {
        private static ProjectorCameraEntity instance;

        public static ProjectorCameraEntity get(final Level level, final Vec3 position, final float rotationY) {
            if (instance == null) {
                instance = new ProjectorCameraEntity(level, BlockPos.ZERO, rotationY);
            }

            instance.setLevel(level);
            instance.moveTo(position.x(), position.y(), position.z(), rotationY, 0);
            instance.xo = position.x();
            instance.yo = position.y();
            instance.zo = position.z();
            instance.yRotO = rotationY;
            instance.xRotO = 0;
            return instance;
        }

        private ProjectorCameraEntity(final Level level, final BlockPos blockPos, final float rotationY) {
            super(level, blockPos, rotationY, FakePlayerUtils.getFakePlayerProfile());
        }

        @Override
        public float getViewYRot(final float partialTicks) {
            return getYRot();
        }

        @Override
        public float getViewXRot(final float partialTicks) {
            return getXRot();
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return true;
        }
    }
}
