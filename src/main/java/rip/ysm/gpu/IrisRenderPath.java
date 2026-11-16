package rip.ysm.gpu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import net.minecraft.resources.Identifier;

/** PORT: Iris/Oculus 着色器包下的 GPU 路径同样暂缓。 */
public final class IrisRenderPath {

    private IrisRenderPath() {
    }

    public static boolean tryRender(GeoModel model, PoseStack.Pose pose, float[] boneParams, int renderPartMask,
                                    int packedLight, int packedOverlay, float r, float g, float b, float a,
                                    Identifier textureLocation) {
        return false;
    }
}
