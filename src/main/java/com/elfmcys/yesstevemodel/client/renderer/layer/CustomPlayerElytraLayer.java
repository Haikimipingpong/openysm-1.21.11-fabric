package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import com.mojang.math.Axis;
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper;

public class CustomPlayerElytraLayer extends GeoLayerRenderer<CustomPlayerEntity> {

    private static final Identifier WINGS_LOCATION = Identifier.withDefaultNamespace("textures/entity/equipment/wings/elytra.png");

    private final ElytraModel elytraModel;

    public CustomPlayerElytraLayer(EntityRendererProvider.Context context) {
        this.elytraModel = new ElytraModel(context.getModelSet().bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(AvatarRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn,
                       CustomPlayerEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        LivingEntity entity = entityLivingBaseIn.getEntity();
        ItemStack stack = CosmeticArmorHelper.getElytraItem(entity);
        AnimatedGeoModel animatedGeoModel = entityLivingBaseIn.getCurrentModel();
        if (stack == null || stack.isEmpty() || animatedGeoModel == null || animatedGeoModel.elytraBones().isEmpty()
                || !(entity instanceof AbstractClientPlayer abstractClientPlayer)) {
            return;
        }
        PlayerSkin skin = abstractClientPlayer.getSkin();
        Identifier cloakTextureLocation;
        if (skin.elytra() != null) {
            cloakTextureLocation = skin.elytra().texturePath();
        } else if (skin.cape() != null && abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE)) {
            cloakTextureLocation = skin.cape().texturePath();
        } else {
            cloakTextureLocation = WINGS_LOCATION;
        }
        poseStack.pushPose();
        renderElytra(poseStack, animatedGeoModel);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        this.elytraModel.setupAnim(state);
        this.elytraModel.renderToBuffer(poseStack,
                ItemRenderer.getFoilBuffer(bufferSource, RenderTypes.armorCutoutNoCull(cloakTextureLocation), false, stack.hasFoil()),
                packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }

    public void renderElytra(PoseStack poseStack, AnimatedGeoModel model) {
        RenderUtils.prepMatrixForLocator(poseStack, model.elytraBones());
    }
}
