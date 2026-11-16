package rip.ysm.gpu;

import net.minecraft.client.gui.GuiGraphics;

/** PORT: 扇形绘制依赖 GPU 路径，1.21.11 暂缓（轮盘背景已降级为不绘制）。 */
public final class Pie {

    public static final float tau = (float) (Math.PI * 2.0);

    private Pie() {
    }

    public static void draw(GuiGraphics graphics, float centerX, float centerY, float innerRadius,
                            float outerRadius, float startAngle, float endAngle, int rgba) {
        draw(graphics, centerX, centerY, innerRadius, outerRadius, startAngle, endAngle, rgba, 1.0f);
    }

    public static void draw(GuiGraphics graphics, float centerX, float centerY, float innerRadius,
                            float outerRadius, float startAngle, float endAngle, int rgba, float feather) {
        // PORT: 无操作
    }
}
