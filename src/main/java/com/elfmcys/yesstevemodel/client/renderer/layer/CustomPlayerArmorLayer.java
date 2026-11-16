package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.RenderContext;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import rip.ysm.compat.simplehats.SimpleHatsHelper;

public class CustomPlayerArmorLayer extends GeoLayerRenderer<CustomPlayerEntity> {

    private final ItemInHandRenderer itemRenderer;

    public CustomPlayerArmorLayer(EntityRendererProvider.Context context) {
        this.itemRenderer = net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
    }

    @Override
    public void render(AvatarRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn,
                       CustomPlayerEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        Player player = entityLivingBaseIn.getEntity();
        AnimatedGeoModel model = entityLivingBaseIn.getCurrentModel();
        if (model == null || model.headBones().isEmpty()) {
            return;
        }
        ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!stack.isEmpty() && !isArmorItem(stack)) {
            renderArmorPiece(poseStack, bufferSource, packedLightIn, model, player, stack);
        }
        ItemStack hatStack = SimpleHatsHelper.getHatItem(player);
        if (hatStack != null && !hatStack.isEmpty()) {
            renderArmorPiece(poseStack, bufferSource, packedLightIn, model, player, hatStack);
        }
    }

    private boolean isArmorItem(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot() == EquipmentSlot.HEAD;
    }

    private void renderArmorPiece(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                  AnimatedGeoModel model, Player player, ItemStack stack) {
        SubmitNodeCollector collector = RenderContext.collector();
        if (collector == null) {
            return;
        }
        poseStack.pushPose();
        RenderUtils.prepMatrixForLocator(poseStack, model.headBones());
        poseStack.scale(0.625f, 0.625f, 0.625f);
        poseStack.translate(0.0f, 0.25f, 0.0f);
        this.itemRenderer.renderItem(player, stack, ItemDisplayContext.HEAD, poseStack, collector, packedLight);
        poseStack.popPose();
    }
}
