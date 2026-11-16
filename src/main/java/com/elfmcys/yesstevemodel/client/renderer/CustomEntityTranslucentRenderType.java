package com.elfmcys.yesstevemodel.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Util;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public final class CustomEntityTranslucentRenderType {

    private CustomEntityTranslucentRenderType() {
    }

    /** PORT: 1.21.11 的 RenderType 构造器改为 (String, RenderSetup)，自定义半透明类型暂用标准实现。 */
    public static net.minecraft.client.renderer.rendertype.RenderType get(Identifier location) {
        return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(location);
    }
}
