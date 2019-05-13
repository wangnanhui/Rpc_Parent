package util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IPLocation {

    public static List<String> loadLocalIPList() {
        List<String> ips = new ArrayList<String>(1);

        Enumeration<NetworkInterface> allNIC = null;
        try {
            allNIC = NetworkInterface.getNetworkInterfaces();
        } catch (Exception ex) {
            System.err.println("获取本机 ip 地址发生错误" + ex.getMessage());
        }

        while (allNIC != null && allNIC.hasMoreElements()) {
            NetworkInterface nic = allNIC.nextElement();
            Enumeration<InetAddress> addresses = nic.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress ip = addresses.nextElement();
                if (ip != null && (ip instanceof Inet4Address) && (!ip.isAnyLocalAddress() && !ip.isLoopbackAddress())) {
                    ips.add(ip.getHostAddress());
                }
            }
        }

        return ips;
    }
}
