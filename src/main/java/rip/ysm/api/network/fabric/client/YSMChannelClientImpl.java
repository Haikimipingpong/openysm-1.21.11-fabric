package rip.ysm.api.network.fabric.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import rip.ysm.api.network.fabric.YSMPayload;
import rip.ysm.api.network.fabric.YSMChannelImpl;

public final class YSMChannelClientImpl {

    private YSMChannelClientImpl() {
    }

    public static void init(Identifier channelId) {
        // PORT: 1.20.5+ 客户端接收器基于 CustomPacketPayload
        ClientPlayNetworking.registerGlobalReceiver(YSMPayload.type0(), (payload, context) -> YSMChannelImpl.dispatch(
                payload.toBuffer(),
                new ClientPacketContext(context.client(), context.client().getConnection().getConnection())));
    }

    public static void sendToServer(Identifier channelId, FriendlyByteBuf buf) {
        ClientPlayNetworking.send(toPayload(buf));
    }

    public static Packet<?> toServerboundPacket(Identifier channelId, FriendlyByteBuf buf) {
        return ClientPlayNetworking.createC2SPacket(toPayload(buf));
    }

    private static YSMPayload toPayload(FriendlyByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);
        return new YSMPayload(data);
    }
}
