package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.mixin.client.EntityRenderDispatcherAccessor;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import rip.ysm.api.PlatformAPI;
import rip.ysm.compat.sbackpack.SBackpackCompat;

public class RendererManager {

    private static CustomPlayerRenderer playerRenderer;

    private static ProjectileRenderer projectileRenderer;

    private static HandItemRenderer handRenderer;

    private static VehicleRenderer vehicleRenderer;

    private RendererManager() {
    }

    public static void register() {
        if (PlatformAPI.isServer()) {
            return;
        }
        ResourceManagerReloadListener listener = resourceManager -> resetRenderers();
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, listener, Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "renderer_manager"));
    }

    private static void resetRenderers() {
        playerRenderer = null;
        projectileRenderer = null;
        handRenderer = null;
        vehicleRenderer = null;
    }

    /**
     * YSM 的渲染器不注册到 EntityRenderers，而是自己造一个
     * {@link EntityRendererProvider.Context}（1.21.11 里它有 10 个构造参数）。
     */
    private static void initRenderers(ResourceManager resourceManager) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(
                dispatcher,
                minecraft.getItemModelResolver(),
                minecraft.getMapRenderer(),
                minecraft.getBlockRenderer(),
                resourceManager,
                minecraft.getEntityModels(),
                ((EntityRenderDispatcherAccessor) dispatcher).getEquipmentAssetManager(),
                minecraft.getAtlasManager(),
                minecraft.font,
                minecraft.playerSkinRenderCache());
        playerRenderer = new CustomPlayerRenderer(context);
        projectileRenderer = new ProjectileRenderer(context);
        handRenderer = new HandItemRenderer();
        vehicleRenderer = new VehicleRenderer(context);
        SBackpackCompat.setupRenderLayers();
    }

    public static CustomPlayerRenderer getPlayerRenderer() {
        if (playerRenderer == null) {
            initRenderers(Minecraft.getInstance().getResourceManager());
        }
        return playerRenderer;
    }

    public static ProjectileRenderer getProjectileRenderer() {
        if (projectileRenderer == null) {
            initRenderers(Minecraft.getInstance().getResourceManager());
        }
        return projectileRenderer;
    }

    public static HandItemRenderer getHandRenderer() {
        if (handRenderer == null) {
            initRenderers(Minecraft.getInstance().getResourceManager());
        }
        return handRenderer;
    }

    public static VehicleRenderer getVehicleRenderer() {
        if (vehicleRenderer == null) {
            initRenderers(Minecraft.getInstance().getResourceManager());
        }
        return vehicleRenderer;
    }
}
