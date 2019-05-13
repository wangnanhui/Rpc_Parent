package nanhui.wang.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import nanhui.wang.rpc.registry.ZkClientUtil;
import org.msgpack.MessagePack;
import util.RpcService;
import util.RpcUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcServer {

    static Map<String, RpcServer> cache = new ConcurrentHashMap<String, RpcServer>();

    private int port;
    private String host;


    public RpcServer(int port, String host) {

        this.port = port;
        this.host = host;
    }


    public void bind(final Object service) {

        RpcService rpc = service.getClass().getAnnotation(RpcService.class);

        ZkClientUtil.register(host, port, rpc.name());//注册服务的地址


        if (!cache.containsKey(service.getClass().getName())) {
            cache.put(service.getClass().getName(), this);
        }


        EventLoopGroup worker = new NioEventLoopGroup();
        EventLoopGroup boss = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).option(ChannelOption.SO_BACKLOG, 1024).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    //和client端处理类似 添加半包粘包支持
                    pipeline.addLast(new ProtobufVarint32FrameDecoder());
                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    //request 反序列化
                    pipeline.addLast(new ProtobufDecoder(RpcUtil.RpcRequestProto.getDefaultInstance()));
                    //添加netty对protobuf的支持
                    pipeline.addLast(new ProtobufEncoder());
                    //添加自己的业务逻辑
                    pipeline.addLast(new ServerHandler(service));
                }
            });
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }

    }

}


class ServerHandler extends ChannelHandlerAdapter {

    private Object service;

    public ServerHandler(Object service) {
        this.service = service;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        RpcUtil.RpcRequestProto req = (RpcUtil.RpcRequestProto) msg;

        Object obj = invoke(req);

        ctx.writeAndFlush(obj);


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 用反射去查找Service中方法
     *
     * @param req
     * @return
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invoke(RpcUtil.RpcRequestProto req) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        long time = System.currentTimeMillis();


        String methodName = req.getMethodName();
        String requestId = req.getRequestId();
        Object[] objs = new Object[1]; //我这里请求封装在request中 所以objes 和type的长度都为1
        objs[0] = req.getRequest();
        Class<?>[] types = new Class<?>[1];
        types[0] = objs[0].getClass();

        Method method = service.getClass().getMethod(methodName, types); //找到后 invoke 反射
        Object obj = method.invoke(service, objs);

        //封装到response中返回
        RpcUtil.RpcResponseProto responseProto = RpcUtil.RpcResponseProto.newBuilder().setResponseCost((System.currentTimeMillis() - time) + "").setResponseId(requestId).setResponseResult(obj.toString()).build();
        return responseProto;


    }


}


