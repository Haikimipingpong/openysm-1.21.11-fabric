package rip.ysm.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

/**
 * 1.21.11 的 9 参 {@code GuiGraphics#blit} 语义和 1.20.1 完全不同：
 *
 * <pre>
 * 1.20.1 : blit(tex, x, y, width, height, u, v, uWidth, vHeight[, texWidth, texHeight])
 * 1.21.11: blit(tex, x0, y0, x1, y1, u0, u1, v0, v1)   // 右下角坐标 + (u0,u1,v0,v1)
 * </pre>
 *
 * 移植时按旧语义写成了 {@code (x, y, w, h, u, v, uw, uh)}，导致 UV 退化（u0==u1、v0==v1），
 * 纹理采样成一个点、整个 quad 被裁掉 —— 表现就是模型图标/背景/头像全部只剩底色（紫块/灰块）。
 * <p>
 * 这个包装保持旧的调用形式，内部换算成 1.21.11 的坐标与 UV 顺序。
 */
public final class PortBlit {

    private PortBlit() {
    }

    /**
     * @param u0 归一化左上 U
     * @param v0 归一化左上 V
     * @param uw 归一化宽度
     * @param uh 归一化高度
     */
    public static void blit(GuiGraphics guiGraphics, Identifier texture, int x, int y, int width, int height,
                            float u0, float v0, float uw, float uh) {
        if (texture == null) {
            return;
        }
        GuiGraphics GRAPHICS = guiGraphics;
        GRAPHICS.blit(texture, x, y, x + width, y + height, u0, u0 + uw, v0, v0 + uh);
    }

    /** 像素 UV 版本（自动按纹理尺寸归一化）。 */
    public static void blitPixels(GuiGraphics guiGraphics, Identifier texture, int x, int y, int width, int height,
                                  float u, float v, float uw, float uh, float texWidth, float texHeight) {
        blit(guiGraphics, texture, x, y, width, height, u / texWidth, v / texHeight, uw / texWidth, uh / texHeight);
    }
}
