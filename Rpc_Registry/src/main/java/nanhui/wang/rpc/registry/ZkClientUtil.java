package nanhui.wang.rpc.registry;

import nanhui.wang.rpc.registry.nanhui.wang.rpc.register.impl.ZookeeperRegister;

import java.util.List;

public class ZkClientUtil {

    static Register register = new ZookeeperRegister();


    public static boolean register(String ip, int port, String serviceName) {

        return register.register(ip, port, serviceName);

    }

    public static List<String> getAvilied(String serviceName) {
        return register.getValidService(serviceName);
    }


}
