package nanhui.wang.client;

import util.Hello;
import util.RpcUtil;

import java.lang.reflect.Proxy;

public class RpcClientDemo {
    public static void main(String[] args) {
        Hello obj = clientProxy(Hello.class, "127.0.0.1", 8888);


        RpcUtil.Request request = RpcUtil.Request.newBuilder().setRequestPar("Hello Rpc").build();

        System.out.println(obj.say(request));


    }

    public static <T> T clientProxy(final Class<?> interfceClass, final String host, final int port) {
        return (T) Proxy.newProxyInstance(interfceClass.getClassLoader(), new Class[]{interfceClass}, new ClientProxy(host, port));
    }
}
