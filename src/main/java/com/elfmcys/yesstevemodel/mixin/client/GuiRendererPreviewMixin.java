package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.client.renderer.gui.YSMPreviewPipRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把「模型卡预览」的离屏渲染转交给每卡位独立的 {@code GuiEntityRenderer}。
 * <p>
 * 原版只按 state 的 class 查一个 renderer，而模型选择页一屏会有十几张卡的预览，
 * 共用一个 renderer 的纹理必然互相覆盖。
 */
@Mixin(GuiRenderer.class)
public abstract class GuiRendererPreviewMixin {

    @Shadow
    @Final
    private GuiRenderState renderState;

    @Inject(method = "preparePictureInPictureState", at = @At("HEAD"), cancellable = true)
    private <T extends PictureInPictureRenderState> void ysm$prepareModelPreview(T state, int guiScale, CallbackInfo ci) {
        if (state instanceof GuiEntityRenderState entityState) {
            LivingAnimatable<?> animatable = ModelPreviewRenderer.getPreviewAnimatable(entityState.renderState());
            if (animatable != null) {
                YSMPreviewPipRenderer.get().prepare(ModelPreviewRenderer.slotOf(animatable), entityState, this.renderState, guiScale);
                ci.cancel();
            }
        }
    }
}
