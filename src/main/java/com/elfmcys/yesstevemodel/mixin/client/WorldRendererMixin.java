package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.entity.EntityRenderCache;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.11 的 {@code LevelRenderer#renderLevel} 签名完全变了（PoseStack 没了，改成
 * GraphicsResourceAllocator + DeltaTracker + 一组矩阵/Buffer），旧的注入点全部失效，
 * 这个 mixin 之前在移植时被移除。
 * <p>
 * 它承担两个作用（和上游 1.20.1 一致）：
 * <ol>
 *   <li>{@code ModelPreviewRenderer.setFirstPersonMode(true)}：整个世界渲染期间置位。
 *       注意这个 flag 名字有误导性，它实际表示“当前处在世界渲染线程上下文”，
 *       {@code AnimatableEntity#setCustomAnimations} 依赖它让动画逐帧推进
 *       （{@code z=false} 才会走 z3 分支），否则动画只会停在 seekTime==0 的第一帧。</li>
 *   <li>{@code EntityRenderCache.tick/clear}：保证实体模型每帧被处理。</li>
 * </ol>
 */
@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void ysm$renderLevelHead(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker, boolean renderBlockOutline,
                                     Camera camera, Matrix4f projectionMatrix, Matrix4f frustumMatrix, Matrix4f cullingMatrix,
                                     GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        if (YesSteveModel.isAvailable()) {
            ModelPreviewRenderer.setFirstPersonMode(true);
            EntityRenderCache.tick(deltaTracker.getGameTimeDeltaPartialTick(false));
        }
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void ysm$renderLevelReturn(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker, boolean renderBlockOutline,
                                       Camera camera, Matrix4f projectionMatrix, Matrix4f frustumMatrix, Matrix4f cullingMatrix,
                                       GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
        if (YesSteveModel.isAvailable()) {
            EntityRenderCache.clear();
            ModelPreviewRenderer.setFirstPersonMode(false);
        }
    }
}
