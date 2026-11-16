package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.VehicleCapability;
import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import rip.ysm.compat.firstperson.FirstPersonCompat;
import rip.ysm.compat.oculus.OculusCompat;
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat;
import com.elfmcys.yesstevemodel.client.animation.AnimationTracker;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.resource.models.GeometryDescription;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.util.AnimatableCacheUtil;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.google.common.collect.MapMaker;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import com.mojang.math.Axis;

public final class ModelPreviewRenderer {

    private static boolean isPreviewMode = false;

    /**
     * 由 {@link #submitPlayerPreview} 提交的 render state。
     * <p>
     * 1.21.11 的 GUI 实体是「提交后延迟渲染」的：{@code submitEntityRenderState} 只入队，
     * 真正的 {@code AvatarRenderer#submit} 发生在同一帧的 GUI flush 阶段，那时
     * {@link #setPreviewMode} 早已被复位。所以预览标记必须挂在 state 上，不能靠临时 flag。
     */
    private static final Set<EntityRenderState> PREVIEW_STATES =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static void markPreviewState(EntityRenderState state) {
        if (state != null) {
            PREVIEW_STATES.add(state);
        }
    }

    public static boolean isPreviewState(EntityRenderState state) {
        return state != null && PREVIEW_STATES.contains(state);
    }

    /** 模型卡预览：state 对应的不是世界玩家，而是卡片自己的 animatable。 */
    private static final Map<EntityRenderState, LivingAnimatable<?>> PREVIEW_ANIMATABLES =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static void bindPreviewAnimatable(EntityRenderState state, LivingAnimatable<?> animatable) {
        if (state != null && animatable != null) {
            PREVIEW_ANIMATABLES.put(state, animatable);
        }
    }

    public static LivingAnimatable<?> getPreviewAnimatable(EntityRenderState state) {
        return state == null ? null : PREVIEW_ANIMATABLES.get(state);
    }

    private static boolean isExtraPlayerMode = false;

    private static boolean isFirstPersonMode = false;

    public static void setPreviewMode(boolean previewMode) {
        isPreviewMode = previewMode;
    }

    public static boolean isPreview() {
        return isPreviewMode;
    }

    public static void setExtraPlayerMode(boolean extraPlayerMode) {
        isExtraPlayerMode = extraPlayerMode;
    }

    public static boolean isExtraPlayer() {
        return isExtraPlayerMode;
    }

    public static void setFirstPersonMode(boolean firstPersonMode) {
        isFirstPersonMode = firstPersonMode;
    }

    public static boolean isFirstPerson() {
        return isFirstPersonMode || OculusCompat.isPBRActive() || FirstPersonCompat.isFirstPersonActive();
    }

    public static boolean isFirstPersonOnRenderThread() {
        RenderSystem.assertOnRenderThread();
        return isFirstPersonMode && !FirstPersonCompat.isFirstPersonActive();
    }

    public static void renderVehicleModel(Entity entity, PoseStack poseStack, float partialTick) {
        // PORT: 1.21.11 的实体预览要改走 GuiGraphics#submitEntityRenderState（新提交式管线），暂未实现
    }

    // 贴图选择页右侧的大预览（自由旋转）
    public static <T extends Player, TAnimatable extends LivingAnimatable<T>, S extends AvatarRenderState> void renderEntityPreview(
            GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, float scale, float pitch, float yaw,
            float offsetX, float offsetY, float partialTick, TAnimatable animatable,
            GeoReplacedEntityRenderer<T, TAnimatable, S> renderer, boolean renderGround) {
        if (animatable == null || x1 <= x0 || y1 <= y0 || scale <= 0.0f) {
            return;
        }
        AvatarRenderState state = new AvatarRenderState();
        state.lightCoords = 15728880;
        // 自由旋转：yaw 直接映射到身体朝向（模型默认朝 -Z，setupRotations 会做 180-yaw）
        state.bodyRot = 180.0f - yaw;
        state.yRot = 0.0f;
        state.xRot = 0.0f;
        state.boundingBoxWidth = 0.6f;
        state.boundingBoxHeight = 1.8f;
        state.scale = 1.0f;

        float tiltDegrees = -10.0f + pitch;
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf tilt = new Quaternionf().rotateX(tiltDegrees * ((float) Math.PI / 180.0f));
        rotation.mul(tilt);
        // zoom 沿用旧实现的「物理像素/格」，而 submitEntityRenderState 的 size 是 GUI 单位
        // 旧实现的 zoom 是「屏幕像素」，这里按面板大小换算成 GUI 单位（0.55 是实测比例）
        float guiScaleSize = Math.max(1.0f, scale * 0.55f);
        Vector3f translation = new Vector3f(offsetX / guiScaleSize, 0.9f, 0.0f);

        markPreviewState(state);
        bindPreviewAnimatable(state, animatable);
        EntityRenderStateBindings.bind(state, animatable.getEntity());
        guiGraphics.submitEntityRenderState(state, guiScaleSize, translation, rotation, tilt, x0, y0, x1, y1);
    }

