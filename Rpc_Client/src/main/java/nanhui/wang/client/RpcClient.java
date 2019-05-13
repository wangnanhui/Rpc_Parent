package nanhui.wang.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import util.RpcUtil;

/**
 *
 */
public class RpcClient {

    private String host;
    private int port;

    private RpcUtil.RpcResponseProto responseProto;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;

    }

    public RpcUtil.RpcResponseProto connect(final RpcUtil.RpcRequestProto req) {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();

            final RpcClientHandler handler = new RpcClientHandler(req);//自己的处理逻辑 其实啥也没处理 就是直接返回了

            bootstrap.group(group).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                    .option(ChannelOption.TCP_NODELAY, true).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            /**
                             * 处理半包和粘包
                             */
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());

                            //序列化respinse
                            pipeline.addLast(new ProtobufDecoder(RpcUtil.RpcResponseProto.getDefaultInstance()));
                            //netty 对protobuf支持
                            pipeline.addLast(new ProtobufEncoder());
                            pipeline.addLast(handler);

                        }
                    });


            ChannelFuture future = bootstrap.connect(host, port).sync();//链接到Server端去执行方法
            future.channel().closeFuture().sync();
            responseProto = handler.getResponse();

            return responseProto;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return responseProto;
        } finally {
            group.shutdownGracefully();
            return responseProto;
        }

    }

}


class RpcClientHandler extends ChannelHandlerAdapter {
    private RpcUtil.RpcRequestProto requestProto;
    private RpcUtil.RpcResponseProto proto;

    public RpcClientHandler(RpcUtil.RpcRequestProto requestProto) {
        this.requestProto = requestProto;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        proto = (RpcUtil.RpcResponseProto) msg;
        ctx.close();


    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(requestProto);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        ctx.flush();


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public RpcUtil.RpcResponseProto getResponse() {


        return proto;

    }
}