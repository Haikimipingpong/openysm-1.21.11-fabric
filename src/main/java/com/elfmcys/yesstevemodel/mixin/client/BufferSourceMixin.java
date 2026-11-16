package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.util.accessors.BufferSourceAccessor;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;
import java.util.SequencedMap;

/**
 * 1.21.11 的 {@code MultiBufferSource.BufferSource#fixedBuffers} 类型改成了
 * {@code SequencedMap<RenderType, ByteBufferBuilder>}（旧版是 {@code Map<RenderType, BufferBuilder>}）。
 * YSM 需要在「先画模型、再画手持物品」之间强制刷新固定缓冲，避免手持物品被模型遮挡。
 */
@Mixin(MultiBufferSource.BufferSource.class)
public class BufferSourceMixin implements BufferSourceAccessor {

    @Shadow
    @Final
    protected SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers;

    @Unique
    @Override
    public void initialize() {
        Iterator<RenderType> it = this.fixedBuffers.keySet().iterator();
        while (it.hasNext()) {
            ((MultiBufferSource.BufferSource) (Object) this).endBatch(it.next());
        }
    }
}
