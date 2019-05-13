package util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

public class MessagePackDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf o, List<Object> list) throws Exception {

        final byte[] bytes;
        final int length = o.readableBytes();
        bytes = new byte[length];
        o.getBytes(o.readerIndex(), bytes, 0, length);
        MessagePack pack = new MessagePack();
        list.add(pack.read(bytes));


    }
}
