package dataengine.apis;

import java.util.function.Supplier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
@RequiredArgsConstructor
public class RpcClientProvider<T> {
  final Supplier<T> supplier;

  @Getter(lazy=true, onMethod = @__({@SuppressWarnings("unchecked")}))
  private final T rpc = lazyCreateRpcClient();

  private T lazyCreateRpcClient() {
    log.info("Getting RPC client for {}", supplier);
    T t = supplier.get();
    log.info("  Created RPC client for {}", t);
    return t;
  }

}
