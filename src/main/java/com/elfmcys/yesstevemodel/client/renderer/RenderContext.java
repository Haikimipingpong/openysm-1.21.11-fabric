package com.elfmcys.yesstevemodel.client.renderer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21.11 的 ItemInHandRenderer / 名牌绘制都需要 {@link SubmitNodeCollector} + {@link CameraRenderState}，
 * 而 YSM 的 layer 链路只传 MultiBufferSource。这里在进入 YSM 绘制前把当前 submit 上下文挂到渲染线程上，
 * 供 layer 内部取用（渲染线程单线程执行，无需加锁）。
 */
public final class RenderContext {

    private static final ThreadLocal<SubmitNodeCollector> COLLECTOR = new ThreadLocal<>();
    private static final ThreadLocal<CameraRenderState> CAMERA = new ThreadLocal<>();

    private RenderContext() {
    }

    public static void enter(SubmitNodeCollector collector, CameraRenderState cameraState) {
        COLLECTOR.set(collector);
        CAMERA.set(cameraState);
    }

    public static void exit() {
        COLLECTOR.remove();
        CAMERA.remove();
    }

    @Nullable
    public static SubmitNodeCollector collector() {
        return COLLECTOR.get();
    }

    @Nullable
    public static CameraRenderState camera() {
        return CAMERA.get();
    }
}
