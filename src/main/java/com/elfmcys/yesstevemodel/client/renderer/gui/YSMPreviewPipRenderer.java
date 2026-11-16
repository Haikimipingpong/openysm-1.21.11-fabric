package com.elfmcys.yesstevemodel.client.renderer.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 模型卡 / 贴图卡的 3D 预览。
 * <p>
 * 原版 1.21.11 的 {@code GuiEntityRenderer} 每种 state 只维护**一个**离屏纹理，
 * 一帧里提交多个实体会互相覆盖（模型选择页一屏有 10 张卡 + 左侧纸娃娃）。
 * 这里给每个卡位单独建一个 {@link GuiEntityRenderer} 实例，各自持有纹理和投影，
 * 渲染仍走原版的 {@code submitEntityRenderState} 路径，尺寸/位置与左面板完全一致。
 */
public final class YSMPreviewPipRenderer {

    private static final YSMPreviewPipRenderer INSTANCE = new YSMPreviewPipRenderer();

    private final Map<Integer, GuiEntityRenderer> renderers = new LinkedHashMap<>();

    private YSMPreviewPipRenderer() {
    }

    public static YSMPreviewPipRenderer get() {
        return INSTANCE;
    }

    public void prepare(int slot, GuiEntityRenderState state, GuiRenderState guiRenderState, int guiScale) {
        Minecraft minecraft = Minecraft.getInstance();
        GuiEntityRenderer renderer = this.renderers.computeIfAbsent(slot,
                key -> new GuiEntityRenderer(minecraft.renderBuffers().bufferSource(),
                        minecraft.getEntityRenderDispatcher()));
        renderer.prepare(state, guiRenderState, guiScale);
    }

    /** 界面关闭 / 资源重载时调用，释放所有离屏纹理。 */
    public void clear() {
        this.renderers.values().forEach(GuiEntityRenderer::close);
        this.renderers.clear();
    }
}
