package nanhui.wang.rpc.registry.nanhui.wang.rpc.register.impl;

import nanhui.wang.rpc.registry.Register;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;

/**
 * 这里面没有写的很复杂 就是简单的使用
 * 使用的是Zkclient
 * 也可以使用apacheCurtor
 */
public class ZookeeperRegister implements Register {
    static final String root = "/nanhui/wang/rpc/";
    private static final String zkHost = "192.168.217.45:2181";

    private ZkClient zkClient;


    public ZookeeperRegister() {

        if (zkClient == null) {


            zkClient = new ZkClient(zkHost);

            zkClient.subscribeChildChanges("/super", new IZkChildListener() {

                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    System.out.println("parentPath: " + parentPath);
                    System.out.println("currentChilds: " + currentChilds);
                }
            });

            //对父节点添加监听子节点变化。
            zkClient.subscribeDataChanges("/super", new IZkDataListener() {

                public void handleDataDeleted(String path) throws Exception {
                    System.out.println("删除的节点为:" + path);
                }

                public void handleDataChange(String path, Object data) throws Exception {
                    System.out.println("变更的节点为:" + path + ", 变更内容为:" + data);
                }
            });


//            zkClient.connect(2000, new Watcher() {
//                public void process(WatchedEvent event) {
//                    Event.EventType type = event.getType();
//                    String path = event.getPath();
//                    Event.KeeperState state = event.getState();
//
//                    switch (state) {
//                        case Expired:
//                            break;
//                        case AuthFailed:
//                            break;
//                        case Disconnected:
//                            break;
//                        case SyncConnected:
//                            break;
//                        case ConnectedReadOnly:
//                            break;
//                        case SaslAuthenticated:
//                            break;
//
//                        default:
//                            break;
//                    }
//
//
//                    switch (type) {
//
//                        case None:
//                            break;
//                        case NodeCreated:
//                            System.out.println(path + "\t注册节点");
//
//
//                            break;
//                        case NodeDeleted:
//                            System.out.println(path + "\t节点删除");
//
//                            break;
//                        case NodeDataChanged:
//                            System.out.println(path + "\t节点数据发生改变");
//
//                            break;
//                        case NodeChildrenChanged:
//                            System.out.println(path + "\t子节点发生改变");
//                            break;
//
//
//                    }
//                }
//            });


        }

    }


    /**
     * 如果存在注册的地址 先删除 再重新注册
     * zkClient 只能一层一层生成一个注册的地址 先注册parent 在注册child
     *
     * @param ip
     * @param port
     * @param serviceName
     * @return
     */
    public boolean register(String ip, int port, String serviceName) {

        String registryPath = root + serviceName;

        boolean exist = zkClient.exists(registryPath);
        if (exist) {
            zkClient.delete(registryPath);
        }

        String[] list = registryPath.split("/");
        String p = "";
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(""))
                continue;
            p += "/" + list[i];
            if (zkClient.exists(p))
                continue;

            zkClient.createPersistent(p);
        }
        //创建临时节点 如果服务器下线 该节点自动删除
        zkClient.createEphemeral(p + "/" + ip + ":" + port);


        return true;


    }

    public List<String> getValidService(String serviceName) {

        String registryPath = root + serviceName;
        boolean exist = zkClient.exists(registryPath);
        if (exist) {

            return zkClient.getChildren(registryPath);


        } else {

            System.out.println("没有可用节点！！！！");
            return null;

        }


    }


}
