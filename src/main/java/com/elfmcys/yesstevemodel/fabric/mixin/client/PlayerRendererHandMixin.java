package com.elfmcys.yesstevemodel.fabric.mixin.client;

import com.elfmcys.yesstevemodel.client.event.ReplacePlayerHandRenderEvent;
import com.elfmcys.yesstevemodel.client.renderer.RenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 第一人称手臂：1.21.11 的 {@code AvatarRenderer#renderRightHand/renderLeftHand} 改成了
 * {@code (PoseStack, SubmitNodeCollector, int, Identifier, boolean)}。
 * YSM 模型有自定义手臂骨骼时接管这两处绘制，否则走原版。
 */
@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererHandMixin {

    @Inject(
            method = "renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void ysm$onRenderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
                                      Identifier skinTexture, boolean sleeveVisible, CallbackInfo ci) {
        if (ysm$dispatchHandRender(poseStack, submitNodeCollector, packedLight, HumanoidArm.RIGHT)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void ysm$onRenderMapHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
                                    Identifier skinTexture, boolean sleeveVisible, CallbackInfo ci) {
        if (ysm$dispatchHandRender(poseStack, submitNodeCollector, packedLight, HumanoidArm.LEFT)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean ysm$dispatchHandRender(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
                                           HumanoidArm humanoidArm) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        RenderContext.enter(submitNodeCollector, null);
        try {
            boolean cancelled = ReplacePlayerHandRenderEvent.onRenderArm(minecraft.player, humanoidArm, poseStack, bufferSource, packedLight);
            if (cancelled) {
                bufferSource.endBatch();
            }
            return cancelled;
        } finally {
            RenderContext.exit();
        }
    }
}
