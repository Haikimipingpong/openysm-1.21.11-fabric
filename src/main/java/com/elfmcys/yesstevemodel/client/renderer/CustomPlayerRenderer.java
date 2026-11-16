package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerArmorLayer;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerElytraLayer;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerItemInHandLayer;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerParrotLayer;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.NotNull;
import rip.ysm.compat.gun.swarfare.SWarfareCompat;
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat;

/**
 * YSM 的玩家模型渲染器。它不由 {@code EntityRenderers} 注册，而是由 {@link RendererManager}
 * 手动实例化，并在原版 {@code AvatarRenderer#submit} 里被调用（见 {@code PlayerRendererMixin}）。
 */
public class CustomPlayerRenderer extends GeoReplacedEntityRenderer<Player, CustomPlayerEntity, AvatarRenderState> {

    private Identifier currentTexture;

    private Player currentPlayer;

    public CustomPlayerRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayerRenderer(new CustomPlayerItemInHandLayer(Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer()));
        addLayerRenderer(new CustomPlayerElytraLayer(context));
        addLayerRenderer(new CustomPlayerParrotLayer(context));
        addLayerRenderer(new CustomPlayerArmorLayer(context));
    }

    public void render(Player player, AvatarRenderState renderState, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        PlayerCapability capability;
        if (SWarfareCompat.isPlayerAiming(player) || (capability = PlayerCapability.get(player).orElse(null)) == null) {
            return;
        }
        this.currentPlayer = player;
        capability.tickModel();
        SpecialPlayerRenderEvent renderEvent = new SpecialPlayerRenderEvent(player, capability, capability.getModelId());
        this.currentTexture = renderEvent.getTextureLocation();
        if (SpecialPlayerRenderEvent.post(renderEvent).isFalse()) {
            return;
        }
        // 左上角纸娃娃：把身体朝向临时钉死，头（yHeadRot）保持玩家真实朝向 → 身体不动、头跟着转
        boolean paperdoll = ModelPreviewRenderer.isPaperdollState(renderState);
        float savedBodyRot = 0.0f;
        float savedBodyRotO = 0.0f;
        float savedYRot = 0.0f;
        float savedYRotO = 0.0f;
        float yaw = entityYaw;
        if (paperdoll) {
            savedBodyRot = player.yBodyRot;
            savedBodyRotO = player.yBodyRotO;
            savedYRot = player.getYRot();
            savedYRotO = player.yRotO;
            yaw = renderState.bodyRot;
            player.yBodyRot = yaw;
            player.yBodyRotO = yaw;
            player.setYRot(yaw);
            player.yRotO = yaw;
        }
        try {
            renderEntityWithTexture(capability, renderState, renderEvent.getTextureLocation(), yaw, partialTick, poseStack, bufferSource, packedLight);
        } finally {
            if (paperdoll) {
                player.yBodyRot = savedBodyRot;
                player.yBodyRotO = savedBodyRotO;
                player.setYRot(savedYRot);
                player.yRotO = savedYRotO;
            }
        }
    }

    /**
     * 模型选择页卡片预览：模型/贴图直接取自卡片自己的 animatable，不经过 PlayerCapability。
     */
    public void renderPreview(CustomPlayerEntity animatable, AvatarRenderState renderState, float partialTick,
                              PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (animatable == null || animatable.isDisabledState() || animatable.getCurrentModel() == null) {
            return;
        }
        this.currentPlayer = animatable.getEntity();
        this.currentTexture = animatable.getTextureLocation();
        renderEntityWithTexture(animatable, renderState, this.currentTexture, renderState.bodyRot, partialTick,
                poseStack, bufferSource, packedLight);
    }

    @Override
    public boolean shouldShowName(Player entity, double distance) {
        float nameRenderDistance = entity.isDiscrete() ? 32.0f : 64.0f;
        LocalPlayer localPlayer;
        Minecraft minecraft;
        if (distance >= (double) (nameRenderDistance * nameRenderDistance)
                || (localPlayer = (minecraft = Minecraft.getInstance()).player) == null) {
            return false;
        }
        boolean isVisible = !entity.isInvisibleTo(localPlayer);
        if (entity != localPlayer) {
            Team team = entity.getTeam();
            Team team2 = localPlayer.getTeam();
            if (team != null) {
                switch (team.getNameTagVisibility()) {
                    case ALWAYS:
                        return isVisible;
                    case NEVER:
                        return false;
                    case HIDE_FOR_OTHER_TEAMS:
                        return team2 == null ? isVisible : team.isAlliedTo(team2) && (team.canSeeFriendlyInvisibles() || isVisible);
                    case HIDE_FOR_OWN_TEAM:
                        return team2 == null ? isVisible : !team.isAlliedTo(team2) && isVisible;
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
        }
        return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && isVisible && !entity.isVehicle();
    }

    @Override
    @NotNull
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @NotNull
    public Identifier getTextureLocation(Player player) {
        return this.currentTexture == null
                ? PlayerCapability.get(player).map((cap) -> cap.getTextureLocation()).orElse(MissingTextureAtlasSprite.getLocation())
                : this.currentTexture;
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState state) {
        if (this.currentTexture != null) {
            return this.currentTexture;
        }
        Player player = this.currentPlayer;
        if (player == null) {
            player = Minecraft.getInstance().player;
        }
        return player != null
                ? PlayerCapability.get(player).map(LivingAnimatable::getTextureLocation).orElse(MissingTextureAtlasSprite.getLocation())
                : MissingTextureAtlasSprite.getLocation();
    }

    @Override
    public void setupRotations(Player player, AvatarRenderState state, PoseStack poseStack, float ageInTicks,
                               float rotationYaw, float partialTicks, float scale) {
        super.setupRotations(player, state, poseStack, ageInTicks, rotationYaw, partialTicks, scale);
        Entity vehicle = player.getVehicle();
        if (TouhouLittleMaidCompat.isSimplePlanesEntity(vehicle) || TouhouLittleMaidCompat.isImmersiveAircraftEntity(vehicle)) {
            poseStack.translate(0.0, 0.5, 0.0);
        }
    }
}
