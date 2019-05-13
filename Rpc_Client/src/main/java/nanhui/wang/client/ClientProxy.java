package nanhui.wang.client;


import nanhui.wang.rpc.registry.ZkClientUtil;
import util.RpcService;
import util.RpcUtil;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class ClientProxy implements InvocationHandler {

    private String host;
    private int port;

    public ClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }


    /**
     * 重写Invoke方法 加入自己要处理的业务逻辑
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws IOException
     */

    public Object invoke(Object proxy, Method method, Object[] args) throws IOException {
        //封装request 然后发送到Server端
        RpcUtil.RpcRequestProto.Builder requestProto = RpcUtil.RpcRequestProto.newBuilder();
        requestProto.setClassName(method.getDeclaringClass().getName());
        requestProto.setMethodName(method.getName());
        requestProto.setRequestId(UUID.randomUUID().toString());
        requestProto.setRequest((RpcUtil.Request) args[0]);

        RpcService rpc = method.getDeclaringClass().getAnnotation(RpcService.class);


        String address = getServiceAddress(rpc.name());//注册中心用了zookeeper 通过Service的名称去查找 Server端是否已经注册了此服务

        String[] adds = address.split(":"); //获取注册此服务的服务器所在地址

        RpcClient client = new RpcClient(adds[0], Integer.parseInt(adds[1]));
        RpcUtil.RpcResponseProto responseProto = client.connect(requestProto.build());//封装后通过RpcClient发送请求到Server端


        return responseProto.getResponseResult();
    }

    public String getServiceAddress(String serviceName) {

        List<String> address = ZkClientUtil.getAvilied(serviceName);

        if (address == null) {
            return null;
        }
        return address.get(0).replace("/", ".");

    }


}
