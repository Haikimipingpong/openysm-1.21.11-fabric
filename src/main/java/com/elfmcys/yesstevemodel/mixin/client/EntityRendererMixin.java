package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.client.renderer.EntityRenderStateBindings;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.11 渲染拆成 extract/submit 两段，submit 阶段只有 render state。
 * 这里在 createRenderState 返回时把 state → entity 记下来，供 YSM 在 submit 阶段反查实体。
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(
            method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN")
    )
    private <T extends Entity, S extends EntityRenderState> void ysm$bindStateToEntity(T entity, float partialTick, CallbackInfoReturnable<S> cir) {
        EntityRenderStateBindings.bind(cir.getReturnValue(), entity);
    }
}
