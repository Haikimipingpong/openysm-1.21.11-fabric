package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import com.mojang.math.Axis;

/**
 * 载具等「非玩家、非投射物」实体的 YSM 模型渲染器。
 * 1.21.11 起 {@code EntityRenderer} 只有两个泛型参数，state 由 {@code EntityRenderState} 承载。
 */
public abstract class GeoEntityRenderer<TEntity extends Entity, T extends AnimatableEntity<TEntity>>
        extends EntityRenderer<TEntity, EntityRenderState> implements IGeoRenderer<T> {

    public Matrix4f worldMatrix = new Matrix4f();

    public Matrix4f modelMatrix = new Matrix4f();

    private IRenderCycle renderState = EModelRenderCycle.INITIAL;

    public MultiBufferSource bufferSource = null;

    public GeoEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void renderEntity(T t, EntityRenderState state, float entityYaw, float partialTick, PoseStack poseStack,
                             MultiBufferSource multiBufferSource, int packedLight) {
        AnimationEvent<?> event = t.processAnimation(partialTick);
        Minecraft minecraft = Minecraft.getInstance();
        if (event != null && minecraft.player != null) {
            Entity entity = t.getEntity();
            boolean isVisible = !entity.isInvisibleTo(minecraft.player);
            boolean shouldEntityAppearGlowing = minecraft.shouldEntityAppearGlowing(entity);
            RenderType renderType = getRenderType(t.getTextureLocation(), isVisible, shouldEntityAppearGlowing,
                    t.getCurrentModel().getGeoModel().isTranslucentTexture(0));
            if (renderType != null && (isVisible || shouldEntityAppearGlowing)) {
                Color color = getRenderColor(t, partialTick, poseStack, multiBufferSource, null, packedLight);
                AnimatedGeoModel model = t.getCurrentModel();
                this.worldMatrix = new Matrix4f(poseStack.last().pose());
                setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
                renderWithBoneAndRenderType(model, t, partialTick, renderType, poseStack, multiBufferSource, 0, null, packedLight,
                        packOverlayCoords(entity, 0.0f), color.getRed() / 255.0f, color.getGreen() / 255.0f,
                        color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
                poseStack.popPose();
            }
        }
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void extractRenderState(TEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public void renderEarly(T animatable, PoseStack poseStack, float partialTick, MultiBufferSource bufferSource,
                            VertexConsumer buffer, int packedLight, int packedOverlayIn,
                            float red, float green, float blue, float alpha) {
        this.modelMatrix = new Matrix4f(poseStack.last().pose());
        IGeoRenderer.super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlayIn, red, green, blue, alpha);
    }

    public static int packOverlayCoords(Entity entity, float f) {
        return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(false));
    }

    @Override
    @NotNull
    public IRenderCycle getCurrentModelRenderCycle() {
        return this.renderState;
    }

    @Override
    public void setCurrentModelRenderCycle(IRenderCycle cycle) {
        this.renderState = cycle;
    }

    @Override
    public void setCurrentRTB(MultiBufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    @Override
    public MultiBufferSource getCurrentRTB() {
        return this.bufferSource;
    }
}
