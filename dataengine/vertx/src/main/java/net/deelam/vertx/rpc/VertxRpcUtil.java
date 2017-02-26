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
    log.debug("Creating RPC client for {} at {} ", iface.getSimpleName(), address);
    final KryoSerDe serde = new KryoSerDe(address+"-client");
    return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[] {iface},
        (proxy, method, args) -> {
          if ("toString".equals(method.getName())) {
            return (T) "RPC client proxy for " + iface.getSimpleName();
          }
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
              throw new UnsupportedOperationException(
                  "Unsupported method '" + method + "' with return type=" + returnType);
            }
          } catch (Throwable e) {
            log.error("Client-side error", e);
            return null;
          }
        });
  }

  public <T> MessageConsumer<Buffer> registerServer(T service) {
    log.debug("Registering RPC server at {}: {}", address, service.getClass().getName());
    HashMap<String, Method> methods = new HashMap<>();
    for (Method method : service.getClass().getDeclaredMethods()) {
      method.setAccessible(true);
      methods.put(genMethodId(method), method);
    }
    CompletableFuture<MessageConsumer<Buffer>> msgConsumerF=new CompletableFuture<>();
    //new Thread(()->{
    final KryoSerDe serde = new KryoSerDe(address+"-service");
    MessageConsumer<Buffer> msgConsumer = eventBus.<Buffer>consumer(address, r -> {
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
              Object[] args = serde.readObjects(r.body(), method.getParameterTypes().length);
              if (hook != null)
                hook.serverReceivesCall(methodId, args);
              result = method.invoke(service, args);
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
            Throwable e = (ex.getCause() == null) ? ex : ex.getCause();
            //checkNotNull(e, ex.getCause());
            if (hook != null)
              hook.serverRepliesThrowable("invoking " + methodId, e);
            DeliveryOptions options = new DeliveryOptions().addHeader(EXCEPTION, e.toString());
            r.reply(serde.writeObject(e), options); //r.reply(serde.writeObject(ex.getTargetException().getMessage()));
          } catch (IllegalArgumentException|IllegalAccessException e){
            if (hook != null)
              hook.serverRepliesThrowable("invoking " + methodId, e);
            DeliveryOptions options = new DeliveryOptions().addHeader(EXCEPTION, e.toString());
            r.reply(serde.writeObject(e), options); //r.reply(serde.writeObject(ex.getTargetException().getMessage()));
          }
        }
      } catch (Throwable e) {
        log.error(e.getMessage(), e);
        r.fail(-1, e.getMessage());
      }
    });
    msgConsumerF.complete(msgConsumer);
    //}, "VertxRpcUtil-service-thread:"+address+":"+service.getClass().getName()).start();
    return msgConsumerF.join();
  }

  static String genMethodId(Method method) {
    // TODO: 1: make methodId more unique with method.getParameterTypes()?
    return method.getName(); // Doesn't work with varargs: method.getParameterCount();
  }

  /**
   * Reminder: Do NOT use varargs as parameters to RPC methods.
   * Doing so causes Kyro to fail, due to wrong parameter count, due to wrong method selection.
   */
  static final class KryoSerDe {
    Kryo kryo;
    final String name;

    public KryoSerDe(String name) {
      this.name=name;
      kryo=newKryo();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Kryo newKryo() {
      Kryo kryo = new Kryo();
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
              if (type.isInterface())
                if (List.class.isAssignableFrom(type)) {
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
      log.debug("Created new Kryo for {}: {}", name, kryo);
      return kryo;
    }

    public synchronized Registration readClass(Input input) {
      try {
        //log.debug("Using kryo={} to read for {}", kryo, name);
        return kryo.readClass(input);
      } catch (Throwable t) {
        log.error("Couldn't read input", t);
        throw t; //return null;
      }
    }

    public synchronized Object[] readObjects(Buffer buffer, int count) {
      try {
        //log.debug("Using kryo={} to read for {}", kryo, name);
        final Input input = new Input(buffer.getBytes());
        Object[] result = new Object[count];
        for (int i = 0; i < count; i++)
          result[i] = kryo.readClassAndObject(input);
        return result;
      } catch (Throwable t) {
        log.error("Couldn't read buffer", t);
        throw t;
      }
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T readObject(Buffer buffer) {
      try {
        //log.debug("Using kryo={} to read for {}", kryo, name);
        return (T) kryo.readClassAndObject(new Input(buffer.getBytes()));
      } catch (Throwable t) {
        log.error("Couldn't read buffer", t);
        throw t; //return null;
      }
    }

    public synchronized Buffer writeObjects(Object[] objs) {
      try {
        //log.debug("Using kryo={} to write for {}", kryo, name);
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
        //log.debug("Using kryo={} to write for {}", kryo, name);
        final Output output = new Output(new ByteArrayOutputStream());
        //log.debug("writeObject: " + obj);
        kryo.writeClassAndObject(output, obj);

//        Object debugCheck=kryo.readClassAndObject(new Input(output.toBytes()));
//        log.debug("debugCheck: " + debugCheck);

        return Buffer.buffer(output.toBytes());
      } catch (Throwable t) {
        try {
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
  @RequiredArgsConstructor
  public static class DebugRpcHook implements RpcHook {
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
}
