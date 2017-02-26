package net.deelam.vertx.rpc;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.rpc.VertxRpcUtil.RpcHook;

@Slf4j
@RequiredArgsConstructor
public class DebugRpcHook implements RpcHook {
  final String iface;

  public void clientSendsCall(String methodId, Object[] args) {
    log.debug("clientSendsCall to {}: {}: {}", iface, methodId, Arrays.toString(args));
  }

  public void serverReceivesCall(String methodId, Object[] args) {
    log.debug("{} serverReceivesCall: {}: {}", iface, methodId, Arrays.toString(args));
  }

  public void serverReplies(String methodId, Object result) {
    log.debug("{} serverReplies: {}: {}", iface, methodId, result);
  }

  public void clientReceivesResult(String methodId, Object result) {
    log.debug("clientReceivesResult from {}: {}: {}", iface, methodId, result);
  }

  public void clientReceivedVoid(String methodId) {
    log.debug("clientReceivedVoid from {}: {}", iface, methodId);
  }

  public void serverRepliesThrowable(String methodId, Throwable e) {
    log.debug(iface+" serverRepliesThrowable: "+methodId+": "+ e);
    //log.debug("{} serverRepliesThrowable: {}: {}", iface, methodId, (e == null) ? e : e.toString()+" msg="+e.getMessage()); // in case getMessage()==null
  }

  public void clientReceivedThrowable(String methodId, Throwable e) {
    log.debug("clientReceivedThrowable from {}: {}: {}", iface, methodId, (e == null) ? e : e.toString()+" msg="+e.getMessage());
  }

  public void clientCallFailed(String methodId, Throwable e) {
    log.debug("clientCallFailed to {}: {}: {}", iface, methodId, (e == null) ? e : e.getMessage());
  }
}