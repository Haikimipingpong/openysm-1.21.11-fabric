package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.geckolib3.extended.LivingEntityRendererAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingRendererMixin extends EntityRenderer<LivingEntity, LivingEntityRenderState> implements LivingEntityRendererAccessor {

    public LivingRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Unique
    @Override
    public void tlm$renderNameTag(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        super.submitNameTag(state, poseStack, collector, cameraState);
    }
}
