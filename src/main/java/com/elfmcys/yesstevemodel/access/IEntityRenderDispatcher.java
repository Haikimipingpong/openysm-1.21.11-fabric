package com.elfmcys.yesstevemodel.access;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface IEntityRenderDispatcher {

    @Nullable
    Entity ysm$getEntityForState(EntityRenderState state);
}
