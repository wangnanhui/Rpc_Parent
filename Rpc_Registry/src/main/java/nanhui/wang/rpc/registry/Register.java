package nanhui.wang.rpc.registry;

import java.util.List;

public interface Register {

    boolean register(String ip, int port, String serviceName); //检测注册是否成功

    List<String> getValidService(String serviceName);//获取可用的服务


}
