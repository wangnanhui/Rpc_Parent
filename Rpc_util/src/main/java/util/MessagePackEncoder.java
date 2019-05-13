package util;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.List;

public class MessagePackEncoder extends MessageToMessageEncoder<Object> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List<Object> list) throws IOException {
        try {
            MessagePack pack = new MessagePack();
            byte[] bytes = pack.write(o);

            ((ByteBuf) list.get(0)).writeBytes(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
