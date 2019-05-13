package util;

@RpcService(name = "Hello")
public class HelloImpl implements Hello {
    public String say(RpcUtil.Request t) {
        return t.getRequestPar() + " : hello RPC ";

    }
}
