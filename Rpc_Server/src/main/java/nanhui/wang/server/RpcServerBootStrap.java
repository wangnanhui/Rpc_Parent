package nanhui.wang.server;

import util.Hello;
import util.HelloImpl;
import util.IPLocation;

import java.util.List;

public class RpcServerBootStrap {

    public static void main(String[] args) {
        List<String> ips = IPLocation.loadLocalIPList(); //ip地址
        int port = 8888;

        for (String ip : ips) {
            Hello hello = new HelloImpl();
            RpcServer server = new RpcServer(port, ip);

            bindService(hello, server);

        }


    }


    public static <T> void bindService(T service, RpcServer server) {
        server.bind(service);


    }

}
