package rip.ysm.gpu;

/**
 * PORT: 1.21.6+ 引入 GpuDevice/RenderPipeline 抽象后，YSM 原生的「裸 GL + 自定义 shader」路径
 * 无法沿用。这里直接把能力查询钉死为不可用，让上层回退到 native/geckolib 的 CPU 顶点路径。
 * （macOS 上原本也因 GL 4.1 限制不可用，所以对当前平台行为无变化。）
 */
public final class GpuCapability {

    private GpuCapability() {
    }

    public static boolean isAvailable() {
        return false;
    }

    public static String getReason() {
        return "GPU renderer is not ported to the 1.21.11 render pipeline yet";
    }

    public static void check() {
        // PORT: 无操作
    }
}