    private static void renderBedPreview(float scale, float pitch, float yaw, MultiBufferSource.BufferSource bufferSource) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.0d, 0.0d, 1000.0d);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0d, 0.8d, 0.0d);
        Quaternionf rotationZ = Axis.ZP.rotationDegrees(180.0f);
        rotationZ.mul(Axis.XP.rotationDegrees((-10.0f) + pitch));
        poseStack.mulPose(rotationZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0f));
        poseStack.translate(-0.5d, 0.0d, 0.5d);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.RED_BED.defaultBlockState(), poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
    }

    private static void renderGroundPreview(float scale, float pitch, float yaw, MultiBufferSource.BufferSource bufferSource) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.0d, 0.0d, 1000.0d);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0d, 0.8d, 0.0d);
        Quaternionf rotationZ = Axis.ZP.rotationDegrees(180.0f);
        rotationZ.mul(Axis.XP.rotationDegrees((-10.0f) + pitch));
        poseStack.mulPose(rotationZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.translate(-1.5d, -1.0d, -2.5d);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                poseStack.translate(0.0f, 0.0f, 1.0f);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.GRASS_BLOCK.defaultBlockState(), poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
            }
            poseStack.translate(1.0f, 0.0f, -3.0f);
        }

        poseStack.translate(-1.0f, 1.0f, 1.0f);
        // PORT-DROP: Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.GRASS.defaultBlockState(), poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
        poseStack.translate(0.0f, 0.0f, 1.0f);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.RED_TULIP.defaultBlockState(), poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
    }

    private static void renderVehicleForAnimation(float yaw, AnimatableEntity animatableEntity, float partialTick, PoseStack poseStack, EntityRenderDispatcher entityRenderDispatcher, MultiBufferSource.BufferSource bufferSource) throws ExecutionException {
        Entity entity = animatableEntity.getEntity();
        AnimationTracker animationTracker = ((IPreviewAnimatable) animatableEntity).getAnimationStateMachine();

        if (animationTracker.isCurrentAnimation("ride")) {
            // PORT-DROP: renderVehicleEntity(yaw, entity, poseStack, entityRenderDispatcher, bufferSource, AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(EntityType.HORSE), () -> EntityType.HORSE.create(entity.level())), partialTick);
        } else if (animationTracker.isCurrentAnimation("ride_pig")) {
            // PORT-DROP: renderVehicleEntity(yaw, entity, poseStack, entityRenderDispatcher, bufferSource, AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(EntityType.PIG), () -> EntityType.PIG.create(entity.level())), partialTick);
        } else if (animationTracker.isCurrentAnimation("boat")) {
            // PORT-DROP: renderVehicleEntity(yaw, entity, poseStack, entityRenderDispatcher, bufferSource, AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(EntityType.OAK_BOAT), () -> EntityType.OAK_BOAT.create(entity.level())), partialTick);
        }
    }

    private static void renderVehicleEntity(float yaw, Entity riderEntity, PoseStack poseStack, EntityRenderDispatcher entityRenderDispatcher, MultiBufferSource.BufferSource bufferSource, Entity vehicleEntity, float partialTick) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        // PORT-DROP: entityRenderDispatcher.render(vehicleEntity, 0.0d, (-vehicleEntity.getPassengersRidingOffset()) - riderEntity.getMyRidingOffset(), 0.0d, 0.0f, partialTick, poseStack, bufferSource, 15728880);
        poseStack.popPose();
    }

    // 模型预览页面（模型卡里的缩略图）
    /**
     * 复用原版 {@code submitEntityRenderState} + {@code GuiEntityRenderer} 来做卡片预览：
     * 变换/尺寸与左面板纸娃娃完全一致，只是把 state 绑到卡片自己的 {@link PlayerPreviewEntity}。
     * 每个卡位在 {@link com.elfmcys.yesstevemodel.client.renderer.gui.YSMPreviewPipRenderer} 里有独立的
     * {@code GuiEntityRenderer} 实例，避免原版单纹理互相覆盖。
     */
    public static <T extends Player, TAnimatable extends LivingAnimatable<T>, S extends AvatarRenderState> void renderLivingEntityPreview(
            GuiGraphics guiGraphics, int x, int y, int width, int height, float scale, float partialTick,
            TAnimatable animatable, GeoReplacedEntityRenderer<T, TAnimatable, S> renderer,
            boolean disablePreviewRotation, boolean hideEquipment) {
        if (animatable == null || width <= 0 || height <= 0) {
            return;
        }
        AvatarRenderState state = new AvatarRenderState();
        state.lightCoords = 15728880;
        state.bodyRot = 180.0f;
        state.yRot = 0.0f;
        state.xRot = 0.0f;
        state.boundingBoxWidth = 0.6f;
        state.boundingBoxHeight = 1.8f;
        state.scale = 1.0f;

        float tiltDegrees = disablePreviewRotation ? 0.0f : -10.0f;
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf tilt = new Quaternionf().rotateX(tiltDegrees * ((float) Math.PI / 180.0f));
        rotation.mul(tilt);
        Vector3f translation = new Vector3f(0.0f, 0.9f, 0.0f);

        markPreviewState(state);
        bindPreviewAnimatable(state, animatable);
        // PlayerRendererMixin 需要 state -> 实体 的绑定才会进入 YSM 接管流程
        com.elfmcys.yesstevemodel.client.renderer.EntityRenderStateBindings.bind(state, animatable.getEntity());
        guiGraphics.submitEntityRenderState(state, scale, translation, rotation, tilt, x, y, x + width, y + height);
    }

    /** 每个卡位固定一个离屏纹理槽（同一个 PlayerPreviewEntity 永远对应同一槽）。 */
    private static final Map<LivingAnimatable<?>, Integer> PREVIEW_SLOTS = new MapMaker().weakKeys().makeMap();

    public static int slotOf(LivingAnimatable<?> animatable) {
        synchronized (PREVIEW_SLOTS) {
            Integer existing = PREVIEW_SLOTS.get(animatable);
            if (existing != null) {
                return existing;
            }
            int slot = PREVIEW_SLOTS.size();
            PREVIEW_SLOTS.put(animatable, slot);
            return slot;
        }
    }

    // 纸娃娃
    /**
     * 1.21.11 的 GUI 实体预览：用 {@code GuiGraphics#submitEntityRenderState} 把玩家的
     * render state 交给 {@code GuiEntityRenderer}（离屏渲染后贴回界面）。
     * <p>
     * 关键点是它内部会走 {@code EntityRenderDispatcher#submit} → {@code AvatarRenderer#submit}，
     * 也就是会命中我们的 {@code PlayerRendererMixin}，因此预览里画的是 YSM 模型而不是原版皮肤。
     */
    public static void submitPlayerPreview(GuiGraphics guiGraphics, LocalPlayer player, int x0, int y0, int x1, int y1,
                                           int size, float yOffset, float mouseX, float mouseY, float yawOffset) {
        submitPlayerPreview(guiGraphics, player, x0, y0, x1, y1, size, yOffset, mouseX, mouseY, yawOffset, 0.0f, 0.0f);
    }

    /** @param headYaw 头部相对身体的偏航（度）；@param headPitch 头部俯仰（度） */
    public static void submitPlayerPreview(GuiGraphics guiGraphics, LocalPlayer player, int x0, int y0, int x1, int y1,
                                           int size, float yOffset, float mouseX, float mouseY, float yawOffset,
                                           float headYaw, float headPitch) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderer<? super LivingEntity, ?> renderer = minecraft.getEntityRenderDispatcher().getRenderer(player);
        EntityRenderState renderState = renderer.createRenderState(player, 1.0f);
        if (!(renderState instanceof LivingEntityRenderState state)) {
            return;
        }
        state.lightCoords = 15728880;
        state.shadowPieces.clear();
        state.outlineColor = 0;

        float centerX = (x0 + x1) / 2.0f;
        float centerY = (y0 + y1) / 2.0f;
        float yaw = (float) Math.atan((centerX - mouseX) / 40.0f);
        float pitch = (float) Math.atan((centerY - mouseY) / 40.0f);
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf tilt = new Quaternionf().rotateX(pitch * 20.0f * ((float) Math.PI / 180.0f));
        rotation.mul(tilt);

        // bodyRot 是身体的绝对朝向，yRot 是「头相对身体」的偏航（原版 InventoryScreen 里
        // bodyRot = 180 + look，yRot = look）。之前把 180 也加到了 yRot 上，头会被拧 180°，
        // 表现为「身体朝后、脸却朝前」的错位渲染。
        String debugYaw = System.getProperty("ysm.previewYaw", "0");
        float extraYaw = "spin".equals(debugYaw)
                ? (System.currentTimeMillis() % 6000L) / 6000.0f * 360.0f
                : Float.parseFloat(debugYaw);
        state.bodyRot = 180.0f + yaw * 20.0f + yawOffset + extraYaw;
        state.yRot = yaw * 20.0f + headYaw;
        state.xRot = -pitch * 20.0f + headPitch;
        if (state.scale != 0.0f) {
            state.boundingBoxWidth /= state.scale;
            state.boundingBoxHeight /= state.scale;
            state.scale = 1.0f;
        }
        Vector3f translation = new Vector3f(0.0f, state.boundingBoxHeight / 2.0f + yOffset, 0.0f);
        markPreviewState(state);
        guiGraphics.submitEntityRenderState(state, size, translation, rotation, tilt, x0, y0, x1, y1);
    }

    /** 纸娃娃的 render state：渲染时要临时把实体朝向钉死，让身体不再跟着玩家转。 */
    private static final Set<EntityRenderState> PAPERDOLL_STATES =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static boolean isPaperdollState(EntityRenderState state) {
        return state != null && PAPERDOLL_STATES.contains(state);
    }

    /**
     * 左上角纸娃娃：身体固定 180°正对镜头，头跟着玩家（yRot/xRot 来自 render state）。
     * 注意不能用 markPreviewState —— 那会走模型卡预览分支（需要绑定 animatable），
     * 这里改成在 {@link CustomPlayerRenderer#render} 里临时改实体朝向。
     */
    public static void submitPlayerOverlay(GuiGraphics guiGraphics, LocalPlayer localPlayer, double x, double y,
                                           float scale, float yawOffset, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderer<? super LivingEntity, ?> renderer = minecraft.getEntityRenderDispatcher().getRenderer(localPlayer);
        if (renderer == null) {
            return;
        }
        EntityRenderState lifecycleState = renderer.createRenderState(localPlayer, partialTick);
        if (!(lifecycleState instanceof LivingEntityRenderState state)) {
            return;
        }
        state.lightCoords = 15728880;
        state.shadowPieces.clear();
        if (state.scale != 0.0f) {
            state.boundingBoxWidth /= state.scale;
            state.boundingBoxHeight /= state.scale;
            state.scale = 1.0f;
        }
        state.bodyRot = 180.0f + yawOffset;
        PAPERDOLL_STATES.add(state);
        int x0 = Math.round((float) x);
        int x1 = Math.round((float) x + scale);
        int y0 = Math.round((float) y);
        int y1 = Math.round((float) y + scale * 2.0f);
        Vector3f translation = new Vector3f(0.0f, state.boundingBoxHeight / 2.0f, 0.0f);
        guiGraphics.submitEntityRenderState(state, scale, translation,
                new Quaternionf().rotateZ((float) Math.PI), null, x0, y0, x1, y1);
    }

    public static void renderPlayerOverlay(GuiGraphics guiGraphics, LocalPlayer localPlayer, double x, double y,
                                           float scale, float yawOffset, int zDepth, float partialTick) {
        submitPlayerOverlay(guiGraphics, localPlayer, x, y, scale, yawOffset, partialTick);
    }

}
