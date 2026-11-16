package rip.ysm.api.network.fabric;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * PORT: 1.20.5 起 Fabric 网络必须是 {@link CustomPacketPayload}，不能再直接发裸 {@link FriendlyByteBuf}。
 * YSM 的包体是「1 字节 discriminator + 自定义编码」，所以这里只做一层原始字节的壳，
 * 内部的 discriminator / 分片协议完全不变。
 */
public final class YSMPayload implements CustomPacketPayload {

    /** 与上游 FRAGMENT 上限保持一致，这里放宽到 2MB（分片后的单包远小于此值）。 */
    public static final int MAX_SIZE = 2 * 1024 * 1024;

    private static volatile CustomPacketPayload.Type<YSMPayload> currentType;

    private final byte[] data;

    public YSMPayload(byte[] data) {
        this.data = data;
    }

    public byte[] data() {
        return data;
    }

    public FriendlyByteBuf toBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
    }

    /** 由 {@code YSMChannelImpl.init} 在注册前调用，绑定本模组的 channel id。 */
    public static CustomPacketPayload.Type<YSMPayload> bind(Identifier channel) {
        CustomPacketPayload.Type<YSMPayload> type = new CustomPacketPayload.Type<>(channel);
        currentType = type;
        return type;
    }

    public static CustomPacketPayload.Type<YSMPayload> type0() {
        return currentType;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, YSMPayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeByteArray(payload.data),
            buf -> new YSMPayload(buf.readByteArray(MAX_SIZE))
    );

    @Override
    public CustomPacketPayload.Type<YSMPayload> type() {
        return currentType;
    }
}
