package rip.ysm.gpu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/** PORT: 整条 GPU 渲染路径在 1.21.11 暂缓（见 GpuCapability 注释）。 */
public final class GpuRenderPath {

    private GpuRenderPath() {
    }

    public static boolean tryRender(GeoModel model, PoseStack.Pose pose, float[] boneParams, float[] stateBuffer,
                                    int textureIndex, int renderPartMask, int packedLight, int packedOverlay,
                                    float red, float green, float blue, float alpha,
                                    @Nullable Identifier textureLocation) {
        return false;
    }

    public static void disposeMesh(GeoModel model) {
        // PORT: 无操作
    }
}
