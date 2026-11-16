package com.elfmcys.yesstevemodel.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.renderer.texture.AbstractTexture;
import rip.ysm.compat.oculus.ShadersTextureType;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * 外部模型自带的贴图（png/webp 字节数组）。
 * <p>
 * 1.21.11 里 {@code AbstractTexture} 没有 {@code load(ResourceManager)} 了，GPU 纹理要自己在渲染线程上
 * 通过 {@code GpuDevice} 创建。这里保持「构造只存字节」，真正上传交给 {@link #load()}，
 * 这样在客户端初始化阶段（GPU device 还没创建）解码模型也不会崩。
 */
public class OuterFileTexture extends AbstractTexture implements ITextureMap {

    private final byte[] data;

    private NativeImage loadedImage;

    private Map<ShadersTextureType, OuterFileTexture> suffixTextures = Reference2ReferenceMaps.emptyMap();

    public OuterFileTexture(byte[] data) {
        this.data = data;
    }

    /** 在渲染线程上创建 GPU 纹理；重复调用安全。 */
    public void load() {
        if (this.texture != null) {
            return;
        }
        if (RenderSystem.isOnRenderThread()) {
            this.doLoad();
        } else {
            RenderSystem.queueFencedTask(this::doLoad);
        }
    }

    public void doLoad() {
        if (this.texture != null) {
            return;
        }
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.queueFencedTask(this::doLoad);
            return;
        }
        NativeImage image = null;
        try {
            image = NativeImage.read(new ByteArrayInputStream(this.data));
        } catch (Throwable t) {
            // 作者头像/图标的原图可能是 jpg/webp，NativeImage 只吃 png，这里转一次
            image = decodeWithImageIO();
        }
        if (image == null) {
            // 兜底：1x1 洋红，保证 getTextureView() 不会因为 null 抛异常把界面搞崩
            image = new NativeImage(1, 1, false);
            image.setPixelABGR(0, 0, 0xFFFF00FF);
        }
        this.loadedImage = image;
        try {
            GpuDevice gpuDevice = RenderSystem.getDevice();
            this.texture = gpuDevice.createTexture("YSMOuterFile", 5, TextureFormat.RGBA8,
                    this.loadedImage.getWidth(), this.loadedImage.getHeight(), 1, 1);
            this.textureView = gpuDevice.createTextureView(this.texture);
            gpuDevice.createCommandEncoder().writeToTexture(this.texture, this.loadedImage);
            this.sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT,
                    FilterMode.NEAREST, FilterMode.NEAREST, false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Nullable
    private NativeImage decodeWithImageIO() {
        try {
            java.awt.image.BufferedImage buffered = javax.imageio.ImageIO.read(new ByteArrayInputStream(this.data));
            if (buffered == null) {
                return null;
            }
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(buffered, "png", out);
            return NativeImage.read(new ByteArrayInputStream(out.toByteArray()));
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public GpuTexture getTexture() {
        load();
        return super.getTexture();
    }

    @Override
    public GpuTextureView getTextureView() {
        load();
        if (this.texture == null) {
            return null;
        }
        return super.getTextureView();
    }

    @Override
    public GpuSampler getSampler() {
        load();
        return super.getSampler();
    }

    public NativeImage getLoadedImage() {
        return this.loadedImage;
    }

    public void setSuffixTextures(Map<ShadersTextureType, OuterFileTexture> map) {
        this.suffixTextures = Reference2ReferenceMaps.unmodifiable(new Reference2ReferenceOpenHashMap<>(map));
    }

    @Override
    public Map<ShadersTextureType, ? extends AbstractTexture> getSuffixTextures() {
        return this.suffixTextures;
    }
}
