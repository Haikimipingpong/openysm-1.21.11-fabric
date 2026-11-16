package rip.ysm.gpu;

import net.minecraft.client.gui.GuiGraphics;

/** PORT: 模糊同样依赖 GPU 路径，1.21.11 暂缓（背景不再模糊）。 */
public final class BlurStack {

    private BlurStack() {
    }

    public static void pushBlur(float x, float y, float w, float h, float cornerRadius, float blurRadius) {
        // PORT: 无操作
    }

    public static void pushBlur(float x, float y, float w, float h, float cornerRadius, float blurRadius, int tintRgba) {
        // PORT: 无操作
    }

    public static void pushBlurPie(float centerX, float centerY, float innerRadius, float outerRadius,
                                   float startAngle, float endAngle, float blurRadius) {
        // PORT: 无操作
    }

    public static void pushBlurPie(float centerX, float centerY, float innerRadius, float outerRadius,
                                   float startAngle, float endAngle, float blurRadius, int tintRgba) {
        // PORT: 无操作
    }

    public static void popBlur() {
        // PORT: 无操作
    }

    public static void clear() {
        // PORT: 无操作
    }

    public static boolean isEmpty() {
        return true;
    }

    public static void flush(GuiGraphics graphics) {
        // PORT: 无操作
    }
}
