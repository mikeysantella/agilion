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

  @Getter(lazy = true)
  private final T rpc = lazyCreateRpcClient();

  private T lazyCreateRpcClient() {
    log.info("-- initializing instance using " + supplier);
    return supplier.get();
  }

}
