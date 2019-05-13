package util;

@RpcService(name = "Hello")
public interface Hello {

    String say(RpcUtil.Request request);

}
