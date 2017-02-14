package net.deelam.vertx.rpc;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.BaseInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Based on https://github.com/xored/vertx-typed-rpc
 */
@Slf4j
@RequiredArgsConstructor
public class VertxRpcUtil {
  final EventBus eventBus;
  final String address;

  @Setter
  RpcHook hook;

  private static final String HEADER_METHOD_ID = "method";
  private static final String EXCEPTION = "exception";


  @SuppressWarnings("unchecked")
  public <T> T createClient(Class<T> iface) {
    final KryoSerDe serde = new KryoSerDe();
    return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[] {iface},
        (proxy, method, args) -> {
          try {
            String methodId = genMethodId(method);
            if (hook != null)
              hook.clientSendsCall(methodId, args);
            DeliveryOptions options = new DeliveryOptions().addHeader(HEADER_METHOD_ID, methodId);
            Buffer buffer = null;
            if (method.getParameterTypes().length > 0)
              buffer = serde.writeObjects(args);

            final Class<?> returnType = method.getReturnType();
            if (returnType == void.class) {
              if (method.getName().startsWith("publish"))
                eventBus.publish(address, buffer, options);
              else
                eventBus.send(address, buffer, options);
              return null;
            } else if (returnType.isAssignableFrom(CompletableFuture.class)) {
              final CompletableFuture<Object> resultF = new CompletableFuture<>();
              eventBus.<Buffer>send(address, buffer, options, r -> {
                if (r.failed()) {
                  if (hook != null)
                    hook.clientCallFailed(methodId, r.cause());
                  resultF.completeExceptionally(r.cause());
                } else {
                  Message<Buffer> msg = r.result();
                  if (msg == null) {
                    if (hook != null)
                      hook.clientReceivedVoid(methodId);
                    resultF.complete(null);
                  } else {
                    String exceptionStr = r.result().headers().get(EXCEPTION);
                    Object result = serde.readObject(msg.body());
                    if (exceptionStr != null) {
                      Throwable throwable = (result instanceof Throwable)
                          ? (Throwable) result
                          : new RuntimeException((result == null)
                              ? exceptionStr : result.toString());
                      if (hook != null)
                        hook.clientReceivedThrowable(methodId, throwable);
                      resultF.completeExceptionally(throwable);
                    } else {
                      if (hook != null)
                        hook.clientReceivesResult(methodId, result);
                      resultF.complete(result);
                    }
                  }
                }
              });
              return resultF;
            } else {
              throw new UnsupportedOperationException("Unsupported return type: " + returnType);
            }
          } catch (Throwable e) {
            log.error("Client-side error", e);
            return null;
          }
        });
  }

  public <T> MessageConsumer<Buffer> registerServer(T service) {
    log.debug("Register server: {}", service.getClass().getName());
    HashMap<String, Method> methods = new HashMap<>();
    for (Method method : service.getClass().getDeclaredMethods()) {
      method.setAccessible(true);
      methods.put(genMethodId(method), method);
    }
    final KryoSerDe serde = new KryoSerDe();
    return eventBus.<Buffer>consumer(address, r -> {
      try {
        String methodId = r.headers().get(HEADER_METHOD_ID);
        if (!methods.containsKey(methodId)) {
          log.error("Method not found: {}", methodId);
          r.fail(1, "Method not found: " + methodId);
        } else {
          Method method = methods.get(methodId);
          Object result = null;
          try {
            if (method.getParameterTypes().length == 0) {
              if (hook != null)
                hook.serverReceivesCall(methodId, null);
              result = method.invoke(service);
            } else {
              Object[] objects = serde.readObjects(r.body(), method.getParameterTypes().length);
              if (hook != null)
                hook.serverReceivesCall(methodId, objects);
              result = method.invoke(service, objects);
            }

            if (method.getReturnType().isAssignableFrom(CompletableFuture.class)) {
              ((CompletableFuture<?>) result).whenComplete((reslt, e) -> {
                if (e == null) {
                  if (hook != null)
                    hook.serverReplies(methodId, reslt);
                  r.reply(serde.writeObject(reslt));
                } else {
                  if (hook != null)
                    hook.serverRepliesThrowable(methodId, e);
                  DeliveryOptions options = new DeliveryOptions().addHeader(EXCEPTION, e.toString());
                  r.reply(serde.writeObject(e), options);
                }
              });
            }
          } catch (InvocationTargetException ex) {
            if (hook != null)
              hook.serverRepliesThrowable("invoking " + methodId, ex.getCause());
            DeliveryOptions options = new DeliveryOptions().addHeader(EXCEPTION, ex.getCause().toString());
            r.reply(serde.writeObject(ex.getCause()), options); //r.reply(serde.writeObject(ex.getTargetException().getMessage()));
          }
        }
      } catch (Throwable e) {
        log.error(e.getMessage(), e);
        r.fail(-1, e.getMessage());
      }
    });
  }

  static String genMethodId(Method method) {
 // TODO: 7: make methodId more unique with method.getParameterTypes()?
    return method.getName()+method.getParameterCount(); 
  }

  static final class KryoSerDe {
    Kryo kryo = new Kryo();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public KryoSerDe() {
      kryo.register(Map.class, new MapSerializer() {
        protected Map create(Kryo kryo, Input input, java.lang.Class<Map> type) {
          return new HashMap();
        }
      });
      ((DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
          .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
      kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(
          new BaseInstantiatorStrategy() {
            @Override
            public ObjectInstantiator newInstantiatorOf(Class type) {
              if (type.isInterface() && List.class.isAssignableFrom(type)) {
                return new ObjectInstantiator<List>() {
                  @Override
                  public List newInstance() {
                    return new ArrayList();
                  }
                };
              }
              return new StdInstantiatorStrategy().newInstantiatorOf(type);
            }
          }));
    }

    public synchronized Registration readClass(Input input) {
      try {
        return kryo.readClass(input);
      } catch (Throwable t) {
        log.error("Couldn't read input", t);
        return null;
      }
    }

    public synchronized Object[] readObjects(Buffer buffer, int count) {
      try {
        final Input input = new Input(buffer.getBytes());
        Object[] result = new Object[count];
        for (int i = 0; i < count; i++)
          result[i] = kryo.readClassAndObject(input);
        return result;
      } catch (Throwable t) {
        log.error("Couldn't read buffer", t);
        return null;
      }
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T readObject(Buffer buffer) {
      try {
        return (T) kryo.readClassAndObject(new Input(buffer.getBytes()));
      } catch (Throwable t) {
        log.error("Couldn't read buffer", t);
        return null;
      }
    }

    public synchronized Buffer writeObjects(Object[] objs) {
      try {
        final Output output = new Output(new ByteArrayOutputStream());
        for (int i = 0; i < objs.length; i++)
          kryo.writeClassAndObject(output, objs[i]);
        return Buffer.buffer(output.toBytes());
      } catch (Throwable t) {
        String arrayStr = Arrays.toString(objs);
        log.error("Couldn't write object of type={}; serializing as string instead: {}", objs.getClass(), arrayStr);
        final Output output = new Output(new ByteArrayOutputStream());
        kryo.writeClassAndObject(output, arrayStr);
        return Buffer.buffer(output.toBytes());
      }
    }

    public synchronized Buffer writeObject(Object obj) {
      try {
        final Output output = new Output(new ByteArrayOutputStream());
        log.debug("writeObject: " + obj);
        kryo.writeClassAndObject(output, obj);
        return Buffer.buffer(output.toBytes());
      } catch (Throwable t) {
        try{
          //log.error("t",t);
          return writeObject(Json.encode(obj));          
        } catch (Throwable t2) {
          // TODO: 3: determine appropriate kryo serializer
          //log.error("t2",t2);
          String objStr = obj.toString();
          log.error("Couldn't write object of {}; serializing as string instead: {}", obj.getClass(), objStr);
          return writeObject(objStr);
        }
      }
    }
  }

  public static interface RpcHook {

    default void clientSendsCall(String methodId, Object[] args) {}

    default void serverReceivesCall(String methodId, Object[] objects) {}

    default void serverReplies(String methodId, Object result) {}

    default void clientReceivesResult(String methodId, Object result) {}

    default void clientReceivedVoid(String methodId) {}

    default void serverRepliesThrowable(String methodId, Throwable e) {}

    default void clientReceivedThrowable(String methodId, Throwable e) {}

    default void clientCallFailed(String methodId, Throwable cause) {}

  }

  @Slf4j
  public static class DebugRpcHook implements RpcHook {
    public void clientSendsCall(String methodId, Object[] args) {
      log.debug("clientSendsCall: {}: {}", methodId, Arrays.toString(args));
    }

    public void serverReceivesCall(String methodId, Object[] args) {
      log.debug("serverReceivesCall: {}: {}", methodId, Arrays.toString(args));
    }

    public void serverReplies(String methodId, Object result) {
      log.debug("serverReplies: {}: {}", methodId, result);
    }

    public void clientReceivesResult(String methodId, Object result) {
      log.debug("clientReceivesResult: {}: {}", methodId, result);
    }

    public void clientReceivedVoid(String methodId) {
      log.debug("clientReceivedVoid: {}", methodId);
    }

    public void serverRepliesThrowable(String methodId, Throwable e) {
      log.debug("serverRepliesThrowable: {}: {}", methodId, (e == null) ? e : e.getMessage());
    }

    public void clientReceivedThrowable(String methodId, Throwable e) {
      log.debug("clientReceivedThrowable: {}: {}", methodId, (e == null) ? e : e.getMessage());
    }

    public void clientCallFailed(String methodId, Throwable e) {
      log.debug("clientCallFailed: {}: {}", methodId, (e == null) ? e : e.getMessage());
    }
  }
}
