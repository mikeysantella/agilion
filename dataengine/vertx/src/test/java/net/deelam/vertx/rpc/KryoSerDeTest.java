package net.deelam.vertx.rpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.CompletionException;

import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;

import io.vertx.core.buffer.Buffer;
import net.deelam.vertx.rpc.VertxRpcUtil.KryoSerDe;

public class KryoSerDeTest {

  KryoSerDe kryo=new KryoSerDe();
  
  @Before
  public void setUp() throws Exception {
    
  }

  @Test
  public void testWriteObject() {
    CompletionException except = new CompletionException(
        (new IllegalArgumentException("blah ")));
    Buffer buf = kryo.writeObject(except);
    Registration c = kryo.readClass(new Input(buf.getBytes()));
    Throwable ex = kryo.readObject(buf);
    System.out.println(buf.getBytes().length+" "+c+", "+ex.getClass()+", "+ex);
  }
  
  @Test
  public void testMethodId(){
    HashMap<String, Method> methods = new HashMap<>();
    for (Method method : KryoSerDe.class.getDeclaredMethods()) {
      method.setAccessible(true);
      methods.put(VertxRpcUtil.genMethodId(method), method);
    }
    System.out.println(methods.toString().replaceAll(", ","\n"));
  }

}
