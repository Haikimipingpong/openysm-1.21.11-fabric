package rip.ysm.gui;

/**
 * PORT: 1.21.11 删除了 RenderSystem.enableScissor/disableScissor（像素坐标 + 左下原点），
 * 改为 {@code GuiGraphics#enableScissor(int left, int top, int right, int bottom)}（GUI 缩放坐标）。
 *
 * 旧调用点传的是「像素坐标 + 左下原点」，需要 guiScale 和窗口高度才能换算，而且必须在持有
 * GuiGraphics 实例的地方调用。这里先降级为 no-op 保证编译与运行，视觉上是「裁剪失效」，
 * 即列表内容可能溢出边框。后续在各自 render 方法里换成 guiGraphics.enableScissor 即可。
 */
public final class PortScissor {

    private PortScissor() {
    }

    public static void enable(int xPixels, int yPixelsFromBottom, int widthPixels, int heightPixels) {
        // PORT: 裁剪暂未实现（见类注释）
    }

    public static void disable() {
        // PORT: 同上
    }
}
