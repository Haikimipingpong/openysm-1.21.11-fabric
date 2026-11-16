package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.capability.VehicleCapability;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.extended.LivingEntityRendererAccessor;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle;
import com.elfmcys.yesstevemodel.mixin.client.LivingEntityAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import rip.ysm.api.client.RenderLivingBridge;

import java.util.List;
import java.util.Optional;

/**
 * 1.21.11 的「两段式」实体渲染：
 * <pre>
 *   extractRenderState(entity, state, partialTick)   // 只搬数据
 *   submit(state, poseStack, collector, camera)      // 只读 state
 * </pre>
 * YSM 的 geckolib 绘制需要活实体（molang / capability / 骨骼），所以实体引用由
 * {@code EntityRenderStateBindings} 维护；渲染入口仍在 submit 阶段，
 * 通过 {@code RenderContext} 拿到 collector 给名牌 / 手持物品用。
 * <p>
 * 本类不直接注册到 EntityRenderers：它由 {@code RendererManager} 手动实例化，
 * 在 {@code PlayerRendererMixin} 里被调用（与原版 1.21.11 官方实现一致）。
 */
public abstract class GeoReplacedEntityRenderer<TEntity extends Player, T extends LivingAnimatable<TEntity>, S extends AvatarRenderState>
        extends LivingEntityRenderer<TEntity, S, PlayerModel> implements IGeoRenderer<T> {

    public final List<GeoLayerRenderer<T>> layerRenderers = new ObjectArrayList<>();

    public Matrix4f dispatchedMat = new Matrix4f();

    public Matrix4f renderEarlyMat = new Matrix4f();

    public MultiBufferSource rtb;

    private IRenderCycle currentModelRenderCycle = EModelRenderCycle.INITIAL;

    private static final boolean DEBUG_RENDER = Boolean.getBoolean("ysm.debugRender");


    private static long LAST_DEBUG = 0L;

    @SuppressWarnings("unchecked")
    public GeoReplacedEntityRenderer(EntityRendererProvider.Context context) {
        super(context, (PlayerModel) new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5f);
        this.rtb = null;
    }

    public static int packOverlayCoords(net.minecraft.world.entity.LivingEntity entity, float u) {
        return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
    }

    @Override
    public void extractRenderState(TEntity entity, S state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, this.itemModelResolver);
    }

    @Override
    @NotNull
    public IRenderCycle getCurrentModelRenderCycle() {
        return this.currentModelRenderCycle;
    }

    @Override
    public void setCurrentModelRenderCycle(IRenderCycle cycle) {
        this.currentModelRenderCycle = cycle;
    }

    @Override
    public void renderEarly(T animatable, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource, VertexConsumer buffer,
                            int packedLight, int packedOverlayIn, float red, float green, float blue, float alpha) {
        // 使用 .set 来避免每次渲染创建新的 Matrix4f, 减少 allocation rate
        this.renderEarlyMat.set(poseStack.last().pose());
        IGeoRenderer.super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlayIn, red, green, blue, alpha);
    }

    public void renderEntity(T t, S state, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        renderEntityWithTexture(t, state, null, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @SuppressWarnings("unchecked")
    public void renderEntityWithTexture(T t, S state, @Nullable Identifier resourceLocation, float entityYaw, float partialTick,
                                        PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (RenderLivingBridge.firePre(t.getEntity(), this, partialTick, poseStack, multiBufferSource, packedLight)) {
            return;
        }
        TEntity entity = t.getEntity();
        // 预览模式下把实体的朝向临时对齐到 render state，保证 GUI 里的旋转正常
        // GUI 预览是延迟渲染的，isPreview() 在真正绘制时已经复位；state 上的标记才是可靠信号
        boolean syncRotationsForPreview = (ModelPreviewRenderer.isPreview() || ModelPreviewRenderer.isPreviewState(state)) && entity != null;
        float savedYBodyRot = 0.0f;
        float savedYBodyRotO = 0.0f;
        float savedYHeadRot = 0.0f;
        float savedYHeadRotO = 0.0f;
        float savedYRot = 0.0f;
        float savedYRotO = 0.0f;
        float savedXRot = 0.0f;
        float savedXRotO = 0.0f;
        if (syncRotationsForPreview) {
            savedYBodyRot = entity.yBodyRot;
            savedYBodyRotO = entity.yBodyRotO;
            savedYHeadRot = entity.yHeadRot;
            savedYHeadRotO = entity.yHeadRotO;
            savedYRot = entity.getYRot();
            savedYRotO = entity.yRotO;
            savedXRot = entity.getXRot();
            savedXRotO = entity.xRotO;
            float bodyRot = state.bodyRot;
            float headYaw = state.bodyRot + state.yRot;
            entity.yBodyRot = bodyRot;
            entity.yBodyRotO = bodyRot;
            entity.yHeadRot = headYaw;
            entity.yHeadRotO = headYaw;
            entity.setYRot(headYaw);
            entity.yRotO = headYaw;
            entity.setXRot(state.xRot);
            entity.xRotO = state.xRot;
        }

        AnimationEvent<?> event;
        try {
            event = t.processAnimation(partialTick);
        } finally {
            if (syncRotationsForPreview) {
                entity.yBodyRot = savedYBodyRot;
                entity.yBodyRotO = savedYBodyRotO;
                entity.yHeadRot = savedYHeadRot;
                entity.yHeadRotO = savedYHeadRotO;
                entity.setYRot(savedYRot);
                entity.yRotO = savedYRotO;
                entity.setXRot(savedXRot);
                entity.xRotO = savedXRotO;
            }
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (DEBUG_RENDER && System.currentTimeMillis() - LAST_DEBUG > 2000L) {
            LAST_DEBUG = System.currentTimeMillis();
            System.out.println("[YSM-DEBUG] renderEntity entity=" + entity.getName().getString()
                    + " event=" + (event != null)
                    + " model=" + (t.getCurrentModel() != null)
                    + " modelId=" + t.getModelId()
                    + " texture=" + t.getTextureLocation()
                    + " bones=" + (t.getCurrentModel() != null ? t.getCurrentModel().getGeoModel().bakedBones.size() : -1));
        }
        if (event != null && minecraft.player != null) {
            EntityModelData modelData = event.getModelData();
            // 使用 .set 来避免每次渲染创建新的 Matrix4f, 减少 allocation rate
            this.dispatchedMat.set(poseStack.last().pose());
            setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
            poseStack.pushPose();
            if (entity.getPose() == Pose.SLEEPING) {
                Direction bedOrientation = entity.getBedOrientation();
                if (bedOrientation != null) {
                    float eyeHeight = entity.getEyeHeight(Pose.STANDING) - 0.1f;
                    poseStack.translate((-bedOrientation.getStepX()) * eyeHeight, 0.0f, (-bedOrientation.getStepZ()) * eyeHeight);
                }
            }
            this.setupRotations(entity, state, poseStack, modelData.lerpedAge, modelData.lerpBodyRot, partialTick, 1.0f);
            if (entity.getVehicle() != null) {
                VehicleCapability.get(entity.getVehicle()).ifPresent(cap -> {
                    Vector3f vector3f = cap.getExpressionOffset();
                    if (vector3f != null) {
                        poseStack.mulPose(new Quaternionf().rotateZYX(vector3f.z, 0.0f, vector3f.x).invert());
                    }
                });
            }
            preRenderCallback(entity, poseStack, partialTick);
            poseStack.translate(0.0f, 0.01f, 0.0f);
            AnimatedGeoModel animatedGeoModel = t.getCurrentModel();
            int textureIndex = resourceLocation == null ? t.getTextureIndex() : 0;
            RenderType renderType = getRenderType(resourceLocation == null ? t.getTextureLocation() : resourceLocation,
                    isBodyVisible(state) && !entity.isInvisibleTo(minecraft.player),
                    minecraft.shouldEntityAppearGlowing(entity),
                    t.getCurrentModel().getGeoModel().isTranslucentTexture(textureIndex));
            boolean useExtraPlayer = t.isRenderLayersFirst();
            Color color = getRenderColor(t, partialTick, poseStack, multiBufferSource, null, packedLight);
            renderWithBone(animatedGeoModel, t, partialTick, poseStack, multiBufferSource, null, packedLight,
                    packOverlayCoords(entity, getHurtOverlayProgress(entity, partialTick)),
                    color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            if (useExtraPlayer && !entity.isSpectator()) {
                render(t, state, partialTick, poseStack, multiBufferSource, packedLight, event, modelData);
            }
            if (renderType != null) {
                renderWithBoneAndRenderType(animatedGeoModel, t, partialTick, renderType, poseStack, multiBufferSource, textureIndex, null,
                        packedLight, packOverlayCoords(entity, getHurtOverlayProgress(entity, partialTick)),
                        color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
            }
            if (!useExtraPlayer && !entity.isSpectator()) {
                render(t, state, partialTick, poseStack, multiBufferSource, packedLight, event, modelData);
            }
            poseStack.popPose();
        }

        SubmitNodeCollector activeCollector = com.elfmcys.yesstevemodel.client.renderer.RenderContext.collector();
        CameraRenderState activeCameraState = com.elfmcys.yesstevemodel.client.renderer.RenderContext.camera();
        if (activeCollector != null && activeCameraState != null && entity != null && entity != minecraft.player
                && !ModelPreviewRenderer.isPreviewState(state)) {
            ((LivingEntityRendererAccessor) this).tlm$renderNameTag(state, poseStack, activeCollector, activeCameraState);
        }
        RenderLivingBridge.firePost(entity, this, partialTick, poseStack, multiBufferSource, packedLight);
    }

    public void render(T entity, S state, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn,
                       AnimationEvent<?> event, EntityModelData data) {
        for (GeoLayerRenderer<T> layerRenderer : this.layerRenderers) {
            layerRenderer.render(state, poseStack, bufferSource, packedLightIn, entity, event.getLimbSwing(),
                    event.getLimbSwingAmount(), partialTick, data.lerpedAge, data.rawNetHeadYaw, data.rawHeadPitch);
        }
    }

    public float getHurtOverlayProgress(TEntity entity, float partialTick) {
        return 0.0f;
    }

    public void preRenderCallback(TEntity entity, PoseStack poseStack, float partialTick) {
    }

    /**
     * 1.21.11 的 setupRotations 只收 render state，实体级别的处理（受伤闪红、爬梯子朝向）
     * 需要在调用前后临时改实体字段，这里按官方实现还原。
     */
    public void setupRotations(TEntity entity, S state, PoseStack poseStack, float ageInTicks, float rotationYaw,
                               float partialTicks, float scale) {
        int deathTime = entity.deathTime;
        boolean isAutoSpinAttack = entity.isAutoSpinAttack();
        if (deathTime > 0) {
            entity.deathTime = 0;
        }
        if (isAutoSpinAttack) {
            ((LivingEntityAccessor) entity).invokeSetLivingEntityFlag(4, false);
        }
        if (((LivingEntityAccessor) entity).invokeOnClimbable()) {
            Optional<BlockPos> lastClimbablePos = entity.getLastClimbablePos();
            if (lastClimbablePos.isPresent()) {
                Optional<Direction> optionalValue = entity.level().getBlockState(lastClimbablePos.get())
                        .getOptionalValue(HorizontalDirectionalBlock.FACING);
                if (optionalValue.isPresent()) {
                    rotationYaw = optionalValue.get().toYRot() * 90;
                }
            }
        }
        super.setupRotations(state, poseStack, rotationYaw, scale);
        if (deathTime > 0) {
            entity.deathTime = deathTime;
        }
        if (isAutoSpinAttack) {
            ((LivingEntityAccessor) entity).invokeSetLivingEntityFlag(4, true);
        }
    }

    @Override
    public boolean shouldShowName(TEntity entity, double distance) {
        double d = entity.isDiscrete() ? 32.0 : 64.0;
        return distance < d * d && entity == this.entityRenderDispatcher.crosshairPickEntity
                && entity.hasCustomName() && Minecraft.renderNames();
    }

    public final boolean addLayerRenderer(GeoLayerRenderer<T> layerRenderer) {
        return this.layerRenderers.add(layerRenderer);
    }

    @Override
    public MultiBufferSource getCurrentRTB() {
        return this.rtb;
    }

    @Override
    public void setCurrentRTB(MultiBufferSource bufferSource) {
        this.rtb = bufferSource;
    }
}
