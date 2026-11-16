package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.client.event.ReplacePlayerRenderEvent;
import com.elfmcys.yesstevemodel.client.renderer.EntityRenderStateBindings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 玩家模型渲染的总入口。
 * <p>
 * 1.21.11 的 {@code AvatarRenderer} 没有 {@code render(entity, ...)} 了，绘制统一走
 * {@code submit(renderState, poseStack, collector, cameraState)}。这里在 submit 里把渲染权交给 YSM：
 * {@link ReplacePlayerRenderEvent#onRenderPlayerPre} 返回 true 表示「YSM 已经画完了」，跳过原版绘制；
 * 返回 false 时落回原版（玩家仍然是正常的原版模型，不会消失）。
 */
@Mixin(AvatarRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, AvatarRenderState, PlayerModel> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    public void submit(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        Entity entity = EntityRenderStateBindings.get(state);
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        if (!(entity instanceof Player player
                && ReplacePlayerRenderEvent.onRenderPlayerPre(player, state, partialTick, poseStack, collector, camera))) {
            super.submit(state, poseStack, collector, camera);
        }
    }
}
