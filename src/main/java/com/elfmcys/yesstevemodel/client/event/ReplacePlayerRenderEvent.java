package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.client.renderer.RenderContext;
import com.elfmcys.yesstevemodel.client.renderer.RendererManager;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.util.CameraUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.player.Player;
import rip.ysm.compat.firstperson.FirstPersonCompat;
import rip.ysm.compat.oculus.OculusCompat;
import rip.ysm.compat.playeranimator.PlayerAnimatorCompat;
import rip.ysm.compat.realcamera.RealCameraCompat;

public class ReplacePlayerRenderEvent {

    private ReplacePlayerRenderEvent() {
    }

    /**
     * @return true 表示 YSM 已经接管这个玩家的渲染，调用方应跳过原版绘制。
     */
    public static boolean onRenderPlayerPre(Player entity, AvatarRenderState renderState, float partialTick,
                                            PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (!YesSteveModel.isAvailable()) {
            return false;
        }
        // 模型卡预览：state 绑的是卡片自己的 PlayerPreviewEntity，直接用卡片模型绘制
        if (ModelPreviewRenderer.getPreviewAnimatable(renderState) instanceof CustomPlayerEntity previewAnimatable) {
            MultiBufferSource.BufferSource previewBuffers = Minecraft.getInstance().renderBuffers().bufferSource();
            RenderContext.enter(collector, cameraState);
            ModelPreviewRenderer.setPreviewMode(true);
            try {
                RendererManager.getPlayerRenderer().renderPreview(previewAnimatable, renderState, partialTick,
                        poseStack, previewBuffers, 15728880);
                if (OculusCompat.isRenderingShadowPass()) {
                    previewBuffers.endBatch();
                }
            } finally {
                ModelPreviewRenderer.setPreviewMode(false);
                RenderContext.exit();
            }
            return true;
        }
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (entity.equals(localPlayer) && GeneralConfig.DISABLE_SELF_MODEL.get()) {
            return false;
        }
        if ((!entity.equals(localPlayer) && GeneralConfig.DISABLE_OTHER_MODEL.get()) || entity.isSpectator()) {
            return false;
        }
        boolean[] cancelled = {false};
        PlayerCapability.get(entity).ifPresent(cap -> {
            if (cap.isModelActive()
                    && (!CameraUtil.isFirstPerson(cap)
                    || FirstPersonCompat.isFirstPersonActive()
                    || RealCameraCompat.isActive()
                    || GeneralConfig.DISABLE_EXTERNAL_FP_ANIM.get()
                    || !PlayerAnimatorCompat.isPlayerAnimated(localPlayer))) {
                cancelled[0] = true;
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                RenderContext.enter(collector, cameraState);
                boolean guiPreview = ModelPreviewRenderer.isPreview() || ModelPreviewRenderer.isPreviewState(renderState);
                boolean previousPreviewMode = ModelPreviewRenderer.isPreview();
                if (guiPreview) {
                    ModelPreviewRenderer.setPreviewMode(true);
                }
                try {
                    int packedLight = guiPreview ? 15728880 : renderState.lightCoords;
                    RendererManager.getPlayerRenderer().render(entity, renderState, entity.getYRot(),
                            ModelPreviewRenderer.isPreview() ? 1.0f : partialTick, poseStack, bufferSource, packedLight);
                    if (OculusCompat.isRenderingShadowPass()) {
                        bufferSource.endBatch();
                    }
                } finally {
                    ModelPreviewRenderer.setPreviewMode(previousPreviewMode);
                    RenderContext.exit();
                }
            }
        });
        return cancelled[0];
    }
}
