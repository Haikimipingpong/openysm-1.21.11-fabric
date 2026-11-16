package com.elfmcys.yesstevemodel.client.renderer;

import com.google.common.collect.MapMaker;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 1.21.11 的实体渲染是「两段式」：extractRenderState 只把数据搬进 state，submit 阶段拿不到实体。
 * YSM 的 geckolib 绘制需要活实体（molang 查询、capability、骨骼动画），所以在这里维护
 * 「render state → 实体」的弱引用映射，由 {@code EntityRendererMixin} 在 createRenderState 返回时写入。
 */
public final class EntityRenderStateBindings {

    private static final Map<EntityRenderState, Entity> BINDINGS = new MapMaker().weakKeys().makeMap();

    private EntityRenderStateBindings() {
    }

    public static void bind(EntityRenderState state, Entity entity) {
        if (state != null && entity != null) {
            BINDINGS.put(state, entity);
        }
    }

    @Nullable
    public static Entity get(@Nullable EntityRenderState state) {
        return state == null ? null : BINDINGS.get(state);
    }
}
